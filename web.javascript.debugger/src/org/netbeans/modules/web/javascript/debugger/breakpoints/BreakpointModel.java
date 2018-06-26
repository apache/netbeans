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
