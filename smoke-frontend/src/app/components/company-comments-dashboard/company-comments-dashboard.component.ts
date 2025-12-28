import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { CommentService } from '../../services/comment.service';
import { SessionService } from '../../core/session.service';
import { Videogame, Image } from '../../models';
import { forkJoin } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

interface GameWithImage extends Videogame {
  imageUrl?: string;
  updatingComments?: boolean;
}

@Component({
  standalone: true,
  selector: 'app-company-comments-dashboard',
  templateUrl: './company-comments-dashboard.component.html',
  styleUrls: ['./company-comments-dashboard.component.css'],
  imports: [CommonModule, FormsModule, RouterModule]
})
export class CompanyCommentsDashboardComponent implements OnInit {
  videogames: GameWithImage[] = [];
  loading = true;
  error: string | null = null;
  user: any = null;
  commentsVisible = true;
  updatingVisibility = false;
  gameCommentsStatus: Map<number, boolean | null> = new Map();
  successMessage: string | null = null;
  successMessageExiting: boolean = false;
  private successTimeout: any;

  constructor(
    private videogameService: VideogameService,
    private commentService: CommentService,
    private session: SessionService,
    private router: Router
  ) {
    this.user = this.session.getUser();
  }

  ngOnInit(): void {
    this.loadVideogames();
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

  loadVideogames(): void {
    this.videogameService.getAll().subscribe({
      next: (games) => {
        this.videogames = games.map(g => ({ ...g, updatingComments: false }));
        this.loadImagesForGames();
        this.loadGameCommentsStatus();
      },
      error: (err) => {
        console.error('Error al cargar videojuegos:', err);
        this.error = 'Error al cargar los videojuegos';
        this.loading = false;
      }
    });
  }

  private loadGameCommentsStatus(): void {
    this.videogames.forEach(game => {
      this.commentService.getGameCommentsStatus(game.videogameId).subscribe({
        next: (status) => {
          this.gameCommentsStatus.set(game.videogameId, status.enabled);
        },
        error: (err) => {
          console.error('Error al cargar estado de comentarios:', err);
          this.gameCommentsStatus.set(game.videogameId, null);
        }
      });
    });
  }

  private loadImagesForGames(): void {
    const imageRequests = this.videogames.map(game =>
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
        this.videogames.forEach((game, index) => {
          const images = imagesArrays[index];
          if (images && images.length > 0 && images[0].image) {
            // Convertir base64 a data URL
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

  toggleCommentsVisibility(): void {
    if (!this.user || !this.user.company_id) {
      this.error = 'No se pudo obtener el ID de la empresa';
      return;
    }

    this.updatingVisibility = true;

    console.log('Disabling comments for company', this.user.company_id);

    this.commentService.updateCommentsVisibility(this.user.company_id, false).subscribe({
      next: (response) => {
        console.log('Respuesta del servidor:', response);
        this.commentsVisible = false;
        this.updatingVisibility = false;
        this.error = null;
        this.showSuccessMessage('Comentarios desactivados exitosamente');
        console.log('Comentarios desactivados exitosamente');
      },
      error: (err) => {
        console.error('Error al desactivar comentarios:', err);
        console.error('Detalles del error:', err.message, err.status);
        this.error = `Error al desactivar comentarios: ${err.error?.message || 'Error desconocido'}`;
        this.updatingVisibility = false;
      }
    });
  }

  activateCommentsVisibility(): void {
    if (!this.user || !this.user.company_id) {
      this.error = 'No se pudo obtener el ID de la empresa';
      return;
    }

    this.updatingVisibility = true;

    console.log('Enabling comments for company', this.user.company_id);

    this.commentService.updateCommentsVisibility(this.user.company_id, true).subscribe({
      next: (response) => {
        console.log('Respuesta del servidor:', response);
        this.commentsVisible = true;
        this.updatingVisibility = false;
        this.error = null;
        this.showSuccessMessage('Comentarios activados exitosamente');
        console.log('Comentarios activados exitosamente');
      },
      error: (err) => {
        console.error('Error al activar comentarios:', err);
        console.error('Detalles del error:', err.message, err.status);
        this.error = `Error al activar comentarios: ${err.error?.message || 'Error desconocido'}`;
        this.updatingVisibility = false;
      }
    });
  }

  disableGameComments(game: GameWithImage): void {
    if (!game.updatingComments) {
      game.updatingComments = true;
      
      this.commentService.disableGameComments(game.videogameId).subscribe({
        next: (response) => {
          console.log('Comentarios desactivados:', response);
          this.gameCommentsStatus.set(game.videogameId, false);
          game.updatingComments = false;
          this.error = null;
          this.showSuccessMessage(`Comentarios de "${game.name}" desactivados`);
        },
        error: (err) => {
          console.error('Error al desactivar comentarios:', err);
          this.error = `Error al desactivar comentarios: ${err.error?.message || 'Error desconocido'}`;
          game.updatingComments = false;
        }
      });
    }
  }

  enableGameComments(game: GameWithImage): void {
    if (!game.updatingComments) {
      game.updatingComments = true;
      
      this.commentService.enableGameComments(game.videogameId).subscribe({
        next: (response) => {
          console.log('Comentarios activados:', response);
          this.gameCommentsStatus.set(game.videogameId, true);
          game.updatingComments = false;
          this.error = null;
          this.showSuccessMessage(`Comentarios de "${game.name}" activados`);
        },
        error: (err) => {
          console.error('Error al activar comentarios:', err);
          this.error = `Error al activar comentarios: ${err.error?.message || 'Error desconocido'}`;
          game.updatingComments = false;
        }
      });
    }
  }

  getCommentsStatus(game: GameWithImage): boolean | null {
    return this.gameCommentsStatus.get(game.videogameId) ?? null;
  }

  enableAllGameComments(): void {
    this.updatingVisibility = true;
    
    const enableRequests = this.videogames.map(game =>
      this.commentService.enableGameComments(game.videogameId).pipe(
        catchError(err => {
          console.error(`Error al activar comentarios del juego ${game.videogameId}:`, err);
          return of(null);
        })
      )
    );

    if (enableRequests.length === 0) {
      this.updatingVisibility = false;
      return;
    }

    forkJoin(enableRequests).subscribe({
      next: () => {
        console.log('Todos los comentarios han sido activados');
        this.videogames.forEach(game => {
          this.gameCommentsStatus.set(game.videogameId, true);
        });
        this.updatingVisibility = false;
        this.error = null;
        this.showSuccessMessage('Todos los comentarios activados exitosamente');
      },
      error: (err) => {
        console.error('Error al activar todos los comentarios:', err);
        this.error = 'Error al activar todos los comentarios';
        this.updatingVisibility = false;
      }
    });
  }

  getImageUrl(game: GameWithImage): string {
    return game.imageUrl || this.getDefaultImage();
  }

  getDefaultImage(): string {
    return 'https://via.placeholder.com/250x350?text=No+Image';
  }
}


