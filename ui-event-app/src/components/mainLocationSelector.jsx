import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { updateLocation } from "../redux/slices/locationSlice";
import { fetchAllCities, searchCities } from "../redux/slices/userSlice";
import { dismissToast, showToast } from "./toast";

export default function MainLocationSelector({ isOpen, onClose, onSelect }) {
  const dispatch = useDispatch();
  const [input, setInput] = useState("");
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState(null);
  const { citySearchResults, citySearchLoading } = useSelector(
    (state) => state.user,
  );

  const cities = [
    {
      label: "Ahmedabad, Gujarat",
      coords: [72.587265, 23.025793],
      name: "Ahmedabad",
      icon: "🏰",
    },
    {
      label: "New Delhi, Delhi",
      coords: [77.2, 28.6],
      name: "New Delhi",
      icon: "🕌",
    },
    {
      label: "Bengaluru, Karnataka",
      coords: [77.587106, 12.977063],
      name: "Bengaluru",
      icon: "🏯",
    },
    {
      label: "Chandigarh, Chandigarh",
      coords: [76.788398, 30.736292],
      name: "Chandigarh",
      icon: "🦅",
    },
    {
      label: "Chennai, Tamil Nadu",
      coords: [80.248357, 13.084622],
      name: "Chennai",
      icon: "🏛️",
    },
    {
      label: "Hyderabad, Telangana",
      coords: [78.456355, 17.384052],
      name: "Hyderabad",
      icon: "🏟️",
    },
    {
      label: "Kolkata, West Bengal",
      coords: [88.363044, 22.562627],
      name: "Kolkata",
      icon: "🏰",
    },
    {
      label: "Mumbai, Maharashtra",
      coords: [72.836447, 18.987807],
      name: "Mumbai",
      icon: "🏤",
    },
    {
      label: "Pune, Maharashtra",
      coords: [73.849852, 18.513271],
      name: "Pune",
      icon: "🏨",
    },
  ];

  const timeoutRef = useRef(null);

  // Debounced fetch with Nominatim API
  const debounceFetch = useMemo(() => {
    return (query) => {
      clearTimeout(timeoutRef.current);

      timeoutRef.current = setTimeout(() => {
        if (!query) {
          setOptions([]);
          return;
        }

        dispatch(searchCities(query)); // Redux fetch
      }, 300);
    };
  }, [dispatch]);

  useEffect(() => {
    if (citySearchResults?.length > 0) {
      setOptions(
        citySearchResults.map((c) => ({
          label: `${c.City}, ${c.State}`,
          coords: [c.Long, c.Lat],
        })),
      );
    } else {
      setOptions([]);
    }
  }, [citySearchResults]);

  // Trigger search when input changes
  useEffect(() => {
    if (input.length > 0) {
      debounceFetch(input);
    } else {
      setOptions([]);
    }
  }, [input, debounceFetch]);

  // 🔹 Close when location is selected
  useEffect(() => {
    if (selectedLocation) {
      onSelect(selectedLocation);
      onClose();
    }
  }, [selectedLocation, onSelect, onClose]);

  if (!isOpen) return null;

  async function handleSelect(option) {
    setSelectedLocation(option.label);
    setInput(option.label);
    setOptions([]);

    // send to backend
    if (option.coords) {
      const param = {
        lng: option.coords?.[0] ?? null,
        lat: option.coords?.[1] ?? null,
        label: option.label,
      };
      await dispatch(
        updateLocation({ lat: param.lat, lng: param.lng, label: param.label }),
      );
    }
  }

  // Handle current location
  function getDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Earth radius in km
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
  async function handleUseCurrentLocation() {
    if (navigator.geolocation) {
      const toastId = showToast({
        type: "loading",
        message: "Fetching nearest city under 100KM",
      });
      navigator.geolocation.getCurrentPosition(
        async (pos) => {
          const userLat = pos.coords.latitude;
          const userLng = pos.coords.longitude;

          try {
            const data = await dispatch(fetchAllCities()).unwrap();

            let nearestCity = null;
            let minDist = Infinity;

            data.forEach((c) => {
              const dist = getDistance(userLat, userLng, c.Lat, c.Long);
              if (dist < minDist) {
                minDist = dist;
                nearestCity = {
                  label: `${c.City}, ${c.State}`,
                  coords: [c.Long, c.Lat],
                };
              }
            });

            if (nearestCity && minDist <= 100) {
              setSelectedLocation(nearestCity.label);
              if (nearestCity.coords) {
                const param = {
                  lng: nearestCity.coords?.[0] ?? null,
                  lat: nearestCity.coords?.[1] ?? null,
                  label: nearestCity.label,
                };
                dispatch(
                  updateLocation({
                    lat: param.lat,
                    lng: param.lng,
                    label: param.label,
                  }),
                );
              }
              dismissToast(toastId);
              showToast({
                type: "success",
                message: `Nearest city found: ${nearestCity.label}`,
              });
            } else {
              showToast({
                type: "error",
                message: "Unknown Location",
              });
            }
          } catch (err) {
            console.error("Error fetching cities:", err);
            alert("Unable to match location.");
          }
        },
        (err) => {
          console.error("Geolocation error:", err);
          alert("Unable to fetch current location.");
        },
      );
    } else {
      alert("Geolocation not supported by your browser.");
    }
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm"
          onClick={onClose}
        >
          {/* Modal Container */}
          <motion.div
            initial={{ y: 40, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: 40, opacity: 0 }}
            transition={{ type: "spring", stiffness: 260, damping: 22 }}
            onClick={(e) => e.stopPropagation()}
            className="
    w-full max-w-2xl 
    bg-white rounded-3xl shadow-2xl 
    h-[83vh] overflow-hidden flex flex-col
  "
          >
            {/* Header */}
            <div className="p-4 md:p-6 border-b border-gray-200 relative">
              <h2 className="text-lg md:text-xl font-semibold text-gray-900 flex items-center gap-2">
                <span className="material-symbols-outlined text-pink-500 text-xl md:text-2xl">
                  location_on
                </span>
                Select Location
              </h2>

              {/* Close Button */}
              <button
                onClick={onClose}
                className="
      absolute right-3 md:right-5 
      top-3 md:top-5 
      text-gray-500 hover:text-gray-700 
      transition cursor-pointer
    "
              >
                <span className="material-symbols-outlined text-2xl md:text-3xl">
                  close
                </span>
              </button>
            </div>

            {/* Body */}
            <div className="p-4 md:p-6 flex-1 overflow-y-auto">
              {/* Search input */}
              <div className="mb-4 md:mb-5">
                <div className="relative">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-base md:text-lg">
                    search
                  </span>
                  <input
                    type="text"
                    value={input}
                    onChange={(e) => {
                      setInput(e.target.value);
                      setSelectedLocation(null);
                    }}
                    placeholder="Search city or area"
                    autoFocus
                    className="
            w-full border border-gray-300 rounded-lg md:rounded-xl 
            py-2.5 md:py-3 
            pl-10 md:pl-11 pr-3 md:pr-4 
            text-sm md:text-base text-gray-800
            focus:outline-none focus:ring-0 focus:border-gray-300
            transition-all placeholder-gray-400
          "
                  />
                </div>
              </div>

              {/* If Searching */}
              {input.length > 0 ? (
                <>
                  {loading ? (
                    <p className="text-gray-500 text-xs md:text-sm mt-3 md:mt-4">
                      Loading…
                    </p>
                  ) : options.length > 0 ? (
                    <ul className="space-y-1">
                      {options.slice(0, 8).map((opt, idx) => (
                        <li
                          key={idx}
                          onClick={() => handleSelect(opt)}
                          className="
                  flex items-center gap-3 
                  px-3 md:px-4 
                  py-2.5 md:py-3 
                  cursor-pointer rounded-lg md:rounded-xl
                  hover:bg-pink-50 transition-all
                "
                        >
                          <span className="material-symbols-outlined text-pink-600 text-xl md:text-2xl">
                            location_on
                          </span>
                          <span className="text-gray-800 text-sm md:text-base">
                            {opt.label}
                          </span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-gray-500 text-xs md:text-sm mt-3 md:mt-4">
                      No results found
                    </p>
                  )}
                </>
              ) : (
                <>
                  {/* Current Location */}
                  <button
                    type="button"
                    onClick={handleUseCurrentLocation}
                    className="
            flex items-center gap-2 
            text-pink-600 font-medium mb-5 md:mb-6
            text-sm md:text-base
            hover:underline underline-offset-2
          "
                  >
                    <span className="material-symbols-outlined text-pink-500 text-xl md:text-2xl">
                      my_location
                    </span>
                    Use Current Location
                  </button>

                  {/* Popular Cities */}
                  <div>
                    <h3 className="text-sm md:text-base font-semibold text-gray-700 mb-2 md:mb-3">
                      Popular Cities
                    </h3>

                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-3 md:gap-4">
                      {cities.map((city) => (
                        <button
                          key={city.name}
                          onClick={() => handleSelect(city)}
                          type="button"
                          className="
                  bg-white border border-gray-200 
                  rounded-lg md:rounded-xl 
                  p-3 md:p-4 
                  flex flex-col items-center
                  hover:border-pink-300 hover:bg-pink-50 
                  transition-all shadow-sm hover:shadow-md
                "
                        >
                          <span className="text-2xl md:text-3xl">
                            {city.icon}
                          </span>
                          <span className="text-xs md:text-sm font-medium text-gray-700 mt-1 md:mt-2">
                            {city.name}
                          </span>
                        </button>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
