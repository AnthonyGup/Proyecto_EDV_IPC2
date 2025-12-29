import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { FamilyGroupService } from '../../services/family-group.service';
import { FamilyGroup } from '../../models';

@Component({
  standalone: true,
  selector: 'app-family-group-invite',
  templateUrl: './family-group-invite.component.html',
  styleUrls: ['./family-group-invite.component.css'],
  imports: [CommonModule, FormsModule]
})
export class FamilyGroupInviteComponent implements OnInit {
  groups: FamilyGroup[] = [];
  selectedGroupId: number | undefined;
  inviteEmail = '';
  currentUserEmail: string | null = null;
  loading = false;
  message: string | null = null;
  success = false;

  constructor(
    private session: SessionService,
    private familyGroupService: FamilyGroupService
  ) {}

  ngOnInit(): void {
    const user = this.session.getUser();
    this.currentUserEmail = user?.mail || null;
    if (!this.currentUserEmail) {
      this.message = 'Debes iniciar sesión para invitar miembros';
      return;
    }
    this.loadGroups();
  }

  loadGroups(): void {
    if (!this.currentUserEmail) return;
    this.familyGroupService.getGroupsByUser(this.currentUserEmail).subscribe({
      next: (groups) => {
        // Solo permitir grupos creados por el usuario (owner)
        const owned = (groups || []).filter(g => g.ownerId === this.currentUserEmail);
        this.groups = owned;
        if (owned.length > 0) {
          this.selectedGroupId = owned[0].groupId;
        }
      },
      error: (err) => {
        console.error('Error cargando grupos', err);
        this.message = 'No se pudieron cargar tus grupos';
      }
    });
  }

  sendInvite(): void {
    if (!this.currentUserEmail) {
      this.message = 'Debes iniciar sesión para invitar miembros';
      this.success = false;
      return;
    }

    if (!this.selectedGroupId) {
      this.message = 'Selecciona un grupo';
      this.success = false;
      return;
    }

    if (!this.inviteEmail.trim()) {
      this.message = 'Ingresa el correo del invitado';
      this.success = false;
      return;
    }

    this.loading = true;
    this.message = null;
    this.success = false;

    this.familyGroupService.sendInvitation(this.selectedGroupId, this.inviteEmail.trim(), this.currentUserEmail).subscribe({
      next: (res) => {
        this.loading = false;
        this.success = true;
        this.message = res.message || 'Invitación enviada';
        this.inviteEmail = '';
      },
      error: (err) => {
        this.loading = false;
        this.success = false;
        this.message = err.error?.message || 'No se pudo enviar la invitación';
        console.error('Error enviando invitación', err);
      }
    });
  }
}
