import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { CategoryService } from '../../services/category.service';
import { Videogame, Image } from '../../models';
import { Category } from '../../models/Category';

@Component({
  standalone: true,
  selector: 'app-videogame-edit',
  templateUrl: './videogame-edit.component.html',
  styleUrls: ['./videogame-edit.component.css'],
  imports: [CommonModule, FormsModule]
})
export class VideogameEditComponent implements OnInit {
  gameId!: number;
  game: Videogame | null = null;
  loading = true;
  error: string | null = null;
  images: Image[] = [];
  uploading = false;
  saving = false;
  successMessage: string | null = null;
  successMessageExiting: boolean = false;
  private successTimeout: any;

  allCategories: Category[] = [];
  selectedCategoryIds: number[] = [];

  constructor(
    private route: ActivatedRoute,
    private videogameService: VideogameService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.gameId = Number(this.route.snapshot.paramMap.get('id'));
    this.videogameService.getAll().subscribe({
      next: (games) => {
        this.game = games.find(g => g.videogameId === this.gameId) || null;
        if (this.game) {
          this.videogameService.getGameImages(this.game.videogameId).subscribe({
            next: imgs => {
              this.images = imgs || [];
              this.loading = false;
            },
            error: () => {
              this.images = [];
              this.loading = false;
            }
          });

          // cargar categorías disponibles y seleccionadas del juego
          this.categoryService.getAll().subscribe({
            next: cats => {
              this.allCategories = cats || [];
              this.videogameService.getCategoriesForGame(this.game!.videogameId).subscribe({
                next: gameCats => {
                  this.selectedCategoryIds = (gameCats || []).map(c => (c as any).categoryId || (c as any).id || c.categoryId);
                },
                error: () => {
                  this.selectedCategoryIds = [];
                }
              });
            },
            error: () => {
              this.allCategories = [];
            }
          });
        } else {
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Error al cargar juego:', err);
        this.error = 'Error al cargar el juego';
        this.loading = false;
      }
    });
  }

  save(): void {
    if (!this.game) return;
    this.saving = true;
    this.videogameService.updateGame({
      videogameId: this.game.videogameId,
      name: this.game.name,
      price: this.game.price,
      description: this.game.description
    }).subscribe({
      next: () => {
        this.showSuccessMessage('Juego actualizado correctamente');
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al guardar el juego';
        this.saving = false;
      }
    });
  }

  toggleAvailability(): void {
    if (!this.game) return;
    const newVal = !this.game.available;
    this.videogameService.setGameAvailability(this.game.videogameId, newVal).subscribe({
      next: () => {
        this.game!.available = newVal;
        this.showSuccessMessage(newVal ? 'Juego disponible para la venta' : 'Juego desactivado de la venta');
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al cambiar disponibilidad';
      }
    });
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !this.game) return;
    const files = Array.from(input.files);
    this.uploading = true;
    this.videogameService.uploadImages(this.game.videogameId, files).subscribe({
      next: () => {
        this.showSuccessMessage('Imágenes subidas correctamente');
        // recargar imágenes
        this.videogameService.getGameImages(this.game!.videogameId).subscribe(imgs => this.images = imgs || []);
        this.uploading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al subir imágenes';
        this.uploading = false;
      }
    });
  }

  removeImage(img: Image): void {
    if (!img?.imageId) return;
    this.videogameService.deleteImage(img.imageId).subscribe({
      next: () => {
        this.images = this.images.filter(i => i.imageId !== img.imageId);
        this.showSuccessMessage('Imagen eliminada');
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al eliminar imagen';
      }
    });
  }

  isCategorySelected(id: number): boolean {
    return this.selectedCategoryIds.includes(id);
  }

  toggleCategory(id: number): void {
    if (this.isCategorySelected(id)) {
      this.selectedCategoryIds = this.selectedCategoryIds.filter(cid => cid !== id);
    } else {
      this.selectedCategoryIds = [...this.selectedCategoryIds, id];
    }
  }

  saveCategories(): void {
    if (!this.game) return;
    this.videogameService.updateGameCategories({
      gameId: this.game.videogameId,
      categoryIds: this.selectedCategoryIds
    }).subscribe({
      next: () => {
        this.showSuccessMessage('Categorías actualizadas');
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al actualizar categorías';
      }
    });
  }

  private showSuccessMessage(message: string): void {
    this.successMessage = message;
    this.successMessageExiting = false;
    if (this.successTimeout) {
      clearTimeout(this.successTimeout);
    }
    this.successTimeout = setTimeout(() => {
      this.successMessageExiting = true;
      setTimeout(() => {
        this.successMessage = null;
        this.successMessageExiting = false;
      }, 200);
    }, 800);
  }
}
