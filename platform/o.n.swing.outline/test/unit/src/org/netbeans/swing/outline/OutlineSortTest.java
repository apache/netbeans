package org.netbeans.swing.outline;

import org.netbeans.swing.etable.ETableColumn;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.junit.NbTestCase;

public class OutlineSortTest extends NbTestCase {

    public OutlineSortTest(String name) {
        super(name);
    }

    /**
     * Tests that nodes dynamically added to an unsorted outline component are rendered in the correct order.
     */
    public void testDynamicNodesInUnsortedOutline() {
        // Basic creation of an outline component:
        Outline outline = new Outline();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(Integer.valueOf(0), true);
        DefaultTreeModel treeModel = new DefaultTreeModel(root, true);
        OutlineModel tableModel = DefaultOutlineModel.createOutlineModel(treeModel, new OneColumnRowModel());
        outline.setRootVisible(false);

        // Add an expansion listener which dynamically creates or removes nodes.
        TreeWillExpandListener listener = new DynamicNodeListener(treeModel);
        tableModel.getTreePathSupport().addTreeWillExpandListener(listener);
        outline.setModel(tableModel);

        // Add some children, and children of children to the tree:
        createTestNodes(treeModel, tableModel, root);

        // Build a list of node values in the order of the tree, which is unsorted:
        List<Integer> list = buildNodeValueList(root);

        // Print some useful info for debugging and visualising the issue to console:
        printUnsortedTree(root);
        printInfo(outline, list, "Unsorted");

        // Test the node order is correct:
        testNodeOrder(outline, list);
    }

    /**
     * Tests that nodes dynamically added to a sorted outline component are rendered in the correct order,
     * when they are re-sorted after adding.
     */
    public void testDynamicNodesInReSortedOutline() {
        // Basic creation of an outline component:
        Outline outline = new Outline();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(Integer.valueOf(0), true);
        DefaultTreeModel treeModel = new DefaultTreeModel(root, true);
        OutlineModel tableModel = DefaultOutlineModel.createOutlineModel(treeModel, new OneColumnRowModel());
        outline.setRootVisible(false);

        // Add an expansion listener which dynamically creates or removes nodes.
        TreeWillExpandListener listener = new DynamicNodeListener(treeModel);
        tableModel.getTreePathSupport().addTreeWillExpandListener(listener);
        outline.setModel(tableModel);

        // Set descending sorting on the column
        setDescendingSort(outline);

        // Add some children, and children of children to the tree:
        createTestNodes(treeModel, tableModel, root);

        // Build a list of node values in the order of the tree, which is sorted descending:
        List<Integer> list = buildSortedNodeValueList(root);

        // Print some useful info for debugging and visualising the issue to console BEFORE resorting:
        printSortedTree(root);
        printInfo(outline, list, "Sorted");

        // Force resort of columns before we look at the rows:
        setDescendingSort(outline);

        // Print some useful info for debugging and visualising the issue to console AFTER resorting:
        printSortedTree(root);
        printInfo(outline, list, "Re-sorted");

        // Test the node order is correct:
        testNodeOrder(outline, list);
    }

    /**
     * Tests that nodes dynamically added to a sorted outline component are rendered in the correct order,
     */
    public void testDynamicNodesInSortedOutline() {
        // Basic creation of an outline component:
        Outline outline = new Outline();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(Integer.valueOf(0), true);
        DefaultTreeModel treeModel = new DefaultTreeModel(root, true);
        OutlineModel tableModel = DefaultOutlineModel.createOutlineModel(treeModel, new OneColumnRowModel());
        outline.setRootVisible(false);

        // Add an expansion listener which dynamically creates or removes nodes.
        TreeWillExpandListener listener = new DynamicNodeListener(treeModel);
        tableModel.getTreePathSupport().addTreeWillExpandListener(listener);
        outline.setModel(tableModel);

        // Set descending sorting on the column
        setDescendingSort(outline);

        // Add some children, and children of children to the tree:
        createTestNodes(treeModel, tableModel, root);

        // Build a list of node values in the order of the tree, which is sorted descending:
        List<Integer> list = buildSortedNodeValueList(root);

        // Print some useful info for debugging and visualising the issue to console:
        printSortedTree(root);
        printInfo(outline, list, "Sorted");

        // Test the node order is correct:
        testNodeOrder(outline, list);
    }


    /* ********************************************************************************************************
     * Private helper methods.
     */

    /**
     * Sets an outline to sort descending on the the first column.
     * @param outline The Outline to set descending sort on.
     */
    private void setDescendingSort(Outline outline) {
        TableColumnModel columnModel = outline.getColumnModel();
        ETableColumn mainColumn = (ETableColumn) columnModel.getColumn(0);
        int modelIndex = mainColumn.getModelIndex();
        outline.setColumnSorted(modelIndex, false, 1);
    }

    /**
     * Tests that the values of the rows in an outline are in the same order as the expected values.
     *
     * @param outline The outline to test.
     * @param expectedValues A list of node values in the expected order.
     */
    private void testNodeOrder(Outline outline, List<Integer> expectedValues) {
        // Test that the values rendered in each row are the same as the
        // the node values obtained from the tree.
        assertEquals(expectedValues.size(), outline.getRowCount());
        for (int row = 0; row < outline.getRowCount(); row++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) outline.getValueAt(row, 0);
            int rowValue = (Integer) (node.getUserObject());
            int listValue = expectedValues.get(row);
            assertEquals(listValue, rowValue);
        }
    }

    /**
     * Creates test nodes.
     * It first creates 5 child nodes under the root.
     * Then it expands the 4th node of these children, which will acquire 5 children due to the DynamicNodeListener.
     * @param treeModel The tree model to add children.
     * @param tableModel The table model to be expanded.
     * @param root The root node of the tree.
     */
    private void createTestNodes(DefaultTreeModel treeModel, OutlineModel tableModel, DefaultMutableTreeNode root) {
        // Add 5 children under the root:
        addChildren(treeModel, root, 5);
        treeModel.nodeStructureChanged(root);

        // Expand the fourth child (will get 5 new nodes adding by the listener):
        DefaultMutableTreeNode fourthChild = getChild(root, 3);
        expandNode(tableModel, fourthChild);
    }

    /**
     * Prints a list of row values, and a list of expected node values.  These should be the same.
     *
     * @param outline The outline component
     * @param nodeOrder A list of node values in their expected order.
     * @param description A description to append to the output.
     */
    private void printInfo(Outline outline, List<Integer> nodeOrder, String description) {
        System.out.println(description + " row values:");
        printRowValues(outline);
        System.out.println(description + " node values:");
        printIntList(nodeOrder);
    }

    /**
     * Prints the row values in the Outline.
     *
     * @param outline The Outline to print.
     */
    private void printRowValues(Outline outline) {
        for (int row = 0; row < outline.getRowCount(); row++) {
            System.out.println(outline.getValueAt(row, 0));
        }
        System.out.println();
    }

    /**
     * Returns the child of a DefaultMutableTreeNode cast to a DefaultMutableTreeNode.
     * @param parent The node to get a child for.
     * @param childIndex The index of the child.
     * @return the child of a DefaultMutableTreeNode cast to a DefaultMutableTreeNode.
     */
    private DefaultMutableTreeNode getChild(DefaultMutableTreeNode parent, int childIndex) {
        return (DefaultMutableTreeNode) parent.getChildAt(childIndex);
    }

    /**
     * Expands a node in the outline model.  This will cause the DynamicNodeListener to add
     * new children to the node.
     *
     * @param outlineModel The outline model to expand.
     * @param node The node to expand.
     */
    private void expandNode(OutlineModel outlineModel, DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        outlineModel.getTreePathSupport().expandPath(path);
    }

    /**
     * Prints a list of integers.
     * @param list The list to print.
     */
    private void printIntList(List<Integer> list) {
        for (Integer i :list) {
            System.out.println(i);
        }
        System.out.println();
    }

    /**
     * Builds a list of node values in the order they are defined in the tree.
     *
     * @param rootNode The node to build a list from.
     * @return A list of node values in the order they are defined in the tree.
     */
    private List<Integer> buildNodeValueList(DefaultMutableTreeNode rootNode) {
        List<Integer> list = new ArrayList<Integer>();
        buildNodeValueList(rootNode, list);
        // get rid of the root node value - it's not visible so doesn't appear in the
        // row count we're going to test against:
        list.remove(0);
        return list;
    }

    /**
     * Builds a list of node values in the order they are defined in the tree.
     *
     * @param node The node to build a list from.
     * @param list the list to add the node values to.
     * @return A list of node values in the order they are defined in the tree.
     */
    private void buildNodeValueList(DefaultMutableTreeNode node, List<Integer> list) {
        int nodeValue = (Integer) node.getUserObject();
        list.add(nodeValue);
        for (int childIndex = 0; childIndex < node.getChildCount(); childIndex++) {
            buildNodeValueList(getChild(node, childIndex), list);
        }
    }

    /**
     * Builds a list of node values in descending order (last child first)
     *
     * @param rootNode The node to build a list from.
     * @return A list of node values in the order they are defined in the tree.
     */
    private List<Integer> buildSortedNodeValueList(DefaultMutableTreeNode rootNode) {
        List<Integer> list = new ArrayList<Integer>();
        buildSortedNodeValueList(rootNode, list);
        // get rid of the root node value - it's not visible so doesn't appear in the
        // row count we're going to test against:
        list.remove(0);
        return list;
    }

    /**
     * Builds a list of node values in descending order (last child first)
     *
     * @param node The node to build a list from.
     * @param list the list to add the node values to.
     * @return A list of node values in the order they are defined in the tree.
     */
    private void buildSortedNodeValueList(DefaultMutableTreeNode node, List<Integer> list) {
        int nodeValue = (Integer) node.getUserObject();
        list.add(nodeValue);
        for (int childIndex = node.getChildCount() - 1; childIndex >= 0; childIndex--) {
            buildSortedNodeValueList(getChild(node, childIndex), list);
        }
    }

    /**
     * Outputs the unsorted tree to the console.
     *
     * @param parentNode The node to begin from.
     */
    private void printUnsortedTree(DefaultMutableTreeNode parentNode) {
        System.out.println("Tree:");
        printUnsortedTree(parentNode, 0, false);
        System.out.println();
    }

    /**
     * Outputs the unsorted tree to the console, with an indentation level.
     *
     * @param node  The node to print
     * @param level The level of indentation for the node.
     */
    private void printUnsortedTree(DefaultMutableTreeNode node, int level, boolean printNode) {
        String indents = "\t\t\t\t\t\t\t\t\t\t";
        if (printNode) {
            System.out.println(indents.substring(0, level + 1) + (Integer) node.getUserObject());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            printUnsortedTree(child, level + 1, true);
        }
    }

    /**
     * Outputs the Sorted tree to the console.
     *
     * @param parentNode The node to begin from.
     */
    private void printSortedTree(DefaultMutableTreeNode parentNode) {
        System.out.println("Tree:");
        printSortedTree(parentNode, 0, false);
        System.out.println();
    }

    /**
     * Outputs the unsorted tree to the console, with an indentation level.
     *
     * @param node  The node to print
     * @param level The level of indentation for the node.
     */
    private void printSortedTree(DefaultMutableTreeNode node, int level, boolean printNode) {
        String indents = "\t\t\t\t\t\t\t\t\t\t";
        if (printNode) {
            System.out.println(indents.substring(0, level + 1) + (Integer) node.getUserObject());
        }
        for (int i =  node.getChildCount() - 1; i >= 0; i--) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            printSortedTree(child, level + 1, true);
        }
    }

    /**
     * Adds a number of new children to a tree node, then refreshes the node structure.
     *
     * @param treeModel  The tree model being used.
     * @param parentNode The node to add children to.
     * @param numChildren The number of children to add.
     */
    private static void addChildren(DefaultTreeModel treeModel, DefaultMutableTreeNode parentNode, int numChildren) {
        int parentNodeValue = (Integer) parentNode.getUserObject();
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            Integer childValue = parentNodeValue * 10 + childIndex;
            DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(childValue, true);
            parentNode.add(newChild);
        }
        treeModel.nodeStructureChanged(parentNode);
    }

    /**
     * Implements a simple row model for the outline with a single column.
     */
    private static class OneColumnRowModel implements org.netbeans.swing.outline.RowModel {

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueFor(Object node, int column) {
            if (node != null) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                return treeNode.getUserObject();
            }
            return null;
        }

        @Override
        public Class getColumnClass(int column) {
            return Integer.class;
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        @Override
        public void setValueFor(Object node, int column, Object value) {
            // can't set values.
        }

        @Override
        public String getColumnName(int column) {
            return "Column 1";
        }
    }

    /**
     * Dynamically adds or removes nodes on TreeExpansionEvents.
     */
    private static class DynamicNodeListener implements TreeWillExpandListener {

        private DefaultTreeModel model;

        public DynamicNodeListener(DefaultTreeModel model) {
            this.model = model;
        }
        /**
         * Adds 5 child nodes to a node which is expanding.
         * @param event The TreeExpansionEvent
         * @throws ExpandVetoException if the tree should not expand.
         */
        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            DefaultMutableTreeNode expandingNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            expandingNode.removeAllChildren();
            addChildren(model, expandingNode, 5);
        }

        /**
         * Removes all children from a collapsing node.
         * @param event The TreeExpansionEvent
         * @throws ExpandVetoException if the tree should not collapse.
         */
        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            DefaultMutableTreeNode collapsingNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            collapsingNode.removeAllChildren();
        }
    }
}
