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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.cnd.refactoring.spi.CsmChangeParametersExtraObjectsProvider;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers). Based on Java refactoring
 *
 */
public class ChangeParametersPlugin extends CsmModificationRefactoringPlugin {

    private final ChangeParametersRefactoring refactoring;
    // objects affected by refactoring
    private Collection<CsmObject> referencedObjects;

    public ChangeParametersPlugin(ChangeParametersRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }

    private Collection<CsmObject> getRefactoredObjects() {
        return referencedObjects == null ? Collections.<CsmObject>emptyList() : Collections.unmodifiableCollection(referencedObjects);
    }

    private CsmFile getStartCsmFile() {
        CsmFile startFile = CsmRefactoringUtils.getCsmFile(getStartReferenceObject());
        if (startFile == null) {
            if (getEditorContext() != null) {
                startFile = getEditorContext().getFile();
            }
        }
        return startFile;
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        Collection<? extends CsmObject> objs = getRefactoredObjects();
        if (objs == null || objs.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<CsmFile> files = new HashSet<>();
        CsmFile startFile = getStartCsmFile();
        for (CsmObject obj : objs) {
            Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(obj, null);
            CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
            refactoring.getContext().add(ar);
            files.addAll(getRelevantFiles(startFile, obj, refactoring));
        }
        return files;
    }

    @Override
    public Problem fastCheckParameters() {
        ParameterInfo paramTable[] = refactoring.getParameterInfo();
        Problem p = null;
        for (int i = 0; i < paramTable.length; i++) {
            int origIndex = paramTable[i].getOriginalIndex();

            if (origIndex == -1) {
                // check parameter name
                CharSequence s;
                s = paramTable[i].getName();
                if ((s == null || s.length() < 1)) {
                    p = createProblem(p, true, newParMessage("ERR_parname")); // NOI18N
                } else {
                    if (!CndLexerUtilities.isCppIdentifier(s)) {
                        p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier", s)); // NOI18N
                    }
                }

                // check parameter type
                CharSequence t = paramTable[i].getType();
                if (t == null) {
                    p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N
                }
                // check the default value
                s = paramTable[i].getDefaultValue();
                if ((s == null || s.length() < 1)) {
                    p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N
                }
            }
            ParameterInfo in = paramTable[i];

            if (in.getType() != null && in.getType().toString().endsWith("...") && i != paramTable.length - 1) {//NOI18N
                p = createProblem(p, true, org.openide.util.NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[]{}));
            }
        }
        return p;
    }

    private static String newParMessage(String par) {
        return new MessageFormat(getString("ERR_newpar")).format(new Object[]{getString(par)}); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPlugin.class, key);
    }

    private CsmObject getRefactoredCsmElement() {
        CsmObject out = getStartReferenceObject();
        if (out == null) {
            CsmContext editorContext = getEditorContext();
            if (editorContext != null) {
                out = editorContext.getEnclosingFunction();
            }
        }
        return out;
    }

    /**
     * Returns list of problems. For the change function signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 5);
        CsmRefactoringUtils.waitParsedAllProjects();
        fireProgressListenerStep();
        // check if resolved element
        CsmObject refactoredCsmElement = getRefactoredCsmElement();
        preCheckProblem = isResovledElement(refactoredCsmElement);
        fireProgressListenerStep();
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        // check if valid element
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(refactoredCsmElement);
        // support only functions and not destructor
        if (!CsmKindUtilities.isFunction(directReferencedObject)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }
        if (CsmKindUtilities.isDestructor(directReferencedObject)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeDestructorParamsWrongType"));
            return preCheckProblem;
        }
        // we do not support constructors completely yet
        if (CsmKindUtilities.isConstructor(directReferencedObject)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "MSG_ChangeConstructorParamsWrongType"));
        }
        
        // create additional objects to resolve
        if (this.referencedObjects == null) {
            initReferencedObjects(directReferencedObject);
            fireProgressListenerStep();
        }
        // check read-only elements
        preCheckProblem = checkIfModificationPossible(preCheckProblem, directReferencedObject);
        fireProgressListenerStop();
        return preCheckProblem;
    }

    private void initReferencedObjects(CsmObject directReferencedObject) {
        CsmObject primaryObject = CsmRefactoringUtils.getReferencedElement(directReferencedObject);
        if (primaryObject != null) {
            Collection<CsmObject> allObjects = new HashSet<>();
            allObjects.add(primaryObject);
            for (CsmChangeParametersExtraObjectsProvider provider : Lookup.getDefault().lookupAll(CsmChangeParametersExtraObjectsProvider.class)) {
                allObjects.addAll(provider.getExtraObjects(primaryObject));
            }
            this.referencedObjects = new LinkedHashSet<>();
            for (CsmObject referencedObject : allObjects) {
                if (CsmKindUtilities.isMethod(referencedObject) && !CsmKindUtilities.isConstructor(referencedObject)) {
                    CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
                    this.referencedObjects.add(method);
                    if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                        this.referencedObjects.addAll(CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, true));
                        assert !this.referencedObjects.isEmpty() : "must be at least start object " + method;
                    }
                } else {
                    this.referencedObjects.add(referencedObject);
                }
            }
        }
    }

    @Override
    protected final void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        Collection<? extends CsmObject> refObjects = getRefactoredObjects();
        assert refObjects != null && refObjects.size() > 0 : "method must be called for resolved element";
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        Collection<CsmReference> refs = new LinkedHashSet<>();
        for (CsmObject obj : refObjects) {
            // do not interrupt refactoring
            Collection<CsmReference> curRefs = CsmReferenceRepository.getDefault().getReferences(obj, csmFile, CsmReferenceKind.ALL, Interrupter.DUMMY);
            refs.addAll(curRefs);
        }
        if (refs.size() > 0) {
            List<CsmReference> sortedRefs = new ArrayList<>(refs);
            Collections.sort(sortedRefs, new Comparator<CsmReference>() {

                @Override
                public int compare(CsmReference o1, CsmReference o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
            processRefactoredReferences(sortedRefs, fo, ces, mr, outProblem);
        }
    }

    private boolean needSpaceAfterComma() {
        // TODO consult formatting
        return true;
    }

    private void processRefactoredReferences(List<CsmReference> sortedRefs, FileObject fo, CloneableEditorSupport ces, ModificationResult mr, AtomicReference<Problem> outProblem) {
        ParameterInfo[] parameterInfo = refactoring.getParameterInfo();
        for (CsmReference ref : sortedRefs) {
            String oldName = ref.getText().toString();
            String descr = getDescription(ref, oldName);
            Difference diff = changeFunRef(ref, ces, oldName, parameterInfo, descr, outProblem);
            if (diff != null) {
                mr.addDifference(fo, diff);
            }
        }
    }

    private String getDescription(CsmReference ref, String targetName) {
        boolean decl = CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
        String out = NbBundle.getMessage(CsmRenameRefactoringPlugin.class, decl ? "UpdateSignature" : "UpdateFunRef", targetName);
        return out;
    }

    private Difference changeFunRef(CsmReference ref, CloneableEditorSupport ces,
            String oldName, ParameterInfo[] parameterInfo, String descr, AtomicReference<Problem> outProblem) {
//        if (outProblem.get() == null) {
//            Problem problem = createProblem(outProblem.get(), false, "something is broken " + ref);
//            outProblem.set(problem);
//            descr = "<html><b>" + descr + "</b></html>";
//        }
        Document document = CsmUtilities.openDocument(ces);
        FunctionInfo funInfo = prepareFunctionInfo(ref, document);
        Difference diff;
        final StringBuilder oldText = new StringBuilder();
        final StringBuilder newText = new StringBuilder();
        final int startOffset;
        final int endOffset;
        if (!funInfo.isValid()) {
            // this is a pointer to function, not function call
            // don't need to change something, but show it to user
            if (oldName == null) {
                oldName = ref.getText().toString();
            }
            oldText.append(oldName);
            newText.append(oldName);
            startOffset = ref.getStartOffset();
            endOffset = ref.getEndOffset();
        } else {
            boolean decl = CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
            boolean def = CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DEFINITION));
            startOffset = funInfo.getStartOffset();
            endOffset = funInfo.getEndOffset();
            boolean skipComma = true;
            final boolean setDefaultValue = refactoring.isUseDefaultValueOnlyInFunctionDeclaration();
            boolean wereChanges = (parameterInfo.length < funInfo.getNrParameters());
            oldText.append(funInfo.getOriginalParamsText());
            newText.append("(");// NOI18N
            // TODO: varargs
            for (int i = 0; i < parameterInfo.length; i++) {
                ParameterInfo pi = parameterInfo[i];
                int originalIndex = pi.getOriginalIndex();
                if (originalIndex == -1) {
                    if (!skipComma) {
                        if (!decl && setDefaultValue) {
                            skipComma = true;
                        }
                    }
                    if (!skipComma) {
                        newText.append(","); // NOI18N
                    }
                    if (!skipComma && needSpaceAfterComma()) {
                        newText.append(" "); // NOI18N
                    }
                    skipComma = false;
                    // new parameter
                    if (decl) {
                        boolean defValueInSignature = setDefaultValue;
                        if (defValueInSignature && def) {
                            // check if there is only one function declaration or more
                            // if only one => change it anyway
                            defValueInSignature = false;
                            CsmFunction fun = (CsmFunction) ref.getReferencedObject();
                            if (fun != null) {
                                if (fun.getDeclaration() == fun.getDefinition()) {
                                    defValueInSignature = true;
                                }
                            }
                        }
                        // in declaration add parameter
                        newText.append(pi.getType()).append(" ").append(pi.getName()); // NOI18N
                        if (setDefaultValue) {
                            if (defValueInSignature) {
                                newText.append(" = ").append(pi.getDefaultValue()); // NOI18N
                            } else {
                                newText.append(" /* = ").append(pi.getDefaultValue()).append(" */"); // NOI18N
                            }
                        }
                        wereChanges = true;
                    } else if (!setDefaultValue) {
                        // in reference add default value
                        newText.append(pi.getDefaultValue());
                        wereChanges = true;
                    } else {
                        skipComma = true;
                    }
                } else if (funInfo.hasParam(originalIndex)) {
                    if (!skipComma) {
                        newText.append(","); // NOI18N
                    }
                    wereChanges |= (originalIndex != i); // swap of params change
                    CharSequence origText = funInfo.getParameter(originalIndex);
                    if (i == 0) {
                        origText = origText.toString().trim();
                    }
                    if (!skipComma && needSpaceAfterComma() && !Character.isWhitespace(origText.charAt(0))) {
                        newText.append(" "); // NOI18N
                    }
                    skipComma = false;
                    newText.append(origText);
                }
            }
            newText.append(")");// NOI18N
            if (!wereChanges) {
                return null;
            }
        }
        assert startOffset <= endOffset;
        diff = CsmRefactoringUtils.rename(startOffset, endOffset, ces, oldText.toString(), newText.toString(), descr);

        return diff;
    }

    private FunctionInfo prepareFunctionInfo(final CsmReference ref, final Document doc) {
        final FunParamsTokenProcessor tp = new FunParamsTokenProcessor(doc);
        if (doc != null) {
            doc.render(new Runnable() {
                @Override
                public void run() {
                    CndTokenUtilities.processTokens(tp, doc, ref.getStartOffset(), doc.getLength());
                }
            });
        }
        return tp.getFunctionInfo();
    }

    private final static class FunctionInfo {
        private int startOffset = -1;
        private int endOffset = -1;
        private CharSequence origParamsText = "";
        private List<CharSequence> paramText = new ArrayList<>();

        public CharSequence getOriginalParamsText() {
            return origParamsText;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        private int getNrParameters() {
            return paramText.size();
        }
                
        private void addParam(String param) {
            paramText.add(param);
        }

        public void setStartOffsetIfNeeded(int startOffset) {
            if (this.startOffset < 0) {
                this.startOffset = startOffset;
                this.endOffset = startOffset;
            }
        }

        private boolean hasParam(int index) {
            return index < paramText.size();
        }

        private CharSequence getParameter(int index) {
            return paramText.get(index);
        }

        private boolean isValid() {
            return endOffset > startOffset;
        }

        private void setEndOffset(int offset) {
            endOffset = offset;
        }

        @Override
        public String toString() {
            return origParamsText + "[" + startOffset + "-" + endOffset +"] params:" + paramText; // NOI18N
        }
    }
    
    private final static class FunParamsTokenProcessor implements CndTokenProcessor<Token<TokenId>> {

        enum State {
            START,IN_PARAMS,END
        }
        private State state = State.START;
        private BlockConsumer blockConsumer;
        private final FunctionInfo funInfo;
        private final Document doc;
        private Boolean inPP = null;
        private int curParamStartOffset = -1;
        private FunParamsTokenProcessor(Document doc) {
            funInfo = new FunctionInfo();
            this.doc = doc;
        }

        @Override
        public boolean isStopped() {
            return state == State.END;
        }

        public FunctionInfo getFunctionInfo() {
            return funInfo;
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            if (blockConsumer != null) {
                if (blockConsumer.isLastToken(token)) {
                    blockConsumer = null;
                }
                return false;
            }
            if (inPP == null) {
                if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                    inPP = Boolean.TRUE;
                    return true;
                } else {
                    inPP = Boolean.FALSE;
                }
            } else if (inPP == Boolean.FALSE) {
                if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                    return false;
                }
            }
            switch (state) {
                case START:
                    skipName(token, tokenOffset);
                    break;
                case IN_PARAMS:
                    inParams(token, tokenOffset);
                    break;
            }
            return false;
        }

        @Override
        public void start(int startOffset, int firstTokenOffset, int lastOffset) {

        }

        @Override
        public void end(int offset, int lastTokenOffset) {
            if (funInfo.startOffset != funInfo.endOffset) {
                try {
                    funInfo.origParamsText = doc.getText(funInfo.startOffset, funInfo.endOffset - funInfo.startOffset);
                } catch (BadLocationException ex) {
                    // skip
                }
            }
        }

        private void inParams(Token<TokenId> token, int offset) {
            TokenId tokenID = token.id();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case LPAREN:
                        blockConsumer = new BlockConsumer(CppTokenId.LPAREN, CppTokenId.RPAREN);
                        break;
                    case LBRACE:
                        blockConsumer = new BlockConsumer(CppTokenId.LBRACE, CppTokenId.RBRACE);
                        break;
                    case LBRACKET:
                        blockConsumer = new BlockConsumer(CppTokenId.LBRACKET, CppTokenId.RBRACKET);
                        break;
                    case COMMA:
                        finishParam(offset);
                        startParam(offset);
                        break;
                    case RPAREN:
                        finishParam(offset);
                        state = State.END;
                        break;
                    case SEMICOLON:
                        // something broken
                        finishParam(offset);
                        state = State.END;
                }
            }
        }

        private void skipName(Token<TokenId> token, int offset) {
            TokenId tokenID = token.id();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case LPAREN:
                        state = State.IN_PARAMS;
                        startParam(offset);
                        break;
                    case LT:
                        blockConsumer = new BlockConsumer(CppTokenId.LT, CppTokenId.GT);
                        break;
                    case SEMICOLON:
                    case RPAREN:
                        state = State.END;
                        break;
                }
            }
        }

        private void finishParam(int offset) {
            assert curParamStartOffset > 0;
            try {
                String paramText = (offset <= curParamStartOffset) ? "" : doc.getText(curParamStartOffset, offset - curParamStartOffset); // NOI18N
                if (!funInfo.paramText.isEmpty() || paramText.trim().length() != 0) {
                    funInfo.addParam(paramText);
                }
            } catch (BadLocationException ex) {
                //skip;
            }
            funInfo.setEndOffset(offset+1);
        }

        private void startParam(int offset) {
            funInfo.setStartOffsetIfNeeded(offset);
            curParamStartOffset = offset + 1;
        }

        private static class BlockConsumer {

            private final CppTokenId openBracket;
            private final CppTokenId closeBracket;
            private int depth;

            public BlockConsumer(CppTokenId openBracket, CppTokenId closeBracket) {
                this.openBracket = openBracket;
                this.closeBracket = closeBracket;
                depth = 0;
            }

            public boolean isLastToken(Token<TokenId> token) {
                boolean stop = false;
                if (token.id() == openBracket) {
                    ++depth;
                } else if (token.id() == closeBracket) {
                    --depth;
                    stop = depth <= 0;
                }
                return stop;
            }
        }
    }
}
