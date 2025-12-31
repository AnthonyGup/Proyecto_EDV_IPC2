import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/User';
import { Gamer } from '../../models/Gamer';

@Component({
  standalone: true,
  selector: 'app-gamer-profile',
  templateUrl: './gamer-profile.component.html',
  styleUrls: ['./gamer-profile.component.css'],
  imports: [CommonModule, FormsModule]
})
export class GamerProfileComponent implements OnInit {
  user: User | null = null;
  gamer: Gamer | null = null;
  isEditing = false;
  editForm: any = {};
  wallet: number | null = null;
  rechargeAmount = 10;
  isRecharging = false;
  rechargeMessage = '';
  rechargeModal = false;

  constructor(private session: SessionService, private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.session.getUser();
    if (this.user) {
      this.editForm = { ...this.user };
      // Cargar info del gamer y esperar antes de proceder
      this.loadGamerInfo();
    }
  }

  loadGamerInfo(): void {
    if (!this.user) return;
    
    this.authService.getGamerInfo(this.user.mail).subscribe({
      next: (gamerInfo: any) => {
        console.log('Gamer info recibida:', gamerInfo);
        // El backend retorna la estructura completa del gamer
        this.wallet = gamerInfo.wallet !== undefined && gamerInfo.wallet !== null ? Number(gamerInfo.wallet) : 0;
        console.log('Wallet asignado:', this.wallet);
      },
      error: (err) => {
        console.error('Error loading gamer info:', err);
        this.wallet = 0;
      }
    });
  }

  rechargeWallet(): void {
    if (this.rechargeAmount <= 0) {
      this.rechargeMessage = 'El monto debe ser mayor a 0';
      return;
    }

    if (!this.user) {
      this.rechargeMessage = 'Usuario no encontrado';
      return;
    }

    this.isRecharging = true;
    this.rechargeMessage = '';

    this.authService.rechargeWallet(this.rechargeAmount, this.user.mail).subscribe({
      next: (res) => {
        this.wallet = Number(res.newWallet);
        this.rechargeMessage = res.message;
        this.rechargeAmount = 10;
        this.isRecharging = false;
        // Auto-hide mensaje en 3 segundos
        setTimeout(() => {
          this.rechargeMessage = '';
        }, 3000);
      },
      error: (err) => {
        this.rechargeMessage = 'Error al recargar el wallet';
        this.isRecharging = false;
        console.error('Recharge error:', err);
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (this.user) {
      this.editForm = { ...this.user };
    }
  }

  saveChanges(): void {
    if (this.user) {
      const updatedUser: User = { ...this.editForm };
      this.user = updatedUser;
      this.session.saveUser(updatedUser);
      this.isEditing = false;
    }
  }

  cancelEdit(): void {
    this.isEditing = false;
  }

  getFormattedDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES');
    } catch {
      return dateString;
    }
  }
}
