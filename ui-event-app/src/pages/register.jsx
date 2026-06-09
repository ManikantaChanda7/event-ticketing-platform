import { signInWithPopup } from "firebase/auth";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useNavigate, useParams } from "react-router-dom";
import image from "../assets/images/logoGold.png";
// import { auth, googleProvider } from "../config/firebaseConfig";
import { loginOAuthUser, registerUser } from "../redux/slices/authSlice";

import { Eye, EyeOff } from "lucide-react"; // from lucide-react

export default function Register() {
  const { type } = useParams(); // 'user' or 'organizer'
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { isAuthenticated } = useSelector((state) => state.auth);

  const title = type === "organizer" ? "Organizer Sign Up" : "Create Account";
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [errors, setErrors] = useState({
    email: "",
    password: "",
    username: "",
  });

  const isFormValid =
    !errors.email &&
    !errors.password &&
    !errors.username &&
    formData.email.trim() &&
    formData.password.trim() &&
    formData.username.trim();

  const validateEmail = (email) => {
    if (!email.trim()) return "Email is required";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      return "Enter a valid email address";
    return "";
  };

  const validatePassword = (pass) => {
    if (!pass.trim()) return "Password is required";
    if (pass.length < 6) return "Password must be at least 6 characters";
    return "";
  };

  const validateUsername = (name) => {
    if (!name.trim()) return "Name is required";
    if (name.trim().length < 3) return "Name must be at least 3 characters";
    return "";
  };

  const handleInputChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (field === "email") {
      setErrors((prev) => ({
        ...prev,
        email: validateEmail(value),
      }));
    }

    if (field === "password") {
      setErrors((prev) => ({
        ...prev,
        password: validatePassword(value),
      }));
    }

    if (field === "username") {
      setErrors((prev) => ({
        ...prev,
        username: validateUsername(value),
      }));
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(registerUser({ formData }));
  };

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/home");
    }
  }, [isAuthenticated, navigate]);

  const handleGoogleLogin = async () => {
    try {
      const { auth, googleProvider } = await import(

    "../config/firebaseConfig"

  );
      const result = await signInWithPopup(auth, googleProvider);
      const idToken = await result.user.getIdToken();

      // Send idToken to backend
      dispatch(loginOAuthUser({ googleToken: idToken })).unwrap();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="flex min-h-screen overflow-hidden bg-gray-100">
      {/* Left side - Branding */}
      <div className="hidden md:flex flex-1 flex-col justify-center px-16 bg-gradient-to-br from-gray-900 via-black to-gray-800 text-white">
        <div className="mb-12 max-w-md">
          <div className="flex items-center gap-2 mb-8">
            <img src={image} alt="Eventify Logo" className="h-25 w-auto" />
          </div>

          <h2 className="text-4xl font-extrabold mb-4 leading-tight">
            Discover tailored events.
          </h2>
          <p className="text-lg text-gray-400 leading-relaxed">
            Sign up for personalized recommendations today!
          </p>
        </div>
      </div>

      {/* Right Side - Register Form */}
      <div className="flex flex-1 justify-center items-center p-6 overflow-auto">
        <div className="bg-white shadow-xl border border-gray-200 rounded-2xl p-10 w-full max-w-md">
          {/* Header */}
          <h2 className="text-3xl font-bold text-center mb-8 text-gray-800">
            Create Your <span className="text-blue-600">RezErvo</span> Account
          </h2>

          {/* Google Login */}
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
                Sign up with Google
              </span>
            </button>
          </div>

          {/* Divider */}
          <div className="flex items-center my-6">
            <hr className="flex-1 border-gray-300" />
            <span className="px-3 text-gray-500 text-sm">OR</span>
            <hr className="flex-1 border-gray-300" />
          </div>

          {/* Registration Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            {/* Full Name */}
            <input
              type="text"
              placeholder="Full Name"
              value={formData.username}
              onChange={(e) => handleInputChange("username", e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 placeholder-gray-400 transition-all"
              required
            />
            {errors.username && (
              <p className="text-red-500 text-sm">{errors.username}</p>
            )}

            {/* Email */}
            <input
              type="email"
              placeholder="E-mail Address"
              value={formData.email}
              onChange={(e) => handleInputChange("email", e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-4 py-3 text-gray-800 placeholder-gray-400 transition-all"
              required
            />
            {errors.email && (
              <p className="text-red-500 text-sm">{errors.email}</p>
            )}

            {/* Password */}
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
            {errors.password && (
              <p className="text-red-500 text-sm">{errors.password}</p>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={!isFormValid}
              className={`bg-blue-600 text-white font-semibold rounded-lg py-3 mt-2 
                transition-all shadow-sm
                ${
                  !isFormValid
                    ? "opacity-80 cursor-not-allowed"
                    : "hover:bg-blue-700"
                }`}
              // className="bg-blue-600 text-white font-semibold rounded-lg py-3 mt-2 hover:bg-blue-700 transition-all shadow-sm"
            >
              Create Account
            </button>
          </form>

          {/* Login Redirect */}
          <p className="text-center text-sm text-gray-600 mt-8">
            Already have an account?{" "}
            <Link
              to="/login"
              className="text-blue-600 font-semibold hover:underline"
            >
              Log In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
