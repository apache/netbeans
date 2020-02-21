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

import java.util.concurrent.Executor;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.SwingConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.JRadioButtonMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.Transferable;

import org.openide.util.HelpCtx;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.actions.Presenter;

import org.openide.awt.Actions;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;

import org.netbeans.api.debugger.Watch;

import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.NodeModel;


/**
 * Factoring of WatchModel and LocalModel.
 * Even though we implement many of the viewmodel interfaces the registration
 * of them as services in META-INF is done in terms of WatchModel and
 * LocalModel.
 */

public abstract class VariableModel extends ModelListenerSupport
    implements TreeModel, ExtendedNodeModelFilter, TableModel, TreeExpansionModel, AsynchronousModelFilter {

    protected NativeDebugger debugger;

    @Override
    public Executor asynchronous(Executor original, AsynchronousModelFilter.CALL asynchCall, Object node) {
        // for now let's use synchronious model (in EDT)
        // TODO: NativeDebugger or ((Variable)node) should be responsible for providing Threading Model,
        // because different engines could use different threading models
        return AsynchronousModelFilter.CURRENT_THREAD;
    }

    protected VariableModel(ContextProvider ctx) {
	super("variable");	// NOI18N
	debugger = ctx.lookupFirst(null, NativeDebugger.class);
    }

    protected VariableModel() {
	super("variable");	// NOI18N
    }


    /**
     * Return true if we're the locals view.
     */

    protected abstract boolean isLocal();


    // interface TreeModel
    @Override
    public Object getRoot() {
	// redundant?
	return ROOT;
    }

    // interface TreeModel
    @Override
    public abstract Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException;

    // interface TreeModel
    @Override
    public abstract int getChildrenCount(Object parent) throws UnknownTypeException;

    // interface TreeModel
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    return v.isLeaf();
	}
	throw new UnknownTypeException (node);
    }



    // interface NodeModel
    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return Catalog.get("PROP_name"); // NOI18N
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    return v.getVariableName();
	} else if (node instanceof ShowMoreMessage) {
            return  ((ShowMoreMessage) node).getMessage();
        } else {
	    throw new UnknownTypeException(node);
	}
    }

    private static final String ICON_PATH =
	"org/netbeans/modules/cnd/debugger/common2/icons/";	// NOI18N

    private static final String ICON_FIELD = ICON_PATH + "field"; // NOI18N
    private static final String ICON_FIELD_STATIC = ICON_FIELD + "_static"; // NOI18N
    private static final String ICON_FIELD_STATIC_PTR = ICON_FIELD_STATIC + "_pointer"; // NOI18N
    private static final String ICON_FIELD_PTR = ICON_FIELD + "_pointer"; // NOI18N

    private static final String ICON_LOCAL = ICON_PATH + "local"; // NOI18N
    private static final String ICON_LOCAL_STATIC = ICON_LOCAL + "_static"; // NOI18N
    private static final String ICON_LOCAL_STATIC_PTR = ICON_LOCAL_STATIC + "_pointer"; // NOI18N
    private static final String ICON_LOCAL_PTR = ICON_LOCAL + "_pointer"; // NOI18N

    // interface NodeModel
    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return null;

	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    if (v.isRoot()) {
		if (v.isStatic() && v.isPtr())
		    return ICON_LOCAL_STATIC_PTR;
		else if (v.isStatic())
		    return ICON_LOCAL_STATIC;
		else if (v.isPtr())
		    return ICON_LOCAL_PTR;
		else
		    return ICON_LOCAL;
	    } else {
		if (v.isStatic() && v.isPtr())
		    return ICON_FIELD_STATIC_PTR;
		else if (v.isStatic())
		    return ICON_FIELD_STATIC;
		else if (v.isPtr())
		    return ICON_FIELD_PTR;
		else
		    return ICON_FIELD;
	    }

	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface NodeModel
    // return value of tooltip
    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return Catalog.get("PROP_name"); // NOI18N
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    if (Log.Variable.tipdebug) {
		String info = v.getDebugInfo();
		info = "<html>" + info + "</html>"; // NOI18N
		return info;
	    } else {
		// value of tooltip for variable node is it's type
		return v.getType();
	    }
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Variable) {
	    return false;
	} else if (node instanceof WatchVariable || node instanceof Watch) {
	    return false;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node)
	throws UnknownTypeException, java.io.IOException {
	if (node == ROOT) {
	    return null;
	} else if (node instanceof Variable) {
	    throw new java.io.IOException();
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Variable) {
	    return false;
	} else if (node instanceof WatchVariable || node instanceof Watch) {
	    return false;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node)
	throws UnknownTypeException, java.io.IOException {
	if (node == ROOT) {
	    return null;
	} else if (node instanceof Variable) {
	    throw new java.io.IOException();
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
        } else if (node instanceof WatchModel.EmptyWatch) {
            return true;
	} else if (node instanceof WatchVariable || node instanceof Watch) {
	    return true;
	} else if (node instanceof Variable) {
	    return false;
        } else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public void setName(ExtendedNodeModel original, Object node, String name)
	throws UnknownTypeException {
	if (node == ROOT) {
	    return;
        } if (node instanceof WatchModel.EmptyWatch) {
            ((WatchModel.EmptyWatch)node).setExpression(name);
            return;
        } else if (node instanceof WatchVariable || node instanceof Watch) {
            WatchVariable w = (WatchVariable) node;
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();

            // replace this node
            debugger.replaceWatch(w.getNativeWatch(), name);
	    return ;
	} else if (node instanceof Variable) {
	    return;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t)
	throws UnknownTypeException {
	if (node == ROOT) {
	    return new PasteType[0];
	} else if (node instanceof Variable) {
	    return new PasteType[0];
	} else if (node instanceof WatchVariable || node instanceof Watch) {
	    return new PasteType[0];
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface ExtendedNodeModel
    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node)
	throws UnknownTypeException {

	if (node == ROOT) {
	    return getIconBase(original, node) + ".gif";	// NOI18N
        } else if (node instanceof WatchModel.EmptyWatch || node instanceof ShowMoreMessage) {
            return null;
	} else if (node instanceof WatchVariable || node instanceof Watch) {
            WatchVariable w = (WatchVariable) node;
            return getIconBase(original, node) + ".gif";	// NOI18N
	} else if (node instanceof Variable) {
	    return getIconBase(original, node) + ".gif";	// NOI18N
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface TableModel
    @Override
    public Object getValueAt(Object node, String columnID)
	throws UnknownTypeException {
	if (node == ROOT) {
	    return null;
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    if (Constants.PROP_LOCAL_TYPE.equals(columnID) ||
		Constants.PROP_WATCH_TYPE.equals(columnID))
	        return v.getType();

	    else if (Constants.PROP_LOCAL_VALUE.equals(columnID) ||
		     Constants.PROP_WATCH_VALUE.equals(columnID) ||
		     Constants.PROP_LOCAL_TO_STRING.equals(columnID) ||
		     Constants.PROP_WATCH_TO_STRING.equals(columnID)) {
			VariableValue value = new VariableValue(v.getAsText(), v.getDelta());
			return value.toString();
		}
	    else return null;
	} else if (node instanceof WatchModel.EmptyWatch || node instanceof ShowMoreMessage){
            return "";
        } else {
	    throw new UnknownTypeException(node);
        }
    }

    // interface TableModel
    @Override
    public boolean isReadOnly(Object node, String columnID)
	throws UnknownTypeException {

	if (node == ROOT) {
	    return false;
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    if (Constants.PROP_LOCAL_VALUE.equals(columnID) ||
		Constants.PROP_WATCH_VALUE.equals(columnID) )
		return !v.isEditable();
	    else
		// 6500791 Type column is read only
		return true;
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface TableModel
    @Override
    public void setValueAt(Object node, String columnID, Object value)
	throws UnknownTypeException {
	if (node == ROOT) {
	    ;
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    if (Constants.PROP_LOCAL_VALUE.equals(columnID) ||
	        Constants.PROP_WATCH_VALUE.equals(columnID) ) {
		// successful result that come back from cmd
		// "assign" will do the work, if not, keep the
		v.setVariableValue((String)value);

	    }
	} else {
	    throw new UnknownTypeException(node);
        }
    }

    // interface TreeExpansionModel
    @Override
    public synchronized boolean isExpanded(Object node) throws UnknownTypeException {
	if (node == ROOT) {
	    return false;
	} else if (node instanceof Variable) {
	    Variable var = (Variable) node;
	    if (Log.Variable.expansion) {
		System.out.printf("isExpanded(%s) -> %s\n", // NOI18N
		    var.getVariableName(), var.isExpanded());
	    }
	    return var.isExpanded();
        }
	throw new UnknownTypeException(node);
    }

    // interface TreeExpansionModel
    @Override
    public synchronized void nodeCollapsed(Object node) {
	if (node instanceof Variable) {
	    Variable var = (Variable) node;
	    if (Log.Variable.expansion) {
		System.out.printf("nodeCollapsed(%s) was %s\n", // NOI18N
		    var.getVariableName(), var.isExpanded());
	    }
	    var.noteCollapsed(!isLocal());
	}
    }

    // interface TreeExpansionModel
    @Override
    public synchronized void nodeExpanded(Object node) {
	if (node instanceof Variable) {
	    Variable var = (Variable) node;
	    if (Log.Variable.expansion) {
		System.out.printf("nodeExpanded(%s) was %s\n", // NOI18N
		    var.getVariableName(), var.isExpanded());
	    }
	    var.noteExpanded(!isLocal());
	}
    }

    public static class BrowseArrayAction extends AbstractAction {

	private Object node = null;

	public BrowseArrayAction(Object node) {
	    // use Calalog.get??
	    // super(Catalog.get("ACT_ABrowser"));
	    super("Browse Array"); // NOI18N
	    this.node = node;
	    if (node instanceof Variable) {
		Variable v = (Variable) node;
		setEnabled(v.isArrayBrowsable());
	    } else {
	        setEnabled(false);
	    }
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    if (node instanceof Variable) {
		Variable v = (Variable) node;
		if (v.isArrayBrowsable()) {
//		    ArrayBrowserWindow.getDefault().
//			getArrayBrowserController().
//			displayArray(v.getVariableName(), "");
		}
	    }
        }
    }

    public static final Action Action_DYNAMIC_TYPE =
	new DynamicTypeAction();

    private static class DynamicTypeAction extends BooleanStateAction {
	DynamicTypeAction() {
	}

	// interface BooleanStateAction
        @Override
	public boolean getBooleanState() {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    if (debugger instanceof NativeDebugger) {
		setEnabled(true);
		return debugger.isDynamicType();
	    }

	    setEnabled(false);
	    return false;
	}

	// interface SystemAction
        @Override
	public String getName() {
	    return Catalog.get("ACT_Dynamic"); // NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("output_dynamic_type");
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    debugger.setDynamicType(!getBooleanState());
        }
    }

    public static final Action Action_INHERITED_MEMBERS =
	new InheritedMembersAction();

    private static class InheritedMembersAction extends BooleanStateAction {
	InheritedMembersAction() {
	}

	// override BooleanStateAction
        @Override
	public boolean getBooleanState() {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    if (debugger instanceof NativeDebugger) {
		setEnabled(true);
		return debugger.isInheritedMembers();
	    }

	    setEnabled(false);
	    return false;

	}

	// interface SystemAction
        @Override
	public String getName() {
	    return Catalog.get("ACT_Inherited"); // NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("output_inherited_members");
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    debugger.setInheritedMembers(!getBooleanState());
        }
    }

    public static final Action Action_STATIC_MEMBERS =
	new StaticMembersAction();

    private static class StaticMembersAction extends BooleanStateAction {
	StaticMembersAction() {
	}

	// override BooleanStateAction
        @Override
	public boolean getBooleanState() {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    if (debugger instanceof NativeDebugger) {
		setEnabled(true);
		return debugger.isStaticMembers();
	    }

	    setEnabled(false);
	    return false;

	}

	// interface SystemAction
        @Override
	public String getName() {
	    return Catalog.get("ACT_Static"); // NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("show_static_members");
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    debugger.setStaticMembers(!getBooleanState());
        }
    }

    private static final OutputFormatAction Action_OUTPUT_FORMAT =
	new OutputFormatAction();

    public static Action getOutputFormatAction(Variable v) {
        Action_OUTPUT_FORMAT.setVar(v);
        return Action_OUTPUT_FORMAT;
    }

    public static Action getWatchAction(final Variable var) {
        return new AbstractAction(Catalog.get("ACT_Variable_Watch")) { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                var.createWatch();
            }
        };
    }

    private static class OutputFormatAction extends SystemAction
			implements Presenter.Popup {

        private Variable var;

	public void setVar(Variable v) {
	    var = v;
	}

	// interface SystemAction
        @Override
	public String getName () {
	    return Catalog.get("ACT_Output_Format"); // NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("output_format");
	}

	// interface SystemAction
        @Override
	public void actionPerformed(ActionEvent ev) {
	}

	// interface Presenter.Popup
        @Override
	public JMenuItem getPopupPresenter() {
	    JMenu mi = new JMenu();
	    Actions.connect(mi, (Action)this, false);
	    mi.addMenuListener(new FormatItemListener());
	    return mi;
	}

	private class FormatItemListener implements MenuListener {

            @Override
	    public void menuCanceled(MenuEvent e) {
	    }

            @Override
	    public void menuDeselected(MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
	    }

            @Override
	    public void menuSelected (MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
		String format_array[] = debugger.formatChoices();

		if (debugger != null) {
		    for (int vx = 0; vx < format_array.length; vx++) {
			boolean status = (var != null) && format_array[vx].equals(var.getFormat());

			JMenuItem formatItem = new JRadioButtonMenuItem (
			    format_array[vx],
			    status);
			menu.add(formatItem);
			formatItem.setHorizontalTextPosition(SwingConstants.LEFT);

			formatItem.addActionListener(new ActionListener() {
                            @Override
			    public void actionPerformed(ActionEvent actionEvent) {
				updateFormat(actionEvent.getActionCommand());
			    }
			});
		    }
		}
	    }

	    private void updateFormat(String format) {
		var.postFormat(format);
	    }
	}
    }

    public static final Action Action_PRETTY_PRINT = new PrettyPrintAction();

    private static class PrettyPrintAction  extends BooleanStateAction {
	PrettyPrintAction() {
	}

	// override BooleanStateAction
        @Override
	public boolean getBooleanState() {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    if (debugger instanceof NativeDebugger) {
		setEnabled(true);
		return debugger.isPrettyPrint();
	    }

	    setEnabled(false);
	    return false;

	}

	// interface SystemAction
        @Override
	public String getName() {
	    return Catalog.get("ACT_Pretty_Print"); // NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("pretty_print");
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
            final boolean newState = !getBooleanState();
	    debugger.setPrettyPrint(newState);
            debugger.postPrettyPrint(newState);
        }
    }

    public static class ShowMoreMessage {
        private final Variable v;

        public ShowMoreMessage(Variable v) {
            this.v = v;
        }

        public void getMore() {
            v.getMoreChildren();
        }

        public String getMessage() {
            return Catalog.get("MSG_Show_More_Message"); // NOI18N
        }
    }
}
