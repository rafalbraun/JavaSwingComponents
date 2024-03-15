package file_browser;

import javax.swing.tree.DefaultMutableTreeNode;

public class NodeUtils {

    // Method to find a node with a given string value
    public static DefaultMutableTreeNode findNodeWithValue(DefaultMutableTreeNode root, String targetValue) {
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            System.out.println("::" + child.getChildAt(i));
        }
        return null; // Node not found
    }


}
