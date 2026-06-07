import { CloudUpload } from "@mui/icons-material";
import {
  Box,
  Button,
  CircularProgress,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useDispatch } from "react-redux";
import { uploadImage } from "../../redux/slices/uploadSlice";

const UploadImagesStep = ({ eventData, handleInputChange, errors }) => {
  const dispatch = useDispatch();

  const [error, setError] = useState("");
  const [fileNames, setFileNames] = useState({
    feature: "",
    card: "",
  });
  const [uploading, setUploading] = useState({
    banner: false,
    card: false,
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
        resolve(file);

        // const canvas = document.createElement("canvas");
        // canvas.width = targetWidth;
        // canvas.height = targetHeight;

        // const ctx = canvas.getContext("2d");

        // const srcAspect = img.width / img.height;
        // const targetAspect = targetWidth / targetHeight;

        // let sx, sy, sWidth, sHeight;

        // if (srcAspect > targetAspect) {
        //   // Wider → crop sides
        //   sHeight = img.height;
        //   sWidth = sHeight * targetAspect;
        //   sx = (img.width - sWidth) / 2;
        //   sy = 0;
        // } else {
        //   // Taller → crop top/bottom
        //   sWidth = img.width;
        //   sHeight = sWidth / targetAspect;
        //   sx = 0;
        //   sy = (img.height - sHeight) / 2;
        // }

        // ctx.drawImage(
        //   img,
        //   sx,
        //   sy,
        //   sWidth,
        //   sHeight,
        //   0,
        //   0,
        //   targetWidth,
        //   targetHeight
        // );

        // canvas.toBlob((blob) => resolve(blob), "image/jpeg", quality);
      };

      img.onerror = () => {
        URL.revokeObjectURL(url);
        setError("Invalid image file.");
        resolve(null);
      };
    });

  const handleFileUpload = async (file, type, targetWidth, targetHeight) => {
    setError("");
    if (!file) {
      return;
    }

    setUploading((prev) => ({ ...prev, [type]: true }));

    await resizeImage(file, targetWidth, targetHeight);
    const formData = new FormData();
    formData.append("image", file);
    try {
      const res = await dispatch(uploadImage({ formData })).unwrap();

      if (type === "banner") {
        handleInputChange("bannerImage", res.url);
      }

      if (type === "card") {
        handleInputChange("thumbnailImage", res.url);
      }

      setFileNames((prev) => ({ ...prev, [type]: file.name }));
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
    } finally {
      // Stop loader
      setUploading((prev) => ({ ...prev, [type]: false }));
    }
  };

  const UploadBox = ({ label, type, targetWidth, targetHeight }) => (
    <Paper
      sx={{
        border: "2px dashed #ccc",
        borderRadius: 2,
        p: 4,
        mb: 3,
        cursor: "pointer",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        "&:hover": { borderColor: "#1976d2", backgroundColor: "#f5f5f5" },
        position: "relative",
      }}
    >
      <CloudUpload sx={{ fontSize: 48, color: "#ccc", mb: 2 }} />
      <Button variant="outlined" component="label" disabled={uploading[type]}>
        {uploading[type] ? "Uploading..." : label}
        <input
          type="file"
          hidden
          accept="image/*"
          onChange={(e) =>
            handleFileUpload(e.target.files[0], type, targetWidth, targetHeight)
          }
        />
      </Button>
      {uploading[type] && <CircularProgress size={28} sx={{ mt: 2 }} />}
      {(fileNames[type] ||
        (type === "banner" && eventData.bannerImage) ||
        (type === "card" && eventData.thumbnailImage)) && (
        <Typography
          variant="body2"
          sx={{ mt: 2, fontStyle: "italic", color: "text.primary" }}
        >
          {fileNames[type] ||
            (type === "banner"
              ? eventData.bannerImage.split("/").pop()
              : eventData.thumbnailImage.split("/").pop())}
        </Typography>
      )}
      {/* Small Image Preview */}
      {((type === "banner" && eventData.bannerImage) ||
        (type === "card" && eventData.thumbnailImage)) && (
        <Box
          sx={{
            mt: 2,
            p: 1,
            borderRadius: 2,
            border: "1px solid #e2e8f0",
            width: 120,
            height: 80,
            overflow: "hidden",
            boxShadow: 1,
            bgcolor: "#fff",
          }}
        >
          <img
            src={
              type === "banner"
                ? eventData.bannerImage
                : eventData.thumbnailImage
            }
            alt="preview"
            style={{
              width: "100%",
              height: "100%",
              objectFit: "cover",
              borderRadius: 6,
            }}
          />
        </Box>
      )}
      <Typography variant="body2" sx={{ mt: 1, color: "text.secondary" }}>
        or drag here
      </Typography>
      {type === "banner" && errors?.bannerImage && (
        <Typography color="error" sx={{ mt: 1, fontSize: ".85rem" }}>
          {errors.bannerImage}
        </Typography>
      )}

      {type === "card" && errors?.thumbnailImage && (
        <Typography color="error" sx={{ mt: 1, fontSize: ".85rem" }}>
          {errors.thumbnailImage}
        </Typography>
      )}
    </Paper>
  );

  return (
    <Box sx={{ maxWidth: 600, mx: "auto" }}>
      <Typography variant="h5" sx={{ mb: 4, fontWeight: 600 }}>
        Upload Images
      </Typography>

      <UploadBox
        label="Choose Feature Image"
        type="banner"
        targetWidth={1200}
        targetHeight={400}
      />

      <UploadBox
        label="Choose Event Card Image"
        type="card"
        targetWidth={800}
        targetHeight={450}
      />

      {error && (
        <Typography variant="body2" color="error" sx={{ mb: 1 }}>
          {error}
        </Typography>
      )}

      {/* Guidelines */}
      <Stack spacing={0.5}>
        <Typography variant="body2" sx={{ color: "text.secondary" }}>
          Feature image must be at least 1200x400 px or 3:1 ratio.
        </Typography>
        <Typography variant="body2" sx={{ color: "text.secondary" }}>
          Event card image must be at least 800x450 px or 2:1 ratio.
        </Typography>
        <Typography variant="body2" sx={{ color: "text.secondary" }}>
          Valid file formats: JPG, GIF, PNG.
        </Typography>
      </Stack>
    </Box>
  );
};

export default UploadImagesStep;
