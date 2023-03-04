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
package org.netbeans.modules.javascript2.debug.ui.models.breakpoints;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.modules.javascript2.debug.ui.breakpoints.ControllerProvider;
import org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizer;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeActionsProviderFilter.class)
public class BreakpointNodeActionProvider implements NodeActionsProviderFilter {


    @Override
    public Action[] getActions (NodeActionsProvider original, Object node)
            throws UnknownTypeException 
    {
        Action[] actions = original.getActions(node);
        if (node instanceof JSLineBreakpoint) {
            Action[] newActions = new Action [actions.length + 4];
            newActions [0] = GO_TO_SOURCE_ACTION;
            newActions [1] = null;
            System.arraycopy (actions, 0, newActions, 2, actions.length);
            newActions [newActions.length - 2] = null;
            newActions [newActions.length - 1] = CUSTOMIZE_ACTION;
            actions = newActions;
        }

        return actions;
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) 
        throws UnknownTypeException 
    {
        if (node instanceof JSLineBreakpoint) {
            goToSource((JSLineBreakpoint) node);
        } else {
            original.performDefaultAction(node);
        }
    }

    private static void goToSource(JSLineBreakpoint breakpoint ) {
        Line line = JSUtils.getLine(breakpoint);
        if (line != null) {
            line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
        }
    }
    
    @NbBundle.Messages("CTL_Breakpoint_Customizer_Title=Breakpoint Properties")
    private static void customize(JSLineBreakpoint lb) {
        JComponent c = JSLineBreakpointCustomizer.getCustomizerComponent(lb);
        HelpCtx helpCtx = HelpCtx.findHelp (c);
        if (helpCtx == null) {
            helpCtx = new HelpCtx ("debug.add.breakpoint");  // NOI18N
        }
        Controller cc;
        if (c instanceof ControllerProvider) {
            cc = ((ControllerProvider) c).getController();
        } else {
            cc = (Controller) c;
        }

        final Controller[] cPtr = new Controller[] { cc };
        final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
        final Dialog[] dialogPtr = new Dialog[1];
        ActionListener buttonsActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                    boolean ok = cPtr[0].ok();
                    if (ok) {
                        dialogPtr[0].setVisible(false);
                    }
                } else {
                    dialogPtr[0].setVisible(false);
                }
            }
        };
        DialogDescriptor descriptor = new DialogDescriptor (
            c,
            Bundle.CTL_Breakpoint_Customizer_Title(),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            helpCtx,
            buttonsActionListener
        );
        descriptor.setClosingOptions(new Object[] {});
        Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
        d.pack ();
        descriptorPtr[0] = descriptor;
        dialogPtr[0] = d;
        d.setVisible (true);
    }

    @NbBundle.Messages("CTL_Breakpoint_GoToSource_Label=Go to Source")
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
            Bundle.CTL_Breakpoint_GoToSource_Label(), 
            new GoToSourcePerformer(),
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );

    private static class GoToSourcePerformer implements Models.ActionPerformer {

        /* (non-Javadoc)
         * @see org.netbeans.spi.viewmodel.Models.ActionPerformer#isEnabled(java.lang.Object)
         */
        @Override
        public boolean isEnabled( Object arg ) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.viewmodel.Models.ActionPerformer#perform(java.lang.Object[])
         */
        @Override
        public void perform( Object[] nodes ) {
            goToSource((JSLineBreakpoint) nodes [0]);            
        }
        
    }
    
    @NbBundle.Messages("CTL_Breakpoint_Customize_Label=Properties")
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        Bundle.CTL_Breakpoint_Customize_Label(),
        new CustomizePerformer(),
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    private static class CustomizePerformer implements Models.ActionPerformer {
        
        @Override
        public boolean isEnabled (Object node) {
            return true;
        }
        
        @Override
        public void perform (Object[] nodes) {
            customize ((JSLineBreakpoint) nodes [0]);
        }
    }
        
}
