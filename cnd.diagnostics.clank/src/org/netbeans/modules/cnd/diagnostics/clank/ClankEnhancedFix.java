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
package org.netbeans.modules.cnd.diagnostics.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.clang.tools.services.ClankDiagnosticEnhancedFix;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 */
/*package*/ class ClankEnhancedFix implements EnhancedFix {

    private final ArrayList<EnhancedFixImpl> fixes = new ArrayList<>();

    public ClankEnhancedFix(CsmFile csmFile, Collection<ClankDiagnosticEnhancedFix> clankFixes) throws Exception {
        if (clankFixes.isEmpty()) {
            throw new IllegalArgumentException("Should contain at least one fix");//NOI18N
        }
        for (ClankDiagnosticEnhancedFix clankFix : clankFixes) {
            fixes.add(new EnhancedFixImpl(csmFile, clankFix));
        }
    }

    @Override
    public CharSequence getSortText() {
        return fixes.get(0).getSortText();
    }

    @Override
    public String getText() {
        return fixes.get(0).getText();
    }

    @Override
    public ChangeInfo implement() throws Exception {
        for (EnhancedFixImpl fix : fixes) {
            fix.implement();
        }
        return null;
    }

    private class EnhancedFixImpl implements EnhancedFix {
        private final AtomicBoolean isInitialized = new AtomicBoolean(false);
        private  final CsmFile file;
        private final ClankDiagnosticEnhancedFix clankFix;
        private  Position insertStartPosition;
        private  Position insertEndPosition;
        private  Position removeStartPosition;
        private  Position removeEndPosition;
        private  boolean isRemoveTokenRange;
        private  boolean isInsertRangeValid;
        private  boolean beforePreviousInsertions;
        private  String textToInsert;
        private  String text;

        EnhancedFixImpl(CsmFile csmFile, ClankDiagnosticEnhancedFix clankFix) throws Exception {
            this.file = csmFile; 
            this.clankFix = clankFix;            
            try{
                init();
            }catch (NullPointerException ex) {
                
            }
        }
        
        private void init() {
            if (isInitialized.get()) {
                return;
            }
            try {
                textToInsert = clankFix.getInsertionText();
                text = clankFix.getText();
                isInsertRangeValid = clankFix.isInsertRangeValid();
                isRemoveTokenRange = clankFix.isRemoveTokenRange();
                beforePreviousInsertions = clankFix.beforePreviousInsertions();
                Document document = CsmUtilities.getDocument(file);                
                insertStartPosition = NbDocument.createPosition(document, clankFix.getInsertStartOffset(), Position.Bias.Forward);
                insertEndPosition = NbDocument.createPosition(document, clankFix.getInsertEndOffset(), Position.Bias.Forward);
                removeStartPosition = NbDocument.createPosition(document, clankFix.getRemoveStartOffset(), Position.Bias.Forward);
                removeEndPosition = NbDocument.createPosition(document, clankFix.getRemoveEndOffset(), Position.Bias.Forward);
                isInitialized.set(true);            
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public CharSequence getSortText() {
            init();
            return text;
        }

        @Override
        public String getText() {
            init();
            return text;
        }

        @Override
        public ChangeInfo implement() throws Exception {            
            try {
                init();
                //see org.clang.rewrite.frontend.FixItRewriter for the logic
                /*
            if (Hint.CodeToInsert.empty()) {
            if (Hint.InsertFromRange.isValid()) {
            commit.insertFromRange(Hint.RemoveRange.getBegin(),
            new CharSourceRange(Hint.InsertFromRange),  false,
            Hint.BeforePreviousInsertions);
            } else {
            commit.remove(new CharSourceRange(Hint.RemoveRange));
            }
            } else {
            if (Hint.RemoveRange.isTokenRange()
            || $noteq_SourceLocation$C(Hint.RemoveRange.getBegin(), Hint.RemoveRange.getEnd())) {
            commit.replace(new CharSourceRange(Hint.RemoveRange), new StringRef(Hint.CodeToInsert));
            } else {
            commit.insert(Hint.RemoveRange.getBegin(), new StringRef(Hint.CodeToInsert),
            false, Hint.BeforePreviousInsertions);
            }
            }
                 */
                Document document = CsmUtilities.getDocument(file);
                if (textToInsert.isEmpty()) {
                    if (isInsertRangeValid) {
                        document.insertString(insertStartPosition.getOffset(), textToInsert, null);
                    } else {
                        document.remove(removeStartPosition.getOffset(), removeEndPosition.getOffset() - removeStartPosition.getOffset());
                    }
                } else {
                    if (isRemoveTokenRange || removeStartPosition.getOffset() != removeEndPosition.getOffset()) {
                        //replace
                        document.remove(removeStartPosition.getOffset(), removeEndPosition.getOffset() - removeStartPosition.getOffset());
                        document.insertString(removeStartPosition.getOffset(), textToInsert, null);
                    } else {
                        document.insertString(removeStartPosition.getOffset(), textToInsert, null);
                    }
                }
                //                //document.remove(0, 0);
                //                //check insertation range first
                //                if (insertStartPosition.getOffset() == 0 && insertEndPosition.getOffset() == 0) {
                //                    final int startPos = removeStartPosition.getOffset();
                //                    //will do replace
                //                    //remove if any
                //                    if (removeStartPosition.getOffset() != removeEndPosition.getOffset()) {
                //                        document.remove(startPos,
                //                                removeEndPosition.getOffset() - startPos + 1);
                //                    }
                //                    //insert
                //                    document.insertString(startPos, textToInsert, null);
                //                } else {
                //                    final int startPos = insertStartPosition.getOffset();
                //                    document.insertString(startPos, textToInsert, null);
                //                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

    }

}
