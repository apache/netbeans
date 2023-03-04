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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.event.MouseEvent;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPopupMenu;

import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * The component that will display a panel corresponding to the node selection of its parent ExplorerManager, usually a
 * ContentPanel. It provides the mapping between selected nodes and displayed panels. A parent ContentPanel will get a node hierarchy
 * by calling getRoot and display that tree in it's structure view. The PanelView will then show an appropriate panel
 * based on the node selection. Subclasses should, at a bare minimum, initialize the root node and override the showSelection method.
 
 * Created on October 29, 2002, 12:24 PM
 * @author B.Ashby, M.Kuchtiak
 */
public abstract class PanelView extends javax.swing.JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(PanelView.class.getName());

    private Node root;
    /** not null if popup menu enabled */
    transient boolean sectionHeaderClicked;
    transient PopupAdapter popupListener;
    /** the most important listener  */
    //transient NodeSelectedListener nodeListener = null;
    
    /** Explorer manager, valid when this view is showing */
    private transient ExplorerManager manager;
    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;
    /** ResourceBundle for ejb package. */
    transient ErrorPanel errorPanel;
    
    public PanelView() {
        initComponents();
    }
    
    public void initComponents () {
    }
    
    void attachErrorPanel(ErrorPanel errorPanel) {
        this.errorPanel=errorPanel;
    }
    
    public ErrorPanel getErrorPanel() {
        return errorPanel;
    }
    
    protected abstract org.netbeans.modules.xml.multiview.Error validateView();
    
    public final void checkValidity() {
        org.netbeans.modules.xml.multiview.Error error = validateView();
        if (error!=null) {
            errorPanel.setError(error);
        } else {
            errorPanel.clearError();
        }
    }
    
    /**
     * Gets the current root Node.
     * @return the Node.
     */
    public Node getRoot() {
        return root;
    }
    
    public void setRoot(Node r){
        root = r;
    }

    public void setSectionHeaderClicked(boolean value) {
        sectionHeaderClicked=value;
    }
    
    public boolean isSectionHeaderClicked() {
        return sectionHeaderClicked;
    }
    
    /** Called when the view wishes to reuse the context menu from the root node
     * as its own context menu
     * @param value true to set the context menu, false otherwise
     */
    public void setPopupAllowed(boolean value) {
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter();
            addMouseListener(popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            removeMouseListener(popupListener);
            popupListener = null;
            return;
        }
    }
    
    /** Popup adapter.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        
        PopupAdapter() {
        }
        
        protected void showPopup(MouseEvent e) {
            JPopupMenu popup = getRoot().getContextMenu();
            popup.show(PanelView.this,e.getX(), e.getY());
            
        }
        
    }
    
    /** Called when explorer manager has changed the current selection.
     * The view should display the panel corresponding to the selected nodes
     * @param nodes the nodes used to update the view
     */
    public abstract void showSelection(Node[] nodes) ;
    
    /** The view can change the explorer manager's current selection.
     * @param value the new node to explore
     * @param nodes the nodes to select
     * @return false if the explorer manager is not able to change the selection
     */
    
    public boolean setManagerExploredContextAndSelection(Node value, Node[] nodes) {
        try{
            getExplorerManager().setExploredContextAndSelection(value, nodes);
        }
        catch (PropertyVetoException e) {
            return false;
        }
        return true;
    }
    /** The view can change the explorer manager's current selection.
     * @param nodes the nodes to select
     * @return false if the explorer manager is not able to change the selection
     */
    
    public boolean setManagerSelection(Node[] nodes) {
        try{
            getExplorerManager().setSelectedNodes(nodes);
        } catch (PropertyVetoException e) {
            return false;
        } catch (IllegalArgumentException iae) {
            // see #140613
            LOGGER.log(Level.WARNING, "Failed to select the given nodes.", iae);
            return false;
        }
        return true;
    }
    /** Called when explorer manager is about to change the current selection.
     * The view can forbid the change if it is not able to display such
     * selection.
     * @param nodes the nodes to select
     * @return false if the view is not able to change the selection
     */
    protected boolean selectionAccept(Node[] nodes) {
        return true;
    }
    
     /**
     * A parent ComponentPanel uses this method to notify  its PanelView children that it was opened
     * and lets them do any needed initialization as a result. Default implementation does nothing.
     */   
    public void open(){
    }
   /**
     * A parent ComponentPanel uses this method to notify  its PanelView children it is about to close.
     * and lets them determine if they are ready. Default implementation just returns true.
     * @return boolean True if the PanelView is ready to close, false otherwise.
     */    
    public boolean canClose(){
        return  true;
    }
    
     /* Initializes the component.
      * We need to register for ExplorerManager events here
      */
    
    public void addNotify() {
        super.addNotify();
        
        // Enter key in the tree
        /*
        ExplorerManager newManager = ExplorerManager.find(this);
        System.out.println("newManager="+newManager);
        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener(wlvc);
                manager.removePropertyChangeListener(wlpc);
            }
            
            manager = newManager;
            
            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(nodeListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(nodeListener, manager));
            //manager.addPropertyChangeListener(nodeListener);
            
        }
         */
    }
    
    /**
     * Gets the current ExplorerManager the view is attached.
     * @return the ExplorerManager.
     */
    
    public ExplorerManager getExplorerManager() {
        return ExplorerManager.find(this);
        //return manager;
    }
    /*
    class NodeSelectedListener implements VetoableChangeListener,PropertyChangeListener {
        
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                if (!selectionAccept((Node[])evt.getNewValue())) {
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            System.out.println("propName = "+evt.getPropertyName()+":"+ExplorerManager.PROP_SELECTED_NODES);
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            showSelection(selectedNodes);
            
        }
    }
     */
}
