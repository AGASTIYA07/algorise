/**
 * ============================================================================
 * TransactionList.java — Per-Stock Transaction Ledger
 * ============================================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Maintains an auditable, bidirectional transaction ledger for each individual stock:
 *   - O(1) Tail Append: logs newly executed trades instantly without list traversal.
 *   - O(1) Tail Removal: unlinks the last transaction during undo operations via the 'prev' pointer.
 *   - Forward & Backward Traversal: displays chronological trade history or reverse-chronological
 *     activity (most recent trades first) on demand.
 */
public class TransactionList {

    // -----------------------------------------------------------------------
    // 'head' points to the first (oldest) transaction.
    // 'tail' points to the last (newest) transaction.
    // Both are maintained for O(1) operations at either end.
    // 'size' is a counter for O(1) getSize().
    // -----------------------------------------------------------------------
    private TransactionNode head;
    private TransactionNode tail;
    private int size;

    /**
     * Constructs an empty TransactionList.
     * Both head and tail are null when the list has no elements.
     *
     * Time Complexity: O(1) — pointer and counter initialization.
     */
    public TransactionList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Appends a trade to the END (tail) of the list.
     *
     * HOW IT WORKS:
     *   1. Create a new node wrapping the trade.
     *   2. If the list is empty, set both head and tail to the new node.
     *   3. Otherwise:
     *      a. Point the new node's 'prev' to the current tail.
     *      b. Point the current tail's 'next' to the new node.
     *      c. Update tail to the new node.
     *
     * WHY O(1)?
     *   We have a direct reference to 'tail', so we don't need to traverse
     *   the list to find where to insert. Just a few pointer updates.
     *
     * Time Complexity:  O(1) — direct tail pointer linking without list traversal.
     * Space Complexity: O(1) — one new node
     *
     * @param trade the Trade to append
     */
    public void append(Trade trade) {
        TransactionNode newNode = new TransactionNode(trade);

        if (isEmpty()) {
            // First element: head and tail both point to it.
            head = newNode;
            tail = newNode;
        } else {
            // Link the new node after the current tail.
            newNode.prev = tail;    // New node looks back at old tail
            tail.next = newNode;    // Old tail looks forward to new node
            tail = newNode;         // New node becomes the new tail
        }
        size++;
    }

    /**
     * Removes and returns the trade at the END (tail) of the list.
     *
     * HOW IT WORKS:
     *   1. Save the tail node's trade.
     *   2. Move tail back to tail.prev.
     *   3. If the new tail exists, set its 'next' to null (cut off the old tail).
     *   4. If the list is now empty, also set head to null.
     *
     * WHY O(1)?
     *   In a SINGLY linked list, removing the last element requires O(n)
     *   traversal because we need to find the second-to-last node to
     *   update its 'next' to null. But in a DOUBLY linked list, the tail
     *   node's 'prev' pointer gives us DIRECT access to its predecessor.
     *
     *   BEFORE removeLast():
     *     head → [A] ⇄ [B] ⇄ [C] ← tail
     *
     *   AFTER removeLast() → returns C:
     *     head → [A] ⇄ [B] ← tail
     *     (B.next = null, tail = B)
     *
     * Time Complexity:  O(1) — direct tail.prev pointer update cuts off the last node in constant time.
     * Space Complexity: O(1)
     *
     * @return the Trade at the tail, or null if the list is empty
     */
    public Trade removeLast() {
        if (isEmpty()) {
            System.out.println("  ⚠ Transaction list is empty — nothing to remove!");
            return null;
        }

        Trade removedTrade = tail.trade;

        if (head == tail) {
            // Only one element — list becomes empty.
            head = null;
            tail = null;
        } else {
            // More than one element — move tail back by one.
            tail = tail.prev;       // Tail retreats to the previous node
            tail.next = null;       // Sever the link to the old tail
        }
        size--;
        return removedTrade;
    }

    /**
     * Displays all transactions from HEAD to TAIL (chronological order).
     *
     * Time Complexity: O(n) — visits every node from head to tail in forward direction.
     *
     * This demonstrates FORWARD TRAVERSAL of a doubly linked list:
     *   Start at head, follow 'next' pointers until null.
     *
     * Use case: "Show me all trades for AAPL in the order they happened."
     */
    public void displayAll() {
        System.out.println("\n  ═══════════════════════════════════════════");
        System.out.println("  📜 Transaction History — Forward (size: " + size + ")");
        System.out.println("  ═══════════════════════════════════════════");

        if (isEmpty()) {
            System.out.println("  (no transactions recorded)");
            return;
        }

        // Forward traversal: head → tail using 'next' pointers.
        TransactionNode current = head;
        int position = 1;
        while (current != null) {
            System.out.println("  " + position + ". " + current.trade);
            current = current.next;   // Move forward
            position++;
        }
    }

    /**
     * Displays all transactions from TAIL to HEAD (reverse chronological).
     *
     * Time Complexity: O(n) — visits every node from tail to head using prev pointers.
     *
     * This demonstrates BACKWARD TRAVERSAL — a key advantage of a doubly
     * linked list over a singly linked list. With 'prev' pointers, we can
     * walk backward without needing to:
     *   - Reverse the list (which would destroy the order)
     *   - Use a stack to temporarily store elements
     *   - Traverse from head each time we want the "previous" element
     *
     * Use case: "Show me the most recent trades first."
     */
    public void displayReverse() {
        System.out.println("\n  ═══════════════════════════════════════════");
        System.out.println("  📜 Transaction History — Reverse (size: " + size + ")");
        System.out.println("  ═══════════════════════════════════════════");

        if (isEmpty()) {
            System.out.println("  (no transactions recorded)");
            return;
        }

        // Backward traversal: tail → head using 'prev' pointers.
        TransactionNode current = tail;
        int position = 1;
        while (current != null) {
            System.out.println("  " + position + ". " + current.trade);
            current = current.prev;   // Move backward
            position++;
        }
    }

    /**
     * Returns the number of transactions in the list.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the list size
     */
    public int getSize() {
        return size;
    }

    /**
     * Alias for getSize() to support standard collection naming.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the list size
     */
    public int size() {
        return size;
    }

    /**
     * Alias for append() to support addLast semantics.
     *
     * Time Complexity: O(1) — delegates to append() which links node at tail in constant time.
     *
     * @param trade the Trade to append
     */
    public void addLast(Trade trade) {
        append(trade);
    }

    /**
     * Checks whether the list contains any transactions.
     *
     * Time Complexity: O(1) — single pointer null check.
     *
     * @return true if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Converts transactions to a List in chronological order (head to tail).
     *
     * Time Complexity: O(n) — traverses n nodes forward from head to tail.
     *
     * @return List of Trade objects
     */
    public java.util.List<Trade> toList() {
        java.util.List<Trade> list = new java.util.ArrayList<>();
        TransactionNode current = head;
        while (current != null) {
            list.add(current.trade);
            current = current.next;
        }
        return list;
    }

    /**
     * Converts transactions to a List in reverse chronological order (tail to head).
     *
     * Time Complexity: O(n) — traverses n nodes backward from tail to head.
     *
     * @return List of Trade objects in reverse order
     */
    public java.util.List<Trade> toReverseList() {
        java.util.List<Trade> list = new java.util.ArrayList<>();
        TransactionNode current = tail;
        while (current != null) {
            list.add(current.trade);
            current = current.prev;
        }
        return list;
    }
}
