import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';
import { GamerNavbarComponent } from '../gamer-navbar/gamer-navbar.component';

@Component({
  standalone: true,
  selector: 'app-gamer-layout',
  templateUrl: './gamer-layout.component.html',
  styleUrls: ['./gamer-layout.component.css'],
  imports: [CommonModule, RouterModule, FooterComponent, GamerNavbarComponent]
})
export class GamerLayoutComponent {
}
