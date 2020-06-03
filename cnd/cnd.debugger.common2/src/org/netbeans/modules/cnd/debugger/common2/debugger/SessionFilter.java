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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.datatransfer.Transferable;

import javax.swing.Action;

import org.openide.util.datatransfer.PasteType;

import org.netbeans.api.debugger.Session;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/SessionsView/
 *	org.netbeans.spi.viewmodel.TreeModelFilter
 *
 *	org.netbeans.spi.viewmodel.NodeModelFilter
 *	org.netbeans.spi.viewmodel.TableModelFilter
 *	org.netbeans.spi.viewmodel.NodeActionsProviderFilter
 */

public final class SessionFilter extends ModelListenerSupport
    implements TreeModelFilter, ExtendedNodeModelFilter, TableModelFilter, NodeActionsProviderFilter, Constants {

    public SessionFilter() {
	super("sessions");		// NOI18N
    }

    // interface TreeModelFilter etc
    @Override
    public void addModelListener(ModelListener l)  {
	if (super.addModelListenerHelp(l))
	    NativeDebuggerManager.get().registerSessionModel(this);
    }

    // interface TreeModelFilter etc
    @Override
    public void removeModelListener(ModelListener l)  {
	if (super.removeModelListenerHelp(l))
	    NativeDebuggerManager.get().registerSessionModel(null);
    }

    /**
     * Convert all children (usually Session's) that are paired
     * with NativeSessions to NativeSessions.
     *
     * We discover the pairing through NativeSession.map(). Not exactly
     * snappy but better than doing it everywhere else which now
     * can use the cheaper "instanceof NativeSession".
     * In other words, this is the most central place to do this.
     */

    // interface TreeModelFilter
    @Override
    public Object [] getChildren(TreeModel original, Object parent,
			        int from, int to) {
	Object [] children = null;
	try {
	    children = original.getChildren(parent, from, to);
	} catch (Exception x) {
	}
	return children;

	/* LATER
	Object [] children = null;
	if (parent == TreeModel.ROOT) {
	    try {
		children = original.getChildren(parent, from, to);
	    } catch (Exception x) {
	    }
	}

	Object [] newChildren = new Object[children.length];

	for (int i = 0; i < children.length; i++) {
	    NativeSession ds = NativeSession.map((Session) children[i]);
	    if (ds != null)
		newChildren[i] = ds;
	    else
		newChildren[i] = children[i];
	}
	return newChildren;
	*/
    }

    // interface TreeModelFilter
    @Override
    public int getChildrenCount(TreeModel original, Object parent) {
	int count = 0;
	try {
	    count = original.getChildrenCount(parent);
	} catch (Exception x) {
	}
	return count;
    }

    // interface TreeModelFilter
    @Override
    public Object getRoot(TreeModel original) {
	return original.getRoot();
    }

    // interface TreeModelFilter
    @Override
    public boolean isLeaf(TreeModel original, Object node) {
	boolean isLeaf = false;
	try {
	    isLeaf = original.isLeaf(node);
	} catch (Exception x) {
	}
	return isLeaf;
    }




    // interface NodeModelFilter
    @Override
    public String getDisplayName(NodeModel original, Object node)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.getDisplayName(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return ds.getName();
	else
	    return original.getDisplayName(node);
    }

    // interface NodeModelFilter
    @Override
    public String getIconBase(NodeModel original, Object node)
	throws UnknownTypeException {

	return original.getIconBase(node);
    }

    // interface NodeModelFilter
    @Override
    public String getShortDescription(NodeModel original, Object node)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.getShortDescription(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    // Actually a _long_ version of the program name
	    return ds.getTarget();
	} else
	    return original.getShortDescription(node);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.canCopy(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return false;
	else
	    return original.canCopy(node);

    }

    // interface ExtendedNodeModelFilter
    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node)
	throws UnknownTypeException, java.io.IOException {

	if (! (node instanceof Session))
	    return original.clipboardCopy(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    throw new java.io.IOException();
	} else
	    return original.clipboardCopy(node);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public boolean canCut(ExtendedNodeModel original, Object node)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.canCut(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return false;
	else
	    return original.canCut(node);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node)
	throws UnknownTypeException, java.io.IOException {

	if (! (node instanceof Session))
	    return original.clipboardCut(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    throw new java.io.IOException();
	} else
	    return original.clipboardCut(node);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public boolean canRename(ExtendedNodeModel original, Object node)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.canRename(node);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return false;
	else
	    return original.canRename(node);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public void setName(ExtendedNodeModel original, Object node, String name)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    original.setName(node, name);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return;
	else
	    original.setName(node, name);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original,
				     Object node,
				     Transferable t)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return original.getPasteTypes(node, t);
	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null)
	    return new PasteType[0];
	else
	    return original.getPasteTypes(node, t);
    }

    // interface ExtendedNodeModelFilter
    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node)
	throws UnknownTypeException {

	return original.getIconBaseWithExtension(node);
    }

    private Object fallbackGetValueAt(TableModel original,
				      Object node,
				      String columnID)
				  throws UnknownTypeException {
	try {
	    return original.getValueAt(node, columnID);
	} catch (UnknownTypeException x) {
		return "";
	}
    }

    // interface TableModelFilter
    @Override
    public Object getValueAt(TableModel original, Object node, String columnID)
	throws UnknownTypeException {

	if (! (node instanceof Session))
	    return fallbackGetValueAt(original, node, columnID);

	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    if (PROP_SESSION_PID.equals(columnID)) {
		return new Long(ds.getPid());
		/* no longer exist
	    } else if (columnID == PROP_SESSION_CURRENT_LANGUAGE ) {
		return node;
	    } else if (columnID == SESSION_HOST_NAME_COLUMN_ID ) {
		return "Location-placeholder";
		*/
	    } else if (SESSION_DEBUGGER_COLUMN_ID.equals(columnID)) {
		return ds.getSessionEngine();
	    } else if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
		return ds.getSessionState();
	    } else if (PROP_SESSION_LOCATION.equals(columnID)) {
		return ds.getSessionLocation();
	    } else if (PROP_SESSION_MODE.equals(columnID)) {
		return ds.getSessionMode();
	    } else if (PROP_SESSION_ARGS.equals(columnID)) {
		return ds.getSessionArgs();
	    } else if (PROP_SESSION_CORE.equals(columnID)) {
		return ds.getSessionCore();
	    } else if (PROP_SESSION_HOST.equals(columnID)) {
		return ds.getSessionHost();
	    } else {
		return original.getValueAt(node, columnID);
	    }
	} else {
	    return fallbackGetValueAt(original, node, columnID);
	}
    }

    // interface TableModelFilter
    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID)
	throws UnknownTypeException {

	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    return true;
	} else {
	    try {
		return original.isReadOnly(node, columnID);
	    } catch (UnknownTypeException x) {
		return true;
	    }
	}
    }

    // interface TableModelFilter
    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value)
	throws UnknownTypeException {

	NativeSession ds = NativeSession.map((Session) node);
	if (ds != null) {
	    // our stuff is all readonly
	    // assert false;
	} else {
	    original.setValueAt(node, columnID, value);
	}

	return;
    }


    // interface NodeActionsProviderFilter
    @Override
    public Action[] getActions(NodeActionsProvider original, Object node)
	throws UnknownTypeException {

	Action[] actions = original.getActions(node);
	Action[] newActions = new Action[actions.length+1];
	System.arraycopy(actions, 0, newActions, 0, actions.length);
	// 6550627 newActions[actions.length] = new AddSessionAction();
	return newActions;
    }

    // interface NodeActionsProviderFilter
    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node)
	throws UnknownTypeException {
	original.performDefaultAction(node);
    }

    // inner class Actions .....................................................
/* 6550627, not needed
    private static class AddSessionAction extends AbstractAction {
	AddSessionAction() {
	    super("AddSessionAction");
	    setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
	    System.out.println("AddWatchAction: NOT IMPLEMENTED");
	}
    }
*/
}
