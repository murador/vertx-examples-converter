package io.vertx.blog.first;

import java.io.IOException;
import java.net.ServerSocket;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {

	/**
	 * Si genera il reference di vertx
	 */
	private Vertx vertx;

	Integer port;

	/**
	 * In the setUp method, we creates an instance of Vertx and deploy our
	 * verticle.
	 * 
	 * @param context
	 * @throws
	 */
	@Before
	public void setUp(TestContext context) {
		vertx = Vertx.vertx();

		ServerSocket socket;
		try {
			socket = new ServerSocket(0);

			port = socket.getLocalPort();
			socket.close();

			DeploymentOptions options = new DeploymentOptions()
					.setConfig(new JsonObject().put("http.port", port));

			vertx.deployVerticle(MyFirstVerticle.class.getName(), options,
					context.asyncAssertSuccess());
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	/**
	 * Let’s now have a look to the test of our application: the
	 * testMyApplication method. The test emits a request to our application and
	 * checks the result. Emitting the request and receiving the response is
	 * asynchronous
	 * 
	 * @param context
	 */
	@Test
	public void testMyApplication(TestContext context) {
		final Async async = context.async();

		vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
			response.handler(body -> {
				// se contiene la stringa hello allora genero
				context.assertTrue(body.toString().contains("Hello"));
				async.complete();
			});
		});
	}
}