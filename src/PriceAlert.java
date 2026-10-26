/**
 * ============================================================================
 * PriceAlert.java — Represents a price-based alert for a stock.
 * ============================================================================
 *
 * A PriceAlert monitors whether a stock's price has crossed a threshold.
 * Each alert specifies:
 *   - A stock symbol (e.g. "AAPL")
 *   - A threshold price (e.g. $200.00)
 *   - An alert type: "ABOVE" means trigger when price goes ABOVE the
 *     threshold; "BELOW" means trigger when price drops BELOW it.
 *   - A triggered flag that records whether the alert has already fired.
 *
 * USAGE IN THE PORTFOLIO TRACKER:
 *   Alerts are stored in a priority queue (min-heap) so the system can
 *   efficiently check which alerts are closest to being triggered.
 *   This class is a simple data container — the logic for checking
 *   prices and firing alerts lives in the main tracker.
 *
 * EXAMPLE:
 *   PriceAlert alert = new PriceAlert(200.0, "AAPL", "ABOVE");
 *   // When AAPL's price goes above $200, call alert.trigger().
 */
public class PriceAlert {

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------
    private final double thresholdPrice;   // The price level to watch for
    private final String stockSymbol;      // Ticker symbol, e.g. "TSLA"
    private final String alertType;        // "ABOVE" or "BELOW"
    private boolean isTriggered;           // Whether this alert has already fired

    /**
     * Constructs a new PriceAlert.
     *
     * Time Complexity: O(1) — field initialization in constant time.
     *
     * @param thresholdPrice the price threshold to monitor
     * @param stockSymbol    the stock ticker to watch
     * @param alertType      "ABOVE" (trigger when price exceeds threshold)
     *                       or "BELOW" (trigger when price drops below)
     */
    public PriceAlert(double thresholdPrice, String stockSymbol, String alertType) {
        this.thresholdPrice = thresholdPrice;
        this.stockSymbol = stockSymbol.toUpperCase();
        this.alertType = alertType.toUpperCase();
        this.isTriggered = false;   // Alerts start untriggered
    }

    /**
     * Overloaded constructor accepting stock symbol first.
     *
     * Time Complexity: O(1) — delegates to primary constructor in constant time.
     */
    public PriceAlert(String stockSymbol, double thresholdPrice, String alertType) {
        this(thresholdPrice, stockSymbol, alertType);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /**
     * Time Complexity: O(1) — constant-time primitive field access.
     * @return the price threshold for this alert
     */
    public double getThreshold() { return thresholdPrice; }

    /**
     * Time Complexity: O(1) — constant-time reference field access.
     * @return the stock symbol being monitored
     */
    public String getSymbol() { return stockSymbol; }

    /**
     * Time Complexity: O(1) — constant-time reference field access.
     * @return "ABOVE" or "BELOW"
     */
    public String getType() { return alertType; }

    /**
     * Checks whether this alert has already been triggered.
     *
     * Time Complexity: O(1) — constant-time boolean field access.
     *
     * @return true if the alert has fired, false otherwise
     */
    public boolean isTriggered() { return isTriggered; }

    /**
     * Marks this alert as triggered.
     *
     * Time Complexity: O(1) — constant-time boolean assignment.
     *
     * Once triggered, an alert is considered "fired" and typically
     * would not fire again (one-shot behavior). The consuming code
     * can check isTriggered() to avoid duplicate notifications.
     */
    public void trigger() {
        this.isTriggered = true;
    }

    /**
     * Returns a formatted string representation of this alert.
     * Includes the trigger status so the user can see at a glance
     * which alerts have already fired.
     *
     * Time Complexity: O(1) — formats string representation with fixed number of fields.
     */
    @Override
    public String toString() {
        String status = isTriggered ? "🔔 TRIGGERED" : "⏳ WATCHING";
        return String.format(
                "[Alert] %s %s $%.2f for %s | Status: %s",
                alertType,
                alertType.equals("ABOVE") ? ">" : "<",
                thresholdPrice,
                stockSymbol,
                status
        );
    }
}
