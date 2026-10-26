import java.util.Scanner;

/**
 * ============================================================================
 *  StockMarketApp.java — Main controller for the Stock Market Portfolio Tracker
 * ============================================================================
 *
 * PURPOSE:
 *   This is the entry point for the application. It provides a menu-driven
 *   console interface that demonstrates SIX data structures in a real-world
 *   context:
 *
 *   ┌───────────────────────────────────────────────────────────────────────┐
 *   │  Data Structure          │  Where It's Used                         │
 *   ├──────────────────────────┼──────────────────────────────────────────┤
 *   │  ⭐ Segment Tree         │  Range queries on price history          │
 *   │  🌳 AVL Tree             │  Leaderboard sorted by daily % change   │
 *   │  📊 Min-Heap             │  Price alerts (lowest threshold first)  │
 *   │  📋 Queue                │  Pending trade orders (FIFO)            │
 *   │  ↩️  Stack                │  Undo / Redo trade operations           │
 *   │  🔗 Doubly Linked List   │  Transaction history per stock          │
 *   └──────────────────────────┴──────────────────────────────────────────┘
 *
 * ARCHITECTURE:
 *   StockMarketApp
 *     ├── Portfolio         — manages stocks + AVL leaderboard
 *     ├── OrderQueue        — FIFO queue of pending trade orders
 *     ├── TradeStack (undo) — LIFO stack for undoing trades
 *     ├── TradeStack (redo) — LIFO stack for redoing undone trades
 *     └── Scanner           — reads user input from System.in
 *
 * SAMPLE DATA:
 *   On launch, loadSampleData() pre-populates three stocks (AAPL, GOOGL,
 *   TSLA) with 8 days of prices, a few alerts, and a pending order so
 *   the demo works immediately without manual setup.
 *
 * @author  DSA Course Project
 * @version 1.0
 */
public class StockMarketApp {

    // ── Instance Fields ───────────────────────────────────────────────────

    /** The portfolio containing all tracked stocks. */
    private Portfolio portfolio;

    /** FIFO queue of pending trade orders. */
    private OrderQueue orderQueue;

    /** LIFO stack holding executed trades (for undo). */
    private TradeStack undoStack;

    /** LIFO stack holding undone trades (for redo). */
    private TradeStack redoStack;

    /** Scanner for reading console input. */
    private Scanner scanner;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Initializes all components of the application.
     *
     * Time Complexity: O(1) — instantiates component objects and scanner in constant time.
     */
    public StockMarketApp() {
        this.portfolio = new Portfolio();
        this.orderQueue = new OrderQueue();
        this.undoStack = new TradeStack();
        this.redoStack = new TradeStack();
        this.scanner = new Scanner(System.in);
    }

    // ── Entry Point ───────────────────────────────────────────────────────

    /**
     * Application entry point.
     * Creates the app, loads sample data, and starts the main loop.
     *
     * Time Complexity: O(n log n) — boots CLI tracker, loads sample data, and enters interactive event loop.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        StockMarketApp app = new StockMarketApp();
        app.loadSampleData();
        app.run();
    }

    // ── Sample Data ───────────────────────────────────────────────────────

    /**
     * Pre-loads sample data so every feature can be demonstrated immediately.
     *
     * Time Complexity: O(n log n) — registers initial stocks, records historical prices, and builds Segment/AVL trees.
     *
     * Loaded data:
     *   • 3 stocks: AAPL, GOOGL, TSLA
     *   • 8 days of closing prices for each
     *   • 3 price alerts (AAPL below $140, GOOGL above $2900, TSLA below $200)
     *   • 1 pending order (BUY 10 AAPL @ $170)
     */
    private void loadSampleData() {
        System.out.println();
        System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║        REAL-TIME STOCK MARKET PORTFOLIO TRACKER               ║");
        System.out.println("  ║               DSA Course Project Demo                         ║");
        System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  📦 Loading sample data...");
        System.out.println();

        // ── Create stocks ──
        Stock aapl  = new Stock("AAPL",  "Apple Inc.");
        Stock googl = new Stock("GOOGL", "Alphabet Inc.");
        Stock tsla  = new Stock("TSLA",  "Tesla Inc.");

        // ── Add stocks to portfolio (inserts into HashMap + AVL tree) ──
        portfolio.addStock(aapl);
        portfolio.addStock(googl);
        portfolio.addStock(tsla);
        System.out.println();

        // ── Record 8 days of prices ──
        // AAPL: 150 → 148 → 155 → 142 → 160 → 138 → 165 → 170
        double[] aaplPrices  = {150, 148, 155, 142, 160, 138, 165, 170};
        // GOOGL: 2800 → 2780 → 2820 → 2750 → 2830 → 2810 → 2790 → 2850
        double[] googlPrices = {2800, 2780, 2820, 2750, 2830, 2810, 2790, 2850};
        // TSLA: 220 → 215 → 230 → 210 → 240 → 225 → 235 → 250
        double[] tslaPrices  = {220, 215, 230, 210, 240, 225, 235, 250};

        System.out.println("  📈 Recording 8 days of price history for each stock...");
        for (int i = 0; i < 8; i++) {
            aapl.addDailyPrice(aaplPrices[i]);
            googl.addDailyPrice(googlPrices[i]);
            tsla.addDailyPrice(tslaPrices[i]);
        }

        // Update leaderboard with final % changes.
        portfolio.updateLeaderboard("AAPL");
        portfolio.updateLeaderboard("GOOGL");
        portfolio.updateLeaderboard("TSLA");

        System.out.println();

        // ── Set sample alerts ──
        System.out.println("  🔔 Setting sample price alerts...");
        aapl.addAlert(new PriceAlert("AAPL", 140.0, "BELOW"));
        googl.addAlert(new PriceAlert("GOOGL", 2900.0, "ABOVE"));
        tsla.addAlert(new PriceAlert("TSLA", 200.0, "BELOW"));
        System.out.println();

        // ── Place a sample order in the queue ──
        System.out.println("  📋 Placing a sample pending order...");
        Trade sampleOrder = new Trade("AAPL", "BUY", 10, 170.0);
        orderQueue.enqueue(sampleOrder);
        System.out.println("   📋 Order queued: BUY 10 AAPL @ $170.00");
        System.out.println();

        // ── Welcome summary ──
        System.out.println("  ✅ Sample data loaded successfully!");
        System.out.println("     • 3 stocks with 8 days of price data each");
        System.out.println("     • 3 price alerts configured");
        System.out.println("     • 1 pending trade order in queue");
        System.out.println();
    }

    // ── Main Loop ─────────────────────────────────────────────────────────

    /**
     * Main application loop. Displays the menu, reads the user's choice,
     * and dispatches to the appropriate handler method.
     *
     * Time Complexity: O(1) amortized per command interaction.
     */
    private void run() {
        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("  Enter your choice: ");
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\n  ❌ Please enter a valid number.\n");
                continue;
            }

            System.out.println();

            switch (choice) {
                case 1  -> handleAddStock();
                case 2  -> handleRecordPrice();
                case 3  -> handleViewPriceHistory();
                case 4  -> handleRangeQueryMin();
                case 5  -> handleRangeQueryMax();
                case 6  -> handleRangeQueryAvg();
                case 7  -> handleViewLeaderboard();
                case 8  -> handleSetAlert();
                case 9  -> handleCheckAlerts();
                case 10 -> handlePlaceTrade();
                case 11 -> handleProcessOrders();
                case 12 -> handleUndo();
                case 13 -> handleRedo();
                case 14 -> handleViewTransactions();
                case 15 -> handleViewAllStocks();
                case 16 -> runCompleteDemo();
                case 0  -> {
                    System.out.println("  👋 Thank you for using the Stock Market Portfolio Tracker!");
                    System.out.println("     Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("  ❌ Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    // ── Menu Display ──────────────────────────────────────────────────────

    /**
     * Prints the main menu with all available operations and the data
     * structure each one demonstrates.
     *
     * Time Complexity: O(1) — prints static ASCII CLI options.
     */
    private void displayMenu() {
        System.out.println("  ═══════════════════════════════════════════════════════════");
        System.out.println("    REAL-TIME STOCK MARKET PORTFOLIO TRACKER");
        System.out.println("  ═══════════════════════════════════════════════════════════");
        System.out.println("   1.  Add Stock to Portfolio");
        System.out.println("   2.  Record Daily Price");
        System.out.println("   3.  View Price History");
        System.out.println("   4.  Range Query — Minimum Price   ⭐ Segment Tree");
        System.out.println("   5.  Range Query — Maximum Price   ⭐ Segment Tree");
        System.out.println("   6.  Range Query — Average Price   ⭐ Segment Tree");
        System.out.println("   7.  View Leaderboard              \uD83C\uDF33 AVL Tree");
        System.out.println("   8.  Set Price Alert               \uD83D\uDCCA Min-Heap");
        System.out.println("   9.  Check Price Alerts            \uD83D\uDCCA Min-Heap");
        System.out.println("  10.  Place Trade Order             \uD83D\uDCCB Queue");
        System.out.println("  11.  Process Pending Orders        \uD83D\uDCCB Queue");
        System.out.println("  12.  Undo Last Trade               ↩️  Stack");
        System.out.println("  13.  Redo Last Undo                ↪️  Stack");
        System.out.println("  14.  View Transaction History      \uD83D\uDD17 Linked List");
        System.out.println("  15.  View All Portfolio Stocks");
        System.out.println("  16.  Run Complete Demo (all features)");
        System.out.println("   0.  Exit");
        System.out.println("  ═══════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CORE API METHODS (UML Class Diagram & Section 9.2 Implementation Plan)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a new Stock and adds it to the Portfolio and AVL leaderboard.
     *
     * Time Complexity: O(log n) — creates Stock entity and inserts into AVL leaderboard in O(log n).
     */
    public void addStock(String symbol, String name) {
        Stock s = new Stock(symbol, name);
        portfolio.addStock(s);
    }

    /**
     * Removes a stock from the Portfolio and AVL leaderboard.
     *
     * Time Complexity: O(log n) — unlinks stock from AVL tree and rebalances in O(log n).
     */
    public void removeStock(String symbol) {
        portfolio.removeStock(symbol);
    }

    /**
     * Records a daily price: updates Array, Segment Tree, AVL leaderboard, and checks alerts.
     *
     * Time Complexity: O(log n) — O(1) array append, O(log n) Segment Tree update, O(log n) AVL re-ranking, and O(1) alert peek.
     */
    public void recordPrice(String symbol, int day, double price) {
        Stock s = portfolio.getStock(symbol);
        if (s != null) {
            s.addDailyPrice(price);
            portfolio.updateLeaderboard(symbol);
            PriceAlert[] triggered = s.checkAlerts();
            for (PriceAlert a : triggered) {
                System.out.printf("   🚨 Alert: %s hit threshold $%.2f (%s)%n", a.getSymbol(), a.getThreshold(), a.getType());
            }
        }
    }

    /**
     * Delegates to Segment Tree for range minimum price lookup.
     *
     * Time Complexity: O(log n) — visits at most 4 nodes per level down the Segment Tree height of log2(n).
     */
    public double queryMinPrice(String symbol, int dayL, int dayR) {
        Stock s = portfolio.getStock(symbol);
        return s != null ? s.getMinPrice(dayL, dayR) : -1;
    }

    /**
     * Delegates to Segment Tree for range maximum price lookup.
     *
     * Time Complexity: O(log n) — visits at most 4 nodes per level down the Segment Tree height of log2(n).
     */
    public double queryMaxPrice(String symbol, int dayL, int dayR) {
        Stock s = portfolio.getStock(symbol);
        return s != null ? s.getMaxPrice(dayL, dayR) : -1;
    }

    /**
     * Computes average price using Segment Tree range sum divided by range width.
     *
     * Time Complexity: O(log n) — O(log n) Segment Tree sum query followed by constant-time division.
     */
    public double queryAvgPrice(String symbol, int dayL, int dayR) {
        Stock s = portfolio.getStock(symbol);
        return s != null ? s.getAvgPrice(dayL, dayR) : -1;
    }

    /**
     * Creates Trade and enqueues to FIFO OrderQueue.
     *
     * Time Complexity: O(1) — instantiates Trade object and links node at queue tail in constant time.
     */
    public void placeBuyOrder(String symbol, int qty, double price) {
        Trade t = new Trade(symbol, "BUY", qty, price);
        orderQueue.enqueue(t);
        System.out.println("   📋 Enqueued BUY order: " + t);
    }

    /**
     * Creates Trade and enqueues to FIFO OrderQueue.
     *
     * Time Complexity: O(1) — instantiates Trade object and links node at queue tail in constant time.
     */
    public void placeSellOrder(String symbol, int qty, double price) {
        Trade t = new Trade(symbol, "SELL", qty, price);
        orderQueue.enqueue(t);
        System.out.println("   📋 Enqueued SELL order: " + t);
    }

    /**
     * Reverses the most recent trade using LIFO TradeStack and doubly linked list.
     *
     * Time Complexity: O(1) — pops undoStack (O(1)), pushes redoStack (O(1)), and unlinks tail transaction (O(1)).
     */
    public void undoLastTrade() {
        if (undoStack.isEmpty()) {
            System.out.println("   ⚠️ Nothing to undo.");
            return;
        }
        Trade t = undoStack.pop();
        redoStack.push(t);
        Stock s = portfolio.getStock(t.getSymbol());
        if (s != null) {
            s.removeLastTransaction();
        }
        System.out.printf("   ↩️ Undone trade: %s %d %s @ $%.2f%n", t.getType(), t.getQuantity(), t.getSymbol(), t.getPrice());
    }

    /**
     * Re-executes the most recently undone trade.
     *
     * Time Complexity: O(1) — pops redoStack (O(1)), pushes undoStack (O(1)), and appends to transaction list (O(1)).
     */
    public void redoLastUndo() {
        if (redoStack.isEmpty()) {
            System.out.println("   ⚠️ Nothing to redo.");
            return;
        }
        Trade t = redoStack.pop();
        undoStack.push(t);
        Stock s = portfolio.getStock(t.getSymbol());
        if (s != null) {
            s.addTransaction(t);
        }
        System.out.printf("   ↪️ Redone trade: %s %d %s @ $%.2f%n", t.getType(), t.getQuantity(), t.getSymbol(), t.getPrice());
    }

    /**
     * Displays performance leaderboard via AVL Tree in-order traversal.
     *
     * Time Complexity: O(n) — recursively traverses all n nodes in the AVL tree to display sorted rankings.
     */
    public void viewLeaderboard() {
        portfolio.displayLeaderboard();
    }

    /**
     * Checks all stocks for triggered price alerts via Min-Heap.
     *
     * Time Complexity: O(n) best / O(n log k) worst — checks each stock's heap root in O(1) and extracts triggered alerts in O(log k).
     */
    public void checkAlerts() {
        for (Stock s : portfolio.getAllStocks()) {
            PriceAlert[] triggered = s.checkAlerts();
            for (PriceAlert a : triggered) {
                System.out.printf("   🚨 Triggered Alert: %s %s $%.2f%n", a.getSymbol(), a.getType(), a.getThreshold());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HANDLER METHODS — one per menu option
    // ═══════════════════════════════════════════════════════════════════════

    // ── 1. Add Stock ──────────────────────────────────────────────────────

    /**
     * Prompts the user for a ticker symbol and company name, creates a
     * new Stock, and adds it to the portfolio.
     *
     * Time Complexity: O(log n) — validates input and adds stock to Portfolio AVL leaderboard in O(log n).
     */
    private void handleAddStock() {
        printHeader("ADD STOCK TO PORTFOLIO");

        System.out.print("   Enter stock symbol (e.g., MSFT): ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        if (symbol.isEmpty()) {
            System.out.println("   ❌ Symbol cannot be empty.\n");
            return;
        }
        if (portfolio.getStock(symbol) != null) {
            System.out.println("   ❌ Stock " + symbol + " already exists in portfolio.\n");
            return;
        }

        System.out.print("   Enter company name (e.g., Microsoft Corp.): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("   ❌ Company name cannot be empty.\n");
            return;
        }

        Stock stock = new Stock(symbol, name);
        portfolio.addStock(stock);
        System.out.println();
    }

    // ── 2. Record Daily Price ─────────────────────────────────────────────

    /**
     * Records a new daily closing price for a stock.
     *
     * Time Complexity: O(log n) — appends price, updates SegmentTree (O(log n)), re-balances AVL (O(log n)), and checks alerts.
     *
     * After recording:
     *   • The segment tree is point-updated (O(log n)).
     *   • The AVL leaderboard is refreshed (delete + insert, O(log n)).
     *   • Price alerts are checked against the new price.
     */
    private void handleRecordPrice() {
        printHeader("RECORD DAILY PRICE — Dynamic Array + Segment Tree Point Update");

        Stock stock = promptForStock();
        if (stock == null) return;

        System.out.printf("   Current day count for %s: %d%n", stock.getSymbol(), stock.getDayCount());
        System.out.print("   Enter closing price: $");
        double price = readDouble();
        if (price < 0) return;

        stock.addDailyPrice(price);

        // Update the AVL leaderboard with the new percent change.
        portfolio.updateLeaderboard(stock.getSymbol());

        // Check if any alerts were triggered by this price.
        PriceAlert[] triggered = stock.checkAlerts();
        if (triggered.length > 0) {
            System.out.println("   🚨 ALERTS TRIGGERED:");
            for (PriceAlert a : triggered) {
                System.out.printf("      ⚠️  %s alert: price $%.2f hit threshold $%.2f (%s)%n",
                        a.getSymbol(), stock.getCurrentPrice(), a.getThreshold(), a.getType());
            }
        }
        System.out.println();
    }

    // ── 3. View Price History ─────────────────────────────────────────────

    /**
     * Displays the complete daily price history for a stock in tabular form.
     *
     * Time Complexity: O(n) — iterates through n historical prices and prints table.
     */
    private void handleViewPriceHistory() {
        printHeader("PRICE HISTORY — Dynamic Array");

        Stock stock = promptForStock();
        if (stock == null) return;

        if (stock.getDayCount() == 0) {
            System.out.println("   📭 No price data recorded for " + stock.getSymbol() + ".\n");
            return;
        }

        System.out.printf("   📈 Price History for %s (%s):%n", stock.getSymbol(), stock.getName());
        stock.displayPriceHistory();
        System.out.println();
    }

    // ── 4. Range Query — Minimum Price (Segment Tree) ─────────────────────

    /**
     * Performs a range-minimum query on the stock's price history using
     * the segment tree.
     *
     * Time Complexity: O(log n) — queries SegmentTree for range minimum across [dayL, dayR].
     *
     * Educational output shows:
     *   • The query being formed
     *   • The result
     *   • The time complexity and why it's O(log n)
     */
    private void handleRangeQueryMin() {
        printHeader("RANGE QUERY — MINIMUM PRICE ⭐ Segment Tree");

        Stock stock = promptForStock();
        if (stock == null) return;

        int[] range = promptForDayRange(stock);
        if (range == null) return;
        int dayL = range[0], dayR = range[1];

        System.out.printf("   📊 Segment Tree Query: Finding MINIMUM price for %s from Day %d to Day %d%n",
                stock.getSymbol(), dayL, dayR);
        System.out.println("   ⏳ Traversing segment tree nodes...");

        double result = stock.getMinPrice(dayL, dayR);
        if (result >= 0) {
            int n = stock.getDayCount();
            int nodesVisited = (int) Math.ceil(2 * (Math.log(n) / Math.log(2)));
            System.out.printf("   ✅ Result: $%.2f%n", result);
            System.out.printf("   ⚡ Time Complexity: O(log n) — visited ~%d nodes out of %d total%n",
                    nodesVisited, n);
        }
        System.out.println();
    }

    // ── 5. Range Query — Maximum Price (Segment Tree) ─────────────────────

    /**
     * Performs a range-maximum query using the segment tree.
     *
     * Time Complexity: O(log n) — queries SegmentTree for range maximum across [dayL, dayR].
     */
    private void handleRangeQueryMax() {
        printHeader("RANGE QUERY — MAXIMUM PRICE ⭐ Segment Tree");

        Stock stock = promptForStock();
        if (stock == null) return;

        int[] range = promptForDayRange(stock);
        if (range == null) return;
        int dayL = range[0], dayR = range[1];

        System.out.printf("   📊 Segment Tree Query: Finding MAXIMUM price for %s from Day %d to Day %d%n",
                stock.getSymbol(), dayL, dayR);
        System.out.println("   ⏳ Traversing segment tree nodes...");

        double result = stock.getMaxPrice(dayL, dayR);
        if (result >= 0) {
            int n = stock.getDayCount();
            int nodesVisited = (int) Math.ceil(2 * (Math.log(n) / Math.log(2)));
            System.out.printf("   ✅ Result: $%.2f%n", result);
            System.out.printf("   ⚡ Time Complexity: O(log n) — visited ~%d nodes out of %d total%n",
                    nodesVisited, n);
        }
        System.out.println();
    }

    // ── 6. Range Query — Average Price (Segment Tree) ─────────────────────

    /**
     * Computes the average price over a day range using the segment tree's
     * range-sum query divided by the number of days.
     *
     * Time Complexity: O(log n) — queries SegmentTree range sum in O(log n) and divides by day count.
     */
    private void handleRangeQueryAvg() {
        printHeader("RANGE QUERY — AVERAGE PRICE ⭐ Segment Tree");

        Stock stock = promptForStock();
        if (stock == null) return;

        int[] range = promptForDayRange(stock);
        if (range == null) return;
        int dayL = range[0], dayR = range[1];

        System.out.printf("   📊 Segment Tree Query: Finding AVERAGE price for %s from Day %d to Day %d%n",
                stock.getSymbol(), dayL, dayR);
        System.out.println("   ⏳ Computing range sum then dividing by range length...");

        double result = stock.getAvgPrice(dayL, dayR);
        if (result >= 0) {
            int n = stock.getDayCount();
            int nodesVisited = (int) Math.ceil(2 * (Math.log(n) / Math.log(2)));
            System.out.printf("   ✅ Result: $%.2f%n", result);
            System.out.printf("   ⚡ Time Complexity: O(log n) — visited ~%d nodes out of %d total%n",
                    nodesVisited, n);
        }
        System.out.println();
    }

    // ── 7. View Leaderboard (AVL Tree) ────────────────────────────────────

    /**
     * Displays the performance leaderboard via in-order traversal of the
     * AVL tree, showing stocks ranked by daily percent change.
     *
     * Time Complexity: O(n) — in-order traversal of AVL tree to display stocks sorted by percent change.
     */
    private void handleViewLeaderboard() {
        printHeader("PERFORMANCE LEADERBOARD 🌳 AVL Tree");
        System.out.println("   🌳 AVL Tree: Performing in-order traversal for sorted leaderboard...");
        System.out.println("   ⚡ Time Complexity: O(n) traversal, O(log n) per insert/delete/rebalance");
        portfolio.displayLeaderboard();
        System.out.println();
    }

    // ── 8. Set Price Alert (Min-Heap) ─────────────────────────────────────

    /**
     * Creates a new price alert for a stock and inserts it into the
     * stock's min-heap.
     *
     * Time Complexity: O(log k) — creates PriceAlert and inserts into Min-Heap in logarithmic time.
     */
    private void handleSetAlert() {
        printHeader("SET PRICE ALERT 📊 Min-Heap");

        Stock stock = promptForStock();
        if (stock == null) return;

        System.out.printf("   Current price of %s: $%.2f%n", stock.getSymbol(), stock.getCurrentPrice());
        System.out.print("   Enter alert threshold price: $");
        double threshold = readDouble();
        if (threshold < 0) return;

        System.out.print("   Alert type — ABOVE or BELOW: ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!type.equals("ABOVE") && !type.equals("BELOW")) {
            System.out.println("   ❌ Invalid type. Must be ABOVE or BELOW.\n");
            return;
        }

        PriceAlert alert = new PriceAlert(stock.getSymbol(), threshold, type);
        stock.addAlert(alert);
        System.out.println("   📊 Min-Heap: Alert inserted. Heap property maintained via bubble-up.");
        System.out.println("   ⚡ Time Complexity: O(log n) insertion\n");
    }

    // ── 9. Check Price Alerts (Min-Heap) ──────────────────────────────────

    /**
     * Checks all stocks for triggered price alerts by scanning each
     * stock's min-heap against the current price.
     *
     * Time Complexity: O(k log k) — inspects alerts across portfolio stocks and extracts fired alerts from Min-Heaps.
     */
    private void handleCheckAlerts() {
        printHeader("CHECK PRICE ALERTS 📊 Min-Heap");

        System.out.println("   📊 Min-Heap: Scanning all stocks for triggered alerts...");
        boolean anyTriggered = false;

        for (Stock stock : portfolio.getAllStocks()) {
            PriceAlert[] triggered = stock.checkAlerts();
            if (triggered.length > 0) {
                anyTriggered = true;
                for (PriceAlert alert : triggered) {
                    System.out.printf("   🚨 %s: Price $%.2f triggered %s alert (threshold $%.2f)%n",
                            alert.getSymbol(), stock.getCurrentPrice(),
                            alert.getType(), alert.getThreshold());
                }
            }
        }

        if (!anyTriggered) {
            System.out.println("   ✅ No alerts triggered at current prices.");
        }
        System.out.println("   ⚡ Time Complexity: O(k log n) where k = triggered alerts\n");
    }

    // ── 10. Place Trade Order (Queue) ─────────────────────────────────────

    /**
     * Prompts the user for trade details and enqueues the order into
     * the OrderQueue (FIFO).
     *
     * Time Complexity: O(1) — instantiates Trade and enqueues to rear of OrderQueue.
     */
    private void handlePlaceTrade() {
        printHeader("PLACE TRADE ORDER 📋 Queue (FIFO)");

        Stock stock = promptForStock();
        if (stock == null) return;

        System.out.print("   Trade type — BUY or SELL: ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!type.equals("BUY") && !type.equals("SELL")) {
            System.out.println("   ❌ Invalid type. Must be BUY or SELL.\n");
            return;
        }

        System.out.print("   Quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
            if (qty <= 0) {
                System.out.println("   ❌ Quantity must be positive.\n");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("   ❌ Invalid quantity.\n");
            return;
        }

        System.out.print("   Price per share: $");
        double price = readDouble();
        if (price < 0) return;

        Trade trade = new Trade(stock.getSymbol(), type, qty, price);
        orderQueue.enqueue(trade);

        System.out.printf("   📋 Order queued: %s %d %s @ $%.2f%n",
                type, qty, stock.getSymbol(), price);
        System.out.printf("   📋 Queue size: %d pending order(s)%n", orderQueue.size());
        System.out.println("   ⚡ Time Complexity: O(1) enqueue\n");
    }

    // ── 11. Process Pending Orders (Queue) ────────────────────────────────

    /**
     * Dequeues and executes all pending orders from the OrderQueue.
     *
     * Time Complexity: O(m) — dequeues each of m orders in O(1), pushes to undoStack, and appends to transaction list.
     *
     * For each executed order:
     *   • The trade is pushed onto the undoStack (for undo support).
     *   • The trade is appended to the stock's TransactionList (linked list).
     *   • The redoStack is cleared (a new action invalidates redo history).
     */
    private void handleProcessOrders() {
        printHeader("PROCESS PENDING ORDERS 📋 Queue (FIFO)");

        if (orderQueue.isEmpty()) {
            System.out.println("   📭 No pending orders in the queue.\n");
            return;
        }

        System.out.printf("   📋 Processing %d pending order(s)...%n", orderQueue.size());
        System.out.println();

        // Clear redo stack — new actions invalidate redo history.
        redoStack = new TradeStack();

        int orderNum = 1;
        while (!orderQueue.isEmpty()) {
            Trade trade = orderQueue.dequeue();

            System.out.printf("   📋 Order #%d: %s %d %s @ $%.2f%n",
                    orderNum, trade.getType(), trade.getQuantity(),
                    trade.getSymbol(), trade.getPrice());

            // Push to undo stack.
            undoStack.push(trade);

            // Add to stock's transaction list.
            Stock stock = portfolio.getStock(trade.getSymbol());
            if (stock != null) {
                stock.addTransaction(trade);
                System.out.printf("      ✅ Executed. Added to %s transaction history.%n", trade.getSymbol());
            } else {
                System.out.printf("      ⚠️  Stock %s not found — trade recorded in undo stack only.%n",
                        trade.getSymbol());
            }

            System.out.println("      ↩️  Pushed to undo stack.");
            orderNum++;
        }
        System.out.println("\n   ⚡ Time Complexity: O(1) per dequeue, O(1) per stack push, O(1) per list append\n");
    }

    // ── 12. Undo Last Trade (Stack) ───────────────────────────────────────

    /**
     * Undoes the most recently executed trade.
     *
     * Time Complexity: O(1) — pops undoStack, unlinks last transaction, and pushes to redoStack in constant time.
     *
     * Steps:
     *   1. Pop the trade from undoStack.
     *   2. Remove it from the stock's transaction list (removeLast).
     *   3. Push it onto redoStack.
     *
     * This demonstrates the LIFO property of stacks — the most recent
     * action is always the first to be undone.
     */
    private void handleUndo() {
        printHeader("UNDO LAST TRADE ↩️ Stack (LIFO)");

        if (undoStack.isEmpty()) {
            System.out.println("   📭 Nothing to undo — undo stack is empty.\n");
            return;
        }

        Trade trade = undoStack.pop();
        System.out.printf("   ↩️  Undoing: %s %d %s @ $%.2f%n",
                trade.getType(), trade.getQuantity(),
                trade.getSymbol(), trade.getPrice());

        // Remove from stock's transaction list.
        Stock stock = portfolio.getStock(trade.getSymbol());
        if (stock != null) {
            stock.removeLastTransaction();
            System.out.printf("      🔗 Removed from %s transaction history (linked list removeLast).%n",
                    trade.getSymbol());
        }

        // Push to redo stack.
        redoStack.push(trade);
        System.out.println("      ↪️  Pushed to redo stack.");
        System.out.println("   ⚡ Time Complexity: O(1) pop, O(1) removeLast, O(1) push\n");
    }

    // ── 13. Redo Last Undo (Stack) ────────────────────────────────────────

    /**
     * Redoes the most recently undone trade.
     *
     * Time Complexity: O(1) — pops redoStack, re-adds to transaction list, and pushes to undoStack in constant time.
     *
     * Steps:
     *   1. Pop the trade from redoStack.
     *   2. Re-add it to the stock's transaction list.
     *   3. Push it back onto undoStack.
     */
    private void handleRedo() {
        printHeader("REDO LAST UNDO ↪️ Stack (LIFO)");

        if (redoStack.isEmpty()) {
            System.out.println("   📭 Nothing to redo — redo stack is empty.\n");
            return;
        }

        Trade trade = redoStack.pop();
        System.out.printf("   ↪️  Redoing: %s %d %s @ $%.2f%n",
                trade.getType(), trade.getQuantity(),
                trade.getSymbol(), trade.getPrice());

        // Re-add to stock's transaction list.
        Stock stock = portfolio.getStock(trade.getSymbol());
        if (stock != null) {
            stock.addTransaction(trade);
            System.out.printf("      🔗 Re-added to %s transaction history (linked list addLast).%n",
                    trade.getSymbol());
        }

        // Push back to undo stack.
        undoStack.push(trade);
        System.out.println("      ↩️  Pushed back to undo stack.");
        System.out.println("   ⚡ Time Complexity: O(1) pop, O(1) addLast, O(1) push\n");
    }

    // ── 14. View Transaction History (Linked List) ────────────────────────

    /**
     * Displays the full transaction history for a stock by traversing
     * its doubly-linked list from head to tail.
     *
     * Time Complexity: O(n) — forward traversal through doubly-linked list of n transactions.
     */
    private void handleViewTransactions() {
        printHeader("TRANSACTION HISTORY 🔗 Doubly-Linked List");

        Stock stock = promptForStock();
        if (stock == null) return;

        System.out.printf("   🔗 Transaction history for %s (doubly-linked list traversal):%n",
                stock.getSymbol());
        stock.displayTransactionHistory();
        System.out.println("   ⚡ Time Complexity: O(n) traversal\n");
    }

    // ── 15. View All Stocks ───────────────────────────────────────

    /**
     * Displays a summary table of all stocks in the portfolio.
     *
     * Time Complexity: O(n) — displays all n stocks in portfolio.
     */
    private void handleViewAllStocks() {
        printHeader("ALL PORTFOLIO STOCKS");
        portfolio.displayAllStocks();
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  16. COMPLETE DEMO — showcases every data structure automatically
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Runs an automated demonstration of every feature and data structure.
     *
     * Time Complexity: O(n log n) — executes end-to-end automated walkthrough of all 7 data structures.
     *
     * This is the KEY method for the DSA course presentation. It walks
     * through each data structure with narration, pausing between steps
     * so the audience can follow along.
     *
     * Steps:
     *   1. Show loaded stocks          (HashMap overview)
     *   2. Show price history          (Dynamic Array)
     *   3. Perform range queries       (Segment Tree)
     *   4. Show leaderboard            (AVL Tree in-order traversal)
     *   5. Set & check alerts          (Min-Heap)
     *   6. Place & process trade       (Queue)
     *   7. Undo & redo the trade       (Stack)
     *   8. Show transaction history    (Doubly-Linked List)
     */
    private void runCompleteDemo() {
        System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║              COMPLETE DEMO — ALL DATA STRUCTURES              ║");
        System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ── Step 1: Portfolio Overview ─────────────────────────────────────
        demoPause("STEP 1: Portfolio Overview (HashMap lookup)");
        portfolio.displayAllStocks();
        System.out.println();

        // ── Step 2: Price History (Dynamic Array) ─────────────────────────
        demoPause("STEP 2: Price History — Dynamic Array");
        Stock aapl = portfolio.getStock("AAPL");
        if (aapl != null) {
            System.out.println("   📈 Showing AAPL price history (stored in a dynamic array):");
            System.out.println("   💡 Array doubles capacity when full — amortised O(1) append.");
            aapl.displayPriceHistory();
        }
        System.out.println();

        // ── Step 3: Range Queries (Segment Tree) ──────────────────────────
        demoPause("STEP 3: Range Queries — ⭐ Segment Tree");
        if (aapl != null) {
            int n = aapl.getDayCount();
            int nodesVisited = (int) Math.ceil(2 * (Math.log(n) / Math.log(2)));

            // Min query: Day 3–6
            System.out.println("   📊 Query 1: MINIMUM price for AAPL, Day 3 to Day 6");
            double minVal = aapl.getMinPrice(3, 6);
            System.out.printf("      ✅ Min = $%.2f%n", minVal);
            System.out.printf("      ⚡ O(log n) — ~%d nodes visited out of %d%n%n", nodesVisited, n);

            // Max query: Day 1–8
            System.out.println("   📊 Query 2: MAXIMUM price for AAPL, Day 1 to Day 8");
            double maxVal = aapl.getMaxPrice(1, 8);
            System.out.printf("      ✅ Max = $%.2f%n", maxVal);
            System.out.printf("      ⚡ O(log n) — ~%d nodes visited out of %d%n%n", nodesVisited, n);

            // Avg query: Day 2–7
            System.out.println("   📊 Query 3: AVERAGE price for AAPL, Day 2 to Day 7");
            double avgVal = aapl.getAvgPrice(2, 7);
            System.out.printf("      ✅ Avg = $%.2f%n", avgVal);
            System.out.printf("      ⚡ O(log n) for sum, then divide by range length%n");
        }
        System.out.println();

        // ── Step 4: Leaderboard (AVL Tree) ────────────────────────────────
        demoPause("STEP 4: Performance Leaderboard — 🌳 AVL Tree");
        System.out.println("   🌳 The AVL tree keeps stocks sorted by daily % change.");
        System.out.println("   🌳 After every price update, the old node is DELETED and a");
        System.out.println("      new one is INSERTED — both O(log n) with auto-rebalancing.");
        portfolio.displayLeaderboard();
        System.out.println();

        // ── Step 5: Price Alerts (Min-Heap) ───────────────────────────────
        demoPause("STEP 5: Price Alerts — 📊 Min-Heap");
        System.out.println("   📊 Min-Heap: Alerts are ordered by threshold (lowest at root).");
        System.out.println("   📊 Setting a NEW alert: AAPL ABOVE $175");
        if (aapl != null) {
            aapl.addAlert(new PriceAlert("AAPL", 175.0, "ABOVE"));
        }
        System.out.println();
        System.out.println("   📊 Checking all alerts against current prices...");
        for (Stock stock : portfolio.getAllStocks()) {
            PriceAlert[] triggered = stock.checkAlerts();
            if (triggered.length > 0) {
                for (PriceAlert a : triggered) {
                    System.out.printf("      🚨 %s: $%.2f triggered %s alert (threshold $%.2f)%n",
                            a.getSymbol(), stock.getCurrentPrice(), a.getType(), a.getThreshold());
                }
            }
        }
        System.out.println("   ✅ Alert check complete.");
        System.out.println();

        // ── Step 6: Place & Process Trade (Queue) ─────────────────────────
        demoPause("STEP 6: Trade Orders — 📋 Queue (FIFO)");

        // First process any existing orders in the queue
        if (!orderQueue.isEmpty()) {
            System.out.printf("   📋 Processing %d existing pending order(s) first...%n", orderQueue.size());
            redoStack = new TradeStack();
            while (!orderQueue.isEmpty()) {
                Trade existingTrade = orderQueue.dequeue();
                undoStack.push(existingTrade);
                Stock s = portfolio.getStock(existingTrade.getSymbol());
                if (s != null) s.addTransaction(existingTrade);
                System.out.printf("      ✅ Executed: %s %d %s @ $%.2f%n",
                        existingTrade.getType(), existingTrade.getQuantity(),
                        existingTrade.getSymbol(), existingTrade.getPrice());
            }
            System.out.println();
        }

        System.out.println("   📋 Placing new order: SELL 5 TSLA @ $250.00");
        Trade demoTrade = new Trade("TSLA", "SELL", 5, 250.0);
        orderQueue.enqueue(demoTrade);
        System.out.println("   📋 Queue: enqueue() — O(1). FIFO order preserved.");
        System.out.println();
        System.out.println("   📋 Processing the order...");
        Trade processed = orderQueue.dequeue();
        undoStack.push(processed);
        Stock tsla = portfolio.getStock("TSLA");
        if (tsla != null) tsla.addTransaction(processed);
        System.out.printf("      ✅ Executed: %s %d %s @ $%.2f%n",
                processed.getType(), processed.getQuantity(),
                processed.getSymbol(), processed.getPrice());
        System.out.println("      ↩️  Pushed to undo stack.");
        // Clear redo stack since a new action was performed
        redoStack = new TradeStack();
        System.out.println();

        // ── Step 7: Undo & Redo (Stack) ───────────────────────────────────
        demoPause("STEP 7: Undo & Redo — ↩️↪️ Stack (LIFO)");

        System.out.println("   ↩️  UNDO: Popping last trade from undo stack...");
        if (!undoStack.isEmpty()) {
            Trade undone = undoStack.pop();
            System.out.printf("      Undone: %s %d %s @ $%.2f%n",
                    undone.getType(), undone.getQuantity(),
                    undone.getSymbol(), undone.getPrice());
            Stock undoStock = portfolio.getStock(undone.getSymbol());
            if (undoStock != null) undoStock.removeLastTransaction();
            redoStack.push(undone);
            System.out.println("      🔗 Removed from transaction list (linked list removeLast).");
            System.out.println("      ↪️  Pushed to redo stack.");
        }
        System.out.println();

        System.out.println("   ↪️  REDO: Popping from redo stack...");
        if (!redoStack.isEmpty()) {
            Trade redone = redoStack.pop();
            System.out.printf("      Redone: %s %d %s @ $%.2f%n",
                    redone.getType(), redone.getQuantity(),
                    redone.getSymbol(), redone.getPrice());
            Stock redoStock = portfolio.getStock(redone.getSymbol());
            if (redoStock != null) redoStock.addTransaction(redone);
            undoStack.push(redone);
            System.out.println("      🔗 Re-added to transaction list (linked list addLast).");
            System.out.println("      ↩️  Pushed back to undo stack.");
        }
        System.out.println();

        // ── Step 8: Transaction History (Doubly-Linked List) ──────────────
        demoPause("STEP 8: Transaction History — 🔗 Doubly-Linked List");
        System.out.println("   🔗 The doubly-linked list supports O(1) addLast and removeLast,");
        System.out.println("      making it ideal for maintaining transaction history with undo.");
        System.out.println();
        if (tsla != null) {
            System.out.println("   Transaction History for TSLA:");
            tsla.displayTransactionHistory();
        }
        if (aapl != null && aapl.getDayCount() > 0) {
            System.out.println("   Transaction History for AAPL:");
            aapl.displayTransactionHistory();
        }
        System.out.println();

        // ── Summary ───────────────────────────────────────────────────────
        System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                    DEMO COMPLETE! 🎉                          ║");
        System.out.println("  ╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("  ║  Data Structures Demonstrated:                                ║");
        System.out.println("  ║    1. Dynamic Array      — Price history storage               ║");
        System.out.println("  ║    2. Segment Tree       — O(log n) range min/max/sum queries  ║");
        System.out.println("  ║    3. AVL Tree           — Self-balancing leaderboard          ║");
        System.out.println("  ║    4. Min-Heap           — Priority-based price alerts         ║");
        System.out.println("  ║    5. Queue (FIFO)       — Pending trade order processing      ║");
        System.out.println("  ║    6. Stack (LIFO)       — Undo / Redo trade operations        ║");
        System.out.println("  ║    7. Doubly-Linked List — Transaction history per stock       ║");
        System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UTILITY / HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Prints a formatted section header for each menu action.
     *
     * Time Complexity: O(1) — prints header banner to console.
     *
     * @param title the header text to display
     */
    private void printHeader(String title) {
        System.out.println("  ───────────────────────────────────────────────────────────────");
        System.out.println("   " + title);
        System.out.println("  ───────────────────────────────────────────────────────────────");
    }

    /**
     * Prompts the user for a stock symbol and looks it up in the portfolio.
     *
     * Time Complexity: O(1) average — reads symbol from console and retrieves stock via HashMap.
     *
     * @return the Stock object, or null if not found
     */
    private Stock promptForStock() {
        System.out.print("   Enter stock symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = portfolio.getStock(symbol);
        if (stock == null) {
            System.out.printf("   ❌ Stock %s not found in portfolio.%n%n", symbol);
            return null;
        }
        return stock;
    }

    /**
     * Prompts the user for a day range [dayL, dayR] and validates it.
     *
     * Time Complexity: O(1) — reads and validates day boundaries in constant time.
     *
     * @param stock the stock whose day count determines the valid range
     * @return an int[] of {dayL, dayR}, or null if input is invalid
     */
    private int[] promptForDayRange(Stock stock) {
        System.out.printf("   Available range: Day 1 to Day %d%n", stock.getDayCount());
        System.out.print("   Enter start day (dayL): ");
        int dayL;
        try {
            dayL = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("   ❌ Invalid day number.\n");
            return null;
        }

        System.out.print("   Enter end day (dayR):   ");
        int dayR;
        try {
            dayR = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("   ❌ Invalid day number.\n");
            return null;
        }

        if (dayL < 1 || dayR < dayL || dayR > stock.getDayCount()) {
            System.out.printf("   ❌ Invalid range [%d, %d]. Must satisfy: 1 ≤ dayL ≤ dayR ≤ %d%n%n",
                    dayL, dayR, stock.getDayCount());
            return null;
        }

        return new int[]{dayL, dayR};
    }

    /**
     * Reads a double value from the console with error handling.
     *
     * Time Complexity: O(1) — parses double input from console in constant time.
     *
     * @return the parsed double, or -1 if input is invalid
     */
    private double readDouble() {
        try {
            double val = Double.parseDouble(scanner.nextLine().trim());
            if (val < 0) {
                System.out.println("   ❌ Value must be non-negative.\n");
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            System.out.println("   ❌ Invalid number.\n");
            return -1;
        }
    }

    /**
     * Pauses the demo between steps so the audience can follow along.
     * Displays a step title and waits for the user to press Enter.
     *
     * Time Complexity: O(1) — prints banner and pauses for Enter key.
     *
     * @param stepTitle description of the next demo step
     */
    private void demoPause(String stepTitle) {
        System.out.println("  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │  " + String.format("%-57s", stepTitle) + "│");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        System.out.print("   Press Enter to continue...");
        scanner.nextLine();
        System.out.println();
    }
}
