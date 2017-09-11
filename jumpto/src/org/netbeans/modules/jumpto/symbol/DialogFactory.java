/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
