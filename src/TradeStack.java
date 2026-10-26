/**
 * ============================================================================
 * TradeStack.java — LIFO Undo / Redo Engine
 * ============================================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Implements a full transactional Undo and Redo engine for executed stock trades:
 *   - Undo Stack: records executed trades. On "Undo", pops the most recent trade in O(1)
 *     and inverts its portfolio impact (buying becomes selling, selling becomes buying).
 *   - Redo Stack: records undone trades. On "Redo", restores the trade in O(1).
 *   - Last-In, First-Out (LIFO) semantics guarantee strict reverse-chronological reversal.
 */
public class TradeStack {

    // -----------------------------------------------------------------------
    // 'top' points to the most recently pushed node (head of the linked list).
    // 'size' tracks the number of elements so getSize() is O(1).
    // -----------------------------------------------------------------------
    private TradeStackNode top;
    private int size;

    /**
     * Constructs an empty TradeStack.
     * An empty stack has no top node and a size of zero.
     *
     * Time Complexity: O(1) — pointer and counter initialization.
     */
    public TradeStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes a trade onto the top of the stack.
     *
     * HOW IT WORKS:
     *   1. Create a new node wrapping the given trade.
     *   2. Point the new node's 'next' to the current top.
     *   3. Update 'top' to be the new node.
     *
     * Time Complexity:  O(1) — no traversal, just pointer manipulation.
     * Space Complexity: O(1) — only one new node is allocated.
     *
     * @param trade the Trade to push onto the stack
     */
    public void push(Trade trade) {
        TradeStackNode newNode = new TradeStackNode(trade);
        newNode.next = top;   // New node points down to the old top
        top = newNode;        // New node becomes the new top
        size++;
    }

    /**
     * Pops (removes and returns) the trade at the top of the stack.
     *
     * HOW IT WORKS:
     *   1. Save a reference to the top node's trade.
     *   2. Move 'top' down to the next node.
     *   3. Return the saved trade.
     *
     * If the stack is empty, prints a warning and returns null.
     *
     * Time Complexity:  O(1) — just pointer update.
     * Space Complexity: O(1)
     *
     * @return the Trade at the top, or null if the stack is empty
     */
    public Trade pop() {
        if (isEmpty()) {
            System.out.println("  ⚠ Stack is empty — nothing to pop!");
            return null;
        }

        Trade poppedTrade = top.trade;   // Grab the data from the top node
        top = top.next;                  // Move top pointer down one level
        size--;
        return poppedTrade;
    }

    /**
     * Returns the trade at the top of the stack WITHOUT removing it.
     *
     * This is useful for inspecting the most recent trade without
     * modifying the stack's state (e.g., showing "Last trade was: ...").
     *
     * Time Complexity: O(1) — directly reads data at the top pointer reference without modifying state.
     *
     * @return the Trade at the top, or null if empty
     */
    public Trade peek() {
        if (isEmpty()) {
            return null;
        }
        return top.trade;
    }

    /**
     * Checks whether the stack contains any elements.
     *
     * Time Complexity: O(1) — single pointer null check.
     *
     * @return true if the stack has no elements, false otherwise
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the number of elements currently in the stack.
     * Maintained as a counter so this is O(1) instead of traversing the list.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the stack size
     */
    public int getSize() {
        return size;
    }

    /**
     * Alias for getSize() to support standard collection naming.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the stack size
     */
    public int size() {
        return size;
    }

    /**
     * Converts the stack elements to a List from top to bottom.
     *
     * Time Complexity: O(n) — traverses from top to bottom adding each of n nodes to list.
     *
     * @return List of Trade objects in LIFO order
     */
    public java.util.List<Trade> toList() {
        java.util.List<Trade> list = new java.util.ArrayList<>();
        TradeStackNode current = top;
        while (current != null) {
            list.add(current.trade);
            current = current.next;
        }
        return list;
    }

    /**
     * Displays all trades in the stack from top to bottom.
     *
     * Time Complexity: O(n) — visits all n elements from top to bottom to print them.
     *
     * The label parameter lets us distinguish between the Undo Stack
     * and the Redo Stack when printing.
     *
     * This traversal demonstrates walking a singly linked list:
     *   Start at 'top', follow 'next' pointers until null.
     *
     * @param label a descriptive label, e.g. "Undo Stack" or "Redo Stack"
     */
    public void displayStack(String label) {
        System.out.println("\n  ═══════════════════════════════════════════");
        System.out.println("  📚 " + label + " (size: " + size + ")");
        System.out.println("  ═══════════════════════════════════════════");

        if (isEmpty()) {
            System.out.println("  (empty)");
            return;
        }

        // Traverse from top to bottom, printing each trade.
        // 'current' walks through the list following 'next' pointers.
        TradeStackNode current = top;
        int position = 1;
        while (current != null) {
            String marker = (position == 1) ? " ← TOP" : "";
            System.out.println("  " + position + ". " + current.trade + marker);
            current = current.next;
            position++;
        }
    }
}
