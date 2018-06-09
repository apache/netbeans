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
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous
 */
public class VarDocSuggestion extends SuggestionRule {
    private static final Logger LOGGER = Logger.getLogger(VarDocSuggestion.class.getName());
    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor();

    @Override
    public String getId() {
        return "Var.Doc.Hint"; //NOI18N
    }

    @Override
    @Messages("VarDocHintDesc=Generate Type Comment For Variable")
    public String getDescription() {
        return Bundle.VarDocHintDesc();
    }

    @Override
    @Messages("VarDocHintDispName=Generate Type Comment For Variable /** @var MyClass $myvariable */")
    public String getDisplayName() {
        return Bundle.VarDocHintDispName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        final BaseDocument doc = context.doc;
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        FileObject fileObject = context.parserResult.getSnapshot().getSource().getFileObject();
        if (lineBounds.containsInclusive(caretOffset) && fileObject != null) {
            try {
                String identifier = Utilities.getIdentifier(doc, caretOffset);
                if (identifier != null && identifier.startsWith("$")) {
                    PHPParseResult parseResult = (PHPParseResult) context.parserResult;
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    Model model = parseResult.getModel();
                    VariableScope variableScope = model.getVariableScope(caretOffset);
                    if (variableScope != null) {
                        int wordStart = LineDocumentUtils.getWordStart(doc, caretOffset);
                        int wordEnd = LineDocumentUtils.getWordEnd(doc, caretOffset);
                        VariableName variable = ModelUtils.getFirst(variableScope.getDeclaredVariables(), identifier);
                        if (variable != null && (wordEnd - wordStart) == identifier.length()) {
                            final OffsetRange identifierRange = new OffsetRange(wordStart, wordEnd);
                            int offset = identifierRange.getEnd();
                            if (variable.getTypes(offset).isEmpty()) {
                                Collection<? extends String> typeNames = variable.getTypeNames(offset);
                                for (String type : typeNames) {
                                    if (!VariousUtils.isSemiType(type)) {
                                        return;
                                    }
                                }
                                hints.add(new Hint(VarDocSuggestion.this, getDisplayName(),
                                        fileObject, identifierRange,
                                        Collections.<HintFix>singletonList(new Fix(context, variable)), 500));
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }

    private class Fix implements HintFix {
        private final RuleContext context;
        private final VariableName vName;

        Fix(RuleContext context, VariableName vName) {
            this.context = context;
            this.vName = vName;
        }

        @Override
        public String getDescription() {
            return VarDocSuggestion.this.getDescription();
        }

        @Override
        public void implement() throws Exception {
            final BaseDocument doc = context.doc;
            final int caretOffset = getOffset(doc);
            final String commentText = getCommentText();
            final int indexOf = commentText.indexOf(getTypeTemplate());
            final EditList editList = getEditList(doc, caretOffset);
            final Position typeOffset = editList.createPosition(caretOffset + indexOf);
            editList.apply();
            if (typeOffset != null && typeOffset.getOffset() != -1) {
                JTextComponent target = GsfUtilities.getPaneFor(context.parserResult.getSnapshot().getSource().getFileObject());
                if (target != null) {
                    final int startOffset = typeOffset.getOffset();
                    final int endOffset = startOffset + getTypeTemplate().length();
                    if (indexOf != -1 && (endOffset <= doc.getLength())) {
                        String s = doc.getText(startOffset, getTypeTemplate().length());
                        if (getTypeTemplate().equals(s)) {
                            target.select(startOffset, endOffset);
                            scheduleShowingCompletion();
                        }

                    }
                }
            }
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        EditList getEditList(BaseDocument doc, int caretOffset) throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(caretOffset, 0, getCommentText(), true, 0);
            return edits;
        }

        private String getCommentText() {
            return String.format("%n/** @var %s %s */", getTypeTemplate(), vName.getName()); //NOI18N
        }

        private String getTypeTemplate() {
            return "type"; //NOI18N
        }

        private int getOffset(BaseDocument doc) throws BadLocationException {
            final int caretOffset = LineDocumentUtils.getLineStart(doc, context.caretOffset);
            return LineDocumentUtils.getLineEnd(doc, caretOffset - 1);
        }

        private void scheduleShowingCompletion() {
            SERVICE.schedule(new Runnable() {

                @Override
                public void run() {
                    Completion.get().showCompletion();
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    }
}
