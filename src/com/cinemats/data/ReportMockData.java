package com.cinemats.data;

// Sample financial reports mock data
public final class ReportMockData {

    private ReportMockData() {}

    // Returns sample shift settlement reports
    public static Object[][] getFinancialReports() {
        return new Object[][]{
            {"Today (Morning)", "Counter #01", "Rahul Sharma", "₹3,450.00", "₹12,240.00", "₹15,690.00"},
            {"Today (Evening)", "Counter #02", "Priya Verma", "₹4,100.00", "₹14,190.00", "₹18,290.00"},
            {"Yesterday", "Counter #01", "Rahul Sharma", "₹5,120.00", "₹16,850.00", "₹21,970.00"},
            {"Yesterday", "Counter #03", "Amit Patel", "₹2,880.00", "₹11,100.00", "₹13,980.00"}
        };
    }
}
