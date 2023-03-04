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
import java.awt.Rectangle;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.SupportiveDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.util.EmptyVisualizer;
import org.openide.explorer.view.Visualizer;

/**
 * Handle IDE's org.openide.explorer.view.TreeTable component
 * which is used instead of JTree in Options dialog, ...
 */
public class TreeTableOperator extends JTableOperator {
    
    private JTreeOperator _tree;
    
    /** Creates new instance.
     * @param view JTable representing requested TreeTable
     */    
    public TreeTableOperator(JTable view) {
        super(view);
    }
    
    /** Creates new instance for the first TreeTable in container.
     * @param contOper container where to find TreeTable
     */
    public TreeTableOperator(ContainerOperator contOper) {
        this(contOper, 0);
    }
    /** Creates new instance for the first TreeTable in container.
     * @param contOper container where to find TreeTable
     * @param index int index
     */
    public TreeTableOperator(ContainerOperator contOper, int index) {
	this((JTable)
	     waitComponent(contOper, 
			   new TreeTableFinder(ComponentSearcher.
                                               getTrueChooser("Any TreeTable")),
			   index));
	copyEnvironment(contOper);
    }

    /** Returns operator for a tree which is showed as a part of
     * the table.
     * @return JTreeOperator instance
     */
    public JTreeOperator tree() {
        if(_tree == null) {
            // Need to wait until TreeTable is populated. Otherwise it can throw
            // NPE from getValueAt(0, 0).
            waitState(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return getColumnCount() > 0 && getRowCount() > 0;
                }
                public String getDescription() {
                    return "TreeTable contains any rows.";
                }
            });
            // cell renderer component for first column is JTree
            Object value = getValueAt(0, 0);
            JTree jTree = (JTree)getCellRenderer(0, 0).getTableCellRendererComponent((JTable)this.getSource(), value, false, false, 0, 0);
            // Need to set EmptyVisualizer because found JTree doesn't have any parent Container
            // and calling makeComponentVisible() throws NPE
//            _tree = new JTreeOperator(jTree);
            _tree = new RenderedTreeOperator(this, jTree);
            _tree.setVisualizer(new EmptyVisualizer());
        }
        // Everytime make parent container visible because tree has EmptyVisualizer
        // and it is need for example for popup menu operations on JTree
        makeComponentVisible();
        return _tree;
    }
    
    /** Selects node in this TreeTable.
     * @param path path to node (e.g. "System|Settings")
     * @return row number of selected node
     */
    public int selectNode(String path) {
        TreePath treePath = tree().findPath(path, "|");
        if(!tree().isPathSelected(treePath)) {
            tree().selectPath(treePath);
        }
        int result = tree().getRowForPath(treePath);
        scrollToCell(result, 0);
        new EventTool().waitNoEvent(500);
        return result;
    }

    /** Registers RenderedMouseDriver to be used by RenderedTreeOperator. */
    static {
        DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, new RenderedMouseDriver(), RenderedTreeOperator.class);
    }

    /** Finder to search for "org.openide.explorer.view.TreeTable".  */
    static class TreeTableFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public TreeTableFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
            Class cls = comp.getClass();
            do {
                if(cls.getName().equals("org.openide.explorer.view.TreeTable")) {
                    return(subFinder.checkComponent(comp));
                }
            } while((cls = cls.getSuperclass()) != null);
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
    
    /** MouseDriver used to process events not on JTree but on TreeTable 
     * component which is used to handle events in real.
     */
    public static class RenderedMouseDriver extends SupportiveDriver implements MouseDriver {
        
        /** Creates new instance of RenderedMouseDriver. */ 
        public RenderedMouseDriver() {
            super(new Class[] {RenderedTreeOperator.class});
        }
        
        /** Presses mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param x x position of mouse operation
         * @param y y position of mouse operation
         * @param mouseButton mouse button identification
         * @param modifiers modifiers pressed during mouse click
         */
        public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).pressMouse(realOper, x, y, mouseButton, modifiers);
        }
        
        /** Releases mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param x x position of mouse operation
         * @param y y position of mouse operation
         * @param mouseButton mouse button identification
         * @param modifiers modifiers pressed during mouse click
         */
        public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).releaseMouse(realOper, x, y, mouseButton, modifiers);
        }

        /** Clicks mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param x x position of mouse operation
         * @param y y position of mouse operation
         * @param clickCount how many times to be clicked
         * @param mouseButton mouse button identification
         * @param modifiers modifiers pressed during mouse click
         * @param mouseClick timeout of mouse click
         */
        public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton, 
                		   int modifiers, Timeout mouseClick) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).clickMouse(realOper, x, y, clickCount, mouseButton, modifiers, mouseClick);
        }

        /** Moves mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param x x position of mouse operation
         * @param y y position of mouse operation
         */
        public void moveMouse(ComponentOperator oper, int x, int y) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).moveMouse(realOper, x, y);
        }
        
        /** Drags mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param x x position of mouse operation
         * @param y y position of mouse operation
         * @param mouseButton mouse button identification
         * @param modifiers modifiers pressed during mouse click
         */
        public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).dragMouse(realOper, x, y, mouseButton, modifiers);
        }
        
        /** Provides drag and drop operation on operator given by 
         * {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         * @param start_x x position where to drag
         * @param start_y y position where to drag
         * @param end_x x position where to drop
         * @param end_y y position where to drop
         * @param mouseButton mouse button identification
         * @param modifiers modifiers pressed during mouse click
         * @param before timeout before drag
         * @param after timeout after drop
         */
        public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y, 
                		  int mouseButton, int modifiers, Timeout before, Timeout after) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).dragNDrop(realOper, start_x, start_y, end_x, end_y, 
                mouseButton, modifiers, before, after);
        }
        
        /** Enters mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         */
        public void enterMouse(ComponentOperator oper){
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).enterMouse(realOper);
        }
        
        /** Exits mouse on operator given by {@link TreeTableOperator.RenderedTreeOperator#getRealOperator getRealOperator()} method.
         * @param oper operator which delegates events on an real operator
         */
        public void exitMouse(ComponentOperator oper) {
            checkSupported(oper);
            ComponentOperator realOper = ((RenderedTreeOperator)oper).getRealOperator();
            DriverManager.getMouseDriver(realOper).exitMouse(realOper);
        }
    }
    
    /** Operator used to process events not on JTree but on TreeTable
     * component which is used to handle events in real.
     */
    public static class RenderedTreeOperator extends JTreeOperator {
        TreeTableOperator oper;
        
        /** Creates new instance of RenderedTreeOperator.
         * @param oper TreeTable operator to handle events
         * @param tree JTree representing nodes
         */        
        public RenderedTreeOperator(TreeTableOperator oper, JTree tree) {
            super(tree);
            this.oper = oper;
        }
        
        /** Returns TreeTableOperator instance which is used to handle events.
         * @return TreeTableOperator instance
         */        
        public ComponentOperator getRealOperator() {
            return(oper);
        }
        
        /** Calls popup menu on specified tree paths.
         * @param paths an array of TreePath instances
         * @param mouseButton mouse button identification
         * @return JPopupMenu instance opened by this method
         */        
        public JPopupMenu callPopupOnPaths(TreePath[] paths, int mouseButton) {
            oper.makeComponentVisible();
            for(int i = 0; i < paths.length; i++) {
                if(paths[i].getParentPath() != null) {
                    expandPath(paths[i].getParentPath());
                }
            }
            selectPaths(paths);
            scrollToPath(paths[paths.length - 1]);
            Point point = getPointToClick(paths[paths.length - 1]);
            return(JPopupMenuOperator.callPopup(oper.getSource(), 
                                                (int)point.getX(), 
                                                (int)point.getY(), 
                                                mouseButton));
        }
        
        /**
         * Scrolls to a path if the tree is on a JScrollPane component.
         * @param path TreePath where to scroll
         */
        public void scrollToPath(TreePath path) {
            makeComponentVisible();
            //try to find JScrollPane under.
            JScrollPane scroll = (JScrollPane)getContainer(new JScrollPaneOperator.
            					       JScrollPaneFinder(ComponentSearcher.
            								 getTrueChooser("JScrollPane")));
            if(scroll == null) {
                return;
            }
            JScrollPaneOperator scroller = new JScrollPaneOperator(scroll);
            scroller.copyEnvironment(this);
            scroller.setVisualizer(new EmptyVisualizer());
            Rectangle rect = getPathBounds(path);
            if(rect != null) {
                scroller.scrollToComponentRectangle(getRealOperator().getSource(), 
                                                    (int)rect.getX(),
                                                    (int)rect.getY(),
                                                    (int)rect.getWidth(),
                                                    (int)rect.getHeight());
            } else {
                throw(new NoSuchPathException(path));
            }
        }
        
        /** Expands path and waits until all children are ready. This method
         * is used in JTreeOperator.findPathPrimitive, so we need it override here.
         * @param treePath tree path to be expanded
         */
        public void expandPath(final TreePath treePath) {
            super.expandPath(treePath);
            Visualizer.findNode(treePath.getLastPathComponent()).getChildren().getNodes(true);
        }
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }

}
