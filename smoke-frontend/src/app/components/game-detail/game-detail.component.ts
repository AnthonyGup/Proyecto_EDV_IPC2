import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { VideogameService } from '../../services/videogame.service';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { SessionService } from '../../core/session.service';
import { LibraryService } from '../../services/library.service';
import { CommentThreadComponent } from '../comment-thread/comment-thread.component';
import { FooterComponent } from '../footer/footer.component';
import { GamerNavbarComponent } from '../gamer-navbar/gamer-navbar.component';
import { Image, Videogame, Comment, CommentThread, Company } from '../../models';
import { RateService } from '../../services/rate.service';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CompanyService } from '../../services/company.service';
import { GamerService } from '../../services/gamer.service';

@Component({
  standalone: true,
  selector: 'app-game-detail',
  templateUrl: './game-detail.component.html',
  styleUrls: ['./game-detail.component.css'],
  imports: [CommonModule, RouterModule, FormsModule, CommentThreadComponent, FooterComponent, GamerNavbarComponent]
})
export class GameDetailComponent implements OnInit {
  game: Videogame | null = null;
  company: Company | null = null;
  gameImages: Image[] = [];
  categories: any[] = [];
  commentThreads: CommentThread[] = [];
  currentImageIndex = 0;
  loading = true;
  error: string | null = null;
  inGamerLayout = false;
  isLibraryMode = false;
  
  // Buy Modal
  showBuyModal = false;
  isLoadingBuy = false;
  userEmail: string | null = null;
  currentWallet: number | null = null;
  buyMessage: string | null = null;
  buySuccess = false;

  // Install state
  isInstalling = false;
  installMessage: string | null = null;
  installSuccess = false;
  isInLibrary = false;
  isInstalled = false;
  isBuyed = false;

  // Rating state
  myRating: number | null = null;
    averageRating: number | null = null;
    ratingsCount = 0;
  averageStarsRounded = 0;
  ratingLoading = false;
  ratingMessage: string | null = null;

  // Comments state
  newComment = '';
  replyingTo: number | null = null;
  commentsError: string | null = null;
  commentsLoading = false;
  commentNicknames: Record<string, string> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private vgService: VideogameService,
    private commentService: CommentService,
    private authService: AuthService,
    private sessionService: SessionService,
    private libraryService: LibraryService,
    private rateService: RateService,
    private companyService: CompanyService,
    private gamerService: GamerService
  ) {}

  ngOnInit(): void {
    // Detectar si estamos bajo la ruta /gamer para evitar duplicar header/footer
    this.inGamerLayout = this.router.url.includes('/gamer');
    
    // Obtener el email del usuario desde sessionStorage
    const user = this.sessionService.getUser();
    this.userEmail = user?.mail || null;

    // Detectar modo desde query params
    this.route.queryParams.subscribe(qp => {
      this.isLibraryMode = qp['from'] === 'library';
    });

    this.route.params.subscribe(params => {
      const gameId = params['id'];
      if (gameId) {
        this.loadGameDetail(Number(gameId));
        this.loadAverage(Number(gameId));
      }
    });
  }

  private loadGameDetail(gameId: number): void {
    this.vgService.getById(gameId).pipe(
      catchError(err => {
        console.error('Error al cargar juego', err);
        this.error = 'Error al cargar los detalles del juego';
        this.loading = false;
        return of(null);
      })
    ).subscribe(game => {
      if (!game) {
        this.loading = false;
        return;
      }
      
      this.game = game;
      // Cargar información de la empresa
      if (game.companyId) {
        this.loadCompany(game.companyId);
      }

      // Verificar si el juego está en la biblioteca del usuario y su estado de instalación
      if (this.userEmail) {
        this.libraryService.getInstallStatus(this.userEmail, gameId).pipe(
          catchError(() => of({ inLibrary: false, installed: false, buyed: false }))
        ).subscribe(status => {
          this.isInLibrary = status.inLibrary;
          this.isInstalled = status.installed;
          this.isBuyed = status.buyed;

          // cargar calificación si comprado
          if (this.isBuyed && this.userEmail) {
            this.loadMyRating(gameId);
          }
        });
      }

      // Cargar imágenes del juego
      this.vgService.getGameImages(gameId).pipe(
        catchError(() => of([] as Image[]))
      ).subscribe(images => {
        this.gameImages = images || [];
      });

      // Cargar categorías del juego
      this.vgService.getGameCategories(gameId).pipe(
        catchError(() => of([]))
      ).subscribe(categories => {
        this.categories = categories || [];
      });

      // Cargar comentarios del juego
      this.commentService.getCommentsByGame(gameId).pipe(
        catchError(() => of([] as any))
      ).subscribe(commentThreads => {
        this.commentThreads = commentThreads || [];
        this.populateCommentNicknames();
        this.loading = false;
      });
    });
  }

  private populateCommentNicknames(): void {
    const emails = new Set<string>();
    const collect = (t: any) => {
      if (t?.comment?.userId) emails.add(t.comment.userId);
      if (t?.replies) t.replies.forEach((r: any) => collect(r));
    };
    (this.commentThreads || []).forEach(t => collect(t));
    emails.forEach(email => {
      if (this.commentNicknames[email]) return;
      this.gamerService.getGamerInfo(email).pipe(catchError(() => of(null as any))).subscribe(info => {
        if (info?.nickname) {
          this.commentNicknames[email] = info.nickname;
        }
      });
    });
  }

  private loadCompany(companyId: number): void {
    this.companyService.getCompanyById(companyId).pipe(
      catchError(err => {
        console.error('Error al cargar la empresa', err);
        return of(null);
      })
    ).subscribe(company => {
      this.company = company;
    });
  }

  private loadMyRating(gameId: number): void {
    if (!this.userEmail) return;
    this.ratingLoading = true;
    this.rateService.getMyRating(gameId, this.userEmail).pipe(
      catchError(() => of({ rating: null }))
    ).subscribe(res => {
      this.myRating = res.rating;
      this.ratingLoading = false;
    });
  }

  private loadAverage(gameId: number) {
    this.rateService.getAverageRating(gameId).subscribe({
      next: (res) => {
        const rawAvg = res.average;
        const hasRatings = res.count > 0 && !isNaN(rawAvg);
        this.averageRating = hasRatings ? Math.round(rawAvg * 10) / 10 : null;
        this.ratingsCount = res.count;
        this.averageStarsRounded = hasRatings ? Math.round(rawAvg) : 0;
      },
      error: () => {}
    });
  }

  setRating(stars: number): void {
    if (!this.userEmail || !this.game) return;
    if (!this.isBuyed) {
      this.ratingMessage = 'Solo puedes calificar juegos comprados';
      return;
    }
    if (this.myRating !== null) {
      this.ratingMessage = 'Ya calificaste este juego';
      return;
    }
    this.ratingLoading = true;
    // Capture non-null values to satisfy TypeScript across async boundaries
    const gameId = this.game!.videogameId;
    const userEmail = this.userEmail!;
    this.rateService.setRating(gameId, userEmail, stars).subscribe({
      next: (res) => {
        this.ratingLoading = false;
        this.ratingMessage = res.message || 'Calificación registrada';
        this.myRating = stars;
        // Refrescar promedio y conteo
        this.loadAverage(gameId);
      },
      error: (err) => {
        this.ratingLoading = false;
        this.ratingMessage = err.error?.error || 'No se pudo registrar la calificación';
      }
    });
  }

  getImageUrl(image: Image): string {
    if (image.image) return `data:image/png;base64,${image.image}`;
    return this.getPlaceholder(400, 300, 'Sin Imagen');
  }

  private getPlaceholder(width: number, height: number, text: string): string {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><defs><linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#1f2937;stop-opacity:1" /><stop offset="100%" style="stop-color:#111827;stop-opacity:1" /></linearGradient></defs><rect fill="url(#grad)" width="${width}" height="${height}"/><text x="50%" y="50%" font-size="24" fill="#6B7280" text-anchor="middle" dominant-baseline="middle">${text}</text></svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
  }

  previousImage(): void {
    if (this.gameImages.length > 0) {
      this.currentImageIndex = (this.currentImageIndex - 1 + this.gameImages.length) % this.gameImages.length;
    }
  }

  nextImage(): void {
    if (this.gameImages.length > 0) {
      this.currentImageIndex = (this.currentImageIndex + 1) % this.gameImages.length;
    }
  }

  goToImage(index: number): void {
    if (index >= 0 && index < this.gameImages.length) {
      this.currentImageIndex = index;
    }
  }

  getTotalCommentCount(): number {
    let count = 0;
    const countThreads = (threads: CommentThread[]) => {
      threads.forEach(thread => {
        count++;
        if (thread.replies && thread.replies.length > 0) {
          countThreads(thread.replies);
        }
      });
    };
    countThreads(this.commentThreads);
    return count;
  }

  toggleInstall(): void {
    if (!this.userEmail || !this.game) {
      this.installMessage = 'Error: Faltan datos para instalar';
      this.installSuccess = false;
      return;
    }
    if (!this.isInLibrary) {
      this.installMessage = 'Este juego no está en tu biblioteca.';
      this.installSuccess = false;
      return;
    }

    this.isInstalling = true;
    this.installMessage = null;
    this.installSuccess = false;

    const request$ = this.isInstalled
      ? this.libraryService.uninstallGame(this.userEmail, this.game.videogameId)
      : this.libraryService.installGame(this.userEmail, this.game.videogameId);

    request$.subscribe({
      next: (res: { message: string }) => {
        this.isInstalling = false;
        this.installMessage = res.message || (this.isInstalled ? 'Juego desinstalado' : 'Juego instalado exitosamente');
        this.isInstalled = !this.isInstalled;
        this.installSuccess = true;
      },
      error: (err: any) => {
        this.isInstalling = false;
        this.installMessage = err.error?.message || err.error?.error || 'Error al procesar la acción';
        this.installSuccess = false;
        console.error('Error al instalar/desinstalar', err);
      }
    });
  }

  openBuyModal(): void {
    if (!this.userEmail) {
      alert('Por favor inicia sesión para comprar juegos');
      return;
    }
    
    this.showBuyModal = true;
    this.buyMessage = null;
    this.buySuccess = false;
    
    // Cargar el wallet del usuario
    this.authService.getGamerInfo(this.userEmail).subscribe({
      next: (gamerInfo: any) => {
        this.currentWallet = gamerInfo.wallet;
      },
      error: (err: any) => {
        console.error('Error al cargar wallet', err);
        this.currentWallet = 0;
      }
    });
  }

  closeBuyModal(): void {
    this.showBuyModal = false;
    this.buyMessage = null;
    this.buySuccess = false;
  }

  completeBuy(): void {
    if (!this.userEmail || !this.game) {
      this.buyMessage = 'Error: Faltan datos para realizar la compra';
      this.buySuccess = false;
      return;
    }

    if (this.currentWallet !== null && this.currentWallet < this.game.price) {
      this.buyMessage = 'No tienes suficiente dinero en tu billetera';
      this.buySuccess = false;
      return;
    }

    this.isLoadingBuy = true;
    this.buyMessage = null;

    this.vgService.buyGame(this.game.videogameId, this.userEmail).subscribe({
      next: (response: any) => {
        this.isLoadingBuy = false;
        this.buyMessage = response.message || '¡Juego comprado exitosamente!';
        this.buySuccess = true;
        this.currentWallet = response.wallet;
        
        // Cerrar el modal después de 2 segundos
        setTimeout(() => {
          this.closeBuyModal();
        }, 2000);
      },
      error: (err: any) => {
        this.isLoadingBuy = false;
        this.buyMessage = err.error?.error || 'Error al procesar la compra';
        this.buySuccess = false;
        console.error('Error al comprar', err);
      }
    });
  }


  startReply(commentId: number): void {
    this.replyingTo = commentId;
  }

  submitComment(): void {
    if (!this.userEmail || !this.game) {
      this.commentsError = 'Debes iniciar sesión para comentar';
      return;
    }
    if (!this.newComment.trim()) {
      this.commentsError = 'Escribe un comentario';
      return;
    }

    this.commentsLoading = true;
    this.commentsError = null;
    const text = this.newComment.trim();
    const parentId = this.replyingTo !== null ? this.replyingTo : undefined;

    this.commentService.addComment(this.game.videogameId, this.userEmail, text, parentId).subscribe({
      next: () => {
        this.newComment = '';
        this.replyingTo = null;
        this.reloadComments();
      },
      error: (err) => {
        this.commentsLoading = false;
        this.commentsError = err.error?.message || err.error?.error || 'No se pudo enviar el comentario';
      }
    });
  }

  private reloadComments(): void {
    if (!this.game) return;
    this.commentService.getCommentsByGame(this.game.videogameId).pipe(
      catchError(() => of([] as any))
    ).subscribe(commentThreads => {
      this.commentThreads = commentThreads || [];
      this.commentsLoading = false;
    });
  }
}
