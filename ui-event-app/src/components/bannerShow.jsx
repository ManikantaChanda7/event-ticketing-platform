import { format } from "date-fns";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { setSelectedEvent } from "../redux/slices/eventSlice";

const BannerShow = ({ events: incomingEvents }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  let { filteredEvents } = useSelector((state) => state.event);

  const [index, setIndex] = useState(1); // start at first real slide
  const [isAnimating, setIsAnimating] = useState(true);
  const autoSlideRef = useRef(null);

  const events = incomingEvents?.length
    ? incomingEvents
    : filteredEvents?.featured?.data?.data || [];

  if (!events.length) {
    return null;
  }

  const slides = [events[events.length - 1], ...events, events[0]];
  const event = slides[index];

  useEffect(() => {
    startAutoSlide();
    return () => clearInterval(autoSlideRef.current);
  }, []);

  useEffect(() => {
    const handleVisibility = () => {
      if (document.visibilityState === "visible") {
        // Stop animation completely
        setIsAnimating(false);

        // Reset index safely to first real slide
        setIndex(1);

        // Restart autoslide fresh
        resetAutoSlide();
      } else {
        // Tab hidden → stop autoslide to avoid broken timers
        clearInterval(autoSlideRef.current);
      }
    };

    document.addEventListener("visibilitychange", handleVisibility);
    return () =>
      document.removeEventListener("visibilitychange", handleVisibility);
  }, []);

  // Go Prev
  const goToPrev = () => {
    resetAutoSlide();
    setIsAnimating(true);
    setIndex((prev) => prev - 1);
  };

  // Go Next
  const goToNext = () => {
    resetAutoSlide();
    setIsAnimating(true);
    setIndex((prev) => prev + 1);
  };

  // Infinite Loop Fix
  const handleTransitionEnd = () => {
    // If moved to cloneLast
    if (index === 0) {
      setIsAnimating(false);
      setIndex(events.length);
    }

    // If moved to cloneFirst
    if (index === events.length + 1) {
      setIsAnimating(false);
      setIndex(1);
    }
  };

  // Auto Slide
  const startAutoSlide = () => {
    autoSlideRef.current = setInterval(() => {
      setIsAnimating(true);
      setIndex((prev) => prev + 1);
    }, 5000);
  };

  const resetAutoSlide = () => {
    clearInterval(autoSlideRef.current);

    // prevent starting interval before DOM is stable
    setTimeout(() => startAutoSlide(), 50);
  };

  const formatEventDateShort = (item) => {
    const start = new Date(item?.startDate);
    const end = item?.endDate ? new Date(item?.endDate) : null;
    const time = format(new Date(`2000-01-01T${item?.startTime}`), "h.mm a");

    switch (item?.recurrence) {
      case "single":
        return `${format(start, "MMM d")} • ${time}`;
      case "multi-day":
        return `${format(start, "MMM d")} – ${format(end, "MMM d")} • ${time}`;
      case "daily":
        return `Daily • ${time}`;
      case "weekly":
        return item?.selectedWeekdays?.length
          ? `${format(start, "MMM d")} – ${format(
              end,
              "MMM d",
            )} • Weekly (${item?.selectedWeekdays.join(", ")})`
          : `${format(start, "MMM d")} – ${format(
              end,
              "MMM d",
            )} • Weekly • ${time}`;
      default:
        return format(start, "EEE, MMM d");
    }
  };

  const categoryLocationMap = {
    Music: (location) => `Live Music in ${location}`,
    Sports: (location) => `Sports Event in ${location}`,
    Workshops: (location) => `Workshop at ${location}`,
    Conferences: (location) => `Conference in ${location}`,
    Festivals: (location) => `Festival in ${location}`,
    "Tech & Innovation": (location) => `Tech Event in ${location}`,
    Charity: (location) => `Charity Event in ${location}`,
    Comedy: (location) => `Comedy Show in ${location}`,
    Exhibitions: (location) => `Exhibition in ${location}`,
  };

  const formatCategoryLocation = (category, location) => {
    const formatter = categoryLocationMap[category];
    return formatter ? formatter(location) : `${category} in ${location}`;
  };

  const handleClick = () => {
    dispatch(setSelectedEvent(event));
    navigate("/event/" + (event._id || event.id));
  };

  return (
    <div className="relative w-full flex items-center justify-center overflow-hidden">
      {/* ============================================ */}
      {/* BACKGROUND FADE LAYER (does NOT slide)     */}
      {/* ============================================ */}
      <div className="absolute inset-0 z-0">
        {events.map((bg, i) => (
          <div
            key={i}
            className={`absolute inset-0 transition-opacity duration-700 ease-in-out`}
            style={{
              backgroundImage: `url(${bg.thumbnailImage})`,
              backgroundSize: "cover",
              backgroundPosition: "center",
              opacity: index === i + 1 ? 1 : 0,
            }}
          >
            <div className="absolute inset-0 bg-gradient-to-t from-white/100 to-white/40 backdrop-blur-lg pointer-events-none" />
          </div>
        ))}
      </div>

      {/* ============================================ */}
      {/* FOREGROUND SLIDE TRACK (slides horizontally) */}
      {/* ============================================ */}
      <div className="relative w-full">
        <div
          className={`flex ${
            isAnimating ? "transition-transform duration-1000 ease-in-out" : ""
          }`}
          style={{
            transform: `translateX(-${index * 100}%)`,
          }}
          onTransitionEnd={handleTransitionEnd}
        >
          {slides.map((ev, idx) => (
            <div
              key={idx}
              className="relative w-full flex-shrink-0 flex items-center justify-center"
            >
              {/* Background removed (fade layer powers it) */}

              {/* Content */}
              <div className="relative z-10 w-full max-w-7xl px-6 sm:px-8 md:px-10 pt-15 pb-15">
                {/* Desktop */}
                <div className="hidden md:flex items-center justify-center w-full">
                  <div className="w-3/5 pr-6 lg:pr-12">
                    {ev && (
                      <p className="text-sm sm:text-base lg:text-lg font-semibold text-gray-900 mb-3">
                        {formatEventDateShort(ev)}
                      </p>
                    )}

                    <h1 className="text-2xl sm:text-4xl lg:text-5xl font-extrabold leading-tight text-gray-900 mb-5">
                      {ev?.title}
                    </h1>

                    <p className="text-base sm:text-xl lg:text-2xl font-semibold text-gray-800 mb-4">
                      {formatCategoryLocation(ev?.category, ev?.location.label)}
                    </p>

                    <p className="text-base sm:text-lg font-bold text-gray-900 mb-6">
                      ₹{ev?.startingPrice}{" "}
                      <span className="font-normal text-sm sm:text-base text-gray-600">
                        onwards
                      </span>
                    </p>

                    <button
                      onClick={handleClick}
                      className="mt-2 px-6 sm:px-8 lg:px-10 py-3 sm:py-4 bg-black text-white text-sm sm:text-base lg:text-lg font-semibold rounded-xl hover:bg-gray-800 transition cursor-pointer"
                    >
                      Book tickets
                    </button>
                  </div>

                  <div className="w-2/5 flex items-end justify-end">
                    <div className="rounded-2xl overflow-hidden shadow-lg w-48 sm:w-56 md:w-72 lg:w-80 aspect-[3/4]">
                      <img
                        src={ev?.thumbnailImage}
                        alt=""
                        className="w-full h-full object-cover"
                      />
                    </div>
                  </div>
                </div>

                {/* Mobile */}
                <div className="flex flex-col items-center justify-center w-full md:hidden gap-4">
                  <div className="rounded-2xl overflow-hidden shadow-2xl w-64 max-w-[90%] aspect-[3/4]">
                    <img
                      src={ev?.thumbnailImage}
                      alt=""
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <h2 className="text-base sm:text-lg font-bold text-gray-900 text-center">
                    {ev?.title}
                  </h2>
                  <button
                    onClick={handleClick}
                    className="px-4 sm:px-6 py-2 sm:py-3 bg-black text-white rounded-xl font-semibold text-sm sm:text-base hover:bg-gray-800 transition cursor-pointer"
                  >
                    Book tickets
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Prev / Next Large */}
      <button
        onClick={goToPrev}
        className="absolute top-1/2 left-8 -translate-y-1/2 hidden xl:flex items-center justify-center w-10 h-10 rounded-full hover:shadow-md z-20 cursor-pointer"
      >
        <ChevronLeft className="w-6 h-6 text-gray-800" />
      </button>

      <button
        onClick={goToNext}
        className="absolute top-1/2 right-8 -translate-y-1/2 hidden xl:flex items-center justify-center w-10 h-10 rounded-full hover:shadow-md z-20 cursor-pointer"
      >
        <ChevronRight className="w-6 h-6 text-gray-800" />
      </button>

      {/* Mobile Controls */}
      <div className="absolute z-30 flex items-center gap-3 bottom-4 left-1/2 -translate-x-1/2 xl:hidden">
        <button
          onClick={goToPrev}
          className="flex items-center justify-center w-8 h-8 rounded-full hover:shadow-md cursor-pointer"
        >
          <ChevronLeft className="w-4 h-4 text-gray-800" />
        </button>

        <div className="flex gap-2">
          {events.map((_, idx) => (
            <div
              key={idx}
              className={`transition-all duration-300 ${
                idx + 1 === index
                  ? "w-6 h-2 rounded-full bg-black"
                  : "w-2 h-2 rounded-full bg-gray-300"
              }`}
            />
          ))}
        </div>

        <button
          onClick={goToNext}
          className="flex items-center justify-center w-8 h-8 rounded-full hover:shadow-md cursor-pointer"
        >
          <ChevronRight className="w-4 h-4 text-gray-800" />
        </button>
      </div>

      {/* Desktop Dots */}
      <div className="absolute z-30 flex items-center gap-2 bottom-3 left-1/2 -translate-x-1/2 hidden xl:flex">
        {events.map((_, idx) => (
          <div
            key={idx}
            className={`transition-all duration-300 ${
              idx + 1 === index
                ? "w-6 h-2 rounded-full bg-black"
                : "w-2 h-2 rounded-full bg-gray-300"
            }`}
          />
        ))}
      </div>
    </div>
  );
};

export default BannerShow;
