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

package org.netbeans.modules.cnd.api.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmSelectProvider;
import org.openide.util.Lookup;

/**
 *
 */
public class CsmSelect {
    private static final Logger LOG = Logger.getLogger(CsmSelect.class.getSimpleName());
    
    private static CsmSelectProvider DEFAULT = new Default();
    public static final CsmFilter FUNCTION_KIND_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
            CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION, CsmDeclaration.Kind.FUNCTION_LAMBDA,
            CsmDeclaration.Kind.FUNCTION_FRIEND,CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
    public static final CsmFilter CLASSIFIER_KIND_FILTER = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.STRUCT,
                                                    CsmDeclaration.Kind.UNION, CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.TYPEDEF, CsmDeclaration.Kind.TYPEALIAS);
    
    public static CsmFilterBuilder getFilterBuilder() {
        return getDefault().getFilterBuilder();
    }

    public static Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
        return getDefault().getMacros(file, filter);
    }

    public static Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
        return getDefault().getIncludes(file, filter);
    }

    public static boolean hasDeclarations(CsmFile file) {
        return getDefault().hasDeclarations(file);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
        return getDefault().getDeclarations(file, filter);
    }

    public static Iterator<CsmOffsetableDeclaration> getExternalDeclarations(CsmFile file) {
        return getDefault().getExternalDeclarations(file);
    }

    public static Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter)  {
        return getDefault().getStaticVariables(file, filter);
    }

    public static Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter)  {
        return getDefault().getStaticFunctions(file, filter);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter)  {
        return getDefault().getDeclarations(namespace, filter);
    }

    public static Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter)  {
        return getDefault().getDeclarations(namespace, filter);
    }

    public static Iterator<CsmScopeElement> getScopeDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter)  {
        return getDefault().getScopeDeclarations(namespace, filter);
    }

    public static Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter)  {
        long time = System.currentTimeMillis();
        try {
            Iterator<CsmMember> out;
            CsmCacheMap cache = CsmCacheManager.getClientCache(ClassMembersKey.class, SELECT_INITIALIZER);
            Object key = new ClassMembersKey(cls, filter);
            IteratorWrapper<CsmMember> wrap = (IteratorWrapper<CsmMember>) CsmCacheMap.getFromCache(cache, key, null);
            if (wrap == null) {
                time = System.currentTimeMillis();
                Iterator<CsmMember> orig = getDefault().getClassMembers(cls, filter);
                time = System.currentTimeMillis() - time;                
                if (cache != null) {
                    wrap = new IteratorWrapper<CsmMember>(orig);
                    cache.put(key, CsmCacheMap.toValue(wrap, time));
                    out = wrap.iterator();
                } else {
                    out = orig;
                }
            } else {
                out = wrap.iterator();
                time = System.currentTimeMillis() - time;
            }
            return out;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getClassMembers took {0}ms:\n\tcls={1}\n\tfilter={2}\n", new Object[]{time, getPosition(cls), filter});
            }
        }
    }
    
    public static Iterator<CsmEnumerator> getEnumerators(CsmEnum en, CsmFilter filter)  {
        long time = System.currentTimeMillis();
        try {
            Iterator<CsmEnumerator> out;
            CsmCacheMap cache = CsmCacheManager.getClientCache(ClassMembersKey.class, SELECT_INITIALIZER);
            Object key = new EnumeratorsKey(en, filter);
            IteratorWrapper<CsmEnumerator> wrap = (IteratorWrapper<CsmEnumerator>) CsmCacheMap.getFromCache(cache, key, null);
            if (wrap == null) {
                time = System.currentTimeMillis();
                Iterator<CsmEnumerator> orig = getDefault().getEnumerators(en, filter);
                time = System.currentTimeMillis() - time;                
                if (cache != null) {
                    wrap = new IteratorWrapper<CsmEnumerator>(orig);
                    cache.put(key, CsmCacheMap.toValue(wrap, time));
                    out = wrap.iterator();
                } else {
                    out = orig;
                }
            } else {
                out = wrap.iterator();
                time = System.currentTimeMillis() - time;
            }
            return out;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getEnumerators took {0}ms:\n\tcls={1}\n\tfilter={2}\n", new Object[]{time, getPosition(en), filter});
            }
        }
    }

    public static Iterator<CsmFriend> getClassFrirends(CsmClass cls, CsmFilter filter)  {
        long time = System.currentTimeMillis();
        try {
            Iterator<CsmFriend> out;
            CsmCacheMap cache = CsmCacheManager.getClientCache(ClassFriendsKey.class, SELECT_INITIALIZER);
            Object key = new ClassFriendsKey(cls, filter);
            IteratorWrapper<CsmFriend> wrap = (IteratorWrapper<CsmFriend>) CsmCacheMap.getFromCache(cache, key, null);
            if (wrap == null) {
                time = System.currentTimeMillis();
                Iterator<CsmFriend> orig = getDefault().getClassFriends(cls, filter);
                time = System.currentTimeMillis() - time;                
                if (cache != null) {
                    wrap = new IteratorWrapper<CsmFriend>(orig);
                    cache.put(key, CsmCacheMap.toValue(wrap, time));
                    out = wrap.iterator();
                } else {
                    out = orig;
                }
            } else {
                out = wrap.iterator();
                time = System.currentTimeMillis() - time;
            }
            return out;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getClassFrirends took {0}ms:\n\tcls={1}\n\tfilter={2}\n", new Object[]{time, getPosition(cls), filter});
            }
        }
    }
    
    public static Iterator<CsmFunction> getFunctions(CsmProject project, CharSequence qualifiedName) {
        // ensure that qName does NOT start with "::"
        if (qualifiedName.length() > 1 && qualifiedName.charAt(0) == ':' && qualifiedName.charAt(1) == ':') {
            qualifiedName = qualifiedName.subSequence(2, qualifiedName.length());
        }
        Collection<CsmFunction> result = new ArrayList<CsmFunction>();
	getFunctions(project, qualifiedName, result, new LinkedHashSet<CsmProject>());
	return result.iterator();
    }

    // NB: qName does NOT start with "::"
    private static void getFunctions(CsmProject project, CharSequence qName,
            Collection<CsmFunction> result, Collection<CsmProject> processedProjects) {
        if (!processedProjects.contains(project)) {
            processedProjects.add(project);
            // find last "::" in Name
            int pos = findLastScopeDelimeterPos(qName);
            if (pos == -1) {
                // qName resides in global namespace
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         FUNCTION_KIND_FILTER,
                         CsmSelect.getFilterBuilder().createNameFilter(qName, true, true, false));
                getFunctions(CsmSelect.getDeclarations(project.getGlobalNamespace(), filter), result);
            } else {
                // split qName into owner name and function name
                CharSequence nsQName = qName.subSequence(0, pos);
                CharSequence classQName = nsQName;
                CharSequence funcName = qName.subSequence(pos+2, qName.length());
                CharSequence shortFuncName = funcName;
                CsmNamespace nsp = project.findNamespace(nsQName);
                // we can have explicit template specialization like std::Class<int>::foo 
                while (nsp == null && pos >= 0) {
                    pos = findLastScopeDelimeterPos(nsQName);
                    if (pos >= 0) {
                        nsQName = nsQName.subSequence(0, pos);
                        nsp = project.findNamespace(nsQName);
                        funcName = qName.subSequence(pos+2, qName.length());
                    }
                }
                if (nsp == null) {
                    nsp = project.getGlobalNamespace();
                    funcName = qName;
                }
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         FUNCTION_KIND_FILTER,
                         CsmSelect.getFilterBuilder().createNameFilter(funcName, true, true, false));
                getFunctions(CsmSelect.getDeclarations(nsp, filter), result);
                
                if (!shortFuncName.equals(funcName)) {
                    filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                            FUNCTION_KIND_FILTER,
                            CsmSelect.getFilterBuilder().createNameFilter(shortFuncName, true, true, false));
                }

                for (CsmClassifier cls : project.findClassifiers(classQName)) {
                    if (CsmKindUtilities.isClass(cls)) {
                        getFunctions(CsmSelect.getClassMembers((CsmClass) cls, filter), result);
                    }
                }
            }
            for (CsmProject lib : project.getLibraries()) {
                getFunctions(lib, qName, result, processedProjects);
            }
        }
    }

    private static int findLastScopeDelimeterPos(CharSequence qName) {
        int pos = -1;
        for (int i = qName.length()-2; i > 1; i--) {
            if (qName.charAt(i) == ':' && qName.charAt(i+1) == ':') { //NOI18N
                pos = i;
                break;
            }
        }
        return pos;
    }

    private static void getFunctions(Iterator<? extends CsmOffsetableDeclaration> iter, Collection<CsmFunction> result) {
        while (iter.hasNext()) {
            CsmOffsetableDeclaration decl = iter.next();
            if (CsmKindUtilities.isFunction(decl)) {
                result.add((CsmFunction) decl);
            }
        }
    }

    public static Iterator<CsmUID<CsmFile>> getFileUIDs(CsmProject csmProject, NameAcceptor nameFilter) {
        return getDefault().getFileUIDs(csmProject, nameFilter);
    }

    private CsmSelect() {
    }
    
    /**
     * Static method to obtain the CsmSelect implementation.
     * @return the selector
     */
    private static CsmSelectProvider getDefault() {
        return DEFAULT;
    }
    
    public static interface CsmFilter {
    }
    
    public static interface NameAcceptor {
        boolean accept(CharSequence name);
    }

    public static interface CsmFilterBuilder {
        CsmFilter createKindFilter(CsmDeclaration.Kind ... kinds);
        CsmFilter createNameFilter(CharSequence strPrefix, boolean match, boolean caseSensitive, boolean allowEmptyName);
        CsmFilter createOffsetFilter(int startOffset, int endOffset);
        CsmFilter createOffsetFilter(int innerOffset);
        CsmFilter createCompoundFilter(CsmFilter first, CsmFilter second);
        CsmFilter createOrFilter(CsmFilter first, CsmFilter second);
        CsmFilter createNameFilter(NameAcceptor nameAcceptor);
    }

    /**
     * Implementation of the default selector
     */  
    private static final class Default implements CsmSelectProvider {
        private final Lookup.Result<CsmSelectProvider> res;
        private static final boolean FIX_SERVICE = true;
        private CsmSelectProvider fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmSelectProvider.class);
        }

        private CsmSelectProvider getService(){
            CsmSelectProvider service = fixedSelector;
            if (service == null) {
                for (CsmSelectProvider selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    fixedSelector = service;
                }
            }
            return service;
        }
        
        @Override
        public CsmFilterBuilder getFilterBuilder() {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getFilterBuilder();
            }
            return null;
        }

        @Override
        public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getMacros(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getIncludes(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getExternalDeclarations(CsmFile file) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getExternalDeclarations(file);
            }
            return null;
        }

        @Override
        public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmScopeElement> getScopeDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getScopeDeclarations(namespace, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getClassMembers(cls, filter);
            }
            return null;
        }
        
        @Override
        public Iterator<CsmFriend> getClassFriends(CsmClass cls, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getClassFriends(cls, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmEnumerator> getEnumerators(CsmEnum en, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getEnumerators(en, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getStaticVariables(file, filter);
            }
            return null;
        }

        @Override
        public Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getStaticFunctions(file, filter);
            }
            return null;
        }

        @Override
        public boolean hasDeclarations(CsmFile file) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.hasDeclarations(file);
            }
            return file.getDeclarations().isEmpty();
        }

        @Override
        public Iterator<CsmUID<CsmFile>> getFileUIDs(CsmProject csmProject, NameAcceptor filter) {
            CsmSelectProvider service = getService();
            if (service != null) {
                return service.getFileUIDs(csmProject, filter);
            }
            return Collections.<CsmUID<CsmFile>>emptyList().iterator();
        }
    }
    
    private static CharSequence getPosition(CsmOffsetableDeclaration obj) {
        CsmFile file = obj.getContainingFile();
        String position = file.getAbsolutePath().toString();
        int[] lineColumn = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, obj.getStartOffset());
        if (lineColumn != null) {
            position = "line=" + lineColumn[0] + ":" + lineColumn[1] + " " + position; // NOI18N
        }
        return position;
    }
    
    private static final Callable<CsmCacheMap> SELECT_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("SELECT Cache", 1); // NOI18N
        }
    };

    private static final class ClassMembersKey {
        private final CsmClass cls;
        private final CsmFilter filter;

        public ClassMembersKey(CsmClass cls, CsmFilter filter) {
            this.cls = cls;
            this.filter = filter;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.cls.hashCode();
            hash = 67 * hash + this.filter.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassMembersKey other = (ClassMembersKey) obj;
            if (!this.filter.equals(other.filter)) {
                return false;
            }
            if (!this.cls.equals(other.cls)) {
                return false;
            }
            return true;
        }            
    }

    private static final class EnumeratorsKey {
        private final CsmEnum en;
        private final CsmFilter filter;

        public EnumeratorsKey(CsmEnum cls, CsmFilter filter) {
            this.en = cls;
            this.filter = filter;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.en.hashCode();
            hash = 67 * hash + this.filter.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EnumeratorsKey other = (EnumeratorsKey) obj;
            if (!this.filter.equals(other.filter)) {
                return false;
            }
            if (!this.en.equals(other.en)) {
                return false;
            }
            return true;
        }            
    }

    private static final class ClassFriendsKey {
        private final CsmClass cls;
        private final CsmFilter filter;

        public ClassFriendsKey(CsmClass cls, CsmFilter filter) {
            this.cls = cls;
            this.filter = filter;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 87 * hash + this.cls.hashCode();
            hash = 87 * hash + this.filter.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassFriendsKey other = (ClassFriendsKey) obj;
            if (!this.filter.equals(other.filter)) {
                return false;
            }
            if (!this.cls.equals(other.cls)) {
                return false;
            }
            return true;
        }            
    }

    private static final class IteratorWrapper<T>  {
        private final Iterator<T> orig;
        private final List<T> fetched = new ArrayList<T>(10);
        
        public IteratorWrapper(Iterator<T> iter) {
            this.orig = iter;
        }

        public Iterator<T> iterator() {
            return new Impl();
        }
        
        private final class Impl implements Iterator<T> {
            int index = 0;
                        
            @Override
            public boolean hasNext() {
                if (index < fetched.size()) {
                    return true;
                }
                return orig.hasNext();
            }

            @Override
            public T next() {
                if (index < fetched.size()) {
                    return fetched.get(index++);
                }
                if (orig.hasNext()) {
                    fetched.add(orig.next());
                    return fetched.get(index++);
                }
                throw new NoSuchElementException("Invalid index " + index + " has only " + fetched.size()); // NOI18N
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "Impl{" + "index=" + index + '}'; // NOI18N
            }                        
        }
    }
}
