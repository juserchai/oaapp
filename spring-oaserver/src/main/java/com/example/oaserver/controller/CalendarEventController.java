package com.example.oaserver.controller;

import com.example.oaserver.model.entity.CalendarEvent;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.CalendarEventRepository;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class CalendarEventController {
    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<CalendarEvent> getAllEvents(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findAllByUserId(userDetails.getId());
    }

    @GetMapping("/date")
    public List<CalendarEvent> getEventsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userDetails.getId(), date, date);
    }

    @GetMapping("/range")
    public List<CalendarEvent> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userDetails.getId(), end, start);
    }

    @GetMapping("/today")
    public List<CalendarEvent> getTodayEvents(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        LocalDate today = LocalDate.now();
        return eventRepository.findTodayEvents(
                userRepository.findById(userDetails.getId()).orElseThrow(), 
                today);
    }

    @GetMapping("/upcoming")
    public List<CalendarEvent> getUpcomingEvents(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return eventRepository.findUpcomingEvents(
                userRepository.findById(userDetails.getId()).orElseThrow(), 
                today, now);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable String id, 
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findByIdAndUserId(id, userDetails.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CalendarEvent createEvent(@Valid @RequestBody CalendarEvent event, 
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        event.setUserId(userDetails.getId());
        return eventRepository.save(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> updateEvent(@PathVariable String id, 
                                                    @Valid @RequestBody CalendarEvent eventDetails,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findByIdAndUserId(id, userDetails.getId())
                .map(event -> {
                    event.setTitle(eventDetails.getTitle());
                    event.setDescription(eventDetails.getDescription());
                    event.setStartDate(eventDetails.getStartDate());
                    event.setEndDate(eventDetails.getEndDate());
                    event.setStartTime(eventDetails.getStartTime());
                    event.setEndTime(eventDetails.getEndTime());
                    event.setLocation(eventDetails.getLocation());
                    event.setEventType(eventDetails.getEventType());
                    event.setPriority(eventDetails.getPriority());
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return eventRepository.findByIdAndUserId(id, userDetails.getId())
                .map(event -> {
                    eventRepository.delete(event);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 