/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Point;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

/**
 * An operator to handle org.netbeans.swing.outline.Outline component used e.g.
 * in debugger views.
 *
 *
 * Warning: Do not use yet unless really neccessary!! Incomplete, under
 * development and most probably still buggy!
 *
 * @author Vojtech Sigler
 */
public class OutlineOperator extends JTableOperator {

    public OutlineOperator(ContainerOperator cont) {
        this(cont, 0);
    }

    public OutlineOperator(ContainerOperator cont, int index) {
        super((Outline) cont.waitSubComponent(
                new OutlineFinder(ComponentSearcher.getTrueChooser("Any Outline")),
                index));
        copyEnvironment(cont);
    }

    public OutlineOperator(Outline outline) {
        super(outline);
    }

    /**
     * Gets the Outline component the operator is working with.
     *
     * @return Outline component
     */
    public Outline getOutline() {
        return (Outline) getSource();
    }

    /**
     * Gets the current index of the tree column (column of the table which
     * contains the tree) in the table.
     *
     * @return index of the tree column or -1 if is not found (error)
     */
    public int getTreeColumnIndex() {
        int lnNumColumns = this.getColumnCount();

        for (int i = 0; i < lnNumColumns; i++) {
            if (convertColumnIndexToModel(i) == 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Tries to find a child of the irParentPath which matches isName. The index
     * parameter is used if we want indexth node of the same name (otherwise use
     * 0). If it is not found a TimeoutExpiredException is thrown.
     *
     * @param irParentPath parent path in which to search
     * @param isName name of the node to be found
     * @param inIndex index of the node among its siblings (in case there are
     * more of the same name)
     * @return found path
     */
    public TreePath findNextPathElement(final TreePath irParentPath, final String isName, final int inIndex) {
        if (!isExpanded(irParentPath)) {
            expandPath(irParentPath);
        }

        TreePath lrTreePath;
        Timeouts lrTimes = getTimeouts().cloneThis();

        //behavior should be similar to JTreeOperator, so we can use its timeout values
        lrTimes.setTimeout("Waiter.WaitingTime", getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout"));
        try {
            Waiter lrWaiter = new Waiter(new NodeWaiter(irParentPath, isName, inIndex));

            lrWaiter.setTimeouts(lrTimes);
            lrTreePath = (TreePath) lrWaiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }

        return lrTreePath;
    }

    /**
     * Tries to find a child of the irParentPath which matches isName. If it is
     * not found a TimeoutExpiredException is thrown.
     *
     * @param irParentPath
     * @param isName
     * @return found path
     */
    public TreePath findNextPathElement(TreePath irParentPath, String isName) {
        return findNextPathElement(irParentPath, isName, 0);
    }

    /**
     * Gets the first root node (child of the root element) of the specified
     * name.
     *
     * @param isName
     * @return
     */
    public OutlineNode getRootNode(String isName) {
        return getRootNode(isName, 0);
    }

    /**
     * Gets a root node (child of the root element) of the specified name. The
     * index parameter is used if we want indexth node of the same name
     * (otherwise use 0).
     *
     * @param isName
     * @param inIndex
     * @return
     */
    public OutlineNode getRootNode(String isName, int inIndex) {
        TreePath lrParentPath = new TreePath(getOutline().getOutlineModel().getRoot());

        return new OutlineNode(this, findNextPathElement(lrParentPath, isName, inIndex));
    }

    /**
     *
     * Tries to find a subpath (descendant) of a parent path in the tree.
     *
     * @param irParentPath parent path to start the search in
     * @param isPath node name or a list of nodes separated by "|"
     * @return found path
     */
    public TreePath findPath(TreePath irParentPath, String isPath) {
        int lnDelimIndex = isPath.indexOf("|");

        if (lnDelimIndex > -1) {
            TreePath lrFoundPath = findNextPathElement(irParentPath, isPath.substring(0, lnDelimIndex));
            return findPath(lrFoundPath, isPath.substring(lnDelimIndex + 1));
        }

        return findNextPathElement(irParentPath, isPath);
    }

    /**
     *
     * Tries to find a the defined path beginning from the root node.
     *
     * @param isPath node name or a list of nodes separated by "|"
     * @return found path
     */
    public TreePath findPath(String isPath) {
        TreePath lrParentPath = new TreePath(getOutline().getOutlineModel().getRoot());
        return findPath(lrParentPath, isPath);
    }

    /**
     * Waits until the given TreePath is expanded.
     *
     * @param irTP TreePath in question
     */
    public void waitExpanded(final TreePath irTP) {
        if (irTP != null) {
            waitState(new ComponentChooser() {

                @Override
                public boolean checkComponent(Component comp) {
                    return (isExpanded(irTP));
                }

                @Override
                public String getDescription() {
                    return ("Has \"" + irTP.toString() + "\" path expanded");
                }
            });
        } else {
            throw (new JemmyException("No such path: null"));
        }
    }

    /**
     * Returns modifier based on root being hidden or not. When the root is
     * hidden, all y coordinates need to be decreased by 1.
     *
     * @return 0 or -1
     */
    protected int getVisibleRootModifier() {
        return getOutline().isRootVisible() ? 0 : -1;
    }

    /**
     * Gets the location of a given TreePath in the Outline table (x for columns
     * and y for rows) or (-1,-1) in case it was not found.
     *
     * @param irTreePath TreePath in question
     * @return Location for the given path or Point(-1,-1) if it was not found.
     */
    public Point getLocationForPath(TreePath irTreePath) {
        int lnX = getTreeColumnIndex();

        int lnY = getRowForPath(irTreePath);

        return (lnY == -1) ? new Point(-1, -1) : new Point(lnX, lnY);
    }

    /**
     * Gets the row in the Outline table for a given TreePath or -1 if the
     * TreePath is invalid.
     *
     * @param irTreePath TreePath in question
     * @return Row of the path or -1 if it is invalid.
     */
    public int getRowForPath(TreePath irTreePath) {
        if (irTreePath.getParentPath() == null) {
            return getVisibleRootModifier();
        }

        if (!isExpanded(irTreePath.getParentPath())) {
            expandPath(irTreePath.getParentPath());
        }

        int lnRow = -1;

        while (irTreePath.getParentPath() != null) {
            lnRow += 1 + getPrecedingSiblingsRowSpan(irTreePath);
            irTreePath = irTreePath.getParentPath();
        }

        return lnRow;
    }

    /**
     * Gets the rowspan of siblings which are above irTreePath in the tree. Only
     * expanded paths are taken into account.
     *
     * @param irTreePath
     * @return
     */
    protected int getPrecedingSiblingsRowSpan(TreePath irTreePath) {
        OutlineModel lrModel = getOutline().getOutlineModel();

        if (irTreePath.getParentPath() == null) {
            return 0 + getVisibleRootModifier();
        }

        Object lrLast = irTreePath.getLastPathComponent();
        TreePath lrParent = irTreePath.getParentPath();
        int lnRowSpan = 0;

        int lnIndex = lrModel.getIndexOfChild(lrParent.getLastPathComponent(), lrLast);

        for (int i = lnIndex - 1; i >= 0; i--) {
            Object lrSibling = lrModel.getChild(lrParent.getLastPathComponent(), i);
            lnRowSpan += getRowSpanOfLastElement(lrParent.pathByAddingChild(lrSibling));
        }

        return lnRowSpan;
    }

    /**
     * Gets the total rowspan of the last element of irTreePath.
     *
     * @param irTreePath
     * @return
     */
    protected int getRowSpanOfLastElement(TreePath irTreePath) {
        OutlineModel lrModel = getOutline().getOutlineModel();

        if (!isExpanded(irTreePath)) {
            return 1;
        }

        Object lrLast = irTreePath.getLastPathComponent();
        int lnRowspan = 1; //1 for the current node
        int lnChildCount = lrModel.getChildCount(lrLast);

        for (int i = 0; i < lnChildCount; i++) {
            Object lnChild = lrModel.getChild(lrLast, i);

            TreePath lrTempPath = irTreePath.pathByAddingChild(lnChild);
            lnRowspan += getRowSpanOfLastElement(lrTempPath);
        }

        return lnRowspan;
    }

    public void selectPath(TreePath irPath) {
        Point lrLocation = getLocationForPath(irPath);

        if (!lrLocation.equals(new Point(-1, -1))) {
            this.selectCell(lrLocation.y, lrLocation.x);
        }
    }

    public void scrollToPath(TreePath irPath) {
        Point lrLocation = getLocationForPath(irPath);

        if (!lrLocation.equals(new Point(-1, -1))) {
            this.scrollToCell(lrLocation.y, lrLocation.x);
        }
    }

    //Mappings
    /**
     * Expands target path.
     *
     * @param irTP
     */
    public void expandPath(final TreePath irTP) {
        runMapping(new MapVoidAction("expandPath") {

            @Override
            public void map() {
                getOutline().expandPath(irTP);
            }
        });
    }

    /**
     * Returns true if target path is expanded.
     *
     * @param irTP
     * @return
     */
    public boolean isExpanded(final TreePath irTP) {
        return ((Boolean) runMapping(new MapAction("isExpanded") {

            @Override
            public Object map() {
                return (getOutline().isExpanded(irTP));
            }
        }));
    }

    //Nested classes
    private static class OutlineFinder implements ComponentChooser {

        private ComponentChooser subFinder;

        public OutlineFinder(ComponentChooser finder) {
            subFinder = finder;
        }

        @Override
        public boolean checkComponent(Component comp) {
            Class cls = comp.getClass();
            do {
                if (cls.getName().equals("org.netbeans.swing.outline.Outline")) {
                    return (subFinder.checkComponent(comp));
                }
            } while ((cls = cls.getSuperclass()) != null);
            return (false);
        }

        @Override
        public String getDescription() {
            return subFinder.getDescription();
        }
    }

    /**
     * Waits for indexth node with string name to appear.
     */
    private class NodeWaiter implements Waitable {

        private TreePath parentPath;
        private int[] rowsToSearch = null;
        private String name;
        private int index;

        public NodeWaiter(TreePath irParentPath, String isName, int inIndex) {
            parentPath = irParentPath;
            name = isName;
            index = inIndex;
        }

        /**
         * Returns the array of rows we will search in. The array is cached and
         * will regenerate when the child count of parentPath changes.
         *
         * @return array of row numbers
         */
        private int[] getRowsToSearch() {
            int lnRowSpan = getOutline().getOutlineModel().getChildCount(parentPath.getLastPathComponent());

            if ((rowsToSearch == null) || (rowsToSearch.length != lnRowSpan)) {
                int lnStartRow = getRowForPath(parentPath) + 1;
                int lnPrecedingSiblingRowSpan = 0;

                rowsToSearch = new int[lnRowSpan];
                for (int i = 0; i < lnRowSpan; i++) {
                    rowsToSearch[i] = lnStartRow + lnPrecedingSiblingRowSpan;
                    Object lrSibling = getOutline().getOutlineModel().getChild(parentPath.getLastPathComponent(), i);

                    TreePath lrSiblingPath = parentPath.pathByAddingChild(lrSibling);

                    lnPrecedingSiblingRowSpan += getRowSpanOfLastElement(lrSiblingPath);
                }
            }

            return rowsToSearch;
        }

        @Override
        public Object actionProduced(Object anObject) {
            TreePath lrPath;

            Point lrFindPoint = findCell(name, getRowsToSearch(), new int[]{getTreeColumnIndex()}, index);

            //no cell found
            if (lrFindPoint.equals(new Point(-1, -1))) {
                return null;
            }

            //y is row, x is not important since we're asking for a row in the tree
            lrPath = getOutline().getLayoutCache().getPathForRow(lrFindPoint.y);

            //path for the specified row not found or it is not visible
            if (lrPath == null) {
                return null;
            }

            //found a cell that is a not a direct child of the parent path
            if (lrPath.getPathCount() != parentPath.getPathCount() + 1) {
                return null;
            }

            return lrPath;
        }

        @Override
        public String getDescription() {
            return ("Tree node cell with name '" + name + "' present.");
        }
    }
}
