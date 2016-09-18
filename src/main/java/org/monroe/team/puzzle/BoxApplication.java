package org.monroe.team.puzzle;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManagerProvider;
import org.monroe.team.puzzle.core.events.Event;
import org.monroe.team.puzzle.core.events.EventSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoxApplication.class, args)
                .registerShutdownHook();
    }

    //TODO graceful shutdown
    @Bean(destroyMethod = "shutdown")
    public MBassador<Event> bus(EventSubscriber... eventSubscribers) {
        MBassador<Event> mBassador = new MBassador<Event>(
                new BusConfiguration()
                        .addFeature(new Feature.SyncPubSub()
                                .setMetadataReader(new MetadataReader())
                                .setPublicationFactory(new MessagePublication.Factory())
                                .setSubscriptionFactory(new SubscriptionFactory())
                                .setSubscriptionManagerProvider(new SubscriptionManagerProvider()))
                        .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                        .addFeature(Feature.AsynchronousMessageDispatch.Default()));
        for (EventSubscriber eventSubscriber : eventSubscribers) {
            mBassador.subscribe(eventSubscriber);
        }
        return mBassador;
    }

}
