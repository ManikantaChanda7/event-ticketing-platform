import {
  BusinessCenter,
  Celebration,
  Code,
  EmojiEmotions,
  MusicNote,
  Palette,
  School,
  SportsBasketball,
  VolunteerActivism,
} from "@mui/icons-material";
import { Box, Container, Grid, Typography } from "@mui/material";
import { AnimatePresence, motion } from "framer-motion";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import BannerShow from "../components/bannerShow";
import EventGridShow from "../components/eventGridShow";
import {
  fetchFilteredEvents,
  fetchPopularEvents,
  fetchRecommendedEvents,
  fetchTrendingEvents,
} from "../redux/slices/eventSlice";

import { CheckCircle } from "@mui/icons-material";
import { Button, CardMedia, InputAdornment, TextField } from "@mui/material";
import { Link } from "react-router-dom";
import become_organizer from "../assets/images/become_organizer.jpg";

const categories = [
  {
    icon: <MusicNote sx={{ color: "#8e24aa" }} />,
    label: "Music",
    bg: "#f3e5f5",
  },
  {
    icon: <SportsBasketball sx={{ color: "#fb8c00" }} />,
    label: "Sports",
    bg: "#fff3e0",
  },
  {
    icon: <School sx={{ color: "#1e88e5" }} />,
    label: "Workshops",
    bg: "#e3f2fd",
  },
  {
    icon: <BusinessCenter sx={{ color: "#3949ab" }} />,
    label: "Conferences",
    bg: "#e8eaf6",
  },
  {
    icon: <Celebration sx={{ color: "#d81b60" }} />,
    label: "Festivals",
    bg: "#fce4ec",
  },
  {
    icon: <Code sx={{ color: "#43a047" }} />,
    label: "Tech & Innovation",
    bg: "#e8f5e9",
  },
  {
    icon: <VolunteerActivism sx={{ color: "#e53935" }} />,
    label: "Charity",
    bg: "#ffebee",
  },
  {
    icon: <EmojiEmotions sx={{ color: "#fdd835" }} />,
    label: "Comedy",
    bg: "#fffde7",
  },
  {
    icon: <Palette sx={{ color: "#6d4c41" }} />,
    label: "Exhibitions",
    bg: "#efebe9",
  },
];

function CategoryCard({ icon, label, color = "#6366f1", onClick }) {
  return (
    <Box
      onClick={onClick}
      sx={{
        width: { xs: 110, sm: 130, md: 150 },
        height: { xs: 110, sm: 130, md: 150 },
        background: "white",
        borderRadius: "22px",
        border: "1px solid #ececec",
        boxShadow: "0 6px 22px rgba(0, 0, 0, 0.05)",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: { xs: 1, sm: 1.2, md: 1.4 },
        cursor: "pointer",
        transition: "all 0.25s ease",
        textAlign: "center",
        "&:hover": {
          transform: "translateY(-6px)",
          boxShadow: "0 12px 28px rgba(0,0,0,0.10)",
        },
      }}
    >
      {/* Icon container */}
      <Box
        sx={{
          width: { xs: 45, sm: 55, md: 60 },
          height: { xs: 45, sm: 55, md: 60 },
          borderRadius: "50%",
          background: `${color}1A`,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Box
          sx={{
            fontSize: { xs: 22, sm: 26, md: 30 },
            color,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            lineHeight: 0,
          }}
        >
          {icon}
        </Box>
      </Box>

      {/* Label */}
      <Typography
        fontWeight={600}
        fontSize={{ xs: 13, sm: 14, md: 15 }}
        sx={{
          color: "#2c2c2c",
          maxWidth: "100%",
          whiteSpace: "nowrap",
          overflow: "hidden",
          textOverflow: "ellipsis",
          px: 1,
        }}
      >
        {label}
      </Typography>
    </Box>
  );
}

export default function HomePage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const role = localStorage.getItem("role");

  const locationLabel =
    useSelector(
      (state) =>
        state.location.location?.label ||
        state.location.location?.locationLabel,
    )?.split(", ")[0] || "Your City";
  const { filteredEvents } = useSelector((state) => state.event);

  const pageVariants = {
    initial: { opacity: 0 },
    in: { opacity: 1 },
    out: { opacity: 0 },
  };

  const pageTransition = {
    type: "tween",
    ease: "anticipate",
    duration: 0.5,
  };

  useEffect(() => {
    dispatch(fetchTrendingEvents()).unwrap();
    dispatch(fetchRecommendedEvents()).unwrap();
    dispatch(fetchPopularEvents()).unwrap();
    dispatch(
      fetchFilteredEvents({
        params: {
          isFeatured: true,
          location: false,
          page: 1,
          limit: 10,
        },
        key: "featured",
      }),
    ).unwrap();
  }, [dispatch]);

  // Refetch popular events when location changes
  useEffect(() => {
    if (locationLabel && locationLabel !== "Your City") {
      dispatch(fetchPopularEvents()).unwrap();
      dispatch(
        fetchFilteredEvents({
          params: {
            isFeatured: true,
            location: true,
            page: 1,
            limit: 10,
          },
          key: "featured",
        }),
      ).unwrap();
    }
  }, [locationLabel, dispatch]);

  const handleCategory = (label) => {
    // navigate("/search", { state: label });
    navigate(`/category/${encodeURIComponent(label)}`);
  };

  return (
    <div style={{ position: "relative", minHeight: "100vh" }}>
      <AnimatePresence mode="wait">
        <motion.div
          key="content"
          initial="initial"
          animate="in"
          exit="out"
          variants={pageVariants}
          transition={pageTransition}
        >
          <Box sx={{ bgcolor: "#f7f7f7" }}>
            {/* BannerShow (UNCHANGED) */}
            {filteredEvents?.featured?.data?.data?.length > 0 && (
              <Box>
                <BannerShow events={filteredEvents.featured.data.data} />
              </Box>
            )}

            {/* Categories */}
            <Box pt={6} pb={8} bgcolor="#fff">
              <Container>
                <Typography
                  variant="h5"
                  fontWeight={800}
                  textAlign="center"
                  mb={4}
                  sx={{
                    fontSize: {
                      xs: "1.1rem", // small screens
                      sm: "1.3rem", // tablets
                      md: "1.5rem", // desktops (same as h5)
                    },
                  }}
                >
                  Explore Events by Category
                </Typography>

                <Grid
                  container
                  justifyContent="center"
                  spacing={{ xs: 2, sm: 3 }}
                >
                  {categories.map((c) => (
                    <Grid item xs={6} sm={4} md={3} lg={2} key={c.label}>
                      <CategoryCard
                        {...c}
                        onClick={() => handleCategory(c.label)}
                        sxOverride={{
                          width: { xs: 110, sm: 130, md: 150 },
                          height: { xs: 110, sm: 130, md: 150 },
                        }}
                      />
                    </Grid>
                  ))}
                </Grid>
              </Container>
            </Box>

            {/* Popular Events (UNCHANGED) */}
            {filteredEvents?.popular && filteredEvents.popular.length > 0 && (
              <Box pb={8} sx={{ bgcolor: "#fff" }}>
                <Container
                  disableGutters
                  maxWidth={false}
                  sx={{ px: { xs: 2, sm: 4, md: 8, lg: 12 } }}
                >
                  <EventGridShow
                    title={`Popular in ${locationLabel}`}
                    events={filteredEvents.popular}
                  />
                </Container>
              </Box>
            )}

            {/* Trending Events (UNCHANGED) */}
            {filteredEvents?.trending && filteredEvents.trending.length > 0 && (
              <Box pb={8} sx={{ bgcolor: "#fff" }}>
                <Container
                  disableGutters
                  maxWidth={false}
                  sx={{ px: { xs: 2, sm: 4, md: 8, lg: 12 } }}
                >
                  <EventGridShow
                    title="Trending events online"
                    events={filteredEvents.trending}
                  />
                </Container>
              </Box>
            )}

            {/* Recommended Events (UNCHANGED) */}
            {filteredEvents?.recommended &&
              filteredEvents.recommended.length > 0 && (
                <Box pb={8} sx={{ bgcolor: "#fff" }}>
                  <Container
                    disableGutters
                    maxWidth={false}
                    sx={{ px: { xs: 2, sm: 4, md: 8, lg: 12 } }}
                  >
                    <EventGridShow
                      title="Events based on your interests"
                      events={filteredEvents.recommended}
                    />
                  </Container>
                </Box>
              )}

            {/* Organizer Section (KEEPING EXACT UPDATE) */}
            {role === "user" && (
              <Box py={{ xs: 4, sm: 5, md: 6 }} sx={{ bgcolor: "#f3f1ff" }}>
                <Container>
                  <Box
                    mx="auto"
                    sx={{
                      maxWidth: { xs: "100%", sm: 700, md: 960 },
                      bgcolor: "#fff",
                      borderRadius: { xs: "28px", sm: "36px", md: "48px" },
                      overflow: "hidden",
                      boxShadow: 4,
                      display: "flex",
                      flexDirection: { xs: "column", md: "row" },
                    }}
                  >
                    {/* Text Content */}
                    <Box
                      flex={{ xs: 1, md: 0.55 }}
                      p={{ xs: 3, sm: 4, md: 5 }}
                      display="flex"
                      flexDirection="column"
                      justifyContent="center"
                      gap={2}
                      textAlign="left" // <-- ALWAYS LEFT ALIGNED
                    >
                      <Typography
                        variant="h5"
                        fontWeight={800}
                        mb={{ xs: 1, md: 2 }}
                        textAlign="left" // <-- LEFT ALWAYS
                      >
                        Become an Event Organizer
                      </Typography>

                      <Typography
                        color="text.secondary"
                        mb={{ xs: 2, sm: 2, md: 3 }}
                        fontSize={{ xs: 14, sm: 15, md: 16 }}
                        textAlign="left" // <-- LEFT ALWAYS
                      >
                        Host your own events and reach thousands of attendees.
                        Our platform provides all the tools you need to create,
                        promote and manage successful events.
                      </Typography>

                      {/* Features List */}
                      <Box
                        display="flex"
                        flexDirection="column"
                        gap={1.2}
                        mb={{ xs: 2, sm: 3 }}
                        alignItems="flex-start" // <-- LEFT ALWAYS
                      >
                        {[
                          "Easy event creation and management",
                          "Powerful promotional tools",
                          "Secure payment processing",
                        ].map((t) => (
                          <Box
                            key={t}
                            display="flex"
                            alignItems="center"
                            gap={1}
                            justifyContent="flex-start"
                          >
                            <CheckCircle
                              sx={{
                                color: "#6755f2ff",
                                fontSize: { xs: 20, sm: 22, md: 24 },
                              }}
                            />
                            <Typography fontSize={{ xs: 14, sm: 15, md: 16 }}>
                              {t}
                            </Typography>
                          </Box>
                        ))}
                      </Box>

                      <Button
                        component={Link}
                        to="/register/organizer"
                        variant="contained"
                        sx={{
                          bgcolor: "#6755f2ff",
                          "&:hover": { bgcolor: "primary.dark" },
                          alignSelf: "flex-start", // <-- LEFT ALWAYS
                          borderRadius: 4,
                          px: { xs: 3, sm: 4 },
                          py: { xs: 1, sm: 1.2 },
                          fontSize: { xs: 14, sm: 15 },
                        }}
                      >
                        Register as Organizer
                      </Button>
                    </Box>

                    {/* Image Section */}
                    {/* <Box
                      flex={{ xs: 1, md: 0.45 }}
                      sx={{
                        // height: "100%",
                        minHeight: { xs: 200, sm: 250, md: "auto" },
                      }}
                    >
                      <CardMedia
                        component="img"
                        image={become_organizer}
                        alt="Event organization"
                        sx={{
                          width: "100%",
                          height: "100%",
                          // maxHeight: { xs: 260, sm: 300, md: 370 },
                          objectFit: "cover",
                          objectPosition: "center",
                        }}
                      />
                    </Box> */}
                    {/* Image Section */}
                    <Box
                      flex={{ xs: 1, md: 0.45 }}
                      sx={{
                        minHeight: { xs: 200, sm: 240 }, // Smaller height on small screens
                        // height: { xs: "auto", md: "100%" }, // Full height only on large screens
                      }}
                    >
                      <CardMedia
                        component="img"
                        image={become_organizer}
                        alt="Event organization"
                        sx={{
                          width: "100%",
                          height: { xs: 240, sm: 280, md: "100%" }, // Responsive heights
                          objectFit: "cover",
                          objectPosition: "center",
                        }}
                      />
                    </Box>
                  </Box>
                </Container>
              </Box>
            )}

            {/* Newsletter */}
            <Box py={6} sx={{ bgcolor: "#f7f7f7" }}>
              <Container sx={{ maxWidth: 640 }}>
                <Typography
                  variant="h5"
                  fontWeight={800}
                  textAlign="center"
                  mb={1}
                >
                  Never Miss an Event
                </Typography>

                <Typography color="text.secondary" textAlign="center" mb={3}>
                  Subscribe to our newsletter and get personalized event
                  recommendations.
                </Typography>

                <Box
                  component="form"
                  display="flex"
                  flexDirection={{ xs: "column", sm: "row" }}
                  gap={2}
                  mx="auto"
                  sx={{ maxWidth: 560 }}
                >
                  <TextField
                    placeholder="Your email address"
                    fullWidth
                    sx={{ bgcolor: "#fff" }}
                  />

                  <Button
                    variant="contained"
                    sx={{
                      bgcolor: "#6755f2ff",
                      "&:hover": { bgcolor: "primary.dark" },
                      width: { xs: "100%", sm: "auto" },
                      borderRadius: 2,
                    }}
                  >
                    Subscribe
                  </Button>
                </Box>
              </Container>
            </Box>
          </Box>
        </motion.div>
      </AnimatePresence>
    </div>
  );
}
