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


package org.netbeans.modules.j2ee.jpa.refactoring.rename;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.openide.filesystems.FileUtil;

/**
 * Handles renaming of entity classes declared 
 * in a persistence.xml file when their package is renamed.
 *
 * @author Erno Mononen
 */
public class PersistenceXmlPackageRename extends PersistenceXmlRefactoring{
    
    private final RenameRefactoring renameRefactoring;
    
    public PersistenceXmlPackageRename(RenameRefactoring renameRefactoring) {
        this.renameRefactoring = renameRefactoring;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return renameRefactoring;
    }
    
    @Override
    protected boolean shouldHandle() {
        return true;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        if (isPackage()){
            FileObject pkg = renameRefactoring.getRefactoringSource().lookup(NonRecursiveFolder.class).getFolder();
            String oldPackageName = JavaIdentifiers.getQualifiedName(pkg);
            
            return doPrepare(refactoringElementsBag, pkg, oldPackageName, renameRefactoring.getNewName());
        } else if (isFolder()){
            FileObject folder = renameRefactoring.getRefactoringSource().lookup(FileObject.class);
            ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
            if(classPath == null){
                return null;//it may happens for folders in php and similar projects, see #181611
            }
            FileObject root = classPath.findOwnerRoot(folder);
            // issue 62320. By JavaDoc, ClassPath.fineOwnerRoot can return null
            if(root == null ) {
                return null;
            }
            String prefix = FileUtil.getRelativePath(root, folder.getParent());
            // #249491
            if (prefix == null) {
                return null;
            }
            prefix = prefix.replace('/','.'); // NOI18N
            String oldName = buildName(prefix, folder.getName());
            // the new package name
            String newName = buildName(prefix, renameRefactoring.getNewName());
            
            return doPrepare(refactoringElementsBag, folder, oldName, newName);
        }
        return null;
    }
    
    private boolean isPackage(){
        return renameRefactoring.getRefactoringSource().lookup(NonRecursiveFolder.class) != null;
    }
    
    private boolean isFolder(){
        FileObject folder = renameRefactoring.getRefactoringSource().lookup(FileObject.class);
        return folder != null && folder.isFolder();
    }
    
    
    /**
     * Prepares the rename.
     *
     * @param refactoringElementsBag
     * @param folder the folder or package to be renamed.
     * @param oldName the old FQN of the folder / package.
     * @param newName the new FQN of the folder / package.
     */
    private Problem doPrepare(RefactoringElementsBag refactoringElementsBag, FileObject folder, String oldName, String newName){
        Problem result = null;
        
        for (FileObject each : getPersistenceXmls(folder)){
            try {
                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                for (String clazz : getClasses(folder, new ArrayList<String>())){
                    List<PersistenceUnit> punits = getAffectedPersistenceUnits(pUDataObject, clazz);
                    String newClassName = clazz.replace(oldName, newName);
                    for (PersistenceUnit persistenceUnit : punits) {
                        refactoringElementsBag.add(getRefactoring(),
                                new PersistenceXmlPackageRenameRefactoringElement(persistenceUnit, clazz, newClassName, pUDataObject, each));
                    }
                }
            } catch (InvalidPersistenceXmlException ex) {
                Problem newProblem =
                        new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                
                result = RefactoringUtil.addToEnd(newProblem, result);
            }
        }
        return result;
        
    }
    
    private String buildName(String prefix, String name){
        if (prefix.length() == 0){
            return name;
        }
        return prefix + "." + name;
    }
    
    /**
     * Collects the names of the classes from the given folder, recursively if possible (i.e. the given
     * folder is not a NonRecursiveFolder).
     *
     * @return a list of fully qualified names of the classes in the given folder and its subfolders.
     */
    private List<String> getClasses(FileObject folder, List<String> result){
        for (FileObject each : folder.getChildren()){
            if (each.isFolder()){
                getClasses(each, result);
            } else {
                result.add(JavaIdentifiers.getQualifiedName(each));
            }
        }
        return result;
    }
    
    
    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            FileObject clazz,
            PUDataObject pUDataObject,
            FileObject persistenceXml) {
        
        return null;
    }
    
    
    /**
     * A rename element for persistence.xml
     */
    private static class PersistenceXmlPackageRenameRefactoringElement extends PersistenceXmlRefactoringElement {
        
        private final String newName;
        
        public PersistenceXmlPackageRenameRefactoringElement(PersistenceUnit persistenceUnit,
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
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlRename.class, "TXT_PersistenceXmlRename"), args);
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
