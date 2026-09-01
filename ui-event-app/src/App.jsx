import "flowbite-datepicker";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import "./App.css";
import Footer from "./components/footer";
import Header from "./components/header";
import CreateEvent from "./pages/createEvent";
import HomePage from "./pages/homePage";
import Login from "./pages/login";
import Register from "./pages/register";
// import EventViewPage from "./pages/eventViewPage";
import CssBaseline from "@mui/material/CssBaseline";
import { ThemeProvider } from "@mui/material/styles";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import BecomeOrganizer from "./components/becomeOrganizer";
import EventSessions from "./components/eventSessions";
import PageLoader from "./components/pageLoader";
import ToastListener from "./components/toastListener";
import ToastProvider from "./components/toastProvider";
import CategoryEventsPage from "./pages/categoryEventsPage";
import Dashboard from "./pages/dashboard";
import EventDetailsPage from "./pages/eventViewPage";
import ManageEvent from "./pages/manageEvent";
import MyInterestsPage from "./pages/myInterests";
import MyTicketsPage from "./pages/myTickets";
import ProfileSettings from "./pages/organizerProfileSettings";
import OrganizerProfile from "./pages/organizerProfileView";
import PaymentPage from "./pages/paymentPage";
import SearchPage from "./pages/searchResult";
import Settings from "./pages/settings";
import ProtectedRoute from "./protectedRoutes/protectedRoute";
import ScrollToTop from "./protectedRoutes/scrollToTop";
import { fetchUserProfile } from "./redux/slices/authSlice";
import theme from "./theme/theme";
import {
  initializeTokenRefresh,
  clearRefreshSchedule,
} from "./utils/tokenRefresh";
function App() {
  const location = useLocation();
  const dispatch = useDispatch();

  const {
    similarLoading,
    filteredLoading,
    sessionsCountLoading,
    eventVenueLoading,
  } = useSelector((state) => state.event);
  const { sessionsLoading, sessionLoading } = useSelector(
    (state) => state.session,
  );
  const { organizerLoading } = useSelector((state) => state.organizerProfile);
  const { interestedEventsLoading } = useSelector((state) => state.auth);
  const { fetchBookingsLoading, fetchUserDetailsLoading } = useSelector(
    (state) => state.user,
  );
  const {
    basicLoading,
    salesLoading,
    categoriesLoading,
    topSellingLoading,
    upcomingLoading,
    manageEventPageLoading,
  } = useSelector((state) => state.organizerProfile);

  const popularLoading = filteredLoading?.popular;
  const recommendedLoading = filteredLoading?.recommended;
  const trendingLoading = filteredLoading?.trending;
  const featuredLoading = filteredLoading?.featured;

  const globalLoading =
    similarLoading ||
    popularLoading ||
    recommendedLoading ||
    trendingLoading ||
    featuredLoading ||
    sessionsCountLoading ||
    organizerLoading ||
    interestedEventsLoading ||
    sessionsLoading ||
    eventVenueLoading ||
    basicLoading ||
    salesLoading ||
    categoriesLoading ||
    topSellingLoading ||
    upcomingLoading ||
    manageEventPageLoading ||
    fetchBookingsLoading ||
    fetchUserDetailsLoading ||
    sessionLoading;

  const hideHeaderFooter = ["/", "/login", "/register"].includes(
    location.pathname,
  );
  const hideFooter =
    ["/", "/login", "/register", "/create-event"].includes(location.pathname) ||
    location.pathname.includes("/sessions");

  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken) {
      dispatch(fetchUserProfile());
      // ✅ Initialize proactive token refresh
      initializeTokenRefresh();
    }

    // Cleanup on unmount
    return () => {
      clearRefreshSchedule();
    };
  }, [dispatch]);
  return (
    <>
      <ToastProvider>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <ScrollToTop />
          {globalLoading && <PageLoader />}
          {!hideHeaderFooter && <Header />}
          <div
            style={{
              backgroundColor: "#f4f4f5",
            }}
          >
            <Routes>
              <Route path="/" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/login" element={<Login />} />
              <Route
                path="/home"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <HomePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/create-event"
                element={
                  <ProtectedRoute allowedRoles={["organizer", "ORGANIZER"]}>
                    <CreateEvent />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/event/:id"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <EventDetailsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/settings"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <Settings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/event/:id/sessions"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <EventSessions />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/payment"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <PaymentPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/register/organizer"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <BecomeOrganizer />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/search"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <SearchPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/category/:categoryName"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <CategoryEventsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/bookings"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <MyTicketsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/interests"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <MyInterestsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/organizer-profile"
                element={
                  <ProtectedRoute allowedRoles={["user", "USER"]}>
                    <OrganizerProfile />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute allowedRoles={["organizer", "ORGANIZER"]}>
                    <Dashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile-settings"
                element={
                  <ProtectedRoute allowedRoles={["organizer", "ORGANIZER"]}>
                    <ProfileSettings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/manage/event/:id"
                element={
                  <ProtectedRoute allowedRoles={["organizer", "ORGANIZER"]}>
                    <ManageEvent />
                  </ProtectedRoute>
                }
              />

              <Route
                path="*"
                element={
                  <ProtectedRoute
                    allowedRoles={["user", "USER", "organizer", "ORGANIZER"]}
                  >
                    {(() => {
                      const role = localStorage.getItem("role");
                      return role === "organizer" || role === "ORGANIZER" ? (
                        <Navigate to="/dashboard" replace />
                      ) : (
                        <Navigate to="/home" replace />
                      );
                    })()}
                  </ProtectedRoute>
                }
              />
            </Routes>
          </div>
          {!hideFooter && <Footer />}
          <ToastListener />
        </ThemeProvider>
      </ToastProvider>
    </>
  );
}

export default App;
