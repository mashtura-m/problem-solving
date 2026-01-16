package graphs.traversals;

public class DFSBase {
    public static void main(String[] args) {
        int[] root = {1, 2, 3, 4, 5, -1, 8, -1, -1, 6, 7, 9};
        TreeNode tree=TreeTraversals.generateTree(root);
        runDFS(tree);
    }

    private static void runDFS(TreeNode tree) {

    }
}
