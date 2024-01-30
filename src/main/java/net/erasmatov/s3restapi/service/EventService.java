package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.entity.EntityStatus;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.repository.EventRepository;
import net.erasmatov.s3restapi.repository.FileRepository;
import net.erasmatov.s3restapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final EventRepository eventRepository;

    public Mono<EventEntity> getEventById(Long id) {
        return eventRepository.findById(id)
                .flatMap(eventEntity -> Mono.zip(
                        userRepository.findById(eventEntity.getUserId()),
                        fileRepository.findById(eventEntity.getFileId())
                ).map(tuples -> {
                    eventEntity.setUser(tuples.getT1());
                    eventEntity.setFile(tuples.getT2());
                    return eventEntity;
                }));
    }

    public Flux<EventEntity> getEventsByUserId(Long userId) {
        return eventRepository.findAllByUserId(userId);
    }

    public Mono<EventEntity> saveEvent(EventEntity entity) {
        return eventRepository.save(entity);
    }

    public Flux<EventEntity> getAllEvents() {
        return eventRepository.findAll()
                .flatMap(eventEntity -> Flux.zip(
                                userRepository.findAll(),
                                fileRepository.findAll())
                        .map(tuples -> {
                            eventEntity.setUser(tuples.getT1());
                            eventEntity.setFile(tuples.getT2());
                            return eventEntity;
                        })
                );
    }

    public Mono<EventEntity> deleteEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .flatMap(eventEntity -> {
                    eventEntity.setStatus(EntityStatus.INACTIVE);
                    return eventRepository.save(eventEntity);
                });
    }
}
