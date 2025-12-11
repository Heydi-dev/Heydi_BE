package com.example.heydibe.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @GetMapping("/monthly")
    public String getAvailableMonths() {
        return "get available months - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/emotions")
    public String getMonthlyEmotions(@PathVariable String yearMonth) {
        return "get monthly emotions - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/topics")
    public String getMonthlyTopics(@PathVariable String yearMonth) {
        return "get monthly topics - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/preferences")
    public String getMonthlyPreferences(@PathVariable String yearMonth) {
        return "get monthly preferences - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/activities")
    public String getMonthlyActivities(@PathVariable String yearMonth) {
        return "get monthly activities - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/insights")
    public String getMonthlyInsights(@PathVariable String yearMonth) {
        return "get monthly insights - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/calendar")
    public String getMonthlyCalendar(@PathVariable String yearMonth) {
        return "get monthly calendar - TODO";
    }

    @GetMapping("/monthly/{yearMonth}/reminder")
    public String getMonthlyReminder(@PathVariable String yearMonth) {
        return "get monthly reminder - TODO";
    }
}
