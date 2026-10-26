/**
 * ============================================================================
 * OrderNode.java — Order Queue Node
 * ============================================================================
 * Internal node in the linked-list OrderQueue storing a Trade order and a reference
 * to the next queued order in the processing pipeline.
 */
public class OrderNode {

    /** The trade order stored in this node. */
    Trade trade;

    /** Pointer to the next node in the queue (closer to the rear), or null. */
    OrderNode next;

    /**
     * Constructs a new queue node containing the given trade.
     *
     * Time Complexity: O(1) — assigns trade reference and sets next pointer to null in constant time.
     *
     * @param trade the Trade object to store in this node
     */
    OrderNode(Trade trade) {
        this.trade = trade;
        this.next = null;
    }
}
