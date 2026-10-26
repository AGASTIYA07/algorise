import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * Trade.java — Represents a single buy/sell transaction in the stock market.
 * ============================================================================
 *
 * This is a plain data object (sometimes called a "record" or "DTO") that
 * encapsulates every detail of one trade:
 *   - What stock was traded (symbol like "AAPL", "GOOGL")
 *   - Whether it was a BUY or SELL
 *   - How many shares and at what price
 *   - The total monetary value (quantity × price)
 *   - When the trade was created (timestamp)
 *
 * KEY DESIGN DECISIONS:
 *   1. Trade IDs are auto-generated using a static counter so every Trade
 *      object gets a unique, monotonically increasing identifier without
 *      the caller needing to manage IDs.
 *   2. The timestamp is captured at construction time using Java 17's
 *      java.time API — giving us an immutable record of when the trade
 *      was created.
 *   3. reverseTrade() is a factory-style method that creates a *new* Trade
 *      with BUY↔SELL flipped. This is essential for the undo/redo system:
 *      undoing a BUY means creating a SELL, and vice versa.
 */
public class Trade {

    // -----------------------------------------------------------------------
    // Static counter shared across ALL Trade instances.
    // Every new Trade increments this, guaranteeing unique IDs.
    // -----------------------------------------------------------------------
    private static int idCounter = 0;

    // -----------------------------------------------------------------------
    // Instance fields — each Trade carries its own copy of these values.
    // -----------------------------------------------------------------------
    private final int tradeId;          // Unique identifier for this trade
    private final String stockSymbol;   // Ticker symbol, e.g. "AAPL"
    private final String tradeType;     // "BUY" or "SELL"
    private final int quantity;         // Number of shares traded
    private final double pricePerUnit;  // Price per single share
    private final double totalValue;    // Computed: quantity * pricePerUnit
    private final String timestamp;     // Human-readable creation time

    // Date-time formatter for a clean, readable timestamp string.
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a new Trade with an auto-assigned ID and current timestamp.
     *
     * Time Complexity: O(1) — initializes trade fields, computes total value, and stamps system time in constant time.
     *
     * @param stockSymbol  the ticker symbol (e.g. "TSLA")
     * @param tradeType    "BUY" or "SELL"
     * @param quantity     number of shares
     * @param pricePerUnit price per share in dollars
     */
    public Trade(String stockSymbol, String tradeType, int quantity, double pricePerUnit) {
        this.tradeId = ++idCounter;                        // Auto-increment ID
        this.stockSymbol = stockSymbol.toUpperCase();      // Normalize to uppercase
        this.tradeType = tradeType.toUpperCase();          // Normalize to uppercase
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalValue = quantity * pricePerUnit;          // Derived field
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    // -----------------------------------------------------------------------
    // Getters — provide read-only access to all fields.
    // -----------------------------------------------------------------------

    /**
     * Time Complexity: O(1) — direct primitive field access.
     * @return the unique trade ID
     */
    public int getTradeId() { return tradeId; }

    /**
     * Time Complexity: O(1) — direct reference field access.
     * @return the stock ticker symbol
     */
    public String getSymbol() { return stockSymbol; }

    /**
     * Time Complexity: O(1) — direct reference field access.
     * @return "BUY" or "SELL"
     */
    public String getType() { return tradeType; }

    /**
     * Time Complexity: O(1) — direct primitive field access.
     * @return number of shares in this trade
     */
    public int getQuantity() { return quantity; }

    /**
     * Time Complexity: O(1) — direct primitive field access.
     * @return price per share
     */
    public double getPrice() { return pricePerUnit; }

    /**
     * Time Complexity: O(1) — direct primitive field access.
     * @return total monetary value (quantity × price)
     */
    public double getTotal() { return totalValue; }

    /**
     * Time Complexity: O(1) — direct reference field access.
     * @return creation timestamp string
     */
    public String getTimestamp() { return timestamp; }

    /**
     * Creates a NEW Trade that is the logical reverse of this one.
     *
     * Time Complexity: O(1) — creates and returns an inverted Trade object in constant time.
     *
     * If this trade was a BUY  → the reverse is a SELL (same stock, qty, price).
     * If this trade was a SELL → the reverse is a BUY.
     *
     * WHY THIS MATTERS FOR UNDO/REDO:
     *   When the user undoes a BUY of 10 shares of AAPL, we need to
     *   effectively "sell" those 10 shares back. Rather than mutating
     *   state, we create a brand-new Trade object that represents the
     *   reversal. This keeps our transaction history clean and auditable.
     *
     * @return a new Trade with the opposite trade type
     */
    public Trade reverseTrade() {
        String reversedType = this.tradeType.equals("BUY") ? "SELL" : "BUY";
        return new Trade(this.stockSymbol, reversedType, this.quantity, this.pricePerUnit);
    }

    /**
     * Returns a formatted string representation of this trade.
     * Useful for displaying trade details in the console UI.
     *
     * Time Complexity: O(1) — formats string representation with fixed number of fields.
     */
    @Override
    public String toString() {
        return String.format(
                "[Trade #%d] %s %d shares of %s @ $%.2f each | Total: $%.2f | %s",
                tradeId, tradeType, quantity, stockSymbol,
                pricePerUnit, totalValue, timestamp
        );
    }
}
