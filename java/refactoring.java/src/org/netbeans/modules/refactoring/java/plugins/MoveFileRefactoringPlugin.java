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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implemented abilities:
 * <ul>
 * <li>Move file(s)</li>
 * <li>Move folder(s)</li>
 * <li>Rename folder</li>
 * <li>Rename package</li>
 * </ul>
 */
@NbBundle.Messages({"ERR_NotClass=Selected element is not a top-level class or interface.",
    "# {0} - The file not of java type.",
    "ERR_NotJava=Selected element is not defined in a java file. {0}",
    "ERR_CannotMovePublicIntoSamePackage=Cannot move public class to the same package.",
    "# {0} - Class name.",
    "ERR_CannotMoveIntoItself=Cannot move {0} into itself.",
    "ERR_NoTargetFound=Cannot find the target to move to.",
    "# {0} - Class name.",
    "ERR_ClassToMoveClashes=Class \"{0}\" already exists in the target package.",
    "# {0} - Source Class name.",
    "# {1} - Target Class name.",
    "ERR_ClassToMoveClashesInner=Type \"{0}\" already exists in the target \"{1}\"."
//    "ERR_CannotMoveIntoSamePackage=Cannot move class(es) into the same package."
})
public class MoveFileRefactoringPlugin extends JavaRefactoringPlugin {

    private Map packagePostfix = new HashMap();
    final AbstractRefactoring refactoring;
    final boolean isRenameRefactoring;
    ArrayList<FileObject> filesToMove = new ArrayList<FileObject>();
    /** top level classes to move */
    Set<ElementHandle<TypeElement>> classes;
    /** packages of which the content will change */
    Set<ElementHandle<PackageElement>> packages;
    /** list of folders grouped by source roots */
    List<List<FileObject>> foldersToMove = new ArrayList<List<FileObject>>();
    /** collection of packages that will change its name */
    Set<String> packageNames;
    
    private Set<ElementHandle<TypeElement>> elementHandles;
    private Set<ElementHandle<TypeElement>> handlesToMove;
    
    public MoveFileRefactoringPlugin(MoveRefactoring move) {
        this(move, false);
        setup(move.getRefactoringSource().lookupAll(FileObject.class), "", true); // NOI18N
    }
    
    public MoveFileRefactoringPlugin(RenameRefactoring rename) {
        this(rename, true);
        FileObject fo = rename.getRefactoringSource().lookup(FileObject.class);
        if (fo!=null) {
            setup(Collections.singletonList(fo), "", true); //NOI18N
        } else {
            setup(Collections.singletonList((rename.getRefactoringSource().lookup(NonRecursiveFolder.class)).getFolder()), "", false); // NOI18N
        }
    }
    
    private MoveFileRefactoringPlugin(AbstractRefactoring refactoring, boolean isRenameRefactoring) {
        this.refactoring = refactoring;
        if (refactoring == null) {
            throw new NullPointerException ();
        }
        this.isRenameRefactoring = isRenameRefactoring;
        elementHandles = new HashSet<ElementHandle<TypeElement>>();
        handlesToMove = new HashSet<ElementHandle<TypeElement>>();
    }
    
    @Override
    public Problem preCheck() {
        cancelRequested.set(false);
        Problem preCheckProblem = null;
        for (FileObject file:filesToMove) {
            if (!RefactoringUtils.isFileInOpenProject(file)) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                        MoveFileRefactoringPlugin.class,
                        "ERR_ProjectNotOpened",
                        FileUtil.getFileDisplayName(file)));
            }
        }
        for (TreePathHandle tph : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            ElementHandle elementHandle = tph.getElementHandle();
            if (elementHandle == null
                    || (!elementHandle.getKind().isClass() && !elementHandle.getKind().isInterface())) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                        MoveFileRefactoringPlugin.class,
                        "ERR_NotClass"));
                continue;
            }

            FileObject file = tph.getFileObject();
            if (!RefactoringUtils.isFileInOpenProject(file)) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                        MoveFileRefactoringPlugin.class,
                        "ERR_ProjectNotOpened",
                        FileUtil.getFileDisplayName(file)));
            }
        }
        return preCheckProblem;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    @NbBundle.Messages("ERR_ClasspathNotFound=No classpath defined for {0}.")
    public Problem fastCheckParameters() {
        if (isRenameRefactoring) {
            //folder rename
            FileObject f = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (f!=null) {
                String newName = ((RenameRefactoring) refactoring).getNewName();
                if (!RefactoringUtils.isValidPackageName(newName) || newName.indexOf('.') > 0) {
                    String msg = new MessageFormat(NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_InvalidFolder")).format(
                            new Object[] {newName}
                    );
                    return new Problem(true, msg);
                }
                FileObject fileObject = f.getParent().getFileObject(newName, f.getExt());
                if (fileObject != null && fileObject.getName().contentEquals(newName)) {
                    String msg = new MessageFormat(NbBundle.getMessage(MoveFileRefactoringPlugin.class,"ERR_PackageExists")).format(
                            new Object[] {newName}
                    );
                    return new Problem(true, msg);
                }
            }
            return super.fastCheckParameters();
        }
        if (!isRenameRefactoring) {
            try {
                final URL targetUrl = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                if(targetUrl != null) {
                    FileObject rootFO = null;
                    try {
                        rootFO = RefactoringUtils.getRootFileObject(targetUrl);
                    } catch (IllegalArgumentException | IOException ex) {
                        // target is invalid
                    }
                    if(rootFO == null || ClassPath.getClassPath(rootFO, ClassPath.SOURCE) == null) {
                        return new Problem(true, ERR_ClasspathNotFound(rootFO));
                    }
                    for (FileObject f: filesToMove) {
                        if (!RefactoringUtils.isJavaFile(f)) {
                            continue;
                        }
                        String targetPackageName = this.getTargetPackageName(f);
                        if (!RefactoringUtils.isValidPackageName(targetPackageName)) {
                            String s = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage"); //NOI18N
                            String msg = new MessageFormat(s).format(
                                    new Object[] {targetPackageName}
                            );
                            return new Problem(true, msg);
                        }
                        FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetUrl);
                        FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));

//                        if (f.getParent().equals(targetF)) {
//                            return new Problem(true, ERR_CannotMoveIntoSamePackage());
//                        }
                        
                        String pkgName = null;
                        if ((targetF!=null && !targetF.canWrite())) {
                            return new Problem(true, new MessageFormat(NbBundle.getMessage(MoveFileRefactoringPlugin.class,"ERR_PackageIsReadOnly")).format( // NOI18N
                                    new Object[] {targetPackageName}
                            ));
                        }

                        //                this.movingToDefaultPackageMap.put(r, Boolean.valueOf(targetF!= null && targetF.equals(classPath.findOwnerRoot(targetF))));
                        pkgName = targetPackageName;

                        if (pkgName == null) {
                            pkgName = ""; // NOI18N
                        } else if (pkgName.length() > 0) {
                            pkgName = pkgName + '.';
                        }
                        //targetPrefix = pkgName;

                        //                JavaClass[] sourceClasses = (JavaClass[]) sourceClassesMap.get(r);
                        //                String[] names = new String [sourceClasses.length];
                        //                for (int x = 0; x < names.length; x++) {
                        //                    names [x] = sourceClasses [x].getName();
                        //                }
                        //
                        //                FileObject movedFile = JavaMetamodel.getManager().getDataObject(r).getPrimaryFile();
                        String fileName = f.getName();
                        if (targetF!=null) {
                            FileObject[] children = targetF.getChildren();
                            for (int x = 0; x < children.length; x++) {
                                if (children[x].getName().equals(fileName) && "java".equals(children[x].getExt()) && !children[x].equals(f) && !children[x].isVirtual()) { //NOI18N
                                    return new Problem(true, new MessageFormat(
                                            NbBundle.getMessage(MoveFileRefactoringPlugin.class,"ERR_ClassToMoveClashes")).format(new Object[] {fileName} // NOI18N
                                    ));
                                }
                            } // for
                        }

                        //                boolean accessedByOriginalPackage = ((Boolean) accessedByOriginalPackageMap.get(r)).booleanValue();
                        //                boolean movingToDefaultPackage = ((Boolean) movingToDefaultPackageMap.get(r)).booleanValue();
                        //                if (p==null && accessedByOriginalPackage && movingToDefaultPackage) {
                        //                    p= new Problem(false, getString("ERR_MovingClassToDefaultPackage")); // NOI18N
                        //                }
                    }
                } else {
                for (TreePathHandle tph : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
                    FileObject f = tph.getFileObject();
                    if (!RefactoringUtils.isJavaFile(f)) {
                        return new Problem(true, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_NotJava", f));
                    }
                    if (targetUrl != null) {
                        String targetPackageName = targetUrl != null ? RefactoringUtils.getPackageName(targetUrl) : null;
                        if (targetPackageName == null || !RefactoringUtils.isValidPackageName(targetPackageName)) {
                            String s = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage"); //NOI18N
                            String msg = new MessageFormat(s).format(
                                    new Object[]{targetPackageName});
                            return new Problem(true, msg);
                        }

                        FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetUrl);
                        FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));

                        if ((targetF != null && !targetF.canWrite())) {
                            return new Problem(true, new MessageFormat(NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_PackageIsReadOnly")).format( // NOI18N
                                    new Object[]{targetPackageName}));
                        }

                        String fileName = f.getName();
                        if (targetF != null) {
                            FileObject[] children = targetF.getChildren();
                            for (int i = 0; i < children.length; i++) {
                                if (children[i].getName().equals(fileName) && "java".equals(children[i].getExt()) && !children[i].equals(f) && !children[i].isVirtual()) { //NOI18N
                                    return new Problem(true, new MessageFormat(
                                            NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_ClassToMoveClashes")).format(new Object[]{fileName} // NOI18N
                                            ));
                                }
                            }
                        }
                    } else {
                        TreePathHandle target = ((MoveRefactoring) refactoring).getTarget().lookup(TreePathHandle.class);
                        if (target == null) {
                            String s = NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_NoTargetFound"); //NOI18N
                            return new Problem(true, s);
                        }
                    }
                }
                }
            } catch (IOException ioe) {
                //do nothing
            }
        }
        
        if(refactoring.getRefactoringSource().lookup(TreePathHandle.class) != null) {
            return super.fastCheckParameters();
        }
        return null;
    }
    
    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        for (TreePathHandle tph : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            FileObject f = tph.getFileObject();
            Element resolveElement = tph.resolveElement(javac);
            URL targetUrl = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);
            if(targetUrl != null) {
                FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetUrl);
                String targetPackageName = RefactoringUtils.getPackageName(targetUrl);
                FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));
                if (f.getParent().equals(targetF)) {
                    if(resolveElement.getModifiers().contains(Modifier.PUBLIC)) {
                        return new Problem(true, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_CannotMovePublicIntoSamePackage"));
                    }
                }
            } else {
                TreePathHandle target = ((MoveRefactoring) refactoring).getTarget().lookup(TreePathHandle.class);
                ElementHandle elementHandle = target.getElementHandle();
                assert elementHandle != null;
                TypeElement targetType = (TypeElement) elementHandle.resolve(javac);
                if (targetType == resolveElement) {
                    return new Problem(true, ERR_CannotMoveIntoItself(resolveElement.getSimpleName()));
                }
                List<? extends Element> enclosedElements = targetType.getEnclosedElements();
                for (Element element : enclosedElements) {
                    switch (element.getKind()) {
                        case ENUM:
                        case CLASS:
                        case ANNOTATION_TYPE:
                        case INTERFACE:
                            if(element.getSimpleName().contentEquals(resolveElement.getSimpleName())) {
                                return new Problem(true, ERR_ClassToMoveClashesInner(element.getSimpleName(), targetType.getSimpleName()));
                            }
                    }
                }
            }
        }
        return super.fastCheckParameters(javac);
    }
    
    @SuppressWarnings("CollectionContainsUrl")
    private Problem checkProjectDeps() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        Set<FileObject> sourceRoots = new HashSet<FileObject>();
        for (TreePathHandle tph : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            FileObject file = tph.getFileObject();
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                sourceRoots.add(root);
            }
        }
        // XXX: there should be no URL in lookup when moving to Class
        URL target = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);
        if (target == null) {
            return null;
        }
        try {
            FileObject r = RefactoringUtils.getClassPathRoot(target);
            URL targetUrl = URLMapper.findURL(r, URLMapper.EXTERNAL);
            Project targetProject = FileOwnerQuery.getOwner(r);
            Set<URL> deps = SourceUtils.getDependentRoots(targetUrl);
            for (FileObject sourceRoot : sourceRoots) {
                URL sourceUrl = URLMapper.findURL(sourceRoot, URLMapper.INTERNAL);
                if (!deps.contains(sourceUrl)) {
                    Project sourceProject = FileOwnerQuery.getOwner(sourceRoot);
                    for (ElementHandle<TypeElement> affected : elementHandles) {
                        FileObject affectedFile = SourceUtils.getFile(affected, cpInfo);
                        if (FileOwnerQuery.getOwner(affectedFile).equals(sourceProject)
                                && !handlesToMove.contains(affected)
                                && !sourceProject.equals(targetProject)) {
                            assert sourceProject != null;
                            assert targetProject != null;
                            String sourceName = ProjectUtils.getInformation(sourceProject).getDisplayName();
                            String targetName = ProjectUtils.getInformation(targetProject).getDisplayName();
                            return createProblem(null, false, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_MissingProjectDeps", sourceName, targetName));
                        }
                    }
                }
            }
        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
        }
        return null;
    }

    @SuppressWarnings("CollectionContainsUrl")
    private Problem checkProjectDeps(Set<FileObject> a) {
        if (!isRenameRefactoring) {
            Set<FileObject> sourceRoots = new HashSet<FileObject>();
            for (FileObject file : filesToMove) {
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                if (cp != null) {
                    FileObject root = cp.findOwnerRoot(file);
                    sourceRoots.add(root);
                }
            }
            URL target = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);
            if (target == null) {
                return null;
            }
            try {
                FileObject r = RefactoringUtils.getClassPathRoot(target);
                URL targetUrl = URLMapper.findURL(r, URLMapper.EXTERNAL);
                Project targetProject = FileOwnerQuery.getOwner(r);
                Set<URL> deps = SourceUtils.getDependentRoots(targetUrl);
                for (FileObject sourceRoot : sourceRoots) {
                    URL sourceUrl = URLMapper.findURL(sourceRoot, URLMapper.INTERNAL);
                    if (!deps.contains(sourceUrl)) {
                        Project sourceProject = FileOwnerQuery.getOwner(sourceRoot);
                        for (FileObject affected: a) {
                            if (FileOwnerQuery.getOwner(affected).equals(sourceProject) && !filesToMove.contains(affected) && !sourceProject.equals(targetProject)) {
                                assert sourceProject!=null;
                                assert targetProject!=null;
                                String sourceName = ProjectUtils.getInformation(sourceProject).getDisplayName();
                                String targetName = ProjectUtils.getInformation(targetProject).getDisplayName();
                                return createProblem(null, false, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_MissingProjectDeps", sourceName, targetName));
                            }
                        }
                    }
                }
            } catch (IOException iOException) {
                Exceptions.printStackTrace(iOException);
            }
        }
        return null;
    }

    private Set<FileObject> getRelevantFiles(TreePathHandle tph) {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new LinkedHashSet<FileObject>();
        if(!isRenameRefactoring) {
            TreePathHandle targetHandle = ((MoveRefactoring) refactoring).getTarget().lookup(TreePathHandle.class);
            if(targetHandle != null) {
                set.add(targetHandle.getFileObject());
            }
        }
        for (ElementHandle<TypeElement> elementHandle : classes) {
            //set.add(SourceUtils.getFile(el, cpInfo));
            Set<FileObject> files = idx.getResources(elementHandle, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
            set.addAll(files);
        }
        for (ElementHandle<PackageElement> elementHandle : packages) {
            Set<FileObject> files = idx.getResourcesForPackage(elementHandle, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            set.addAll(files);
        }
        set.addAll(filesToMove);
        elementHandles.clear();
        if(tph != null) {
            ElementHandle elementHandle = tph.getElementHandle();
            handlesToMove.add(elementHandle);
            set.add(SourceUtils.getFile(elementHandle, cpInfo));
            Set<FileObject> files = idx.getResources(elementHandle,
                    EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES,
                    ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE));
            set.addAll(files);
            Set<ElementHandle<TypeElement>> handles = idx.getElements(elementHandle,
                    EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES,
                    ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE));
            elementHandles.addAll(handles);
        }
        return set;
    }    
    
    private void initClasses() {
        classes = new HashSet<ElementHandle<TypeElement>>();
        packages = new HashSet<ElementHandle<PackageElement>>();
        for (int i=0;i<filesToMove.size();i++) {
            final int j = i;
            try {
                JavaSource source = JavaSource.forFileObject(filesToMove.get(i));
                
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    
                    @Override
                    public void cancel() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }
                    
                    @Override
                    public void run(final CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        List<? extends Tree> trees= parameter.getCompilationUnit().getTypeDecls();
                        for (Tree t: trees) {
                            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                                TypeElement klass = (TypeElement) parameter.getTrees().getElement(TreePath.getPath(parameter.getCompilationUnit(), t));
                                classes.add(ElementHandle.create(klass));
                                PackageElement packageOf = parameter.getElements().getPackageOf(klass);
                                packages.add(ElementHandle.create(packageOf));
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            }
            
        }
    }

    private Problem initPackages() {
        if (foldersToMove.isEmpty()) {
            packageNames = Collections.emptySet();
            return null;
        } else {
            packageNames = new HashSet<String>();
        }
        
        for (List<FileObject> folders : foldersToMove) {
            ClassPath cp = ClassPath.getClassPath(folders.get(0), ClassPath.SOURCE);
            if (cp == null) {
                return new Problem(true, NbBundle.getMessage(
                        MoveFileRefactoringPlugin.class,
                        "ERR_ClasspathNotFound",
                        folders.get(0)));
            }
            for (FileObject folder : folders) {
                String pkgName = cp.getResourceName(folder, '.', false);
                packageNames.add(pkgName);
            }
        }
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        fireProgressListenerStart(AbstractRefactoring.PREPARE, -1);
        initClasses();
        Problem p = initPackages();
        if (p != null) {
            return p;
        }
        
        TreePathHandle sourceTph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        Set<FileObject> a = getRelevantFiles(sourceTph);
        
        RefactoringVisitor transformer;
        
        if(!isRenameRefactoring) {
            URL targetUrl = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);
            TreePathHandle targetTph = ((MoveRefactoring) refactoring).getTarget().lookup(TreePathHandle.class);

            if(targetUrl != null && sourceTph == null) {
                p = checkProjectDeps(a);
                transformer = new MoveTransformer(this);
            } else {
                p = checkProjectDeps();
                if(targetUrl != null) {
                    transformer = new MoveClassTransformer(sourceTph, targetUrl);
                } else {
                    if(sourceTph != null) {
                        transformer = new MoveClassTransformer(sourceTph, targetTph.getElementHandle(), targetTph.getFileObject(), sourceTph.getFileObject());
                    } else {
                        transformer = new MoveClassTransformer(classes.iterator().next(), targetTph.getElementHandle(), null, null); //TODO: unclear - source?
                    }
                }
            }
        } else {
            p = checkProjectDeps(a);
            transformer = new MoveTransformer(this);
        }
        
        fireProgressListenerStep(a.size());
        
        TransformTask task = new TransformTask(transformer, null);
        Problem prob = createAndAddElements(a, task, elements, refactoring);
        Problem problem = null;
        if(transformer instanceof MoveClassTransformer) {
            MoveClassTransformer moveClassTransformer = (MoveClassTransformer) transformer;
            if(moveClassTransformer.deleteFile()) {
                final DeleteFile deleteFile;
                if(sourceTph != null) {
                    deleteFile = new DeleteFile(sourceTph.getFileObject(), elements);
                } else {
                    deleteFile = new DeleteFile(filesToMove.get(0), elements);
                }
                elements.addFileChange(refactoring, deleteFile);
                elements.add(refactoring, deleteFile);
            }
            problem = moveClassTransformer.getProblem();
        } else if(transformer instanceof MoveTransformer) {
            MoveTransformer moveTransformer = (MoveTransformer) transformer;
            problem = moveTransformer.getProblem();
        }
        fireProgressListenerStop();
        return prob != null ? prob : JavaPluginUtils.chainProblems(p, problem);
    }
    
    String getNewPackageName() {
        if (isRenameRefactoring) {
            return ((RenameRefactoring) refactoring).getNewName();
        } else {
            // XXX cache it !!!
            return RefactoringUtils.getPackageName(((MoveRefactoring) refactoring).getTarget().lookup(URL.class));
        }
    }
    
    String getTargetPackageName(FileObject fo) {
        if (isRenameRefactoring) {
            if (refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class) !=null) {
                return getNewPackageName();
            }
            else {
                //folder rename
                FileObject folder = refactoring.getRefactoringSource().lookup(FileObject.class);
                ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                FileObject root = cp.findOwnerRoot(folder);
                String relativePath = FileUtil.getRelativePath(root, folder.getParent());
                String prefix = relativePath == null? "" : relativePath.replace('/','.');
                String relativePath1 = FileUtil.getRelativePath(folder, fo.isFolder() ? fo : fo.getParent());
                String postfix = relativePath1 == null? "" : relativePath1.replace('/', '.');
                String t = concat(prefix, getNewPackageName(), postfix);
                return t;
            }
        } else if (packagePostfix != null) {
            if (fo == null) {
                return getNewPackageName();
            }
            String postfix = (String) packagePostfix.get(fo);
            String packageName = concat(null, getNewPackageName(), postfix);
            return packageName;
        } else {
            return getNewPackageName();
        }
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        setup(fileObjects, postfix, recursively, null);
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively, List<FileObject> sameRootList) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (RefactoringUtils.isJavaFile(fo)) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addDot = !"".equals(postfix);
                Collection col = new ArrayList();
                for (FileObject fo2: fo.getChildren()) {
                    if (!fo2.isFolder() || (fo2.isFolder() && recursively)) {
                        col.add(fo2);
                    }
                }
                List<FileObject> curRootList = sameRootList;
                if (sameRootList == null) {
                    curRootList = new ArrayList<FileObject>();
                    foldersToMove.add(curRootList);
                }
                curRootList.add(fo);
                setup(col,
                        postfix + (addDot ? "." : "") + fo.getName(), // NOI18N
                        recursively,
                        curRootList);
            }
        }
    }
 
    private String concat(String s1, String s2, String s3) {
        String result = "";
        if (s1 != null && !"".equals(s1)) {
            result += s1 + "."; // NOI18N
        }
        result +=s2;
        if (s3 != null && !"".equals(s3)) {
            result += ("".equals(result)? "" : ".") + s3; // NOI18N
        }
        return result;
    }        

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if(tph == null) {
            return null;
        }
        switch (p) {
            case FASTCHECKPARAMETERS:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, tph.getFileObject());
            default:
                return JavaSource.forFileObject(tph.getFileObject());
        }
    }
}    
