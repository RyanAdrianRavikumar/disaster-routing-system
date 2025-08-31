package com.tursa.route.controller;

import com.tursa.route.model.Edge;
import com.tursa.route.model.Node;
import com.tursa.route.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/route")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/shortest-path")
    public RouteService.RouteResponse getShortestPath(@RequestParam String start, @RequestParam String end) throws InterruptedException {
        List<Edge> edges = routeService.getEdges();
        return routeService.shortestPathWithDistance(start, end, edges);
    }

    @GetMapping("/nodes")
    public List<Node> getNodes() throws InterruptedException {
        return routeService.getNodes();
    }

    @GetMapping("/edges")
    public List<Edge> getEdges() throws InterruptedException {
        return routeService.getEdges();
    }
}