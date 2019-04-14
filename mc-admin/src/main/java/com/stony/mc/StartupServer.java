package com.stony.mc;

import com.stony.mc.session.MasterServer;
import com.stony.reactor.jersey.JerseyBasedHandler;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class StartupServer {

    public static void main(String[] args) throws Exception {

        MasterServer server = new MasterServer(4088);
        server.startup();

        System.out.println("----->>" + StartupServer.class.getResource("/webapp"));
//        final Path resource = Paths.get(StartupServer.class.getResource("/webapp").toURI());
        final Path resource = Paths.get("D:\\IdeaProjects\\message-channel\\mc-admin\\src\\main\\resources\\webapp");
        HttpServer.create(8266)
                .startAndAwait(JerseyBasedHandler.builder()
                        .withClassPath("com.stony.mc.controllers")
                        .addValueProvider(FastJsonProvider.class)
                        .withRouter(routes -> {
                            routes.get("/test/get", (req, resp) -> resp.sendString(Mono.just("asdfasdf")))
                                    .directory("/res", resource);
                        }).build()
                );


        server.shutdown();

    }
}
