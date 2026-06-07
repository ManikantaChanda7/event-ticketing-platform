import LockResetIcon from "@mui/icons-material/LockReset";
import PhotoCameraIcon from "@mui/icons-material/PhotoCamera";
import SaveIcon from "@mui/icons-material/Save";
import UploadIcon from "@mui/icons-material/Upload";
import Visibility from "@mui/icons-material/Visibility";
import VisibilityOff from "@mui/icons-material/VisibilityOff";
import {
  Avatar,
  Box,
  Button,
  Container,
  Grid,
  IconButton,
  InputAdornment,
  MenuItem,
  Paper,
  Tab,
  Tabs,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  getOrganizerProfileByUserId,
  updateOrganizerProfile,
} from "../redux/slices/organizerProfileSlice";
import { uploadImage } from "../redux/slices/uploadSlice";
import { updatePassword } from "../redux/slices/userSlice";

export default function ProfileSettings() {
  const dispatch = useDispatch();

  const id = localStorage.getItem("userId");

  const { user } = useSelector((state) => state.user);
  const isOAuthUser = user?.isOAuth; // Detect OAuth login

  const [tab, setTab] = useState(0);
  const [showPassword, setShowPassword] = useState({
    current: false,
    new: false,
    confirm: false,
  });
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [organizer, setOrganizer] = useState({});

  const [formData, setFormData] = useState({
    orgName: "",
    orgDescription: "",
    orgSpecialities: [],
    organizerBannerImage: "",
    organizerProfileImage: "",
    phone: "",
    orgEmail: "",
  });

  const [profileErrors, setProfileErrors] = useState({
    orgName: "",
    orgEmail: "",
    phone: "",
    orgDescription: "",
  });

  const [passwordErrors, setPasswordErrors] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const isProfileValid =
    !profileErrors.orgName &&
    !profileErrors.orgEmail &&
    !profileErrors.phone &&
    !profileErrors.orgDescription &&
    formData.orgName &&
    formData.orgEmail &&
    formData.phone &&
    formData.orgDescription;

  const isPasswordFormValid =
    !passwordErrors.newPassword &&
    !passwordErrors.confirmPassword &&
    passwordData.newPassword &&
    passwordData.confirmPassword &&
    (isOAuthUser ||
      (!passwordErrors.currentPassword && passwordData.currentPassword));

  const validateOrgName = (value) => {
    if (!value.trim()) return "Organization name is required";
    if (value.trim().length < 3) return "Must be at least 3 characters";
    return "";
  };

  const validateEmail = (value) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!value.trim()) return "Email is required";
    if (!emailRegex.test(value)) return "Enter a valid email";
    return "";
  };

  const validatePhone = (value) => {
    const phoneRegex = /^(\+91)?\s?\d{10}$/;
    if (!value.trim()) return "Phone number is required";
    if (!phoneRegex.test(value)) return "Enter a valid 10-digit number";
    return "";
  };

  const validateDescription = (value) => {
    if (!value.trim()) return "Description is required";
    if (value.trim().length < 20) return "Must be at least 20 characters";
    return "";
  };

  const validateCurrentPassword = (value) => {
    if (!value.trim()) return "Current password is required";
    return "";
  };

  const validateNewPassword = (value) => {
    // Must have: 8+ chars, upper, lower, number, special char
    const strongPass =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

    if (!value.trim()) return "New password is required";
    if (!strongPass.test(value))
      return "Password must be 8+ chars and include upper, lower, number, and special character.";

    return "";
  };

  const validateConfirmPassword = (value, newPassword) => {
    if (!value.trim()) return "Please confirm your password";
    if (value !== newPassword) return "Passwords do not match";
    return "";
  };

  const handleProfileChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));

    let error = "";
    if (field === "orgName") error = validateOrgName(value);
    if (field === "orgEmail") error = validateEmail(value);
    if (field === "phone") error = validatePhone(value);
    if (field === "orgDescription") error = validateDescription(value);

    setProfileErrors((prev) => ({
      ...prev,
      [field]: error,
    }));
  };

  const handleSaveChanges = async () => {
    dispatch(updateOrganizerProfile(formData)).unwrap();
  };

  const handlePasswordChange = (field) => (e) => {
    const value = e.target.value;

    setPasswordData((prev) => ({
      ...prev,
      [field]: value,
    }));

    let error = "";

    if (!isOAuthUser) {
      if (field === "currentPassword") error = validateCurrentPassword(value);
    }

    if (field === "newPassword") error = validateNewPassword(value);

    if (field === "confirmPassword")
      error = validateConfirmPassword(value, passwordData.newPassword);

    setPasswordErrors((prev) => ({
      ...prev,
      [field]: error,
    }));
  };

  const handlePasswordSubmit = () => {
    const payload = isOAuthUser
      ? {
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.confirmPassword,
        }
      : passwordData;

    dispatch(updatePassword(payload))
      .unwrap()
      .then(() => {
        setPasswordData({
          currentPassword: "",
          newPassword: "",
          confirmPassword: "",
        });
      });
  };

  useEffect(() => {
    dispatch(getOrganizerProfileByUserId(id))
      .unwrap()
      .then((data) => {
        setOrganizer(data);
        setFormData({
          orgName: data.orgName || "",
          orgDescription: data.orgDescription || "",
          orgSpecialities: data.orgSpecialities || [],
          organizerBannerImage: data.organizerBannerImage || "",
          organizerProfileImage: data.organizerProfileImage || "",
          phone: data.phone || "",
          orgEmail: data.orgEmail || "",
        });
      });
  }, [id]);

  const [error, setError] = useState("");
  const [fileNames, setFileNames] = useState({
    banner: "",
    profile: "",
  });

  const resizeImage = (file, targetWidth, targetHeight, quality = 0.9) =>
    new Promise((resolve) => {
      if (!file) {
        setError("No file selected.");
        resolve(null);
        return;
      }

      const img = new Image();
      const url = URL.createObjectURL(file);
      img.src = url;

      img.onload = () => {
        URL.revokeObjectURL(url);

        // ✅ Check if image is smaller than required
        if (img.width < targetWidth || img.height < targetHeight) {
          setError(
            `Image must be at least ${targetWidth} x ${targetHeight} pixels. Selected image is ${img.width} x ${img.height}.`
          );
          resolve(null);
          return;
        }

        const canvas = document.createElement("canvas");
        canvas.width = targetWidth;
        canvas.height = targetHeight;

        const ctx = canvas.getContext("2d");

        const srcAspect = img.width / img.height;
        const targetAspect = targetWidth / targetHeight;

        let sx, sy, sWidth, sHeight;

        if (srcAspect > targetAspect) {
          // Wider → crop sides
          sHeight = img.height;
          sWidth = sHeight * targetAspect;
          sx = (img.width - sWidth) / 2;
          sy = 0;
        } else {
          // Taller → crop top/bottom
          sWidth = img.width;
          sHeight = sWidth / targetAspect;
          sx = 0;
          sy = (img.height - sHeight) / 2;
        }

        ctx.drawImage(
          img,
          sx,
          sy,
          sWidth,
          sHeight,
          0,
          0,
          targetWidth,
          targetHeight
        );

        canvas.toBlob((blob) => resolve(blob), "image/jpeg", quality);
      };

      img.onerror = () => {
        URL.revokeObjectURL(url);
        setError("Invalid image file.");
        resolve(null);
      };
    });

  const handleFileUpload = async (file, type, targetWidth, targetHeight) => {
    setError("");
    if (!file) return;

    try {
      const resizedBlob = await resizeImage(file, targetWidth, targetHeight);
      if (resizedBlob) {
        const resizedFile = new File([resizedBlob], file.name, {
          type: "image/jpeg",
        });

        const formData = new FormData();
        formData.append("image", resizedFile);

        const res = await dispatch(uploadImage({ formData })).unwrap();

        // update your local form state
        if (type === "banner") {
          setFormData((prev) => ({
            ...prev,
            organizerBannerImage: res.url,
          }));
        }

        if (type === "profile") {
          setFormData((prev) => ({
            ...prev,
            organizerProfileImage: res.url,
          }));
        }

        setFileNames((prev) => ({ ...prev, [type]: file.name }));
      }
    } catch (err) {
      console.error("Upload failed:", err);
      // Set error message properly
      if (err?.message) {
        setError(err.message);
      } else if (typeof err === "string") {
        setError(err);
      } else {
        setError("Image upload failed. Try again.");
      }
    }
  };

  return (
    <Box sx={{ bgcolor: "#f9fafb", minHeight: "100vh", p: 2 }}>
      <Container maxWidth="md">
        <Paper sx={{ borderRadius: 3, overflow: "hidden", boxShadow: 3 }}>
          {/* Banner */}
          <Box
            sx={{
              position: "relative",
              height: { xs: 130, md: 180 },
              width: "100%",
              background: "linear-gradient(to right, #631bff, #3c08aa)",
            }}
          >
            <Typography
              variant="h4"
              sx={{
                position: "absolute",
                bottom: 24,
                left: 24,
                color: "white",
                fontWeight: "bold",
              }}
            >
              Profile Settings
            </Typography>
          </Box>

          {/* Tabs */}
          <Box sx={{ p: 3 }}>
            <Tabs
              value={tab}
              onChange={(e, newVal) => setTab(newVal)}
              sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}
            >
              <Tab
                label="Profile"
                sx={{ textTransform: "none", fontWeight: 500 }}
              />
              <Tab
                label="Password"
                sx={{ textTransform: "none", fontWeight: 500 }}
              />
            </Tabs>

            {/* Profile Tab */}
            {tab === 0 && (
              <Box
                component="form"
                sx={{ display: "flex", flexDirection: "column", gap: 4 }}
              >
                <Box sx={{ display: "flex", gap: 3 }}>
                  {/* Profile Image */}
                  <Box sx={{ flex: "0 0 300px" }}>
                    <Paper
                      variant="outlined"
                      sx={{
                        p: 3,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        gap: 2,
                        bgcolor: "#f9fafb",
                        borderStyle: "dashed",
                        height: "100%",
                      }}
                    >
                      <Box sx={{ position: "relative" }}>
                        <Avatar
                          src={formData?.organizerProfileImage}
                          alt="Profile"
                          sx={{ width: 128, height: 128, mb: 2 }}
                        />
                        <IconButton
                          sx={{
                            position: "absolute",
                            inset: 0,
                            color: "white",
                            bgcolor: "rgba(0,0,0,0.4)",
                            opacity: 0,
                            transition: "0.3s",
                            ".MuiAvatar-root:hover &": { opacity: 1 },
                          }}
                        >
                          <PhotoCameraIcon />
                        </IconButton>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        Profile Image
                      </Typography>
                      <Button
                        component="label"
                        startIcon={<UploadIcon />}
                        size="small"
                        sx={{ color: "#631bff" }}
                      >
                        Upload
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={(e) =>
                            handleFileUpload(
                              e.target.files[0],
                              "profile",
                              200,
                              200
                            )
                          }
                        />
                      </Button>
                    </Paper>
                  </Box>

                  {/* Banner Image */}
                  <Box sx={{ flex: 1 }}>
                    <Paper
                      variant="outlined"
                      sx={{
                        p: 3,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        gap: 2,
                        bgcolor: "#f9fafb",
                        borderStyle: "dashed",
                        height: "100%",
                      }}
                    >
                      <Box
                        sx={{
                          width: "100%",
                          height: 128,
                          position: "relative",
                        }}
                      >
                        <Box
                          component="img"
                          src={formData?.organizerBannerImage}
                          alt="Banner"
                          sx={{
                            width: "100%",
                            height: "100%",
                            objectFit: "cover",
                            borderRadius: 2,
                          }}
                        />
                        <IconButton
                          sx={{
                            position: "absolute",
                            inset: 0,
                            color: "white",
                            bgcolor: "rgba(0,0,0,0.4)",
                            opacity: 0,
                            transition: "0.3s",
                            "&:hover": { opacity: 1 },
                          }}
                        >
                          <PhotoCameraIcon />
                        </IconButton>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        Banner Image
                      </Typography>
                      <Button
                        component="label"
                        startIcon={<UploadIcon />}
                        size="small"
                        sx={{ color: "#631bff" }}
                      >
                        Upload
                        <input
                          type="file"
                          hidden
                          accept="image/*"
                          onChange={(e) =>
                            handleFileUpload(
                              e.target.files[0],
                              "banner",
                              1200,
                              400
                            )
                          }
                        />
                      </Button>
                    </Paper>
                  </Box>
                </Box>

                {/* Org Info */}
                <Grid container spacing={3}>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="Organization Name"
                      value={formData?.orgName || ""}
                      onChange={(e) =>
                        handleProfileChange("orgName", e.target.value)
                      }
                      error={!!profileErrors.orgName}
                      helperText={profileErrors.orgName}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      type="email"
                      label="Email"
                      value={formData?.orgEmail || ""}
                      onChange={(e) =>
                        handleProfileChange("orgEmail", e.target.value)
                      }
                      error={!!profileErrors.orgEmail}
                      helperText={profileErrors.orgEmail}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      type="tel"
                      label="Phone"
                      value={formData?.phone || ""}
                      onChange={(e) =>
                        handleProfileChange("phone", e.target.value)
                      }
                      error={!!profileErrors.phone}
                      helperText={profileErrors.phone}
                    />
                  </Grid>
                </Grid>

                <TextField
                  fullWidth
                  label="Specialities"
                  value={formData?.orgSpecialities || []}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      orgSpecialities: Array.isArray(e.target.value)
                        ? e.target.value
                        : [e.target.value],
                    }))
                  }
                  select
                  SelectProps={{ multiple: true }}
                >
                  <MenuItem value="Conference">Conference</MenuItem>
                  <MenuItem value="Workshop">Workshop</MenuItem>
                  <MenuItem value="Seminar">Seminar</MenuItem>
                  <MenuItem value="Concert">Concert</MenuItem>
                  <MenuItem value="Exhibition">Exhibition</MenuItem>
                  <MenuItem value="Sports">Sports</MenuItem>
                  <MenuItem value="Other">Other</MenuItem>
                </TextField>

                <TextField
                  fullWidth
                  label="Description"
                  value={formData?.orgDescription || ""}
                  onChange={(e) =>
                    handleProfileChange("orgDescription", e.target.value)
                  }
                  error={!!profileErrors.orgDescription}
                  helperText={profileErrors.orgDescription}
                  multiline
                  rows={4}
                />

                <Box
                  sx={{ display: "flex", justifyContent: "flex-end", gap: 2 }}
                >
                  <Button variant="outlined" color="inherit">
                    Cancel
                  </Button>
                  <Button
                    variant="contained"
                    sx={{ bgcolor: "#631bff" }}
                    startIcon={<SaveIcon />}
                    onClick={handleSaveChanges}
                    disabled={!isProfileValid}
                  >
                    Save Changes
                  </Button>
                </Box>
              </Box>
            )}

            {/* Password Tab */}
            {tab === 1 && (
              <Box
                component="form"
                sx={{
                  maxWidth: 500,
                  mx: "auto",
                  display: "flex",
                  flexDirection: "column",
                  gap: 3,
                }}
              >
                <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
                  {isOAuthUser ? "Set Password" : "Change Password"}
                </Typography>

                <Typography sx={{ color: "text.secondary", mb: 2 }}>
                  {isOAuthUser
                    ? "Create a new password for your account."
                    : "Update your password to keep your account secure."}
                </Typography>

                {/* ONLY SHOW FOR NORMAL USERS */}
                {!isOAuthUser && (
                  <TextField
                    fullWidth
                    label="Current Password"
                    type={showPassword.current ? "text" : "password"}
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange("currentPassword")}
                    error={!!passwordErrors.currentPassword}
                    helperText={passwordErrors.currentPassword}
                    InputProps={{
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton
                            onClick={() =>
                              setShowPassword((p) => ({
                                ...p,
                                current: !p.current,
                              }))
                            }
                          >
                            {showPassword.current ? (
                              <Visibility />
                            ) : (
                              <VisibilityOff />
                            )}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                  />
                )}

                {/* NEW PASSWORD */}
                <TextField
                  fullWidth
                  label="New Password"
                  type={showPassword.new ? "text" : "password"}
                  value={passwordData.newPassword}
                  onChange={handlePasswordChange("newPassword")}
                  error={!!passwordErrors.newPassword}
                  helperText={
                    passwordErrors.newPassword ||
                    "Minimum 8 chars, must include uppercase, lowercase, number & special symbol."
                  }
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() =>
                            setShowPassword((p) => ({ ...p, new: !p.new }))
                          }
                        >
                          {showPassword.new ? (
                            <Visibility />
                          ) : (
                            <VisibilityOff />
                          )}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />

                {/* CONFIRM PASSWORD */}
                <TextField
                  fullWidth
                  label="Confirm New Password"
                  type={showPassword.confirm ? "text" : "password"}
                  value={passwordData.confirmPassword}
                  onChange={handlePasswordChange("confirmPassword")}
                  error={!!passwordErrors.confirmPassword}
                  helperText={passwordErrors.confirmPassword}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() =>
                            setShowPassword((p) => ({
                              ...p,
                              confirm: !p.confirm,
                            }))
                          }
                        >
                          {showPassword.confirm ? (
                            <Visibility />
                          ) : (
                            <VisibilityOff />
                          )}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />

                {/* BUTTONS */}
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "flex-end",
                    gap: 2,
                    pt: 2,
                  }}
                >
                  <Button variant="outlined" color="inherit">
                    Cancel
                  </Button>

                  <Button
                    variant="contained"
                    sx={{ bgcolor: "#631bff" }}
                    startIcon={<LockResetIcon />}
                    onClick={() => handlePasswordSubmit()}
                    disabled={!isPasswordFormValid}
                  >
                    {isOAuthUser ? "Set Password" : "Update Password"}
                  </Button>
                </Box>
              </Box>
            )}
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
