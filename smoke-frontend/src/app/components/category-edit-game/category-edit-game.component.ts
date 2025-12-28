import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { CategoryService } from '../../services/category.service';
import { Category, Videogame } from '../../models';

@Component({
  standalone: true,
  selector: 'app-category-edit-game',
  templateUrl: './category-edit-game.component.html',
  styleUrls: ['./category-edit-game.component.css'],
  imports: [CommonModule]
})
export class CategoryEditGameComponent implements OnInit {
  game: Videogame | null = null;
  allCategories: Category[] = [];
  selectedCategoryIds: Set<number> = new Set<number>();
  
  loading = false;
  saving = false;
  error = '';
  successMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private videogameService: VideogameService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const gameId = params['id'];
      if (gameId) {
        this.loadData(gameId);
      }
    });
  }

  loadData(gameId: number): void {
    this.loading = true;
    this.error = '';

    // Cargar todas las categorías
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.allCategories = cats || [];
        
        // Cargar categorías del juego
        this.videogameService.getCategoriesForGame(gameId).subscribe({
          next: (gameCats) => {
            this.selectedCategoryIds = new Set(gameCats.map(c => c.categoryId));
            this.loading = false;
          },
          error: () => {
            this.selectedCategoryIds = new Set();
            this.loading = false;
          }
        });
      },
      error: () => {
        this.error = 'Error cargando categorías';
        this.loading = false;
      }
    });
  }

  toggleCategory(categoryId: number, checked: boolean): void {
    if (checked) {
      this.selectedCategoryIds.add(categoryId);
    } else {
      this.selectedCategoryIds.delete(categoryId);
    }
  }

  save(): void {
    const gameId = this.route.snapshot.params['id'];
    if (!gameId) return;

    this.saving = true;
    this.error = '';
    this.successMessage = '';

    const payload = {
      gameId: parseInt(gameId),
      categoryIds: Array.from(this.selectedCategoryIds)
    };

    this.videogameService.updateGameCategories(payload).subscribe({
      next: () => {
        this.successMessage = 'Categorías actualizadas exitosamente';
        this.saving = false;
        setTimeout(() => this.router.navigate(['/admin/category/admin']), 2000);
      },
      error: (err) => {
        this.error = err?.error?.error || 'Error al actualizar categorías';
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/category/admin']);
  }
}
