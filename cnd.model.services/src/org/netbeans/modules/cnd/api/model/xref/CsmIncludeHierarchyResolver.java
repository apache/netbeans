/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
