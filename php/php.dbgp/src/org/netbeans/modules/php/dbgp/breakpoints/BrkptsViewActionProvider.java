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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.modules.php.dbgp.ui.DbgpLineBreakpointCustomizer;
import org.netbeans.modules.php.dbgp.ui.DbgpLineBreakpointCustomizerPanel;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class BrkptsViewActionProvider implements NodeActionsProviderFilter {
    private static final String GO_TO_SOURCE_LABEL = "CTL_Breakpoint_GoToSource_Label"; // NOI18N

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action[] actions = original.getActions(node);
        if (node instanceof LineBreakpoint) {
            Action[] newActions = new Action[actions.length + 4];
            newActions[0] = GO_TO_SOURCE_ACTION;
            newActions[1] = null;
            System.arraycopy(actions, 0, newActions, 2, actions.length);
            newActions[newActions.length - 2] = null;
            newActions[newActions.length - 1] = CUSTOMIZE_ACTION;
            actions = newActions;
        }
        return actions;
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) {
            goToSource((LineBreakpoint) node);
        } else {
            original.performDefaultAction(node);
        }
    }

    private static void goToSource(LineBreakpoint breakpoint) {
        Line line = breakpoint.getLine();
        if (line != null) {
            line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
        }
    }

    @NbBundle.Messages("CTL_Breakpoint_Customizer_Title=Breakpoint Properties")
    private static void customize(LineBreakpoint lb) {
        DbgpLineBreakpointCustomizerPanel panel = DbgpLineBreakpointCustomizer.getCustomizerComponent(lb);
        HelpCtx helpCtx = HelpCtx.findHelp(panel);
        if (helpCtx == null) {
            helpCtx = new HelpCtx("debug.add.breakpoint"); // NOI18N
        }

        Controller controller = panel.getController();
        if (controller == null) {
            return;
        }

        final Controller[] cPtr = new Controller[]{controller};
        final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
        final Dialog[] dialogPtr = new Dialog[1];
        final PropertyChangeListener propertyChangeListener = (PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals(NotifyDescriptor.PROP_ERROR_NOTIFICATION)) {
                Object v = e.getNewValue();
                String message = (v == null) ? null : v.toString();
                descriptorPtr[0].getNotificationLineSupport().setErrorMessage(message);
            } else if (e.getPropertyName().equals(Controller.PROP_VALID)) {
                descriptorPtr[0].setValid(controller.isValid());
            }
        };
        controller.addPropertyChangeListener(propertyChangeListener);
        ActionListener buttonsActionListener = (ActionEvent e) -> {
            if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                boolean ok = cPtr[0].ok();
                if (ok) {
                    dialogPtr[0].setVisible(false);
                    cPtr[0].removePropertyChangeListener(propertyChangeListener);
                }
            } else {
                dialogPtr[0].setVisible(false);
                cPtr[0].removePropertyChangeListener(propertyChangeListener);
            }
        };
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.CTL_Breakpoint_Customizer_Title(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                buttonsActionListener
        );
        descriptor.setClosingOptions(new Object[]{});
        descriptor.createNotificationLineSupport();
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.pack();
        descriptorPtr[0] = descriptor;
        dialogPtr[0] = d;
        d.setVisible(true);
    }

    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
            NbBundle.getMessage(BrkptsViewActionProvider.class, GO_TO_SOURCE_LABEL),
            new GoToSourcePerformer(),
            Models.MULTISELECTION_TYPE_EXACTLY_ONE);

    private static class GoToSourcePerformer implements Models.ActionPerformer {

        @Override
        public boolean isEnabled(Object arg) {
            return true;
        }

        @Override
        public void perform(Object[] nodes) {
            goToSource((LineBreakpoint) nodes[0]);
        }

    }

    @NbBundle.Messages("CTL_Breakpoint_Customize_Label=Properties")
    private static final Action CUSTOMIZE_ACTION = Models.createAction(
            Bundle.CTL_Breakpoint_Customize_Label(),
            new CustomizePerformer(),
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private static class CustomizePerformer implements Models.ActionPerformer {

        @Override
        public boolean isEnabled(Object node) {
            return true;
        }

        @Override
        public void perform(Object[] nodes) {
            customize((LineBreakpoint) nodes[0]);
        }
    }

}
