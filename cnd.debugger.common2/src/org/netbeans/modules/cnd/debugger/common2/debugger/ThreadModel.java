/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger;


import java.awt.event.ActionEvent;
import java.awt.datatransfer.Transferable;

import javax.swing.Action;
import javax.swing.AbstractAction;
import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;

import org.openide.util.datatransfer.PasteType;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/ThreadsView/
 *	META-INF/debugger/netbeans-GdbDebuggerEngine/ThreadsView/
 *	org.netbeans.spi.viewmodel.TreeModel
 *	org.netbeans.spi.viewmodel.TableModel
 *	org.netbeans.spi.viewmodel.NodeModel
 *	org.netbeans.spi.viewmodel.NodeActionsProvider
 */

public final class ThreadModel extends ModelListenerSupport
    implements TreeModel, ExtendedNodeModel, NodeActionsProvider, TableModel,
    Constants {

    private NativeDebugger debugger;

    public ThreadModel(ContextProvider ctx) {
	super("threads");		// NOI18N
	debugger = ctx.lookupFirst(null, NativeDebugger.class);
    }

    // interface TreeModel
    @Override
    public Object getRoot() {
	// redundant?
	return ROOT;
    } 

    // interface TreeModel
    @Override
    public Object[] getChildren(Object parent, int from, int to) {
	if (parent == ROOT) {
	    Object o[] =  debugger.getThreads();
	    for (int i = 0; i < o.length; i++) {
		if (o[i] == null) {
		    System.out.printf("ThreadModel.getChildren(): " + // NOI18N
			              "NULL Thread[%d]\n", i); // NOI18N
		}
	    }
	    return o;
	}
	else
	    return null;
    }

    // interface TreeModel
    @Override
    public int getChildrenCount(Object parent) {
	if (parent == ROOT)
	    return debugger.getThreads().length;
	else
	    return 0;
    }

    // interface TreeModel
    @Override
    public boolean isLeaf(Object node) {
	if (node == ROOT)
	    return false;
	else
	    return true;
    }

    private Thread getThread(Object node) throws UnknownTypeException {
	// Probably SHOULD test for ROOT and return a dummy RootThread!
	if (node instanceof Thread) {
	    return (Thread) node;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface NodeModel
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return ROOT;
        } else {
            Thread thread = getThread(node);
            if (thread.isCurrent()) {
                return VariableValue.bold(thread.getName());
            } else {
                return thread.getName();
            }
        }
    }

    /* OLD
    // These are standard NB icons for threads.
    // See
    //	org.netbeans.modules.debugger.jpda.ui.views.ThreadsNodeModel
    // for reference

    private static String ICON_PATH =
	"org/netbeans/modules/debugger/resources";	// NOI18N

    private static String ICON_CURRENT_EVENT =
	ICON_PATH + "/threadsView/CurrentThread";	// NOI18N
    private static String ICON_EVENT =
	ICON_PATH + "/threadsView/RunningThread";	// NOI18N
    private static String ICON_CURRENT_NORMAL =
	ICON_PATH + "/threadsView/CurrentThread";	// NOI18N
    private static String ICON_NORMAL =
	ICON_PATH + "/threadsView/RunningThread";	// NOI18N
    */

    private static final String ICON_PATH =
	"org/netbeans/modules/cnd/debugger/common2/icons/";		// NOI18N

    private static String ICON_NORMAL =
	ICON_PATH + "thread";				// NOI18N
    private static String ICON_EVENT =
	ICON_PATH + "thread_hit";			// NOI18N
    private static String ICON_CURRENT_NORMAL =
	ICON_PATH + "thread_current";			// NOI18N
    private static String ICON_CURRENT_EVENT =
	ICON_PATH + "thread_current_hit";		// NOI18N


    // interface NodeModel
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
	if (node == ROOT)
	    return ICON_CURRENT_NORMAL;

	Thread t = getThread(node);
	if (t.hasEvent()) {
	    if (t.isCurrent()) {
		return ICON_CURRENT_EVENT;
	    } else {
		return ICON_EVENT;
	    }
	} else {
	    if (t.isCurrent()) {
		return ICON_CURRENT_NORMAL;
	    } else {
		return ICON_NORMAL;
	    }
	}
    }

    // interface NodeModel
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
	if (node == ROOT)
	    return ROOT;
	else
	    return getThread(node).getName();
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Thread) {
	    return false;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public Transferable clipboardCopy(Object node)
	throws UnknownTypeException, java.io.IOException {
	if (node == ROOT) {
	    return null;
	} else if (node instanceof Thread) {
	    throw new java.io.IOException();
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Thread) {
	    return false;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public Transferable clipboardCut(Object node)
	throws UnknownTypeException, java.io.IOException {
	if (node == ROOT) {
	    return null;
	} else if (node instanceof Thread) {
	    throw new java.io.IOException();
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Thread) {
	    return false;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public void setName(Object node, String name)
	throws UnknownTypeException {
	if (node == ROOT) {
	    return;
	} else if (node instanceof Thread) {
	    return;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t)
	throws UnknownTypeException {
	if (node == ROOT) {
	    return new PasteType[0];
	} else if (node instanceof Thread) {
	    return new PasteType[0];
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public String getIconBaseWithExtension(Object node) 
	throws UnknownTypeException {

	return getIconBase(node) + ".png";	// NOI18N
    }

    // interface TableModel
    @Override
    public Object getValueAt(Object node, String columnID)
	throws UnknownTypeException {

	Thread thread = getThread(node);

	if (columnID.equals(PROP_THREAD_START_FUNCTION)) {
	    return thread.getStartFunction();

	} else if (columnID.equals(PROP_THREAD_EXECUTING_FUNCTION)) {
	    return thread.getCurrentFunction();

	} else if (columnID.equals(PROP_THREAD_LWP)) {
	    return thread.getLWP();

	} else if (columnID.equals(PROP_THREAD_PRIORITY)) {
	    return thread.getPriority();

	} else if (columnID.equals(PROP_THREAD_STARTUP_FLAGS)) {
	    return thread.getStartupFlags();

	} else if (columnID.equals(PROP_THREAD_ADDRESS)) {
	    return thread.getAddress();

	} else if (columnID.equals(PROP_THREAD_SIZE)) {
	    return thread.getStackSize();


	// debuggercore predefined column id's:

	} else if (columnID.equals(PROP_THREAD_SUSPENDED)) {
	    return new Boolean(thread.isSuspended());

	} else if (columnID.equals(PROP_THREAD_STATE) ) {
	    return thread.getState();
	
	} else if (columnID.equals(PROP_THREAD_FILE) ) {
	    return thread.getFile();
	
	} else if (columnID.equals(PROP_THREAD_LINE) ) {
	    return thread.getLine();

	} else {
	    return "?" + columnID + "?";	// NOI18N
	}
    }

    // interface TableModel
    @Override
    public boolean isReadOnly(Object node, String columnID) {
	// TMP return true;

	if (columnID.equals(PROP_THREAD_LWP))
	    return false;
	else
	    return true;
    }

    // interface TableModel
    @Override
    public void setValueAt(Object node, String columnID, Object value) 
	throws UnknownTypeException, NumberFormatException {

	Thread thread = getThread(node);
	{
	    throw new UnknownTypeException(node);
	}
    }

    // interface NodeActionsProvider
    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {

	if (node == ROOT) {
	    return new Action[0];

	} else {
	    Thread thread = getThread(node);
	    return new Action[] {
		new MakeCurrentAction(debugger, thread),
	    };
	}
    }

    // interface NodeActionsProvider
    @Override
    public void performDefaultAction (Object node) throws UnknownTypeException {
	// This gets called redundantly, see issue 48891.
	debugger.makeThreadCurrent(getThread(node));
    }

    // innerclasses ...........................................................
    private static class MakeCurrentAction extends AbstractAction {
	private NativeDebugger debugger;
	private Thread thread;

	MakeCurrentAction(NativeDebugger debugger, Thread thread) {
	    super(Catalog.get("ACT_Make_Current")); // NOI18N
	    this.debugger = debugger;
	    this.thread = thread;
	    setEnabled(!thread.isCurrent());
	} 

        @Override
	public void actionPerformed(ActionEvent e) {
	    debugger.makeThreadCurrent(thread);
	}
    }

    // interface TreeModel etc
    @Override
    public void addModelListener(ModelListener l) {
	if (super.addModelListenerHelp(l))
	    debugger.registerThreadModel(this);
    }

    // interface TreeModel etc
    @Override
    public void removeModelListener(ModelListener l) {
	if (super.removeModelListenerHelp(l))
	    debugger.registerThreadModel(null);
    }
}
