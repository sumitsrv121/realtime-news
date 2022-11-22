package com.sumit.srv.relatimenews.controller;

import com.github.javafaker.Faker;
import com.sumit.srv.relatimenews.model.LatestNews;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class NewsController {
    //private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    @Autowired
    private Faker faker;

    // method for client subscription
    @CrossOrigin
    @RequestMapping(value = "/subscribe", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribe(@RequestParam("userId") Long userId) {
        SseEmitter emitter = new SseEmitter((long) (60000 * 10));
        sendInitEvent(emitter);

        log.info("Adding a emitter for subscription, current user base is {}", emitters.size());
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }

    private SseEmitter sendInitEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }

    // method for dispatching events to all the clients
    @PostMapping(value = "/dispatchEvent")
    public void dispatchEventToAllClient(@RequestBody String freshNews) {
        log.info("Got a dummy news from the microservice {}", freshNews);
        String title = faker.chuckNorris().fact();
        String text = faker.buffy().quotes();
        for (SseEmitter emitter : emitters.values()) {
            try {
                log.info("Processing and sending news.......");
                emitter.send(SseEmitter.event().name("latestNews").data(new LatestNews(title, text)));
            } catch (IOException e) {
                log.info("Emitter is not in our list, removing it");
                emitters.remove(emitter);
            }
        }
    }

    // method for dispatching events to all the clients
    @PostMapping(value = "/dispatchEventToUser")
    public void dispatchEventToSpecificClient(@RequestBody String userId) {
        log.info("Got a dummy news from the microservice for user with id {}", userId);
        String title = faker.chuckNorris().fact();
        String text = faker.buffy().quotes();
        SseEmitter sseEmitter = null;
        boolean hasErrorOccurred = true;
        Long userID = null;
        if (userId != null) {
            userID = Long.valueOf(userId);
            if (emitters.containsKey(userID)) {
                log.info("User is valid....Trying to send the message");
                sseEmitter = emitters.get(userID);
                hasErrorOccurred = false;
            }
        }

        if (hasErrorOccurred) {
            log.error("No option can be performed as, we don't have any data to a user with id {}", userId);
            return;
        }

        try {
            log.info("Processing and sending news.......");
            sseEmitter.send(SseEmitter.event().name("latestNews").data(new LatestNews(title, text)));
        } catch (IOException e) {
            log.info("Emitter is not in our list, removing it");
            emitters.remove(userID);
        }
    }
}
