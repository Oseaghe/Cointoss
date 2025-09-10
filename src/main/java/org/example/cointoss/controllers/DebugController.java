// Create a temporary file like src/main/java/org/example/cointoss/controllers/DebugController.java

import org.springframework.web.bind.annotation.PostMapping;

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