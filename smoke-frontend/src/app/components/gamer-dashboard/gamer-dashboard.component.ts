import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BannerCarouselComponent } from '../banner-carousel/banner-carousel.component';
import { TopGamesListComponent } from '../top-games-list/top-games-list.component';

@Component({
  standalone: true,
  selector: 'app-gamer-dashboard',
  templateUrl: './gamer-dashboard.component.html',
  styleUrls: ['./gamer-dashboard.component.css'],
  imports: [CommonModule, BannerCarouselComponent, TopGamesListComponent]
})
export class GamerDashboardComponent implements OnInit {
  ngOnInit(): void {
    // Aquí puedes cargar datos del gamer si es necesario
  }
}
