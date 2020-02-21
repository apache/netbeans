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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmReturnStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import static org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver3.LOGGER;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * extracted part of Resolver3 which is responsible for gathering file maps.
 * 
 */
public final class FileMapsCollector {
    private final CsmFile currentFile;
    private final CsmFile startFile;
    private final int stopAtOffset;
    private CsmCacheMap filesCollectorCache;
    private boolean collectedDependencies;

    private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDirective*/> usedNamespaces = new LinkedHashMap<>();
    private final Map<CharSequence, CsmObject/*CsmNamespace or CsmNamespaceAlias*/> namespaceAliases = new HashMap<>();
    private final Map<CharSequence, ArrayList<CsmObject/*CsmDeclaration or CsmUsingDeclaration*/>> usingDeclarations = new HashMap<>();

    private final Set<CsmFile> visitedFiles = new HashSet<>();
    
    private static final int INCLUDE_STACK_MARKER = -1;
    private static final CsmSelect.CsmFilter NO_FILTER = CsmSelect.getFilterBuilder().createOffsetFilter(0, Integer.MAX_VALUE);
    private static final CsmSelect.CsmFilter NAMESPACE_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
            CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.NAMESPACE_ALIAS, CsmDeclaration.Kind.USING_DECLARATION, CsmDeclaration.Kind.USING_DIRECTIVE
    );
    private static final CsmSelect.CsmFilter CLASS_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
            CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.NAMESPACE_ALIAS, CsmDeclaration.Kind.USING_DECLARATION, CsmDeclaration.Kind.USING_DIRECTIVE, CsmDeclaration.Kind.TYPEDEF, CsmDeclaration.Kind.TYPEALIAS, CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.STRUCT, CsmDeclaration.Kind.UNION
    );

    public FileMapsCollector(CsmFile file, CsmFile startFile, int stopAtOffset) {
        this.currentFile = file;
        this.startFile = startFile;
        this.stopAtOffset = stopAtOffset;
        this.collectedDependencies = false;
    }

    CsmDeclaration getUsingDeclaration(CharSequence name) {
        final CharSequence s = CharSequences.create(name);
        final ArrayList<CsmObject> list = usingDeclarations.get(s);
        if (list == null) {
            return null;
        }
        ListIterator<CsmObject> listIterator = list.listIterator(list.size());
        while(listIterator.hasPrevious()) {
            CsmObject obj = listIterator.previous();
            if (CsmKindUtilities.isUsingDeclaration(obj)) {
                CsmDeclaration decl = ((CsmUsingDeclaration)obj).getReferencedDeclaration();
                listIterator.set(decl);
                if (decl != null) {
                    return decl;
                }
            } else if (CsmKindUtilities.isDeclaration(obj)) {
                return (CsmDeclaration) obj;
            }
        }
        return null;
    }

    Object getNamespaceAlias(CharSequence name) {
        final CharSequence s = CharSequences.create(name);
        CsmObject obj = namespaceAliases.get(s);
        CsmNamespace ns = null;
        if (CsmKindUtilities.isNamespaceAlias(obj)) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) obj;
            ns = alias.getReferencedNamespace();
            namespaceAliases.put(s, ns);
        } else if (CsmKindUtilities.isNamespace(obj)) {
            ns = (CsmNamespace) obj;
        }
        return ns;
    }

    Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> getUsedNamespaces() {
        // XXX: do not copy yet
        if (false) {
            return new HashMap<>(usedNamespaces);
        }
        return usedNamespaces;
    }

    void rememberResolvedUsing(CharSequence key, CsmNamespace value) {
        // XXX: do not modify if not copied above
        if (false) usedNamespaces.put(key, value);
    }
    
    void initFileMaps(boolean needClassifiers, Callback callback) {
        // when in parsing mode, we do not gather dependencies for
        // probably not yet parsed files
        if (!FileImpl.isFileBeingParsedInCurrentThread(currentFile)) {
            if (!collectedDependencies) {
                this.filesCollectorCache = CsmCacheManager.getClientCache(FileMapsCollector.class, CACHE_INITIALIZER);
                collectedDependencies = true;
                // check which includes need to traverse
                int lastIncludeOffset = 0;
                ArrayList<CsmInclude> incBeforeOffset = new ArrayList<>();
                Iterator<CsmInclude> iter = CsmSelect.getIncludes(currentFile, CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset));
                while (iter.hasNext()) {
                    CsmInclude inc = iter.next();
                    if (inc.getStartOffset() >= stopAtOffset) {
                        break;
                    }
                    incBeforeOffset.add(inc);
                    lastIncludeOffset = inc.getEndOffset();
                }
                MapsCollection out = new MapsCollection(EMPTY_CALLBACK, needClassifiers, visitedFiles, usedNamespaces, namespaceAliases, usingDeclarations);
                FileMapsCacheKey incKey = new FileMapsCacheKey(lastIncludeOffset, this.startFile, this.currentFile, out.needClassifiers());
                if (!findInCache(incKey, out)) {
                    long allTime = System.currentTimeMillis();
                    // gather all visible by this file's include stack
                    List<CsmInclude> includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(this.currentFile);
                    for (CsmInclude inc : includeStack) {
                        CsmFile includedFrom = inc.getContainingFile();
                        int incOffset = inc.getStartOffset();
                        gatherMaps(includedFrom, incOffset, out);
                    }
                    if (Resolver3.LOGGER.isLoggable(Level.FINE)) {
                        Resolver3.LOGGER.log(Level.FINE, "{0}ms initMapsFromIncludeStack for {1}\n\twith start file {2}\n", new Object[]{System.currentTimeMillis() - allTime, currentFile.getAbsolutePath(), this.startFile.getAbsolutePath()});
                    }
                    // gather all visible by #include directives in this file till offset
                    long incTime = System.currentTimeMillis();
                    for (CsmInclude inc : incBeforeOffset) {
                        CsmFile incFile = inc.getIncludeFile();
                        if (incFile != null) {
                            gatherMaps(incFile, Integer.MAX_VALUE, out);
                        }
                    }
                    // cache 
                    if (Resolver3.LOGGER.isLoggable(Level.FINE)) {
                        Resolver3.LOGGER.log(Level.FINE, "{0}ms initMapsFromIncludes for {1}\n\twith start file {2}\n", new Object[]{System.currentTimeMillis() - incTime, currentFile.getAbsolutePath(), this.startFile.getAbsolutePath()});
                    }
                    allTime = System.currentTimeMillis() - allTime;
                    if (filesCollectorCache != null) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "KEEP INCLUDE STACK {0}=>{1} ({2}) Took {3}ms\n", new Object[]{startFile, currentFile, out.needClassifiers(), allTime});
                        }
                        filesCollectorCache.put(incKey, new FileMapsCacheValue(out, allTime));
                    }                    
                }
            }
        }
        initMapsFromCurrentFileOnly(needClassifiers, stopAtOffset, callback);
    }

    void initMapsFromCurrentFileOnly(boolean needClassifiers, int stopAtOffset, Callback callback) {
        assert stopAtOffset != Integer.MAX_VALUE;
        CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset);
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(currentFile, filter);
        MapsCollection out = new MapsCollection(callback, needClassifiers, visitedFiles, usedNamespaces, namespaceAliases, usingDeclarations);
        gatherMaps(declarations, false, stopAtOffset, out);
    }
    
    private boolean findInCache(FileMapsCacheKey cacheKey, MapsCollection maps) {
        FileMapsCacheValue cacheValue = null;
        if (filesCollectorCache != null) {
            cacheValue = (FileMapsCacheValue) filesCollectorCache.get(cacheKey);
        }
        if (cacheValue != null) {
            cacheValue.copyTo(maps);
            cacheValue.hits++;
            String kind = (cacheKey.lastSearchedIncudeOffset == INCLUDE_STACK_MARKER) ? "STACK" : "INCLUDE";// NOI18N
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "HIT {4} {0}=>{1} ({2}) Hits {3}\n", new Object[]{startFile, currentFile, maps.needClassifiers(), cacheValue.hits, kind});
            }
            return true;
        }        
        return false;
    }

    interface Callback {

        boolean needToTraverseDeeper(CsmScope scope);

        void onVisibleClassifier(CsmClassifier cls);
    }

    private static final Callback EMPTY_CALLBACK = new FileMapsCollector.Callback() {

        @Override
        public boolean needToTraverseDeeper(CsmScope scope) {
            if (CsmKindUtilities.isNamespace(scope) || CsmKindUtilities.isNamespaceDefinition(scope)) {
                return ((CsmNamedElement)scope).getName().length() == 0;
            }
            return false;
        }

        @Override
        public void onVisibleClassifier(CsmClassifier cls) {
            
        }
    };
    
    private final static class MapsCollection {

        final boolean needClassifiers;
        private final Set<CsmFile> antiLoop;
        private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDirective*/> usedNamespaces;
        private final Map<CharSequence, CsmObject/*CsmNamespace or CsmNamespaceAlias*/> namespaceAliases;
        private final Map<CharSequence, ArrayList<CsmObject/*CsmDeclaration or CsmUsingDeclaration*/>> usingDeclarations;
        private final Callback callback;

        public MapsCollection(Callback cb, boolean needClassifiers,
                Set<CsmFile> antiLoop,
                Map<CharSequence, CsmObject> usedNamespaces,
                Map<CharSequence, CsmObject> namespaceAliases,
                Map<CharSequence, ArrayList<CsmObject>> usingDeclarations) {
            this.callback = cb;
            this.needClassifiers = needClassifiers;
            this.antiLoop = antiLoop;
            this.usedNamespaces = usedNamespaces;
            this.namespaceAliases = namespaceAliases;
            this.usingDeclarations = usingDeclarations;
        }

        public boolean needClassifiers() {
            return needClassifiers;
        }

        public boolean needToTraverseDeeper(CsmScope scope) {
            return callback.needToTraverseDeeper(scope);
        }

        public void onVisibleClassifier(CsmClassifier cls) {
            callback.onVisibleClassifier(cls);
        }
    }

    private static void gatherMaps(CsmFile file, int stopAtOffset, MapsCollection out) {
        if (file == null  || !file.isValid() || out.antiLoop.contains(file)) {
            return;
        }
        out.antiLoop.add(file);
        CsmSelect.CsmFilter filter;
        if (stopAtOffset == Integer.MAX_VALUE) {
            filter = NO_FILTER;
        } else {
            filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset);
        }
        // gather file's #include maps
        Iterator<CsmInclude> iter = CsmSelect.getIncludes(file, filter);
        while (iter.hasNext()) {
            CsmInclude inc = iter.next();
            if (inc.getStartOffset() >= stopAtOffset) {
                break;
            }
            CsmFile incFile = inc.getIncludeFile();
            if (incFile != null) {
                gatherMaps(incFile, Integer.MAX_VALUE, out);
            }
        }
        if (stopAtOffset == Integer.MAX_VALUE) {
            if (out.needClassifiers()) {
                filter = CLASS_FILTER;
            } else {
                filter = NAMESPACE_FILTER;
            }
        }
        // gather own maps up to stop offset
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
        gatherMaps(declarations, false, stopAtOffset, out);
    }

    private static void gatherMaps(Iterator<? extends CsmObject> it, boolean inLocalContext, int stopAtOffset, MapsCollection out) {
        while (it.hasNext()) {
            CsmObject o = it.next();
            assert o == null || CsmKindUtilities.isOffsetable(o) : "non CsmOffsetable" + o;
            if (o == null) {
                if (FileImpl.reportErrors) {
                    // FIXUP: do not crush on NPE
                    DiagnosticExceptoins.register(new NullPointerException("Unexpected NULL element in declarations collection")); // NOI18N
                }
                continue;
            }
            int start = ((CsmOffsetable) o).getStartOffset();
            int end = ((CsmOffsetable) o).getEndOffset();
            if (start >= stopAtOffset) {
                break;
            }
            if (CsmKindUtilities.isScopeElement(o)) {
                if (!inLocalContext && CsmKindUtilities.isFunctionDefinition(o)) {
                    if (end >= stopAtOffset) {
                        gatherMaps((CsmScopeElement) o, end, true, stopAtOffset, out);
                    }
                } else {
                    gatherMaps((CsmScopeElement) o, end, inLocalContext, stopAtOffset, out);
                }
            } else {
                if (FileImpl.reportErrors) {
                    System.err.println("Expected CsmScopeElement, got " + o);
                }
            }
        }
    }

//    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd, MapsCollection out) {
//        CsmFilter filter =  CsmSelect.getFilterBuilder().createKindFilter(
//                                  CsmDeclaration.Kind.NAMESPACE_DEFINITION,
//                                  CsmDeclaration.Kind.TYPEDEF,
//                                  CsmDeclaration.Kind.TYPEALIAS);
//        for (Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(nsd, filter); iter.hasNext();) {
//            CsmOffsetableDeclaration decl = iter.next();
//            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
//                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl, out);
//            } else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF || decl.getKind() == CsmDeclaration.Kind.TYPEALIAS ) {
//                CsmTypedef typedef = (CsmTypedef) decl;
//                out.onVisibleClassifier(typedef);
//            }
//        }
//    }
//
//    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd, MapsCollection out) {
//        if( CharSequences.comparator().compare(nsd.getName(),currName())==0 )  {
//            currNamIdx++;
//            doProcessTypedefsInUpperNamespaces(nsd, out);
//        } else {
//            CsmNamespace cns = context.getContainingNamespace();
//            if( cns != null ) {
//                if( cns.equals(nsd.getNamespace())) {
//                    doProcessTypedefsInUpperNamespaces(nsd, out);
//                }
//            }
//        }
//    }
    /**
     * It is guaranteed that element.getStartOffset < this.offset
     */
    private static void gatherMaps(CsmScopeElement element, int endOfScopeElement, boolean inLocalContext, int stopAtOffset, MapsCollection out) {

        CsmDeclaration.Kind kind = CsmKindUtilities.isDeclaration(element) ? ((CsmDeclaration) element).getKind() : null;
        if (kind != null) {
            switch (kind) {
                case NAMESPACE_DEFINITION: {
                    CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
                    if (nsd.getName().length() == 0) {
                        // this is unnamed namespace and it should be considered as
                        // it declares using itself
                        out.usedNamespaces.put(nsd.getQualifiedName(), nsd.getNamespace());
                    }
                    if (stopAtOffset < endOfScopeElement || out.needToTraverseDeeper(nsd)) {
                        //currentNamespace = nsd.getNamespace();
                        if (nsd.getNamespace() instanceof NamespaceImpl) {
                            NamespaceImpl namespace = (NamespaceImpl) nsd.getNamespace();
                            for (CsmUID<CsmUsingDirective> directiveUID : namespace.getUsingDirectives()) {
                                CsmUsingDirective directive = directiveUID.getObject();
                                if (directive != null && directive.getContainingFile() != null) {
                                    int stopAtOffsetDirective = directive.getContainingFile().equals(nsd.getContainingFile()) ? stopAtOffset : Integer.MAX_VALUE;
                                    gatherMaps(directive, directive.getEndOffset(), inLocalContext, stopAtOffsetDirective, out);
                                }
                            }
                            // TODO: this could be rewritten in the way as it is done with using directives to get better performance
                            Iterator<CsmOffsetableDeclaration> udecls = CsmSelect.getDeclarations(namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
                            while (udecls.hasNext()) {
                                final CsmUsingDeclaration usingDecl = (CsmUsingDeclaration) udecls.next();
                                int stopAtOffsetDecl = usingDecl.getContainingFile().equals(nsd.getContainingFile()) ? stopAtOffset : Integer.MAX_VALUE;
                                gatherMaps(usingDecl, usingDecl.getEndOffset(), inLocalContext, stopAtOffsetDecl, out);
                            }
                        } else {
                            Resolver3.LOGGER.log(Level.WARNING, "Unexpected implementation of logical namespace: {0}", nsd.getNamespace().getClass()); //NOI18N
                        }
                        gatherMaps(nsd.getDeclarations().iterator(), inLocalContext, stopAtOffset, out);
                    } else if (out.needClassifiers()) {
                        // VV: removed this phase
//                        processTypedefsInUpperNamespaces(nsd, out);
                    }
                    return;
                }
                case NAMESPACE_ALIAS: {
                    // don't want using to find itself
                    if (stopAtOffset > endOfScopeElement) {
                        CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
                        out.namespaceAliases.put(alias.getAlias(), (CsmNamespaceAlias) element);//alias.getReferencedNamespace());
                    }
                    return;
                }
                case USING_DECLARATION: {
                    // don't want using to find itself
                    if (stopAtOffset > endOfScopeElement) {
                        final CsmUsingDeclaration usingDecl = (CsmUsingDeclaration) element;
                        CharSequence name = usingDecl.getName();
                        int lastIndex = CharSequenceUtils.lastIndexOf(name, "::");//NOI18N
                        if (lastIndex >= 0) {
                            name = name.subSequence(lastIndex+2, name.length());
                        }
                        name = CharSequences.create(name);
                        ArrayList<CsmObject> list = out.usingDeclarations.get(name);
                        if (list == null) {
                            list = new ArrayList<>();
                            out.usingDeclarations.put(name, list);
                        }
                        // do not add using several times
                        // it could be the case when outer container has several copies
                        // with same offsets but different names (i.e. macro-based name)
                        // we don't want to have infinite loop 
                        if (!list.contains(usingDecl)) {
                            list.add(usingDecl);
                        }
//                    CsmDeclaration decl = (usingDecl).getReferencedDeclaration();
//                    if (decl != null) {
//                        CharSequence id;
//                        if (decl.getKind() == CsmDeclaration.Kind.FUNCTION
//                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION
//                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA
//                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND
//                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
//                            // TODO: decide how to resolve functions
//                            id = ((CsmFunction) decl).getSignature();
//                        } else {
//                            id = decl.getName();
//                        }
//                        out.usingDeclarations.put(id, decl);
//                    }
                    }
                    return;
                }
                case USING_DIRECTIVE: {
                    // don't want using to find itself
                    if (stopAtOffset > endOfScopeElement) {
                        CsmUsingDirective udir = (CsmUsingDirective) element;
                        CharSequence name = udir.getName();
                        if (!out.usedNamespaces.containsKey(name)) {
                            out.usedNamespaces.put(name, udir); // getReferencedNamespace()
                        }
                    }
                    return;
                }
                case TYPEALIAS:
                case TYPEDEF: {
                    CsmTypedef typedef = (CsmTypedef) element;
                    // don't want typedef to find itself
                    if (stopAtOffset > endOfScopeElement) {
                        out.onVisibleClassifier(typedef);
                    }
                    return;
                }
            }
        }
        if (CsmKindUtilities.isDeclarationStatement(element)) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if (ds.getStartOffset() < stopAtOffset) {
                gatherMaps(((CsmDeclarationStatement) element).getDeclarators().iterator(), inLocalContext, stopAtOffset, out);
            }
        } else if (CsmKindUtilities.isScope(element)) {
            if (inLocalContext && out.needClassifiers() && CsmKindUtilities.isClassifier(element)) {
                // don't want forward to find itself
                if (!CsmKindUtilities.isClassForwardDeclaration(element) || (stopAtOffset > endOfScopeElement)) {
                    out.onVisibleClassifier((CsmClassifier) element);
                }
            }
            if (stopAtOffset < endOfScopeElement || out.needToTraverseDeeper((CsmScope) element)) {
                gatherMaps(((CsmScope) element).getScopeElements().iterator(), inLocalContext, stopAtOffset, out);
            }
        } else if (CsmKindUtilities.isVariable(element)) {
            gatherMaps(((CsmVariable) element).getInitialValue(), inLocalContext, stopAtOffset, out);            
        } else if (CsmKindUtilities.isReturnStatement(element)) {
            gatherMaps(((CsmReturnStatement) element).getReturnExpression(), inLocalContext, stopAtOffset, out);
        }
    }
    
    private static void gatherMaps(CsmExpression expr, boolean inLocalContext, int stopAtOffset, MapsCollection out) {
        if (expr != null && expr.getLambdas() != null) {
            for (CsmStatement lambdaStmt : expr.getLambdas()) {
                assert CsmKindUtilities.isDeclarationStatement(lambdaStmt) : "Found lamda statement of type: " + lambdaStmt.getClass(); // NOI18N
                CsmDeclarationStatement ds = (CsmDeclarationStatement) lambdaStmt;
                if (ds.getStartOffset() < stopAtOffset) {
                    gatherMaps(ds.getDeclarators().iterator(), inLocalContext, stopAtOffset, out);
                }
            }
        }        
    }
    
    private static final class FileMapsCacheKey {

        private final CsmFile startFile;
        private final CsmFile file;
        private final boolean needClassifiers;
        private final int lastSearchedIncudeOffset;
        private int hashCode = 0;

        public FileMapsCacheKey(int lastSearchedIncudeOffset, CsmFile startFile, CsmFile file, boolean needClassifiers) {
            this.startFile = startFile;
            this.file = file;
            this.lastSearchedIncudeOffset = lastSearchedIncudeOffset;
            this.needClassifiers = needClassifiers;
        }

        @Override
        public String toString() {
            return "FileMapsCacheKey{file=" + file + "startFile=" + startFile + ", needClassifiers=" + needClassifiers + ", hashCode=" + hashCode + '}';// NOI18N
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash = 41 * hash + this.lastSearchedIncudeOffset;
                hash = 41 * hash + Objects.hashCode(this.startFile);
                hash = 41 * hash + Objects.hashCode(this.file);
                hash = 41 * hash + (this.needClassifiers ? 0 : 1);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileMapsCacheKey other = (FileMapsCacheKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            if (this.lastSearchedIncudeOffset != other.lastSearchedIncudeOffset) {
                return false;
            }
            if (this.needClassifiers != other.needClassifiers) {
                return false;
            }
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (!Objects.equals(this.startFile, other.startFile)) {
                return false;
            }
            return true;
        }
    }

    private static final class FileMapsCacheValue implements CsmCacheMap.TraceValue {

        private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> usedNamespaces;
        private final Map<CharSequence, CsmObject/*CsmNamespace or CsmNamespaceAlias*/> namespaceAliases;
        private final Map<CharSequence, ArrayList<CsmObject/*CsmDeclaration or CsmUsingDeclaration*/>> usingDeclarations;
        private final Set<CsmFile> antiLoop;

        final long resolveTime;
        int hits = 0;

        public FileMapsCacheValue(MapsCollection out, long resolveTime) {
            this.antiLoop = new HashSet<>(out.antiLoop);
            this.usedNamespaces = new LinkedHashMap<>(out.usedNamespaces);
            this.namespaceAliases = new HashMap<>(out.namespaceAliases);
            this.usingDeclarations = new HashMap<>(out.usingDeclarations);
            this.resolveTime = resolveTime;
        }

        public void copyTo(MapsCollection out) {
            out.antiLoop.addAll(this.antiLoop);
            out.usedNamespaces.putAll(this.usedNamespaces);
            out.namespaceAliases.putAll(this.namespaceAliases);
            out.usingDeclarations.putAll(this.usingDeclarations);
        }
        
        @Override
        public String toString() {
            String saved = "";
            if (hits > 0 && resolveTime > 0) {
                saved = ", saved=" + (hits * resolveTime) + "ms";// NOI18N
            }
            return "HITS=" + hits + ", resolveTime=" + resolveTime + saved; // NOI18N
        }

        @Override
        public Object getResult() {
            return this;
        }

        @Override
        public int onCacheHit() {
            return ++hits;
        }

        @Override
        public int getHitsCount() {
            return hits;
        }

        @Override
        public long getCalculationTime() {
            return resolveTime;
        }
    }

    private static final Callable<CsmCacheMap> CACHE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("FileMaps Cache"); // NOI18N
        }

    };    
}
