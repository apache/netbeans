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

package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;

import org.netbeans.spi.debugger.ui.BreakpointType;

import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;


/**
* @author Martin Entlicher
*/
@BreakpointType.Registration(displayName="#CTL_AWTComponent_breakpoint_type_name")
public class ComponentBreakpointType extends BreakpointType {

    private Reference<ComponentBreakpointPanel> customizerRef = new WeakReference<ComponentBreakpointPanel>(null);

    @Override
    public String getCategoryDisplayName () {
        return NbBundle.getMessage (
            ComponentBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    @Override
    public JComponent getCustomizer () {
        ComponentBreakpointPanel panel = new ComponentBreakpointPanel();
        customizerRef = new WeakReference<ComponentBreakpointPanel>(panel);
        return panel;
    }

    @Override
    public Controller getController() {
        ComponentBreakpointPanel panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeDisplayName () {
        return NbBundle.getMessage (ComponentBreakpointType.class, 
            "CTL_AWTComponent_breakpoint_type_name"
        );
    }
    
    @Override
    public boolean isDefault () {
        ScreenshotUIManager activeScreenshotManager = ScreenshotUIManager.getActive();
        if (activeScreenshotManager != null) {
            ComponentInfo ci = activeScreenshotManager.getSelectedComponent();
            return (ci instanceof AWTComponentInfo);
        }
        return false;
    }
}
