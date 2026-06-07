import { useEffect, useRef } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useToast } from "./toastProvider";
import {
  setBookingSuccessStatus,
  setCreateEventSuccessStatus,
  setFiltersApplied,
  setReviewSuccessStatus,
  setTitleSuccessStatus,
  setUpdateImageSuccessStatus,
} from "../redux/slices/eventSlice";
import {
  setDeleteSessionSuccessStatus,
  setUpdateSessionSuccessStatus,
} from "../redux/slices/sessionSlice";
import {
  resetUpdateInterestsStatus,
  updateRoleSuccessStatus,
} from "../redux/slices/authSlice";
import { clearStatus } from "../redux/slices/userSlice";
import { clearOrgStatus } from "../redux/slices/organizerProfileSlice";

export default function ToastListener() {
  const dispatch = useDispatch();
  const toast = useToast();

  // Track previous loading states to detect transitions (true -> false)
  const prevLoadingStates = useRef({
    reviewLoader: false,
    bookingsLoading: false,
    updateTitleLoading: false,
    updateSessionLoading: false,
    deleteSessionLoading: false,
    updateImageLoading: false,
  });

  // Track if review success toast has been shown to prevent duplicate toasts
  const reviewSuccessShown = useRef(false);

  // Reset stale review states on mount to prevent toasts from previous navigations
  useEffect(() => {
    if (reviewSuccess) {
      dispatch(setReviewSuccessStatus(false));
    }
    if (reviewLoader) {
      // Reset loader if it's stuck in loading state
      dispatch({ type: "event/writeReview/fulfilled" });
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Collect all slice states here
  const {
    interestsLoading,
    interestsError,
    interestsSuccess,
    reviewLoader,
    reviewError,
    reviewSuccess,
    bookingsLoading,
    bookingsError,
    bookingsSuccess,
    searchEventsLoading,
    searchEventsError,
    searchEventsSuccess,
    filtersApplied,
    updateTitleLoading,
    updateTitleError,
    updateTitleSuccess,
    updateSessionLoading,
    updateSessionError,
    updateSessionSuccess,
    updateImageLoading,
    updateImageError,
    updateImageSuccess,
    uploadLoading,
    uploadError,
    deleteSessionLoading,
    deleteSessionError,
    deleteSessionSuccess,
    createEventLoading,
    createEventError,
    createEventSuccess,
    updateRoleLoading,
    updateRoleError,
    updateRoleSuccess,
    updateEmailLoading,
    updateEmailError,
    updateEmailSuccess,

    updatePasswordLoading,
    updatePasswordError,
    updatePasswordSuccess,

    updateProfileLoading,
    updateProfileError,
    updateProfileSuccess,
    updateOrgLoading,
    updateOrgError,
    updateOrgSuccess,
  } = useSelector((state) => ({
    updateOrgLoading: state.organizerProfile.updateOrgLoading,
    updateOrgError: state.organizerProfile.updateOrgError,
    updateOrgSuccess: state.organizerProfile.updateOrgSuccess,

    updateEmailLoading: state.user.updateEmailLoading,
    updateEmailError: state.user.updateEmailError,
    updateEmailSuccess: state.user.updateEmailSuccess,

    updatePasswordLoading: state.user.updatePasswordLoading,
    updatePasswordError: state.user.updatePasswordError,
    updatePasswordSuccess: state.user.updatePasswordSuccess,

    updateProfileLoading: state.user.updateProfileLoading,
    updateProfileError: state.user.updateProfileError,
    updateProfileSuccess: state.user.updateProfileSuccess,

    interestsLoading: state.auth.updateInterestsLoading,
    interestsError: state.auth.updateInterestsError,
    interestsSuccess: state.auth.updateInterestsSuccess,

    updateRoleLoading: state.auth.updateRoleLoading,
    updateRoleError: state.auth.updateRoleError,
    updateRoleSuccess: state.auth.updateRoleSuccess,

    searchEventsLoading: state.event.filteredLoading.searchEvents,
    searchEventsError: state.event.filteredError.searchEvents,
    searchEventsSuccess: state.event.filteredSuccess.searchEvents,

    filtersApplied: state.event.filtersApplied,

    reviewLoader: state.event.reviewLoader,
    reviewError: state.event.reviewError,
    reviewSuccess: state.event.reviewSuccess,

    updateTitleLoading: state.event.updateTitleLoading,
    updateTitleError: state.event.updateTitleError,
    updateTitleSuccess: state.event.updateTitleSuccess,

    bookingsLoading: state.event.bookingloader,
    bookingsError: state.event.bookingError,
    bookingsSuccess: state.event.bookingSuccess,

    uploadLoading: state.upload.uploadLoading,
    uploadError: state.upload.uploadError,

    updateImageLoading: state.event.updateImageLoading,
    updateImageError: state.event.updateImageError,
    updateImageSuccess: state.event.updateImageSuccess,

    createEventLoading: state.event.createEventLoading,
    createEventError: state.event.createEventError,
    createEventSuccess: state.event.createEventSuccess,

    updateSessionLoading: state.session.updateSessionLoading,
    updateSessionError: state.session.updateSessionError,
    updateSessionSuccess: state.session.updateSessionSuccess,

    deleteSessionLoading: state.session.deleteSessionLoading,
    deleteSessionError: state.session.deleteSessionError,
    deleteSessionSuccess: state.session.deleteSessionSuccess,
  }));

  // Create a list of toast logic rules
  const toastRules = [
    {
      loading: updateOrgLoading,
      error: updateOrgError,
      success: updateOrgSuccess,
      loadingMessage: "Updating Organizer...",
      successMessage: "Organizer updated!",
      onSuccess: () => dispatch(clearOrgStatus()),
    },
    {
      loading: updateEmailLoading,
      error: updateEmailError,
      success: updateEmailSuccess,
      loadingMessage: "Updating Email...",
      successMessage: "Email updated!",
      onSuccess: () => dispatch(clearStatus()),
    },
    {
      loading: updatePasswordLoading,
      error: updatePasswordError,
      success: updatePasswordSuccess,
      loadingMessage: "Updating password...",
      successMessage: "Password updated!",
      onSuccess: () => dispatch(clearStatus()),
    },
    {
      loading: updateProfileLoading,
      error: updateProfileError,
      success: updateProfileSuccess,
      loadingMessage: "Updating profile...",
      successMessage: "Profile updated!",
      onSuccess: () => dispatch(clearStatus()),
    },
    {
      loading: interestsLoading,
      error: interestsError,
      success: interestsSuccess,
      loadingMessage: "Updating interests...",
      successMessage: "Interests updated!",
      onSuccess: () => dispatch(resetUpdateInterestsStatus(false)),
    },
    {
      loading: updateRoleLoading,
      error: updateRoleError,
      success: updateRoleSuccess,
      loadingMessage: "Updating user role...",
      successMessage: "Organizer registration success!",
      onSuccess: () => dispatch(updateRoleSuccessStatus(false)),
    },
    {
      loading: filtersApplied && searchEventsLoading,
      error: searchEventsError,
      success: filtersApplied && searchEventsSuccess,
      loadingMessage: "Applying filters...",
      successMessage: "Filters applied!",
      onSuccess: () => dispatch(setFiltersApplied(false)),
    },
    {
      loading: uploadLoading,
      error: uploadError,
      loadingMessage: "Processing image...",
    },
    {
      loading: createEventLoading,
      error: createEventError,
      success: createEventSuccess,
      loadingMessage: "Creating event...",
      successMessage: "Event Created!",
      onSuccess: () => dispatch(setCreateEventSuccessStatus(false)),
    },
    {
      loading: updateImageLoading,
      error: updateImageError,
      success: updateImageSuccess,
      loadingMessage: "Uploading image...",
      successMessage: "Image uploaded!",
      onSuccess: () => dispatch(setUpdateImageSuccessStatus(false)),
    },
    {
      loading: false, // Don't show loading toast for reviews (submission is quick)
      error: reviewError,
      success:
        reviewSuccess &&
        !reviewLoader &&
        prevLoadingStates.current.reviewLoader === true &&
        !reviewSuccessShown.current,
      loadingMessage: "Submitting review...",
      successMessage: "Review submitted!",
      onSuccess: () => {
        reviewSuccessShown.current = true;
        dispatch(setReviewSuccessStatus(false));
      },
    },
    {
      loading: updateSessionLoading,
      error: updateSessionError,
      success: updateSessionSuccess,
      loadingMessage: "Updating session data...",
      successMessage: "Session updated!",
      onSuccess: () => dispatch(setUpdateSessionSuccessStatus(false)),
    },
    {
      loading: deleteSessionLoading,
      error: deleteSessionError,
      success: deleteSessionSuccess,
      loadingMessage: "Deleting session...",
      successMessage: "Session deleted!",
      onSuccess: () => dispatch(setDeleteSessionSuccessStatus(false)),
    },
    {
      loading: updateTitleLoading,
      error: updateTitleError,
      success: updateTitleSuccess,
      loadingMessage: "Updating title...",
      successMessage: "Title updated!",
      onSuccess: () => dispatch(setTitleSuccessStatus(false)),
    },
    {
      loading: bookingsLoading,
      error: bookingsError,
      success: bookingsSuccess,
      loadingMessage: "Processing booking...",
      successMessage: "Booking successful!",
      onSuccess: () => dispatch(setBookingSuccessStatus(false)),
    },
  ];

  // Update previous loading states after each render
  useEffect(() => {
    prevLoadingStates.current = {
      reviewLoader,
      bookingsLoading,
      updateTitleLoading,
      updateSessionLoading,
      deleteSessionLoading,
      updateImageLoading,
    };
  }, [
    reviewLoader,
    bookingsLoading,
    updateTitleLoading,
    updateSessionLoading,
    deleteSessionLoading,
    updateImageLoading,
  ]);

  // Reset review success shown flag when review success changes to false
  useEffect(() => {
    if (!reviewSuccess) {
      reviewSuccessShown.current = false;
    }
  }, [reviewSuccess]);

  // Loop & trigger toast automatically
  useEffect(() => {
    toastRules.forEach((rule) => {
      if (rule.loading) {
        toast.loading(rule.loadingMessage);
      } else if (rule.error) {
        toast.hide();
        toast.error(rule.error);
      } else if (rule.success) {
        toast.hide();
        toast.success(rule.successMessage);
        if (rule.onSuccess) rule.onSuccess();
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    reviewLoader,
    reviewError,
    reviewSuccess,
    bookingsLoading,
    bookingsError,
    bookingsSuccess,
    updateTitleLoading,
    updateTitleError,
    updateTitleSuccess,
    updateSessionLoading,
    updateSessionError,
    updateSessionSuccess,
    deleteSessionLoading,
    deleteSessionError,
    deleteSessionSuccess,
    updateImageLoading,
    updateImageError,
    updateImageSuccess,
    createEventLoading,
    createEventError,
    createEventSuccess,
    updateRoleLoading,
    updateRoleError,
    updateRoleSuccess,
    updateEmailLoading,
    updateEmailError,
    updateEmailSuccess,
    updatePasswordLoading,
    updatePasswordError,
    updatePasswordSuccess,
    updateProfileLoading,
    updateProfileError,
    updateProfileSuccess,
    updateOrgLoading,
    updateOrgError,
    updateOrgSuccess,
    interestsLoading,
    interestsError,
    interestsSuccess,
    searchEventsLoading,
    searchEventsError,
    searchEventsSuccess,
    filtersApplied,
    uploadLoading,
    uploadError,
  ]); // react to changes in any rule

  return null;
}
