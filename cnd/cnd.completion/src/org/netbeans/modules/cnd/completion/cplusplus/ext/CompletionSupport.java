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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctional;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmReturnStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement.Kind;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import static org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.DeduceTemplateTypeStrategy;
import static org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.DefaultDeduceTemplateTypeStrategy;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.completion.cplusplus.CsmFinderFactory;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletion.SimpleClass;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.Context;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmContextUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.ClassifiersEqualizer;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.EqualResult;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.TypeInfoCollector;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.iterateTypeChain;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.howMany;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.Qualificator;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.QualifiersEqualizer;
import org.openide.util.Pair;

/**
 *
 */
public final class CompletionSupport implements DocumentListener {

    private final Reference<Document> docRef;
    // not for external instantiation

    private CompletionSupport(Document doc) {
        docRef = new WeakReference<Document>(doc);
        doc.addDocumentListener(this);
    }

    /** NB: can return NULL, for example, if a text/plain file is #included */
    public static CompletionSupport get(JTextComponent component) {
        return get(component.getDocument());
    }

    /** NB: can return NULL, for example, if a text/plain file is #included */
    public static CompletionSupport get(final Document doc) {
        CompletionSupport support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
        if (support == null) {
            // for now accept only documents with known languages
            boolean valid = (CndLexerUtilities.getLanguage(doc) != null);
            if (valid) {
                support = new CompletionSupport(doc);
                doc.putProperty(CompletionSupport.class, support);
//                synchronized (doc) {
//                    support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
//                    if (support == null) {
//                        doc.putProperty(CompletionSupport.class, support = new CompletionSupport(doc));
//                    }
//                }
            }
        }
        return support;
    }

    public final Document getDocument() {
        return this.docRef.get();
    }

    public static boolean isPreprocCompletionEnabled(Document doc, int offset) {
        return isIncludeCompletionEnabled(doc, offset) || isPreprocessorDirectiveCompletionEnabled(doc, offset);
    }

    private static boolean isPreprocessorDirectiveCompletionEnabledImpl(Document doc, int offset) {
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, false, true);
        if (ts == null) {
            return false;
        }
        if (ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<TokenId> embedded = (TokenSequence<TokenId>) ts.embedded();
            embedded.moveStart();
            embedded.moveNext();
            // skip starting #
            if (!embedded.moveNext()) {
                return true; // the end of embedded token stream
            }
            CndTokenUtilities.shiftToNonWhite(embedded, false);
            return embedded.offset() + embedded.token().length() >= offset;
        }
        return false;
    }

    public static boolean isPreprocessorDirectiveCompletionEnabled(final Document doc, final int offset) {
        final AtomicBoolean out = new AtomicBoolean(false);
        doc.render(new Runnable() {

            @Override
            public void run() {
                out.set(isPreprocessorDirectiveCompletionEnabledImpl(doc, offset));
            }
        });
        return out.get();
    }

    public static boolean isIncludeCompletionEnabled(final Document doc, final int offset) {
        final AtomicBoolean out = new AtomicBoolean(false);
        doc.render(new Runnable() {

            @Override
            public void run() {
                out.set(isIncludeCompletionEnabledImpl(doc, offset));
            }
        });
        return out.get();
    }

    private static boolean isIncludeCompletionEnabledImpl(Document doc, int offset) {
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, false, true);
        if (ts == null) {
            return false;
        }
        if (ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<TokenId> embedded = (TokenSequence<TokenId>) ts.embedded();
            if (CndTokenUtilities.moveToPreprocKeyword(embedded)) {
                TokenId id = embedded.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                            // completion enabled after #include(_next) keywords
                            return (embedded.offset() + embedded.token().length()) <= offset;
                    }
                }
            }
        }
        return false;
    }

    public final CsmFinder getFinder() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        return CsmFinderFactory.getDefault().getFinder(fo);
    }

    private static int NOT_INITIALIZED = -1;
    private int lastSeparatorOffset = -1;
    private int contextOffset = NOT_INITIALIZED;

    public void setContextOffset(int offset) {
        this.contextOffset = offset;
    }

    public int doc2context(int docPos) {
        int offset = this.contextOffset == NOT_INITIALIZED ? docPos : this.contextOffset;
        offset = CsmMacroExpansion.getOffsetInOriginalText(getDocument(), offset);
        return offset;
    }

    protected void setLastSeparatorOffset(int lastSeparatorOffset) {
        this.lastSeparatorOffset = lastSeparatorOffset;
    }

    /** Return the position of the last command separator before
     * the given position.
     */
    protected int getLastCommandSeparator(CsmFile file, final int pos, FileReferencesContext fileReferences) throws BadLocationException {
        if (pos < 0 || pos > getDocument().getLength()) {
            throw new BadLocationException("position is out of range[" + 0 + "-" + getDocument().getLength() + "]", pos); // NOI18N
        }
        if (pos == 0) {
            return 0;
        }

        int modelSeparator = tryGetSeparatorFromModel(file, pos, fileReferences);
        if (modelSeparator >= 0) {
            return modelSeparator;
        }

        // freeze the value to prevent modification of cache value from diff thread
        int curCachedValue = lastSeparatorOffset;
        if (!CndTokenUtilities.isInPreprocessorDirective(getDocument(), pos) &&
                !CndTokenUtilities.isInProCDirective(getDocument(), pos)) {
            if (curCachedValue >= 0 && curCachedValue < pos &&
                    !CndTokenUtilities.isInProCDirective(getDocument(), curCachedValue)) {
                return curCachedValue;
            }
            // have to return newLastSeparatorOffset
            int newLastSeparatorOffset = CndTokenUtilities.getLastCommandSeparator(getDocument(), pos);
            // it's OK if cache is set to different values from different threads
            // so no sync here
            if (curCachedValue == lastSeparatorOffset) {
                lastSeparatorOffset = newLastSeparatorOffset;
            }
            return newLastSeparatorOffset;
        } else {
            return CndTokenUtilities.getLastCommandSeparator(getDocument(), pos);
        }
    }

    public static boolean areTemplatesEnabled(CsmFile csmFile) {
        if (csmFile != null) {
            switch (csmFile.getFileType()) {
                case SOURCE_C_FILE:
                case SOURCE_FORTRAN_FILE:
                    return false;
            }
        }
        return true;
    }
    
    // When getting separator from model, it is the max amount of characters 
    // to process with token processor. If it is neccessary to parse more, then
    // cached separator is used in most cases.
    private static final int MAX_EXPRESSION_LENGTH_TO_REPARSE = 1024;

    private static int tryGetSeparatorFromModel(CsmFile file, int pos, FileReferencesContext fileReferences) {
        // Enable only for cpp11 ant later because for previous standards simple
        // text based logic worked decently
        if (CsmFileInfoQuery.getDefault().isCpp11OrLater(file)) {
            CsmContext context = CsmOffsetResolver.findContext(file, pos, fileReferences);
            CsmObject lastObj = context.getLastObject();
            if (CsmKindUtilities.isLambda(lastObj)) {
                // If pos is inside return type of lambda, set separator to its beginning
                CsmType retType = ((CsmFunction) lastObj).getReturnType();
                if (CsmOffsetUtilities.isInObject(retType, pos)) {
                    return ((CsmOffsetable) retType).getStartOffset();
                }
            }
            if (CsmKindUtilities.isVariable(lastObj)) {
                CsmVariable v = (CsmVariable)lastObj;
                CsmExpression initialValue = v.getInitialValue();
                if (CsmOffsetUtilities.isInObject(initialValue, pos)) {
                    lastObj = initialValue;
                }
            }
            if (CsmKindUtilities.isExpression(lastObj)) {
                CsmExpression expression = (CsmExpression) lastObj;
                if (expression.getStartOffset() < pos && expression.getEndOffset() > pos) {
                    List<CsmStatement> lambdas = expression.getLambdas();
                    if (lambdas != null && !lambdas.isEmpty()) {
                        // Think about restriction on max expression length.
                        return ((CsmOffsetable) lastObj).getStartOffset();
                    }
                }
            }
            if (CsmKindUtilities.isOffsetable(lastObj)) {
                CsmOffsetable offs = (CsmOffsetable) lastObj;
                if (offs.getStartOffset() < pos && offs.getEndOffset() > pos) {
                    int distance = pos - offs.getStartOffset();
                    if (distance < MAX_EXPRESSION_LENGTH_TO_REPARSE) {
                        return offs.getStartOffset();
                    }
                }
            }
        }
        return -1;
    }

    /** Get the class from name. The import sections are consulted to find
     * the proper package for the name. If the search in import sections fails
     * the method can ask the finder to search just by the given name.
     * @param className name to resolve. It can be either the full name
     *   or just the name without the package.
     * @param searchByName if true and the resolving through the import sections fails
     *   the finder is asked to find the class just by the given name
     */
    public static CsmClassifier getClassFromName(CsmFinder finder, String className, boolean searchByName) {
        // XXX handle primitive type
        CsmClassifier ret = null;
//        CsmClass ret = JavaCompletion.getPrimitiveClass(className);
//        if (ret == null) {
//
//            ret = getIncludeProc().getClassifier(className);
//        }
        if (ret == null && searchByName) {
            List<CsmClassifier> clsList = finder.findClasses(null, className, true, false);
            if (clsList != null && clsList.size() > 0) {
                if (!clsList.isEmpty()) { // more matching classes
                    ret = clsList.get(0); // get the first one
                }
            }

        }
        return ret;
    }

    /** Get the class that belongs to the given position */
    public CsmClass getClass(CsmFile file, int docPos) {
        int pos = doc2context(docPos);
        return CompletionUtilities.findClassOnPosition(file, getDocument(), pos);
    }

    /** Get the class or function definition that belongs to the given position */
    public CsmOffsetableDeclaration getDefinition(CsmFile file, int docPos, FileReferencesContext fileContext) {
        int pos = doc2context(docPos);
        return CompletionUtilities.findFunDefinitionOrClassOnPosition(file, getDocument(), pos, fileContext);
    }

    /** Get the class or function definition from the known scope */
    public CsmOffsetableDeclaration getDefinition(CsmScope scope) {
        while (CsmKindUtilities.isScopeElement(scope) && (!CsmKindUtilities.isClass(scope) && !CsmKindUtilities.isFunction(scope))) {
            scope = ((CsmScopeElement) scope).getScope();
        }
        return (CsmKindUtilities.isClass(scope) || CsmKindUtilities.isFunction(scope)) ? (CsmOffsetableDeclaration) scope : null;
    }

    public boolean isStaticBlock(int docPos) {
        // pos = doc2context(pos);
        return false;
    }

    public static boolean isAssignable(CsmType from, CsmType to) {
        return isAssignable(null, from, to);
    }

    static boolean isAssignable(Context ctx, CsmType origFrom, CsmType origTo) {
        AnalyzedType from = AnalyzedType.create(ctx, origFrom, false, false);
        if (from == null) {
            return false;
        }
        AnalyzedType to = AnalyzedType.create(ctx, origTo, true, true);
        if (to == null) {
            return false;
        }
        if (!isAutoConvertible(from.type, from.origType, from.typeInfo, to.type, to.origType, to.typeInfo, from.classifier, to.classifier)) {
            return isUserConvertible(from.type, from.origType, from.typeInfo, to.type, to.origType, to.typeInfo, from.classifier, to.classifier);
        }
        return true;
    }

    /**
     * Checks if type 'to' can be converted from type 'from' with default
     * conversions (all except user defined ones).
     *
     * @param from
     * @param to
     * @return true if convertible
     */
    public static boolean isAutoConvertible(CsmType from, CsmType to) {
        return isAutoConvertible(null, from, to);
    }

    static boolean isAutoConvertible(Context ctx, CsmType origFrom, CsmType origTo) {
        AnalyzedType from = AnalyzedType.create(ctx, origFrom, false, false);
        if (from == null) {
            return false;
        }
        AnalyzedType to = AnalyzedType.create(ctx, origTo, true, true);
        if (to == null) {
            return false;
        }
        return isAutoConvertible(from.type, from.origType, from.typeInfo, to.type, to.origType, to.typeInfo, from.classifier, to.classifier);
    }

    public static boolean isAutoConvertible(CsmType from, CsmType origFrom, TypeInfoCollector fromInfo, CsmType to, CsmType origTo, TypeInfoCollector toInfo, CsmClassifier fromCls, CsmClassifier toCls) {
        final int fromArrayDepth = howMany(fromInfo, Qualificator.ARRAY);
        final int toArrayDepth = howMany(toInfo, Qualificator.ARRAY);
        final int fromPointerDepth = howMany(fromInfo, Qualificator.POINTER);
        final int toPointerDepth = howMany(toInfo, Qualificator.POINTER);

        // XXX review!
        if (fromCls.equals(CsmCompletion.NULL_CLASS)) {
            return toArrayDepth > 0 || !CsmCompletion.isPrimitiveClass(toCls);
        }

        if (toCls.equals(CsmCompletion.OBJECT_CLASS)) {
            // everything is object
            return (fromArrayDepth > toArrayDepth) || (fromArrayDepth == toArrayDepth && !CsmCompletion.isPrimitiveClass(fromCls));
        }

        if (canBePointer(from) && toPointerDepth > 0) {
            return true;
        }

        if (fromPointerDepth > 0 && canBePointer(to)) {
            return true;
        }

        if (fromArrayDepth != toArrayDepth ||
                fromPointerDepth != toPointerDepth) {
            return false;
        }

        if (fromCls.equals(toCls)) {
            return true; // equal classes
        }

        String tfrom = origFrom.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N
        String tto = origTo.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N

        if (tfrom.equals(tto)) {
            return true;
        }

        if (CsmCompletion.isPrimitiveClass(fromCls) && CsmCompletion.isPrimitiveClass(toCls)) {
            return true;
        }

        if (CsmKindUtilities.isClass(toCls) && CsmKindUtilities.isClass(fromCls)) {
            return CsmInheritanceUtilities.isAssignableFrom((CsmClass)fromCls, (CsmClass)toCls);
        }
        return false;
    }

    /**
     * Checks if type 'to' can be converted from type 'from' with user
     * defined conversion.
     *
     * @param from
     * @param to
     * @return true if convertible
     */
    public static boolean isUserConvertible(CsmType from, CsmType to) {
        return isUserConvertible(null, from, to);
    }

    static boolean isUserConvertible(Context ctx, CsmType origFrom, CsmType origTo) {
        AnalyzedType from = AnalyzedType.create(ctx, origFrom, false, false);
        if (from == null) {
            return false;
        }
        AnalyzedType to = AnalyzedType.create(ctx, origTo, true, true);
        if (to == null) {
            return false;
        }
        return isUserConvertible(from.type, from.origType, from.typeInfo, to.type, to.origType, to.typeInfo, from.classifier, to.classifier);
    }

    public static boolean isUserConvertible(CsmType from, CsmType origFrom, TypeInfoCollector fromInfo, CsmType to, CsmType origTo, TypeInfoCollector toInfo, CsmClassifier fromCls, CsmClassifier toCls) {
        final int fromArrayDepth = howMany(fromInfo, Qualificator.ARRAY);
        final int toArrayDepth = howMany(toInfo, Qualificator.ARRAY);
        final int fromPointerDepth = howMany(fromInfo, Qualificator.POINTER);
        final int toPointerDepth = howMany(toInfo, Qualificator.POINTER);

        if (fromArrayDepth == 0 && toArrayDepth == 0 && fromPointerDepth == 0 && toPointerDepth == 0 && CsmKindUtilities.isClass(toCls)) {
            CsmClass clazz = (CsmClass) toCls;
            for (CsmMember member : clazz.getMembers()) {
                if (CsmKindUtilities.isConstructor(member)) {
                    CsmConstructor constructor = (CsmConstructor) member;
                    if (constructor.getParameters() != null) {
                        boolean appropriate = true;
                        boolean first = true;
                        for (CsmParameter param : constructor.getParameters()) {
                            if (first) {
                                first = false;
                                CsmType paramType = param.getType();
                                if (paramType != null && !CsmKindUtilities.isTemplateParameterType(paramType)) {
                                    TypeInfoCollector paramInfo = new TypeInfoCollector();
                                    paramType = iterateTypeChain(paramType, paramInfo);
                                    CsmClassifier paramCls = paramType.getClassifier();
                                    if (!isViableClassifier(paramCls, true)) {
                                        appropriate = false;
                                        break;
                                    }
                                    if (!isAutoConvertible(from, origFrom, fromInfo, paramType, param.getType(), paramInfo, fromCls, paramCls)) {
                                        appropriate = false;
                                        break;
                                    }
                                }
                            } else {
                                if (param.getInitialValue() == null) {
                                    appropriate = false;
                                    break;
                                }
                            }
                        }
                        if (appropriate) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean isViableClassifier(CsmClassifier cls, boolean checkInstantiationViability) {
        if (cls == null || CsmBaseUtilities.isUnresolved(cls)) {
            return false;
        }
        if (checkInstantiationViability && CsmKindUtilities.isInstantiation(cls)) {
            if (!CsmInstantiationProvider.getDefault().isViableInstantiation((CsmInstantiation) cls, true)) {
                return false;
            }
        }
        return true;
    }
    
    /*package*/ CsmType getCommonType(Context ctx, CsmType typ1, CsmType typ2, CppTokenId operator) {
        // Handle logical operators
        switch (operator) {
            case AMPAMP:
            case BARBAR: {
                CsmClassifier cls1 = typ1.getClassifier();
                if (cls1 != null) {
                    CsmClassifier cls2 = typ2.getClassifier();
                    if (cls2 != null) {
                        if (CndLexerUtilities.isType(cls1.getName().toString()) &&
                                CndLexerUtilities.isType(cls2.getName().toString())) {
                            // Both types are primitive
                            return CsmCompletion.BOOLEAN_TYPE;
                        }
                    }
                }
                break;
            }
        }
        
        // If types are equal, then no need to check them further
        if (typ1.equals(typ2)) {
            return typ1;
        }
        
        // Handle pointer arithmetic
        switch (operator) {
            case PLUS: {
                if (CsmBaseUtilities.isPointer(typ1)) {
                    // (pointer + int) case
                    if (!CsmBaseUtilities.isPointer(typ2)) {
                        CsmClassifier cls2 = CsmBaseUtilities.getClassifier(typ2, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true);
                        if (cls2 != null && CsmCompletion.isPrimitiveClass(cls2)) {
                            return typ1;
                        }
                    }
                } else if (CsmBaseUtilities.isPointer(typ2)) {
                    // (int + pointer) case
                    CsmClassifier cls1 = CsmBaseUtilities.getClassifier(typ1, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true);
                    if (cls1 != null && CsmCompletion.isPrimitiveClass(cls1)) {
                        return typ2;
                    }
                }
                break;
            }
                
            case MINUS: {
                if (CsmBaseUtilities.isPointer(typ1)) {
                    if (CsmBaseUtilities.isPointer(typ2)) {
                        // (pointer - pointer) case
                        return CsmCompletion.INT_TYPE;
                    } else {
                        // (pointer - int) case
                        CsmClassifier cls2 = CsmBaseUtilities.getClassifier(typ2, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true);
                        if (cls2 != null && CsmCompletion.isPrimitiveClass(cls2)) {
                            return typ1;
                        }
                    }
                } else if (CsmBaseUtilities.isPointer(typ2)) {
                    // (int - pointer) case. it is prohibited, but let's treat it as (pointer - int).
                    CsmClassifier cls1 = CsmBaseUtilities.getClassifier(typ1, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true);
                    if (cls1 != null && CsmCompletion.isPrimitiveClass(cls1)) {
                        return typ2;
                    }
                }
                break;
            }
        }
        
        return getCommonType(typ1, typ2);
    }
    
    public CsmType getCommonType(CsmType typ1, CsmType typ2) {
        if (typ1.equals(typ2)) {
            return typ1;
        }

        CsmClassifier cls1 = typ1.getClassifier();

        if (cls1 != null) {
            CsmClassifier cls2 = typ2.getClassifier();

            if (cls2 != null) {
                // The following part
                boolean firstIsPrimitiveClassifier = CndLexerUtilities.isType(cls1.getName().toString());
                boolean secondIsPrimitiveClassifier = CndLexerUtilities.isType(cls2.getName().toString());
                if (!firstIsPrimitiveClassifier && !secondIsPrimitiveClassifier) { // non-primitive classes
                    if (isAutoConvertible(typ1, typ2)) {
                        return typ1;
                    } else if (isAutoConvertible(typ2, typ1)) {
                        return typ2;
                    } else {
                        return null;
                    }
                } else { // at least one primitive class
                    boolean firstIsPurePrimitive = firstIsPrimitiveClassifier 
                        && !CsmBaseUtilities.isPointer(typ1) 
                        && typ1.getArrayDepth() == 0;
                    boolean secondIsPurePrimitive = secondIsPrimitiveClassifier 
                        && !CsmBaseUtilities.isPointer(typ2)
                        && typ2.getArrayDepth() == 0;
                    if (firstIsPurePrimitive && secondIsPurePrimitive) {
                        return applyArithmeticConversion(typ1, cls1, typ2, cls2);
                    } else if (secondIsPrimitiveClassifier) {
                        if (isAutoConvertible(typ2, typ1)) {
                            return typ1;
                        } else if (isAutoConvertible(typ1, typ2)) {
                            return typ2;
                        }
                    } else if (firstIsPrimitiveClassifier) {
                        if (isAutoConvertible(typ1, typ2)) {
                            return typ2;
                        } else if (isAutoConvertible(typ2, typ1)) {
                            return typ1;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static CsmType applyArithmeticConversion(CsmType type1, CsmClassifier cls1, CsmType type2, CsmClassifier cls2) {
        CharSequence name1 = cls1.getName();
        CharSequence name2 = cls2.getName();
        if (isSpecifiedClass(name1, CsmCompletion.LONG_DOUBLE_CLASS)) {
            return type1;
        }
        if (isSpecifiedClass(name2, CsmCompletion.LONG_DOUBLE_CLASS)) {
            return type2;
        }
        if (isSpecifiedClass(name1, CsmCompletion.DOUBLE_CLASS)) {
            return type1;
        }
        if (isSpecifiedClass(name2, CsmCompletion.DOUBLE_CLASS)) {
            return type2;
        }
        if (isSpecifiedClass(name1, CsmCompletion.FLOAT_CLASS)) {
            return type1;
        }
        if (isSpecifiedClass(name2, CsmCompletion.FLOAT_CLASS)) {
            return type2;
        }
        // Convert the rest according to integral ranks
        if (isEitherOfSpecifiedClass(name1, name2, CsmCompletion.UNSIGNED_LONG_LONG_CLASS)) {
            return isSpecifiedClass(name1, CsmCompletion.UNSIGNED_LONG_LONG_CLASS) ? type1 : type2;
        }
        if (isEitherOfSpecifiedClass(name1, name2, CsmCompletion.LONG_LONG_CLASS)) {
            return isSpecifiedClass(name1, CsmCompletion.LONG_LONG_CLASS) ? type1 : type2;
        }
        if (isEitherOfSpecifiedClass(name1, name2, CsmCompletion.UNSIGNED_LONG_CLASS)) {
            return isSpecifiedClass(name1, CsmCompletion.UNSIGNED_LONG_CLASS) ? type1 : type2;
        }
        if (isEitherOfSpecifiedClass(name1, name2, CsmCompletion.LONG_CLASS)) {
            return isSpecifiedClass(name1, CsmCompletion.LONG_CLASS) ? type1 : type2;
        }
        if (isEitherOfSpecifiedClass(name1, name2, CsmCompletion.UNSIGNED_INT_CLASS)) {
            return isSpecifiedClass(name1, CsmCompletion.UNSIGNED_INT_CLASS) ? type1 : type2;
        }
        // If none of the preceding conditions are met, both operands are converted to type int.
        return CsmCompletion.INT_TYPE;
    }
    
    private static boolean isSpecifiedClass(CharSequence clsName, SimpleClass cls) {
        return CharSequenceUtilities.equals(clsName, cls.getName());
    }
    
    private static boolean isEitherOfSpecifiedClass(CharSequence clsName1, CharSequence clsName2, SimpleClass cls) {
        return isSpecifiedClass(clsName1, cls) || isSpecifiedClass(clsName2, cls);
    }

    private static boolean canBePointer(CsmType type) {
        if (type instanceof CsmCompletion.OffsetableType) {
            CsmClassifier typeCls = type.getClassifier();
            if (CsmCompletion.NULLPTR_CLASS.getName().equals(typeCls.getName())) {
                return true;
            }
            if (CsmCompletion.isPrimitiveClass(typeCls) &&
                CsmCompletion.INT_CLASS.getName().equals(typeCls.getName()) &&
                ((CsmCompletion.OffsetableType) type).isZeroConst()) {
                return true;
            }
        }
        return false;
    }
    
    public static <T extends CsmObject> T getFirstVisible(List<T> objects, CsmFile contextFile) {
        if (objects != null && !objects.isEmpty()) {
            if (objects.size() > 1) {
                for (T element : objects) {
                    if (CsmIncludeResolver.getDefault().isObjectVisible(contextFile, element)) {
                        return element;
                    }
                }
            }
            return objects.get(0); // FIXME: or null in case of no visible objects?
        }
        return null;
    }
    
    public static <T extends CsmObject> List<T> filterByVisibility(List<T> objects, CsmFile contextFile) {
        if (objects != null && objects.size() > 1) {
            List<T> filtered = objects.stream()
                    .filter((obj)->CsmIncludeResolver.getDefault().isObjectVisible(contextFile, obj))
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                return filtered;
            }
        }
        return objects;
    }

    /** Filter the list of the methods (usually returned from
     * Finder.findMethods()) or the list of the constructors
     * by the given parameter specification.
     *
     * @param ctx - completion context (could be null)
     * @param methodList list of the methods. They should have the same
     *   name but in fact they don't have to.
     * @param exp - instantiation params (could be null)
     * @param parmTypes parameter types specification. If it is set to null, no filtering
     *   is performed and the same list is returned.
     * @param acceptMoreParameters useful for code completion to get
     *   even the methods with more parameters.
     */
    public static <T extends CsmFunctional> Collection<T> filterMethods(Context ctx,
                                                                          Collection<T> methodList,
                                                                          CsmCompletionExpression exp,
                                                                          List<CsmType> parmTypeList,
                                                                          boolean acceptMoreParameters,
                                                                          boolean acceptIfSameNumberParams) {
        Map<T, List<CsmType>> paramsPerMethod = null;
        if (parmTypeList != null) {
            paramsPerMethod = new IdentityHashMap<T, List<CsmType>>();
            for (T mtd : methodList) {
                paramsPerMethod.put(mtd, parmTypeList);
            }
        }
        return filterMethods(ctx, methodList, paramsPerMethod, exp, acceptMoreParameters, acceptIfSameNumberParams);
    }

    /** Filter the list of the methods (usually returned from
     * Finder.findMethods()) or the list of the constructors
     * by the given parameter specification.
     *
     * @param ctx - completion context (could be null)
     * @param methodList list of the methods. They should have the same
     *   name but in fact they don't have to.
     * @param paramsPerMethod parameter types specification. Each method could have its own parameters.
     * @param exp - instantiation params (could be null)
     * @param acceptMoreParameters useful for code completion to get
     *   even the methods with more parameters.
     */
    public static <T extends CsmFunctional> Collection<T> filterMethods(Context ctx,
                                                                          Collection<T> methodList,
                                                                          Map<T, List<CsmType>> paramsPerMethod,
                                                                          CsmCompletionExpression exp,
                                                                          boolean acceptMoreParameters,
                                                                          boolean acceptIfSameNumberParams)
    {
        boolean mustBeTemplate = (exp != null) ? exp.getExpID() == CsmCompletionExpression.GENERIC_TYPE : false;
        Collection<T> result = filterOverloadedOperators(ctx, methodList, paramsPerMethod);
        result = filterMethods(ctx, result, paramsPerMethod, mustBeTemplate, acceptMoreParameters, acceptIfSameNumberParams, false);
        if (result.size() > 1) {
            // it seems that this call couldn't filter anything
            result = filterMethods(ctx, result, paramsPerMethod, mustBeTemplate, acceptMoreParameters, acceptIfSameNumberParams, true);

            // perform more accurate filtering if it is a strict request (for navigation probably)
            if (!acceptMoreParameters && acceptIfSameNumberParams) {
                result = accurateFilterMethods(ctx, result, exp, paramsPerMethod);
            }
        }
        return result;
    }
    
    private static <T extends CsmFunctional> Collection<T> filterOverloadedOperators(Context ctx, Collection<T> methodList, Map<T, List<CsmType>> paramTypesPerMethod) {
        List<T> filteredOut = null;
        Map<CsmType, CsmClassifier> typesMap = new IdentityHashMap<>();
        for (T m : methodList) {
            if (m instanceof CsmFunction) {
                CsmFunction func = (CsmFunction) m;
                if (func.isOperator()) {
                    switch (func.getOperatorKind()) {
                        case PLUS:
                        case MINUS:
                        case DIV:
                        case MUL:
                        case MOD:
                        case PLUS_PLUS:
                        case MINUS_MINUS:
                            List<CsmType> paramTypeList = paramTypesPerMethod.get(m);
                            // Set to true if we have at least one parameter. Param will be checked and
                            // variable is set to false if necessary
                            boolean allParamsArePrimitive = !paramTypeList.isEmpty();
                            for (CsmType paramType : paramTypeList) {
                                if (paramType != null) {
                                    CsmClassifier classifier = typesMap.get(paramType);
                                    if (classifier == null) {
                                        if (ctx != null) {
                                            classifier = CsmBaseUtilities.getClassifier(paramType, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true);
                                        } else {
                                            classifier = paramType.getClassifier();
                                        }
                                        if (classifier != null) {
                                            typesMap.put(paramType, classifier);
                                        }
                                    }
                                    allParamsArePrimitive &= (CsmCompletion.safeIsPrimitiveClass(paramType, classifier) || CsmBaseUtilities.isPointer(paramType));
                                } else {
                                    // Failed to resolve at least one type. 
                                    // We cannot be sure that all parameters are primitive.
                                    allParamsArePrimitive = false;
                                    break;
                                }
                            }
                            if (allParamsArePrimitive) {
                                // If all parameters are primitive, then default
                                // arithmetic operator must be called. They cannot be
                                // overloaded.
                                if (filteredOut == null) {
                                    filteredOut = new ArrayList<>();
                                }
                                filteredOut.add(m);
                            }
                            break;
                    }
                }
            }
        }
        if (filteredOut != null) {
            List<T> result = new ArrayList(methodList);
            result.removeAll(filteredOut);
            return result;
        }
        return methodList;
    }

    private static <T extends CsmFunctional> Collection<T> filterMethods(Context ctx, Collection<T> methodList, Map<T, List<CsmType>> paramTypesPerMethod,
            boolean mustBeTemplate, boolean acceptMoreParameters, boolean acceptIfSameNumberParams, boolean ignoreConstAndRef) {
        assert (methodList != null);
        if (paramTypesPerMethod == null) {
            return methodList;
        }

        List<T> ret = new ArrayList<T>();
        int maxMatched = acceptIfSameNumberParams ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (T m : methodList) {
            List<CsmType> parmTypeList = paramTypesPerMethod.get(m);
            int parmTypeCnt = parmTypeList.size();
            // Use constructor conversion to allow to use it too for the constructors
            CsmParameter[] methodParms = m.getParameters().toArray(new CsmParameter[m.getParameters().size()]);
            int minParamLenght = 0;
            for (CsmParameter parameter : methodParms) {
                if (parameter.getInitialValue() == null) {
                    CsmType paramType = parameter.getType();
                    if (paramType != null && !paramType.isPackExpansion()) {
                        minParamLenght++;
                    }
                }
            }
            if ((methodParms.length >= parmTypeCnt && minParamLenght <= parmTypeCnt) || (acceptMoreParameters && methodParms.length >= parmTypeCnt)) {
                boolean accept = true;
                boolean bestMatch = !acceptMoreParameters && canBeBestMatch(m, mustBeTemplate);
                int matched = 0;
                for (int j = 0; accept && j < parmTypeCnt; j++) {
                    if (methodParms[j] == null) {
                        System.err.println("Null parameter " + j + " in function " + UIDs.get(m)); //NOI18N
                        bestMatch = false;
                        continue;
                    }
                    CsmType mpt = methodParms[j].getType();
                    CsmType t = parmTypeList.get(j);
                    if (t != null) {
                        if (!methodParms[j].isVarArgs() && !equalTypes(t, mpt, ignoreConstAndRef)) {
                            bestMatch = false;
                            if (!isAssignable(ctx, t, mpt)) {
                                if (CsmKindUtilities.isTemplateParameterType(mpt)) {
                                    if (mpt.getArrayDepth() + mpt.getPointerDepth() <= t.getArrayDepth() + t.getPointerDepth()) {
                                        matched++;
                                    } else {
                                        accept = false;
                                    }
                                } else {
                                    accept = false;
                                }
                            } else {
                                matched++;
                            }
                        } else {
                            if (methodParms[j].isVarArgs()) {
                                bestMatch = false;
                            }
                            matched++;
                        }
                    } else { // type in list is null
                        bestMatch = false;
                    }
                }

                if (accept) {
                    if (bestMatch) {
                        ret.clear();
                    } else if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                    }
                    ret.add(m);
                    if (bestMatch) {
                        break;
                    }
                } else {
                    if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                        ret.add(m);
                    }
                }

            } else if (methodParms.length == 0 && parmTypeCnt == 1) { // for cases like f(void)
                CsmType t = parmTypeList.get(0);
                if (t != null && "void".equals(t.getText())) { // best match // NOI18N
                    ret.clear();
                    ret.add(m);
                }
            }
        }
        return ret;
    }
    
    private static boolean canBeBestMatch(CsmFunctional fun, boolean mustBeTemplate) {
        if (mustBeTemplate) {
            // Template functions still must be checked for viability,
            // non-template functions are definetely not the best match
            return false;
        }
        return !isTemplateFunction(fun);
    }

    /**
     * Perform more accurate filtering. Could be used for navigation tasks.
     * Please note: This method is designed to be called after preliminary filtering.
     *              But this is not a necessary requirement.
     *
     * @param ctx - completion context (could be null)
     * @param methods
     * @param exp - template part of expression (could be null)
     * @param paramTypes
     *
     * @return candidates
     */
    private static <T extends CsmFunctional> Collection<T> accurateFilterMethods(Context ctx, Collection<T> methods, CsmCompletionExpression exp, Map<T, List<CsmType>> paramTypesPerMethod) {
        if (methods.size() <= 1) {
            return methods;
        }

        List<OverloadingCandidate<T>> candidates = new ArrayList<OverloadingCandidate<T>>();

        for (T m : methods) {
            List<CsmType> paramTypes = paramTypesPerMethod.get(m);
            int paramsCnt = paramTypes.size();

            CsmParameter[] methodParams = m.getParameters().toArray(new CsmParameter[m.getParameters().size()]);
            int minParamLenght = 0;
            for (CsmParameter parameter : methodParams) {
                if (parameter.getInitialValue() == null) {
                    CsmType paramType = parameter.getType();
                    if (paramType != null && !paramType.isPackExpansion()) {
                        minParamLenght++;
                    }
                }
            }

            if (methodParams.length >= paramsCnt && minParamLenght <= paramsCnt) {
                List<Conversion> conversions = new ArrayList<Conversion>(methodParams.length);

                for (int j = 0; j < paramsCnt; j++) {
                    CsmType methodParamType = methodParams[j].getType();
                    CsmType paramType = paramTypes.get(j);
                    if (paramType != null && methodParamType != null) {
                        conversions.add(new Conversion(ctx, paramType, methodParamType));
                    } else if (paramType != null && methodParamType == null) {
                        conversions.add(new Conversion(paramType, methodParamType, ConversionCategory.VarArgConversion));
                    }
                }

                if (!checkTemplateAcceptable(m, conversions)) {
                    for (int i = 0; i < conversions.size(); i++) {
                        Conversion conversion = conversions.get(i);
                        if (ConversionCategory.Template.equals(conversion.category)) {
                            conversions.set(i, new Conversion(conversion.from, conversion.to, ConversionCategory.NotConvertable));
                        }
                    }
                }

                Collections.sort(conversions);

                candidates.add(new OverloadingCandidate(m, conversions));
            }
        }

        Collections.sort(candidates, new OverloadingCandidatesComparator<T>());

        List<T> result = new ArrayList<T>();

        OverloadingCandidate<T> prev = null;

        for (OverloadingCandidate<T> candidate : candidates) {
            if (prev != null) {
                if (candidate.compareTo(prev) != 0) {
                    break;
                }
            }

            boolean validCandidate = true;

            if (ctx != null && CsmKindUtilities.isTemplate(candidate.function)) {
                List<CsmType> paramTypes = paramTypesPerMethod.get(candidate.function);

                // we should check if this function is viable
                CsmType retType = extractFunctionType(ctx, Arrays.asList(candidate.function), exp, paramTypes);
                CsmClassifier cls = retType != null ? CsmBaseUtilities.getClassifier(retType, ctx.getLexicalContextScope(), ctx.getContextFile(), ctx.getEndOffset(), true) : null;
                validCandidate = (cls != null && cls.isValid());
            }

            if (validCandidate) {
                prev = candidate;
                result.add(candidate.function);
            }
        }

        return result.isEmpty() ? methods : result;
    }

    /**
     * Checks if template conversions are not conflicting
     * @param function
     * @param conversions
     * @return true if there are no conflicts
     */
    private static boolean checkTemplateAcceptable(CsmFunctional function, List<Conversion> conversions) {
        if (CsmKindUtilities.isTemplate(function)) {
            Map<String, String> map = new HashMap<String, String>();

            for (Conversion conversion : conversions) {
                if (ConversionCategory.Template.equals(conversion.category)) {
                    String paramCanonicalText = conversion.to.getCanonicalText().toString();

                    if (map.containsKey(paramCanonicalText)) {
                        String valueText = conversion.from.getCanonicalText().toString();
                        if (!valueText.equals(map.get(paramCanonicalText))) {
                            return false;
                        }
                    } else {
                        map.put(paramCanonicalText, conversion.from.getCanonicalText().toString());
                    }
                }
            }
        }
        return true;
    }

    /**
     * Represents conversion from one type to another.
     */
    private static class Conversion implements Comparable<Conversion> {

        public final CsmType from;

        public final CsmType to;

        public final ConversionCategory category;

        public final int templateScore;

        public Conversion(Context ctx, CsmType from, CsmType to) {
            this.from = from;
            this.to = to;
            this.category = ConversionCategory.getWorstConversion(ctx, from, to);
            this.templateScore = calcTemplateScore(from, to, category);
        }

        public Conversion(CsmType from, CsmType to, ConversionCategory category) {
            this.from = from;
            this.to = to;
            this.category = category;
            this.templateScore = 0;
        }

        public boolean isBetter(Conversion other) {
            return category.rank < other.category.rank;
        }

        public boolean isEqual(Conversion other) {
            return category.rank == other.category.rank;
        }

        public boolean isWorse(Conversion other) {
            return category.rank > other.category.rank;
        }

        @Override
        public int compareTo(Conversion o) {
            return category.getRank() - o.category.getRank();
        }

        private int calcTemplateScore(CsmType from, CsmType to, ConversionCategory category) {
            if (ConversionCategory.Template.equals(category)) {
                CsmType origToType = CsmInstantiationProvider.getDefault().getOriginalType(to);
                if (origToType != null) {
                    int score = 0;

                    if (from.isConst() && origToType.isConst()) {
                        score++;
                    }
                    if (from.isPointer() && origToType.isPointer()) {
                        score++;
                    }

                    return score;
                }
            }
            return 0;
        }
    }

    /**
     * Represents different conversion categories.
     */
    private static enum ConversionCategory {
        Identity(1),
        Qualification(1),
        Template(1),
        Promotion(3),
        StandardConversion(4),
        UserDefinedConversion(5),
        VarArgConversion(6),
        NotConvertable(100);

        public int getRank() {
            return rank;
        }

        public static ConversionCategory getWorstConversion(Context ctx, CsmType from, CsmType to) {
            // if parameter of a function is a template parameter, then conversion should be marked as template
            if (CsmKindUtilities.isTemplateParameterType(to)) {
                return ConversionCategory.Template;
            }
            
            ClassifiersEqualizer clsEqualizer = new InstantiatedClassifiersEqualizer();
            QualifiersEqualizer strongQualsEqualizer = new CsmUtilities.ExactMatchQualsEqualizer();
            QualifiersEqualizer softQualsEqualizer = new CsmUtilities.AssignableQualsEqualizer();
            
            EqualResult res = CsmUtilities.checkTypesEqual(from, from.getContainingFile(), to, to.getContainingFile(), clsEqualizer, strongQualsEqualizer, true);
            if (res == EqualResult.EQUAL) {
                // qualifiers and classifiers are identical
                return ConversionCategory.Identity;
            } else if (res == EqualResult.QUALS_NOT_EQUAL) {
                res = CsmUtilities.checkTypesEqual(from, from.getContainingFile(), to, to.getContainingFile(), clsEqualizer, softQualsEqualizer, true);
                if (res == EqualResult.EQUAL) {
                    // qualifiers are not identical, classifiers are identical
                    return ConversionCategory.Qualification;
                }
            }

            // try to convert "from" type to the "to" type.
            if (isAssignable(ctx, from, to)) {
                CsmClassifier fromCls = from.getClassifier();
                CsmClassifier toCls = to.getClassifier();

                if (CsmCompletion.isPrimitiveClass(toCls) && CsmCompletion.isPrimitiveClass(fromCls)) {
                    if (isPromotion(from.getClassifierText().toString(), to.getClassifierText().toString())) {
                        return ConversionCategory.Promotion;
                    } else {
                        return ConversionCategory.StandardConversion;
                    }
                }

                return ConversionCategory.UserDefinedConversion;
            }

            return ConversionCategory.NotConvertable;
        }


        private final int rank;

        private ConversionCategory(int rank) {
            this.rank = rank;
        }

        private static boolean isPromotion(String from, String to) {
            return (to.equals("int") && (from.equals("char") || from.equals("unsigned char") || from.equals("short"))) || // NOI18N
                   (to.equals("double") && from.equals("float")) ||  // NOI18N
                   (to.equals("int") && from.equals("bool"));  // NOI18N
        }
    }

    /**
     * Represents function with a list of conversions needed to call it.
     */
    private static class OverloadingCandidate<T extends CsmFunctional> implements Comparable<OverloadingCandidate<T>> {

        public final T function;

        public final List<Conversion> conversions;

        public OverloadingCandidate(T function, List<Conversion> conversions) {
            this.function = function;
            this.conversions = conversions;
        }

        @Override
        public int compareTo(OverloadingCandidate<T> o) {
            if (conversions.isEmpty() && o.conversions.isEmpty())  {
                return 0;
            } else if (o.conversions.isEmpty()) {
                return -1;
            } else if (conversions.isEmpty()) {
                return 1;
            }

            // 1. Compare by conversions ranks
            int ourWorst = 0;
            int theirWorst = 0;

            while ((ourWorst != conversions.size() - 1 || theirWorst != o.conversions.size() - 1) && conversions.get(ourWorst).isEqual(o.conversions.get(theirWorst))) {
                ourWorst = findNextWorstConversion(conversions, ourWorst);
                theirWorst = findNextWorstConversion(o.conversions, theirWorst);
            }

            Conversion ourConversion = conversions.get(ourWorst);
            Conversion theirConversion = o.conversions.get(theirWorst);

            if (!ourConversion.isEqual(theirConversion)) {
                return ourConversion.category.getRank() - theirConversion.category.getRank();
            }

            // They have similar set of conversion categories - check if one of them is template and other is not
            if (CsmKindUtilities.isTemplate(function) != CsmKindUtilities.isTemplate(o.function)) {
                if (CsmKindUtilities.isTemplate(o.function)) {
                    return -1; // this is better;
                } else {
                    return 1;  // other is better
                }
            }

            // Check if they are not template
            if (!CsmKindUtilities.isTemplate(function)) {
                return 0;
            }

            // Compare two template functions
//            int ourTemplateScore = calcTemplateScore(conversions);
//            int otherTemplateScore = calcTemplateScore(o.conversions);
            return calcTemplateScore((CsmTemplate) o.function, o.conversions) - calcTemplateScore((CsmTemplate) function, conversions);
        }

        private int findNextWorstConversion(List<Conversion> conversions, int from) {
            int index = from;
            Conversion current = conversions.get(index);

            do {
                index++;
            } while (index < conversions.size() && conversions.get(index).isEqual(current));

            return index < conversions.size() ? index : index - 1;
        }

        private int calcTemplateScore(CsmTemplate template, List<Conversion> conversions) {
            int score = 0;
            for (Conversion conversion : conversions) {
                score += conversion.templateScore;
            }
            return score - template.getTemplateParameters().size();
        }
    }

    /**
     * Comparator takes into account number of qualification conversions to ensure that
     * candidates with less such conversions will be before in a list of candidates.
     */
    private static final class OverloadingCandidatesComparator<T extends CsmFunctional> implements Comparator<OverloadingCandidate<T>> {

        @Override
        public int compare(OverloadingCandidate<T> o1, OverloadingCandidate<T> o2) {
            int comparison = o1.compareTo(o2);
            if (comparison == 0) {
                return howManyQualConversions(o1) - howManyQualConversions(o2);
            }
            return comparison;
        }

        private int howManyQualConversions(OverloadingCandidate<T> candidate) {
            int qualConversions = 0;
            for (Conversion conversion : candidate.conversions) {
                if (ConversionCategory.Qualification.equals(conversion.category)) {
                    qualConversions++;
                }
            }
            return qualConversions;
        }
    }
    
    private static final class InstantiatedClassifiersEqualizer implements CsmUtilities.ClassifiersEqualizer {

        @Override
        public boolean areClassifiersEqual(CsmType type1, CsmClassifier cls1, CsmType type2, CsmClassifier cls2) {
            if (CsmKindUtilities.isInstantiation(cls1) && CsmKindUtilities.isInstantiation(cls2)) {
                if (CharSequenceUtilities.textEquals(cls1.getQualifiedName(), cls2.getQualifiedName())) {
                    CharSequence instText1 = CsmInstantiationProvider.getDefault().getInstantiatedText(type1);
                    int lt1 = CharSequenceUtilities.indexOf(instText1, CppTokenId.LT.fixedText());
                    int gt1 = CharSequenceUtilities.lastIndexOf(instText1, CppTokenId.GT.fixedText());
                    CharSequence instText2 = CsmInstantiationProvider.getDefault().getInstantiatedText(type2);
                    int lt2 = CharSequenceUtilities.indexOf(instText2, CppTokenId.LT.fixedText());
                    int gt2 = CharSequenceUtilities.lastIndexOf(instText2, CppTokenId.GT.fixedText());
                    if (lt1 >= 0 && gt1 >= lt1 && lt2 >= 0 && gt2 >= lt2 && (gt1 - lt1) == (gt2 - lt2)) {
                        for (int i = 0; i < (gt1 - lt1); ++i) {
                            if (instText1.charAt(lt1 + i) != instText2.charAt(lt2 + i)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
            return CharSequenceUtilities.textEquals(cls1.getQualifiedName(), cls2.getQualifiedName());
        }
        
    }
    
    private static boolean isTemplateFunction(CsmFunctional fun) {
        if (CsmKindUtilities.isTemplate(fun)) {
            List<CsmTemplateParameter> templateParameters = ((CsmTemplate) fun).getTemplateParameters();
            return templateParameters != null && !templateParameters.isEmpty();
        }
        return false;
    }
    
    public static List<CsmFunction> filterUserDefinedLiterals(CsmType literalType, Collection<CsmFunction> candidates, boolean onlyBest) {
        List<CsmFunction> best = new ArrayList<>();
        if (CsmCompletion.INT_TYPE.equals(literalType)) {
            best.addAll(findUserDefinedLiteral(candidates, onlyBest,
                    CsmCompletion.UNSIGNED_LONG_LONG_TYPE,
                    CsmCompletion.UNSIGNED_LONG_LONG_INT_TYPE,
                    CsmCompletion.STRING_TYPE, 
                    CsmCompletion.CONST_STRING_TYPE
            ));
            if (!onlyBest || best.isEmpty()) {
                best.addAll(findTemplateUserDefinedLiteral(candidates, onlyBest));
            }
        } else if (CsmCompletion.DOUBLE_TYPE.equals(literalType)) {
            best.addAll(findUserDefinedLiteral(candidates, onlyBest,
                    CsmCompletion.LONG_DOUBLE_TYPE, 
                    CsmCompletion.STRING_TYPE, 
                    CsmCompletion.CONST_STRING_TYPE
            ));
            if (!onlyBest || best.isEmpty()) {
                best.addAll(findTemplateUserDefinedLiteral(candidates, onlyBest));
            }
        } else if (CsmCompletion.CHAR_TYPE.equals(literalType)
                || CsmCompletion.WCHAR_TYPE.equals(literalType)) {
            best.addAll(findUserDefinedLiteral(candidates, onlyBest, literalType));
        } else if (CsmCompletion.STRING_TYPE.equals(literalType)
                || CsmCompletion.CONST_STRING_TYPE.equals(literalType)) {
            best.addAll(findUserDefinedLiteral(candidates, onlyBest, 
                    Arrays.asList(CsmCompletion.CONST_STRING_TYPE, CsmCompletion.UNSIGNED_LONG_TYPE),
                    Arrays.asList(CsmCompletion.CONST_STRING_TYPE, CsmCompletion.LONG_UNSIGNED_TYPE),
                    Arrays.asList(CsmCompletion.CONST_STRING_TYPE, CsmCompletion.UNSIGNED_LONG_INT_TYPE),
                    Arrays.asList(CsmCompletion.CONST_STRING_TYPE, CsmCompletion.LONG_UNSIGNED_INT_TYPE)
            ));
        } else if (CsmCompletion.WSTRING_TYPE.equals(literalType)
                || CsmCompletion.CONST_WSTRING_TYPE.equals(literalType)) {
            best.addAll(findUserDefinedLiteral(candidates, onlyBest, 
                    Arrays.asList(CsmCompletion.CONST_WSTRING_TYPE, CsmCompletion.UNSIGNED_LONG_TYPE),
                    Arrays.asList(CsmCompletion.CONST_WSTRING_TYPE, CsmCompletion.LONG_UNSIGNED_TYPE),
                    Arrays.asList(CsmCompletion.CONST_WSTRING_TYPE, CsmCompletion.UNSIGNED_LONG_INT_TYPE),
                    Arrays.asList(CsmCompletion.CONST_WSTRING_TYPE, CsmCompletion.LONG_UNSIGNED_INT_TYPE)
            ));
        }
        return best;
    }
    
    private static Collection<CsmFunction> findUserDefinedLiteral(Collection<CsmFunction> candidates, boolean onlyBest, CsmType...orderedParamTypes) {
        List<CsmType> orderedParamsAsLists[] = new List[orderedParamTypes.length];
        for (int i = 0; i < orderedParamTypes.length; ++i) {
            orderedParamsAsLists[i] = Arrays.asList(orderedParamTypes[i]);
        }
        return findUserDefinedLiteral(candidates, onlyBest, orderedParamsAsLists);
    }
    
    private static Collection<CsmFunction> findUserDefinedLiteral(Collection<CsmFunction> candidates, boolean onlyBest, List<CsmType>...orderedParamTypes) {
        LinkedHashSet<CsmFunction> result = new LinkedHashSet<>(2);
        for (List<CsmType> paramTypes : orderedParamTypes) {
            result.addAll(findExactUserDefinedLiteral(paramTypes, candidates, onlyBest));
            if (!result.isEmpty() && onlyBest) {
                break;
            }
        }
        return result;
    }
    
    private static List<CsmFunction> findExactUserDefinedLiteral(List<CsmType> paramTypes, Collection<CsmFunction> candidates, boolean onlyBest) {
        List<CsmFunction> res = new ArrayList<>(1);
        outer:
        for (CsmFunction candidate : candidates) {
            if (candidate.getParameters().size() != paramTypes.size()) {
                continue;
            }
            if (CsmKindUtilities.isTemplate(candidate)) {
                continue;
            }
            CsmParameter[] methodParms = candidate.getParameters().toArray(new CsmParameter[candidate.getParameters().size()]);
            for (int i = 0; i < methodParms.length; ++i) {
                CsmType mpt = methodParms[i].getType();
                CsmType goldenType = paramTypes.get(i);
                if (mpt == null || goldenType == null) {
                    continue outer;
                }
                if (!CsmUtilities.checkTypesEqual(
                        goldenType, goldenType.getContainingFile(), 
                        mpt, mpt.getContainingFile(), 
                        IgnoreConstVolatileQualsEqualizer.INSTANCE
                )) {
                    continue outer;
                }
            }
            res.add(candidate);
            if (onlyBest) {
                break;
            }
        }
        return res;
    }
    
    private static List<CsmFunction> findTemplateUserDefinedLiteral(Collection<CsmFunction> candidates, boolean onlyBest) {
        // If the literal operator is a template, it must have an empty parameter list 
        // and can have only one template parameter, which must be a non-type template parameter pack 
        // with element type char
        List<CsmFunction> result = new ArrayList<>(2);
        for (CsmFunction fun : candidates) {
            if (!fun.getParameters().isEmpty()) {
                continue;
            }
            if (!CsmKindUtilities.isTemplate(fun)) {
                continue;
            }
            CsmTemplate asTemplate = (CsmTemplate) fun;
            List<CsmTemplateParameter> templateParameters = asTemplate.getTemplateParameters();
            if (templateParameters.size() != 1) {
                continue;
            }
            CsmTemplateParameter templateParam = templateParameters.get(0);
            if (templateParam.isTypeBased() 
                    || !CsmKindUtilities.isClassifierBasedTemplateParameter(templateParam)
                    /*|| !templateParam.isVarArgs()*/ ) { // FIXME: isVarArgs works only for type based parameters!
                continue;
            }
            // FIXME: classifier based template parameters are not working now:
            // for parameter "char...Param" the class of it is "Param". This must be fixed!
            if (false) {
                CsmClassifier templateCls = (CsmClassifier) templateParam;
                if (!isSpecifiedClass(templateCls.getName(), CsmCompletion.CHAR_CLASS)) {
                    continue;
                }
            } else {
                // workaround
                if (!CharSequenceUtilities.startsWith(templateParam.getText(), CsmCompletion.CHAR_CLASS.getName())) {
                    continue;
                }
            }
            result.add(fun);
            if (onlyBest) {
                break;
            }
        }
        return result;
    }
    
    private static final class IgnoreConstVolatileQualsEqualizer implements QualifiersEqualizer {
        
        public static final IgnoreConstVolatileQualsEqualizer INSTANCE = new IgnoreConstVolatileQualsEqualizer();
        
        private IgnoreConstVolatileQualsEqualizer() {}

        @Override
        public boolean areQualsEqual(CsmType from, CsmType to) {
            TypeInfoCollector typeInfo1 = new TypeInfoCollector();
            iterateTypeChain(from, typeInfo1);

            TypeInfoCollector typeInfo2 = new TypeInfoCollector();
            iterateTypeChain(to, typeInfo2);

            Iterator<Qualificator> qualIter1 = typeInfo1.qualificators.iterator();
            Iterator<Qualificator> qualIter2 = typeInfo2.qualificators.iterator();
            while (qualIter1.hasNext() && qualIter2.hasNext()) {
                if (fetchNextQual(qualIter1) != fetchNextQual(qualIter2)) {
                    return false;
                }
            }
            return fetchNextQual(qualIter1) == fetchNextQual(qualIter2);
        }
        
        private Qualificator fetchNextQual(Iterator<Qualificator> qualIter) {
            while (qualIter.hasNext()) {
                Qualificator qual = qualIter.next();
                if (qual != Qualificator.CONST && qual != Qualificator.VOLATILE) {
                    return qual;
                }
            }
            return null;
        }
    }

    ////////////////////////////////////////////////
    // overriden functions to resolve expressions
    /////////////////////////////////////////////////

    // utitlies

    public static boolean isCompletionEnabled(Document doc, int offset) {
        return isCompletionEnabled(doc, offset, -1);
    }

    public static boolean isCompletionEnabled(final Document doc, final int offset, final int queryType) {
        final AtomicBoolean out = new AtomicBoolean(false);
        doc.render(new Runnable() {
            @Override
            public void run() {
                out.set(isCompletionEnabledImpl(doc, offset, queryType));
            }
        });
        return out.get();
    }

    private static boolean isCompletionEnabledImpl(Document doc, int offset, int queryType) {
        if (doc.getLength() == 0) {
            // it's fine to show completion in empty doc
            return true;
        }
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, true, offset > 0);
        if (ts == null) {
            return false;
        }
        if (ts.offset() < offset && offset <= ts.offset() + ts.token().length()) {
            TokenId id = ts.token().id();
            if(id instanceof CppTokenId) {
                // completion is disabled in some tokens
                switch ((CppTokenId)id) {
                    case RPAREN:
                        if (queryType > 0) {
                            return false;
                        }
                        break;

                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case CHAR_LITERAL:
                    case STRING_LITERAL:
                    case RAW_STRING_LITERAL:
                    case PREPROCESSOR_USER_INCLUDE:
                    case PREPROCESSOR_SYS_INCLUDE:
                    case PREPROCESSOR_DEFINED:
                        return false;

                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        // ok after end of token
                        return offset == ts.offset() + ts.token().length();
                }
                // main completion is not responsible
                if (CppTokenId.PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY.equals(ts.token().id().primaryCategory())) {
                    return false;
                }
                if (queryType != CompletionProvider.TOOLTIP_QUERY_TYPE && CppTokenId.NUMBER_CATEGORY.equals(ts.token().id().primaryCategory())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean needShowCompletionOnTextLite(JTextComponent target, String typedText, String[] triggers) {
        char typedChar = typedText.charAt(typedText.length() - 1);
        for(String pattern : triggers) {
            if (pattern.length() > 0) {
                if (pattern.charAt(pattern.length()-1) == typedChar)  {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean needShowCompletionOnText(JTextComponent target, String typedText, String[] triggers) {
        if (needShowCompletionOnTextLite(target, typedText, triggers)) {
            int dotPos = target.getCaret().getDot();
            Document doc = target.getDocument();
            for (String pattern : triggers) {
                if (!pattern.isEmpty()) {
                    if (dotPos >= pattern.length()) {
                        try {
                            String text = doc.getText(dotPos-pattern.length(), pattern.length());
                            if (pattern.equals(text)) {
                                if (dotPos > pattern.length()) {
                                    char prev = doc.getText(dotPos-pattern.length()-1, 1).charAt(0);
                                    char first = pattern.charAt(0);
                                    if (Character.isJavaIdentifierPart(first)) {
                                        if (!Character.isJavaIdentifierPart(prev)) {
                                            return true;
                                        }
                                    } else {
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            }
                        } catch (BadLocationException ex) {
                            //
                        }
                    }
                }

            }
        }
        return false;
    }

    private static boolean equalTypes(CsmType t, CsmType mpt, boolean ignoreConstAndRef) {
        assert t != null;
        if (t.equals(mpt)) {
            return true;
        } else if (mpt != null) {
            String t1 = t.getCanonicalText().toString();
            String canonicalText = mpt.getCanonicalText().toString().trim();
            if (ignoreConstAndRef) {
                if (canonicalText.endsWith("&")) { //NOI18N
                    canonicalText = canonicalText.substring(0, canonicalText.length()-1);
                }
                if (canonicalText.startsWith("const") && canonicalText.length() > 5 && Character.isWhitespace(canonicalText.charAt(5))) { //NOI18N
                    canonicalText = canonicalText.substring(6);
                }
            }
            return t1.equals(canonicalText);
        }
        return false;
    }

    static CsmType extractFunctionType(Context context, Collection<? extends CsmFunctional> mtdList, CsmCompletionExpression genericNameExp, List<CsmType> typeList) {
        CsmType out = null;
        if (mtdList.isEmpty()) {
            return null;
        }
        for (CsmFunctional fun : mtdList) {
            CsmObject entity = fun;

            if (CsmKindUtilities.isConstructor(entity)) {
                entity = ((CsmConstructor) entity).getContainingClass();
            }

            if (CsmKindUtilities.isFunction(entity)) {
                if (context != null) {
                    out = context.getObjectType(entity, false);
                } else {
                    out = ((CsmFunction) entity).getReturnType();
                }
            } else if (CsmKindUtilities.isFunctional(entity)) {
                out = ((CsmFunctional) entity).getReturnType();
            } else if (CsmKindUtilities.isClassifier(entity)) {
                out = CsmCompletion.createType((CsmClassifier) entity, 0, 0, 0, false, false);
            }
            if (out != null) {
                break;
            }
        }
        return out;
    }

    static CsmObject createInstantiation(Context context, CsmTemplate template, CsmCompletionExpression exp, List<CsmType> typeList) {
        if (exp != null || !typeList.isEmpty() || context.getContextInstantiations() != null) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();

            List<CsmSpecializationParameter> params = new ArrayList<CsmSpecializationParameter>();
            params.addAll(collectInstantiationParameters(context, template, ip, exp));
            if (CsmKindUtilities.isFunction(template)) {
                params.addAll(collectInstantiationParameters(context, ip, (CsmFunction)template, params.size(), typeList));
            }

            CsmObject instantiation = ip.instantiate(template, params);

            if (CsmKindUtilities.isTemplate(instantiation)) {
                List<CsmInstantiation> contextInstantiations = context.getContextInstantiations();
                if (contextInstantiations != null && !contextInstantiations.isEmpty()) {
                    ListIterator<CsmInstantiation> iter = contextInstantiations.listIterator(contextInstantiations.size());
                    while (iter.hasPrevious()) {
                        instantiation = ip.instantiate((CsmTemplate) instantiation, iter.previous());
                    }
                }
            }

            return instantiation;
        }
        return null;
    }

    static List<CsmSpecializationParameter> collectInstantiationParameters(Context context, CsmTemplate template, CsmInstantiationProvider ip, CsmCompletionExpression exp) {
        if (exp != null) {
            List<CsmSpecializationParameter> params = new ArrayList<CsmSpecializationParameter>();
            if (exp.getExpID() == CsmCompletionExpression.GENERIC_TYPE) {
                Iterator<CsmTemplateParameter> tplParamIter = template.getTemplateParameters().iterator();
                if (tplParamIter.hasNext()) {
                    CsmTemplateParameter tplParam = null;
                    int paramsNumber = exp.getParameterCount() - 1;
                    for (int i = 0; i < paramsNumber; i++) {
                        if  (tplParamIter.hasNext()) {
                            tplParam = tplParamIter.next();
                        } else if (tplParam == null || !tplParam.isVarArgs()) {
                            // Actually tplParam cannot be null
                            break;
                        }
                        CsmCompletionExpression paramInst = exp.getParameter(i + 1);
                        if (paramInst != null) {
                            switch (paramInst.getExpID()) {
                                case CsmCompletionExpression.CONSTANT:
                                    params.add(ip.createExpressionBasedSpecializationParameter(
                                        paramInst.getTokenText(0),
                                        getContextScope(context),
                                        context.getContextFile(),
                                        paramInst.getTokenOffset(0),
                                        paramInst.getTokenOffset(0) + paramInst.getTokenLength(0))
                                    );
                                    break;
                                default:
                                    CsmType type = null;

                                    // Handle declaration of function types
                                    if (tplParam.isTypeBased() && canBeFunctionType(paramInst)) {
                                        Pair<String, String> aptLangFlavor = CsmFileInfoQuery.getDefault().getAPTLanguageFlavor(
                                                CsmFileInfoQuery.getDefault().getFileLanguageFlavor(context.getContextFile())
                                        );
                                        RenderedExpression renderedExpression = renderExpression(paramInst, new ExpressionBuilderImpl.Creator());
                                        type = CsmTypes.createType(renderedExpression.text, context.getLexicalContextScope(), new CsmTypes.SequenceDescriptor(
                                            aptLangFlavor.first(),
                                            aptLangFlavor.second(),
                                            false,
                                            true,
                                            false,
                                            new CsmTypes.OffsetDescriptor(context.getContextFile(), renderedExpression.startOffset, renderedExpression.endOffset)
                                        ));
                                    } else if (!canBeExpression(paramInst)) {
                                        type = context.resolveType(paramInst);
                                        if (type != null) {
                                            // Check if it is variable
                                            List<? extends CompletionItem> candidates = context.resolveObj(paramInst);
                                            if (candidates != null && !candidates.isEmpty() && candidates.get(0) instanceof CsmResultItem) {
                                                CsmResultItem resItem = (CsmResultItem) candidates.get(0);
                                                if (CsmKindUtilities.isCsmObject(resItem.getAssociatedObject()) && CsmKindUtilities.isVariable((CsmObject) resItem.getAssociatedObject())) {
                                                    type = null;
                                                }
                                            }
                                        }
                                    }

                                    if (type != null) {
                                        RenderedExpression renderedExpression = renderExpression(paramInst, new MockExpressionBuilderImpl.Creator());
                                        params.add(ip.createTypeBasedSpecializationParameter(
                                                type,
                                                getContextScope(context),
                                                context.getContextFile(),
                                                renderedExpression.startOffset,
                                                renderedExpression.endOffset
                                        ));
                                    } else {
                                        RenderedExpression renderedExpression = renderExpression(paramInst, new ExpressionBuilderImpl.Creator());
                                        params.add(ip.createExpressionBasedSpecializationParameter(
                                                renderedExpression.text,
                                                getContextScope(context),
                                                context.getContextFile(),
                                                renderedExpression.startOffset,
                                                renderedExpression.endOffset
                                        ));
                                    }
                                }
                        } else {
                            break;
                        }
                    }
                }
            }
            return params;
        }
        return Collections.emptyList();
    }

    static List<CsmSpecializationParameter> collectInstantiationParameters(Context context, CsmInstantiationProvider ip, CsmFunction function, int explicitelyMappedSize, List<CsmType> typeList) {
        if (CsmKindUtilities.isTemplate(function)) {
            List<CsmSpecializationParameter> result = new ArrayList<CsmSpecializationParameter>();

            CsmTemplate template = (CsmTemplate) function;
            List<CsmTemplateParameter> templateParams = template.getTemplateParameters();

            if (templateParams.size() > explicitelyMappedSize) {
                Map<CsmTemplateParameter, CsmType[]> paramsMap = gatherTemplateParamsMap(function, typeList);

                for (int i = explicitelyMappedSize; i < templateParams.size(); i++) {
                    CsmTemplateParameter param = templateParams.get(i);
                    CsmType mappedTypes[] = paramsMap.get(param);
                    if (mappedTypes != null && mappedTypes.length > 0) {
                        if (!param.isVarArgs()) {
                            result.add(ip.createTypeBasedSpecializationParameter(mappedTypes[0], getContextScope(context)));
                        } else {
                            for (CsmType mappedType : mappedTypes) {
                                result.add(ip.createTypeBasedSpecializationParameter(mappedType, getContextScope(context)));
                            }
                        }
                    } else {
                        // error
                        return result;
                    }
                }
            }

            return result;
        }
        return Collections.emptyList();
    }

    static Map<CsmTemplateParameter, CsmType[]> gatherTemplateParamsMap(CsmFunction function, List<CsmType> typeList) {
        assert CsmKindUtilities.isTemplate(function) : "Attempt to gather template parameters map from non-template function"; // NOI18N
        CsmTemplate template = (CsmTemplate) function;

        Map<CsmTemplateParameter, CsmType[]> map = new HashMap<CsmTemplateParameter, CsmType[]>();

        for (CsmTemplateParameter templateParam : template.getTemplateParameters()) {
            int paramIndex = 0;
            Collection<CsmParameter> funParams = function.getParameters();
            for (CsmParameter param : funParams) {
                if (paramIndex >= typeList.size()) {
                    break;
                }
                CsmType paramType = param.getType();
                DefaultDeduceTemplateTypeStrategy calcStrategy = new DefaultDeduceTemplateTypeStrategy(DeduceTemplateTypeStrategy.Error.MatchQualsError);
                if (CsmKindUtilities.isTemplateParameterType(paramType) && paramIndex == funParams.size() - 1
                        && templateParam.equals(((CsmTemplateParameterType) paramType).getParameter())
                        && templateParam.isVarArgs()) {
                    List<CsmType> varArgs = new ArrayList<CsmType>();
                    int argIndex = paramIndex;
                    while (argIndex < typeList.size()) {
                        CsmType calculatedTypes[] = CsmInstantiationProvider.getDefault().deduceTemplateType(templateParam, paramType, typeList.get(argIndex), calcStrategy);
                        if (calculatedTypes != null && calculatedTypes.length > 0) {
                            varArgs.addAll(Arrays.asList(calculatedTypes)); // actually, calculatedTypes should have length = 1
                        }
                        argIndex++;
                    }
                    map.put(templateParam, varArgs.toArray(new CsmType[varArgs.size()]));
                    break;
                } else {
                    CsmType calculatedTypes[] = CsmInstantiationProvider.getDefault().deduceTemplateType(templateParam, paramType, typeList.get(paramIndex), calcStrategy);
                    if (calculatedTypes != null && calculatedTypes.length > 0) {
                        map.put(templateParam, calculatedTypes);
                        break;
                    }
                }
                paramIndex++;
            }
        }

        return map;
    }

    static RenderedExpression renderExpression(CsmCompletionExpression expr, ExpressionBuilderCreator creator) {
        if (expr == null) {
            return null;
        }
        boolean allowSimilarEntities = true;
        switch (expr.getExpID()) {
            case CsmCompletionExpression.GENERIC_TYPE: {
                ExpressionBuilder eb = creator.create();
                int startExpOffset = expr.getTokenOffset(0);
                int endExpOffset = startExpOffset;

                for (int paramIndex = 0; paramIndex < expr.getParameterCount(); paramIndex++) {
                    RenderedExpression current = renderExpression(expr.getParameter(paramIndex), creator);

                    eb.append(current.text);

                    if (paramIndex > 0) {
                        if (paramIndex < expr.getParameterCount() - 1) {
                            eb.append(","); // NOI18N
                        } else {
                            eb.append("> "); // NOI18N
                        }
                        endExpOffset++;
                    } else {
                        eb.append(expr.getTokenText(0));
                    }

                    endExpOffset = current.endOffset;
                }

                return new RenderedExpression(eb.toString(), startExpOffset, endExpOffset);
            }

            case CsmCompletionExpression.UNARY_OPERATOR:
            case CsmCompletionExpression.OPERATOR:
            case CsmCompletionExpression.SCOPE:
                allowSimilarEntities = false;
                // fall through

            case CsmCompletionExpression.METHOD:
            case CsmCompletionExpression.CONVERSION:
            case CsmCompletionExpression.PARENTHESIS: {
                ExpressionBuilder eb = creator.create();
                int startExpOffset = -1;
                int endExprOffset = -1;

                int paramCount = expr.getParameterCount();
                int tokenCount = expr.getTokenCount();
                int paramIndex = 0;
                int tokenIndex = 0;

                RenderedExpression renderedParam = null;
                RenderedExpression renderedToken = null;
                boolean lastWasParam = false;
                boolean lastWasToken = false;

                boolean entityChanged = true;

                while (entityChanged || allowSimilarEntities) {
                    entityChanged = false;

                    if (renderedParam == null && paramIndex < paramCount) {
                        renderedParam = renderExpression(expr.getParameter(paramIndex), creator);
                        paramIndex++;
                    }

                    if (renderedToken == null && tokenIndex < tokenCount) {
                        ExpressionBuilder tokenExprBuilder = creator.create();
                        tokenExprBuilder.append(expr.getTokenText(tokenIndex));
                        renderedToken = new RenderedExpression(
                            tokenExprBuilder.toString(),
                            expr.getTokenOffset(tokenIndex),
                            expr.getTokenOffset(tokenIndex) + expr.getTokenLength(tokenIndex)
                        );
                        tokenIndex++;
                    }

                    if (renderedToken == null && renderedParam == null) {
                        break;
                    }

                    RenderedExpression chosenExpression;

                    if (renderedParam != null && renderedToken != null) {
                        if (renderedParam.startOffset < renderedToken.startOffset) {
                            chosenExpression = renderedParam;
                        } else if (renderedParam.startOffset == renderedToken.startOffset) {
                            chosenExpression = (renderedParam.endOffset < renderedToken.endOffset) ? renderedParam : renderedToken;
                        } else {
                            chosenExpression = renderedToken;
                        }
                    } else if (renderedToken == null) {
                        chosenExpression = renderedParam;
                    } else {
                        chosenExpression = renderedToken;
                    }

                    if (chosenExpression != null) {
                        if (chosenExpression == renderedParam) {
                            renderedParam = null;
                            entityChanged = !lastWasParam;
                            lastWasParam = true;
                            lastWasToken = false;
                        } else {
                            renderedToken = null;
                            entityChanged = !lastWasToken;
                            lastWasToken = true;
                            lastWasParam = false;
                        }

                        if (entityChanged || allowSimilarEntities) {
                            eb.append(chosenExpression.text);
                            if (startExpOffset == -1) {
                                startExpOffset = chosenExpression.startOffset;
                            }
                            endExprOffset = chosenExpression.endOffset;
                        }
                    }
                }

                return new RenderedExpression(eb.toString(), startExpOffset, endExprOffset);
            }

            default: {
                if (expr.getTokenCount() > 0) {
                    ExpressionBuilder eb = creator.create();
                    eb.append(expr.getTokenText(0));
                    return new RenderedExpression(
                            eb.toString(),
                            expr.getTokenOffset(0),
                            expr.getTokenOffset(0) + expr.getTokenLength(0)
                    );
                }
                return new RenderedExpression("", 0, 0); // NOI18N
            }
        }
    }

    CsmType findExactVarType(CsmFile file, String var, int docPos, FileReferencesContext refContext) {
        if (file == null) {
            return null;
        }
        int pos = doc2context(docPos);
        CsmContext context = CsmOffsetResolver.findContext(file, pos, refContext);
        if (var.length() == 0 && CsmKindUtilities.isVariable(context.getLastObject()) && ((CsmVariable)context.getLastObject()).getInitialValue() != null) {
            // probably in initializer of variable, like
            // struct AAA a[] = { { .field = 1}, { .field = 2}};
            CsmVariable varObj = (CsmVariable) context.getLastObject();
            CsmClassifier varCls = CsmBaseUtilities.getOriginalClassifier(varObj.getType().getClassifier(), file);
            if (varCls != null) {
                CsmClass contextClass = CsmContextUtilities.getContextClassInInitializer(varObj, varCls, pos, getFinder());
                if (contextClass != null) {
                    // Note that we can extract type from CsmField if necessary
                    return CsmCompletion.createType(contextClass, 0, 0, 0, false, false);
                }
            }
            if (CsmOffsetUtilities.isInObject(varObj.getInitialValue(), pos)) {
                CsmType type = varObj.getType();
                if (type.getArrayDepth() > 0) {
                    CsmClassifier cls = type.getClassifier();
                    if (cls != null) {
                        type = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                    }
                }
                return type;
            }
        }
        if (var.length() == 0 && CsmKindUtilities.isStatement(context.getLastObject())) {
            CsmStatement stmt = (CsmStatement)context.getLastObject();
            if(stmt.getKind() == Kind.RETURN) {
                CsmReturnStatement ret = (CsmReturnStatement) stmt;
                // ret.getReturnExpression() is not implemented
                // so here is a hack with regexp...
                try {
                    String e = getDocument().getText(ret.getStartOffset(), ret.getEndOffset() - ret.getStartOffset());
                    String typeName = e.replaceAll("((\\W|\n)*)return((\\W|\n|&)*)\\((.*)\\)((\\W|\n)*)\\{((.|\n)*)\\}((.|\n)*)", "$5"); // NOI18N
                    CsmClassifier cls = getClassFromName(getFinder(), typeName, true);
                    if (cls != null) {
                        CsmType type = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                        return type;
                    }
                } catch (BadLocationException ex) {
                }
            } else if(stmt.getKind() == Kind.EXPRESSION) {
                // ret.getReturnExpression() is not implemented
                // so here is a hack with regexp...
                try {
                    String e = getDocument().getText(stmt.getStartOffset(), stmt.getEndOffset() - stmt.getStartOffset());
                    String typeName = e.replaceAll("((.|\n)*)=((\\W|\n|&)*)\\((.*)\\)((\\W|\n)*)\\{((.|\n)*)\\}((.|\n)*)", "$5"); // NOI18N
                    CsmClassifier cls = getClassFromName(getFinder(), typeName, true);
                    if (cls != null) {
                        CsmType type = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                        return type;
                    }
                } catch (BadLocationException ex) {
                }
            }
        }
        for (CsmDeclaration decl : CsmContextUtilities.findFunctionLocalVariables(context)) {
            if (CsmKindUtilities.isVariable(decl)) {
                CsmVariable v = (CsmVariable) decl;
                if (v.getName().toString().equals(var)) {
                    return v.getType();
                }
            }
        }
        return null;
    }

    private static boolean canBeFunctionType(CsmCompletionExpression expression) {
        if (expression.getExpID() == CsmCompletionExpression.METHOD) {
            // aaa(bbb)
            return true;
        }
        if (expression.getExpID() == CsmCompletionExpression.PARENTHESIS) {
            if (expression.getParameterCount() > 0) {
                if (expression.getParameter(0).getExpID() == CsmCompletionExpression.CONVERSION) {
                    // int(int, int, int)
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canBeExpression(CsmCompletionExpression expression) {
        return expression.getExpID() == CsmCompletionExpression.OPERATOR ||
               expression.getExpID() == CsmCompletionExpression.UNARY_OPERATOR ||
               expression.getExpID() == CsmCompletionExpression.CONSTANT ||
               expression.getExpID() == CsmCompletionExpression.METHOD;
    }

    private static CsmScope getContextScope(Context context) {
        if (context != null) {
            if (context.getLexicalContextScope() != null) {
                return context.getLexicalContextScope();
            }
            CsmOffsetableDeclaration contextElement = context.getContextElement();
            if (CsmKindUtilities.isScope(contextElement)) {
                return (CsmScope) contextElement;
            } else if (CsmKindUtilities.isScopeElement(contextElement)) {
                return contextElement.getScope();
            }
        }
        return null;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.lastSeparatorOffset = -1;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.lastSeparatorOffset = -1;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.lastSeparatorOffset = -1;
    }


    static class RenderedExpression {

        public final String text;

        public final int startOffset;

        public final int endOffset;

        public RenderedExpression(String text, int startOffset, int endOffset) {
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public String toString() {
            return text + "[" + startOffset + "," + endOffset + "]"; // NOI18N
        }
    }

    static interface ExpressionBuilder {

        ExpressionBuilder append(String str);

        @Override
        public String toString();

    }

    static interface ExpressionBuilderCreator {

        ExpressionBuilder create();

    }

    private static class ExpressionBuilderImpl implements ExpressionBuilder {

        private final StringBuilder sb = new StringBuilder();

        @Override
        public ExpressionBuilder append(String str) {
            sb.append(str);
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }

        public static class Creator implements ExpressionBuilderCreator {

            @Override
            public ExpressionBuilder create() {
                return new ExpressionBuilderImpl();
            }
        }
    }

    private static class MockExpressionBuilderImpl implements ExpressionBuilder {

        @Override
        public ExpressionBuilder append(String str) {
            return this;
        }

        @Override
        public String toString() {
            return ""; // NOI18N
        }

        public static class Creator implements ExpressionBuilderCreator {

            private final MockExpressionBuilderImpl instance = new MockExpressionBuilderImpl();

            @Override
            public ExpressionBuilder create() {
                return instance;
            }
        }
    }

    private static class ResolvedTypeInfoCollector implements CsmExpressionResolver.ResolvedTypeHandler {

        private final TypeInfoCollector collector;

        public ResolvedTypeInfoCollector(TypeInfoCollector collector) {
            this.collector = collector;
        }

        @Override
        public void process(CsmType resolvedType) {
            if (resolvedType != null) {
                collector.check(resolvedType);
            }
        }
    }

    private static class AnalyzedType {

        public final CsmType type;

        public final CsmType origType;

        public final TypeInfoCollector typeInfo;

        public final CsmClassifier classifier;

        public static AnalyzedType create(Context ctx, CsmType toAnalyze, boolean deepResolving, boolean checkInstantiationViability) {
            TypeInfoCollector typeInfo = new TypeInfoCollector();
            CsmType type = iterateTypeChain(toAnalyze, typeInfo);
            CsmClassifier cls = type.getClassifier();

            if (deepResolving && (cls == null || CsmBaseUtilities.isUnresolved(cls))) {
                if (ctx != null && CsmExpressionResolver.shouldResolveAsMacroType(type, ctx.getLexicalContextScope())) {
                    List<CsmInstantiation> instantiations = CsmInstantiationProvider.getDefault().getInstantiatedTypeInstantiations(type);
                    type = CsmExpressionResolver.resolveMacroType(type, ctx.getLexicalContextScope(), instantiations, new ResolvedTypeInfoCollector(typeInfo));
                    cls = (type != null) ? type.getClassifier() : null;
                }
            }

            if (!isViableClassifier(cls, checkInstantiationViability)) {
                return null;
            }

            return new AnalyzedType(type, toAnalyze, typeInfo, cls);
        }

        private AnalyzedType(CsmType type, CsmType origType, TypeInfoCollector typeInfo, CsmClassifier classifier) {
            this.type = type;
            this.origType = origType;
            this.typeInfo = typeInfo;
            this.classifier = classifier;
        }
    }
}
