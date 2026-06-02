import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import api from "../../utils/api";

export const fetchSessionById = createAsyncThunk(
  "session/fetchSessionById",
  async (sessionId) => {
    let response = await api.get(`sessions/${sessionId}`);
    return response.data;
  },
);

export const fetchSessionsByEventId = createAsyncThunk(
  "session/fetchSessionsByEventId",
  async (eventId) => {
    let response = await api.get(`sessions/event/${eventId}`);
    return response.data;
  },
);

export const updateSession = createAsyncThunk(
  "session/updateSession",
  async ({ sessionId, payload }, { rejectWithValue }) => {
    try {
      const res = await api.put(
        `organizer/${sessionId}/update-session`,
        payload,
      );
      return res.data.session; // full response from backend
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to update session");
    }
  },
);

export const deleteSession = createAsyncThunk(
  "session/deleteSession",
  async (sessionId, { rejectWithValue }) => {
    try {
      const res = await api.delete(`sessions/${sessionId}`);
      return res.data; // full response from backend
    } catch (err) {
      return rejectWithValue(err.response?.data || "Failed to delete session");
    }
  },
);

export const sessionSlice = createSlice({
  name: "session",
  initialState: {
    sessions: [],
    sessionsLoading: false,
    sessionsError: null,
    sessionSuccess: false,
    sessionLoading: false,
    sessionError: null,
    sessionsSuccess: false,
    selectedSession: null,
    updateSessionLoading: false,
    updateSessionError: null,
    updateSessionSuccess: false,
    updatedSession: null,
    deleteSessionLoading: false,
    deleteSessionError: null,
    deleteSessionSuccess: false,
    loading: false,
    error: null,
  },
  reducers: {
    setUpdateSessionSuccessStatus: (state, action) => {
      state.updateSessionSuccess = action.payload;
    },
    setDeleteSessionSuccessStatus: (state, action) => {
      state.deleteSessionSuccess = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchSessionById.pending, (state) => {
        state.sessionLoading = true;
        state.sessionError = null;
      })
      .addCase(fetchSessionById.fulfilled, (state, action) => {
        state.sessionLoading = false;
        state.selectedSession = action.payload.data;
        state.sessionSuccess = true;
      })
      .addCase(fetchSessionById.rejected, (state, action) => {
        state.sessionLoading = false;
        state.sessionError = action.payload || "Something went wrong";
        state.selectedSession = null;
      });
    builder
      .addCase(updateSession.pending, (state) => {
        state.updateSessionLoading = true;
        state.updateSessionError = null;
        state.updateSessionSuccess = false;
      })
      .addCase(updateSession.fulfilled, (state, action) => {
        state.updateSessionLoading = false;
        state.updateSessionSuccess = true;
        state.updatedSession = action.payload;
      })
      .addCase(updateSession.rejected, (state, action) => {
        state.updateSessionLoading = false;
        state.updateSessionError = action.payload;
        state.updateSessionSuccess = false;
      })

      .addCase(deleteSession.pending, (state) => {
        state.deleteSessionLoading = true;
        state.deleteSessionError = null;
        state.deleteSessionSuccess = false;
      })
      .addCase(deleteSession.fulfilled, (state, action) => {
        state.deleteSessionLoading = false;
        state.deleteSessionSuccess = true;
      })
      .addCase(deleteSession.rejected, (state, action) => {
        state.deleteSessionLoading = false;
        state.deleteSessionError = action.payload;
        state.deleteSessionSuccess = false;
      })

      .addCase(fetchSessionsByEventId.pending, (state) => {
        state.sessionsLoading = true;
        state.sessionsError = null;
      })
      .addCase(fetchSessionsByEventId.fulfilled, (state, action) => {
        state.sessionsLoading = false;
        state.sessions = action.payload.data;
        state.sessionsSuccess = true;
      })
      .addCase(fetchSessionsByEventId.rejected, (state, action) => {
        state.sessionsLoading = false;
        state.sessionsError = action.payload || "Something went wrong";
        state.sessions = [];
      });
  },
});

export const { setUpdateSessionSuccessStatus, setDeleteSessionSuccessStatus } =
  sessionSlice.actions;
export const sessionReducer = sessionSlice.reducer;
