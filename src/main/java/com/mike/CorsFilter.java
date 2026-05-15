package com.mike;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            String origin = origin(req);
            req.abortWith(Response.ok()
                    .header("Access-Control-Allow-Origin", origin)
                    .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Accept, Content-Type, ngrok-skip-browser-warning")
                    .header("Access-Control-Max-Age", "86400")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        res.getHeaders().add("Access-Control-Allow-Origin", origin(req));
        res.getHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        res.getHeaders().add("Access-Control-Allow-Headers", "Accept, Content-Type, ngrok-skip-browser-warning");
    }

    private String origin(ContainerRequestContext req) {
        String origin = req.getHeaderString("Origin");
        return origin != null ? origin : "*";
    }
}
