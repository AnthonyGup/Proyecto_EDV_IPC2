import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { Image, Videogame } from '../../models';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { forkJoin } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-banner-carousel',
  templateUrl: './banner-carousel.component.html',
  styleUrls: ['./banner-carousel.component.css'],
  imports: [CommonModule, RouterModule]
})
export class BannerCarouselComponent implements OnInit {
  bannerImages: Array<{ image: Image; gameName: string; gameId?: number }> = [];
  currentIndex = 0;
  loading = true;
  error: string | null = null;

  constructor(private vgService: VideogameService, private router: Router) {}

  ngOnInit(): void {
    this.loadBannerImages();
  }

  private loadBannerImages(): void {
    this.vgService.getAll().pipe(catchError(() => of([] as Videogame[]))).subscribe({
      next: (games) => {
        if (!games || games.length === 0) {
          this.loading = false;
          return;
        }

        // Cargar imágenes de todos los juegos
        const imageRequests = games.map(game =>
          this.vgService.getGameImages(game.videogameId).pipe(
            catchError(() => of([] as Image[]))
          )
        );

        forkJoin(imageRequests).subscribe({
          next: (imagesArrays) => {
            // Filtrar solo imágenes con baner = true y asociar con nombre del juego
            this.bannerImages = imagesArrays
              .flatMap((images, index) =>
                images
                  .filter(img => img.baner === true)
                  .map(img => ({
                    image: img,
                    gameName: games[index].name || 'Juego desconocido',
                    gameId: games[index].videogameId
                  }))
              );
            
            this.loading = false;
          },
          error: (err) => {
            console.error('Error al cargar imágenes del banner', err);
            this.error = 'Error al cargar el banner';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error al cargar juegos', err);
        this.error = 'Error al cargar los juegos';
        this.loading = false;
      }
    });
  }

  getCurrentGameName(): string {
    if (this.bannerImages.length > 0 && this.currentIndex < this.bannerImages.length) {
      return this.bannerImages[this.currentIndex].gameName;
    }
    return '';
  }

  getImageUrl(image: Image): string {
    if (image.image) return `data:image/png;base64,${image.image}`;
    return this.getPlaceholder(1200, 400, 'Sin Imagen');
  }

  private getPlaceholder(width: number, height: number, text: string): string {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><defs><linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#1f2937;stop-opacity:1" /><stop offset="100%" style="stop-color:#111827;stop-opacity:1" /></linearGradient></defs><rect fill="url(#grad)" width="${width}" height="${height}"/><text x="50%" y="50%" font-size="24" fill="#6B7280" text-anchor="middle" dominant-baseline="middle">${text}</text></svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
  }

  previousSlide(): void {
    if (this.bannerImages.length > 0) {
      this.currentIndex = (this.currentIndex - 1 + this.bannerImages.length) % this.bannerImages.length;
    }
  }

  nextSlide(): void {
    if (this.bannerImages.length > 0) {
      this.currentIndex = (this.currentIndex + 1) % this.bannerImages.length;
    }
  }

  goToSlide(index: number): void {
    if (index >= 0 && index < this.bannerImages.length) {
      this.currentIndex = index;
    }
  }
 
  goToGameDetail(): void {
    if (this.bannerImages.length > 0 && this.currentIndex < this.bannerImages.length) {
      const gameId = this.bannerImages[this.currentIndex].gameId;
      if (gameId) {
        this.router.navigate(['/game', gameId]);
      }
    }
  }
}
