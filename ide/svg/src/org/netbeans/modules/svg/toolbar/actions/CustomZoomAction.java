/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.svg.toolbar.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.svg.SVGViewerElement;

import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action that can always be invoked and work procedurally.
 *
 * @author Christian Lenz
 */
@ActionID(id = "org.netbeans.modules.svg.toolbar.CustomZoomAction", category = "View")
@ActionRegistration(lazy = false, displayName = "#LBL_CustomZoom")
public class CustomZoomAction extends CallableSystemAction {

    /**
     * Generated serial version UID.
     */
    static final long serialVersionUID = 8247068408606777895L;

    /**
     * Actually performs action.
     */
    @Override
    public void performAction() {
        TopComponent currentComponent = TopComponent.getRegistry().getActivated();
        Lookup tcLookup = currentComponent != null ? currentComponent.getLookup() : null;
        SVGViewerElement svgViewerElement = tcLookup != null ? tcLookup.lookup(SVGViewerElement.class) : null;
        if (svgViewerElement != null) {
            final Dialog[] dialogs = new Dialog[1];
            final CustomZoomPanel zoomPanel = new CustomZoomPanel();

            zoomPanel.setEnlargeFactor(1);
            zoomPanel.setDecreaseFactor(1);

            DialogDescriptor dd = new DialogDescriptor(
                zoomPanel,
                NbBundle.getMessage(CustomZoomAction.class, "LBL_CustomZoomAction"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        int enlargeFactor;
                        int decreaseFactor;

                        try {
                            enlargeFactor = zoomPanel.getEnlargeFactor();
                            decreaseFactor = zoomPanel.getDecreaseFactor();
                        } catch (NumberFormatException nfe) {
                            notifyInvalidInput();
                            return;
                        }

                        // Invalid values.
                        if (enlargeFactor == 0 || decreaseFactor == 0) {
                            notifyInvalidInput();
                            return;
                        }

                        svgViewerElement.customZoom(enlargeFactor, decreaseFactor);
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }

                private void notifyInvalidInput() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomZoomAction.class, "MSG_InvalidValues"),
                        NotifyDescriptor.ERROR_MESSAGE
                    ));
                }

            } // End of annonymnous ActionListener.
            );
            dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
            dialogs[0].setVisible(true);
        }
    }

    /**
     * Gets action name. Implements superclass abstract method.
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * Gets action help context. Implemenets superclass abstract method.
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.svg.toolbar.CustomZoomAction");
    }

    /**
     * Gets icon resource. Overrides superclass method.
     */
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/image/customZoom.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
