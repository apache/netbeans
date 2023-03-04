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
