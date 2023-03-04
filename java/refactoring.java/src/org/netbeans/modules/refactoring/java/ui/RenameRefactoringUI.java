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

import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.EnumSet;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.refactoring.java.ui.Bundle.*;

/**
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "# {0} - OldName",
    "# {1} - NewName",
    "DSC_Rename=Rename <b>{0}</b> to <b>{1}</b>"})
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass, Openable {
    protected RenameRefactoring refactoring;
    private String newName = null;
    private final String oldName;
    private RenamePanel panel;
    private boolean fromListener = false;
    private TreePathHandle handle;
    private DocTreePathHandle docHandle;
    private ElementKind kind;
    private ElementHandle elementHandle;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    private boolean pkgRename = true;
    
    private RenameRefactoringUI(TreePathHandle handle, String labelName) {
        this.handle = handle;
        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
        newName = labelName;
        oldName = labelName;
    }
    
    private RenameRefactoringUI(TreePathHandle handle, CompilationInfo info) {
        this.handle = handle;
        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
        Element element = handle.resolveElement(info);
        if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
            this.elementHandle = ElementHandle.create(element);
        }
        this.oldName = this.newName = element.getSimpleName().toString();
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            this.refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(false, handle.getFileObject()));
        } else {
            this.refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(true, true, RefactoringUtils.getFileObject(handle)));
        }
    }
    
    private RenameRefactoringUI(FileObject file, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            this.handle = handle;
            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
            Element element = handle.resolveElement(info);
            if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
                this.elementHandle = ElementHandle.create(element);
            }
            this.oldName = element.getSimpleName().toString();
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
            this.oldName = file.isFolder()? file.getNameExt() : file.getName();
        }
        this.newName = this.oldName;
        ClasspathInfo cpInfo = handle==null?JavaRefactoringUtils.getClasspathInfoFor(file):RefactoringUtils.getClasspathInfoFor(handle);
        this.refactoring.getContext().add(cpInfo);
    }

    private RenameRefactoringUI(NonRecursiveFolder file) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
        this.oldName = RefactoringUtils.getPackageName(file.getFolder());
        this.refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file.getFolder()));
        this.newName = this.oldName;
        this.pkgRename = true;
    }
    
    
    private RenameRefactoringUI(FileObject fileObject, String newName, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            this.refactoring = new RenameRefactoring(Lookups.fixed(fileObject, handle));
            Element element = handle.resolveElement(info);
            if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
                this.elementHandle = ElementHandle.create(element);
            }
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(fileObject));
        }
        this.newName = newName;
        this.oldName = fileObject.isFolder()? fileObject.getNameExt() : fileObject.getName();
        ClasspathInfo cpInfo = handle==null?JavaRefactoringUtils.getClasspathInfoFor(fileObject):RefactoringUtils.getClasspathInfoFor(handle);
        this.refactoring.getContext().add(cpInfo);
        this.fromListener = true;
    }
    
    public RenameRefactoringUI(RenameRefactoring refactoring, String oldName, String newName, TreePathHandle handle, DocTreePathHandle docHandle) {
        this.refactoring = refactoring;
        this.oldName = oldName;
        this.newName = newName;
        this.handle = handle;
        this.docHandle = docHandle;
    }
    
    private RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(jmiObject.getFolder()));
        this.newName = newName;
        this.oldName = RefactoringUtils.getPackageName(jmiObject.getFolder());
        fromListener = true;
        pkgRename = true;
    }
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String suffix = "";
            if(handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT) {
                suffix = getString("LBL_Label");
            } else if (handle != null && handle.getElementHandle() !=null) {
                ElementKind kind = handle.getElementHandle().getKind();
                if (kind!=null && (kind.isClass() || kind.isInterface())) {
                    suffix  = kind.isInterface() ? getString("LBL_Interface") : getString("LBL_Class");
                } else if (kind == ElementKind.METHOD) {
                    suffix = getString("LBL_Method");
                } else if (kind == ElementKind.FIELD) {
                    suffix = getString("LBL_Field");
                } else if (kind == ElementKind.LOCAL_VARIABLE) {
                    suffix = getString("LBL_LocalVar");
                } else if (kind == ElementKind.PACKAGE || (handle == null && fromListener)) {
                    suffix = pkgRename ? getString("LBL_Package") : getString("LBL_Folder");
                } else if (kind == ElementKind.PARAMETER) {
                    suffix = getString("LBL_Parameter");
                }
            }
            suffix = suffix + " " + this.oldName; // NOI18N
            panel = new RenamePanel(handle, newName, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename") + " " + suffix, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).setSearchInComments(panel.searchJavadoc());
            JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
            if (properties==null) {
                properties = new JavaRenameProperties();
                refactoring.getContext().add(properties);
            }
            properties.setIsRenameGettersSetters(panel.isRenameGettersSetters());
            properties.setIsRenameTestClass(panel.isRenameTestClass());
            properties.setIsRenameTestClassMethod(panel.isRenameTestClassMethod());
            
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.checkParameters();
    }
    
    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
            if (properties==null) {
                properties = new JavaRenameProperties();
                refactoring.getContext().add(properties);
            }
            properties.setIsRenameGettersSetters(panel.isRenameGettersSetters());
            
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.fastCheckParameters();
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return DSC_Rename(oldName, newName);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        String postfix;
        if (handle==null) {
            postfix = ".JavaPackage";//NOI18N
        } else {
            ElementHandle elHandle = handle.getElementHandle();
            if (elHandle == null) {
                postfix = "";
            } else {
                ElementKind k = elHandle.getKind();

                if (k == null) {
                    postfix = "";
                } else if (k.isClass() || k.isInterface()) {
                    postfix = ".JavaClass";//NOI18N
                } else if (k == ElementKind.METHOD) {
                    postfix = ".Method";//NOI18N
                } else if (k.isField()) {
                    postfix = ".Field";//NOI18N
                } else {
                    postfix = "";
                }
            }
        }
        
        return new HelpCtx(RenameRefactoringUI.class.getName() + postfix);
    }
    
    @Override
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    @Override
    public void doRefactoringBypass() throws IOException {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dob = null;
                    if (byPassFolder != null) {
                        dob = DataFolder.findFolder(byPassFolder);
                    } else {
                        FileObject fob = refactoring.getRefactoringSource().lookup(FileObject.class);
                        if (fob != null) {
                            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
                        }
                    }
                    final DataObject dobFin = dob;
                    FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                        @Override
                        public void run() throws IOException {
                            if (dobFin != null) {
                                dobFin.rename(panel.getNameValue());
                            } else {
                                NonRecursiveFolder pack = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
                                if (pack != null) {
                                    renamePackage(pack.getFolder(), panel.getNameValue());
                                }
                            }
                        }
                    });
                    
                } catch (IOException iOException) {
                    Exceptions.printStackTrace(iOException);
                }
            }
        });
    }
    
    private void renamePackage(FileObject source, String name) {
        //copy/paste from PackageNode.setName()
        FileObject root = ClassPath.getClassPath(source, ClassPath.SOURCE).findOwnerRoot(source);

        name = name.replace('.', '/') + '/';           //NOI18N
        String oldName = this.oldName.replace('.', '/') + '/';     //NOI18N
        int i;
        for (i = 0; i < oldName.length() && i < name.length(); i++) {
            if (oldName.charAt(i) != name.charAt(i)) {
                break;
            }
        }
        i--;
        int index = oldName.lastIndexOf('/', i);     //NOI18N
        String commonPrefix = index == -1 ? null : oldName.substring(0, index);
        String toCreate = (index + 1 == name.length()) ? "" : name.substring(index + 1);    //NOI18N
        try {
            FileObject commonFolder = commonPrefix == null ? root : root.getFileObject(commonPrefix);
            FileObject destination = commonFolder;
            StringTokenizer dtk = new StringTokenizer(toCreate, "/");    //NOI18N
            while (dtk.hasMoreTokens()) {
                String pathElement = dtk.nextToken();
                FileObject tmp = destination.getFileObject(pathElement);
                if (tmp == null) {
                    tmp = destination.createFolder(pathElement);
                }
                destination = tmp;
            }
            DataFolder sourceFolder = DataFolder.findFolder(source);
            DataFolder destinationFolder = DataFolder.findFolder(destination);
            DataObject[] children = sourceFolder.getChildren();
            for (int j = 0; j < children.length; j++) {
                if (children[j].getPrimaryFile().isData()) {
                    children[j].move(destinationFolder);
                }
            }
            while (!commonFolder.equals(source)) {
                if (source.getChildren().length == 0) {
                    FileObject tmp = source;
                    source = source.getParent();
                    tmp.delete();
                } else {
                    break;
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    @Override
    public void open() {
        if (elementHandle!=null) {
            ElementOpen.open(handle.getFileObject(), elementHandle);
        }
    }

    public static JavaRefactoringUIFactory factory(final Lookup lookup) {
        return new JavaRefactoringUIFactory() {
            @Override
            public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
                final String n = RefactoringActionsProvider.getName(lookup);
                if (packages.length == 1) {
                    return n==null?new RenameRefactoringUI(packages[0]):new RenameRefactoringUI(packages[0], n);
                }

                if (handles.length == 0 || n != null) {
                    assert files.length == 1;
                    return n==null?new RenameRefactoringUI(files[0], null, null):new RenameRefactoringUI(files[0], n, null, null);
                }
                assert handles.length == 1;
                TreePathHandle selectedElement = handles[0];
                TreePath resolve = selectedElement.resolve(info);
                if(resolve != null && EnumSet.of(Kind.LABELED_STATEMENT, Kind.BREAK, Kind.CONTINUE).contains(resolve.getLeaf().getKind())) {
                    TreePath path = resolve;
                    if (path.getLeaf().getKind() != Kind.LABELED_STATEMENT) {
                        Tree tgt = info.getTreeUtilities().getBreakContinueTargetTree(path);
                        path = tgt != null ? info.getTrees().getPath(info.getCompilationUnit(), tgt) : null;
                    }
                    if (path == null) {
                        logger().log(Level.INFO, "doRename: " + handles[0], new NullPointerException("selected")); // NOI18N
                        return null;
                    }
                    LabeledStatementTree label = (LabeledStatementTree) path.getLeaf();
                    return new RenameRefactoringUI(TreePathHandle.create(path, info), label.getLabel().toString());
                }
                Element selected = handles[0].resolveElement(info);
                if (selected == null) {
                    logger().log(Level.INFO, "doRename: " + handles[0], new NullPointerException("selected")); // NOI18N
                    return null;
                }
                if (selected.getKind() == ElementKind.CONSTRUCTOR) {
                    selected = selected.getEnclosingElement();
                    TreePath path = info.getTrees().getPath(selected);
                    if (path == null) {
                        logger().log(Level.INFO, "doRename: " + selected, new NullPointerException("selected")); // NOI18N
                        return null;
                    }
                    selectedElement = TreePathHandle.create(path, info);
                }
                if (selected.getKind() == ElementKind.PACKAGE) {
                    final FileObject pkg = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(selected.toString().replace('.', '/'));
                    if (pkg != null) {
                        NonRecursiveFolder folder = new NonRecursiveFolder() {

                            @Override
                            public FileObject getFolder() {
                                return pkg;
                            }
                        };
                        return new RenameRefactoringUI(folder);
                    } else {
                        if (selected.getSimpleName().length() != 0) {
                            return new RenameRefactoringUI(selectedElement, info);
                        } else {
                            TreePath path = selectedElement.resolve(info);
                            if (path != null && path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                                return new RenameRefactoringUI(selectedElement.getFileObject(), null, info);
                            } else {
                                return null;
                            }
                        }
                    }
                } else if (selected instanceof TypeElement && !((TypeElement) selected).getNestingKind().isNested()) {
                    ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement) selected);
                    FileObject f = SourceUtils.getFile(handle, info.getClasspathInfo());
                    if (f != null && selected.getSimpleName().toString().equals(f.getName())) {
                        return new RenameRefactoringUI(f, selectedElement, info);
                    } else {
                        return new RenameRefactoringUI(selectedElement, info);
                    }
                } else {
                    return new RenameRefactoringUI(selectedElement, info);
                }
            }
        };
    }
    
    private static Logger logger() {
        return Logger.getLogger(RefactoringActionsProvider.class.getName());
    }

}
