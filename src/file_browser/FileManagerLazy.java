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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileManagerLazy extends JPanel {
    private static final String SYSTEM_TEMP_DIR = "/tmp";
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private JTable table;
    private JTree tree;
    private FileTableModel model;
    private JScrollPane treeView;
    private JScrollPane tableScroll;

    private final int rowIconPadding = 6;

    public FileManagerLazy(final String topDir) {
        super(new GridLayout(1,0));

        //Create a tree that allows one selection at a time
        DefaultMutableTreeNode top = createFilesystemNodesLazy(Paths.get(topDir));
        tree = new JTree(top);
        tree.addTreeWillExpandListener(new CustomTreeExpansionListener());
        tree.addTreeSelectionListener(new CustomTreeSelectionListener());
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeView = new JScrollPane(tree);

        //Create table that lists all files under selected directory
        table = new JTable();
        model = new FileTableModel();
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        tableScroll = new JScrollPane(table);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(tableScroll);
        splitPane.setDividerLocation(350);

        //Add the split pane to this panel.
        add(splitPane);

        tree.addMouseListener(new MouseAdapter() {
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
        });

    }

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
                    File[] files = fileSystemView.getFiles(file, true); //!!
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
                    table.setRowHeight( icon.getIconHeight()+rowIconPadding );
                }

                setColumnWidth(0,-1);
                setColumnWidth(3,100);
                setColumnWidth(4,90);
            }
        });
    }

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
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            File file = (File) node.getUserObject();
            //System.out.println(file.getPath());
            showChildren(node);
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
