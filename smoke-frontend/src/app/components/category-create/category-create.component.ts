// Importaciones necesarias de Angular
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { HttpClient } from '@angular/common/http'; 
import { Router } from '@angular/router'; 
import { API_BASE_URL } from '../../core/appi.config';
import { Category } from '../../models/Category'; 

@Component({
  selector: 'app-category-create',
  standalone: true, // Componente standalone (no necesita módulo)
  imports: [CommonModule, FormsModule], // Importamos módulos necesarios
  templateUrl: './category-create.component.html',
  styleUrl: './category-create.component.css'
})
export class CategoryCreateComponent {
  
  // Modelo para el formulario: almacena el nombre de la categoría que el usuario escribe
  category = {
    name: ''
  };

  // Variables para mostrar mensajes al usuario
  successMessage: string = ''; // Mensaje cuando se crea exitosamente
  errorMessage: string = '';   // Mensaje cuando hay un error
  isLoading: boolean = false;  // Indica si está procesando la petición

  /**
   * Constructor del componente
   * @param http - Servicio para hacer peticiones HTTP
   * @param router - Servicio para navegar entre páginas
   */
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Método que se ejecuta cuando el usuario envía el formulario
   * Crea una nueva categoría en el backend
   */
  onSubmit(): void {
    // Limpiar mensajes anteriores
    this.successMessage = '';
    this.errorMessage = '';

    // Validar que el nombre no esté vacío
    if (!this.category.name || this.category.name.trim() === '') {
      this.errorMessage = 'El nombre de la categoría es obligatorio';
      return; // Salir del método si no hay nombre
    }

    // Indicar que está cargando (desactiva el botón y muestra spinner)
    this.isLoading = true;

    // Preparar los datos para enviar al backend
    const payload = {
      name: this.category.name.trim() // Quitamos espacios al inicio/final
    };

    // Hacer petición POST al backend para crear la categoría
    // La URL completa será: http://localhost:8080/smoke/category/create
    this.http.post<Category>(`${API_BASE_URL}/category/create`, payload)
      .subscribe({
        // next: se ejecuta cuando la petición es exitosa
        next: (response) => {
          this.isLoading = false; // Quitar el indicador de carga
          this.successMessage = `Categoría "${response.name}" creada exitosamente con ID: ${response.categoryId}`;
          
          // Limpiar el formulario después de 2 segundos
          setTimeout(() => {
            this.category.name = '';
            this.successMessage = '';
          }, 2000);
        },
        // error: se ejecuta cuando hay un error en la petición
        error: (error) => {
          this.isLoading = false; // Quitar el indicador de carga
          console.error('Error al crear categoría:', error);
          
          // Mostrar mensaje de error personalizado según el tipo de error
          if (error.status === 400) {
            this.errorMessage = 'Datos inválidos. Verifica el nombre de la categoría.';
          } else if (error.status === 409) {
            this.errorMessage = 'Esta categoría ya existe.';
          } else if (error.status === 0) {
            this.errorMessage = 'No se puede conectar con el servidor. Verifica que el backend esté corriendo.';
          } else {
            this.errorMessage = error.error?.message || 'Error al crear la categoría. Intenta nuevamente.';
          }
        }
      });
  }

  /**
   * Método para cancelar y volver atrás
   */
  onCancel(): void {
    // Navegar de regreso al dashboard de administrador
    this.router.navigate(['/admin']);
  }

  /**
   * Método para limpiar los mensajes de error/éxito
   */
  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}