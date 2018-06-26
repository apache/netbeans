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


import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;

/**
 *
 * @author Petr Pisl
 */

public class JSFRenamePlugin extends JavaRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private TreePathHandle treePathHandle = null;
    
    private static final Logger LOGGER = Logger.getLogger(JSFRenamePlugin.class.getName());
    
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of WicketRenameRefactoringPlugin */
    public JSFRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        LOGGER.fine("preCheck() called.");                                      //NOI18N
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        LOGGER.fine("checkParameters() called.");                               //NOI18N
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        LOGGER.fine("fastCheckParameters() called.");                           //NOI18N
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            FileObject fileObject = refactoring.getRefactoringSource().lookup(FileObject.class);
            NonRecursiveFolder nonRecursivefolder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            
            if (fileObject != null && (JSFRefactoringUtils.isJavaFile(fileObject) || fileObject.isFolder())) {
                if (fileObject.isFolder()){
                    // renaming folder -> recursively
                    
                    // find the old package name
                    ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
                    if(classPath == null){
                        return null;//it may happens for folders in php and similar projects, see #181611
                    }
                    FileObject root = classPath.findOwnerRoot(fileObject);
                    String relativePath = FileUtil.getRelativePath(root, fileObject.getParent());
                    // change of the one of project source roots - issue #222730
                    if (relativePath == null) { relativePath = ""; } //NOI18N
                    String prefix = relativePath.replace('/','.'); //NOI18N
                    String oldName = (prefix.length() == 0 ? fileObject.getName() : prefix + "." + fileObject.getName());
                    // the new package name
                    String newName = (prefix.length() == 0 ? refactoring.getNewName() : prefix + "." + refactoring.getNewName());
                    
                    JSFRefactoringUtils.renamePackage(refactoring, refactoringElements, fileObject, oldName, newName, true);
                }
                else {
                    if (JSFRefactoringUtils.isJavaFile(fileObject)){
                        JavaSource source = JavaSource.forFileObject(fileObject);
                        // Can be null, if it is just folder. 
                        if (source != null){
                            try {
                                source.runUserActionTask(new Task<CompilationController>() {
                                    public void run(CompilationController co) throws Exception {
                                        co.toPhase(JavaSource.Phase.RESOLVED);
                                        CompilationUnitTree cut = co.getCompilationUnit();
                                        if(!cut.getTypeDecls().isEmpty()){
                                            treePathHandle = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                                        }
                                    }
                                }, false);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);  //NOI18N
                            } catch (IOException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);  //NOI18N
                            }
                        }
                    }
                }
            }
            if (nonRecursivefolder != null){
                // non recursive package renaming
                String oldName = JSFRefactoringUtils.getPackageName(nonRecursivefolder.getFolder());
                String newName = refactoring.getNewName();
                    
                JSFRefactoringUtils.renamePackage(refactoring, refactoringElements, nonRecursivefolder.getFolder(), oldName, newName, false);
            }
            
            if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())){
                //renaming a class
                Project project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                if (project != null){
                    Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                    // issue #242249
                    if (resElement == null) {
                        return null;
                    }
                    TypeElement type = (TypeElement) resElement;
                    String oldFQN = type.getQualifiedName().toString();
                    String newFQN = renameClass(oldFQN, refactoring.getNewName());
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, oldFQN, newFQN);
                    Modifications modification = new Modifications();
                    for (Occurrences.OccurrenceItem item : items) {
                       // refactoringElements.add(refactoring, new JSFConfigRenameClassElement(item));
                        PositionBounds position = item.getChangePosition();
                        Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, position.getBegin(),
                                position.getEnd(), oldFQN, newFQN, item.getChangeMessage());
                        modification.addDifference(item.getFacesConfig(), difference);
                        refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
                    }
                }
            }
            
            semafor.set(null);
        }
        return null;
    }
    
    /**
     * @return true if given str is null or empty.
     */
    private static boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
    
    /**
     * Constructs new name for given class.
     * @param originalFullyQualifiedName old fully qualified name of the class.
     * @param newName new unqualified name of the class.
     * @return new fully qualified name of the class.
     */
    private static String renameClass(String originalFullyQualifiedName, String newName){
        if (isEmpty(originalFullyQualifiedName) || isEmpty(newName)){
            throw new IllegalArgumentException("Old and new name of the class must be given."); //NOI18N
        }
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        if (lastDot <= 0){
            // no package
            return newName;
        }
        return originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    
   
    
}
