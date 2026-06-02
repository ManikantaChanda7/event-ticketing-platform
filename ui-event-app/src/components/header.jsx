import { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import brand from "../assets/images/brand6.png";
import { logout } from "../redux/slices/authSlice";
import { fetchLocation } from "../redux/slices/locationSlice";
import MainLocationSelector from "./mainLocationSelector";

const Header = () => {
  const dispatch = useDispatch();
  const location = useLocation();
  const navigate = useNavigate();

  const { location: userLocation } = useSelector((state) => state.location);
  const [activeTab, setActiveTab] = useState(location.pathname);
  const [isLocationOpen, setIsLocationOpen] = useState(false);
  const [selectedCity, setSelectedCity] = useState("city");
  const [selectedState, setSelectedState] = useState("state");
  const [selectedLocation, setSelectedLocation] = useState("New York");
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isAccountMenuOpen, setIsAccountMenuOpen] = useState(false);
  const accountRef = useRef(null);
  const accountMenuRef = useRef(null);

  const role = localStorage.getItem("role")?.toLowerCase();
  const tabs = [
    { label: "Home", to: "/home" },
    { label: "Interests", to: "/interests" },
    { label: "Tickets", to: "/bookings" },
  ];

  // close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (
        accountRef.current &&
        !accountRef.current.contains(e.target) &&
        accountMenuRef.current &&
        !accountMenuRef.current.contains(e.target)
      ) {
        setIsAccountMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    const getLocation = async () => {
      try {
        dispatch(fetchLocation());
      } catch (err) {
        console.error("Error fetching location:", err);
      }
    };

    getLocation();
  }, []);

  useEffect(() => {
    if (userLocation) {
      const loc = userLocation;

      if (loc && (loc.label || loc.locationLabel)) {
        const label = loc.label || loc.locationLabel;
        setSelectedLocation(label);
        const [city, state] = label.split(", ");
        setSelectedCity(city || "City");
        setSelectedState(state || "State");
      }
    }
  }, [userLocation]);

  useEffect(() => {
    setActiveTab(location.pathname);
  }, [location.pathname]);

  useEffect(() => {
    const [city, state] = selectedLocation?.split(", "); // split the "City, State" string
    setSelectedCity(city || "City");
    setSelectedState(state || "State");
  }, [selectedLocation]);

  const handleLogout = () => {
    dispatch(logout());
    window.location.href = "/login";
  };

  const onLogoClick = () => {
    const route = role === "user" ? "/home" : "/dashboard";
    navigate(route);
  };

  return (
    <>
      <header className="sticky top-0 z-[1100] bg-white shadow-md px-4 overflow-x-hidden">
        <div className="flex justify-between items-center px-4 sm:px-6 py-3">
          {/* LEFT: Brand + Divider + Location (desktop only) */}
          <div className="flex items-center gap-3">
            {/* Logo */}
            <button
              onClick={() => onLogoClick()}
              className="flex items-center gap-2 no-underline cursor-pointer"
            >
              <img src={brand} alt="Brand Logo" className="h-12  w-32" />
            </button>

            {/* Divider */}
            <div className="hidden lg:block h-8 w-px bg-gray-200"></div>

            {role === "user" && (
              <button
                className="flex items-center gap-3 bg-transparent font-medium text-gray-700 rounded cursor-pointer"
                onClick={() => setIsLocationOpen(!isLocationOpen)}
              >
                {/* Left: Location icon */}
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-6 h-6" // increased size
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  style={{ color: "#6755f2ff" }}
                >
                  <path d="M10 2C6.13 2 3 5.13 3 9c0 5.25 7 11 7 11s7-5.75 7-11c0-3.87-3.13-7-7-7zm0 16.55C7.26 15.57 4 12.1 4 9a6 6 0 1 1 12 0c0 3.1-3.26 6.57-6 9.55z" />
                  <circle cx="10" cy="9" r="2" />
                </svg>

                {/* Right: City / State vertically */}
                <div className="flex flex-col items-start">
                  <span className="text-md font-semibold text-black">
                    {selectedCity}
                  </span>
                  {/* larger + bold */}
                  <span className="text-xs text-gray-500">{selectedState}</span>
                  {/* slightly larger for readability */}
                </div>
              </button>
            )}
          </div>

          {/* MIDDLE: Tabs (desktop only) */}
          {role === "user" && (
            <nav className="hidden md:flex px-10 gap-5">
              {tabs.map((tab) => (
                <Link
                  key={tab.to}
                  to={tab.to}
                  onClick={() => setActiveTab(tab.to)}
                  className={`text-[#545459] px-3 sm:px-4 py-2 rounded-full  text-[15px] font-sans ${
                    activeTab === tab.to ? "bg-[#f9f4dc]" : ""
                  }`}
                  style={{ fontWeight: 650 }}
                >
                  {tab.label}
                </Link>
              ))}
            </nav>
          )}

          {/* MIDDLE: Search Bar (medium only) */}
          {role === "user" && (
            <div className="hidden md:flex lg:hidden flex-1 justify-center px-4">
              <Link
                to="/search"
                className="
      w-full max-w-md 
      flex items-center gap-2 
      border border-gray-300 
      px-3 py-2 
      rounded-lg cursor-pointer
    "
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-5 h-5 text-gray-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <circle cx="11" cy="11" r="6" strokeWidth={2} />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" strokeWidth={2} />
                </svg>
                <span className="text-gray-500 text-sm">Search events...</span>
              </Link>
            </div>
          )}

          {/* RIGHT: Search (desktop), Hamburger (sm+md), Account */}
          <div className="flex items-center gap-2">
            {/* Search (desktop only) */}
            {role === "user" && (
              <Link
                to="/search"
                className="hidden md:flex items-center gap-2 border border-gray-300 px-3 py-2 rounded-lg w-64 cursor-pointer"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-5 h-5 text-gray-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <circle cx="11" cy="11" r="6" strokeWidth={2} />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" strokeWidth={2} />
                </svg>
                <span className="text-gray-500 text-sm">Search events...</span>
              </Link>
            )}

            {/* Hamburger (sm + md only) */}
            <button
              className="md:flex lg:hidden p-2 rounded-md cursor-pointer"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="w-6 h-6 text-gray-700"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                {mobileMenuOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )}
              </svg>
            </button>

            {/* Account Icon + Dropdown */}
            {/* Account Icon */}
            <div className="relative" ref={accountRef}>
              <button
                className="hover:bg-gray-100 p-2 rounded-full cursor-pointer"
                onClick={() => setIsAccountMenuOpen((prev) => !prev)}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-7 h-7 text-gray-700"
                  fill="currentColor"
                  viewBox="0 0 24 24"
                >
                  <circle cx="12" cy="7.5" r="4.5" />
                  <path d="M21 21v-1a6 6 0 0 0-12 0v1" />
                </svg>
              </button>
            </div>

            {/* PORTAL DROPDOWN */}
            {isAccountMenuOpen && (
              <div
                ref={accountMenuRef}
                className="fixed top-[65px] right-4 w-40 bg-white shadow-xl rounded-lg border border-gray-200 py-2 z-[999999]"
              >
                <button
                  onClick={() => {
                    if (role === "user") {
                      navigate("/settings");
                    } else if (role === "organizer") {
                      navigate("/profile-settings");
                    }
                    setIsAccountMenuOpen(false);
                  }}
                  className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                >
                  Settings
                </button>

                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-100 cursor-pointer"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>

        {/* MOBILE NAV (sm + md only) */}
        {mobileMenuOpen && (
          <div
            className="
          fixed top-[64px] left-0 
          w-full 
          bg-white shadow-xl 
          p-4 
          flex flex-col gap-4 
          z-[9999]
          md:hidden
        "
          >
            {/* Searchbar (small only) */}
            <div className="sm:block md:hidden">
              <Link
                to="/search"
                onClick={() => setMobileMenuOpen(false)}
                className="
      w-full 
      flex items-center gap-2 
      border border-gray-300 
      px-3 py-2 
      rounded-lg 
      cursor-pointer
    "
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-5 h-5 text-gray-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <circle cx="11" cy="11" r="6" strokeWidth={2} />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" strokeWidth={2} />
                </svg>
                <span className="text-gray-500 text-sm">Search events...</span>
              </Link>
            </div>

            {/* Tabs */}
            {tabs.map((tab) => (
              <Link
                key={tab.to}
                to={tab.to}
                onClick={() => {
                  setActiveTab(tab.to);
                  setMobileMenuOpen(false);
                }}
                className={`text-[#545459] px-4 py-2 rounded-full font-bold text-[15px] font-sans ${
                  activeTab === tab.to ? "bg-[#f9f4dc]" : ""
                }`}
              >
                {tab.label}
              </Link>
            ))}

            {/* Location Selector (inside hamburger for sm+md) */}
            {role === "user" && (
              <button
                className="flex items-center gap-1 bg-transparent font-medium text-gray-700 py-2 rounded hover:bg-gray-100 cursor-pointer"
                onClick={() => setIsLocationOpen(!isLocationOpen)}
              >
                {selectedLocation}
              </button>
            )}
          </div>
        )}
      </header>
      {/* Location Selector Modal */}
      {isLocationOpen && (
        <div className="absolute left-0 top-16 w-full bg-white shadow-md z-2000 p-4">
          <MainLocationSelector
            isOpen={isLocationOpen}
            onClose={() => setIsLocationOpen(false)}
            onSelect={(loc) => setSelectedLocation(loc)}
          />
        </div>
      )}
    </>
  );
};

export default Header;
