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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 * entry point to search references of model object in projects
 */
public abstract class CsmReferenceRepository {
    /** A dummy Repository that never returns any results.
     */
    private static final CsmReferenceRepository EMPTY = new Empty();
    
    /** default instance */
    private static CsmReferenceRepository defaultRepository;
    
    protected CsmReferenceRepository() {
    }
    
    /** Static method to obtain the Repository.
     * @return the Repository
     */
    public static CsmReferenceRepository getDefault() {
        /*no need for sync synchronized access*/
        if (defaultRepository != null) {
            return defaultRepository;
        }
        defaultRepository = Lookup.getDefault().lookup(CsmReferenceRepository.class);
        return defaultRepository == null ? EMPTY : defaultRepository;
    }
    
    public abstract Collection<CsmFile> findRelevantFiles(Collection<CsmProject> projects, CharSequence id);
    
    /**
     * look for references of target object in project
     * @param target target object to find references
     * @param project project as scope where to search
     * @param kinds flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    public abstract Collection<CsmReference> getReferences(CsmObject target, CsmProject project, Set<CsmReferenceKind> kinds, Interrupter interrupter);

    /**
     * look for references of target object in project
     * @param target target object to find references
     * @param file file as scope where to search
     * @param kinds flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    public abstract Collection<CsmReference> getReferences(CsmObject target, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter);
    
    /**
     * look for references of target objects in project
     * @param targets target objects to find references
     * @param project project as scope where to search
     * @param kinds flag indicating wether or not to include 
     *      self declaration object in collection
     * @return references for target object, empty collection if not found
     */
    //public abstract Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, Set<CsmReferenceKind> kinds);

    /**
     * look for references of target objects in file
     * @param targets target objects to find references
     * @param file file as scope where to search
     * @param kinds kind of references to search
     * @return references for target objects in file sorted from beginning, 
     *          empty collection if no references
     */
    public abstract Collection<CsmReference> getReferences(CsmObject[] targets, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter);
    
    public static BaseDocument getDocument(CsmFile file) {
        BaseDocument doc = null;
        try {
            doc = CsmReferenceRepository.getBaseDocument(file.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return doc;
    }
    
    private static BaseDocument getBaseDocument(FileObject fileObject) throws DataObjectNotFoundException, IOException {
        if (fileObject == null || !fileObject.isValid()) {
            return null;
        }
        StyledDocument doc = CsmUtilities.openDocument(DataObject.find(fileObject));
        return doc instanceof BaseDocument ? (BaseDocument) doc : null;
    }

    //
    // Implementation of the default Repository
    //
    private static final class Empty extends CsmReferenceRepository {
        Empty() {
        }

        @Override
        public Collection<CsmReference> getReferences(CsmObject target, CsmProject project, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
            return Collections.<CsmReference>emptyList();
        }

        public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
            return Collections.<CsmObject, Collection<CsmReference>>emptyMap();
        }

        @Override
        public Collection<CsmReference> getReferences(CsmObject target, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
            return Collections.<CsmReference>emptyList();
        }

        @Override
        public Collection<CsmReference> getReferences(CsmObject[] targets, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
            return Collections.<CsmReference>emptyList();
        }

        @Override
        public Collection<CsmFile> findRelevantFiles(Collection<CsmProject> projects, CharSequence id) {
            return Collections.emptyList();
        }
    }    
}
