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
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder.StatementResult;
import org.netbeans.modules.cnd.refactoring.hints.StatementFinder.AddMissingCasesFixImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class SuggestionFactoryTask extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public SuggestionFactoryTask() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        if (cancel.isCancelled()) {
            return;
        }
        Collection<CodeAudit> audits = SuggestionProvider.getInstance().getAudits();
        boolean enabled = false;
        for(CodeAudit audit : audits) {
            if (audit.isEnabled()) {
                enabled = true;
                break;
            }
        }
        if (!enabled) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask started"); //NOI18N
            time = System.currentTimeMillis();
        }
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        final CsmFile file = CsmFileInfoQuery.getDefault().getCsmFile(result);
        if (file != null && doc != null && doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
            if (event instanceof CursorMovedSchedulerEvent) {
                CsmCacheManager.enter();
                process(audits, doc, fileObject, (CursorMovedSchedulerEvent)event, file, canceled);
                CsmCacheManager.leave();
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    private void process(Collection<CodeAudit> audits, final Document doc, final FileObject fileObject, CursorMovedSchedulerEvent cursorEvent, final CsmFile file, final AtomicBoolean canceled) {
        clearHint(doc, fileObject);
        int caretOffset = cursorEvent.getCaretOffset();
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        int selectionStart = caretOffset;
        int selectionEnd = caretOffset;
        if (comp != null) {
            selectionStart = Math.min(cursorEvent.getCaretOffset(),cursorEvent.getMarkOffset());//comp.getSelectionStart();
            selectionEnd = Math.max(cursorEvent.getCaretOffset(),cursorEvent.getMarkOffset());//comp.getSelectionEnd();
        }
        if (canceled.get())  {
            return;
        }
        boolean introduce = false;
        boolean assign = false;
        boolean cases = false;
        boolean inline = false;
        boolean pragma = false;
        boolean defineMacro = false;
        for(CodeAudit audit : audits) {
            if (IntroduceVariable.ID.equals(audit.getID()) && audit.isEnabled()) {
                introduce = true;
            } else if (AssignVariable.ID.equals(audit.getID()) && audit.isEnabled()) {
                assign = true;
            } else if (AddMissingCases.ID.equals(audit.getID()) && audit.isEnabled()) {
                cases = true;
            } else if (InstantInline.ID.equals(audit.getID()) && audit.isEnabled()) {
                inline = true;
            } else if (PragmaOnceAudit.ID.equals(audit.getID()) && audit.isEnabled()) {
                pragma = true;
            } else if (SurroundWithIfndefAudit.ID.equals(audit.getID()) && audit.isEnabled()) {
                defineMacro = true;
            }
        }
        if (assign || introduce) {
            detectIntroduceVariable(file, caretOffset, selectionStart, selectionEnd, doc, canceled, assign, fileObject, introduce, comp);
        }
        if (cases) {
            if (caretOffset > 0) {
                try {
                    String text = doc.getText(caretOffset-1, 1);
                    if (text.startsWith("{")) { //NOI18N
                        // probably it is switch
                        StatementFinder finder = new StatementFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
                        CsmStatement findStatement = finder.findStatement();
                        if (findStatement != null && findStatement.getKind() == CsmStatement.Kind.SWITCH) {
                            createSwitchHint(findStatement, doc, comp, fileObject, caretOffset);
                        }
                    }
                } catch (BadLocationException ex) {
                }
            }
        }
        if (inline) {
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, caretOffset);
            if (ref == null) {
                return;
            }
            if (CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                String replacement = CsmMacroExpansion.expand(doc, file, ref.getStartOffset(), ref.getEndOffset(), false);
                int refLine = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, ref.getStartOffset())[0];
                int objLine = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, ((CsmMacro) ref.getReferencedObject()).getStartOffset())[0];
                if (replacement != null && (!replacement.isEmpty()) && (refLine != objLine)) {
                    createInstantInlineHint(ref, doc, file, fileObject, replacement);
                }
            }
        }
        if (pragma) {
            CsmFileInfoQuery query = CsmFileInfoQuery.getDefault();
            if (file.isHeaderFile() && query.hasGuardBlock(file) && CndTokenUtilities.isInPreprocessorDirective(doc, caretOffset)) {
                final AtomicIntegerArray result = new AtomicIntegerArray(2);
                result.set(0, -1);
                result.set(1, -1);
                Runnable runnable = new Runnable () {
                    @Override
                    public void run() {
                        TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                        if (docTokenSequence == null) {
                            return;
                        }
                        
                        boolean isGuardMacro = false;
                        docTokenSequence.moveStart();
                        while(docTokenSequence.moveNext()) {
                            if (docTokenSequence.token().id() instanceof CppTokenId) {
                                CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                                if (tokenId.equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                                    TokenSequence<CppTokenId> preprocTokenSequence = docTokenSequence.embedded(CppTokenId.languagePreproc());
                                    if (preprocTokenSequence == null) {
                                        return;
                                    }
                                    
                                    preprocTokenSequence.moveStart();
                                    while(preprocTokenSequence.moveNext()) {
                                        switch(preprocTokenSequence.token().id()) {
                                            case PREPROCESSOR_PRAGMA:
                                                return;
                                            case PREPROCESSOR_IFNDEF:
                                                    result.set(0, preprocTokenSequence.offset());
                                                    isGuardMacro = true;
                                                    preprocTokenSequence.moveEnd();
                                                break;
                                            case PREPROCESSOR_IF:
                                                    isGuardMacro = true;
                                                break;
                                            case PREPROCESSOR_DEFINED:
                                                if (isGuardMacro) {
                                                    result.set(0, preprocTokenSequence.offset());
                                                    preprocTokenSequence.moveEnd();
                                                    break;
                                                }
                                                return;
                                            case PREPROCESSOR_DEFINE:
                                                if (isGuardMacro) {
                                                    result.set(1, preprocTokenSequence.offset());
                                                }
                                                    return;
                                            default:
                                                break;
                                        }
                                    }
                                } else if (!tokenId.primaryCategory().equals(CppTokenId.WHITESPACE_CATEGORY) 
                                        && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)) {
                                    isGuardMacro = false;
                                }
                            }
                        }
                    }
                };
                
                FutureTask<AtomicIntegerArray> atomicOffsetsArray = new FutureTask<>(runnable, result);
                doc.render(atomicOffsetsArray);
                
                try {
                    int startResult = atomicOffsetsArray.get().get(0);
                    int endResult = atomicOffsetsArray.get().get(1);
                    if (startResult != -1 && endResult != -1) {
                        int startGuardLine = query.getLineColumnByOffset(file, startResult)[0];
                        int guardStart = (int)query.getOffset(file, startGuardLine, 1);
                        int endGuardLine = query.getLineColumnByOffset(file, endResult)[0] + 1;
                        int guardEnd = (int)query.getOffset(file, endGuardLine, 1) - 1;
                        if (caretOffset >= guardStart && caretOffset <= guardEnd) {
                            createReplaceWithPragmaOnceHint(caretOffset, doc, fileObject, guardStart, guardEnd);
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (defineMacro) {
            CsmReference reference = CsmReferenceResolver.getDefault().findReference(file, doc, caretOffset);
            if (reference == null) {
                return;
            }
            if (CsmKindUtilities.isMacro(reference.getReferencedObject())) {
                CsmMacro macro = (CsmMacro) reference.getReferencedObject();
                if (caretOffset >= macro.getStartOffset() 
                        && caretOffset <= macro.getEndOffset() 
                        && macro.getKind().equals(CsmMacro.Kind.DEFINED)) {
                    final int caret = caretOffset;
                    final AtomicBoolean isLoneDefine = new AtomicBoolean(false);
                    Runnable runnable = new Runnable () {
                        @Override
                        public void run() {
                            TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, caret, false, true);
                            if (docTokenSequence == null) {
                                return;
                            }
                            docTokenSequence.movePrevious();
                            if (docTokenSequence.token().id() instanceof CppTokenId) {
                                CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                                if (tokenId.equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                                    TokenSequence<CppTokenId> preprocTokenSequence = docTokenSequence.embedded(CppTokenId.languagePreproc());
                                    if (preprocTokenSequence == null) {
                                        return;
                                    }
                                    // check wheter previouse token is #ifndef or #if macro
                                    preprocTokenSequence.moveStart();
                                    preprocTokenSequence.moveNext();
                                    preprocTokenSequence.moveNext();
                                    CppTokenId preprocTokenId = preprocTokenSequence.token().id();
                                    if (preprocTokenId.equals(CppTokenId.PREPROCESSOR_IFNDEF) 
                                            || preprocTokenId.equals(CppTokenId.PREPROCESSOR_IF)
                                            || preprocTokenId.equals(CppTokenId.PREPROCESSOR_ELIF)
                                            || preprocTokenId.equals(CppTokenId.PREPROCESSOR_ELSE)) {
                                        return;
                                    }
                                }
                            }                            
                            isLoneDefine.set(true);
                        }
                    };
                    
                    FutureTask<AtomicBoolean> checkLoneDefine = new FutureTask<>(runnable, isLoneDefine);
                    doc.render(checkLoneDefine);
                    try {
                        if (checkLoneDefine.get().get()) {
                            createSurroundWithIfndef(reference, doc, file, fileObject, macro.getName().toString());
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
    }

    private void detectIntroduceVariable(final CsmFile file, int caretOffset, int selectionStart, int selectionEnd, final Document doc, final AtomicBoolean canceled, boolean assign, final FileObject fileObject, boolean introduce, JTextComponent comp) {
        ExpressionFinder expressionFinder = new ExpressionFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
        StatementResult res = expressionFinder.findExpressionStatement();
        if (res == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        if (assign) {
            CsmExpressionStatement expression = res.getExpression();
            if (expression != null) {
                createStatementHint(expression, doc, fileObject);
            }
        }
        if (introduce) {
            if (res.getContainer() != null && res.getStatementInBody() != null && comp != null && selectionStart < selectionEnd) {
                if (/*CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionStart)[0] ==
                      CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionEnd)[0] &&
                    */expressionFinder.isExpressionSelection()) {
                    if (!(res.getContainer().getStartOffset() == selectionStart &&
                            res.getContainer().getEndOffset() == selectionEnd)) {
                        CsmOffsetable applicableTextExpression = expressionFinder.applicableTextExpression();
                        if (applicableTextExpression != null) {
                            createExpressionHint(res.getStatementInBody(), applicableTextExpression, doc, comp, fileObject);
                        }
                    }
                }
            }
        }
    }

    private void createStatementHint(CsmExpressionStatement expression, Document doc, FileObject fo) {
        List<Fix> fixes = Collections.<Fix>singletonList(new AssignmentVariableFix(expression.getExpression(), doc, fo));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        expression.getStartOffset(), expression.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
        
    }

    private void createExpressionHint(CsmStatement st, CsmOffsetable expression, Document doc, JTextComponent comp, FileObject fo) {
        List<Fix> fixes = Collections.<Fix>singletonList(new IntroduceVariableFix(st, expression, doc, comp, fo));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        expression.getStartOffset(), expression.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }

    private void createSwitchHint(CsmStatement st, Document doc, JTextComponent comp, FileObject fo, int caretOffset) {
        List<Fix> fixes = Collections.<Fix>singletonList(new AddMissingCasesFixImpl(st, doc, comp, fo, caretOffset));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        st.getStartOffset(), st.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }
    
    private void createInstantInlineHint(CsmReference ref, Document doc, CsmFile file, FileObject fo, String replacement) {
        List<Fix> fixes = Collections.<Fix>singletonList(new InlineFix(ref, doc, file, replacement));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "InstantInline.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        ref.getStartOffset(), ref.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }
    
    private void createReplaceWithPragmaOnceHint(int caret, Document doc, FileObject fo, int guardBlockStart, int guardBlockEnd) {
        List<Fix> fixes = Collections.<Fix>singletonList(new ReplaceWithPragmaOnce(doc, guardBlockStart, guardBlockEnd));
        String text = NbBundle.getMessage(ReplaceWithPragmaOnce.class, "HINT_Pragma"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, text, fixes, fo, caret, caret));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }
    
    private void createSurroundWithIfndef(CsmReference ref, Document doc, CsmFile file, FileObject fo, String macroIdentifier) {
        List<Fix> fixes = Collections.<Fix>singletonList(new SurroundWithIfndef(doc, file, ref, macroIdentifier));
        String text = NbBundle.getMessage(ReplaceWithPragmaOnce.class, "HINT_Ifndef"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, text, fixes, fo, ref.getStartOffset(), ref.getEndOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }
    
    private void clearHint(Document doc, FileObject fo) {
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), Collections.<ErrorDescription>emptyList());
    }
    
    @Override
    public int getPriority() {return 500;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask cancelled"); //NOI18N
        }
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
    })
    public static class SuggestionSourceFactory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new SuggestionFactoryTask());
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 1000)
    public static final class IntroduceVariable implements CodeAuditFactory {
        private static final String ID = "IntroduceVariable"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 2000)
    public static final class AssignVariable implements CodeAuditFactory {
        private static final String ID = "AssignVariable"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 3000)
    public static final class AddMissingCases implements CodeAuditFactory {
        private static final String ID = "AddMissingCases"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class InstantInline implements CodeAuditFactory {
        private static final String ID = "InstantInline"; // NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(InstantInline.class, "InstantInline.name"); // NOI18N
            String description = NbBundle.getMessage(InstantInline.class, "InstantInline.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 5000)
    public static final class PragmaOnceAudit implements CodeAuditFactory {
        private static final String ID = "ReplaceWithPragmaOnce"; // NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String text = NbBundle.getMessage(ReplaceWithPragmaOnce.class, "HINT_Pragma"); //NOI18N
            return new AbstractCodeAudit(ID, text, text, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 6000)
    public static final class SurroundWithIfndefAudit implements CodeAuditFactory {
        private static final String ID = "SurroundWithIfndef"; // NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String text = NbBundle.getMessage(SurroundWithIfndef.class, "HINT_Ifndef"); //NOI18N
            return new AbstractCodeAudit(ID, text, text, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
