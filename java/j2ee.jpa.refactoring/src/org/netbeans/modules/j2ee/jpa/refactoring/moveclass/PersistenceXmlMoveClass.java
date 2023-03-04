/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.j2ee.jpa.refactoring.moveclass;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.openide.filesystems.FileUtil;

/**
 * Handles move class refactoring for entities, i.e. renames the reference
 * of entities in persistence.xml.
 *
 * @author Erno Mononen
 */
public class PersistenceXmlMoveClass extends PersistenceXmlRefactoring{
    
    private final MoveRefactoring moveRefactoring;
    
    public PersistenceXmlMoveClass(MoveRefactoring moveRefactoring) {
        this.moveRefactoring = moveRefactoring;
    }

    protected AbstractRefactoring getRefactoring() {
        return moveRefactoring;
    }

    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
                                                                     FileObject clazz,
                                                                     PUDataObject pUDataObject,
                                                                     FileObject persistenceXml) {

        String clazzFqn = JavaIdentifiers.getQualifiedName(clazz);
        String pkg = getTargetPackageName(clazz.getParent());
        String newName = pkg + "." + JavaIdentifiers.unqualify(clazzFqn);
        return new PersistenceXmlMoveClassRefactoringElement(persistenceUnit, clazzFqn, newName, pUDataObject, persistenceXml);
    }

    private String getTargetPackageName(FileObject fo) {
        String newPackageName = RefactoringUtil.getPackageName(moveRefactoring.getTarget().lookup(URL.class));
        String  postfix = "";
        
        for (FileObject folder : getMovedFolders()){
            if (FileUtil.isParentOf(folder, fo) || folder.equals(fo)){
                postfix = FileUtil.getRelativePath(folder.getParent(), fo).replace('/', '.');
                break;
            }
        }

        if (newPackageName.length() == 0) {
            return postfix;
        }
        if (postfix.length() == 0) {
            return newPackageName;
        }
        return newPackageName + "." + postfix;
    }

    private Set<FileObject> getMovedFolders(){
        Collection<? extends FileObject> fos = moveRefactoring.getRefactoringSource().lookupAll(FileObject.class);
        Set<FileObject> result = new HashSet<FileObject>();
        for (FileObject each : fos){
            if (each.isFolder()){
                result.add(each);
            }
        }
        return result;
    }

    /**
     * Move class element for persistence.xml
     */
    private static class PersistenceXmlMoveClassRefactoringElement extends PersistenceXmlRefactoringElement {
        
        private final String newName;
        
        public PersistenceXmlMoveClassRefactoringElement(PersistenceUnit persistenceUnit,
                String oldName,  String newName, PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, oldName, puDataObject, parentFile);
            this.newName = newName;
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {parentFile.getNameExt(), clazz, newName};
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlMoveClassRefactoringElement.class, "TXT_PersistenceXmlRename"), args);
        }
        
        public void undoChange() {
            ProviderUtil.renameManagedClass(persistenceUnit, clazz, newName, puDataObject);
        }
        
        /** Performs the change represented by this refactoring element.
         */
        public void performChange() {
            ProviderUtil.renameManagedClass(persistenceUnit, newName, clazz, puDataObject);
        }
        
    }
    
    
}
