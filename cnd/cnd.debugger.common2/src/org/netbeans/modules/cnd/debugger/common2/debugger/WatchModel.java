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

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.openide.util.actions.SystemAction;

import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxObjectAction;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.spi.viewmodel.NodeModel;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Registered i
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/WatchesView/
 *	org.netbeans.spi.viewmodel.TreeModel
 *	org.netbeans.spi.viewmodel.NodeModel
 *	org.netbeans.spi.viewmodel.TreeExpansionModel
 *      org.netbeans.spi.viewmodel.NodeActionsProvider
 */

public final class WatchModel extends VariableModel
    implements NodeActionsProvider {
    private static final String PROP_SHOW_PINNED_WATCHES = "showPinnedWatches"; // NOI18N
    private static final Properties PROPERTIES = Properties.getDefault().getProperties("native.debugger").getProperties("watchesProps");    // NOI18N

    static final ShowPinnedWatches showPinnedWatches = new ShowPinnedWatches();    
    private final EmptyWatch EMPTY_WATCH = new EmptyWatch();

    public WatchModel(ContextProvider ctx) {
	super(ctx);
        PROPERTIES.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PROP_SHOW_PINNED_WATCHES.equals(evt.getPropertyName())) {
                    WatchModel.this.treeChanged();
                    return;
                }
            }
        });
    }

    public WatchModel() {
	super();
    }

    private static NativeDebuggerManager manager() {
	return NativeDebuggerManager.get();
    }

    private static WatchBag watchBag() {
	return manager().watchBag();
    }

    // interface VariableModel
    @Override
    protected boolean isLocal() {
	return false;
    }

    /*
     * A note on children ...
     * debuggercore will first try to find a model by looking it up in a map
     * indexed by the class name. The debuggercoreWatch class will therefore
     * always map to the default debuggercore watch model.
     * That watch model will always return true for isLeaf of a Watch.
     * As a result we should never end up with a Watch object in any of the 
     * pulls.
     *
     * Therefore, when we return children we need to return something other
     * than a Watch.
     */

    // interface TreeModel
    @Override
    public Object[] getChildren(Object parent, int from, int to) 
		throws UnknownTypeException {
	assert !(parent instanceof Watch);

	if (parent == ROOT) {
            Object[] watches;
	    if (debugger != null) {
                watches = watchBag().watchesFor(debugger);
            } else {
                watches = manager().getWatches();
            }
            Object[] res = new Object[watches.length+1];
            System.arraycopy(watches, 0, res, 0, watches.length);
            res[watches.length] = EMPTY_WATCH;
            return res;
	} else if (parent instanceof Variable) {
	    Variable v = (Variable) parent;
	    Object[] children = v.getChildren();
            
            if (v.hasMore()) {
                Object[] newChildren = new Object[children.length+1];
                System.arraycopy(children, 0, newChildren, 0, children.length);
                newChildren[newChildren.length-1] = new ShowMoreMessage(v);
                children = newChildren;
            }
            
            return children;
	}

	throw new UnknownTypeException (parent);
    }

    // interface TreeModel
    @Override
    public int getChildrenCount(Object parent)  throws UnknownTypeException {
	assert !(parent instanceof Watch);
	if (parent == ROOT) {
	    if (debugger != null)
		return watchBag().watchesFor(debugger).length;
	    else
		return manager().getWatches().length;

	} else if (parent instanceof Variable) {
	    Variable v = (Variable) parent;
	    return v.getNumChild();
	}
	throw new UnknownTypeException (parent);
    }

    // interface TreeModel
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	return super.isLeaf(node);
    }

    // interface NodeModel
    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
        if (node instanceof EmptyWatch) {
            return "<_html><font color=\"#808080\">&lt;" + // [TODO] <_html> tag used as workaround, see TreeModelNode.setName() // NOI18N
                        Catalog.get("CTL_WatchesModel_Empty_Watch_Hint") + // NOI18N
                        "&gt;</font></html>"; // NOI18N
        }
        if (node instanceof Watch)
	    return ((Watch) node).getExpression ();
	else
	    return super.getDisplayName(original, node);
    }

    private static final String ICON_PATH =
	"org/netbeans/modules/cnd/debugger/common2/icons/";	// NOI18N

    private static final String ICON_WATCH1 = ICON_PATH + "watch_type1"; // NOI18N
    private static final String ICON_WATCH1_PTR = ICON_WATCH1 + "_pointer"; // NOI18N
    private static final String ICON_WATCH2 = ICON_PATH + "watch_type2"; // NOI18N
    private static final String ICON_WATCH2_PTR = ICON_WATCH2 + "_pointer"; // NOI18N

    // interface NodeModel
    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
	if (node instanceof WatchVariable) {
	    WatchVariable w = (WatchVariable) node;
	    if (w.getVariableName().startsWith("`")) { // NOI18N
		// fully qualified (only under dbx)
		// SHOULD make fully-qualified predicate an interface
		if (w.isPtr())
		    return ICON_WATCH2_PTR;
		else
		    return ICON_WATCH2;
	    } else {
		if (w.isPtr())
		    return ICON_WATCH1_PTR;
		else
		    return ICON_WATCH1;
	    }

	} else if (node instanceof Watch) {
	    // generic debuggercore watch
	    Watch w = (Watch) node;
	    if (w.getExpression().startsWith("`")) { // NOI18N
		// fully qualified
		return ICON_WATCH2;
	    } else {
		return ICON_WATCH1;
	    }

	} else {
	    return super.getIconBase(original, node);
	}
    }

    // interface NodeModel
    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	String sd = super.getShortDescription(original, node);

	if (Log.Variable.tipdebug && node instanceof NativeWatch) {
	    NativeWatch w = (NativeWatch) node;
	    if (w.watch() == null) {
		sd += 
		    "<html>" + // NOI18N
		    "<code>" + // NOI18N
		    "<hr>" + // NOI18N
		    "<b>expr</b>" + w.getExpression() + "<br>" + // NOI18N
		    "<b>scope</b>" + w.getScope() + "<br>" + // NOI18N
		    "<b>restricted</b>" + w.isRestricted() + "<br>" + // NOI18N
		    "</code>" + // NOI18N
		    "</html>"; // NOI18N
	    }
	}
	return sd;
    }

    // interface TableModel
    @Override
    public Object getValueAt(Object node, String columnID)
	throws UnknownTypeException {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	return super.getValueAt(node, columnID);
    }

    // interface TableModel
    @Override
    public boolean isReadOnly(Object node, String columnID)
	throws UnknownTypeException {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	return super.isReadOnly(node, columnID);
    }

    // interface TableModel
    @Override
    public void setValueAt(Object node, String columnID, Object value)
	throws UnknownTypeException {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	super.setValueAt(node, columnID, value);
    }

    // interface TreeExpansionModel
    @Override
    public synchronized boolean isExpanded(Object node)
	throws UnknownTypeException {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	return super.isExpanded(node);
    }

    // interface TreeExpansionModel
    @Override
    public synchronized void nodeCollapsed(Object node) {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	super.nodeCollapsed(node);
    }

    // interface TreeExpansionModel
    @Override
    public synchronized void nodeExpanded(Object node) {

	assert ! (node instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
	super.nodeExpanded(node);
    }

    // interface NodeActionsProvider
    @Override
    public void performDefaultAction(Object o) throws UnknownTypeException {
	assert !(o instanceof Watch);
        if (o instanceof EmptyWatch) {
            NEW_WATCH_ACTION.actionPerformed(null);
        } else if (o instanceof ShowMoreMessage) {
            ((ShowMoreMessage) o).getMore();
        }
	// no-op
	// LATER: super.performDefaultAction(o);
    }

    public /* TMP */ static final Action DELETE_ACTION = Models.createAction (
	Catalog.get("ACT_WATCH_Delete"), // NOI18N
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return !(node instanceof EmptyWatch);
            }
            @Override
            public void perform (final Object[] nodes) {
		if (!SwingUtilities.isEventDispatchThread()) {
		    SwingUtilities.invokeLater(new Runnable() {
                        @Override
			public void run() {
			    perform(nodes);
			}
		    } );
		    return;
		}

                int i, k = nodes.length;
                for (i = 0; i < k; i++)
		    if (nodes [i] instanceof WatchVariable) {
	    		WatchVariable w = (WatchVariable) nodes [i];
	    		w.getNativeWatch().postDelete(false);
		    } else
			((Watch) nodes [i]).remove ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    static {
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE") // NOI18N
        );
    };

    private static abstract class CheckBoxAction extends AbstractAction implements Presenter.Popup {

        private JCheckBoxMenuItem popupItem;

        CheckBoxAction(String name, String id) {
            super(name);
            putValue("WatchActionId", id);  // NOI18N
        }

        @Override
        public final void actionPerformed(ActionEvent e) {
            setSelected(!isSelected());
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (popupItem == null) {
                popupItem = new JCheckBoxMenuItem();
                popupItem.setSelected(isSelected());
                Actions.connect(popupItem, this, true);
            }
            return popupItem;
        }

        protected abstract boolean isSelected();

        protected abstract void setSelected(boolean selected);

    }
    public static final Action SHOW_PINNED_WATCHES_ACTION = new CheckBoxAction
        (NbBundle.getBundle(WatchModel.class).getString("CTL_WatchAction_ShowPinned"),
         "showPinned") {//NOI18N
            @Override protected boolean isSelected() {
                return showPinnedWatches.isShowPinnedWatches();
            }

            @Override protected void setSelected(boolean selected) {
                showPinnedWatches.setShowPinnedWatches(selected);
            }
    };    
    
    public static final Action NEW_WATCH_ACTION = new AbstractAction
        (Catalog.get("ACT_WATCH_NewWatch")) { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                DebuggerEngine engine = org.netbeans.api.debugger.DebuggerManager.getDebuggerManager().getCurrentEngine();
                    if (engine != null) {
                        engine.getActionsManager().doAction(ActionsManager.ACTION_NEW_WATCH);
                    }
            }
    };


    // interface NodeActionsProvider
    @Override
    public Action[] getActions(Object o) throws UnknownTypeException {
	assert ! (o instanceof NativeWatch) : 
	       "WatchModel.get*(): got a NativeWatch"; // NOI18N
       EngineDescriptor desp = debugger.getNDI().getEngineDescriptor();
        boolean canDoMaxObject = desp.hasCapability(EngineCapability.MAX_OBJECT);
        boolean canDoDy = desp.hasCapability(EngineCapability.DYNAMIC_TYPE);
        boolean canDoIn = desp.hasCapability(EngineCapability.INHERITED_MEMBERS);
        boolean canDoSt = desp.hasCapability(EngineCapability.STATIC_MEMBERS);
	boolean canDoPP = desp.hasCapability(EngineCapability.PRETTY_PRINT);

	if (o == TreeModel.ROOT) {
	    return new Action[] {
		NEW_WATCH_ACTION,
                SHOW_PINNED_WATCHES_ACTION,
		null,
		new DeleteAllAction(),
		null,
                canDoIn ? Action_INHERITED_MEMBERS : null,
                canDoDy ? Action_DYNAMIC_TYPE : null,
                canDoSt ? Action_STATIC_MEMBERS : null,
		canDoPP ? Action_PRETTY_PRINT : null,
                null,
                canDoMaxObject ? SystemAction.get(MaxObjectAction.class) : null,
		null,
	    };

	} else if (o instanceof Watch) {
	    return new Action[] {
                NEW_WATCH_ACTION,
                SHOW_PINNED_WATCHES_ACTION,
		null,
		DELETE_ACTION,
		new DeleteAllAction(),
		null,
                canDoIn ? Action_INHERITED_MEMBERS : null,
                canDoDy ? Action_DYNAMIC_TYPE : null,
                canDoSt ? Action_STATIC_MEMBERS : null,
		canDoPP ? Action_PRETTY_PRINT : null,
                null,
                canDoMaxObject ? SystemAction.get(MaxObjectAction.class) : null,
		null,
	    };

	} else if (o instanceof Variable) {
	    Variable v = (Variable) o;
	    return v.getActions(true);

	} else if (o instanceof EmptyWatch) {
            return new Action[]{SHOW_PINNED_WATCHES_ACTION};
        } else {
	    throw new UnknownTypeException(o);
	}
    }

    // interface TreeModel etc
    @Override
    public void addModelListener(ModelListener l) {
	if (super.addModelListenerHelp(l) && debugger != null)
	    debugger.registerWatchModel(this);
    }

    // interface TreeModel etc
    @Override
    public void removeModelListener(ModelListener l) {
	if (super.removeModelListenerHelp(l) && debugger != null)
	    debugger.registerWatchModel(null);
    }

    // inner class Actions .....................................................

    /* TMP */ public static class DeleteAllAction extends AbstractAction {

	public DeleteAllAction() {
	    super(Catalog.get("ACT_WATCH_DeleteAll"));    // NOI18N
	    setEnabled(true);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    watchBag().postDeleteAllWatches();
	}

	@Override
	public boolean isEnabled() {
	    Object [] watches = watchBag().getWatches();
	    return watches.length > 0;
	}
    }

/*
    private static class DeleteAction extends Action {
	private NativeWatch watch;

	DeleteAction(NativeWatch watch) {
	    super(Catalog.get("ACT_WATCH_Delete"));       // NOI18N
	    this.watch = watch;
	    setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
	    // we don't spread because postDelete explicitly iterates through
	    // it's children.

	    final boolean spread = false;
	    watch.postDelete(spread);
	}
    }
    */

/*
 * wait for glue changes to support (-r) (-d) for watches
 *
    private static class DynamicTypeAction extends AbstractAction {
	private NativeDebugger debugger;
	private Variable watch;

	DynamicTypeAction(NativeDebugger debugger, Variable watch) {
	    super(Catalog.get("ACT_WATCH_Dynamic"));       // NOI18N
	    this.debugger = debugger;
	    this.watch = watch;
	    setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
	    debugger.postDynamicWatch(watch);
	}
    }

    private static class InheritedMembersAction extends AbstractAction {
	private NativeDebugger debugger;
	private Variable watch;

	InheritedMembersAction(NativeDebugger debugger, Variable watch) {
	    super(Catalog.get("ACT_WATCH_Inherited"));
	    this.debugger = debugger;
	    this.watch = watch;
	    setEnabled(true);
	}

	public void actionPerformed(ActionEvent e) {
	    debugger.postInheritedWatch(watch);
	}
    }
*/

    /**
     * An item displayed at the end of watches that can be used to enter new watch expressions.
     */
    static class EmptyWatch {
        public void setExpression(String expr) {
            String infoStr = Catalog.get("CTL_WatchesModel_Empty_Watch_Hint"); //NOI18N
            infoStr = "<" + infoStr + ">"; // NOI18N
            if (expr == null || expr.trim().length() == 0 || infoStr.equals(expr)) {
                return; // cancel action
            }
            NativeDebuggerManager.get().createWatch(expr.trim());

//            Vector v = (Vector) listeners.clone ();
//            int i, k = v.size ();
//            for (i = 0; i < k; i++)
//                ((ModelListener) v.get (i)).modelChanged (
//                    new ModelEvent.NodeChanged (WatchesTreeModel.this, EmptyWatch.this)
//                );
        }
    }
    
    static class ShowPinnedWatches {

        boolean isShowPinnedWatches() {
            return PROPERTIES.getBoolean(PROP_SHOW_PINNED_WATCHES, false);
        }

        boolean isShowPinnedWatchesEnabled() {
            return true;//areAnyPinnedWatches;
        }

        void setShowPinnedWatches(boolean showPinnedWatches) {
            PROPERTIES.setBoolean(PROP_SHOW_PINNED_WATCHES, showPinnedWatches);
            //fire action
            
        }

    }    
}
