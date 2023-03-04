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
package org.netbeans.modules.terminal.nb.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.modules.terminal.nb.PinPanel;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.netbeans.modules.terminal.support.TerminalPinSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.PIN_TAB_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_PinTab", lazy = true) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "PinTabAction") //NOI18N
})
public class PinTabAction extends TerminalAction {

    public static final String pinMessage = getMessage("CTL_PinTab");
    public static final String unpinMessage = getMessage("CTL_UnpinTab");

    private final TerminalPinSupport support = TerminalPinSupport.getDefault();

    public PinTabAction(Terminal context) {
        super(context);

        final Terminal terminal = getTerminal();

        putValue(NAME, getMessage(terminal.isPinned()));
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void performAction() {
        final Terminal terminal = getTerminal();
        final Term term = terminal.term();

        TerminalPinSupport.TerminalPinningDetails pinningDetails = support.findPinningDetails(term);
        boolean oldState = pinningDetails == null ? false : pinningDetails.isPinned();
        boolean newState = !oldState;

        putValue(NAME, getMessage(!terminal.isPinned()));

        if (newState) {
            boolean customTitle = terminal.isCustomTitle();
            String title = terminal.getTitle();
            String name = terminal.name();
            TerminalPinSupport.TerminalPinningDetails defaultValues = TerminalPinSupport.TerminalPinningDetails.create(customTitle, customTitle ? title : name, terminal.getCwd(), enabled);
            PinPanel pinPanel = new PinPanel(new TerminalPinSupport.TerminalDetails(support.findCreationDetails(term), defaultValues));

            final DialogDescriptor dd = new DialogDescriptor(
                    pinPanel,
                    NbBundle.getMessage(Terminal.class, "LBL_PinTab"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    null
            );

            pinPanel.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (PinPanel.PROPERTY_VALID.equals(evt.getPropertyName())) {
                        boolean oldValue = (Boolean) evt.getOldValue();
                        boolean newValue = (Boolean) evt.getNewValue();
                        if (oldValue != newValue) {
                            dd.setValid(newValue);
                        }
                    }
                }
            });

            Dialog cfgDialog = DialogDisplayer.getDefault().createDialog(dd);

            try {
                cfgDialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                dd.setValue(DialogDescriptor.CANCEL_OPTION);
            } finally {
                cfgDialog.dispose();
            }

            if (dd.getValue() != DialogDescriptor.OK_OPTION) {
                return;
            }

            String chosenTitle = pinPanel.getTitle();
            boolean chosenIsCustom = pinPanel.isCustomTitle();
            String chosenDirectory = pinPanel.getDirectory();

            if (chosenDirectory.isEmpty()) {
                chosenDirectory = null;
            }

            support.tabWasPinned(
                    term,
                    TerminalPinSupport.TerminalPinningDetails.create(
                            chosenIsCustom,
                            chosenIsCustom ? chosenTitle : name,
                            chosenDirectory,
                            enabled
                    )
            );

            if (chosenIsCustom && !title.equals(chosenTitle)) {
                customTitle = true;
                terminal.updateName(chosenTitle);
            }
        } else {
            support.tabWasUnpinned(term);
        }

        terminal.pin(newState);
    }

    public static String getMessage(boolean isPinned) {
        return isPinned ? unpinMessage : pinMessage;
    }

    // --------------------------------------------- 
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new PinTabAction(actionContext.lookup(Terminal.class));
    }
}
