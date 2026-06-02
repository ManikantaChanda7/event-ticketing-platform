import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import api from "../../utils/api";

export const fetchOrganizer = createAsyncThunk(
  "organizerProfile/fetchOrganizer",
  async (organizerId) => {
    let response = await api.get(`organizers/${organizerId}`);
    return response.data;
  },
);

export const fetchOrganizerEvents = createAsyncThunk(
  "organizerProfile/fetchOrganizerEvents",
  async ({ organizerId, query }) => {
    let response = await api.get(`events/organizer/${organizerId}`, {
      params: query,
    });
    return response.data;
  },
);

export const updateOrganizerProfile = createAsyncThunk(
  "organizer/updateProfile",
  async (formData, { rejectWithValue }) => {
    try {
      const res = await api.put(`/organizer/update`, formData);
      return res.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Update failed");
    }
  },
);

// 3️⃣ Update Password
export const updateOrganizerPassword = createAsyncThunk(
  "organizer/updatePassword",
  async (passwordData, { rejectWithValue }) => {
    try {
      const res = await api.put(`/organizer/update-password`, passwordData);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Password update failed");
    }
  },
);

export const getDashboardStats = createAsyncThunk(
  "organizer/getDashboardStats",
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get(`/organizer/dashboard`);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to fetch stats");
    }
  },
);

export const getManageEventData = createAsyncThunk(
  "organizer/getManageEventData",
  async (eventId, { rejectWithValue }) => {
    try {
      const res = await api.get(`/organizer/manage-event/${eventId}`);
      return res.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch event data",
      );
    }
  },
);

export const getOrganizerEvents = createAsyncThunk(
  "organizer/getEvents",
  async (params, { rejectWithValue }) => {
    try {
      const res = await api.get("organizer/events", { params });
      return res.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch organizer events",
      );
    }
  },
);

export const getOrganizerProfileByUserId = createAsyncThunk(
  "organizer/getProfileByUserId",
  async (userId, { rejectWithValue }) => {
    try {
      const res = await api.get(`organizer/profile/user/${userId}`);
      return res.data.data; // return the actual data object
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Failed to fetch organizer profile",
      );
    }
  },
);

export const getOrganizerBasicStats = createAsyncThunk(
  "organizer/basic",
  async (_, thunkAPI) => {
    const res = await api.get("/organizer/dashboard/basic");
    return res.data;
  },
);

export const getOrganizerSalesStats = createAsyncThunk(
  "organizer/sales",
  async (_, thunkAPI) => {
    const res = await api.get("/organizer/dashboard/sales");
    return res.data;
  },
);

export const getOrganizerCategoryStats = createAsyncThunk(
  "organizer/categories",
  async (_, thunkAPI) => {
    const res = await api.get("/organizer/dashboard/categories");
    return res.data;
  },
);

export const getOrganizerTopSellingEvents = createAsyncThunk(
  "organizer/topSelling",
  async (_, thunkAPI) => {
    const res = await api.get("/organizer/dashboard/top-selling");
    return res.data;
  },
);

export const getOrganizerUpcomingEvents = createAsyncThunk(
  "organizer/upcoming",
  async (_, thunkAPI) => {
    const res = await api.get("/organizer/dashboard/upcoming");
    return res.data;
  },
);

export const organizerProfileSlice = createSlice({
  name: "organizerProfile",
  initialState: {
    organizer: null,

    organizerLoading: false,
    organizerError: null,
    organizerSuccess: false,
    organizerProfile: null,
    organizerEventsLoading: false,
    organizerEventsError: null,
    organizerEventsSuccess: false,
    profileLoading: false,
    dashboardStats: [],
    events: [],
    totalPages: 1,
    basicLoading: false,
    basicError: null,

    salesLoading: false,
    salesError: null,

    categoriesLoading: false,
    categoriesError: null,

    topSellingLoading: false,
    topSellingError: null,

    upcomingLoading: false,
    upcomingError: null,
    manageEventData: null,
    dashboardLoading: false,
    manageEventPageLoading: false,
    loading: false,
    error: null,
    organizerEvents: [],
    loadingEvents: false,
    eventsError: null,

    updateOrgLoading: false,
    updateOrgError: null,
    updateOrgSuccess: false,
  },
  reducers: {
    clearOrgStatus: (state) => {
      state.updateOrgSuccess = false;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrganizer.pending, (state) => {
        state.organizerLoading = true;
        state.organizerError = null;
      })
      .addCase(fetchOrganizer.fulfilled, (state, action) => {
        state.organizerLoading = false;
        state.organizer = action.payload.data;
        state.organizerSuccess = true;
      })
      .addCase(fetchOrganizer.rejected, (state, action) => {
        state.organizerLoading = false;
        state.organizerError = action.payload || "Something went wrong";
        state.organizer = null;
      })

      .addCase(getOrganizerProfileByUserId.pending, (state) => {
        state.profileLoading = true;
        state.error = null;
      })
      .addCase(getOrganizerProfileByUserId.fulfilled, (state, action) => {
        state.profileLoading = false;
        state.organizerProfile = action.payload;
      })
      .addCase(getOrganizerProfileByUserId.rejected, (state, action) => {
        state.profileLoading = false;
        state.error = action.payload;
      })

      .addCase(fetchOrganizerEvents.pending, (state) => {
        state.organizerEventsLoading = true;
        state.organizerEventsError = null;
      })
      .addCase(fetchOrganizerEvents.fulfilled, (state, action) => {
        state.organizerEventsLoading = false;
        state.organizerEvents = action.payload;
        state.organizerEventsSuccess = true;
        state.totalPages = action.payload?.data?.totalPages || 1;
      })
      .addCase(fetchOrganizerEvents.rejected, (state, action) => {
        state.organizerEventsLoading = false;
        state.organizerEventsError = action.payload || "Something went wrong";
        state.organizerEvents = null;
      })

      .addCase(updateOrganizerProfile.pending, (state) => {
        state.updateOrgLoading = true;
      })
      .addCase(updateOrganizerProfile.fulfilled, (state, action) => {
        state.updateOrgLoading = false;
        state.organizer = action.payload; // update UI instantly
        state.updateOrgSuccess = true;
      })
      .addCase(updateOrganizerProfile.rejected, (state, action) => {
        state.updateOrgLoading = false;
        state.updateOrgError = action.payload;
      })

      .addCase(getOrganizerEvents.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getOrganizerEvents.fulfilled, (state, action) => {
        state.loading = false;
        state.events = action.payload.data || [];
        state.totalPages = action.payload.pagination?.totalPages || 1;
      })
      .addCase(getOrganizerEvents.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Something went wrong";
      })

      .addCase(getDashboardStats.pending, (state) => {
        state.dashboardLoading = true;
        state.error = null;
      })
      .addCase(getDashboardStats.fulfilled, (state, action) => {
        state.dashboardLoading = false;
        state.dashboardStats = action.payload;
        state.error = null;
      })
      .addCase(getDashboardStats.rejected, (state, action) => {
        state.dashboardLoading = false;
        state.error = action.payload || "Something went wrong";
      })

      .addCase(getManageEventData.pending, (state) => {
        state.manageEventPageLoading = true;
        state.error = null;
      })
      .addCase(getManageEventData.fulfilled, (state, action) => {
        state.manageEventPageLoading = false;
        state.manageEventData = action.payload;
        state.error = null;
      })
      .addCase(getManageEventData.rejected, (state, action) => {
        state.manageEventPageLoading = false;
        state.error = action.payload || "Something went wrong";
      })

      // UPDATE PASSWORD
      .addCase(updateOrganizerPassword.pending, (state) => {
        // state.passwordLoading = true;
      })
      .addCase(updateOrganizerPassword.fulfilled, (state) => {
        // state.passwordLoading = false;
      })
      .addCase(updateOrganizerPassword.rejected, (state, action) => {
        // state.passwordLoading = false;
        state.error = action.payload;
      });

    builder
      .addCase(getOrganizerBasicStats.pending, (state) => {
        state.basicLoading = true;
        state.basicError = null;
      })
      .addCase(getOrganizerBasicStats.fulfilled, (state, action) => {
        state.basicLoading = false;
        state.dashboardStats = {
          ...state.dashboardStats,
          ...action.payload,
        };
      })
      .addCase(getOrganizerBasicStats.rejected, (state, action) => {
        state.basicLoading = false;
        state.basicError = action.payload || "Failed to load basic stats";
      });

    builder
      .addCase(getOrganizerSalesStats.pending, (state) => {
        state.salesLoading = true;
        state.salesError = null;
      })
      .addCase(getOrganizerSalesStats.fulfilled, (state, action) => {
        state.salesLoading = false;
        state.dashboardStats = {
          ...state.dashboardStats,
          ...action.payload,
        };
      })
      .addCase(getOrganizerSalesStats.rejected, (state, action) => {
        state.salesLoading = false;
        state.salesError = action.payload || "Failed to load sales stats";
      });

    builder
      .addCase(getOrganizerCategoryStats.pending, (state) => {
        state.categoriesLoading = true;
        state.categoriesError = null;
      })
      .addCase(getOrganizerCategoryStats.fulfilled, (state, action) => {
        state.categoriesLoading = false;
        state.dashboardStats = {
          ...state.dashboardStats,
          ...action.payload,
        };
      })
      .addCase(getOrganizerCategoryStats.rejected, (state, action) => {
        state.categoriesLoading = false;
        state.categoriesError =
          action.payload || "Failed to load category stats";
      });

    builder
      .addCase(getOrganizerTopSellingEvents.pending, (state) => {
        state.topSellingLoading = true;
        state.topSellingError = null;
      })
      .addCase(getOrganizerTopSellingEvents.fulfilled, (state, action) => {
        state.topSellingLoading = false;
        state.dashboardStats = {
          ...state.dashboardStats,
          topSelling: action.payload.topSelling,
        };
      })
      .addCase(getOrganizerTopSellingEvents.rejected, (state, action) => {
        state.topSellingLoading = false;
        state.topSellingError =
          action.payload || "Failed to load top-selling events";
      });

    builder
      .addCase(getOrganizerUpcomingEvents.pending, (state) => {
        state.upcomingLoading = true;
        state.upcomingError = null;
      })
      .addCase(getOrganizerUpcomingEvents.fulfilled, (state, action) => {
        state.upcomingLoading = false;
        state.dashboardStats = {
          ...state.dashboardStats,
          upcomingEvents: action.payload.upcomingEvents,
        };
      })
      .addCase(getOrganizerUpcomingEvents.rejected, (state, action) => {
        state.upcomingLoading = false;
        state.upcomingError =
          action.payload || "Failed to load upcoming events";
      });
  },
});
export const { clearOrgStatus } = organizerProfileSlice.actions;
export const organizerProfileReducer = organizerProfileSlice.reducer;
