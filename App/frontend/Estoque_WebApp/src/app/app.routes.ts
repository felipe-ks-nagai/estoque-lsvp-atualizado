import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ManageLayoutComponent } from './shared/layouts/manage-layout/manage-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { loginGuard } from './core/guards/login.guard';
import { roleGuard } from './core/guards/role.guard';
import { inject } from '@angular/core';
import { AuthenticationService, ROLES } from './core/authentication/authentication.service';
import { UsersViewComponent } from './pages/user/users-view/users-view.component';
import { ManageViewComponent } from './pages/manage-view/manage-view.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CreateUserComponent } from './pages/user/create-user/create-user.component';
import { EditUserComponent } from './pages/user/edit-user/edit-user.component';
import { EditProductsComponent } from './pages/products/edit-products/edit-products.component';
import { ViewProductsComponent } from './pages/products/view-products/view-products.component';
import { CreateProductsComponent } from './pages/products/create-products/create-products.component';
import { CreateContainerComponent } from './pages/container/create-container/create-container.component';
import { ViewContainersComponent } from './pages/container/view-containers/view-containers.component';
import { The404PageComponent } from './shared/components/the404-page/the404-page.component';
//import { UnitInputComponent } from './pages/unit/unit-input/unit-input.component';
import { EditContainerComponent } from './pages/container/edit-container/edit-container.component';
import { CreateCategoryComponent } from './pages/category/create-category/create-category.component';
import { ViewCategoriesComponent } from './pages/category/view-categories/view-categories.component';
import { EditCategoryComponent } from './pages/category/edit-category/edit-category.component';
import { MovementInputComponent } from './pages/movements/movement-input/movement-input.component';
import { ScannerInputComponent } from './pages/movements/scanner-input/scanner-input.component';
import { CookDashboardComponent } from './pages/cook-dashboard/cook-dashboard.component';
import { KitchenOrderComponent } from './pages/request/kitchen-order/kitchen-order.component';
import { ViewUnitComponent } from './pages/units/view-unit/view-unit.component';
import { EditUnitComponent } from './pages/units/edit-unit/edit-unit.component';
import { ViewMovementsComponent } from './pages/movements/view/view-movements.component';
import { ViewCategoryComponent } from './pages/category/view-category/view-category.component';
import { PedingOrdersComponent } from './pages/request/peding-orders/peding-orders.component';
import { ViewKitchenUnitsComponent } from './pages/kitchen/view-kitchen-units/view-kitchen-units.component';
import { FinishOrderComponent } from './pages/request/finish-order/finish-order.component';
import { ReportComponent } from './pages/report/report.component';
import { ExpiredUnitViewComponent } from './pages/expired-unit-view/expired-unit-view.component';

// Atalhos de permissão (ADMINISTRADOR passa em todos)
const todos        = [roleGuard(ROLES.ESTOQUE, ROLES.COZINHA)]; // qualquer usuário logado
const somenteAdmin = [roleGuard()];
const estoque      = [roleGuard(ROLES.ESTOQUE)];   // aba Gestão + Movimentações
const cozinha      = [roleGuard(ROLES.COZINHA)];   // aba Gestão Cozinha

export const routes: Routes = [
   { path: 'login', component: LoginComponent },

   // Rota padrão: manda cada usuário para a página inicial da sua role
   { path: '', pathMatch: 'full', redirectTo: () => inject(AuthenticationService).getHomeRoute() },

   // ---- TODOS: Home e Relatórios ----
   { path: 'dashboard/stock', component: DashboardComponent, canActivate: todos },
   { path: 'reports', component: ReportComponent, canActivate: todos },

   // Unidades vencidas (aberta pelo aviso da Home) é gestão de estoque
   { path: 'expired-units', component: ExpiredUnitViewComponent, canActivate: estoque },

   // ---- COZINHA: Dashboard Cozinha + Gestão Cozinha ----
   { path: 'dashboard/cook', component: CookDashboardComponent, canActivate: cozinha },
   { path: 'kitchen/units/view', component: ViewKitchenUnitsComponent, canActivate: cozinha },
   { path: 'kitchen/request', component: KitchenOrderComponent, canActivate: cozinha },
   { path: 'kitchen/pending', component: PedingOrdersComponent, canActivate: cozinha },
   { path: 'fulfill/order/:id', component: FinishOrderComponent, canActivate: cozinha },

   // ---- Rotas de gerenciamento ----
   { path: 'manage', component: ManageLayoutComponent,
        children: [
            { path: '', redirectTo: 'view', pathMatch: 'full' },
            { path: 'view', component: ManageViewComponent, canActivate: somenteAdmin },

            // Usuários (ADMINISTRADOR)
            { path: 'view/users', component: UsersViewComponent, canActivate: somenteAdmin },
            { path: 'create/user', component: CreateUserComponent, canActivate: somenteAdmin },
            { path: 'edit/user/:id', component: EditUserComponent, canActivate: somenteAdmin },

            // Gestão (ESTOQUE)
            { path: 'view/products', component: ViewProductsComponent, canActivate: estoque },
            { path: 'create/product', component: CreateProductsComponent, canActivate: estoque },
            { path: 'edit/products/:id', component: EditProductsComponent, canActivate: estoque },

            { path: 'view/containers', component: ViewContainersComponent, canActivate: estoque },
            { path: 'create/container', component: CreateContainerComponent, canActivate: estoque },
            { path: 'edit/container/:id', component: EditContainerComponent, canActivate: estoque },

            { path: 'view/category/:id', component: ViewCategoryComponent, canActivate: estoque },
            { path: 'view/categories', component: ViewCategoriesComponent, canActivate: estoque },
            { path: 'create/category', component: CreateCategoryComponent, canActivate: estoque },
            { path: 'edit/category/:id', component: EditCategoryComponent, canActivate: estoque },

            { path: 'view/units', component: ViewUnitComponent, canActivate: estoque },
            { path: 'edit/unit/:id', component: EditUnitComponent, canActivate: estoque },

            // Movimentações (ESTOQUE)
            { path: 'view/movements', component: ViewMovementsComponent, canActivate: estoque },
            { path: 'movements/scan', component: ScannerInputComponent, canActivate: estoque },
            { path: 'movements/input', component: MovementInputComponent, canActivate: estoque },
        ],
   },

   { path: '**', component: The404PageComponent }, // 404
];
