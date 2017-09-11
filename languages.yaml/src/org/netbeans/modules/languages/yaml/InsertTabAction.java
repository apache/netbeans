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
package org.netbeans.modules.languages.yaml;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class InsertTabAction extends BaseAction {

    private static final List<Action> CUSTOM_ACTIONS = new LinkedList<Action>();

    static {
        CUSTOM_ACTIONS.add(new InsertTabAction());
    }

    public InsertTabAction() {
        super(BaseKit.insertTabAction);
    }

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        BaseKit.InsertTabAction insertTabAction = new BaseKit.InsertTabAction();
        insertTabAction.actionPerformed(evt, target);
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            final Caret caret = target.getCaret();
            final BaseDocument doc = (BaseDocument) target.getDocument();
            doc.runAtomic(new TabReplacer(doc, caret.getDot()));
        }
    }

    private static class TabReplacer implements Runnable {
        private static final String TAB_CHARACTER = "\t"; //NOI18N
        private static final Logger LOGGER = Logger.getLogger(TabReplacer.class.getName());
        private final BaseDocument baseDocument;
        private final int caretOffset;

        public TabReplacer(final BaseDocument baseDocument, final int caretOffset) {
            this.baseDocument = baseDocument;
            this.caretOffset = caretOffset;
        }

        @Override
        public void run() {
            try {
                tryReplaceTab();
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        private void tryReplaceTab() throws BadLocationException {
            int rowFirstNonWhite = Utilities.getRowFirstNonWhite(baseDocument, caretOffset);
            if (shouldBeReplaced(rowFirstNonWhite, caretOffset)) {
                replaceTab();
            }
        }

        private void replaceTab() throws BadLocationException {
            final int rowStart = Utilities.getRowStart(baseDocument, caretOffset);
            assert caretOffset >= rowStart : "Caret: " + caretOffset + " rowStart: " + rowStart;
            final String indentString = baseDocument.getText(rowStart, caretOffset - rowStart);
            if (indentString.contains(TAB_CHARACTER)) {
                final String newIndentString = indentString.replace(TAB_CHARACTER, IndentUtils.getIndentString(IndentUtils.getIndentSize(baseDocument)));
                baseDocument.replace(rowStart, caretOffset - rowStart, newIndentString, null);
            }
        }

        private static boolean shouldBeReplaced(final int firstNonWhiteCharOffset, final int caretOffset) {
            return firstNonWhiteCharOffset >= caretOffset || firstNonWhiteCharOffset == -1;
        }

    }

    public static List<Action> createCustomActions() {
        return CUSTOM_ACTIONS;
    }

}
