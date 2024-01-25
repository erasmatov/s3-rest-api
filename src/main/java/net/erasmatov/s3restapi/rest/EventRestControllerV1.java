package net.erasmatov.s3restapi.rest;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.dto.EventDto;
import net.erasmatov.s3restapi.mapper.EventMapper;
import net.erasmatov.s3restapi.security.CustomPrincipal;
import net.erasmatov.s3restapi.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventRestControllerV1 {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping("/{eventId}")
    public Flux<EventDto> getEvent(Authentication authentication, @PathVariable("eventId") Long eventId) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        if (authentication.getAuthorities().contains("USER")) {
            return eventService.getEventsByUserId(principal.getId())
                    .filter(eventEntity -> eventEntity.getId().equals(eventId))
                    .map(eventMapper::map);
        } else {
            return eventService.getAllEvents()
                    .map(eventMapper::map);
        }
    }

    @GetMapping
    public Flux<EventDto> getEvents() {
        return eventService.getAllEvents()
                .map(eventMapper::map);
    }

    @DeleteMapping("/{eventId}")
    public Mono<ResponseEntity<Void>> deleteEvent(@PathVariable("eventId") Long eventId) {
        return eventService.deleteEventById(eventId)
                .map(eventEntity -> ResponseEntity.status(HttpStatus.OK).build());
    }
}
