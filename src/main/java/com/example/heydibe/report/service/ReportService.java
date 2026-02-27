package com.example.heydibe.report.service;

public interface ReportService {
    Object getAvailableMonths();
    Object getMonthlyEmotions(String yearMonth);
    Object getMonthlyTopics(String yearMonth);
    Object getMonthlyPreferences(String yearMonth);
    Object getMonthlyActivities(String yearMonth);
    Object getMonthlyInsights(String yearMonth);
    Object getMonthlyCalendar(String yearMonth);
    Object getMonthlyReminder(String yearMonth);
}

