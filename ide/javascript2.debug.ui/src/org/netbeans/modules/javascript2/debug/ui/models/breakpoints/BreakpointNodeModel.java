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

package org.netbeans.modules.javascript2.debug.ui.models.breakpoints;

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=ExtendedNodeModel.class)
public class BreakpointNodeModel implements ExtendedNodeModel {
    
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint.gif";                   // NOI18N
    public static final String CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit.gif";                // NOI18N
    public static final String DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint.gif";           // NOI18N
    public static final String DISABLED_CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit.gif";        // NOI18N
    public static final String LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint.gif";        // NOI18N
    public static final String CURRENT_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit.gif";     // NOI18N
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint.gif";// NOI18N
    public static final String DEACTIVATED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint_stroke.png";                     // NOI18N
    public static final String DEACTIVATED_DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint_stroke.png";             // NOI18N
    
    private List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    public BreakpointNodeModel() {
        JSBreakpointsInfoManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                ModelEvent.NodeChanged nch = new ModelEvent.NodeChanged(BreakpointNodeModel.this, null);
                for (ModelListener ml : listeners) {
                    ml.modelChanged(nch);
                }
            }
        });
    }
    
    @NbBundle.Messages({
        "# {0} - The file name and line number",
        "LBL_LineBreakpoint_on=Line {0}"
    })
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof JSLineBreakpoint) {
            JSLineBreakpoint breakpoint = (JSLineBreakpoint) node;
            String fileName = JSUtils.getFileName(breakpoint);
            int lineNumber = breakpoint.getLineNumber();
            if(breakpoint.isActive()) {
                return ViewModelSupport.toHTML(
                        Bundle.LBL_LineBreakpoint_on(fileName + ":" + lineNumber),
                        true, false, null);
            } else {
                return Bundle.LBL_LineBreakpoint_on(fileName + ":" + lineNumber);
            }
        }
        throw new UnknownTypeException(node);
    }
    
    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (!(node instanceof JSLineBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSLineBreakpoint b = (JSLineBreakpoint) node;
        boolean disabled = !b.isEnabled();
        boolean invalid = b.getValidity() == Breakpoint.VALIDITY.INVALID;
        boolean active = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
        String iconBase;
        if (disabled) {
            if (active) {
                iconBase = DISABLED_LINE_BREAKPOINT;
            } else {
                iconBase = DEACTIVATED_DISABLED_LINE_BREAKPOINT;
            }
        } else {
            if (b.isActive()) {
                iconBase = CURRENT_LINE_BREAKPOINT;
            } else {
                if (active) {
                    iconBase = LINE_BREAKPOINT;
                } else {
                    iconBase = DEACTIVATED_LINE_BREAKPOINT;
                }
            }
        }
        if (invalid && !disabled) {
            int dot = iconBase.lastIndexOf('.');
            iconBase = iconBase.substring(0, dot) + "_broken" + iconBase.substring(dot);
        }
        return iconBase;
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not to be called, replaced with getIconBaseWithExtension().");
    }
    
    @NbBundle.Messages({
        "CTL_APPEND_BP_Valid=[Active]",
        "CTL_APPEND_BP_Invalid=[Invalid]",
        "# {0} - message describing why is the breakpoint invalid.",
        "CTL_APPEND_BP_Invalid_with_reason=[Invalid, reason: {0}]"
    })
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof JSLineBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSLineBreakpoint b = (JSLineBreakpoint) node;
        String appendMsg = null;
        Breakpoint.VALIDITY validity = b.getValidity();
        boolean valid = validity == Breakpoint.VALIDITY.VALID;
        boolean invalid = validity == Breakpoint.VALIDITY.INVALID;
        String message = b.getValidityMessage();
        if (valid) {
            appendMsg = Bundle.CTL_APPEND_BP_Valid();
        }
        if (invalid) {
            if (message != null) {
                appendMsg = Bundle.CTL_APPEND_BP_Invalid_with_reason(message);
            } else {
                appendMsg = Bundle.CTL_APPEND_BP_Invalid();
            }
        }
        String description = JSUtils.getLine(b).getDisplayName();
        if (appendMsg != null) {
            description = description + " " + appendMsg;
        }
        return description;
    }

    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

}
