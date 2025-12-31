import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FamilyGroupService } from '../../services/family-group.service';
import { SessionService } from '../../core/session.service';
import { FamilyGroup, GroupMember } from '../../models';
import { GamerService } from '../../services/gamer.service';

@Component({
  standalone: true,
  selector: 'app-family-group-list',
  templateUrl: './family-group-list.component.html',
  styleUrls: ['./family-group-list.component.css'],
  imports: [CommonModule, FormsModule]
})
export class FamilyGroupListComponent implements OnInit {
  currentUserEmail: string | null = null;
  groups: FamilyGroup[] = [];
  members: Record<number, GroupMember[]> = {};
  loading = false;
  message: string | null = null;

  nicknameMap: Record<string, string> = {};

  constructor(
    private session: SessionService,
    private familyService: FamilyGroupService,
    private gamerService: GamerService
  ) {}

  ngOnInit(): void {
    const user = this.session.getUser();
    this.currentUserEmail = user?.mail || null;
    if (!this.currentUserEmail) {
      this.message = 'Debes iniciar sesión para ver tus grupos';
      return;
    }
    this.loadGroups();
  }

  loadGroups(): void {
    if (!this.currentUserEmail) return;
    this.loading = true;
    this.familyService.getGroupsByUser(this.currentUserEmail).subscribe({
      next: (groups) => {
        this.groups = groups;
        this.loading = false;
        for (const g of groups) {
          if (g.groupId) {
            this.loadMembers(g.groupId);
          }
        }
      },
      error: (err) => {
        console.error('Error cargando grupos', err);
        this.message = 'No se pudieron cargar tus grupos';
        this.loading = false;
      }
    });
  }

  loadMembers(groupId: number): void {
    this.familyService.getMembers(groupId).subscribe({
      next: (ms) => {
        this.members[groupId] = ms;
        // Prefetch nicknames for members
        (ms || []).forEach(m => this.ensureNickname(m.userId));
      },
      error: (err) => {
        console.error('Error cargando miembros', err);
      }
    });
  }

  getNickname(email: string): string {
    if (!email) return '';
    const cached = this.nicknameMap[email];
    if (cached) return cached;
    this.ensureNickname(email);
    return email; // fallback until loaded
  }

  private ensureNickname(email: string): void {
    if (!email || this.nicknameMap[email]) return;
    this.gamerService.getGamerInfo(email).subscribe({
      next: (info) => {
        if (info?.nickname) this.nicknameMap[email] = info.nickname;
      },
      error: () => {}
    });
  }

  deleteGroup(group: FamilyGroup): void {
    if (!this.currentUserEmail || !group.groupId) return;
    if (group.ownerId !== this.currentUserEmail) {
      this.message = 'Solo el dueño puede eliminar el grupo';
      return;
    }
    if (!confirm(`¿Estás seguro de eliminar el grupo "${group.groupName || group.name}"? Esto eliminará todos los juegos no comprados de las librerías de los miembros.`)) {
      return;
    }
    this.loading = true;
    this.familyService.deleteGroup(group.groupId, this.currentUserEmail).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Grupo eliminado exitosamente';
        this.loadGroups();
      },
      error: (err) => {
        this.loading = false;
        this.message = err.error?.message || 'No se pudo eliminar el grupo';
        console.error('Error eliminando grupo', err);
      }
    });
  }

  removeMember(group: FamilyGroup, memberEmail: string): void {
    if (!this.currentUserEmail || !group.groupId) return;
    if (group.ownerId !== this.currentUserEmail) {
      this.message = 'Solo el dueño puede eliminar miembros';
      return;
    }
    if (memberEmail === this.currentUserEmail) {
      this.message = 'No puedes eliminarte a ti mismo del grupo';
      return;
    }
    const nickname = this.getNickname(memberEmail);
    if (!confirm(`¿Estás seguro de eliminar a ${nickname} del grupo? Perderá acceso a los juegos compartidos.`)) {
      return;
    }
    this.loading = true;
    this.familyService.removeMember(group.groupId, memberEmail, this.currentUserEmail).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Miembro eliminado exitosamente';
        if (group.groupId) {
          this.loadMembers(group.groupId);
        }
      },
      error: (err) => {
        this.loading = false;
        this.message = err.error?.message || err.error?.error || 'No se pudo eliminar el miembro';
        console.error('Error eliminando miembro', err);
      }
    });
  }
}
