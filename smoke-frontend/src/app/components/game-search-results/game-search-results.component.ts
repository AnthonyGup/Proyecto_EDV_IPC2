import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { Videogame, Image } from '../../models';

@Component({
  standalone: true,
  selector: 'app-game-search-results',
  templateUrl: './game-search-results.component.html',
  styleUrls: ['./game-search-results.component.css'],
  imports: [CommonModule, RouterModule]
})
export class GameSearchResultsComponent implements OnInit {
  results: Videogame[] = [];
  gameImages: Map<number, string> = new Map();
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private videogameService: VideogameService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.search(params);
    });
  }

  search(params: any): void {
    this.loading = true;
    this.error = '';

    const filters: any = {
      name: params['name'],
      available: params['available'] === 'true' ? true : params['available'] === 'false' ? false : undefined,
      categoryId: params['categoryId'] ? +params['categoryId'] : undefined,
      minPrice: params['minPrice'] ? +params['minPrice'] : undefined,
      maxPrice: params['maxPrice'] ? +params['maxPrice'] : undefined,
      maxAge: params['maxAge'] ? +params['maxAge'] : undefined
    };

    // Remove undefined
    Object.keys(filters).forEach(key => filters[key] === undefined && delete filters[key]);

    this.videogameService.searchGames(filters).subscribe({
      next: (games: Videogame[]) => {
        this.results = games;
        this.loading = false;
        this.loadImages();
      },
      error: (err: any) => {
        this.error = 'Error al buscar juegos';
        this.loading = false;
        console.error('Search error:', err);
      }
    });
  }

  loadImages(): void {
    this.results.forEach(game => {
      this.videogameService.getGameImages(game.videogameId).subscribe({
        next: (images: Image[]) => {
          if (images && images.length > 0 && images[0].image) {
            this.gameImages.set(game.videogameId, images[0].image);
          }
        },
        error: (err: any) => {
          console.error(`Error loading images for game ${game.videogameId}:`, err);
        }
      });
    });
  }

  getGameImage(videogameId: number): string | undefined {
    return this.gameImages.get(videogameId);
  }
}
