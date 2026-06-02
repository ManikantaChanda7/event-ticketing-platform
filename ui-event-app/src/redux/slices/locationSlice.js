import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../../utils/api";

export const fetchLocation = createAsyncThunk(
  "location/fetchLocation",
  async () => {
    const response = await api.get("user/selectedLocation", {
      withCredentials: true, // ✅ must be here
    });
    return response.data;
  },
);

export const updateLocation = createAsyncThunk(
  "location/updateLocation",
  async ({ lat, lng, label }) => {
    const response = await api.put(
      "user/location",
      { latitude: lat, longitude: lng, locationLabel: label },
      {
        withCredentials: true, // ✅ must be here
      },
    );
    return response.data;
  },
);

const initialState = {
  location: null,
  loading: false,
  error: null,
};

export const locationSlice = createSlice({
  name: "location",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchLocation.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchLocation.fulfilled, (state, action) => {
        state.loading = false;
        state.location = action.payload.data;
      })
      .addCase(fetchLocation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error?.message || "Failed to fetch location";
      })
      .addCase(updateLocation.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateLocation.fulfilled, (state, action) => {
        state.loading = false;
        state.location = action.meta.arg;
      })
      .addCase(updateLocation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error?.message || "Failed to update location";
      });
  },
});

export const locationReducer = locationSlice.reducer;
