import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { HomePage } from './pages/HomePage'
import { RootRedirect } from './pages/RootRedirect'
import { SellerLayout } from './layouts/SellerLayout'
import { ManagerLayout } from './layouts/ManagerLayout'
import { OrdersPage } from './pages/orders/OrdersPage'
import { OrderCreatePage } from './pages/orders/OrderCreatePage'
import { OrderEditPage } from './pages/orders/OrderEditPage'
import { OrderDeletePage } from './pages/orders/OrderDeletePage'
import { OrderVehicleDataPage } from './pages/orders/OrderVehicleDataPage'
import { OrderDetailPage } from './pages/orders/OrderDetailPage'
import { CarsPage } from './pages/cars/CarsPage'
import { ClientsPage } from './pages/clients/ClientsPage'
import { AccessoriesPage } from './pages/accessories/AccessoriesPage'
import { UsersPage } from './pages/users/UsersPage'
import { ClientCreatePage } from './pages/clients/ClientCreatePage'
import { ClientEditPage } from './pages/clients/ClientEditPage'
import { ClientDeletePage } from './pages/clients/ClientDeletePage'
import { CarCreatePage } from './pages/cars/CarCreatePage'
import { CarEditPage } from './pages/cars/CarEditPage'
import { CarDeletePage } from './pages/cars/CarDeletePage'
import { AccessoryCreatePage } from './pages/accessories/AccessoryCreatePage'
import { AccessoryEditPage } from './pages/accessories/AccessoryEditPage'
import { AccessoryDeletePage } from './pages/accessories/AccessoryDeletePage'
import { getAccessToken } from './api/tokenStorage'

function ProtectedLayout() {
  if (!getAccessToken()) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<ProtectedLayout />}>
        <Route index element={<RootRedirect />} />
        <Route path="seller" element={<SellerLayout />}>
          <Route index element={<OrdersPage />} />
          <Route path="orders/new" element={<OrderCreatePage />} />
          <Route path="orders/edit" element={<OrderEditPage />} />
          <Route path="orders/delete" element={<OrderDeletePage />} />
          <Route path="orders/vehicle-data" element={<OrderVehicleDataPage />} />
          <Route path="orders/detail" element={<OrderDetailPage />} />
          <Route path="cars" element={<CarsPage />} />
          <Route path="clients" element={<ClientsPage />} />
          <Route path="clients/new" element={<ClientCreatePage />} />
          <Route path="clients/edit" element={<ClientEditPage />} />
          <Route path="clients/delete" element={<ClientDeletePage />} />
        </Route>
        <Route path="manager" element={<ManagerLayout />}>
          <Route index element={<OrdersPage />} />
          <Route path="orders/new" element={<OrderCreatePage />} />
          <Route path="orders/edit" element={<OrderEditPage />} />
          <Route path="orders/delete" element={<OrderDeletePage />} />
          <Route path="orders/vehicle-data" element={<OrderVehicleDataPage />} />
          <Route path="orders/detail" element={<OrderDetailPage />} />
          <Route path="cars" element={<CarsPage />} />
          <Route path="cars/new" element={<CarCreatePage />} />
          <Route path="cars/edit" element={<CarEditPage />} />
          <Route path="cars/delete" element={<CarDeletePage />} />
          <Route path="clients" element={<ClientsPage />} />
          <Route path="clients/new" element={<ClientCreatePage />} />
          <Route path="clients/edit" element={<ClientEditPage />} />
          <Route path="clients/delete" element={<ClientDeletePage />} />
          <Route path="accessories" element={<AccessoriesPage />} />
          <Route path="accessories/new" element={<AccessoryCreatePage />} />
          <Route path="accessories/edit" element={<AccessoryEditPage />} />
          <Route path="accessories/delete" element={<AccessoryDeletePage />} />
          <Route path="users" element={<UsersPage />} />
        </Route>
        <Route path="home" element={<HomePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
