/**
 * SegmentTree.java — Range Analytics Data Structure
 * ==================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Stores historical daily price segments for each stock, enabling instant analytical queries:
 *   - Range Minimum Price: lowest price reached in any given window [dayL, dayR].
 *   - Range Maximum Price: highest price reached in any given window [dayL, dayR].
 *   - Range Average Price: simple average price computed via range sum / day count.
 *   - Real-Time Point Updates: propagates new daily closing prices upward in O(log n) time
 *     without recomputing the entire historical dataset.
 */
class SegmentTree {

    /**
     * The internal array storing SegmentTreeNode objects.
     * Index 0 is unused; the tree is 1-indexed for easier child/parent math.
     * Size is 4 * capacity to handle worst-case tree size.
     */
    private SegmentTreeNode[] tree;

    /**
     * Maximum number of elements (days of price data) this tree can hold.
     */
    private int capacity;

    /**
     * Constructs a Segment Tree with the given maximum capacity.
     *
     * Time Complexity: O(capacity) — allocates and initializes 4 * capacity nodes with identity values.
     *
     * We allocate 4 * capacity nodes because:
     *   - A complete binary tree with n leaves has 2n - 1 nodes.
     *   - But our tree might not be perfectly complete (n may not be a power of 2).
     *   - 4 * n is a safe upper bound that guarantees no index-out-of-bounds.
     *
     * Each node is initialized with IDENTITY VALUES:
     *   - min → Double.MAX_VALUE  (any real value is ≤ this, so it's a neutral element for min)
     *   - max → Double.MIN_VALUE  (any real value is ≥ this, so it's a neutral element for max)
     *   - sum → 0.0              (adding 0 doesn't change a sum)
     *
     * @param capacity the maximum number of price entries this tree supports
     */
    public SegmentTree(int capacity) {
        this.capacity = capacity;
        int size = 4 * capacity;
        tree = new SegmentTreeNode[size];

        // Initialize every node with identity values so that unused nodes
        // don't corrupt query results.
        for (int i = 0; i < size; i++) {
            tree[i] = new SegmentTreeNode(Double.MAX_VALUE, Double.MIN_VALUE, 0.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BUILD — Constructing the Segment Tree from a price array
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Recursively builds the segment tree from the given price array.
     *
     * ALGORITHM (Divide and Conquer):
     * ───────────────────────────────
     * 1. BASE CASE: If the segment has only one element (start == end),
     *    create a leaf node where min = max = sum = prices[start].
     *
     * 2. RECURSIVE CASE: Split the segment into two halves:
     *    - Left half:  [start, mid]      → stored at node 2*node
     *    - Right half: [mid+1, end]      → stored at node 2*node + 1
     *    Recursively build both halves.
     *
     * 3. MERGE: Combine the two children into the current node:
     *    - min = Math.min(left.min, right.min)
     *    - max = Math.max(left.max, right.max)
     *    - sum = left.sum + right.sum
     *
     * EXAMPLE: Building from prices = [142.5, 145.0, 138.2]
     *
     *   build(prices, node=1, start=0, end=2)
     *     mid = 1
     *     build(prices, node=2, start=0, end=1)     ← left half
     *       mid = 0
     *       build(prices, node=4, start=0, end=0)   ← leaf: prices[0] = 142.5
     *       build(prices, node=5, start=1, end=1)   ← leaf: prices[1] = 145.0
     *       node 2: min=142.5, max=145.0, sum=287.5
     *     build(prices, node=3, start=2, end=2)     ← leaf: prices[2] = 138.2
     *     node 1: min=138.2, max=145.0, sum=425.7   ← ROOT
     *
     * TIME COMPLEXITY: O(n) — each of the n elements is visited exactly once
     *                  as a leaf, and there are O(n) internal nodes to merge.
     *
     * @param prices the array of stock prices (one per day)
     * @param node   the current node index in the tree array (start with 1 for root)
     * @param start  the left boundary of the current segment (inclusive)
     * @param end    the right boundary of the current segment (inclusive)
     */
    public void build(double[] prices, int node, int start, int end) {
        // BASE CASE: Leaf node — represents a single element
        if (start == end) {
            // A leaf node's min, max, and sum are all just the single price value
            tree[node].minVal = prices[start];
            tree[node].maxVal = prices[start];
            tree[node].sumVal = prices[start];
            return;
        }

        // RECURSIVE CASE: Split the range into two halves
        int mid = (start + end) / 2;

        // Build left subtree: covers range [start, mid]
        // Left child of node i is stored at index 2*i
        build(prices, 2 * node, start, mid);

        // Build right subtree: covers range [mid+1, end]
        // Right child of node i is stored at index 2*i + 1
        build(prices, 2 * node + 1, mid + 1, end);

        // MERGE: Combine children's results into the current (parent) node
        // The parent's min is the smaller of its two children's mins
        tree[node].minVal = Math.min(tree[2 * node].minVal, tree[2 * node + 1].minVal);
        // The parent's max is the larger of its two children's maxes
        tree[node].maxVal = Math.max(tree[2 * node].maxVal, tree[2 * node + 1].maxVal);
        // The parent's sum is the total of both children's sums
        tree[node].sumVal = tree[2 * node].sumVal + tree[2 * node + 1].sumVal;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  POINT UPDATE — Changing a single price and propagating up
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Updates the price at a specific index and propagates the change up the tree.
     *
     * ALGORITHM:
     * ──────────
     * 1. Navigate down the tree to find the leaf node for index `idx`.
     *    - If idx ≤ mid, go left  (node → 2*node)
     *    - If idx > mid, go right (node → 2*node + 1)
     *
     * 2. At the leaf, set min = max = sum = val (the new price).
     *
     * 3. On the way back up (after the recursive call returns), recalculate
     *    each ancestor's min, max, and sum from its two children.
     *    This is the "propagation" step — it ensures every node on the
     *    path from the leaf to the root reflects the updated value.
     *
     * EXAMPLE: Update index 1 from 145.0 → 148.0
     *
     *   Before:  node 1 (root): min=138.2, max=145.0
     *   Update leaf at index 1: now 148.0
     *   Recompute node 2: min = min(142.5, 148.0) = 142.5, max = max(142.5, 148.0) = 148.0
     *   Recompute node 1: min = min(142.5, 138.2) = 138.2, max = max(148.0, 138.2) = 148.0
     *
     * WHY THIS WORKS:
     * Only the nodes on the path from the updated leaf to the root are affected.
     * There are O(log n) such nodes, so the update is O(log n).
     *
     * TIME COMPLEXITY: O(log n) — we traverse exactly one root-to-leaf path.
     *
     * @param node  the current node index in the tree array
     * @param start left boundary of the segment this node represents
     * @param end   right boundary of the segment this node represents
     * @param idx   the index in the original array to update
     * @param val   the new price value
     */
    public void update(int node, int start, int end, int idx, double val) {
        // BASE CASE: We've reached the leaf node for index idx
        if (start == end) {
            // Update the leaf with the new value
            tree[node].minVal = val;
            tree[node].maxVal = val;
            tree[node].sumVal = val;
            return;
        }

        int mid = (start + end) / 2;

        // Decide which half contains the index to update
        if (idx <= mid) {
            // Index is in the left half → recurse left
            update(2 * node, start, mid, idx, val);
        } else {
            // Index is in the right half → recurse right
            update(2 * node + 1, mid + 1, end, idx, val);
        }

        // PROPAGATE UP: After updating the leaf, recalculate this node
        // from its (possibly updated) children.
        // This ensures the current node's aggregate values are correct.
        tree[node].minVal = Math.min(tree[2 * node].minVal, tree[2 * node + 1].minVal);
        tree[node].maxVal = Math.max(tree[2 * node].maxVal, tree[2 * node + 1].maxVal);
        tree[node].sumVal = tree[2 * node].sumVal + tree[2 * node + 1].sumVal;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RANGE QUERIES — Min, Max, Sum over an arbitrary range [l, r]
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Queries the MINIMUM value in the range [l, r].
     *
     * Handles the three interval cases:
     *   1. No overlap: segment is outside [l, r] -> return identity (Double.MAX_VALUE).
     *   2. Complete overlap: segment is inside [l, r] -> return cached minVal.
     *   3. Partial overlap: recurse left and right children and return min.
     *
     * Time Complexity: O(log n) — visits at most 4 nodes per level down tree height of log2(n).
     *
     * @param node  current node index in the tree array
     * @param start left boundary of the segment this node represents
     * @param end   right boundary of the segment this node represents
     * @param l     left boundary of the query range (inclusive)
     * @param r     right boundary of the query range (inclusive)
     * @return the minimum value in prices[l..r]
     */
    public double queryMin(int node, int start, int end, int l, int r) {
        // CASE 1: No overlap — this node's range is entirely outside the query range
        if (r < start || end < l) {
            return Double.MAX_VALUE; // Identity for min: won't affect Math.min result
        }

        // CASE 2: Complete overlap — this node's range is entirely within the query range
        if (l <= start && end <= r) {
            return tree[node].minVal; // We already have the answer stored!
        }

        // CASE 3: Partial overlap — split and recurse into both children
        int mid = (start + end) / 2;
        double leftMin = queryMin(2 * node, start, mid, l, r);
        double rightMin = queryMin(2 * node + 1, mid + 1, end, l, r);

        return Math.min(leftMin, rightMin);
    }

    /**
     * Queries the MAXIMUM value in the range [l, r].
     *
     * This works identically to queryMin, but:
     *   - Returns Double.MIN_VALUE as the identity (no-overlap case)
     *   - Uses Math.max instead of Math.min to combine results
     *
     * The three cases (no overlap, complete overlap, partial overlap)
     * are the same as described in queryMin above.
     *
     * TIME COMPLEXITY: O(log n)
     *
     * @param node  current node index in the tree array
     * @param start left boundary of the segment this node represents
     * @param end   right boundary of the segment this node represents
     * @param l     left boundary of the query range (inclusive)
     * @param r     right boundary of the query range (inclusive)
     * @return the maximum value in prices[l..r]
     */
    public double queryMax(int node, int start, int end, int l, int r) {
        // CASE 1: No overlap
        if (r < start || end < l) {
            return Double.MIN_VALUE; // Identity for max: won't affect Math.max result
        }

        // CASE 2: Complete overlap
        if (l <= start && end <= r) {
            return tree[node].maxVal;
        }

        // CASE 3: Partial overlap
        int mid = (start + end) / 2;
        double leftMax = queryMax(2 * node, start, mid, l, r);
        double rightMax = queryMax(2 * node + 1, mid + 1, end, l, r);

        return Math.max(leftMax, rightMax);
    }

    /**
     * Queries the SUM of all values in the range [l, r].
     *
     * This is used to compute the AVERAGE price over a period:
     *   average = querySum(...) / numberOfDays
     *
     * The three cases (no overlap, complete overlap, partial overlap)
     * are the same as described in queryMin above, but:
     *   - Returns 0.0 as the identity (no-overlap case)
     *   - Uses addition (+) instead of Math.min to combine results
     *
     * TIME COMPLEXITY: O(log n)
     *
     * @param node  current node index in the tree array
     * @param start left boundary of the segment this node represents
     * @param end   right boundary of the segment this node represents
     * @param l     left boundary of the query range (inclusive)
     * @param r     right boundary of the query range (inclusive)
     * @return the sum of all values in prices[l..r]
     */
    public double querySum(int node, int start, int end, int l, int r) {
        // CASE 1: No overlap
        if (r < start || end < l) {
            return 0.0; // Identity for sum: adding 0 doesn't change the total
        }

        // CASE 2: Complete overlap
        if (l <= start && end <= r) {
            return tree[node].sumVal;
        }

        // CASE 3: Partial overlap
        int mid = (start + end) / 2;
        double leftSum = querySum(2 * node, start, mid, l, r);
        double rightSum = querySum(2 * node + 1, mid + 1, end, l, r);

        return leftSum + rightSum;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  REBUILD — Reconstruct the entire tree from a new price array
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Rebuilds the entire segment tree from a new or updated price array.
     *
     * This is useful when:
     *   - The portfolio is reset with new data
     *   - Multiple prices change at once (rebuilding is simpler than many updates)
     *   - The number of tracked days changes
     *
     * The method first resets all nodes to identity values (to clear stale data
     * from a previous build that may have used more elements), then calls build.
     *
     * TIME COMPLEXITY: O(n) where n is the number of prices.
     *
     * @param prices the new array of stock prices
     * @param n      the number of valid elements in the prices array
     */
    public void rebuild(double[] prices, int n) {
        // Reset all nodes to identity values to clear any stale data
        for (int i = 0; i < tree.length; i++) {
            tree[i].minVal = Double.MAX_VALUE;
            tree[i].maxVal = Double.MIN_VALUE;
            tree[i].sumVal = 0.0;
        }

        // Rebuild the tree if there is data to build from
        if (n > 0) {
            build(prices, 1, 0, n - 1);
        }
    }

    /**
     * Convenience method: point update at index `idx` with `val`.
     * Operates over the full capacity range [0, capacity - 1].
     *
     * Time Complexity: O(log capacity) — delegates to root-to-leaf update traversing log2(capacity) levels.
     *
     * @param idx the 0-based index to update
     * @param val the new price value
     */
    public void update(int idx, double val) {
        if (idx < 0 || idx >= capacity) {
            return;
        }
        update(1, 0, capacity - 1, idx, val);
    }

    /**
     * Convenience method: queries minimum in range [l, r].
     *
     * Time Complexity: O(log capacity) — delegates to range query decomposing [l, r] into at most O(log capacity) nodes.
     *
     * @param l left boundary (0-based, inclusive)
     * @param r right boundary (0-based, inclusive)
     * @return minimum price in [l, r]
     */
    public double queryMin(int l, int r) {
        if (l < 0 || r >= capacity || l > r) {
            return Double.MAX_VALUE;
        }
        return queryMin(1, 0, capacity - 1, l, r);
    }

    /**
     * Convenience method: queries maximum in range [l, r].
     *
     * Time Complexity: O(log capacity) — delegates to range query decomposing [l, r] into at most O(log capacity) nodes.
     *
     * @param l left boundary (0-based, inclusive)
     * @param r right boundary (0-based, inclusive)
     * @return maximum price in [l, r]
     */
    public double queryMax(int l, int r) {
        if (l < 0 || r >= capacity || l > r) {
            return Double.MIN_VALUE;
        }
        return queryMax(1, 0, capacity - 1, l, r);
    }

    /**
     * Convenience method: queries sum in range [l, r].
     *
     * Time Complexity: O(log capacity) — delegates to range query decomposing [l, r] into at most O(log capacity) nodes.
     *
     * @param l left boundary (0-based, inclusive)
     * @param r right boundary (0-based, inclusive)
     * @return sum of prices in [l, r]
     */
    public double querySum(int l, int r) {
        if (l < 0 || r >= capacity || l > r) {
            return 0.0;
        }
        return querySum(1, 0, capacity - 1, l, r);
    }

    /**
     * Returns the maximum capacity of this segment tree.
     *
     * Time Complexity: O(1) — returns capacity integer constant.
     *
     * @return the maximum number of price entries this tree can hold
     */
    public int getCapacity() {
        return capacity;
    }
}
