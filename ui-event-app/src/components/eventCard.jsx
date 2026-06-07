import { format } from "date-fns";
import { Bookmark, BookmarkCheck, MapPin } from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";
import { updateUserInterests } from "../redux/slices/authSlice";
import { setSelectedEvent } from "../redux/slices/eventSlice";

const EventCard = ({ item }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const { interestedEvents, interestedEventsLoading } = useSelector(
    (state) => state.auth,
  );

  const isMyInterests = location.pathname === "/interests";
  const bookmarked = interestedEvents?.includes(item?._id);
  const isPast = item?.status?.toUpperCase() === "COMPLETED";

  const handleClick = () => {
    dispatch(setSelectedEvent(item));
    navigate("/event/" + item._id);
    setTimeout(() => window.scrollTo(0, 0), 0);
  };

  const changeBookmarkStatus = async () => {
    await dispatch(updateUserInterests(item?._id));
  };

  const formatEventDateShort = () => {
    const start = new Date(item?.startDate);
    const end = item?.endDate ? new Date(item?.endDate) : null;
    const time = format(new Date(`2000-01-01T${item?.startTime}`), "h.mm a");

    switch (item?.recurrence) {
      case "single":
        return `${format(start, "EEE, MMM d")} • ${time}`;
      case "multi-day":
        return `${format(start, "MMM d")} – ${format(end, "MMM d")} • ${time}`;
      case "daily":
        return `Daily • ${time}`;
      case "weekly":
        return `Weekly • ${format(start, "MMM d")} – ${format(
          end,
          "MMM d",
        )} • ${time}`;
      default:
        return format(start, "EEE, MMM d");
    }
  };

  return (
    //     <div
    //       onClick={handleClick}
    //       className={`
    //     group rounded-2xl bg-white overflow-hidden shadow-sm
    //     hover:shadow-xl hover:-translate-y-2 transition-all duration-300
    //     w-full max-w-[330px] sm:max-w-[300px] lg:max-w-[290px]
    //  flex flex-col cursor-pointer
    //     ${isPast ? "opacity-75" : ""}
    //   `}
    //     >
    //       {/* IMAGE SECTION */}
    //       <div className="relative w-full aspect-[3/4]">
    //         <img
    //           src={item?.thumbnailImage}
    //           alt={item?.title}
    //           className={`
    //         absolute inset-0 w-full h-full object-cover
    //         transition-all duration-300 group-hover:scale-105
    //         ${isPast ? "grayscale brightness-90" : ""}
    //       `}
    //         />

    //         <div className="absolute inset-0 bg-gradient-to-t from-black/10 via-black/10 to-transparent pointer-events-none" />

    //         <div className="absolute top-5 left-5 right-5 flex items-start justify-between">
    //           {/* BOOKMARK — moved to LEFT */}
    //           <div className="relative group/bookmark">
    //             <button
    //               onClick={(e) => {
    //                 e.stopPropagation();
    //                 changeBookmarkStatus();
    //               }}
    //               disabled={interestedEventsLoading}
    //               className="
    //         p-2 rounded-full
    //         bg-black/40 backdrop-blur-sm text-white
    //         opacity-0 group-hover:opacity-100 transition duration-300 shadow-md cursor-pointer
    //       "
    //             >
    //               {bookmarked ? (
    //                 <BookmarkCheck className="w-5 h-5 text-[#ff512f]" />
    //               ) : (
    //                 <Bookmark className="w-5 h-5" />
    //               )}
    //             </button>

    //             {/* TOOLTIP */}
    //             <div
    //               className="
    //         absolute left-full top-1/2 -translate-y-1/2 ml-2
    //         px-2 py-1 rounded-md text-xs text-white whitespace-nowrap
    //         bg-black/70 shadow-lg
    //         opacity-0 group-hover/bookmark:opacity-100
    //         pointer-events-none
    //         transition-opacity duration-200 z-[2000]
    //       "
    //             >
    //               {bookmarked ? "Remove from interests" : "Save to interests"}
    //             </div>
    //           </div>

    //           {/* UPCOMING STATUS — moved to RIGHT */}
    //           {!isMyInterests && item?.status === "upcoming" && (
    //             <div className="px-2 py-[3px] text-[10px] font-semibold rounded-md text-white uppercase bg-gradient-to-r from-[#ff512f] to-[#dd2476] shadow-md">
    //               {item?.status}
    //             </div>
    //           )}
    //         </div>
    //       </div>

    //       {/* CONTENT */}
    //       <div className="p-4 flex flex-col flex-1">
    //         {/* Date */}
    //         <p
    //           className={`text-xs mb-1 font-medium tracking-wide ${
    //             isPast ? "text-gray-400" : "text-gray-500"
    //           }`}
    //         >
    //           {formatEventDateShort()}
    //         </p>

    //         {/* Title */}
    //         <h3
    //           className={`
    //         text-[16px] font-bold mb-2 leading-snug line-clamp-2
    //         ${
    //           isPast
    //             ? "text-gray-400"
    //             : "text-gray-800 group-hover:text-[#ff512f] transition-colors"
    //         }
    //       `}
    //         >
    //           {item?.title}
    //         </h3>

    //         {/* Location */}
    //         <div
    //           className={`flex items-center gap-1.5 mb-3 ${
    //             isPast ? "text-gray-400" : "text-gray-600"
    //           }`}
    //         >
    //           <MapPin className="w-4 h-4" />
    //           <p className="text-[13px] truncate">{item?.location.label}</p>
    //         </div>

    //         {/* PRICE */}
    //         <div className="mt-auto">
    //           <p
    //             className={`text-[14px] font-semibold ${
    //               isPast ? "text-gray-400" : "text-gray-700"
    //             }`}
    //           >
    //             ₹{item?.startingPrice}
    //             <span className="text-[12px] font-medium text-gray-400 ml-1">
    //               onwards
    //             </span>
    //           </p>
    //         </div>
    //       </div>
    //     </div>

    <div
      onClick={handleClick}
      className={`
    group rounded-2xl bg-white overflow-hidden shadow-sm 
    hover:shadow-xl hover:-translate-y-2 transition-all duration-300
    w-full max-w-[330px] sm:max-w-[300px] lg:max-w-[290px]
    flex flex-col cursor-pointer
    ${isPast ? "opacity-75" : ""}
  `}
    >
      {/* IMAGE */}
      <div className="relative w-full aspect-[3/4]">
        <img
          src={item?.thumbnailImage}
          alt={item?.title}
          className={`
        absolute inset-0 w-full h-full object-contain 
        transition-all duration-300 group-hover:scale-105
        ${isPast ? "grayscale brightness-90" : ""}
      `}
        />

        <div className="absolute inset-0 bg-gradient-to-t from-black/10 via-black/10 to-transparent pointer-events-none" />

        {/* Top badges */}
        <div className="absolute top-4 left-4 right-4 flex items-start justify-between">
          {/* Bookmark */}
          <div className="relative group/bookmark">
            <button
              onClick={(e) => {
                e.stopPropagation();
                changeBookmarkStatus();
              }}
              disabled={interestedEventsLoading}
              className="
            p-1.5 sm:p-2 rounded-full 
            bg-black/40 backdrop-blur-sm text-white 
            opacity-0 group-hover:opacity-100 transition duration-300 shadow-md cursor-pointer
          "
            >
              {bookmarked ? (
                <BookmarkCheck className="w-4 h-4 sm:w-5 sm:h-5 text-[#ff512f]" />
              ) : (
                <Bookmark className="w-4 h-4 sm:w-5 sm:h-5" />
              )}
            </button>

            {/* Tooltip */}
            <div
              className="
            absolute left-full top-1/2 -translate-y-1/2 ml-1.5 sm:ml-2
            px-1.5 py-0.5 rounded-md text-[10px] sm:text-xs text-white whitespace-nowrap
            bg-black/70 shadow-lg 
            opacity-0 group-hover/bookmark:opacity-100 
            pointer-events-none
            transition-opacity duration-200 z-[2000]
          "
            >
              {bookmarked ? "Remove from interests" : "Save to interests"}
            </div>
          </div>

          {/* Status */}
          {!isMyInterests && item?.status?.toUpperCase() === "UPCOMING" && (
            <div
              className="
          px-1.5 sm:px-2 py-[2px] 
          text-[9px] sm:text-[10px] font-semibold rounded-md 
          text-white uppercase 
          bg-gradient-to-r from-[#ff512f] to-[#dd2476] shadow-md
        "
            >
              Upcoming
            </div>
          )}
          {!isMyInterests && item?.status?.toUpperCase() === "COMPLETED" && (
            <div
              className="
          px-1.5 sm:px-2 py-[2px] 
          text-[9px] sm:text-[10px] font-semibold rounded-md 
          text-white uppercase 
          bg-gray-500 shadow-md
        "
            >
              Completed
            </div>
          )}
        </div>
      </div>

      {/* CONTENT */}
      <div className="p-3 sm:p-4 flex flex-col flex-1">
        {/* Date */}
        <p
          className={`
        text-[10px] sm:text-xs mb-1 
        font-medium tracking-wide
        ${isPast ? "text-gray-400" : "text-gray-500"}
      `}
        >
          {formatEventDateShort()}
        </p>

        {/* Title */}
        <h3
          className={`
        text-[13px] sm:text-[16px] font-bold mb-2 leading-snug line-clamp-2
        ${
          isPast
            ? "text-gray-400"
            : "text-gray-800 group-hover:text-[#ff512f] transition-colors"
        }
      `}
        >
          {item?.title}
        </h3>

        {/* Location */}
        <div
          className={`
        flex items-center gap-1.5 mb-2 sm:mb-3
        ${isPast ? "text-gray-400" : "text-gray-600"}
      `}
        >
          <MapPin className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          <p className="text-[11px] sm:text-[13px] truncate">
            {item?.location.label}
          </p>
        </div>

        {/* PRICE */}
        <div className="mt-auto">
          <p
            className={`
          text-[13px] sm:text-[14px] font-semibold
          ${isPast ? "text-gray-400" : "text-gray-700"}
        `}
          >
            ₹{item?.startingPrice}
            <span className="text-[11px] sm:text-[12px] font-medium text-gray-400 ml-1">
              onwards
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default EventCard;
