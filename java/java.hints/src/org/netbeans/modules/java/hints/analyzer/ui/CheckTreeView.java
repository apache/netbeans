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

package org.netbeans.modules.java.hints.analyzer.ui;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.java.hints.analyzer.ui.CheckRenderer.State;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hrebejk
 */
public class CheckTreeView extends BeanTreeView  {
    
    private NodeTreeModel nodeTreeModel;
    
    /** Creates a new instance of CheckTreeView */
    public CheckTreeView() {
        
        setFocusable( false );
        
        CheckListener l = new CheckListener();
        tree.addMouseListener( l );
        tree.addKeyListener( l );

        CheckRenderer check = new CheckRenderer();
        tree.setCellRenderer( check );
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        tree.setShowsRootHandles(false);
        
        InputMap input = tree.getInputMap( JTree.WHEN_FOCUSED );
        if( null != input )
            input.remove( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0) );
        
        setBorder( UIManager.getBorder("ScrollPane.border") );
    }
    
    @Override
    protected NodeTreeModel createModel() {
        nodeTreeModel = super.createModel();
        return nodeTreeModel;
    }

    public void expandRow( int row ) {        
        tree.expandRow(row);
    }
    
    public boolean getScrollsOnExpand() {
        return tree.getScrollsOnExpand();
    }
    
    public void setScrollsOnExpand( boolean scrolls ) {
        tree.setScrollsOnExpand( scrolls );
    }
    
    @Override
    protected void showPath(TreePath path) {
        tree.expandPath(path);
        showPathWithoutExpansion(path);
    }
    
    @Override
    protected void showSelection(TreePath[] treePaths) {
        tree.getSelectionModel().setSelectionPaths(treePaths);

        if (treePaths.length == 1) {
            showPathWithoutExpansion(treePaths[0]);
        }
    }
    
    private void showPathWithoutExpansion(TreePath path) {
        Rectangle rect = tree.getPathBounds(path);

        if (rect != null && getWidth() > 0 && getHeight() > 0 ) {
            tree.scrollRectToVisible(rect);
        }
    }
    
    class CheckListener implements MouseListener, KeyListener {

        // MouseListener -------------------------------------------------------
        
        public void mouseClicked(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                TreePath path = tree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                Rectangle r = tree.getPathBounds(path);
                if (r != null) {
                    r.width = r.height;

                    if (r.contains(e.getPoint())) {
                        if (toggle(path)) {
                            e.consume();
                            repaint(); //XXX

                        }
                    }
                }
            }
        }

        public void keyTyped(KeyEvent e) {}

        public void keyReleased(KeyEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {}

        // Key Listener --------------------------------------------------------
        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE ) {
                
                if ( e.getSource() instanceof JTree ) {
                    JTree tree = (JTree) e.getSource();
                    TreePath path = tree.getSelectionPath();

                    if ( toggle( path )) {
                        e.consume();
                        repaint(); //XXX
                    }
                }
            }
        }
        
        // Private methods -----------------------------------------------------
        
        private boolean toggle( TreePath treePath ) {
            
            if( treePath == null )
                return false;
            
            Node node = Visualizer.findNode( treePath.getLastPathComponent() );
            if( node == null )
                return false ;

            Collection<? extends FixDescription> fixes = node.getLookup().lookupAll(FixDescription.class);
            if (!fixes.isEmpty()) {
                State s = CheckRenderer.getCheckState(fixes);
                boolean select = s != State.SELECTED;
                for (FixDescription fd : fixes) {
                    fd.setSelected(select);
                }
                return true;
//                if( description.isSelectable()  ) {
//                    description.setSelected( !description.isSelected() );
//                    return true;
//                } else {
//                    boolean newState = !description.isSelected();
//                    description.setSelected(newState);
//                    toggleChildren( description.getSubs(), newState );
//                }
            }
            
            return false;
        }
        
//        private void toggleChildren( List<ElementNode.Description> children, boolean newState ) {
//            if( null == children )
//                return;
//            for( ElementNode.Description d : children ) {
//                d.setSelected( newState );
//                toggleChildren( d.getSubs(), newState );
//            }
//        }
    }
    
}
