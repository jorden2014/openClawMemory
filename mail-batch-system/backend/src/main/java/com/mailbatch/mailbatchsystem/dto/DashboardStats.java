package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStats {

    private long customerCount;

    private long todaySentCount;

    private long monthSentCount;

    private long failedCount;

    private List<MailRecordResponse> recentRecords;

    private List<DailyCount> weeklyTrend;

    @Data
    public static class DailyCount {
        private String date;
        private long count;

        public DailyCount(String date, long count) {
            this.date = date;
            this.count = count;
        }
    }
}