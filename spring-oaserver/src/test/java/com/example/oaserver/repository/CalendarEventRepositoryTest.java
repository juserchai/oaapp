package com.example.oaserver.repository;

import com.example.oaserver.model.entity.CalendarEvent;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.EventType;
import com.example.oaserver.model.enums.EventPriority;
import com.example.oaserver.repository.CalendarEventRepository;
import com.example.oaserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CalendarEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CalendarEventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private CalendarEvent event1;
    private CalendarEvent event2;
    private CalendarEvent event3;
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate dayAfterTomorrow;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        dayAfterTomorrow = today.plusDays(2);
        
        // Create a test user
        user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        entityManager.persist(user);

        // Create test events
        event1 = new CalendarEvent();
        event1.setTitle("Team Meeting");
        event1.setDescription("Weekly team sync");
        event1.setStartDate(today);
        event1.setEndDate(today);
        event1.setStartTime(LocalTime.of(10, 0));
        event1.setEndTime(LocalTime.of(11, 0));
        event1.setLocation("Conference Room A");
        event1.setEventType(EventType.MEETING);
        event1.setPriority(EventPriority.HIGH);
        event1.setUserId(user.getId());
        entityManager.persist(event1);

        event2 = new CalendarEvent();
        event2.setTitle("Project Review");
        event2.setDescription("Monthly project review");
        event2.setStartDate(tomorrow);
        event2.setEndDate(tomorrow);
        event2.setStartTime(LocalTime.of(14, 0));
        event2.setEndTime(LocalTime.of(15, 30));
        event2.setLocation("Conference Room B");
        event2.setEventType(EventType.MEETING);
        event2.setPriority(EventPriority.MEDIUM);
        event2.setUserId(user.getId());
        entityManager.persist(event2);

        event3 = new CalendarEvent();
        event3.setTitle("Project Deadline");
        event3.setDescription("Project submission deadline");
        event3.setStartDate(dayAfterTomorrow);
        event3.setEndDate(dayAfterTomorrow);
        event3.setStartTime(LocalTime.of(17, 0));
        event3.setEndTime(LocalTime.of(18, 0));
        event3.setLocation("Office");
        event3.setEventType(EventType.REMINDER);
        event3.setPriority(EventPriority.HIGH);
        event3.setUserId(user.getId());
        entityManager.persist(event3);

        entityManager.flush();
    }

    @Test
    void testFindAllByUserId() {
        // When
        List<CalendarEvent> events = eventRepository.findAllByUserId(user.getId());

        // Then
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getTitle()).isEqualTo("Team Meeting");
        assertThat(events.get(1).getTitle()).isEqualTo("Project Review");
        assertThat(events.get(2).getTitle()).isEqualTo("Project Deadline");
    }

    @Test
    void testFindByIdAndUserId() {
        // When
        Optional<CalendarEvent> foundEvent = eventRepository.findByIdAndUserId(event1.getId(), user.getId());

        // Then
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getTitle()).isEqualTo("Team Meeting");
    }

    @Test
    void testFindByIdAndUserIdNotFound() {
        // When
        Optional<CalendarEvent> foundEvent = eventRepository.findByIdAndUserId("non-existent-id", user.getId());

        // Then
        assertThat(foundEvent).isEmpty();
    }

    @Test
    void testFindAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual() {
        // When - find events for today
        List<CalendarEvent> todayEvents = eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), today, today);

        // Then
        assertThat(todayEvents).hasSize(1);
        assertThat(todayEvents.get(0).getTitle()).isEqualTo("Team Meeting");

        // When - find events for a date range (today to tomorrow)
        List<CalendarEvent> rangeEvents = eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), tomorrow, today);

        // Then
        assertThat(rangeEvents).hasSize(2);
        assertThat(rangeEvents.get(0).getTitle()).isEqualTo("Team Meeting");
        assertThat(rangeEvents.get(1).getTitle()).isEqualTo("Project Review");
    }

    @Test
    void testFindTodayEvents() {
        // When
        List<CalendarEvent> todayEvents = eventRepository.findTodayEvents(user, today);

        // Then
        assertThat(todayEvents).hasSize(1);
        assertThat(todayEvents.get(0).getTitle()).isEqualTo("Team Meeting");
    }

    @Test
    void testFindUpcomingEvents() {
        // When
        LocalTime currentTime = LocalTime.of(9, 0); // Before the 10:00 meeting
        List<CalendarEvent> upcomingEvents = eventRepository.findUpcomingEvents(user, today, currentTime);

        // Then
        assertThat(upcomingEvents).hasSize(1);
        assertThat(upcomingEvents.get(0).getTitle()).isEqualTo("Team Meeting");

        // When - after the meeting time
        LocalTime afterMeetingTime = LocalTime.of(12, 0);
        upcomingEvents = eventRepository.findUpcomingEvents(user, today, afterMeetingTime);

        // Then - should not include today's meeting
        assertThat(upcomingEvents).hasSize(0);
    }

    @Test
    void testFindAllByUserIdAndEventType() {
        // When
        List<CalendarEvent> meetingEvents = eventRepository.findAllByUserIdAndEventTypeOrderByStartDateAscStartTimeAsc(
                user.getId(), EventType.MEETING);
        List<CalendarEvent> reminderEvents = eventRepository.findAllByUserIdAndEventTypeOrderByStartDateAscStartTimeAsc(
                user.getId(), EventType.REMINDER);

        // Then
        assertThat(meetingEvents).hasSize(2);
        assertThat(meetingEvents.get(0).getTitle()).isEqualTo("Team Meeting");
        assertThat(meetingEvents.get(1).getTitle()).isEqualTo("Project Review");
        
        assertThat(reminderEvents).hasSize(1);
        assertThat(reminderEvents.get(0).getTitle()).isEqualTo("Project Deadline");
    }

    @Test
    void testFindAllByUserIdAndPriority() {
        // When
        List<CalendarEvent> highPriorityEvents = eventRepository.findAllByUserIdAndPriorityOrderByStartDateAsc(
                user.getId(), EventPriority.HIGH);
        List<CalendarEvent> mediumPriorityEvents = eventRepository.findAllByUserIdAndPriorityOrderByStartDateAsc(
                user.getId(), EventPriority.MEDIUM);

        // Then
        assertThat(highPriorityEvents).hasSize(2);
        assertThat(highPriorityEvents.get(0).getTitle()).isEqualTo("Team Meeting");
        assertThat(highPriorityEvents.get(1).getTitle()).isEqualTo("Project Deadline");
        
        assertThat(mediumPriorityEvents).hasSize(1);
        assertThat(mediumPriorityEvents.get(0).getTitle()).isEqualTo("Project Review");
    }
} 