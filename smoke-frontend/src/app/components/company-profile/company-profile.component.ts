import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CompanyService } from '../../services/company.service';
import { VideogameService } from '../../services/videogame.service';
import { Company, Videogame, Image } from '../../models';
import { catchError, of } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-company-profile',
  templateUrl: './company-profile.component.html',
  styleUrls: ['./company-profile.component.css'],
  imports: [CommonModule, RouterModule]
})
export class CompanyProfileComponent implements OnInit {
  company: Company | null = null;
  games: Videogame[] = [];
  covers: Record<number, string> = {};
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private companyService: CompanyService,
    private vgService: VideogameService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const companyId = Number(params['id']);
      if (companyId) {
        this.loadCompanyProfile(companyId);
      }
    });
  }

  loadCompanyProfile(companyId: number): void {
    this.companyService.getCompanyById(companyId).pipe(
      catchError(err => {
        this.error = 'Error al cargar la empresa';
        this.loading = false;
        return of(null);
      })
    ).subscribe(company => {
      if (!company) {
        this.loading = false;
        return;
      }
      this.company = company;
      this.loadCompanyGames(companyId);
    });
  }

  loadCompanyGames(companyId: number): void {
    this.companyService.getCompanyGames(companyId).pipe(
      catchError(() => of([] as Videogame[]))
    ).subscribe(games => {
      this.games = games || [];
      this.loadCovers();
      this.loading = false;
    });
  }

  openGame(game: Videogame): void {
    this.router.navigate(['/gamer/game', game.videogameId]);
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
