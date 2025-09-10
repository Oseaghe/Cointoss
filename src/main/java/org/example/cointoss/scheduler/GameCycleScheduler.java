// src/main/java/org/example/cointoss/scheduler/GameCycleScheduler.java
package org.example.cointoss.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.cointoss.service.BettingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameCycleScheduler {

    private final BettingService bettingService;

    /**
     * This scheduled job creates a new betting pool every 10 minutes.
     */

    @Scheduled(cron = "0 */10 * * * *")
    public void schedulePoolCreation() {
        System.out.println("SCHEDULER: Running job to create a new betting pool...");
        bettingService.createNextPool();
    }

    /**
     * This scheduled job runs every minute to check the status of pools.
     * It looks for pools that need to be locked or settled and processes them.
     * `fixedRate = 60000` means it will run every 60,000 milliseconds (1 minute).
     */
    @Scheduled(fixedRate = 30000)
    public void schedulePoolUpdates() {
        System.out.println("SCHEDULER: Running job to check for pools to lock or settle...");
        bettingService.lockDuePools();
        bettingService.settleDuePools();
    }
}