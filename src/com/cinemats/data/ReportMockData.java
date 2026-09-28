package com.cinemats.data;

// Sample financial reports mock data
public final class ReportMockData {

    private ReportMockData() {}

    // Returns sample shift settlement reports
    public static Object[][] getFinancialReports() {
        return new Object[][]{
            {"Today (Morning)", "Counter #01", "Rahul Sharma", "20.00", ",240.00", ",860.00"},
            {"Today (Morning)", "Counter #02", "Priya Verma", "00.00", ",190.00", ",990.00"},
            {"Yesterday", "Counter #01", "Rahul Sharma", ",120.00", ",850.00", ",970.00"},
            {"Yesterday", "Counter #02", "Amit Patel", "80.00", ",100.00", ",080.00"}
        };
    }
}
