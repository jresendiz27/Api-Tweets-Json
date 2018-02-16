package mx.jresendiz.tweet

import io.vertx.core.AbstractVerticle


class MainVerticle extends AbstractVerticle {
    @Override
    void start() throws Exception {
        vertx.createHttpServer().requestHandler({ req ->
            req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!");
        }).listen(8080);
        System.out.println("HTTP server started on port 8080");
    }
}
