/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.jellytools.nodes;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.explorer.view.Visualizer;

/**
 * Ancestor class for all nodes.<p> Nodes should help to easier testing of
 * JTree's. The most frequent usage in IDE is in file views but nodes can be
 * used in any component which includes a JTree instance. Nodes are also used as
 * parameters for action's performing. <p> Example:
 * <pre>
 *     Node node = new Node(new SourcePackagesNode("My Project"), "org.netbeans.jellytools.nodes|Node.java");
 *     System.out.println(node.getText());
 *     new OpenAction().performAPI(node);
 * </pre>
 */
public class Node {

    static final String linkSuffix = Bundle.getString("org.openide.loaders.Bundle", "FMT_shadowName", new String[]{""});
    /**
     * JTreeOperator of tree where node lives
     */
    protected JTreeOperator treeOperator;
    /**
     * TreePath of node
     */
    protected TreePath treePath;
    /**
     * Path of node names representing this node.
     */
    protected String stringPath;
    /**
     * Comparator used for this node instance.
     */
    private Operator.StringComparator comparator;

    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
    }

    /**
     * creates new Node instance
     *
     * @param treeOperator JTreeOperator of tree where node lives
     * @param treePath String tree path of node
     */
    public Node(JTreeOperator treeOperator, String treePath) {
        this(treeOperator, new NodesJTreeOperator(treeOperator).findPath(treePath, "|"));
    }

    /**
     * creates new Node instance
     *
     * @param treeOperator JTreeOperator of tree where node lives
     * @param treePath String tree path of node
     * @param indexes String list of indexes of nodes in each level
     */
    public Node(JTreeOperator treeOperator, String treePath, String indexes) {
        this(treeOperator, new NodesJTreeOperator(treeOperator).findPath(treePath, indexes, "|"));
    }

    /**
     * creates new Node instance
     *
     * @param parent parent Node
     * @param treeSubPath String tree sub-path from parent
     */
    public Node(Node parent, String treeSubPath) {
        this(parent.tree(), parent.findSubPath(treeSubPath, "|"));
    }

    /**
     * creates new Node instance
     *
     * @param parent parent Node
     * @param childIndex int index of child under parent node
     */
    public Node(Node parent, int childIndex) {
        this(parent.tree(), parent.tree().getChildPath(parent.getTreePath(), childIndex));
    }

    /**
     * creates new Node instance
     *
     * @param treeOperator JTreeOperator of tree where node lives
     * @param path TreePath of node
     */
    public Node(JTreeOperator treeOperator, TreePath path) {
        // Only for plain JTreeOperator creates NodesJTreeOperator. If treeOperator
        // is something different (like TreeTableOperator$RenderedTreeOperator)
        // we cannot replace it.
        if (treeOperator.getClass().getName().endsWith("JTreeOperator")) {
            this.treeOperator = new NodesJTreeOperator(treeOperator);
        } else {
            this.treeOperator = treeOperator;
        }
        this.treePath = path;
        this.stringPath = convertPath(path);
    }

    /**
     * Sets comparator for this node. Comparator is used for all methods after
     * this method is called.
     *
     * @param comparator new comparator to be set (e.g. new
     * Operator.DefaultStringComparator(true, true); to search string item
     * exactly and case sensitive)
     */
    public void setComparator(Operator.StringComparator comparator) {
        this.comparator = comparator;
        tree().setComparator(comparator);
    }

    /**
     * Gets comparator for this node instance.
     *
     * @return comparator for this node instance.
     */
    public Operator.StringComparator getComparator() {
        if (comparator == null) {
            comparator = tree().getComparator();
        }
        return comparator;
    }

    /**
     * getter for JTreeOperator of tree where node lives
     *
     * @return JTreeOperator of tree where node lives
     */
    public JTreeOperator tree() {
        return treeOperator;
    }

    /**
     * Getter for TreePath of node.
     *
     * @return TreePath of node
     */
    public TreePath getTreePath() {
        if (treePath.getLastPathComponent().toString().isEmpty() || tree().getRowForPath(treePath) < 0) {
            // node was removed or re-created
            treePath = tree().findPath(stringPath);
        }
        return treePath;
    }

    /**
     * getter for node text
     *
     * @return String node text
     */
    public String getText() {
        return getTreePath().getLastPathComponent().toString();
    }

    private static String convertPath(TreePath path) {
        if (path == null) {
            return null;
        }
        int pathCount = path.getPathCount();
        if (pathCount < 2) {
            return "";
        }
        StringBuilder bufResult = new StringBuilder(path.getPathComponent(1).toString());
        for (int i = 2; i < pathCount; i++) {
            bufResult.append("|").append(path.getPathComponent(i).toString());
        }
        return bufResult.toString();
    }

    /**
     * getter for node path
     *
     * @return String node path
     */
    public String getPath() {
        return convertPath(getTreePath());
    }

    /**
     * getter for path of parent node
     *
     * @return String path of parent node
     */
    public String getParentPath() {
        return convertPath(getTreePath().getParentPath());
    }

    /**
     * Returns Object instance which represents org.openide.nodes.Node for this
     * jellytools node.
     *
     * @return Object instance which represents org.openide.nodes.Node
     */
    public Object getOpenideNode() {
        return Visualizer.findNode(this.getTreePath().getLastPathComponent());
    }

    /**
     * calls popup menu on node
     *
     * @return JPopupMenuOperator
     */
    public JPopupMenuOperator callPopup() {
        return new JPopupMenuOperator(treeOperator.callPopupOnPath(getTreePath()));
    }

    /**
     * performs action on node through main menu
     *
     * @param menuPath main menu path of action
     */
    public void performMenuAction(String menuPath) {
        new Action(menuPath, null).performMenu(this);
    }

    /**
     * performs action on node through popup menu
     *
     * @param popupPath popup menu path of action
     */
    public void performPopupAction(String popupPath) {
        new Action(null, popupPath).performPopup(this);
    }

    /**
     * performs action on node through API menu
     *
     * @param systemActionClass String class name of SystemAction (use null
     * value if API mode is not supported)
     */
    public void performAPIAction(String systemActionClass) {
        new Action(null, null, systemActionClass).performAPI(this);
    }

    /**
     * performs action on node through main menu
     *
     * @param menuPath main menu path of action
     */
    public void performMenuActionNoBlock(String menuPath) {
        new ActionNoBlock(menuPath, null).performMenu(this);
    }

    /**
     * performs action on node through popup menu
     *
     * @param popupPath popup menu path of action
     */
    public void performPopupActionNoBlock(String popupPath) {
        new ActionNoBlock(null, popupPath).performPopup(this);
    }

    /**
     * performs action on node through API menu
     *
     * @param systemActionClass String class name of SystemAction (use null
     * value if API mode is not supported)
     */
    public void performAPIActionNoBlock(String systemActionClass) {
        new ActionNoBlock(null, null, systemActionClass).performAPI(this);
    }

    /**
     * selects node
     */
    public void select() {
        tree().selectPath(getTreePath());
        // We need to click on path on Mac because selectPath doesn't fire selection change event
        if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) { // NOI18N
            try {
                // sleep to workaround IDE's behavior. IDE consider as double click
                // two single clicks on the same position with delay shorter than 300 ms.
                // See org.openide.awt.MouseUtils.isDoubleClick().
                Thread.sleep(300);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
            tree().clickOnPath(getTreePath());
        }
        // sleep to workaround IDE's behavior. IDE consider as double click
        // two single clicks on the same position with delay shorter than 300 ms.
        // See org.openide.awt.MouseUtils.isDoubleClick().
        try {
            Thread.sleep(300);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }

    /**
     * adds node into set of selected nodes
     */
    public void addSelectionPath() {
        tree().addSelectionPath(getTreePath());
    }

    /**
     * tests if node is leaf
     *
     * @return boolean true when node does not have children
     */
    public boolean isLeaf() {
        return tree().getChildCount(getTreePath()) < 1;
    }

    /**
     * returns list of names of children
     *
     * @return String[] list of names of children
     */
    public String[] getChildren() {
        tree().expandPath(getTreePath());
        Object o[] = tree().getChildren(getTreePath().getLastPathComponent());
        if (o == null) {
            return new String[0];
        }
        String s[] = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            s[i] = o[i].toString();
        }
        return s;
    }

    /**
     * determines if current node is link
     *
     * @return boolean true if node is link
     */
    public boolean isLink() {
        return getText().endsWith(linkSuffix);
    }

    /**
     * verifies if node is still present. It expands parent path of the node
     * during verification.
     *
     * @return boolean true when node is still present
     */
    public boolean isPresent() {
        // do not use getTreePath() in this method
        tree().expandPath(treePath.getParentPath());
        int row = tree().getRowForPath(treePath);
        if (row < 0) {
            return false;
        } else {
            return treePath.equals(tree().getPathForRow(row));
        }
    }

    /**
     * verifies node's popup path for presence (without invocation)
     *
     * @param popupPath String popup path
     */
    public void verifyPopup(String popupPath) {
        verifyPopup(new String[]{popupPath});
    }

    /**
     * verifies node's popup paths for presence (without invocation)
     *
     * @param popupPaths String[] popup paths
     */
    public void verifyPopup(String[] popupPaths) {
        //invocation of root popup
        final JPopupMenuOperator popup = callPopup();
        for (int i = 0; i < popupPaths.length; i++) {
            try {
                popup.showMenuItem(popupPaths[i], "|");
            } catch (NullPointerException npe) {
                throw new JemmyException("Popup path [" + popupPaths[i] + "] not found.");
            }
        }
        //close popup and wait until is not visible
        popup.waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                popup.pushKey(KeyEvent.VK_ESCAPE);
                return !popup.isVisible();
            }

            @Override
            public String getDescription() {
                return "Popup menu closed";
            }
        });
    }

    static class StringArraySubPathChooser implements JTreeOperator.TreePathChooser {

        String[] arr;
        int[] indices;
        JTreeOperator.StringComparator comparator;
        TreePath parentPath;
        int parentPathCount;

        StringArraySubPathChooser(TreePath parentPath, String[] arr, int[] indices, JTreeOperator.StringComparator comparator) {
            this.arr = arr;
            this.comparator = comparator;
            this.indices = indices;
            this.parentPath = parentPath;
            this.parentPathCount = parentPath.getPathCount();
        }

        /**
         * implementation of JTreeOperator.TreePathChooser
         *
         * @param path TreePath
         * @param indexInParent int
         * @return boolean
         */
        @Override
        public boolean checkPath(TreePath path, int indexInParent) {
            return (path.getPathCount() == arr.length + parentPathCount
                    && hasAsParent(path, indexInParent));
        }

        /**
         * implementation of JTreeOperator.TreePathChooser
         *
         * @param path TreePath
         * @param indexInParent int
         * @return boolean
         */
        @Override
        public boolean hasAsParent(TreePath path, int indexInParent) {
            if (path.getPathCount() <= parentPathCount) {
                return path.isDescendant(parentPath);
            }
            if (arr.length + parentPathCount < path.getPathCount()) {
                return (false);
            }
            if (indices.length >= path.getPathCount() - parentPathCount
                    && indices[path.getPathCount() - parentPathCount - 1] != indexInParent) {
                return (false);
            }
            Object[] comps = path.getPath();
            for (int i = parentPathCount; i < comps.length; i++) {
                if (!comparator.equals(comps[i].toString(), arr[i - parentPathCount])) {
                    return (false);
                }
            }
            return (true);
        }

        /**
         * implementation of JTreeOperator.TreePathChooser
         *
         * @return String description
         */
        @Override
        public String getDescription() {
            StringBuilder bufDesc = new StringBuilder();
            Object parr[] = parentPath.getPath();
            for (int i = 0; i < parr.length; i++) {
                bufDesc.append(parr[i].toString()).append(", ");
            }
            for (int i = 0; i < arr.length; i++) {
                bufDesc.append(arr[i]).append(", ");
            }
            String desc = bufDesc.toString();
            if (desc.length() > 0) {
                desc = desc.substring(0, desc.length() - 2);
            }
            return ("[ " + desc + " ]");
        }
    }

    TreePath findSubPath(String subPath, String delimiter) {
        return findSubPath(subPath, "", delimiter);
    }

    TreePath findSubPath(String subPath, String indexes, String delimiter) {
        String indexStr[] = tree().parseString(indexes, delimiter);
        int indexInt[] = new int[indexStr.length];
        for (int i = 0; i < indexStr.length; i++) {
            indexInt[i] = Integer.parseInt(indexStr[i]);
        }
        TreePath foundTreePath;
        try {
            foundTreePath = tree().findPath(new Node.StringArraySubPathChooser(getTreePath(),
                    tree().parseString(subPath, delimiter),
                    indexInt,
                    getComparator()));
        } catch (JTreeOperator.NoSuchPathException e) {
            // try it once more. Probably IDE somehow changed nodes.
            foundTreePath = tree().findPath(new Node.StringArraySubPathChooser(getTreePath(),
                    tree().parseString(subPath, delimiter),
                    indexInt,
                    getComparator()));
        }
        return foundTreePath;
    }

    /**
     * Expands current node to see children
     */
    public void expand() {
        treeOperator.expandPath(getTreePath());
        waitExpanded();
    }

    /**
     * Collapse current node to hide children
     */
    public void collapse() {
        treeOperator.collapsePath(getTreePath());
        waitCollapsed();
    }

    /**
     * Waits for node to be expanded
     */
    public void waitExpanded() {
        treeOperator.waitExpanded(getTreePath());
    }

    /**
     * Waits for node to be collapsed
     */
    public void waitCollapsed() {
        treeOperator.waitCollapsed(getTreePath());
    }

    /**
     * Informs if current node is expanded
     *
     * @return boolean true when node is expanded
     */
    public boolean isExpanded() {
        return treeOperator.isExpanded(getTreePath());
    }

    /**
     * Informs if current node is collapsed
     *
     * @return boolean true when node is collapsed
     */
    public boolean isCollapsed() {
        return treeOperator.isCollapsed(getTreePath());
    }

    /*
     * protected Action[] getActions() { return null; }
     *
     * public boolean hasAction(Class actionClass) { Action actions[] =
     * getActions(); for (int i=0; actions!=null && i<actions.length; i++) if
     * (actionClass.equals(actions[i].getClass())) return true; return false;
    }
     */
    /**
     * verifies node's popup paths (of all actions) for presence (without
     * invocation)
     *
     * @param actions array of actions to be verified
     */
    public void verifyPopup(Action actions[]) {
        ArrayList<String> popupPaths = new ArrayList<String>();
        String path;
        for (int i = 0; i < actions.length; i++) {
            path = actions[i].getPopupPath();
            if (path != null) {
                popupPaths.add(path);
            }
        }
        verifyPopup(popupPaths.toArray(new String[0]));
    }

    /**
     * Checks whether child with specified name is present under this node.
     *
     * @param childName name of child node
     * @return true if child is present; false otherwise
     */
    public boolean isChildPresent(String childName) {
        String[] children = this.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (getComparator().equals(children[i], childName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Waits until child with specified name is not present under this node. It
     * can throw TimeoutExpiredException, if child is still present.
     *
     * @param childName name of child node
     */
    public void waitChildNotPresent(final String childName) {
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object anObject) {
                    return isChildPresent(childName) ? null : Boolean.TRUE;
                }

                @Override
                public String getDescription() {
                    return ("Child \"" + childName + "\" not present under parent \"" + getPath() + "\"");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    /**
     * Waits until this node is no longer present. It can throw
     * TimeoutExpiredException, if the node is still present.
     */
    public void waitNotPresent() {
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object anObject) {
                    return isPresent() ? null : Boolean.TRUE;
                }

                @Override
                public String getDescription() {
                    // do not use getPath() or getTreePath() here
                    return ("Wait node " + convertPath(treePath) + " not present.");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    /**
     * For IDE nodes we need to call openideNode.getChildren().getNodes(true);
     * which should satisfy that all children are correctly initialized. In fact
     * it skips 'Please, wait..." node and final tree should not be regenerated
     * anymore.
     */
    private static class NodesJTreeOperator extends JTreeOperator {

        /**
         * Creates new instance of NodesJTreeOperator from given JTreeOperator.
         *
         * @param origOperator original JTreeOperator
         */
        public NodesJTreeOperator(JTreeOperator origOperator) {
            super((JTree) origOperator.getSource());
            copyEnvironment(origOperator);
        }

        /**
         * Expands path and waits until all children are ready. This method is
         * used in JTreeOperator.findPathPrimitive, so we need it override here.
         *
         * @param treePath tree path to be expanded
         */
        @Override
        public void expandPath(final TreePath treePath) {
            super.expandPath(treePath);
            // call getNodes(true) but restrict it by timeout to prevent occasional infinite loop
            try {
                new Waiter(new Waitable() {
                    @Override
                    public Object actionProduced(Object anObject) {
                        try {
                            Visualizer.findNode(treePath.getLastPathComponent()).getChildren().getNodes(true);
                        } catch (ClassCastException e) {
                            // ignore for trees in IDE which are not compound from VisualizerNode instances
                        }
                        return Boolean.TRUE;
                    }

                    @Override
                    public String getDescription() {
                        return "org.openide.nodes.Node.getChildren().getNodes(true)";  //NOI18N
                    }
                }).waitAction(null);
            } catch (InterruptedException e) {
                throw new JemmyException("Interrupted.", e);
            } catch (TimeoutExpiredException tee) {
                // ignore and just prints nodes to jemmy log
                for (org.openide.nodes.Node n : Visualizer.findNode(treePath.getLastPathComponent()).getChildren().getNodes()) {
                    getOutput().printLine("    " + n.getDisplayName() + "  " + n);
                }
            }
        }
    }
}
