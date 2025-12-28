import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { CompanyService } from '../../services/company.service';
import { Videogame, Category } from '../../models';

interface GameCardView {
  game: Videogame;
  companyName: string;
  categories: string[];
  loading: boolean;
}

@Component({
  standalone: true,
  selector: 'app-category-admin',
  templateUrl: './category-admin.component.html',
  styleUrls: ['./category-admin.component.css'],
  imports: [CommonModule, RouterLink]
})
export class CategoryAdminComponent {
  loading = false;
  error = '';
  cards: GameCardView[] = [];

  constructor(
    private videogameService: VideogameService,
    private companyService: CompanyService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.cards = [];

    // Paso 1: Cargar todos los juegos
    this.videogameService.getAll().subscribe({
      next: (games) => {
        this.loading = false;
        if (!games || games.length === 0) {
          return;
        }

        // Paso 2: Para cada juego, crear una card y cargar sus datos
        games.forEach(game => {
          const card: GameCardView = {
            game: game,
            companyName: '...',
            categories: [],
            loading: true
          };
          this.cards.push(card);

          // Cargar compañía
          this.companyService.getCompanyById(game.companyId).subscribe({
            next: (company) => {
              card.companyName = company?.name || '—';
            },
            error: () => {
              card.companyName = '—';
            }
          });

          // Cargar categorías
          this.videogameService.getCategoriesForGame(game.videogameId).subscribe({
            next: (categories: Category[]) => {
              card.categories = categories.map(c => c.name);
              card.loading = false;
            },
            error: () => {
              card.categories = [];
              card.loading = false;
            }
          });
        });
      },
      error: (err) => {
        this.error = err?.error?.error || 'Error cargando juegos';
        this.loading = false;
      }
    });
  }
}
