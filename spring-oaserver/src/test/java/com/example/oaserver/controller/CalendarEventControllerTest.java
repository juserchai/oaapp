package com.example.oaserver.controller;

import com.example.oaserver.model.entity.CalendarEvent;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.EventType;
import com.example.oaserver.model.enums.EventPriority;
import com.example.oaserver.repository.CalendarEventRepository;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarEventController.class)
public class CalendarEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalendarEventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    private UserDetailsImpl userDetails;
    private User user;
    private CalendarEvent event1;
    private CalendarEvent event2;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        // Create a mock user
        userDetails = new UserDetailsImpl(
            "user1",             // id
            "testuser",          // username
            "password",          // password
            "Test User",         // name
            "test@example.com",  // email
            "USER",              // role
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
        );
        
        user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setName("Test User");
        
        today = LocalDate.now();
        
        // Create test events
        event1 = new CalendarEvent();
        event1.setId("event1");
        event1.setTitle("Team Meeting");
        event1.setDescription("Weekly team sync");
        event1.setStartDate(today);
        event1.setEndDate(today);
        event1.setStartTime(LocalTime.of(10, 0));
        event1.setEndTime(LocalTime.of(11, 0));
        event1.setLocation("Conference Room A");
        event1.setEventType(EventType.MEETING);
        event1.setPriority(EventPriority.HIGH);
        event1.setUserId("user1");
        
        event2 = new CalendarEvent();
        event2.setId("event2");
        event2.setTitle("Project Review");
        event2.setDescription("Monthly project review");
        event2.setStartDate(today.plusDays(1));
        event2.setEndDate(today.plusDays(1));
        event2.setStartTime(LocalTime.of(14, 0));
        event2.setEndTime(LocalTime.of(15, 30));
        event2.setLocation("Conference Room B");
        event2.setEventType(EventType.MEETING);
        event2.setPriority(EventPriority.MEDIUM);
        event2.setUserId("user1");
    }

    @Test
    void testGetAllEvents() throws Exception {
        List<CalendarEvent> events = Arrays.asList(event1, event2);
        
        when(eventRepository.findAllByUserId("user1")).thenReturn(events);
        
        mockMvc.perform(get("/api/events")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("event1")))
                .andExpect(jsonPath("$[1].id", is("event2")));
                
        verify(eventRepository).findAllByUserId("user1");
    }

    @Test
    void testGetEventsByDate() throws Exception {
        List<CalendarEvent> events = Arrays.asList(event1);
        
        when(eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("user1"), any(LocalDate.class), any(LocalDate.class))).thenReturn(events);
        
        mockMvc.perform(get("/api/events/date")
                .param("date", today.toString())
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("event1")));
                
        verify(eventRepository).findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("user1"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testGetEventsByDateRange() throws Exception {
        List<CalendarEvent> events = Arrays.asList(event1, event2);
        
        when(eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("user1"), any(LocalDate.class), any(LocalDate.class))).thenReturn(events);
        
        mockMvc.perform(get("/api/events/range")
                .param("start", today.toString())
                .param("end", today.plusDays(1).toString())
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
                
        verify(eventRepository).findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("user1"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testGetTodayEvents() throws Exception {
        List<CalendarEvent> events = Arrays.asList(event1);
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(eventRepository.findTodayEvents(eq(user), any(LocalDate.class))).thenReturn(events);
        
        mockMvc.perform(get("/api/events/today")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("event1")));
                
        verify(userRepository).findById("user1");
        verify(eventRepository).findTodayEvents(eq(user), any(LocalDate.class));
    }

    @Test
    void testGetUpcomingEvents() throws Exception {
        List<CalendarEvent> events = Arrays.asList(event2);
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(eventRepository.findUpcomingEvents(eq(user), any(LocalDate.class), any(LocalTime.class))).thenReturn(events);
        
        mockMvc.perform(get("/api/events/upcoming")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("event2")));
                
        verify(userRepository).findById("user1");
        verify(eventRepository).findUpcomingEvents(eq(user), any(LocalDate.class), any(LocalTime.class));
    }

    @Test
    void testGetEventById() throws Exception {
        when(eventRepository.findByIdAndUserId("event1", "user1")).thenReturn(Optional.of(event1));
        
        mockMvc.perform(get("/api/events/event1")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("event1")))
                .andExpect(jsonPath("$.title", is("Team Meeting")));
                
        verify(eventRepository).findByIdAndUserId("event1", "user1");
    }

    @Test
    void testCreateEvent() throws Exception {
        CalendarEvent newEvent = new CalendarEvent();
        newEvent.setTitle("Project Kickoff");
        newEvent.setDescription("New project kickoff meeting");
        newEvent.setStartDate(today.plusDays(3));
        newEvent.setEndDate(today.plusDays(3));
        newEvent.setStartTime(LocalTime.of(9, 0));
        newEvent.setEndTime(LocalTime.of(10, 0));
        newEvent.setLocation("Conference Room C");
        newEvent.setEventType(EventType.MEETING);
        newEvent.setPriority(EventPriority.HIGH);
        
        CalendarEvent savedEvent = new CalendarEvent();
        savedEvent.setId("event3");
        savedEvent.setTitle("Project Kickoff");
        savedEvent.setDescription("New project kickoff meeting");
        savedEvent.setStartDate(today.plusDays(3));
        savedEvent.setEndDate(today.plusDays(3));
        savedEvent.setStartTime(LocalTime.of(9, 0));
        savedEvent.setEndTime(LocalTime.of(10, 0));
        savedEvent.setLocation("Conference Room C");
        savedEvent.setEventType(EventType.MEETING);
        savedEvent.setPriority(EventPriority.HIGH);
        savedEvent.setUserId("user1");
        
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(savedEvent);
        
        mockMvc.perform(post("/api/events")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("event3")))
                .andExpect(jsonPath("$.title", is("Project Kickoff")));
                
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testUpdateEvent() throws Exception {
        CalendarEvent eventUpdate = new CalendarEvent();
        eventUpdate.setTitle("Updated Meeting");
        eventUpdate.setDescription("Updated description");
        eventUpdate.setStartDate(today);
        eventUpdate.setEndDate(today);
        eventUpdate.setStartTime(LocalTime.of(11, 0));
        eventUpdate.setEndTime(LocalTime.of(12, 0));
        eventUpdate.setLocation("Updated Location");
        eventUpdate.setEventType(EventType.MEETING);
        eventUpdate.setPriority(EventPriority.LOW);
        
        CalendarEvent updatedEvent = new CalendarEvent();
        updatedEvent.setId("event1");
        updatedEvent.setTitle("Updated Meeting");
        updatedEvent.setDescription("Updated description");
        updatedEvent.setStartDate(today);
        updatedEvent.setEndDate(today);
        updatedEvent.setStartTime(LocalTime.of(11, 0));
        updatedEvent.setEndTime(LocalTime.of(12, 0));
        updatedEvent.setLocation("Updated Location");
        updatedEvent.setEventType(EventType.MEETING);
        updatedEvent.setPriority(EventPriority.LOW);
        updatedEvent.setUserId("user1");
        
        when(eventRepository.findByIdAndUserId("event1", "user1")).thenReturn(Optional.of(event1));
        when(eventRepository.save(any(CalendarEvent.class))).thenReturn(updatedEvent);
        
        mockMvc.perform(put("/api/events/event1")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("event1")))
                .andExpect(jsonPath("$.title", is("Updated Meeting")))
                .andExpect(jsonPath("$.location", is("Updated Location")));
                
        verify(eventRepository).findByIdAndUserId("event1", "user1");
        verify(eventRepository).save(any(CalendarEvent.class));
    }

    @Test
    void testDeleteEvent() throws Exception {
        when(eventRepository.findByIdAndUserId("event1", "user1")).thenReturn(Optional.of(event1));
        
        mockMvc.perform(delete("/api/events/event1")
                .with(user(userDetails)))
                .andExpect(status().isOk());
                
        verify(eventRepository).findByIdAndUserId("event1", "user1");
        verify(eventRepository).delete(any(CalendarEvent.class));
    }
} 