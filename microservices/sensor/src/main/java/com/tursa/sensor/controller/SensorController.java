package com.tursa.sensor.controller;

import com.tursa.sensor.sevice.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensorService;

    @Autowired
    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("/record")
    public String recordSensor(@RequestParam String sensorId,
                               @RequestParam String data) {
        return sensorService.recordSensorData(sensorId, data);
    }

    @PostMapping("/clear-obstacle")
    public String clearObstacle(@RequestParam String sensorId) {
        return sensorService.clearObstacle(sensorId);
    }

    @GetMapping("/test")
    public String testRoute() {
        return "Sensor route is working!";
    }
}