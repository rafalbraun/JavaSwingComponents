package file_browser;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class FileManagerLazy extends JPanel {
    private static final String SYSTEM_TEMP_DIR = "/tmp";
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private final JTable table;
    private final JTree tree;
    private FileTableModel model;

    private CustomTreeExpansionListener treeExpansionListener = new CustomTreeExpansionListener();
    private CustomTreeSelectionListener treeSelectionListener = new CustomTreeSelectionListener();
    private DefaultMutableTreeNode currentNode;

    private final int rowIconPadding = 6;

    public FileManagerLazy(final String topDir) {
        super(new GridLayout(1,0));

        //Create a tree that allows one selection at a time
        DefaultMutableTreeNode top = createFilesystemNodesLazy(Paths.get(topDir));
        tree = new JTree(top);
        tree.addTreeWillExpandListener(treeExpansionListener);
        tree.addTreeSelectionListener(treeSelectionListener);
        //tree.addMouseListener(new CustomTreeMouseListener());
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane treeView = new JScrollPane(tree);

        //Create table that lists all files under selected directory
        table = new JTable();
        model = new FileTableModel();
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        JScrollPane tableScroll = new JScrollPane(table);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(tableScroll);
        splitPane.setDividerLocation(350);

        //Add the split pane to this panel.
        add(splitPane);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    //Object rowData = model.getValueAt(selectedRow, 0); // Assuming the object is in the first column
                    //System.out.println("Object associated with row " + selectedRow + ": " + rowData);

                    File file = model.getFileNode(selectedRow);
                    String filePath = file.getAbsolutePath();

//                    String[] pathNodes = {"/home/vanqyard"};
//                    TreePath treePath = getTreePathFromNames(tree, pathNodes);
//                    tree.setSelectionPath(treePath);

//                    TreeNode root = (TreeNode) tree.getModel().getRoot();
//                    TreeNode[] nodes = {root, root.getChildAt(2)};
//                    TreePath treePath = new TreePath(nodes);

                    TreePath treePath = tree.getSelectionPath();
                    //DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

//                    for (int i = 0; i < root.getChildCount(); i++) {
//                        TreeNode childNode = root.getChildAt(i);
//                        System.out.println(childNode);
//                        System.out.println(childNode.getChildCount());
//                    }

                    //DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
                    TreeExpansionEvent event = new TreeExpansionEvent(table, treePath);
                    treeExpansionListener.treeWillExpand(event);
                    tree.expandPath(treePath);

                    //System.out.println(file.getName());

                    DefaultMutableTreeNode finalNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                    Enumeration<?> children = finalNode.children();
                    while (children.hasMoreElements()) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
                        System.out.println(childNode.getUserObject());

                        if (childNode.getUserObject().toString().equals(filePath)) {
                            System.out.println("EQUAL");
                        }

                        TreeNode[] originalNodes = finalNode.getPath();

                        // Create a new array with a larger size
                        TreeNode[] updatedNodes = new TreeNode[originalNodes.length + 1];
                        System.arraycopy(originalNodes, 0, updatedNodes, 0, originalNodes.length);
                        updatedNodes[originalNodes.length] = childNode;

                        TreeSelectionEvent event2 = new TreeSelectionEvent(table, new TreePath(updatedNodes), false, null, null);
                        treeSelectionListener.valueChanged(event2);
                    }



//                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
//
//                    TreeNode newNode = NodeUtils.findNodeWithValue(node, file.getName());
//                    System.out.println(node);
//                    System.out.println(newNode);

                    //System.out.println(root.getChildAt(2).getChildCount());
                    //System.out.println(node.getChildCount());

                    //tree.expandPath(treePath);
                    //System.out.println(selectedRow);


                    /*
                    TreeNode newNode = currentNode.getChildAt(selectedRow);
                    TreeNode[] originalNodes = currentNode.getPath();

                    // Create a new array with a larger size
                    TreeNode[] updatedNodes = new TreeNode[originalNodes.length + 1];
                    System.arraycopy(originalNodes, 0, updatedNodes, 0, originalNodes.length);
                    updatedNodes[originalNodes.length] = newNode;

                    TreeSelectionEvent event2 = new TreeSelectionEvent(table, new TreePath(updatedNodes), false, null, null);
                    treeSelectionListener.valueChanged(event2);
                    */

                }
            }
        });
    }

//    private void expandTreeNode(TreePath treePath, TreeNode treeNode) {
//        TreeNode[] originalNodes = currentNode.getPath();
//
//        // Create a new array with a larger size
//        TreeNode[] updatedNodes = new TreeNode[originalNodes.length + 1];
//        System.arraycopy(originalNodes, 0, updatedNodes, 0, originalNodes.length);
//        updatedNodes[originalNodes.length] = newNode;
//
//        TreeSelectionEvent event2 = new TreeSelectionEvent(table, new TreePath(updatedNodes), false, null, null);
//        treeSelectionListener.valueChanged(event2);
//    }


    /////////////////////////////////////

    private DefaultMutableTreeNode createFilesystemNodesEager(Path path) {
        return loadNodes(path);
    }

    private DefaultMutableTreeNode loadNodes(Path path) {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(path.toFile());
        DefaultMutableTreeNode node = null;

        File[] files = fileSystemView.getFiles(path.toFile(), true);
        Arrays.sort(files);
        for (File file : files) {
            if(file.isDirectory()) {
                if (containsDirs(file.toPath())) {
                    node = loadNodes(file.toPath());
                    top.add(node);
                } else {
                    node = new DefaultMutableTreeNode(file);
                    top.add(node);
                }
            }
        }
        return top;
    }

    /**
     * Add the files that are contained within the directory of this node.
     */
    private void showChildren(final DefaultMutableTreeNode node) {
        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    File[] files = fileSystemView.getFiles(file, true);
                    if (node.isLeaf()) {
                        for (File child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                            }
                        }
                    }
                    setTableData(files);
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }
        };
        worker.execute();
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width<0) {
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (model==null) {
                    model = new FileTableModel();
                    table.setModel(model);
                }
                model.setFiles(files);

                // need to check if there are any files in folder before we reference file/folder icon
                if (files.length > 0) {
                    // add file/directory icon
                    Icon icon = fileSystemView.getSystemIcon(files[0]);

                    // size adjustment to better account for icons
                    table.setRowHeight(icon.getIconHeight() + rowIconPadding);
                }

                setColumnWidth(0,-1);
                setColumnWidth(3,100);
                setColumnWidth(4,90);
            }
        });
    }
/*
    public class CustomTreeMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            tree.setSelectionPath(selPath);

            if (e.isPopupTrigger()) {
                if(selRow != -1) {
                    if (e.getButton() == MouseEvent.BUTTON3) {

                        // read path from tree node
                        assert selPath != null;
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        File file = (File)node.getUserObject();
                        String path = file.getAbsolutePath();

                        //doPop(e, path);
                    }
                }
            }
        }

//            private void doPop(MouseEvent e, String data) {
//                PopupMenu menu = new PopupMenu(data);
//                menu.show(e.getComponent(), e.getX(), e.getY());
//            }
    }
*/
    public class CustomTreeExpansionListener implements TreeWillExpandListener {
        public void treeWillExpand(TreeExpansionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            node.removeAllChildren();
            File file = (File) node.getUserObject();
            File[] files = fileSystemView.getFiles(file, true);
            Arrays.sort(files);
            for (File f : files) {
                if(f.isDirectory()) {
                    DefaultMutableTreeNode subn = new DefaultMutableTreeNode(f);
                    node.add(subn);
                    if (containsDirs(f.toPath())) {
                        DefaultMutableTreeNode subsub = new DefaultMutableTreeNode(new File(SYSTEM_TEMP_DIR));
                        subn.add(subsub);
                    }
                }
            }
        }

        public void treeWillCollapse(TreeExpansionEvent e) {}
    }

    public class CustomTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            //DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();

            currentNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            showChildren(currentNode);

            //File file = (File) node.getUserObject();
            //System.out.println(file.getPath());
        }
    }

    /**
     * Checks if directory has any contents
     * @href https://www.baeldung.com/java-check-empty-directory
     */
    public boolean containsDirs(Path path) {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                for (Path p : directory) {
                    if (p.toFile().isDirectory()) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error: are you sure that " + path + " directory exists?");
                e.printStackTrace();
            }
        }
        return false;
    }

    private DefaultMutableTreeNode createFilesystemNodesLazy(Path path) {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(path.toFile());
        DefaultMutableTreeNode node = null;

        File[] files = fileSystemView.getFiles(path.toFile(), true);
        Arrays.sort(files);
        for (File file : files) {
            if(file.isDirectory()) {
                node = new DefaultMutableTreeNode(file);
                top.add(node);
                if (containsDirs(file.toPath())) {
                    DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(new File(SYSTEM_TEMP_DIR));
                    node.add(subnode);
                }
            }
        }
        return top;
    }


}
