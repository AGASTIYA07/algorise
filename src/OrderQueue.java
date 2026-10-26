/**
 * ============================================================================
 * OrderQueue.java — FIFO Trade Order Queue
 * ============================================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Implements a fair, chronological First-In, First-Out (FIFO) trade execution pipeline.
 * Orders submitted by users (BUY/SELL) wait in this queue until processed by the exchange engine:
 *   - enqueue(): adds new pending orders to the rear in O(1) time.
 *   - dequeue(): processes orders in the exact sequence they arrived in O(1) time.
 *   - Guarantees trade fairness and market integrity without capacity bottlenecks.
 */
public class OrderQueue {

    // -----------------------------------------------------------------------
    // 'front' points to the first-in-line node (next to be dequeued).
    // 'rear' points to the last-in-line node (most recently enqueued).
    // 'size' is maintained as a counter for O(1) getSize().
    // -----------------------------------------------------------------------
    private OrderNode front;
    private OrderNode rear;
    private int size;

    /**
     * Constructs an empty OrderQueue.
     * Both front and rear are null when the queue has no elements.
     *
     * Time Complexity: O(1) — pointer and counter initialization.
     */
    public OrderQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    /**
     * Adds a trade order to the REAR of the queue.
     *
     * HOW IT WORKS:
     *   1. Create a new node wrapping the trade.
     *   2. If the queue is empty, both front and rear point to the new node.
     *   3. Otherwise, link the current rear's 'next' to the new node,
     *      then update rear to the new node.
     *
     * Time Complexity:  O(1) — direct pointer access to rear, no traversal.
     * Space Complexity: O(1) — one new node allocated.
     *
     * @param trade the Trade order to add to the queue
     */
    public void enqueue(Trade trade) {
        OrderNode newNode = new OrderNode(trade);

        if (isEmpty()) {
            // Special case: first element — both front and rear point to it.
            front = newNode;
            rear = newNode;
        } else {
            // Normal case: attach new node after current rear, update rear.
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the trade order at the FRONT of the queue.
     *
     * HOW IT WORKS:
     *   1. Save a reference to the front node's trade.
     *   2. Move 'front' forward to the next node.
     *   3. If the queue becomes empty, also set 'rear' to null.
     *   4. Return the saved trade.
     *
     * If the queue is empty, prints a warning and returns null.
     *
     * Time Complexity:  O(1) — just pointer update at the head.
     * Space Complexity: O(1)
     *
     * @return the Trade at the front of the queue, or null if empty
     */
    public Trade dequeue() {
        if (isEmpty()) {
            System.out.println("  ⚠ Order queue is empty — no orders to process!");
            return null;
        }

        Trade dequeuedTrade = front.trade;  // Grab the data from the front node
        front = front.next;                 // Advance the front pointer
        size--;

        // If we just removed the last element, rear must also be null.
        if (front == null) {
            rear = null;
        }

        return dequeuedTrade;
    }

    /**
     * Returns the trade at the front of the queue WITHOUT removing it.
     *
     * Useful for previewing the next order to be processed.
     *
     * Time Complexity: O(1) — direct read of front node payload without modification.
     *
     * @return the Trade at the front, or null if empty
     */
    public Trade peek() {
        if (isEmpty()) {
            return null;
        }
        return front.trade;
    }

    /**
     * Checks whether the queue contains any elements.
     *
     * Time Complexity: O(1) — single pointer null check.
     *
     * @return true if the queue has no elements, false otherwise
     */
    public boolean isEmpty() {
        return front == null;
    }

    /**
     * Returns the number of elements currently in the queue.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the queue size
     */
    public int getSize() {
        return size;
    }

    /**
     * Alias for getSize() to support standard collection naming.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the queue size
     */
    public int size() {
        return size;
    }

    /**
     * Converts pending orders to a List from front to rear (FIFO order).
     *
     * Time Complexity: O(n) — traverses through all n pending orders from front to rear.
     *
     * @return List of Trade objects in processing order
     */
    public java.util.List<Trade> toList() {
        java.util.List<Trade> list = new java.util.ArrayList<>();
        OrderNode current = front;
        while (current != null) {
            list.add(current.trade);
            current = current.next;
        }
        return list;
    }

    /**
     * Displays all pending trade orders from front (next to process) to rear.
     *
     * Time Complexity: O(n) — visits every node in the queue from front to rear.
     *
     * This traversal walks the singly linked list from front to rear,
     * following 'next' pointers.
     */
    public void displayPendingOrders() {
        System.out.println("\n  ═══════════════════════════════════════════");
        System.out.println("  📋 Pending Order Queue (size: " + size + ")");
        System.out.println("  ═══════════════════════════════════════════");

        if (isEmpty()) {
            System.out.println("  (no pending orders)");
            return;
        }

        // Traverse from front to rear.
        OrderNode current = front;
        int position = 1;
        while (current != null) {
            String marker = (position == 1) ? " ← NEXT TO PROCESS" : "";
            System.out.println("  " + position + ". " + current.trade + marker);
            current = current.next;
            position++;
        }
    }
}
