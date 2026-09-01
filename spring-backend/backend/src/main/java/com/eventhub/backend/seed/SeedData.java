package com.eventhub.backend.seed;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.SeatStatus;
import com.eventhub.backend.enums.BookingStatus;
import com.eventhub.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

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
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private static final Random random = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static long emailCounter = 0;

    // Image pools (using placeholder URLs - replace with actual Cloudinary URLs)
    private static final String[] BANNER_IMAGES = {
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358450/events/sw4dk82igz96lo08kjik.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358465/events/r2qf2esjweclw3orj24h.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358577/events/lx0jlz6wf19caz5zq8jw.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1762673015/events/uzf5ykcpr3gwy93invu1.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1788247070/rdovjl5t9yah8wyhjbd6_duyyyv.jpg"
    };

    private static final String[] THUMBNAIL_IMAGES = {
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757756214/iuiflkqpe4l8hazie0dv.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756886339/udwsn8zsnzebfn0viq9o.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753269077/hlxlycj5weawbwgtu9ku.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753952045/lbwumjip6z5s91nn3hsu.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757415532/oks9ckfcntuudvakxiw5.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756887171/tyavdj29mm3hdtcle7y6.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1750449286/tjli9nctsqmef6d2q1gt.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756110944/k4v176evbapoutmf3q5t.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1752427986/aqnrirnkp8yxwnjxatfa.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753775866/iexwafvevzjg4wlvg8dm.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757753677/eb8ghgkfwxn5k8n2rwbe.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755927576/poqk6cx9ojccxa3d0l3d.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757302023/sykymhhzd1cf2yqba2eh.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757826850/apte7xd5pvdt9impmroj.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755003926/aydchg46fnutxrux4v9u.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755170212/ha3sy5xao8ryc3ndjqjf.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755518637/ailke6ywntn6e2z5bh1h.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758198451/iv1pb8upq7ochnuwyisc.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1754319439/gjzedwmrxfdboi45secc.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753602975/oeapeqrbhczxpowgsyie.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757017632/ugdr6kynuetnhyijxn6j.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756985711/lijx7b7oglhivbcfauvj.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756987926/pr7s3fjv1w5trer0szf2.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757017450/gyonfmmhkj8j3k4ysxol.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1748867860/hwttmqwqohqaenu8gdui.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757500877/kamrp7o4jktxdvrgcdav.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757315858/iapbabyqyelsudt52myu.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753875069/lsg8rbnnoueha90oimma.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1754047645/av0lbalb68xudg9zyri3.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757229923/bv2zruvlquebqgb1xavi.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758096255/scm03kslveqayywiiyry.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757608305/yc9wzt6439dnhf68f4di.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757768632/edyyn1apqz1se9bxw2zq.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230655/rmeyoincw81xksoith48.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757868601/plhvv4nnubb8askey1lk.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756789034/b7grxisnifmudpq8keyo.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1744713264/zuekrtlbgwhsoecsrogh.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756298976/azaieah37fca0jr8pujd.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230561/ntlgjunedcljttragzvn.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757229973/ah6wldxqb02caojvot5z.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757324953/fw98n6zsegrfsy9flqbx.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1744029211/ewqhlivlbo7fnmfnjipv.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757229849/rqws7chvyd2oif408ha1.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757005182/of4qdz6ts6kllxyp20dn.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755583975/i2jrubtjjelqzouw7tk7.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755491134/hgclnn1vhccfexg3zicr.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1748859931/dljaxi11jlhpynilsa37.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757180437/avrljdszipyamfu3vhgg.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758274925/n00n1on4pxntyzpxs63o.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756102753/r4zlbjozh0wbjigob3pm.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756730403/xzy3byovt0a531omoy7w.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758285885/dnutxnbc3mezigodhzvx.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230717/dnurs0hyog4kj8qmjma9.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758031120/gf7dbgoxvvb2blzbrqpj.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757493457/sy0steor6e6wakastpwe.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757155694/dmmvrrg0trbo3fkslkcz.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757426064/vjctihndku4w5ytmzvmv.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230345/dnlv4xcppqoofy6j5bd6.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757408872/domrvqzmuk1py1ji3qkw.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757504539/pnuozfxgw9m04swbwypr.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757151138/ukzd2iix7pzqpuy7ka6f.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756963991/kf0ihotmukpfdobpgafh.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757326320/rpkdszlzjs0cth0vdq64.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757487536/jrmhl3w5qac5bxbmgjwp.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758266778/nafzdx9v5qswwvnvi0do.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755790589/vniwfzyzsdtje9epg939.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758219313/azwzcftlgicuwenhljej.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757528564/rdybk9lxxkjnopr5n6uu.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1754899053/i4qvf1kjwsidtsdnjqke.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755336167/mxvimd3fy0jernzyb7qq.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757956098/ld4mumddi1sngslznizi.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1753515141/ioutwp76g1mpubg6d1ki.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757476349/sbhpyo7o2msduc6jkfko.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757133280/efybcz6c4nzcrscbww3f.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757446746/f0kegazwltyy9cxedr1q.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757586892/pexkx9vxrsrzogkavonv.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1741943906/o5piw4srw6dm00h6rh1o.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758138401/zqcllr4uxwufsfpl87ci.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757572590/pwzcok7hsvwjm7p4bcwa.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756383612/somhe5c2rbay1efuost2.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758277332/ya4xnmphikagu2rw6vx8.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757409327/e56tyxw5zygaylosgaiu.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1741942069/xkjat9t3gxie5igsb9v5.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757503857/tk907rjlni22syceyuxs.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757940598/a4pj15jw88jxtfdou7gd.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758280883/zu9f4nvwmjb9jptkjrhs.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758291570/dpj12jzhldpqnfrskuwc.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758201425/xj9fsl2n7cgbm8nreg78.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757582564/rx6m5xyvjc2yrvcduo7u.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230849/tuipk5dc26gskqciyhxq.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756973505/c4ao2tegr9wdjxszsan7.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755337422/oz1n9xrbzjhl0kcxha7v.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757413126/hsihxhvgw75oz5rujl9u.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1750272202/pgppqv5xcqbzjdc6ji7s.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1755182670/jtjiwahoy9hcmyobup1g.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757304342/fswfrb3rvuowp3wfkp7m.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758106626/d59mfqi4wv0iwjpybjkn.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758055041/hmfuilkcr6zjkxwklksy.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757744030/bib7ixbknmtlqurfwur7.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757665099/dguw69grva6bycpjxpxs.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757230102/kkbuwgw46ur0pehallkw.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1758176398/jxfaw27vkggfyndwrgx8.jpg",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1754774154/mpbsphe9xaawrfkmmc2j.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1757309416/wwc5hghufjurw7s4mpg2.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1744032287/uankoaxbgdswihixypqh.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1752320027/zwlatvsemxliv7on5aln.png",
            "https://media.insider.in/image/upload/c_crop,g_custom/v1756366304/dcbyzlcf4fknj7i7apjw.png"
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
            BookingRepository bookingRepository,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager) {
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.reviewRepository = reviewRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // This intentionally resets the local/demo DB and recreates the seed dataset.
        // seedDatabase();
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

        // 9. Seed bookings
        seedBookings();

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

        entityManager.createNativeQuery("""
                    TRUNCATE TABLE
                    bookings,
                    reviews,
                    seats,
                    sessions,
                    events,
                    venues,
                    organizers,
                    users
                    RESTART IDENTITY CASCADE
                """).executeUpdate();

        System.out.println("Existing data cleared");
    }

    private void seedUsers(String password) {
        System.out.println("Seeding users...");
        int totalUsers = 30000;
        int batchSize = 50;
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
        int totalEvents = 2000;

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

        Map<Long, Double> minimumPriceByEventId = new HashMap<>();
        List<Session> allSessions = sessionRepository.findAllWithTickets();

        for (Session session : allSessions) {
            if (session.getEvent() == null || session.getTickets() == null || session.getTickets().isEmpty()) {
                continue;
            }

            long eventId = session.getEvent().getId();
            double sessionMinPrice = session.getTickets().stream()
                    .mapToDouble(Session.Ticket::getPrice)
                    .min()
                    .orElse(0.0);

            minimumPriceByEventId.merge(eventId, sessionMinPrice, Math::min);
        }

        List<Event> eventsToUpdate = events.stream()
                .map(event -> {
                    double minPrice = minimumPriceByEventId.getOrDefault(event.getId(), 0.0);
                    event.setStartingPrice(minPrice);
                    return event;
                })
                .collect(Collectors.toList());

        eventRepository.saveAll(eventsToUpdate);
        System.out.println("Starting prices updated for " + eventsToUpdate.size() + " events");
    }

    private void updateOrganizerStats() {
        System.out.println("Updating organizer stats...");
        Map<Long, OrganizerStats> organizerStatsMap = new HashMap<>();

        // Fetch all reviews in one query with event loaded
        List<Review> allReviews = reviewRepository.findAll();
        Map<Long, List<Review>> reviewsByEventId = allReviews.stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));

        for (Event event : events) {
            Long organizerId = event.getOrganizer().getId();
            OrganizerStats stats = organizerStatsMap.computeIfAbsent(organizerId, k -> new OrganizerStats());
            stats.hostedIds.add(event.getId());

            List<Review> eventReviews = reviewsByEventId.getOrDefault(event.getId(), List.of());
            for (Review review : eventReviews) {
                stats.ratings.add((double) review.getRating());
            }
        }

        // Create map of event by ID for fast lookup
        Map<Long, Event> eventById = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<Organizer> organizersToUpdate = new ArrayList<>();
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
                Set<Event> hostedEventSet = stats.hostedIds.stream()
                        .map(eventById::get)
                        .collect(Collectors.toSet());
                organizer.setEventsHosted(hostedEventSet);
                organizersToUpdate.add(organizer);
            }
        }

        organizerRepository.saveAll(organizersToUpdate);
        System.out.println("Organizer stats updated");
    }

    private void updateUserInterests() {
        System.out.println("Updating user interests...");
        Map<Long, Integer> eventInterestCount = new HashMap<>();
        Map<Long, Event> eventById = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        int totalUsers = users.size();
        int count = 0;
        List<User> usersToUpdate = new ArrayList<>();

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
            user.setInterestedEvents(new HashSet<>(interestedEvents));
            usersToUpdate.add(user);

            for (Event event : interestedEvents) {
                eventInterestCount.put(event.getId(), eventInterestCount.getOrDefault(event.getId(), 0) + 1);
            }
        }

        userRepository.saveAll(usersToUpdate);
        System.out.println("Processed " + totalUsers + " users");

        System.out.println("Updating event interest counts...");
        List<Event> eventsToUpdate = events.stream()
                .map(event -> {
                    event.setInterestedUsers(eventInterestCount.getOrDefault(event.getId(), 0));
                    return event;
                })
                .collect(Collectors.toList());

        eventRepository.saveAll(eventsToUpdate);
        System.out.println("User interests updated for " + totalUsers + " users and " + events.size() + " events");
    }

    private void seedBookings() {
        System.out.println("Seeding bookings...");
        List<Booking> bookings = new ArrayList<>();
        int batchSize = 500;
        int bookingCount = 0;

        // Get all sessions
        List<Session> allSessions = sessionRepository.findAll();
        System.out.println("Found " + allSessions.size() + " sessions");

        // Fetch all seats in one query with session and user loaded
        List<Seat> allSeats = seatRepository.findAll();
        Map<Long, List<Seat>> seatsBySessionId = allSeats.stream()
                .collect(Collectors.groupingBy(s -> s.getSession().getId()));

        for (Session session : allSessions) {
            // Skip sessions that are too far in the future or too old
            if (session.getDate().isAfter(LocalDate.now().plusDays(90)) ||
                    session.getDate().isBefore(LocalDate.now().minusDays(365))) {
                continue;
            }

            Event event = session.getEvent();

            // Get seats for this session that are already booked
            List<Seat> bookedSeats = seatsBySessionId.getOrDefault(session.getId(), List.of()).stream()
                    .filter(s -> s.getUser() != null && s.getStatus() == SeatStatus.BOOKED)
                    .collect(Collectors.toList());

            if (bookedSeats.isEmpty()) {
                continue;
            }

            // Group booked seats by user to create bookings
            Map<User, List<Seat>> seatsByUser = bookedSeats.stream()
                    .collect(Collectors.groupingBy(Seat::getUser));

            for (Map.Entry<User, List<Seat>> entry : seatsByUser.entrySet()) {
                User user = entry.getKey();
                List<Seat> userSeats = entry.getValue();

                // Create booking
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setEvent(event);
                booking.setSession(session);
                booking.setStatus(BookingStatus.CONFIRMED);

                // Create booked seats list
                List<Booking.BookedSeat> bookedSeatList = new ArrayList<>();
                double totalAmount = 0;

                for (Seat seat : userSeats) {
                    Booking.BookedSeat bookedSeat = new Booking.BookedSeat();
                    bookedSeat.setSeatId(seat.getSeatId());
                    bookedSeat.setSection(seat.getSection());

                    double seatPrice = seat.getPrice() != null ? seat.getPrice() : 0.0;

                    bookedSeat.setPrice(java.math.BigDecimal.valueOf(seatPrice));
                    bookedSeatList.add(bookedSeat);
                    totalAmount += seatPrice;
                }

                booking.setSeats(bookedSeatList);
                booking.setTotalAmount(java.math.BigDecimal.valueOf(totalAmount));

                // Create ticket summary from session tickets
                List<Booking.TicketSummary> ticketSummaryList = new ArrayList<>();
                if (session.getTickets() != null) {
                    for (Session.Ticket ticket : session.getTickets()) {
                        int seatsForType = (int) userSeats.stream()
                                .filter(s -> s.getSection().equals(ticket.getType()))
                                .count();
                        if (seatsForType > 0) {
                            Booking.TicketSummary summary = new Booking.TicketSummary();
                            summary.setType(ticket.getType());
                            summary.setQuantity(seatsForType);
                            summary.setTotalPrice(java.math.BigDecimal.valueOf(seatsForType * ticket.getPrice()));
                            ticketSummaryList.add(summary);
                        }
                    }
                }
                booking.setTicketsSummary(ticketSummaryList);

                bookings.add(booking);
                bookingCount++;

                if (bookings.size() >= batchSize) {
                    bookingRepository.saveAll(bookings);
                    bookings.clear();
                    System.out.println("Created " + bookingCount + " bookings so far");
                }
            }
        }

        // Save remaining bookings
        if (!bookings.isEmpty()) {
            bookingRepository.saveAll(bookings);
        }

        System.out.println("Bookings seeded: " + bookingCount);
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

        Set<Long> featuredEventIds = new HashSet<>();

        // Featured per location
        for (List<Event> list : byLocation.values()) {
            int featuredCount = Math.min(15, Math.max(5, (int) (list.size() * 0.1)));
            List<Event> selected = randomItems(list, featuredCount);
            for (Event event : selected) {
                featuredEventIds.add(event.getId());
            }
        }

        // Featured per location + category
        for (List<Event> list : byLocationCategory.values()) {
            int featuredCount = Math.min(4, Math.max(2, (int) (list.size() * 0.15)));
            List<Event> selected = randomItems(list, featuredCount);
            for (Event event : selected) {
                featuredEventIds.add(event.getId());
            }
        }

        // Batch update featured events
        List<Event> eventsToUpdate = events.stream()
                .map(event -> {
                    if (featuredEventIds.contains(event.getId())) {
                        event.setIsFeatured(true);
                    }
                    return event;
                })
                .filter(Event::getIsFeatured) // only save the ones that are featured
                .collect(Collectors.toList());

        if (!eventsToUpdate.isEmpty()) {
            eventRepository.saveAll(eventsToUpdate);
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
                        if ("Platinum".equals(entry.getKey())) {
                            seat.setPrice(2000.0);
                        } else if ("Gold".equals(entry.getKey())) {
                            seat.setPrice(1200.0);
                        } else {
                            seat.setPrice(600.0);
                        }
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
