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
