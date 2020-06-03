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
package org.netbeans.modules.cnd.highlight.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;

/**
 *
 */

public class MissingGuardBlock extends AbstractCodeAudit {
    
    private MissingGuardBlock(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }

    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }

    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file.isHeaderFile()) {            
            if (!CsmFileInfoQuery.getDefault().hasGuardBlock(file)) {
                Document doc_ = request.getDocument();
                if (doc_ == null) {
                    CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                    doc_ = CsmUtilities.openDocument(ces);
                }
                final Document doc = doc_;
                final AtomicInteger startOffset = new AtomicInteger(0);
                
                Runnable runnable = new Runnable () {
                    @Override
                    public void run() {
                        TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                        if (docTokenSequence == null) {
                            return;
                        }
                        docTokenSequence.moveStart();
                        
                        while (docTokenSequence.moveNext()) {
                            if (docTokenSequence.token().id() instanceof CppTokenId) {
                                CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                                switch (tokenId) {
                                    case LINE_COMMENT:
                                    case NEW_LINE:
                                    case DOXYGEN_LINE_COMMENT:
                                    case BLOCK_COMMENT:
                                    case DOXYGEN_COMMENT:
                                    case WHITESPACE:
                                    case ESCAPED_WHITESPACE:
                                    case ESCAPED_LINE:
                                        continue;
                                    default:
                                        startOffset.set(docTokenSequence.offset());
                                        return;
                                }
                            }
                        }
                        docTokenSequence.moveEnd();
                        docTokenSequence.movePrevious();
                        startOffset.set(docTokenSequence.offset());
                    }
                };
                
                doc.render(runnable);
                
                String message = NbBundle.getMessage(MissingGuardBlock.class, "MissingGuardBlock.description"); // NOI18N
                int start = startOffset.get();
                CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                if (response instanceof AnalyzerResponse) {
                    ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                        new MissingGuardBlock.MissingGuardBlockErrorInfoImpl(doc, file, CsmHintProvider.NAME, getID(), getName()+"\n"+message, severity, start));  // NOI18N
                } else {
                    response.addError(new MissingGuardBlock.MissingGuardBlockErrorInfoImpl(doc, file, CsmHintProvider.NAME, getID(), message, severity, start));
                }
            }
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(MissingGuardBlock.class, "MissingGuardBlock.name");  // NOI18N
            String description = NbBundle.getMessage(MissingGuardBlock.class, "MissingGuardBlock.description");  // NOI18N
            return new MissingGuardBlock(id, id, description, "warning", true, preferences);  // NOI18N
        }
    }
    
    private static final class MissingGuardBlockErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final CsmFile file;
        private final int insertionStart;
        
        public MissingGuardBlockErrorInfoImpl(Document doc, CsmFile file, String providerName, String audutName, String message, CsmErrorInfo.Severity severity, int startOffset) {
            super(providerName, audutName, message, severity, 0, 0);
            this.doc = (BaseDocument) doc;
            this.file = file;
            insertionStart = startOffset;
        }
    }
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1100)
    public static final class MissingGuardBlockFixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof MissingGuardBlock.MissingGuardBlockErrorInfoImpl) {
                alreadyFound.addAll(createFixes((MissingGuardBlock.MissingGuardBlockErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(MissingGuardBlock.MissingGuardBlockErrorInfoImpl info) {
            try {
                List<Fix> fixes = new ArrayList<>();
                fixes.add(new MissingGuardBlock.AddGuardBlock(info.doc, info.file, info.insertionStart));
                fixes.add(new MissingGuardBlock.AddPragmaOnce(info.doc, info.file, info.insertionStart));
                return fixes;
            } catch (BadLocationException ex) {
                return Collections.emptyList();
            }
        }
    }
    
    private static final class AddGuardBlock extends SafeFix {
        private final BaseDocument doc;
        private final CsmFile file;
        private final int startOffset;
        
        public AddGuardBlock (BaseDocument doc, CsmFile file, int startOffset) throws BadLocationException {
            this.doc = doc;
            this.file = file;
            this.startOffset = startOffset;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissingGuardBlock.class, "MissingGuardBlock.fix.block"); // NOI18N
        }
        
        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            // Strings to build guard block
            final String defName = file.getFileObject().getName().toUpperCase() + "_H\n";  // NOI18N
            final String ifndefMacro = "#ifndef ";  // NOI18N
            final String defineMacro = "#define ";  // NOI18N
            final String endifMacro = "#endif\t// ";  // NOI18N
            final String endifText = endifMacro + defName + "\n";  // NOI18N
            final String openGuardBlockText = ifndefMacro + defName + defineMacro + defName + "\n";  // NOI18N
            
            // offsets
            final int ifndefStartPos = startOffset + ifndefMacro.length();
            final int ifndefEndPos = ifndefStartPos + defName.length();
            final int defStartPos = ifndefEndPos + defineMacro.length();
            final int defEndPos = defStartPos + defName.length();
            
            Position ifndefPosition = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);
            doc.insertString(ifndefPosition.getOffset(), openGuardBlockText, null);
            Position endifPossition = NbDocument.createPosition(doc, file.getText().length(), Position.Bias.Backward);
            doc.insertString(endifPossition.getOffset(), "\n"+endifText, null); // NOI18N
            
            Position ifndefStart = NbDocument.createPosition(doc, ifndefStartPos, Position.Bias.Forward);
            Position ifndefEnd = NbDocument.createPosition(doc, ifndefEndPos-1, Position.Bias.Backward); // substracts 1 because of new line symols
            Position defineStart = NbDocument.createPosition(doc, defStartPos, Position.Bias.Forward);
            Position defineEnd = NbDocument.createPosition(doc, defEndPos-1, Position.Bias.Backward); // substracts 1 because of new line symols
            Position endifStart = NbDocument.createPosition(doc, endifPossition.getOffset()+endifMacro.length()+1, Position.Bias.Forward);
            Position endifEnd = NbDocument.createPosition(doc, endifPossition.getOffset()+endifMacro.length()+defName.length(), Position.Bias.Backward);
            
            final ChangeInfo changeInfo = new ChangeInfo();
            final FileObject fo = file.getFileObject();
            changeInfo.add(fo, ifndefStart, ifndefEnd);
            changeInfo.add(fo, defineStart, defineEnd);
            changeInfo.add(fo, endifStart, endifEnd);
            CsmRefactoringActionsFactory.performInstantRenameAction(EditorRegistry.lastFocusedComponent(), changeInfo);
            return null;
        }
    }
    
    private static final class AddPragmaOnce extends SafeFix {
        private final BaseDocument doc;
        private final CsmFile file;
        private final int offset;
        
        public AddPragmaOnce (BaseDocument doc, CsmFile file, int offset) throws BadLocationException {
            this.doc = doc;
            this.file = file;
            this.offset = offset;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissingGuardBlock.class, "MissingGuardBlock.fix.pragma"); // NOI18N
        }
        
        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            Position ifndefPosition = NbDocument.createPosition(doc, offset, Position.Bias.Forward);
            doc.insertString(ifndefPosition.getOffset(), "#pragma once\n\n", null); // NOI18N
            return null;
        }
    }
    
}
