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
package org.netbeans.modules.python.editor.imports;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;

/**
 * Python fast import library. 
 *
 * @todo When you insert an import, attempt to add it in the right sorted
 *   place.
 *
 */
public class FastImportAction extends BaseAction {
    private static final String ACTION_NAME = "fast-import";

    /** Creates a new instance of FastImportAction */
    public FastImportAction() {
        super(ACTION_NAME);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        try {
            final Rectangle caretRectangle = target.modelToView(target.getCaretPosition());
            final Font font = target.getFont();
            final Point where = new Point(caretRectangle.x, caretRectangle.y + caretRectangle.height);
            SwingUtilities.convertPointToScreen(where, target);

            final int position = target.getCaretPosition();
            final String ident = Utilities.getIdentifier(Utilities.getDocument(target), position);
            FileObject file = GsfUtilities.findFileObject(target.getDocument());

            if (ident == null || file == null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

//            SourceModel model = SourceModelFactory.getInstance().getModel(file);
//            if (model != null) {
//                final CompilationInfo[] infoHolder = new CompilationInfo[1];
//                try {
//                    model.runUserActionTask(new CancellableTask<CompilationInfo>() {
//                        public void cancel() {
//                        }
//
//                        public void run(CompilationInfo info) throws Exception {
//                            importItem(info, where, caretRectangle, font, position, ident);
//                        }
//                    }, false);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }

        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private void importItem(final PythonParserResult info, final Point where, final Rectangle caretRectangle, final Font font, final int position, final String ident) {
        PythonTree root = PythonAstUtils.getRoot(info);
        if (root == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // Compute suggestions
        PythonIndex index = PythonIndex.get(info.getSnapshot().getSource().getFileObject());
        Set<String> modules = index.getImportsFor(ident, true);

        // TODO - check the file to pick a better default (based on existing imports, usages of the symbol
        // which implies which alternative is eligible, etc.)


        final List<String> privileged = new ArrayList<>(modules);
        Collections.sort(privileged);
        final List<String> denied = new ArrayList<>();
        Collections.sort(denied);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ImportModulePanel panel = new ImportModulePanel(ident, privileged, denied, font, info, position);
                PopupUtil.showPopup(panel, "", where.x, where.y, true, caretRectangle.height);
            }
        });
    }
}
