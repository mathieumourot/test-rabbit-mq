package fr.mathieumourot;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.rabbitmq.RabbitMQClient;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Path("/hello")
public class ExampleResource {

    RabbitMQClient rabbitMQClient;

    @Inject
    ExampleResource(Vertx vertx) {
        this.rabbitMQClient = RabbitMQClient.create(vertx);
        rabbitMQClient.confirmSelect();
    }

    @OnOverflow(value = OnOverflow.Strategy.NONE, bufferSize = 1L)
    @Inject
    @Channel("testexchange")
    MutinyEmitter<String> testExchangeEmitter;

    Logger logger = Logger.getLogger(this.getClass().getName());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        String uuid = UUID.randomUUID().toString();
        logger.info("Sending message: " + uuid);
        testExchangeEmitter.send(uuid).await().atMost(Duration.of(5, ChronoUnit.SECONDS));
        return "Coucou";
    }
}
