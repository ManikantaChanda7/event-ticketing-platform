import { ChevronLeft, ChevronRight, ShoppingCart } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import {
  bookTickets,
  fetchEventById,
  fetchEventVenue,
} from "../redux/slices/eventSlice";
import {
  fetchSessionById,
  fetchSessionsByEventId,
} from "../redux/slices/sessionSlice";

export default function EventSessions() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { id } = useParams();

  const scrollRef = useRef(null);

  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);
  const [openSection, setOpenSection] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [paymentStatus, setPaymentStatus] = useState(null);
  const [sessionId, setSessionId] = useState(null);

  const { selectedEvent: event, selectedEventVenue: venue } = useSelector(
    (state) => state.event,
  );
  const { sessions, selectedSession: session } = useSelector(
    (state) => state.session,
  );

  useEffect(() => {
    if (id && !event?._id) {
      dispatch(fetchEventById(id));
    }
  }, [dispatch, id, event?._id]);

  useEffect(() => {
    if (event?._id) {
      dispatch(fetchSessionsByEventId(event._id));
    }
  }, [dispatch, event?._id]);

  useEffect(() => {
    if (sessions?.length > 0) {
      setSessionId(sessions[0]._id);
    }
  }, [sessions]);

  useEffect(() => {
    if (sessionId) {
      dispatch(fetchSessionById(sessionId));
    }
  }, [dispatch, sessionId]);

  useEffect(() => {
    if (event?.venue) {
      dispatch(fetchEventVenue(event.venue));
    }
  }, [dispatch, event?.venue]);

  useEffect(() => {
    updateScrollButtons();
  }, [sessions]);

  useEffect(() => {
    updateScrollButtons();
    const el = scrollRef.current;
    if (!el) return;
    el.addEventListener("scroll", updateScrollButtons);
    window.addEventListener("resize", updateScrollButtons); // handle resize
    return () => {
      el.removeEventListener("scroll", updateScrollButtons);
      window.removeEventListener("resize", updateScrollButtons);
    };
  }, []);

  const getSeatPrice = (sectionName) => {
    const ticket = session?.tickets.find((t) => t.type === sectionName);
    return ticket ? ticket.price : 12; // fallback to $12 if not found
  };

  const totalSeatPrice = selectedSeats.reduce(
    (sum, s) => sum + getSeatPrice(s.section),
    0,
  );

  const fakePayment = async () => {
    try {
      setPaymentStatus("initializing"); // Step 1
      await new Promise((res) => setTimeout(res, 800));

      setPaymentStatus("processing"); // Step 2
      await new Promise((res) => setTimeout(res, 1400));

      setPaymentStatus("verifying"); // Step 3
      await new Promise((res) => setTimeout(res, 1200));

      // Final: call your real booking API
      await handleBooking();

      setPaymentStatus("success");
    } catch (err) {
      setPaymentStatus("failed");
    }
  };

  const toggleSeat = (sectionName, seatId) => {
    setSelectedSeats((prev) => {
      const exists = prev.find(
        (s) => s.seatId === seatId && s.section === sectionName,
      );
      if (exists) {
        // remove it
        return prev.filter(
          (s) => !(s.seatId === seatId && s.section === sectionName),
        );
      } else {
        const price = session?.tickets.find(
          (t) => t.type === sectionName,
        ).price;
        return [...prev, { section: sectionName, seatId, price }];
      }
    });
  };

  const handleBooking = async () => {
    await dispatch(
      bookTickets({ eventId: event._id, sessionId, selectedSeats }),
    ).unwrap();
    navigate("/bookings");
  };

  // Check scroll position to toggle buttons
  const updateScrollButtons = () => {
    const el = scrollRef.current;
    if (el) {
      setCanScrollLeft(el.scrollLeft > 0);
      setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth);
    }
  };

  const scroll = (direction) => {
    const el = scrollRef.current;
    if (!el) return;
    const scrollAmount = 120;
    el.scrollBy({
      left: direction === "left" ? -scrollAmount : scrollAmount,
      behavior: "smooth",
    });
  };

  const formatSession = (dateStr, timeStr) => {
    const date = new Date(dateStr); // your ISO date
    const [hours, minutes] = timeStr.split(":").map(Number);
    date.setHours(hours, minutes);

    return date.toLocaleString("en-US", {
      month: "short", // Sep
      day: "numeric", // 7
      hour: "numeric", // 10
      minute: "2-digit", // 00
      hour12: true, // AM/PM
    });
  };

  return (
    <div
      className="font-sans min-h-screen p-4 md:p-6 lg:p-8"
      style={{
        background:
          "linear-gradient(to bottom, #e8e5f6ff 0%, #ffffff 60%, #ffffff 100%)",
      }}
    >
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row gap-6 ">
        <div className="w-full md:w-2/3 flex flex-col gap-4">
          {/* Session Selector */}
          <div
            className="bg-white backdrop-blur rounded-2xl shadow-md  p-6 relative"
            // style={{ border: "1.5px solid #e7e8ea" }}
          >
            <h2 className="text-2xl font-bold" style={{ color: "#0c172f" }}>
              🎬 Select Session
            </h2>

            {/* Legend at top-right */}
            <div className="absolute top-9 right-10 flex flex-wrap gap-2 text-xs items-center max-sm:static max-sm:mt-2 max-sm:ml-2 max-sm:flex-wrap max-sm:gap-1">
              <span className="w-3 h-3 max-sm:w-2.5 max-sm:h-2.5 bg-green-500 rounded-full"></span>{" "}
              Available
              <span className="w-3 h-3 max-sm:w-2.5 max-sm:h-2.5 bg-yellow-400 rounded-full max-sm:ml-1 ml-2"></span>{" "}
              Fast Filling
              <span className="w-3 h-3 max-sm:w-2.5 max-sm:h-2.5 bg-red-500 rounded-full max-sm:ml-1 ml-2"></span>{" "}
              Sold Out
            </div>

            <div className="flex items-center gap-2 px-2 mt-6">
              {/* Left arrow */}
              {canScrollLeft && (
                <button
                  onClick={() => scroll("left")}
                  className="p-2 rounded-full hover:bg-gray-100 cursor-pointer"
                >
                  <ChevronLeft size={20} />
                </button>
              )}

              {/* Scroll container */}
              <div ref={scrollRef} className="flex gap-3 overflow-x-hidden">
                {sessions?.map((s, i) => {
                  // Assuming 's' is the session object and has seats array
                  const totalSeats = venue?.capacity;
                  const bookedSeats = s.occupancy; // number of booked seats
                  const occupancyRate = bookedSeats / totalSeats; // 0 - 1

                  let lineColor = "bg-green-500"; // default: low occupancy
                  if (occupancyRate > 0.7) {
                    lineColor = "bg-red-500"; // sold
                  } else if (occupancyRate > 0.4) {
                    lineColor = "bg-yellow-400"; // fast filling
                  } // else green for available

                  return (
                    <div
                      key={i}
                      className="flex flex-col items-center flex-shrink-0"
                    >
                      <button
                        className={`px-5 py-2 rounded-full text-sm font-medium max-sm:px-3 max-sm:py-1 max-sm:text-xs transition shadow cursor-pointer
                  ${
                    session?._id === s._id
                      ? "text-white"
                      : "bg-gray-200 hover:bg-gray-200 text-gray-700"
                  }`}
                        style={
                          session?._id === s._id
                            ? {
                                background:
                                  "linear-gradient(90deg, #ff512f, #dd2476)",
                              }
                            : { backgroundColor: "#f3f3f5", color: "#0c172f" }
                        }
                        onClick={() => {
                          setSessionId(s._id);
                          setSelectedSeats([]); // clear seats on session change
                        }}
                      >
                        {formatSession(s.date, s.startTime)}
                      </button>
                      <div
                        className={`w-2 h-1 mt-1 rounded-full ${lineColor}`}
                      ></div>
                    </div>
                  );
                })}
              </div>

              {/* Right arrow */}
              {canScrollRight && (
                <button
                  onClick={() => scroll("right")}
                  className="p-2 rounded-full hover:bg-gray-100 cursor-pointer"
                >
                  <ChevronRight size={20} />
                </button>
              )}
            </div>
          </div>

          {/* Seating Layout */}
          <div
            className="bg-white backdrop-blur rounded-2xl shadow-md p-6 w-full"
            // style={{ border: "1.5px solid #e7e8ea" }}
          >
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold">🎟 Select Seats</h2>
            </div>

            {/* Screen */}
            <div className="relative flex justify-center mb-12">
              <div
                className="w-2/3 h-6 bg-gray-800 rounded-b-full shadow-inner"
                style={{ backgroundColor: "#0c172f" }}
              />
              <span className="absolute -bottom-6 text-sm text-gray-600">
                SCREEN
              </span>
            </div>

            {/* Sections → Rows → Seats */}
            {venue?.seatingLayout?.map((section, sIdx) => (
              <div key={sIdx} className="mb-4">
                <div
                  onClick={() => setOpenSection(section)}
                  className="h-20 px-10 rounded-xl flex items-center justify-center 
             cursor-pointer hover:scale-105 transition mx-auto shadow-sm w-3/4"
                  style={{ backgroundColor: "#f3f3f5" }}
                >
                  <span className=" font-bold" style={{ font: "#oc172f" }}>
                    Click to view {section.section} seats
                  </span>
                </div>
              </div>
            ))}

            {/* 🔹 Modal for huge sections */}
          </div>
        </div>
        <div className="w-full md:w-1/3 h-fit sticky top-24">
          {/* Order Summary */}
          <div
            className="bg-white backdrop-blur rounded-2xl shadow-md  p-6 w-full  h-fit sticky top-6"
            // style={{ border: "1.5px solid #e7e8ea" }}
          >
            <h2 className="text-2xl font-bold mb-4">Your Booking</h2>
            <div className="mb-5">
              <h3 className="font-semibold text-md mb-2">{event?.title}</h3>
              <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                <span
                  className="material-symbols-outlined"
                  style={{
                    fontSize: "18px",
                  }}
                >
                  schedule
                </span>
                {session && formatSession(session.date, session.startTime)}
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <span
                  className="material-symbols-outlined"
                  style={{
                    fontSize: "19px",
                  }}
                >
                  location_on
                </span>
                {venue?.name}, {venue?.city}
              </div>
            </div>

            <div className="border-t border-b border-gray-300 py-4 mb-4">
              <h3 className="font-semibold mb-3">
                Selected Seats ({selectedSeats?.length})
              </h3>
              <div className="mb-4 space-y-2">
                {Object.entries(
                  selectedSeats?.reduce((acc, s) => {
                    if (!acc[s.section]) acc[s.section] = [];
                    acc[s.section].push(s.seatId);
                    return acc;
                  }, {}),
                ).map(([section, seats]) => {
                  const pricePerSeat =
                    session?.tickets.find((t) => t.type === section)?.price ||
                    12;
                  const subtotal = pricePerSeat * seats.length;

                  return (
                    <div
                      key={section}
                      className="flex flex-col border border-gray-200 rounded-lg p-3 bg-gray-50"
                    >
                      <div className="flex justify-between items-center mb-1">
                        <span className="font-semibold text-gray-800">
                          {section}
                        </span>
                        <span className="text-sm text-gray-600">
                          {seats.length} × ₹{pricePerSeat}
                        </span>
                      </div>
                      <div className="text-sm text-gray-700">
                        Seats: {seats.join(", ")}
                      </div>
                      <div className="text-right font-medium text-gray-800">
                        Subtotal: ₹{subtotal.toFixed(2)}
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span>Seats Total</span>
                  <span>₹{totalSeatPrice?.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Convenience Fee</span>
                  <span>₹3.50</span>
                </div>
              </div>
            </div>

            <div className="flex justify-between font-bold text-lg mb-6">
              <span>Total</span>
              <span>₹{(totalSeatPrice + 3.5).toFixed(2)}</span>
            </div>

            <button
              style={{ backgroundColor: "#0c172f" }}
              onClick={selectedSeats.length > 0 ? fakePayment : () => {}}
              disabled={selectedSeats.length === 0 || paymentStatus !== null}
              className={`w-full text-white py-3 rounded-xl font-medium flex items-center justify-center gap-2 shadow 
    ${
      selectedSeats.length === 0
        ? "opacity-50 bg-gray-400 text-gray-200 cursor-not-allowed"
        : "cursor-pointer"
    }
  `}
            >
              {paymentStatus ? (
                paymentStatus === "initializing" ? (
                  "Initializing Payment..."
                ) : paymentStatus === "processing" ? (
                  "Processing Payment..."
                ) : paymentStatus === "verifying" ? (
                  "Verifying Payment..."
                ) : paymentStatus === "success" ? (
                  "Payment Completed!"
                ) : (
                  "Try Again"
                )
              ) : (
                <>
                  <ShoppingCart size={18} />
                  Proceed to Payment
                </>
              )}
            </button>
          </div>
        </div>
      </div>
      {paymentStatus && paymentStatus !== "success" && (
        <div className="fixed inset-0 z-2000 bg-black/70 backdrop-blur-sm flex flex-col items-center justify-center gap-4 text-white">
          <div className="loader" />
          <p className="text-xl font-semibold">
            {paymentStatus === "initializing" &&
              "Initializing Payment Gateway..."}
            {paymentStatus === "processing" && "Processing your payment..."}
            {paymentStatus === "verifying" && "Verifying transaction..."}
          </p>
        </div>
      )}

      {openSection && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm"
          onClick={() => setOpenSection(null)}
        >
          <div
            className="
        relative
        bg-white rounded-2xl 
        w-11/12 md:w-2/3 
        max-h-[80vh] overflow-hidden shadow-lg 
        p-4 sm:p-5 md:p-6
      "
            onClick={(e) => e.stopPropagation()}
          >
            <div
              className="
    mb-6
    flex flex-wrap items-center justify-between 
    gap-3
  "
            >
              <div
                className="
      w-full sm:w-auto
      flex items-center justify-between
    "
              >
                {/* Title */}
                <h2 className="text-xl font-bold">
                  {session?.tickets.filter(
                    (t) => t.type === openSection.section,
                  ).length > 0
                    ? `${openSection.section} - ₹${
                        session?.tickets.find(
                          (t) => t.type === openSection.section,
                        ).price
                      }`
                    : `${openSection.section} - ₹12.00`}
                </h2>

                {/* Close Button (Right side) */}
                <button
                  onClick={() => setOpenSection(null)}
                  className="
        w-8 h-8 ml-3
        flex items-center justify-center
        rounded-full 
        bg-gray-200 hover:bg-gray-300 
        text-gray-700 
        transition cursor-pointer
        sm:hidden
      "
                >
                  ✕
                </button>
              </div>

              {/* ----------- LEGEND (Row 2 on small screens) ----------- */}
              <div
                className="
      flex gap-3 text-sm flex-wrap
      max-sm:text-xs max-sm:gap-2
    "
              >
                <Legend color="bg-gray-200" label="Available" />
                <Legend color="bg-indigo-600" label="Selected" />
                <Legend color="bg-gray-400" label="Booked" />
                <Legend color="bg-red-500" label="Blocked" />
              </div>
              <button
                onClick={() => setOpenSection(null)}
                className="
      hidden sm:flex
      w-8 h-8
      items-center justify-center
      rounded-full 
      bg-gray-200 hover:bg-gray-300 
      text-gray-700 transition cursor-pointer
    "
              >
                ✕
              </button>
            </div>

            {/* Screen */}
            <div className="relative flex justify-center mb-12 w-full max-sm:mb-8">
              <div
                className="
            w-full md:w-2/3 
            h-6 max-sm:h-4 
            bg-gray-800 rounded-b-full shadow-inner
          "
                style={{ backgroundColor: "#0c172f" }}
              />
              <span className="absolute -bottom-6 text-sm text-gray-600">
                SCREEN
              </span>
            </div>

            {/* Seats Grid */}
            <div className="max-h-[50vh] overflow-y-auto flex flex-col items-center gap-3 pb-1">
              {openSection.rows.map((row, rIdx) => (
                <div
                  key={rIdx}
                  className="flex items-center gap-2 max-sm:gap-1"
                >
                  <span className="w-6 max-sm:w-5 text-center font-medium text-sm max-sm:text-xs">
                    {row.label}
                  </span>

                  <div className="flex gap-2 max-sm:gap-1">
                    {row.seats.map((seatId) => {
                      const seatInfo = session?.seats.find(
                        (s) =>
                          s.seatId === seatId &&
                          s.section === openSection.section,
                      );

                      const status = seatInfo?.status || "available";

                      const isSelected = selectedSeats?.some(
                        (s) =>
                          s.seatId === seatId &&
                          s.section === openSection.section,
                      );

                      let classes =
                        "flex items-center justify-center rounded-md font-medium transition " +
                        "w-6 h-6 text-xs " +
                        "max-sm:w-5 max-sm:h-5 max-sm:text-[10px] ";

                      if (status === "booked") {
                        classes += "bg-gray-400 cursor-not-allowed text-white";
                      } else if (status === "blocked") {
                        classes += "bg-red-500 cursor-not-allowed text-white";
                      } else if (isSelected) {
                        classes += "bg-indigo-600 text-white cursor-pointer";
                      } else {
                        classes +=
                          "bg-gray-100 hover:bg-gray-200 text-gray-700 cursor-pointer";
                      }

                      return (
                        <button
                          key={seatId}
                          disabled={status === "booked" || status === "blocked"}
                          className={classes}
                          onClick={() =>
                            toggleSeat(openSection.section, seatId)
                          }
                        />
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function Legend({ color, label }) {
  return (
    <div className="flex items-center gap-2">
      <div className={`w-4 h-4 rounded-full ${color}`}></div>
      <span>{label}</span>
    </div>
  );
}
