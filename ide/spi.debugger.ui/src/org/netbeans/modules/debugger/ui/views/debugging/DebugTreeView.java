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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.FixedHeightLayoutCache;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;

import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThreadGroup;
import org.netbeans.spi.viewmodel.TreeExpansionModel;

import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Dan
 */
public class DebugTreeView extends BeanTreeView {

    private static final Logger logger = Logger.getLogger(DebugTreeView.class.getName());

    private int thickness = 0;
    private final Color highlightColor;
    private final Color currentThreadColor;
    
    private DVThread focusedThread;
    
    private DebuggingView.DVSupport currentDVSupport;
    private boolean currentDVSupportSet;
    private final Object currentDVSupportLock = new Object();
    private DebuggerManagerListener dmListener;
    
    DebugTreeView() {
        super();
        Color c = UIManager.getColor("nb.debugger.debugging.currentThread");
        if (c == null) {
            c = new Color(233, 255, 230);
            Color tbc = tree.getBackground();
            int dl = Math.abs(DebuggingViewComponent.luminance(c) - DebuggingViewComponent.luminance(tbc));
            if (dl > 125) {
                c = new Color(30, 80, 28);
            }
        }
        currentThreadColor = c;
        c = UIManager.getColor("nb.debugger.debugging.highlightColor");
        if (c == null) {
            c = new Color(233, 239, 248);
            Color tbc = tree.getBackground();
            int dl = Math.abs(DebuggingViewComponent.luminance(c) - DebuggingViewComponent.luminance(tbc));
            if (dl > 125) {
                c = new Color(40, 60, 38);
            }
        }
        highlightColor = c;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Debugging view panel background color = "+javax.swing.UIManager.getDefaults().getColor("Panel.background"));
            logger.fine("Debugging view tree text background color = "+javax.swing.UIManager.getDefaults().getColor("Tree.textBackground"));
            logger.fine("Tree color = "+tree.getBackground()+", tree is opaque = "+tree.isOpaque());
            logger.fine("Tree parent color = "+((JComponent)tree.getParent()).getBackground()+", tree parent is opaque = "+((JComponent)tree.getParent()).isOpaque());
            logger.fine("Tree parent = "+tree.getParent());
            logger.fine("Current Thread Color = "+currentThreadColor);
            logger.fine("Thread Highlight Color = "+highlightColor);
        }

        NodeRenderer rend = new DebugTreeNodeRenderer();
        tree.setCellRenderer(rend);

        setBackground(tree.getBackground());
        tree.setOpaque(false);
        ((JComponent)tree.getParent()).setOpaque(false);
        ((JComponent)tree.getParent()).setBackground(tree.getBackground());
        setWheelScrollingEnabled(false);
    }

    @Override
    protected JViewport createViewport() {
        // We're a ScrollPane, but inside of our own ScrollPane
        // Therefore requests to scroll to a visible rectangle need to be delegated to the outer viewport
        return new DelegateViewport();
    }
    
    public JTree getTree() {
        return tree;
    }

    void resetSelection() {
        tree.getSelectionModel().clearSelection(); // To flush selection cache
        clearSelectionCache(tree.getSelectionModel().getRowMapper());
        clearDrawingCache(tree);
        tree.repaint(); // To flush SynthTreeUI.drawingCache
    }

    // HACK to clear Swing caches
    private static void clearSelectionCache(RowMapper rm) {
        if (rm instanceof FixedHeightLayoutCache) {
            try {
                Field infoField = rm.getClass().getDeclaredField("info");
                infoField.setAccessible(true);
                Object searchInfo = infoField.get(rm);
                if (searchInfo != null) {
                    Field nodeField = searchInfo.getClass().getDeclaredField("node");
                    nodeField.setAccessible(true);
                    nodeField.set(searchInfo, null);
                }
            } catch (Exception ex) {}
        }
    }

    // HACK http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6258067
    private static void clearDrawingCache(JTree tree) {
        TreeUI tui = tree.getUI();
        if (tui instanceof BasicTreeUI) {
            try {
                Field drawingCacheField = BasicTreeUI.class.getDeclaredField("drawingCache");
                drawingCacheField.setAccessible(true);
                Map<BasicTreeUI, Map> table = (Map) drawingCacheField.get(tui);
                table.clear();
            } catch (Exception ex) {}
        }
     }

    public List<TreePath> getVisiblePaths() {
        synchronized(tree) {
            List<TreePath> result = new ArrayList<TreePath>();
            int count = tree.getRowCount();
            for (int x = 0; x < count; x++) {
                TreePath path = tree.getPathForRow(x);
                if (tree.isVisible(path)) {
                    result.add(path);
                }
            }
            return result;
        }
    }

    /**
     * Get the DVThread or DVThreadGroup instance on the given path.
     * @param path
     * @return an instance of DVThread or DVThreadGroup
     */
    public Object getThreadObject(TreePath path) {
        Node node = Visualizer.findNode(path.getLastPathComponent());
        DVThread dvThread = node.getLookup().lookup(DVThread.class);
        if (dvThread != null) {
            return dvThread;
        }
        DVThreadGroup dvThreadGroup = node.getLookup().lookup(DVThreadGroup.class);
        return dvThreadGroup;
    }

    public int getUnitHeight() {
        return thickness;
    }

    public void addTreeExpansionListener(TreeExpansionListener listener) {
        tree.addTreeExpansionListener(listener);
    }
    
    public void removeTreeExpansionListener(TreeExpansionListener listener) {
        tree.removeTreeExpansionListener(listener);
    }

    void setExpansionModel(TreeExpansionModel model) {
        // [TODO] ???
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintStripes(g, this);
    }

    // [TODO] optimize paintStripes() method
    void paintStripes(Graphics g, JComponent comp) {
        List<TreePath> paths = getVisiblePaths(); // [TODO] do not call getVisiblePaths()
        int linesNumber = paths.size();
        Rectangle rect = paths.size() > 0 ? tree.getRowBounds(tree.getRowForPath(paths.get(0))) : null;
        if (rect != null) {
            thickness = (int)Math.round(rect.getHeight());
        }
        int rowHeight;
        if (thickness > 0) { // [TODO] compute height for each particular row
            rowHeight = thickness;
        } else if (tree.getRowHeight() > 0) {
            rowHeight = tree.getRowHeight() + 2;
        } else {
            rowHeight = 18;
        }
        int zebraHeight = linesNumber * rowHeight;
        
        int width = comp.getWidth();
        int height = comp.getHeight();
        
        if ((width <= 0) || (height <= 0)) {
            return;
        }

        Rectangle clipRect = g.getClipBounds();
        int clipX;
        int clipY;
        int clipW;
        int clipH;
        if (clipRect == null) {
            clipX = clipY = 0;
            clipW = width;
            clipH = height;
        }
        else {
            clipX = clipRect.x;
            clipY = clipRect.y;
            clipW = clipRect.width;
            clipH = clipRect.height;
        }

        if(clipW > width) {
            clipW = width;
        }
        if(clipH > height) {
            clipH = height;
        }

        Color origColor = g.getColor();
        DVThread currentThread = getCurrentThread();
        boolean isHighlighted = false;
        boolean isCurrent = false;
        Iterator<TreePath> iter = paths.iterator();
        int firstGroupNumber = clipY / rowHeight;
        for (int x = 0; x <= firstGroupNumber && iter.hasNext(); x++) {
            Node node = Visualizer.findNode(iter.next().getLastPathComponent());
            DVThread thread = node.getLookup().lookup(DVThread.class);
            isHighlighted = focusedThread != null && thread == focusedThread;
            if (thread != null) {
                isCurrent = currentThread == thread;
            }
        }
        
        int sy = (clipY / rowHeight) * rowHeight;
        int limit = Math.min(clipY + clipH - 1, zebraHeight);
        while (sy < limit) {
            int y1 = Math.max(sy, clipY);
            int y2 = Math.min(clipY + clipH, y1 + rowHeight);
            if (isHighlighted || isCurrent) {
                //g.setColor(isHighlighted ? highlightColor : (isCurrent ? currentThreadColor : whiteColor));
                g.setColor(isHighlighted ? highlightColor : currentThreadColor);
                g.fillRect(clipX, y1, clipW, y2 - y1);
            }
            sy += rowHeight;
            if (iter.hasNext()) {
                Node node = Visualizer.findNode(iter.next().getLastPathComponent());
                DVThread thread = node.getLookup().lookup(DVThread.class);
                isHighlighted = focusedThread != null && thread == focusedThread;
                if (thread != null) {
                    isCurrent = currentThread == thread;
                }
            } else {
                isHighlighted = false;
                isCurrent = false;
            }
        }
//        if (sy < clipY + clipH - 1) {
//            g.setColor(whiteColor);
//            g.fillRect(clipX, sy, clipW, clipH + clipY - sy);
//        }
        g.setColor(origColor);
    }
    
    private DebuggingView.DVSupport getCurrentDVsupport() {
        synchronized (currentDVSupportLock) {
            if (dmListener == null) {
                dmListener = new CurrentEngineListener();
                DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_ENGINE, dmListener);
            }
            if (currentDVSupportSet) {
                return currentDVSupport;
            }
        }
        DebuggingView.DVSupport dvSupport;
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine != null) {
            dvSupport = currentEngine.lookupFirst(null, DebuggingView.DVSupport.class);
        } else {
            dvSupport = null;
        }
        synchronized (currentDVSupportLock) {
            currentDVSupport = dvSupport;
            currentDVSupportSet = true;
        }
        return dvSupport;
    }
    
    private DVThread getCurrentThread() {
        DVThread currentThread = null;
        DebuggingView.DVSupport dvSupport = getCurrentDVsupport();
        if (dvSupport != null) {
            currentThread = dvSupport.getCurrentThread();
        }
        return currentThread;
    }
    
    boolean threadFocuseGained(DVThread dvThread) {
        if (dvThread != null && focusedThread != dvThread) {
            focusedThread = dvThread;
            repaint();
            return true;
        }
        return false;
    }

    boolean threadFocuseLost(DVThread dvThread) {
        if (dvThread != null && focusedThread == dvThread) {
            focusedThread = null;
            repaint();
            return true;
        }
        return false;
    }

    private class DebugTreeNodeRenderer extends NodeRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Color background = null;
            if (value instanceof TreeNode) {
                try {
                    java.lang.reflect.Field fnode = value.getClass().getDeclaredField("node");
                    fnode.setAccessible(true);
                    value = fnode.get(value);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (value instanceof Node) {
                Node node = (Node) value;
                DVThread thread;
                do {
                    thread = node.getLookup().lookup(DVThread.class);
                    if (thread == null) {
                        node = node.getParentNode();
                    }
                } while (thread == null && node != null);
                if (thread != null) {
                    DVThread currentThread = getCurrentThread();
                    boolean isHighlighted = focusedThread != null && thread == focusedThread && node == value;
                    boolean isCurrent = currentThread == thread;
                    if (isHighlighted || isCurrent) {
                        background = isHighlighted ? highlightColor : currentThreadColor;
                    }
                }
            }
            Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (background != null) {
                component.setBackground(background);
                if (component instanceof JComponent) {
                    ((JComponent) component).setOpaque(true);
                }
            }
            return component;
        }
        
    }
    
    private class CurrentEngineListener extends DebuggerManagerAdapter {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (DebuggerManager.PROP_CURRENT_ENGINE.equals(evt.getPropertyName())) {
                synchronized (currentDVSupportLock) {
                    currentDVSupport = null;
                    currentDVSupportSet = false;
                    if (evt.getNewValue() == null) {
                        // No debug engine
                        if (dmListener != null) {
                            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_CURRENT_ENGINE, dmListener);
                            dmListener = null;
                        }
                    }
                }
            }
        }
        
    }

}
