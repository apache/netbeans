/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.models;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeModel.class)
public class ComponentBreakpointsNodeModel implements NodeModel {

    public static final String C_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    public static final String DISABLED_C_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";
    public static final String C_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint";
    public static final String DISABLED_C_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint";
    
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof ComponentBreakpoint) {
            ComponentBreakpoint ab = (ComponentBreakpoint) node;
            String componentName;
            if (ab.getComponent() != null && ab.getComponent().getComponentInfo() != null) {
                componentName = ab.getComponent().getComponentInfo().getDisplayName();
            } else {
                componentName = "";
            }
            return NbBundle.getMessage(ComponentBreakpointsNodeModel.class, "CTL_ComponentBreakpoint", componentName);
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        boolean disabled = !((Breakpoint) node).isEnabled();
        boolean invalid = ((Breakpoint) node).getValidity() == VALIDITY.INVALID;
        if (node instanceof ComponentBreakpoint) {
            String condition = ((ComponentBreakpoint) node).getCondition();
            boolean conditional = condition != null && condition.trim().length() > 0;
            String iconBase;
            if (disabled) {
                if (conditional) {
                    iconBase = DISABLED_C_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = DISABLED_C_BREAKPOINT;
                }
            } else {
                if (conditional) {
                    iconBase = C_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = C_BREAKPOINT;
                }
            }
            if (invalid && !disabled) {
                iconBase += "_broken";  // NOI18N
            }
            return iconBase;
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof ComponentBreakpoint) {
            ComponentBreakpoint ab = (ComponentBreakpoint) node;
            String componentName;
            if (ab.getComponent() != null && ab.getComponent().getComponentInfo() != null) {
                componentName = ab.getComponent().getComponentInfo().getDisplayName();
            } else {
                componentName = "";
            }
            return NbBundle.getMessage(ComponentBreakpointsNodeModel.class, "CTL_ComponentBreakpoint", componentName);
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}
