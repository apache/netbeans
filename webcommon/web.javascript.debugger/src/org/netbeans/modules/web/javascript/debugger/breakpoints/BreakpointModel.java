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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode.NodeId;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeModel.class)
public class BreakpointModel extends ViewModelSupport
        implements NodeModel { 

    public static final String BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint";            // NOI18N
    public static final String CURRENT_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpointHit";         // NOI18N
    public static final String DISABLED_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpoint";    // NOI18N
    public static final String DISABLED_CURRENT_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpointHit"; // NOI18N

    public BreakpointModel() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getDisplayName(java.lang.Object)
     */
    @NbBundle.Messages({
        "# {0} - The name of the DOM node",
        "# {1} - The type of modification, one more of the three listed below (comma separated)",
        "LBL_DOMBreakpoint_on=DOM \"{0}\" on modifications of: {1}",
        "LBL_DOM_Subtree=subtree",
        "LBL_DOM_Attributes=attributes",
        "LBL_DOM_Removal=removal",
        "# {0} - A list of event names",
        "LBL_EventsBreakpoint_on=Event listener on: {0}",
        "# {0} - A part of an URL. Do not translate \"XMLHttpRequest\", which is a name of the API on which the breakpoint acts.",
        "LBL_XHRBreakpoint_on=XMLHttpRequest URL containing \"{0}\""
    })
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof DOMBreakpoint) {
            DOMBreakpoint breakpoint = (DOMBreakpoint) node;
            String nodeName = breakpoint.getNode().getNodeName();
            List<? extends NodeId> nodePath = breakpoint.getNode().getPath();
            if (nodePath != null && !nodePath.isEmpty()) {
                int chn = nodePath.get(nodePath.size() - 1).getChildNumber();
                if (chn >= 0) {
                    nodeName = nodeName + '[' + chn + ']';
                }
            }
            StringBuilder modifications = new StringBuilder();
            if (breakpoint.isOnSubtreeModification()) {
                modifications.append(Bundle.LBL_DOM_Subtree());
            }
            if (breakpoint.isOnAttributeModification()) {
                if (modifications.length() > 0) {
                    modifications.append(", ");
                }
                modifications.append(Bundle.LBL_DOM_Attributes());
            }
            if (breakpoint.isOnNodeRemoval()) {
                if (modifications.length() > 0) {
                    modifications.append(", ");
                }
                modifications.append(Bundle.LBL_DOM_Removal());
            }
            return Bundle.LBL_DOMBreakpoint_on(nodeName, modifications);
        }
        if (node instanceof EventsBreakpoint) {
            EventsBreakpoint eb = (EventsBreakpoint) node;
            String eventsStr = eb.getEvents().toString();
            eventsStr = eventsStr.substring(1, eventsStr.length() - 1);
            return Bundle.LBL_EventsBreakpoint_on(eventsStr);
        }
        if (node instanceof XHRBreakpoint) {
            XHRBreakpoint xb = (XHRBreakpoint) node;
            String urlSubstring = xb.getUrlSubstring();
            return Bundle.LBL_XHRBreakpoint_on(urlSubstring);
        }
        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getIconBase(java.lang.Object)
     */
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (!(node instanceof Breakpoint)) {
            throw new UnknownTypeException(node);
        }
        Breakpoint b = (Breakpoint) node;
        boolean disabled = !b.isEnabled();
        boolean invalid = b.getValidity() == Breakpoint.VALIDITY.INVALID;
        String iconBase;
        if (node instanceof AbstractBreakpoint) {
            AbstractBreakpoint breakpoint = (AbstractBreakpoint) node;
            if (disabled) {
                iconBase = DISABLED_BREAKPOINT;
            } else {
                iconBase = BREAKPOINT;
            }
        } else {
            throw new UnknownTypeException(node);
        }
        if (invalid && !disabled) {
            iconBase += "_broken";
        }
        return iconBase;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getShortDescription(java.lang.Object)
     */
    @NbBundle.Messages({
        "CTL_APPEND_BP_Valid=[Active]",
        "CTL_APPEND_BP_Invalid=[Invalid]",
        "# {0} - message describing why is the breakpoint invalid.",
        "CTL_APPEND_BP_Invalid_with_reason=[Invalid, reason: {0}]"
    })
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof Breakpoint)) {
            throw new UnknownTypeException(node);
        }
        Breakpoint b = (Breakpoint) node;
        String appendMsg = null;
        if (node instanceof Breakpoint) {
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
        }
        String description;
        if (true /*TODO: describe breakpoints*/) {
            return null;
        }
        if (appendMsg != null) {
            description = description + " " + appendMsg;
        }
        return description;
    }

}
