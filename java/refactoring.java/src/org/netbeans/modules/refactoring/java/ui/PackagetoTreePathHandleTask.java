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
