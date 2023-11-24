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

package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import javax.tools.Diagnostic;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;
import org.netbeans.modules.java.hints.jdk.ConvertToDiamondBulkHint;
import org.netbeans.modules.java.hints.jdk.ConvertToLambda;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.source.parsing.Hacks;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;



/**
 * @author Jan Lahoda
 * @author leon chiver
 */
public final class ErrorHintsProvider extends JavaParserResultTask {
    
    public static ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.java.hints"); // NOI18N
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.java.hints"); // NOI18N
    
    public ErrorHintsProvider() {
        super(Phase.RESOLVED);
    }
    
    private static final Map<Diagnostic.Kind, Severity> errorKind2Severity;
    
    static {
        errorKind2Severity = new EnumMap<Diagnostic.Kind, Severity>(Diagnostic.Kind.class);
        errorKind2Severity.put(Diagnostic.Kind.ERROR, Severity.ERROR);
        errorKind2Severity.put(Diagnostic.Kind.MANDATORY_WARNING, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.WARNING, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.NOTE, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.OTHER, Severity.WARNING);
    }

    /**
     * @return errors for whole file
     */
    public List<ErrorDescription> computeErrors(CompilationInfo info, Document doc, String mimeType) throws IOException {
        return computeErrors(info, doc, null, mimeType);
    }
    
    ErrorDescription processRule(CompilationInfo info, Integer forPosition, Diagnostic d, String code, Map<String, List<ErrorRule>> code2Rules,
            Map<Class, Data> data, Document doc, boolean processDefault) throws IOException {
        List<ErrorRule> rules = code2Rules.get(code);
        List<ErrorRule> allRules = rules == null ? new ArrayList<ErrorRule>() : new ArrayList<>(rules);
        List<ErrorRule> catchAllRules = code2Rules.get("*");
        if (catchAllRules != null) {
            allRules.addAll(catchAllRules);
        }

        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "code= " + code);
            ERR.log(ErrorManager.INFORMATIONAL, "rules = " + rules);
        }

        LazyFixList ehm;
        String desc = d.getMessage(null);

        int pos = (int) getPrefferedPosition(info, d);
        TreePath path = info.getTreeUtilities().pathFor(pos + 1);
        Data ruleData = new Data();
        
        int messageRuleCount = 0;
        for (ErrorRule r : allRules) {
            if (!(r instanceof OverrideErrorMessage)) {
                continue;
            }
            OverrideErrorMessage rcm = (OverrideErrorMessage) r;
            Data rd = data.get(rcm.getClass());
            if (rd == null) {
                rd = ruleData;
            }
            String msg = rcm.createMessage(info, d, pos, path, rd);
            if (msg != null) {
                if (msg.isEmpty()) {
                    // ignore the error
                    return null;
                }
                desc = msg;
                break;
            }
            if (rd.getData() != null) {
                data.put(rcm.getClass(), rd);
                ruleData = new Data();
            }
        }
        if (messageRuleCount < allRules.size()) {
            ehm = new CreatorBasedLazyFixList(info.getFileObject(), code, desc, pos, allRules, data);
        } else if (processDefault) {
            ehm = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());
        } else {
            return null;
        }
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "ehm=" + ehm);
        }

        int endPos = (int) d.getEndPosition();

        final Position[] range = getLine(info, d, doc, pos, endPos < 0 ? pos : endPos);

        if (isCanceled()) {
            return null;
        }

        if (range == null || range[0] == null || range[1] == null) {
            return null;
        }

        if (forPosition != null) {
            try {
                int posRowStart = org.netbeans.editor.Utilities.getRowStart((NbEditorDocument) doc, forPosition);
                int errRowStart = org.netbeans.editor.Utilities.getRowStart((NbEditorDocument) doc, range[0].getOffset());
                if (posRowStart != errRowStart) {
                    return null;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return ErrorDescriptionFactory.createErrorDescription(errorKind2Severity.get(d.getKind()), desc, ehm, doc, range[0], range[1]);
    }
    
    /**
     * @param forPosition position for which errors would be computed
     * @return errors for line specified by forPosition
     * @throws IOException
     */
    List<ErrorDescription> computeErrors(CompilationInfo info, Document doc, Integer forPosition, String mimeType) throws IOException {
        if ("text/x-javahints".equals(mimeType)) {
            if (info.getText().startsWith("//no-errors")) return Collections.emptyList();
        }

        List<Diagnostic> errors = info.getDiagnostics();
        Set<ErrorDescription> descs = new LinkedHashSet<>();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "errors = " + errors );

        boolean isJava = org.netbeans.modules.java.hints.errors.Utilities.JAVA_MIME_TYPE.equals(mimeType);

        Map<Class, Data> data = new HashMap<>();

        OUTER: for (Diagnostic _d : errors) {
            if (ConvertToDiamondBulkHint.CODES.contains(_d.getCode())) {
                if (isJava) continue; //handled separatelly in the hint
                if (!ConvertToDiamondBulkHint.isHintEnabled()) continue; //disabled
            }
            
            if (ConvertToLambda.CODES.contains(_d.getCode())) {
                continue;
            }

            if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
                ERR.log(ErrorManager.INFORMATIONAL, "d = " + _d );
            Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors(mimeType);
            
            List<String> composedCodes = Collections.singletonList(_d.getCode());
            Diagnostic[] nested = Hacks.getNestedDiagnostics(_d);
            NESTED: {
                if (nested != null) {
                    composedCodes = new ArrayList<>(nested.length + 1);
                    composedCodes.add(_d.getCode());

                    StringBuilder b = new StringBuilder();
                    b.append(_d.getCode());
                    for (Diagnostic d2 : nested) {

                        if (isCanceled())
                            return null;

                        String code = d2.getCode();
                        b.append("/"); // NOI18N
                        b.append(code);

                        ErrorDescription ed = processRule(info, forPosition, _d, b.toString(), code2Rules, data, doc, false);
                        if (ed != null) {
                            descs.add(ed);
                            break NESTED;
                        }
                    }
                }
                if (isCanceled()) {
                    return null;
                }
                ErrorDescription ed = processRule(info, forPosition, _d, _d.getCode(), code2Rules, data, doc, true);
                if (ed != null) {
                    descs.add(ed);
                }
            }
        }        
        if (isCanceled()) {
            return null;
        }
        Set<Severity> disabled = org.netbeans.modules.java.hints.spiimpl.Utilities.disableErrors(info.getFileObject());
        List<ErrorDescription> result = new ArrayList<>(descs.size());

        for (ErrorDescription ed : descs) {
            if (!disabled.contains(ed.getSeverity())) {
                result.add(ed);
            }
        }

        if (isJava) {
            LazyHintComputationFactory.getAndClearToCompute(info.getFileObject());
        } else {
            for (ErrorDescription d : result) {
                d.getFixes().getFixes();
            }
        }
        
        return result;
    }
    
    public static Token findUnresolvedElementToken(CompilationInfo info, int offset) throws IOException {
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        
        if (ts == null) {
            return null;
        }
        
        ts.move(offset);
        if (ts.moveNext()) {
            Token<JavaTokenId> t = ts.token();

            if (t.id() == JavaTokenId.DOT) {
                ts.moveNext();
                t = ts.token();
            } else {
                if (t.id() == JavaTokenId.LT) {
                    ts.moveNext();
                    t = ts.token();
                } else {
                    if (t.id() == JavaTokenId.NEW || t.id() == JavaTokenId.WHITESPACE) {
                        t = skipWhitespaces(ts);

                        if (t == null) return null;
                    } else if (t.id() == JavaTokenId.IMPORT) {
                        t = skipWhitespaces(ts);

                        if (t == null) return null;
                    }
                }
            }

            while (t.id() == JavaTokenId.WHITESPACE) {
                ts.moveNext();
                t = ts.token();
            }
            
            switch (t.id()) {
                case IDENTIFIER:
                case INT_LITERAL:
                case LONG_LITERAL:
                case FLOAT_LITERAL:
                case DOUBLE_LITERAL:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                case TRUE:
                case FALSE:
                case NULL:
                    return ts.offsetToken();
            }
        }
        return null;
    }

    private static Token skipWhitespaces(TokenSequence<JavaTokenId> ts) {
        boolean cont = ts.moveNext();

        while (cont && ts.token().id() == JavaTokenId.WHITESPACE) {
            cont = ts.moveNext();
        }

        if (!cont) {
            return null;
        }

        return ts.token();
    }
    
    private static int[] findUnresolvedElementSpan(CompilationInfo info, int offset) throws IOException {
        Token t = findUnresolvedElementToken(info, offset);
        
        if (t != null) {
            return new int[] {
                t.offset(null),
                t.offset(null) + t.length()
            };
        }
        
        return null;
    }
    
    public static TreePath findUnresolvedElement(CompilationInfo info, int offset) throws IOException {
        int[] span = findUnresolvedElementSpan(info, offset);
        
        if (span != null) {
            return info.getTreeUtilities().pathFor(span[0] + 1);
        } else {
            return null;
        }
    }
    
    private static final Set<String> INVALID_METHOD_INVOCATION = new HashSet<String>(Arrays.asList(
        "compiler.err.prob.found.req",
        "compiler.err.cant.apply.symbol",
        "compiler.err.cant.apply.symbol.1",
//        "compiler.err.cant.resolve.location",
        "compiler.err.cant.resolve.location.args"
    ));
    
    private static final Set<String> CANNOT_RESOLVE = new HashSet<String>(Arrays.asList(
            "compiler.err.cant.resolve",
            "compiler.err.cant.resolve.location",
            "compiler.err.cant.resolve.location.args",
            "compiler.err.doesnt.exist",
            "compiler.err.type.error"
    ));
    
    private static final Set<String> UNDERLINE_IDENTIFIER = new HashSet<String>(Arrays.asList(
            "compiler.err.local.var.accessed.from.icls.needs.final",
            "compiler.err.var.might.not.have.been.initialized",
            "compiler.err.report.access",
            "compiler.err.does.not.override.abstract",
            "compiler.err.abstract.cant.be.instantiated",
            "compiler.warn.missing.SVUID",
            "compiler.warn.has.been.deprecated",
            "compiler.warn.raw.class.use",
            "compiler.err.class.public.should.be.in.file",
            "compiler.err.cant.ref.non.effectively.final.var"
    ));
    
    private static final Set<String> USE_PROVIDED_SPAN = new HashSet<String>(Arrays.asList(
            "compiler.err.method.does.not.override.superclass",
            "compiler.err.illegal.unicode.esc",
            "compiler.err.unreported.exception.need.to.catch.or.throw"
    ));

    private static final Set<JavaTokenId> WHITESPACE = EnumSet.of(JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE);
    
    private int[] handlePossibleMethodInvocation(CompilationInfo info, Diagnostic d, final Document doc, int startOffset, int endOffset) throws IOException {
        int pos = (int) getPrefferedPosition(info, d);
        TreePath tp = info.getTreeUtilities().pathFor(pos + 1);
        
        if (tp != null && tp.getParentPath() != null && tp.getParentPath().getLeaf() != null && (tp.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION || tp.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS)) {
            int[] index = new int[1];
            
            tp = tp.getParentPath();
            
            if (!Utilities.fuzzyResolveMethodInvocation(info, tp, new ArrayList<TypeMirror>(), index).isEmpty()) {
                Tree a;
                
                if (tp.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
                    
                    a = mit.getArguments().get(index[0]);
                } else {
                    NewClassTree mit = (NewClassTree) tp.getLeaf();
                    
                    a = mit.getArguments().get(index[0]);
                }

                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), a);
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), a);
            
                return new int[] {start, end};
            }
        }
        
        return null;
    }
    
    /**
     * Helper class, which performs related document inspection under doc readlock.
     * Factored out from getLine method, because either the whole method would turn
     * to one big Runnable (this class), or two Runnables would need to exchange
     * data through final arrays which is awkward.,
     */
    class PosExtractor implements Runnable {
        private final CompilationInfo info;
        private int startOffset;
        private int endOffset;
        private final boolean rangePrepared;
        private final StyledDocument sdoc;
        private final DataObject dobj;
        
        private String text;
        private int lineOffset;
        private Position[] result = new Position[2];

        public PosExtractor(CompilationInfo info, StyledDocument doc, int startOffset, int endOffset, DataObject dobj, boolean rangePrepared) {
            this.info = info;
            this.sdoc = doc;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.rangePrepared = rangePrepared;
            this.dobj = dobj;
        }
        
        private void findText() {
            final int lineNumber = NbDocument.findLineNumber(sdoc, info.getSnapshot().getOriginalOffset(startOffset));
            lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);

            if (rangePrepared) {
                return;
            }
            if (dobj == null) {
                sdoc.render(new Runnable() {
                    public void run() {
                        javax.swing.text.Element root = NbDocument.findLineRootElement(sdoc);
                        if (root.getElementCount() <= lineNumber) {
                            text = null;
                        } else{
                            try {
                                javax.swing.text.Element line = root.getElement(lineNumber);
                                text = sdoc.getText(line.getStartOffset(), line.getEndOffset() - line.getStartOffset());
                            } catch (BadLocationException ex) {
                                text = null;
                            }
                        }
                    }
                });
            } else {
                LineCookie lc = dobj.getCookie(LineCookie.class);
                Line line = lc.getLineSet().getCurrent(lineNumber);
                text = line.getText();
            }

            if (text == null) {
                //#116560, (according to the javadoc, means the document is closed):
                cancel();
                return;
            }
        }
        
        private int state;
        
        public String getText() {
            state = 0;
            sdoc.render(this);
            return text;
        }
        
        public Position[] getResult(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            state = 1;
            sdoc.render(this);
            
            return result;
        }
        
        private void findResult() {
            
            if (isCanceled())
                return;

            int len = sdoc.getLength();

            if (startOffset >= len || endOffset > len) {
                if (!isCanceled() && ERR.isLoggable(ErrorManager.WARNING)) {
                    ERR.log(ErrorManager.WARNING, "document changed, but not canceled?" );
                    ERR.log(ErrorManager.WARNING, "len = " + len );
                    ERR.log(ErrorManager.WARNING, "startOffset = " + startOffset );
                    ERR.log(ErrorManager.WARNING, "endOffset = " + endOffset );
                }
                cancel();

                return;
            }

            try {
                result[0] = NbDocument.createPosition(sdoc, startOffset, Bias.Forward);
                result[1] = NbDocument.createPosition(sdoc, endOffset, Bias.Backward);
            } catch (BadLocationException e) {
                ERR.notify(ErrorManager.ERROR, e);
            }
        }
        
        public void run() {
            switch (state) {
                case 0:
                    findText();
                    break;
                case 1:
                    findResult();
                    break;
            }
        }
        
    }
    
    private Position[] getLine(CompilationInfo info, Diagnostic d, final Document doc, int startOffset, int endOffset) throws IOException {
        StyledDocument sdoc = (StyledDocument) doc;
        FileObject f = EditorDocumentUtils.getFileObject(doc);
        if (f == null)
            return new Position[] {null, null};
        
        Object rawProp = doc.getProperty(doc.StreamDescriptionProperty );
        DataObject dObj = rawProp instanceof DataObject ? (DataObject)rawProp : null;
        
        int originalStartOffset = info.getSnapshot().getOriginalOffset(startOffset);
        int soff = startOffset;
        boolean rangePrepared = false;

        if (INVALID_METHOD_INVOCATION.contains(d.getCode())) {
            int[] span = translatePositions(info, handlePossibleMethodInvocation(info, d, doc, soff, endOffset));
            
            if (span != null) {
                soff = span[0];
                endOffset = span[1];
                rangePrepared = true;
            }
        }
        
        if (CANNOT_RESOLVE.contains(d.getCode()) && !rangePrepared) {
            int[] span = translatePositions(info, findUnresolvedElementSpan(info, (int) getPrefferedPosition(info, d)));
            
            if (span != null) {
                soff = span[0];
                endOffset   = span[1];
                rangePrepared = true;
            }
        }
        
        if (UNDERLINE_IDENTIFIER.contains(d.getCode())) {
            int[] span = findIdentifierSpan(info, soff);
            if (span != null) {
                soff = span[0];
                endOffset   = span[1];
                rangePrepared = true;
            }
        }
        
        if ("compiler.err.illegal.unicode.esc".equals(d.getCode())) {
            String text = info.getText();
            endOffset = info.getSnapshot().getOriginalOffset((int) d.getEndPosition());
            soff = endOffset;
            while (text.charAt(soff) != '\\') {
                soff--;
            }
            rangePrepared = true;
        }

        // check that the start offset and end offset map into the document
        if (!rangePrepared && (info.getSnapshot().getOriginalOffset(startOffset) == -1 ||
            info.getSnapshot().getOriginalOffset(endOffset) == -1)) {
            // ignore
            return null;
        }
        
        if ("compiler.err.preview.feature.disabled.plural".equals(d.getCode())) {
            //workaround for: JDK-8310314
            Diagnostic[] nested = Hacks.getNestedDiagnostics(d);
            if ("compiler.misc.feature.unnamed.classes".equals(nested[0].getCode())) {
                if (endOffset == info.getText().length()) {
                    ClassTree topLevelClass = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
                    TreePath topLevelClassTP = new TreePath(new TreePath(info.getCompilationUnit()), topLevelClass);
                    Tree firstNonClass = topLevelClass.getMembers().stream().filter(t -> !TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())).filter(t -> !info.getTreeUtilities().isSynthetic(new TreePath(topLevelClassTP, t))).findFirst().orElse(null);

                    if (firstNonClass != null) {
                        soff = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), firstNonClass);
                        endOffset = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), firstNonClass);
                    }
                }
            }
        }

        PosExtractor ex = new PosExtractor(info, sdoc, soff, endOffset, dObj, rangePrepared);
        // getText also fetches lineOffset
        final String text = ex.getText();
        final int lineOffset = ex.lineOffset;
        
        if (isCanceled()) {
            return null;
        }

        if (!rangePrepared && d.getCode().endsWith("proc.messager")) {
            int originalEndOffset = info.getSnapshot().getOriginalOffset(endOffset);

            if (originalEndOffset <= lineOffset + text.length() && originalStartOffset != (-1) && originalEndOffset != (-1)) {
                soff = originalStartOffset;
                endOffset = originalEndOffset;
                rangePrepared = true;
            }
        }
        
        if (!rangePrepared && USE_PROVIDED_SPAN.contains(d.getCode())) {
            soff = originalStartOffset;
            endOffset = info.getSnapshot().getOriginalOffset(endOffset);
            rangePrepared = true;
        }
        
        if (!rangePrepared || endOffset < soff) {
            int column = 0;
            int length = text.length();

            while (column < text.length() && Character.isWhitespace(text.charAt(column)))
                column++;

            while (length > 0 && Character.isWhitespace(text.charAt(length - 1)))
                length--;

            if(length == 0) //whitespace only
                soff = lineOffset;
            else
                soff = lineOffset + column;

            endOffset = lineOffset + length;
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "startOffset = " + soff );
            ERR.log(ErrorManager.INFORMATIONAL, "endOffset = " + endOffset );
        }
        if (soff < 0) {
            LOG.log(Level.WARNING, "Incorrect offsets computed, add report to https://netbeans.org/bugzilla/show_bug.cgi?id=242191");
            LOG.log(Level.WARNING, "Diagnostic: {0}, code: {1} ", new Object[] { d, d.getCode() });
            LOG.log(Level.WARNING, "Start: {0}, End: {1}, Soff: {2}, RangePrepared: {3}", new Object[] { startOffset, endOffset, soff, rangePrepared });
            LOG.log(Level.WARNING, "Source text:\n--------------------------------------------------------\n" 
                    + info.getSnapshot().getText() + 
                    "\n--------------------------------------------------------");
        }
        return ex.getResult(soff, endOffset);
    }
    
    private boolean cancel;
    
    synchronized boolean isCanceled() {
        return cancel;
    }
    
    public synchronized void cancel() {
        cancel = true;
    }
    
    synchronized void resume() {
        cancel = false;
    }
    
    @Override
    public void run(Result result, SchedulerEvent event) {
        resume();

        CompilationInfo info = CompilationInfo.get(result);

        if (info == null) {
            return ;
        }

        Document doc = result.getSnapshot().getSource().getDocument(false);
        
        if (doc == null) {
            Logger.getLogger(ErrorHintsProvider.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return ;
        }

        long version = DocumentUtilities.getDocumentVersion(doc);
        String mimeType = result.getSnapshot().getSource().getMimeType();
        
        long start = System.currentTimeMillis();

        try {
            List<ErrorDescription> errors = computeErrors(info, doc, mimeType);

            if (errors == null) //meaning: cancelled
                return ;
            EmbeddedHintsCollector.setAnnotations(result.getSnapshot(), errors);

            ErrorPositionRefresherHelper.setVersion(doc, errors);
            
            long end = System.currentTimeMillis();

            Logger.getLogger("TIMER").log(Level.FINE, "Java Hints",
                    new Object[]{info.getFileObject(), end - start});
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    private static int[] translatePositions(CompilationInfo info, int[] span) {
        if (span == null || span[0] == (-1) || span[1] == (-1))
            return null;
        
        int start = info.getSnapshot().getOriginalOffset(span[0]);
        int end   = info.getSnapshot().getOriginalOffset(span[1]);
        
        if (start == (-1) || end == (-1) || end < start)
            return null;
        
        return new int[] {start, end};
    }
    
    public static long getPrefferedPosition(CompilationInfo info, Diagnostic d) throws IOException {
        if ("compiler.err.doesnt.exist".equals(d.getCode()) || "compiler.err.try.with.resources.expr.needs.var".equals(d.getCode())) {
            return d.getStartPosition();
        }
        if ("compiler.err.cant.resolve.location".equals(d.getCode()) || "compiler.err.cant.resolve.location.args".equals(d.getCode())) {
            int[] span = findUnresolvedElementSpan(info, (int) d.getPosition());
            
            if (span != null) {
                return span[0];
            } else {
                return d.getPosition();
            }
        }
        if ("compiler.err.not.stmt".equals(d.getCode())) {
            //check for "Collections.":
            TreePath path = findUnresolvedElement(info, (int) d.getStartPosition() - 1);
            Element el = path != null ? info.getTrees().getElement(path) : null;
            
            if (el == null || el.asType().getKind() == TypeKind.ERROR) {
                return d.getStartPosition() - 1;
            }
            /*
            if (el.asType().getKind() == TypeKind.PACKAGE) {
                //check if the package does actually exist:
                String s = ((PackageElement) el).getQualifiedName().toString();
                if (info.getElements().getPackageElement(s) == null) {
                    //it does not:
                    return d.getStartPosition() - 1;
                }
            }
                    */
            
            return d.getStartPosition();
        }
        if ("compiler.err.var.might.not.have.been.initialized".equals(d.getCode())) {
            int[] span = findIdentifierSpan(info, (int) d.getPosition());
            if (span == null) {
                Object param = SourceUtils.getDiagnosticParam(d, 0);
                if (param instanceof VariableElement) {
                    TreePath path = info.getTrees().getPath((VariableElement) param);
                    if (path != null && path.getLeaf().getKind() == Kind.VARIABLE) {
                        span = info.getTreeUtilities().findNameSpan((VariableTree) path.getLeaf());
                        if (span != null) {
                            return span[0];
                        }
                    }
                }
            }
        }
        
        return d.getPosition();
    }

    private static int[] findIdentifierSpan(CompilationInfo info, int offset) {
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        int diff = ts.move(offset);

        if (ts.moveNext() && diff >= 0 && diff < ts.token().length()) {
            Token<JavaTokenId> t = ts.token();

            if (t.id() == JavaTokenId.DOT) {
                while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                    ;
                t = ts.token();
            }

            if (t.id() == JavaTokenId.NEW) {
                while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                    ;
                t = ts.token();
            }

            if (t.id() == JavaTokenId.CLASS) {
                while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                    ;
                t = ts.token();
            }

            if (t.id() == JavaTokenId.IDENTIFIER) {
                return translatePositions(info, new int[] {ts.offset(), ts.offset() + t.length()});
            }
        }
        return null;
    }
}

