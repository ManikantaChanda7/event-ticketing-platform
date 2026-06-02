import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import EventCard from "../components/eventCard";
import { fetchUserInterestedEvents } from "../redux/slices/eventSlice";
// import { Heart, Sparkles } from "lucide-react";
import {
  Activity,
  CalendarClock,
  CheckCircle,
  Heart,
  Sparkles,
} from "lucide-react";

export default function MyInterestsPage() {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const [tab, setTab] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState("ongoing");

  const userInterests = useSelector(
    (state) => state.event.filteredEvents?.userInterests,
  );

  const events = userInterests?.data?.data;
  const totalPages = userInterests?.data?.totalPages;
  const totalItems = userInterests?.data?.totalItems;
  const limit = 8;

  const TABS = [
    { label: "Ongoing", icon: <Activity className="w-4 h-4" /> },
    { label: "Upcoming", icon: <CalendarClock className="w-4 h-4" /> },
    { label: "Completed", icon: <CheckCircle className="w-4 h-4" /> },
  ];

  const handleChipClick = (label, idx) => {
    setTab(idx);
    if (label === "Ongoing") {
      setStatus("ongoing");
    } else if (label === "Upcoming") {
      setStatus("upcoming");
    } else {
      setStatus("completed");
    }
    setPage(1);
  };

  const buildQuery = () => {
    let query = {};
    if (page) query.page = page;
    if (limit) query.limit = limit;
    if (status) query.status = status;
    return query;
  };

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        await dispatch(
          fetchUserInterestedEvents({
            key: "userInterests",
            params: buildQuery(),
          }),
        ).unwrap();
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [page, status, dispatch]);

  const handlePrevPage = () => setPage((prev) => prev - 1);
  const handleNextPage = () => setPage((prev) => prev + 1);

  return (
    <div
      className="min-h-screen px-4 md:px-10 lg:px-20 py-10"
      style={{
        background:
          "radial-gradient(circle at top left, #f4e6ff 0%, #ffffff 45%, #ffffff 100%)",
      }}
    >
      {/* TOP HERO */}
      <div className="max-w-7xl mx-auto mb-8">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
          {/* Left: Title + subtitle */}
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#ff512f] to-[#dd2476] flex items-center justify-center shadow-lg">
              <Heart className="w-6 h-6 text-white fill-white/80" />
            </div>
            <div>
              <h1 className="text-3xl md:text-4xl font-extrabold text-gray-900 leading-tight">
                Your <span className="text-[#ff512f]">Interested</span> Events
              </h1>
              <p className="text-sm md:text-base text-gray-600 mt-1">
                Curated events you&apos;ve shown interest in. Keep track of what
                you don&apos;t want to miss.
              </p>
            </div>
          </div>

          {/* Right: Small stats pill */}
          {typeof totalItems === "number" && (
            <div className="flex items-center gap-2 bg-white/80 border border-gray-200 shadow-sm rounded-full px-4 py-2">
              <Sparkles className="w-4 h-4 text-[#dd2476]" />
              <span className="text-xs sm:text-sm text-gray-700">
                You&apos;re interested in{" "}
                <span className="font-semibold text-gray-900">
                  {totalItems} event{totalItems === 1 ? "" : "s"}
                </span>
              </span>
            </div>
          )}
        </div>
      </div>

      {/* MAIN CARD WRAPPER */}
      <div className="max-w-7xl mx-auto bg-white/80 rounded-3xl shadow-lg border border-gray-100 backdrop-blur-sm px-4 sm:px-6 md:px-8 py-6 md:py-8">
        {/* Tabs row */}
        <div
          className="
  flex flex-col
  items-center text-center lg:items-start lg:text-left
      /* center for mobile, sm, md */
  lg:flex-row lg:items-center lg:justify-between 
  gap-4 mb-6
"
        >
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-gray-400 mb-1">
              FILTER BY STATUS
            </p>
            <p className="text-sm text-gray-600">
              View events that are currently live, coming soon, or already
              completed.
            </p>
          </div>

          <div className="flex bg-gray-100 rounded-full shadow-inner p-1 w-fit mx-auto sm:mx-0">
            {TABS.map((item, idx) => {
              const active = tab === idx;
              return (
                <button
                  key={item.label}
                  onClick={() => handleChipClick(item.label, idx)}
                  className={`flex items-center gap-1.5 
                    px-3 py-1.5 text-xs        // Mobile (default)
                    sm:px-4 sm:py-2 sm:text-sm // Small screens
                    md:px-5 md:py-2 md:text-sm // Medium & above (normal size)
                    font-medium rounded-full transition-all duration-300
                    ${
                      active
                        ? "bg-gradient-to-r from-[#ff512f] to-[#dd2476] text-white shadow-md"
                        : "text-gray-600 hover:bg-gray-200"
                    }`}
                >
                  {item.icon}
                  {item.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Loading indicator */}
        {loading && (
          <div className="flex justify-center py-10">
            <div className="flex items-center gap-3 text-gray-500 text-sm">
              <span className="w-4 h-4 border-2 border-gray-300 border-t-[#ff512f] rounded-full animate-spin" />
              <span>Fetching your interested events…</span>
            </div>
          </div>
        )}

        {/* Empty state */}
        {!loading && events?.length === 0 && (
          <div className="flex flex-col items-center justify-center py-12 px-4">
            <img
              src="https://cdn-icons-png.flaticon.com/512/4076/4076503.png"
              alt="No Interests"
              className="w-28 h-28 opacity-90 mb-4 drop-shadow-sm"
            />
            <h2 className="text-lg md:text-xl font-bold text-gray-800 mb-1 text-center">
              No interests selected yet
            </h2>
            <p className="text-gray-500 text-center max-w-sm mb-5 text-sm">
              Explore events based on categories and discover what genuinely
              excites you. Tap the heart icon on any event to save it here.
            </p>
            <button
              onClick={() => navigate("/search")}
              className="px-5 py-2.5 rounded-full text-white text-sm font-medium
               bg-gradient-to-r from-[#ff512f] to-[#dd2476]
               hover:opacity-90 transition shadow-lg"
            >
              Discover Events
            </button>
          </div>
        )}

        {/* Event Cards Grid (UNCHANGED) */}
        {/* <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 items-stretch">
          {events?.length > 0 &&
            events?.map((item, idx) => <EventCard key={idx} item={item} />)}
        </div> */}

        <div
          className="
    grid
    grid-cols-2
    sm:grid-cols-2
    md:grid-cols-3
    lg:grid-cols-4
    gap-6
    items-stretch      /* ensures all cards are equal height */
  "
        >
          {events?.length > 0 &&
            events.map((item, idx) => <EventCard key={idx} item={item} />)}
        </div>

        {/* Pagination (UNCHANGED) */}
        {events?.length > 0 && (
          <div className="flex justify-center mt-10">
            <nav>
              <ul className="inline-flex gap-1 text-xs md:text-sm">
                {/* Prev */}
                <li>
                  <button
                    onClick={handlePrevPage}
                    disabled={page === 1}
                    className="
        px-2.5 md:px-4 
        h-8 md:h-10 
        flex items-center justify-center 
        rounded-md md:rounded-lg
        bg-white border border-gray-300 text-gray-600
        hover:bg-gray-100 disabled:opacity-50
      "
                  >
                    Previous
                  </button>
                </li>

                {/* Page Numbers */}
                {(() => {
                  const maxVisible = 7;
                  let start = Math.max(1, page - Math.floor(maxVisible / 2));
                  let end = start + maxVisible - 1;

                  if (end > totalPages) {
                    end = totalPages;
                    start = Math.max(1, end - maxVisible + 1);
                  }

                  return Array.from(
                    { length: end - start + 1 },
                    (_, i) => start + i,
                  ).map((num) => (
                    <li key={num}>
                      <button
                        onClick={() => setPage(num)}
                        aria-current={num === page ? "page" : undefined}
                        className={`
              px-2.5 md:px-4 
              h-8 md:h-10 
              flex items-center justify-center 
              rounded-md md:rounded-lg
              border transition
              ${
                num === page
                  ? "bg-pink-50 text-pink-600 border-pink-300"
                  : "bg-white text-gray-600 border-gray-300 hover:bg-gray-100"
              }
            `}
                      >
                        {num}
                      </button>
                    </li>
                  ));
                })()}

                {/* Next */}
                <li>
                  <button
                    onClick={handleNextPage}
                    disabled={page === totalPages}
                    className="
        px-2.5 md:px-4 
        h-8 md:h-10 
        flex items-center justify-center 
        rounded-md md:rounded-lg
        bg-white border border-gray-300 text-gray-600
        hover:bg-gray-100 disabled:opacity-50
      "
                  >
                    Next
                  </button>
                </li>
              </ul>
            </nav>
          </div>
        )}
      </div>
    </div>
  );
}
