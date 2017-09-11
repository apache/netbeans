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
