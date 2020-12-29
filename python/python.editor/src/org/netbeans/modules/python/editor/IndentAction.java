/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.util.Exceptions;

/**
 * Override the indent action; see
 *   http://www.netbeans.org/issues/show_bug.cgi?id=150830
 * for justification.
 *
 */
public class IndentAction extends BaseKit.InsertTabAction {
    public IndentAction() {
    }

    @Override
    public Class getShortDescriptionBundleClass() {
        return IndentAction.class;
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            try {
                final BaseDocument doc = (BaseDocument)target.getDocument();
                final int offset = target.getCaretPosition();
                if ((target.getSelectionStart() == target.getSelectionEnd()) &&
                   (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset))) {
                    doc.runAtomicAsUser(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DocumentUtilities.setTypingModification(doc, true);
                                int indentLevelSize = IndentUtils.indentLevelSize(doc);
                                int column = Utilities.getVisualColumn(doc, offset);
                                // Truncate
                                column = (column / indentLevelSize) * indentLevelSize + indentLevelSize;
                                GsfUtilities.setLineIndentation(doc, offset, column);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                    return;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            // The behavior is correct when you have text on the line so just
            // delegate
            super.actionPerformed(evt, target);
        }
    }
}
