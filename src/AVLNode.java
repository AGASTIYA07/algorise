/**
 * AVLNode.java — AVL Tree Node
 * ============================
 * Represents a single stock node within the AVL Tree leaderboard.
 * Stores a reference to the Stock object and uses the stock's current percent change
 * as the BST sorting key, alongside subtree height for O(1) balance calculations.
 */
class AVLNode {

    /**
     * Reference to the Stock object containing all stock details
     * (symbol, name, price, percent change, etc.).
     */
    Stock stock;

    /**
     * The sorting key for BST ordering — this is the stock's percent change.
     * Nodes with smaller keys go to the left subtree; larger keys go right.
     * If two stocks have the same percent change, we use the stock symbol
     * (alphabetical order) as a tiebreaker.
     */
    double key;

    /**
     * Pointer to the left child (subtree with smaller keys).
     * null if this node has no left child.
     */
    AVLNode left;

    /**
     * Pointer to the right child (subtree with larger keys).
     * null if this node has no right child.
     */
    AVLNode right;

    /**
     * The height of the subtree rooted at this node.
     * - A leaf node (no children) has height 0.
     * - An internal node's height = 1 + max(height(left), height(right)).
     * - A null reference is treated as height -1.
     *
     * This is updated after every insertion/deletion to support
     * O(1) balance factor calculation.
     */
    int height;

    /**
     * Constructs a new AVL node with the given stock and sorting key.
     * The node starts as a leaf (height 0, no children).
     *
     * Time Complexity: O(1) — initializes node references and sets height to 0 in constant time.
     *
     * @param stock the Stock object this node represents
     * @param key   the sorting key (percent change of the stock)
     */
    AVLNode(Stock stock, double key) {
        this.stock = stock;
        this.key = key;
        this.left = null;
        this.right = null;
        this.height = 0; // New nodes are always leaves
    }
}
