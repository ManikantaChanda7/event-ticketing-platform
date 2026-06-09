import {
  BarChart3,
  Check,
  CircleDollarSign,
  Edit,
  Info,
  Trash2,
  TrendingDown,
  TrendingUp,
  X,
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import Chart from "react-apexcharts";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { updateEventImage, updateEventTitle } from "../redux/slices/eventSlice";
import { getManageEventData } from "../redux/slices/organizerProfileSlice";
import { deleteSession, updateSession } from "../redux/slices/sessionSlice";
import { uploadImage } from "../redux/slices/uploadSlice";
import { useLocation, useParams } from "react-router-dom";

export default function ManageEvent() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  // const selectedEventId = location.state;
  const { id: selectedEventId } = useParams();

  // Redirect to dashboard if event ID is missing
  useEffect(() => {
    if (!selectedEventId) {
      console.error("Event ID is missing from URL, redirecting to dashboard");
      navigate("/dashboard");
    }
  }, [selectedEventId, navigate]);

  const { manageEventData } = useSelector((state) => state.organizerProfile);
  const { updatedSession } = useSelector((state) => state.session);

  const [eventData, setEventData] = useState({
    title: "",
    name: "",
    category: "",
    tags: [],
    startDate: "",
    endDate: "",
    recurrence: "",
    venue: "",
    location: "",
    attendees: "",
    duration: "",
    description: "",
    additionalInfo: "",
    heroImage: "",
    thumbnailImage: "",
    organizer: "",
    expected: "",
    status: "",
    createdAt: "",
  });
  const [sessionsList, setSessionsList] = useState([]);
  const [sessionTickets, setSessionTickets] = useState({});
  const [selectedSession, setSelectedSession] = useState("");
  const [accumulatedStats, setAccumulatedStats] = useState(null);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [editedSession, setEditedSession] = useState(null);

  const [isEditingName, setIsEditingName] = useState(false);
  const [tempName, setTempName] = useState(eventData.title);
  const heroInputRef = useRef(null);
  const thumbInputRef = useRef(null);
  const [uploading, setUploading] = useState(false);

  function formatDate(dateString) {
    const date = new Date(dateString);

    const day = date.getDate();
    const month = date.toLocaleString("en-US", { month: "short" });
    const year = date.getFullYear();

    // Suffix: 1st, 2nd, 3rd, 4th...
    const suffix =
      day % 10 === 1 && day !== 11
        ? "st"
        : day % 10 === 2 && day !== 12
        ? "nd"
        : day % 10 === 3 && day !== 13
        ? "rd"
        : "th";

    return `${day}${suffix} ${month} ${year}`;
  }

  const renderValue = (v) => {
    if (v === null || v === undefined) return "";
    if (typeof v === "string" || typeof v === "number") return v;
    if (Array.isArray(v)) return v.map((x) => (x.name ? x.name : x)).join(", ");
    if (typeof v === "object") return v.name || v.label || JSON.stringify(v);
    return String(v);
  };

  useEffect(() => {
    if (!selectedEventId) {
      console.error("Event ID is missing from URL");
      return;
    }
    dispatch(getManageEventData(selectedEventId)).unwrap();
  }, [selectedEventId]);

  useEffect(() => {
    const fetchEventData = async () => {
      try {
        const evt = manageEventData?.event || {};

        setEventData({
          title: evt.title ?? "",
          category: evt.category ?? "",
          tags: Array.isArray(evt.tags) ? evt.tags : [],
          startDate: formatDate(evt.startDate),
          endDate: formatDate(evt.endDate),
          timing: evt.startTime + " - " + evt.endTime,
          recurrence: evt.recurrence ?? "",
          venue: evt.venue ?? evt.venueName ?? evt.location ?? "",
          location: evt.location ?? "",
          attendees:
            manageEventData?.accumulatedStats?.totalTicketsSold !== undefined
              ? String(manageEventData?.accumulatedStats.totalTicketsSold)
              : "",
          duration: evt.duration ?? "",
          description: evt.description ?? "",
          additionalInfo: evt.additionalInfo ?? "",
          heroImage: evt.bannerImage ?? evt.heroImage ?? "",
          thumbnailImage: evt.thumbnailImage ?? "",
          organizer: evt.organizerName ?? evt.organizer ?? "",
          expected: evt.expected ?? "",
          status: evt.status ?? "",
          createdAt: evt.createdAt ?? "",
        });

        setAccumulatedStats(manageEventData?.accumulatedStats ?? null);

        const sessions = (manageEventData?.sessions || []).map((s) => ({
          id: s.sessionId,
          name:
            s.date && s.startTime
              ? formatDate(s.date)
              : `Session ${s.sessionId ?? ""}`,
          raw: s,
        }));
        setSessionsList(sessions);

        const ticketsBySession = {};
        (manageEventData?.sessions || []).forEach((s) => {
          const rows = [];
          const typeStats = s.stats?.typeStats;

          if (typeStats && Object.keys(typeStats).length) {
            Object.entries(typeStats).forEach(([type, stats]) => {
              const price = stats.price ?? 0;
              const sold = stats.sold ?? 0;
              const available = stats.available ?? 0;
              const status =
                available > 0 ? (
                  <span className="text-green-700 border border-green-600 text-xs px-2 py-0.5 rounded">
                    On Sale
                  </span>
                ) : (
                  <span className="text-gray-500 border border-gray-300 text-xs px-2 py-0.5 rounded">
                    Sold Out
                  </span>
                );
              rows.push({
                type: type,
                desc: stats.desc ?? "",
                price,
                sold,
                available,
                status,
              });
            });
          } else if (Array.isArray(s.tickets)) {
            s.tickets.forEach((t) => {
              const sold = (t.totalSeats ?? 0) - (t.available ?? 0);
              const price =
                typeof t.price === "number" ? `$${t.price}` : t.price ?? "-";
              const status =
                (t.available ?? 0) > 0 ? (
                  <span className="text-green-700 border border-green-600 text-xs px-2 py-0.5 rounded">
                    On Sale
                  </span>
                ) : (
                  <span className="text-gray-500 border border-gray-300 text-xs px-2 py-0.5 rounded">
                    Sold Out
                  </span>
                );
              rows.push({
                name: t.type ?? t.name ?? "Ticket",
                desc: t.desc ?? "",
                price,
                sold: `${sold}/${t.totalSeats ?? "-"}`,
                available: t.available ?? 0,
                status,
              });
            });
          }
          ticketsBySession[s.sessionId] = rows;
        });

        setSessionTickets(ticketsBySession);
        if (sessions.length > 0) setSelectedSession(sessions[0].id);
      } catch (err) {
        console.error("Error fetching manage-event data:", err);
      }
    };

    fetchEventData();
  }, [manageEventData]);

  const ticketDistributionSeries = accumulatedStats?.ticketTypeStats
    ? Object.values(accumulatedStats.ticketTypeStats).map((t) => t.sold ?? 0)
    : [];
  const ticketDistributionLabels = accumulatedStats?.ticketTypeStats
    ? Object.keys(accumulatedStats.ticketTypeStats)
    : [];
  const revenueSeries = accumulatedStats?.ticketTypeStats
    ? Object.values(accumulatedStats.ticketTypeStats).map((t) => t.revenue ?? 0)
    : [];
  const revenueCategories = accumulatedStats?.ticketTypeStats
    ? Object.keys(accumulatedStats.ticketTypeStats)
    : [];

  useEffect(() => {
    setTempName(eventData.title || "");
  }, [eventData.title]);

  const handleSaveName = async () => {
    try {
      dispatch(updateEventTitle({ selectedEventId, tempName }));
      setEventData({ ...eventData, title: tempName });
    } catch (err) {
      console.error("Error updating event name:", err);
    }
    setIsEditingName(false);
  };

  const canEditSession = (session) => {
    const now = new Date();
    const releaseDate = new Date(session.raw.releaseDate);
    return now < releaseDate;
  };

  const handleOpenEdit = (session) => {
    const normalizedTickets = sessionTickets[session.id] || [];

    setEditedSession({
      ...session,
      // fallback for date/time if missing
      startTime: session.raw.startTime || "",
      endTime: session.raw.endTime || "",
      tickets: normalizedTickets.map((t) => ({
        type: t.type,
        price: t.price,
        available: t.available,
        totalSeats: t.available + t.sold,
      })),
    });

    setOpenEditDialog(true);
  };

  const handleClose = () => {
    setOpenEditDialog(false);
    setEditedSession(null);
  };

  const handleSave = async () => {
    if (!editedSession || !editedSession.id) return;

    try {
      dispatch(
        updateSession({
          sessionId: editedSession.id,
          payload: {
            startTime: editedSession.startTime,
            endTime: editedSession.endTime,
            tickets: editedSession.tickets?.map((t) => ({
              type: t.type,
              price: Number(t.price),
              available: t.available ?? 0,
              totalSeats: t.totalSeats ?? 0,
            })),
          },
        })
      ).unwrap();
      setOpenEditDialog(false);
    } catch (error) {
      console.error("❌ Failed to update session:", error);
      alert(
        error.response?.data?.message ||
          "Something went wrong while updating the session"
      );
    }
  };

  useEffect(() => {
    if (!updatedSession) return;

    // update displayed sessions list
    setSessionsList((prev) =>
      prev.map((s) => (s._id === updatedSession?._id ? updatedSession : s))
    );

    // update tickets for that session
    setSessionTickets((prev) => ({
      ...prev,
      [updatedSession._id]: updatedSession.tickets.map((t) => ({
        type: t.type,
        price: `${t.price}`,
        sold: t.totalSeats - t.available,
        available: t.available,
        status:
          t.available > 0 ? (
            <span className="text-green-700 border border-green-600 text-xs px-2 py-0.5 rounded">
              On Sale
            </span>
          ) : (
            <span className="text-gray-500 border border-gray-300 text-xs px-2 py-0.5 rounded">
              Sold Out
            </span>
          ),
      })),
    }));
  }, [updatedSession]);

  const handleDelete = async () => {
    await dispatch(deleteSession(editedSession?.id)).unwrap();

    setSessionsList((prev) => {
      const updated = prev.filter((s) => s.id !== editedSession?.id);

      // update selected session if the deleted one was selected
      if (editedSession?.id === selectedSession) {
        if (updated.length > 0) {
          setSelectedSession(updated[0].id); // pick first available
        } else {
          setSelectedSession(""); // no sessions left
        }
      }

      return updated;
    });

    setOpenEditDialog(false);
  };

  const handleImageUpload = async (e, type) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("image", file);

    try {
      setUploading(true);
      const res = await dispatch(uploadImage({ formData })).unwrap();
      const imageUrl = res.url;

      const apiType = type === "hero" ? "banner" : "thumbnail";

      // Get event ID from URL parameter or from manageEventData
      const eventId = selectedEventId || manageEventData?.event?.id;

      if (!eventId) {
        console.error("Event ID is undefined, cannot update image");
        alert("Event ID is missing. Please navigate to this page from the dashboard.");
        setUploading(false);
        return;
      }

      dispatch(
        updateEventImage({
          eventId: eventId,
          type: apiType,
          url: imageUrl,
        })
      )
        .unwrap()
        .then(() => {
          setEventData((prev) => ({
            ...prev,
            [type === "hero" ? "heroImage" : "thumbnailImage"]: imageUrl,
          }));
        })
        .catch((err) => console.error("Image update failed:", err));
    } catch (error) {
      console.error("❌ Upload failed:", error);
      alert("Image upload failed. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-gray-50 min-h-screen p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900">
            Manage Event
          </h1>
          <p className="text-gray-500 mt-1">
            View insights, edit details, and manage ticket sales
          </p>
        </div>

        <div className="flex flex-col lg:flex-row justify-between items-start gap-10 mb-10">
          {/* LEFT: Hero Image */}
          <div className="relative w-full lg:max-w-7xl h-[420px] rounded-xl overflow-hidden shadow-lg">
            {/* Hero Background Image */}
            <div
              className="absolute inset-0 bg-center bg-cover"
              style={{ backgroundImage: `url('${eventData.heroImage || ""}')` }}
            />

            {/* Overlay */}
            <div className="absolute inset-0 bg-black/10" />

            {/* Text + Edit Button Overlay */}
            <div className="absolute top-0 left-0 right-0 flex justify-between items-center px-5 py-3 bg-gradient-to-b from-black/60 to-transparent">
              <h3 className="text-white text-lg font-semibold">Hero Image</h3>
              <input
                type="file"
                accept="image/*"
                ref={heroInputRef}
                className="hidden"
                onChange={(e) => handleImageUpload(e, "hero")}
              />
              <button
                onClick={() => heroInputRef.current.click()}
                disabled={uploading}
                className="bg-white/80 hover:bg-white text-gray-700 p-2 rounded-full shadow-md transition"
                title="Edit Hero Image"
              >
                <Edit size={18} className="text-indigo-600" />
              </button>
            </div>
          </div>

          {/* RIGHT: Thumbnail Image (no background wrapper) */}
          <div className="relative w-full lg:w-[380px] rounded-xl overflow-hidden shadow-lg border border-gray-100">
            {eventData.thumbnailImage ? (
              <img
                src={eventData.thumbnailImage}
                alt="Event Thumbnail"
                className="w-full h-[420px] object-cover rounded-xl"
              />
            ) : (
              <div className="w-full h-56 flex items-center justify-center bg-gray-100 text-gray-400 rounded-xl">
                No Image
              </div>
            )}

            {/* Overlay for title + edit icon */}
            <div className="absolute top-0 left-0 right-0 flex justify-between items-center px-4 py-2 bg-gradient-to-b from-black/20 to-transparent">
              <h3 className="text-white text-base font-semibold">
                Thumbnail Image
              </h3>
              <input
                type="file"
                accept="image/*"
                ref={thumbInputRef}
                className="hidden"
                onChange={(e) => handleImageUpload(e, "thumbnail")}
              />
              <button
                onClick={() => thumbInputRef.current.click()}
                disabled={uploading}
                className="bg-white/80 hover:bg-white text-gray-700 p-1.5 rounded-full shadow-md transition"
                title="Edit Thumbnail"
              >
                <Edit size={16} className="text-indigo-600" />
              </button>
            </div>
          </div>
        </div>

        {/* Ticket Overview Cards */}
        <div className="bg-white shadow-lg rounded-xl p-6 mb-8">
          <h3 className="text-xl font-semibold mb-6 text-gray-800">
            Performance Overview
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {[
              {
                title: "Tickets Sold",
                value: accumulatedStats?.totalTicketsSold ?? 0,
                change: "Total purchased tickets",
                color: "text-blue-700",
                bg: "bg-blue-50",
                icon: <TrendingUp />,
              },
              {
                title: "Revenue",
                value: `₹${accumulatedStats?.totalRevenue ?? 0}`,
                change: "Total earnings from ticket sales",
                color: "text-green-700",
                bg: "bg-green-50",
                icon: <CircleDollarSign />,
              },
              {
                title: "Tickets Available",
                value: accumulatedStats?.totalTicketsAvailable ?? 0,
                change: "Unsold tickets across all sessions",
                color: "text-purple-700",
                bg: "bg-purple-50",
                icon: <TrendingDown />,
              },
              {
                title: "Avg Ticket Price",
                value: `₹${accumulatedStats?.averageTicketPrice ?? 0}`,
                change: "Average price across all ticket types",
                color: "text-yellow-700",
                bg: "bg-yellow-50",
                icon: <BarChart3 />,
              },
            ].map((stat, i) => (
              <div
                key={i}
                className={`${stat.bg} p-6 rounded-xl shadow hover:shadow-md transition`}
              >
                <div className="flex justify-between items-center mb-2">
                  <p className={`${stat.color} font-semibold text-sm`}>
                    {stat.title}
                  </p>
                  <span className={`${stat.color}`}>{stat.icon}</span>
                </div>
                <p className="text-3xl font-bold">{stat.value}</p>
                <p className={`${stat.color} text-sm mt-2`}>{stat.change}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Charts Section */}
        <div className="grid md:grid-cols-2 gap-6 mb-8">
          <div className="bg-white shadow-lg rounded-xl p-6">
            <h3 className="text-lg font-semibold mb-4 text-gray-800">
              Ticket Distribution
            </h3>
            {ticketDistributionSeries.some((v) => v > 0) ? (
              <Chart
                type="donut"
                height={250}
                series={ticketDistributionSeries}
                options={{
                  labels: ticketDistributionLabels,
                  colors: ["#4F46E5", "#818CF8", "#C7D2FE", "#E5E7EB"],
                  legend: { position: "bottom" },
                }}
              />
            ) : (
              <div className="text-center py-10 text-gray-400">
                No ticket distribution data
              </div>
            )}
          </div>

          <div className="bg-white shadow-lg rounded-xl p-6">
            <h3 className="text-lg font-semibold mb-4 text-gray-800">
              Revenue Breakdown
            </h3>
            {revenueSeries.some((v) => v > 0) ? (
              <Chart
                type="bar"
                height={250}
                series={[
                  {
                    name: "Revenue ($)",
                    data: revenueSeries,
                  },
                ]}
                options={{
                  chart: { toolbar: { show: false } },
                  colors: ["#6366F1"],
                  plotOptions: {
                    bar: { borderRadius: 6, columnWidth: "45%" },
                  },
                  xaxis: { categories: revenueCategories },
                  grid: { borderColor: "#E5E7EB" },
                }}
              />
            ) : (
              <div className="text-center py-10 text-gray-400">
                No revenue breakdown available
              </div>
            )}
          </div>
        </div>

        {/* Event Details */}
        <div className="mb-8">
          {/* RIGHT SIDE: Event Information Card */}
          <div className="bg-white shadow-lg rounded-xl p-6 flex-1">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-gray-800">
                Event Information
              </h2>
            </div>

            <div className="grid sm:grid-cols-2 gap-8">
              {/* LEFT COLUMN */}
              <div>
                <h3 className="text-base font-semibold mb-4">
                  Basic Information
                </h3>

                {/* Editable Event Name */}
                <div className="mb-4 flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-500">Event Name</p>
                    {isEditingName ? (
                      <div className="flex items-center gap-2 mt-1">
                        <input
                          type="text"
                          value={tempName}
                          onChange={(e) => setTempName(e.target.value)}
                          className="border border-gray-300 w-80 rounded-md px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        />
                        <button
                          onClick={handleSaveName}
                          className="text-green-600 hover:text-green-700"
                          title="Save"
                        >
                          <Check size={16} />
                        </button>
                        <button
                          onClick={() => setIsEditingName(false)}
                          className="text-gray-500 hover:text-gray-700"
                          title="Cancel"
                        >
                          <X size={16} />
                        </button>
                      </div>
                    ) : (
                      <div className="flex items-center gap-2 mt-1">
                        <p>{renderValue(eventData.title)}</p>
                        <button
                          onClick={() => setIsEditingName(true)}
                          className="text-indigo-600 hover:text-indigo-800 transition"
                          title="Edit Event Name"
                        >
                          <Edit size={16} />
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">Start Date & Time</p>
                  <p>{renderValue(eventData.startDate)}</p>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">End Date & Time</p>
                  <p>{renderValue(eventData.endDate)}</p>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">Timing</p>
                  <p>{renderValue(eventData.timing)}</p>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">Location</p>
                  <p>
                    {renderValue(eventData.location) ||
                      renderValue(eventData.venue)}
                  </p>
                </div>
              </div>

              {/* RIGHT COLUMN */}
              <div>
                <h3 className="text-base font-semibold mb-4">
                  Additional Details
                </h3>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">Category</p>
                  <p>{renderValue(eventData.category)}</p>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500">Recurrence</p>
                  <p>{renderValue(eventData.recurrence)}</p>
                </div>

                <div className="mb-4">
                  <p className="text-xs text-gray-500 mr-2">Status</p>
                  <div className="flex items-center">
                    <div
                      className={`w-2 h-2 rounded-full mr-2 ${
                        eventData.status === "completed"
                          ? "bg-red-600"
                          : "bg-green-600"
                      }`}
                    />
                    <span>{renderValue(eventData.status) || "Draft"}</span>
                  </div>
                </div>

                <div>
                  <p className="text-xs text-gray-500">Created On</p>
                  <p>
                    {eventData.createdAt ? formatDate(eventData.createdAt) : ""}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Session Tickets */}
        <div className="bg-white shadow-lg rounded-xl p-6 mb-20">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-800">
              Session-wise Tickets
            </h2>
            <div className="flex gap-3 items-center">
              {/* Info Icon + Tooltip */}
              <div className="relative group flex items-center">
                <Info
                  size={16}
                  className="text-gray-500 hover:text-indigo-600 cursor-pointer transition"
                />
                <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-max bg-gray-800 text-white text-xs rounded-md px-2 py-1 opacity-0 group-hover:opacity-100 transition pointer-events-none whitespace-nowrap shadow-lg">
                  Edit is enabled only for sessions not yet released
                </div>
              </div>

              {/* Edit Session Button (always visible) */}
              {(() => {
                const session = sessionsList.find(
                  (s) => s.id === selectedSession
                );
                const isEditable = session && canEditSession(session);

                return (
                  <button
                    onClick={() => handleOpenEdit(session)}
                    // disabled={!isEditable}
                    className={`flex items-center gap-1 px-3 py-2 rounded-lg font-medium transition ${
                      isEditable
                        ? "bg-indigo-50 text-indigo-700 hover:bg-indigo-100 cursor-pointer"
                        : "bg-gray-100 text-gray-400 cursor-not-allowed"
                    }`}
                  >
                    <Edit className="w-4 h-4" /> Edit Session
                  </button>
                );
              })()}

              {/* Session Selector */}
              <select
                className="border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                value={selectedSession}
                onChange={(e) => setSelectedSession(e.target.value)}
              >
                {sessionsList.length > 0 ? (
                  sessionsList.map((session) => (
                    <option key={session.id} value={session.id}>
                      {renderValue(session.name)}
                    </option>
                  ))
                ) : (
                  <option disabled>No Sessions</option>
                )}
              </select>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm text-gray-700 border-collapse">
              <thead className="bg-gray-100">
                <tr>
                  {["Name", "Price", "Sold", "Available", "Status"].map(
                    (head) => (
                      <th
                        key={head}
                        className="px-4 py-3 text-left font-semibold"
                      >
                        {head}
                      </th>
                    )
                  )}
                </tr>
              </thead>
              <tbody>
                {sessionTickets[selectedSession] &&
                sessionTickets[selectedSession].length > 0 ? (
                  sessionTickets[selectedSession].map((row, i) => (
                    <tr
                      key={i}
                      className={i % 2 === 0 ? "bg-white" : "bg-gray-50"}
                    >
                      <td className="px-4 py-3">{renderValue(row.type)}</td>
                      <td className="px-4 py-3">₹{renderValue(row.price)}</td>
                      <td className="px-4 py-3">{renderValue(row.sold)}</td>
                      <td className="px-4 py-3">
                        {renderValue(row.available)}
                      </td>
                      <td className="px-4 py-3">{row.status}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5} className="text-center py-4 text-gray-500">
                      No tickets available for this session.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {openEditDialog && editedSession && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[1100]">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6 relative">
              <button
                onClick={handleClose}
                className="absolute top-3 right-3 text-gray-500 hover:text-gray-700"
              >
                <X size={20} />
              </button>

              <h3 className="text-lg font-semibold mb-4">
                Edit Session – {editedSession.name}
              </h3>

              <div className="space-y-4">
                {/* Start Time */}
                <div>
                  <label className="text-sm font-medium text-gray-600 block mb-1">
                    Start Time
                  </label>
                  <input
                    type="time"
                    value={editedSession.startTime || ""}
                    onChange={(e) =>
                      setEditedSession({
                        ...editedSession,
                        startTime: e.target.value,
                      })
                    }
                    className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                {/* End Time */}
                <div>
                  <label className="text-sm font-medium text-gray-600 block mb-1">
                    End Time
                  </label>
                  <input
                    type="time"
                    value={editedSession.endTime || ""}
                    onChange={(e) =>
                      setEditedSession({
                        ...editedSession,
                        endTime: e.target.value,
                      })
                    }
                    className="w-full border rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                {/* Ticket Prices */}
                <div>
                  <label className="text-sm font-medium text-gray-600 block mb-1">
                    Ticket Prices
                  </label>
                  {editedSession.tickets?.map((ticket, i) => (
                    <div
                      key={i}
                      className="flex justify-between items-center mb-2"
                    >
                      <span className="text-sm">{ticket.type}</span>
                      <input
                        type="number"
                        value={ticket.price || ""}
                        onChange={(e) => {
                          const updatedTickets = [...editedSession.tickets];
                          updatedTickets[i].price = e.target.value;
                          setEditedSession({
                            ...editedSession,
                            tickets: updatedTickets,
                          });
                        }}
                        className="w-24 border rounded-lg px-2 py-1 text-sm"
                      />
                    </div>
                  ))}
                </div>

                {/* Delete Button */}
                <button
                  onClick={handleDelete}
                  className="flex items-center justify-center gap-2 text-red-600 hover:text-red-700 mt-2 text-sm font-medium"
                >
                  <Trash2 size={16} /> Delete Session
                </button>
              </div>

              {/* Actions */}
              <div className="flex justify-end gap-3 mt-6">
                <button
                  onClick={handleClose}
                  className="px-4 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 text-sm"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSave}
                  className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 text-sm"
                >
                  Save Changes
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
