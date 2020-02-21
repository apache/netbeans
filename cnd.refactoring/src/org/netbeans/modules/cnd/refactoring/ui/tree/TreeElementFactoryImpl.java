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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.ElementGrip;
import org.netbeans.modules.cnd.refactoring.support.ElementGripFactory;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * factory of tree elements for C/C++ refactorings
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation.class, position=50)
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    public Map<Object, TreeElement> map = new WeakHashMap<>();
    
    @Override
    public TreeElement getTreeElement(Object o) {
        TreeElement result = map.get(o);
        if (result!= null) {
            return result;
        }
        if (o instanceof RefactoringElement) {
            Lookup lkp = ((RefactoringElement) o).getLookup();
            CsmOffsetable csmObject = lkp.lookup(CsmOffsetable.class);
            if (csmObject != null) {
                result = new RefactoringTreeElement((RefactoringElement) o);
            } else {
                CsmObject obj = ((RefactoringElement) o).getLookup().lookup(CsmObject.class);
                if (obj != null) {
                    System.err.println("unhandled CsmObject: " + obj);
                }
            }
        } else if (o instanceof ElementGrip) {
            result = new ElementGripTreeElement((ElementGrip)o);
        } else if (CsmKindUtilities.isProject(o)) {
            result = new ProjectTreeElement((CsmProject)o);
        } else if (CsmKindUtilities.isCsmObject(o)) {
            CsmObject csm = (CsmObject)o;
            if (CsmKindUtilities.isFile(csm)) {
                FileObject fo = CsmUtilities.getFileObject((CsmFile)o);
                result = new FileTreeElement(fo, (CsmFile)o);
            } else {
                result = new ParentTreeElement(csm);
            }
        } else if (o instanceof FileObject) {
            FileObject fo = (FileObject)o;
            CsmFile csmFile = CsmUtilities.getCsmFile(fo, false, false);
            // our factory is asked about any FileObject, so check for CND ones only
            if (csmFile != null) {
                result = new FileTreeElement(fo, csmFile);
            }
        }
        if (result != null) {
            map.put(o, result);
        }
        return result;
    }

    @Override
    public void cleanUp() {
        map.clear();
        ElementGripFactory.getDefault().cleanUp();
    }
}
