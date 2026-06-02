import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useState } from "react";
import EventCard from "./eventCard";

const EventGridShow = ({ title, events }) => {
  const [page, setPage] = useState(0);
  const [itemsPerPage, setItemsPerPage] = useState(1);

  const eventsArray = Array.isArray(events) ? events : [];
  const totalPages = Math.ceil(eventsArray.length / itemsPerPage);

  const goPrev = () => page > 0 && setPage((p) => p - 1);
  const goNext = () => page < totalPages - 1 && setPage((p) => p + 1);

  const currentItems = eventsArray.slice(
    page * itemsPerPage,
    (page + 1) * itemsPerPage,
  );

  useEffect(() => {
    const updateItemsPerPage = () => {
      if (window.innerWidth >= 1536)
        setItemsPerPage(4); // 2xl
      else if (window.innerWidth >= 1280)
        setItemsPerPage(4); // xl
      else if (window.innerWidth >= 800)
        setItemsPerPage(3); // lg
      else if (window.innerWidth >= 400)
        setItemsPerPage(2); // md
      else setItemsPerPage(1); // sm
    };

    updateItemsPerPage();
    window.addEventListener("resize", updateItemsPerPage);
    return () => window.removeEventListener("resize", updateItemsPerPage);
  }, []);

  return (
    <div className={` `}>
      {/* Header */}
      <div className="flex items-center justify-between mb-8 px-8 max-sm:mb-6 max-sm:px-6">
        <h2
          className="
      text-xl md:text-2xl 
      font-bold text-gray-900 tracking-tight
    "
        >
          {title}
        </h2>
      </div>

      {/* Grid with floating arrows */}
      <div className="relative">
        {/* Prev button */}
        {page > 0 && (
          <button
            onClick={goPrev}
            className={`absolute -left-1 top-1/2 -translate-y-1/2 z-10
      flex items-center justify-center w-12 h-12 rounded-full
      backdrop-blur-sm text-white shadow-lg transition
      bg-black/40 hover:bg-black/70 cursor-pointer`}
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
        )}

        {/* Grid */}
        <div
          className="grid  justify-items-center  gap-6 px-6
          grid-cols-1 min-[400px]:grid-cols-2
 min-[800px]:grid-cols-3 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-4"
        >
          {currentItems.map((item, idx) => (
            <EventCard key={idx} item={item} />
          ))}
        </div>

        {/* Next button */}
        {page < totalPages - 1 && (
          <button
            onClick={goNext}
            className={`absolute -right-1 top-1/2 -translate-y-1/2 z-10
      flex items-center justify-center w-12 h-12 rounded-full
      backdrop-blur-sm text-white shadow-lg transition
      bg-black/40 hover:bg-black/70 cursor-pointer`}
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        )}
      </div>
    </div>
  );
};

export default EventGridShow;
