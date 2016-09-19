package org.monroe.team.puzzle;

import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.core.events.MessageSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.Environment;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;

@SpringBootApplication
public class BoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoxApplication.class, args)
                .registerShutdownHook();
    }

    @Bean
    TaskScheduler taskScheduler(){
        TaskScheduler scheduler = new ThreadPoolTaskScheduler();
        return scheduler;
    }

    @Bean
    Environment env() {
        return Environment.initializeIfEmpty()
                .assignErrorJournal();
    }

    @Bean
    EventBus bus(Environment env, AbstractMessageSubscriber... eventSubscribers) {
        EventBus eventBus = EventBus.create(env, Environment.THREAD_POOL);
        for (AbstractMessageSubscriber eventSubscriber : eventSubscribers) {
            eventBus.getConsumerRegistry().register(
                    Selectors.<Object>$(eventSubscriber.subscriberKey),
                    eventSubscriber
            );
        }

        return eventBus;
    }

}
