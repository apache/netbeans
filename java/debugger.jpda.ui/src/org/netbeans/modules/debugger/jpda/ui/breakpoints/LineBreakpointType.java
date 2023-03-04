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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ui.BreakpointType;

import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;


/**
* @author   Daniel Prusa
*/
@BreakpointType.Registration(displayName="#CTL_Line_event_type_name")
public class LineBreakpointType extends BreakpointType {

    private Reference<LineBreakpointPanel> customizerRef = new WeakReference<LineBreakpointPanel>(null);

    public String getCategoryDisplayName () {
        return NbBundle.getMessage (
            LineBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    public JComponent getCustomizer () {
        LineBreakpointPanel panel = new LineBreakpointPanel();
        customizerRef = new WeakReference<LineBreakpointPanel>(panel);
        return panel;
    }

    @Override
    public Controller getController() {
        LineBreakpointPanel panel = customizerRef.get();
        if (panel != null) {
            return panel.getController();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeDisplayName () {
        return NbBundle.getMessage (LineBreakpointType.class, 
            "CTL_Line_event_type_name"
        );
    }
    
    public boolean isDefault () {
        return EditorContextBridge.getDefaultType () == EditorContextBridge.LINE;
    }
}
