import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { LibraryService } from '../../services/library.service';
import { SessionService } from '../../core/session.service';
import { Videogame, Image } from '../../models';
import { VideogameService } from '../../services/videogame.service';

@Component({
  standalone: true,
  selector: 'app-gamer-library',
  templateUrl: './gamer-library.component.html',
  styleUrls: ['./gamer-library.component.css'],
  imports: [CommonModule, RouterModule]
})
export class GamerLibraryComponent implements OnInit {
  games: Videogame[] = [];
  loading = true;
  error: string | null = null;
  covers: Record<number, string> = {};

  constructor(
    private libraryService: LibraryService,
    private sessionService: SessionService,
    private router: Router,
    private vgService: VideogameService
  ) {}

  ngOnInit(): void {
    const user = this.sessionService.getUser();
    const email = user?.mail;
    if (!email) {
      this.error = 'No hay usuario en sesión';
      this.loading = false;
      return;
    }
    this.libraryService.getUserLibrary(email).subscribe({
      next: (games) => {
        this.games = games || [];
        this.loadCovers();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.error || 'Error al cargar la biblioteca';
        this.loading = false;
      }
    });
  }

  openGame(game: Videogame): void {
    this.router.navigate(['/gamer/game', game.videogameId], { queryParams: { from: 'library' } });
  }

  private loadCovers(): void {
    this.games.forEach(g => {
      this.vgService.getGameImages(g.videogameId).subscribe({
        next: (images: Image[]) => {
          const baner = images.find(img => (img as any).baner === true);
          const chosen = baner || images[0];
          if (chosen && chosen.image) {
            this.covers[g.videogameId] = `data:image/png;base64,${chosen.image}`;
          } else {
            this.covers[g.videogameId] = this.getPlaceholder(300, 180, g.name);
          }
        },
        error: () => {
          this.covers[g.videogameId] = this.getPlaceholder(300, 180, g.name);
        }
      });
    });
  }

  getPlaceholder(width: number, height: number, text: string): string {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><defs><linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#1f2937;stop-opacity:1" /><stop offset="100%" style="stop-color:#111827;stop-opacity:1" /></linearGradient></defs><rect fill="url(#grad)" width="${width}" height="${height}"/><text x="50%" y="50%" font-size="20" fill="#9CA3AF" text-anchor="middle" dominant-baseline="middle">${text}</text></svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
  }
}
