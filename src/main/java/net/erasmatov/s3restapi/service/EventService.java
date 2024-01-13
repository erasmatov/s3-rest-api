package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.repository.EventRepository;
import net.erasmatov.s3restapi.repository.FileRepository;
import net.erasmatov.s3restapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    public Flux<EventEntity> findAllEvents() {
        return eventRepository.findAll();
    }

    public Mono<EventEntity> findEventById(Long id) {
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

    public Mono<List<EventEntity>> findEventsByUserId(Long userId) {
        return eventRepository.findEventEntitiesByUserId(userId)
                .collectList();
    }

    public Mono<EventEntity> updateEvent(EventEntity entity) {
        return null;
    }

    public Mono<Void> deleteEventById(Long id) {
        return eventRepository.deleteById(id);
    }

    public Mono<EventEntity> saveEvent(EventEntity entity) {
        return eventRepository.save(entity);
    }

}

