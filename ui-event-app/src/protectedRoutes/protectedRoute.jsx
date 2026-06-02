import { Navigate, useLocation } from "react-router-dom";

const ProtectedRoute = ({ children, allowedRoles }) => {
  const userId = localStorage.getItem("userId");
  const role = localStorage.getItem("role");
  const location = useLocation();

  // Not logged in: redirect to login
  if (!userId) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Role is not allowed for this route: redirect to home or unauthorized page
  if (allowedRoles && !allowedRoles.includes(role)) {
    if (role === "user" || role === "USER") {
      return <Navigate to="/home" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  // Authorized: render children
  return children;
};

export default ProtectedRoute;
