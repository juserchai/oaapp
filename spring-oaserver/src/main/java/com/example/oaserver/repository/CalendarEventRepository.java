package com.example.oaserver.repository;

import com.example.oaserver.model.entity.CalendarEvent;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.EventPriority;
import com.example.oaserver.model.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, String> {
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1 ORDER BY e.startDate ASC, e.startTime ASC")
    List<CalendarEvent> findByCreatorOrderByStartTimeAsc(User creator);
    
    // 新增方法
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1")
    List<CalendarEvent> findAllByUserId(String userId);
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.id = ?1 AND e.userId = ?2")
    Optional<CalendarEvent> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1 AND e.startDate <= ?2 AND e.endDate >= ?3")
    List<CalendarEvent> findAllByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String userId, LocalDate endDate, LocalDate startDate);
    
    // 查找用户创建的或被邀请的事件
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1 OR ?1 MEMBER OF e.attendees ORDER BY e.startDate ASC, e.startTime ASC")
    List<CalendarEvent> findUserEvents(User user);
    
    // 查找指定日期范围内的事件
    @Query("""
        SELECT e FROM CalendarEvent e 
        WHERE (e.userId = ?1 OR ?1 MEMBER OF e.attendees) 
        AND ((e.startDate BETWEEN ?2 AND ?3) 
        OR (e.endDate BETWEEN ?2 AND ?3) 
        OR (e.startDate <= ?2 AND e.endDate >= ?3))
        ORDER BY e.startDate ASC, e.startTime ASC
    """)
    List<CalendarEvent> findEventsBetween(User user, LocalDate start, LocalDate end);
    
    // 查找今天的事件
    @Query("""
        SELECT e FROM CalendarEvent e 
        WHERE (e.userId = ?1 OR ?1 MEMBER OF e.attendees) 
        AND e.startDate = ?2
        ORDER BY e.startTime ASC
    """)
    List<CalendarEvent> findTodayEvents(User user, LocalDate today);
    
    // 查找即将到来的事件
    @Query("""
        SELECT e FROM CalendarEvent e 
        WHERE (e.userId = ?1 OR ?1 MEMBER OF e.attendees) 
        AND (e.startDate > ?2 OR (e.startDate = ?2 AND e.startTime >= ?3))
        ORDER BY e.startDate ASC, e.startTime ASC
    """)
    List<CalendarEvent> findUpcomingEvents(User user, LocalDate today, LocalTime now);
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1 AND e.eventType = ?2 ORDER BY e.startDate ASC, e.startTime ASC")
    List<CalendarEvent> findAllByUserIdAndEventTypeOrderByStartDateAscStartTimeAsc(String userId, EventType eventType);
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.userId = ?1 AND e.priority = ?2 ORDER BY e.startDate ASC")
    List<CalendarEvent> findAllByUserIdAndPriorityOrderByStartDateAsc(String userId, EventPriority priority);
} 