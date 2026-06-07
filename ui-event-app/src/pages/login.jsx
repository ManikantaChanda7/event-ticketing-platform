import { signInWithPopup } from "firebase/auth";
import { Eye, EyeOff } from "lucide-react"; // from lucide-react
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import image from "../assets/images/logoGold.png";
import { auth, googleProvider } from "../config/firebaseConfig";
import { loginOAuthUser, loginUser } from "../redux/slices/authSlice";
import { toastRef } from "../components/toastProvider";

export default function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { isAuthenticated } = useSelector((state) => state.auth);

  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });
  const [errors, setErrors] = useState({ email: "" });

  const isFormValid =
    formData.email.trim() && !errors.email && formData.password.trim();

  const handleInputChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    if (field === "email") {
      const errorMsg = validateEmail(value);
      setErrors((prev) => ({ ...prev, email: errorMsg }));
    }
  };

  const validateEmail = (email) => {
    if (!email.trim()) return "Email is required";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      return "Enter a valid email address";
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    // Show toast immediately
    toastRef.current?.loading("Logging in...");

    try {
      await dispatch(loginUser(formData)).unwrap();

      // Replace loading with success message
      toastRef.current?.success("Login successful!");
    } catch (error) {
      // Replace loading with error
      toastRef.current?.error(error.message || "Login failed!");
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      const role = localStorage.getItem("role");
      navigate(
        role === "organizer" || role === "ORGANIZER" ? "/dashboard" : "/home",
      );
    }
  }, [isAuthenticated, navigate]);

  const handleGoogleLogin = async () => {
    try {
      toastRef.current?.loading("Signing in with Google...");

      const result = await signInWithPopup(auth, googleProvider);
      const idToken = await result.user.getIdToken();

      await dispatch(loginOAuthUser({ googleToken: idToken })).unwrap();

      toastRef.current?.success("Google Login successful!");
    } catch (err) {
      toastRef.current?.error("Google Login failed!");
      console.error(err);
    }
  };

  return (
    <div className="flex min-h-screen overflow-hidden bg-gray-100">
      {/* Left side - Branding */}
      <div className="hidden md:flex flex-1 flex-col justify-center px-16 bg-gradient-to-br from-gray-900 via-black to-gray-800 text-white relative">
        <div className="mb-12 max-w-md">
          <div className="flex items-center gap-2 mb-8">
            <img
              src={image} // replace with actual path
              alt="Eventify Logo"
              className="h-25 w-auto"
            />
          </div>

          <h2 className="text-4xl font-extrabold mb-4 leading-tight">
            Discover tailored events.
          </h2>
          <p className="text-lg text-gray-400 leading-relaxed">
            Sign up for personalized recommendations today!
          </p>
        </div>
      </div>

      {/* Right side - Form */}
      <div className="flex flex-1 justify-center items-center p-6 overflow-auto">
        <div className="bg-white shadow-xl border border-gray-200 rounded-2xl p-10 w-full max-w-md">
          {/* Header */}
          <h2 className="text-3xl font-bold text-center mb-8 text-gray-800">
            Sign In to <span className="text-blue-600">RezErvo</span>
          </h2>

          {/* Social login */}
          <div className="flex flex-col gap-4 mb-8">
            <button
              onClick={handleGoogleLogin}
              className="flex items-center justify-center gap-3 w-full py-3 rounded-lg border border-gray-300 bg-white shadow-sm hover:shadow-md transition-all duration-200"
            >
              <img
                src="https://www.svgrepo.com/show/355037/google.svg"
                alt="Google"
                className="w-5 h-5"
              />
              <span className="text-gray-700 font-medium">
                Continue with Google
              </span>
            </button>
          </div>

          {/* Divider */}
          <div className="flex items-center my-6">
            <hr className="flex-1 border-gray-300" />
            <span className="px-3 text-gray-500 text-sm">OR</span>
            <hr className="flex-1 border-gray-300" />
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            {/* Email Input */}
            <input
              type="email"
              placeholder="E-mail Address"
              value={formData.email}
              onChange={(e) => handleInputChange("email", e.target.value)}
              // className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 placeholder-gray-400 transition-all"
              className={`w-full rounded-lg border px-4 py-3 text-gray-800 
                placeholder-gray-400 transition-all 
                ${errors.email ? "border-red-500" : "border-gray-300"}`}
              required
            />
            {errors.email && (
              <p className="text-red-500 text-sm mt-1">{errors.email}</p>
            )}

            {/* Password Input */}
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                value={formData.password}
                onChange={(e) => handleInputChange("password", e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-4 py-3 pr-11 text-gray-800 placeholder-gray-400 transition-all"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-3 flex items-center text-gray-500 hover:text-gray-700"
              >
                {showPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>

            <button
              type="submit"
              disabled={!isFormValid}
              className={`bg-blue-600 text-white font-semibold rounded-lg py-3 mt-2 
                hover:bg-blue-700 transition-all shadow-sm
                ${!isFormValid ? "opacity-80 cursor-not-allowed" : ""}`}
              // className="bg-blue-600 text-white font-semibold rounded-lg py-3 mt-2 hover:bg-blue-700 transition-all shadow-sm"
            >
              Sign In
            </button>
          </form>

          {/* Signup link */}
          <p className="text-center text-sm text-gray-600 mt-8">
            Don&apos;t have an account?{" "}
            <Link
              to="/register"
              className="text-blue-600 font-semibold hover:underline"
            >
              Sign Up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
