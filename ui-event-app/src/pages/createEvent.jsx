import {
  Box,
  Button,
  Container,
  Step,
  StepLabel,
  Stepper,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import EventDetailsStep from "../components/eventCreationSteps/eventDetailsStep";
import ReviewStep from "../components/eventCreationSteps/reviewStep";
import TicketDetailsStep from "../components/eventCreationSteps/ticketDetailsStep";
import UploadImagesStep from "../components/eventCreationSteps/uploadImagesStep";
import { createEvent, getCategories } from "../redux/slices/eventSlice";

const steps = ["Edit", "Ticketing", "Banner", "Review"];

export default function CreateEvent() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { categories } = useSelector((state) => state.event);

  const [activeStep, setActiveStep] = useState(0);
  const [eventData, setEventData] = useState({
    title: "",
    category: "",
    recurrence: "single",
    startDate: "",
    endDate: "",
    startTime: "",
    endTime: "",
    location: "",
    ageLimit: "",
    venue: "",
    languages: [],
    selectedWeekdays: [],
    description: "",
    tickets: [],
    bannerImage: "",
    thumbnailImage: "",
  });
  const [errors, setErrors] = useState({});

  const handleNext = () => setActiveStep((prev) => prev + 1);
  const handleBack = () => setActiveStep((prev) => prev - 1);

  const handleNextClick = () => {
    const validationErrors = validateEvent(eventData);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length === 0) {
      handleNext();
    }
  };

  const handleInputChange = (field, value) => {
    setEventData((prev) => ({ ...prev, [field]: value }));
  };

  const handleTicketChange = (index, field, value) => {
    const newTickets = [...eventData.tickets];
    newTickets[index] = { ...newTickets[index], [field]: value };
    setEventData((prev) => ({ ...prev, tickets: newTickets }));
  };

  const addTicket = () => {
    setEventData((prev) => ({
      ...prev,
      tickets: [...prev.tickets, { name: "", price: "" }],
    }));
  };

  const validateEvent = (eventData) => {
    const errors = {};
    const today = new Date().toISOString().split("T")[0];

    const isTimeGreater = (start, end) => start && end && end > start;
    if (activeStep == 0) {
      // --- Title ---
      if (!eventData.title.trim()) errors.title = "Event title is required";
      else if (eventData.title.length < 3)
        errors.title = "Title must be at least 3 characters";

      // --- Category ---
      if (!eventData.category) errors.category = "Category is required";

      // --- Recurrence ---
      if (!eventData.recurrence) errors.recurrence = "Event type is required";

      // SINGLE-DAY VALIDATION
      if (eventData.recurrence === "single") {
        if (!eventData.startDate) errors.startDate = "Event date is required";
        else if (eventData.startDate < today)
          errors.startDate = "Event date cannot be in the past";

        if (!eventData.startTime) errors.startTime = "Start time is required";
        if (!eventData.endTime) errors.endTime = "End time is required";

        if (
          eventData.startTime &&
          eventData.endTime &&
          !isTimeGreater(eventData.startTime, eventData.endTime)
        ) {
          errors.endTime = "End time must be later than start time";
        }
      }

      // MULTI-DAY VALIDATION
      if (eventData.recurrence === "multi-day") {
        if (!eventData.startDate) errors.startDate = "Start date is required";
        else if (eventData.startDate < today)
          errors.startDate = "Start date cannot be in the past";

        if (!eventData.endDate) errors.endDate = "End date is required";
        else if (eventData.endDate < eventData.startDate)
          errors.endDate = "End date cannot be before start date";

        if (!eventData.startTime)
          errors.startTime = "Daily start time is required";
        if (!eventData.endTime) errors.endTime = "Daily end time is required";

        if (
          eventData.startTime &&
          eventData.endTime &&
          !isTimeGreater(eventData.startTime, eventData.endTime)
        ) {
          errors.endTime = "End time must be later than start time";
        }
      }

      // WEEKLY VALIDATION
      if (eventData.recurrence === "weekly") {
        if (!eventData.startDate) errors.startDate = "Start date is required";
        else if (eventData.startDate < today)
          errors.startDate = "Start date cannot be in the past";

        if (!eventData.endDate) errors.endDate = "End date is required";
        else if (eventData.endDate < eventData.startDate)
          errors.endDate = "End date cannot be before start date";

        if (!eventData.selectedWeekdays.length)
          errors.selectedWeekdays = "Select at least one weekday";

        if (!eventData.startTime) errors.startTime = "Start time is required";
        if (!eventData.endTime) errors.endTime = "End time is required";

        if (
          eventData.startTime &&
          eventData.endTime &&
          !isTimeGreater(eventData.startTime, eventData.endTime)
        ) {
          errors.endTime = "End time must be later than start time";
        }
      }

      // --- Age ---
      if (!eventData.ageLimit) errors.ageLimit = "Age limit is required";

      // --- Languages ---
      if (!eventData.languages?.length)
        errors.languages = "Select at least one language";

      // --- Description ---
      if (!eventData.description.trim())
        errors.description = "Description is required";
      else if (eventData.description.length < 20)
        errors.description = "Description must be at least 20 characters";
    }
    if (activeStep == 1) {
      // --- Ticket Prices ---
      if (!eventData.tickets || eventData.tickets.length === 0) {
        errors.tickets = "At least one ticket section must exist";
      } else {
        eventData.tickets.forEach((t, idx) => {
          if (!t.price || Number(t.price) <= 0) {
            errors[`ticket_price_${idx}`] =
              `${t.section} price must be greater than 0`;
          }
        });
      }
    }

    if (activeStep === 2) {
      // Banner
      if (!eventData.bannerImage) {
        errors.bannerImage = "Feature image is required";
      }

      // Thumb
      if (!eventData.thumbnailImage) {
        errors.thumbnailImage = "Event card image is required";
      }
    }

    return errors;
  };

  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <EventDetailsStep
            eventData={eventData}
            categories={categories}
            handleInputChange={handleInputChange}
            errors={errors}
          />
        );
      case 1:
        return (
          <TicketDetailsStep
            eventData={eventData}
            setEventData={setEventData}
            handleInputChange={handleInputChange}
            handleTicketChange={handleTicketChange}
            addTicket={addTicket}
            errors={errors}
          />
        );
      case 2:
        return (
          <UploadImagesStep
            eventData={eventData}
            handleInputChange={handleInputChange}
            errors={errors}
          />
        );
      case 3:
        return <ReviewStep eventData={eventData} />;
      default:
        return null;
    }
  };

  useEffect(() => {
    dispatch(getCategories());
  }, []);

  const handleSubmit = async () => {
    // Create a payload with all eventData and map venue -> venueId!
    const payload = {
      ...eventData,
      venueId: eventData.venue,
      startDate: new Date(eventData.startDate).toISOString(), // always UTC
      endDate: eventData.endDate
        ? new Date(eventData.endDate).toISOString()
        : eventData.startDate,
    };
    try {
      await dispatch(createEvent(payload)).unwrap();
      navigate("/dashboard");
    } catch (err) {
      console.error("Error creating event:", err);
    }
  };

  return (
    <Container
      disableGutters
      maxWidth="90%"
      sx={{ pt: 4, pb: 2, px: 15 }}
      bgcolor="grey.50"
      p={3}
      borderRadius={3}
      boxShadow={2}
    >
      {/* Header */}
      <Box
        sx={{ display: "flex", alignItems: "center", mb: 2 }}
        bgcolor="grey.50"
        p={3}
        borderRadius={3}
        boxShadow={2}
      >
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 600 }}>
            Create a New Event
          </Typography>
        </Box>
      </Box>

      {/* Progress Stepper */}
      <Box bgcolor="grey.50" p={3} borderRadius={3} boxShadow={2}>
        <Box sx={{ mb: 6 }}>
          <Stepper activeStep={activeStep} alternativeLabel>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
        </Box>

        {/* Step Content */}
        <Box sx={{ mb: 6 }}>{renderStepContent()}</Box>
        {/* Navigation Buttons */}
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            maxWidth: 600,
            mx: "auto",
          }}
        >
          <Button
            onClick={handleBack}
            disabled={activeStep === 0}
            variant="outlined"
            sx={{ px: 4 }}
          >
            {activeStep === 1 ? "Go back to Edit Event" : "Go back"}
          </Button>

          {activeStep === steps.length - 1 ? (
            <Button
              onClick={handleSubmit}
              variant="contained"
              sx={{
                px: 4,
                backgroundColor: "#2D3748",
                "&:hover": { backgroundColor: "#1A202C" },
              }}
            >
              Create Event
            </Button>
          ) : (
            <Button
              onClick={handleNextClick}
              variant="contained"
              sx={{
                px: 4,
                backgroundColor: "#2D3748",
                "&:hover": { backgroundColor: "#1A202C" },
              }}
            >
              Save & Continue
            </Button>
          )}
        </Box>
      </Box>
    </Container>
  );
}
