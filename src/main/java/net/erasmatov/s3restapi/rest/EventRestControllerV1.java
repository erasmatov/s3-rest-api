package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.EventDto;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.mapper.EventMapper;
import net.erasmatov.s3restapi.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventRestControllerV1 {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    public Mono<EventDto> createEvent(@RequestBody EventDto dto) {
        EventEntity entity = eventMapper.map(dto);
        return eventService.saveEvent(entity)
                .map(eventMapper::map);
    }

    @GetMapping
    public Flux<EventDto> getEvents() {
        return eventService.getAllEvents()
                .map(eventMapper::map);
    }

    @GetMapping("/{eventId}")
    public Mono<EventDto> getEvent(@PathVariable("eventId") Long eventId) {
        return eventService.getEventById(eventId)
                .map(eventMapper::map);
    }

    @DeleteMapping("/{eventId}")
    public Mono<ResponseEntity<Void>> deleteEvent(@PathVariable("eventId") Long eventId) {
        return eventService.deleteEventById(eventId)
                .map(eventEntity -> ResponseEntity.status(HttpStatus.OK).build());
    }
}
