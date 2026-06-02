import TuneSharpIcon from "@mui/icons-material/TuneSharp";
import { AnimatePresence, motion } from "framer-motion";
import { Search } from "lucide-react";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useLocation } from "react-router-dom";
import EventCard from "../components/eventCard";
import FilterModal from "../components/filterModel";
import TicketLoader from "../components/loader";
import {
  fetchFilteredEvents,
  setFiltersApplied,
} from "../redux/slices/eventSlice";

const SearchPage = ({ defaultCategory }) => {
  const dispatch = useDispatch();
  const category = defaultCategory;

  const [search, setSearch] = useState("");
  const [inputValue, setInputValue] = useState("");
  const [open, setOpen] = useState(false);
  const [filters, setFilters] = useState({
    recurrence: "",
    category: [],
    ageLimit: "",
    language: [],
    rating: null,
    location: false,
    dateRange: { start: "", end: "" },
    timeRange: { start: "", end: "" },
  });
  const [page, setPage] = useState(1);
  const [isActive, setIsActive] = useState();
  const [locationActive, setLocationActive] = useState(false);

  const { filteredEvents, loading, filteredLoading, filteredError } =
    useSelector((state) => state.event);

  const totalPages = filteredEvents?.searchEvents?.data?.totalPages || 1;
  const totalItems = filteredEvents?.searchEvents?.data?.totalItems || 0;
  const chipRanges = {
    Today: () => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return { start: formatDate(today), end: formatDate(today) };
    },
    Tomorrow: () => {
      const tomorrow = new Date();
      tomorrow.setDate(new Date().getDate() + 1);
      return { start: formatDate(tomorrow), end: formatDate(tomorrow) };
    },
    "This Weekend": () => {
      const today = new Date();
      const weekendEnd = new Date(today);
      weekendEnd.setDate(today.getDate() + 7);
      return { start: formatDate(today), end: formatDate(weekendEnd) };
    },
  };

  useEffect(() => {
    const query = buildQuery();
    dispatch(
      fetchFilteredEvents({ params: query, key: "searchEvents" }),
    ).unwrap();
  }, [dispatch, filters, page, search, category]);

  const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const buildQuery = () => {
    const query = {
      page,
      limit: 8,
    };
    if (category) query.category = category;
    else if (filters.category.length)
      query.category = filters.category.join(",");
    if (search) query.search = search;
    if (filters.location) query.location = true;
    if (filters.recurrence) query.recurrence = filters.recurrence;
    if (filters.rating) query.minRating = filters.rating;
    if (filters.language.length) query.language = filters.language.join(",");
    if (filters.ageLimit) query.age = filters.ageLimit;
    if (filters.dateRange.start) query.startDate = filters.dateRange.start;
    if (filters.dateRange.end) query.endDate = filters.dateRange.end;
    if (filters.timeRange.start) query.startTime = filters.timeRange.start;
    if (filters.timeRange.end) query.endTime = filters.timeRange.end;

    return query;
  };

  const handleLocationChip = () => {
    setLocationActive(!locationActive);
    setFilters((prev) => ({
      ...prev,
      location: !prev.location,
    }));
    setPage(1);
  };

  const handleChipClick = (label) => {
    if (isActive === label) {
      // Chip already active → remove filter
      setIsActive(null);
      setFilters((prev) => ({
        ...prev,
        dateRange: { start: "", end: "" },
      }));
    } else {
      // New chip clicked → apply filter
      setIsActive(label);
      setFilters((prev) => ({
        ...prev,
        dateRange: chipRanges[label]
          ? chipRanges[label]()
          : { start: "", end: "" },
      }));
    }
    setPage(1);
  };

  const countAppliedFilters = () => {
    let count = 0;
    if (filters.recurrence) count++;
    if (filters.category.length) count++;
    if (filters.ageLimit) count++;
    if (filters.language.length) count++;
    if (filters.rating) count++;
    if (filters.dateRange.start || filters.dateRange.end) count++;
    if (filters.timeRange.start || filters.timeRange.end) count++;
    return count;
  };

  const handlePrevPage = () => setPage((prev) => prev - 1);
  const handleNextPage = () => setPage((prev) => prev + 1);

  return (
    <div className="min-h-screen bg-white">
      <AnimatePresence mode="wait">
        {loading ? (
          <motion.div
            key="loader"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.5 }}
            className="flex items-center justify-center h-screen"
          >
            <TicketLoader />
          </motion.div>
        ) : (
          <motion.div
            key="content"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.5 }}
            className="max-w-[1440px] mx-auto px-1 sm:px-2 md:px-12 py-10"
          >
            {/* TOP SECTION */}
            <div className="flex flex-col gap-8">
              {/* ⭐ Search Bar */}
              <div className="flex justify-center">
                <div className="relative w-full max-w-3xl">
                  <input
                    type="text"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    placeholder="Search events by name, description, category..."
                    className="
    w-full pl-10 md:pl-12 pr-14 md:pr-16 
    py-2.5 md:py-3 
    rounded-lg md:rounded-xl
    text-sm md:text-base
    border border-gray-300 text-gray-800 placeholder-gray-400
    shadow-sm hover:shadow transition-all
    focus:outline-none focus:ring-2 focus:ring-pink-300/50
  "
                  />

                  {/* Search Icon */}
                  <Search className="w-5 h-5 text-gray-400 absolute left-4 top-1/2 -translate-y-1/2" />

                  {/* Go Button */}
                  <button
                    onClick={() => {
                      setSearch(inputValue);
                    }}
                    className="
    absolute right-2 top-1/2 -translate-y-1/2
    text-white text-xs md:text-sm 
    px-3 md:px-4 
    py-1 md:py-1.5 
    rounded-lg shadow-md
    bg-gradient-to-r from-[#ff512f] to-[#dd2476]
    hover:opacity-90 transition
  "
                  >
                    Search
                  </button>
                </div>
              </div>

              {/* ⭐ Events Header + Filters */}
              <div className="flex flex-col md:flex-row justify-between items-start md:items-center px-2 md:px-10 gap-4">
                {/* Result Count */}
                <h2 className="text-xl md:text-2xl font-bold text-gray-900 flex items-center gap-2">
                  <h2 className="text-xl md:text-2xl font-bold text-gray-900 flex items-center gap-2">
                    <span>{totalItems} Events Found</span>
                    {category && (
                      <span className="text-pink-600">{category}</span>
                    )}
                  </h2>
                </h2>

                {/* Filter Button + Chips */}
                <div className="flex items-center space-x-3">
                  {/* Filter Button */}
                  <button
                    onClick={() => setOpen(!open)}
                    className="
                    flex items-center gap-1.5 
                    px-2.5 md:px-3 
                    py-1 
                    rounded-md md:rounded-lg
                    border border-gray-300 shadow-sm bg-white
                    hover:bg-gray-100 transition
                    text-[10px] md:text-xs font-medium
"
                  >
                    {/* Gradient Icon */}
                    <TuneSharpIcon
                      sx={{
                        fontSize: 18,
                        color: "transparent",
                        fill: "url(#gradient)",
                      }}
                    />

                    <svg width="0" height="0">
                      <defs>
                        <linearGradient
                          id="gradient"
                          x1="0"
                          y1="0"
                          x2="1"
                          y2="0"
                        >
                          <stop offset="0%" stopColor="#ff512f" />
                          <stop offset="100%" stopColor="#dd2476" />
                        </linearGradient>
                      </defs>
                    </svg>

                    <span
                      className="bg-clip-text text-transparent"
                      style={{
                        backgroundImage:
                          "linear-gradient(90deg,#ff512f,#dd2476)",
                      }}
                    >
                      Filters
                    </span>

                    {countAppliedFilters() > 0 && (
                      <span className="bg-pink-600 text-white text-[10px] font-bold rounded-full px-1.5 py-[2px]">
                        {countAppliedFilters()}
                      </span>
                    )}
                  </button>

                  {/* Quick Filters */}
                  <div className="flex gap-2">
                    {["Today", "Tomorrow", "This Weekend", "Under 10KM"].map(
                      (label) => {
                        let active;
                        if (label === "Under 10KM") active = locationActive;
                        else active = isActive === label;

                        return (
                          <button
                            key={label}
                            onClick={() => {
                              if (label === "Under 10KM") {
                                handleLocationChip();
                              } else handleChipClick(label);
                              dispatch(setFiltersApplied(true));
                            }}
                            className={`
                              px-2.5 md:px-3 
                              py-1 
                              rounded-md md:rounded-lg
                              text-[10px] md:text-xs font-medium transition
                  ${
                    active
                      ? "text-white shadow-md border-0"
                      : "border border-gray-300 text-gray-600"
                  }
                `}
                            style={
                              active
                                ? {
                                    background:
                                      "linear-gradient(90deg,#ff512f,#dd2476)",
                                  }
                                : {}
                            }
                          >
                            {label}
                          </button>
                        );
                      },
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-10 mx-2 md:mx-8">
              {filteredLoading["searchEvents"] && (
                <div className="flex justify-center py-6">
                  <div className="animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-pink-500"></div>
                </div>
              )}

              <div className="grid justify-items-center grid-cols-2 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredEvents?.searchEvents?.data?.data?.map((event) => (
                  <EventCard key={event._id} item={event} />
                ))}
              </div>
            </div>

            {/* ⭐ PAGINATION */}
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
          </motion.div>
        )}
      </AnimatePresence>

      {/* Filter Modal */}
      <FilterModal
        isOpen={open}
        onApply={(newFilters) => {
          setFilters(newFilters);
          setPage(1); // reset page when filters applied
          dispatch(setFiltersApplied(true));
        }}
        onClose={() => setOpen(false)}
        selected={filters}
        setSelected={setFilters}
        isCategoryFilter={!!category}
        setIsActive={setIsActive}
        locationActive={locationActive}
      />
    </div>
  );
};

export default SearchPage;
