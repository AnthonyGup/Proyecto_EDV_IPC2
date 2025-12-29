import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { Videogame, Image } from '../../models';
import { forkJoin } from 'rxjs';
import { catchError, of } from 'rxjs';

interface GameWithImage {
  game: Videogame;
  imageUrl: string;
}

@Component({
  standalone: true,
  selector: 'app-top-games-list',
  templateUrl: './top-games-list.component.html',
  styleUrls: ['./top-games-list.component.css'],
  imports: [CommonModule, RouterModule]
})
export class TopGamesListComponent implements OnInit {
  topGames: GameWithImage[] = [];
  loading = true;
  error: string | null = null;

  constructor(private videogameService: VideogameService) {}

  ngOnInit() {
    this.loadTopGames();
  }

  loadTopGames() {
    this.loading = true;
    this.error = null;
    
    this.videogameService.getTopGames().subscribe({
      next: (games) => {
        // Cargar imágenes para cada juego
        const imageRequests = games.map(game =>
          this.videogameService.getGameImages(game.videogameId).pipe(
            catchError(() => of([]))
          ).toPromise().then(images => ({
            game,
            imageUrl: this.getImageUrl(images || [])
          }))
        );

        Promise.all(imageRequests).then(gamesWithImages => {
          this.topGames = gamesWithImages;
          this.loading = false;
        }).catch(err => {
          console.error('Error loading images:', err);
          this.topGames = games.map(game => ({
            game,
            imageUrl: this.generatePlaceholder(game.name)
          }));
          this.loading = false;
        });
      },
      error: (err) => {
        console.error('Error loading top games:', err);
        this.error = 'Error al cargar los juegos destacados';
        this.loading = false;
      }
    });
  }

  private getImageUrl(images: Image[]): string {
    if (images.length > 0) {
      return `data:image/png;base64,${images[0].image}`;
    }
    return this.generatePlaceholder('Game');
  }

  private generatePlaceholder(gameName: string): string {
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8'];
    const colorIndex = gameName.length % colors.length;
    const initials = gameName
      .split(' ')
      .map(word => word.charAt(0).toUpperCase())
      .join('')
      .slice(0, 2);

    const svg = `
      <svg width="150" height="150" xmlns="http://www.w3.org/2000/svg">
        <rect width="150" height="150" fill="${colors[colorIndex]}"/>
        <text x="50%" y="50%" font-size="48" font-weight="bold" fill="white" 
              text-anchor="middle" dominant-baseline="middle">
          ${initials}
        </text>
      </svg>
    `;
    return 'data:image/svg+xml;base64,' + btoa(svg);
  }
}
