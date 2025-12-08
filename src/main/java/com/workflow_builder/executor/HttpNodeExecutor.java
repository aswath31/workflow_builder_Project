package com.workflow_builder.executor;

import com.workflow_builder.workflow.NodeDefinition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class HttpNodeExecutor implements NodeExecutor {

    private final WebClient webClient;

    public HttpNodeExecutor(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Map<String, Object>> execute(NodeDefinition node, Map<String, Object> input, Map<String,Object> context) {

        Map<String,Object> cfg = node.getConfig();
        String method = Optional.ofNullable(cfg.get("method")).map(Object::toString).orElse("GET");
        String url = Optional.ofNullable(cfg.get("url")).map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("HTTP node missing url"));
        Map<String,String> headers = (Map<String,String>) cfg.getOrDefault("headers", Collections.emptyMap());
        Object body = cfg.get("body");

        WebClient.RequestBodySpec req = webClient.method(org.springframework.http.HttpMethod.valueOf(method.toUpperCase()))
                .uri(url);

        for (var e : headers.entrySet()) req = req.header(e.getKey(), e.getValue());

        Mono<String> respMono;
        if (body != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH"))) {
            respMono = req.contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().bodyToMono(String.class);
        } else {
            respMono = req.retrieve().bodyToMono(String.class);
        }

        // timeout from config
        int timeoutSeconds = Optional.ofNullable(cfg.get("timeoutSeconds")).map(Object::toString).map(Integer::valueOf).orElse(30);

        return respMono.timeout(Duration.ofSeconds(timeoutSeconds))
                .map(resp -> {
                    Map<String,Object> summary = new HashMap<>();
                    summary.put("status","success");
                    summary.put("output", resp);
                    return summary;
                })
                .onErrorResume(ex -> {
                    Map<String,Object> summary = new HashMap<>();
                    summary.put("status","failed");
                    summary.put("error", ex.getMessage());
                    return Mono.just(summary);
                })
                .toFuture();
    }
}
