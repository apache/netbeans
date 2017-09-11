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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * A task that extracts tree path handles representing java types within a package
 * 
 * @author Bharath Ravi Kumar
 */
abstract class PackagetoTreePathHandleTask implements Runnable, CancellableTask<CompilationController> {

    private static final String JAVA_EXTENSION = "java";
    public CompilationInfo cinfo;
    private final Collection<FileObject> javaFileObjects = new HashSet<FileObject>();
    private final Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
    
    public PackagetoTreePathHandleTask(Collection<? extends Node> nodes) {
        for (Node packageNode : nodes) {
            DataObject dataObject = packageNode.getLookup().lookup(DataObject.class);
            FileObject primaryFileObject = dataObject.getPrimaryFile();
            javaFileObjects.addAll(findJavaSourceFiles(primaryFileObject));
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void run(CompilationController info) throws Exception {
        //TODO:Should this be a WeakReference?
        info.toPhase(Phase.ELEMENTS_RESOLVED);
        cinfo = info;
        CompilationUnitTree unit = info.getCompilationUnit();
        for (Tree tree : unit.getTypeDecls()) {
            Element element = info.getTrees().getElement(TreePath.getPath(unit, tree));
            if (element == null || !(element.getKind().isClass() || element.getKind().isInterface())) {
                // syntax errors #111195
                continue;
            }
            //TODO:Revisit this check
            if (!element.getModifiers().contains(Modifier.PRIVATE)) {
                TreePathHandle typeHandle = TreePathHandle.create(TreePath.getPath(unit, tree), info);
                handles.add(typeHandle);
            }
        }

    }

    @Override
    public void run() {

        for (FileObject javaFileObject : javaFileObjects) {
            JavaSource source = JavaSource.forFileObject(javaFileObject);
            assert source != null;
            try {
                source.runUserActionTask(this, false);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        TopComponent activetc = TopComponent.getRegistry().getActivated();

        RefactoringUI ui = createRefactoringUI(handles, cinfo);
        if (ui != null) {
            UI.openRefactoringUI(ui, activetc);
        } else {
            JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
        }
    }

    protected final FileObject[] getFileHandles() {
        return javaFileObjects.toArray(new FileObject[0]);
    }

    protected abstract RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles, CompilationInfo info);

    public static Collection<FileObject> findJavaSourceFiles(FileObject pkg) {
        Collection<FileObject> javaSrcFiles = new ArrayList<FileObject>();
        addSourcesInPackage(pkg, javaSrcFiles);
        return javaSrcFiles;
    }

    private static void addSourcesInPackage(FileObject pkgFileObject, Collection<FileObject> javaSrcFiles) {
        for (FileObject childFileObject : pkgFileObject.getChildren()) {
            if (childFileObject.isData() && JAVA_EXTENSION.equalsIgnoreCase(childFileObject.getExt())) {
                javaSrcFiles.add(childFileObject);
            }
            //We do not recursively delete subpackages
//            else if (childFileObject.isFolder()) {
//                addSourcesInPackage(childFileObject, javaSrcFiles);
//            }
        }
    }
}
