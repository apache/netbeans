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
package org.netbeans.modules.debugger.ui.models;

import org.netbeans.modules.debugger.ui.views.ToolTipView;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Assures that the variable in tooltip is expanded automatically.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="ToolTipView",
                             types=TreeExpansionModelFilter.class,
                             position=10200)
public class ToolTipExpansionFilter implements TreeExpansionModelFilter {

    @Override
    public boolean isExpanded(TreeExpansionModel original, Object node) throws UnknownTypeException {
        if (node == ToolTipView.getVariable()) {
            return true;
        } else {
            return original.isExpanded(node);
        }
    }

    @Override
    public void nodeExpanded(Object node) {}

    @Override
    public void nodeCollapsed(Object node) {}

    @Override
    public void addModelListener(ModelListener l) {}

    @Override
    public void removeModelListener(ModelListener l) {}

}
