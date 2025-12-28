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
import { adminGuard } from './core/admin.guard';
import { companyAdminGuard } from './core/company-admin.guard';

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
      { path: 'category/create', component: CategoryCreateComponent },
      { path: 'category/edit', component: CategoryEditComponent },
      { path: 'category/admin', component: CategoryAdminComponent },
      { path: 'category-edit-game/:id', component: CategoryEditGameComponent }
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
      { path: 'game/edit/:id', component: VideogameEditComponent }
    ]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
