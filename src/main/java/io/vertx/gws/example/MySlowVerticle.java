package io.vertx.gws.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.gws.example.util.CustomMessage;

public class MySlowVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		EventBus eventBus = getVertx().eventBus();

		// Does not have to register codec because sender already registered
		/*
		 * eventBus.registerDefaultCodec(CustomMessage.class, new
		 * CustomMessageCodec());
		 */

		// Receive message
		eventBus.consumer("local-message-receiver", message -> {
			CustomMessage customMessage = (CustomMessage) message.body();

			System.out.println("Custom message received: " + customMessage.getSummary());

			vertx.executeBlocking(future -> {
				// Call some blocking API that takes a significant amount of
				// time to return
				// String result = someAPI.blockingMethod("hello");
				// future.complete(result);
			} , res -> {
				System.out.println("The result is: " + res.result());
			});

			// Replying is same as publishing
			CustomMessage replyMessage = new CustomMessage(200, "a00000002", "Message sent from local receiver!");
			message.reply(replyMessage);
		});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
	}

}
