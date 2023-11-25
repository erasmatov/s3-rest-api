package net.erasmatov.s3restapi.service;

import lombok.RequiredArgsConstructor;
import net.erasmatov.s3restapi.entity.EventEntity;
import net.erasmatov.s3restapi.repository.EventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final FileService fileService;

    public Mono<EventEntity> findEventById(Long id) {
        return eventRepository.findById(id)
                .flatMap(eventEntity -> Mono.zip(userService.findUserById(eventEntity.getUserId()),
                                fileService.findFileById(eventEntity.getFileId()))
                        .map(tuples -> {
                            eventEntity.setUser(tuples.getT1());
                            eventEntity.setFile(tuples.getT2());
                            return eventEntity;
                        }));
    }
}

