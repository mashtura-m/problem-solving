package graphs.traversals;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

class TreeTraversals {
    public static void main(String[] args) {
        int[] root = {1, 2, 3, 4, 5, -1, 8, -1, -1, 6, 7, 9};
        TreeNode tree = generateTree(root);
        System.out.println(tree);
        List<Integer> path = doInorderTraversal(tree);
        System.out.println(path);
    }

    private static List<Integer> doInorderTraversal(TreeNode root) {
        List<Integer> path =new LinkedList<>();
        Stack <TreeNode> nodes=new Stack<>();
        TreeNode current=root;
        while(current!=null || !nodes.empty()) {
            while(current!=null){
                nodes.add(current);
                current=current.left;
            }
            current=nodes.pop();
            path.add(current.val);
            current=current.right;
        }
        return path;
    }

    private static TreeNode generateTree(int[] root) {
        if (root == null || root.length == 0) {
            return null;
        }

        // Create the root node
        TreeNode rootNode = new TreeNode(root[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(rootNode);

        int index = 1;

        while (index < root.length) {
            TreeNode currentNode = queue.poll();

            // Left child
            if (root[index] != -1) {
                currentNode.left = new TreeNode(root[index]);
                queue.offer(currentNode.left);
            }
            index++;

            // Right child
            if (index < root.length && root[index] != -1) {
                currentNode.right = new TreeNode(root[index]);
                queue.offer(currentNode.right);
            }
            index++;
            System.out.println(currentNode);
        }

        return rootNode;
    }
}


// Definition for a binary tree node.
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode() {
    }

    TreeNode(int val) {
        this.val = val;
    }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public String toString() {
        return val + " " + left + " " + right;
    }
}