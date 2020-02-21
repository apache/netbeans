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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.spi.model.services.CsmInlcudeHierachyViewProvider;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.util.Lookup;

/**
 * entry point to resolve usages of include directives
 */
public abstract class CsmIncludeHierarchyResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmIncludeHierarchyResolver EMPTY = new Empty();
    private static final CsmInlcudeHierachyViewProvider V_EMPTY = new VEmpty();
    
    /** default instance */
    private static CsmIncludeHierarchyResolver defaultResolver;
    
    protected CsmIncludeHierarchyResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmIncludeHierarchyResolver getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmIncludeHierarchyResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }

    public static void showIncludeHierachyView(CsmFile file) {
        UIGesturesSupport.submit("USG_CND_SHOW_INCLUDE_HIERARCHY"); //NOI18N
        getInlcudeHierachyViewProvider().showIncludeHierachyView(file);
    }

    private static CsmInlcudeHierachyViewProvider getInlcudeHierachyViewProvider() {
        CsmInlcudeHierachyViewProvider instance = Lookup.getDefault().lookup(CsmInlcudeHierachyViewProvider.class);
        return instance == null ? V_EMPTY : instance;
    }
    /**
     * Search for usage of referenced file in include directives.
     * Return collection of files that direct include referenced file.
     * Search in file project and dependant projects
     */
    public abstract Collection<CsmFile> getFiles(CsmFile referencedFile);
    
    /**
     * Search for usage of referenced file in include directives.
     * Return collection of files that direct include referenced file.
     * Search in file project, dependant projects and dependant projects libraries
     */
    public abstract Collection<CsmFile> getAllFiles(CsmFile referencedFile);

    /**
     * Search for usage of referenced file in include directives.
     * Return collection of include directives that direct include referenced file.
     */
    public abstract Collection<CsmReference> getIncludes(CsmFile referencedFile);
    
    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmIncludeHierarchyResolver {
        Empty() {
        }

        @Override
        public Collection<CsmFile> getFiles(CsmFile referencedFile) {
            return Collections.<CsmFile>emptyList();
        }

        @Override
        public Collection<CsmFile> getAllFiles(CsmFile referencedFile) {
            return Collections.<CsmFile>emptyList();
        }

        @Override
        public Collection<CsmReference> getIncludes(CsmFile referencedFile) {
            return Collections.<CsmReference>emptyList();
        }
    }
    private static final class VEmpty implements CsmInlcudeHierachyViewProvider {

        VEmpty() {
        }

        @Override
        public void showIncludeHierachyView(CsmFile file) {
            // do nothing
        }
    }

}
