/**
 * ============================================================================
 *  Stock.java — Core entity representing a single stock in the portfolio
 * ============================================================================
 *
 * PURPOSE:
 *   This class models one stock (e.g., AAPL — Apple Inc.) and acts as the
 *   hub that ties together several data structures:
 *
 *   ┌──────────────────────────────────────────────────────────────────┐
 *   │  Stock                                                          │
 *   │  ├── priceHistory[]        — dynamic array of daily prices      │
 *   │  ├── SegmentTree           — O(log n) range queries on prices   │
 *   │  ├── TransactionList       — doubly-linked list of trades       │
 *   │  └── MinHeap               — min-heap of price alerts           │
 *   └──────────────────────────────────────────────────────────────────┘
 *
 * DATA STRUCTURE HIGHLIGHTS:
 *   • Dynamic Array   — priceHistory resizes itself (doubles capacity)
 *   • Segment Tree    — answers min / max / sum range queries in O(log n)
 *   • Doubly Linked List — O(1) append / remove-last for transactions
 *   • Min-Heap        — efficiently retrieves the lowest-threshold alert
 *
 * DESIGN DECISION (Segment Tree growth):
 *   We pre-allocate a segment tree with room for MAX_DAYS = 1000 entries.
 *   Unused slots stay at 0. When a new daily price arrives we perform a
 *   *point update* at the correct index, keeping the tree up-to-date in
 *   O(log n). All queries are restricted to the valid range [1, dayCount]
 *   so the zero-filled slots are never surfaced to the user.
 *
 * @author  DSA Course Project
 * @version 1.0
 */
public class Stock {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Maximum number of days the segment tree can accommodate. */
    private static final int MAX_DAYS = 1000;

    /** Initial capacity for the priceHistory dynamic array. */
    private static final int INITIAL_CAPACITY = 100;

    // ── Instance Fields ───────────────────────────────────────────────────

    /** Ticker symbol (e.g., "AAPL"). */
    private String symbol;

    /** Full company name (e.g., "Apple Inc."). */
    private String companyName;

    /**
     * Dynamic array holding daily closing prices.
     * Index 0 → Day 1, Index 1 → Day 2, … Index dayCount-1 → Day dayCount.
     *
     * When dayCount == priceHistory.length the array doubles its capacity,
     * exactly like java.util.ArrayList works internally.
     */
    private double[] priceHistory;

    /** Number of days for which we have a recorded price. */
    private int dayCount;

    /** Most recently recorded closing price. */
    private double currentPrice;

    /** Closing price from the previous day (used for % change calculation). */
    private double previousPrice;

    /**
     * Daily percent change: ((current − previous) / previous) × 100.
     * Stored so the AVL leaderboard can use it as a sort key.
     */
    private double percentChange;

    /**
     * Segment Tree built over priceHistory.
     * Supports O(log n) range-min, range-max, and range-sum queries.
     * Pre-allocated for MAX_DAYS entries; unused entries remain 0.
     */
    private SegmentTree segTree;

    /**
     * Doubly-linked list recording every buy / sell transaction for this stock.
     * Supports O(1) append and O(1) remove-last (needed for undo).
     */
    private TransactionList transactions;

    /**
     * Min-Heap of PriceAlert objects.
     * The alert with the *lowest* threshold is at the root, making it
     * efficient to check whether the current price has breached any alert.
     */
    private MinHeap alerts;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Creates a new Stock with the given symbol and company name.
     *
     * Time Complexity: O(1) — allocates initial price array, SegmentTree, TransactionList, and MinHeap in constant time.
     *
     * Initialisation steps:
     *   1. priceHistory is allocated with INITIAL_CAPACITY (100) slots.
     *   2. A segment tree is pre-built for MAX_DAYS (1000) empty slots.
     *   3. An empty TransactionList (doubly-linked list) is created.
     *   4. An empty MinHeap for price alerts is created.
     *
     * @param symbol      ticker symbol, e.g. "AAPL"
     * @param companyName full company name, e.g. "Apple Inc."
     */
    public Stock(String symbol, String companyName) {
        this.symbol = symbol.toUpperCase();
        this.companyName = companyName;
        this.priceHistory = new double[INITIAL_CAPACITY];
        this.dayCount = 0;
        this.currentPrice = 0.0;
        this.previousPrice = 0.0;
        this.percentChange = 0.0;

        // Segment tree is initialised with all zeros — point updates fill it.
        this.segTree = new SegmentTree(MAX_DAYS);

        // Doubly-linked list starts empty.
        this.transactions = new TransactionList();

        // Min-heap starts empty.
        this.alerts = new MinHeap();
    }

    // ── Price Management ──────────────────────────────────────────────────

    /**
     * Records a new daily closing price for this stock.
     *
     * Algorithm:
     *   1. If the internal array is full, create a new array with 2× capacity
     *      and copy all elements over (amortised O(1) per insertion).
     *   2. Store the price at priceHistory[dayCount].
     *   3. Update previousPrice, currentPrice, and compute percentChange.
     *   4. Perform a *point update* on the segment tree at position dayCount
     *      (0-indexed internally, the tree maps this to its own 1-indexed
     *      structure). This is O(log n).
     *   5. Increment dayCount.
     *
     * Time Complexity: O(1) amortized array insertion + O(log n) Segment Tree point update.
     *
     * @param price the closing price for the next sequential day
     */
    public void addDailyPrice(double price) {
        // ── Step 1: Dynamic array resize (doubling strategy) ──
        if (dayCount == priceHistory.length) {
            double[] bigger = new double[priceHistory.length * 2];
            // Manual copy — we could use System.arraycopy but this is more
            // educational, showing the exact O(n) copy cost.
            for (int i = 0; i < dayCount; i++) {
                bigger[i] = priceHistory[i];
            }
            priceHistory = bigger;
            System.out.println("   💡 Dynamic Array: Resized priceHistory capacity to " + bigger.length);
        }

        // ── Step 2: Store the price ──
        priceHistory[dayCount] = price;

        // ── Step 3: Update current / previous / percent change ──
        previousPrice = currentPrice;
        currentPrice = price;
        if (dayCount > 0 && previousPrice != 0) {
            // percentChange = ((new − old) / old) × 100
            percentChange = ((currentPrice - previousPrice) / previousPrice) * 100.0;
        } else {
            percentChange = 0.0;
        }

        // ── Step 4: Point-update the segment tree ──
        // The segment tree uses 0-based indexing internally.
        segTree.update(dayCount, price);

        // ── Step 5: Advance the day counter ──
        dayCount++;

        System.out.printf("   ✅ %s Day %d price recorded: $%.2f%n", symbol, dayCount, price);
    }

    /**
     * Overloaded method matching UML specification: addDailyPrice(day, price).
     *
     * Time Complexity: O(log n) — delegates to addDailyPrice(price) performing O(1) amortized append + O(log n) Segment Tree update.
     */
    public void addDailyPrice(int day, double price) {
        addDailyPrice(price);
    }

    // ── Range Queries (delegated to Segment Tree) ─────────────────────────

    /**
     * Returns the minimum closing price in the day range [dayL, dayR].
     *
     * Days are 1-indexed from the user's perspective:
     *   Day 1 → priceHistory[0], Day 2 → priceHistory[1], …
     *
     * We convert to 0-based indices before querying the segment tree.
     *
     * Time Complexity: O(log n) — delegates to Segment Tree range query which visits at most 4 nodes per level.
     *
     * @param dayL start day (1-indexed, inclusive)
     * @param dayR end day (1-indexed, inclusive)
     * @return minimum price in the range, or -1 if the range is invalid
     */
    public double getMinPrice(int dayL, int dayR) {
        if (!isValidRange(dayL, dayR)) {
            System.out.printf("   ❌ Invalid range [%d, %d]. Valid range is [1, %d].%n",
                    dayL, dayR, dayCount);
            return -1;
        }
        // Convert 1-based day numbers to 0-based array indices.
        return segTree.queryMin(dayL - 1, dayR - 1);
    }

    /**
     * Returns the maximum closing price in the day range [dayL, dayR].
     * Same conventions as {@link #getMinPrice}.
     *
     * Time Complexity: O(log n) — delegates to Segment Tree range query which visits at most 4 nodes per level.
     *
     * @param dayL start day (1-indexed, inclusive)
     * @param dayR end day (1-indexed, inclusive)
     * @return maximum price in the range, or -1 if the range is invalid
     */
    public double getMaxPrice(int dayL, int dayR) {
        if (!isValidRange(dayL, dayR)) {
            System.out.printf("   ❌ Invalid range [%d, %d]. Valid range is [1, %d].%n",
                    dayL, dayR, dayCount);
            return -1;
        }
        return segTree.queryMax(dayL - 1, dayR - 1);
    }

    /**
     * Returns the average closing price in the day range [dayL, dayR].
     *
     * Uses the segment tree's range-sum query divided by the number of days:
     *   avg = sum(dayL … dayR) / (dayR − dayL + 1)
     *
     * Time Complexity: O(log n) — performs an O(log n) Segment Tree sum query followed by constant-time division.
     *
     * @param dayL start day (1-indexed, inclusive)
     * @param dayR end day (1-indexed, inclusive)
     * @return average price in the range, or -1 if the range is invalid
     */
    public double getAvgPrice(int dayL, int dayR) {
        if (!isValidRange(dayL, dayR)) {
            System.out.printf("   ❌ Invalid range [%d, %d]. Valid range is [1, %d].%n",
                    dayL, dayR, dayCount);
            return -1;
        }
        double sum = segTree.querySum(dayL - 1, dayR - 1);
        int count = dayR - dayL + 1;
        return sum / count;
    }

    /**
     * Returns the sum of closing prices in the day range [dayL, dayR].
     * Matches UML specification: getSumPrice(dayL, dayR).
     *
     * Time Complexity: O(log n) — performs an O(log n) Segment Tree sum query.
     *
     * @param dayL start day (1-indexed)
     * @param dayR end day (1-indexed)
     * @return sum of prices in the range
     */
    public double getSumPrice(int dayL, int dayR) {
        if (!isValidRange(dayL, dayR)) {
            return -1;
        }
        return segTree.querySum(dayL - 1, dayR - 1);
    }

    /**
     * Validates that a user-supplied [dayL, dayR] range is legal.
     *
     * Time Complexity: O(1) — basic boundary checks in constant time.
     *
     * @param dayL start day (1-indexed)
     * @param dayR end day (1-indexed)
     * @return true if 1 ≤ dayL ≤ dayR ≤ dayCount
     */
    private boolean isValidRange(int dayL, int dayR) {
        return dayL >= 1 && dayR >= dayL && dayR <= dayCount;
    }

    // ── Price Alerts (Min-Heap) ───────────────────────────────────────────

    /**
     * Adds a price alert to this stock's min-heap.
     *
     * The heap is ordered by threshold price, so the alert with the
     * smallest threshold sits at the root. This makes checking for
     * "BELOW" alerts very efficient — if the root's threshold is above
     * the current price, no "BELOW" alert can possibly be triggered.
     *
     * Time Complexity: O(log k) — inserts alert into Min-Heap and bubbles up along tree height of log2(k).
     *
     * @param alert the PriceAlert to enqueue
     */
    public void addAlert(PriceAlert alert) {
        alerts.insert(alert);
        System.out.printf("   🔔 Alert set: %s when %s price goes %s $%.2f%n",
                symbol, symbol, alert.getType(), alert.getThreshold());
    }

    /**
     * Checks all queued alerts against the current price and returns
     * any that have been triggered.
     *
     * Time Complexity: O(k log k) — extracts and re-inserts k alerts into Min-Heap in worst case.
     *
     * Logic:
     *   • For "BELOW" alerts → triggered if currentPrice ≤ threshold.
     *   • For "ABOVE" alerts → triggered if currentPrice ≥ threshold.
     *
     * We repeatedly peek at the heap root. If it is triggered, we extract
     * it and add it to the results. Because the heap is a MIN-heap sorted
     * by threshold, once the root is *not* triggered for a "BELOW" check,
     * no deeper element can be triggered either. However, "ABOVE" alerts
     * can be anywhere in the heap, so we do a full scan by extracting all,
     * checking, and re-inserting the non-triggered ones.
     *
     * @return array of triggered PriceAlert objects (may be empty)
     */
    public PriceAlert[] checkAlerts() {
        if (alerts.isEmpty()) {
            return new PriceAlert[0];
        }

        // Temporary storage for alerts that are NOT triggered — will be
        // re-inserted into the heap after the scan.
        PriceAlert[] tempNotTriggered = new PriceAlert[alerts.size()];
        int notTriggeredCount = 0;

        // Temporary storage for triggered alerts to return.
        PriceAlert[] triggered = new PriceAlert[alerts.size()];
        int triggeredCount = 0;

        // Extract every alert, check it, and sort into triggered / not.
        int originalSize = alerts.size();
        for (int i = 0; i < originalSize; i++) {
            PriceAlert alert = alerts.extractMin();
            boolean fire = false;

            if (alert.getType().equalsIgnoreCase("BELOW") && currentPrice <= alert.getThreshold()) {
                fire = true;
            } else if (alert.getType().equalsIgnoreCase("ABOVE") && currentPrice >= alert.getThreshold()) {
                fire = true;
            }

            if (fire) {
                triggered[triggeredCount++] = alert;
            } else {
                tempNotTriggered[notTriggeredCount++] = alert;
            }
        }

        // Re-insert un-triggered alerts back into the heap.
        for (int i = 0; i < notTriggeredCount; i++) {
            alerts.insert(tempNotTriggered[i]);
        }

        // Build a right-sized result array.
        PriceAlert[] result = new PriceAlert[triggeredCount];
        for (int i = 0; i < triggeredCount; i++) {
            result[i] = triggered[i];
        }
        return result;
    }

    // ── Transaction Management (Doubly-Linked List) ───────────────────────

    /**
     * Appends a trade to this stock's transaction history.
     * Delegates to TransactionList.addLast() — O(1) doubly-linked list append.
     *
     * Time Complexity: O(1) — constant-time pointer linking at the tail of the doubly-linked list.
     *
     * @param trade the Trade record to append
     */
    public void addTransaction(Trade trade) {
        transactions.addLast(trade);
    }

    /**
     * Removes the most recent transaction from the linked list.
     * Used during "undo" operations.
     *
     * Time Complexity: O(1) — constant-time pointer unlinking at the tail without list traversal.
     *
     * @return the removed Trade, or null if the list is empty
     */
    public Trade removeLastTransaction() {
        return transactions.removeLast();
    }

    /**
     * Prints every transaction in chronological order.
     * Walks the doubly-linked list from head → tail.
     *
     * Time Complexity: O(n) — traverses all n transactions forward in doubly-linked list.
     */
    public void displayTransactionHistory() {
        transactions.displayAll();
    }

    // ── Display Helpers ───────────────────────────────────────────────────

    /**
     * Prints the complete price history in a nicely formatted table.
     *
     * Time Complexity: O(n) — iterates through n recorded days and prints formatted rows.
     *
     * Example output:
     *   ┌──────┬────────────┐
     *   │ Day  │   Price    │
     *   ├──────┼────────────┤
     *   │   1  │   $150.00  │
     *   │   2  │   $148.00  │
     *   └──────┴────────────┘
     */
    public void displayPriceHistory() {
        System.out.println("   ┌──────┬────────────┐");
        System.out.println("   │ Day  │   Price    │");
        System.out.println("   ├──────┼────────────┤");
        for (int i = 0; i < dayCount; i++) {
            System.out.printf("   │ %4d │ $%8.2f │%n", i + 1, priceHistory[i]);
        }
        System.out.println("   └──────┴────────────┘");
    }

    // ── Getters ───────────────────────────────────────────────────────────

    /**
     * Time Complexity: O(1) — constant-time reference field access.
     * @return the ticker symbol (e.g., "AAPL")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Time Complexity: O(1) — constant-time reference field access.
     * @return the full company name (e.g., "Apple Inc.")
     */
    public String getName() {
        return companyName;
    }

    /**
     * Time Complexity: O(1) — constant-time primitive field access.
     * @return the most recently recorded closing price
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Time Complexity: O(1) — constant-time primitive field access.
     * @return daily percent change vs. the prior day's close
     */
    public double getPercentChange() {
        return percentChange;
    }

    /**
     * Time Complexity: O(1) — constant-time primitive field access.
     * @return the number of days for which prices have been recorded
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Time Complexity: O(n) — copies n daily price records into a fresh array.
     * @return array of recorded daily prices
     */
    public double[] getPrices() {
        return java.util.Arrays.copyOf(priceHistory, dayCount);
    }

    /**
     * Time Complexity: O(1) — constant-time reference accessor.
     * @return the transaction list
     */
    public TransactionList getTransactions() {
        return transactions;
    }

    /**
     * Time Complexity: O(1) — constant-time reference accessor.
     * @return the alerts min-heap
     */
    public MinHeap getAlerts() {
        return alerts;
    }

    /**
     * Time Complexity: O(1) — constant-time reference accessor.
     * @return the segment tree
     */
    public SegmentTree getSegmentTree() {
        return segTree;
    }

    // ── Portfolio Holdings & Position Management ─────────────────────────
    private int sharesOwned = 0;
    private double totalCost = 0.0;

    /**
     * Time Complexity: O(1) — constant-time primitive accessor.
     */
    public int getSharesOwned() { return sharesOwned; }

    /**
     * Time Complexity: O(1) — constant-time primitive accessor.
     */
    public double getTotalCost() { return totalCost; }

    /**
     * Time Complexity: O(1) — constant-time primitive division.
     */
    public double getAvgBuyPrice() { return sharesOwned > 0 ? totalCost / sharesOwned : 0.0; }

    /**
     * Time Complexity: O(1) — constant-time multiplication.
     */
    public double getMarketValue() { return sharesOwned * currentPrice; }

    /**
     * Time Complexity: O(1) — constant-time subtraction.
     */
    public double getUnrealizedPnL() { return getMarketValue() - totalCost; }

    /**
     * Time Complexity: O(1) — constant-time arithmetic evaluation.
     */
    public double getUnrealizedPnLPercent() { return totalCost > 0 ? ((getMarketValue() - totalCost) / totalCost) * 100.0 : 0.0; }

    /**
     * Time Complexity: O(1) — constant-time arithmetic updates to position and cost basis.
     */
    public void applyTrade(String type, int qty, double price) {
        if ("BUY".equalsIgnoreCase(type)) {
            sharesOwned += qty;
            totalCost += (qty * price);
        } else if ("SELL".equalsIgnoreCase(type)) {
            if (sharesOwned <= qty) {
                sharesOwned = 0;
                totalCost = 0.0;
            } else {
                double avg = getAvgBuyPrice();
                sharesOwned -= qty;
                totalCost -= (qty * avg);
            }
        }
    }

    /**
     * Time Complexity: O(1) — constant-time inversion of trade position.
     */
    public void rollbackTrade(String type, int qty, double price) {
        if ("BUY".equalsIgnoreCase(type)) {
            applyTrade("SELL", qty, price);
        } else if ("SELL".equalsIgnoreCase(type)) {
            applyTrade("BUY", qty, price);
        }
    }
}
