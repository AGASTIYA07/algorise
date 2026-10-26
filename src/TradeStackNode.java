/**
 * ============================================================================
 * TradeStackNode.java — Trade Stack Node
 * ============================================================================
 * Internal node in the linked-list TradeStack storing an executed/undone Trade
 * and a reference to the preceding trade node in the stack.
 */
public class TradeStackNode {

    /** The trade data stored in this node. */
    Trade trade;

    /** Pointer to the next node below this one in the stack (or null if bottom). */
    TradeStackNode next;

    /**
     * Constructs a new stack node containing the given trade.
     * The 'next' pointer defaults to null (will be set by TradeStack).
     *
     * Time Complexity: O(1) — stores trade payload reference and initializes next to null in constant time.
     *
     * @param trade the Trade object to store in this node
     */
    TradeStackNode(Trade trade) {
        this.trade = trade;
        this.next = null;
    }
}
