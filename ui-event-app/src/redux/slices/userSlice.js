import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../../utils/api";

// 1️⃣ Fetch User Details
export const fetchUserDetails = createAsyncThunk(
  "user/fetchUserDetails",
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get("user/profile");
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

// 2️⃣ Update Profile (name + phone)
export const updateProfile = createAsyncThunk(
  "user/updateProfile",
  async (formData, { rejectWithValue }) => {
    try {
      const response = await api.put("user/update-profile", formData);
      return response.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

// 3️⃣ Update Email
export const updateEmail = createAsyncThunk(
  "user/updateEmail",
  async (formData, { rejectWithValue }) => {
    try {
      const response = await api.put("user/update-email", formData);
      return response.data.user;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

// 4️⃣ Update Password
export const updatePassword = createAsyncThunk(
  "user/updatePassword",
  async (formData, { rejectWithValue }) => {
    try {
      const response = await api.put("user/update-password", formData);
      return response.data.message; // password usually returns message
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

export const isUserBookedEvent = createAsyncThunk(
  "user/isUserBookedEvent",
  async (eventId, { rejectWithValue }) => {
    try {
      const response = await api.get(`bookings/event/${eventId}/is-booked`);
      return response.data.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  },
);

export const fetchUserBookings = createAsyncThunk(
  "user/fetchUserBookings",
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get("bookings/my-bookings");
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to fetch bookings");
    }
  },
);

export const searchCities = createAsyncThunk(
  "user/searchCities",
  async (query, { rejectWithValue }) => {
    try {
      const res = await api.get(`cities/search?q=${encodeURIComponent(query)}`);
      return res.data; // list of cities
    } catch (err) {
      return rejectWithValue(err.response?.data || "City search failed");
    }
  },
);

export const fetchAllCities = createAsyncThunk(
  "user/fetchAllCities",
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get("cities/search?q=");
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to fetch cities");
    }
  },
);

const userSlice = createSlice({
  name: "user",
  initialState: {
    user: null,
    eventBooked: false,
    userBookings: [],
    loading: false,
    error: null,
    updateSuccess: false,
    citySearchResults: [],
    citySearchLoading: false,
    citySearchError: null,
    userBookedEventLoading: false,
    userBookedEventError: null,
    userBookedEventSuccess: false,
    updateProfileLoading: false,
    updateProfileError: null,
    updateProfileSuccess: false,
    updateEmailLoading: false,
    updateEmailError: null,
    updateEmailSuccess: false,
    updatePasswordLoading: false,
    updatePasswordError: null,
    updatePasswordSuccess: false,
    fetchBookingsLoading: false,
    fetchBookingsError: null,
    fetchBookingsSuccess: false,
    fetchUserDetailsLoading: false,
    fetchUserDetailsError: null,
    fetchUserDetailsSuccess: false,
  },

  reducers: {
    clearStatus: (state) => {
      state.updateEmailSuccess = false;
      state.updatePasswordSuccess = false;
      state.updateProfileSuccess = false;
    },
  },

  extraReducers: (builder) => {
    builder
      // 👉 FETCH USER
      .addCase(fetchUserDetails.pending, (state) => {
        state.fetchUserDetailsLoading = true;
        state.fetchUserDetailsError = null;
      })
      .addCase(fetchUserDetails.fulfilled, (state, action) => {
        state.fetchUserDetailsLoading = false;
        state.user = action.payload.data;
        state.fetchUserDetailsSuccess = true;
      })
      .addCase(fetchUserDetails.rejected, (state, action) => {
        state.fetchUserDetailsLoading = false;
        state.fetchUserDetailsError = action.payload;
      })

      .addCase(fetchUserBookings.pending, (state) => {
        state.fetchBookingsLoading = true;
        state.fetchBookingsError = null;
      })
      .addCase(fetchUserBookings.fulfilled, (state, action) => {
        state.fetchBookingsLoading = false;
        state.userBookings = action.payload.data;
        state.fetchBookingsSuccess = true;
      })
      .addCase(fetchUserBookings.rejected, (state, action) => {
        state.fetchBookingsLoading = false;
        state.fetchBookingsError = action.payload || "Something went wrong";
      })

      .addCase(searchCities.pending, (state) => {
        state.citySearchLoading = true;
        state.citySearchError = null;
      })

      .addCase(searchCities.fulfilled, (state, action) => {
        state.citySearchLoading = false;
        state.citySearchResults = action.payload.data || [];
      })

      .addCase(searchCities.rejected, (state, action) => {
        state.citySearchLoading = false;
        state.citySearchError = action.payload;
      })

      .addCase(fetchAllCities.pending, (state) => {
        state.citySearchLoading = true;
      })
      .addCase(fetchAllCities.fulfilled, (state, action) => {
        state.citySearchLoading = false;
        state.citySearchResults = action.payload.data || [];
      })
      .addCase(fetchAllCities.rejected, (state, action) => {
        state.citySearchLoading = false;
        state.error = action.payload;
      })

      // 👉 UPDATE PROFILE
      .addCase(updateProfile.pending, (state) => {
        state.updateProfileLoading = true;
        state.updateProfileError = null;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.updateProfileLoading = false;
        state.user = action.payload;
        state.updateProfileSuccess = true;
      })
      .addCase(updateProfile.rejected, (state, action) => {
        state.updateProfileLoading = false;
        state.updateProfileError = action.payload;
      })

      // 👉 UPDATE EMAIL
      .addCase(updateEmail.pending, (state) => {
        state.updateEmailLoading = true;
        state.updateEmailError = null;
      })
      .addCase(updateEmail.fulfilled, (state, action) => {
        state.updateEmailLoading = false;
        state.user = action.payload;
        state.updateEmailSuccess = true;
      })
      .addCase(updateEmail.rejected, (state, action) => {
        state.updateEmailLoading = false;
        state.updateEmailError = action.payload;
      })

      // 👉 UPDATE PASSWORD
      .addCase(updatePassword.pending, (state) => {
        state.updatePasswordLoading = true;
        state.updatePasswordError = null;
      })
      .addCase(updatePassword.fulfilled, (state) => {
        state.updatePasswordLoading = false;
        state.updatePasswordSuccess = true;
      })
      .addCase(updatePassword.rejected, (state, action) => {
        state.updatePasswordLoading = false;
        state.updatePasswordError = action.payload;
      })

      // 👉 IS USER BOOKED EVENT
      .addCase(isUserBookedEvent.pending, (state) => {
        state.userBookedEventLoading = true;
        state.userBookedEventError = null;
      })
      .addCase(isUserBookedEvent.fulfilled, (state, action) => {
        state.userBookedEventLoading = false;
        state.eventBooked = action.payload;
        state.userBookedEventSuccess = true;
      })
      .addCase(isUserBookedEvent.rejected, (state, action) => {
        state.userBookedEventLoading = false;
        state.userBookedEventError = action.payload;
      });
  },
});

export const { clearStatus } = userSlice.actions;
export const userReducer = userSlice.reducer;
