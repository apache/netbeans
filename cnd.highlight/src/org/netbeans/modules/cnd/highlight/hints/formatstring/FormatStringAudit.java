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
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.hints.CsmHintProvider;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.highlight.hints.SafeFix;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class FormatStringAudit extends AbstractCodeAudit {
    
    private static final boolean CHECKS_ENABLED;
    private List<FormattedPrintFunction> result;
    
    static {
        String checksEnabled = System.getProperty("printf.check.enable"); //NOI18N
        if (checksEnabled != null) {
            CHECKS_ENABLED = Boolean.parseBoolean(checksEnabled);
        } else {
            CHECKS_ENABLED = true;
        }
    }
    
    private FormatStringAudit(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        final CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            Document doc_ = request.getDocument();
            if (doc_ == null) {
                CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                doc_ = CsmUtilities.openDocument(ces);
            }
            final Document doc = doc_;
            
            result = new LinkedList<>();
            CsmFileReferences.getDefault().accept(request.getFile()
                                                 ,request.getDocument()
                                                 ,new FormatStringAudit.ReferenceVisitor(request, response, doc, file)
                                                 ,CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        }
    }
    
    private class ReferenceVisitor implements CsmFileReferences.Visitor {
        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;
        private final CsmFile file;
        private final Document doc;
        
        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response, Document doc, CsmFile file) {
            this.request = request;
            this.response = response;
            this.file = file;
            this.doc = doc;
        }
        
        @Override
        public void visit(CsmReferenceContext context) {
            CsmReference reference =  context.getReference();
            if (reference != null) {
                if (Utilities.checkPrintf(reference.getText()) == -1) {
                    return;
                }
                CsmObject object = reference.getReferencedObject();
                final int formatStringPosition = Utilities.checkFormattedPrintFunction(object);
                if (formatStringPosition > -1) {
                    final int startOffset = reference.getStartOffset();
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, startOffset, false, true);
                            if (docTokenSequence == null) {
                                return;
                            }

                            CsmReferenceResolver rr = CsmReferenceResolver.getDefault();
                            State state = State.START;
                            int formatStringOffset = -1;
                            int innerBracketsCounter = 0;
                            int parameterPosition = 0;
                            int parameterOffset = -1;
                            boolean containsMacros = false;
                            StringBuilder formatString = null;
                            ArrayList<Parameter> parameters = new ArrayList<>();
                            StringBuilder parameterBuffer = new StringBuilder();
                            while (docTokenSequence.moveNext()) {
                                Token<TokenId> token = docTokenSequence.token();
                                TokenId tokenId = token.id();
                                if (state == State.START && tokenId.equals(CppTokenId.LPAREN)) {
                                    state = State.BEFOR_FORMAT;
                                } else if (tokenId.equals(CppTokenId.COMMA)) {
                                    parameterPosition++;

                                    if (state == State.FORMAT) { // if state is FORMAT it should be changed to VAR_ARGS
                                        state = State.VAR_ARGS;
                                    } else if (state == State.VAR_ARGS) { // if state is VAR_ARGS parameter should be stored
                                        parameters.add(new Parameter(parameterBuffer.toString(), parameterOffset, !containsMacros));
                                        // set default values
                                        assert (innerBracketsCounter == 0);
                                        containsMacros = false;
                                        parameterOffset = -1;
                                        parameterBuffer = new StringBuilder();
                                    }
                                } else if (state == State.BEFOR_FORMAT && parameterPosition == formatStringPosition) {
                                    state = State.FORMAT;
                                    formatString = new StringBuilder();
                                    if (tokenId.equals(CppTokenId.STRING_LITERAL)) {
                                        formatStringOffset = docTokenSequence.offset();
                                        formatString.append(token.text().toString());
                                    } else {
                                        return; // skip checking for complicated expressions
                                    }
                                } else if (state == State.FORMAT && tokenId.equals(CppTokenId.STRING_LITERAL)) {
                                    formatString.append(token.text().toString());
                                } else if (state == State.FORMAT && !tokenId.equals(CppTokenId.STRING_LITERAL)
                                                                 && !tokenId.equals(CppTokenId.RPAREN) 
                                                                 && !tokenId.primaryCategory().equals(CppTokenId.WHITESPACE_CATEGORY)
                                                                 && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)) {
                                    // skip checking for complicated expressions
                                    return; 
                                } else if ((state == State.VAR_ARGS || state == State.VAR_ARGS_IN_BRACKETS) 
                                                                    && !tokenId.equals(CppTokenId.LPAREN)
                                                                    && !tokenId.equals(CppTokenId.RPAREN)
                                                                    && !tokenId.primaryCategory().equals(CppTokenId.WHITESPACE_CATEGORY)
                                                                    && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)) {
                                    parameterBuffer.append(token.text());
                                    if (parameterOffset == -1) {
                                        parameterOffset = docTokenSequence.offset();
                                    }
                                    // do not resolve expression type if it contains macros
                                    CsmReference ref = rr.findReference(file, doc, docTokenSequence.offset());
                                    if (ref != null && CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                                        containsMacros = true;
                                    }
                                } else if (state == State.VAR_ARGS && tokenId.equals(CppTokenId.LPAREN)) {
                                    innerBracketsCounter++;
                                    state = State.VAR_ARGS_IN_BRACKETS;

                                    parameterBuffer.append(token.text());
                                    if (parameterOffset == -1) {
                                        parameterOffset = docTokenSequence.offset();
                                    }
                                } else if (state == State.VAR_ARGS_IN_BRACKETS && tokenId.equals(CppTokenId.LPAREN)) {
                                    innerBracketsCounter++;

                                    parameterBuffer.append(token.text());
                                    if (parameterOffset == -1) {
                                        parameterOffset = docTokenSequence.offset();
                                    }
                                } else if (state == State.VAR_ARGS_IN_BRACKETS && tokenId.equals(CppTokenId.RPAREN)) {
                                    innerBracketsCounter--;
                                    if (innerBracketsCounter == 0) {
                                        state = State.VAR_ARGS;
                                    }

                                    parameterBuffer.append(token.text());
                                    if (parameterOffset == -1) {
                                        parameterOffset = docTokenSequence.offset();
                                    }
                                } else if ((state == State.VAR_ARGS || state == State.FORMAT) && tokenId.equals(CppTokenId.RPAREN)) {
                                    if (parameterBuffer.length() > 0) {
                                        parameters.add(new Parameter(parameterBuffer.toString(), parameterOffset, !containsMacros));
                                    }
                                    addMessage(new FormattedPrintFunction(file
                                                                         ,formatStringOffset
                                                                         ,(formatString == null) ? "" : formatString.toString()
                                                                         ,parameters));
                                    return;
                                }
                            }
                        }
                    });
                }
            }
        }
        
        private void addMessage(FormattedPrintFunction function) throws MissingResourceException {
            List<FormatError> errors = new LinkedList<>(function.validate());
            for (FormatError error : errors) {
                CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                try {
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new FormatStringErrorInfoImpl(doc
                                                         ,CsmHintProvider.NAME, getID()
                                                         ,getName()+"\n"+Utilities.getMessageForError(error)
                                                         ,severity
                                                         ,error));
                    } else {
                        response.addError(new FormatStringErrorInfoImpl(doc
                                                                       ,CsmHintProvider.NAME
                                                                       ,getID()
                                                                       ,Utilities.getMessageForError(error)
                                                                       ,severity
                                                                       ,error));
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        @Override
        public boolean cancelled() {
            return request.isCancelled();
        }
        
    }
    
    private static enum State {
        START,
        BEFOR_FORMAT,
        FORMAT,
        VAR_ARGS,
        VAR_ARGS_IN_BRACKETS
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.name");  // NOI18N
            String description = NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.description");  // NOI18N
            return new FormatStringAudit(id, id, description, "error", CHECKS_ENABLED, preferences);  // NOI18N
        }
    }
    
    private static final class FormatStringErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final Position startPosition;
        private final Position endPosition;
        private final FormatError error;
        
        public FormatStringErrorInfoImpl(Document doc
                                        ,String providerName
                                        ,String audutName
                                        ,String message
                                        ,CsmErrorInfo.Severity severity
                                        ,FormatError error) throws BadLocationException {
            super(providerName, audutName, message, severity, error.startOffset(), error.endOffset());
            this.doc = (BaseDocument) doc;
            this.error = error;
            startPosition = NbDocument.createPosition(doc, this.error.startOffset(), Position.Bias.Forward);
            endPosition = NbDocument.createPosition(doc, this.error.endOffset(), Position.Bias.Backward);
        }
    }
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1600)
    public static final class FormatStringFixProvider extends CsmErrorInfoHintProvider {
        
        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof FormatStringErrorInfoImpl) {
                alreadyFound.addAll(createFixes((FormatStringErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(FormatStringErrorInfoImpl info) {
            try {
                List<Fix> fixes = new ArrayList<>();
                switch (info.error.getType()) {
                    case FLAG:
                    case LENGTH:
                    case TYPE_MISMATCH:
                        fixes.add(new FixFormat(info.doc, info.error, info.startPosition, info.endPosition));
                        break;
                    default:
                        break;
                }
                return fixes;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return Collections.emptyList();
            }
        }
    }
    
    private static final class FixFormat extends SafeFix {
        private final BaseDocument doc;
        private final FormatError error;
        private final Position start;
        private final Position end;
        private final String oldText;
        private final String newText;
        
        public FixFormat(BaseDocument doc, FormatError error, Position start, Position end) throws BadLocationException {
            this.doc = doc;
            this.error = error;
            this.start = start;
            this.end = end;
            int length = end.getOffset() - start.getOffset();
            oldText = doc.getText(start.getOffset(), length);
            if (error.getType().equals(FormatError.FormatErrorType.TYPE_MISMATCH)) {
                newText = getAppropriateFormat(error.getFlag());
            } else {
                newText = oldText.replace(error.getFlag(), ""); // NOI18N
            }
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.fix.flag", oldText, newText); // NOI18N
        }
        
        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            int length = end.getOffset() - start.getOffset();
            doc.replace(start.getOffset(), length, newText, null);
            return null;
        }
        
        private String getAppropriateFormat(String type) {
            List<String> formats = Utilities.typeToFormat(type);
            String specifier = error.getSpecifier(); 
            if (specifier.startsWith("l")) {         // NOI18N
                specifier = specifier.replace("l", "");          // NOI18N
            } else if (specifier.startsWith("h")) {  // NOI18N
                specifier = specifier.replace("h", "");          // NOI18N
            }
            for (String format : formats) {
                if (format.contains(specifier)) {
                    return format;
                }
            }
            if (formats.isEmpty()) {
                return "p";  // NOI18N
            }
            return formats.get(0);
        }
    }
    
}
