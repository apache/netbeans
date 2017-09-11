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

package org.netbeans.modules.j2ee.jpa.refactoring;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.jpa.refactoring.moveclass.PersistenceXmlMoveClass;
import org.netbeans.modules.j2ee.jpa.refactoring.rename.EntityRename;
import org.netbeans.modules.j2ee.jpa.refactoring.rename.PersistenceXmlPackageRename;
import org.netbeans.modules.j2ee.jpa.refactoring.rename.PersistenceXmlRename;
import org.netbeans.modules.j2ee.jpa.refactoring.safedelete.PersistenceXmlSafeDelete;
import org.netbeans.modules.j2ee.jpa.refactoring.whereused.PersistenceXmlWhereUsed;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;

/**
 * A refactoring factory for creating JPA refactoring plugins.
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class JPARefactoringFactory implements RefactoringPluginFactory{
    
    public JPARefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        FileObject targetFile = refactoring.getRefactoringSource().lookup(FileObject.class);
        NonRecursiveFolder pkg = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        
        boolean folder = targetFile != null && targetFile.isFolder();
        boolean javaPackage = pkg != null && RefactoringUtil.isOnSourceClasspath(pkg.getFolder());
        boolean javaFile = targetFile != null && RefactoringUtil.isJavaFile(targetFile);
        boolean javaMember = handle != null;
        
        if (refactoring instanceof RenameRefactoring) {
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            if (javaFile){
                return new PersistenceXmlRename(rename);
            } else if (javaPackage || folder){
                return new PersistenceXmlPackageRename(rename);
            } else if (javaMember){
                return new EntityRename(rename);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            MoveRefactoring move = (MoveRefactoring) refactoring;
            return new PersistenceXmlMoveClass(move);
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            SafeDeleteRefactoring safeDeleteRefactoring = (SafeDeleteRefactoring) refactoring;
            return new PersistenceXmlSafeDelete(safeDeleteRefactoring);
        } else if (refactoring instanceof WhereUsedQuery) {
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            return new PersistenceXmlWhereUsed(whereUsedQuery);
        }
        
        return null;
    }
    
}
