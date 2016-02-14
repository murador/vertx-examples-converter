package io.vertx.gws.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.gws.example.util.CustomMessage;
import io.vertx.gws.example.util.CustomMessageCodec;

/**
 * 
 * @author Gianfranco Murador Qui definiamo il comportamento del main verticle.
 */
public class MainVericle extends AbstractVerticle {

	private static Integer NUM_VERTICLES = 2;

	@Override
	public void start(Future<Void> startFuture) {

		// Create a router object.
		Router router = Router.router(vertx);
		EventBus eventBus = vertx.eventBus();

		// Register codec for custom message
		eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());

		// Custom message
		CustomMessage clusterWideMessage = new CustomMessage(200, "a00000001", "Message sent from publisher!");
		CustomMessage localMessage = new CustomMessage(200, "a0000001", "Local message!");

		// in this phase we describe a set of unkind worker
		NUM_VERTICLES = config().getInteger("num_of_worker", 2);

		DeploymentOptions options = new DeploymentOptions().setInstances(NUM_VERTICLES);

		// Quest nel caso il nostro worker è locale, altrimenti basta
		// sempicmente
		// spedire il messaggio
		vertx.deployVerticle("io.vertx.gws.example.MySlowVerticle", deployResult -> {
			// Deploy succeed
			if (deployResult.succeeded()) {
				// Send a message to [local receiver] every 2 second
				getVertx().setPeriodic(2000, _id -> {
					eventBus.send("local-message-receiver", localMessage, reply -> {
						if (reply.succeeded()) {
							CustomMessage replyMessage = (CustomMessage) reply.result().body();
							System.out.println("Received local reply: " + replyMessage.getSummary());
						} else {
							System.out.println("No reply from local receiver");
						}
					});
				});
				// Deploy failed
			} else {
				deployResult.cause().printStackTrace();
			}
		});

		// Se spediamo il messaggio in cluster mode
		getVertx().setPeriodic(1000, _id -> {
			eventBus.send("cluster-message-receiver", clusterWideMessage, reply -> {
				if (reply.succeeded()) {
					CustomMessage replyMessage = (CustomMessage) reply.result().body();
					System.out.println("Received reply: " + replyMessage.getSummary());
				} else {
					System.out.println("No reply from cluster receiver");
				}
			});
		});

		// Ogno main verticle ha una interfaccia web verso il client.
		// Questa potrebbe non essere utilizzata al momento
		// Bind "/" to our hello message - so we are still compatible.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
		});

		vertx.createHttpServer().requestHandler(r -> {
			r.response().end("<h1>Hello from my first " + "Vert.x 3 application</h1>");
		}).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
	}
}
