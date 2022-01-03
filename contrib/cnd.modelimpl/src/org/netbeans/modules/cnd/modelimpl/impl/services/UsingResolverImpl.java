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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * implementation of using directives and using declarations resolver
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmUsingResolver.class)
public final class UsingResolverImpl extends CsmUsingResolver implements CsmProgressListener {
    
    public UsingResolverImpl() {
        if (cache) {
            CsmListeners.getDefault().addProgressListener(this);
        }
    }
    
    @Override
    public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getUsedDeclarations();
    }
    
    @Override
    public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace) {
        List<CsmUsingDeclaration> res = new ArrayList<>();
        Iterator<CsmOffsetableDeclaration> udecls = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
        while (udecls.hasNext()) {
            res.add((CsmUsingDeclaration) udecls.next());
        }
        // Let's also look for similarly named namespace in libraries,
        // like it's done in CsmProjectContentResolver.getNamespaceMembers()
        if (!namespace.isGlobal()) {
            for(CsmProject lib : namespace.getProject().getLibraries()){
                CsmNamespace ns = lib.findNamespace(namespace.getQualifiedName());
                if (ns != null) {
                    Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(
                            ns, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
                    while (it.hasNext()) {
                        res.add((CsmUsingDeclaration) it.next());
                    }
                }
            }
        }
        return extractDeclarations(res);
    }
    
    @Override
    public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace, CharSequence name) {
        List<CsmUsingDeclaration> res = new ArrayList<>();
        Iterator<CsmOffsetableDeclaration> udecls = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
        while (udecls.hasNext()) {
            final CsmUsingDeclaration usindDecl = (CsmUsingDeclaration) udecls.next();
            CharSequence n = usindDecl.getName();
            int lastIndex = CharSequenceUtils.lastIndexOf(n, "::"); //NOI18N
            if (lastIndex >= 0) {
                if (CharSequences.comparator().compare(name, n.subSequence(lastIndex+2, n.length())) == 0) {
                    res.add(usindDecl);
                }
            } else if (CharSequences.comparator().compare(name, n) == 0) {
                res.add(usindDecl);
            }
        }
        // Let's also look for similarly named namespace in libraries,
        // like it's done in CsmProjectContentResolver.getNamespaceMembers()
        if (!namespace.isGlobal()) {
            for(CsmProject lib : namespace.getProject().getLibraries()){
                CsmNamespace ns = lib.findNamespace(namespace.getQualifiedName());
                if (ns != null) {
                    Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(
                            ns, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
                    while (it.hasNext()) {
                        final CsmUsingDeclaration usindDecl = (CsmUsingDeclaration) it.next();
                        CharSequence n = usindDecl.getName();
                        int lastIndex = CharSequenceUtils.lastIndexOf(n, "::"); //NOI18N
                        if (lastIndex >= 0) {
                            if (CharSequences.comparator().compare(name, n.subSequence(lastIndex+2, n.length())) == 0) {
                                res.add(usindDecl);
                            }
                        } else if (CharSequences.comparator().compare(name, n) == 0) {
                            res.add(usindDecl);
                        }
                    }
                }
            }
        }
        return extractDeclarations(res);
    }
    
    @Override
    public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
        Set<CsmNamespace> seen = new LinkedHashSet<>();
        Queue<CsmNamespace> queue = new LinkedList<>(
                getCollector(file, offset, onlyInProject).getVisibleNamespaces());
        findVisibleNamespacesBfs(seen, queue, onlyInProject, file.getProject());
        return seen;
    }

    private void findVisibleNamespacesBfs(Set<CsmNamespace> seen, Queue<CsmNamespace> queue, CsmProject onlyInProject, CsmProject startProject) {
        // breadth-first search in namespace inclusion graph
        while (!queue.isEmpty()) {
            CsmNamespace namespace = queue.poll();
            for (CsmNamespace used : findVisibleNamespaces(namespace, startProject)) {
                if (!seen.contains(used) && !queue.contains(used) &&
                        (onlyInProject == null || onlyInProject == used.getProject())) {
                    queue.add(used);
                }
            }
            seen.add(namespace);
        }
    }

//    public Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject) {
//        return getCollector(file, offset, onlyInProject).getDirectVisibleNamespaceDefinitions();
//    }
    
    @Override
    public Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace) {
        List<CsmUsingDirective> res = new ArrayList<>();
        Iterator<CsmOffsetableDeclaration> udirs = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DIRECTIVE));
        while (udirs.hasNext()) {
            res.add((CsmUsingDirective)udirs.next());
        }
        return res;
    }

    @Override
    public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getNamespaceAliases();
    }

    @Override
    public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmNamespace namespace) {
        List<CsmNamespaceAlias> res = new ArrayList<>();
        Iterator<CsmOffsetableDeclaration> udirs = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_ALIAS));
        while (udirs.hasNext()) {
            res.add((CsmNamespaceAlias)udirs.next());
        }
        return res;
    }

    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmDeclaration> extractDeclarations(Collection<CsmUsingDeclaration> decls) {
        // TODO check the correctness of order
        LinkedHashMap<CharSequence, CsmDeclaration> out = new LinkedHashMap<>(decls.size());
        for (CsmUsingDeclaration decl : decls) {
            CsmDeclaration ref = null;
            ref = decl.getReferencedDeclaration();
            if (ref != null) {
                CharSequence name = decl.getName();
                // remove previous inclusion
                out.remove(name);
                out.put(name, ref);
            }
        }
        return new ArrayList<>(out.values());
    }


    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmNamespace> extractNamespaces(Collection<CsmUsingDirective> decls, CsmProject startPrj) {
        // TODO check the correctness of order
        Collection<Pair> namespaces = new LinkedHashSet<>();
        for (CsmUsingDirective decl : decls) {
            CsmNamespace ref = decl.getReferencedNamespace();
            if (ref != null) {
                CsmFile file = decl.getContainingFile();
                if (file != null) {
                    CsmProject proj = file.getProject();
                    if (proj != null) {
                        Pair p = new Pair(ref, proj);
                        namespaces.remove(p);
                        namespaces.add(p);
                    }
                }
            }
        }
        Collection<CsmNamespace> out = new LinkedHashSet<>();
        Collection<CsmProject> libraries = startPrj.getLibraries();
        for (Pair p : namespaces) {
            for (CsmNamespace ns : findNamespacesInProject(p.proj, p.fqn, libraries)) {
                out.remove(ns);
                out.add(ns);
            }
        }
        return out;
    }

    /**
     * Finds all namespaces visible in given namespace (unnamed, inlined or through "using" directives).
     *
     * @param namespace  namespace of interest
     * @return unmodifiable collection of namespaces visible in given namespace though "using" directives
     */
    @Override
    public Collection<CsmNamespace> findVisibleNamespaces(CsmNamespace namespace, CsmProject startPrj) {
        List<CsmNamespace> res = new ArrayList<>();
        if (!namespace.isGlobal()) {
            for (CsmNamespace ns : namespace.getNestedNamespaces()) {
                if (ns.getName().length() == 0 || ns.isInline()) {
                    res.add(ns);
                }
            }
        }
        res.addAll(extractNamespaces(findUsingDirectives(namespace), startPrj));
        return res;
    }
    
    /**
     * Finds namespace in project and it's libraries
     *
     * @param project - project
     * @param namespaceQualifiedName - namespace name
     * @return collection of namespaces
     */
    private static Collection<CsmNamespace> findNamespacesInProject(CsmProject project, CharSequence namespaceQualifiedName, Collection<CsmProject> libs) {
        HashSet<CsmProject> scannedProjects = new HashSet<>();
        Collection<CsmNamespace> out = new ArrayList<>();
        CsmNamespace namespace = project.findNamespace(namespaceQualifiedName);
        if (namespace != null) {
            out.add(namespace);
        }
        scannedProjects.add(project);
        out.addAll(findNamespacesInProjects(libs, namespaceQualifiedName, scannedProjects));
        return out;
    }

    /**
     * Finds namespace in projects and libraries
     *
     * @param project - project
     * @param namespaceQualifiedName - namespace name
     * @param scannedProjects - set of already scanned projects
     * @return collection of namespaces
     */
    private static Collection<CsmNamespace> findNamespacesInProjects(Collection<CsmProject> projects, CharSequence namespaceQualifiedName, HashSet<CsmProject> scannedProjects) {
        Collection<CsmNamespace> out = new ArrayList<>();
        for (CsmProject proj : projects) {
            if (!scannedProjects.contains(proj)) {
                CsmNamespace namespace = proj.findNamespace(namespaceQualifiedName);
                if (namespace != null) {
                    out.add(namespace);
                }
                scannedProjects.add(proj);
                Collection<CsmProject> libs = proj.getLibraries();
                if (!libs.isEmpty()) {
                    out.addAll(findNamespacesInProjects(libs, namespaceQualifiedName, scannedProjects));
                }
            }
        }
        return out;
    }

    private static class Pair {

        private final CharSequence fqn;
        private CsmProject proj;

        private Pair(CsmNamespace ref, CsmProject proj) {
            this.fqn = ref.getQualifiedName();
            this.proj = proj;
        }

        @Override
        public int hashCode() {
            return fqn.hashCode() + proj.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair p = (Pair) obj;
                return fqn.equals(p.fqn) && proj.equals(p.proj);
            }
            return false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // try to cache a little the last request
    
    private static final class Lock {}
    private final Object lock = new Lock();
    private Set<Reference<SearchInfo>> allThreadsCache = new HashSet<>();
    private ThreadLocal<Reference<SearchInfo>> lastSearch = new ThreadLocal<Reference<SearchInfo>>() {

        @Override
        protected Reference<SearchInfo> initialValue() {
            return new SoftReference<>(null);
        }
        
    };
    
    private final ThreadLocal<Reference<SearchInfo>> lastSearchInProject = new ThreadLocal<Reference<SearchInfo>>() {

        @Override
        protected Reference<SearchInfo> initialValue() {
            return new SoftReference<>(null);
        }
    };
    
    private final boolean cache = true;
    private FileElementsCollector getCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        if (!cache) {
            return new FileElementsCollector(file, offset, onlyInProject);
        } else {
            synchronized (lock) {
                Reference<SearchInfo> ref;
                if (onlyInProject == null) {
                    ref = lastSearch.get();
                } else {
                    ref = lastSearchInProject.get();
                }
                SearchInfo search = null;
                if (ref != null) {
                    search = ref.get();
                }
                if (search == null || !search.valid(file, offset, onlyInProject)) {
                    if (ref != null) {
                        allThreadsCache.remove(ref);
                    }
                    FileElementsCollector collector = new FileElementsCollector(file, offset, onlyInProject);
                    search = new SearchInfo(file, onlyInProject, collector);
                    ref = new SoftReference<>(search);
                    allThreadsCache.add(ref);
                    if (onlyInProject == null) {
                        lastSearch.set(ref);
                    } else {
                        lastSearchInProject.set(ref);
                    }
                } else {
                    search.collector.incrementOffset(offset);
                }
                assert search != null;
                assert search.collector != null;
                return search.collector;
            }
        }
    }
    
    private static final class SearchInfo {
        private final CsmFile file;
        private final FileElementsCollector collector;
        private final CsmProject onlyInProject;
        public SearchInfo(CsmFile file, CsmProject onlyInProject, FileElementsCollector collector) {
            this.file = file;
            this.collector = collector;
            this.onlyInProject = onlyInProject;
        }
        
        private boolean valid(CsmFile file, int offset, CsmProject onlyInProject) {
            return this.file.equals(file) && collector.getReturnPoint() <= offset && this.onlyInProject == onlyInProject;
        }
    }
    
    @Override
    public void projectParsingStarted(CsmProject project) {
    }
    
    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }
    
    @Override
    public void projectParsingFinished(CsmProject project) {
        cleanCache();
    }
    
    @Override
    public void projectParsingCancelled(CsmProject project) {
    }
    
    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }
    
    @Override
    public void fileParsingFinished(CsmFile file) {
        cleanCache();
    }
    
    @Override
    public void projectLoaded(CsmProject project) {
    }
    
    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }
    
    private void cleanCache() {
        synchronized (lock) {
            for(Reference<SearchInfo> ref : allThreadsCache) {
                ref.clear();
            }
        }
    }
}
