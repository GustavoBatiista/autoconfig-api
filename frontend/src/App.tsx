import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { HomePage } from './pages/HomePage'
import { RootRedirect } from './pages/RootRedirect'
import { SellerLayout } from './layouts/SellerLayout'
import { ManagerLayout } from './layouts/ManagerLayout'
import { OrdersPage } from './pages/orders/OrdersPage'
import { OrderCreatePage } from './pages/orders/OrderCreatePage'
import { CarsPage } from './pages/cars/CarsPage'
import { ClientsPage } from './pages/clients/ClientsPage'
import { AccessoriesPage } from './pages/accessories/AccessoriesPage'
import { VehicleStockPage } from './pages/vehicles/VehicleStockPage'
import { VehicleEntryCreatePage } from './pages/vehicles/VehicleEntryCreatePage'
import { UsersPage } from './pages/users/UsersPage'
import { ClientCreatePage } from './pages/clients/ClientCreatePage'
import { CarCreatePage } from './pages/cars/CarCreatePage'
import { AccessoryCreatePage } from './pages/accessories/AccessoryCreatePage'
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
          <Route path="cars" element={<CarsPage />} />
          <Route path="clients" element={<ClientsPage />} />
          <Route path="clients/new" element={<ClientCreatePage />} />
        </Route>
        <Route path="manager" element={<ManagerLayout />}>
          <Route index element={<OrdersPage />} />
          <Route path="orders/new" element={<OrderCreatePage />} />
          <Route path="cars" element={<CarsPage />} />
          <Route path="cars/new" element={<CarCreatePage />} />
          <Route path="clients" element={<ClientsPage />} />
          <Route path="clients/new" element={<ClientCreatePage />} />
          <Route path="accessories" element={<AccessoriesPage />} />
          <Route path="accessories/new" element={<AccessoryCreatePage />} />
          <Route path="vehicles" element={<VehicleStockPage />} />
          <Route path="vehicles/new" element={<VehicleEntryCreatePage />} />
          <Route path="users" element={<UsersPage />} />
        </Route>
        <Route path="home" element={<HomePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
