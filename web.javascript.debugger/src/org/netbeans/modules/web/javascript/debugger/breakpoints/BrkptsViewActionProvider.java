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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.web.javascript.debugger.breakpoints.ui.AbstractBreakpointCustomizer;
import org.netbeans.modules.web.javascript.debugger.breakpoints.ui.ControllerProvider;
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
public class BrkptsViewActionProvider implements NodeActionsProviderFilter {


    @Override
    public Action[] getActions (NodeActionsProvider original, Object node)
            throws UnknownTypeException 
    {
        Action[] actions = original.getActions(node);
        if (node instanceof AbstractBreakpoint) {
            Action[] newActions = new Action [actions.length + 2];
            System.arraycopy (actions, 0, newActions, 0, actions.length);
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
        original.performDefaultAction(node);
    }

    @NbBundle.Messages("CTL_Breakpoint_Customizer_Title=Breakpoint Properties")
    private static void customize(AbstractBreakpoint ab) {
        JComponent c = AbstractBreakpointCustomizer.getCustomizerComponent(ab);
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
            customize ((AbstractBreakpoint) nodes [0]);
        }
    }
        
}
