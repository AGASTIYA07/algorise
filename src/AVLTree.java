import java.util.ArrayList;
import java.util.List;

/**
 * AVLTree.java — Self-Balancing Binary Search Tree
 * =================================================
 *
 * PURPOSE IN THIS PROJECT:
 * ────────────────────────
 * Implements a real-time performance leaderboard for the portfolio.
 * Stocks are inserted, deleted, and re-ranked using their daily percent price change as the sorting key.
 *
 * By maintaining the AVL balance invariant (|balance factor| <= 1), the tree guarantees:
 *   - O(log n) stock ranking insertion, removal, and re-balancing after price changes.
 *   - O(n) in-order traversal to produce the leaderboard sorted from worst to best performer.
 *   - Prevents tree degeneration into an O(n) linked list during monotonic market swings.
 */
class AVLTree {

    /**
     * The root of the AVL tree. null when the tree is empty.
     */
    private AVLNode root;

    /**
     * Constructs an empty AVL tree.
     *
     * Time Complexity: O(1) — initializes root pointer to null in constant time.
     */
    public AVLTree() {
        this.root = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPER METHODS — Height, Balance Factor
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns the height of the given node.
     * A null node has height -1 (this simplifies balance factor calculation).
     * A leaf node has height 0.
     *
     * Time Complexity: O(1) — null check and node field access in constant time.
     *
     * @param node the node to get the height of
     * @return the height, or -1 if node is null
     */
    private int getHeight(AVLNode node) {
        if (node == null) {
            return -1;
        }
        return node.height;
    }

    /**
     * Calculates the BALANCE FACTOR of a node.
     *
     * Time Complexity: O(1) — reads heights of left and right children in constant time.
     *
     * Balance Factor = height(left subtree) - height(right subtree)
     *
     * Interpretation:
     *   BF > 0  → left-heavy  (left subtree is taller)
     *   BF = 0  → perfectly balanced
     *   BF < 0  → right-heavy (right subtree is taller)
     *
     * AVL INVARIANT: |BF| ≤ 1 for every node.
     * If |BF| > 1 after an insertion or deletion, we must ROTATE.
     *
     *   BF = +2 → left-heavy violation  → needs right rotation (possibly with left rotation first)
     *   BF = -2 → right-heavy violation → needs left rotation (possibly with right rotation first)
     *
     * @param node the node to compute the balance factor for
     * @return the balance factor
     */
    private int getBalanceFactor(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return getHeight(node.left) - getHeight(node.right);
    }

    /**
     * Updates the height of a node based on its children's heights.
     * Must be called after any structural change (rotation, insert, delete).
     *
     * Time Complexity: O(1) — recalculates node height from child heights in constant time.
     *
     * @param node the node whose height to update
     */
    private void updateHeight(AVLNode node) {
        if (node != null) {
            node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ROTATIONS — The core rebalancing operations
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Performs a LEFT ROTATION on the given node.
     * Used when a node is right-heavy (balance factor = -2).
     *
     * Time Complexity: O(1) — performs exactly three reference pointer reassignments and height recalculations between adjacent nodes.
     *
     * @param x the node to rotate (becomes the left child of the result)
     * @return the new root of this subtree (was x's right child)
     */
    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;    // y is x's right child
        AVLNode T2 = y.left;    // T2 is y's left subtree (will be reassigned)

        // Perform the rotation
        y.left = x;             // x drops down to become y's left child
        x.right = T2;           // T2, which was between x and y, becomes x's right child

        // Update heights — x first because it's now below y
        updateHeight(x);
        updateHeight(y);

        return y; // y is the new root of this subtree
    }

    /**
     * Performs a RIGHT ROTATION on the given node.
     * Used when a node is left-heavy (balance factor = +2).
     *
     * Time Complexity: O(1) — performs constant-time reference pointer swaps and updates subtree heights in O(1).
     *
     * @param y the node to rotate (becomes the right child of the result)
     * @return the new root of this subtree (was y's left child)
     */
    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;     // x is y's left child
        AVLNode T2 = x.right;   // T2 is x's right subtree (will be reassigned)

        // Perform the rotation
        x.right = y;            // y drops down to become x's right child
        y.left = T2;            // T2, which was between x and y, becomes y's left child

        // Update heights — y first because it's now below x
        updateHeight(y);
        updateHeight(x);

        return x; // x is the new root of this subtree
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BALANCE — Detect and fix imbalances using the 4 rotation cases
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Checks the balance factor of the given node and performs rotations
     * if necessary to restore the AVL balance property (LL, LR, RR, RL cases).
     *
     * Time Complexity: O(1) — evaluates balance factor from child heights and executes at most two single rotations in constant time.
     *
     * @param node the node to check and potentially rebalance
     * @return the new root of this subtree after balancing
     */
    private AVLNode balance(AVLNode node) {
        if (node == null) {
            return null;
        }

        // Recalculate height after structural changes
        updateHeight(node);

        int balanceFactor = getBalanceFactor(node);

        // LEFT-HEAVY: balance factor is +2
        if (balanceFactor > 1) {
            if (getBalanceFactor(node.left) >= 0) {
                // CASE 1: LL — single right rotation
                return rotateRight(node);
            } else {
                // CASE 2: LR — left rotate the left child, then right rotate this node
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }
        }

        // RIGHT-HEAVY: balance factor is -2
        if (balanceFactor < -1) {
            if (getBalanceFactor(node.right) <= 0) {
                // CASE 3: RR — single left rotation
                return rotateLeft(node);
            } else {
                // CASE 4: RL — right rotate the right child, then left rotate this node
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }
        }

        // No imbalance — node is already balanced
        return node;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INSERT — Add a stock to the leaderboard
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Inserts a stock into the AVL tree, sorted by percent change.
     *
     * PUBLIC WRAPPER: Calls the recursive insert and updates the root.
     *
     * Time Complexity: O(log n) — traverses balanced tree height to place leaf, then executes at most 2 rotations to restore balance.
     *
     * @param stock the Stock object to insert
     */
    public void insert(Stock stock) {
        root = insert(root, stock, stock.getPercentChange());
    }

    /**
     * Recursively inserts a new node into the AVL tree and rebalances.
     *
     * ALGORITHM:
     * 1. Standard BST insert: compare keys, go left or right.
     * 2. For equal keys, use stock symbol (String comparison) as tiebreaker
     *    to ensure a deterministic, total ordering.
     * 3. After insertion, update height and rebalance using the balance() method.
     *
     * TIME COMPLEXITY: O(log n)
     *   - BST search to find insertion point: O(log n)
     *   - At most 2 rotations to rebalance: O(1) each
     *   - Height updates along the path: O(log n)
     *
     * @param node  the current node in the recursion
     * @param stock the Stock to insert
     * @param key   the sorting key (percent change)
     * @return the (possibly new) root of this subtree after insertion and balancing
     */
    private AVLNode insert(AVLNode node, Stock stock, double key) {
        // BASE CASE: Found the insertion point — create a new leaf
        if (node == null) {
            return new AVLNode(stock, key);
        }

        // Compare keys to decide direction
        if (key < node.key) {
            // Smaller key → go left
            node.left = insert(node.left, stock, key);
        } else if (key > node.key) {
            // Larger key → go right
            node.right = insert(node.right, stock, key);
        } else {
            // TIEBREAKER: Same percent change — use stock symbol alphabetically.
            // This ensures that two different stocks with the same percent change
            // both exist in the tree (the tree does NOT merge duplicates).
            if (stock.getSymbol().compareTo(node.stock.getSymbol()) < 0) {
                node.left = insert(node.left, stock, key);
            } else if (stock.getSymbol().compareTo(node.stock.getSymbol()) > 0) {
                node.right = insert(node.right, stock, key);
            } else {
                // Same symbol AND same key — update the stock reference in place
                node.stock = stock;
                return node; // No structural change, no need to rebalance
            }
        }

        // After inserting into a subtree, rebalance this node if needed
        return balance(node);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  REMOVE — Delete a stock from the leaderboard
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Removes a stock from the AVL tree by symbol alone.
     * Searches for the matching stock node, extracts its key, and deletes it.
     *
     * Time Complexity: O(log n) — searches down height of balanced tree to locate node, replaces with successor, and rebalances ancestors.
     *
     * @param symbol the stock ticker symbol (e.g., "AAPL")
     */
    public void remove(String symbol) {
        AVLNode target = findBySymbol(root, symbol);
        if (target != null) {
            remove(symbol, target.key);
        }
    }

    /**
     * Time Complexity: O(n) — traverses AVL nodes in worst-case to locate stock node matching ticker symbol.
     */
    private AVLNode findBySymbol(AVLNode node, String symbol) {
        if (node == null) return null;
        if (node.stock != null && node.stock.getSymbol().equalsIgnoreCase(symbol)) {
            return node;
        }
        AVLNode leftRes = findBySymbol(node.left, symbol);
        if (leftRes != null) return leftRes;
        return findBySymbol(node.right, symbol);
    }

    /**
     * Searches for a node with the given key (percent change).
     *
     * Time Complexity: O(log n) — compares keys down tree whose balanced height is guaranteed <= 1.44 log2(n).
     *
     * @param key the percent change to search for
     * @return the AVLNode if found, or null
     */
    public AVLNode search(double key) {
        return search(root, key);
    }

    /**
     * Time Complexity: O(log n) — compares keys down tree height of at most 1.44 log2(n).
     */
    private AVLNode search(AVLNode node, double key) {
        if (node == null || Double.compare(node.key, key) == 0) {
            return node;
        }
        if (key < node.key) {
            return search(node.left, key);
        }
        return search(node.right, key);
    }

    /**
     * Removes a stock from the AVL tree by its symbol and key (percent change).
     *
     * Time Complexity: O(log n) — recursively navigates down AVL height and rebalances ancestors.
     *
     * We need BOTH the symbol and the key because:
     *   - The key alone might match multiple stocks (same percent change)
     *   - The symbol uniquely identifies which node to remove
     *
     * @param symbol the stock ticker symbol (e.g., "AAPL")
     * @param key    the percent change value used as the tree key
     */
    public void remove(String symbol, double key) {
        root = remove(root, symbol, key);
    }

    /**
     * Recursively removes a node with the given symbol and key, then rebalances.
     *
     * ALGORITHM:
     * 1. Navigate using the key (and symbol for tiebreaking) to find the node.
     * 2. Once found, handle the standard BST deletion cases:
     *    a) Leaf node (no children): simply remove it (return null)
     *    b) One child: replace with the child
     *    c) Two children: find the IN-ORDER SUCCESSOR (smallest node in right subtree),
     *       copy its data, and recursively delete the successor.
     * 3. After deletion, update height and rebalance.
     *
     * TIME COMPLEXITY: O(log n)
     *
     * @param node   the current node in the recursion
     * @param symbol the stock symbol to match
     * @param key    the sorting key to match
     * @return the (possibly new) root of this subtree after deletion and balancing
     */
    private AVLNode remove(AVLNode node, String symbol, double key) {
        // BASE CASE: Node not found
        if (node == null) {
            return null;
        }

        // Navigate to find the target node
        if (key < node.key) {
            node.left = remove(node.left, symbol, key);
        } else if (key > node.key) {
            node.right = remove(node.right, symbol, key);
        } else {
            // Keys match — check symbol for tiebreaking
            int symbolCmp = symbol.compareTo(node.stock.getSymbol());

            if (symbolCmp < 0) {
                // Target symbol comes before this node's symbol → search left
                node.left = remove(node.left, symbol, key);
            } else if (symbolCmp > 0) {
                // Target symbol comes after this node's symbol → search right
                node.right = remove(node.right, symbol, key);
            } else {
                // ═══ FOUND THE NODE TO DELETE ═══

                // CASE A: Leaf node or node with only one child
                if (node.left == null) {
                    return node.right; // Replace with right child (may be null)
                } else if (node.right == null) {
                    return node.left;  // Replace with left child
                }

                // CASE B: Node has TWO children
                // Find the IN-ORDER SUCCESSOR: the smallest node in the right subtree
                // (leftmost node in the right subtree)
                AVLNode successor = findMin(node.right);

                // Copy the successor's data into this node
                node.stock = successor.stock;
                node.key = successor.key;

                // Recursively delete the successor from the right subtree
                // (the successor has at most one child — its right child)
                node.right = remove(node.right, successor.stock.getSymbol(), successor.key);
            }
        }

        // Rebalance this node after deletion
        return balance(node);
    }

    /**
     * Finds the node with the minimum key in the given subtree.
     * This is the leftmost node (keep going left until left is null).
     *
     * Time Complexity: O(log n) — traverses left child pointers down tree height to leftmost node.
     *
     * Used to find the IN-ORDER SUCCESSOR during deletion of a node
     * with two children.
     *
     * @param node the root of the subtree to search
     * @return the node with the smallest key in the subtree
     */
    private AVLNode findMin(AVLNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  IN-ORDER TRAVERSAL — Produce sorted leaderboard
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns all stocks sorted by percent change in ASCENDING order.
     *
     * An in-order traversal of a BST visits nodes in sorted order:
     *   1. Visit left subtree  (all smaller keys)
     *   2. Visit current node  (current key)
     *   3. Visit right subtree (all larger keys)
     *
     * This gives us the stock leaderboard from worst performer to best.
     *
     * TIME COMPLEXITY: O(n) — every node is visited exactly once.
     *
     * @return a List of Stock objects sorted by percent change (ascending)
     */
    public List<Stock> inOrderTraversal() {
        List<Stock> result = new ArrayList<>();
        inOrderTraversal(root, result);
        return result;
    }

    /**
     * Recursive helper for in-order traversal.
     *
     * Time Complexity: O(n) — visits every node in the subtree exactly once.
     *
     * @param node   the current node
     * @param result the list accumulating stocks in sorted order
     */
    private void inOrderTraversal(AVLNode node, List<Stock> result) {
        if (node == null) {
            return; // Base case: empty subtree
        }

        // 1. Recurse into left subtree (smaller keys first)
        inOrderTraversal(node.left, result);

        // 2. Process current node (add to result list)
        result.add(node.stock);

        // 3. Recurse into right subtree (larger keys next)
        inOrderTraversal(node.right, result);
    }

    /**
     * Returns the root node (for testing/debugging purposes).
     *
     * Time Complexity: O(1) — returns root pointer in constant time.
     *
     * @return the root AVLNode, or null if the tree is empty
     */
    public AVLNode getRoot() {
        return root;
    }
}
