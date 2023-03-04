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
package org.netbeans.modules.jumpto.symbol;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import org.netbeans.modules.jumpto.type.UiOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
class DialogFactory {

    private static Dimension initialDimension;

    static Dialog createDialog(
            final String title,
            final GoToPanelImpl panel,
            final GoToPanelImpl.ContentProvider contentProvider,
            final JButton okButton) {
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToSymbolAction.class, "AN_GoToSymbol")  ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToSymbolAction.class, "AD_GoToSymbol")  ); //NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            panel,                             // innerPane
            title, // displayName
            true,
            new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
            okButton,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new DialogButtonListener(panel, okButton));

         dialogDescriptor.setClosingOptions(new Object[] {okButton, DialogDescriptor.CANCEL_OPTION});


        Dialog d = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        // Set size when needed
        final int width = UiOptions.GoToSymbolDialog.getWidth();
        final int height = UiOptions.GoToSymbolDialog.getHeight();
        if (width != -1 && height != -1) {
            d.setPreferredSize(new Dimension(width,height));
        }

        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        final Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        d.setBounds(Utilities.findCenterBounds(dim));
        initialDimension = dim;
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                contentProvider.closeDialog();
            }
        });

        return d;
    }

    static void storeDialogDimensions(final Dimension dim) {
            // Save dialog size only when changed
            final int currentWidth = dim.width;
            final int currentHeight = dim.height;
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                UiOptions.GoToSymbolDialog.setHeight(currentHeight);
                UiOptions.GoToSymbolDialog.setWidth(currentWidth);
            }
            initialDimension = null;
    }



    private static class DialogButtonListener implements ActionListener {

        private final GoToPanelImpl panel;
        private final JButton okButton;

        public DialogButtonListener(
                final GoToPanelImpl panel,
                final JButton okButton) {
            this.panel = panel;
            this.okButton = okButton;
        }

        public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == okButton) {
                panel.setSelectedSymbol();
            }
        }

    }
}
