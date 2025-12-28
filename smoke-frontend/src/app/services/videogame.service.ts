import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/appi.config';
import { Videogame, Category, Image } from '../models';

@Injectable({ providedIn: 'root' })
export class VideogameService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Videogame[]> {
    return this.http.get<Videogame[]>(`${API_BASE_URL}/videogame/all`);
  }

  getCategoriesForGame(videogameId: number): Observable<Category[]> {
    return this.http.get<Category[]>(`${API_BASE_URL}/game-categories?gameId=${videogameId}`);
  }

  createGame(
    videogame: Partial<Videogame> & { companyId: number },
    categoryIds?: number[],
    imageFiles?: File[]
  ): Observable<{ message: string }> {
    const payload: any = { videogame };
    
    if (categoryIds && categoryIds.length > 0) {
      payload.categories = categoryIds;
    }
    
    console.log('Payload antes de enviar:', payload);
    
    if (imageFiles && imageFiles.length > 0) {
      const imagePromises = imageFiles.map(file => this.fileToBase64(file));
      return new Observable(observer => {
        Promise.all(imagePromises).then(base64Images => {
          payload.images = base64Images.map(img => ({ image: img }));
          console.log('Payload con imágenes:', payload);
          this.http.post<{ message: string }>(`${API_BASE_URL}/games/creator`, payload)
            .subscribe({
              next: (res) => observer.next(res),
              error: (err) => observer.error(err),
              complete: () => observer.complete()
            });
        }).catch(err => observer.error(err));
      });
    }
    
    return this.http.post<{ message: string }>(`${API_BASE_URL}/games/creator`, payload);
  }

  private fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = (reader.result as string).split(',')[1];
        resolve(base64);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  addCategoriesToGame(videogameId: number, categoryIds: number[]): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/videogame/${videogameId}/categories`, { categoryIds });
  }

  uploadImages(videogameId: number, files: File[]): Observable<{ message: string }> {
    const fd = new FormData();
    files.forEach((f, idx) => fd.append('files', f, f.name));
    return this.http.post<{ message: string }>(`${API_BASE_URL}/videogame/${videogameId}/images`, fd);
  }

  updateGameCategories(payload: { gameId: number; categoryIds: number[] }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/game-categories-update`, payload);
  }

  getGameImages(videogameId: number): Observable<Image[]> {
    return this.http.get<Image[]>(`${API_BASE_URL}/images?videogame_id=${videogameId}`);
  }

  // Actualiza datos básicos del juego
  updateGame(videogame: Partial<Videogame> & { videogameId: number }): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${API_BASE_URL}/videogame/${videogame.videogameId}`, videogame);
  }

  // Cambia disponibilidad del juego
  setGameAvailability(videogameId: number, available: boolean): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/videogame/${videogameId}/availability`, { available });
  }

  // Elimina una imagen por id
  deleteImage(imageId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${API_BASE_URL}/images/${imageId}`);
  }
}
