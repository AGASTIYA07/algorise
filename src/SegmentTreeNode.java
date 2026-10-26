/**
 * SegmentTreeNode.java — Segment Tree Interval Node
 * ==================================================
 * Represents a single interval node in the Segment Tree.
 * Caches three aggregated metrics over a given day window:
 *   - minVal: lowest price in the interval
 *   - maxVal: highest price in the interval
 *   - sumVal: total price sum across the interval (used to compute window averages)
 */
class SegmentTreeNode {

    /**
     * The minimum value in the segment (range) represented by this node.
     * For a leaf node, this equals the single element's value.
     * For an internal node, this is min(leftChild.minVal, rightChild.minVal).
     */
    double minVal;

    /**
     * The maximum value in the segment (range) represented by this node.
     * For a leaf node, this equals the single element's value.
     * For an internal node, this is max(leftChild.maxVal, rightChild.maxVal).
     */
    double maxVal;

    /**
     * The sum of all values in the segment (range) represented by this node.
     * For a leaf node, this equals the single element's value.
     * For an internal node, this is leftChild.sumVal + rightChild.sumVal.
     * We use sum to compute AVERAGE = sumVal / numberOfElements.
     */
    double sumVal;

    /**
     * Constructs a SegmentTreeNode with the given aggregate values.
     *
     * Time Complexity: O(1) — assigns primitive min, max, and sum values in constant time.
     *
     * @param min the minimum value for this segment
     * @param max the maximum value for this segment
     * @param sum the sum of values for this segment
     */
    SegmentTreeNode(double min, double max, double sum) {
        this.minVal = min;
        this.maxVal = max;
        this.sumVal = sum;
    }
}
