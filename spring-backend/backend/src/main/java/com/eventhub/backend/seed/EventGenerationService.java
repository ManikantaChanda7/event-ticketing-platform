package com.eventhub.backend.seed;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.VenueRepository;
import java.util.List;
import com.eventhub.backend.enums.EventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class EventGenerationService {
    private static final long MIN_ACTIVE_EVENTS = 1000;

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueRepository venueRepository;
    private final SessionRepository sessionRepository;

    private final String[] CATEGORIES = {
            "Music", "Sports", "Comedy", "Theatre", "Workshop",
            "Conference", "Festival", "Exhibition", "Food & Drink", "Technology"
    };

    private final String[] TITLE_PREFIXES = {
            "Grand", "Ultimate", "Annual", "International", "National", "Premium", "Exclusive",
            "Mega", "Super", "Royal", "Elite", "Classic", "Modern", "Digital", "Global"
    };

    private final String[] TITLE_MAIN = {
            "Music Festival", "Tech Summit", "Comedy Night", "Art Exhibition", "Food Carnival",
            "Sports Championship", "Business Conference", "Cultural Celebration", "Charity Gala",
            "Concert Series", "Workshop", "Seminar", "Trade Show", "Networking Event", "Awards Night",
            "Hackathon", "Startup Meetup", "Design Conference", "Film Festival", "Theater Show"
    };

    private final String[] TITLE_SUFFIXES = {
            "2026", "Edition", "Extravaganza", "Experience", "Weekend", "Marathon", "Showcase",
            "Fest", "Celebration", "Gala", "Night", "Day", "Series", "Festival", "Expo"
    };

    private final String[] DESCRIPTION_TEMPLATES = {
            "Join us for an unforgettable experience featuring world-class performers and speakers. This {category} event brings together enthusiasts from all over to celebrate and network.",
            "Experience the best in {category} at this spectacular gathering. Don't miss out on amazing activities, entertainment, and networking opportunities with like-minded individuals.",
            "A must-attend {category} event for professionals and enthusiasts alike. Discover new trends, connect with industry leaders, and create lasting memories.",
            "Celebrate with us at this premier {category} event featuring top talent, exciting activities, and incredible experiences. Perfect for families and friends.",
            "An exciting {category} event showcasing the best talent and innovation. Join thousands of attendees for a day filled with entertainment, learning, and networking."
    };

    private final String[] CITIES = {
            "Mumbai", "Delhi", "Bangalore", "Chennai", "Hyderabad", "Pune", "Kolkata",
            "Ahmedabad", "Jaipur", "Lucknow", "Chandigarh", "Indore", "Nagpur", "Surat"
    };

    private final String[] BANNER_IMAGES = {
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358450/events/sw4dk82igz96lo08kjik.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358465/events/r2qf2esjweclw3orj24h.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1764358577/events/lx0jlz6wf19caz5zq8jw.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1762673015/events/uzf5ykcpr3gwy93invu1.jpg",
            "https://res.cloudinary.com/dgrpyrxrn/image/upload/v1788247070/rdovjl5t9yah8wyhjbd6_duyyyv.jpg"
    };

    private final String[] THUMBNAIL_IMAGES = {
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

    private final String[] LANGUAGES = { "English", "Hindi", "Tamil", "Telugu", "Kannada", "Malayalam" };

    public EventGenerationService(
            EventRepository eventRepository,
            OrganizerRepository organizerRepository,
            VenueRepository venueRepository,
            SessionRepository sessionRepository) {

        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.sessionRepository = sessionRepository;
    }

    private String getRecurrence() {
        double r = Math.random();

        if (r < 0.75)
            return "single";
        if (r < 0.95)
            return "multi-day";

        return "weekly";
    }

    private String getRandomTitle() {
        String prefix = TITLE_PREFIXES[(int) (Math.random() * TITLE_PREFIXES.length)];
        String main = TITLE_MAIN[(int) (Math.random() * TITLE_MAIN.length)];
        String suffix = TITLE_SUFFIXES[(int) (Math.random() * TITLE_SUFFIXES.length)];
        return prefix + " " + main + " " + suffix;
    }

    private String getRandomDescription(String category) {
        String template = DESCRIPTION_TEMPLATES[(int) (Math.random() * DESCRIPTION_TEMPLATES.length)];
        return template.replace("{category}", category);
    }

    private String getCategory() {
        return CATEGORIES[(int) (Math.random() * CATEGORIES.length)];
    }

    private String getRandomBannerImage() {
        return BANNER_IMAGES[(int) (Math.random() * BANNER_IMAGES.length)];
    }

    private String getRandomThumbnailImage() {
        return THUMBNAIL_IMAGES[(int) (Math.random() * THUMBNAIL_IMAGES.length)];
    }

    private int getRandomAgeLimit() {
        return (int) (Math.random() * 18) + 12; // 12 to 30
    }

    private java.util.Set<String> getRandomLanguages() {
        int count = (int) (Math.random() * 2) + 1; // 1 to 2 languages
        java.util.Set<String> langs = new java.util.HashSet<>();
        for (int i = 0; i < count; i++) {
            langs.add(LANGUAGES[(int) (Math.random() * LANGUAGES.length)]);
        }
        return langs;
    }

    private String getRandomStartTime() {
        int hour = (int) (Math.random() * 12) + 9; // 9 to 20
        return String.format("%02d:00", hour);
    }

    private String getRandomEndTime(String startTime) {
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int duration = (int) (Math.random() * 4) + 2; // 2 to 5 hours
        int endHour = startHour + duration;
        return String.format("%02d:00", endHour);
    }

    private double getRandomPrice() {
        double price = (Math.random() * 2000) + 99; // 99 to 2099
        return Math.round(price * 100.0) / 100.0; // Round to 2 decimal places
    }

    @Transactional
    public void generateDailyEvents() {
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.UPCOMING
                        || event.getStatus() == EventStatus.ONGOING)
                .count();
        long completedEvents = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                .count();

        System.out.println("========================================");
        System.out.println("EVENT GENERATION REPORT");
        System.out.println("========================================");
        System.out.println("Total events in database: " + totalEvents);
        System.out.println("Active events (UPCOMING + ONGOING): " + activeEvents);
        System.out.println("Completed events: " + completedEvents);
        System.out.println("Target active events: " + MIN_ACTIVE_EVENTS);
        System.out.println("Gap to target: " + (MIN_ACTIVE_EVENTS - activeEvents));
        System.out.println("========================================");

        if (activeEvents >= MIN_ACTIVE_EVENTS) {
            System.out.println("Active event target already satisfied. Skipping generation.");
            System.out.println("========================================");
            return;
        }

        List<Organizer> organizers = organizerRepository.findAll();
        List<Venue> venues = venueRepository.findAll();

        System.out.println("Available organizers: " + organizers.size());
        System.out.println("Available venues: " + venues.size());

        if (organizers.isEmpty() || venues.isEmpty()) {
            System.out.println("ERROR: No organizers or venues found. Skipping generation.");
            System.out.println("========================================");
            return;
        }

        int eventsToGenerate = (int) (MIN_ACTIVE_EVENTS - activeEvents);

        System.out.println("Events to generate this run: " + eventsToGenerate);
        System.out.println("========================================");

        for (int i = 0; i < eventsToGenerate; i++) {
            Organizer organizer = organizers.get((int) (Math.random() * organizers.size()));
            Venue venue = venues.get((int) (Math.random() * venues.size()));

            String recurrence = getRecurrence();
            LocalDate startDate = LocalDate.now().plusDays(7 + (int) (Math.random() * 53));

            Event event = new Event();

            String category = getCategory();
            event.setTitle(getRandomTitle());
            event.setDescription(getRandomDescription(category));
            event.setCategory(category);

            event.setOrganizer(organizer);
            event.setVenue(venue);

            event.setStartDate(startDate);
            if ("multi-day".equals(recurrence)) {
                event.setEndDate(startDate.plusDays(ThreadLocalRandom.current().nextInt(2, 6)));
            } else if ("weekly".equals(recurrence)) {
                event.setEndDate(startDate.plusDays(ThreadLocalRandom.current().nextInt(14, 31)));
            } else {
                event.setEndDate(startDate);
            }

            String startTime = getRandomStartTime();
            event.setStartTime(startTime);
            event.setEndTime(getRandomEndTime(startTime));
            event.setRecurrence(recurrence);

            if ("weekly".equals(recurrence)) {
                Set<String> weekdays = new HashSet<>();
                weekdays.add(startDate.getDayOfWeek().name().substring(0, 1)
                        + startDate.getDayOfWeek().name().substring(1).toLowerCase());
                event.setSelectedWeekdays(weekdays);
            }

            event.setStatus(EventStatus.UPCOMING);

            event.setLocationLabel(venue.getName() + ", " + venue.getCity());
            event.setLocationLatitude(venue.getLatitude());
            event.setLocationLongitude(venue.getLongitude());
            event.setLocationType("Point");

            event.setStartingPrice(0.0); // Will be calculated from session tickets
            event.setInterestedUsers(0);

            // Add all event details matching seed data
            event.setBannerImage(getRandomBannerImage());
            event.setThumbnailImage(getRandomThumbnailImage());
            event.setAgeLimit(getRandomAgeLimit());
            event.setLanguages(getRandomLanguages());
            event.setIsFeatured(Math.random() < 0.2); // 20% chance to be featured

            Event savedEvent = eventRepository.save(event);

            int sessionCount = 0;
            List<Session> sessions = new ArrayList<>();
            if ("weekly".equals(recurrence)) {

                for (LocalDate sessionDate = startDate; !sessionDate
                        .isAfter(savedEvent.getEndDate()); sessionDate = sessionDate.plusWeeks(1)) {

                    Session session = buildSession(savedEvent, venue, sessionDate);
                    sessionRepository.save(session);
                    sessions.add(session);
                    sessionCount++;
                }

            } else if ("multi-day".equals(recurrence)) {

                for (LocalDate sessionDate = startDate; !sessionDate
                        .isAfter(savedEvent.getEndDate()); sessionDate = sessionDate.plusDays(1)) {

                    Session session = buildSession(savedEvent, venue, sessionDate);
                    sessionRepository.save(session);
                    sessions.add(session);
                    sessionCount++;
                }

            } else {

                Session session = buildSession(savedEvent, venue, startDate);
                sessionRepository.save(session);
                sessions.add(session);
                sessionCount++;
            }

            // Calculate starting price from session tickets (matching SeedData logic)
            double minPrice = sessions.stream()
                    .flatMap(s -> s.getTickets().stream())
                    .mapToDouble(Session.Ticket::getPrice)
                    .min()
                    .orElse(0.0);
            savedEvent.setStartingPrice(minPrice);
            eventRepository.save(savedEvent);

            System.out.println("Event " + (i + 1) + "/" + eventsToGenerate + " created:");
            System.out.println("  - Title: " + savedEvent.getTitle());
            System.out.println("  - Category: " + savedEvent.getCategory());
            System.out.println("  - Date: " + savedEvent.getStartDate() + " to " + savedEvent.getEndDate());
            System.out.println("  - Time: " + savedEvent.getStartTime() + " - " + savedEvent.getEndTime());
            System.out.println("  - Recurrence: " + recurrence);
            System.out.println("  - Sessions: " + sessionCount);
            System.out.println("  - Price: ₹" + savedEvent.getStartingPrice());
            System.out.println("  - Featured: " + savedEvent.getIsFeatured());
            System.out.println("  - Age Limit: " + savedEvent.getAgeLimit());
            System.out.println("  - Languages: " + savedEvent.getLanguages());
        }

        System.out.println("========================================");
        System.out.println("GENERATION COMPLETED");
        System.out.println("Generated " + eventsToGenerate + " event(s) with session(s)");
        System.out.println("========================================");
    }

    private Session buildSession(Event event, Venue venue, LocalDate sessionDate) {
        Session session = new Session();
        session.setEvent(event);
        session.setDate(sessionDate);

        String startTime = getRandomStartTime();
        session.setStartTime(startTime);
        session.setEndTime(getRandomEndTime(startTime));

        session.setOccupancy(0);
        session.setReleaseDate(sessionDate.minusDays(14).atStartOfDay());

        Session.Ticket ticket = new Session.Ticket();
        ticket.setType("General");
        ticket.setPrice(getRandomPrice());
        ticket.setAvailable(venue.getCapacity());
        ticket.setTotalSeats(venue.getCapacity());

        java.util.List<Session.Ticket> tickets = new java.util.ArrayList<>();
        tickets.add(ticket);
        session.setTickets(tickets);

        return session;
    }

}