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
package org.netbeans.modules.cnd.refactoring.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.cnd.refactoring.plugins.CsmWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class CsmRefactoringQuerySupport {

    private CsmRefactoringQuerySupport() {
    }

    /**
     * 
     * @param fo file
     * @param offset zero-based offset in file
     * @return 
     */
    public static Collection<RefactoringElementImplementation> getWhereUsed(FileObject fo, int offset) {
        Collection<RefactoringElementImplementation> out = Collections.emptyList();
        if (offset >= 0) {
            CsmFile csmFile = CsmUtilities.getCsmFile(fo, true, false);
            if (csmFile != null) {
                CsmReference ref = CsmReferenceResolver.getDefault().findReference(csmFile, null, (int) offset);
                if (ref != null) {
                    out = CsmWhereUsedQueryPlugin.getWhereUsed(ref, Collections.<Object, Boolean>emptyMap(), null);
                }
            }        
        }
        return out;
    }
    
    // Default params for find usages
    private static final Map<Object, Boolean> DEFAULT_PARAMS = new HashMap<>();
    static {
        DEFAULT_PARAMS.put(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
    }
    
    /**
     * 
     * @param fo file
     * @param line zero-based line number
     * @param col zero-based column
     * @return 
     */
    public static Collection<RefactoringElementImplementation> getWhereUsed(FileObject fo, int line, int col) {
        CsmFile csmFile = CsmUtilities.getCsmFile(fo, true, false);
        Collection<RefactoringElementImplementation> out = Collections.emptyList();
        if (csmFile != null) {
            long offset = CsmFileInfoQuery.getDefault().getOffset(csmFile, line, col);
            if (offset >= 0) {
                CsmReference ref = CsmReferenceResolver.getDefault().findReference(csmFile, null, (int)offset);
                if (ref != null) {
                    out = CsmWhereUsedQueryPlugin.getWhereUsed(ref, DEFAULT_PARAMS, null);
                }
            }
        }
        return out;
    }
}
