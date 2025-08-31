package com.tursa.route.controller;

import com.tursa.route.service.DataInitService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class DataController {

    private final DataInitService dataInitService;

    public DataController(DataInitService dataInitService) {
        this.dataInitService = dataInitService;
    }

    @PostMapping("/init-data")
    public String initData() {
        dataInitService.addSampleData();
        return "Sample data initialized";
    }

    @PostMapping("/clear-data")
    public String clearData() {
        dataInitService.clearData();
        return "Data cleared";
    }
}