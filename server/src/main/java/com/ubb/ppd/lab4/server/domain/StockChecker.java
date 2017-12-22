package com.ubb.ppd.lab4.server.domain;

import com.ubb.ppd.lab4.server.util.Money;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author Marius Adam
 */
public class StockChecker implements Runnable {
    private final Store          store;
    private       PrintWriter    writer;
    private       CheckResult    lastCheck;
    private       CountDownLatch emptyStoreLatch;

    public StockChecker(Store store, OutputStream output, CountDownLatch emptyStoreLatch) {
        this.store = store;
        this.writer = new PrintWriter(output);
        this.emptyStoreLatch = emptyStoreLatch;

        // perform the first check on the main thread
        // to populate the check with the initial values
        this.lastCheck = check();
    }

    @Override
    public synchronized void run() {
        writer.println("Health check starting.... : ");

        CheckResult currentResult = check();
        writer.println(currentResult.formatAsString(lastCheck));
        writer.println("\"Total products: \"" + store.totalProducts());
        writer.println(Store.DUMP_DATA_SEPARATOR

        );
        writer.flush();

        if (currentResult.availableInStock < 1) {
            emptyStoreLatch.countDown();
        }

        lastCheck = currentResult;
    }

    private CheckResult check() {
        return new CheckResult(
                new Date(),
                store.getLifetimeProfit(),
                store.soldProducts(),
                store.createdOrders(),
                store.canceledOrders(),
                store.processedOrders(),
                store.totalOrders(),
                store.availableProducts()
        );
    }

    private static class CheckResult {
        public static final Object[] TABLE_HEADER = new String[]{
                "Date",
                "Last date",
                "Profit",
                "Δ Profit",
                "Sold products",
                "Δ Sold products",
                "Created orders",
                "Δ Created orders",
                "Canceled orders",
                "Δ Canceled orders",
                "Processed orders",
                "Δ Processed orders",
                "Total orders",
                "Δ Total orders",
                "Products available orders",
                "Δ Products available orders",
        };

        public static final  String           FORMAT      =
                "\"Criteria -> Current, ΔCriteria,\",\n" +
                        "\"Date -> %s, %s,\",\n" +
//                        "\"Last date -> %s\",\n" +
                        "\"Profit -> %s, %s\",\n" +
//                        "\"Δ Profit -> %s\",\n" +
                        "\"Sold products -> %s, %s\",\n" +
//                        "\"Δ Sold products -> %s\",\n" +
                        "\"Created orders -> %s, %s\",\n" +
//                        "\"Δ Created orders -> %s\",\n" +
                        "\"Canceled orders -> %s, %s\",\n" +
//                        "\"Δ Canceled orders -> %s\",\n" +
                        "\"Processed orders -> %s, %s\",\n" +
//                        "\"Δ Processed orders -> %s\",\n" +
                        "\"Total orders -> %s, %s\",\n" +
//                        "\"Δ Total orders -> %s\",\n" +
                        "\"Products available -> %s, %s\"";
        //                        "\"Δ Products available orders -> %s\",";
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        private Date  date;
        private Money profit;
        private long  soldProducts;
        private long  createdOrders;
        private long  canceledOrders;
        private long  processedOrders;
        private long  totalOrders;
        private long  availableInStock;

        public CheckResult(Date date, Money profit, long soldProducts, long createdOrders, long canceledOrders, long processedOrders, long totalOrders, long availableInStock) {
            this.date = date;
            this.profit = profit;
            this.soldProducts = soldProducts;
            this.createdOrders = createdOrders;
            this.canceledOrders = canceledOrders;
            this.processedOrders = processedOrders;
            this.totalOrders = totalOrders;
            this.availableInStock = availableInStock;
        }

        public String formatAsString(CheckResult previous) {
            return String.format(
                    FORMAT,
                    date,
                    previous.date,
                    profit,
                    profit.minus(previous.profit),
                    soldProducts,
                    soldProducts - previous.soldProducts,
                    createdOrders,
                    createdOrders - previous.createdOrders,
                    canceledOrders,
                    canceledOrders - previous.canceledOrders,
                    processedOrders,
                    processedOrders - previous.processedOrders,
                    totalOrders,
                    totalOrders - previous.totalOrders,
                    availableInStock,
                    previous.availableInStock
            );
        }
    }
}
