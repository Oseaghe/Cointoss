package org.example.cointoss.controllers;// Create a temporary file like src/main/java/org/example/cointoss/controllers/DebugController.java

import lombok.RequiredArgsConstructor;
import org.example.cointoss.scheduler.GameCycleScheduler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {
    private final GameCycleScheduler scheduler;

    @PostMapping("/trigger-creation")
    public void triggerCreation() {
        scheduler.schedulePoolCreation();
    }

    @PostMapping("/trigger-updates")
    public void triggerUpdates() {
        scheduler.schedulePoolUpdates();
    }
}