import {
  Box,
  Checkbox,
  FormControl,
  FormControlLabel,
  FormGroup,
  Grid,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Radio,
  RadioGroup,
  Select,
  TextField,
  Typography,
} from "@mui/material";

import { useEffect, useState } from "react";

const recurrenceOptions = [
  { value: "single", label: "Single-day" },
  { value: "multi-day", label: "Multi-day Continuous" },
  { value: "weekly", label: "Weekly Recurring" },
];

const weekdaysList = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

const inputStyle = {
  sx: {
    borderRadius: 3,
    backgroundColor: "#f9fafb",
    "& .MuiOutlinedInput-notchedOutline": { borderColor: "#d1d5db" },
    "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: "#9ca3af" },
    "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
      borderColor: "#6366f1",
      boxShadow: "0 0 0 4px rgba(99,102,241,0.2)",
    },
  },
};

const selectStyle = {
  borderRadius: 3,
  backgroundColor: "#f9fafb",
  "& .MuiOutlinedInput-notchedOutline": { borderColor: "#d1d5db" },
  "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: "#9ca3af" },
  "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
    borderColor: "#6366f1",
    boxShadow: "0 0 0 4px rgba(99,102,241,0.2)",
  },
};

const StyledLabel = ({ children }) => (
  <Typography sx={{ fontSize: "0.9rem", fontWeight: 600, mb: 1 }}>
    {children}
  </Typography>
);

export default function EventDetailsStep({
  eventData,
  categories = [],
  handleInputChange,
  errors,
}) {
  const todayDate = new Date();
  const minDate = new Date(todayDate);
  minDate.setDate(todayDate.getDate() + 4);

  const minAllowedDate = minDate.toISOString().split("T")[0];

  // ✅ Use only parent handler
  const handleChange = (field, value) => {
    handleInputChange(field, value);
  };

  const handleWeekdayToggle = (day) => {
    const exists = eventData.selectedWeekdays.includes(day);
    const updatedWeekdays = exists
      ? eventData.selectedWeekdays.filter((d) => d !== day)
      : [...eventData.selectedWeekdays, day];

    handleInputChange("selectedWeekdays", updatedWeekdays);
  };

  return (
    <Box
      sx={{
        maxWidth: 650,
        mx: "auto",
        p: 3,
        backgroundColor: "rgba(255,255,255,0.85)",
        backdropFilter: "blur(10px)",
        borderRadius: 4,
        boxShadow: "0 8px 30px rgba(0,0,0,0.05)",
        border: "1px solid #e5e7eb",
      }}
    >
      {/* Title */}
      <Typography variant="h5" sx={{ mb: 4, fontWeight: 700 }}>
        Event Details
      </Typography>

      {/* Event Title + Category */}
      <Box sx={{ mb: 4 }}>
        <Typography sx={{ fontSize: "0.9rem", fontWeight: 600, mb: 1 }}>
          Event Title
        </Typography>
        <TextField
          fullWidth
          placeholder="Enter the name of your event"
          value={eventData.title}
          onChange={(e) => handleChange("title", e.target.value)}
          required
          error={!!errors.title}
          helperText={errors.title}
          InputProps={inputStyle}
          sx={{ mb: 3 }}
        />

        <Typography sx={{ fontSize: "0.9rem", fontWeight: 600, mb: 1 }}>
          Event Category
        </Typography>
        <FormControl fullWidth>
          <Select
            value={eventData.category}
            onChange={(e) => handleChange("category", e.target.value)}
            error={!!errors.category}
            sx={selectStyle}
          >
            {categories.map((c) => (
              <MenuItem key={c} value={c}>
                {c}
              </MenuItem>
            ))}
          </Select>

          {errors.category && (
            <Typography color="error" sx={{ fontSize: "0.8rem", mt: 0.5 }}>
              {errors.category}
            </Typography>
          )}
        </FormControl>
      </Box>

      {/* Event Type */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Event Type
      </Typography>

      <RadioGroup
        row
        value={eventData.recurrence}
        onChange={(e) => handleChange("recurrence", e.target.value)}
        sx={{
          mb: 4,
          "& .MuiFormControlLabel-label": {
            fontSize: "0.9rem",
            fontWeight: 600,
          },
        }}
      >
        {recurrenceOptions.map((opt) => (
          <FormControlLabel
            key={opt.value}
            value={opt.value}
            control={<Radio />}
            label={opt.label}
          />
        ))}
      </RadioGroup>

      {/* Single Day */}
      {eventData.recurrence === "single" && (
        <Grid container spacing={2} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6}>
            <StyledLabel>Event Date</StyledLabel>
            <TextField
              fullWidth
              type="date"
              value={eventData.startDate}
              onChange={(e) => handleChange("startDate", e.target.value)}
              error={!!errors.startDate}
              helperText={errors.startDate}
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: minAllowedDate }}
              sx={inputStyle.sx}
            />
          </Grid>

          <Grid item xs={6} sm={3}>
            <StyledLabel>Start Time</StyledLabel>
            <TextField
              fullWidth
              type="time"
              value={eventData.startTime}
              onChange={(e) => handleChange("startTime", e.target.value)}
              error={!!errors.startTime}
              helperText={errors.startTime}
              sx={inputStyle.sx}
            />
          </Grid>

          <Grid item xs={6} sm={3}>
            <StyledLabel>End Time</StyledLabel>
            <TextField
              fullWidth
              type="time"
              value={eventData.endTime}
              onChange={(e) => handleChange("endTime", e.target.value)}
              error={!!errors.endTime}
              helperText={errors.endTime}
              sx={inputStyle.sx}
            />
          </Grid>
        </Grid>
      )}

      {/* Multi-Day */}
      {eventData.recurrence === "multi-day" && (
        <Grid container spacing={2} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6}>
            <StyledLabel>Start Date</StyledLabel>
            <TextField
              fullWidth
              type="date"
              value={eventData.startDate}
              onChange={(e) => handleChange("startDate", e.target.value)}
              error={!!errors.startDate}
              helperText={errors.startDate}
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: minAllowedDate }}
              sx={inputStyle.sx}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <StyledLabel>End Date</StyledLabel>
            <TextField
              fullWidth
              type="date"
              value={eventData.endDate}
              onChange={(e) => handleChange("endDate", e.target.value)}
              error={!!errors.endDate}
              helperText={errors.endDate}
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: eventData.startDate || minAllowedDate }}
              sx={inputStyle.sx}
            />
          </Grid>

          <Grid item xs={6} sm={3}>
            <StyledLabel>Daily Start</StyledLabel>
            <TextField
              fullWidth
              type="time"
              value={eventData.startTime}
              onChange={(e) => handleChange("startTime", e.target.value)}
              error={!!errors.startTime}
              helperText={errors.startTime}
              sx={inputStyle.sx}
            />
          </Grid>

          <Grid item xs={6} sm={3}>
            <StyledLabel>Daily End</StyledLabel>
            <TextField
              fullWidth
              type="time"
              value={eventData.endTime}
              onChange={(e) => handleChange("endTime", e.target.value)}
              error={!!errors.endTime}
              helperText={errors.endTime}
              sx={inputStyle.sx}
            />
          </Grid>
        </Grid>
      )}

      {/* Weekly */}
      {eventData.recurrence === "weekly" && (
        <Box sx={{ mb: 4 }}>
          <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid item xs={12} sm={6}>
              <StyledLabel>Start Date</StyledLabel>
              <TextField
                fullWidth
                type="date"
                value={eventData.startDate}
                onChange={(e) => handleChange("startDate", e.target.value)}
                error={!!errors.startDate}
                helperText={errors.startDate}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: minAllowedDate }}
                sx={inputStyle.sx}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <StyledLabel>End Date</StyledLabel>
              <TextField
                fullWidth
                type="date"
                value={eventData.endDate}
                onChange={(e) => handleChange("endDate", e.target.value)}
                error={!!errors.endDate}
                helperText={errors.endDate}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: eventData.startDate || minAllowedDate }}
                sx={inputStyle.sx}
              />
            </Grid>
          </Grid>

          <Typography sx={{ mb: 1, fontWeight: 600 }}>
            Select Weekdays:
          </Typography>

          <FormGroup row sx={{ mb: 2 }}>
            {weekdaysList.map((day) => (
              <FormControlLabel
                key={day}
                control={
                  <Checkbox
                    checked={eventData.selectedWeekdays?.includes(day)}
                    onChange={() => handleWeekdayToggle(day)}
                  />
                }
                label={day}
              />
            ))}
          </FormGroup>

          {errors.selectedWeekdays && (
            <Typography color="error" sx={{ fontSize: ".8rem", mt: 0.5 }}>
              {errors.selectedWeekdays}
            </Typography>
          )}

          <Grid container spacing={2}>
            <Grid item xs={6}>
              <StyledLabel>Start Time</StyledLabel>
              <TextField
                fullWidth
                type="time"
                value={eventData.startTime}
                onChange={(e) => handleChange("startTime", e.target.value)}
                error={!!errors.startTime}
                helperText={errors.startTime}
                sx={inputStyle.sx}
              />
            </Grid>

            <Grid item xs={6}>
              <StyledLabel>End Time</StyledLabel>
              <TextField
                fullWidth
                type="time"
                value={eventData.endTime}
                onChange={(e) => handleChange("endTime", e.target.value)}
                error={!!errors.endTime}
                helperText={errors.endTime}
                sx={inputStyle.sx}
              />
            </Grid>
          </Grid>
        </Box>
      )}

      {/* Age Limit */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Age Limit
      </Typography>

      <FormControl fullWidth sx={{ mb: 4 }}>
        <Select
          value={eventData.ageLimit || ""}
          onChange={(e) => handleChange("ageLimit", e.target.value)}
          input={<OutlinedInput />}
          error={!!errors.ageLimit}
          sx={selectStyle}
        >
          <MenuItem value="All Ages">All Ages</MenuItem>
          <MenuItem value="5">5yrs +</MenuItem>
          <MenuItem value="12">12yrs +</MenuItem>
          <MenuItem value="16">16yrs +</MenuItem>
          <MenuItem value="18">18yrs +</MenuItem>
          <MenuItem value="21">21yrs +</MenuItem>
        </Select>
        {errors.ageLimit && (
          <Typography color="error" sx={{ fontSize: "0.8rem", mt: 0.5 }}>
            {errors.ageLimit}
          </Typography>
        )}
      </FormControl>

      {/* Languages */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Languages
      </Typography>

      <FormControl fullWidth sx={{ mb: 4 }}>
        <Select
          multiple
          value={eventData.languages || []}
          onChange={(e) => handleChange("languages", e.target.value)}
          error={!!errors.languages}
          input={<OutlinedInput />}
          sx={selectStyle}
          renderValue={(selected) => selected.join(", ")}
        >
          {[
            "Telugu",
            "Hindi",
            "Tamil",
            "English",
            "Marathi",
            "Urdu",
            "Malayalam",
          ].map((lang) => (
            <MenuItem key={lang} value={lang.toLowerCase()}>
              {lang}
            </MenuItem>
          ))}
        </Select>
        {errors.languages && (
          <Typography color="error" sx={{ fontSize: ".8rem", mt: 0.5 }}>
            {errors.languages}
          </Typography>
        )}
      </FormControl>

      {/* Description */}
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
        Description
      </Typography>

      <TextField
        fullWidth
        multiline
        rows={6}
        placeholder="Describe your event — what makes it unique?"
        value={eventData.description}
        onChange={(e) => handleChange("description", e.target.value)}
        error={!!errors.description}
        helperText={errors.description}
        InputProps={inputStyle}
        sx={{ mb: 4 }}
      />
    </Box>
  );
}
