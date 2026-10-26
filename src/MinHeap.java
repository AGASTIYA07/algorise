/**
 * MinHeap.java — Priority Queue for Stock Price Alerts
 * ====================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Implements a prioritized alert monitor for stock price thresholds.
 * By keeping the alert with the minimum threshold at the root (index 1):
 *   - Price monitoring check is O(1) via peek(). If the lowest alert has not been breached,
 *     no higher alert can be triggered either, eliminating unnecessary scans.
 *   - When triggered, alerts are extracted in O(log k) via extractMin(), maintaining optimal efficiency.
 */
class MinHeap {

    /**
     * The internal array storing PriceAlert objects.
     * Index 0 is unused (1-indexed for easier parent/child calculation).
     * The heap occupies indices 1 through size (inclusive).
     */
    private PriceAlert[] heap;

    /**
     * The current number of elements in the heap.
     * Valid elements are at indices 1 through size.
     */
    private int size;

    /**
     * The current capacity of the underlying array.
     * When size reaches capacity - 1 (since index 0 is unused),
     * the array is resized (doubled).
     */
    private int capacity;

    /**
     * Constructs an empty MinHeap with an initial capacity of 100.
     * The array is 1-indexed, so we allocate capacity + 1 slots.
     *
     * Time Complexity: O(1) — allocates initial array of capacity 100 in constant time.
     */
    public MinHeap() {
        this.capacity = 100;
        this.size = 0;
        this.heap = new PriceAlert[capacity + 1]; // +1 because index 0 is unused
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INDEX NAVIGATION — Parent, Left Child, Right Child
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns the index of the parent of node at index i.
     * Using 1-based indexing: parent(i) = i / 2
     *
     * Time Complexity: O(1) — performs integer division (i / 2) in constant time.
     *
     * Example: parent(5) = 2, parent(4) = 2, parent(3) = 1
     *
     * @param i the index of the current node
     * @return the index of the parent node
     */
    private int parent(int i) {
        return i / 2;
    }

    /**
     * Returns the index of the left child of node at index i.
     * Using 1-based indexing: leftChild(i) = 2 * i
     *
     * Time Complexity: O(1) — performs integer multiplication (2 * i) in constant time.
     *
     * Example: leftChild(1) = 2, leftChild(2) = 4, leftChild(3) = 6
     *
     * @param i the index of the current node
     * @return the index of the left child
     */
    private int leftChild(int i) {
        return 2 * i;
    }

    /**
     * Returns the index of the right child of node at index i.
     * Using 1-based indexing: rightChild(i) = 2 * i + 1
     *
     * Time Complexity: O(1) — performs arithmetic calculation (2 * i + 1) in constant time.
     *
     * Example: rightChild(1) = 3, rightChild(2) = 5, rightChild(3) = 7
     *
     * @param i the index of the current node
     * @return the index of the right child
     */
    private int rightChild(int i) {
        return 2 * i + 1;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SWAP — Exchange two elements in the array
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Swaps the elements at indices i and j in the heap array.
     *
     * Time Complexity: O(1) — performs three reference assignments in constant time.
     *
     * @param i first index
     * @param j second index
     */
    private void swap(int i, int j) {
        PriceAlert temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RESIZE — Double the array capacity when full
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Doubles the capacity of the internal array.
     *
     * Time Complexity: O(n) — allocates doubled array and copies all n elements.
     *
     * Called when the heap is full (size == capacity).
     * Creates a new array of double the size and copies all elements.
     *
     * This amortized resizing strategy means that even though a single
     * resize is O(n), across n insertions the average cost per insert
     * remains O(log n) (the resize cost is amortized O(1)).
     */
    private void resize() {
        capacity *= 2;
        PriceAlert[] newHeap = new PriceAlert[capacity + 1];

        // Copy existing elements (indices 1 through size)
        for (int i = 1; i <= size; i++) {
            newHeap[i] = heap[i];
        }

        heap = newHeap;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HEAPIFY UP — Restore heap property after insertion
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Bubbles a node UP from the given index until the min-heap property is restored.
     *
     * Time Complexity: O(log k) — bubbles new alert up the complete binary tree along height log2(k).
     *
     * @param index the index of the node to bubble up
     */
    private void heapifyUp(int index) {
        // Keep going while we haven't reached the root AND the current node
        // is smaller than its parent (heap property violation)
        while (index > 1 && heap[index].getThreshold() < heap[parent(index)].getThreshold()) {
            // Swap with parent to move the smaller value up
            swap(index, parent(index));
            // Move up to the parent's position and check again
            index = parent(index);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HEAPIFY DOWN — Restore heap property after extraction
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Sinks a node DOWN from the given index until the min-heap property is restored.
     *
     * Time Complexity: O(log k) — sinks replacement root down the tree along height log2(k) by swapping with smaller child.
     *
     * @param index the index of the node to sink down
     */
    private void heapifyDown(int index) {
        while (true) {
            int smallest = index; // Assume current node is the smallest
            int left = leftChild(index);
            int right = rightChild(index);

            // Check if left child exists AND is smaller than current smallest
            if (left <= size && heap[left].getThreshold() < heap[smallest].getThreshold()) {
                smallest = left;
            }

            // Check if right child exists AND is smaller than current smallest
            if (right <= size && heap[right].getThreshold() < heap[smallest].getThreshold()) {
                smallest = right;
            }

            // If current node is already the smallest, heap property is satisfied
            if (smallest == index) {
                break; // No more violations — we're done
            }

            // Swap with the smallest child and continue sinking
            swap(index, smallest);
            index = smallest;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PUBLIC OPERATIONS — Insert, Extract, Peek, isEmpty, getSize
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Inserts a new PriceAlert into the min-heap.
     *
     * ALGORITHM:
     * 1. If the heap is full, resize (double the capacity).
     * 2. Place the new element at the END of the array (next available index).
     *    This maintains the COMPLETE BINARY TREE property.
     * 3. Call heapifyUp to restore the MIN-HEAP PROPERTY by bubbling
     *    the new element up to its correct position.
     *
     * TIME COMPLEXITY: O(log n) amortized
     *   - heapifyUp: O(log n)
     *   - resize: O(n) but amortized O(1) over n insertions
     *
     * @param alert the PriceAlert to insert
     */
    public void insert(PriceAlert alert) {
        // Resize if the heap array is full
        if (size >= capacity) {
            resize();
        }

        // Step 1: Place new element at the end (maintains complete tree shape)
        size++;
        heap[size] = alert;

        // Step 2: Bubble up to restore heap property
        heapifyUp(size);
    }

    /**
     * Removes and returns the MINIMUM element (root) from the heap.
     *
     * ALGORITHM:
     * 1. Save the root element (the minimum).
     * 2. Move the LAST element to the root position.
     *    This maintains the COMPLETE BINARY TREE property (we remove from the end).
     * 3. Decrease size.
     * 4. Call heapifyDown to sink the new root to its correct position,
     *    restoring the MIN-HEAP PROPERTY.
     *
     * WHY MOVE THE LAST ELEMENT TO ROOT?
     * If we just removed the root, we'd have two separate subtrees with no root.
     * By moving the last element up, we maintain the tree structure and only
     * need to fix the heap property at one location (the root), which heapifyDown does.
     *
     * TIME COMPLEXITY: O(log n) — heapifyDown traverses at most O(log n) levels.
     *
     * @return the PriceAlert with the lowest threshold price, or null if empty
     */
    public PriceAlert extractMin() {
        if (isEmpty()) {
            return null;
        }

        // Save the minimum (root)
        PriceAlert min = heap[1];

        // Move last element to root
        heap[1] = heap[size];
        heap[size] = null; // Help garbage collection
        size--;

        // Sink the new root down to restore heap property
        if (size > 0) {
            heapifyDown(1);
        }

        return min;
    }

    /**
     * Returns the minimum element (root) WITHOUT removing it.
     *
     * Since the min-heap property guarantees the root is the smallest,
     * this is simply heap[1].
     *
     * TIME COMPLEXITY: O(1)
     *
     * @return the PriceAlert with the lowest threshold price, or null if empty
     */
    public PriceAlert peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[1];
    }

    /**
     * Checks whether the heap is empty.
     *
     * TIME COMPLEXITY: O(1)
     *
     * @return true if the heap contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements currently in the heap.
     *
     * TIME COMPLEXITY: O(1)
     *
     * @return the number of PriceAlert objects in the heap
     */
    public int getSize() {
        return size;
    }

    /**
     * Alias for getSize() to support standard collection naming.
     *
     * Time Complexity: O(1) — returns cached integer size counter.
     *
     * @return the number of PriceAlert objects in the heap
     */
    public int size() {
        return size;
    }

    /**
     * Returns all PriceAlerts currently in the heap as a list.
     * Elements are returned in their internal array order (indices 1 to size).
     *
     * Time Complexity: O(k) — iterates through all k active alerts in heap array.
     *
     * @return List of active PriceAlert objects
     */
    public java.util.List<PriceAlert> toList() {
        java.util.List<PriceAlert> list = new java.util.ArrayList<>();
        for (int i = 1; i <= size; i++) {
            if (heap[i] != null) {
                list.add(heap[i]);
            }
        }
        return list;
    }
}
