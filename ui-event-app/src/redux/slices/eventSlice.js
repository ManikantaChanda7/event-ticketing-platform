import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../../utils/api";

export const fetchHomeEvents = createAsyncThunk(
  "event/fetchHomeEvents",
  async () => {
    const response = await api.get("events/homeEvents", {
      withCredentials: true,
    });
    return response.data;
  }
);

export const fetchPopularEvents = createAsyncThunk(
  "event/fetchPopularEvents",
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get("events/popularEvents", {
        withCredentials: true,
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || "Failed to load popular events"
      );
    }
  }
);

export const fetchTrendingEvents = createAsyncThunk(
  "event/fetchTrendingEvents",
  async (_, { rejectWithValue }) => {
    try {
      ``;
      const response = await api.get("events/trendingEvents", {
        withCredentials: true,
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || "Failed to load trending events"
      );
    }
  }
);

export const fetchRecommendedEvents = createAsyncThunk(
  "event/fetchRecommendedEvents",
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get("events/recommendedEvents", {
        withCredentials: true,
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || "Failed to load recommended events"
      );
    }
  }
);

// Fetch single event by id (full details)
export const fetchEventById = createAsyncThunk(
  "event/fetchEventById",
  async (eventId, { rejectWithValue }) => {
    try {
      const response = await api.get(`events/${eventId}`, {
        withCredentials: true,
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || "Failed to load event"
      );
    }
  }
);

// Fetch single event venue
export const fetchEventVenue = createAsyncThunk(
  "event/fetchEventVenue",
  async (venueId) => {
    const response = await api.get(`venue/${venueId}`);
    return response.data;
  }
);

// Fetch filtered events dynamically
// Accepts query object, with optional `key` to store in filteredEvents
export const fetchFilteredEvents = createAsyncThunk(
  "event/fetchFilteredEvents",
  async (query, { rejectWithValue }) => {
    try {
      const response = await api.get(`events/filtered`, {
        params: query.params,
        withCredentials: true,
      });
      return { key: query.key, data: response.data };
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || "Failed to fetch filtered events"
      );
    }
  }
);

export const fetchSimilarEvents = createAsyncThunk(
  "events/fetchSimilarEvents",
  async (query) => {
    const response = await api.get(`events/${query.id}/similarEvents`);
    return { key: query.key, data: response.data };
  }
);

export const fetchUserInterestedEvents = createAsyncThunk(
  "event/fetchUserInterestedEvents",
  async (query) => {
    const response = await api.get("events/user/interests", {
      params: query.params,
    });
    return { key: query.key, data: response.data };
  }
);

export const updateEventTitle = createAsyncThunk(
  "event/updateEventName",
  async ({ eventId, tempName }, { rejectWithValue }) => {
    try {
      const res = await api.put(`/events/${eventId}/updateEventName`, {
        title: tempName,
      });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Event name update failed");
    }
  }
);

export const bookTickets = createAsyncThunk(
  "event/bookTickets",
  async ({ eventId, sessionId, selectedSeats }, { rejectWithValue }) => {
    try {
      const response = await api.post("/bookings", {
        eventId,
        sessionId,
        selectedSeats,
      });

      return response.data; // booking confirmation
    } catch (error) {
      return rejectWithValue(
        error.response?.data || "Booking failed. Please try again."
      );
    }
  }
);

export const writeReview = createAsyncThunk(
  "event/writeReview",
  async ({ eventId, rating, review }, { rejectWithValue }) => {
    try {
      const response = await api.post(`events/${eventId}/writeReview`, {
        rating,
        review,
      });

      return response.data; // backend response
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to submit review. Try again."
      );
    }
  }
);

export const fetchSessionsCount = createAsyncThunk(
  "event/fetchSessionsCount",
  async (eventId, { rejectWithValue }) => {
    try {
      const res = await api.get(`events/${eventId}/sessionsCount`);
      return res.data; // return backend response
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch sessions count"
      );
    }
  }
);

export const createEvent = createAsyncThunk(
  "event/createEvent",
  async (eventData, { rejectWithValue }) => {
    try {
      const res = await api.post("events", eventData); // token auto-added by interceptor
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to create event");
    }
  }
);

export const getCategories = createAsyncThunk(
  "events/getCategories",
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get("events/categories");
      return res.data.categories; // return categories array
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch categories"
      );
    }
  }
);

export const updateEventImage = createAsyncThunk(
  "event/updateEventImage",
  async ({ eventId, type, url }, { rejectWithValue }) => {
    try {
      const res = await api.put(`events/${eventId}/updateEventImage`, {
        type, // "banner" or "thumbnail"
        url,
      });

      return { eventId, type, url }; // return updated details
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to update event image"
      );
    }
  }
);

export const fetchVenueAvailability = createAsyncThunk(
  "event/fetchVenueAvailability",
  async ({ startDate, endDate }, { rejectWithValue }) => {
    try {
      const res = await api.get("venue/availability", {
        params: {
          startDate,
          endDate: endDate || undefined,
        },
      });

      return res.data.data || []; // return venue list only
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch venue availability"
      );
    }
  }
);

export const fetchVenueInfo = createAsyncThunk(
  "event/fetchVenueInfo",
  async ({ id }, { rejectWithValue }) => {
    try {
      const res = await api.get(`venue/${id}`);

      return res.data.data; // return venue list only
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch venue availability"
      );
    }
  }
);

const initialState = {
  events: [], // all events
  filteredEvents: {}, // dynamic map for any section: { popular: [], online: [], music: [] }
  filteredLoading: {}, // ← dynamic loading state per key
  filteredError: {},
  filteredSuccess: {},
  similarLoading: false,
  similarError: null,
  similarSuccess: false,
  filtersApplied: false,
  categories: [],

  selectedEvent: null,
  selectedEventVenue: null,
  selectedEventSessionsAvailable: false,
  eventVenueLoading: false,
  eventVenueError: null,
  eventVenueSuccess: false,
  bookingLoader: false,
  bookingSuccess: false,
  bookingError: null,
  reviewLoader: false,
  reviewSuccess: false,
  reviewError: null,
  totalItems: 0,
  totalPages: 0,
  loading: false,
  error: null,
  updateTitleLoading: false,
  updateTitleError: null,
  updateTitleSuccess: false,
  updateImageLoading: false,
  updateImageError: null,
  updateImageSuccess: false,
  createEventLoading: false,
  createEventError: null,
  createEventSuccess: false,
  sessionsCountError: null,
  sessionsCountLoading: false,
  sessionsCountSuccess: false,
};

export const eventSlice = createSlice({
  name: "event",
  initialState,
  reducers: {
    setSelectedEvent: (state, action) => {
      state.selectedEvent = action.payload;
    },
    updateSelectedEvent: (state, action) => {
      state.selectedEvent = action.payload;
    },
    setFiltersApplied: (state, action) => {
      state.filtersApplied = action.payload;
    },
    setTitleSuccessStatus: (state, action) => {
      state.updateTitleSuccess = action.payload;
    },
    setUpdateImageSuccessStatus: (state, action) => {
      state.updateImageSuccess = action.payload;
    },
    setCreateEventSuccessStatus: (state, action) => {
      state.createEventSuccess = action.payload;
    },
    setReviewSuccessStatus: (state, action) => {
      state.reviewSuccess = action.payload;
    },
    setBookingSuccessStatus: (state, action) => {
      state.bookingSuccess = action.payload;
    },
    updateInterestedUsers: (state, action) => {
      state.selectedEvent.interestedUsers = action.payload;
    },
  },
  extraReducers: (builder) => {
    const handlePending = (state) => {
      state.loading = true;
      state.error = null;
    };
    const handleRejected = (state, action) => {
      state.loading = false;
      state.error = action.error?.message || "Something went wrong";
    };

    builder

      .addCase(fetchHomeEvents.pending, handlePending)
      .addCase(fetchHomeEvents.fulfilled, (state, action) => {
        state.loading = false;
        state.filteredEvents["popular"] = action.payload.data.popularEvents;
        state.filteredEvents["recommended"] =
          action.payload.data.recommendedEvents;
        state.filteredEvents["trending"] = action.payload.data.trendingEvents;
      })
      .addCase(fetchHomeEvents.rejected, handleRejected)

      .addCase(fetchPopularEvents.pending, (state) => {
        state.filteredLoading["popular"] = true; // NEW: dynamic loader
        state.filteredError["popular"] = null;
      })
      .addCase(fetchPopularEvents.fulfilled, (state, action) => {
        state.filteredLoading["popular"] = false;
        state.filteredEvents["popular"] = action.payload.data;
      })
      .addCase(fetchPopularEvents.rejected, (state, action) => {
        state.filteredLoading["popular"] = false; // NEW
        state.filteredError["popular"] =
          action.payload || "Failed to load recommended events";
      })

      .addCase(fetchTrendingEvents.pending, (state) => {
        state.filteredLoading["trending"] = true; // NEW: dynamic loader
        state.filteredError["trending"] = null;
      })
      .addCase(fetchTrendingEvents.fulfilled, (state, action) => {
        state.filteredLoading["trending"] = false;
        state.filteredEvents["trending"] = action.payload.data;
      })
      .addCase(fetchTrendingEvents.rejected, (state, action) => {
        state.filteredLoading["trending"] = false; // NEW
        state.filteredError["trending"] =
          action.payload || "Failed to load recommended events";
      })

      .addCase(fetchRecommendedEvents.pending, (state) => {
        state.filteredLoading["recommended"] = true; // NEW: dynamic loader
        state.filteredError["recommended"] = null;
      })
      .addCase(fetchRecommendedEvents.fulfilled, (state, action) => {
        state.filteredLoading["recommended"] = false;
        state.filteredEvents["recommended"] = action.payload.data;
      })
      .addCase(fetchRecommendedEvents.rejected, (state, action) => {
        state.filteredLoading["recommended"] = false; // NEW
        state.filteredError["recommended"] =
          action.payload || "Failed to load recommended events";
      })

      // fetchEventVenue
      .addCase(fetchEventVenue.pending, (state) => {
        state.eventVenueLoading = true;
        state.eventVenueError = null;
      })
      .addCase(fetchEventVenue.fulfilled, (state, action) => {
        state.eventVenueLoading = false;
        state.selectedEventVenue = action.payload.data;
        state.eventVenueSuccess = true;
      })
      .addCase(fetchEventVenue.rejected, (state, action) => {
        state.eventVenueLoading = false;
        state.eventVenueError = action.payload;
      })

      // fetchFilteredEvents (dynamic sections)
      .addCase(fetchFilteredEvents.pending, (state, action) => {
        const key = action.meta.arg.key;
        if (key) {
          state.filteredLoading[key] = true;
          state.filteredError[key] = null;
        }
      })

      .addCase(fetchFilteredEvents.fulfilled, (state, action) => {
        state.loading = false;
        const { key, data } = action.payload;
        if (key) {
          state.filteredLoading[key] = false;
          state.filteredEvents[key] = data;
          state.filteredSuccess[key] = true;
        }
      })
      .addCase(fetchFilteredEvents.rejected, (state, action) => {
        const key = action.meta.arg.key;

        if (key) {
          state.filteredLoading[key] = false;
          state.filteredError[key] =
            action.error?.message || "Failed to fetch filtered events";
        }
      })

      // fetchFilteredEvents (dynamic sections)
      .addCase(fetchSimilarEvents.pending, (state) => {
        state.similarLoading = true;
        state.similarError = null;
      })
      .addCase(fetchSimilarEvents.fulfilled, (state, action) => {
        state.similarLoading = false;
        state.similarSuccess = true;
        const { key, data } = action.payload;
        if (key) {
          state.filteredEvents[key] = data; // store in dynamic key
        }
      })
      .addCase(fetchSimilarEvents.rejected, (state, action) => {
        state.similarLoading = false;
        state.similarError = action.payload;
      })

      .addCase(bookTickets.pending, (state) => {
        state.bookingLoader = true;
        state.bookingSuccess = false;
        state.bookingError = null;
      })
      .addCase(bookTickets.fulfilled, (state, action) => {
        state.bookingLoader = false;
        state.bookingSuccess = true;
      })
      .addCase(bookTickets.rejected, (state, action) => {
        state.bookingLoader = false;
        state.bookingSuccess = false;
        state.bookingError =
          action.payload || "Booking failed. Please try again.";
      })

      .addCase(fetchSessionsCount.pending, (state) => {
        state.sessionsCountLoading = true;
        state.sessionsCountError = null;
      })
      .addCase(fetchSessionsCount.fulfilled, (state, action) => {
        state.sessionsCountLoading = false;
        state.sessionsCountSuccess = true;
        state.selectedEventSessionsAvailable = action.payload.sessionsAvailable; // backend returns {count: X}
      })
      .addCase(fetchSessionsCount.rejected, (state, action) => {
        state.sessionsCountLoading = false;
        state.sessionsCountError = action.payload;
      })

      .addCase(writeReview.pending, (state) => {
        state.reviewLoader = true;
        state.reviewSuccess = false;
        state.reviewError = null;
      })
      .addCase(writeReview.fulfilled, (state, action) => {
        state.reviewLoader = false;
        state.reviewSuccess = true;
      })
      .addCase(writeReview.rejected, (state, action) => {
        state.reviewLoader = false;
        state.reviewSuccess = false;
        state.reviewError =
          action.payload || "Failed to submit review. Try again.";
      })

      .addCase(updateEventImage.pending, (state) => {
        state.updateImageLoading = true;
        state.updateImageError = null;
      })
      .addCase(updateEventImage.fulfilled, (state, action) => {
        state.updateImageLoading = false;
        state.updateImageSuccess = true;
      })
      .addCase(updateEventImage.rejected, (state, action) => {
        state.updateImageLoading = false;
        state.updateImageError =
          action.payload || "Failed to update event image";
      })

      .addCase(updateEventTitle.pending, (state) => {
        state.updateTitleLoading = true;
        state.updateTitleError = null;
      })
      .addCase(updateEventTitle.fulfilled, (state, action) => {
        state.updateTitleLoading = false;
        state.updateTitleSuccess = true;
      })
      .addCase(updateEventTitle.rejected, (state, action) => {
        state.updateTitleLoading = false;
        state.updateTitleError = action.payload || "Something went wrong";
      })

      .addCase(createEvent.pending, (state) => {
        state.createEventLoading = true;
        state.createEventError = null;
        state.createEventSuccess = false;
      })
      .addCase(createEvent.fulfilled, (state, action) => {
        state.createEventLoading = false;
        state.createEventSuccess = true;
      })
      .addCase(createEvent.rejected, (state, action) => {
        state.createEventLoading = false;
        state.createEventError = action.payload || "Something went wrong";
        state.createEventSuccess = false;
      })

      .addCase(getCategories.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getCategories.fulfilled, (state, action) => {
        state.loading = false;
        state.categories = action.payload;
      })
      .addCase(getCategories.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to load categories";
      })

      .addCase(fetchUserInterestedEvents.pending, handlePending)
      .addCase(fetchUserInterestedEvents.fulfilled, (state, action) => {
        state.loading = false;
        const { key, data } = action.payload;
        if (key) {
          state.filteredEvents[key] = data; // store in dynamic key
        }
      })
      .addCase(fetchUserInterestedEvents.rejected, handleRejected)

      .addCase(fetchEventById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchEventById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedEvent = action.payload.data;
      })
      .addCase(fetchEventById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to load event";
      });
  },
});

export const {
  setSelectedEvent,
  updateSelectedEvent,
  setFiltersApplied,
  setTitleSuccessStatus,
  setUpdateImageSuccessStatus,
  setCreateEventSuccessStatus,
  setReviewSuccessStatus,
  setBookingSuccessStatus,
  updateInterestedUsers,
} = eventSlice.actions;
export const eventReducer = eventSlice.reducer;
