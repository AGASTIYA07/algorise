import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 *  Portfolio.java — Manages all stocks and the performance leaderboard
 * ============================================================================
 *
 * PURPOSE:
 *   The Portfolio is the central registry of all tracked stocks. It provides
 *   two views into the data:
 *
 *   1. **Hash Map (java.util.HashMap)** — O(1) lookup by ticker symbol.
 *      We use the JDK HashMap here because hashing is NOT one of the
 *      data structures being demonstrated in this project.
 *
 *   2. **AVL Tree (custom implementation)** — Maintains stocks sorted by
 *      daily percent-change so we can display a live leaderboard.
 *      The AVL tree IS one of the six data structures being showcased.
 *
 *   ┌───────────────────────────────────────────────────────────────────┐
 *   │  Portfolio                                                       │
 *   │  ├── HashMap<String, Stock>   — O(1) symbol → Stock lookup       │
 *   │  └── AVLTree                  — sorted by %change (leaderboard)  │
 *   └───────────────────────────────────────────────────────────────────┘
 *
 * AVL LEADERBOARD DESIGN:
 *   The AVL tree is keyed by (percentChange, symbol). When a stock's
 *   price changes, its old node is deleted and a new one is inserted
 *   with the updated key. This ensures the tree is always correctly
 *   sorted and balanced (height difference ≤ 1 at every node).
 *
 *   An in-order traversal yields all stocks sorted by percent change
 *   in ascending order (worst → best), which we reverse for display.
 *
 * @author  DSA Course Project
 * @version 1.0
 */
public class Portfolio {

    // ── Instance Fields ───────────────────────────────────────────────────

    /**
     * Primary storage: maps ticker symbol → Stock object.
     * Using java.util.HashMap (NOT a demonstrated data structure).
     */
    private HashMap<String, Stock> stocks;

    /**
     * AVL Tree used as a leaderboard sorted by daily percent change.
     *
     * Each node stores:
     *   key   = percent change (double)
     *   value = ticker symbol  (String)
     *
     * Because two stocks could theoretically have the same percent change,
     * the AVL tree must handle duplicate keys. Our AVL implementation
     * stores the symbol alongside the key to distinguish nodes.
     */
    private AVLTree leaderboard;

    /**
     * Parallel map that remembers the *last inserted* percent change for
     * each symbol. This lets us delete the old AVL node when a stock's
     * percent change is updated.
     *
     * Without this, we would have to search the AVL tree by symbol, which
     * is O(n) in the worst case. By remembering the old key we can do a
     * precise O(log n) delete.
     */
    private HashMap<String, Double> lastPercentChange;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Creates an empty portfolio.
     *
     * Time Complexity: O(1) — instantiates empty HashMaps and AVLTree in constant time.
     */
    public Portfolio() {
        this.stocks = new HashMap<>();
        this.leaderboard = new AVLTree();
        this.lastPercentChange = new HashMap<>();
    }

    // ── Stock Management ──────────────────────────────────────────────────

    /**
     * Adds a stock to the portfolio.
     *
     * Steps:
     *   1. Put into HashMap for O(1) lookup.
     *   2. Insert into the AVL tree with key = 0.0 (no change yet).
     *   3. Record the initial percent change (0.0) in lastPercentChange.
     *
     * Time Complexity: O(log n) — O(1) average HashMap insert plus O(log n) AVL tree leaderboard insertion.
     *
     * @param stock the Stock to add
     */
    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);

        // Insert into AVL tree leaderboard
        leaderboard.insert(stock);
        lastPercentChange.put(stock.getSymbol(), stock.getPercentChange());

        System.out.printf("   ✅ %s (%s) added to portfolio.%n",
                stock.getSymbol(), stock.getName());
    }

    /**
     * Removes a stock from the portfolio.
     *
     * Steps:
     *   1. Look up the last known percent change.
     *   2. Delete from the AVL tree using (symbol, percentChange) as key.
     *   3. Remove from both HashMaps.
     *
     * Time Complexity: O(log n) — O(1) HashMap removal plus O(log n) AVL tree node deletion and rebalancing.
     *
     * @param symbol ticker symbol to remove
     * @return true if the stock was found and removed, false otherwise
     */
    public boolean removeStock(String symbol) {
        symbol = symbol.toUpperCase();
        if (!stocks.containsKey(symbol)) {
            System.out.printf("   ❌ Stock %s not found in portfolio.%n", symbol);
            return false;
        }

        // Delete from AVL using the stored percent change
        Double oldPct = lastPercentChange.getOrDefault(symbol, 0.0);
        leaderboard.remove(symbol, oldPct);

        stocks.remove(symbol);
        lastPercentChange.remove(symbol);
        System.out.printf("   🗑️ %s removed from portfolio.%n", symbol);
        return true;
    }

    /**
     * Looks up a stock by ticker symbol. O(1) via HashMap.
     *
     * Time Complexity: O(1) average — retrieves stock from HashMap by symbol key.
     *
     * @param symbol ticker symbol (case-insensitive)
     * @return the Stock object, or null if not found
     */
    public Stock getStock(String symbol) {
        return stocks.get(symbol.toUpperCase());
    }

    /**
     * Returns all stocks in the portfolio.
     *
     * Time Complexity: O(n) — constructs an ArrayList containing all n portfolio stocks.
     *
     * @return ArrayList of all Stock objects
     */
    public ArrayList<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    /**
     * Returns the number of stocks currently in the portfolio.
     *
     * Time Complexity: O(1) — returns current size of the internal HashMap.
     *
     * @return stock count
     */
    public int getStockCount() {
        return stocks.size();
    }

    // ── Leaderboard (AVL Tree) ────────────────────────────────────────────

    /**
     * Updates the AVL leaderboard after a stock's price has changed.
     *
     * The AVL tree is keyed by percent change. When a stock records a
     * new daily price, its percent change may have shifted, so we must:
     *
     *   1. Remove the OLD node  (O(log n) delete + rebalance)
     *   2. Insert a NEW node    (O(log n) insert + rebalance)
     *
     * This guarantees the tree remains sorted and balanced at all times.
     *
     * Why not just update in place?
     *   Because the key has changed, an in-place update would violate the
     *   BST ordering property. Delete + re-insert is the correct approach.
     *
     * Time Complexity: O(log n) — O(log n) AVL removal of previous percent key plus O(log n) insertion of updated key.
     *
     * @param symbol ticker symbol of the stock whose price changed
     */
    public void updateLeaderboard(String symbol) {
        symbol = symbol.toUpperCase();
        Stock stock = stocks.get(symbol);
        if (stock == null) return;

        // Step 1 — Remove old AVL node (if it exists)
        Double oldPct = lastPercentChange.getOrDefault(symbol, 0.0);
        leaderboard.remove(symbol, oldPct);

        // Step 2 — Insert new AVL node with updated percent change
        leaderboard.insert(stock);

        // Step 3 — Record the new percent change for future updates
        lastPercentChange.put(symbol, stock.getPercentChange());
    }

    /**
     * Updates the AVL leaderboard for all stocks in the portfolio.
     *
     * Time Complexity: O(n log n) — removes and re-inserts all n stocks into AVL tree.
     */
    public void updateLeaderboard() {
        for (String sym : stocks.keySet()) {
            updateLeaderboard(sym);
        }
    }

    /**
     * Returns the sorted leaderboard list of stocks from the AVL tree.
     *
     * Time Complexity: O(n) — visits every stock node in the AVL tree via in-order traversal to produce sorted rankings.
     *
     * @return List of stocks in sorted order
     */
    public List<Stock> getSortedLeaderboard() {
        return leaderboard.inOrderTraversal();
    }

    /**
     * Displays the performance leaderboard by performing an in-order
     * traversal of the AVL tree.
     *
     * Time Complexity: O(n) — traverses and prints all n stocks from the AVL tree in-order.
     *
     * An in-order traversal of a BST visits nodes in ascending key order,
     * so stocks are listed from worst daily change to best daily change.
     * We reverse the result to show best performers first.
     *
     * Output format:
     *   ┌──────┬────────┬──────────────────┬────────────┬───────────┐
     *   │ Rank │ Symbol │ Company          │   Price    │  Change   │
     *   ├──────┼────────┼──────────────────┼────────────┼───────────┤
     *   │   1  │ TSLA   │ Tesla Inc.       │   $250.00  │  +6.38%   │
     *   │   2  │ AAPL   │ Apple Inc.       │   $170.00  │  +3.03%   │
     *   │   3  │ GOOGL  │ Alphabet Inc.    │  $2850.00  │  +2.15%   │
     *   └──────┴────────┴──────────────────┴────────────┴───────────┘
     */
    public void displayLeaderboard() {
        System.out.println("\n   🌳 AVL Tree: Performing in-order traversal for sorted leaderboard...");

        // In-order traversal returns entries sorted ascending by % change.
        List<Stock> entries = leaderboard.inOrderTraversal();

        if (entries == null || entries.isEmpty()) {
            System.out.println("   📭 No stocks in the leaderboard.");
            return;
        }

        // Print the header
        System.out.println("   ┌──────┬────────┬──────────────────┬────────────┬───────────┐");
        System.out.println("   │ Rank │ Symbol │ Company          │   Price    │  Change   │");
        System.out.println("   ├──────┼────────┼──────────────────┼────────────┼───────────┤");

        // Reverse to show best performer first (descending % change)
        int rank = 1;
        for (int i = entries.size() - 1; i >= 0; i--) {
            Stock s = entries.get(i);
            if (s != null) {
                String changeStr = s.getPercentChange() >= 0
                        ? String.format("+%.2f%%", s.getPercentChange())
                        : String.format("%.2f%%", s.getPercentChange());
                System.out.printf("   │ %4d │ %-6s │ %-16s │ $%9.2f │ %9s │%n",
                        rank, s.getSymbol(), s.getName(),
                        s.getCurrentPrice(), changeStr);
                rank++;
            }
        }
        System.out.println("   └──────┴────────┴──────────────────┴────────────┴───────────┘");
    }

    /**
     * Lists all stocks currently in the portfolio (unordered, from HashMap).
     *
     * Time Complexity: O(n) — iterates through all n stocks in portfolio and prints summaries.
     */
    public void displayAllStocks() {
        System.out.println("\n   📋 All Portfolio Stocks:");
        System.out.println("   ┌────────┬──────────────────┬────────────┬───────────┬──────┐");
        System.out.println("   │ Symbol │ Company          │   Price    │  Change   │ Days │");
        System.out.println("   ├────────┼──────────────────┼────────────┼───────────┼──────┤");

        for (Stock s : stocks.values()) {
            String changeStr = s.getPercentChange() >= 0
                    ? String.format("+%.2f%%", s.getPercentChange())
                    : String.format("%.2f%%", s.getPercentChange());
            System.out.printf("   │ %-6s │ %-16s │ $%9.2f │ %9s │ %4d │%n",
                    s.getSymbol(), s.getName(),
                    s.getCurrentPrice(), changeStr, s.getDayCount());
        }
        System.out.println("   └────────┴──────────────────┴────────────┴───────────┴──────┘");
    }

    // ── Portfolio Financial Aggregation ──────────────────────────────────
    private double cashBalance = 50000.0; // $50,000 starting cash balance

    /**
     * Time Complexity: O(1) — constant-time primitive field access.
     */
    public double getCashBalance() { return cashBalance; }

    /**
     * Time Complexity: O(1) — constant-time primitive assignment.
     */
    public void setCashBalance(double cash) { this.cashBalance = cash; }

    /**
     * Time Complexity: O(1) — constant-time arithmetic addition.
     */
    public void adjustCash(double delta) { this.cashBalance += delta; }

    /**
     * Time Complexity: O(n) — iterates over all n stocks summing their current market values.
     */
    public double getTotalMarketValue() {
        double sum = 0.0;
        for (Stock s : stocks.values()) {
            sum += s.getMarketValue();
        }
        return sum;
    }

    /**
     * Time Complexity: O(n) — iterates over all n stocks summing their total acquisition costs.
     */
    public double getTotalCostBasis() {
        double sum = 0.0;
        for (Stock s : stocks.values()) {
            sum += s.getTotalCost();
        }
        return sum;
    }

    /**
     * Time Complexity: O(n) — calculates total stock market value in O(n) plus cash balance.
     */
    public double getTotalPortfolioValue() {
        return cashBalance + getTotalMarketValue();
    }

    /**
     * Time Complexity: O(n) — computes difference between total market value and total cost basis.
     */
    public double getTotalUnrealizedPnL() {
        return getTotalMarketValue() - getTotalCostBasis();
    }
}
