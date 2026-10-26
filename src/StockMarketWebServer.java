import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * StockMarketWebServer.java — Real-World Financial Web Application Server
 * ============================================================================
 *
 * Implements a full REST API for a modern Stock Market Portfolio Tracker.
 * Powers real financial features using 7 foundational Data Structures:
 *   - Segment Tree: Ultra-fast O(log n) range queries for 7D, 30D, 60D analytics & performance screeners
 *   - AVL Tree: Self-balancing Market Leaderboard & Top Gainers/Losers screener
 *   - Min-Heap: Priority alert notification engine
 *   - Queue: FIFO Trade Order Book
 *   - Stack: LIFO Trade Reversibility (Undo/Redo)
 *   - Doubly Linked List: Transaction Ledger & Account Statements
 *   - Dynamic Array: High-frequency historical price series
 *
 * @author Algorise Team
 */
public class StockMarketWebServer {

    private static final int PORT = 8080;
    private static Portfolio portfolio;
    private static OrderQueue orderQueue;
    private static TradeStack undoStack;
    private static TradeStack redoStack;

    public static void main(String[] args) throws IOException {
        int port = PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Initialize Data Structures and realistic multi-stock market data
        loadRealisticMarketData();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Core Financial REST API Endpoints
        server.createContext("/api/portfolio/summary", new PortfolioSummaryHandler());
        server.createContext("/api/stocks", new StocksHandler());
        server.createContext("/api/stock", new SingleStockHandler());
        server.createContext("/api/price", new RecordPriceHandler());
        server.createContext("/api/query", new RangeQueryHandler());
        server.createContext("/api/performance", new ComparativePerformanceHandler());
        server.createContext("/api/leaderboard", new LeaderboardHandler());
        server.createContext("/api/alert", new AlertHandler());
        server.createContext("/api/order", new OrderHandler());
        server.createContext("/api/undo", new UndoHandler());
        server.createContext("/api/redo", new RedoHandler());
        server.createContext("/api/transactions", new TransactionsHandler());
        server.createContext("/api/reset", new ResetHandler());

        // Static Web Dashboard Context
        server.createContext("/", new StaticWebHandler());

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║        ALGORISE — REAL-TIME STOCK MARKET SERVER               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.printf("  🚀 Live Web Application running at: http://localhost:%d/%n", port);
        System.out.println("  💼 Real-world portfolio tracking with Segment Tree 30-day analytics.");
        System.out.println("  Press Ctrl+C to stop.\n");
    }

    /**
     * Loads realistic 30-40 day price histories for 6 major stocks,
     * realistic user holdings, cash balance, price alerts, and order queue.
     */
    private static void loadRealisticMarketData() {
        portfolio = new Portfolio();
        portfolio.setCashBalance(28450.00); // $28,450.00 cash buying power

        orderQueue = new OrderQueue();
        undoStack = new TradeStack();
        redoStack = new TradeStack();

        // 1. AAPL — Apple Inc. (Steady growth, tech blue-chip)
        Stock aapl = new Stock("AAPL", "Apple Inc.");
        double[] aapl35Days = {
            168.20, 169.50, 171.10, 170.40, 172.80, 171.90, 173.50, 175.20,
            174.60, 176.10, 175.50, 177.30, 179.00, 178.20, 180.10, 181.50,
            180.80, 182.40, 183.90, 182.70, 184.20, 185.80, 184.90, 186.50,
            185.30, 187.20, 186.80, 188.40, 187.90, 189.60, 188.50, 190.20,
            189.40, 191.10, 192.50
        };
        for (double p : aapl35Days) aapl.addDailyPrice(p);
        aapl.applyTrade("BUY", 45, 174.20); // User owns 45 shares
        aapl.addTransaction(new Trade("AAPL", "BUY", 45, 174.20));

        // 2. NVDA — NVIDIA Corp. (High growth, high momentum AI leader)
        Stock nvda = new Stock("NVDA", "NVIDIA Corp.");
        double[] nvda35Days = {
            104.50, 106.20, 105.80, 108.40, 110.10, 109.30, 112.50, 114.80,
            113.60, 116.20, 115.40, 118.90, 121.20, 120.50, 123.80, 126.10,
            124.90, 127.50, 129.80, 128.40, 131.20, 133.50, 131.90, 134.60,
            133.20, 135.80, 134.90, 137.40, 136.10, 138.90, 137.80, 140.50,
            139.20, 141.80, 143.60
        };
        for (double p : nvda35Days) nvda.addDailyPrice(p);
        nvda.applyTrade("BUY", 80, 118.50); // User owns 80 shares
        nvda.addTransaction(new Trade("NVDA", "BUY", 80, 118.50));

        // 3. TSLA — Tesla Inc. (Volatile EV pioneer)
        Stock tsla = new Stock("TSLA", "Tesla Inc.");
        double[] tsla35Days = {
            214.50, 218.20, 212.90, 219.40, 224.10, 220.80, 226.50, 231.20,
            228.60, 234.10, 229.50, 236.80, 241.50, 237.20, 244.60, 248.90,
            243.50, 250.20, 254.80, 249.10, 256.40, 261.20, 255.80, 263.50,
            258.90, 265.40, 260.10, 267.80, 262.50, 269.80, 264.20, 271.50,
            266.90, 273.40, 275.80
        };
        for (double p : tsla35Days) tsla.addDailyPrice(p);
        tsla.applyTrade("BUY", 25, 232.00); // User owns 25 shares
        tsla.addTransaction(new Trade("TSLA", "BUY", 25, 232.00));

        // 4. MSFT — Microsoft Corp. (Cloud & enterprise giant)
        Stock msft = new Stock("MSFT", "Microsoft Corp.");
        double[] msft35Days = {
            412.30, 414.50, 413.80, 416.20, 418.90, 417.40, 420.50, 422.80,
            421.60, 424.10, 423.50, 426.20, 428.50, 427.10, 430.40, 432.80,
            431.50, 434.20, 436.50, 435.10, 438.40, 440.90, 439.50, 442.80,
            441.20, 444.60, 443.10, 446.50, 445.20, 448.90, 447.50, 451.20,
            449.80, 453.40, 455.20
        };
        for (double p : msft35Days) msft.addDailyPrice(p);
        msft.applyTrade("BUY", 20, 421.50); // User owns 20 shares
        msft.addTransaction(new Trade("MSFT", "BUY", 20, 421.50));

        // 5. GOOGL — Alphabet Inc. (Search, Ads, AI)
        Stock googl = new Stock("GOOGL", "Alphabet Inc.");
        double[] googl35Days = {
            164.20, 165.80, 164.90, 167.30, 169.10, 168.40, 170.80, 172.50,
            171.60, 173.90, 172.80, 175.40, 177.20, 176.10, 178.60, 180.40,
            179.20, 181.80, 183.50, 182.40, 185.10, 187.30, 186.20, 188.90,
            187.50, 190.40, 189.20, 192.10, 190.80, 193.50, 192.10, 194.80,
            193.60, 196.20, 197.50
        };
        for (double p : googl35Days) googl.addDailyPrice(p);
        googl.applyTrade("BUY", 35, 172.00); // User owns 35 shares
        googl.addTransaction(new Trade("GOOGL", "BUY", 35, 172.00));

        // 6. AMZN — Amazon.com Inc. (E-commerce & AWS Cloud)
        Stock amzn = new Stock("AMZN", "Amazon.com Inc.");
        double[] amzn35Days = {
            176.40, 178.10, 177.20, 179.80, 182.10, 181.30, 183.90, 186.20,
            185.40, 188.10, 187.30, 190.20, 192.50, 191.40, 194.20, 196.80,
            195.60, 198.40, 200.90, 199.70, 202.60, 205.10, 203.90, 206.80,
            205.40, 208.30, 207.10, 210.20, 208.90, 212.10, 210.80, 213.90,
            212.50, 215.80, 217.40
        };
        for (double p : amzn35Days) amzn.addDailyPrice(p);
        amzn.applyTrade("BUY", 30, 184.00); // User owns 30 shares
        amzn.addTransaction(new Trade("AMZN", "BUY", 30, 184.00));

        // 7. META — Meta Platforms Inc. (Social Media & AI)
        Stock meta = new Stock("META", "Meta Platforms Inc.");
        double[] meta35Days = {
            495.20, 498.80, 496.50, 502.10, 506.40, 504.20, 510.50, 514.80,
            512.60, 518.20, 516.40, 522.90, 526.50, 524.10, 530.80, 534.50,
            532.20, 538.90, 543.20, 540.60, 546.80, 551.40, 548.90, 555.20,
            552.80, 559.40, 557.10, 563.80, 560.50, 567.20, 564.90, 571.50,
            568.80, 573.20, 575.40
        };
        for (double p : meta35Days) meta.addDailyPrice(p);
        meta.applyTrade("BUY", 15, 515.00); // User owns 15 shares
        meta.addTransaction(new Trade("META", "BUY", 15, 515.00));

        // 8. NFLX — Netflix Inc. (Streaming Entertainment)
        Stock nflx = new Stock("NFLX", "Netflix Inc.");
        double[] nflx35Days = {
            612.00, 615.50, 613.20, 619.40, 624.80, 621.50, 628.20, 634.50,
            631.80, 638.20, 635.40, 642.10, 647.50, 644.80, 652.10, 657.80,
            654.50, 661.20, 666.90, 663.50, 670.80, 676.20, 673.40, 680.50,
            677.20, 684.60, 681.90, 689.40, 686.20, 693.50, 690.10, 696.80,
            694.20, 697.10, 698.50
        };
        for (double p : nflx35Days) nflx.addDailyPrice(p);
        nflx.applyTrade("BUY", 10, 630.00); // User owns 10 shares
        nflx.addTransaction(new Trade("NFLX", "BUY", 10, 630.00));

        // 9. AMD — Advanced Micro Devices Inc. (Semiconductors & Computing)
        Stock amd = new Stock("AMD", "Advanced Micro Devices Inc.");
        double[] amd35Days = {
            138.20, 139.80, 138.90, 141.40, 143.20, 142.10, 144.80, 146.50,
            145.20, 147.90, 146.80, 149.50, 151.20, 150.10, 152.80, 154.50,
            153.20, 155.90, 157.40, 156.10, 158.80, 160.50, 159.20, 161.80,
            160.40, 163.10, 161.80, 164.50, 163.20, 165.90, 164.50, 166.80,
            165.40, 155.20, 156.80
        };
        for (double p : amd35Days) amd.addDailyPrice(p);
        amd.applyTrade("BUY", 40, 144.00); // User owns 40 shares
        amd.addTransaction(new Trade("AMD", "BUY", 40, 144.00));

        // 10. JPM — JPMorgan Chase & Co. (Banking & Financial Services)
        Stock jpm = new Stock("JPM", "JPMorgan Chase & Co.");
        double[] jpm35Days = {
            198.40, 199.50, 199.10, 201.20, 202.80, 202.10, 204.50, 206.20,
            205.40, 207.80, 207.10, 209.50, 211.20, 210.40, 212.80, 214.50,
            213.80, 216.10, 217.90, 217.20, 219.40, 221.10, 220.50, 222.80,
            221.90, 224.10, 223.50, 225.80, 224.90, 226.50, 225.80, 227.40,
            226.50, 223.80, 224.50
        };
        for (double p : jpm35Days) jpm.addDailyPrice(p);
        jpm.applyTrade("BUY", 30, 206.00); // User owns 30 shares
        jpm.addTransaction(new Trade("JPM", "BUY", 30, 206.00));

        // Add all 10 stocks to portfolio & balance AVL leaderboard
        portfolio.addStock(aapl);
        portfolio.addStock(nvda);
        portfolio.addStock(tsla);
        portfolio.addStock(msft);
        portfolio.addStock(googl);
        portfolio.addStock(amzn);
        portfolio.addStock(meta);
        portfolio.addStock(nflx);
        portfolio.addStock(amd);
        portfolio.addStock(jpm);

        portfolio.updateLeaderboard("AAPL");
        portfolio.updateLeaderboard("NVDA");
        portfolio.updateLeaderboard("TSLA");
        portfolio.updateLeaderboard("MSFT");
        portfolio.updateLeaderboard("GOOGL");
        portfolio.updateLeaderboard("AMZN");
        portfolio.updateLeaderboard("META");
        portfolio.updateLeaderboard("NFLX");
        portfolio.updateLeaderboard("AMD");
        portfolio.updateLeaderboard("JPM");

        // Price alerts
        nvda.addAlert(new PriceAlert("NVDA", 140.0, "ABOVE"));
        tsla.addAlert(new PriceAlert("TSLA", 260.0, "BELOW"));
        aapl.addAlert(new PriceAlert("AAPL", 195.0, "ABOVE"));
        meta.addAlert(new PriceAlert("META", 580.0, "ABOVE"));
        amd.addAlert(new PriceAlert("AMD", 150.0, "BELOW"));

        // Pending trade in order book
        orderQueue.enqueue(new Trade("NVDA", "BUY", 15, 143.60));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP Handlers
    // ─────────────────────────────────────────────────────────────────────────

    /** GET /api/portfolio/summary — Total Net Worth, Cash, Holdings, P&L */
    static class PortfolioSummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            double cash = portfolio.getCashBalance();
            double marketValue = portfolio.getTotalMarketValue();
            double costBasis = portfolio.getTotalCostBasis();
            double totalValue = cash + marketValue;
            double unrealizedPnL = marketValue - costBasis;
            double unrealizedPnLPct = costBasis > 0 ? (unrealizedPnL / costBasis) * 100.0 : 0.0;

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"totalPortfolioValue\":").append(String.format(Locale.US, "%.2f", totalValue)).append(",");
            sb.append("\"cashBalance\":").append(String.format(Locale.US, "%.2f", cash)).append(",");
            sb.append("\"totalInvested\":").append(String.format(Locale.US, "%.2f", costBasis)).append(",");
            sb.append("\"totalMarketValue\":").append(String.format(Locale.US, "%.2f", marketValue)).append(",");
            sb.append("\"unrealizedPnL\":").append(String.format(Locale.US, "%.2f", unrealizedPnL)).append(",");
            sb.append("\"unrealizedPnLPercent\":").append(String.format(Locale.US, "%.2f", unrealizedPnLPct)).append(",");
            sb.append("\"totalPositions\":").append(portfolio.getStockCount()).append(",");
            sb.append("\"pendingOrders\":").append(orderQueue.size()).append(",");
            sb.append("\"undoDepth\":").append(undoStack.size()).append(",");
            sb.append("\"redoDepth\":").append(redoStack.size());
            sb.append("}");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET /api/performance?days=30 — Comparative Ranking Powered by Segment Tree! */
    static class ComparativePerformanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            int days = 30; // default 30 days
            if (params.containsKey("days")) {
                try {
                    days = Integer.parseInt(params.get("days"));
                } catch (NumberFormatException ignored) {}
            }

            // Segment Tree Analytics for every stock over the specified lookback days
            List<StockPerformance> results = new ArrayList<>();
            for (Stock s : portfolio.getAllStocks()) {
                int total = s.getDayCount();
                if (total < 2) continue;

                int window = Math.min(days, total);
                int dayL = total - window + 1;
                int dayR = total;

                double startPrice = s.getPrices()[dayL - 1];
                double endPrice = s.getPrices()[dayR - 1];
                double netChange = endPrice - startPrice;
                double returnPct = startPrice > 0 ? (netChange / startPrice) * 100.0 : 0.0;

                // Ultra-fast O(log n) calculations via Segment Tree!
                double minPrice = s.getMinPrice(dayL, dayR);
                double maxPrice = s.getMaxPrice(dayL, dayR);
                double avgPrice = s.getAvgPrice(dayL, dayR);
                double spread = avgPrice > 0 ? ((maxPrice - minPrice) / avgPrice) * 100.0 : 0.0;

                results.add(new StockPerformance(s.getSymbol(), s.getName(), startPrice, endPrice, returnPct, minPrice, maxPrice, avgPrice, spread, dayL, dayR, window));
            }

            // Sort by return percentage descending (#1 best performer first)
            results.sort((a, b) -> Double.compare(b.returnPct, a.returnPct));

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"lookbackDays\":").append(days).append(",");
            sb.append("\"analyticsMode\":\"Real-Time Multi-Period Analysis\",");
            sb.append("\"rankings\":[");
            for (int i = 0; i < results.size(); i++) {
                if (i > 0) sb.append(",");
                StockPerformance sp = results.get(i);
                sb.append("{");
                sb.append("\"rank\":").append(i + 1).append(",");
                sb.append("\"symbol\":\"").append(escape(sp.symbol)).append("\",");
                sb.append("\"name\":\"").append(escape(sp.name)).append("\",");
                sb.append("\"startPrice\":").append(String.format(Locale.US, "%.2f", sp.startPrice)).append(",");
                sb.append("\"currentPrice\":").append(String.format(Locale.US, "%.2f", sp.endPrice)).append(",");
                sb.append("\"returnPercent\":").append(String.format(Locale.US, "%.2f", sp.returnPct)).append(",");
                sb.append("\"periodLow\":").append(String.format(Locale.US, "%.2f", sp.minPrice)).append(",");
                sb.append("\"periodHigh\":").append(String.format(Locale.US, "%.2f", sp.maxPrice)).append(",");
                sb.append("\"periodAverage\":").append(String.format(Locale.US, "%.2f", sp.avgPrice)).append(",");
                sb.append("\"volatilitySpread\":").append(String.format(Locale.US, "%.2f", sp.volatilitySpread)).append(",");
                sb.append("\"dayL\":").append(sp.dayL).append(",");
                sb.append("\"dayR\":").append(sp.dayR);
                sb.append("}");
            }
            sb.append("]}");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    private static class StockPerformance {
        String symbol, name;
        double startPrice, endPrice, returnPct, minPrice, maxPrice, avgPrice, volatilitySpread;
        int dayL, dayR, window;
        StockPerformance(String symbol, String name, double start, double end, double ret, double min, double max, double avg, double spread, int l, int r, int w) {
            this.symbol = symbol; this.name = name; this.startPrice = start; this.endPrice = end; this.returnPct = ret;
            this.minPrice = min; this.maxPrice = max; this.avgPrice = avg; this.volatilitySpread = spread;
            this.dayL = l; this.dayR = r; this.window = w;
        }
    }

    /** GET /api/stocks — list all stocks with holdings and pricing */
    static class StocksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            ArrayList<Stock> stocks = portfolio.getAllStocks();
            for (int i = 0; i < stocks.size(); i++) {
                if (i > 0) sb.append(",");
                appendStockJSON(sb, stocks.get(i));
            }
            sb.append("]");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET or POST /api/stock */
    static class SingleStockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                String symbol = params.get("symbol");
                if (symbol == null || symbol.isEmpty()) {
                    sendJSON(exchange, 400, "{\"error\":\"Missing symbol\"}");
                    return;
                }
                Stock s = portfolio.getStock(symbol);
                if (s == null) {
                    sendJSON(exchange, 404, "{\"error\":\"Stock not found\"}");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                appendStockJSON(sb, s);
                sendJSON(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> body = parseFormData(readRequestBody(exchange));
                String symbol = body.get("symbol");
                String name = body.get("name");
                if (symbol == null || symbol.isEmpty() || name == null || name.isEmpty()) {
                    sendJSON(exchange, 400, "{\"error\":\"Symbol and name are required\"}");
                    return;
                }
                symbol = symbol.toUpperCase().trim();
                if (portfolio.getStock(symbol) != null) {
                    sendJSON(exchange, 400, "{\"error\":\"Stock already exists\"}");
                    return;
                }
                Stock stock = new Stock(symbol, name.trim());
                stock.addDailyPrice(100.0);
                portfolio.addStock(stock);
                sendJSON(exchange, 200, "{\"success\":true,\"message\":\"Stock added successfully\"}");
            } else {
                sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /** POST /api/price — Records a new market closing price */
    static class RecordPriceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> body = parseFormData(readRequestBody(exchange));
            String symbol = body.get("symbol");
            String priceStr = body.get("price");

            if (symbol == null || priceStr == null) {
                sendJSON(exchange, 400, "{\"error\":\"Symbol and price required\"}");
                return;
            }

            Stock s = portfolio.getStock(symbol);
            if (s == null) {
                sendJSON(exchange, 404, "{\"error\":\"Stock not found\"}");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sendJSON(exchange, 400, "{\"error\":\"Invalid price\"}");
                return;
            }

            // 1. Array + Segment Tree Point Update (O(log n))
            s.addDailyPrice(price);

            // 2. AVL Tree Leaderboard Rebalance (O(log n))
            portfolio.updateLeaderboard(s.getSymbol());

            // 3. Min-Heap Priority Alert Check (O(1) peek)
            PriceAlert[] triggered = s.checkAlerts();

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":true,");
            sb.append("\"symbol\":\"").append(escape(s.getSymbol())).append("\",");
            sb.append("\"newPrice\":").append(s.getCurrentPrice()).append(",");
            sb.append("\"percentChange\":").append(String.format(Locale.US, "%.2f", s.getPercentChange())).append(",");
            sb.append("\"dayCount\":").append(s.getDayCount()).append(",");
            sb.append("\"triggeredAlerts\":[");
            for (int i = 0; i < triggered.length; i++) {
                if (i > 0) sb.append(",");
                PriceAlert a = triggered[i];
                sb.append("{\"symbol\":\"").append(escape(a.getSymbol())).append("\",");
                sb.append("\"threshold\":").append(a.getThreshold()).append(",");
                sb.append("\"type\":\"").append(escape(a.getType())).append("\"}");
            }
            sb.append("]");
            sb.append("}");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET /api/query?symbol=AAPL&type=min|max|avg&l=1&r=30 — Segment Tree */
    static class RangeQueryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String symbol = params.get("symbol");
            String type = params.getOrDefault("type", "min").toLowerCase();
            String lStr = params.get("l");
            String rStr = params.get("r");

            if (symbol == null || lStr == null || rStr == null) {
                sendJSON(exchange, 400, "{\"error\":\"symbol, l, and r required\"}");
                return;
            }

            Stock s = portfolio.getStock(symbol);
            if (s == null) {
                sendJSON(exchange, 404, "{\"error\":\"Stock not found\"}");
                return;
            }

            int dayL, dayR;
            try {
                dayL = Integer.parseInt(lStr);
                dayR = Integer.parseInt(rStr);
            } catch (NumberFormatException e) {
                sendJSON(exchange, 400, "{\"error\":\"Invalid day range numbers\"}");
                return;
            }

            if (dayL < 1 || dayR > s.getDayCount() || dayL > dayR) {
                sendJSON(exchange, 400, String.format("{\"error\":\"Invalid range [%d, %d]. Valid is [1, %d]\"}", dayL, dayR, s.getDayCount()));
                return;
            }

            double result;
            if ("max".equals(type)) {
                result = s.getMaxPrice(dayL, dayR);
            } else if ("avg".equals(type)) {
                result = s.getAvgPrice(dayL, dayR);
            } else {
                result = s.getMinPrice(dayL, dayR);
            }

            int n = s.getDayCount();
            int nodesVisited = Math.max(1, (int) Math.ceil(2 * (Math.log(n) / Math.log(2))));

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"symbol\":\"").append(escape(s.getSymbol())).append("\",");
            sb.append("\"queryType\":\"").append(escape(type)).append("\",");
            sb.append("\"dayL\":").append(dayL).append(",");
            sb.append("\"dayR\":").append(dayR).append(",");
            sb.append("\"result\":").append(String.format(Locale.US, "%.2f", result)).append(",");
            sb.append("\"nodesVisited\":").append(nodesVisited).append(",");
            sb.append("\"totalDays\":").append(n).append(",");
            sb.append("\"complexity\":\"O(log n)\"");
            sb.append("}");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET /api/leaderboard — AVL Tree In-Order Traversal */
    static class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Stock> sorted = portfolio.getSortedLeaderboard();
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int rank = 1;
            for (int i = sorted.size() - 1; i >= 0; i--) {
                if (rank > 1) sb.append(",");
                Stock s = sorted.get(i);
                sb.append("{");
                sb.append("\"rank\":").append(rank++).append(",");
                sb.append("\"symbol\":\"").append(escape(s.getSymbol())).append("\",");
                sb.append("\"company\":\"").append(escape(s.getName())).append("\",");
                sb.append("\"name\":\"").append(escape(s.getName())).append("\",");
                sb.append("\"price\":").append(String.format(Locale.US, "%.2f", s.getCurrentPrice())).append(",");
                sb.append("\"percentChange\":").append(String.format(Locale.US, "%.2f", s.getPercentChange()));
                sb.append("}");
            }
            sb.append("]");

            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET or POST /api/alert — Min-Heap priority queue */
    static class AlertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                int count = 0;
                for (Stock s : portfolio.getAllStocks()) {
                    List<PriceAlert> list = s.getAlerts().toList();
                    for (PriceAlert a : list) {
                        if (count > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"symbol\":\"").append(escape(a.getSymbol())).append("\",");
                        sb.append("\"threshold\":").append(String.format(Locale.US, "%.2f", a.getThreshold())).append(",");
                        sb.append("\"type\":\"").append(escape(a.getType())).append("\",");
                        sb.append("\"isTriggered\":").append(a.isTriggered());
                        sb.append("}");
                        count++;
                    }
                }
                sb.append("]");
                sendJSON(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> body = parseFormData(readRequestBody(exchange));
                String symbol = body.get("symbol");
                String thresholdStr = body.get("threshold");
                String type = body.getOrDefault("type", "BELOW").toUpperCase();

                if (symbol == null || thresholdStr == null) {
                    sendJSON(exchange, 400, "{\"error\":\"Symbol and threshold required\"}");
                    return;
                }
                Stock s = portfolio.getStock(symbol);
                if (s == null) {
                    sendJSON(exchange, 404, "{\"error\":\"Stock not found\"}");
                    return;
                }
                double threshold;
                try {
                    threshold = Double.parseDouble(thresholdStr);
                } catch (NumberFormatException e) {
                    sendJSON(exchange, 400, "{\"error\":\"Invalid threshold\"}");
                    return;
                }

                PriceAlert alert = new PriceAlert(s.getSymbol(), threshold, type);
                s.addAlert(alert);
                sendJSON(exchange, 200, "{\"success\":true,\"message\":\"Price target alert activated in Min-Heap\"}");
            } else {
                sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /** GET or POST /api/order — Queue FIFO & Instant Execution with Portfolio updates */
    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Trade> pending = orderQueue.toList();
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < pending.size(); i++) {
                    if (i > 0) sb.append(",");
                    appendTradeJSON(sb, pending.get(i));
                }
                sb.append("]");
                sendJSON(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String path = exchange.getRequestURI().getPath();
                if (path.endsWith("/execute") || path.endsWith("/process")) {
                    if (orderQueue.isEmpty()) {
                        sendJSON(exchange, 400, "{\"error\":\"No pending orders in queue\"}");
                        return;
                    }
                    Trade trade = orderQueue.dequeue();
                    undoStack.push(trade);
                    redoStack = new TradeStack();

                    Stock s = portfolio.getStock(trade.getSymbol());
                    if (s != null) {
                        s.addTransaction(trade);
                        s.applyTrade(trade.getType(), trade.getQuantity(), trade.getPrice());
                        // Adjust cash
                        if ("BUY".equalsIgnoreCase(trade.getType())) {
                            portfolio.adjustCash(-trade.getTotal());
                        } else {
                            portfolio.adjustCash(trade.getTotal());
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"success\":true,\"executed\":");
                    appendTradeJSON(sb, trade);
                    sb.append("}");
                    sendJSON(exchange, 200, sb.toString());
                } else {
                    Map<String, String> body = parseFormData(readRequestBody(exchange));
                    String symbol = body.get("symbol");
                    String type = body.getOrDefault("type", "BUY").toUpperCase();
                    String qtyStr = body.get("quantity");
                    String priceStr = body.get("price");
                    boolean instant = "true".equalsIgnoreCase(body.get("instant"));

                    if (symbol == null || qtyStr == null || priceStr == null) {
                        sendJSON(exchange, 400, "{\"error\":\"symbol, quantity, and price required\"}");
                        return;
                    }
                    int qty;
                    double price;
                    try {
                        qty = Integer.parseInt(qtyStr);
                        price = Double.parseDouble(priceStr);
                        if (qty <= 0 || price <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        sendJSON(exchange, 400, "{\"error\":\"Invalid quantity or price\"}");
                        return;
                    }

                    Stock s = portfolio.getStock(symbol);
                    if (s == null) {
                        sendJSON(exchange, 404, "{\"error\":\"Stock not found\"}");
                        return;
                    }

                    double total = qty * price;
                    if ("BUY".equalsIgnoreCase(type) && portfolio.getCashBalance() < total) {
                        sendJSON(exchange, 400, "{\"error\":\"Insufficient cash balance to place buy order\"}");
                        return;
                    }
                    if ("SELL".equalsIgnoreCase(type) && s.getSharesOwned() < qty) {
                        sendJSON(exchange, 400, String.format("{\"error\":\"Cannot sell %d shares. You only own %d shares of %s\"}", qty, s.getSharesOwned(), s.getSymbol()));
                        return;
                    }

                    Trade trade = new Trade(s.getSymbol(), type, qty, price);

                    if (instant) {
                        // Execute immediately
                        undoStack.push(trade);
                        redoStack = new TradeStack();
                        s.addTransaction(trade);
                        s.applyTrade(type, qty, price);
                        if ("BUY".equalsIgnoreCase(type)) {
                            portfolio.adjustCash(-total);
                        } else {
                            portfolio.adjustCash(total);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"success\":true,\"executed\":");
                        appendTradeJSON(sb, trade);
                        sb.append("}");
                        sendJSON(exchange, 200, sb.toString());
                    } else {
                        // Place in FIFO Order Queue
                        orderQueue.enqueue(trade);
                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"success\":true,\"enqueued\":");
                        appendTradeJSON(sb, trade);
                        sb.append("}");
                        sendJSON(exchange, 200, sb.toString());
                    }
                }
            } else {
                sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /** POST /api/undo — Reverts last trade, restores cash and shares */
    static class UndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (undoStack.isEmpty()) {
                sendJSON(exchange, 400, "{\"error\":\"No trades to undo\"}");
                return;
            }

            Trade trade = undoStack.pop();
            redoStack.push(trade);

            Stock s = portfolio.getStock(trade.getSymbol());
            if (s != null) {
                s.removeLastTransaction();
                s.rollbackTrade(trade.getType(), trade.getQuantity(), trade.getPrice());
                // Revert cash balance
                if ("BUY".equalsIgnoreCase(trade.getType())) {
                    portfolio.adjustCash(trade.getTotal()); // refund
                } else {
                    portfolio.adjustCash(-trade.getTotal()); // retract
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"message\":\"Trade reversed and refunded\",\"undone\":");
            appendTradeJSON(sb, trade);
            sb.append("}");
            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** POST /api/redo — Re-applies undone trade */
    static class RedoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (redoStack.isEmpty()) {
                sendJSON(exchange, 400, "{\"error\":\"No trades to redo\"}");
                return;
            }

            Trade trade = redoStack.pop();
            undoStack.push(trade);

            Stock s = portfolio.getStock(trade.getSymbol());
            if (s != null) {
                s.addTransaction(trade);
                s.applyTrade(trade.getType(), trade.getQuantity(), trade.getPrice());
                if ("BUY".equalsIgnoreCase(trade.getType())) {
                    portfolio.adjustCash(-trade.getTotal());
                } else {
                    portfolio.adjustCash(trade.getTotal());
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"message\":\"Trade re-executed\",\"redone\":");
            appendTradeJSON(sb, trade);
            sb.append("}");
            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** GET /api/transactions — Doubly Linked List */
    static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String symbol = params.get("symbol");
            String dir = params.getOrDefault("direction", "reverse"); // default newest first

            List<Trade> list = new ArrayList<>();
            if (symbol != null && !symbol.isEmpty()) {
                Stock s = portfolio.getStock(symbol);
                if (s != null) {
                    list = "reverse".equalsIgnoreCase(dir) ? s.getTransactions().toReverseList() : s.getTransactions().toList();
                }
            } else {
                for (Stock s : portfolio.getAllStocks()) {
                    list.addAll(s.getTransactions().toList());
                }
                if ("reverse".equalsIgnoreCase(dir)) {
                    Collections.reverse(list);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                appendTradeJSON(sb, list.get(i));
            }
            sb.append("]");
            sendJSON(exchange, 200, sb.toString());
        }
    }

    /** POST /api/reset */
    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            loadRealisticMarketData();
            sendJSON(exchange, 200, "{\"success\":true,\"message\":\"Reset to live market data\"}");
        }
    }

    /** Serves the modern HTML/JS/CSS Frontend */
    static class StaticWebHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "/index.html".equals(path)) {
                File htmlFile = new File("/Users/agastiya/.gemini/antigravity/scratch/StockMarketTracker/web/index.html");
                byte[] bytes;
                if (htmlFile.exists()) {
                    bytes = java.nio.file.Files.readAllBytes(htmlFile.toPath());
                } else {
                    bytes = "<html><body><h1>Algorise</h1></body></html>".getBytes(StandardCharsets.UTF_8);
                }

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────────────────────

    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJSON(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        if (body.trim().startsWith("{")) {
            String clean = body.trim().replace("{", "").replace("}", "");
            for (String part : clean.split(",")) {
                String[] kv = part.split(":", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim().replace("\"", ""), kv[1].trim().replace("\"", ""));
                }
            }
            return map;
        }
        return parseQuery(body);
    }

    private static void appendStockJSON(StringBuilder sb, Stock s) {
        sb.append("{");
        sb.append("\"symbol\":\"").append(escape(s.getSymbol())).append("\",");
        sb.append("\"company\":\"").append(escape(s.getName())).append("\",");
        sb.append("\"name\":\"").append(escape(s.getName())).append("\",");
        sb.append("\"currentPrice\":").append(String.format(Locale.US, "%.2f", s.getCurrentPrice())).append(",");
        sb.append("\"percentChange\":").append(String.format(Locale.US, "%.2f", s.getPercentChange())).append(",");
        sb.append("\"dayCount\":").append(s.getDayCount()).append(",");
        sb.append("\"sharesOwned\":").append(s.getSharesOwned()).append(",");
        sb.append("\"avgBuyPrice\":").append(String.format(Locale.US, "%.2f", s.getAvgBuyPrice())).append(",");
        sb.append("\"totalCost\":").append(String.format(Locale.US, "%.2f", s.getTotalCost())).append(",");
        sb.append("\"marketValue\":").append(String.format(Locale.US, "%.2f", s.getMarketValue())).append(",");
        sb.append("\"unrealizedPnL\":").append(String.format(Locale.US, "%.2f", s.getUnrealizedPnL())).append(",");
        sb.append("\"unrealizedPnLPercent\":").append(String.format(Locale.US, "%.2f", s.getUnrealizedPnLPercent())).append(",");
        sb.append("\"prices\":[");
        double[] prices = s.getPrices();
        for (int i = 0; i < prices.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.2f", prices[i]));
        }
        sb.append("]");
        sb.append("}");
    }

    private static void appendTradeJSON(StringBuilder sb, Trade t) {
        sb.append("{");
        sb.append("\"id\":").append(t.getTradeId()).append(",");
        sb.append("\"symbol\":\"").append(escape(t.getSymbol())).append("\",");
        sb.append("\"type\":\"").append(escape(t.getType())).append("\",");
        sb.append("\"quantity\":").append(t.getQuantity()).append(",");
        sb.append("\"price\":").append(String.format(Locale.US, "%.2f", t.getPrice())).append(",");
        sb.append("\"total\":").append(String.format(Locale.US, "%.2f", t.getTotal())).append(",");
        sb.append("\"timestamp\":\"").append(escape(t.getTimestamp())).append("\"");
        sb.append("}");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
