// uploadSlice.js
import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../../utils/api"; // axios instance

export const uploadImage = createAsyncThunk(
  "upload/uploadImage",
  async ({ formData }, { rejectWithValue }) => {
    try {
      const res = await api.post("upload/uploadImage", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        _skipToast: true,
      });

      return {
        url: res.data.data.url,
      };
    } catch (err) {
      return rejectWithValue(
        err.response?.data || "Image upload failed. Try again."
      );
    }
  }
);

const uploadSlice = createSlice({
  name: "upload",
  initialState: {
    loading: false,
    uploadedBanner: "",
    uploadedProfile: "",
    uploadLoading: false,
    uploadError: null,
    uploadSucess: false,
    error: null,
  },

  reducers: {
    resetUploadState: (state) => {
      state.loading = false;
      state.error = null;
      state.uploadLoading = false;
      state.uploadError = null;
      state.uploadSucess = false;
    },
  },

  extraReducers: (builder) => {
    builder
      .addCase(uploadImage.pending, (state) => {
        state.uploadLoading = true;
        state.uploadError = null;
      })
      .addCase(uploadImage.fulfilled, (state, action) => {
        state.uploadLoading = false;
        state.uploadSucess = true;

        if (action.payload.type === "banner") {
          state.uploadedBanner = action.payload.url;
        }
        if (action.payload.type === "profile") {
          state.uploadedProfile = action.payload.url;
        }
      })
      .addCase(uploadImage.rejected, (state, action) => {
        state.uploadLoading = false;
        state.uploadError = action.payload;
      });
  },
});

export const { resetUploadState } = uploadSlice.actions;
export const uploadReducer = uploadSlice.reducer;
