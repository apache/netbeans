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
package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** Responsible for handling tree model events from the user-supplied treemodel
 * portion of a DefaultOutlineModel, translating them into appropriate 
 * TableModelEvents and refiring these events to listeners on the table model.
 * <p>
 * This class could be (and originally was) incorporated directly into 
 * DefaultOutlineModel, but is separated for better readability and separation
 * of concerns.
 *
 * @author  Tim Boudreau
 */
final class EventBroadcaster implements TableModelListener, TreeModelListener, ExtTreeWillExpandListener, TreeExpansionListener {
    
    /** Debugging constant for whether logging should be enabled */
    static boolean log = false;
    
    /** Debugging message counter to differentiate log entries */
    private int logcount = 0;
    
    /** The model we will proxy */
    private DefaultOutlineModel model;
    
    /** The last event sent to treeWillExpand/Collapse, used to compare against the
     * next value sent to treeExpanded/Collapse */
    private TreeExpansionEvent inProgressEvent = null;
    
    /** A TableModelEvent generated in treeWillExpand/Collapse (so, generated when
     * data about the rows/columns in the tree model is still in sync with the
     * TableModel), which will be fired from treeExpanded/Collapsed if the
     * expansion event is not vetoed */
    private TableModelEvent pendingExpansionEvent = null;

    /** Are we in the middle of firing multiple TableModelEvents for a single
     * TreeModelEvent. */
    private boolean inMultiEvent = false;
    
    //Some constants we use to have a single method handle all translated
    //event firing
    private static final int NODES_CHANGED = 0;
    private static final int NODES_INSERTED = 1;
    private static final int NODES_REMOVED = 2;
    private static final int STRUCTURE_CHANGED = 3;
    
    //XXX deleteme - string version of the avoid constants debug output:
    private static final String[] types = new String[] {
        "nodesChanged", "nodesInserted", "nodesRemoved", "structureChanged"
    }; //NOI18N

    /** List of table model listeners */
    private List<TableModelListener> tableListeners = new ArrayList<TableModelListener>();
    
    /** List of tree model listeners */
    private List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
    
    
    /** Creates a new instance of EventBroadcaster which will
     * produce events for the passed DefaultOutlineModel model.  */
    public EventBroadcaster(DefaultOutlineModel model) {
        setModel (model);
    }
    
    /** Debug logging */
    private void log (String method, Object o) {
        if (log) {
            if (o instanceof TableModelEvent) {
                //TableModelEvents just give their hash code in toString()
                o = tableModelEventToString ((TableModelEvent) o);
            }
            System.err.println("EB-" + (logcount++) + " " + method + ":" + 
                (o instanceof String ? 
                (String) o : o.toString()));
        }
    }
    
    
//***************** Bean properties/convenience getters & setters ************    
    /** Flag which is set to true while multiple TableModelEvents generated
     * from a single TreeModelEvent are being fired, so clients can avoid
     * any model queries until all pending changes have been fired.  The
     * main thing to avoid is any mid-process repaints, which can only happen
     * if the response to an event will be to call paintImmediately(). 
     * <p>
     * This value is guaranteed to be true for the first of a group of
     * related events, and false if tested in response to the final event.
     */
    public boolean areMoreEventsPending() {
        return inMultiEvent;
    }
    
    /** Get the outline model for which this broadcaster will proxy events*/
    private DefaultOutlineModel getModel() {
        return model;
    }
    
    /** Set the outline model this broadcaster will proxy events for */
    private void setModel(DefaultOutlineModel model) {
        this.model = model;
    }
    
    /** Convenience getter for the proxied model's layout cache */
    private AbstractLayoutCache getLayout() {
        return getModel().getLayout();
    }
    
    /** Convenience getter for the proxied model's TreePathSupport */
    private TreePathSupport getTreePathSupport() {
        return getModel().getTreePathSupport();
    }
    
    /** Convenience getter for the proxied model's user-supplied TreeModel */
    private TreeModel getTreeModel() {
        return getModel().getTreeModel();
    }
    
    /** Convenience getter for the proxied model's user-supplied TableModel (in
     * practice, an instance of ProxyTableModel driven by the tree model and a
     * RowModel) */
    private TableModel getTableModel() {
        return getModel().getTableModel();
    }
   
    
    
//******************* Event source implementation **************************
    
    /** Add a table model listener.  All events fired by this EventBroadcaster
     * will have the OutlineModel as the event source */
    public synchronized void addTableModelListener(TableModelListener l) {
        tableListeners.add (l);
    }
    
    /** Add a tree model listener.  All events fired by this EventBroadcaster
     * will have the OutlineModel as the event source */
    public synchronized void addTreeModelListener(TreeModelListener l) {
        treeListeners.add (l);
    }    
    
    /** Remove a table model listener.  */
    public synchronized void removeTableModelListener(TableModelListener l) {
        tableListeners.remove(l);
    }
    
    /** Remove a tree model listener.  */
    public synchronized void removeTreeModelListener(TreeModelListener l) {
        treeListeners.remove(l);
    }
    
    /** Fire a table change to the list of listeners supplied. The event should
     * already have its source set to be the OutlineModel we're proxying for. */
    private void fireTableChange (TableModelEvent e, TableModelListener[] listeners) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        
        assert (e.getSource() == getModel());
        
        log ("fireTableChange", e);
        
        for (int i=0; i < listeners.length; i++) {
            listeners[i].tableChanged(e);
        }
    }
    
    /** Convenience method to fire a single table change to all listeners */
    void fireTableChange (TableModelEvent e) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        inMultiEvent = false;
        fireTableChange(e, getTableModelListeners());
    }
    
    /** Fires multiple table model events, setting the inMultiEvent flag
     * as appropriate. */
    private void fireTableChange (TableModelEvent[] e) {
        //Event may be null for offscreen info, etc.
        if (e == null || e.length==0) {
            return;
        }
        
        TableModelListener[] listeners = getTableModelListeners();
        inMultiEvent = e.length > 1;
        //System.err.println("fireTableChange("+Arrays.toString(e)+")");
        try {
            for (int i=0; i < e.length; i++) {
                if (i == e.length-1) {
                    inMultiEvent = false;
                }
                fireTableChange (e[i], listeners);
            }
        } finally {
            inMultiEvent = false;
        }
    }
    
    /** Fetch an array of the currently registered table model listeners */
    private TableModelListener[] getTableModelListeners() {
        TableModelListener[] listeners;
        synchronized (this) {
            listeners = new TableModelListener[
                tableListeners.size()];
            
            listeners = tableListeners.toArray(listeners);
        }
        return listeners;
    }
    
    /** Fire the passed TreeModelEvent of the specified type to all
     * registered TreeModelListeners.  The passed event should already have
     * its source set to be the model. */
    private synchronized void fireTreeChange (TreeModelEvent e, int type) {
        //Event may be null for offscreen info, etc.
        if (e == null) {
            return;
        }
        assert (e.getSource() == getModel());
        
        TreeModelListener[] listeners;
        synchronized (this) {
            listeners = new TreeModelListener[treeListeners.size()];
            listeners = treeListeners.toArray(listeners);
        }
        
        log ("fireTreeChange-" + types[type], e);
        
        //Now refire it to any listeners
        for (int i=0; i < listeners.length; i++) {
            switch (type) {
                case NODES_CHANGED :
                    listeners[i].treeNodesChanged(e);
                    break;
                case NODES_INSERTED :
                    listeners[i].treeNodesInserted(e);
                    break;
                case NODES_REMOVED :
                    listeners[i].treeNodesRemoved(e);
                    break;
                case STRUCTURE_CHANGED :
                    listeners[i].treeStructureChanged(e);
                    break;
                default :
                    assert false;
            }
        }
    }    
    
//******************* Event listener implementations ************************    
    
    /** Process a change event from the user-supplied tree model.  This
     * method will throw an assertion failure if it receives any event type
     * other than TableModelEvent.UPDATE - the ProxyTableModel should never,
     * ever fire structural changes - only the tree model is allowed to do
     * that. */
    @Override
    public void tableChanged(final TableModelEvent e) {
        //The *ONLY* time we should see events here is due to user
        //data entry.  The ProxyTableModel should never change out
        //from under us - all structural changes happen through the
        //table model.
        assert (e.getType() == TableModelEvent.UPDATE) : "Table model should only fire " +
            "updates, never structural changes";

        if( SwingUtilities.isEventDispatchThread() ) {
            fireTableChange (translateEvent(e));
        } else {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    tableChanged(e);
                }
            });
        }
    }
    
    /** Process a change event from the user-supplied tree model.
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (translateEvent(e), NODES_CHANGED);
        
        TableModelEvent[] events = translateEvent(e, NODES_CHANGED);
        getLayout().treeNodesChanged(e);
        fireTableChange(events);
    }
    
    /** Process a node insertion event from the user-supplied tree model 
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (translateEvent(e), NODES_INSERTED);
        
        TableModelEvent[] events = translateEvent(e, NODES_INSERTED);
        getLayout().treeNodesInserted(e);
        fireTableChange(events);
    }
    
    /** Process a node removal event from the user-supplied tree model 
     * Order of operations: 
     * <ol><li>Refire the same tree event with the OutlineModel we're
     *   proxying as the source</li>
     * <li>Create one or more table model events (more than one if the
     * incoming event affects discontiguous rows) reflecting the effect
     * of the tree change</li>
     * <li>Call the method with the same signature as this one on the
     * layout cache, so it will update its state appropriately</li>
     * <li>Fire the generated TableModelEvent(s)</li></ol>
     */
    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        fireTreeChange (translateEvent(e), NODES_REMOVED);
        
        TableModelEvent[] events = translateEvent(e, NODES_REMOVED);
        getLayout().treeNodesRemoved(e);
        fireTableChange(events);
    }
    
    /** Process a structural change event from the user-supplied tree model.
     * This will result in a generic &quot;something changed&quot; 
     * TableModelEvent being fired.  */
    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        assert SwingUtilities.isEventDispatchThread();
        
        getTreePathSupport().treeStructureChanged(e);
        fireTreeChange (translateEvent(e), STRUCTURE_CHANGED);

        if (!getLayout().isExpanded(e.getTreePath())) {
            // Do not care about structural changes in collapsed nodes.
            // But the node's leaf property could change...
            treeNodesChanged(e);
            return ;
        }
        
        getTreePathSupport().clear();
        
        //We will just fire a "Something happened. Go figure out what." event.
        fireTableChange (new TableModelEvent (getModel()));
    }
    
    /** Receives a TreeWillCollapse event and constructs a TableModelEvent
     * based on the pending changes while the model still reflects the unchanged
     * state */
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeWillCollapse", event);
        
        //Construct the TableModelEvent here, before data structures have
        //changed.  We will fire it from TreeCollapsed if the change is 
        //not vetoed.
        pendingExpansionEvent = translateEvent (event, false);
        log ("treeWillCollapse generated ", pendingExpansionEvent);
        inProgressEvent = event;
    }
    
    /** Receives a TreeWillExpand event and constructs a TableModelEvent
     * based on the pending changes while the model still reflects the unchanged
     * state */
    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        assert SwingUtilities.isEventDispatchThread();

        log ("treeWillExpand", event);
        
        //Construct the TableModelEvent here, before data structures have
        //changed.  We will fire it from TreeExpanded if the change is not
        //vetoed
        pendingExpansionEvent = translateEvent (event, true);
        
        log ("treeWillExpand generated", pendingExpansionEvent);
        inProgressEvent = event;
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        assert SwingUtilities.isEventDispatchThread();

        log ("treeCollapsed", event);
        
        //FixedHeightLayoutCache tests if the event is null.
        //Don't know how it could be, but there's probably a reason...
        if(event != null) {
            TreePath path = event.getPath();

            //Tell the layout about the change
            if(path != null && getTreePathSupport().isVisible(path)) {
                getLayout().setExpandedState(path, false);
            }
        }

        
        log ("about to fire", pendingExpansionEvent);
        
        //Now fire a change on the owning row so its display is updated (it
        //may have just become an expandable node)
        int row;
        if (event != null) {
            TreePath path = event.getPath();
            row = getLayout().getRowForPath(path);
        } else {
            row = -1;
        }
        TableModelEvent evt;
        if (row == -1) {
            evt = new TableModelEvent(getModel());
        } else {
            evt = new TableModelEvent(getModel(), row, row, 0, TableModelEvent.UPDATE);
        }
        fireTableChange(new TableModelEvent[] {evt, pendingExpansionEvent});
        
        pendingExpansionEvent = null;
        inProgressEvent = null;
    }
    
    /** Updates the layout to mark the descendants of the events path as also
     * expanded if they were the last it was expanded, then fires a table change. */
    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeExpanded", event);
        
        //Mysterious how the event could be null, but JTree tests it
        //so we will too.
        if(event != null) {
            updateExpandedDescendants(event.getPath());
        }

        log ("about to fire", pendingExpansionEvent);
        
        //Now fire a change on the owning row so its display is updated (it
        //may have just become an expandable node)
        int row;
        if (event != null) {
            TreePath path = event.getPath();
            row = getLayout().getRowForPath(path);
        } else {
            row = -1;
        }
        TableModelEvent evt;
        if (row == -1) {
            evt = new TableModelEvent(getModel());
        } else {
            evt = new TableModelEvent(getModel(), row, row, 0, TableModelEvent.UPDATE);
        }
        fireTableChange(new TableModelEvent[] {evt, pendingExpansionEvent});
        
        pendingExpansionEvent = null;
        inProgressEvent = null;
    }
    
    /** Messaged if the tree expansion event (for which we will have already
     * constructed a TableModelEvent) was vetoed;  disposes of the constructed
     * TableModelEvent in that circumstance. */
    @Override
    public void treeExpansionVetoed(TreeExpansionEvent event, ExpandVetoException exception) {
        assert SwingUtilities.isEventDispatchThread();
        
        log ("treeExpansionVetoed", exception);
        
        //Make sure the event that was vetoed is the one we're interested in
        if (event == inProgressEvent) {
            //If so, delete the expansion event we thought we were going
            //to use in treeExpanded/treeCollapsed, so that it doesn't
            //stick around forever holding references to objects from the
            //model
            pendingExpansionEvent = null;
            inProgressEvent = null;
        }
    }
    
//******************* Support routines for handling events ******************
    //do I date myself by using the word "routines"? :-)

    /** Re&euml;expand descendants of a newly expanded path which were
     * expanded the last time their parent was expanded */
    private void updateExpandedDescendants(TreePath path) {
        getLayout().setExpandedState(path, true);

        TreePath[] descendants = 
            getTreePathSupport().getExpandedDescendants(path);

        if(descendants.length > 0) {
            for (int i=0; i < descendants.length; i++) {
                getLayout().setExpandedState(descendants[i], true);
            }
        }
    }    

    
//******************* Event translation routines ****************************
    
    /** Creates a TableModelEvent identical to the original except that the
     * column index has been shifted by +1.  This is used to refire events
     * from the ProxyTableModel (generated by RowModel.setValueFor()) as 
     * change events on the OutlineModel. */
    private TableModelEvent translateEvent (TableModelEvent e) {
        TableModelEvent nue = new TableModelEvent (getModel(),
            e.getFirstRow(), e.getLastRow(), e.getColumn()+1, e.getType());
        return nue;
    }
    
    /** Creates an identical TreeModelEvent with the model we are proxying
     * as the event source */
    private TreeModelEvent translateEvent (TreeModelEvent e) {
        //Create a new TreeModelEvent with us as the source
        TreeModelEvent nue = new TreeModelEvent (getModel(), e.getPath(), 
            e.getChildIndices(), e.getChildren());
        return nue;
    }
    
    /** Translates a TreeModelEvent into one or more contiguous TableModelEvents
     */
    private TableModelEvent[] translateEvent (TreeModelEvent e, int type) {

        TreePath path = e.getTreePath();
        
        //If the node is not expanded, we simply fire a change
        //event for the parent
        boolean inClosedNode = !getLayout().isExpanded(path);
        if (inClosedNode) {
            int row = getLayout().getRowForPath(path);
            //If the node is closed, no expensive checks are needed - just
            //fire a change on the parent node in case it needs to update
            //its display
            if (row != -1) {
                switch (type) {
                    case NODES_CHANGED :
                    case NODES_INSERTED :
                    case NODES_REMOVED :
                        return new TableModelEvent[] {
                            new TableModelEvent (getModel(), row, row,
                              0, TableModelEvent.UPDATE)
                        };
                    default: 
                        assert false : "Unknown event type " + type;
                }
            }
            //In a closed node that is not visible, no event needed
            return new TableModelEvent[0];
        }
        
        int[] rowIndices = computeRowIndices(e);
        boolean discontiguous = isDiscontiguous(rowIndices);

        int[][] blocks;
        if (discontiguous) {
            blocks = getContiguousIndexBlocks(rowIndices, type == NODES_REMOVED);
            log ("discontiguous " + types[type] + " event", blocks.length + " blocks");
        } else {
            blocks = new int[][]{rowIndices};
        }
        
        
        TableModelEvent[] result = new TableModelEvent[blocks.length];
        for (int i=0; i < blocks.length; i++) {
            
            int[] currBlock = blocks[i];
            switch (type) {
                case NODES_CHANGED :
                    result[i] = createTableChangeEvent (e, currBlock);
                    break;
                case NODES_INSERTED :
                    result[i] = createTableInsertionEvent (e, currBlock);
                    break;
                case NODES_REMOVED :
                    result[i] = createTableDeletionEvent (e, currBlock);
                    break;
                default :
                    assert false : "Unknown event type: " + type;
            }            
        }
        log ("translateEvent", e);
        log ("generated table events", new Integer(result.length));
        if (log) {
            for (int i=0; i < result.length; i++) {
                log ("  Event " + i, result[i]);
            }
        }
        return result;
    }
    
    /** Translates tree expansion event into an appropriate TableModelEvent
     * indicating the number of rows added/removed at the appropriate index */
    private TableModelEvent translateEvent (TreeExpansionEvent e, boolean expand) {
        //PENDING:  This code should be profiled - the descendent paths search
        //is not cheap, and it might be less expensive (at least if the table
        //does not have expensive painting logic) to simply fire a generic
        //"something changed" table model event and be done with it.
        
        TreePath path = e.getPath();
        
        //Add one because it is a child of the row.
        int firstRow = getLayout().getRowForPath(path) + 1;
        if (firstRow == -1) {
            //This does not mean nothing happened, it may just be that we are
            //a large model tree, and the FixedHeightLayoutCache says the
            //change happened in a row that is not showing.
            
            //TODO:  Just to make the table scrollbar adjust itself appropriately,
            //we may want to look up the number of children in the model and
            //fire an event that says that that many rows were added.  Waiting
            //to see if anybody actually will use this (i.e. fires changes in
            //offscreen nodes as a normal part of usage
            return null;
        }
        
        //Get all the expanded descendants of the path that was expanded/collapsed
        TreePath[] paths = getTreePathSupport().getExpandedDescendants(path);
        
        //Start with the number of children of whatever was expanded/collapsed
        int count = getTreeModel().getChildCount(path.getLastPathComponent());
        
        if (count == 0) {
            return null;
        }
        
        //Iterate any of the expanded children, adding in their child counts
        for (int i=0; i < paths.length; i++) {
            count += getTreeModel().getChildCount(paths[i].getLastPathComponent());
        }
        
        //Now we can calculate the last row affected for real
        int lastRow = firstRow + count -1;
        
        //Construct a table model event reflecting this data
        TableModelEvent result = new TableModelEvent (getModel(), firstRow, lastRow, 
            TableModelEvent.ALL_COLUMNS, expand ? TableModelEvent.INSERT : 
            TableModelEvent.DELETE);
            
        return result;
    }

    /** Create a change TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of row indices. */
    private TableModelEvent createTableChangeEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result;
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        
        int first = null == indices ? row : indices[0];
        int last = null == indices ? row : indices[indices.length - 1];
        
        //TODO - does not need to be ALL_COLUMNS, but we need a way to determine
        //which column index is the tree
        result = new TableModelEvent (getModel(), first, last, 
            0/*TableModelEvent.ALL_COLUMNS*/, TableModelEvent.UPDATE);
        
        return result;
    }
    
    /** Create an insertion TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of row indices. */
    private TableModelEvent createTableInsertionEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result;

        log ("createTableInsertionEvent", e);
        
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        
        boolean realInsert = getLayout().isExpanded(path);

        if (realInsert) {
            if (indices.length == 1) {
                //Only one index to change, fire a simple event.  It
                //will be the first index in the array + the row +
                //1 because the 0th child of a node is 1 greater than
                //its row index
                int affectedRow = indices[0];
                result = new TableModelEvent (getModel(), affectedRow, affectedRow, 
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            } else {
                //Find the first and last indices.  Add one since it is at 
                //minimum the first index after the affected row, since it
                //is a child of it.
                int lowest = indices[0];
                int highest = indices[indices.length - 1];
                result = new TableModelEvent(getModel(), lowest, highest,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

            }
        } else {
            //Nodes were inserted in an unexpanded parent.  Just fire
            //a change for that row and column so that it gets repainted
            //in case the node there changed from leaf to non-leaf
            result = new TableModelEvent (getModel(), row, row, 
                TableModelEvent.ALL_COLUMNS); //TODO - specify only the tree column
        }        
        return result;
    }
    
    
    /** Create a deletion TableModelEvent for the passed TreeModelEvent and the 
     * contiguous subrange of row indices. */
    private TableModelEvent createTableDeletionEvent (TreeModelEvent e, int[] indices) {
        TableModelEvent result;
        
        log ("createTableDeletionEvent " + Arrays.asList(toArrayOfInteger(indices)), e);
        
        TreePath path = e.getTreePath();
        int row = getLayout().getRowForPath(path);
        if (row == -1) {
            //XXX could calculate based on last visible row?
            //return null;
            // 
            // never mind, just assume that the row -1 is the invisible
            // root node and in such case the calculation bellow
            // will just succeed and returning null was even more stupid ...
        }

        int firstRow = indices[0];
        int lastRow = indices[indices.length - 1];

        log("TableModelEvent: fromRow: ", new Integer(firstRow));
        log(" toRow: ", new Integer(lastRow));

        result = new TableModelEvent(getModel(), firstRow, lastRow,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        return result;
    }


//**************** Static utility routines *****************************    

    /** Determine if the indices referred to by a TreeModelEvent are
     * contiguous.  If they are not, we will need to generate multiple
     * TableModelEvents for each contiguous block */
    static boolean isDiscontiguous(int[] indices) {
        if (indices == null || indices.length <= 1) {
            return false;
        }
        Arrays.sort(indices);
        int lastVal = indices[0];
        for (int i=1; i < indices.length; i++) {
            if (indices[i] != lastVal + 1) {
                return true;
            } else {
                lastVal++;
            }
        }
        return false;
    }
    
    /** Returns an array of int[]s each one representing a contiguous set of 
     * indices in the tree model events child indices - each of which can be
     * fired as a single TableModelEvent.  The length of the return value is
     * the number of TableModelEvents required to represent this TreeModelEvent.
     * If reverseOrder is true (needed for remove events, where the last indices
     * must be removed first or the indices of later removals will be changed),
     * the returned int[]s will be sorted in reverse order, and the order in
     * which they are returned will also be from highest to lowest. */
    static int[][] getContiguousIndexBlocks(int[] indices, final boolean reverseOrder) {
        
        //Quick checks
        if (indices.length == 0) {
            return new int[][] {{}};
        }
        if (indices.length == 1) {
            return new int[][] {indices};
        }
        
        //Sort the indices as requested
        if (reverseOrder) {
            inverseSort (indices);
        } else {
            Arrays.sort (indices);
        }

        final List<int[]> blocks = new ArrayList<int[]>();
        int startIndex = 0;
        
        //Iterate the indices
        for (int i = 1; i < indices.length; i++) {
            //See if we've hit a discontinuity
            int lastVal = indices[i-1];
            boolean newBlock = reverseOrder ? indices[i] != lastVal - 1
                                            : indices[i] != lastVal + 1;

            if (newBlock) {
                // new block detected
                // copy the last contiguous block and add it to the result array
                int[] block = new int[i - startIndex];
                System.arraycopy(indices, startIndex, block, 0, block.length);
                blocks.add(block);
                startIndex = i;
            }
        }
        
        // add last block to the result array
        int[] block = new int[indices.length - startIndex];
        System.arraycopy(indices, startIndex, block, 0, block.length);
        blocks.add(block);
        
        return blocks.toArray(new int[][] {});
    }
    
    /** Converts an Integer[] to an int[] */
    //XXX deleteme - used for debug logging only
    private static Integer[] toArrayOfInteger (int[] ints) {
        Integer[] result = new Integer[ints.length];
        for (int i=0; i < ints.length; i++) {
            result[i] = new Integer(ints[i]);
        }
        return result;
    }
    
    
    /** Sort an array of ints from highest to lowest */
    private static void inverseSort (int[] array) {
        //XXX replace with a proper sort algorithm at some point -
        //this is brute force
        for (int i=0; i < array.length; i++) {
            array[i] *= -1;
        }
        Arrays.sort(array);
        for (int i=0; i < array.length; i++) {
            array[i] *= -1;
        }
    }
    
    private static String tableModelEventToString (TableModelEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append ("TableModelEvent ");
        switch (e.getType()) {
            case TableModelEvent.INSERT : sb.append ("insert ");
                 break;
            case TableModelEvent.DELETE : sb.append ("delete ");
                 break;
            case TableModelEvent.UPDATE : sb.append ("update ");
                 break;
            default : sb.append("Unknown type ").append(e.getType());
        }
        sb.append ("from ");
        switch (e.getFirstRow()) {
            case TableModelEvent.HEADER_ROW : sb.append ("header row ");
                break;
            default : sb.append (e.getFirstRow());
                      sb.append (' ');
        }
        sb.append ("to ");
        sb.append (e.getLastRow());
        sb.append (" column ");
        switch (e.getColumn()) {
            case TableModelEvent.ALL_COLUMNS :
                sb.append ("ALL_COLUMNS");
                break;
            default : sb.append (e.getColumn());
        }
        return sb.toString();
    }

    /**
     * Compute real table row indices of nodes that are affected by the event.
     *
     * @param e Event description.
     * @return Indices of rows in the table where the nodes (affected by the
     * event) are displayed, or null if they are not available.
     */
    private int[] computeRowIndices(TreeModelEvent e) {
        int[] rowIndices;
        int parentRow = getLayout().getRowForPath(e.getTreePath());
        if (e.getChildren() != null) {
            rowIndices = new int[e.getChildren().length];
            for (int i = 0; i < e.getChildren().length; i++) {
                TreePath childPath =
                        e.getTreePath().pathByAddingChild(e.getChildren()[i]);
                int index = getLayout().getRowForPath(childPath);
                rowIndices[i] = index < 0
                        // child node is not shown yet, compute child row from
                        // parent row and index of the child
                        ? parentRow + e.getChildIndices()[i] + 1
                        : index;
            }
        } else {
            rowIndices = null;
        }
        return rowIndices;
    }
}
