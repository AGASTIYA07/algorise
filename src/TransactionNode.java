/**
 * ============================================================================
 * TransactionNode.java — Doubly-Linked Transaction Node
 * ============================================================================
 * Represents a single transaction node in the per-stock TransactionList.
 * Contains the Trade object and both forward ('next') and backward ('prev') pointers
 * for bidirectional ledger navigation and O(1) tail deletion on trade undo.
 */
public class TransactionNode {

    /** The trade data stored in this node. */
    Trade trade;

    /** Pointer to the next node (toward the tail), or null if this is the tail. */
    TransactionNode next;

    /** Pointer to the previous node (toward the head), or null if this is the head. */
    TransactionNode prev;

    /**
     * Constructs a new doubly linked list node containing the given trade.
     * Both prev and next default to null (will be set by TransactionList).
     *
     * Time Complexity: O(1) — stores trade reference and initializes prev and next pointers to null in constant time.
     *
     * @param trade the Trade object to store in this node
     */
    TransactionNode(Trade trade) {
        this.trade = trade;
        this.next = null;
        this.prev = null;
    }
}
