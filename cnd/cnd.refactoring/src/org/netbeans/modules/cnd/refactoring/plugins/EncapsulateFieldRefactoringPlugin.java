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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.FormattingSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.DeclarationGenerator;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils.InsertInfo;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.refactoring.ui.InsertPoint;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class EncapsulateFieldRefactoringPlugin extends CsmModificationRefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger(EncapsulateFieldRefactoringPlugin.class.getName());

    // objects affected by refactoring
    private Collection<CsmObject> referencedObjects;
    
    private CsmClass fieldEncloser;
    /**
     * most restrictive accessibility modifier on tree path 
     */
    private CsmVisibility fieldEncloserAccessibility = CsmVisibility.PUBLIC;
    /**
     * present accessibility of field
     */
    private CsmVisibility fieldAccessibility;
    private CsmMethod currentGetter;
    private CsmMethod currentSetter;
    private static Set<CsmVisibility> accessModifiers = EnumSet.of(CsmVisibility.PRIVATE, CsmVisibility.PROTECTED, CsmVisibility.PUBLIC);
    private static List<CsmVisibility> MODIFIERS = Arrays.asList(CsmVisibility.PRIVATE, null, CsmVisibility.PROTECTED, CsmVisibility.PUBLIC);
    private final EncapsulateFieldRefactoring refactoring;
    public static final String CLASS_FIELD_PREFIX = "_"; // NOI18N
    /**
     * path in source with field declaration; refactoring.getSelectedObject()
     * may contain path to a reference
     */
    private CsmObject sourceType;
    
    /** Creates a new instance of RenameRefactoring */
    public EncapsulateFieldRefactoringPlugin(EncapsulateFieldRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 3);
        CsmRefactoringUtils.waitParsedAllProjects();
        fireProgressListenerStep();
        try {
            CsmField field = refactoring.getSourceField();
            Problem result = checkIfModificationPossible(null, field);
            if (result != null) {
                return result;
            }
            fireProgressListenerStep();
//            if (ElementKind.FIELD == field.getKind()) {
//               TreePath tp = javac.getTrees().getPath(field);
//               sourceType = TreePathHandle.create(tp, javac);
//            } else {
//                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongType"));
//            }
//            if (!RetoucheUtils.isElementInOpenProject(sourceType.getFileObject())) {
//                return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoring.class, "ERR_ProjectNotOpened"));
//            }
//
            fieldEncloser = field.getContainingClass();
            fieldAccessibility = field.getVisibility();
//
//            fieldAccessibility = field.getModifiers();
//            fieldEncloserAccessibility = resolveVisibility(fieldEncloser);
//
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        return fastCheckParameters(refactoring.getGetterName(), refactoring.getSetterName());
    }

    @Override
    public Problem checkParameters() {
        Problem p = null;
        CsmField field = refactoring.getSourceField();
        CsmClass clazz = field.getContainingClass();
        String getname = refactoring.getGetterName();
        String setname = refactoring.getSetterName();
        CsmMethod getter = null;
        CsmMethod setter = null;

        if (getname != null) {
            getter = findMethod(clazz, getname, Collections.<CsmVariable>emptyList(), true);
        }

        if (getter != null) {
            if (!GeneratorUtils.isSameType(field.getType(), getter.getReturnType())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongGetter", // NOI18N
                        getname,
                        getter.getReturnType().getCanonicalText());
                p = createProblem(p, false, msg);
            }
            if (clazz.equals(getter.getContainingClass())) {
                currentGetter = getter;
            }
        }

        if (setname != null) {
            setter = findMethod(clazz, setname, Collections.singletonList(field), true);
        }

        if (setter != null) {
            if (GeneratorUtils.getTypeKind(setter.getReturnType()) != GeneratorUtils.TypeKind.VOID) {
                p = createProblem(p, false, NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongSetter", // NOI18N
                        setname,
                        setter.getReturnType().getCanonicalText()));
            }

            if (clazz.equals(setter.getContainingClass())) {
                currentSetter = setter;
            }
        }
        return p;
    }

    private void addDiff(InsertInfo declInsert, DeclarationGenerator.Kind kind, CharSequence text, final String mtdName, String bundle, ModificationResult mr, FileObject fo) throws MissingResourceException {
        CharSequence declText = FormattingSupport.getFormattedText(CsmUtilities.openDocument(declInsert.ces), declInsert.dot, text);
        String descr = NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, bundle, mtdName); // NOI8N
        String prefix = "\n"; // NOI18N
        if (kind == DeclarationGenerator.Kind.EXTERNAL_DEFINITION || 
                kind == DeclarationGenerator.Kind.INLINE_DEFINITION ||
                kind == DeclarationGenerator.Kind.INLINE_DEFINITION_MAKRED_INLINE) {
            prefix = "\n\n";// NOI18N
        }
        Difference declDiff = new Difference(Difference.Kind.INSERT, declInsert.start, declInsert.end, bundle, prefix + declText, descr); // NOI18N
        mr.addDifference(fo, declDiff);
    }
    
    private Problem fastCheckParameters(String getter, String setter) {
        
        if ((getter != null && !CndLexerUtilities.isCppIdentifier(getter))
                || (setter != null && !CndLexerUtilities.isCppIdentifier(setter))
                || (getter == null && setter == null)) {
            // user doesn't use valid java identifier, it cannot be used
            // as getter/setter name
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethods"));
        } else {
            // we have no problems
            return null;
        }
    }

    private FieldAccessInfo prepareFieldAccessInfo(final CsmReference ref, final Document doc) {
        final FieldAccessTokenProcessor tp = new FieldAccessTokenProcessor(ref.getStartOffset(), doc);
        if (doc != null) {
            doc.render(new Runnable() {

                @Override
                public void run() {
                    try {
                        int start = CndTokenUtilities.getLastCommandSeparator(doc, ref.getStartOffset());
                        CndTokenUtilities.processTokens(tp, doc, start, doc.getLength());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
        return tp.getFieldAccessInfo();
    }

    private final static class FieldAccessInfo {

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

        public List<CharSequence> getParametersText() {
            return paramText;
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
            return origParamsText + "[" + startOffset + "-" + endOffset + "] params:" + paramText; // NOI18N
        }
    }

    private final static class FieldAccessTokenProcessor implements CndTokenProcessor<Token<TokenId>> {

        enum State {

            START, AFTER_FIELD_ACCESS, LVALUE, END
        }
        private State state = State.START;
        private BlockConsumer blockConsumer;
        private final int refStartPos;
        private final FieldAccessInfo fldInfo;
        private final Document doc;
        private Boolean inPP = null;
        private int curParamStartOffset = -1;

        private FieldAccessTokenProcessor(int refStartPos, Document doc) {
            fldInfo = new FieldAccessInfo();
            this.doc = doc;
            this.refStartPos = refStartPos;
        }

        @Override
        public boolean isStopped() {
            return state == State.END;
        }

        public FieldAccessInfo getFieldAccessInfo() {
            return fldInfo;
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
                    if (tokenOffset == refStartPos) {
                        state = State.AFTER_FIELD_ACCESS;
                        fldInfo.startOffset = tokenOffset;
                        fldInfo.origParamsText = token.text();
                    }
                    break;
                case AFTER_FIELD_ACCESS:
                    afterFieldAccess(token, tokenOffset);
                    break;
            }
            return false;
        }

        private void afterFieldAccess(Token<TokenId> token, int offset) {
            if (!isWS(token)) {
                TokenId tokenID = token.id();
                if(tokenID instanceof CppTokenId) {
                    switch ((CppTokenId)tokenID) {
                        case LPAREN:
                            blockConsumer = new BlockConsumer(CppTokenId.LT, CppTokenId.GT);
                            break;
                        case SEMICOLON:
                        case RPAREN:
                            state = State.END;
                            break;
                        case EQ:
                            state = State.LVALUE;
                            break;
                    }
                }
            }
        }

        private boolean isWS(Token<TokenId> t) {
            TokenId tokenID = t.id();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case WHITESPACE:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case LINE_COMMENT:
                    case ESCAPED_LINE:
                    case ESCAPED_WHITESPACE:
                        return true;
                }
            }
            return false;
        }

        @Override
        public void start(int startOffset, int firstTokenOffset, int lastOffset) {
        }

        @Override
        public void end(int offset, int lastTokenOffset) {
            if (fldInfo.startOffset != fldInfo.endOffset) {
                try {
                    fldInfo.origParamsText = doc.getText(fldInfo.startOffset, fldInfo.endOffset - fldInfo.startOffset);
                } catch (BadLocationException ex) {
                    // skip
                }
            }
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
//    private CsmVisibility resolveVisibility(CsmClass clazz) {
//        NestingKind nestingKind = clazz.getNestingKind();
//
//        if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
//            return CsmVisibility.PRIVATE;
//        }
//
//        Set<CsmVisibility> mods = clazz.getModifiers();
//        if (nestingKind == NestingKind.TOP_LEVEL) {
//            return mods.contains(CsmVisibility.PUBLIC)
//                    ? CsmVisibility.PUBLIC
//                    : null;
//        }
//
//        if (mods.contains(CsmVisibility.PRIVATE)) {
//            return CsmVisibility.PRIVATE;
//
//        }
//        CsmVisibility mod1 = resolveVisibility((CsmClass) clazz.getEnclosingElement());
//        CsmVisibility mod2 = null;
//        if (mods.contains(CsmVisibility.PUBLIC)) {
//            mod2 = CsmVisibility.PUBLIC;
//        } else if (mods.contains(CsmVisibility.PROTECTED)) {
//            mod2 = CsmVisibility.PROTECTED;
//        }
//
//        return max(mod1, mod2);
//    }
//
//    private CsmVisibility max(CsmVisibility a, CsmVisibility b) {
//        if (a == b) {
//            return a;
//        }
//        int ai = MODIFIERS.indexOf(a);
//        int bi = MODIFIERS.indexOf(b);
//        return ai > bi? a: b;
//    }
//
//    private static CsmVisibility getAccessibility(Set<CsmVisibility> mods) {
//        if (mods.isEmpty()) {
//            return null;
//        }
//        Set<CsmVisibility> s = new HashSet<CsmVisibility>(mods);
//        s.retainAll(accessModifiers);
//        return s.isEmpty()? null: s.iterator().next();
//    }
//
//    private static Set<CsmVisibility> replaceAccessibility(CsmVisibility currentAccess, CsmVisibility futureAccess, Element elm) {
//        Set<CsmVisibility> mods = new HashSet<CsmVisibility>(elm.getModifiers());
//        if (currentAccess != null) {
//            mods.remove(currentAccess);
//        }
//        if (futureAccess != null) {
//            mods.add(futureAccess);
//        }
//        return mods;
//    }
//
    public static CsmMethod findMethod(CsmClass clazz, String name, Collection<? extends CsmVariable> params, boolean includeSupertypes) {
        if (name == null || name.length() == 0) {
            return null;
        }

        CsmClass c = clazz;
//        while (true) {
            for (CsmMember elm : c.getMembers()) {
                if (CsmKindUtilities.isMethod(elm)) {
                    CsmMethod m = (CsmMethod) elm;
                    if (name.contentEquals(m.getName()) && compareParams(params, m.getParameters())
                            /*&& isAccessible(clazz, m)*/) {
                        return m;
                    }
                }
            }
//
//            TypeMirror superType = c.getSuperclass();
//            if (!includeSupertypes || superType.getKind() == TypeKind.NONE) {
                return null;
//            }
//            c = (CsmClass) ((DeclaredType) superType).asElement();
//        }
    }

    /**
     * returns true if elm is accessible from clazz. elm must be member of clazz
     * or its superclass
     */
//    private static boolean isAccessible(CompilationInfo javac, CsmClass clazz, Element elm) {
//        if (clazz == elm.getEnclosingElement()) {
//            return true;
//        }
//        Set<CsmVisibility> mods = elm.getModifiers();
//        if (mods.contains(CsmVisibility.PUBLIC) || mods.contains(CsmVisibility.PROTECTED)) {
//            return true;
//        } else if (mods.contains(CsmVisibility.PRIVATE)) {
//            return false;
//        }
//        Elements utils = javac.getElements();
//        return utils.getPackageOf(elm) == utils.getPackageOf(clazz);
//    }
    
    private static boolean compareParams(Collection<? extends CsmVariable> params1, Collection<? extends CsmVariable> params2) {
        if (params1.size() == params2.size()) {
            Iterator<? extends CsmVariable> it1 = params1.iterator();
            for (CsmVariable ve2 : params2) {
                CsmVariable ve1 = it1.next();
                CsmType type2 = ve2.getType();
                CsmType type1 = ve1.getType();
                if ((type2 == null && type1 == null) || !GeneratorUtils.isSameType(type2, type1)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        return Collections.emptySet();
    }

//    @Override
//    protected Collection<CsmObject> getRefactoredObjects() {
//        return referencedObjects;
//    }

    @Override
    protected void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        // add declaration/definition to file
        InsertPoint insPt = refactoring.getContext().lookup(InsertPoint.class);
        CsmField field = refactoring.getSourceField();
        CsmFile classDeclarationFile = refactoring.getClassDeclarationFile();
        CsmFile classDefinitionFile = refactoring.getClassDefinitionFile();
        CloneableEditorSupport ces = null;
        FileObject fo = null;

        final String getterName = refactoring.getGetterName();
        final String setterName = refactoring.getSetterName();
        // prepare to generate declaration/definition
        if (((getterName != null && refactoring.getDefaultGetter() == null) || (setterName != null && refactoring.getDefaultSetter() == null)) && (csmFile.equals(classDeclarationFile) || csmFile.equals(classDefinitionFile))) {
//      SortBy sortBy = refactoring.getContext().lookup(SortBy.class);
            fo = CsmUtilities.getFileObject(csmFile);
            CsmClass enclosing = refactoring.getEnclosingClass();
            InsertInfo[] insertPositons = GeneratorUtils.getInsertPositons(null, enclosing, insPt);
            if (csmFile.equals(classDeclarationFile)) {
                DeclarationGenerator.Kind declKind;
                if (refactoring.isMethodInline()) {
                    declKind = DeclarationGenerator.Kind.INLINE_DEFINITION;
                    Document doc = CsmUtilities.getDocument(fo);
                    if (CodeStyle.getDefault(CodeStyle.Language.CPP, doc).getUseInlineKeyword()) {
                        declKind = DeclarationGenerator.Kind.INLINE_DEFINITION_MAKRED_INLINE;
                    }
                } else {
                    declKind = DeclarationGenerator.Kind.DECLARATION;
                }
                // create declaration
                InsertInfo declInsert = insertPositons[0];
                if (getterName != null && refactoring.getDefaultGetter() == null) {
                    CharSequence text = DeclarationGenerator.createGetter(field, getterName, declKind);
                    addDiff(declInsert, declKind, text,
                            getterName,
                            refactoring.isMethodInline() ? "EncapsulateFieldInlineDefinition" : "EncapsulateFieldInsertDeclartion", // NOI18N
                            mr, fo);
                }
                if (setterName != null && refactoring.getDefaultSetter() == null) {
                    CharSequence text = DeclarationGenerator.createSetter(field, setterName, declKind);
                    addDiff(declInsert, declKind, text,
                            setterName,
                            refactoring.isMethodInline() ? "EncapsulateFieldInlineDefinition" : "EncapsulateFieldInsertDeclartion", // NOI18N
                            mr, fo);
                }
            }
            if (!refactoring.isMethodInline() && csmFile.equals(classDefinitionFile)) {
                // create definition
                DeclarationGenerator.Kind defKind = DeclarationGenerator.Kind.EXTERNAL_DEFINITION;
                InsertInfo defInsert = insertPositons[1];
                if (getterName != null && refactoring.getDefaultGetter() == null) {
                    CharSequence text = DeclarationGenerator.createGetter(field, getterName, defKind);
                    addDiff(defInsert, defKind, text,
                            getterName,
                            "EncapsulateFieldInsertDefinition", // NOI18N
                            mr, fo);
                }
                if (setterName != null && refactoring.getDefaultSetter() == null) {
                    CharSequence text = DeclarationGenerator.createSetter(field, setterName, defKind);
                    addDiff(defInsert, defKind, text,
                            setterName,
                            "EncapsulateFieldInsertDefinition", // NOI18N
                            mr, fo);
                }
            }
        }
        // change references
        if (refactoring.isAlwaysUseAccessors()) {
            fo = fo != null ? fo : CsmUtilities.getFileObject(csmFile);
            ces = ces != null ? ces : CsmUtilities.findCloneableEditorSupport(csmFile);
            // do not interrupt refactoring
            Collection<CsmReference> refs = CsmReferenceRepository.getDefault().getReferences(field, csmFile, CsmReferenceKind.ALL, Interrupter.DUMMY);
            if (refs.size() > 0) {
                List<CsmReference> sortedRefs = new ArrayList<>(refs);
                Collections.sort(sortedRefs, new Comparator<CsmReference>() {

                    @Override
                    public int compare(CsmReference o1, CsmReference o2) {
                        return o1.getStartOffset() - o2.getStartOffset();
                    }
                });
                processRefactoredReferences(sortedRefs, fo, ces, mr, outProblem);
            }
        }
    }

    private void processRefactoredReferences(List<CsmReference> sortedRefs, FileObject fo, CloneableEditorSupport ces, ModificationResult mr, AtomicReference<Problem> outProblem) {
        for (CsmReference curRef : sortedRefs) {
            CsmObject encl = CsmRefactoringUtils.getEnclosingElement(curRef);
            // change in functions, but not in constructors/destructors
            if (CsmKindUtilities.isFunction(encl)) {
                if (!CsmKindUtilities.isConstructor(encl) && CsmKindUtilities.isDestructor(encl)
                    && !encl.equals(refactoring.getDefaultGetter()) && !encl.equals(refactoring.getDefaultSetter())) {
//                    FieldAccessInfo fldAccess = prepareFieldAccessInfo(curRef, getDoc(ces));
                }
            }
        }
    }
//    @Override
//    public Problem prepare(RefactoringElementsBag bag) {
//
//        fireProgressListenerStart(AbstractRefactoring.PREPARE, 9);
//        try {
//            fireProgressListenerStep();
//
//            EncapsulateDesc desc = prepareEncapsulator(null);
//            if (desc.p != null && desc.p.isFatal()) {
//                return desc.p;
//            }
//
//            Encapsulator encapsulator = new Encapsulator(
//                    Collections.singletonList(desc), desc.p,
//                    refactoring.getContext().lookup(InsertPoint.class),
//                    refactoring.getContext().lookup(SortBy.class),
//                    refactoring.getContext().lookup(Javadoc.class)
//                    );
//
//            Problem problem = createAndAddElements(
//                    desc.refs,
//                    new TransformTask(encapsulator, desc.fieldHandle),
//                    bag, refactoring);
//
//            return problem != null ? problem : encapsulator.getProblem();
//        } finally {
//            fireProgressListenerStop();
//        }
//    }
    
//    EncapsulateDesc prepareEncapsulator(Problem previousProblem) {
//        Set<FileObject> refs = getRelevantFiles();
//        EncapsulateDesc etask = new EncapsulateDesc();
//
//        if (refactoring.isAlwaysUseAccessors()
//                && refactoring.getMethodModifiers().contains(CsmVisibility.PRIVATE)
//                // is reference fromother files?
//                && refs.size() > 1) {
//            // breaks code
//            etask.p = createProblem(previousProblem, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsAccess"));
//            return etask;
//        }
//        if (refactoring.isAlwaysUseAccessors()
//                // is default accessibility?
//                && getAccessibility(refactoring.getMethodModifiers()) == null
//                // is reference fromother files?
//                && refs.size() > 1) {
//            // breaks code likely
//            etask.p = createProblem(previousProblem, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsDefaultAccess"));
//        }
//
//        etask.fieldHandle = sourceType;
//        etask.refs = refs;
//        etask.currentGetter = currentGetter;
//        etask.currentSetter = currentSetter;
//        etask.refactoring = refactoring;
//        return etask;
//    }
    
//    private Set<FileObject> getRelevantFiles() {
//        // search class index just in case Use accessors even when the field is accessible == true
//        // or the field is accessible:
//        // * private eclosers|private field -> CP: .java (project) => JavaSource.forFileObject
//        // * default enclosers|default field -> CP: package (project)
//        // * public|protected enclosers&public|protected field -> CP: project + dependencies
//        Set<FileObject> refs;
//        FileObject source = sourceType.getFileObject();
//        if (fieldAccessibility.contains(CsmVisibility.PRIVATE) || fieldEncloserAccessibility == CsmVisibility.PRIVATE) {
//            // search file
//            refs = Collections.singleton(source);
//        } else { // visible field
//            ClasspathInfo cpinfo;
//            if (fieldEncloserAccessibility == CsmVisibility.PUBLIC
//                    && (fieldAccessibility.contains(CsmVisibility.PUBLIC) || fieldAccessibility.contains(CsmVisibility.PROTECTED))) {
//                // search project and dependencies
//                cpinfo = RetoucheUtils.getClasspathInfoFor(true, source);
//            } else {
//                // search project
//                cpinfo = RetoucheUtils.getClasspathInfoFor(false, source);
//            }
//            ClassIndex index = cpinfo.getClassIndex();
//            refs = index.getResources(fieldEncloserHandle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
//            if (!refs.contains(source)) {
//                refs = new HashSet<FileObject>(refs);
//                refs.add(source);
//            }
//        }
//        return refs;
//    }
    
//    private static boolean isSubclassOf(CsmClass subclass, CsmClass superclass) {
//        TypeMirror superType = subclass.getSuperclass();
//        while(superType.getKind() != TypeKind.NONE) {
//            CsmClass superTypeElm = (CsmClass) ((DeclaredType) superType).asElement();
//            if (superclass == superTypeElm) {
//                return true;
//            }
//            superType = superTypeElm.getSuperclass();
//        }
//        return false;
//    }
    
//    static final class Encapsulator extends RefactoringVisitor {
//
//        private final FileObject sourceFile;
//        private final InsertPoint insertPoint;
//        private final SortBy sortBy;
//        private final Javadoc javadocType;
//        private Problem problem;
//        private List<EncapsulateDesc> descs;
//        private Map<CsmField, EncapsulateDesc> fields;
//
//        public Encapsulator(List<EncapsulateDesc> descs, Problem problem, InsertPoint ip, SortBy sortBy, Javadoc jd) {
//            assert descs != null && descs.size() > 0;
//            this.sourceFile = descs.get(0).fieldHandle.getFileObject();
//            this.descs = descs;
//            this.problem = problem;
//            this.insertPoint = ip == null ? InsertPoint.DEFAULT : ip;
//            this.sortBy = sortBy == null ? SortBy.PAIRS : sortBy;
//            this.javadocType = jd == null ? Javadoc.NONE : jd;
//        }
//
//        public Problem getProblem() {
//            return problem;
//        }
//
//        @Override
//        public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
//            super.setWorkingCopy(workingCopy);
//
//            // init caches
//            fields = new HashMap<CsmField, EncapsulateDesc>(descs.size());
//            for (EncapsulateDesc desc : descs) {
//                desc.field = (CsmField) desc.fieldHandle.resolveElement(workingCopy);
//                fields.put(desc.field, desc);
//            }
//        }
//
//        @Override
//        public Tree visitCompilationUnit(CompilationUnitTree node, Element field) {
//            return scan(node.getTypeDecls(), field);
//        }
//
//        @Override
//        public Tree visitClass(ClassTree node, Element field) {
//            CsmClass clazz = (CsmClass) workingCopy.getTrees().getElement(getCurrentPath());
//            boolean[] origValues = new boolean[descs.size()];
//            int counter = 0;
//            for (EncapsulateDesc desc : descs) {
//                origValues[counter++] = desc.useAccessors;
//                desc.useAccessors = resolveUseAccessor(clazz, desc);
//            }
//
//            if (sourceFile == workingCopy.getFileObject()) {
//                Element el = workingCopy.getTrees().getElement(getCurrentPath());
//                if (el == descs.get(0).field.getEnclosingElement()) {
//                    // all fields come from the same class so testing the first field should be enough
//                    ClassTree nct = node;
//                    List<MethodTree> newMethods = new ArrayList<MethodTree>();
//                    int getterIdx = 0;
//                    for (EncapsulateDesc desc : descs) {
//                        MethodTree[] ms = createGetterAndSetter(
//                                desc.field,
//                                desc.refactoring.getGetterName(),
//                                desc.refactoring.getSetterName(),
//                                desc.refactoring.getMethodModifiers());
//                        if (ms[0] != null) {
//                            newMethods.add(getterIdx++, ms[0]);
//                        }
//                        if (ms[1] != null) {
//                            int setterIdx = sortBy == SortBy.GETTERS_FIRST
//                                    ? newMethods.size()
//                                    : getterIdx++;
//                            newMethods.add(setterIdx, ms[1]);
//                        }
//                    }
//
//                    if (!newMethods.isEmpty()) {
//                        if (sortBy == SortBy.ALPHABETICALLY) {
//                            Collections.sort(newMethods, new SortMethodsByNameComparator());
//                        }
//                        if (insertPoint == InsertPoint.DEFAULT) {
//                            nct = GeneratorUtilities.get(workingCopy).insertClassMembers(node, newMethods);
//                        } else {
//                            List<? extends Tree> members = node.getMembers();
//                            if (insertPoint.getIndex() >= members.size()) {
//                                // last method
//                                for (MethodTree mt : newMethods) {
//                                    nct = make.addClassMember(nct, mt);
//                                }
//                            } else {
//                                int idx = insertPoint.getIndex();
//                                for (MethodTree mt : newMethods) {
//                                    nct = make.insertClassMember(nct, idx++, mt);
//                                }
//                            }
//                        }
//                        rewrite(node, nct);
//                    }
//                }
//            }
//
//            Tree result = scan(node.getMembers(), field);
//            counter = 0;
//            for (EncapsulateDesc desc : descs) {
//                desc.useAccessors = origValues[counter++];
//            }
//            return result;
//        }
//
//        private static final class SortMethodsByNameComparator implements Comparator<MethodTree> {
//
//            public int compare(MethodTree o1, MethodTree o2) {
//                String n1 = o1.getName().toString();
//                String n2 = o2.getName().toString();
//                return n1.compareTo(n2);
//            }
//
//        }
//
//        @Override
//        public Tree visitVariable(VariableTree node, Element field) {
//            if (sourceFile == workingCopy.getFileObject()) {
//                Element el = workingCopy.getTrees().getElement(getCurrentPath());
//                EncapsulateDesc desc = fields.get(el);
//                if (desc != null) {
//                    resolveFieldDeclaration(node, desc);
//                    return null;
//                }
//            }
//            return scan(node.getInitializer(), field);
//        }
//
//        @Override
//        public Tree visitAssignment(AssignmentTree node, Element field) {
//            ExpressionTree variable = node.getVariable();
//            boolean isArray = false;
//            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArray = true;
//                variable = ((ArrayAccessTree) variable).getExpression();
//            }
//
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
//                    // check (field = 3) == 3
//                    && (isArray || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                if (isArray) {
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//                    rewrite(variable, invkgetter);
//                } else {
//                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    ExpressionTree newExpTree;
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
//                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    TypeMirror expType = trees.getTypeMirror(expPath);
//                    if (workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = expTree;
//                    } else {
//                        newExpTree = make.TypeCast(make.Type(varType), expTree);
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return scan(node.getExpression(), field);
//        }
//
//        @Override
//        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element field) {
//            ExpressionTree variable = node.getVariable();
//            boolean isArray = false;
//            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArray = true;
//                variable = ((ArrayAccessTree) variable).getExpression();
//            }
//
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
//                    // check (field += 3) == 3
//                    && (isArray || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                if (isArray) {
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//                    rewrite(variable, invkgetter);
//                } else {
//                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());
//
//                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
//                    String s = node.getKind().name();
//                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
//                    Tree.Kind operator = Tree.Kind.valueOf(s);
//
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    ExpressionTree newExpTree;
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
//                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    // getter need not exist yet, use variable to resolve type of binary expression
//                    ExpressionTree expTreeFake = make.Binary(operator, variable, expTree);
//                    TypeMirror expType = workingCopy.getTreeUtilities().attributeTree(expTreeFake, trees.getScope(expPath));
//
//                    newExpTree = make.Binary(operator, invkgetter, expTree);
//                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return scan(node.getExpression(), field);
//        }
//
//        @Override
//        public Tree visitUnary(UnaryTree node, Element field) {
//            ExpressionTree t = node.getExpression();
//            Kind kind = node.getKind();
//            boolean isArrayOrImmutable = kind != Kind.POSTFIX_DECREMENT
//                    && kind != Kind.POSTFIX_INCREMENT
//                    && kind != Kind.PREFIX_DECREMENT
//                    && kind != Kind.PREFIX_INCREMENT;
//            while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArrayOrImmutable = true;
//                t = ((ArrayAccessTree) t).getExpression();
//            }
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors
//                    && desc.refactoring.getGetterName() != null
//                    && (isArrayOrImmutable || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                // check (++field + 3)
//                ExpressionTree invkgetter = createGetterInvokation(t, desc.refactoring.getGetterName());
//                if (isArrayOrImmutable) {
//                    rewrite(t, invkgetter);
//                } else if (desc.refactoring.getSetterName() != null) {
//                    ExpressionTree setter = createMemberSelection(node.getExpression(), desc.refactoring.getSetterName());
//
//                    Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
//                            ? Tree.Kind.PLUS
//                            : Tree.Kind.MINUS;
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    TypeMirror expType = workingCopy.getTypes().getPrimitiveType(TypeKind.INT);
//                    ExpressionTree newExpTree = make.Binary(operator, invkgetter, make.Literal(1));
//                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        public Tree visitMemberSelect(MemberSelectTree node, Element field) {
//            Element el = workingCopy.getTrees().getElement(getCurrentPath());
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
//                rewrite(node, nodeNew);
//            }
//            return super.visitMemberSelect(node, field);
//        }
//
//        @Override
//        public Tree visitIdentifier(IdentifierTree node, Element field) {
//            Element el = workingCopy.getTrees().getElement(getCurrentPath());
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
//                rewrite(node, nodeNew);
//            }
//            return null;
//        }
//
//        private boolean checkAssignmentInsideExpression() {
//            Tree exp1 = getCurrentPath().getLeaf();
//            Tree parent = getCurrentPath().getParentPath().getLeaf();
//            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
//                // XXX would be useful if Problems support HTML
////                String code = parent.toString();
////                String replace = exp1.toString();
////                code = code.replace(replace, "&lt;b&gt;" + replace + "&lt;/b&gt;");
//                problem = createProblem(
//                        problem,
//                        false,
//                        NbBundle.getMessage(
//                                EncapsulateFieldRefactoringPlugin.class,
//                                "ERR_EncapsulateInsideAssignment", // NOI18N
//                                exp1.toString(),
//                                parent.toString(),
//                                FileUtil.getFileDisplayName(workingCopy.getFileObject())));
//                return false;
//            }
//            return true;
//        }
//
//        /**
//         * replace current expresion with the proper one.<p>
//         * c.field -> c.getField()
//         * field -> getField()
//         * or copy in case of refactoring.getGetterName() == null
//         */
//        private ExpressionTree createGetterInvokation(ExpressionTree current, String getterName) {
//            // check if exist refactoring.getGetterName() != null and visibility (subclases)
//            if (getterName == null) {
//                return current;
//            }
//            ExpressionTree getter = createMemberSelection(current, getterName);
//
//            MethodInvocationTree invkgetter = make.MethodInvocation(
//                    Collections.<ExpressionTree>emptyList(),
//                    getter,
//                    Collections.<ExpressionTree>emptyList());
//            return invkgetter;
//        }
//
//        private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
//            ExpressionTree selector;
//            if (node.getKind() == Tree.Kind.MEMBER_SELECT) {
//                MemberSelectTree select = (MemberSelectTree) node;
//                selector = make.MemberSelect(select.getExpression(), name);
//            } else {
//                selector = make.Identifier(name);
//            }
//            return selector;
//        }
//
//        private MethodTree[] createGetterAndSetter(
//                CsmField field, String getterName,
//                String setterName, Set<CsmVisibility> useModifiers) {
//
//            String fieldName = field.getSimpleName().toString();
//            boolean staticMod = field.getModifiers().contains(CsmVisibility.STATIC);
//            String parName = staticMod ? "a" + getCapitalizedName(field) : stripPrefix(fieldName); //NOI18N
//            String getterBody = "{return " + fieldName + ";}"; //NOI18N
//            String setterBody = (staticMod? "{": "{this.") + fieldName + " = " + parName + ";}"; //NOI18N
//
//            Set<CsmVisibility> mods = new HashSet<CsmVisibility>(useModifiers);
//            if (staticMod) {
//                mods.add(CsmVisibility.STATIC);
//            }
//
//            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
//            MethodTree[] result = new MethodTree[2];
//
//            ExecutableElement getterElm = null;
//            if (getterName != null) {
//                getterElm = findMethod(
//                        workingCopy,
//                        (CsmClass) field.getEnclosingElement(),
//                        getterName,
//                        Collections.<CsmField>emptyList(), false);
//            }
//            if (getterElm == null && getterName != null) {
//                MethodTree getter = make.Method(
//                        make.Modifiers(mods),
//                        getterName,
//                        fieldTree.getType(),
//                        Collections.<TypeParameterTree>emptyList(),
//                        Collections.<VariableTree>emptyList(),
//                        Collections.<ExpressionTree>emptyList(),
//                        getterBody,
//                        null);
//                result[0] = getter;
//                String jdText = null;
//                if (javadocType == Javadoc.COPY) {
//                    jdText = workingCopy.getElements().getDocComment(field);
//                    jdText = trimNewLines(jdText);
//                }
//                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
//                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
//                    Comment comment = Comment.create(
//                            Comment.Style.JAVADOC, -2, -2, -2,
//                            prefix + "@return the " + field.getSimpleName()); // NOI18N
//                    make.addComment(getter, comment, true);
//                }
//            }
//
//            ExecutableElement setterElm = null;
//            if (setterName != null) {
//                setterElm = findMethod(
//                        workingCopy,
//                        (CsmClass) field.getEnclosingElement(),
//                        setterName,
//                        Collections.<CsmField>singletonList(field), false);
//            }
//            if (setterElm == null && setterName != null) {
//                VariableTree paramTree = make.Variable(
//                        make.Modifiers(Collections.<CsmVisibility>emptySet()), parName, fieldTree.getType(), null);
//                MethodTree setter = make.Method(
//                        make.Modifiers(mods),
//                        setterName,
//                        make.PrimitiveType(TypeKind.VOID),
//                        Collections.<TypeParameterTree>emptyList(),
//                        Collections.singletonList(paramTree),
//                        Collections.<ExpressionTree>emptyList(),
//                        setterBody,
//                        null);
//                result[1] = setter;
//
//                String jdText = null;
//                if (javadocType == Javadoc.COPY) {
//                    jdText = workingCopy.getElements().getDocComment(field);
//                    jdText = trimNewLines(jdText);
//                }
//                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
//                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
//                    Comment comment = Comment.create(
//                            Comment.Style.JAVADOC, -2, -2, -2,
//                            prefix + String.format("@param %s the %s to set", parName, fieldName)); // NOI18N
//                    make.addComment(setter, comment, true);
//                }
//            }
//
//            return result;
//        }
//
//        private String trimNewLines(String javadoc) {
//            if (javadoc == null) {
//                return null;
//            }
//
//            int len = javadoc.length();
//            int st = 0;
//            int off = 0;      /* avoid getfield opcode */
//            char[] val = javadoc.toCharArray();    /* avoid getfield opcode */
//
//            while ((st < len) && Character.isWhitespace(val[off + st])/* && (val[off + st] <= '\n')*/) {
//                st++;
//            }
//            while ((st < len) && Character.isWhitespace(val[off + len - 1])/*val[off + len - 1] <= '\n')*/) {
//                len--;
//            }
//            return ((st > 0) || (len < val.length)) ? javadoc.substring(st, len) : javadoc;
//        }
//
//        private void resolveFieldDeclaration(VariableTree node, EncapsulateDesc desc) {
//            CsmVisibility currentAccess = getAccessibility(desc.field.getModifiers());
//            CsmVisibility futureAccess = getAccessibility(desc.refactoring.getFieldModifiers());
//            ModifiersTree newModTree = null;
//            if (currentAccess != futureAccess) {
//                newModTree = make.Modifiers(
//                        replaceAccessibility(currentAccess, futureAccess, desc.field),
//                        node.getModifiers().getAnnotations());
//            }
//
//            if (node.getModifiers().getFlags().contains(CsmVisibility.FINAL)
//                    && desc.refactoring.getSetterName() != null) {
//                // remove final flag in case user wants to create setter
//                ModifiersTree mot = newModTree == null ? node.getModifiers(): newModTree;
//                Set<CsmVisibility> flags = new HashSet<CsmVisibility>(mot.getFlags());
//                flags.remove(CsmVisibility.FINAL);
//                newModTree = make.Modifiers(flags, mot.getAnnotations());
//            }
//
//            if (newModTree != null) {
//                VariableTree newNode = make.Variable(
//                        newModTree, node.getName(), node.getType(), node.getInitializer());
//                rewrite(node, newNode);
//            }
//        }
//
//        private boolean resolveUseAccessor(CsmClass where, EncapsulateDesc desc) {
//            if (desc.refactoring.isAlwaysUseAccessors()) {
//                return true;
//            }
//
//            // target field accessibility
//            Set<CsmVisibility> mods = desc.refactoring.getFieldModifiers();
//            if (mods.contains(CsmVisibility.PRIVATE)) {
//                // check enclosing top level class
//                // return SourceUtils.getOutermostEnclosingTypeElement(where) != SourceUtils.getOutermostEnclosingTypeElement(desc.field);
//                return where != desc.field.getEnclosingElement();
//            }
//
//            if (mods.contains(CsmVisibility.PROTECTED)) {
//                // check inheritance
//                if (isSubclassOf(where, (CsmClass) desc.field.getEnclosingElement())) {
//                    return false;
//                }
//                // check same package
//                return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
//            }
//
//            if (mods.contains(CsmVisibility.PUBLIC)) {
//                return false;
//            }
//
//            // default access
//            // check same package
//            return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
//        }
//
//        private boolean isInConstructorOfFieldClass(TreePath path, Element field) {
//            Tree leaf = path.getLeaf();
//            Kind kind = leaf.getKind();
//            while (true) {
//                switch (kind) {
//                case METHOD:
//                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
//                        return false;
//                    }
//                    Element m = workingCopy.getTrees().getElement(path);
//                    return m.getKind() == ElementKind.CONSTRUCTOR
//                            && (m.getEnclosingElement() == field.getEnclosingElement()
//                                || isSubclassOf((CsmClass) m.getEnclosingElement(), (CsmClass) field.getEnclosingElement()));
//                case COMPILATION_UNIT:
//                case CLASS:
//                case NEW_CLASS:
//                    return false;
//                }
//                path = path.getParentPath();
//                leaf = path.getLeaf();
//                kind = leaf.getKind();
//            }
//        }
//
//        private boolean isInGetterSetter(
//                TreePath path,
//                ElementHandle<ExecutableElement> currentGetter,
//                ElementHandle<ExecutableElement> currentSetter) {
//
//            if (sourceFile != workingCopy.getFileObject()) {
//                return false;
//            }
//
//            Tree leaf = path.getLeaf();
//            Kind kind = leaf.getKind();
//            while (true) {
//                switch (kind) {
//                case METHOD:
//                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
//                        return false;
//                    }
//                    Element m = workingCopy.getTrees().getElement(path);
//                    return currentGetter != null && m == currentGetter.resolve(workingCopy)
//                            || currentSetter != null && m == currentSetter.resolve(workingCopy);
//                case COMPILATION_UNIT:
//                case CLASS:
//                case NEW_CLASS:
//                    return false;
//                }
//                path = path.getParentPath();
//                leaf = path.getLeaf();
//                kind = leaf.getKind();
//            }
//        }
//
//    }
    
//    /**
//     * A descriptor of the encapsulated field for Encapsulator.
//     */
//    static final class EncapsulateDesc {
//        Problem p;
//        Set<FileObject> refs;
//        TreePathHandle fieldHandle;
//
//        // following fields are used solely by Encapsulator
//        CsmField field;
//        private ElementHandle<ExecutableElement> currentGetter;
//        private ElementHandle<ExecutableElement> currentSetter;
//        private EncapsulateFieldRefactoring refactoring;
//        private boolean useAccessors;
//    }
    
}
