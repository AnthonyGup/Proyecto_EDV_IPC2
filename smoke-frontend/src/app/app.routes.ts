import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AdminLayoutComponent } from './components/admin-layout/admin-layout.component';
import { CompanyCreateComponent } from './components/company-create/company-create.component';
import { CompanyAdminCreateComponent } from './components/company-admin-create/company-admin-create.component';
import { CompanyAdminLayoutComponent } from './components/company-admin-layout/company-admin-layout.component';
import { CompanyAdminDashboardComponent } from './components/company-admin-dashboard/company-admin-dashboard.component';
import { CompanyCommentsDashboardComponent } from './components/company-comments-dashboard/company-comments-dashboard.component';
import { CompanyGamesDashboardComponent } from './components/company-games-dashboard/company-games-dashboard.component';
import { VideogameEditComponent } from './components/videogame-edit/videogame-edit.component';
import { GlobalCommissionComponent } from './components/global-commission/global-commission.component';
import { CompanyCommissionsComponent } from './components/company-commissions/company-commissions.component';
import { CategoryCreateComponent } from './components/category-create/category-create.component';
import { CategoryEditComponent } from './components/category-edit/category-edit.component';
import { CategoryAdminComponent } from './components/category-admin/category-admin.component';
import { CategoryEditGameComponent } from './components/category-edit-game/category-edit-game.component';
import { VideogameCreateComponent } from './components/videogame-create/videogame-create.component';
import { BannerAdminComponent } from './components/banner-admin/banner-admin.component';
import { ReportGlobalEarningsComponent } from './components/report-global-earnings/report-global-earnings.component';
import { ReportTopGamesComponent } from './components/report-top-games/report-top-games.component';
import { ReportCompanyEarningsComponent } from './components/report-company-earnings/report-company-earnings.component';
import { ReportUserRankingComponent } from './components/report-user-ranking/report-user-ranking.component';
import { CompanySalesReportComponent } from './components/company-sales-report/company-sales-report.component';
import { ReportCompanyFeedbackComponent } from './components/report-company-feedback/report-company-feedback.component';
import { CompanyTopGamesReportComponent } from './components/company-top-games-report/company-top-games-report.component';
import { GamerExpenseHistoryComponent } from './components/gamer-expense-history/gamer-expense-history.component';
import { GamerLibraryAnalysisComponent } from './components/gamer-library-analysis/gamer-library-analysis.component';
import { GamerFamilyLibraryComponent } from './components/gamer-family-library/gamer-family-library.component';
import { GamerLayoutComponent } from './components/gamer-layout/gamer-layout.component';
import { GamerDashboardComponent } from './components/gamer-dashboard/gamer-dashboard.component';
import { GameDetailComponent } from './components/game-detail/game-detail.component';
import { GameSearchResultsComponent } from './components/game-search-results/game-search-results.component';
import { GamerProfileComponent } from './components/gamer-profile/gamer-profile.component';
import { adminGuard } from './core/admin.guard';
import { companyAdminGuard } from './core/company-admin.guard';
import { gamerGuard } from './core/gamer.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'company/create', component: CompanyCreateComponent },
      { path: 'company/admin/create', component: CompanyAdminCreateComponent },
      { path: 'company/commissions', component: CompanyCommissionsComponent },
      { path: 'commission/global', component: GlobalCommissionComponent },
      { path: 'banner', component: BannerAdminComponent },
      { path: 'category/create', component: CategoryCreateComponent },
      { path: 'category/edit', component: CategoryEditComponent },
      { path: 'category/admin', component: CategoryAdminComponent },
      { path: 'category-edit-game/:id', component: CategoryEditGameComponent },
      { path: 'reports/global-earnings', component: ReportGlobalEarningsComponent },
      { path: 'reports/top-games', component: ReportTopGamesComponent },
      { path: 'reports/company-earnings', component: ReportCompanyEarningsComponent },
      { path: 'reports/user-ranking', component: ReportUserRankingComponent }
    ]
  },
  {
    path: 'company/admin',
    component: CompanyAdminLayoutComponent,
    canActivate: [companyAdminGuard],
    children: [
      { path: '', component: CompanyAdminDashboardComponent },
      { path: 'create', component: CompanyAdminCreateComponent },
      { path: 'game/create', component: VideogameCreateComponent },
      { path: 'comments', component: CompanyCommentsDashboardComponent },
      { path: 'games', component: CompanyGamesDashboardComponent },
      { path: 'game/edit/:id', component: VideogameEditComponent },
      { path: 'reports/sales', component: CompanySalesReportComponent },
      { path: 'reports/feedback', component: ReportCompanyFeedbackComponent },
      { path: 'reports/top-games', component: CompanyTopGamesReportComponent }
    ]
  },
  {
    path: 'gamer',
    component: GamerLayoutComponent,
    canActivate: [gamerGuard],
    children: [
      { path: '', component: GamerDashboardComponent },
      { path: 'game/:id', component: GameDetailComponent },
      { path: 'search', component: GameSearchResultsComponent },
      { path: 'company/search', loadComponent: () => import('./components/company-search-results/company-search-results.component').then(m => m.CompanySearchResultsComponent) },
      { path: 'gamers/search', loadComponent: () => import('./components/gamer-search-results/gamer-search-results.component').then(m => m.GamerSearchResultsComponent) },
      { path: 'gamers/:email', loadComponent: () => import('./components/gamer-public-profile/gamer-public-profile.component').then(m => m.GamerPublicProfileComponent) },
      { path: 'library', loadComponent: () => import('./components/gamer-library/gamer-library.component').then(m => m.GamerLibraryComponent) },
      { path: 'family-group/create', loadComponent: () => import('./components/family-group-create/family-group-create.component').then(m => m.FamilyGroupCreateComponent) },
      { path: 'family-group/invitations', loadComponent: () => import('./components/family-group-invitations/family-group-invitations.component').then(m => m.FamilyGroupInvitationsComponent) },
      { path: 'family-group/invite', loadComponent: () => import('./components/family-group-invite/family-group-invite.component').then(m => m.FamilyGroupInviteComponent) },
      { path: 'family-group/list', loadComponent: () => import('./components/family-group-list/family-group-list.component').then(m => m.FamilyGroupListComponent) },
      { path: 'company/:id', loadComponent: () => import('./components/company-profile/company-profile.component').then(m => m.CompanyProfileComponent) },
      { path: 'reports/expenses', component: GamerExpenseHistoryComponent },
      { path: 'reports/library-analysis', component: GamerLibraryAnalysisComponent },
      { path: 'reports/family-library', component: GamerFamilyLibraryComponent },
      { path: 'profile', component: GamerProfileComponent }
    ]
  },
  { path: 'game/:id', component: GameDetailComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
