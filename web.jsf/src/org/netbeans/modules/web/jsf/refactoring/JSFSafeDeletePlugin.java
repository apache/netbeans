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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Petr Pisl, Po-Ting Wu
 */
public class JSFSafeDeletePlugin extends JavaRefactoringPlugin{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private TreePathHandle treePathHandle = null;
    
    private static final Logger LOGGER = Logger.getLogger(JSFSafeDeletePlugin.class.getName());
    
    private final SafeDeleteRefactoring refactoring;
    
    /** Creates a new instance of JSFWhereUsedPlugin */
    public JSFSafeDeletePlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            NonRecursiveFolder nonRecursivefolder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            Project project;
            
            if (nonRecursivefolder != null){
                // non recursive package
                FileObject folder = nonRecursivefolder.getFolder();
                project = FileOwnerQuery.getOwner(folder);
                if (project != null){
                    String packageName = JSFRefactoringUtils.getPackageName(folder);
                    List <Occurrences.OccurrenceItem> items = Occurrences.getPackageOccurrences(project, packageName, packageName, false);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
                    }
                }
            }

            if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())){
                project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                if (project != null){
                    Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                    TypeElement type = (TypeElement) resElement;
                    String fqcn = type.getQualifiedName().toString();
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, fqcn, null);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
                    }
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    public static class JSFSafeDeleteClassElement extends SimpleRefactoringElementImplementation {
        private final Occurrences.OccurrenceItem item;
        
        JSFSafeDeleteClassElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getSafeDeleteMessage();
        }
        
        public void performChange() {
            try {
                item.performSafeDelete();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(item.getFacesConfig());
        }
        
        public FileObject getParentFile() {
            return item.getFacesConfig();
        }
        
        public PositionBounds getPosition() {
            return item.getChangePosition();
        }
        
        @Override
        public void undoChange() {
            try {
                item.undoSafeDelete();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
