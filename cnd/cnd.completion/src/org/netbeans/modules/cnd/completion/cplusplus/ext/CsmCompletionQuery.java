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

import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmFunctional;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeAlias;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.deep.CsmRangeForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmReturnStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import static org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver.ResolvedTypeHandler;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmResolveContext;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.isPointer;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmCompletionQuery.NbCsmItemFactory;
import static org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport.areTemplatesEnabled;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.isAutoType;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem.TemplateParameterResultItem;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem.VariableResultItem;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.QueryScope;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver.Result;
import org.netbeans.modules.cnd.completion.csm.CompletionResolverImpl;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmContextUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmStatementResolver;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.Predicate;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.ConstantPredicate;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.TypeInfoCollector;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.howMany;
import static org.netbeans.modules.cnd.modelutil.CsmUtilities.isDecltypeAutoType;
import org.netbeans.modules.cnd.utils.MutableObject;

/**
 *
 *
 * @version 1.00
 */
abstract public class CsmCompletionQuery {

    private BaseDocument baseDocument;
    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CsmCompletionQuery.class, "completion-no-suggestions");
    private static final String PROJECT_BEEING_PARSED = NbBundle.getMessage(CsmCompletionQuery.class, "completion-project-beeing-parsed");
    private static final boolean TRACE_COMPLETION = Boolean.getBoolean("cnd.completion.trace");
    private static final boolean TRACE_MULTIPLE_VISIBE_OBJECTS = Boolean.getBoolean("cnd.trace.multiple.visible");
    private static CsmItemFactory itemFactory;

    private static final int MAX_DEPTH = 15;

    private static enum AntiloopClient {
        query_type,
        query_objects
    }

    private static final ThreadLocal<Map<AntiloopClient, Set<CsmOffsetable>>> threadLocalAntiloopMap = new ThreadLocal<Map<AntiloopClient, Set<CsmOffsetable>>>() {

        @Override
        protected Map<AntiloopClient, Set<CsmOffsetable>> initialValue() {
            return new EnumMap<AntiloopClient, Set<CsmOffsetable>>(AntiloopClient.class);
        }

    };

    Set<CsmOffsetable> antiLoop = new HashSet<CsmOffsetable>();

    // the only purpose of this method is that NbJavaCompletionQuery
    // can use it to retrieve baseDocument's fileobject and create correct
    // CompletionResolver with the correct classpath of project to which the file belongs
    protected BaseDocument getBaseDocument() {
        return baseDocument;
    }

    abstract protected CompletionResolver getCompletionResolver(CsmScope contextScope, boolean openingSource, boolean sort, boolean inIncludeDirective);

    abstract protected CsmFinder getFinder();

    abstract protected QueryScope getCompletionQueryScope();

    abstract protected FileReferencesContext getFileReferencesContext();

    abstract public CsmFile getCsmFile();

    private CsmProject getCsmProject() {
        CsmFile csmFile = getCsmFile();
        if (csmFile != null) {
            return csmFile.getProject();
        }
        return null;
    }

    public CsmCompletionQuery() {
    }

    public CsmCompletionResult query(JTextComponent component, int offset, boolean instantiateTypes) {
        boolean sort = false; // TODO: review
        return query(component, offset, false, sort, instantiateTypes);
    }

    /**
     * Perform the query on the given component. The query usually
     * gets the component's baseDocument, the caret position and searches back
     * to find the last command start. Then it inspects the text up to the caret
     * position and returns the result.
     *
     * @param component the component to use in this query.
     * @param offset position in the component's baseDocument to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @param openingSource whether the query is performed to open the source file.
     *  The query tries to return exact matches if this flag is true
     * @return result of the query or null if there's no result.
     */
    public CsmCompletionResult query(JTextComponent component, int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes) {
        BaseDocument doc = (BaseDocument) component.getDocument();
//        CompletionDocumentationProviderImpl.doc = doc; // Fixup....
        return query(component, doc, offset, openingSource, sort, instantiateTypes);
    }

    public CsmCompletionResult query(JTextComponent component, int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes, boolean tooltip) {
        BaseDocument doc = (BaseDocument) component.getDocument();
//        CompletionDocumentationProviderImpl.doc = doc; // Fixup....
        return query(component, doc, offset, openingSource, sort, instantiateTypes, tooltip);
    }

    static class QueryObjectsTask implements QueryTask {

        private Collection<CsmObject> objects;

        @Override
        public void perform(Context context) {
            if (context != null && context.result != null) {
                List<? extends CompletionItem> items = context.result.getItems();
                if (!items.isEmpty()) {
                    objects = new ArrayList<CsmObject>(items.size());
                    for (CompletionItem item : items) {
                        if (item instanceof CsmResultItem) {
                            CsmResultItem csmItem = (CsmResultItem) item;
                            objects.add((CsmObject) csmItem.getAssociatedObject());
                        }
                    }
                }
            }
        }

        @Override
        public boolean isFindTypeTask() {
            return false;
        }

        @Override
        public AntiloopClient getAntiloopKind() {
            return AntiloopClient.query_objects;
        }

        public Collection<CsmObject> getObjects() {
            return objects;
        }
    }

    /**
     * Perform the query on the given component to get collection of possible objects.
     *
     * @param expression - expression (qualified id for example)
     * @param instantiations - context
     */
    public Collection<CsmObject> queryObjects(CsmOffsetable expression, List<CsmInstantiation> instantiations) {
        QueryObjectsTask task = new QueryObjectsTask();
        performQueryTask(expression, instantiations, task);
        return task.getObjects();
    }

    static class QueryTypeTask implements QueryTask {

        private final ResolvedTypeHandler resolvedHandler;

        public QueryTypeTask(ResolvedTypeHandler resolvedHandler) {
            this.resolvedHandler = resolvedHandler;
        }

        @Override
        public void perform(Context context) {
            if (context != null) {
                CsmType type = context.lastType;
                resolvedHandler.process(type);
            }
        }

        @Override
        public boolean isFindTypeTask() {
            return true;
        }

        @Override
        public AntiloopClient getAntiloopKind() {
            return AntiloopClient.query_type;
        }
    }

    /**
     * Perform the query on the given component to get type of expression.
     *
     * @param expression - expression to get type from
     * @param instantiations - context
     * @param resolvedHandler - handler for resolved type
     */
    public void queryType(CsmOffsetable expression, List<CsmInstantiation> instantiations, ResolvedTypeHandler resolvedHandler) {
        QueryTypeTask task = new QueryTypeTask(resolvedHandler);
        performQueryTask(expression, instantiations, task);
    }

    static interface QueryTask {

        AntiloopClient getAntiloopKind();

        boolean isFindTypeTask();

        void perform(Context context);

    }

    /**
     * Perform the query on the given component to get type of expression.
     *
     * @param expression - expression to get type from
     * @param instantiations - context
     */
    private boolean performQueryTask(CsmOffsetable expression, List<CsmInstantiation> instantiations, QueryTask task) {
        if (enterThreadLocalAntiloop(AntiloopClient.query_type, expression)) {
            try {
                CsmCacheManager.enter();
                baseDocument = (BaseDocument) CsmUtilities.getDocument(expression.getContainingFile());
                if (baseDocument == null) {
                    CloneableEditorSupport support = CsmUtilities.findCloneableEditorSupport(expression.getContainingFile());
                    if (support == null) {
                        return false;
                    }
                    baseDocument = (BaseDocument) support.openDocument();
                }

                final int startOffset = expression.getStartOffset();
                final int endOffset = expression.getEndOffset();

//                if (!checkCondition(baseDocument, endOffset, true)) {
//                    return false;
//                }

                long docVersion = DocumentUtilities.getDocumentVersion(baseDocument);

        //        TokenProcessorCache property = (TokenProcessorCache) baseDocument.getProperty(TOKEN_PROCESSOR_CACHE_KEY);
        //        CsmCompletionTokenProcessor tp = null;
        //        if (property != null) {
        //            if (property.queryOffset == endOffset && property.docVersion == docVersion) {
        //                tp = property.tp;
        //            }
        //        }
                CsmFile csmFile = getCsmFile();
                if (csmFile == null) {
                    csmFile = CsmUtilities.getCsmFile(baseDocument, true, false);
                }

                final boolean processAsExpression = CsmKindUtilities.isExpression(expression);
                
                CsmCompletionTokenProcessor tp;

                CsmScope passedScope = null;

                if (processAsExpression) {
                    passedScope = ((CsmExpression) expression).getScope();
                    tp = processTokensInExpression((CsmExpression) expression, task.isFindTypeTask());
                } else {
                    tp = processTokensInFile(csmFile, startOffset, endOffset, baseDocument, docVersion);
                }

                if (!checkErrorTokenState(tp)) {
                    try {
                        pushResolveContext(CsmResolveContext.create(csmFile, startOffset));

                        CsmCompletionExpression exp = getResultExpression(tp, processAsExpression, task.isFindTypeTask());

                        Context ctx = getResolvedContext(
                                null,
                                baseDocument,
                                true,
                                endOffset,
                                exp,
                                instantiations,
                                false,
                                isInIncludeDirective(baseDocument, endOffset),
                                true,
                                task.isFindTypeTask(),
                                passedScope
                        );

                        task.perform(ctx);

                        if (TRACE_COMPLETION) {
                            System.err.println("expression " + exp);
                        }

                        return true;

                    } finally {
                        popResolveContext();
                    }
                } else if (TRACE_COMPLETION) {
                    System.err.println("Error expression " + tp.getResultExp());
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                exitThreadLocalAntiloop(AntiloopClient.query_type, expression);
                CsmCacheManager.leave();
            }
        }
        return false;
    }

    public static boolean checkCondition(final Document doc, final int dot, boolean takeLock) {
        if (!takeLock) {
            return _checkCondition(doc, dot);
        }
        final AtomicBoolean res = new AtomicBoolean(false);
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).render(new Runnable() {
                @Override
                public void run() {
                    res.set(_checkCondition(doc, dot));
                }
            });
        } else {
            res.set(_checkCondition(doc, dot));
        }
        return res.get();
    }

    public static boolean checkCondition(final Document doc, final int dot, boolean takeLock, final int queryType) {
        if (!takeLock) {
            return _checkCondition(doc, dot, queryType);
        }
        final AtomicBoolean res = new AtomicBoolean(false);
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).render(new Runnable() {
                @Override
                public void run() {
                    res.set(_checkCondition(doc, dot, queryType));
                }
            });
        } else {
            res.set(_checkCondition(doc, dot, queryType));
        }
        return res.get();
    }

    private static boolean _checkCondition(Document doc, int dot) {
        return !CompletionSupport.isPreprocCompletionEnabled(doc, dot) && CompletionSupport.isCompletionEnabled(doc, dot);
    }

    private static boolean _checkCondition(Document doc, int dot, int queryType) {
        return !CompletionSupport.isPreprocCompletionEnabled(doc, dot) && CompletionSupport.isCompletionEnabled(doc, dot, queryType);
    }

//    private boolean parseExpression(CsmCompletionTokenProcessor tp, TokenSequence<?> cppTokenSequence, int startOffset, int lastOffset) {
//        boolean processedToken = false;
//        while (cppTokenSequence.moveNext()) {
//            if (cppTokenSequence.offset() >= lastOffset) {
//                break;
//            }
//            Token<CppTokenId> token = (Token<CppTokenId>) cppTokenSequence.token();
//            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
//                TokenSequence<?> embedded = cppTokenSequence.embedded();
//                if (cppTokenSequence.offset() < startOffset) {
//                    embedded.move(startOffset);
//                }
//                processedToken |= parseExpression(tp, embedded, startOffset, lastOffset);
//            } else {
//                processedToken = true;
//                tp.token(token, cppTokenSequence.offset());
//            }
//        }
//        return processedToken;
//    }
    private final static String TOKEN_PROCESSOR_CACHE_KEY = "TokenProcessorCache"; // NOI18N
    private static final class TokenProcessorCache {
        private final int queryOffset;
        private final long docVersion;
        private final CsmCompletionTokenProcessor tp;

        public TokenProcessorCache(int queryOffset, long docVersion, CsmCompletionTokenProcessor tp) {
            this.queryOffset = queryOffset;
            this.docVersion = docVersion;
            this.tp = tp;
        }
    }

    public CsmCompletionResult query(JTextComponent component, final BaseDocument doc, final int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes) {
        return query(component, doc, offset, openingSource, sort, instantiateTypes, false);
    }

    public CsmCompletionResult query(JTextComponent component, final BaseDocument doc, final int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes, boolean tooltip) {
        try {
            CsmCacheManager.enter();
            return queryImpl(component, doc, offset, openingSource, sort, instantiateTypes, tooltip);
        } finally {
            CsmCacheManager.leave();
        }
    }

    private CsmCompletionResult queryImpl(JTextComponent component, final BaseDocument doc, final int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes, boolean tooltip) {
        // remember baseDocument here. it is accessible by getBaseDocument() {

        // method for subclasses of JavaCompletionQuery, ie. NbJavaCompletionQuery
        baseDocument = doc;

        CsmCompletionResult ret = null;

        CompletionSupport sup = CompletionSupport.get(doc);
        if (sup == null || (!checkCondition(doc, offset, true) && !tooltip)) {
            return null;
        }

        try {
            TokenProcessorCache property = (TokenProcessorCache) baseDocument.getProperty(TOKEN_PROCESSOR_CACHE_KEY);
            long docVersion = DocumentUtilities.getDocumentVersion(doc);
            CsmCompletionTokenProcessor tp = null;
            if (property != null) {
                if (property.queryOffset == offset && property.docVersion == docVersion) {
                    tp = property.tp;
                }
            }
            CsmFile csmFile = getCsmFile();
            if (csmFile == null) {
                csmFile = CsmUtilities.getCsmFile(doc, true, false);
            }
            if (tp == null) {
                // find last separator position
                final int lastSepOffset = sup.getLastCommandSeparator(csmFile, offset, getFileReferencesContext());
                tp = processTokensInFile(csmFile, lastSepOffset, offset, doc, docVersion);
                baseDocument.putProperty(TOKEN_PROCESSOR_CACHE_KEY, new TokenProcessorCache(offset, docVersion, tp));
            } else {
                // hit
            }
            sup.setLastSeparatorOffset(tp.getLastSeparatorOffset());
//            boolean cont = true;
//            while (cont) {
//                sup.tokenizeText(tp, ((lastSepOffset < offset) ? lastSepOffset + 1 : offset), offset, true);
//                cont = tp.isStopped() && (lastSepOffset = sup.findMatchingBlock(tp.getCurrentOffest(), true)[0]) < offset - 1;
//            }

            if (!checkErrorTokenState(tp)) {
                try {
                    pushResolveContext(CsmResolveContext.create(csmFile, offset));

                    CsmCompletionExpression exp = null;
                    if(!tooltip) {
                        exp = tp.getResultExp();
                        ret = getResult(component, doc, openingSource, offset, exp, sort, isInIncludeDirective(doc, offset), instantiateTypes);
                    } else {
                        List<CsmCompletionExpression> stack = tp.getStack();
                        for (int i = stack.size() - 1; i >= 0; i--) {
                            CsmCompletionExpression e = stack.get(i);
                            if(e.getExpID() == CsmCompletionExpression.METHOD_OPEN) {
                                exp = e;
                                break;
                            } else if(e.getExpID() == CsmCompletionExpression.SCOPE) {
                                if(e.getParameterCount() > 1 &&
                                        e.getParameter(e.getParameterCount() - 1).getExpID() == CsmCompletionExpression.METHOD_OPEN) {
                                    exp = e;
                                    break;
                                }
                            }
                        }
                        exp = (exp != null) ? exp : tp.getResultExp();
                        ret = getResult(component, doc, openingSource, offset, exp, sort, isInIncludeDirective(doc, offset), instantiateTypes);
                        if(ret == null && exp != null && exp.getParameterCount() >= 1 && exp.getParameter(0).getExpID() == CsmCompletionExpression.VARIABLE) {
                            ret = getResult(component, doc, openingSource, offset, exp.getParameter(0), sort, isInIncludeDirective(doc, offset), instantiateTypes);
                            if(ret != null && !ret.getItems().isEmpty()) {
                                if(ret.getItems().get(0) instanceof CsmResultItem.VariableResultItem) {
                                    VariableResultItem item = (CsmResultItem.VariableResultItem)ret.getItems().get(0);
                                    if(item.getAssociatedObject() instanceof CsmObject && CsmKindUtilities.isVariable((CsmObject)item.getAssociatedObject())) {
                                        CsmVariable var = (CsmVariable)item.getAssociatedObject();
                                        if(var.getType() != null) {
                                            CsmClassifier cls = var.getType().getClassifier();
                                            cls = CsmBaseUtilities.getOriginalClassifier(cls, getFinder().getCsmFile());
                                            if(CsmKindUtilities.isClass(cls)) {
                                                List<CsmMember> items = new ArrayList<CsmMember>();
                                                for (CsmMember member : ((CsmClass)cls).getMembers()) {
                                                    if(CsmKindUtilities.isConstructor(member)) {
                                                        items.add(member);
                                                    }
                                                }
                                                if(!items.isEmpty()) {
                                                    CsmOffsetableDeclaration context = sup.getDefinition(csmFile, offset, getFileReferencesContext());
                                                    ret = new CsmCompletionResult(component, doc, items, cls.getName().toString(), exp, offset, 0, 0, isProjectBeeingParsed(openingSource), context, instantiateTypes);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (TRACE_COMPLETION) {
                        System.err.println("expression " + exp);
                    }
                } finally {
                    popResolveContext();
                }
            } else if (TRACE_COMPLETION) {
                System.err.println("Error expression " + tp.getResultExp());
            }
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }

        return ret;
    }

    private boolean enterThreadLocalAntiloop(AntiloopClient client, CsmOffsetable expression) {
        Map<AntiloopClient, Set<CsmOffsetable>> antiloopMap = threadLocalAntiloopMap.get();
        Set<CsmOffsetable> antiloopSet = antiloopMap.get(client);

        if (antiloopSet == null) {
            antiloopSet = new HashSet<CsmOffsetable>(4);
            antiloopMap.put(client, antiloopSet);
        }

        if (!antiloopSet.contains(expression)) {
            antiloopSet.add(expression);
            return true;
        }
        return false;
    }

    private void exitThreadLocalAntiloop(AntiloopClient client, CsmOffsetable expression) {
        Map<AntiloopClient, Set<CsmOffsetable>> antiloopMap = threadLocalAntiloopMap.get();
        Set<CsmOffsetable> antiloopSet = antiloopMap.get(client);

        assert antiloopSet != null : "Must be set in enter method!"; //NOI18N

        antiloopSet.remove(expression);
    }

    private void pushResolveContext(CsmResolveContext context) {
        Stack<CsmResolveContext> contexts = (Stack<CsmResolveContext>) CsmCacheManager.get(CsmResolveContext.class);
        if (contexts == null) {
            contexts = new Stack<CsmResolveContext>();
            CsmCacheManager.put(CsmResolveContext.class, contexts);
        }
        contexts.push(context);
    }

    private CsmResolveContext popResolveContext() {
        Stack<CsmResolveContext> contexts = (Stack<CsmResolveContext>) CsmCacheManager.get(CsmResolveContext.class);
        if (contexts != null && !contexts.isEmpty()) {
            return contexts.pop();
        }
        return null;
    }

    private boolean checkErrorTokenState(CsmCompletionTokenProcessor tp) {
        // Check whether there's an erroneous token state under the cursor
        boolean errState = false;
        CppTokenId lastValidTokenID = tp.getLastValidTokenID();
        if (lastValidTokenID != null) {
            switch (lastValidTokenID) {
//            case STAR:
//                errState = true;
//                break;
                case BLOCK_COMMENT:
                    if (tp.getLastValidTokenText() == null || !tp.getLastValidTokenText().endsWith("*/") // NOI18N
                            ) {
                        errState = true;
                    }
                    break;

                case LINE_COMMENT:
                case DOXYGEN_LINE_COMMENT:
                    errState = true;
                    break;
                default:
                    if (CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(lastValidTokenID.primaryCategory())) {
                        // this provider doesn't handle preprocessor tokens
                        errState = true;
                    } else {
                        errState = tp.isErrorState();
                    }
            }
        }
        if (!errState && tp.getResultExp() == null) {
            errState = true;
        }
        return errState;
    }

    private CsmCompletionTokenProcessor processTokensInFile(CsmFile csmFile, final int startOffset, final int endOffset, final BaseDocument doc, final long docVersion) {
        CsmCompletionTokenProcessor tp = new CsmCompletionTokenProcessor(endOffset, startOffset);
        final CndTokenProcessor<Token<TokenId>> etp = CsmExpandedTokenProcessor.create(csmFile, doc, tp, endOffset);
        if(etp instanceof CsmExpandedTokenProcessor) {
            tp.setMacroCallback((CsmExpandedTokenProcessor)etp);
        }
        boolean isCpp11OrLater = CsmFileInfoQuery.getDefault().isCpp11OrLater(csmFile);
        tp.enableLambdaSupport(isCpp11OrLater);
        tp.enableUserDefinedLiteralsSupport(isCpp11OrLater);
        tp.enableUniformInitializationSupport(isCpp11OrLater);
        tp.enableTemplateSupport(areTemplatesEnabled(csmFile));
        doc.render(new Runnable() {
            @Override
            public void run() {
                CndTokenUtilities.processTokens(etp, doc, startOffset, endOffset);
            }
        });
        return tp;
    }

    private CsmCompletionTokenProcessor processTokensInExpression(CsmExpression expression, boolean keepWholeAst) {
        // TODO: alter token processor to handle situations when we need to
        // keep ast for the whole expression. For now expression is placed
        // inside parens: (<expression>)
        String expressionText = keepWholeAst ?
                CppTokenId.LPAREN.fixedText() + expression.getExpandedText().toString() + CppTokenId.RPAREN.fixedText() :
                expression.getExpandedText().toString();

        int exprStartOffset = expression.getStartOffset() - (keepWholeAst ? 1 : 0);
        int exprEndOffset = exprStartOffset + expressionText.length() + (keepWholeAst ? 1 : 0);

        final CsmCompletionTokenProcessor tp = new CsmCompletionTokenProcessor(exprEndOffset, exprStartOffset);
        
        Language<CppTokenId> language = CndLexerUtilities.getLanguage(getBaseDocument());
        if (language == null) {
            CsmFile csmFile = getCsmFile();
            if (csmFile != null) {
                Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> fileLanguageFlavor = CsmFileInfoQuery.getDefault().getFileLanguageFlavor(csmFile);
                switch (fileLanguageFlavor.first()) {
                    case C_HEADER:
                        language = CppTokenId.languageHeader();
                        break;
                    case CPP:
                        language = CppTokenId.languageCpp();
                        break;
                    case C:
                        language = CppTokenId.languageC();
                        break;
                }
            }
        }
        
        if (language != null) {
            TokenHierarchy<String> hi = TokenHierarchy.create(expressionText,
                false, 
                language,
                null,
                (InputAttributes) getBaseDocument().getProperty(InputAttributes.class)
            );
            List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(exprEndOffset - exprStartOffset, true);
            // Go from inner to outer TSes
            TokenSequence<TokenId> cppts = null;
            for (int i = tsList.size() - 1; i >= 0; i--) {
                TokenSequence<?> ts = tsList.get(i);
                final Language<?> lang = ts.languagePath().innerLanguage();
                if (CndLexerUtilities.isCppLanguage(lang, false)) {
                    @SuppressWarnings("unchecked") // NOI18N
                    TokenSequence<TokenId> uts = (TokenSequence<TokenId>) ts;
                    cppts = uts;
                }
            }
            if (cppts != null) {
                final TokenSequence<TokenId> cppTokenSequence = cppts;
                CsmFile csmFile = expression.getContainingFile();
                boolean isCpp11OrLater = CsmFileInfoQuery.getDefault().isCpp11OrLater(csmFile);
                tp.enableLambdaSupport(isCpp11OrLater);
                tp.enableUserDefinedLiteralsSupport(isCpp11OrLater);
                tp.enableUniformInitializationSupport(isCpp11OrLater);
                tp.enableTemplateSupport(areTemplatesEnabled(csmFile));
                CndTokenUtilities.processTokens(tp, cppTokenSequence, exprStartOffset, exprEndOffset, exprStartOffset);
            }
        }

        return tp;
    }
    
    private CsmCompletionExpression getResultExpression(CsmCompletionTokenProcessor tp, boolean processedAsExpression, boolean keepWholeAst) {
        if (processedAsExpression && keepWholeAst) {
            // See implementation of processTokensInExpression
            CsmCompletionExpression resultExp = tp.getResultExp();
            if (resultExp != null && resultExp.getParameterCount() == 1 && resultExp.getExpID() == CsmCompletionExpression.PARENTHESIS) {
                return resultExp.getParameter(0);
            }
        }
        return tp.getResultExp();
    }

    abstract protected boolean isProjectBeeingParsed(boolean openingSource);

    private CsmCompletionResult getResult(JTextComponent component, Document doc, boolean openingSource, int offset, CsmCompletionExpression exp, boolean sort, boolean inIncludeDirective, boolean instantiateTypes) {
        Context ctx = getResolvedContext(component, doc, openingSource, offset, exp, null, sort, inIncludeDirective, instantiateTypes, false, null);
        if (ctx != null) {
            return ctx.result;
        } else {
            boolean isProjectBeeingParsed = isProjectBeeingParsed(openingSource);
            return new CsmCompletionResult(component, getBaseDocument(), Collections.EMPTY_LIST, "", exp, 0, isProjectBeeingParsed, null, instantiateTypes);
        }
    }

    private Context getResolvedContext(JTextComponent component,
                                       Document doc,
                                       boolean openingSource,
                                       int offset,
                                       CsmCompletionExpression exp,
                                       List<CsmInstantiation> instantiations,
                                       boolean sort,
                                       boolean inIncludeDirective,
                                       boolean instantiateTypes,
                                       boolean findType,
                                       CsmScope contextScope
    ) {
        CompletionResolver resolver = getCompletionResolver(contextScope, openingSource, sort, inIncludeDirective);
        if (resolver != null) {
            if (CsmKindUtilities.isOffsetable(contextScope)) {
                CsmOffsetable offsetableScope = (CsmOffsetable) contextScope;
                if (offsetableScope.getEndOffset() < offset) {
                    ((CompletionResolverImpl) resolver).setContextOffset(
                            (offsetableScope.getStartOffset() + offsetableScope.getEndOffset()) / 2
                    );
                }
            }
            CompletionSupport sup = CompletionSupport.get(doc);
            if (sup == null) {
                Logger.getLogger(CsmCompletion.class.getSimpleName()).log(Level.INFO, "Null CompletionSupport for  {0}", doc); //NOI18N
                return null;
            }
            CsmOffsetableDeclaration context = sup.getDefinition(contextScope);
            if (context == null) {
                context = sup.getDefinition(getCsmFile(), offset, getFileReferencesContext());
            }
            if (!openingSource && context == null) {
                instantiateTypes = false;
            }

            Context ctx = new Context(component, sup, openingSource, offset, getFinder(), resolver, context, contextScope, sort, instantiateTypes, instantiations);
            ctx.setFindType(findType);

            ctx.resolveExp(exp, true);

            if (ctx.result != null) {
                ctx.result.setSimpleVariableExpression(isSimpleVariableExpression(exp));
            }

            if (TRACE_COMPLETION) {
                CompletionItem[] array = ctx.result == null ? new CompletionItem[0] : ctx.result.getItems().toArray(new CompletionItem[ctx.result.getItems().size()]);
                //Arrays.sort(array, CompletionItemComparator.BY_PRIORITY);
                System.err.println("Completion Items " + array.length);
                for (int i = 0; i < array.length; i++) {
                    CompletionItem completionItem = array[i];
                    System.err.println(completionItem.toString());
                }
            }
            return ctx;
        }
        return null;
    }

    // ================= help methods to generate CsmCompletionResult ==========
    private static String formatType(CsmType type, boolean useFullName,
            boolean appendDblComma, boolean appendStar) {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            CsmClassifier classifier = type.getClassifier();
            if (classifier != null) {
                sb.append(useFullName ? classifier.getQualifiedName() : classifier.getName());
            }
        }
        if (appendDblComma) {
            sb.append(CsmCompletion.SCOPE);
        }
        if (appendStar) {
            sb.append('*'); //NOI18N
        }
        return sb.toString();
    }

// commented out: isn't used any more (except for commented out code fragments)
//    private static String getNamespaceName(CsmClassifier classifier) {
//        CsmNamespace ns = null;
//        if (CsmKindUtilities.isClass(classifier)) {
//            ns = ((CsmClass)classifier).getContainingNamespace();
//        }
//        return ns != null ? ns.getQualifiedName() : ""; //NOI18N
//    }
    /** Finds the fields, methods and the inner classes.
     */
//    static List findFieldsAndMethods(JCFinder finder, String curPkg, CsmClass cls, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
    // [TODO]
//        if (staticOnly) {
//            JCPackage pkg = finder.getExactPackage(cls.getPackageName());
//            if (pkg != null) {
//                ret = finder.findClasses(pkg, cls.getName() + '.' + name, false);
//            }
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
//        if (finder instanceof JCBaseFinder) {
//            // Add fields
//            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
//            // Add methods
//            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
//        } else {
//            // Add fields
//            ret.addAll(finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses));
//            // Add methods
//            ret.addAll(finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses));
//        }
//
//        return ret;
//    }
    /** Finds the fields, methods and the inner classes.
     */
//    static List findFieldsAndMethods(CsmFinder finder, String curNamespace, CsmClassifier classifier, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
//        if (!CsmKindUtilities.isClass(classifier)) {
//            return ret;
//        }
//        CsmClass cls = (CsmClass)classifier;
//        if (staticOnly) {
////            CsmNamespace pkg = finder.getExactNamespace(getNamespaceName(cls));
//            CsmNamespace ns = cls.getContainingNamespace();
//            if (ns != null) {
//                ret = finder.findClasses(ns, cls.getName() + '.' + name, false);
//            }
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
////        if (finder instanceof JCBaseFinder) {
////            // Add fields
////            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////            // Add methods
////            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////        } else {
//            // Add fields
//            List res = finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
//            // Add methods
//            res = finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
////        }
//
//        return ret;
//    }
    static List<CsmClassifier> findNestedClassifiers(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
            boolean exactMatch, boolean inspectParentClasses, boolean sort) {
        // Find inner classes
        List<CsmClassifier> ret = new ArrayList<CsmClassifier>();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, finder.getCsmFile());
        if (!CsmKindUtilities.isClass(classifier)) {
            return ret;
        }
        CsmClass cls = (CsmClass) classifier;

        // Add fields
        List<CsmClassifier> res = finder.findNestedClassifiers(context, cls, name, exactMatch, inspectParentClasses, sort);
        if (res != null) {
            ret.addAll(res);
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    static List<CsmObject> findFieldsAndMethods(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
            boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean skipConstructors, boolean sort) {
        // Find inner classes
        List ret = new ArrayList<CsmObject>();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, finder.getCsmFile());
        if (CsmKindUtilities.isClass(classifier)) {
            CsmClass cls = (CsmClass) classifier;
            CsmFunction contextFunction = CsmBaseUtilities.getContextFunction(context);
            CsmClass contextClass = CsmBaseUtilities.getContextClass(context);
    //        if (staticOnly) {
    ////            CsmNamespace pkg = finder.getExactNamespace(getNamespaceName(cls));
    //            CsmNamespace ns = cls.getContainingNamespace();
    //            if (ns != null) {
    //                ret = finder.findClasses(ns, cls.getName() + '.' + name, false);
    //            }
    //        }
            if (CsmInheritanceUtilities.isAssignableFrom(contextClass, cls)) {
                staticOnly = false;
            }
            // Add fields
            List<?> res = finder.findFields(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
            if (res != null) {
                ret.addAll(res);
            }
            // add enumerators
            res = finder.findEnumerators(context, cls, name, exactMatch, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
            if (res != null) {
                ret.addAll(res);
            }

            // in global context add all methods, but only direct ones
            if (contextFunction == null && contextClass == null) {
                staticOnly = false;
                context = cls;
            }
            // Add methods
            res = finder.findMethods(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
            if (res != null) {
                if (!skipConstructors) {
                    ret.addAll(res);
                } else {
                    // add all but skip constructors
                    for (Object mtd : res) {
                        if (!CsmKindUtilities.isConstructor(((CsmObject) mtd))) {
                            ret.add(mtd);
                        }
                    }
                }
            }
            return ret;
        } else if(CsmKindUtilities.isEnum(classifier)) {
            for (CsmEnumerator enumerator : ((CsmEnum) classifier).getEnumerators()) {
                if (CsmSortUtilities.matchName(enumerator.getName(), name, exactMatch, exactMatch)) {
                    ret.add(enumerator);
                }
            }
            return ret;
        }
        return ret;
    }

    /** Finds the fields, methods and the inner classes.
     */
//    static List findFields(CsmFinder finder, CsmContext context, CsmClassifier classifier, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
//        CsmClass cls = null;
//        if (CsmKindUtilities.isClass(classifier)) {
//            cls = (CsmClass)classifier;
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
////        if (finder instanceof JCBaseFinder) {
////            // Add fields
////            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////            // Add methods
////            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////        } else {
//            // Add fields
//            List res = finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
//            // Add methods
//            res = finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
////        }
//
//        return ret;
//    }
    static enum ExprKind {

        NONE, SCOPE, ARROW, DOT
    }

    private static CsmClassifier getClassifier(CsmType type, CsmScope contextScope, CsmFile contextFile, int offset) {
//        if (type instanceof CsmCompletion.BaseType || type instanceof CsmCompletion.OffsetableType) {
//            new Exception(type.getClass().getName() + type).printStackTrace();
//        }
//        boolean resolveTypeChain = true;
        CsmClassifier cls = CsmBaseUtilities.getClassifier(type, contextScope, contextFile, offset, true);
        return cls;
    }

    private static CsmFunction getOperator(CsmClassifier classifier, CsmFile contextFile, int offset, CsmFunction.OperatorKind opKind) {
        if (!CsmKindUtilities.isClass(classifier)) {
            return null;
        }
        CsmClass cls = (CsmClass) classifier;
        CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter("operator " + opKind.getImage(), false, true, false); // NOI18N
        return getOperatorCheckBaseClasses(cls, contextFile, offset, filter, opKind, new ClassifiersAntiLoop());
    }

    private static CsmFunction getOperatorCheckBaseClasses(CsmClass cls, CsmFile contextFile, int offset, CsmFilter filter, CsmFunction.OperatorKind opKind, ClassifiersAntiLoop antiLoop) {
        if (antiLoop.contains(cls)) {
            return null;
        }
        antiLoop.add(cls);
        Iterator<CsmMember> it = CsmSelect.getClassMembers(cls, filter);
        while (it.hasNext()) {
            CsmMember member = it.next();
            if (CsmKindUtilities.isOperator(member)) {
                if (((CsmFunction) member).getOperatorKind() == opKind) {
                    return (CsmFunction) member;
                }
            }
        }
        // now check base classes as well
        for (CsmInheritance csmInheritance : cls.getBaseClasses()) {
            CsmClassifier baseClassifier = getClassifier(csmInheritance.getAncestorType(), null, contextFile, offset); // TODO: think about contextScope
            if (CsmKindUtilities.isClass(baseClassifier)) {
                CsmFunction operatorFun = getOperatorCheckBaseClasses((CsmClass) baseClassifier, contextFile, offset, filter, opKind, antiLoop);
                if (operatorFun != null) {
                    return operatorFun;
                }
            }
        }
        return null;
    }

    private static CsmType getOverloadedOperatorReturnType(CsmType type, CsmFile contextFile, int offset, CsmFunction.OperatorKind operator, int level) {
        // Note: in case of performance issues merge isPointer() and getClassifier() calls into one
        if (type == null || type.getArrayDepth() > 0 || isPointer(type)) {
            return null;
        }
        CsmType opType = null;
        CsmClassifier cls = getClassifier(type, null, contextFile, offset); // TODO: think about contextScope
        if (CsmKindUtilities.isClass(cls)) {
            CsmFunction op = CsmCompletionQuery.getOperator((CsmClass) cls, contextFile, offset, operator);
            if (op != null) {
                opType = op.getReturnType();
                if ((!type.equals(opType)) && (level > 0)) {
                    if (operator == CsmFunction.OperatorKind.ARROW) {
                        // recursion only for ->
                        CsmType opType2 = getOverloadedOperatorReturnType(opType, contextFile, offset, operator, level - 1);
                        if (opType2 != null) {
                            opType = opType2;
                        }
                    }
                } else {
                    CsmFile typeFile = type.getContainingFile();
                    System.err.printf("circular pointer delegation detected:%s, line %d/n", (typeFile != null ? typeFile.getAbsolutePath() : type), type.getStartOffset());//NOI18N
                    CndUtils.assertTrueInConsole(false, "Infinite recursion in file " + typeFile + " type " + type); //NOI18N
                }
            }
        }
        return opType;
    }
    
    private static boolean isOverloadableOperator(CppTokenId opId) {
        // TODO: must depend on language and flavor!
        switch (opId) {
            case QUESTION:
            case DOT:
            case DOTMBR:
            case SCOPE:
            case SIZEOF:
            case TYPEID:
                return false;
        }
        return true;
    }

    private boolean isInIncludeDirective(BaseDocument doc, int offset) {
        if (true) {
            return false;
        }
        if (doc == null) {
            return false;
        }
        TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, false, false);
        if (cppTokenSequence == null) {
            return false;
        }
        boolean inIncludeDirective = false;
        if (cppTokenSequence.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<TokenId> embedded = (TokenSequence<TokenId>)cppTokenSequence.embedded();
            if (CndTokenUtilities.moveToPreprocKeyword(embedded)) {
                final TokenId id = embedded.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                            inIncludeDirective = true;
                    }
                }
            }
        }
        return inIncludeDirective;
    }

    class Context {

        private boolean sort;
        /** Text component */
        private JTextComponent component;
        /**
         * Syntax support for the given baseDocument
         */
        private CompletionSupport sup;
        /** Whether the query is performed to open the source file. It has slightly
         * different handling in some situations.
         */
        private boolean openingSource;
        /** End position of the scanning - usually the caret position */
        private int endOffset;
        /** If set to true true - find the type of the result expression.
         * It's stored in the lastType variable or lastNamespace if it's a namespace.
         * The result variable is not populated.
         * False means that the code completion output should be collected.
         */
        private boolean findType;
        /** Whether currently scanning either the package or the class name
         * so the results should limit the search to the static fields and methods.
         */
        private boolean staticOnly = false;
        private boolean memberPointer = false;
        /**
         * stores information where there is class or variable was resolved
         */
        private boolean scopeAccessedClassifier = false;
        /** Last package found when scanning dot expression */
        private CsmNamespace lastNamespace;
        /** Last type found when scanning dot expression */
        private CsmType lastType;
        /** Result list when code completion output is generated */
        private CsmCompletionResult result;
        /** Helper flag for recognizing constructors */
        private boolean isConstructor;
        /** Finder associated with this Context. */
        /** Finder associated with this Context. */
        private final CsmFinder finder;
        private final CsmFile contextFile;
        /** Array of two scopes - first is passed scope, second is the previous lastType or lastNamespace*/
        private final CsmScope contextScopes[]; 
        /** Completion resolver associated with this Context. */
        private CompletionResolver compResolver;
        /** function or class in context */
        private CsmOffsetableDeclaration contextElement;
        private final boolean instantiateTypes;
        /** Top level context (always null if this is first [top level] query) */
        private List<CsmInstantiation> instantiations;

        public Context(JTextComponent component,
                CompletionSupport sup, boolean openingSource, int endOffset,
                CsmFinder finder,
                CompletionResolver compResolver, CsmOffsetableDeclaration contextElement, CsmScope contextScope,
                boolean sort, boolean instantiateTypes,
                List<CsmInstantiation> instantiations) {
            this.component = component;
            this.sup = sup;
            this.openingSource = openingSource;
            this.endOffset = endOffset;
            this.finder = finder;
            this.contextFile = finder == null ? null : finder.getCsmFile();
            this.compResolver = compResolver;
            this.contextElement = contextElement;
            this.sort = sort;
            this.instantiateTypes = instantiateTypes;
            this.instantiations = instantiations;
            this.contextScopes = new CsmScope[]{contextScope, contextScope};
        }

        CsmFile getContextFile() {
            return contextFile;
        }

        CsmScope getLexicalContextScope() {
            // is not null when scope cannot be calculated from offset
            // (when resolving was started from macros)
            return contextScopes[0];
        }

        CsmOffsetableDeclaration getContextElement() {
            return contextElement;
        }

        List<CsmInstantiation> getContextInstantiations() {
            return instantiations;
        }

        int getEndOffset() {
            return endOffset;
        }

        private int convertOffset(int pos) {
            return sup.doc2context(pos);
        }
        
        private CsmScope getLogicalContextScope() {
            // this scope is changing while resolving qualified name:
            // AAA::BBB::CCC
            //           ^
            //        Here local scope is BBB.
            return contextScopes[1];
        }
        
        private void setLogicalContextScope(CsmScope scope) { 
            contextScopes[1] = scope;
        }

        /**
         *
         * @param res
         * @param visible [in] true/false if only visible objects should be considered
         *               [out] contains information if type is visible
         * @param item
         * @return
         */
        private CsmType getVariableOrClassifierType(Result res, AtomicBoolean visible, CsmCompletionExpression item) {
            CsmType resolveType = null;
            CsmObject resolveObj = null;
            List<? extends CsmObject> vars = new ArrayList<CsmObject>();
            res.addResulItemsToCol(vars);
            CsmObject bestCandidate = null;
            CsmIncludeResolver ir = CsmIncludeResolver.getDefault();
            boolean onlyVisible = visible.get();
            // type is not visible until check
            visible.set(false);
            boolean _const = checkQualifier(item, CppTokenId.CONST);
            List<CsmObject> visibleObject = new ArrayList<CsmObject>();
            List<CsmObject> td = new ArrayList<CsmObject>();
            AtomicBoolean hasClassifier = new AtomicBoolean(false);
            for (CsmObject firstElem : vars) {
                if (CsmKindUtilities.isClassifier(firstElem) || CsmKindUtilities.isVariable(firstElem)) {
                    if (ir.isObjectVisible(contextFile, firstElem)) {
                        if (!CsmKindUtilities.isClassifier(firstElem)) {
                            visibleObject.add(firstElem);
                            break;
                        } else {
                            fillVisibleListAndFilterTypedefs(firstElem, hasClassifier, visibleObject, td);
                        }
                    } else if (!onlyVisible && bestCandidate == null) {
                        bestCandidate = firstElem;
                    }
                }
            }
            if (!visibleObject.isEmpty()) {
                resolveObj = visibleObject.get(0);
//                resolveObj = createInstantiation(resolveObj);
                resolveType = getObjectType(resolveObj, _const);
                visible.set(true);
                // trace
                if (TRACE_MULTIPLE_VISIBE_OBJECTS) {
                    if (visibleObject.size() > 1) {
                        // we have several visible classifiers
                        System.err.printf("getVariableOrClassifierType: : we have several objects visible from %s [%d]%n", contextFile.getAbsolutePath(), endOffset); // NOI18N
                        int ind = 0;
                        for (CsmObject obj : visibleObject) {
                            System.err.printf("[%d] %s%n", ind++, obj); // NOI18N
                            System.err.flush(); // to prevent OOM when calls on System.err are interposed and flushed by System.err.println by defaul
                        }
                    }
                }
            }
            if (resolveType == null && bestCandidate != null) {
                resolveObj = bestCandidate;
                resolveType = getObjectType(resolveObj, _const);
            }

            return resolveType;
        }

        /*package*/ CsmType getObjectType(CsmObject obj, boolean _constIfClassifier) {
            CsmType objType = CsmCompletion.getObjectType(obj, _constIfClassifier);
            if (isAutoType(objType) || isDecltypeAutoType(objType)) {
                CsmType funAutoType = findAutoOrDecltypeAutoType(obj, null);
                if (funAutoType != null) {
                    objType = funAutoType;
                }
            }
            return objType;
        }
        
        private CsmType findAutoOrDecltypeAutoType(CsmFile file, int startOffset, MutableObject<Boolean> wasDeduced) {
            FileReferencesContext fileRefs = (file == contextFile) ? getFileReferencesContext() : null;
            CsmContext ctx = CsmOffsetResolver.findContext(file, startOffset, fileRefs);
            CsmObject contextObject = ctx.getLastObject();
            if (CsmKindUtilities.isType(contextObject)) {
                contextObject = ctx.getLastOwner();
            }
            return findAutoOrDecltypeAutoType(contextObject, wasDeduced);
        }
        
        private CsmType findAutoOrDecltypeAutoType(CsmObject contextObject, MutableObject<Boolean> wasDeduced) {
            if (wasDeduced != null) {
                wasDeduced.value = true;
            }
            if (CsmKindUtilities.isVariable(contextObject)) {
                CsmVariable contextVar = (CsmVariable) contextObject;
                if (isAutoType(contextVar.getType())) {
                    return findAutoVariableType(contextVar, contextVar.getType());
                } else if (isDecltypeAutoType(contextVar.getType())) {
                    return findExpressionType(contextVar.getInitialValue());
                } else {
                    if (wasDeduced != null) {
                        wasDeduced.value = false;
                    }
                    contextVar.getType();
                }
            } else if (CsmKindUtilities.isFunction(contextObject)) {
                CsmFunction contextFunc  = (CsmFunction) contextObject;
                if (isAutoType(contextFunc.getReturnType())) {
                    // C++14 auto function without trailing type specifier
                    CsmReturnStatement retStmt = CsmStatementResolver.findStatement(contextFunc.getDefinition().getBody(), CsmReturnStatement.class);
                    if (retStmt != null) {
                        return findExpressionType(retStmt.getReturnExpression());
                    }
                } else if (isDecltypeAutoType(contextFunc.getReturnType())) {
                    // C++14 decltype(auto) function 
                    CsmReturnStatement retStmt = CsmStatementResolver.findStatement(contextFunc.getDefinition().getBody(), CsmReturnStatement.class);
                    if (retStmt != null) {
                        return findExpressionType(retStmt.getReturnExpression());
                    }
                } else {
                    if (wasDeduced != null) {
                        wasDeduced.value = false;
                    }
                    return contextFunc.getReturnType();
                }
            }
            return null;
        }

        private CsmType findAutoVariableType(CsmVariable var, CsmType varType) {
            assert isAutoType(varType) : "var must have auto type!"; // NOI18N
            CsmType oldType = varType;
            final CsmExpression initialValue = var.getInitialValue();
            if (initialValue != null) {
                varType = findExpressionType(initialValue);
                if (varType != null) {
                    varType = CsmCompletion.createType(
                        varType.getClassifier(),
                        varType.getPointerDepth(),
                        getReferenceValue(oldType),
                        varType.getArrayDepth(),
                        oldType.isConst() | (varType.isPointer() & varType.isConst()),
                        oldType.isVolatile() | (varType.isPointer() & varType.isVolatile())
                    );
                }
            } else {
                if(CsmKindUtilities.isStatement(var.getScope()) ) {
                    if(((CsmStatement) var.getScope()).getKind().equals(CsmStatement.Kind.RANGE_FOR)) {
                        CsmRangeForStatement forStmt = (CsmRangeForStatement) var.getScope();
                        final CsmExpression initializer = forStmt.getInitializer();
                        if (initializer != null && initializer.getText() != null) {
                            varType = findExpressionType(initializer);
                            if (varType != null) {
                                if(varType.getArrayDepth() == 0) {
                                    // In fact this is a workaround, because according to standard we
                                    // must look for standalone functions begin(<expression>). But for now
                                    // NetBeans doesn't support functions like
                                    //
                                    // template<typename C> auto begin(C& c) -> decltype(c.begin());
                                    //
                                    // So, just try to find member function "begin"
                                    //
                                    // @see http://www.open-std.org/JTC1/SC22/WG21/docs/papers/2009/n2930.html

                                    CsmClassifier cls = CsmBaseUtilities.getOriginalClassifier(varType.getClassifier(), contextFile);
                                    List<CsmObject> decls = findFieldsAndMethods(finder, contextElement, cls, "begin", true, false, false, true, false, false, false); // NOI18N
                                    for (CsmObject csmObject : decls) {
                                        if(CsmKindUtilities.isFunction(csmObject)) {
                                            varType = ((CsmFunction)csmObject).getReturnType();
                                            break;
                                        }
                                    }

                                    cls = CsmBaseUtilities.getOriginalClassifier(varType.getClassifier(), contextFile);
                                    CsmFunction dereferenceOperator = getOperator(cls, contextFile, endOffset, CsmFunction.OperatorKind.POINTER);
                                    if (dereferenceOperator != null) {
                                        varType = dereferenceOperator.getReturnType();
                                    }

                                    varType = CsmCompletion.createType(
                                            varType.getClassifier(),
                                            varType.getPointerDepth(),
                                            getReferenceValue(oldType),
                                            varType.getArrayDepth(),
                                            oldType.isConst() | (varType.isPointer() & varType.isConst()),
                                            oldType.isVolatile() | (varType.isPointer() & varType.isVolatile())
                                    );
                                } else {
                                    // Iterations on array
                                    varType = CsmCompletion.createType(
                                            varType.getClassifier(),
                                            varType.getPointerDepth(),
                                            getReferenceValue(oldType),
                                            varType.getArrayDepth() - 1,
                                            oldType.isConst() | (varType.isPointer() & varType.isConst()),
                                            oldType.isVolatile() | (varType.isPointer() & varType.isVolatile())
                                    );
                                }
                            }
                        }
                    }
                }
            }
            return varType;
        }

        private CsmType findExpressionType(final CsmOffsetable expression) {
            if (expression != null && !antiLoop.contains(expression)) {
                antiLoop.add(expression);
                
                final boolean processAsExpression = CsmKindUtilities.isExpression(expression);
                CsmCompletionTokenProcessor tp;

                if (processAsExpression) {
                    tp = processTokensInExpression((CsmExpression) expression, true);
                } else {
                    long docVersion = DocumentUtilities.getDocumentVersion(getBaseDocument());
                    tp = processTokensInFile(expression.getContainingFile(), expression.getStartOffset(), expression.getEndOffset(), getBaseDocument(), docVersion);
                }

                if (!checkErrorTokenState(tp)) {
                    CsmCompletionExpression exp = getResultExpression(tp, processAsExpression, true);
                    return resolveType(exp);
                }
            }
            return null;
        }

        private void fillVisibleListAndFilterTypedefs(CsmObject elem, AtomicBoolean hasClassifier, List<CsmObject> visibleObjects, List<CsmObject> td) {
            if (CsmKindUtilities.isTypedef(elem) || CsmKindUtilities.isTypeAlias(elem)) {
                CsmTypedef curTd = (CsmTypedef) elem;
                CharSequence classifierText = curTd.getType().getClassifierText();
                if (curTd.getName().equals(classifierText)) {
                    if (!hasClassifier.get()) {
                        visibleObjects.add(elem);
                        td.add(curTd);
                    }
                } else {
                    visibleObjects.add(elem);
                }
            } else if (CsmKindUtilities.isEnumForwardDeclaration(elem) || CsmClassifierResolver.getDefault().isForwardEnum(elem)) {
                if (!hasClassifier.get()) {
                    visibleObjects.add(elem);
                    td.add(elem);
                }
            } else if (CsmKindUtilities.isClassForwardDeclaration(elem) || CsmClassifierResolver.getDefault().isForwardClass(elem)) {
                if (!hasClassifier.get()) {
                    visibleObjects.add(elem);
                    td.add(elem);
                }
            } else if (CsmKindUtilities.isClassifier(elem)) {
                if (!td.isEmpty()) {
                    // remove typedefs
                    visibleObjects.removeAll(td);
                    td.clear();
                }
                hasClassifier.set(true);
                visibleObjects.add(elem);
            } else {
                visibleObjects.add(elem);
            }
        }

        private boolean resolve(int varPos, String var, boolean match) {
            varPos = convertOffset(varPos);
            return (compResolver.refresh() && compResolver.resolve(varPos, var, match));
        }

        private void setFindType(boolean findType) {
            this.findType = findType;
        }

        @Override
        protected Object clone() {
            return new Context(component, sup, openingSource, endOffset, finder, compResolver, contextElement, contextScopes[0], sort, instantiateTypes, instantiations);
        }

        private CsmClassifier extractLastTypeClassifier(ExprKind expKind) {
            CsmClassifier lastTypeCls = extractTypeClassifier(lastType, getLogicalContextScope(), expKind);
            if (CsmKindUtilities.isScope(lastTypeCls)) {
                setLogicalContextScope((CsmScope) lastTypeCls);
            }
            return lastTypeCls;
        }

        private CsmClassifier extractTypeClassifier(CsmType type, CsmScope scope, ExprKind expKind) {
            if (type != null) {
                CsmClassifier cls;
                if (type.getArrayDepth() == 0 || (expKind == ExprKind.ARROW)) {
                    // Not array or deref array with arrow
                    cls = getClassifier(type, scope, contextFile, endOffset);
                } else {
                    // Array of some depth
                    cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
                }
                return cls;
            }
            return null;
        }

        private Collection<CsmFunction> getConstructors(CsmClass cls) {
            Collection<CsmFunction> out = new ArrayList<CsmFunction>();
            CsmFilterBuilder filterBuilder = CsmSelect.getFilterBuilder();
            CsmSelect.CsmFilter filter = filterBuilder.createCompoundFilter(CsmSelect.FUNCTION_KIND_FILTER,
                    filterBuilder.createNameFilter(cls.getName(), true, true, false));
            Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
            while (classMembers.hasNext()) {
                CsmMember csmMember = classMembers.next();
                if (CsmKindUtilities.isConstructor(csmMember)) {
                    out.add((CsmConstructor)csmMember);
                }
            }
            return out;
        }

        /*private CsmClassifier resolveTemplateParameter(CsmClassifier cls, CsmType type) {
        if (cls instanceof CsmClassifierBasedTemplateParameter) {
        CsmClassifierBasedTemplateParameter tp = (CsmClassifierBasedTemplateParameter) cls;
        String n = tp.getName().toString();
        CsmScope container = tp.getScope();
        if (CsmKindUtilities.isTemplate(container)) {
        CsmTemplate template = (CsmTemplate) container;
        List<CsmTemplateParameter> formal = template.getTemplateParameters();
        List<CsmType> fact = type.getInstantiationParams();
        for (int i = 0; i < fact.size() && i < formal.size(); i++) {
        CsmTemplateParameter formalParameter = formal.get(i);
        CsmType factParameter = fact.get(i);
        String name = formalParameter.getName().toString();
        if (name.equals(n)) {
        return factParameter.getClassifier();
        }
        }
        }
        }
        return cls;
        }*/

        Pair<Boolean, Context> resolveExp(CsmCompletionExpression exp, boolean first, ContextCloneStrategy cloneStrategy) {
            Context ctx = cloneStrategy.cloneContext(this);
            return Pair.of(ctx.resolveExp(exp, first), ctx);
        }

        CsmType resolveType(CsmCompletionExpression exp) {
            return resolveType(exp, true, new ContextCloneStrategy());
        }

        private CsmType resolveType(CsmCompletionExpression exp, boolean first, ContextCloneStrategy cloneStrategy) {
            CsmType typ = exp.getCachedType();
            if (typ == null) {
                Context ctx = cloneStrategy.cloneContext(this);
                ctx.setFindType(true);
                // when resolve type use full scope of search
                QueryScope old = ctx.compResolver.setResolveScope(QueryScope.GLOBAL_QUERY);
                try {
                    if (ctx.resolveExp(exp, first)) {
                        typ = ctx.lastType;
                    }
                } finally {
                    exp.cacheType(typ);
                    // restore old
                    ctx.compResolver.setResolveScope(old);
                }
            }
            return typ;
        }

        List<? extends CompletionItem> resolveObj(CsmCompletionExpression exp) {
            return resolveObj(exp, true, new ContextCloneStrategy());
        }

        private List<? extends CompletionItem> resolveObj(CsmCompletionExpression exp, boolean first, ContextCloneStrategy cloneStrategy) {
            Context ctx = cloneStrategy.cloneContext(this);
            ctx.setFindType(false);
            // when resolve type use full scope of search
            QueryScope old = ctx.compResolver.setResolveScope(QueryScope.GLOBAL_QUERY);
            try {
                ctx.resolveExp(exp, first);
                if (ctx.result != null && !ctx.result.getItems().isEmpty()) {
                    return ctx.result.getItems();
                }
            } finally {
                // restore old
                ctx.compResolver.setResolveScope(old);
            }
            return null;
        }

        private boolean isProjectBeeingParsed() {
            return CsmCompletionQuery.this.isProjectBeeingParsed(openingSource);
        }

        private ExprKind extractKind(CsmCompletionExpression exp, int i, int startIdx, boolean lastDot, boolean reset) {
            ExprKind kind = ExprKind.NONE;
            int tokCount = exp.getTokenCount();
            if (i == startIdx) {
                kind = ExprKind.NONE;
            } else if (i - 1 < tokCount) {
                switch (exp.getTokenID(i - 1)) {
                    case ARROW:
                        kind = ExprKind.ARROW;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case DOT:
                        kind = ExprKind.DOT;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case SCOPE:
                        kind = ExprKind.SCOPE;
                        if (reset) {
                            scopeAccessedClassifier = true;
                        }
                        break;
                    default:
                        System.err.println("unexpected token " + exp.getTokenID(i));
                }
            } else if (lastDot) {
                switch (exp.getExpID()) {
                    case CsmCompletionExpression.ARROW:
                    case CsmCompletionExpression.ARROW_OPEN:
                        kind = ExprKind.ARROW;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case CsmCompletionExpression.DOT:
                    case CsmCompletionExpression.DOT_OPEN:
                        kind = ExprKind.DOT;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case CsmCompletionExpression.SCOPE:
                    case CsmCompletionExpression.SCOPE_OPEN:
                        kind = ExprKind.SCOPE;
                        if (reset) {
                            scopeAccessedClassifier = true;
                        }
                        break;
                    default:
                        System.err.println("unexpected expression" + exp);
                }
            }
            return kind;
        }

        private boolean resolveParams(CsmCompletionExpression exp, boolean lastDot, /*out*/ ExprKind[] lastKind) {
            boolean ok = true;
            int parmCnt = exp.getParameterCount(); // Number of items in the dot exp
            // Fix for IZ#139143 : unresolved identifiers in "(*cur.object).*cur.creator"
            // Resolving should start after the last "->*" or ".*".
            int startIdx = 0;
            int tokCount = exp.getTokenCount();
            for (int i = tokCount - 1; 0 <= i; --i) {
                CppTokenId token = exp.getTokenID(i);
                if (token == CppTokenId.DOTMBR || token == CppTokenId.ARROWMBR) {
                    startIdx = i + 1;
                    break;
                }
            }
            ExprKind kind = ExprKind.NONE;
            ExprKind nextKind;
            int lastInd = parmCnt - 1;
            AtomicBoolean derefOfTHIS = new AtomicBoolean(false);
            for (int i = startIdx; i < parmCnt && ok; i++) { // resolve all items in exp
                kind = extractKind(exp, i, startIdx, lastDot, true);
                nextKind = extractKind(exp, i + 1, startIdx, lastDot, false);
                /*resolve arrows*/
                if ((kind == ExprKind.ARROW && !derefOfTHIS.get()) && (i != startIdx) && (i < parmCnt || lastDot || findType) && (lastType != null) && (lastType.getArrayDepth() == 0)) {
                    CsmType opType = getOverloadedOperatorReturnType(lastType, contextFile, endOffset, CsmFunction.OperatorKind.ARROW, MAX_DEPTH);
                    if (opType != null) {
                        lastType = opType;
                    }
                }
                derefOfTHIS.set(false);
                ok = resolveItem(exp.getParameter(i), (i == startIdx),
                        (!lastDot && i == lastInd),
                        kind, nextKind, derefOfTHIS);
            }
            if (ok && lastDot) {
                kind = extractKind(exp, tokCount + 1, startIdx, true, true);
                /*resolve arrows*/
                if ((kind == ExprKind.ARROW && !derefOfTHIS.get()) && (lastType != null) && (lastType.getArrayDepth() == 0)) {
                    CsmType opType = getOverloadedOperatorReturnType(lastType, contextFile, endOffset, CsmFunction.OperatorKind.ARROW, MAX_DEPTH);
                    if (opType != null) {
                        lastType = opType;
                    }
                }
            }
            lastKind[0] = kind;
            return ok;
        }

        @SuppressWarnings({"fallthrough", "unchecked"})
        boolean resolveExp(CsmCompletionExpression exp, boolean first) {
            boolean lastDot = false; // dot at the end of the whole expression?
            boolean ok = true;

             switch (exp.getExpID()) {
                case CsmCompletionExpression.DOT_OPEN: // Dot expression with the dot at the end
                case CsmCompletionExpression.ARROW_OPEN: // Arrow expression with the arrow at the end
                    lastDot = true;
                // let it flow to DOT
                // nobreak
                case CsmCompletionExpression.DOT: // Dot expression
                case CsmCompletionExpression.ARROW: // Arrow expression
                    ExprKind lastParamKind[] = new ExprKind[]{ExprKind.NONE};
                    ok = resolveParams(exp, lastDot, lastParamKind);

                    if (ok && lastDot) { // Found either type or package help
                        // Need to process dot at the end of the expression
                        int tokenCntM1 = exp.getTokenCount() - 1;
                        int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                        if (lastType != null) { // Found type
                            CsmClassifier cls = extractLastTypeClassifier(lastParamKind[0]);
                            List<CsmObject> res;
                            if (openingSource) {
                                res = new ArrayList<CsmObject>();
                                if (cls == null) {
                                    cls = lastType.getClassifier();
                                }
                                if (cls != null) {
                                    if (!CsmKindUtilities.isBuiltIn(cls)) {
                                        res.add(cls);
                                    }
                                }
                            } else { // not source-help
                                res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly && !memberPointer, false, true, this.scopeAccessedClassifier, true, sort); // NOI18N
                            }
                            CsmResultItem.SubstitutionHint hint = getSubstitutionHint(lastParamKind[0], lastType);
                            // Get all fields and methods of the cls
                            result = new CsmCompletionResult(component, getBaseDocument(), res, hint, formatType(lastType, true, true, true),
                                    exp, substPos, 0, 0/*cls.getName().length() + 1*/, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        } else { // Found namespace (otherwise ok would be false)
                            if (true) {
                                // in C++ it's not legal to have NS-> or NS.
                                result = null;
                                break;
                            }
                            String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getQualifiedName()) + CsmCompletion.SCOPE;
                            List res;
                            if (openingSource) {
                                res = new ArrayList<CsmObject>();
                                res.add(lastNamespace); // return only the package
                            } else {
                                res = finder.findNestedNamespaces(lastNamespace, "", false, false); // find all nested namespaces
                            }
                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*',
                                    exp, substPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        }
                    }
                    break;

                case CsmCompletionExpression.SCOPE_OPEN: // Scope expression with the arrow at the end
                    lastDot = true;
                // let it flow to SCOPE
                // nobreak
                case CsmCompletionExpression.SCOPE: // Scope expression
                    staticOnly = true;
                    lastParamKind = new ExprKind[]{ExprKind.NONE};
                    ok = resolveParams(exp, lastDot, lastParamKind);

                    if (ok && lastDot) { // Found either type or namespace help
                        // Need to process dot at the end of the expression
                        int tokenCntM1 = exp.getTokenCount() - 1;
                        int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                        if (lastType != null) { // Found type
                            CsmClassifier cls = extractLastTypeClassifier(ExprKind.SCOPE);
                            List res;
                            if (openingSource) {
                                res = new ArrayList();
                                if (cls == null) {
                                    cls = lastType.getClassifier();
                                }
                                if (cls != null) {
                                    if (!CsmKindUtilities.isBuiltIn(cls)) {
                                        res.add(cls);
                                    }
                                }
                            } else { // not source-help
//                            CsmClass curCls = sup.getClass(exp.getTokenOffset(tokenCntM1));
//                            res = findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls),
//                                    cls, "", false, staticOnly, false); // NOI18N
                                res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly && !memberPointer, false, true, this.scopeAccessedClassifier, false, sort); // NOI18N
                                List nestedClassifiers = findNestedClassifiers(finder, contextElement, cls, "", false, true, sort);
                                res.addAll(nestedClassifiers);
                            }
                            // Get all fields and methods of the cls
                            result = new CsmCompletionResult(component, getBaseDocument(), res, formatType(lastType, true, true, true),
                                    exp, substPos, 0, 0/*cls.getName().length() + 1*/, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        } else { // Found package (otherwise ok would be false)
                            String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getQualifiedName()) + CsmCompletion.SCOPE;
                            List res;
                            if (openingSource) {
                                res = new ArrayList();
                                res.add(lastNamespace); // return only the package
                            } else {
                                res = finder.findNestedNamespaces(lastNamespace, "", false, false); // find all nested namespaces

                                // if not "using namespace A::" or "namespace A = B::" then add elements
                                if (!isInNamespaceOnlyUsage(exp.getTokenOffset(0))) {
                                    // FIXME: review how can we do not ask for search in nested unnamed namespace?
                                    res.addAll(finder.findNamespaceElements(lastNamespace, "", false, false, false)); // namespace elements //NOI18N
                                }
                            }
                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*', //NOI18N
                                    exp, substPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        }
                    }
                    break;

                case CsmCompletionExpression.NEW: // 'new' keyword
                {
                    List<CsmClassifier> res = finder.findClasses(null, "", false, false); // Find all classes by name // NOI18N
                    result = new CsmCompletionResult(component, getBaseDocument(), res, "*", exp, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                    break;
                }

                case CsmCompletionExpression.LABEL: {
                    CsmCompletionExpression item = exp.getParameter(0);
                    String name = item.getTokenText(0);
                    List<CsmLabel> res = finder.findLabel(contextElement, name, false, false);
                    result = new CsmCompletionResult(component, getBaseDocument(), res, "*", exp, // NOI18N
                            name.isEmpty() ? endOffset : item.getTokenOffset(0),
                            name.isEmpty() ? 0 : item.getTokenLength(0),
                            0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                    break;
                }

                case CsmCompletionExpression.CASE:
                    // TODO: check with NbJavaJMICompletionQuery
                    // FIXUP: now just analyze expression after "case "
                    exp = exp.getParameter(0);
                // nobreak
                default: // The rest of the situations is resolved as a singleton item
                    AtomicBoolean derefOfTHIS = new AtomicBoolean(false);
                    ok = resolveItem(exp, first, true, ExprKind.NONE, ExprKind.NONE, derefOfTHIS);
                    break;
            }

            return ok;
        }

        private CsmType getPredefinedType(CsmCompletionExpression item) {
            CsmFile containingFile = getFinder().getCsmFile();
            int startOffset = item.getTokenOffset(0);
            int lastInd = item.getTokenCount() - 1;
            int endtOffset = item.getTokenOffset(lastInd) + item.getTokenLength(lastInd);
            return CsmCompletion.getPredefinedType(containingFile, startOffset, endtOffset, item);
        }
        /** Resolve one item from the expression connected by dots.
         * @param item expression item to resolve
         * @param first whether this expression is the first one in a dot expression
         * @param last whether this expression is the last one in a dot expression
         */
        @SuppressWarnings({"fallthrough", "unchecked"})
        boolean resolveItem(CsmCompletionExpression item, boolean first, boolean last, ExprKind kind, ExprKind nextKind, AtomicBoolean derefOfThisOUT) {
            boolean cont = true; // whether parsing should continue or not
            boolean methodOpen = false; // helper flag for unclosed methods
            boolean skipConstructors = (kind != ExprKind.NONE && kind != ExprKind.SCOPE);
            switch (item.getExpID()) {
                case CsmCompletionExpression.USER_DEFINED_LITERAL: // Constant item with ud-suffix
                    if (first) {
                        Collection<CsmFunction> mtdList = new LinkedHashSet<CsmFunction>();
                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                        //String operatorPrefix = "operator " + item.getTokenText(0);  // NOI18N
                        if (resolve(item.getTokenOffset(0), item.getTokenText(0), openingSource)) {
                            CompletionResolver.Result res = compResolver.getResult();
                            res.addResulItemsToCol(mtdList);
                        }
                        if (!mtdList.isEmpty()) {
                            List<CsmType> typeList = getTypeList(item, 0);
                            if (typeList.size() == 1) {
                                List<CsmFunction> filtered = CompletionSupport.filterUserDefinedLiterals(typeList.get(0), mtdList, openingSource);
                                if (filtered.size() > 0) { // do we need this check?
                                    mtdList = filtered;
                                }
                                if (last && !findType) {
                                    String parmStr = formatTypeList(typeList, methodOpen);
                                    result = new CsmCompletionResult(component, getBaseDocument(), mtdList,
                                                formatType(lastType, true, true, false) + item.getTokenText(0) + '(' + parmStr + ')',
                                                item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                } else {
                                    lastType = CompletionSupport.extractFunctionType(this, mtdList, null, typeList);
                                    staticOnly = false;
                                }
                            }
                        } else {
                            // Fallback
                            if (last && !findType) {
                                cont = false;
                                lastType = null;
                            } else { 
                                // pretend that this is usual constant
                                lastType = getPredefinedType(item); // Get the constant type
                                staticOnly = false;
                            }
                        }
                    } else { // Not the first item in a dot exp
                        cont = false; // impossible to have constant inside the expression
                    }
                    break;
                
                case CsmCompletionExpression.CONSTANT: // Constant item
                    if (first) {
                        lastType = getPredefinedType(item); // Get the constant type
                        staticOnly = false;
                    } else { // Not the first item in a dot exp
                        cont = false; // impossible to have constant inside the expression
                    }
                    break;

                case CsmCompletionExpression.VARIABLE: // Variable or special keywords
                    switch (item.getTokenID(0)) {
                        case THIS: // 'this' keyword
                            if (first) { // first item in expression
                                CsmClass cls = sup.getClass(getCsmFile(), item.getTokenOffset(0));
                                if (cls != null) {
                                    derefOfThisOUT.set(true);
                                    lastType = CsmCompletion.createType(cls, 1, 0, 0, false, false);
                                    staticOnly = false;
                                }
                            } else { // 'something.this'
                                staticOnly = false;
                            }
                            break;

//                    case CLASS: // 'class' keyword
//                        if (!first) {
//                            lastType = CsmCompletion.CLASS_TYPE;
//                            staticOnly = false;
//                        } else {
//                            cont = false;
//                        }
//                        break;

                        default: // Regular constant
                            String var = item.getTokenText(0);
                            int varPos = item.getTokenOffset(0) + item.getTokenLength(0);
                            if (first) { // try to find variable for the first item
                                if (last && !findType) { // both first and last item
                                    if (isConstructor) {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES |
                                                CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS |
                                                CompletionResolver.RESOLVE_GLOB_NAMESPACES |
                                                CompletionResolver.RESOLVE_LIB_NAMESPACES |
                                                CompletionResolver.RESOLVE_LIB_CLASSES |
                                                CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS |
                                                CompletionResolver.RESOLVE_LOCAL_CLASSES);
                                    } else {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                                    }
                                    if (resolve(varPos, var, openingSource)) {
                                        if (isConstructor && !var.isEmpty() && !openingSource) {
                                            // completion after new should propose constructors as well to see signatures
                                            // #204910 - Auto complete misses c++ constructors
                                            Collection<? extends CsmObject> candidates = compResolver.getResult().addResulItemsToCol(new ArrayList<CsmObject>());
                                            Collection<CsmObject> res = new ArrayList<CsmObject>(candidates);
                                            for (CsmObject object : candidates) {
                                                if (CsmKindUtilities.isClass(object)) {
                                                    res.addAll(getConstructors((CsmClass) object));
                                                }
                                            }
                                            result = new CsmCompletionResult(component, getBaseDocument(), res, var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                        } else if (openingSource) {
                                            Result lookupSet = compResolver.getResult();
                                            if (!lookupSet.getFileLocalFunctions().isEmpty()
                                                    || !lookupSet.getLibFunctions().isEmpty()
                                                    || !lookupSet.getGlobalProjectFunctions().isEmpty()
                                                    || !lookupSet.getClassMethods().isEmpty()) {
                                                List<CsmObject> filtered = new ArrayList<>();
                                                lookupSet.addResulItemsToCol(filtered);
                                                Iterator<CsmObject> iter = filtered.iterator();
                                                while (iter.hasNext()) {
                                                    CsmObject obj = iter.next();
                                                    if (CsmKindUtilities.isFunction(obj)) {
                                                        iter.remove();
                                                    }
                                                }
                                                if (!filtered.isEmpty()) {
                                                    result = new CsmCompletionResult(component, getBaseDocument(), filtered, var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                                    break;
                                                }
                                            }
                                            result = new CsmCompletionResult(component, getBaseDocument(), compResolver.getResult(), var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                        } else {
                                            result = new CsmCompletionResult(component, getBaseDocument(), compResolver.getResult(), var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                        }
                                    } else {
                                        result = new CsmCompletionResult(component, getBaseDocument(), (CompletionResolver.Result)null, var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                    }
                                } else { // not last item or finding type
                                    // find type of variable
                                    if (nextKind != ExprKind.SCOPE) {
                                        if (first) {
                                            lastType = findExactVarType(var, varPos);
                                        }
                                        if (lastType == null || isAutoType(lastType)) {
                                            // try to find with resolver
                                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                                            if (resolve(varPos, var, true)) {
                                                CompletionResolver.Result res = compResolver.getResult();
                                                lastType = getVariableOrClassifierType(res, new AtomicBoolean(false), item);
                                            }
                                        }
                                    }
                                    if (lastType != null) { // variable found
                                        staticOnly = false;
                                    } else { // no variable found
//                                    scopeAccessedClassifier = (kind == ExprKind.SCOPE);
                                        if (var.length() == 0) {
                                            lastNamespace = finder.getCsmFile().getProject().getGlobalNamespace();
                                        } else {
                                            compResolver.setResolveTypes(
                                                    CompletionResolver.RESOLVE_CLASSES |
                                                    CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS |
                                                    CompletionResolver.RESOLVE_LIB_CLASSES |
                                                    CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS |
                                                    CompletionResolver.RESOLVE_LOCAL_CLASSES);
                                            AtomicBoolean foundVisibleClassifier = new AtomicBoolean(false);
                                            if (resolve(varPos, var, true)) {
                                                lastType = getVariableOrClassifierType(compResolver.getResult(), foundVisibleClassifier, item);
                                            }
                                            if (!foundVisibleClassifier.get()) {
                                                QueryScope old = compResolver.setResolveScope(QueryScope.GLOBAL_QUERY);
                                                compResolver.setResolveTypes(
                                                        CompletionResolver.RESOLVE_GLOB_NAMESPACES |
                                                        CompletionResolver.RESOLVE_LIB_NAMESPACES);
                                                if (resolve(varPos, var, true)) {
                                                    Collection<? extends CsmObject> res = compResolver.getResult().addResulItemsToCol(new ArrayList<CsmObject>());
                                                    if (!res.isEmpty()) {
                                                        CsmObject ns = res.iterator().next();
                                                        if (CsmKindUtilities.isNamespaceAlias(ns)) {
                                                            lastNamespace = ((CsmNamespaceAlias) ns).getReferencedNamespace();
                                                            lastNamespace = CsmCompletion.getProjectNamespace(getCsmProject(), lastNamespace);
                                                        } else if (CsmKindUtilities.isNamespace(ns)) {
                                                            lastNamespace = CsmCompletion.getProjectNamespace(getCsmProject(), (CsmNamespace) ns);
                                                        }
                                                    }
                                                }
                                                // restore old
                                                compResolver.setResolveScope(old);
                                                if (lastNamespace != null) {
                                                    setLogicalContextScope(lastNamespace);
                                                    // clean up invisible backup
                                                    lastType = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { // not the first item
                                boolean needToCheckNS = true;
                                if (lastType != null) { // last was type
                                    needToCheckNS = false;
                                    if (findType || !last) {
                                        boolean inner = false;
                                        int ad = lastType.getArrayDepth();
                                        if (staticOnly && ad == 0) { // can be inner class
                                            CsmClassifier classifier = extractLastTypeClassifier(kind);
                                            CsmType nestedType = findNestedType(classifier, var, getLogicalContextScope(), endOffset);
                                            if (nestedType != null) {
                                                lastType = nestedType;
                                                inner = true;
                                            }
                                        }
                                        if (!inner) { // not inner class name
                                            if (ad == 0 || (kind == ExprKind.ARROW)) { // zero array depth or deref array as pointer
                                                CsmClassifier classifier = getClassifier(lastType, getLogicalContextScope(), contextFile, endOffset);
                                                if (CsmKindUtilities.isClass(classifier)) {
                                                    CsmClass clazz = (CsmClass) classifier;
                                                    List elemList = finder.findFields(contextElement, clazz, var, true, staticOnly, true, true, scopeAccessedClassifier, this.sort);
                                                    if (kind == ExprKind.ARROW || kind == ExprKind.DOT) {
                                                        List<CsmClass> baseClasses = getBaseClasses(var, varPos, clazz, nextKind, true);
                                                        if (elemList == null) {
                                                            elemList = baseClasses;
                                                        } else if (baseClasses != null) {
                                                            elemList.addAll(baseClasses);
                                                        }
                                                    }
                                                    if (elemList != null && elemList.size() > 0) { // match found
                                                        CsmObject csmObj = (CsmObject) elemList.get(0);
                                                        lastType = getObjectType(csmObj, false);
                                                        staticOnly = false;
                                                    } else if (kind == ExprKind.ARROW || kind == ExprKind.SCOPE) {
                                                    } else { // no match found
                                                        lastType = null;
                                                        cont = false;
                                                    }
                                                } else {
                                                    lastType = null;
                                                    cont = false;
                                                }
                                            } else { // array depth > 0 but no array dereference
                                                cont = false;
                                            }
                                        }
                                    } else { // last and searching for completion output
                                        scopeAccessedClassifier = (kind == ExprKind.SCOPE);
//                                    CsmClass curCls = sup.getClass(varPos);
                                        CsmClassifier classifier = extractLastTypeClassifier(kind);
                                        if (classifier == null) {
                                            lastType = null;
                                            cont = false;
                                        } else {
                                            // IZ#143044, IZ#160677
                                            // There is no need for searching in parents for global declarations/definitions
                                            // in case of csope access
                                            boolean inspectParentClasses = (this.contextElement != null || !scopeAccessedClassifier || staticOnly);
                                            List res = findFieldsAndMethods(finder, contextElement, classifier, var, openingSource, staticOnly && !memberPointer, false, inspectParentClasses, this.scopeAccessedClassifier, skipConstructors, sort);
                                            List nestedClassifiers = findNestedClassifiers(finder, contextElement, classifier, var, openingSource, true, sort);
                                            res.addAll(nestedClassifiers);
                                            // add base classes as well
                                            if (kind == ExprKind.ARROW || kind == ExprKind.DOT || kind == ExprKind.SCOPE) {
                                                // try base classes names like in this->Base::foo()
                                                // or like in a.Base::foo()
                                                List<CsmClass> baseClasses = finder.findBaseClasses(contextElement, classifier, var, openingSource, sort);
                                                res.addAll(baseClasses);
                                                if(res.isEmpty()) {
                                                    CsmNamespace ns = finder.getExactNamespace(var);
                                                    if(ns != null && lastNamespace == null) {
                                                        res.add(ns);
                                                    }
                                                }
                                                if (res.isEmpty() && !scopeAccessedClassifier) {
                                                    // Typedef which is synonym of one of base classes
                                                    CsmClassifier varCls = resolveClassifier(var, varPos);
                                                    if (last && CsmBaseUtilities.isValid(varCls)) {
                                                        res.add(varCls);
                                                    } else {
                                                        CsmType varType = CsmCompletion.createType(varCls, 0, 0, 0, false, false);
                                                        baseClasses = getBaseClasses(varType, classifier, nextKind, true);
                                                        if (baseClasses != null && !baseClasses.isEmpty()) {
                                                            res.add(varCls);
                                                        }
                                                    }
                                                }
                                            }
                                            if (res.isEmpty() && scopeAccessedClassifier) {
                                                //  C++11 - allow sizeof to work on members of classes without an explicit object
                                                if (CsmKindUtilities.isClass(classifier)) {
                                                    CsmClass cls = (CsmClass) classifier;
                                                    List<CsmField> fields = finder.findFields(contextElement, cls, var, openingSource, false, false, inspectParentClasses, scopeAccessedClassifier, sort);
                                                    res.addAll(fields);
                                                }
                                            }
                                            if (res.isEmpty() && scopeAccessedClassifier && lastNamespace != null) {
                                                needToCheckNS = true;
                                            } else {
                                                CsmResultItem.SubstitutionHint hint = getSubstitutionHint(kind, lastType);
                                                result = new CsmCompletionResult(
                                                        component, getBaseDocument(),
                                                        //                                                 findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls), cls, var, false, staticOnly, false),
                                                        res,
                                                        hint,
                                                        formatType(lastType, true, true, false) + var + '*',
                                                        item,
                                                        item.getTokenOffset(0),
                                                        item.getTokenLength(0),
                                                        0/*cls.getName().length() + 1*/,
                                                        isProjectBeeingParsed(), contextElement, instantiateTypes);
                                            }
                                        }
                                    }
                                }
                                if (lastNamespace != null && needToCheckNS) { // currently package
                                    String searchPkg = (lastNamespace.isGlobal() ? "" : (lastNamespace.getQualifiedName() + CsmCompletion.SCOPE)) + var;
                                    if (findType || !last) {
                                        List<?> res = finder.findNestedNamespaces(lastNamespace, var, true, true); // find matching nested namespaces
                                        CsmNamespace curNs = res.isEmpty() ? null : (CsmNamespace) res.get(0);
                                        if (curNs != null) {
                                            lastNamespace = CsmCompletion.getProjectNamespace(getCsmProject(), curNs);
                                            lastType = null;
                                        } else { // package doesn't exist
                                            res = finder.findNamespaceElements(lastNamespace, var, true, true, true);
//                                        if(res.isEmpty()) {
//                                            res = finder.findStaticNamespaceElements(lastNamespace, endOffset, var, true, false, true);
//                                        }
                                            CsmObject obj = CompletionSupport.getFirstVisible((List<CsmObject>) res, contextFile);
                                            lastType = getObjectType(obj, false);
                                            cont = (lastType != null);
                                            lastNamespace = null;
                                        }
                                    } else { // last and searching for completion output
                                        if (last) { // get all matching fields/methods/packages
                                            List res = finder.findNestedNamespaces(lastNamespace, var, openingSource, true); // find matching nested namespaces
                                            res.addAll(finder.findNamespaceElements(lastNamespace, var, openingSource, true, false)); // matching classes
//                                            res.addAll(finder.findStaticNamespaceElements(lastNamespace, var, openingSource)); // matching static elements
                                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*', item, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    break;

                case CsmCompletionExpression.ARRAY:
//                cont = resolveItem(item.getParameter(0), first, false, ExprKind.NONE);
                    lastType = resolveType(item.getParameter(0));
                    cont = false;
                    if (lastType != null) { // must be type
                        CsmClassifier cls = getClassifier(lastType, getLogicalContextScope(), contextFile, endOffset);
                        if (cls != null) {
                            if (item.getParameterCount() == 2) { // index in array follows
                                int ptrDepth = lastType.getPointerDepth();
                                int arrDepth = lastType.getArrayDepth();
                                int ref = getReferenceValue(lastType);
                                if (arrDepth == 0 && ptrDepth == 0) {
                                    // Note: in case of performance issues merge iterateTypeChain() and getClassifier() calls into one
                                    TypeInfoCollector typeInfo = new TypeInfoCollector();
                                    CsmUtilities.iterateTypeChain(lastType, typeInfo);
                                    ptrDepth = howMany(typeInfo, CsmUtilities.Qualificator.POINTER);
                                    arrDepth = howMany(typeInfo, CsmUtilities.Qualificator.ARRAY);
                                }
                                // first try to decrease depth of array, then handle pointer as arrays as well
                                if (arrDepth > 0) {
                                    arrDepth--;
                                    lastType = CsmCompletion.createType(cls, ptrDepth, ref, arrDepth, lastType.isConst(), lastType.isVolatile());
                                } else if (ptrDepth > 0) {
                                    ptrDepth--;
                                    lastType = CsmCompletion.createType(cls, ptrDepth, ref, arrDepth, lastType.isConst(), lastType.isVolatile());
                                } else {
                                    CsmFunction opArray = CsmCompletionQuery.getOperator(cls, contextFile, endOffset, CsmFunction.OperatorKind.ARRAY);
                                    if (opArray != null) {
                                        lastType = opArray.getReturnType();
                                    }
                                }
                                cont = true;
                            } else { // no index, increase array depth
                                lastType = CsmCompletion.createType(cls, lastType.getPointerDepth(), getReferenceValue(lastType),
                                        lastType.getArrayDepth() + 1, lastType.isConst(), lastType.isVolatile());
                                cont = true;
                            }
                        }
                    }
                    break;

                case CsmCompletionExpression.INSTANCEOF:
                    lastType = CsmCompletion.BOOLEAN_TYPE;
                    break;

                case CsmCompletionExpression.GENERIC_TYPE: {
                    // if last type exists and we will found type alias not in its scope
                    // we should check that type alias is a synonym of one of base classes
                    boolean outsiderTypeAlias = false;
                    CsmType typ = null;
                    CsmType contextType = lastType;
                    if (resolveTemplateVariable(item, first, last, kind)) {
                        break;
                    }
                    if (first) {
                        // TODO: in case of cpp 14 it is worth to reuse resolved object!
                        typ = resolveType(item.getParameter(0));
                    }
                    if(typ == null) {
                        typ = resolveType(
                                item.getParameter(0),
                                first,
                                new ContextCloneStrategy(
                                        new ContextPropertyChanger()
                                                .setStaticOnly(kind == ExprKind.SCOPE)
                                                .setLastNamespace(lastNamespace)
                                                .setLastType(lastType)
                                )
                        );
                        if (typ == null) {
                            if (kind == ExprKind.DOT || kind == ExprKind.ARROW || kind == ExprKind.SCOPE) {
                                // This could be type alias - AAA().TypeAliasName<BBB>::foo();
                                outsiderTypeAlias = true;
                                typ = resolveType(
                                        item.getParameter(0),
                                        true,
                                        new ContextCloneStrategy(new ContextPropertyChanger().setStaticOnly(true))
                                );
                            }
                        }
                    }
                    lastType = (typ != null) ? contextType : null;
                    if (typ != null) {
                        typ = CsmUtilities.iterateTypeChain(typ, new CsmUtilities.SearchTemplatePredicate());
                        CsmClassifier cls = typ.getClassifier();
                        if (CsmKindUtilities.isSpecialization(cls)) {
                            Collection<CsmOffsetableDeclaration> baseTemplateCandidates = CsmInstantiationProvider.getDefault().getBaseTemplate(cls);
                            for (CsmOffsetableDeclaration baseTemplate : baseTemplateCandidates) {
                                if (CsmKindUtilities.isClassifier(baseTemplate)) {
                                    cls = (CsmClassifier) baseTemplate;
                                    break;
                                }
                            }
                        }
                        if (cls != null && CsmKindUtilities.isTemplate(cls)) {
                            CsmObject obj = CompletionSupport.createInstantiation(this, (CsmTemplate)cls, item, Collections.<CsmType>emptyList());

                            if (CsmKindUtilities.isClassifier(obj)) {
                                typ = CsmCompletion.createType((CsmClassifier)obj, 0, 0, 0, false, false);
                            }

                            if (outsiderTypeAlias && CsmKindUtilities.isTypeAlias(obj)) {
                                if (contextType != null) {
                                    CsmClassifier classifier = getClassifier(contextType, getLogicalContextScope(), contextFile, endOffset);
                                    if (CsmKindUtilities.isClass(classifier)) {
                                        List<CsmClass> baseClasses = getBaseClasses(typ, classifier, nextKind, true);
                                        if (baseClasses == null || baseClasses.isEmpty()) {
                                            typ = lastType = null;
                                            // Do not clear obj here - if it is last item,
                                            // we should return type alias. Clearing
                                            // typ and lastType guarantees that in case of not last
                                            // item, symbols will not be resolved
                                        }
                                    }
                                }
                            }

                            if (CsmKindUtilities.isClass(obj) || CsmKindUtilities.isTypeAlias(obj)) {
                                if (!last || findType) {
                                    lastType = typ;
                                } else {
                                    CsmClassifier c = (CsmClassifier) obj;
                                    Collection<CsmClassifier> classList = new LinkedHashSet<CsmClassifier>();
                                    classList.add(c);
                                    result = new CsmCompletionResult(component, getBaseDocument(), classList,
                                            c.getQualifiedName().toString(),
                                            item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                }
                            }
                        }
                    }
                    break;
                }
                case CsmCompletionExpression.GENERIC_TYPE_OPEN:
                case CsmCompletionExpression.OPERATOR:
                {
                    boolean boolOperator = false;
                    CompletionResolver.Result res = null;
                    List<CsmType> typeList = null;
//                    CsmClassifier cls = null;
//                    if (findType) {
//                        lastType = resolveType(item.getParameter(0));
//                        cls = extractLastTypeClassifier(ExprKind.NONE);
//                    }
                    Collection<CsmFunction> mtdList = new LinkedHashSet<CsmFunction>();
                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                    String operatorPrefix = "operator " + item.getTokenText(0);  // NOI18N
                    if (resolve(item.getTokenOffset(0), operatorPrefix, false)) {
                        res = compResolver.getResult();
                    }
                    if (res != null) {
                        res.addResulItemsToCol(mtdList);
                    }
                    if (shouldDoADLLookup(first, methodOpen) && isOverloadableOperator(item.getTokenID(0))) {
                        // FIXME: To decrease impact on performance, perform it only if mtdList is empty!
                        if (mtdList.isEmpty()) {
                            typeList = getTypeList(item, 0);
                            adlLookup(operatorPrefix, mtdList, typeList);
                        }
                    }
                    result = new CsmCompletionResult(component, getBaseDocument(), res, operatorPrefix, item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                    lastType = null;
                    switch (item.getTokenID(0)) {
                        case EQ: // Assignment operators
                        case PLUSEQ:
                        case MINUSEQ:
                        case STAREQ:
                        case SLASHEQ:
                        case AMPEQ:
                        case BAREQ:
                        case CARETEQ:
                        case PERCENTEQ:
                        case LTLTEQ:
                        case GTGTEQ:
                            if (item.getParameterCount() > 0) {
                                lastType = resolveType(item.getParameter(0));
                                staticOnly = false;
                            }
                            break;

                        case LT: // Binary, result is boolean
                        case GT:
                        case LTEQ:
                        case GTEQ:
                        case EQEQ:
                        case NOTEQ:
                        case AMPAMP: // Binary, result is boolean
                        case BARBAR:
                            boolOperator = true;
                            // nobreak;

                        case PLUS:
                        case MINUS:
                            if (findType && mtdList.isEmpty() && lastType == null) {
                                if (item.getParameterCount() == 2) {
                                    CsmType typ1 = resolveType(item.getParameter(0));
                                    if (typ1 != null && typ1.getArrayDepth() == 0) {
                                        if (findTypeViaOperator(typ1, item)) {
                                            break;
                                        }
                                    }
                                }
                                findStandardCommonType(item);
                                break;
                            }
                            // nobreak;

                        case LTLT: // Always binary
                        case GTGT:
//                    case RUSHIFT:
                        case STAR:
                        case SLASH:
                        case AMP:
                        case BAR:
                        case CARET:
                        case PERCENT:
                            if (findType && lastType == null) {
                                if (!mtdList.isEmpty()) {
                                    if (typeList == null) {
                                        typeList = getTypeList(item, 0);
                                    }
                                    // check exact overloaded operator
                                    mtdList = instantiateFunctions(mtdList, null, typeList, new CsmFunctionsAcceptor());
                                    Collection<CsmFunction> filtered = CompletionSupport.filterMethods(this, mtdList, null, typeList, methodOpen, true);
                                    if (filtered.size() > 0) {
                                        mtdList = filtered;
                                        lastType = CompletionSupport.extractFunctionType(this, mtdList, null, typeList);
                                    } else {
                                        findStandardCommonType(item);
                                    }
                                }
                                if(lastType == null) {
                                    // simple backup
                                    switch (item.getParameterCount()) {
                                        case 2:
                                            CsmType typ1 = resolveType(item.getParameter(0));
                                            if (typ1 != null && typ1.getArrayDepth() == 0) {
                                                if (CsmCompletion.safeIsPrimitiveClass(typ1, typ1.getClassifier())) {
                                                    CsmType typ2 = resolveType(item.getParameter(1));
                                                    if (typ2 != null && typ2.getArrayDepth() == 0) {
                                                        if (CsmCompletion.safeIsPrimitiveClass(typ2, typ2.getClassifier())) {
                                                            lastType = sup.getCommonType(typ1, typ2);
                                                        }
                                                    }
                                                } else {
                                                    findTypeViaOperator(typ1, item);
                                                }
                                            }
                                            break;
                                        case 1: // get the only one parameter
                                            CsmType typ = resolveType(item.getParameter(0));
                                            if (typ != null && CsmCompletion.safeIsPrimitiveClass(typ, typ.getClassifier())) {
                                                lastType = typ;
                                            }
                                            break;
                                    }
                                }
                                if(lastType == null && boolOperator) {
                                    lastType = CsmCompletion.BOOLEAN_TYPE;
                                }
                            }
                            break;

                        case COLON:
                            switch (item.getParameterCount()) {
                                case 2:
                                    CsmType typ1 = resolveType(item.getParameter(0));
                                    CsmType typ2 = resolveType(item.getParameter(1));
                                    if (typ1 != null && typ2 != null) {
                                        lastType = sup.getCommonType(typ1, typ2);
                                        if(lastType == null) {
                                            lastType = typ1;
                                        }
                                    }
                                    break;

                                case 1:
                                    lastType = resolveType(item.getParameter(0));
                                    break;
                            }
                            break;

                        case QUESTION:
                            if (item.getParameterCount() >= 2) {
                                lastType = resolveType(item.getParameter(1)); // should be colon
                            }
                            break;
                    }
                    break;
                }
                case CsmCompletionExpression.UNARY_OPERATOR:
                    if (item.getParameterCount() > 0) {
                        lastType = resolveType(item.getParameter(0));
                        staticOnly = false;
                    }
                    break;

                case CsmCompletionExpression.MEMBER_POINTER_OPEN:
                    if (item.getParameterCount() > 0) {
                        boolean hasAmp = false;
                        if (item.getTokenCount() == 1) {
                            switch (item.getTokenID(0)) {
                                case AMP:
                                    hasAmp = true;
                                    break;
                            }
                        }
                        memberPointer = hasAmp;
                        cont = resolveExp(item.getParameter(0), first);
                        memberPointer = false;
                    }
                    break;
                case CsmCompletionExpression.MEMBER_POINTER:
                    if (item.getParameterCount() > 0) {
                        lastType = resolveType(item.getParameter(0));
                        staticOnly = false;
                        CsmFunction.OperatorKind opKind = null;
                        if (item.getTokenCount() == 1) {
                            switch (item.getTokenID(0)) {
                                case AMP:
                                    opKind = CsmFunction.OperatorKind.ADDRESS;
                                    break;
                                case STAR:
                                    opKind = CsmFunction.OperatorKind.POINTER;
                                    break;
                            }
                        }
                        if (opKind != null) {
                            CsmType opType = CsmCompletionQuery.getOverloadedOperatorReturnType(lastType, contextFile, endOffset, opKind, MAX_DEPTH);
                            if (opType != null) {
                                lastType = opType;
                            } else if (lastType != null) {
                                CsmType lastNestedType = CsmUtilities.iterateTypeChain(lastType, new CsmUtilities.ConstantPredicate<CsmType>(false));

                                int ptrDepth = lastNestedType.getPointerDepth();
                                if (ptrDepth > 0 && opKind == CsmFunction.OperatorKind.POINTER) {
                                    ptrDepth--;
                                }
                                if (opKind == CsmFunction.OperatorKind.ADDRESS) {
                                    ptrDepth++;
                                }

                                lastType = CsmCompletion.createType(
                                    getClassifier(lastNestedType, getLogicalContextScope(), contextFile, endOffset), 
                                    ptrDepth, 
                                    getReferenceValue(lastNestedType), 
                                    lastNestedType.getArrayDepth(), 
                                    lastNestedType.isConst(), 
                                    lastNestedType.isVolatile(), 
                                    lastNestedType.isTemplateBased()
                                );
                            }
                        }
                    // TODO: need to convert lastType into reference based on item token '&' or '*'
                    // and nested pointer expressions
                    }
                    break;

                case CsmCompletionExpression.CONVERSION:
                    lastType = resolveType(item.getParameter(0));
                    staticOnly = false;
                    break;

                case CsmCompletionExpression.TYPE_REFERENCE:
                    if (item.getParameterCount() > 0) {
                        CsmCompletionExpression param = item.getParameter(0);
                        staticOnly = false;
                        lastType = resolveType(param);
                        if(lastType != null) {
                            int ref = getReferenceValue(lastType);
                            int ptrLevel = lastType.getPointerDepth();
                            if (item.getTokenCount() > 0) {
                                switch (item.getTokenID(0)) {
                                    case AMP:
                                        ref = 1;
                                        break;
                                    case AMPAMP:
                                        ref = 2;
                                        break;
                                    case STAR:
                                        ptrLevel++;
                                        break;
                                    default:
                                }
                            }
                            lastType = CsmCompletion.createType(lastType.getClassifier(), ptrLevel, ref, lastType.getArrayDepth(), lastType.isConst(), lastType.isVolatile());
                        }
                    }
                    break;

                case CsmCompletionExpression.TYPE:
                    if (findType) {
                        if (item.getParameterCount() > 0) {
                            lastType = resolveType(item.getParameter(0));
                        } else {
                            lastType = getPredefinedType(item);
                        }
                    }
                    if (!findType || lastType == null) {
                        // this is the case of code completion on parameter or unresolved predefined type
                        int nrTokens = item.getTokenCount();
                        if (nrTokens >= 1) {
                            String varName = item.getTokenText(nrTokens - 1);
                            int varPos = item.getTokenOffset(nrTokens - 1);
                            if (item.getTokenID(nrTokens - 1) == CppTokenId.AUTO) {
                                if (CsmFileInfoQuery.getDefault().isCpp11OrLater(contextFile)) {
                                    MutableObject<Boolean> wasDeduced = new MutableObject<>();
                                    CsmType autoType = findAutoOrDecltypeAutoType(contextFile, item.getTokenOffset(0), wasDeduced);
                                    if (autoType != null) {
                                        if (findType) {
                                            lastType = autoType;
                                        } else {
                                            CsmType userFriendlyType = autoType;
                                            if (wasDeduced.value) {
                                                userFriendlyType = CsmUtilities.iterateTypeChain(autoType, new CsmUtilities.SmartTypeUnrollPredicate());
                                            }
                                            CsmClassifier autoVarCls = userFriendlyType.getClassifier();
                                            if (CsmBaseUtilities.isValid(autoVarCls) && !CsmKindUtilities.isBuiltIn(autoVarCls)) {
                                                result = new CsmCompletionResult(
                                                        component,
                                                        getBaseDocument(),
                                                        Arrays.asList(autoVarCls),
                                                        varName,
                                                        item,
                                                        varPos,
                                                        0,
                                                        0,
                                                        isProjectBeeingParsed(),
                                                        contextElement,
                                                        instantiateTypes
                                                );
                                            }
                                        }
                                    }
                                }
                            } else {
                                compResolver.setResolveTypes(CompletionResolver.RESOLVE_LOCAL_VARIABLES | CompletionResolver.RESOLVE_CLASSES | CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS | CompletionResolver.RESOLVE_GLOB_NAMESPACES | CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS);
                                if (resolve(varPos, varName, openingSource)) {
                                    CompletionResolver.Result res = compResolver.getResult();
                                    if (findType) {
                                        lastType = getVariableOrClassifierType(res, new AtomicBoolean(false), item);
                                    }
                                    result = new CsmCompletionResult(component, getBaseDocument(), res, varName + '*', item, varPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                }
                            }
                        }
                    }
                    break;

                case CsmCompletionExpression.TERNARY_OPERATOR:
                    if (item.getParameterCount() > 1) {
                        // first param - condition, second - colon operator
                        lastType = resolveType(
                                item.getParameter(1),
                                true,
                                new ContextCloneStrategy(new ContextPropertyChanger().setLastNamespace(lastNamespace).setLastType(lastType))
                        );
                    } else {
                        lastType = null;
                    }
                    break;

                case CsmCompletionExpression.DECLTYPE:
                case CsmCompletionExpression.PARENTHESIS:
                    lastType = resolveType(item.getParameter(item.getParameterCount() - 1));
                    break;

                case CsmCompletionExpression.CONSTRUCTOR: // constructor can be part of a DOT expression
                    isConstructor = true;
                    cont = resolveExp(item.getParameter(0), true);
                    staticOnly = false;
                    break;

                case CsmCompletionExpression.METHOD_OPEN: // Unclosed method
                    methodOpen = true;
                // let it flow to method
                // nobreak
                case CsmCompletionExpression.METHOD: // Closed method
                    CsmCompletionExpression mtdNameExp = item.getParameter(0);
                    CsmCompletionExpression genericNameExp = null;
                    while (mtdNameExp.getExpID() == CsmCompletionExpression.GENERIC_TYPE) {
                        genericNameExp = mtdNameExp;
//                        lastType = resolveType(mtdNameExp);
                        if (mtdNameExp.getParameterCount() > 0) {
                            mtdNameExp = mtdNameExp.getParameter(0);
                        } else {
                            break;
                        }
                    }
                    String mtdName = mtdNameExp.getTokenText(0);

                    CsmCompletionExpression param = item.getParameter(0);
                    if(param.getExpID() == CsmCompletionExpression.METHOD) {
                        boolean oldFindType = findType;
                        setFindType(true);
                        cont = resolveExp(param, true);
                        setFindType(oldFindType);
                    }

                    // this() invoked, offer constructors
//                if( ("this".equals(mtdName)) && (item.getTokenCount()>0) ){ //NOI18N
//                    CsmClassifier cls = sup.getClass(item.getTokenOffset(0));
//                    if (cls != null) {
//                        cls = finder.getExactClassifier(cls.getQualifiedName());
//                        if (cls != null) {
//                            isConstructor = true;
//                            mtdName = cls.getName();
//                        }
//                    }
//                }

                    // super() invoked, offer constructors for super class
//                if( ("super".equals(mtdName)) && (item.getTokenCount()>0) ){ //NOI18N
//                    CsmClassifier cls = sup.getClass(item.getTokenOffset(0));
//                    if (cls != null) {
//                        cls = finder.getExactClassifier(cls.getQualifiedName());
//                        if (cls != null) {
//                            cls = cls.getSuperclass();
//                            if (cls != null) {
//                                isConstructor = true;
//                                mtdName = cls.getName();
//                            }
//                        }
//                    }
//                }

                    if (isConstructor) { // Help for the constructor
                        CsmClassifier cls = null;
                        if (first) {
                            cls = CompletionSupport.getClassFromName(CsmCompletionQuery.this.getFinder(), mtdName, true);
                        } else { // not first
//                        if ((last)&&(lastNamespace != null)) { // valid package
//                            cls = JCUtilities.getExactClass(finder, mtdName, (lastNamespace.isGlobal() ? "" : lastNamespace.getName()));
//                        } else if (lastType != null) {
//                            if(last){ // inner class
//                                cls = JCUtilities.getExactClass(finder, mtdName,
//                                lastType.getClassifier().getFullName());
//                            }else{
//                                if (lastType.getArrayDepth() == 0) { // Not array
//                                    cls = lastType.getClassifier();
//                                } else { // Array of some depth
//                                    cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
//                                }
//                            }
//                        }
                        }
                        if (cls == null) {
                            cls = findExactClass(mtdName, mtdNameExp.getTokenOffset(0), genericNameExp);
                        }
                        if (cls != null) {
                            lastType = CsmCompletion.createType(cls, 1, 0, 0, false, false);
//
//                        List ctrList = (finder instanceof JCBaseFinder) ?
//                            JCUtilities.getConstructors(cls, ((JCBaseFinder)finder).showDeprecated()) :
//                            JCUtilities.getConstructors(cls);
//                        String parmStr = "*"; // NOI18N
//                        List typeList = getTypeList(item, 1);
//                        List filtered = sup.filterMethods(ctrList, typeList, methodOpen);
//                        if (filtered.size() > 0) {
//                            ctrList = filtered;
//                            parmStr = formatTypeList(typeList, methodOpen);
//                        }
//                        List mtdList = finder.findMethods(cls, mtdName, true, false, first);
//                        if (mtdList.size() > 0) {
//                            if (last && !findType) {
//                                result = new CsmCompletionResult(component, mtdList,
//                                                        formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
//                                                        item, endOffset, 0, 0);
//                            } else {
//                                    lastType = ((CsmMethod)mtdList.get(0)).getReturnType();
//                                    staticOnly = false;
//                            }
//                        } else{
//                            result = new CsmCompletionResult(component, ctrList,
//                            formatType(lastType, true, false, false) + '(' + parmStr + ')',
//                            item, endOffset, 0, 0);
//                        }
                        } else {
                            isConstructor = false;
                        }
                    }
                    if (true || isConstructor == false) {
                        // Help for the method

                        // when use hyperlink => method() is passed as methodOpen, but we
                        // want to resolve "method"
                        // otherwise we need all in current context
                        if (!methodOpen || openingSource) {
                            // Methods, operators and function pointers classifiers
                            Collection<CsmFunctional> mtdList = new LinkedHashSet<CsmFunctional>();
                            if (first && !(isConstructor && lastType != null)) { // already resolved for constructor
                                // resolve all functions in context
                                int varPos = mtdNameExp.getTokenOffset(0);
                                boolean look4Constructors = findType || openingSource;
                                if(methodOpen) {
                                    Collection<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                                    // try to resolve field initializers
                                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_VARIABLES);
                                    if (resolve(varPos, mtdName, true)) {
                                        compResolver.getResult().addResulItemsToCol(candidates);
                                    }
                                    ArrayList<CsmField> varList = new ArrayList<CsmField>();
                                    for (CsmObject object : candidates) {
                                        if (CsmKindUtilities.isField(object)) {
                                            varList.add((CsmField)object);
                                            break;
                                        }
                                    }
                                    if(!varList.isEmpty()) {
                                        result = new CsmCompletionResult(component, getBaseDocument(), varList,
                                                formatType(lastType, true, true, false) + mtdName,
                                                item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                        return true;
                                    }
                                }
                                if (look4Constructors) {
                                    Collection<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                                    // try to resolve the most visible
                                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_LOCAL_VARIABLES | CompletionResolver.RESOLVE_LOCAL_CLASSES);
                                    if (resolve(varPos, mtdName, true)) {
                                        compResolver.getResult().addResulItemsToCol(candidates);
                                    }

                                    if (candidates.isEmpty()) {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS | CompletionResolver.RESOLVE_CONTEXT_CLASSES);
                                        if (resolve(varPos, mtdName, true)) {
                                            compResolver.getResult().addResulItemsToCol(candidates);
                                        }
                                    }

                                    if (candidates.isEmpty() && instantiations != null) {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS);
                                        if (resolve(varPos, mtdName, true)) {
                                            List<CsmObject> templateParams = new ArrayList<CsmObject>();
                                            compResolver.getResult().addResulItemsToCol(templateParams);
                                            for (CsmObject templateParam : templateParams) {
                                                if (CsmKindUtilities.isTemplateParameter(templateParam)) {
                                                    CsmType resolvedType = resolveTemplateParameter((CsmTemplateParameter) templateParam, instantiations);
                                                    CsmClassifier cls = resolvedType != null ? resolvedType.getClassifier() : null;
                                                    if (CsmKindUtilities.isClass(cls)) {
                                                        ((Collection<CsmObject>)candidates).add(cls);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    for (CsmObject object : candidates) {
                                        if (CsmKindUtilities.isClass(object)) {
                                            mtdList.addAll(getConstructors((CsmClass) object));
                                        } else if (CsmKindUtilities.isFunction(object)) {
                                            mtdList.add((CsmFunction) object);
                                        } else if (CsmKindUtilities.isVariable(object)) {
                                            updateWithVariableOperator(mtdList, (CsmVariable) object);
                                        }
                                    }
                                }
                                compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                                if (resolve(varPos, mtdName, openingSource)) {
                                    compResolver.getResult().addResulItemsToCol(mtdList);
                                }
                                if (!last || findType) {
                                    Collection<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_VARIABLES | CompletionResolver.RESOLVE_LOCAL_VARIABLES);
                                    if (resolve(varPos, mtdName, true)) {
                                        compResolver.getResult().addResulItemsToCol(candidates);
                                    }
                                    for (CsmObject object : candidates) {
                                        if (CsmKindUtilities.isVariable(object)) {
                                            updateWithVariableOperator(mtdList, (CsmVariable)object);
                                        }
                                    }
                                }
                                if (lastType != null && (!last || findType)) {
                                    updateWithOperator(mtdList, lastType);
                                }
                            } else {
                                // if prev expression was resolved => get it's class
                                if (lastType != null) {
                                    CsmClassifier classifier = extractLastTypeClassifier(kind);
                                    // try to find method in last resolved class appropriate for current context
                                    if (CsmKindUtilities.isClass(classifier)) {
                                        // IZ#143044
                                        // There is no need for searching in parents for global declarations/definitions
                                        boolean inspectParentClasses = (this.contextElement != null);
                                        mtdList.addAll(finder.findMethods(this.contextElement, (CsmClass) classifier, mtdName, true, false, first, inspectParentClasses, scopeAccessedClassifier, this.sort));
                                        if (mtdList.isEmpty() && !isConstructor) {
                                            // No methods means no user-defined constructor.
                                            // We do not need to look for other elements
                                            // if we know that we are looking for constructor.

                                            CsmType previousType = lastType;
                                            CsmClassifier previousClassifier = classifier;
                                            lastType = null;
                                            boolean returnImmediately = true;
                                            List<CsmField> foundFields = finder.findFields(this.contextElement, (CsmClass) classifier, mtdName, true, false, first, true, scopeAccessedClassifier, this.sort);
                                            if (foundFields != null && !foundFields.isEmpty()) {
                                                // we found field with correct name, check if it has function pointer type
                                                for (CsmField csmField : foundFields) {
                                                    CsmType fldType = csmField.getType();
                                                    if (fldType != null) {
                                                        if (CsmKindUtilities.isFunctionPointerType(fldType)) {
                                                            // that was a function-type field
                                                            lastType = ((CsmFunctionPointerType) fldType).getReturnType();
                                                        } else {
                                                            // variable like function definition (IZ#159422)
                                                            CsmClassifier cls = fldType.getClassifier();
                                                            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                                                                CsmType type = ((CsmTypedef) cls).getType();
                                                                if (CsmKindUtilities.isFunctionPointerType(type)) {
                                                                    lastType = ((CsmFunctionPointerType) type).getReturnType();
                                                                }
                                                            }
                                                        }
                                                        if (lastType == null) {
                                                            // field can have type with defined "operator()"
                                                            CsmClassifier cls = getClassifier(fldType, null, contextFile, endOffset); // TODO: think about contextScope
                                                            if (CsmKindUtilities.isFunctionPointerClassifier(cls)) {
                                                                lastType = ((CsmFunctional) cls).getReturnType();
                                                            } else {
                                                                CsmFunction funCall = cls == null ? null : CsmCompletionQuery.getOperator(cls, contextFile, endOffset, CsmFunction.OperatorKind.CAST);
                                                                if (funCall != null) {
                                                                    lastType = funCall.getReturnType();
                                                                }
                                                            }
                                                        }
                                                        if (lastType != null) {
                                                            if (last) {
                                                                List<CsmType> typeList = getTypeList(item, 1);
                                                                String parmStr = formatTypeList(typeList, methodOpen);
                                                                Collection<CsmVariable> varList = new ArrayList<CsmVariable>(1);
                                                                varList.add(csmField);
                                                                result = new CsmCompletionResult(component, getBaseDocument(), varList,
                                                                        formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
                                                                        item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                assert previousType != null && previousClassifier != null;
                                                // can be default constructor
                                                if ((staticOnly || kind == ExprKind.SCOPE) && previousType.getArrayDepth() == 0) {
                                                    lastType = findNestedType(previousClassifier, mtdName, getLogicalContextScope(), endOffset);
                                                    returnImmediately = (lastType == null); // do not return if type is resolved
                                                }
                                            }
                                            if (returnImmediately) {
                                                return (lastType != null);
                                            }
                                        }
                                    }
                                } else if (lastNamespace != null) {
                                    final boolean exactMatch = (last ? openingSource : true);
                                    CsmNamespace curNs = lastNamespace;
                                    lastNamespace = null;
                                    List<CsmNamespace> res = finder.findNestedNamespaces(curNs, mtdName, exactMatch, true); // find matching nested namespaces
                                    for (CsmNamespace csmNamespace : res) {
                                        lastNamespace = CsmCompletion.getProjectNamespace(getCsmProject(), csmNamespace);
                                        break;
                                    }
                                    List<CsmObject> elems = finder.findNamespaceElements(curNs, mtdName, exactMatch, true, false); // matching classes
//                                    elems.addAll(finder.findStaticNamespaceElements(curNs, mtdName, openingSource)); // matching static elements
                                    for (CsmObject obj: elems) {
                                        if (CsmKindUtilities.isFunction(obj)) {
                                            mtdList.add((CsmFunction)obj);
                                        } else if (CsmKindUtilities.isTypedef(obj)) {
                                            lastType = ((CsmTypedef)obj).getType();
                                            break;
                                        } else if (CsmKindUtilities.isTypeAlias(obj)) {
                                            CsmTypeAlias alias = (CsmTypeAlias) obj;
                                            alias = (CsmTypeAlias) tryInstantiateClassifier(alias, genericNameExp);
                                            lastType = alias.getType();
                                            break;
                                        } else if (CsmKindUtilities.isClassifier(obj)) {
                                            CsmClassifier cls = (CsmClassifier) obj;
                                            if (CsmKindUtilities.isClass(cls)) {
                                                Collection<CsmFunction> constructors = getConstructors((CsmClass) cls);
                                                if (!constructors.isEmpty()) {
                                                    mtdList.addAll(constructors);
                                                    continue;
                                                }
                                            }
                                            cls = tryInstantiateClassifier(cls, genericNameExp);
                                            lastType = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                                            break;
                                        }
                                    }
                                    if ((findType || (!last && !first)) && mtdList.isEmpty()) {
                                        return lastType != null || lastNamespace != null;
                                    }
                                }
                            }
                            List<CsmType> typeList = null;
                            if (shouldDoADLLookup(first, methodOpen)) {
                                // FIXME: To decrease impact on performance, perform it only if mtdList is empty!
                                if (mtdList.isEmpty()) {
                                    typeList = getTypeList(item, 1);
                                    adlLookup(mtdName, mtdList, typeList);
                                }
                            }
                            if (mtdList.isEmpty()) {
                                // If we have not found method and (lastType != null) it could be default constructor.
                                if (!isConstructor) {
                                    if (first || staticOnly || kind == ExprKind.SCOPE) {
                                        // It could be default constructor call without "new"
                                        CsmClassifier cls = null;
                                        //cls = sup.getClassFromName(CsmCompletionQuery.this.getFinder(), mtdName, true);
                                        if (cls == null) {
                                            cls = findExactClass(mtdName, mtdNameExp.getTokenOffset(0), genericNameExp);
                                        }
                                        if (cls != null) {
                                            lastType = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                                        }
                                    } else {
                                        lastType = null;
                                    }
                                    if (lastType == null && (!last || findType)) {
                                        lastType = findBuiltInFunctionReturnType(mtdName,  mtdNameExp.getTokenOffset(0));
                                    }
                                }
                                return lastType != null;
                            }
                            String parmStr = "*"; // NOI18N
                            if (typeList == null) {
                                typeList = getTypeList(item, 1);
                            }
                            mtdList = instantiateFunctions(mtdList, genericNameExp, typeList, new ConstantPredicate<CsmFunctional>(true));
                            Collection<CsmFunctional> filtered = CompletionSupport.filterMethods(this, mtdList, genericNameExp, typeList, methodOpen, true);
                            if (filtered.size() > 0) {
                                mtdList = filtered;
                                parmStr = formatTypeList(typeList, methodOpen);
                            }
                            if (mtdList.size() > 0) {
                                if (last && !findType) {
                                    // TODO: uninstantiating could be removed after adding instantiations for functions/methods/constructors definitions
                                    mtdList = uninstantiateFunctionDefinitions(mtdList, new ConstantPredicate<CsmFunctional>(true));
                                    result = new CsmCompletionResult(component, getBaseDocument(), mtdList,
                                            formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
                                            item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                } else {
                                    lastType = CompletionSupport.extractFunctionType(this, mtdList, genericNameExp, typeList);
                                    staticOnly = false;
                                }
                            } else {
                                lastType = null; // no method found
                                cont = false;
                            }
                        } else { // package.method() is invalid
                            // this is the case of code completion after opening paren "method(|"
                            int varPos = endOffset; // mtdNameExp.getTokenOffset(0);
                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                            if (resolve(varPos, "", false)) {
                                CompletionResolver.Result res = compResolver.getResult();
                                result = new CsmCompletionResult(component, getBaseDocument(), res, mtdName + '*', mtdNameExp, varPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                            }

//                        } else {
//                            lastNamespace = null;
//                            cont = false;
//                        }
                        }
                    }
                    break;

                case CsmCompletionExpression.LAMBDA_CALL:
                    if (!last || findType) {
                        if (item.getParameterCount() > 0 && item.getParameter(0).getExpID() == CsmCompletionExpression.LAMBDA_FUNCTION) {
                            CsmCompletionExpression lambdaFunction = item.getParameter(0);
                            CsmFunctionDefinition lambda = getCsmLambda(lambdaFunction);
                            if (lambda != null) {
                                List<CsmType> typeList = getTypeList(item, 1);
                                lastType = CompletionSupport.extractFunctionType(this, Arrays.asList(lambda), null, typeList);
                            }
                        }
                    }
                    break;
                    
                case CsmCompletionExpression.UNIFORM_INITIALIZATION:
                    if (!last || findType) {
                        if (item.getParameterCount() > 0) {
                            lastType = resolveType(item.getParameter(0));
                        } else {
                            lastType = null;
                            cont = false;
                        }
                    }
                    break;

                case CsmCompletionExpression.LAMBDA_FUNCTION:
                    if (findType) {
                        // LAMBDA_FUNCTION node is declaration, so only findType is supported
                        CsmFunctionDefinition lambda = getCsmLambda(item);
                        if (lambda != null) {
                            lastType = new CsmCompletion.CompletionClosureType(lambda, 0, false, false);
                        } else {
                            lastType = null; // no lambda found
                            cont = false;
                        }
                    }
                    break;
            }

            // Handle decltypes
            if (lastType != null && instantiations != null && lastType.isTemplateBased()) {
                assert !instantiations.isEmpty() : "Instantiations must not be empty"; //NOI18N
                // FIXME: what about nested classifiers based on template type? Like "_Tp::something".
                // For now getClassifier() returns UnresolvedClass for such types.
                CsmClassifier cls = getClassifier(lastType, getLogicalContextScope(), contextFile, endOffset);
                if (CsmKindUtilities.isTemplateParameter(cls)) {
                    CsmType resolvedType = resolveTemplateParameter((CsmTemplateParameter) cls, instantiations);
                    if (resolvedType != null) {
                        lastType = resolvedType;
                    }
                }   
            }

            if (!findType) {
                if ((result == null || result.getItems().isEmpty()) && lastType != null) {
                    if (last) {
                        if(lastType.isTemplateBased() || CsmFileReferences.isTemplateParameterInvolved(lastType)
                                || CsmFileReferences.hasTemplateBasedAncestors(lastType)) {
                            Collection<CsmObject> data = new ArrayList<CsmObject>();
                            data.add(new TemplateBasedReferencedObjectImpl(lastType, ""));
                            result = new CsmCompletionResult(component, getBaseDocument(), data, "", item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                        }
                    }
                }

                if (last && !first && (result == null || result.getItems().isEmpty()) && lastType != null) {
                    CsmClassifier classifier = getClassifier(lastType, getLogicalContextScope(), contextFile, endOffset);
                    if(CsmKindUtilities.isInstantiation(classifier)) {
                        boolean instantiatedByTemplateParam = false;
                        CsmInstantiation inst = (CsmInstantiation)classifier;
                        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();
                        for (CsmSpecializationParameter specParam : mapping.values()) {
                            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                                CsmType type = ((CsmTypeBasedSpecializationParameter)specParam).getType();
                                if(type != null && type.isTemplateBased()) {
                                    instantiatedByTemplateParam = true;
                                    break;
                                }
                            }
                        }
                        if(instantiatedByTemplateParam) {
                            if(!CsmInstantiationProvider.getDefault().getSpecializations(classifier, contextFile, endOffset).isEmpty()) {
                                Collection<CsmObject> data = new ArrayList<CsmObject>();
                                data.add(new TemplateBasedReferencedObjectImpl(lastType, ""));
                                result = new CsmCompletionResult(component, getBaseDocument(), data, "", item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                            }
                        }
                    }
                }
            }

            if (lastType == null && lastNamespace == null) { // !!! shouldn't be necessary
                cont = false;
            }
            return cont;
        }
        
        // Returns true if resolved (even if resolved with an error)
        private boolean resolveTemplateVariable(CsmCompletionExpression item, boolean first, boolean last, ExprKind kind) {
            if (!last || findType) {
                if (CsmFileInfoQuery.getDefault().isCpp14OrLater(getContextFile())) {
                    // Check if it is variable
                    ContextCloneStrategy strategy;
                    if (first) {
                        strategy = new ContextCloneStrategy();
                    } else {
                        strategy = new ContextCloneStrategy(
                                new ContextPropertyChanger()
                                        .setStaticOnly(kind == ExprKind.SCOPE)
                                        .setLastNamespace(lastNamespace)
                                        .setLastType(lastType)
                        );
                    }
                    List<? extends CompletionItem> candidates = resolveObj(item.getParameter(0), first, strategy);
                    if (candidates != null && !candidates.isEmpty() && candidates.get(0) instanceof CsmResultItem) {
                        CsmResultItem resItem = (CsmResultItem) candidates.get(0);
                        if (CsmKindUtilities.isCsmObject(resItem.getAssociatedObject()) && CsmKindUtilities.isVariable((CsmObject) resItem.getAssociatedObject())) {
                            CsmVariable var = (CsmVariable) resItem.getAssociatedObject();
                            if (CsmKindUtilities.isTemplate(var)) {
                                CsmObject inst = CompletionSupport.createInstantiation(this, (CsmTemplate) var, item, Collections.emptyList());
                                if (CsmKindUtilities.isVariable(inst)) {
                                    lastType = ((CsmVariable) inst).getType();
                                    return true;
                                }
                            }
                            // Error! Trying to instantiate non-template variable
                            lastType = null;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private CsmFunctionDefinition getCsmLambda(CsmCompletionExpression lambdaFunction) {
            if (lambdaFunction != null && lambdaFunction.getExpID() == CsmCompletionExpression.LAMBDA_FUNCTION) {
                int bodyStartOffset = -1;
                for (int i = 0; i < lambdaFunction.getTokenCount(); i++) {
                    if (lambdaFunction.getTokenID(i) == CppTokenId.LBRACE) {
                        bodyStartOffset = lambdaFunction.getTokenOffset(i);
                    }
                }
                if (bodyStartOffset >= 0) {
                    CsmContext context;
                    if (getLexicalContextScope() != null) {
                        context = CsmOffsetResolver.findContextFromScope(contextFile, bodyStartOffset, getLexicalContextScope());
                    } else {
                        context = CsmOffsetResolver.findContext(contextFile, bodyStartOffset, getFileReferencesContext());
                    }
                    if (CsmKindUtilities.isLambda(context.getLastObject())) {
                        return (CsmFunctionDefinition) context.getLastObject();
                    }
                }
            }
            return null;
        }

        private <T extends CsmFunctional> List<T> instantiateFunctions(Collection<T> functions, CsmCompletionExpression exp, List<CsmType> passedParams, Predicate<CsmFunctional> acceptor) {
            List<T> instantiated = new ArrayList<T>(functions.size());
            for (T func : functions) {
                CsmFunctional inst = tryInstantiateFunction(func, exp, passedParams);
                if (acceptor.check(inst)) {
                    instantiated.add((T) inst);
                }
            }
            return instantiated;
        }

        private class CsmFunctionsAcceptor implements Predicate<CsmFunctional> {
            @Override
            public boolean check(CsmFunctional value) {
                return CsmKindUtilities.isFunction(value);
            }
        }

        private CsmClassifier tryInstantiateClassifier(CsmClassifier template, CsmCompletionExpression exp) {
            if (exp != null && CsmKindUtilities.isTemplate(template)) {
                CsmObject inst = CompletionSupport.createInstantiation(this, (CsmTemplate) template, exp, Collections.<CsmType>emptyList());
                if (CsmKindUtilities.isClassifier(inst)) {
                    template = (CsmClassifier) inst;
                }
            }
            return template;
        }

        private CsmFunctional tryInstantiateFunction(CsmFunctional func, CsmCompletionExpression exp, List<CsmType> passedParams) {
            if (CsmKindUtilities.isTemplate(func)) {
                CsmObject inst = CompletionSupport.createInstantiation(this, (CsmTemplate) func, exp, passedParams);
                if (CsmKindUtilities.isFunctional(inst)) {
                    func = (CsmFunctional) inst;
                }
            }
            return func;
        }
        
        private boolean findTypeViaOperator(CsmType type, CsmCompletionExpression opExp) {
            CsmClassifier classifier = extractTypeClassifier(type, getLogicalContextScope(), ExprKind.NONE);
            if (CsmKindUtilities.isClass(classifier)) {
                CsmClass cls = (CsmClass) classifier;
                CsmFunction.OperatorKind opKind = CsmFunction.OperatorKind.getKindByImage(opExp.getTokenText(0), true);
                if (opKind != CsmFunction.OperatorKind.NONE) {
                    CsmFunction member = getOperator(cls, contextFile, endOffset, opKind);
                    if (member != null) {
                        lastType = member.getReturnType();
                        return true;
                    }
                }
            }
            return false;
        }
        
        private void findStandardCommonType(CsmCompletionExpression item) {
            if (item.getParameterCount() > 1) {
                CsmType type0 = resolveType(item.getParameter(0));
                CsmType type1 = resolveType(item.getParameter(1));
                if (type0 != null && type1 != null) {
                    lastType = sup.getCommonType(this, type1, type0, item.getTokenID(0));
                    if (lastType == null) {
                        if (type0.isBuiltInBased(true)) {
                            lastType = type1;
                        } else {
                            lastType = type0;
                        }
                    }
                } else {
                    lastType = type0;
                }
                staticOnly = false;
            } else if (item.getParameterCount() > 0) {
                lastType = resolveType(item.getParameter(0));
                staticOnly = false;
            }
        }
        
        private boolean shouldDoADLLookup(boolean first, boolean methodOpen) {
            return first && !methodOpen 
                && lastType == null && lastNamespace == null
                && CsmFileInfoQuery.getDefault().isCpp98OrLater(contextFile);
        }
        
        /**
         * Performs argument-dependent lookup.
         * 
         * It works as a fallback for now, i.e. not according to the standard.
         * 
         * @param <T>
         * @param name of the function/operator
         * @param mtdList list of elements found with unqualified name lookup
         * @param typeList list of function call parameters
         * 
         * @return true if lookup found something, false otherwise
         */
        private <T extends CsmFunctional> boolean adlLookup(String name, Collection<T> mtdList, List<CsmType> typeList) {
            if (name == null || name.isEmpty()) {
                return false;
            }
            // First of all, see if we can perform ADL
            for (CsmFunctional fun : mtdList) {
                if (CsmKindUtilities.isClassMember(fun)) {
                    return false;
                }
                if (CsmKindUtilities.isFunctionPointerClassifier(fun)) {
                    return false;
                }
                // TODO: block-scope function declaration that is not a using-declaration must prevent ADL.
                // But now we do not look for such functions.
            }
            Set<CsmNamespace> handledNamespaces = new HashSet<CsmNamespace>();
            ClassifiersAntiLoop handledClasses = new ClassifiersAntiLoop();
            for (CsmType type : typeList) {
                if (adlLookupType(type, name, mtdList, handledClasses, handledNamespaces)) {
                    return true;
                }
            }
            return false;
        }
        
        private <T extends CsmFunctional> boolean adlLookupType(CsmType type, String name, Collection<T> mtdList, ClassifiersAntiLoop handledClasses, Set<CsmNamespace> handledNamespaces) {
            if (type != null) {
                CsmClassifier typeCls = getClassifier(type, getLexicalContextScope(), contextFile, endOffset);
                if (CsmKindUtilities.isClass(typeCls)) {
                    if (adlLookupClass((CsmClass) typeCls, name, mtdList, handledClasses, handledNamespaces)) {
                        return true;
                    }
                } else if (CsmKindUtilities.isUnion(typeCls)) {
                    if (adlLookupDirectly(typeCls, name, mtdList, handledNamespaces)) {
                        return true;
                    }
                } else if (CsmKindUtilities.isEnum(typeCls)) {
                    if (adlLookupDirectly(typeCls, name, mtdList, handledNamespaces)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private <T extends CsmFunctional> boolean adlLookupClass(CsmClass cls, String name, Collection<T> mtdList, ClassifiersAntiLoop handledClasses, Set<CsmNamespace> handledNamespaces) {
            if (!handledClasses.add(cls)) {
                return false;
            }
            if (adlLookupDirectly(cls, name, mtdList, handledNamespaces)) {
                return true;
            }
            Collection<CsmInheritance> baseClasses = cls.getBaseClasses();
            for (CsmInheritance inh : baseClasses) {
                CsmClass baseCls = CsmInheritanceUtilities.getCsmClass(inh);
                if (baseCls != null) {
                    if (adlLookupClass(baseCls, name, mtdList, handledClasses, handledNamespaces)) {
                        return true;
                    }
                }
            }
            if (CsmKindUtilities.isTemplate(cls) && CsmKindUtilities.isInstantiation(cls)) {
                // FIXME: this is not right. The correct way to get types of parameters
                // is via InstantiationProviderImpl.InstantiationParametersInfo.
                // See InstantiationParametersInfoImpl.
                CsmTemplate asTemplate = (CsmTemplate) cls;
                CsmInstantiation asInstantiation = (CsmInstantiation) cls;
                Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = asInstantiation.getMapping();
                for (CsmTemplateParameter param : asTemplate.getTemplateParameters()) {
                    CsmSpecializationParameter specParam = mapping.get(param);
                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                        CsmType type = ((CsmTypeBasedSpecializationParameter) specParam).getType();
                        if (adlLookupType(type, name, mtdList, handledClasses, handledNamespaces)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        private <T extends CsmFunctional> boolean adlLookupDirectly(CsmObject obj, String name, Collection<T> mtdList, Set<CsmNamespace> handledNamespaces) {
            List<CsmNamespace> associatedNamespaces = adlGetAssociatedNamespaces(obj, handledNamespaces);
            if (associatedNamespaces != null) {
                for (CsmNamespace namespace : associatedNamespaces) {
                    if (adlAddFunctions(mtdList, finder.findNamespaceElements(namespace, name, true, false, false))) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private List<CsmNamespace> adlGetAssociatedNamespaces(CsmObject elem, Set<CsmNamespace> handledNamespaces) {
            while (!CsmKindUtilities.isNamespace(elem) && CsmKindUtilities.isScopeElement(elem)) {
                elem = ((CsmScopeElement) elem).getScope();
            }
            if (CsmKindUtilities.isNamespace(elem)) {
                CsmNamespace candidate = (CsmNamespace) elem;
                if (!candidate.isGlobal() && handledNamespaces.add(candidate)) {
                    if (candidate.isInline()) {
                        CsmNamespace parentNs = candidate.getParent();
                        if (parentNs != null && !parentNs.isGlobal() && handledNamespaces.add(parentNs)) {
                            List<CsmNamespace> candidates = new ArrayList<CsmNamespace>(2);
                            candidates.add(candidate);
                            candidates.add(parentNs);
                            return candidates;
                        }
                    }
                    return Collections.singletonList(candidate);
                }
            }
            return null;
        }
        
        private <T extends CsmFunctional> boolean adlAddFunctions(Collection<T> mtdList, List<CsmObject> elements) {
            if (elements != null && !elements.isEmpty()) {
                boolean hasAddedElems = false;
                for (CsmObject elem : elements) {
                    if (CsmKindUtilities.isFunction(elem)) { // is this safe?
                        mtdList.add((T) elem);
                        hasAddedElems = true;
                    }
                }
                return hasAddedElems;
            }
            return false;
        }

        private CsmClassifier resolveClassifier(String var, int varPos)  {
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES |
                                         CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS |
                                         CompletionResolver.RESOLVE_LIB_CLASSES |
                                         CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS |
                                         CompletionResolver.RESOLVE_LOCAL_CLASSES);

            if (resolve(varPos, var, true)) {
                List<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                compResolver.getResult().addResulItemsToCol(candidates);

                for (CsmObject obj : candidates) {
                    if (CsmKindUtilities.isClassifier(obj)) {
                        return (CsmClassifier) obj;
                    }
                }
            }

            return null;
        }

        private List<CsmClass> getBaseClasses(String var, int varPos, CsmClassifier clazz, ExprKind nextKind, boolean exactMatch) {
            // try base classes names like in this->Base::foo()
            // or like in a.Base::foo()
            CsmClassifier cls = resolveClassifier(var, varPos);
            CsmType varType = CsmCompletion.createType(cls, 0, 0, 0, false, false);
            return getBaseClasses(varType, clazz, nextKind, exactMatch);
        }

        private List<CsmClass> getBaseClasses(CsmType varType, CsmClassifier clazz, ExprKind nextKind, boolean exactMatch) {
            // try base classes names like in this->Base::foo()
            // or like in a.Base::foo()
            CsmClassifier varCls = extractTypeClassifier(varType, null, nextKind); // FIXME: scope is null?
            List<CsmClass> baseClasses = null;
            if (varCls != null) {
                baseClasses = finder.findBaseClasses(contextElement, clazz, varCls.getName().toString(), exactMatch, this.sort);
            }
            return baseClasses;
        }

        private CsmType resolveTemplateParameter(CsmTemplateParameter param, List<CsmInstantiation> instantiations) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            return ip.instantiate(param, instantiations);
        }

        private void updateWithVariableOperator(Collection<CsmFunctional> mtdList, CsmVariable var) {
            CsmFunctional fun = lookForVariableOperator(var);
            if (fun != null) {
                mtdList.add(fun);
            }
        }

        private void updateWithOperator(Collection<CsmFunctional> mtdList, CsmType type) {
            CsmFunctional fun = lookForOperator(type);
            if (fun != null) {
                mtdList.add(fun);
            }
        }

        private CsmFunctional lookForVariableOperator(CsmVariable object) {
            CsmType varType = object.getType();
            if (isAutoType(varType)) {
                varType = findAutoVariableType(object, varType);
            }
            return lookForOperator(varType);
        }

        private CsmFunctional lookForOperator(CsmType type) {
            if (type != null) {
                CsmClassifier cls = getClassifier(type, getLogicalContextScope(), contextFile, endOffset);
                if (CsmKindUtilities.isFunctionPointerClassifier(cls)) {
                    return (CsmFunctional) cls;
                } else if (CsmKindUtilities.isClosureClassifier(cls)) {
                    return (CsmFunctional) cls;
                } else {
                    CsmFunction funCall = cls == null ? null : CsmCompletionQuery.getOperator(cls, contextFile, endOffset, CsmFunction.OperatorKind.CAST);
                    if (funCall != null) {
                        return funCall;
                    }
                }
            }
            return null;
        }

        private boolean checkQualifier(CsmCompletionExpression exp, CppTokenId qualifier) {
            // start and end are set to default values for CsmCompletionExpression.TYPE
            int start = 0;
            int end = exp.getTokenCount() - 1;

            if (exp.getExpID() == CsmCompletionExpression.VARIABLE) {
                // TODO: maybe it is better to have VARIABLE tokens in the same sequence as in TYPE.
                start = 1;
                end = exp.getTokenCount();
            }

            for (int i = start; i < end; i++) {
                if (qualifier.equals(exp.getTokenID(i))) {
                  return true;
                }
            }
            return false;
        }

        private CsmType findBuiltInFunctionReturnType(String mtdName, int tokenOffset) {
            CsmType out = null;
            if ("typeid".contentEquals(mtdName)) { // NOI18N
                CsmClassifier cls = getFinder().getExactClassifier("std::type_info"); // NOI18N
                if (cls == null) {
                    CsmNamespace ns = findExactNamespace("std", tokenOffset); // NOI18N
                    if (ns != null) {
                        List<CsmClassifier> findClasses = getFinder().findClasses(ns, mtdName, true, false);
                        for (CsmClassifier csmClassifier : findClasses) {
                            cls = csmClassifier;
                            break;
                        }
                    }
                }
                if (cls != null) {
                    out = CsmCompletion.createType(cls, 0, 0, 0, false, false);
                }
            }
            return out;
        }

        private CsmNamespace findExactNamespace(final String var, final int varPos) {
            CsmNamespace ns = null;
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_GLOB_NAMESPACES | CompletionResolver.RESOLVE_LIB_NAMESPACES);
            if (resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Collection<? extends CsmObject> addResulItemsToCol = res.addResulItemsToCol(new ArrayList<CsmObject>());
                for (CsmObject csmObject : addResulItemsToCol) {
                    if (CsmKindUtilities.isNamespace(csmObject)) {
                        return (CsmNamespace) csmObject;
                    } else if (CsmKindUtilities.isNamespaceAlias(csmObject)) {
                        ns = ((CsmNamespaceAlias) csmObject).getReferencedNamespace();
                        if (ns != null) {
                            return ns;
                        }
                    }
                }
            }
            return ns;
        }

        private CsmClassifier findExactClass(final String var, final int varPos, CsmCompletionExpression genericNameExp) {
            CsmClassifier cls = null;
            if (instantiations != null) {
                compResolver.setResolveTypes(CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS | CompletionResolver.RESOLVE_CLASSES | CompletionResolver.RESOLVE_LOCAL_CLASSES | CompletionResolver.RESOLVE_LIB_CLASSES | CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS);
            } else {
                compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES | CompletionResolver.RESOLVE_LOCAL_CLASSES | CompletionResolver.RESOLVE_LIB_CLASSES | CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS);
            }
            if (resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Collection<? extends CsmObject> allItems = res.addResulItemsToCol(new ArrayList<CsmObject>());
                // check visibility if more than one element in collection
                Collection<CsmClassifier> otherClassifiers = new ArrayList<CsmClassifier>();
                List<CsmObject> visibleClassifiers = new ArrayList<CsmObject>();
                final CsmIncludeResolver ir = CsmIncludeResolver.getDefault();
                List<CsmObject> td = new ArrayList<CsmObject>();
                AtomicBoolean hasClassifier = new AtomicBoolean(false);
                for (CsmObject item : allItems) {
                    if (CsmKindUtilities.isTemplateParameter(item)) {
                        CsmType resolvedType = resolveTemplateParameter((CsmTemplateParameter) item, instantiations);
                        CsmClassifier resolvedCls = resolvedType != null ? resolvedType.getClassifier() : null;
                        if (CsmKindUtilities.isClass(resolvedCls)) {
                            visibleClassifiers.add(resolvedCls);
                        }
                    } else if (CsmKindUtilities.isClassifier(item)) {
                        // if more than one we prefer visible
                        if (ir.isObjectVisible(contextFile, item)) {
                            fillVisibleListAndFilterTypedefs(item, hasClassifier, visibleClassifiers, td);
                        } else {
                            // remember is only one or the first and invisible
                            otherClassifiers.add((CsmClassifier) item);
                        }
                    }
                }
                if (TRACE_MULTIPLE_VISIBE_OBJECTS) {
                    if (visibleClassifiers.size() > 1) {
                        // we have several visible classifiers
                        System.err.printf("findExactClass: we have several classifiers %s visible from %s [%d]%n", var, contextFile.getAbsolutePath(), endOffset); // NOI18N
                        int ind = 0;
                        for (CsmObject csmClassifier : visibleClassifiers) {
                            System.err.printf("[%d] %s%n", ind++, csmClassifier); // NOI18N
                        }
                    }
                }
                for (CsmObject csmClassifier : visibleClassifiers) {
                    csmClassifier = tryInstantiateClassifier((CsmClassifier) csmClassifier, genericNameExp);
                    cls = CsmBaseUtilities.getOriginalClassifier((CsmClassifier)csmClassifier, contextFile);
                    if (cls != null) {
                        break;
                    }
                }
                if (cls == null) {
                    for (CsmClassifier csmClassifier : otherClassifiers) {
                        csmClassifier = tryInstantiateClassifier(csmClassifier, genericNameExp);
                        cls = CsmBaseUtilities.getOriginalClassifier(csmClassifier, contextFile);
                        if (cls != null) {
                            break;
                        }
                    }
                }
            }
            return cls;
        }

        private CsmType findNestedType(CsmClassifier classifier, String name, CsmScope contextScope, int endOffset) {
            if (CsmKindUtilities.isClass(classifier)) {
                CsmClass clazz = (CsmClass) classifier;
                List<CsmClassifier> classes = finder.findNestedClassifiers(contextElement, clazz, name, true, true, this.sort);
                if (classes != null && !classes.isEmpty()) {
                    return CsmCompletion.createType(classes.get(0), 0, 0, 0, false, false);
                }
            }
            return null;
        }

        private CsmType findExactVarType(final String var, final int varPos) {
            return sup.findExactVarType(finder.getCsmFile(), var, varPos, getFileReferencesContext());
        }

        private List<CsmType> getTypeList(CsmCompletionExpression item, int firstChildIdx) {
            List<CsmType> typeList = new ArrayList<CsmType>();
            List<CsmCompletionExpression> fixedParams = fixTypeReferences(item, firstChildIdx);
            if (fixedParams != null) {
                for (CsmCompletionExpression param : fixedParams) {
                    insertTypeOfParam(param, fixedParams.size() == 1, typeList);
                }
            } else {
                int parmCnt = item.getParameterCount();
                if (parmCnt > firstChildIdx) { // will try to filter by parameters
                    for (int i = firstChildIdx; i < parmCnt; i++) {
                        insertTypeOfParam(item.getParameter(i), ((parmCnt - firstChildIdx) == 1), typeList);
                    }
                }
            }
            return typeList;
        }
        
        private List<CsmCompletionExpression> fixTypeReferences(CsmCompletionExpression item, int firstChildIdx) {
            CsmContext context = compResolver.getResult().getContext();
            if (context != null) {
                CsmScope lastScope = context.getLastScope();
                // Inside variable shouldn't be type references.
                if (!CsmKindUtilities.isVariable(context.getLastObject())) {
                    if (!CsmKindUtilities.isCompoundStatement(lastScope)) {
                        // Let's fix ast only in compound scope
                        return null;
                    }
                    if (!CsmKindUtilities.isScopeElement(lastScope)) {
                        // Compound statement must be in some scope
                        return null;
                    }
                    if (!CsmKindUtilities.isFunctionDefinition(((CsmScopeElement) lastScope).getScope())) {
                        // That scope must be a function definition
                        return null;
                    }
                }
            } else {
                // No context => we cannot check whether type references are valid or not
                return null;
            }
            int paramCnt = item.getParameterCount();
            if (paramCnt > firstChildIdx) {
                List<CsmCompletionExpression> paramsAsts = null;
                for (int i = firstChildIdx; i < paramCnt; i++) {
                    CsmCompletionExpression param = item.getParameter(i);
                    CsmCompletionExpression typeRef = null;
                    CsmCompletionExpression argument = null;
                    if (param.getExpID() == CsmCompletionExpression.VARIABLE) {
                        if (param.getParameterCount() == 1 
                                && param.getParameter(0).getExpID() == CsmCompletionExpression.TYPE_REFERENCE) {
                            typeRef = param.getParameter(0);
                            if (typeRef.getParameterCount() == 1) {
                                if (typeRef.getParameter(0).getExpID() == CsmCompletionExpression.TYPE) {
                                    argument = typeRef.getParameter(0);
                                }
                            }
                        }
                    }
                    if (typeRef != null && argument != null && argument.getTokenCount() > 0) {
                        if (paramsAsts == null) {
                            paramsAsts = initParamAstsList(item, firstChildIdx, i);
                        }
                        CsmCompletionExpression operator = CsmCompletionExpression.createTokenExp(
                                CsmCompletionExpression.OPERATOR, typeRef
                        );
                        operator.addParameter(CsmCompletionExpression.createTokenExp(
                                CsmCompletionExpression.VARIABLE, param
                        ));
                        switch (argument.getTokenID(0)) {
                            case SCOPE:
                                operator.addParameter(CsmCompletionExpression.createTokenExp(
                                        CsmCompletionExpression.SCOPE, argument, true
                                )); 
                                break;
                            case IDENTIFIER:
                                operator.addParameter(CsmCompletionExpression.createTokenExp(
                                        CsmCompletionExpression.VARIABLE, argument, true
                                )); 
                                break;
                            default:
                                operator.addParameter(argument);
                                break;
                        }
                        paramsAsts.add(operator);
                    } else if (paramsAsts != null) {
                        paramsAsts.add(param);
                    }
                }
                return paramsAsts;
            }
            return null;
        }
        
        private List<CsmCompletionExpression> initParamAstsList(CsmCompletionExpression item, int from, int to) {
            List<CsmCompletionExpression> params = new ArrayList<CsmCompletionExpression>();
            for (int i = from; i < to; i++) {
                params.add(item.getParameter(i));
            }
            return params;
        }
        
        private void insertTypeOfParam(CsmCompletionExpression param, boolean isTheOnlyParam, List<CsmType> typeList) {
            CsmType typ = resolveType(param);
            if(!(isTheOnlyParam && typ != null && "void".equals(typ.getCanonicalText().toString()))) { // NOI18N
                typeList.add(typ);
            }
        }

        private boolean isInNamespaceOnlyUsage(final int tokenOffset) {
            // check for "using namespace " or "namespace A = "
            final AtomicBoolean out = new AtomicBoolean(false);
            final Document document = sup.getDocument();
            document.render(new Runnable() {
                @Override
                public void run() {
                    TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(document, tokenOffset, true, true);
                    if (ts != null) {
                        // check back for using directive or namespace aliasing
                        // stop overwise
                        ts.move(tokenOffset);
                        OUTER:
                        while (ts.movePrevious()) {
                            Token<TokenId> token = ts.token();
                            final TokenId id = token.id();
                            if (CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory())) {
                                continue;
                            }
                            if (id instanceof CppTokenId) {
                                switch ((CppTokenId)id) {
                                    case IDENTIFIER:
                                    case SCOPE:
                                    case EQ:
                                        // valid
                                        break;
                                    case NAMESPACE:
                                        out.set(true);
                                        break OUTER;
                                    default:
                                        break OUTER;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            });
            return out.get();
        }

        private CsmResultItem.SubstitutionHint getSubstitutionHint(ExprKind kind, CsmType type) {
            CsmResultItem.SubstitutionHint hint = CsmResultItem.SubstitutionHint.NONE;
            if (openingSource) {
                return hint;
            }
            if (type != null) {
                if (kind == ExprKind.DOT) {
                    type = CsmUtilities.iterateTypeChain(type, new CsmUtilities.Predicate<CsmType>() {

                        @Override
                        public boolean check(CsmType value) {
                            return value.isPointer();
                        }

                    });

                    if (type.isPointer()) {
                        hint = CsmResultItem.SubstitutionHint.DOT_TO_ARROW;
                    }
                } else if (kind == ExprKind.ARROW && !type.isPointer()) {

                }
            }
            return hint;
        }

        private class ContextPropertyChanger {

            private Boolean staticOnly;

            private Boolean findType;
            
            private boolean lastTypeGiven;
            private CsmType lastType;

            private boolean lastNamespaceGiven;
            private CsmNamespace lastNamespace;

            ContextPropertyChanger setStaticOnly(boolean staticOnly) {
                this.staticOnly = staticOnly;
                return this;
            }

            ContextPropertyChanger setFindType(boolean findType) {
                this.findType = findType;
                return this;
            }
            
            ContextPropertyChanger setLastType(CsmType lastType) {
                this.lastType = lastType;
                lastTypeGiven = true;
                return this;
            }

            ContextPropertyChanger setLastNamespace(CsmNamespace lastNamespace) {
                this.lastNamespace = lastNamespace;
                lastNamespaceGiven = true;
                return this;
            }

            void apply(Context ctx) {
                if (this.staticOnly != null) {
                    ctx.staticOnly = this.staticOnly;
                }
                if (this.findType != null) {
                    ctx.findType = this.findType;
                }
                if (this.lastTypeGiven) {
                    ctx.lastType = this.lastType;
                }
                if (this.lastNamespaceGiven) {
                    ctx.lastNamespace = this.lastNamespace;
                }
            }
        }

        private class ContextCloneStrategy {

            private final ContextPropertyChanger propertyChanger;

            public ContextCloneStrategy() {
                this.propertyChanger = null;
            }

            public ContextCloneStrategy(ContextPropertyChanger propertyChanger) {
                this.propertyChanger = propertyChanger;
            }

            Context cloneContext(Context context) {
                Context ctx = (Context) context.clone();
                if (propertyChanger != null) {
                    propertyChanger.apply(ctx);
                }
                return ctx;
            }

        }
    }

    private static String formatTypeList(List<CsmType> typeList, boolean methodOpen) {
        StringBuilder sb = new StringBuilder();
        if (typeList.size() > 0) {
            int cntM1 = typeList.size() - 1;
            for (int i = 0; i <= cntM1; i++) {
                CsmType t = typeList.get(i);
                if (t != null) {
// XXX                    sb.append(t.format(false));
                    sb.append(t.getText());
                } else {
                    sb.append('?'); //NOI18N
                }
                if (i < cntM1) {
                    sb.append(", "); // NOI18N
                }
            }
            if (methodOpen) {
                sb.append(", *"); // NOI18N
            }
        } else { // no parameters
            if (methodOpen) {
                sb.append("*"); // NOI18N
            }
        }
        return sb.toString();
    }

    public static class CsmCompletionResult {

        /** First offset in the name of the (inner) class
         * to be displayed. It's used to display the inner classes
         * of the main class to exclude the initial part of the name.
         */
        private final int classDisplayOffset;
        /** Expression to substitute */
        private final CsmCompletionExpression substituteExp;
        /** Starting position of the text to substitute */
        private final int substituteOffset;
        /** Length of the text to substitute */
        private final int substituteLength;
        /** Component to update */
        private final JTextComponent component;
        /**
         * baseDocument to work with
         */
        private BaseDocument baseDocument;
        private final List<CsmResultItem> items;

        CsmCompletionResult(JTextComponent component, BaseDocument doc, Collection<?> data, String title,
                CsmCompletionExpression substituteExp, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc, data, title, substituteExp, substituteExp.getTokenOffset(0),
                    substituteExp.getTokenLength(0), classDisplayOffset, isProjectBeeingParsed, contextElement, instantiateTypes);
        }

        CsmCompletionResult(JTextComponent component, BaseDocument doc, CompletionResolver.Result res, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc,
                    convertData(res, classDisplayOffset, substituteExp, substituteOffset, contextElement, instantiateTypes),
                    true,
                    title,
                    substituteExp,
                    substituteOffset,
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }

        CsmCompletionResult(JTextComponent component, BaseDocument doc, Collection<?> data, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc,
                    convertData(data, classDisplayOffset, substituteExp, substituteOffset, contextElement, instantiateTypes, CsmResultItem.SubstitutionHint.NONE),
                    true, title, substituteExp, substituteOffset,
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }

        CsmCompletionResult(JTextComponent component, BaseDocument doc, Collection<?> data, CsmResultItem.SubstitutionHint hint, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc,
                    convertData(data, classDisplayOffset, substituteExp, substituteOffset, contextElement, instantiateTypes, hint),
                    true, title, substituteExp, substituteOffset,
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }

        private CsmCompletionResult(JTextComponent component, BaseDocument doc, List<CsmResultItem> data, boolean updateTitle, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed) {
//            super(component,
//                    updateTitle ? getTitle(data, title, isProjectBeeingParsed) : title,
//                    data,
//                    substituteOffset,
//                    substituteLength);

            this.component = component;
            this.baseDocument = doc;
            this.substituteExp = substituteExp;
            this.substituteOffset = substituteOffset;
            this.substituteLength = substituteLength;
            this.classDisplayOffset = classDisplayOffset;
            this.items = data;
        }

        public List<? extends CompletionItem> getItems() {
            return Collections.unmodifiableList(items);
        }

        private static String getTitle(List<?> data, String origTitle, boolean isProjectBeeingParsed) {
            if (CsmUtilities.DEBUG) {
                System.out.println("original title (resolved type) was " + origTitle); //NOI18N
            }
            String out = NO_SUGGESTIONS;
            if (data != null && data.size() > 0) {
                out = origTitle;
            }
            if (isProjectBeeingParsed) {
                out = MessageFormat.format(PROJECT_BEEING_PARSED, new Object[]{out});
            }
            return out;
        }

        protected JTextComponent getComponent() {
            return component;
        }

        protected int getSubstituteLength() {
            return substituteLength;
        }

        public int getSubstituteOffset() {
            return substituteOffset;
        }

        protected CsmCompletionExpression getSubstituteExp() {
            return substituteExp;
        }

        protected int getClassDisplayOffset() {
            return classDisplayOffset;
        }
        private boolean simpleVariableExpression;

        private void setSimpleVariableExpression(boolean simple) {
            this.simpleVariableExpression = simple;
        }

        public boolean isSimpleVariableExpression() {
            return simpleVariableExpression;
        }
    }

    private static boolean isSimpleVariableExpression(CsmCompletionExpression exp) {
        switch (exp.getExpID()) {
            case CsmCompletionExpression.MEMBER_POINTER:
            case CsmCompletionExpression.MEMBER_POINTER_OPEN:
                if (exp.getParameterCount() > 0) {
                    return isSimpleVariableExpression(exp.getParameter(0));
                }
                break;
            case CsmCompletionExpression.DOT_OPEN: // Dot expression with the dot at the end
            case CsmCompletionExpression.ARROW_OPEN: // Arrow expression with the arrow at the end
            case CsmCompletionExpression.DOT: // Dot expression
            case CsmCompletionExpression.ARROW: // Arrow expression
            case CsmCompletionExpression.SCOPE_OPEN: // Scope expression with the arrow at the end
            case CsmCompletionExpression.SCOPE: // Scope expression
            case CsmCompletionExpression.NEW: // 'new' keyword
                return false;
        }
        return true;
    }

    //========================== Items Factory ===============================
    protected static void setCsmItemFactory(NbCsmItemFactory factory) {
        itemFactory = factory;
    }

    public static CsmItemFactory getCsmItemFactory() {
        return itemFactory;
    }

    public interface CsmItemFactory {

        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var);

        public CsmResultItem createLabelResultItem(CsmLabel csmStatement);

        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld);

        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);

        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.ForwardEnumResultItem createForwardEnumResultItem(CsmEnumForwardDeclaration cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac);

        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac);

        public CsmResultItem.TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par);

        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enm, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac);

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);

        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath);

        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);

        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac);

        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac);

        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes);

        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);

        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath);
    }
    private static final int FAKE_PRIORITY = 1000;

    public static final class DefaultCsmItemFactory implements CsmItemFactory {
        public DefaultCsmItemFactory() {
        }

        @Override
        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return new CsmResultItem.NamespaceResultItem(pkg, displayFullNamespacePath, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new CsmResultItem.NamespaceAliasResultItem(alias, displayFullNamespacePath, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.EnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        @Override
        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        @Override
        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.ClassResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.ForwardClassResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.ForwardEnumResultItem createForwardEnumResultItem(CsmEnumForwardDeclaration cls, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.ForwardEnumResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.EnumResultItem(enm, enumDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.TypedefResultItem(def, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN) {
            return createClassResultItem(cls, classDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return createEnumResultItem(enm, enumDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return createTypedefResultItem(def, classDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld) {
            return new CsmResultItem.FieldResultItem(fld, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.MethodResultItem(mtd, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        @Override
        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.ConstructorResultItem(ctr, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        @Override
        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.GlobalFunctionResultItem(fun, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        @Override
        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.GlobalVariableResultItem(var, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.LocalVariableResultItem(var, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.FileLocalVariableResultItem(var, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.FileLocalFunctionResultItem(fun, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        @Override
        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac) {
            return new CsmResultItem.MacroResultItem(mac, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        @Override
        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        @Override
        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var) {
            return createGlobalVariableResultItem(var);
        }

        @Override
        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        @Override
        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes) {
            return createGlobalFunctionResultItem(fun, substituteExp, false, instantiateTypes);
        }

        @Override
        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return createNamespaceResultItem(pkg, displayFullNamespacePath);
        }

        @Override
        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return createNamespaceAliasResultItem(alias, displayFullNamespacePath);
        }

        @Override
        public TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par) {
            return new CsmResultItem.TemplateParameterResultItem(par, FAKE_PRIORITY);
        }

        @Override
        public CsmResultItem createLabelResultItem(CsmLabel csmStatement) {
            return new CsmResultItem.LabelResultItem(csmStatement, FAKE_PRIORITY);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // convert data into CompletionItem
    private static List<CsmResultItem> convertData(Collection<?> dataList, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset,
            CsmOffsetableDeclaration contextElement, boolean instantiateTypes, CsmResultItem.SubstitutionHint hint) {
        List<CsmResultItem> ret = new ArrayList<CsmResultItem>();
        for (Object obj : dataList) {
            if (!isProhibitedResultItem(obj)) {
                CsmResultItem item = createResultItem(obj, classDisplayOffset, substituteExp, contextElement, instantiateTypes);
                assert item != null : "why null item? object " + obj + " iof " + (obj==null?"null":obj.getClass()) + " in expr" + substituteExp;
                if (item != null) {
                    item.setSubstituteOffset(substituteOffset);
                    item.setHint(hint);
                    ret.add(item);
                }
            }
        }
        return ret;
    }

    private static <T extends CsmFunctional> List<T> uninstantiateFunctionDefinitions(Collection<T> functions, Predicate<CsmFunctional> acceptor) {
        List<T> uninstantiated = new ArrayList<T>();
        for (T func : functions) {
            if (CsmKindUtilities.isFunctionDefinition(func)) {
                CsmFunctional inst = tryUninstantiateFunction(func);
                if (acceptor.check(inst)) {
                    uninstantiated.add((T) inst);
                }
            } else {
                uninstantiated.add(func);
            }
        }
        return uninstantiated;
    }

    private static CsmFunctional tryUninstantiateFunction(CsmFunctional function) {
        while (CsmKindUtilities.isInstantiation(function)) {
            CsmOffsetableDeclaration decl = ((CsmInstantiation) function).getTemplateDeclaration();
            if (CsmKindUtilities.isFunctional(decl)) {
                function = (CsmFunctional) decl;
            } else {
                break;
            }
        }
        return function;
    }

    private static boolean isProhibitedResultItem(Object obj) {
        if (obj instanceof CsmObject) {
            CsmObject csmObj = (CsmObject) obj;
            if (CsmKindUtilities.isFunctionPointerClassifier(csmObj)) {
                return true;
            } else if (CsmKindUtilities.isClosureClassifier(csmObj)) {
                return true;
            }
        }
        return false;
    }

    private static CsmResultItem createResultItem(Object obj, int classDisplayOffset, CsmCompletionExpression substituteExp, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
        if (CsmKindUtilities.isCsmObject(obj)) {
            CsmObject csmObj = (CsmObject) obj;
            assert (!CsmKindUtilities.isMethod(csmObj) || CsmKindUtilities.isMethodDeclaration(csmObj)) : "completion result can not have method definitions " + obj;
            if (CsmKindUtilities.isNamespace(csmObj)) {
                return getCsmItemFactory().createNamespaceResultItem((CsmNamespace) csmObj, false);
            } else if (CsmKindUtilities.isEnum(csmObj)) {
                return getCsmItemFactory().createEnumResultItem((CsmEnum) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isEnumerator(csmObj)) {
                return getCsmItemFactory().createGlobalEnumeratorResultItem((CsmEnumerator) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClass(csmObj)) {
                return getCsmItemFactory().createClassResultItem((CsmClass) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClassForwardDeclaration(csmObj)) {
                return getCsmItemFactory().createForwardClassResultItem((CsmClassForwardDeclaration) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isEnumForwardDeclaration(csmObj)) {
                return getCsmItemFactory().createForwardEnumResultItem((CsmEnumForwardDeclaration) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isField(csmObj)) {
                return getCsmItemFactory().createFieldResultItem((CsmField) csmObj);
            } else if (CsmKindUtilities.isConstructor(csmObj)) { // must be checked before isMethod, because constructor is method too
                return getCsmItemFactory().createConstructorResultItem((CsmConstructor) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else if (CsmKindUtilities.isMethodDeclaration(csmObj)) {
                return getCsmItemFactory().createMethodResultItem((CsmMethod) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else if (CsmKindUtilities.isGlobalFunction(csmObj)) {
                if (CsmBaseUtilities.isFileLocalFunction((CsmFunction) csmObj)) {
                    return getCsmItemFactory().createFileLocalFunctionResultItem((CsmFunction) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
                } else {
                    return getCsmItemFactory().createGlobalFunctionResultItem((CsmFunction) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
                }
            } else if (CsmKindUtilities.isGlobalVariable(csmObj)) {
                return getCsmItemFactory().createGlobalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isFileLocalVariable(csmObj)) {
                return getCsmItemFactory().createFileLocalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isLocalVariable(csmObj)) {
                return getCsmItemFactory().createLocalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isMacro(csmObj)) {
                return getCsmItemFactory().createGlobalMacroResultItem((CsmMacro) csmObj);
            } else if (CsmKindUtilities.isTypedef(csmObj)) {
                return getCsmItemFactory().createTypedefResultItem((CsmTypedef) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isTypeAlias(csmObj)) {
                return getCsmItemFactory().createTypedefResultItem((CsmTypeAlias) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isStatement(csmObj)) {
                return getCsmItemFactory().createLabelResultItem((CsmLabel) csmObj);
            } else if (CsmKindUtilities.isNamespaceAlias(csmObj)) {
                return getCsmItemFactory().createNamespaceAliasResultItem((CsmNamespaceAlias) csmObj, false);
            } else if (CsmKindUtilities.isTemplateParameter(csmObj)) {
                return getCsmItemFactory().createTemplateParameterResultItem((CsmTemplateParameter)csmObj);
            } else if(csmObj instanceof CsmTemplateBasedReferencedObject) {
                return new TemplateBasedReferencedObjectResultItem((CsmObject) obj);
            }
        }
        return null;
    }

    private static List<CsmResultItem> convertData(CompletionResolver.Result res, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
        if (res == null) {
            return Collections.<CsmResultItem>emptyList();
        }
        List<CsmResultItem> out = new ArrayList<CsmResultItem>(res.size());
        CsmItemFactory factory = getCsmItemFactory();
        CsmResultItem item;
        for (CsmVariable elem : res.getLocalVariables()) {
            item = factory.createLocalVariableResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmTemplateParameter elem : res.getTemplateparameters()) {
            item = factory.createTemplateParameterResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmField elem : res.getClassFields()) {
            item = factory.createFieldResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmEnumerator elem : res.getClassEnumerators()) {
            item = factory.createMemberEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            out.add(item);
        }

        for (CsmMethod elem : res.getClassMethods()) {
            if (CsmKindUtilities.isConstructor(elem)) {
                item = factory.createConstructorResultItem((CsmConstructor) elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else {
                item = factory.createMethodResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            }
            assert item != null;
            out.add(item);
        }

        for (CsmClassifier elem : res.getProjectClassesifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createClassResultItem((CsmClass) elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClassForwardDeclaration(elem)) {
                CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) elem;
                if (fd.getCsmClass() != null) {
                    item = factory.createClassResultItem(fd.getCsmClass(), classDisplayOffset, false);
                } else {
                    // TODO fix me!
                    continue;
                }
            } else if (CsmKindUtilities.isEnumForwardDeclaration(elem)) {
                CsmEnumForwardDeclaration fd = (CsmEnumForwardDeclaration) elem;
                if (fd.getCsmEnum() != null) {
                    item = factory.createEnumResultItem(fd.getCsmEnum(), classDisplayOffset, false);
                } else {
                    // TODO fix me!
                    continue;
                }
            } else if (CsmKindUtilities.isTypedef(elem) || CsmKindUtilities.isTypeAlias(elem)) {
                item = factory.createTypedefResultItem((CsmTypedef) elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createEnumResultItem((CsmEnum) elem, classDisplayOffset, false);
            }
            assert item != null;
            out.add(item);
        }

        for (CsmVariable elem : res.getFileLocalVars()) {
            item = factory.createFileLocalVariableResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmEnumerator elem : res.getFileLocalEnumerators()) {
            item = factory.createFileLocalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            out.add(item);
        }

        for (CsmMacro elem : res.getFileLocalMacros()) {
            item = factory.createFileLocalMacroResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmFunction elem : res.getFileLocalFunctions()) {
            item = factory.createFileLocalFunctionResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            assert item != null;
            out.add(item);
        }

        for (CsmMacro elem : res.getInFileIncludedProjectMacros()) {
            item = factory.createFileIncludedProjectMacroResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmVariable elem : res.getGlobalVariables()) {
            item = factory.createGlobalVariableResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmEnumerator elem : res.getGlobalEnumerators()) {
            item = factory.createGlobalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            out.add(item);
        }

        for (CsmMacro elem : res.getGlobalProjectMacros()) {
            item = factory.createGlobalMacroResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmFunction elem : res.getGlobalProjectFunctions()) {
            item = factory.createGlobalFunctionResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            assert item != null;
            out.add(item);
        }

        for (CsmNamespace elem : res.getGlobalProjectNamespaces()) {
            item = factory.createNamespaceResultItem(elem, false);
            assert item != null;
            out.add(item);
        }

        for (CsmNamespaceAlias elem : res.getProjectNamespaceAliases()) {
            item = factory.createNamespaceAliasResultItem(elem, false);
            assert item != null;
            out.add(item);
        }

        for (CsmClassifier elem : res.getLibClassifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createLibClassResultItem((CsmClass) elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isTypedef(elem) || CsmKindUtilities.isTypeAlias(elem)) {
                item = factory.createLibTypedefResultItem((CsmTypedef) elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createLibEnumResultItem((CsmEnum) elem, classDisplayOffset, false);
            }
            assert item != null;
            out.add(item);
        }

        for (CsmMacro elem : res.getInFileIncludedLibMacros()) {
            item = factory.createFileIncludedLibMacroResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmMacro elem : res.getLibMacros()) {
            item = factory.createLibMacroResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmVariable elem : res.getLibVariables()) {
            item = factory.createLibGlobalVariableResultItem(elem);
            assert item != null;
            out.add(item);
        }

        for (CsmEnumerator elem : res.getLibEnumerators()) {
            item = factory.createLibGlobalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            out.add(item);
        }

        for (CsmFunction elem : res.getLibFunctions()) {
            item = factory.createLibGlobalFunctionResultItem(elem, substituteExp, instantiateTypes);
            assert item != null;
            out.add(item);
        }

        for (CsmNamespace elem : res.getLibNamespaces()) {
            item = factory.createLibNamespaceResultItem(elem, false);
            assert item != null;
            out.add(item);
        }

        for (CsmNamespaceAlias elem : res.getLibNamespaceAliases()) {
            item = factory.createLibNamespaceAliasResultItem(elem, false);
            assert item != null;
            out.add(item);
        }
        for (CsmResultItem completionItem : out) {
            completionItem.setSubstituteOffset(substituteOffset);
        }
        return out;
    }

    private static boolean isDeclaration(CsmCompletionExpression substituteExp, CsmOffsetableDeclaration scopeElement) {
        int expId = substituteExp.getExpID();
        return scopeElement == null && (expId == CsmCompletionExpression.VARIABLE || expId == CsmCompletionExpression.SCOPE || expId == CsmCompletionExpression.SCOPE_OPEN);
    }

    private static class TemplateBasedReferencedObjectImpl implements CsmTemplateBasedReferencedObject {
        private final CsmType lastType;
        private final CharSequence textAfterType;

        private TemplateBasedReferencedObjectImpl(CsmType lastType, CharSequence textAfterType) {
            this.lastType = lastType;
            assert lastType != null;
            this.textAfterType = textAfterType;
            assert textAfterType != null;
        }

        @Override
        public int getNameStartOffset() {
            return 0;
        }

        @Override
        public int getNameEndOffset() {
            return 0;
        }

        @Override
        public CharSequence getName() {
            return NbBundle.getMessage(CsmCompletionQuery.class, "completion-template-based-object", lastType.getCanonicalText(), textAfterType); // NOI18N
        }

        @Override
        public CsmFile getContainingFile() {
            return lastType.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return lastType.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return lastType.getEndOffset();
        }

        @Override
        public Position getStartPosition() {
            return lastType.getStartPosition();
        }

        @Override
        public Position getEndPosition() {
            return lastType.getEndPosition();
        }

        @Override
        public CharSequence getText() {
            return getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TemplateBasedReferencedObjectImpl other = (TemplateBasedReferencedObjectImpl) obj;
            if (!this.lastType.equals(other.lastType)) {
                return false;
            }
            if (!this.textAfterType.equals(other.textAfterType)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + this.lastType.hashCode();
            hash = 71 * hash + this.textAfterType.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "lastType=" + lastType + ", textAfterType=" + textAfterType; // NOI18N
        }

    }

    private static class TemplateBasedReferencedObjectResultItem extends CsmResultItem {

        TemplateBasedReferencedObjectResultItem(CsmObject obj) {
            super(obj, 0);
        }

        @Override
        public String getItemText() {
            return "TemplateBasedReferencedObjectResultItem for " + getAssociatedObject(); // NOI18N
        }

        @Override
        protected Component getPaintComponent(boolean isSelected) {
            return new CsmPaintComponent() {

                @Override
                protected void draw(Graphics g) {
                }

                @Override
                public String toString() {
                    return "fake TemplateBasedReferencedObjectResultItem paint component"; // NOI18N
                }
            };
        }

        @Override
        public String getStringPresentation() {
            return "TemplateBasedReferencedObjectResultItem for " + getAssociatedObject(); // NOI18N
        }
    }

//
//    static int getPointerDepth(CsmType type) {
//        type = extractLastNestedType(type, new TruePredicate<CsmType>());
//        return type.getPointerDepth();
//    }

    static int getReferenceValue(CsmType type) {
        if (type.isRValueReference()) {
            return 2;
        } else if (type.isReference()) {
            return 1;
        }
        return 0;
    }
}
