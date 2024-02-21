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
package org.netbeans.modules.versioning.util.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreePath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.util.NbBundle;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.awt.MouseUtils;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Treetable to show diff/status nodes.
 * 
 * @author Ondra Vrabec
 */
public abstract class FileTreeView<T extends VCSStatusNode> implements FileViewComponent<T>, AncestorListener, PropertyChangeListener, MouseListener {

    protected final OutlineView view;
    private final ExplorerManager em;
    private boolean displayed;
    private EditorCookie[] editorCookies;
    private final ViewContainer viewComponent;
    private T[] nodes;
    private final Map<T, TreeFilterNode> nodeMapping = Collections.synchronizedMap(new WeakHashMap<T, TreeFilterNode>());
    private boolean internalTraverse;
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; //NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; //NOI18N
    private static final String PATH_SEPARATOR_REGEXP = File.separator.replace("\\", "\\\\"); //NOI18N
    
    private static Image FOLDER_ICON;
    
    private static class ViewContainer extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager em;
        
        private ViewContainer (ExplorerManager em) {
            this.em = em;
            setLayout(new BorderLayout());
        }
        
        @Override
        public ExplorerManager getExplorerManager () {
            return em;
        }
        
    }
    
    @NbBundle.Messages({
        "CTL_FileTree.treeColumn.Name=File"
    })
    public FileTreeView () {
        em = new ExplorerManager();
        view = new OutlineView(Bundle.CTL_FileTree_treeColumn_Name());
        view.getOutline().setShowHorizontalLines(true);
        view.getOutline().setShowVerticalLines(false);
        view.getOutline().setRootVisible(false);
        view.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        view.setPopupAllowed(false);
        view.getOutline().addMouseListener(this);
        view.getOutline().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        view.getOutline().getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(view.getOutline()));
            }
        });
        view.getOutline().getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "slideOut");
        viewComponent = new ViewContainer(em);
        viewComponent.add(view, BorderLayout.CENTER);
        viewComponent.addAncestorListener(this);
        em.addPropertyChangeListener(this);
    }

    @Override
    public final void focus () {
        view.requestFocusInWindow();
    }

    @Override
    public int getPreferredHeaderHeight () {
        return view.getOutline().getTableHeader().getPreferredSize().height;
    }

    @Override
    public JComponent getComponent () {
        return viewComponent;
    }

    @Override
    public int getPreferredHeight () {
        return view.getOutline().getPreferredSize().height;
    }
    
    private Node getNodeAt( int rowIndex ) {
        Node result = null;
        TreePath path = view.getOutline().getOutlineModel().getLayout().getPathForRow(rowIndex);
        if (path != null) {
            result = Visualizer.findNode(path.getLastPathComponent());
        }
        return result;
    }
    
    @Override
    public void setModel (T[] nodes, EditorCookie[] editorCookies, Object modelData) {
        this.editorCookies = editorCookies;
        this.nodes = nodes;
        em.setRootContext((Node) modelData);
        for (T n : nodes) {
            view.expandNode(toTreeNode(n));
        }
    }
    
    protected abstract void setDefaultColumnSizes ();

    @Override
    public void ancestorAdded(AncestorEvent event) {
        if (!displayed) {
            displayed = true;
            setDefaultColumnSizes();
        }
    }

    @Override
    public void ancestorRemoved (AncestorEvent event) {
    }

    @Override
    public void ancestorMoved (AncestorEvent event) {
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) && !internalTraverse) {
            Node[] selectedNodes = em.getSelectedNodes();
            final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, view);
            if (tc != null) {
                tc.setActivatedNodes(selectedNodes);
            }
            if (selectedNodes.length == 1) {
                // single selection
                T node = convertNode(selectedNodes[0]);
                if (node != null) {
                    nodeSelected(node);
                    return;
                }
            }
            nodeSelected(null);
        }
    }
    
    private void showPopup (final MouseEvent e) {
        int row = view.getOutline().rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = view.getOutline().getSelectedRows();

            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                view.getOutline().getSelectionModel().setSelectionInterval(row, row);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // invoke later so the selection on the table will be set first
                JPopupMenu menu = getPopup();
                if (menu != null) {
                    menu.show(view.getOutline(), e.getX(), e.getY());
                }
            }
        });
    }

    private void showPopup(Point p) {
        JPopupMenu menu = getPopup();
        if (menu != null) {
            menu.show(view.getOutline(), p.x, p.y);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = view.getOutline().rowAtPoint(e.getPoint());
            if (row == -1) return;
            T n = convertNode(getNodeAt(view.getOutline().convertRowIndexToModel(row)));
            if (n != null) {
                Action action = n.getNodeAction();
                if (action != null && action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    e.consume();
                }
            }
        }
    }
    
    @Override
    public Object prepareModel (T[] nodes) {
        Map<File, Collection<T>> sortedNodes = new HashMap<File, Collection<T>>();
        for (T n : nodes) {
            File root = n.getFileNode().getRoot();
            if (root == null) {
                continue;
            }
            Collection<T> repositorySetups = sortedNodes.get(root);
            if (repositorySetups == null) {
                repositorySetups = new TreeSet<>(new PathComparator());
                sortedNodes.put(root, repositorySetups);
            }
            repositorySetups.add(n);
        }
        Node rootNode;
        Class<?> type = nodes.getClass().getComponentType();
        if (sortedNodes.size() == 1) {
            Map.Entry<File, Collection<T>> e = sortedNodes.entrySet().iterator().next();
            rootNode = new RepositoryRootNode(e.getKey(), toArray(e.getValue(), type));
            ((TreeViewChildren) rootNode.getChildren()).buildSubNodes(type);
        } else {
            rootNode = new RootNode(sortedNodes);
            ((TreeViewChildren) rootNode.getChildren()).buildSubNodes(type);
        }
        return rootNode;
    }

    protected T convertToAcceptedNode (Node node) {
        Class c = nodes.getClass().getComponentType();
        return c.isInstance(node) ? (T) node : null;
    }
    
    private T convertNode (Node node) {
        if (node instanceof TreeFilterNode) {
            return (T) ((TreeFilterNode) node).getOriginal();
        } else {
            return convertToAcceptedNode(node);
        }
    }

    protected abstract void nodeSelected (T node);
    
    protected abstract JPopupMenu getPopup ();
    
    @Override
    public T getSelectedNode () {
        T node = null;
        Node[] selectedNodes = em.getSelectedNodes();
        if (selectedNodes.length == 1) {
            node = convertNode(selectedNodes[0]);
        }
        return node;
    }

    /**
     * 
     * @return may contain also other than T nodes
     */
    public final List<Node> getSelectedNodes () {
        Node[] selectedNodes = em.getSelectedNodes();
        List<Node> nodeList = new ArrayList<Node>(selectedNodes.length);
        for (Node n : selectedNodes) {
            T converted = convertNode(n);
            if (converted == null) {
                nodeList.add(n);
            } else {
                nodeList.add(converted);
            }
        }
        return nodeList;
    }

    protected final Node createFilterNode (T original) {
        TreeFilterNode n = new TreeFilterNode(original);
        nodeMapping.put(original, n);
        return n;
    }

    @Override
    public void setSelectedNode (T toSelect) {
        try {
            em.setSelectedNodes(new Node[] { toTreeNode(toSelect) });
        } catch (PropertyVetoException ex) {
            Logger.getLogger(FileTreeView.class.getName()).log(Level.FINE, null, ex);
        }
    }

    @Override
    public T getNodeAtPosition (int position) {
        for (int i = 0; i < view.getOutline().getRowCount(); ++i) {
            Node n = getNodeAt(view.getOutline().convertRowIndexToModel(i));
            T converted = convertNode(n);
            if (converted != null) {
                if (position-- == 0) {
                    return converted;
                }
            }
        }
        return null;
    }

    @Override
    public T[] getNeighbouringNodes (T node, int boundary) {
        assert EventQueue.isDispatchThread();
        Set<T> neighbours = new LinkedHashSet<T>(5);
        neighbours.add(node);
        for (int i = 1; i < boundary; ++i) {
            T next = convertNode(findShiftNode(toTreeNode(node), i, false));
            if (next != null) {
                neighbours.add(next);
            }
            T prev = convertNode(findShiftNode(toTreeNode(node), -i, false));
            if (prev != null) {
                neighbours.add(prev);
            }
        }
        return neighbours.toArray((T[]) Array.newInstance(
                                    node.getClass(),
                                    neighbours.size()));
    }

    @Override
    public T getNextNode (T node) {
        Node nextNode = findShiftNode(toTreeNode(node), 1, true);
        return convertNode(nextNode);
    }

    @Override
    public T getPreviousNode (T node) {
        Node prevNode = findShiftNode(toTreeNode(node), -1, true);
        return convertNode(prevNode);
    }

    @Override
    public boolean hasNextNode (T node) {
        return convertNode(findShiftNode(toTreeNode(node), 1, false)) != null;
    }

    @Override
    public boolean hasPreviousNode (T node) {
        return convertNode(findShiftNode(toTreeNode(node), -1, false)) != null;
    }

    private Node toTreeNode (T n) {
        Node filterNode = nodeMapping.get(n);
        return filterNode == null ? n : filterNode;
    }
    
    private Node findShiftNode (Node startingNode, int direction, boolean canExpand) {
        boolean oldVal = internalTraverse;
        Node[] selected = em.getSelectedNodes();
        try {
            internalTraverse = true;
            return startingNode == null ? null : findDetailNode(startingNode, direction, view, canExpand);
        } finally {
            try {
                em.setSelectedNodes(selected);
            } catch (PropertyVetoException ex) { }
            internalTraverse = oldVal;
        }
    }

    private Node findDetailNode(Node fromNode, int direction,
            OutlineView outlineView, boolean canExpand) {
        return findUp(fromNode, direction,
                convertNode(fromNode) != null || direction < 0 ? direction : 0,
                outlineView, canExpand);
    }
    
    /**
     * Start finding for next or previous occurance, from a node or its previous
     * or next sibling of node {@code node}
     *
     * @param node reference node
     * @param offset 0 to start from node {@code node}, 1 to start from its next
     * sibling, -1 to start from its previous sibling.
     * @param dir Direction: 1 for next, -1 for previous.
     */
    private Node findUp(Node node, int dir, int offset, OutlineView outlineView,
            boolean canExpand) {
        if (node == null) {
            return null;
        }
        Node parent = node.getParentNode();
        Node[] siblings;
        if (parent == null) {
            siblings = new Node[]{node};
        } else {
            siblings = getChildren(parent, outlineView, canExpand);
        }
        int nodeIndex = findChildIndex(node, siblings);
        if (nodeIndex + offset < 0 || nodeIndex + offset >= siblings.length) {
            return findUp(parent, dir, dir, outlineView, canExpand);
        }
        for (int i = nodeIndex + offset;
                i >= 0 && i < siblings.length; i += dir) {
            Node found = findDown(siblings[i], siblings, i, dir, outlineView,
                    canExpand);
            return found;
        }
        return findUp(parent, dir, offset, outlineView, canExpand);
    }

    /**
     * Find Depth-first search to find a detail node in the subtree.
     */
    private Node findDown(Node node, Node[] siblings, int nodeIndex,
            int dir, OutlineView outlineView, boolean canExpand) {

        Node[] children = getChildren(node, outlineView, canExpand);
        for (int i = dir > 0 ? 0 : children.length - 1;
                i >= 0 && i < children.length; i += dir) {
            Node found = findDown(children[i], children, i, dir, outlineView,
                    canExpand);
            if (found != null) {
                return found;
            }
        }
        for (int i = nodeIndex; i >= 0 && i < siblings.length; i += dir) {
            Node converted = convertNode(siblings[i]);
            if (converted != null) {
                return converted;
            }
        }
        return null;
    }

    private static int findChildIndex(Node selectedNode, Node[] siblings) {
        int pos = -1;
        for (int i = 0; i < siblings.length; i++) {
            if (siblings[i] == selectedNode) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private static Node[] getChildren(Node n, OutlineView outlineView,
            boolean canExpand) {
        if (outlineView != null) {
            if (!outlineView.isExpanded(n)) {
                if (canExpand) {
                    outlineView.expandNode(n);
                } else {
                    return n.getChildren().getNodes(true);
                }
            }
            return getChildrenInDisplayedOrder(n, outlineView);
        } else {
            return n.getChildren().getNodes(true);
        }
    }

    private static Node[] getChildrenInDisplayedOrder(Node parent,
            OutlineView outlineView) {

        Outline outline = outlineView.getOutline();
        Node[] unsortedChildren = parent.getChildren().getNodes(true);
        int rows = outlineView.getOutline().getRowCount();
        int start = findRowIndexInOutline(parent, outline, rows);
        if (start == -1 && parent != ExplorerManager.find(outlineView).getRootContext()) {
            return unsortedChildren;
        }
        List<Node> children = new LinkedList<Node>();
        for (int j = start + 1; j < rows; j++) {
            int childModelIndex = outline.convertRowIndexToModel(j);
            if (childModelIndex == -1) {
                continue;
            }
            Object childObject = outline.getModel().getValueAt(
                    childModelIndex, 0);
            Node childNode = Visualizer.findNode(childObject);
            if (childNode.getParentNode() == parent) {
                children.add(childNode);
            } else if (children.size() == unsortedChildren.length) {
                break;
            }
        }
        return children.toArray(new Node[0]);
    }

    private static int findRowIndexInOutline(Node node, Outline outline,
            int rows) {

        int startRow = Math.max(outline.getSelectedRow(), 0);
        int offset = 0;
        while (startRow + offset < rows || startRow - offset >= 0) {
            int up = startRow + offset + 1;
            int down = startRow - offset;

            if (up < rows && testNodeInRow(outline, node, up)) {
                return up;
            } else if (down >= 0 && testNodeInRow(outline, node, down)) {
                return down;
            } else {
                offset++;
            }
        }
        return -1;
    }

    private static boolean testNodeInRow(Outline outline, Node node, int i) {
        int modelIndex = outline.convertRowIndexToModel(i);
        if (modelIndex != -1) {
            Object o = outline.getModel().getValueAt(modelIndex, 0);
            Node n = Visualizer.findNode(o);
            if (n == node) {
                return true;
            }
        }
        return false;
    }

    private static Image getFolderIcon () {
        if (FOLDER_ICON == null) {
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER);
            Image base;
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB);
                if (base == null) { // fallback to our owns
                    base = ImageUtilities.loadImage("org/openide/loaders/defaultFolder.gif"); //NOI18N
                }
            }
            FOLDER_ICON = base;
        }
        return FOLDER_ICON;
    }

    protected abstract class AbstractRenderDataProvider implements RenderDataProvider {
        
        @Override
        public String getDisplayName (Object o) {
            Node n = Visualizer.findNode(o);
            String value = n.getDisplayName();
            T leafNode = convertNode(n);
            if (leafNode != null) {
                // do not set selected flag, outline view handles color in its own way
                // instead return fg color in getForeground
                String htmlDisplayName = DiffUtils.getHtmlDisplayName(leafNode, isModified(leafNode), false);
                htmlDisplayName = annotateName(leafNode, htmlDisplayName);
                if (htmlDisplayName != null) {
                    value = "<html>" + htmlDisplayName; //NOI18N
                }
            }
            return value;
        }

        @Override
        public boolean isHtmlDisplayName (Object o) {
            return true;
        }

        @Override
        public Color getBackground (Object o) {
            return null;
        }

        @Override
        public Color getForeground (Object o) {
            Color c = null;
            Node n = Visualizer.findNode(o);
            T leafNode = convertNode(n);
            if (leafNode != null) {
                c = leafNode.getAnnotatedFontColor();
            }
            return c;
        }

        @Override
        public String getTooltipText (Object o) {
            Node n = Visualizer.findNode(o);
            File file = n.getLookup().lookup(File.class); 
            return file != null ? file.getAbsolutePath() : n.getShortDescription();
        }

        @Override
        public Icon getIcon (Object o) {
            Node n = Visualizer.findNode(o);
            return new ImageIcon(n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        }

        private boolean isModified (T node) {
            int index = Arrays.asList(nodes).indexOf(node);
            EditorCookie editorCookie = index >= 0 && index < editorCookies.length ? editorCookies[index] : null;
            return (editorCookie != null) ? editorCookie.isModified() : false;
        }

        protected abstract String annotateName (T leafNode, String htmlDisplayName);
    }
    
    private static <T> T[] toArray (Collection<T> list, Class<?> type) {
        return list.toArray((T[]) java.lang.reflect.Array.newInstance(type, list.size()));
    }
    
    private static class TreeFilterNode<T extends Node> extends FilterNode {

        public TreeFilterNode (T original) {
            super(original);
        }
        
        @Override
        public T getOriginal () {
            return (T) super.getOriginal();
        }
    }
    
    private class RootNode extends AbstractNode {

        private RootNode (Map<File, Collection<T>> nodes) {
            super(new RootNodeChildren(nodes));
        }
    }
    
    private abstract static class TreeViewChildren extends Children.Array {
        abstract void buildSubNodes (Class<?> type);
    }

    private class RootNodeChildren extends TreeViewChildren {
        private final java.util.Map<File, Collection<T>> nestedNodes;
        
        public RootNodeChildren (java.util.Map<File, Collection<T>> setups) {
            this.nestedNodes = setups;
        }

        @Override
        void buildSubNodes (Class<?> type) {
            add(createNodes(type));
        }
        
        private Node[] createNodes (Class<?> type) {
            List<Node> nodes = new ArrayList<>(nestedNodes.size());
            for (java.util.Map.Entry<File, Collection<T>> e : nestedNodes.entrySet()) {
                RepositoryRootNode root = new RepositoryRootNode(e.getKey(), toArray(e.getValue(), type));
                ((TreeViewChildren) root.getChildren()).buildSubNodes(type);
                nodes.add(root);
            }
            return nodes.toArray(new Node[0]);
        }
    }
    
    private class RepositoryRootNode extends AbstractNode {
        private final File repo;
        private RepositoryRootNode (File repository, T[] nestedNodes) {
            super(new NodeChildren(new NodeData(new File(repository, getCommonPrefix(nestedNodes)), getCommonPrefix(nestedNodes), nestedNodes), true), Lookups.fixed(repository));
            this.repo = repository;
        }
        
        @Override
        public String getName () {
            return repo.getName();
        }

        @Override
        public Image getIcon (int type) {
            return getFolderIcon();
        }
    }

    private String getCommonPrefix (T[] nodes) {
        String prefix = "";
        if (nodes.length > 0) {
            prefix = nodes[0].getFileNode().getRelativePath();
            int index = prefix.lastIndexOf(File.separator);
            if (index == -1) {
                prefix = "";
            } else {
                prefix = prefix.substring(0, index);
            }
        }
        boolean slashNeeded = !prefix.isEmpty();
        for (T n : nodes) {
            String location = n.getFileNode().getRelativePath();
            while (!location.startsWith(prefix)) {
                slashNeeded = false;
                int index = prefix.lastIndexOf(File.separator);
                if (index == -1) {
                    prefix = "";
                } else {
                    prefix = prefix.substring(0, index);
                }
            }
        }
        return slashNeeded ? prefix + File.separator : prefix;
    }
    
    private class NodeChildren extends TreeViewChildren {
        private final T[] nestedNodes;
        private final String path;
        private final boolean top;
        private final File file;
    
        public NodeChildren (NodeData<T> data, boolean top) {
            this.nestedNodes = data.nestedNodes;
            this.path = data.path;
            this.file = data.file;
            this.top = top;
        }
    
        @Override
        void buildSubNodes (Class<?> type) {
            List<NodeData<T>> data = new ArrayList<>(nestedNodes.length);
            String prefix = null;
            List<T> subNodes = new ArrayList<>();
            for (T n : nestedNodes) {
                String location = n.getFileNode().getRelativePath();
                if (prefix == null) {
                    prefix = path + location.substring(path.length()).split(PATH_SEPARATOR_REGEXP, 0)[0];
                }
                if (location.equals(prefix)) {
                    if (!subNodes.isEmpty()) {
                        data.add(new NodeData(getFile(prefix), prefix, toArray(subNodes, type)));
                        subNodes.clear();
                    }
                    data.add(new NodeData(getFile(prefix), prefix, toArray(Arrays.asList(n), type)));
                    prefix = null;
                } else if (location.startsWith(prefix)) {
                    subNodes.add(n);
                } else {
                    data.add(new NodeData(getFile(prefix), prefix, toArray(subNodes, type)));
                    subNodes.clear();
                    prefix = path + location.substring(path.length()).split(PATH_SEPARATOR_REGEXP, 0)[0];
                    subNodes.add(n);
                }
            }
            if (!subNodes.isEmpty()) {
                data.add(new NodeData(getFile(prefix), prefix, toArray(subNodes, type)));
            }
            
            add(createNodes(data, type));
        }
    
        private Node[] createNodes (List<NodeData<T>> keys, Class<?> type) {
            List<Node> toCreate = new ArrayList<>(keys.size());
            for (NodeData<T> key : keys) {
                final Node node;
                if (key.nestedNodes.length == 0) {
                    continue;
                } else if (key.nestedNodes.length == 1 && key.path.equals(key.nestedNodes[0].getFileNode().getRelativePath())) {
                    node = FileTreeView.this.createFilterNode(key.nestedNodes[0]);
                } else {
                    final String name;
                    if (top) {
                        name = key.path;
                    } else {
                        String[] segments = key.path.split(PATH_SEPARATOR_REGEXP);
                        name = segments[segments.length - 1];
                    }
                    final Image icon = getFolderIcon(key.file);
                    NodeChildren ch = new NodeChildren(new NodeData<T>(key.file, key.path + File.separator, key.nestedNodes), false);
                    node = new AbstractNode(ch, Lookups.fixed(key.file)) {
                        @Override
                        public String getName () {
                            return name;
                        }

                        @Override
                        public Image getIcon (int type) {
                            return icon;
                        }
                    };
                    ch.buildSubNodes(type);
                }
                toCreate.add(node);
            }
            return toCreate.toArray(new Node[0]);
        }

        private Image getFolderIcon (File file) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            Icon icon = null;
            if (fo != null) {
                try {
                    ProjectManager.Result res = ProjectManager.getDefault().isProject2(fo);
                    if (res != null) {
                        icon = res.getIcon();
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(FileTreeView.class.getName()).log(Level.INFO, null, ex);
                }
            }
            return icon == null ? FileTreeView.getFolderIcon() : ImageUtilities.icon2Image(icon);
        }

        private File getFile (String prefix) {
            String p = prefix;
            if (prefix.startsWith(path)) {
                p = prefix.substring(path.length());
            }
            return new File(file, p);
        }
    }
    
    private static class NodeData<T extends VCSStatusNode> {
        private final File file;
        private final String path;
        private final T[] nestedNodes;

        public NodeData (File file, String path, T[] nested) {
            this.file = file;
            this.path = path;
            this.nestedNodes = nested;
        }
    }

    private static class PathComparator<T extends VCSStatusNode> implements Comparator<T> {

        @Override
        public int compare (T o1, T o2) {
            String[] segments1 = o1.getFileNode().getRelativePath().split(PATH_SEPARATOR_REGEXP);
            String[] segments2 = o2.getFileNode().getRelativePath().split(PATH_SEPARATOR_REGEXP);
            for (int i = 0; i < Math.min(segments1.length, segments2.length); ++i) {
                String segment1 = segments1[i];
                String segment2 = segments2[i];
                int comp = segment1.compareTo(segment2);
                if (comp != 0) {
                    if (segment1.startsWith(segment2)) {
                        // xml.xdm must precede xml node
                        return segment2.length() - segment1.length();
                    } else if (segment2.startsWith(segment1)) {
                        // xml must follow xml.xdm node
                        return segment2.length() - segment1.length();
                    }
                    return comp;
                }
            }
            return segments2.length - segments1.length;
        }

    }
}
