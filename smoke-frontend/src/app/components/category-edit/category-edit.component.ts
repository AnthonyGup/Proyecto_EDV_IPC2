import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/Category';

@Component({
  standalone: true,
  selector: 'app-category-edit',
  templateUrl: './category-edit.component.html',
  styleUrls: ['./category-edit.component.css'],
  imports: [CommonModule, FormsModule, RouterLink]
})
export class CategoryEditComponent {
  categories: Category[] = [];
  loading = false;
  error = '';
  success = '';

  editingId: number | null = null;
  editName = '';

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.categoryService.getAll().subscribe({
      next: (list) => {
        this.categories = list;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.error || 'Error cargando categorías';
      }
    });
  }

  startEdit(cat: Category): void {
    this.editingId = cat.categoryId;
    this.editName = cat.name;
    this.success = '';
    this.error = '';
  }

  cancelEdit(): void {
    this.editingId = null;
    this.editName = '';
  }

  saveEdit(): void {
    if (!this.editingId) return;
    const name = this.editName?.trim();
    if (!name) {
      this.error = 'El nombre es obligatorio';
      return;
    }
    this.loading = true;
    this.categoryService.update(this.editingId, name).subscribe({
      next: (updated) => {
        const idx = this.categories.findIndex(c => c.categoryId === updated.categoryId);
        if (idx >= 0) this.categories[idx] = updated;
        this.success = 'Categoría actualizada';
        this.loading = false;
        this.cancelEdit();
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 409) this.error = 'La categoría ya existe';
        else this.error = err?.error?.error || 'Error actualizando categoría';
      }
    });
  }

  delete(cat: Category): void {
    if (!confirm(`¿Eliminar categoría "${cat.name}"?`)) return;
    this.loading = true;
    this.categoryService.delete(cat.categoryId).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.categoryId !== cat.categoryId);
        this.success = 'Categoría eliminada';
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.error || 'Error eliminando categoría';
      }
    });
  }
}
