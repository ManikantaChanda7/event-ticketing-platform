import { Bookmark, BookmarkCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import EventGridShow from "../components/eventGridShow";

import { format } from "date-fns";
import { AnimatePresence } from "framer-motion";
import { useDispatch, useSelector } from "react-redux";
import NoData from "../components/noData";
import { updateUserInterests } from "../redux/slices/authSlice";
import {
  fetchEventById,
  fetchEventReviews,
  fetchSessionsCount,
  fetchSimilarEvents,
  updateInterestedUsers,
  writeReview,
  setReviewSuccessStatus,
} from "../redux/slices/eventSlice";
import { fetchOrganizer } from "../redux/slices/organizerProfileSlice";
import { isUserBookedEvent } from "../redux/slices/userSlice";

export default function Component() {
  const navigate = useNavigate();
  const { id } = useParams();
  const dispatch = useDispatch();

  const { organizer } = useSelector((state) => state.organizerProfile);
  const { selectedEvent, filteredEvents, selectedEventSessionsAvailable } =
    useSelector((state) => state.event);
  const { interestedEvents } = useSelector((state) => state.auth);
  const { eventBooked } = useSelector((state) => state.user);

  const [copied, setCopied] = useState(false);

  const [showAll, setShowAll] = useState(false);
  const [filterRating, setFilterRating] = useState(null);
  const [sessionAvailable, setSessionAvailable] = useState(false);

  const [showReviewInput, setShowReviewInput] = useState(false);
  const [userReview, setUserReview] = useState("");
  const [userRating, setUserRating] = useState(0);

  const bookmarked = interestedEvents?.includes(selectedEvent?._id);

  // Filter reviews by rating if filter applied
  const filteredReviews = filterRating
    ? selectedEvent?.ratings.filter((r) => r.rating === filterRating)
    : selectedEvent?.ratings;

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
  };

  const changeBookmarkStatus = async () => {
    const res = await dispatch(updateUserInterests(selectedEvent?._id));
    dispatch(updateInterestedUsers(res.payload?.data?.interestedUsers));
  };

  const handleOrganizer = () => {
    navigate("/organizer-profile");
  };

  const formatEventDateShort = () => {
    if (!selectedEvent?.startDate) return "TBD";

    const start = new Date(selectedEvent?.startDate);
    const end = selectedEvent?.endDate
      ? new Date(selectedEvent?.endDate)
      : null;

    // Validate dates
    if (isNaN(start.getTime())) return "TBD";
    if (end && isNaN(end.getTime())) return "TBD";

    // const time = format(
    //   new Date(`2000-01-01T${selectedEvent?.startTime}`),
    //   "h.mm a",
    // );
    const weekdayOrder = [
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday",
    ];

    switch (selectedEvent?.recurrence) {
      case "single":
        return `${format(start, "EEE, MMM d yyyy")}`;
      case "multi-day":
        return `${format(start, "MMM d")} – ${format(end, "MMM d")}`;
      case "daily":
        return `Daily (${format(start, "MMM d")} – ${format(end, "MMM d")})`;
      case "weekly": {
        const days = selectedEvent?.selectedWeekdays?.length
          ? [...selectedEvent.selectedWeekdays]
              .sort((a, b) => weekdayOrder.indexOf(a) - weekdayOrder.indexOf(b)) // sort correctly
              .map((d) => d.slice(0, 3)) // convert to Mon, Tue, Wed...
              .join(", ")
          : null;

        if (start && end) {
          return days
            ? `Weekly (${format(start, "MMM d")} – ${format(
                end,
                "MMM d",
              )}) - ${days}`
            : `Weekly (${format(start, "MMM d")} – ${format(end, "MMM d")})`;
        }

        return days ? `Weekly — ${days}` : "Weekly";
      }

      default:
        return format(start, "EEE, MMM d");
    }
  };

  const formatTime = (timeString) => {
    if (!timeString) return "TBD";
    // Ensure it's always parsed into a Date
    const date = new Date(`2000-01-01T${timeString}`);
    if (isNaN(date.getTime())) return "TBD";
    return format(date, "h:mm a"); // → 1:30 PM, 9:00 AM
  };

  const handleSubmitReview = async () => {
    if (userRating === 0) {
      alert("Please select a rating");
      return;
    }

    try {
      await dispatch(
        writeReview({
          eventId: selectedEvent._id,
          rating: userRating,
          review: userReview,
        }),
      ).unwrap();

      // Refresh reviews for the event instead of updating entire event
      dispatch(fetchEventReviews(selectedEvent._id)).unwrap();

      // close form
      setShowReviewInput(false);
      setUserRating(0);
      setUserReview("");
    } catch (err) {
      console.error("Review submit error:", err);
    }
  };

  // Reset review success state on component mount to prevent stale toast
  useEffect(() => {
    dispatch(setReviewSuccessStatus(false));
  }, []);

  useEffect(() => {
    const fetchSessionCount = async () => {
      try {
        dispatch(fetchSessionsCount(selectedEvent._id)).unwrap();
      } catch (err) {
        console.error("Error fetching sessions count", err);
      }
    };

    if (selectedEvent?._id) {
      fetchSessionCount();
    }
  }, [selectedEvent?._id, dispatch]);

  // Fetch full event details by ID when component mounts
  useEffect(() => {
    if (id) {
      dispatch(fetchEventById(id)).unwrap();
    }
  }, [id, dispatch]);

  // Fetch reviews for the event
  useEffect(() => {
    if (selectedEvent?._id) {
      dispatch(fetchEventReviews(selectedEvent._id)).unwrap();
    }
  }, [selectedEvent?._id, dispatch]);

  useEffect(() => {
    // Determine if booking button should be enabled based on event status and sessions
    if (selectedEvent?.status?.toUpperCase() === "COMPLETED") {
      setSessionAvailable(false);
      return;
    }

    if (selectedEvent?.status?.toUpperCase() === "ONGOING") {
      setSessionAvailable(true);
      return;
    }

    // For UPCOMING events, check if any session has released tickets
    if (selectedEvent?.status?.toUpperCase() === "UPCOMING") {
      const now = new Date();
      const hasReleasedTickets = selectedEvent?.sessions?.some((session) => {
        if (!session?.releaseDate) return false;
        return new Date(session.releaseDate) <= now;
      });
      setSessionAvailable(hasReleasedTickets);
      return;
    }

    // Fallback to the old behavior for other statuses
    setSessionAvailable(selectedEventSessionsAvailable);
  }, [
    selectedEvent?.status,
    selectedEvent?.sessions,
    selectedEventSessionsAvailable,
  ]);

  useEffect(() => {
    const eventId = selectedEvent?._id;
    const organizerId =
      selectedEvent?.organizer?.id || selectedEvent?.organizer?._id;
    if (!eventId) return;
    if (organizerId) {
      dispatch(fetchOrganizer(organizerId)).unwrap();
    }
    dispatch(isUserBookedEvent(eventId)).unwrap();
    dispatch(
      fetchSimilarEvents({ key: "similarEvents", id: eventId }),
    ).unwrap();
  }, [dispatch, selectedEvent?._id]);

  return (
    <div
      className="font-sans min-h-screen p-4 md:p-6 lg:p-8"
      style={{
        background:
          "linear-gradient(to bottom, #fbf6e0ff 0%, #ffffff 20%, #ffffff 100%)",
      }}
    >
      <div className="max-w-7xl mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
          {/* LEFT COLUMN */}
          <div className="md:col-span-7 lg:col-span-8 space-y-6">
            {/* Banner */}
            <div
              className="
    bg-white rounded-xl shadow-lg overflow-hidden 
    h-56 sm:h-72 md:h-[380px] lg:h-[420px] 
    relative
  "
            >
              <div className="relative h-full overflow-hidden">
                <img
                  src={selectedEvent?.bannerImage}
                  alt={selectedEvent?.title}
                  className="w-full h-full object-cover"
                />

                {/* Bookmark Button */}
                <div className="absolute top-2 sm:top-3 right-2 sm:right-3 group">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      changeBookmarkStatus();
                    }}
                    className="
          p-1 sm:p-1.5 
          rounded-full bg-white/80 hover:bg-white 
          text-gray-800 cursor-pointer shadow-sm 
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
          absolute right-full top-1/2 -translate-y-1/2 mr-2 
          px-1.5 py-1 sm:px-2 sm:py-1 
          rounded-md text-[10px] sm:text-xs 
          text-white whitespace-nowrap
          bg-black/80 shadow-lg
          opacity-0 group-hover:opacity-100 
          pointer-events-none transition-opacity duration-200
        "
                  >
                    {bookmarked ? "Remove from interests" : "Save to interests"}
                  </div>
                </div>

                {/* Category Chip */}
                <div className="absolute bottom-2 sm:bottom-3 left-2 sm:left-3">
                  <span
                    className="
          bg-primary-500/90 backdrop-blur-sm 
          text-white 
          px-2 sm:px-3 
          py-0.5 sm:py-1
          rounded-full 
          text-xs sm:text-sm 
          font-medium 
          shadow-lg
        "
                  >
                    {selectedEvent?.category}
                  </span>
                </div>
              </div>
            </div>

            {/* Title + Description */}
            <div className="bg-white rounded-2xl px-3 sm:px-4 md:px-3 pb-3 sm:pb-4">
              {/* Interested Count */}
              <div className="flex justify-end items-center gap-1.5 sm:gap-2">
                <span className="material-symbols-rounded text-xs sm:text-sm text-green-500">
                  thumb_up
                </span>
                <span className="text-black/90 text-xs sm:text-sm md:text-sm font-medium">
                  {selectedEvent?.interestedUsers} are interested
                </span>
              </div>

              {/* Title */}
              <h1
                className="
      text-2xl sm:text-3xl md:text-4xl 
      font-extrabold text-gray-900 
      mb-2 sm:mb-3 
      tracking-tight
    "
              >
                {selectedEvent?.title}
              </h1>

              {/* Subtitle */}
              <p className="text-sm sm:text-base md:text-lg text-gray-600 mb-4 sm:mb-6 italic">
                Join us for an exciting day of this event
              </p>

              {/* Divider */}
              <div
                className="w-20 sm:w-24 h-1 rounded-full mb-4 sm:mb-6"
                style={{
                  background: "linear-gradient(90deg, #ff512f, #dd2476)",
                }}
              ></div>

              {/* Description Header */}
              <h2
                className="
      text-xl sm:text-2xl font-bold 
      mb-3 sm:mb-4 
      text-gray-800 
      relative
    "
              >
                Event Description
              </h2>

              {/* Description Text */}
              <p className="text-gray-700 leading-relaxed text-justify text-sm sm:text-base">
                {selectedEvent?.description}
              </p>
            </div>

            {/* Organizer */}
            <div className="bg-white rounded-2xl shadow-md p-6 relative max-sm:p-4">
              {/* View Profile Button */}
              <button
                onClick={handleOrganizer}
                className="
      absolute 
      top-6 right-6 
      flex items-center gap-1 
      px-3 py-1.5 
      rounded-full 
      text-sm font-medium 
      text-primary-600 
      hover:text-white hover:bg-gradient-to-r hover:from-[#ff512f] hover:to-[#dd2476]
      transition-all shadow-sm cursor-pointer
      max-sm:text-xs max-sm:px-2.5 max-sm:py-1
    "
              >
                <span className="material-symbols-outlined text-base max-sm:text-sm">
                  person
                </span>
                View Profile
              </button>

              {/* Section Title */}
              <h2 className="text-2xl font-bold mb-4 text-gray-800 max-sm:text-xl max-sm:mb-3">
                Organizer
              </h2>

              {/* Organizer Block */}
              <div className="flex gap-4 max-sm:gap-3">
                {/* Avatar */}
                <div className="w-10 h-10 min-w-10 rounded-full overflow-hidden bg-gray-200 max-sm:w-9 max-sm:h-9">
                  <img
                    src={
                      "https://ui-avatars.com/api/?background=random&name=" +
                      organizer?.orgName
                    }
                    onError={(e) => {
                      e.currentTarget.src =
                        "https://ui-avatars.com/api/?background=random&name=" +
                        organizer?.orgName;
                    }}
                    alt="Organizer"
                    className="w-full h-full object-cover"
                  />
                </div>

                {/* Details */}
                <div>
                  <h3 className="text-xl font-semibold text-gray-800 max-sm:text-lg">
                    {organizer?.orgName}
                  </h3>

                  <p className="text-gray-600 mt-2 mb-2 max-sm:text-sm max-sm:mb-2">
                    {organizer?.orgDescription}
                  </p>

                  {/* Contact Buttons */}
                  <div className="flex pt-2 items-center gap-4 max-sm:gap-3 max-sm:pt-1">
                    {/* Email */}
                    <button
                      className="flex items-center gap-1 cursor-pointer hover:opacity-90"
                      onClick={() => {
                        copyToClipboard(organizer?.orgEmail);
                        setCopied("Email copied!");
                        setTimeout(() => setCopied(null), 1500);
                      }}
                    >
                      <span
                        className="material-symbols-outlined text-sm max-sm:text-xs"
                        style={{
                          background:
                            "linear-gradient(90deg, #ff512f, #dd2476)",
                          WebkitBackgroundClip: "text",
                          WebkitTextFillColor: "transparent",
                        }}
                      >
                        mail
                      </span>
                      <span className="text-sm text-primary-600 hover:text-primary-800 max-sm:text-xs">
                        Contact
                      </span>
                    </button>

                    {/* Phone */}
                    <button
                      className="flex items-center gap-1 cursor-pointer text-primary-600 hover:text-primary-800"
                      onClick={() => {
                        copyToClipboard(organizer?.phone);
                        setCopied("Phone number copied!");
                        setTimeout(() => setCopied(null), 1500);
                      }}
                    >
                      <span
                        className="material-symbols-outlined text-sm max-sm:text-xs"
                        style={{
                          background:
                            "linear-gradient(90deg, #ff512f, #dd2476)",
                          WebkitBackgroundClip: "text",
                          WebkitTextFillColor: "transparent",
                        }}
                      >
                        call
                      </span>
                      <span className="text-sm max-sm:text-xs">Phone</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Reviews */}
            <div className="bg-white rounded-2xl shadow-md p-6 max-sm:p-4">
              {/* Header */}
              <div className="flex justify-between items-center mb-6 max-sm:mb-4">
                <h2 className="text-2xl max-sm:text-xl font-bold text-gray-800">
                  Reviews
                </h2>

                <div className="flex items-center gap-2 max-sm:gap-1.5">
                  <div className="flex">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <span
                        key={star}
                        className={`material-symbols-outlined text-xl max-sm:text-lg ${
                          star <= Math.round(selectedEvent?.averageRating)
                            ? "text-amber-400"
                            : "text-gray-300"
                        }`}
                      >
                        star
                      </span>
                    ))}
                  </div>

                  <span className="text-gray-600 font-medium text-sm max-sm:text-xs">
                    {selectedEvent?.averageRating} (
                    {selectedEvent?.ratings?.length} reviews)
                  </span>
                </div>
              </div>

              {/* No Reviews */}
              {selectedEvent?.ratings?.length === 0 ? (
                <NoData title="No reviews found" />
              ) : (
                <div className="space-y-4 max-sm:space-y-3">
                  {selectedEvent?.ratings?.slice(0, 3).map((review, idx) => {
                    const user = review.user;

                    return (
                      <div
                        key={review.id || review._id}
                        className={`${
                          idx !==
                          Math.min(3, selectedEvent?.ratings?.length) - 1
                            ? "border-b border-gray-200 pb-6 max-sm:pb-4"
                            : "pb-2"
                        }`}
                      >
                        <div className="flex justify-between mb-2 max-sm:mb-1">
                          {/* User Info */}
                          <div className="flex items-center gap-3 max-sm:gap-2">
                            <div className="w-10 h-10 max-sm:w-8 max-sm:h-8 bg-gray-200 rounded-full overflow-hidden">
                              <img
                                src={user?.userProfileImage}
                                onError={(e) => {
                                  e.currentTarget.src =
                                    "https://ui-avatars.com/api/?background=random&name=" +
                                    user?.username;
                                }}
                                alt={user?.username}
                                className="w-full h-full object-cover"
                              />
                            </div>

                            <div>
                              <p className="font-medium text-gray-800 text-sm max-sm:text-[13px]">
                                {user?.username}
                              </p>
                              <p className="text-sm max-sm:text-xs text-gray-500">
                                {review.createdAt &&
                                  new Date(review.createdAt).toLocaleDateString(
                                    "en-US",
                                    {
                                      month: "short",
                                      day: "numeric",
                                      year: "numeric",
                                    },
                                  )}
                              </p>
                            </div>
                          </div>

                          {/* Rating Stars */}
                          <div className="flex">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <span
                                key={star}
                                className={`material-symbols-outlined text-lg max-sm:text-base ${
                                  star <= review.rating
                                    ? "text-amber-400"
                                    : "text-gray-300"
                                }`}
                              >
                                star
                              </span>
                            ))}
                          </div>
                        </div>

                        {/* Review Text */}
                        <p className="text-gray-700 text-sm max-sm:text-[13px] leading-relaxed">
                          {review?.review}
                        </p>
                      </div>
                    );
                  })}

                  {/* View All Button */}
                  {selectedEvent?.ratings?.length > 3 && (
                    <button
                      onClick={() => setShowAll(true)}
                      className="
          w-full border border-gray-300 text-gray-700 
          hover:bg-gray-50 font-medium 
          py-2 px-4 rounded-lg transition 
          flex items-center justify-center gap-2
          max-sm:text-sm max-sm:py-1.5 max-sm:px-3 cursor-pointer
        "
                    >
                      <span className="material-symbols-outlined text-base max-sm:text-sm">
                        forum
                      </span>
                      View All Reviews
                    </button>
                  )}

                  {/* Full Reviews Modal */}
                  <AnimatePresence>
                    {showAll && (
                      <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[2000] p-3"
                        onClick={() => setShowAll(false)}
                      >
                        <motion.div
                          initial={{ y: 40, opacity: 0 }}
                          animate={{ y: 0, opacity: 1 }}
                          exit={{ y: 40, opacity: 0 }}
                          onClick={(e) => e.stopPropagation()}
                          className="
              bg-white rounded-2xl shadow-2xl 
              w-full max-w-3xl max-h-[80vh] 
              p-6 md:p-8 overflow-hidden
              max-sm:p-4
            "
                        >
                          {/* Modal Header */}
                          <div className="flex justify-between items-center mb-6 max-sm:mb-4">
                            <h2 className="text-2xl max-sm:text-xl font-bold text-gray-900">
                              All Reviews
                            </h2>
                            <button
                              onClick={() => setShowAll(false)}
                              className="px-2 pt-2 pb-1 hover:bg-gray-100 rounded-full transition cursor-pointer"
                            >
                              <span className="material-symbols-outlined text-gray-700">
                                close
                              </span>
                            </button>
                          </div>

                          {/* Filter */}
                          <div className="flex items-center gap-2 mb-6 max-sm:mb-4">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <button
                                key={star}
                                onClick={() =>
                                  setFilterRating(
                                    filterRating === star ? null : star,
                                  )
                                }
                                className={`
                    px-4 py-1.5 rounded-full border text-sm font-medium shadow-sm
                    max-sm:px-3 max-sm:py-1 max-sm:text-xs cursor-pointer
                    ${
                      filterRating === star
                        ? "bg-amber-400 border-amber-400 text-white"
                        : "border-gray-300 text-gray-700 hover:bg-gray-100"
                    }
                  `}
                              >
                                {star} ★
                              </button>
                            ))}
                          </div>

                          {/* Reviews List */}
                          <div className="space-y-6 max-sm:space-y-4 max-h-[62vh] pr-4 overflow-y-auto custom-scrollbar">
                            {filteredReviews?.length === 0 ? (
                              <p className="text-gray-500 text-center py-10 max-sm:text-sm">
                                No reviews found.
                              </p>
                            ) : (
                              filteredReviews?.map((review) => {
                                const user = review.user;

                                return (
                                  <div
                                    key={review.id || review._id}
                                    className="pb-6 max-sm:pb-4 border-b last:border-b-0 border-gray-200"
                                  >
                                    <div className="flex justify-between items-start mb-2 max-sm:mb-1">
                                      {/* User */}
                                      <div className="flex items-center gap-3 max-sm:gap-2">
                                        <div className="w-10 h-10 max-sm:w-8 max-sm:h-8 rounded-full overflow-hidden bg-gray-200 shadow-inner">
                                          <img
                                            src={user?.userProfileImage}
                                            onError={(e) => {
                                              e.currentTarget.src =
                                                "https://ui-avatars.com/api/?background=random&name=" +
                                                user?.username;
                                            }}
                                            alt={user?.username}
                                            className="w-full h-full object-cover"
                                          />
                                        </div>

                                        <div>
                                          <p className="font-semibold text-gray-900 text-sm max-sm:text-[13px]">
                                            {user?.username}
                                          </p>
                                          <p className="text-sm max-sm:text-xs text-gray-500">
                                            {review.createdAt &&
                                              new Date(
                                                review.createdAt,
                                              ).toLocaleDateString("en-US", {
                                                month: "short",
                                                day: "numeric",
                                                year: "numeric",
                                              })}
                                          </p>
                                        </div>
                                      </div>

                                      {/* Rating */}
                                      <div className="flex items-center">
                                        {[1, 2, 3, 4, 5].map((star) => (
                                          <span
                                            key={star}
                                            className={`material-symbols-outlined text-lg max-sm:text-base ${
                                              star <= review.rating
                                                ? "text-amber-400"
                                                : "text-gray-300"
                                            }`}
                                          >
                                            star
                                          </span>
                                        ))}
                                      </div>
                                    </div>

                                    {/* Review Text */}
                                    <p className="text-gray-700 text-sm max-sm:text-[13px] leading-relaxed">
                                      {review?.review}
                                    </p>
                                  </div>
                                );
                              })
                            )}
                          </div>
                        </motion.div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              )}

              {/* Write Review Button */}
              {!showReviewInput && (
                <button
                  onClick={() => setShowReviewInput(true)}
                  disabled={!eventBooked}
                  className="
      relative group w-full mt-3 
      bg-gradient-to-r from-[#ff512f] to-[#dd2476] 
      text-white font-semibold 
      py-2 px-4 rounded-lg shadow 
      transition flex items-center justify-center gap-2
      max-sm:py-1.5 max-sm:text-sm
      disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed
    "
                >
                  <span className="material-symbols-outlined text-base max-sm:text-sm">
                    edit
                  </span>
                  Write a Review
                  {!eventBooked && (
                    <div className=" absolute left-1/2 -translate-x-1/2 -bottom-9 px-2 py-1 rounded-md text-xs text-white whitespace-nowrap bg-black/80 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none ">
                      {" "}
                      You can write a review after attending the event{" "}
                    </div>
                  )}
                </button>
              )}
              {showReviewInput && (
                <div className="mt-4 p-4 max-sm:p-3 border border-gray-300 rounded-lg bg-gray-50 space-y-3">
                  {/* Rating Stars */}
                  <div className="flex gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <span
                        key={star}
                        onClick={() => setUserRating(star)}
                        className={`material-symbols-outlined cursor-pointer ${
                          star <= userRating
                            ? "text-amber-400"
                            : "text-gray-300"
                        } text-3xl max-sm:text-2xl`}
                      >
                        star
                      </span>
                    ))}
                  </div>

                  {/* Textarea */}
                  <textarea
                    className="
        w-full border border-gray-300 rounded-lg 
        p-3 max-sm:p-2.5 
        focus:ring-2 focus:ring-[#ff512f] outline-none 
        text-gray-700 text-sm max-sm:text-[13px]
      "
                    rows={4}
                    placeholder="Share your experience..."
                    value={userReview}
                    onChange={(e) => setUserReview(e.target.value)}
                  />

                  {/* Submit + Cancel */}
                  <div className="flex justify-end gap-3 max-sm:gap-2">
                    <button
                      onClick={() => setShowReviewInput(false)}
                      className="
          px-4 py-2 max-sm:px-3 max-sm:py-1.5 
          border border-gray-400 rounded-lg 
          text-gray-600 text-sm max-sm:text-xs 
          hover:bg-gray-200 cursor-pointer
        "
                    >
                      Cancel
                    </button>

                    <button
                      onClick={handleSubmitReview}
                      className="
          px-5 py-2 max-sm:px-4 max-sm:py-1.5 
          bg-gradient-to-r from-[#ff512f] to-[#dd2476] 
          text-white rounded-lg shadow 
          font-medium text-sm max-sm:text-xs 
          hover:opacity-90 cursor-pointer
        "
                    >
                      Submit Review
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* RIGHT COLUMN: selectedEvent Details + Tickets */}
          <div className="md:col-span-5 lg:col-span-4">
            <div className="bg-white rounded-xl shadow-md p-6 sticky top-24 max-sm:p-4">
              <div className="flex justify-between items-start mb-5">
                <h3 className="text-xl font-bold text-gray-800 max-sm:text-lg">
                  Event Details
                </h3>
                <span className="bg-primary-100 uppercase text-primary-500 px-2 py-1 border border-gray-200 rounded-md text-xs font-medium max-sm:text-[10px]">
                  {selectedEvent?.status}
                </span>
              </div>
              <div className="space-y-4 mb-6 max-sm:space-y-3">
                <div className="flex items-center gap-3">
                  <span
                    className="material-symbols-outlined p-2 rounded-full bg-primary-50 max-sm:p-1 max-sm:text-xl"
                    style={{
                      background: "linear-gradient(90deg, #ff512f, #dd2476)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                    }}
                  >
                    calendar_month
                  </span>
                  <div>
                    <p className="text-sm text-gray-500 max-sm:text-sm">Date</p>
                    <p className="text-gray-700 font-medium max-sm:text-sm">
                      {formatEventDateShort()}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span
                    className="material-symbols-outlined p-2 rounded-full bg-primary-50 max-sm:p-1 max-sm:text-xl"
                    style={{
                      background: "linear-gradient(90deg, #ff512f, #dd2476)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                    }}
                  >
                    schedule
                  </span>
                  <div>
                    <p className="text-sm text-gray-500 max-sm:text-sm">Time</p>
                    <p className="text-gray-700 font-medium max-sm:text-sm">
                      {formatTime(selectedEvent?.startTime)} -{" "}
                      {formatTime(selectedEvent?.endTime)}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span
                    className="material-symbols-outlined p-2 rounded-full bg-primary-50 max-sm:p-1 max-sm:text-xl"
                    style={{
                      background: "linear-gradient(90deg, #ff512f, #dd2476)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                    }}
                  >
                    location_on
                  </span>
                  <div>
                    <p className="text-sm text-gray-500 max-sm:text-sm">
                      Location
                    </p>
                    <div className="flex items-center gap-1.5">
                      <p className="text-gray-700 font-medium max-sm:text-sm">
                        {selectedEvent?.location?.label ||
                          selectedEvent?.locationLabel ||
                          "TBD"}
                      </p>

                      {selectedEvent?.location?.coordinates && (
                        <div className="relative group">
                          <button
                            onClick={() =>
                              window.open(
                                `https://www.google.com/maps?q=${selectedEvent?.location.coordinates[1]},${selectedEvent?.location.coordinates[0]}`,
                                "_blank",
                              )
                            }
                            className="flex items-center justify-center cursor-pointer"
                          >
                            <span
                              className="material-symbols-outlined text-base max-sm:text-xl"
                              style={{
                                background:
                                  "linear-gradient(90deg, #ff512f, #dd2476)",
                                WebkitBackgroundClip: "text",
                                WebkitTextFillColor: "transparent",
                              }}
                            >
                              near_me
                            </span>
                          </button>
                          <div
                            className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2
                                   px-2.5 py-1.5 bg-black/80 text-white text-xs rounded-md
                                   whitespace-nowrap shadow-lg opacity-0 group-hover:opacity-100
                                   transition-opacity duration-200 pointer-events-none"
                          >
                            View on Google Maps
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span
                    className="material-symbols-outlined p-2 rounded-full bg-primary-50 max-sm:p-1 max-sm:text-xl"
                    style={{
                      background: "linear-gradient(90deg, #ff512f, #dd2476)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                    }}
                  >
                    translate
                  </span>
                  <div>
                    <p className="text-sm text-gray-500 max-sm:text-sm">
                      Languages
                    </p>
                    {selectedEvent?.languages ? (
                      <p className="text-gray-700 font-medium">
                        {selectedEvent?.languages.join(", ")}
                      </p>
                    ) : (
                      <p className="text-gray-700 font-medium max-sm:text-sm">
                        Not provided
                      </p>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span
                    className="material-symbols-outlined p-2 rounded-full bg-primary-50 max-sm:p-1 max-sm:text-xl"
                    style={{
                      background: "linear-gradient(90deg, #ff512f, #dd2476)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                    }}
                  >
                    category
                  </span>
                  <div>
                    <p className="text-sm text-gray-500 max-sm:text-sm">
                      Category
                    </p>
                    <p className="text-gray-700 font-medium max-sm:text-sm">
                      {selectedEvent?.category}
                    </p>
                  </div>
                </div>
              </div>

              <div className="border-t border-gray-200 pt-5">
                <div className="mb-4">
                  <div className="flex items-baseline">
                    <span className="text-3xl font-bold text-primary-600 max-sm:text-2xl">
                      ₹{selectedEvent?.startingPrice}
                    </span>
                    <span className="text-gray-600 ml-2 text-sm">onwards</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    Price excluding platform fee and taxes
                  </p>
                </div>
                <button
                  onClick={
                    selectedEvent?.status?.toUpperCase() !== "COMPLETED" &&
                    sessionAvailable
                      ? () =>
                          navigate("/event/" + selectedEvent._id + "/sessions")
                      : undefined
                  }
                  className={`w-full font-bold py-3 max-sm:py-2 max-sm:text-sm px-4 rounded-lg shadow flex items-center justify-center gap-2 mb-2 transition-all duration-300
    ${
      selectedEvent?.status?.toUpperCase() === "COMPLETED" || !sessionAvailable
        ? "bg-gray-400 cursor-not-allowed text-gray-200"
        : "text-white cursor-pointer"
    }
  `}
                  style={
                    selectedEvent?.status?.toUpperCase() !== "COMPLETED" &&
                    sessionAvailable
                      ? {
                          background:
                            "linear-gradient(90deg, #ff512f, #dd2476)",
                        }
                      : {}
                  }
                >
                  <span className="material-symbols-outlined">
                    confirmation_number
                  </span>
                  {selectedEvent?.status?.toUpperCase() === "COMPLETED"
                    ? "Event Completed"
                    : sessionAvailable
                      ? "Buy Tickets"
                      : "Bookings Opening Soon"}
                </button>

                <p className="text-sm text-gray-500 text-center">
                  Limited seats available
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
      {filteredEvents?.similarEvents && (
        <div className="px-2 sm:px-6 lg:px-18 py-8">
          <EventGridShow
            title="Explore similar events"
            events={filteredEvents?.similarEvents}
          />
        </div>
      )}
      {copied && (
        <div
          className="fixed bottom-6 left-1/2 -translate-x-1/2 
                  bg-black/80 text-white text-sm px-4 py-2 rounded-lg shadow-lg
                  animate-fade-in-out z-[9999]"
        >
          {copied}
        </div>
      )}
    </div>
  );
}
