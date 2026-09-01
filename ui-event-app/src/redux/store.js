import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";
import storage from "redux-persist/lib/storage";

import { authReducer } from "./slices/authSlice";
import { userReducer } from "./slices/userSlice";
import { eventReducer } from "./slices/eventSlice";
import { organizerProfileReducer } from "./slices/organizerProfileSlice";
import { sessionReducer } from "./slices/sessionSlice";
import { locationReducer } from "./slices/locationSlice";
import { uploadReducer } from "./slices/uploadSlice";

import { setStore } from "../utils/api";

// ⭐ Persist ONLY tiny fields inside each slice
const eventPersistConfig = {
  key: "event",
  storage,
  whitelist: ["selectedEvent"],
};

const organizerPersistConfig = {
  key: "organizerProfile",
  storage,
  whitelist: ["organizer"],
};

// Combine reducers with slice-level persistence
const rootReducer = combineReducers({
  auth: authReducer,
  user: userReducer,

  event: persistReducer(eventPersistConfig, eventReducer),
  organizerProfile: persistReducer(
    organizerPersistConfig,
    organizerProfileReducer,
  ),

  session: sessionReducer,
  location: locationReducer,
  upload: uploadReducer,
});

// Create store
export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ["persist/PERSIST", "persist/REHYDRATE"],
        ignoredPaths: ["register", "rehydrate"],
      },
    }),
});

export const persistor = persistStore(store);

setStore(store);
