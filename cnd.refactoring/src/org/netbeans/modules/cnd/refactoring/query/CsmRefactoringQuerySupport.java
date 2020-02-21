/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
