import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';
import { FamilyGroupService } from '../../services/family-group.service';

@Component({
  standalone: true,
  selector: 'app-family-group-create',
  templateUrl: './family-group-create.component.html',
  styleUrls: ['./family-group-create.component.css'],
  imports: [CommonModule, FormsModule]
})
export class FamilyGroupCreateComponent implements OnInit {
  groupName = '';
  description = '';
  ownerEmail: string | null = null;
  isSubmitting = false;
  message: string | null = null;
  success = false;

  constructor(
    private sessionService: SessionService,
    private familyGroupService: FamilyGroupService
  ) {}

  ngOnInit(): void {
    const user = this.sessionService.getUser();
    this.ownerEmail = user?.mail || null;
    if (!this.ownerEmail) {
      this.message = 'Debes iniciar sesión para crear un grupo familiar';
      this.success = false;
    }
  }

  createGroup(): void {
    if (!this.ownerEmail) {
      this.message = 'Debes iniciar sesión para crear un grupo familiar';
      this.success = false;
      return;
    }
    if (!this.groupName.trim()) {
      this.message = 'Ingresa un nombre para el grupo';
      this.success = false;
      return;
    }

    this.isSubmitting = true;
    this.message = null;
    this.success = false;

    this.familyGroupService.createGroup(this.groupName.trim(), this.ownerEmail, this.description?.trim()).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.success = true;
        this.message = res.message || 'Grupo familiar creado exitosamente';
        this.groupName = '';
        this.description = '';
      },
      error: (err) => {
        this.isSubmitting = false;
        this.success = false;
        this.message = err.error?.message || err.error?.error || 'No se pudo crear el grupo';
        console.error('Error al crear grupo familiar', err);
      }
    });
  }
}
