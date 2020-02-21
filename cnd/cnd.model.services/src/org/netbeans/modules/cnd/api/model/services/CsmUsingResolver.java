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

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.openide.util.Lookup;

/**
 * entry point to resolve using directives and using declarations
 */
public abstract class CsmUsingResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmUsingResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmUsingResolver defaultResolver;
    
    protected CsmUsingResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmUsingResolver getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmUsingResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * return all using declarations visible for offsetable element, i.e.
     *  using std::cout;
     *  using std::printf;
     *  
     *  void method(){
     *  }
     * returns: std::printf() + std::cout
     *
     * @return sorted unmodifiable collection of declarations visible for input offsetable element through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject);
    
    /**
     * Finds all declarations visible in given namespace through "using" delcarations.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of declarations visible in given namespace through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace);
    
    /**
     * Finds all declarations visible in given namespace through "using" delcarations.
     * 
     * @param namespace  namespace of interest
     * @param name
     * @return unmodifiable collection of declarations visible in given namespace through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace, CharSequence name);
    
    /**
     * return all namespace visible for offsetable element, i.e.
     *  using namespace std;
     *  using namespace myNS;
     *  
     *  void method(){
     *  }
     * returns: global namespace (the container of method()) + myNs + std 
     * @return sorted unmodifiable collection of namespaces visible for input offsetable element
     */
    public abstract Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * Finds all "using" directives in given namespace.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of "using" directives in given namespace
     */
    public abstract Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace);

    /**
     * Finds all namespaces visible in given namespace through "using" directives.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of namespaces visible in given namespace though "using" directives
     */
    public abstract Collection<CsmNamespace> findVisibleNamespaces(CsmNamespace namespace, CsmProject startPrj);

//    /**
//     * Finds all direct visible namespace definitions.
//     * 
//     * @param namespace  namespace of interest
//     * @return unmodifiable collection of namespace definitions direct visible in includes
//     */
//    public abstract Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * return all namespace aliases visible for offsetable element, i.e.
     *  namespace B = A;
     *  namespace D = E;
     *  
     *  void method(){
     *  }
     * returns: B + D
     * @return sorted unmodifiable collection of namespace aliases visible for input offsetable element
     */
    public abstract Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * Finds all namespace aliases given namespace.
     *
     * @param namespace - namespace of interest
     * @return unmodifiable collection of namespace aliases in given namespace
     */
    public abstract Collection<CsmNamespaceAlias> findNamespaceAliases(CsmNamespace namespace);

    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmUsingResolver {
        Empty() {
        }

        @Override
        public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmDeclaration>emptyList();
        }
        
        @Override
        public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace) {
            return Collections.<CsmDeclaration>emptyList();
        }

        @Override
        public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace, CharSequence name) {
            return Collections.<CsmDeclaration>emptyList();
        }

        @Override
        public Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace) {
            return Collections.<CsmUsingDirective>emptyList();
        }

        @Override
        public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmNamespace>emptyList();
        }
        
//        public Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject) {
//            return Collections.<CsmNamespaceDefinition>emptyList();
//        }
    
        @Override
        public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        @Override
        public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmNamespace ns) {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        @Override
        public Collection<CsmNamespace> findVisibleNamespaces(CsmNamespace namespace, CsmProject prj) {
            return Collections.<CsmNamespace>emptyList();
        }
    }    
}
