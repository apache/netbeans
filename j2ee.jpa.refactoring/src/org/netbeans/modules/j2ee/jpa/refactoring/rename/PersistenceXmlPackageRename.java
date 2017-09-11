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
