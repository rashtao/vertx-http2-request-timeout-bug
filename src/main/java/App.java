import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class App {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        Future<String> server = vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(Promise<Void> startPromise) {
                vertx
                        .createHttpServer()
                        .requestHandler(r ->
                                vertx.setTimer(2_000, id ->
                                        r.response().end("Hello!")
                                )
                        )
                        .listen(8080)
                        .<Void>mapEmpty()
                        .onSuccess(startPromise::complete)
                        .onFailure(startPromise::fail);
            }
        });

        WebClient client = WebClient.create(vertx, new WebClientOptions()
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setDefaultPort(8080));

        server
                .onFailure(Throwable::printStackTrace)
                .onSuccess(it -> client
                        .get("/")
                        .timeout(1_000)
                        .send()
                        .onFailure(Throwable::printStackTrace)
                        .onSuccess(r -> System.out.println(r.body().toString()))
                );
    }
}
