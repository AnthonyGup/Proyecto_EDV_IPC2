import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { SessionService } from '../../core/session.service';
import { Videogame, Image } from '../../models';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface GameWithImage extends Videogame {
  imageUrl?: string;
}

@Component({
  standalone: true,
  selector: 'app-company-games-dashboard',
  templateUrl: './company-games-dashboard.component.html',
  styleUrls: ['./company-games-dashboard.component.css'],
  imports: [CommonModule, RouterModule]
})
export class CompanyGamesDashboardComponent implements OnInit {
  games: GameWithImage[] = [];
  loading = true;
  error: string | null = null;
  user: any = null;

  constructor(
    private videogameService: VideogameService,
    private session: SessionService,
    private router: Router
  ) {
    this.user = this.session.getUser();
  }

  ngOnInit(): void {
    this.loadGames();
  }

  private loadGames(): void {
    this.videogameService.getAll().subscribe({
      next: (games) => {
        // Filtrar por la empresa del usuario cuando sea posible
        const companyId = this.user?.company_id ?? this.user?.companyId;
        const filtered = companyId ? games.filter(g => g.companyId === companyId) : games;
        this.games = filtered;
        this.loadImagesForGames();
      },
      error: (err) => {
        console.error('Error al cargar videojuegos:', err);
        this.error = 'Error al cargar los videojuegos';
        this.loading = false;
      }
    });
  }

  private loadImagesForGames(): void {
    const imageRequests = this.games.map(game =>
      this.videogameService.getGameImages(game.videogameId).pipe(
        catchError(() => of([] as Image[]))
      )
    );

    if (imageRequests.length === 0) {
      this.loading = false;
      return;
    }

    forkJoin(imageRequests).subscribe({
      next: (imagesArrays) => {
        this.games.forEach((game, index) => {
          const images = imagesArrays[index];
          if (images && images.length > 0 && images[0].image) {
            game.imageUrl = `data:image/png;base64,${images[0].image}`;
          }
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar imágenes:', err);
        this.loading = false;
      }
    });
  }

  getImageUrl(game: GameWithImage): string {
    return game.imageUrl || 'https://via.placeholder.com/250x350?text=No+Image';
  }

  editGame(game: GameWithImage): void {
    this.router.navigate(['/company/admin/game/edit', game.videogameId]);
  }
}
