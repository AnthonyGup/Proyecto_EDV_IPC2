import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { Videogame, Image } from '../../models';
import { of, forkJoin } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface GameWithImage extends Videogame { imageUrl?: string }

@Component({
  standalone: true,
  selector: 'app-banner-admin',
  templateUrl: './banner-admin.component.html',
  styleUrls: ['./banner-admin.component.css'],
  imports: [CommonModule, FormsModule, RouterModule]
})
export class BannerAdminComponent implements OnInit {
  videogames: GameWithImage[] = [];
  selectedGame: Videogame | null = null;
  images: Image[] = [];
  loadingGames = true;
  loadingImages = false;
  error: string | null = null;
  successMessage: string | null = null;
  successMessageExiting = false;
  private successTimeout: any;
  selecting: Set<number> = new Set<number>();
  updating = false;

  constructor(private vgService: VideogameService) {}

  ngOnInit(): void {
    this.loadGames();
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

  loadGames(): void {
    this.loadingGames = true;
    this.vgService.getAll().subscribe({
      next: (games) => {
        this.videogames = games.map(g => ({ ...g }));
        this.loadImagesForGames();
        this.loadingGames = false;
      },
      error: (err) => {
        console.error('Error al cargar videojuegos', err);
        this.error = 'Error al cargar videojuegos';
        this.loadingGames = false;
      }
    });
  }

  private loadImagesForGames(): void {
    const imageRequests = this.videogames.map(game =>
      this.vgService.getGameImages(game.videogameId).pipe(
        catchError(() => of([] as Image[]))
      )
    );

    if (imageRequests.length === 0) {
      return;
    }

    forkJoin(imageRequests).subscribe({
      next: (imagesArrays) => {
        this.videogames.forEach((game, index) => {
          const images = imagesArrays[index];
          if (images && images.length > 0 && images[0].image) {
            game.imageUrl = `data:image/png;base64,${images[0].image}`;
          }
        });
      },
      error: (err) => {
        console.error('Error al cargar imágenes de juegos', err);
      }
    });
  }

  selectGame(game: Videogame): void {
    if (this.updating) return;
    this.selectedGame = game;
    this.images = [];
    this.selecting.clear();
    this.loadImagesForGame(game.videogameId);
  }

  loadImagesForGame(videogameId: number): void {
    this.loadingImages = true;
    this.vgService.getGameImages(videogameId).pipe(catchError(() => of([] as Image[])))
      .subscribe({
        next: (imgs) => {
          this.images = imgs || [];
          this.loadingImages = false;
        },
        error: (err) => {
          console.error('Error al cargar imágenes', err);
          this.error = 'Error al cargar imágenes';
          this.loadingImages = false;
        }
      });
  }

  toggleSelect(image: Image): void {
    // Deprecated: selection is now via checkbox; kept for compatibility if needed
    if (!image.imageId) return;
    const id = image.imageId;
    if (this.selecting.has(id)) this.selecting.delete(id);
    else this.selecting.add(id);
  }

  onSelectCheckbox(image: Image, event: Event): void {
    const id = image.imageId;
    if (!id) return;
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) this.selecting.add(id);
    else this.selecting.delete(id);
  }

  addSelectedToBanner(): void {
    if (this.selecting.size === 0 || !this.selectedGame) return;
    this.updating = true;
    const ids = Array.from(this.selecting);
    this.vgService.updateImagesBaner(ids, true).subscribe({
      next: (res) => {
        this.showSuccessMessage('Imágenes agregadas al banner');
        // reflect local state
        this.images = this.images.map(img => ids.includes(img.imageId) ? { ...img, baner: true } : img);
        this.selecting.clear();
        this.updating = false;
      },
      error: (err) => {
        console.error('Error al agregar al banner', err);
        this.error = 'Error al actualizar banner';
        this.updating = false;
      }
    });
  }

  removeFromBanner(image: Image): void {
    if (!image.imageId) return;
    this.updating = true;
    this.vgService.updateImagesBaner([image.imageId], false).subscribe({
      next: () => {
        image.baner = false;
        this.showSuccessMessage('Imagen removida del banner');
        this.updating = false;
      },
      error: (err) => {
        console.error('Error al remover del banner', err);
        this.error = 'Error al actualizar banner';
        this.updating = false;
      }
    });
  }

  isSelected(image: Image): boolean {
    return !!image.imageId && this.selecting.has(image.imageId);
  }

  getImageUrl(image: Image): string {
    if (image.image) return `data:image/png;base64,${image.image}`;
    return this.getPlaceholder(300, 200, 'No Image');
  }

  getGameImageUrl(game: GameWithImage): string {
    if (game.imageUrl) return game.imageUrl;
    return this.getPlaceholder(250, 350, 'Sin Imagen');
  }

  private getPlaceholder(width: number, height: number, text: string): string {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><rect fill="#374151" width="${width}" height="${height}"/><text x="50%" y="50%" font-size="14" fill="#9CA3AF" text-anchor="middle" dominant-baseline="middle">${text}</text></svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
  }
}
