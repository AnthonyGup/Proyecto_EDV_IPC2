import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AdminLayoutComponent } from './components/admin-layout/admin-layout.component';
import { CompanyCreateComponent } from './components/company-create/company-create.component';
import { CompanyAdminCreateComponent } from './components/company-admin-create/company-admin-create.component';
import { CompanyAdminLayoutComponent } from './components/company-admin-layout/company-admin-layout.component';
import { CompanyAdminDashboardComponent } from './components/company-admin-dashboard/company-admin-dashboard.component';
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
      { path: 'company/create', component: CompanyCreateComponent }
    ]
  },
  {
    path: 'company/admin',
    component: CompanyAdminLayoutComponent,
    canActivate: [companyAdminGuard],
    children: [
      { path: '', component: CompanyAdminDashboardComponent },
      { path: 'create', component: CompanyAdminCreateComponent }
    ]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
