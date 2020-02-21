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

import java.awt.Color;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.PopLastDebuggerCallAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.PopToCurrentFrameAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxFrameAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;

import org.openide.util.HelpCtx;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/CallStackView/
 *	org.netbeans.spi.viewmodel.TreeModel
 *	org.netbeans.spi.viewmodel.NodeModel
 *	org.netbeans.spi.viewmodel.NodeActionsProvider
 */

public final class StackModel extends ModelListenerSupport
    implements TreeModel, NodeModel, NodeActionsProvider, TableModel,
    Constants {

    private NativeDebugger debugger;

    public StackModel(ContextProvider ctx) {
	super("stack");		// NOI18N
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
	if (parent == ROOT)
	    return debugger.getStack();
	else
	    return null;
    }

    // interface TreeModel
    @Override
    public int getChildrenCount(Object parent) {
	if (parent == ROOT)
	    return debugger.getStack().length;
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
    
    // interface NodeModel
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
	if (! (node instanceof Frame)) {
	    return Catalog.get("LBL_FunctionCol");	// NOI18N
	}
	Frame frame = (Frame) node;
	if (frame.isCurrent()) {
	    return VariableValue.bold(frame.getLocationName());
	} else {
	    return frame.getLocationName();
        }
    }

    // These are standard NB icons for stack frames.
    // See
    //	org.netbeans.modules.debugger.jpda.ui.views.StackNodeModel
    // for reference

    private static String ICON_PATH =
	"org/netbeans/modules/debugger/resources";	// NOI18N
    private static String ICON_BASE =
	ICON_PATH + "/callStackView/NonCurrentFrame";	// NOI18N
    private static String ICON_BASE_CURRENT =
	ICON_PATH + "/callStackView/CurrentFrame";	// NOI18N

    private static String ICON_EMPTY =
	"org/netbeans/modules/cnd/debugger/common2/icons/empty";	// NOI18N

    private static String ICON_SIGNAL_HANDLER =
	"org/netbeans/modules/cnd/debugger/common2/icons/signal_handler_frame";// NOI18N

    private static String ICON_USER_CALL =
	"org/netbeans/modules/cnd/debugger/common2/icons/user_call_frame";// NOI18N


    // interface NodeModel
    @Override
    public String getIconBase(Object node) {
	if (node instanceof Frame) {
	    Frame f = (Frame) node;
	    if (f.isCurrent())
		return ICON_BASE_CURRENT;
	    else if (f.isSignalHandler())
		return ICON_SIGNAL_HANDLER;
	    else if (f.isUserCall())
		return ICON_USER_CALL;
	    else if (f.isSpecial())
		return ICON_EMPTY;
	    else
		return ICON_BASE;
	} else {
	    return null;
	}
    }

    // interface NodeModel
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
	if (node instanceof Frame) {
	    Frame frame = (Frame) node;
	    return frame.getLocationName() + "at " + frame.getLocation();//NOI18N
	} else
	    return getDisplayName(node);	// SHOULD do something different?
    }

    // interface TableModel
    @Override
    public Object getValueAt(Object node, String columnID)
	throws UnknownTypeException {

	if (! (node instanceof Frame)) {
	    throw new UnknownTypeException(node);
	}
	Frame frame = (Frame) node;

	if (PROP_FRAME_LOCATION.equals(columnID)) {
	    return frame.getLocation();

	} else if (PROP_FRAME_NUMBER.equals(columnID)) {
	    return frame.getNumber();

	} else if (PROP_FRAME_OPTIMIZED.equals(columnID)) {
	    return frame.getOptimized();

	} else if (PROP_FRAME_CURRENT_PC.equals(columnID)) {
	    return frame.getCurrentPC();

	} else if (PROP_FRAME_LOADOBJ.equals(columnID)) {
	    return frame.getLoadObjBase();

	} else {
	    return "?" + columnID + "?";	// NOI18N
	}
    }

    // interface TableModel
    @Override
    public boolean isReadOnly(Object node, String columnID) {
	return true;
    }

    // interface TableModel
    @Override
    public void setValueAt(Object node, String columnID, Object value)
	throws UnknownTypeException {

	if (!(node instanceof Frame)) {
	    throw new UnknownTypeException(node);
	}
	// should never be called on us
    }

    private static final Action Action_VERBOSE = new VerboseAction();

    // interface NodeActionsProvider
    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
	EngineDescriptor desp = debugger.getNDI().getEngineDescriptor();
	boolean canDoMaxFrame = desp.hasCapability(EngineCapability.STACK_MAXFRAME);
	boolean canDoVerbose = desp.hasCapability(EngineCapability.STACK_VERBOSE);
	if (node == TreeModel.ROOT) {
	    return new Action[] {
		// LATER for GdbDebugger
		new PopTopmostCallAction(debugger),
		SystemAction.get(PopToCurrentFrameAction.class),
		SystemAction.get(PopLastDebuggerCallAction.class),
		new CopyStackAction(debugger),
		null,
		canDoMaxFrame ? SystemAction.get(MaxFrameAction.class) : null,
		canDoVerbose ? Action_VERBOSE : null,
	    };
	} else if (node instanceof Frame) {
	    Frame frame = (Frame) node;
	    return new Action[] {
		new MakeCurrentAction(debugger, frame),
		// LATER for GdbDebugger
		null,
		new PopTopmostCallAction(debugger),
		new PopToHereAction(debugger, frame),
		SystemAction.get(PopLastDebuggerCallAction.class),
		new CopyStackAction(debugger),
		null,
		canDoMaxFrame ? SystemAction.get(MaxFrameAction.class) : null,
		canDoVerbose ? Action_VERBOSE : null,
	    };
	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface NodeActionsProvider
    @Override
    public void performDefaultAction (Object node) {
	Frame frame = (Frame) node;
	if (frame.more) {
	    debugger.moreFrame();
	} else
	    debugger.makeFrameCurrent(frame);
    }

    // innerclasses ...........................................................
    private static class MakeCurrentAction extends AbstractAction {
	private NativeDebugger debugger;
	private Frame frame;

	MakeCurrentAction(NativeDebugger debugger, Frame frame) {
	    super(Catalog.get("ACT_Make_Current"));	// NOI18N
	    this.debugger = debugger;
	    this.frame = frame;
	    setEnabled(!frame.isCurrent() && !frame.isSpecial());
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    debugger.makeFrameCurrent(frame);
	}
    }

    private static class PopToHereAction extends AbstractAction {
	private NativeDebugger debugger;
	private Frame frame;

	PopToHereAction(NativeDebugger debugger, Frame frame) {
	    super(Catalog.get("ACT_PopTo_Here"));	// NOI18N
	    this.debugger = debugger;
	    this.frame = frame;
	    setEnabled(!frame.isCurrent() && !frame.isSpecial());
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    debugger.popToHere(frame);
	}
    }

    /*
     * It would be nice if we could use debuggercores ACTION_POP_TOPMOST_CALL
     * but I can't figure how to do that.
     */
    private static class PopTopmostCallAction extends AbstractAction {
	private NativeDebugger debugger;

	PopTopmostCallAction(NativeDebugger debugger) {
	    super(Catalog.get("ACT_Pop_Caller"));	// NOI18N
	    this.debugger = debugger;
	    setEnabled(!debugger.state().isCore);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    debugger.popTopmostCall();
	}
    }

    private static class VerboseAction extends BooleanStateAction {

	// interface BooleanStateAction
        @Override
	public boolean getBooleanState() {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    return debugger.getVerboseStack();
	}

	// interface SystemAction
        @Override
	public String getName() {
	    return Catalog.get("LBL_Verbose");	// NOI18N
	}

	// interface SystemAction
        @Override
	public HelpCtx getHelpCtx() {
	    return new HelpCtx("Debugging_stack_verbose");	// NOI18N
	}

	// interface SystemAction
        @Override
	public void actionPerformed(ActionEvent e) {
	    NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
	    debugger.postVerboseStack( ! getBooleanState());
	}
    }

    private static class CopyStackAction extends AbstractAction {
	private NativeDebugger debugger;

	CopyStackAction (NativeDebugger debugger) {
	    super(Catalog.get("ACT_Copy_Stack"));	// NOI18N
	    this.debugger = debugger;
	}

        @Override
	public void actionPerformed(ActionEvent e) {
	    debugger.copyStack();
	}
    }

    // interface TreeModel etc
    @Override
    public void addModelListener(ModelListener l) {
	if (super.addModelListenerHelp(l))
	    debugger.registerStackModel(this);
    }

    // interface TreeModel etc
    @Override
    public void removeModelListener(ModelListener l) {
	if (super.removeModelListenerHelp(l))
	    debugger.registerStackModel(null);
    }
}
