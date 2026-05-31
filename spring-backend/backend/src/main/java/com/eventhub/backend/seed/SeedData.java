package com.eventhub.backend.seed;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.SeatStatus;
import com.eventhub.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Random random = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static long emailCounter = 0;

    // Image pools (using placeholder URLs - replace with actual Cloudinary URLs)
    private static final String[] BANNER_IMAGES = {
            "https://res.cloudinary.com/dwzmsvp7f/image/upload/f_auto,w_1280/c_crop,g_custom/v1754982826/vilaasv0gvddvoxtibuf.png",
            "https://res.cloudinary.com/dwzmsvp7f/image/upload/f_auto,w_1280/c_crop,g_custom/v1756293972/pjizc69iyi7uuianqkvg.jpg",
            "https://res.cloudinary.com/dwzmsvp7f/image/upload/f_auto,w_1280/c_crop,g_custom/v1755504411/k7qag8tlilih20xlto5c.jpg",
            "https://res.cloudinary.com/dwzmsvp7f/image/upload/f_auto,w_1280/c_crop,g_custom/v1754371118/jub74bw6cc99zqe9rg49.jpg",
            "https://res.cloudinary.com/dwzmsvp7f/image/upload/f_auto,w_1280/c_crop,g_custom/v1748859911/ad5ql06hb51m6zswqbp9.png",
    };

    private static final String[] THUMBNAIL_IMAGES = {
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756455254/iquljstfxkgepi8s18pk.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755504163/ovj5vbqwrkhpgwuxuot7.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755754372/arhhmtdu5jfwd0p7ltea.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1754319439/gjzedwmrxfdboi45secc.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753269077/hlxlycj5weawbwgtu9ku.jpg",
    };

    private static final String[] CATEGORIES = {
            "Music", "Sports", "Workshops", "Conferences", "Festivals",
            "Tech & Innovation", "Charity", "Comedy", "Exhibitions"
    };

    private static final String[] WEEKDAYS = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    private static final String[] ORGANIZER_SPECIALITIES = {
            "Conference", "Workshop", "Seminar", "Concert", "Exhibition", "Sports", "Other"
    };

    private List<CityData> citiesData;
    private List<User> users;
    private List<Organizer> organizers;
    private List<Venue> venues;
    private List<Event> events;

    public SeedData(
            UserRepository userRepository,
            OrganizerRepository organizerRepository,
            VenueRepository venueRepository,
            EventRepository eventRepository,
            SessionRepository sessionRepository,
            ReviewRepository reviewRepository,
            SeatRepository seatRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.reviewRepository = reviewRepository;
        this.seatRepository = seatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Uncomment the line below to run seeding on application startup
        seedDatabase();
    }

    @Transactional
    public void seedDatabase() throws Exception {
        System.out.println("Starting database seeding...");

        // Load cities data
        loadCitiesData();

        // Clear existing data
        clearDatabase();

        String password = passwordEncoder.encode("password123");

        // 1. Seed Users
        seedUsers(password);

        // 2. Seed Organizers
        seedOrganizers(password);

        // 3. Seed Venues
        seedVenues();

        // 4. Seed Events
        seedEvents();

        // 5. Seed Sessions
        seedSessions();

        // 6. Update starting prices
        updateStartingPrices();

        // 7. Update organizer stats
        updateOrganizerStats();

        // 8. Update user interests
        updateUserInterests();

        System.out.println("Database seeding completed successfully!");
    }

    private void loadCitiesData() throws Exception {
        // Load cities from JSON file
        ClassPathResource resource = new ClassPathResource("com/eventhub/backend/data/Indian-Cities-Database.json");
        JsonNode rootNode = mapper.readTree(resource.getInputStream());
        citiesData = new ArrayList<>();

        for (JsonNode node : rootNode) {
            String city = node.get("City").asText();
            String state = node.get("State").asText();
            double lng = node.get("Long").asDouble();
            double lat = node.get("Lat").asDouble();
            citiesData.add(new CityData(city, state, lng, lat));
        }

        System.out.println("Loaded " + citiesData.size() + " cities from JSON file");
    }

    private void clearDatabase() {
        System.out.println("Clearing existing data...");
        try {
            reviewRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Reviews table might not exist yet");
        }
        try {
            sessionRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Sessions table might not exist yet");
        }
        try {
            seatRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Seats table might not exist yet");
        }
        try {
            eventRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Events table might not exist yet");
        }
        try {
            venueRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Venues table might not exist yet");
        }
        try {
            organizerRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Organizers table might not exist yet");
        }
        try {
            userRepository.deleteAll();
        } catch (Exception e) {
            System.out.println("Users table might not exist yet");
        }
        System.out.println("Existing data cleared");
    }

    private void seedUsers(String password) {
        System.out.println("Seeding users...");
        int totalUsers = 25000;
        int batchSize = 100;
        users = new ArrayList<>();

        for (int i = 0; i < totalUsers; i += batchSize) {
            List<User> batch = new ArrayList<>();
            int currentBatchSize = Math.min(batchSize, totalUsers - i);

            for (int j = 0; j < currentBatchSize; j++) {
                User user = new User();
                user.setUsername(generateRandomName());
                user.setEmail(generateRandomEmail());
                user.setPassword(password);
                user.setRole(Role.USER);
                user.setPhone(generateRandomIndianPhone());
                user.setUserProfileImage("https://cdn-icons-png.flaticon.com/512/847/847969.png");
                user.setInterestedEvents(new HashSet<>());
                batch.add(user);
            }

            List<User> savedUsers = userRepository.saveAll(batch);
            users.addAll(savedUsers);
            System.out.println("Inserted " + Math.min(i + batchSize, totalUsers) + " users so far");
        }
        System.out.println("Users seeded: " + users.size());
    }

    private void seedOrganizers(String password) {
        System.out.println("Seeding organizers...");
        organizers = new ArrayList<>();
        int totalOrganizers = 80;

        for (int i = 0; i < totalOrganizers; i++) {
            User user = new User();
            user.setUsername(generateRandomName());
            user.setEmail(generateRandomEmail());
            user.setPassword(password);
            user.setRole(Role.ORGANIZER);
            user.setPhone(generateRandomIndianPhone());
            user.setUserProfileImage("https://cdn-icons-png.flaticon.com/512/847/847969.png");

            User savedUser = userRepository.save(user);

            Organizer organizer = new Organizer();
            organizer.setUser(savedUser);
            organizer.setPhone(generateRandomIndianPhone());
            organizer.setOrgName(generateRandomCompanyName());
            organizer.setOrgEmail(generateRandomEmail());
            organizer.setOrgDescription(generateRandomDescription());
            organizer.setOrgSpecialities(
                    new HashSet<>(randomItems(Arrays.asList(ORGANIZER_SPECIALITIES), random.nextInt(3) + 2)));
            organizer.setOrganizerProfileImage("https://yourcdn.com/default-profile.png");
            organizer.setOrganizerBannerImage(randomItem(BANNER_IMAGES));
            organizer.setAverageRating(0.0);
            organizer.setTotalReviews(0);
            organizer.setEventsHosted(new HashSet<>());

            organizers.add(organizerRepository.save(organizer));
            if ((i + 1) % 20 == 0) {
                System.out.println("Created " + (i + 1) + "/" + totalOrganizers + " organizers");
            }
        }
        System.out.println("Organizers seeded: " + organizers.size());
    }

    private void seedVenues() {
        System.out.println("Seeding venues...");
        venues = new ArrayList<>();
        int totalVenues = 500;

        for (int i = 0; i < totalVenues; i++) {
            CityData city = randomItem(citiesData);
            SeatingLayoutResult layoutResult = generateSeatingLayout();
            double[] coordinates = randomNearbyCoordinates(city.lng, city.lat, 10);

            Venue venue = new Venue();
            venue.setName(generateVenueName());
            venue.setCity(city.city);
            venue.setState(city.state);
            venue.setCapacity(layoutResult.totalCapacity);
            venue.setLatitude(coordinates[1]);
            venue.setLongitude(coordinates[0]);
            venue.setSeatingLayout(layoutResult.layout.toString());

            venues.add(venueRepository.save(venue));
            if ((i + 1) % 100 == 0) {
                System.out.println("Created " + (i + 1) + "/" + totalVenues + " venues");
            }
        }
        System.out.println("Venues seeded: " + venues.size());
    }

    private void seedEvents() {
        System.out.println("Seeding events...");
        events = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int totalEvents = 300;

        for (int i = 0; i < totalEvents; i++) {
            String category = randomItem(CATEGORIES);
            Organizer organizer = randomItem(organizers);
            Venue venue = randomItem(venues);
            String recurrence = getRecurrence();

            EventDateResult dateResult = generateEventDates(today, recurrence);
            TimeRangeResult timeResult = getRandomTimeRange();

            List<Review> ratings = new ArrayList<>();
            double averageRating = 0.0;

            if (!dateResult.status.equals("upcoming")) {
                ratings = generateRatings(users);
                if (!ratings.isEmpty()) {
                    averageRating = ratings.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0);
                    averageRating = Math.round(averageRating * 10.0) / 10.0;
                }
            }

            Event event = new Event();
            event.setTitle(capitalize(generateRandomBuzzPhrase()));
            event.setDescription(generateRandomDescription());
            event.setStartDate(dateResult.startDate);
            event.setEndDate(dateResult.endDate);
            event.setStartTime(timeResult.startTime);
            event.setEndTime(timeResult.endTime);
            event.setRecurrence(recurrence);
            event.setSelectedWeekdays(new HashSet<>(dateResult.selectedWeekdays));
            event.setLocationLabel(venue.getName() + ", " + venue.getCity());
            event.setLocationLatitude(venue.getLatitude());
            event.setLocationLongitude(venue.getLongitude());
            event.setLocationType("Point");
            event.setAgeLimit(randomItem(new Integer[] { 0, 5, 12, 16, 18, 21 }));
            event.setLanguages(new HashSet<>(Arrays.asList("English", "Hindi")));
            event.setCategory(category);
            event.setIsFeatured(false);
            event.setBannerImage(randomItem(BANNER_IMAGES));
            event.setThumbnailImage(randomItem(THUMBNAIL_IMAGES));
            event.setAverageRating(averageRating);
            event.setInterestedUsers(0);
            event.setOrganizer(organizer);
            event.setVenue(venue);
            event.setStatus(EventStatus.valueOf(dateResult.status.toUpperCase()));
            event.setStartingPrice(0.0);

            Event savedEvent = eventRepository.save(event);
            events.add(savedEvent);

            // Save ratings
            if (!ratings.isEmpty()) {
                for (Review rating : ratings) {
                    rating.setEvent(savedEvent);
                    reviewRepository.save(rating);
                }
            }
            if ((i + 1) % 500 == 0) {
                System.out.println("Created " + (i + 1) + "/" + totalEvents + " events");
            }
        }
        System.out.println("Events seeded: " + events.size());

        // Assign featured events
        assignFeaturedEvents();
    }

    private void seedSessions() {
        System.out.println("Seeding sessions...");
        List<Session> sessions = new ArrayList<>();
        int batchSize = 500;
        int eventCount = 0;
        int totalEvents = events.size();

        for (Event event : events) {
            eventCount++;
            if (eventCount % 500 == 0) {
                System.out.println("Processing event " + eventCount + "/" + totalEvents);
            }

            List<LocalDate> sessionDates = generateSessionDates(event);

            Venue venue = venues.stream()
                    .filter(v -> v.getId().equals(event.getVenue().getId()))
                    .findFirst()
                    .orElseThrow();

            for (LocalDate sDate : sessionDates) {
                Session session = new Session();
                session.setDate(sDate);
                session.setStartTime(event.getStartTime());
                session.setEndTime(event.getEndTime());
                session.setEvent(event);

                // Generate release date (21 days before event start)
                int batchIndex = sessionDates.indexOf(sDate) / 7;
                LocalDate baseRelease = event.getStartDate().minusDays(21).plusDays(batchIndex * 7);
                session.setReleaseDate(baseRelease.atStartOfDay());

                // Generate seats
                List<Seat> seats = generateSeats(venue, users);
                int bookedCount = (int) seats.stream().filter(s -> s.getUser() != null).count();
                session.setOccupancy((int) ((bookedCount * 100) / seats.size()));

                // Generate tickets
                Map<String, Object> seatingLayout = parseSeatingLayout(venue.getSeatingLayout());
                List<Session.Ticket> tickets = generateTickets(seatingLayout);
                session.setTickets(tickets);

                Session savedSession = sessionRepository.save(session);

                // Save seats
                for (Seat seat : seats) {
                    seat.setSession(savedSession);
                    seatRepository.save(seat);
                }

                sessions.add(savedSession);

                if (sessions.size() >= batchSize) {
                    sessionRepository.saveAll(sessions);
                    sessions.clear();
                }
            }
        }

        if (!sessions.isEmpty()) {
            sessionRepository.saveAll(sessions);
        }
        System.out.println("Sessions seeded. Total events processed: " + eventCount);
    }

    private void updateStartingPrices() {
        System.out.println("Updating starting prices...");
        int totalEvents = events.size();
        int count = 0;
        for (Event event : events) {
            count++;
            if (count % 100 == 0) {
                System.out.println("Updated prices for " + count + "/" + totalEvents + " events");
            }
            List<Session> eventSessions = sessionRepository.findByEvent(event);
            double minPrice = eventSessions.stream()
                    .flatMap(s -> s.getTickets().stream())
                    .mapToDouble(Session.Ticket::getPrice)
                    .min()
                    .orElse(0.0);
            event.setStartingPrice(minPrice);
            eventRepository.save(event);
        }
        System.out.println("Starting prices updated for " + totalEvents + " events");
    }

    private void updateOrganizerStats() {
        System.out.println("Updating organizer stats...");
        Map<Long, OrganizerStats> organizerStatsMap = new HashMap<>();

        for (Event event : events) {
            Long organizerId = event.getOrganizer().getId();
            OrganizerStats stats = organizerStatsMap.computeIfAbsent(organizerId, k -> new OrganizerStats());
            stats.hostedIds.add(event.getId());

            List<Review> eventReviews = reviewRepository.findByEvent(event);
            for (Review review : eventReviews) {
                stats.ratings.add((double) review.getRating());
            }
        }

        for (Organizer organizer : organizers) {
            OrganizerStats stats = organizerStatsMap.get(organizer.getId());
            if (stats != null) {
                int totalReviews = stats.ratings.size();
                double avgRating = totalReviews > 0
                        ? Math.round(
                                (stats.ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)) * 10.0)
                                / 10.0
                        : 0.0;
                organizer.setTotalReviews(totalReviews);
                organizer.setAverageRating(avgRating);
                Set<Event> hostedEventSet = new HashSet<>();
                for (Long id : stats.hostedIds) {
                    events.stream().filter(e -> e.getId().equals(id)).findFirst().ifPresent(hostedEventSet::add);
                }
                organizer.setEventsHosted(hostedEventSet);
                organizerRepository.save(organizer);
            }
        }
        System.out.println("Organizer stats updated");
    }

    private void updateUserInterests() {
        System.out.println("Updating user interests...");
        Map<Long, Integer> eventInterestCount = new HashMap<>();
        int totalUsers = users.size();
        int count = 0;

        for (User user : users) {
            count++;
            if (count % 5000 == 0) {
                System.out.println("Processed " + count + "/" + totalUsers + " users");
            }
            int k;
            double r = random.nextDouble();
            if (r < 0.6)
                k = random.nextInt(11) + 5; // 5-15
            else if (r < 0.9)
                k = random.nextInt(15) + 16; // 16-30
            else
                k = random.nextInt(20) + 31; // 31-50

            List<Event> interestedEvents = randomItems(events, Math.min(k, events.size()));
            List<Long> interestedIds = interestedEvents.stream().map(Event::getId).collect(Collectors.toList());

            Set<Event> interestedEventSet = new HashSet<>();
            for (Long id : interestedIds) {
                events.stream().filter(e -> e.getId().equals(id)).findFirst().ifPresent(interestedEventSet::add);
            }
            user.setInterestedEvents(interestedEventSet);
            userRepository.save(user);

            for (Event event : interestedEvents) {
                eventInterestCount.put(event.getId(), eventInterestCount.getOrDefault(event.getId(), 0) + 1);
            }
        }
        System.out.println("Processed " + totalUsers + " users");

        System.out.println("Updating event interest counts...");
        int eventCount = 0;
        for (Event event : events) {
            event.setInterestedUsers(eventInterestCount.getOrDefault(event.getId(), 0));
            eventRepository.save(event);
            eventCount++;
            if (eventCount % 500 == 0) {
                System.out.println("Updated " + eventCount + "/" + events.size() + " events");
            }
        }
        System.out.println("User interests updated for " + totalUsers + " users and " + events.size() + " events");
    }

    private void assignFeaturedEvents() {
        System.out.println("Assigning featured events...");
        Map<String, List<Event>> byLocation = new HashMap<>();
        Map<String, List<Event>> byLocationCategory = new HashMap<>();

        for (Event event : events) {
            String city = event.getLocationLabel().split(", ")[1];
            String category = event.getCategory();

            byLocation.computeIfAbsent(city, k -> new ArrayList<>()).add(event);
            String key = city + "-" + category;
            byLocationCategory.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
        }

        // Featured per location
        for (List<Event> list : byLocation.values()) {
            int featuredCount = Math.min(15, Math.max(5, (int) (list.size() * 0.1)));
            List<Event> selected = randomItems(list, featuredCount);
            for (Event event : selected) {
                event.setIsFeatured(true);
                eventRepository.save(event);
            }
        }

        // Featured per location + category
        for (List<Event> list : byLocationCategory.values()) {
            int featuredCount = Math.min(4, Math.max(2, (int) (list.size() * 0.15)));
            List<Event> selected = randomItems(list, featuredCount);
            for (Event event : selected) {
                event.setIsFeatured(true);
                eventRepository.save(event);
            }
        }
        System.out.println("Featured events assigned");
    }

    // Helper methods

    private String generateRandomName() {
        String[] firstNames = { "John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Lisa", "James",
                "Maria" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez" };
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    private String generateRandomEmail() {
        String[] domains = { "gmail.com", "yahoo.com", "hotmail.com", "outlook.com" };
        long counter = emailCounter++;
        return "user" + counter + "@" + domains[random.nextInt(domains.length)];
    }

    private String generateRandomIndianPhone() {
        StringBuilder phone = new StringBuilder("+91");
        for (int i = 0; i < 10; i++) {
            phone.append(random.nextInt(10));
        }
        return phone.toString();
    }

    private String generateRandomCompanyName() {
        String[] prefixes = { "Global", "Tech", "Creative", "Digital", "Smart", "Future", "Innovative", "Premium" };
        String[] suffixes = { "Solutions", "Systems", "Technologies", "Industries", "Corp", "Group", "Labs",
                "Ventures" };
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)];
    }

    private String generateRandomDescription() {
        return "A leading organization dedicated to innovation and excellence. We bring together communities and create meaningful connections through our events.";
    }

    private String generateVenueName() {
        String[] prefixes = { "Grand", "Royal", "City", "National", "International", "Metropolitan", "Central" };
        String[] suffixes = { "Convention Center", "Auditorium", "Arena", "Palace Grounds", "Banquet Hall",
                "Expo Center", "Stadium" };
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)];
    }

    private String generateRandomBuzzPhrase() {
        String[] buzzwords = { "Innovation", "Excellence", "Future", "Growth", "Success", "Leadership",
                "Transformation" };
        return "The " + buzzwords[random.nextInt(buzzwords.length)] + " Summit";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private <T> T randomItem(T[] array) {
        return array[random.nextInt(array.length)];
    }

    private <T> T randomItem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <T> List<T> randomItems(List<T> list, int count) {
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private double[] randomNearbyCoordinates(double lng, double lat, double radiusInKm) {
        double earthRadius = 6371.0; // km
        double dist = random.nextDouble() * (radiusInKm / earthRadius);
        double bearing = random.nextDouble() * 2 * Math.PI;

        double latRad = Math.toRadians(lat);
        double lngRad = Math.toRadians(lng);

        double newLatRad = Math.asin(
                Math.sin(latRad) * Math.cos(dist) +
                        Math.cos(latRad) * Math.sin(dist) * Math.cos(bearing));

        double newLngRad = lngRad + Math.atan2(
                Math.sin(bearing) * Math.sin(dist) * Math.cos(latRad),
                Math.cos(dist) - Math.sin(latRad) * Math.sin(newLatRad));

        return new double[] { Math.toDegrees(newLngRad), Math.toDegrees(newLatRad) };
    }

    private SeatingLayoutResult generateSeatingLayout() {
        String[] sections = { "Platinum", "Gold", "Silver" };
        StringBuilder layout = new StringBuilder();
        layout.append("{");
        int totalCapacity = 0;

        for (int i = 0; i < sections.length; i++) {
            String section = sections[i];
            int seatsPerRowRangeMin, seatsPerRowRangeMax, rowCountRangeMin, rowCountRangeMax;

            if (section.equals("Platinum")) {
                seatsPerRowRangeMin = 6;
                seatsPerRowRangeMax = 10;
                rowCountRangeMin = 3;
                rowCountRangeMax = 5;
            } else if (section.equals("Gold")) {
                seatsPerRowRangeMin = 8;
                seatsPerRowRangeMax = 12;
                rowCountRangeMin = 6;
                rowCountRangeMax = 10;
            } else {
                seatsPerRowRangeMin = 10;
                seatsPerRowRangeMax = 15;
                rowCountRangeMin = 8;
                rowCountRangeMax = 15;
            }

            int rowCount = rowCountRangeMin + random.nextInt(rowCountRangeMax - rowCountRangeMin + 1);
            int sectionCapacity = 0;

            layout.append("\"").append(section).append("\":{");
            layout.append("\"rows\":[");

            for (int j = 0; j < rowCount; j++) {
                int seatsPerRow = seatsPerRowRangeMin + random.nextInt(seatsPerRowRangeMax - seatsPerRowRangeMin + 1);
                String rowLabel = String.valueOf((char) ('A' + j));
                sectionCapacity += seatsPerRow;
                totalCapacity += seatsPerRow;

                layout.append("{\"label\":\"").append(rowLabel).append("\",\"seats\":[");
                for (int k = 0; k < seatsPerRow; k++) {
                    layout.append("\"").append(rowLabel).append(k + 1).append("\"");
                    if (k < seatsPerRow - 1)
                        layout.append(",");
                }
                layout.append("]}");
                if (j < rowCount - 1)
                    layout.append(",");
            }

            layout.append("],\"sectionCapacity\":").append(sectionCapacity);
            layout.append("}");
            if (i < sections.length - 1)
                layout.append(",");
        }

        layout.append("}");

        return new SeatingLayoutResult(layout.toString(), totalCapacity);
    }

    private String getRecurrence() {
        double r = random.nextDouble();
        if (r < 0.75)
            return "single";
        if (r < 0.95)
            return "multi-day";
        return "weekly";
    }

    private EventDateResult generateEventDates(LocalDate today, String recurrence) {
        int statusBucket = random.nextInt(3); // 0=completed, 1=ongoing, 2=upcoming
        LocalDate startDate, endDate;
        String status;
        List<String> selectedWeekdays = new ArrayList<>();

        if (recurrence.equals("single")) {
            if (statusBucket == 0) {
                startDate = randomDateBetween(today.minusDays(60), today.minusDays(2));
            } else if (statusBucket == 1) {
                startDate = today;
            } else {
                startDate = randomDateBetween(today.plusDays(1), today.plusDays(30));
            }
            endDate = startDate;
        } else if (recurrence.equals("multi-day")) {
            int durationDays = 2 + random.nextInt(6);
            if (statusBucket == 0) {
                startDate = randomDateBetween(today.minusDays(60), today.minusDays(10));
                endDate = startDate.plusDays(durationDays);
            } else if (statusBucket == 1) {
                startDate = randomDateBetween(today.minusDays(5), today);
                endDate = today.plusDays(durationDays);
            } else {
                startDate = randomDateBetween(today.plusDays(1), today.plusDays(30));
                endDate = startDate.plusDays(durationDays);
            }
        } else { // weekly
            selectedWeekdays = randomItems(Arrays.asList(WEEKDAYS), 1 + random.nextInt(3));
            LocalDate rawStart, rawEnd;

            if (statusBucket == 0) {
                rawStart = randomDateBetween(today.minusDays(90), today.minusDays(42));
                rawEnd = rawStart.plusDays(7 + random.nextInt(36));
            } else if (statusBucket == 1) {
                rawStart = randomDateBetween(today.minusDays(20), today);
                rawEnd = rawStart.plusDays(14 + random.nextInt(29));
            } else {
                rawStart = randomDateBetween(today.plusDays(2), today.plusDays(30));
                rawEnd = rawStart.plusDays(14 + random.nextInt(29));
            }

            // Find matching weekdays
            List<LocalDate> matches = new ArrayList<>();
            for (LocalDate d = rawStart; !d.isAfter(rawEnd); d = d.plusDays(1)) {
                String dayName = d.getDayOfWeek().toString();
                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();
                if (selectedWeekdays.contains(dayName)) {
                    matches.add(d);
                }
            }

            if (!matches.isEmpty()) {
                startDate = matches.get(0);
                endDate = matches.get(matches.size() - 1);
            } else {
                startDate = rawStart;
                endDate = rawStart.plusDays(7);
            }
        }

        // Determine status
        if (endDate.isBefore(today)) {
            status = "completed";
        } else if (!startDate.isAfter(today) && !endDate.isBefore(today)) {
            status = "ongoing";
        } else {
            status = "upcoming";
        }

        return new EventDateResult(startDate, endDate, status, selectedWeekdays);
    }

    private LocalDate randomDateBetween(LocalDate from, LocalDate to) {
        long daysBetween = ChronoUnit.DAYS.between(from, to);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);
        return from.plusDays(randomDays);
    }

    private TimeRangeResult getRandomTimeRange() {
        int startHour = 9 + random.nextInt(12); // 9-20
        int duration = 1 + random.nextInt(4); // 1-4 hours
        int endHour = Math.min(startHour + duration, 23);
        String minute = random.nextBoolean() ? "00" : "30";
        String startTime = String.format("%02d:%s", startHour, minute);
        String endTime = String.format("%02d:%s", endHour, minute);
        return new TimeRangeResult(startTime, endTime);
    }

    private List<Review> generateRatings(List<User> users) {
        int count = random.nextInt(31);
        List<User> selectedUsers = randomItems(users, count);
        List<Review> ratings = new ArrayList<>();

        for (User user : selectedUsers) {
            Review review = new Review();
            review.setUser(user);
            review.setRating(1 + random.nextInt(5));
            review.setReview(generateRandomDescription());
            ratings.add(review);
        }

        return ratings;
    }

    private List<LocalDate> generateSessionDates(Event event) {
        List<LocalDate> dates = new ArrayList<>();

        if (event.getRecurrence().equals("single")) {
            dates.add(event.getStartDate());
        } else if (event.getRecurrence().equals("multi-day")) {
            LocalDate current = event.getStartDate();
            while (!current.isAfter(event.getEndDate())) {
                dates.add(current);
                current = current.plusDays(1);
            }
        } else if (event.getRecurrence().equals("weekly")) {
            LocalDate current = event.getStartDate();
            while (!current.isAfter(event.getEndDate())) {
                String dayName = current.getDayOfWeek().toString();
                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1).toLowerCase();
                if (event.getSelectedWeekdays().contains(dayName)) {
                    dates.add(current);
                }
                current = current.plusDays(1);
            }
            if (dates.isEmpty()) {
                dates.add(event.getStartDate());
            }
        }

        return dates;
    }

    private List<Seat> generateSeats(Venue venue, List<User> users) {
        List<Seat> seats = new ArrayList<>();
        Map<String, Object> seatingLayout = parseSeatingLayout(venue.getSeatingLayout());
        String[] statuses = { "AVAILABLE", "BOOKED" };

        for (Map.Entry<String, Object> entry : seatingLayout.entrySet()) {
            if (entry.getKey().equals("Platinum") || entry.getKey().equals("Gold") || entry.getKey().equals("Silver")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sectionData = (Map<String, Object>) entry.getValue();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rows = (List<Map<String, Object>>) sectionData.get("rows");

                for (Map<String, Object> row : rows) {
                    @SuppressWarnings("unchecked")
                    List<String> seatIds = (List<String>) row.get("seats");

                    for (String seatId : seatIds) {
                        Seat seat = new Seat();
                        seat.setSeatId(seatId);
                        seat.setSection(entry.getKey());
                        String status = statuses[random.nextInt(statuses.length)];
                        seat.setStatus(SeatStatus.valueOf(status));

                        if (status.equals("BOOKED") && !users.isEmpty()) {
                            User randomUser = users.get(random.nextInt(users.size()));
                            seat.setUser(randomUser);
                        }

                        seats.add(seat);
                    }
                }
            }
        }

        return seats;
    }

    private Map<String, Object> parseSeatingLayout(String layoutJson) {
        try {
            JsonNode node = mapper.readTree(layoutJson);
            return mapper.convertValue(node, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Session.Ticket> generateTickets(Map<String, Object> seatingLayout) {
        List<Session.Ticket> tickets = new ArrayList<>();

        for (String section : Arrays.asList("Platinum", "Gold", "Silver")) {
            if (seatingLayout.containsKey(section)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sectionData = (Map<String, Object>) seatingLayout.get(section);
                int sectionCapacity = (Integer) sectionData.get("sectionCapacity");

                Session.Ticket ticket = new Session.Ticket();
                ticket.setType(section);
                ticket.setPrice((double) (75 + random.nextInt(1925))); // 75-2000
                ticket.setAvailable(sectionCapacity);
                ticket.setTotalSeats(sectionCapacity);
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    // Inner classes for data structures

    private static class CityData {
        String city;
        String state;
        double lng;
        double lat;

        CityData(String city, String state, double lng, double lat) {
            this.city = city;
            this.state = state;
            this.lng = lng;
            this.lat = lat;
        }
    }

    private static class SeatingLayoutResult {
        String layout;
        int totalCapacity;

        SeatingLayoutResult(String layout, int totalCapacity) {
            this.layout = layout;
            this.totalCapacity = totalCapacity;
        }
    }

    private static class EventDateResult {
        LocalDate startDate;
        LocalDate endDate;
        String status;
        List<String> selectedWeekdays;

        EventDateResult(LocalDate startDate, LocalDate endDate, String status, List<String> selectedWeekdays) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.selectedWeekdays = selectedWeekdays;
        }
    }

    private static class TimeRangeResult {
        String startTime;
        String endTime;

        TimeRangeResult(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private static class OrganizerStats {
        List<Long> hostedIds = new ArrayList<>();
        List<Double> ratings = new ArrayList<>();
    }
}
