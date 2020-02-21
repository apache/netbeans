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
