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

package org.netbeans.modules.form.refactoring;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Entry point for refactoring, registered in META-INF/services. Whenever
 * a refactoring is about to start, createInstance is called where we analyze
 * the type of refactoring and attach RefactoringInfo object to the refactoring,
 * which can be later accessed from various places where we prepare or perform
 * the additional changes in forms and resources.
 * 
 *  @author Tomas Pavek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class RefactoringPluginFactoryImpl implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        RefactoringInfo.ChangeType changeType = null;
        List<FileObject> fileList = new LinkedList<FileObject>();
        List<String> nameList = new LinkedList<String>();

        // We must do some more analysis here, though it would be better to do
        // it later in the plugin's prepare method, but we can't be sure the
        // guarded handler is not called sooner from java plugin than our plugin.

        if (refactoring instanceof RenameRefactoring) {
            FileObject primaryFile = null;
            String oldName = null;
            Lookup sourceLookup = refactoring.getRefactoringSource();
            FileObject file = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkgFolder = sourceLookup.lookup(NonRecursiveFolder.class);
            final TreePathHandle tpHandle = sourceLookup.lookup(TreePathHandle.class);
            // assumption: if file is being renamed (even as result of renaming
            // a class) then file != null, and if something inside the class
            // is renamed then file == null

            if (file != null && RefactoringInfo.isJavaFile(file)) {
                // renaming a java file within the same package
                // (can be a form, or a component used in a form, or both)
                 if (isOnSourceClasspath(file)) {
                    changeType = RefactoringInfo.ChangeType.CLASS_RENAME;
                    primaryFile = file;
                    oldName = file.getName();
                 }
            } else if (file == null && tpHandle != null) {
                // renaming an element inside a java file
                primaryFile = tpHandle.getFileObject();
                if (RefactoringInfo.isJavaFileOfForm(primaryFile)) {
                    JavaSource source = JavaSource.forFileObject(tpHandle.getFileObject());
                    final RefactoringInfo.ChangeType[] changeTypes = new RefactoringInfo.ChangeType[1];
                    final String[] oldNames = new String[1];
                    try {
                        source.runUserActionTask(new CancellableTask<CompilationController>() {
                            @Override
                            public void cancel() {
                            }
                            @Override
                            public void run(CompilationController controller) throws Exception {
                                controller.toPhase(JavaSource.Phase.RESOLVED);
                                Element el = tpHandle.resolveElement(controller);
                                if (el != null) {
                                    switch(el.getKind()) {
                                    case FIELD:
                                        changeTypes[0] = RefactoringInfo.ChangeType.VARIABLE_RENAME;
                                        break;
                                    case LOCAL_VARIABLE:
                                        Element parentEl = el.getEnclosingElement();
                                        if (parentEl.getKind() == ElementKind.METHOD
                                                && "initComponents".equals(parentEl.getSimpleName().toString()) // NOI18N
                                                && ((ExecutableElement)parentEl).getParameters().isEmpty()) {
                                            changeTypes[0] = RefactoringInfo.ChangeType.VARIABLE_RENAME;
                                        }
                                        break;
                                    }
                                    // [should we also check if it really matches an existing component in the form?]
                                    oldNames[0] = el.getSimpleName().toString();
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    changeType = changeTypes[0];
                    oldName = oldNames[0];
                }
                // TBD: changing a property method of a component
            } else if (file != null && file.isFolder()) {
                // renaming a folder (incl. subfolders)
                if (isOnSourceClasspath(file)) {
                    changeType = RefactoringInfo.ChangeType.FOLDER_RENAME;
                    primaryFile = file;
                    oldName = file.getName();
                }
            } else if (pkgFolder != null) {
                // renaming a package (without subfolders)
                if (isOnSourceClasspath(pkgFolder.getFolder())) {
                    changeType = RefactoringInfo.ChangeType.PACKAGE_RENAME;
                    primaryFile = pkgFolder.getFolder();
                    ClassPath cp = ClassPath.getClassPath(primaryFile, ClassPath.SOURCE);
                    oldName = cp.getResourceName(primaryFile, '.', false);
                }
            }
            if (changeType != null) {
                fileList.add(primaryFile);
                nameList.add(oldName);
            }
        } else {
            if (refactoring instanceof MoveRefactoring) {
                Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
                for (FileObject file : files) {
                    if (RefactoringInfo.isJavaFile(file) && isOnSourceClasspath(file)) {
                        // moving a java file (between packages)
                        changeType = RefactoringInfo.ChangeType.CLASS_MOVE;
                        fileList.add(file);
                        ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                        nameList.add(cp.getResourceName(file, '.', false));
                    }
                }
            } else if (refactoring instanceof SingleCopyRefactoring) {
                Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
                for (FileObject file : files) {
                    if (RefactoringInfo.isJavaFileOfForm(file) && isOnSourceClasspath(file)) {
                        // copying a java file
                        changeType = RefactoringInfo.ChangeType.CLASS_COPY;
                        fileList.add(file);
                        nameList.add(file.getName());
                    }
                }
            } else if (refactoring instanceof SafeDeleteRefactoring) {
                Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
                for (FileObject file : files) {
                    if (file != null && RefactoringInfo.isJavaFileOfForm(file) && isOnSourceClasspath(file)) {
                        // deleting a form
                        changeType = RefactoringInfo.ChangeType.CLASS_DELETE;
                        fileList.add(file);
                        nameList.add(null);
                    }
                }
            }
        }

        if (changeType != null) {
            FileObject[] originalFiles = fileList.toArray(new FileObject[0]);
            String[] oldNames = nameList.toArray(new String[0]);
            RefactoringInfo refInfo = new RefactoringInfo(refactoring, changeType, originalFiles, oldNames);
            refactoring.getContext().add(refInfo); // to be accessible to the GuardedBlockHandlerFactoryImpl
            return new RefactoringPluginImpl(refInfo);
        }
        return null;
    }

    private static boolean isOnSourceClasspath(FileObject fo) {
        // TBD
        return true;
    }

    // -----

    private static class RefactoringPluginImpl implements RefactoringPlugin {

        private RefactoringInfo refInfo;

        RefactoringPluginImpl(RefactoringInfo refInfo) {
            this.refInfo = refInfo;
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
        public void cancelRequest() {
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            // even if guarded blocks are not affected directly we might want some changes
            for (FileObject file : refInfo.getOriginalFiles()) {
                if (RefactoringInfo.isJavaFileOfForm(file)) {
                    FormRefactoringUpdate update = refInfo.getUpdateForFile(file);
                    switch (refInfo.getChangeType()) {
                    case CLASS_DELETE: // in case of delete we only backup the form file
                        refactoringElements.registerTransaction(update);
                        return null;
                    case CLASS_RENAME: // renaming form class, always needs to load - auto-i18n
                        if (!update.prepareForm(true)) {
                            return new Problem(true, "Error loading form. Cannot update generated code.");
                        }
                        break;
                    // for VARIABLE_RENAME and EVENT_HANDLER_RENAME we don't know yet
                    // if they affect the form - guarded block handler will take care
                    }
                    refactoringElements.add(refInfo.getRefactoring(), update.getPreviewElement());
                    refactoringElements.addFileChange(refInfo.getRefactoring(), update);
                } else if (refInfo.getChangeType() == RefactoringInfo.ChangeType.PACKAGE_RENAME
                           || refInfo.getChangeType() == RefactoringInfo.ChangeType.FOLDER_RENAME) {
                    boolean anyForm = false;
                    for (FileObject fo : file.getChildren()) {
                        if (RefactoringInfo.isJavaFileOfForm(fo)) {
                            anyForm = true;
                            FormRefactoringUpdate update = refInfo.getUpdateForFile(fo);
                            refactoringElements.addFileChange(refInfo.getRefactoring(), update);
                        }
                    }
                    if (anyForm) {
                        // TODO add refactoring element informing about updating references to resources in GUI forms in this package
                    }
                }
            }
            return null;
        }
    }
}
