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
package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.utils.props.Property;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import java.util.Date;
import java.util.ArrayList;
import java.text.DateFormat;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.concurrent.Executor;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.BooleanStateAction;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.CheckNodeModelFilter;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.DialogManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.ModelListenerSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.Constants;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.NewBreakpointAction;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Registered,
 * OLD>
 * for non-global breakpoints, under
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/BreakpointsView/
 *	META-INF/debugger/netbeans-GdbDebuggerEngine/BreakpointsView/
 * <OLD
 * and for global breakpoints under
 *	META-INF/debugger/BreakpointsView/
 * in
 *	org.netbeans.spi.viewmodel.TreeModelFilter
 *	org.netbeans.spi.viewmodel.TableModelFilter
 *	org.netbeans.spi.viewmodel.NodeModelFilter
 *	org.netbeans.spi.viewmodel.NodeActionsProviderFilter
 *	org.netbeans.spi.viewmodel.CheckNodeModelFilter
 */
public final class BreakpointFilter extends ModelListenerSupport
		implements TreeModelFilter, TableModelFilter,
		NodeActionsProviderFilter, CheckNodeModelFilter,
		AsynchronousModelFilter,
		Constants {

	private static NativeDebuggerManager manager() {
		return NativeDebuggerManager.get();
	}

	/**
	 * This constructor is called when instantiated globally
	 */
	public BreakpointFilter() {
		super("breakpoint");	// NOI18N
	}

	private static BreakpointBag breakpointBag() {
		return manager().breakpointBag();
	}

	// interface TreeModelFilter
        @Override
	public Object getRoot(TreeModel original) {
		// redundant?
		return TreeModel.ROOT;
	}

	private static NativeBreakpoint skipParent(NativeBreakpoint nb) {
		/*
		if (Log.Bpt.ghostbuster)
		System.out.printf(">skipParent(%s)\n", nb);
		 */
		NativeBreakpoint rv = null;

		if (!NativeBreakpoint.getSkipSingleParent()) {
			rv = nb;
		} else {
			Object[] children = sessionOnly(nb, nb.getChildren());
			if (children.length == 1) {
				NativeBreakpoint child = (NativeBreakpoint) children[0];
				if (!child.isBound() && child.isMidlevel() && child.isUniqueLite()) {
					rv = nb;		// don't skip
				} else {
					rv = skipParent(child);
				}
			} else {
				rv = nb;
			}
		}

		/*
		if (Log.Bpt.ghostbuster)
		System.out.printf("< %s\n", rv);
		 */
		return rv;
	}

	/**
	 * Return an array which contains only bpts belonging to the current
	 * session or all of them if there are no sessions.
	 */
	private static Object[] sessionOnly(NativeBreakpoint parent,
			Object[] children) {
		if (!NativeBreakpoint.getSessionOnly()) {
			return children;
		}

		if (parent == null) {
			if (NativeDebuggerManager.isPerTargetBpts()) {
				// list of top-level bpts which have a child in current session
				ArrayList<Object> newChildren = new ArrayList<Object>();
				for (Object child : children) {
					if (child instanceof NativeBreakpoint) {
						NativeBreakpoint nb = (NativeBreakpoint) child;
						Object[] inSession = sessionOnly(nb, nb.getChildren());
						if (inSession.length > 0) {
							newChildren.add(child);
						}
					}
				}
				return newChildren.toArray();
			} else {
				// list of top-level bpts
				return children;
			}
		}

		if (!parent.isToplevel()) {
			return children;
		}

		assert parent.isToplevel();

		if (NativeDebuggerManager.get().sessionCount() == 0) {
			// no sessions
			if (NativeDebuggerManager.isPerTargetBpts()) {
				return new Object[0];
			} else {
				return children;
			}
		}

		ArrayList<Object> newChildren = new ArrayList<Object>();
		for (Object child : children) {
			if (child instanceof NativeBreakpoint) {
				NativeBreakpoint nb = (NativeBreakpoint) child;
				if (nb.isCurrent()) {
					newChildren.add(child);
				}
			} else {
				newChildren.add(child);
			}
		}
		return newChildren.toArray();
	}

	/**
	 * If a child has itself only one child substitute the childs child
	 * for the child.
	<pre>
	Forest of the form:

	-- in main
	|-- in main			a.out
	|-- in main			a.out
	-- in maybeOverload
	|-- in maybeOverload		a.out
	|-- in maybeOverload()		a.out
	|-- in maybeOverload		b.out
	|-- in maybeOverload(X)		b.out
	|-- in maybeOverload(Y)		b.out
	|-- in maybeOverload(Z)		b.out

	Simplified to look like this:

	-- in main				a.out
	-- in maybeOverload
	|-- in maybeOverload		a.out
	|-- in maybeOverload		b.out
	|-- in maybeOverload(X)		b.out
	|-- in maybeOverload(Y)		b.out
	|-- in maybeOverload(Z)		b.out
	</pre>
	 */
	private static Object[] simplifyHierarchy(NativeBreakpoint parent,
			Object[] children) {
		children = sessionOnly(parent, children);
		if (Log.Bpt.model) {
			System.out.printf("BreakpointFilter.simplifyHierarchy():\n"); // NOI18N
			System.out.printf("\tafter sessionOnly: %d\n", children.length); // NOI18N
		}
		for (int ox = 0; ox < children.length; ox++) {
			Object o = children[ox];
			if (o instanceof NativeBreakpoint) {
				NativeBreakpoint child = (NativeBreakpoint) o;
				children[ox] = skipParent(child);
			}
		}
		if (Log.Bpt.model) {
			System.out.printf("\t       eventually: %d\n", children.length); // NOI18N
			if (children.length == 0) {
				System.out.printf("\t0 bpt hook\n"); // NOI18N
			}
		}
		return children;
	}

	// interface TreeModelFilter
        @Override
	public Object[] getChildren(TreeModel original, Object parent,
			int from, int to) throws UnknownTypeException {
            CndUtils.assertNonUiThread();
            
            breakpointBag();	// cause restoration of bpts

            // Factoring of original.getChildren() somehow messes up
            // isLeaf(), so don't.

            if (parent == TreeModel.ROOT) {
                // OLD Object originalChildren[] = original.getChildren(parent, from, to);
                Object[] oChildren = original.getChildren(parent,
                        0,
                        original.getChildrenCount(parent));
                return simplifyHierarchy(null, oChildren);

            } else {
                if (parent instanceof NativeBreakpoint) {
                    NativeBreakpoint nb = (NativeBreakpoint) parent;
                    return simplifyHierarchy(nb, nb.getChildren());
                } else {
                    Object originalChildren[] = original.getChildren(parent, from, to);
                    return originalChildren;
                }
            }
	}

	// interface TreeModelFilter
        @Override
	public int getChildrenCount(TreeModel original, Object parent)
			throws UnknownTypeException {
            CndUtils.assertNonUiThread();

            breakpointBag();	// cause restoration of bpts

            // dispense from global bag
            int count;
            if (parent instanceof NativeBreakpoint) {
                NativeBreakpoint nb = (NativeBreakpoint) parent;
                count = nb.nChildren();
                /* LATER
                 modelview seems to be absolutely insensitive to nChildren.
                 NativeBreakpoint onlyChild = nb.onlyChild();
                 if (onlyChild == null)
                 return nb.nChildren();
                 else
                 return 1;
                 */
            } else {
                if (NativeDebuggerManager.isPerTargetBpts()) {
                    Object[] oChildren = original.getChildren(parent,
                            0,
                            original.getChildrenCount(parent));
                    Object[] inSession = sessionOnly(null, oChildren);
                    count = inSession.length;
                } else {
                    count = original.getChildrenCount(parent);
                }
            }
            if (Log.Bpt.model) {
                System.out.printf("BreakpointFilter.getChildrenCount(%s) -> %d\n", parent, count); // NOI18N
            }
            return count;
	}

	/**
	 * Should this bpt be presented as a leaf in the bpt view?
	 */
	private boolean isLeaf(NativeBreakpoint nb) {
		// Not a method of NativeBreakpoint because the notion of leaf
		// in the bpt view is different then the leafness of the three-level
		// bpt tree.
		nb = skipParent(nb);
		return nb.nChildren() == 0;
	}

	// interface TreeModelFilter
        @Override
	public boolean isLeaf(TreeModel original, Object node)
			throws UnknownTypeException {

		boolean isLeaf = false;
		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint nb = (NativeBreakpoint) node;
			return isLeaf(nb);

		} else {
			isLeaf = original.isLeaf(node);
			if (Log.Bpt.model) {
				String who = (node == TreeModel.ROOT) ? "ROOT" : "notours"; // NOI18N
				System.out.printf("isLeaf(%s) = %s\n", who, isLeaf); // NOI18N
			}
		}
		return isLeaf;
	}

	// interface TreeModelFilter
        @Override
	public void addModelListener(ModelListener l) {
		if (super.addModelListenerHelp(l)) {
			manager().breakpointUpdater().addListener(this);
		}
	}

	// interface TreeModelFilter etc
        @Override
	public void removeModelListener(ModelListener l) {
		if (super.removeModelListenerHelp(l)) {
			manager().breakpointUpdater().removeListener(l);
		}
	}

	// interface NodeModelFilter etc
        @Override
	public String getDisplayName(NodeModel original, Object node)
			throws UnknownTypeException {

		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint b = (NativeBreakpoint) node;
			return b.getDisplayName();
		} else {
			return original.getDisplayName(node);
		}
	}

	// interface NodeModelFilter
        @Override
	public String getIconBase(NodeModel original, Object node)
			throws UnknownTypeException {

		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint b = (NativeBreakpoint) node;
			return b.getIconBase();
		} else {
			return original.getIconBase(node);
		}
	}

	// interface NodeModelFilter
        @Override
	public String getShortDescription(NodeModel original, Object node)
			throws UnknownTypeException {

		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint b = (NativeBreakpoint) node;
			String summary = ( b.getSummary() == null ? "" : b.getSummary() );
			if (b.getError() != null) {
				summary += " (" + b.getError() + ")";	// NOI18N
			}
			return summary;
		} else {
			return original.getShortDescription(node);
		}
	}

	// interface TableModelFilter
        @Override
	public Object getValueAt(TableModel original, Object node, String columnID)
			throws UnknownTypeException {
		if (!(node instanceof NativeBreakpoint)) /* OLD, columnID may not honored by original
		return original.getValueAt(node, columnID);
		 */ {
			return null;
		}

		NativeBreakpoint b = (NativeBreakpoint) node;

		if (columnID.equals(PROP_BREAKPOINT_TIMESTAMP)) {
			Date timestamp = b.timestamp();
			if (timestamp == null) {
				return "";
			} else {
				return DateFormat.getTimeInstance(DateFormat.LONG).
						format(timestamp);
			}
		}

		Property p = b.propertyByKey(columnID);

		if (Log.Bpt.enabling) {
			if (columnID.equals(PROP_BREAKPOINT_ENABLE)) {
				if (b.isToplevel()) {
					System.out.println("?T getValueAt() ->" + p); // NOI18N
				} else {
					System.out.println("? S getValueAt() ->" + p); // NOI18N
				}
			}
		}

		if (p != null) {
			if (columnID.equals(PROP_BREAKPOINT_CONTEXT)) {
				return b.embellishedContext(p.toString());
			} else {
				return p.getAsObject();
			}
		} else {
			return null;
		}
	}

	// interface TableModelFilter
        @Override
	public boolean isReadOnly(TableModel original, Object node, String columnID)
			throws UnknownTypeException {

		if (!(node instanceof NativeBreakpoint)) /* OLD  columnID can't be honored by original
		return original.isReadOnly(node, columnID);
		 */ {
			return true;
		}

		NativeBreakpoint b = (NativeBreakpoint) node;
		Property p = b.propertyByKey(columnID);
		if (p != null) {
			return p.isReadOnly();
		} else {
			return true;
		}
	}

	// interface TableModelFilter
        @Override
	public void setValueAt(TableModel original, Object node,
			String columnID, Object value)
			throws UnknownTypeException {

		if (!(node instanceof NativeBreakpoint)) {
			/* OLD  columnID can't be honored by original
			original.setValueAt(node, columnID, value);
			 */
			return;
		}

		NativeBreakpoint b = (NativeBreakpoint) node;
		Property p = b.propertyByKey(columnID);
		if (p != null) {
			// don't set property here yet,
			// we should wait until the engine sends back a success msg,
			// Handler will take care of it.
			// If it fail, we would have a chance to PropUndo.undo it
			// p.setFromObject(value);
		} else {
			System.out.println("BreakpointFilter.setValueAt Property " + p); // NOI18N
		}

		if (PROP_BREAKPOINT_ENABLE.equals(columnID)) {
			assert false : "setValueAt() should nto be called for PROP_BREAKPOINT_ENABLE";
			/* OLD
			p.setFromObject(value);
			b.setPropEnabled(((BooleanProperty)p).get());
			 */
		} else if (PROP_BREAKPOINT_TEMP.equals(columnID)) {
			p.setFromObject(value);
			b.setPropTemp(((BooleanProperty) p).get());
		} else if (PROP_BREAKPOINT_JAVA.equals(columnID)) {
			p.setFromObject(value);
			b.setPropJava(((BooleanProperty) p).get());
		} else if (PROP_BREAKPOINT_THREAD.equals(columnID)) {
			b.setPropThread((String) value);
			//b.setPropThread(((StringProperty)p).get());
		} else if (PROP_BREAKPOINT_LWP.equals(columnID)) {
			b.setPropLwp((String) value);
			//b.setPropLwp(((StringProperty)p).get());
		} else if (PROP_BREAKPOINT_WHILEIN.equals(columnID)) {
			b.setPropWhileIn((String) value);
			//b.setPropWhileIn(((StringProperty)p).get());
		} else if (PROP_BREAKPOINT_CONDITION.equals(columnID)) {
			b.setPropCondition((String) value);
			//b.setPropCondition(((StringProperty)p).get());
		} else if (PROP_BREAKPOINT_COUNTLIMIT.equals(columnID)) {
			b.setPropCountLimit(value);
		}
	}

	// interface CheckNodeModelFilter
        @Override
	public boolean isCheckable(NodeModel originalNM, Object node) throws UnknownTypeException {
		if (!(originalNM instanceof CheckNodeModel)) {
			throw new UnknownTypeException(node);
		}
		CheckNodeModel original = (CheckNodeModel) originalNM;
		if (node instanceof NativeBreakpoint) {
			return true;
		} else {
			return original.isCheckable(node);
		}
	}

	// interface CheckNodeModelFilter
        @Override
	public boolean isCheckEnabled(NodeModel originalNM, Object node) throws UnknownTypeException {
		if (!(originalNM instanceof CheckNodeModel)) {
			throw new UnknownTypeException(node);
		}
		CheckNodeModel original = (CheckNodeModel) originalNM;
		if (node instanceof NativeBreakpoint) {
			return true;
		} else {
			return original.isCheckEnabled(node);
		}
	}

	// interface CheckNodeModelFilter
        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL") // This method is allowed to return null in case the state is unknown
	public Boolean isSelected(NodeModel originalNM, Object node) throws UnknownTypeException {
		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint b = (NativeBreakpoint) node;
			Property p = b.propertyByKey(PROP_BREAKPOINT_ENABLE);

			if (Log.Bpt.enabling) {
				if (b.isToplevel()) {
					System.out.println("?T isSelected() ->" + p); // NOI18N
				} else {
					System.out.println("? S isSelected() ->" + p); // NOI18N
				}
			}

			if (p != null) {
				Object bObj = p.getAsObject();
				if (bObj instanceof Boolean) {
					return (Boolean) bObj;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			if (!(originalNM instanceof CheckNodeModel)) {
				throw new UnknownTypeException(node);
			}
			CheckNodeModel original = (CheckNodeModel) originalNM;
			return original.isSelected(node);
		}
	}

	// interface CheckNodeModelFilter
        @Override
	public void setSelected(NodeModel originalNM, Object node, Boolean selected) throws UnknownTypeException {
		if (node instanceof NativeBreakpoint) {
			NativeBreakpoint b = (NativeBreakpoint) node;
			Property p = b.propertyByKey(PROP_BREAKPOINT_ENABLE);
			if (p != null) {
				p.setFromObject(selected);
				b.setPropEnabled(((BooleanProperty) p).get());
			}
		} else {
			if (!(originalNM instanceof CheckNodeModel)) {
				throw new UnknownTypeException(node);
			}
			CheckNodeModel original = (CheckNodeModel) originalNM;
			original.setSelected(node, selected);
		}
	}

	// interface AsynchronousModelFilter
        @Override
	public Executor asynchronous(Executor arg0, CALL arg1, Object arg2) throws UnknownTypeException {
		// IZ 172060 Make calls come in on the EDT.
            switch (arg1) {
                case CHILDREN:
                case VALUE:
                    return AsynchronousModelFilter.DEFAULT;
                case DISPLAY_NAME:
                case SHORT_DESCRIPTION:
                    return AsynchronousModelFilter.CURRENT_THREAD;
                default:
                    assert false;
                    return null;
            }
	}

	//
	// Action stuff
	//
	/**
	 * Common code for breakpoint group actions
	 */
	public abstract static class BptAction extends AbstractAction {

		protected BptAction(String name) {
			super(name);
		}

		protected NativeDebuggerManager manager() {
			return NativeDebuggerManager.get();
		}
	}

	/**
	 * Common code for breakpoint instance actions
	 */
	private abstract static class BptActionPerformer
			implements Models.ActionPerformer {

		protected NativeDebuggerManager manager() {
			return NativeDebuggerManager.get();
		}

		// interface Models.ActionPerformer
                @Override
		public boolean isEnabled(Object node) {
			if (!(node instanceof NativeBreakpoint)) {
				return false;
			}
			NativeBreakpoint b = (NativeBreakpoint) node;
			return isEnabled(b);
		}

		/**
		 * Call perform(NativeBreakpoint) on every passed object that is a Bpt
		 */
		// interface Models.ActionPerformer
                @Override
		public void perform(Object[] nodes) {
			for (int nx = 0; nx < nodes.length; nx++) {
				if (!(nodes[nx] instanceof NativeBreakpoint)) {
					continue;
				}
				NativeBreakpoint b = (NativeBreakpoint) nodes[nx];
				perform(b);
			}
		}

		protected abstract void perform(NativeBreakpoint b);

		protected abstract boolean isEnabled(NativeBreakpoint b);
	}

	private static class DeleteAction extends BptActionPerformer {

		DeleteAction() {
			/* How do you do this if we're not related to Actions?
			putValue(Action.ACCELERATOR_KEY,
			KeyStroke.getKeyStroke("DELETE"));	// NOI18N
			 */
		}

                @Override
		public boolean isEnabled(NativeBreakpoint b) {
			return true;
		}

                @Override
		protected void perform(NativeBreakpoint b) {
			b.dispose();
		}
	}
	private static final Action DELETE_ACTION =
			Models.createAction(Catalog.get("ACT_BPT_Delete"), // NOI18N
			new DeleteAction(),
			Models.MULTISELECTION_TYPE_ANY);

	static {
		DELETE_ACTION.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke("DELETE"));    // NOI18N
	}

	private static class EnableAction extends BptActionPerformer {

		EnableAction() {
			/* Grrr how do you do this if we're not related to Actions?
			putValue(Action.ACCELERATOR_KEY,
			KeyStroke.getKeyStroke("DELETE"));	// NOI18N
			 */
		}

                @Override
		public boolean isEnabled(NativeBreakpoint b) {
			return true;
		}

                @Override
		protected void perform(NativeBreakpoint b) {
			if (b.isEnabled()) {
				b.disable();
			} else {
				b.enable();
			}
		}
	}
	private static final Action ENABLE_ACTION =
			Models.createAction(Catalog.get("ACT_BPT_Enable"), // NOI18N
			new EnableAction(),
			Models.MULTISELECTION_TYPE_ANY);
	private static final Action DISABLE_ACTION =
			Models.createAction(Catalog.get("ACT_BPT_Disable"), // NOI18N
			new EnableAction(),
			Models.MULTISELECTION_TYPE_ANY);

	private static class CustomizeAction extends BptActionPerformer {

                @Override
		public boolean isEnabled(NativeBreakpoint b) {
			return true;
		}

                @Override
		protected void perform(NativeBreakpoint b) {
			NativeBreakpoint editableBreakpoint;
			editableBreakpoint = b.makeEditableCopy();
			CustomizeBreakpointProcessor processor =
					new CustomizeBreakpointProcessor(editableBreakpoint);
			processor.setVisible(true);
		}
	}
	private static final Action CUSTOMIZE_ACTION =
			Models.createAction(Catalog.get("ACT_BPT_Customize"), // NOI18N
			new CustomizeAction(),
			Models.MULTISELECTION_TYPE_EXACTLY_ONE);

	private static class GoToSourceAction extends BptActionPerformer {

                @Override
		public boolean isEnabled(NativeBreakpoint b) {
			return b.isVisitable();
		}

                @Override
		protected void perform(NativeBreakpoint b) {
			b.visitNextAnnotation();
		}
	}
	private static final Action GOTO_SOURCE_ACTION =
			Models.createAction(Catalog.get("ACT_BPT_GoToSource"), // NOI18N
			new GoToSourceAction(),
			Models.MULTISELECTION_TYPE_EXACTLY_ONE);

	public static class EnableAllAction extends BptAction {

		EnableAllAction() {
			super(Catalog.get("ACT_BPT_EnableAll"));	// NOI18N
		}

                @Override
		public void actionPerformed(ActionEvent e) {
			breakpointBag().postEnableAllHandlers(true);
		}

		@Override
		public boolean isEnabled() {
			return breakpointBag().anyDisabled();
		}

		public static Action getAction() {
			return ENABLE_ALL_ACTION;
		}
	}
	private static final Action ENABLE_ALL_ACTION = new EnableAllAction();

	public static class DisableAllAction extends BptAction {

		DisableAllAction() {
			super(Catalog.get("ACT_BPT_DisableAll"));	// NOI18N
		}

                @Override
		public void actionPerformed(ActionEvent e) {
			breakpointBag().postEnableAllHandlers(false);
		}

		@Override
		public boolean isEnabled() {
			return breakpointBag().anyEnabled();
		}

		public static Action getAction() {
			return DISABLE_ALL_ACTION;
		}
	}
	private static final Action DISABLE_ALL_ACTION = new DisableAllAction();

	public static class DeleteAllAction extends BptAction {

		DeleteAllAction() {
			super(Catalog.get("ACT_BPT_DeleteAll"));	// NOI18N
		}

                @Override
		public void actionPerformed(ActionEvent e) {
			breakpointBag().postDeleteAllHandlers();
		}

		@Override
		public boolean isEnabled() {
			Object[] breakpoints = breakpointBag().getBreakpoints();
			return breakpoints.length > 0;
		}

		public static Action getAction() {
			return DELETE_ALL_ACTION;
		}
	}
	private static final Action DELETE_ALL_ACTION = new DeleteAllAction();

	public static class DeleteInapplicableAction extends BptAction {

		DeleteInapplicableAction() {
			super(Catalog.get("ACT_BPT_DeleteInapplicable"));	// NOI18N
		}

                @Override
		public void actionPerformed(ActionEvent e) {
			NativeBreakpoint[] bpts = breakpointBag().getBreakpoints();
			for (NativeBreakpoint b : bpts) {
				assert b.isToplevel();
				NativeDebugger d = NativeDebuggerManager.get().currentDebugger();
				NativeBreakpoint m = b.getMidlevelFor(d);
				if (m != null && m.isBroken()) {
					m.dispose();
				}
			}
		}

		@Override
		public boolean isEnabled() {

			if (NativeDebuggerManager.get().sessionCount() == 0) {
				// no sessions
				return false;

			} else {
				// Only enabel if we at least one broken bpt in this session
				NativeBreakpoint[] bpts = breakpointBag().getBreakpoints();
				for (NativeBreakpoint b : bpts) {
					assert b.isToplevel();
					NativeDebugger d = NativeDebuggerManager.get().currentDebugger();
					NativeBreakpoint m = b.getMidlevelFor(d);
					if (m != null && m.isBroken()) {
						return true;
					}
				}
				return false;
			}
		}

		public static Action getAction() {
			return DELETE_INAPPLICABLE_ACTION;
		}
	}
	private static final Action DELETE_INAPPLICABLE_ACTION =
			new DeleteInapplicableAction();

	public static class SaveBptAction extends BooleanStateAction {

		OptionSet globalOptions;

		SaveBptAction() {
			globalOptions = NativeDebuggerManager.get().globalOptions();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DebuggerOption.SAVE_BREAKPOINTS.setEnabled(new OptionLayers(globalOptions), !getBooleanState());
		}

		// interface BooleanStateAction
		@Override
		public boolean getBooleanState() {
			return DebuggerOption.SAVE_BREAKPOINTS.isEnabled(globalOptions);
		}

		// interface SystemAction
                @Override
		public String getName() {
			return Catalog.get("ACT_BPT_SaveBpt"); // NOI18N
		}

		// interface SystemAction
                @Override
		public HelpCtx getHelpCtx() {
			return null;
		}
	}
	private static final Action SAVE_ACTION = new SaveBptAction();
	private static final Action NEW_BREAKPOINT_ACTION =
			// OLD SystemAction.get(AddBreakpointAction.class);	// debuggercores
			SystemAction.get(NewBreakpointAction.class);	// ours

	private static class GhostBusterAction extends BooleanStateAction {

		GhostBusterAction() {
		}

		// interface BooleanStateAction
		@Override
		public boolean getBooleanState() {
			return NativeBreakpoint.getGhostBuster();
		}

		// interface SystemAction
                @Override
		public String getName() {
			return "ghost buster"; // NOI18N
		}

		// interface SystemAction
                @Override
		public HelpCtx getHelpCtx() {
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			NativeBreakpoint.toggleGhostBuster();
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}
	private static final Action GHOST_BUSTER_ACTION =
			new GhostBusterAction();

	private static class SessionOnlyAction extends BooleanStateAction {

		SessionOnlyAction() {
		}

		// interface BooleanStateAction
		@Override
		public boolean getBooleanState() {
			return NativeBreakpoint.getSessionOnly();
		}

		// interface SystemAction
                @Override
		public String getName() {
			return "only show bpts in current session"; // NOI18N
		}

		// interface SystemAction
                @Override
		public HelpCtx getHelpCtx() {
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			NativeBreakpoint.toggleSessionOnly();
		}
	}
	private static final Action SESSION_ONLY_ACTION =
			new SessionOnlyAction();

	private static class OnlyChildAction extends BooleanStateAction {

		OnlyChildAction() {
		}

		// interface BooleanStateAction
		@Override
		public boolean getBooleanState() {
			return NativeBreakpoint.getSkipSingleParent();
		}

		// interface SystemAction
                @Override
		public String getName() {
			return "skip single parents"; // NOI18N
		}

		// interface SystemAction
                @Override
		public HelpCtx getHelpCtx() {
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			NativeBreakpoint.toggleSkipSingleParent();
		}
	}
	private static final Action ONLY_CHILD_ACTION =
			new OnlyChildAction();

	private static class EnableDifferentiatesAction extends BooleanStateAction {

		EnableDifferentiatesAction() {
		}

		// interface BooleanStateAction
		@Override
		public boolean getBooleanState() {
			return NativeBreakpoint.enableDifferentiates;
		}

		// interface SystemAction
                @Override
		public String getName() {
			return "enabled property differentiates"; // NOI18N
		}

		// interface SystemAction
                @Override
		public HelpCtx getHelpCtx() {
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			NativeBreakpoint.enableDifferentiates =
					!NativeBreakpoint.enableDifferentiates;
		}
	}
	private static final Action ENABLE_DIFFERENTIATES_ACTION =
			new EnableDifferentiatesAction();

	private void tackOnDebugging(ArrayList<Action> a) {
		if (Log.Bpt.hierarchy) {
			a.add(null);
			a.add(GHOST_BUSTER_ACTION);
			a.add(SESSION_ONLY_ACTION);
			a.add(ONLY_CHILD_ACTION);
			a.add(ENABLE_DIFFERENTIATES_ACTION);
			a.add(null);
		}
	}

	// interface NodeActionsProviderFilter
        @Override
	public Action[] getActions(NodeActionsProvider original, Object o)
			throws UnknownTypeException {

		final Action originals[] = original.getActions(o);
		final ArrayList<Action> a = new ArrayList<Action>(originals.length);

		if (NativeDebuggerManager.isPerTargetBpts()) {
			if (o == TreeModel.ROOT) {

				// global actions
				a.add(NEW_BREAKPOINT_ACTION);
				a.add(null);
				a.add(ENABLE_ALL_ACTION);
				a.add(DISABLE_ALL_ACTION);
				a.add(DELETE_ALL_ACTION);
				a.add(SAVE_ACTION);
				a.add(null);
				tackOnDebugging(a);

			} else if (o instanceof NativeBreakpoint) {
				// item-specific actions
				a.add(GOTO_SOURCE_ACTION);
				a.add(null);
				a.add(((NativeBreakpoint)o).isEnabled() ?
					DISABLE_ACTION : ENABLE_ACTION);
				a.add(DELETE_ACTION);
				a.add(CUSTOMIZE_ACTION);
				a.add(null);

				// global actions
				a.add(NEW_BREAKPOINT_ACTION);
				a.add(null);
				a.add(ENABLE_ALL_ACTION);
				a.add(DISABLE_ALL_ACTION);
				a.add(DELETE_ALL_ACTION);
				a.add(SAVE_ACTION);
				a.add(null);

				tackOnDebugging(a);

			} else {
				for (int ox = 0; ox < originals.length; ox++) {
					a.add(originals[ox]);
				}
			}
			return a.toArray(new Action[a.size()]);

		} else {

			for (int ox = 0; ox < originals.length; ox++) {
				if (ox != 1) // Temp solution to hide "Move into group" action
				{
					a.add(originals[ox]);
				}
			}

			if (!(o instanceof NativeBreakpoint)) {
				// includes TreeModel.ROOT

				a.add(a.size() - 1, DELETE_INAPPLICABLE_ACTION);

				tackOnDebugging(a);

				//
				// for IDE start model, this is not applicable
				// refer to codes in ProfileBridge SAVE_BREAKPOINTS
				//
				if (NativeDebuggerManager.isStandalone()) {
					a.add(SAVE_ACTION);
				}

			} else {
				// Add at top
				a.add(0, GOTO_SOURCE_ACTION);
				a.add(1, null);

				a.add(a.size() - 1, DELETE_INAPPLICABLE_ACTION);
				a.add(CUSTOMIZE_ACTION);

				tackOnDebugging(a);
			}
			return a.toArray(new Action[a.size()]);
		}
	}

	// interface NodeActionsProviderFilter
        @Override
	public void performDefaultAction(NodeActionsProvider original, Object o)
			throws UnknownTypeException {

		if (o == TreeModel.ROOT) {
			// noop
		} else if (o instanceof NativeBreakpoint) {
			NativeBreakpoint breakpoint = (NativeBreakpoint) o;
			breakpoint.visitNextAnnotation();
		} else {
			original.performDefaultAction(o);
		}
	}

	// innerclasses ............................................................
	private static class CustomizeBreakpointProcessor extends DialogManager
			implements ActionListener, PropertyChangeListener {

		private final DialogDescriptor dd;
		private final EditBreakpointPanel panel;
		private Dialog dialog;

		CustomizeBreakpointProcessor(NativeBreakpoint editableBreakpoint) {

			panel = new EditBreakpointPanel(editableBreakpoint);
			boolean isModal = true;
			final String title = Catalog.get("CTL_CustomizeBreakpointTitle"); // NOI18N
			dd = new DialogDescriptor(panel, title, isModal, this);

			Object[] buttons = new Object[]{
				DialogDescriptor.OK_OPTION,
				DialogDescriptor.CANCEL_OPTION
			};
			dd.setOptions(buttons);
			// We will close the dialog ourselves.
			// // This so it's still there if we pop up an error.
			// OLD dd.setClosingOptions(buttons);
			dd.setClosingOptions(new Object[0]);
			dialog = DialogDisplayer.getDefault().createDialog(dd);
			dialog.pack();

			setValid();

			Controller controller = panel.getController();
			if (controller != null) {
				controller.addPropertyChangeListener(this);
			}
			// LATER panel.addPropertyChangeListener(this);
		}

		public void setVisible(boolean v) {
			dialog.setVisible(v);
		}

		/**
		 * Called when some dialog button is pressed
		 */
		// interface ActionListener
                @Override
		public void actionPerformed(ActionEvent e) {
			boolean ok = (e.getSource() == DialogDescriptor.OK_OPTION);
			accept(ok);
		}

		// interface DialogManager
                @Override
		public void accept(boolean yes) {
			boolean done = false;
			Controller controller = panel.getController();
			if (yes) {
				NativeDebuggerManager.get().registerDialog(this);
				done = controller.ok();
				// keep up until bringDown is called externally.
			} else {
				done = controller.cancel();
				bringDown();
			}
		}

		// interface DialogManager
                @Override
		public void bringDown() {
			NativeDebuggerManager.get().deRegisterDialog(this);
			dialog.setVisible(false);
			dialog.dispose();
			dialog = null;
		}

		// interface DialogManager
                @Override
		public void refocus() {
                    // do nothing
		}

		// interface PropertyChangeListener
                @Override
		public void propertyChange(PropertyChangeEvent e) {
			if (Controller.PROP_VALID.equals(e.getPropertyName())) {
				setValid();
			}
		}

		void setValid() {
			dd.setValid(panel.getController().isValid());
		}
	}
}
