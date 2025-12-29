import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { GamerService, GamerPublicInfo } from '../../services/gamer.service';
import { VideogameService } from '../../services/videogame.service';
import { Image, Videogame } from '../../models';
import { LibraryService } from '../../services/library.service';

@Component({
  standalone: true,
  selector: 'app-gamer-public-profile',
  templateUrl: './gamer-public-profile.component.html',
  styleUrls: ['./gamer-public-profile.component.css'],
  imports: [CommonModule, RouterModule]
})
export class GamerPublicProfileComponent implements OnInit {
  info: GamerPublicInfo | null = null;
  ownedGames: (Videogame & { buyed?: boolean; installed?: boolean })[] = [];
  covers = new Map<number, string>();
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gamerService: GamerService,
    private videogameService: VideogameService,
    private libraryService: LibraryService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const email = params['email'];
      if (!email) { this.loading = false; return; }
      this.loadInfo(email);
      this.loadOwnedGames(email);
    });
  }

  loadInfo(email: string): void {
    this.gamerService.getGamerInfo(email).subscribe({
      next: (info) => { this.info = info; },
      error: (err) => { console.error(err); this.error = 'Error al cargar el gamer'; this.loading = false; }
    });
  }

  loadOwnedGames(email: string): void {
    this.libraryService.getUserLibrary(email).subscribe({
      next: (games: Videogame[]) => {
        const list = (games as (Videogame & { buyed?: boolean; installed?: boolean })[]) || [];
        this.ownedGames = list.filter(g => g.buyed === true);
        this.loading = false;
        this.loadCovers();
      },
      error: (err) => {
        console.error(err);
        this.error = 'Error al cargar juegos';
        this.loading = false;
      }
    });
  }

  loadCovers(): void {
    this.ownedGames.forEach(g => {
      this.videogameService.getGameImages(g.videogameId).subscribe({
        next: (images: Image[]) => {
          if (images && images.length > 0 && images[0].image) {
            this.covers.set(g.videogameId, images[0].image);
          }
        }
      });
    });
  }

  getCover(gameId: number): string | undefined {
    return this.covers.get(gameId);
  }
}
