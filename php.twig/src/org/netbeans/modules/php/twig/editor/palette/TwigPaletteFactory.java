/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.php.twig.editor.palette;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.*;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

public class TwigPaletteFactory {

    private static final String TWIG_PALETTE_FOLDER = "Palettes/Twig"; // NOI18N
    private static PaletteController controller = null;

    @MimeRegistration(mimeType = TwigLanguage.TWIG_MIME_TYPE, service = PaletteController.class)
    public static PaletteController createPalette() throws IOException {
        if (controller == null) {
            controller = PaletteFactory.createPalette(
                    TWIG_PALETTE_FOLDER,
                    new TwigPaletteActions(),
                    null,
                    new TwigPaletteDragAndDropHandler()
            );
        }
        return controller;
    }

    //~ Inner classes
    private static class TwigPaletteActions extends PaletteActions {

        @Override
        public Action[] getImportActions() {
            return new Action[0];
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return new Action[0];
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup category) {
            return new Action[0];
        }

        @Override
        public Action[] getCustomItemActions(Lookup item) {
            return new Action[0];
        }

        @Override
        public Action getPreferredAction(Lookup item) {
            return new TwigPaletteInsertAction(item);
        }

    }

    private static class TwigPaletteInsertAction extends AbstractAction {

        private static final long serialVersionUID = -2545383594436146990L;

        private final Lookup item;

        TwigPaletteInsertAction(Lookup item) {
            this.item = item;
        }

        @NbBundle.Messages("TwigPaletteInsertAction.ErrorNoFocusedDocument=No document selected. Please select a document to insert the item into.")
        @Override
        public void actionPerformed(ActionEvent event) {

            ActiveEditorDrop drop = item.lookup(ActiveEditorDrop.class);

            JTextComponent target = Utilities.getFocusedComponent();
            if (target == null) {
                String msg = Bundle.TwigPaletteInsertAction_ErrorNoFocusedDocument();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                return;
            }

            if (drop == null) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "{0} doesn''t provide {1}", new Object[]{item.getClass(), ActiveEditorDrop.class}); //NOI18N
                return;
            }

            try {
                drop.handleTransfer(target);
            } finally {
                Utilities.requestFocus(target);
            }

            try {
                PaletteController paletteController = TwigPaletteFactory.createPalette();
                paletteController.clearSelection();
            } catch (IOException ioe) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, null, ioe);
            }

        }
    }

    private static class TwigPaletteDragAndDropHandler extends DragAndDropHandler {

        public TwigPaletteDragAndDropHandler() {
            super(true);
        }

        @Override
        public void customize(ExTransferable t, Lookup item) {
        }

    }
}
