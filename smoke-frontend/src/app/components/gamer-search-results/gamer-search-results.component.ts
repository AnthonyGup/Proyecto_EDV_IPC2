import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { GamerService, GamerSummary } from '../../services/gamer.service';

@Component({
  standalone: true,
  selector: 'app-gamer-search-results',
  templateUrl: './gamer-search-results.component.html',
  styleUrls: ['./gamer-search-results.component.css'],
  imports: [CommonModule, RouterModule]
})
export class GamerSearchResultsComponent implements OnInit {
  results: GamerSummary[] = [];
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private gamerService: GamerService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const q = (params['q'] || '').trim();
      if (!q) {
        this.results = [];
        this.error = '';
        this.loading = false;
        return;
      }
      this.performSearch(q);
    });
  }

  performSearch(q: string): void {
    this.loading = true;
    this.error = '';
    this.gamerService.searchGamers(q).subscribe({
      next: (gamers) => {
        this.results = gamers;
        this.loading = false;
      },
      error: (err) => {
        console.error('Gamer search error', err);
        this.error = 'Error al buscar gamers';
        this.loading = false;
      }
    });
  }
}
