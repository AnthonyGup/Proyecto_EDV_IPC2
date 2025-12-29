import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FamilyGroupService } from '../../services/family-group.service';
import { SessionService } from '../../core/session.service';
import { Invitation } from '../../models/Invitation';
import { GamerService } from '../../services/gamer.service';

@Component({
  standalone: true,
  selector: 'app-family-group-invitations',
  templateUrl: './family-group-invitations.component.html',
  styleUrls: ['./family-group-invitations.component.css'],
  imports: [CommonModule]
})
export class FamilyGroupInvitationsComponent {
  loading = true;
  error: string | null = null;
  invitations: (Invitation & { groupName?: string; ownerId?: string })[] = [];
  userEmail: string | null = null;
  message: string | null = null;
  nicknameMap: Record<string, string> = {};

  constructor(private fgService: FamilyGroupService, private session: SessionService, private gamerService: GamerService) {}

  ngOnInit() {
    const user = this.session.getUser();
    this.userEmail = user?.mail || null;
    if (!this.userEmail) {
      this.loading = false;
      this.error = 'Debes iniciar sesión para ver tus invitaciones';
      return;
    }
    this.loadInvitations();
  }

  loadInvitations() {
    if (!this.userEmail) return;
    this.loading = true;
    this.error = null;
    this.fgService.getInvitations(this.userEmail).subscribe({
      next: (list) => {
        this.invitations = list || [];
        // Prefetch owner nicknames
        this.invitations.forEach(inv => {
          if (inv.ownerId) this.ensureNickname(inv.ownerId);
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar invitaciones';
        this.loading = false;
      }
    });
  }

  getNickname(email: string | undefined): string {
    if (!email) return '';
    const cached = this.nicknameMap[email];
    if (cached) return cached;
    this.ensureNickname(email);
    return email;
  }

  private ensureNickname(email: string): void {
    if (!email || this.nicknameMap[email]) return;
    this.gamerService.getGamerInfo(email).subscribe({
      next: (info) => { if (info?.nickname) this.nicknameMap[email] = info.nickname; },
      error: () => {}
    });
  }

  respond(invitationId: number, action: 'accept'|'reject') {
    if (!this.userEmail) return;
    this.message = null;
    this.fgService.respondInvitation(invitationId, action, this.userEmail).subscribe({
      next: (res) => {
        this.message = res.message || 'Acción realizada';
        // Actualizar lista removiendo la invitación
        this.invitations = this.invitations.filter(i => i.invitationId !== invitationId);
      },
      error: (err) => {
        this.message = err.error?.error || err.error?.message || 'No se pudo procesar la invitación';
      }
    });
  }
}
