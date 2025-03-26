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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.file.launcher.api.SourceLauncher;
import org.netbeans.modules.refactoring.java.plugins.LocalVarScanner;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringUtils {

    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    private static final Logger LOG = Logger.getLogger(RefactoringUtils.class.getName());

    /**
     * Get all overriding methods for given ExecutableElement
     *
     * @param e
     * @param info
     * @return
     * @deprecated 
     */
    @Deprecated
    public static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, CompilationInfo info) {
        return getOverridenMethods(e, info.getElementUtilities().enclosingTypeElement(e), info);
    }

    private static Collection<ExecutableElement> getOverridenMethods(ExecutableElement e, TypeElement parent, CompilationInfo info) {
        ArrayList<ExecutableElement> result = new ArrayList<ExecutableElement>();

        TypeMirror sup = parent.getSuperclass();
        if (sup.getKind() == TypeKind.DECLARED) {
            TypeElement next = (TypeElement) ((DeclaredType) sup).asElement();
            ExecutableElement overriden = getMethod(e, next, info);
            result.addAll(getOverridenMethods(e, next, info));
            if (overriden != null) {
                result.add(overriden);
            }
        }
        for (TypeMirror tm : parent.getInterfaces()) {
            TypeElement next = (TypeElement) ((DeclaredType) tm).asElement();
            ExecutableElement overriden2 = getMethod(e, next, info);
            result.addAll(getOverridenMethods(e, next, info));
            if (overriden2 != null) {
                result.add(overriden2);
            }
        }
        return result;
    }

    private static ExecutableElement getMethod(ExecutableElement method, TypeElement type, CompilationInfo info) {
        for (ExecutableElement met : ElementFilter.methodsIn(type.getEnclosedElements())) {
            if (info.getElements().overrides(method, met, type)) {
                return met;
            }
        }
        return null;
    }

    public static Set<ElementHandle<TypeElement>> getImplementorsAsHandles(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el, AtomicBoolean cancel) {
        LinkedList<ElementHandle<TypeElement>> elements = new LinkedList<ElementHandle<TypeElement>>(
                implementorsQuery(idx, ElementHandle.create(el)));
        Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        while (!elements.isEmpty()) {
            if (cancel.get()) {
                return Collections.emptySet();
            }
            ElementHandle<TypeElement> next = elements.removeFirst();
            if (!result.add(next)) {
                // it is a duplicate; do not query again
                continue;
            }
            Set<ElementHandle<TypeElement>> foundElements = implementorsQuery(idx, next);
            elements.addAll(foundElements);
        }
        return result;
    }

    private static Set<ElementHandle<TypeElement>> implementorsQuery(ClassIndex idx, ElementHandle<TypeElement> next) {
        return idx.getElements(next,
                EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
    }

    /**
     * 
     * @param e
     * @param info
     * @param cancel 
     * @return
     * @deprecated
     */
    @Deprecated
    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement e, CompilationInfo info, AtomicBoolean cancel) {
        Collection<ExecutableElement> result = new ArrayList<>();
        TypeElement parentType = (TypeElement) e.getEnclosingElement();
        Set<ElementHandle<TypeElement>> subTypes = getImplementorsAsHandles(info.getClasspathInfo().getClassIndex(), info.getClasspathInfo(), parentType, cancel);
        for (ElementHandle<TypeElement> subTypeHandle : subTypes) {
            TypeElement type = subTypeHandle.resolve(info);
            if (type == null) {
                // #214462: removed logging, logs show coupling errors
                continue;
                // #120577: log info to find out what is going wrong
//                FileObject file = SourceUtils.getFile(subTypeHandle, info.getClasspathInfo());
//                if (file == null) {
//                    //Deleted file
//                    continue;
//                } else {
//                    throw new NullPointerException("#120577: Cannot resolve " + subTypeHandle + "; file: " + file + " Classpath: " + info.getClasspathInfo());
//                }
            }
            List<ExecutableElement> methods = new LinkedList<>(ElementFilter.methodsIn(type.getEnclosedElements()));
            // #253063 - Anonymous classes of enum constants are not returned by index, need to get them manually
            if(type.getKind() == ElementKind.ENUM) {
                for (VariableElement variableElement : ElementFilter.fieldsIn(type.getEnclosedElements())) {
                    TreePath varPath = info.getTrees().getPath(variableElement);
                    if(varPath != null && varPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                        ExpressionTree initializer = ((VariableTree)varPath.getLeaf()).getInitializer();
                        if(initializer != null && initializer.getKind() == Tree.Kind.NEW_CLASS) {
                            NewClassTree ncTree = (NewClassTree) initializer;
                            ClassTree classBody = ncTree.getClassBody();
                            if(classBody != null) {
                                Element anonEl = info.getTrees().getElement(new TreePath(varPath, classBody));
                                if(anonEl != null) {
                                    methods.addAll(ElementFilter.methodsIn(anonEl.getEnclosedElements()));
                                }
                            }
                        }
                    }
                }
            }
            for (ExecutableElement method : methods) {
                if (info.getElements().overrides(method, e, type)) {
                    result.add(method);
                }
            }
        }
        return result;
    }
    
    public static CodeStyle getCodeStyle(CompilationInfo info) {
        if (info != null) {
            try {
                Document doc = info.getDocument();
                if (doc != null) {
                    CodeStyle cs = (CodeStyle)doc.getProperty(CodeStyle.class);
                    return cs != null ? cs : CodeStyle.getDefault(doc);
                }
            } catch (IOException ioe) {
                // ignore
            }

            FileObject file = info.getFileObject();
            if (file != null) {
                return CodeStyle.getDefault(file);
            }
        }

        return CodeStyle.getDefault((Document)null);
    }

    /**
     *
     * @param f
     * @return true if f is java
     */
    public static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(FileUtil.getMIMEType(f, JAVA_MIME_TYPE));
    }

    /**
     * @param element
     * @param info
     * @return true if given element comes from library
     */
    public static boolean isFromLibrary(ElementHandle<? extends Element> element, ClasspathInfo info) {
        FileObject file = SourceUtils.getFile(element, info);
        if (file == null) {
            //no source for given element. Element is from library
            return true;
        }
        return FileUtil.getArchiveFile(file) != null;
    }

    /**
     * is given name valid package name
     *
     * @param name
     * @return
     */
    public static boolean isValidPackageName(String name) {
        if (name.endsWith(".")) //NOI18N
        {
            return false;
        }
        if (name.startsWith(".")) //NOI18N
        {
            return false;
        }
        if (name.contains("..")) //NOI18N
        {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(name, "."); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            if (!Utilities.isJavaIdentifier(tokenizer.nextToken())) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param f
     * @return true if given file is in open project
     */
    public static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        // Future<Project[]> o.n.api.project.ui.OpenProjects.openProjects()
        Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();
        if(!openProjects.isDone()) {
            return false;
        }
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return SourceLauncher.isSourceLauncherFile(file);
        }
        return isOpenProject(p);
    }

    /**
     * Is given file on any source classpath?
     *
     * @param fo
     * @return
     * @deprecated 
     */
    @Deprecated
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project pr = FileOwnerQuery.getOwner(fo);
        if (pr == null) {
            return false;
        }

        //workaround for 143542
        for (String type : new String[]{JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_TYPE_RESOURCES}) {
            for (SourceGroup sg : ProjectUtils.getSources(pr).getSourceGroups(type)) {
                if (fo == sg.getRootFolder() || (FileUtil.isParentOf(sg.getRootFolder(), fo) && sg.contains(fo))) {
                    return ClassPath.getClassPath(fo, ClassPath.SOURCE) != null;
                }
            }
        }
        return false;
        //end of workaround
        //return ClassPath.getClassPath(fo, ClassPath.SOURCE)!=null;
    }

    /**
     * Is given file a root of source classpath?
     *
     * @param fo
     * @return
     */
    public static boolean isClasspathRoot(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        return cp != null ? fo.equals(cp.findOwnerRoot(fo)) : false;
    }

    /**
     * Is the given file "java" && in open projects && on source classpath?
     *
     * @param file
     * @return
     * @deprecated 
     */
    @Deprecated
    public static boolean isRefactorable(FileObject file) {
        return file != null && isJavaFile(file) && isFileInOpenProject(file) && isOnSourceClasspath(file);
    }

    /**
     * returns package name for given folder. Folder must be on source classpath
     *
     * @param folder
     * @return
     */
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp == null) {
            // see http://www.netbeans.org/issues/show_bug.cgi?id=159228
            throw new IllegalStateException(String.format("No classpath for %s.", folder.getPath())); // NOI18N
        }
        return cp.getResourceName(folder, '.', false);
    }

    /**
     * get package name for given CompilationUnitTree
     *
     * @param unit
     * @return
     */
    public static String getPackageName(CompilationUnitTree unit) {
        assert unit != null;
        ExpressionTree name = unit.getPackageName();
        if (name == null) {
            //default package
            return "";
        }
        return name.toString();
    }

    /**
     * get package name for given url.
     *
     * @param url
     * @return
     */
    public static String getPackageName(URL url) {
        File f = null;
        try {
            f = FileUtil.normalizeFile(Utilities.toFile(url.toURI()));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        String suffix = "";

        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix)) {
                    return getPackageName(fo);
                }
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix) ? "" : ".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            suffix = f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1) + suffix; // NOI18N
            f = f.getParentFile();
        } while (f != null);
        throw new IllegalArgumentException("Cannot create package name for url " + url); // NOI18N
    }

    /**
     * creates or finds FileObject according to
     *
     * @param url
     * @return FileObject
     */
    public static FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null) {
                return result;
            }
            File f = new File(url.toURI());

            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static FileObject getClassPathRoot(URL url) throws IOException {
        FileObject result = getRootFileObject(url);
        if(result == null) {
            return null;
        }
        ClassPath classPath = ClassPath.getClassPath(result, ClassPath.SOURCE);
        if(classPath == null) {
            return null;
        }
        return classPath.findOwnerRoot(result);
    }

    /**
     * Get all supertypes for given type
     *
     * @param type
     * @param info
     * @return
     * @deprecated 
     */
    @Deprecated
    public static Collection<TypeElement> getSuperTypes(TypeElement type, CompilationInfo info) {
        Collection<TypeElement> result = new HashSet<TypeElement>();
        LinkedList<TypeElement> l = new LinkedList<TypeElement>();
        l.add(type);
        while (!l.isEmpty()) {
            TypeElement t = l.removeFirst();
            TypeElement superClass = typeToElement(t.getSuperclass(), info);
            if (superClass != null) {
                result.add(superClass);
                l.addLast((TypeElement) superClass);
            }
            Collection<TypeElement> interfaces = typesToElements(t.getInterfaces(), info);
            result.addAll(interfaces);
            l.addAll(interfaces);
        }
        return result;
    }

    /**
     * get supertypes of given types
     *
     * @param type
     * @param info
     * @param sourceOnly true if only types defined in open project should be
     * searched
     * @return
     * @deprecated 
     */
    @Deprecated
    public static Collection<TypeElement> getSuperTypes(TypeElement type, CompilationInfo info, boolean sourceOnly) {
        if (!sourceOnly) {
            return getSuperTypes(type, info);
        }
        Collection<TypeElement> result = new HashSet<TypeElement>();
        for (TypeElement el : getSuperTypes(type, info)) {
            ElementHandle<TypeElement> handle = ElementHandle.create(el);
            FileObject file = SourceUtils.getFile(handle, info.getClasspathInfo());
            if (file != null && isFileInOpenProject(file) && !isFromLibrary(handle, info.getClasspathInfo())) {
                result.add(el);
            }
        }
        return result;
    }

    public static TypeElement typeToElement(TypeMirror type, CompilationInfo info) {
        return (TypeElement) info.getTypes().asElement(type);
    }

    private static boolean isOpenProject(Project p) {
        return OpenProjects.getDefault().isProjectOpen(p);
    }

    private static Collection<TypeElement> typesToElements(Collection<? extends TypeMirror> types, CompilationInfo info) {
        Collection<TypeElement> result = new HashSet<>();
        for (TypeMirror tm : types) {
            result.add(typeToElement(tm, info));
        }
        return result;
    }

    public static Collection<FileObject> elementsToFile(Collection<? extends Element> elements, ClasspathInfo cpInfo) {
        Collection<FileObject> result = new HashSet<>();
        for (Element handle : elements) {
            result.add(SourceUtils.getFile(handle, cpInfo));
        }
        return result;
    }

    public static boolean elementExistsIn(TypeElement target, Element member, CompilationInfo info) {
        for (Element currentMember : target.getEnclosedElements()) {
            if (currentMember.getKind() == member.getKind()
                    && currentMember.getSimpleName().equals(member.getSimpleName())) {
                if (currentMember.getKind() == ElementKind.METHOD) {
                    ExecutableElement exMethod = (ExecutableElement) currentMember;
                    ExecutableElement method = (ExecutableElement) member;
                    if (exMethod.getParameters().size() == method.getParameters().size()) {
                        boolean sameParameters = true;
                        for (int j = 0; j < exMethod.getParameters().size(); j++) {
                            TypeMirror exType = ((VariableElement) exMethod.getParameters().get(j)).asType();
                            TypeMirror paramType = method.getParameters().get(j).asType();
                            if (!info.getTypes().isSameType(exType, paramType)) {
                                sameParameters = false;
                            }
                        }
                        if (sameParameters) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param fqn
     * @param info
     * @return
     */
    public static boolean typeExists(String fqn, CompilationInfo info) {
        return info.getElements().getTypeElement(fqn) != null;
    }

    /**
     * create ClasspathInfo for specified files includes dependencies
     *
     * @param files
     * @return
     * @deprecated 
     */
    @Deprecated
    public static ClasspathInfo getClasspathInfoFor(FileObject... files) {
        return getClasspathInfoFor(true, files);
    }

    /**
     * create ClasspathInfo for specified files
     *
     * @param dependencies
     * @param files
     * @return
     */
    public static ClasspathInfo getClasspathInfoFor(boolean dependencies, FileObject... files) {
        return getClasspathInfoFor(dependencies, false, files);
    }

    /**
     * create ClasspathInfo for specified files
     *
     * @param dependencies include dependencies
     * @param backSource libraries replaces by sources using
     * SourceForBinaryQuery
     * @param files
     * @return
     */
    @SuppressWarnings("CollectionContainsUrl")
    public static ClasspathInfo getClasspathInfoFor(boolean dependencies, boolean backSource, FileObject... files) {
        assert files.length > 0;
        Set<URL> dependentSourceRoots = new HashSet<>();
        Set<URL> dependentCompileRoots = new HashSet<>();
        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = null;
        ClassPath moduleBoot = null;
        ClassPath compile = null;
        ClassPath moduleCompile = null;
        ClassPath moduleClass = null;        
        for (FileObject fo : files) {
            ClassPath cp = null;
            FileObject ownerRoot = null;
            if (fo != null) {
                cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (cp != null) {
                    ownerRoot = cp.findOwnerRoot(fo);
                }
            }
            if (cp != null && ownerRoot != null && FileUtil.getArchiveFile(ownerRoot) == null) {
                for (FileObject src : cp.getRoots()) { // Keep all source roots from cp. Needed if project has multiple source roots.
                URL sourceRoot = URLMapper.findURL(src, URLMapper.INTERNAL);
                if (dependencies) {
                    Set<URL> urls = SourceUtils.getDependentRoots(sourceRoot, false);
                    Set<ClassPath> cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
                    Set<URL> toRetain = new HashSet<URL>();
                    for (ClassPath path : cps) {
                        for (ClassPath.Entry e : path.entries()) {
                            toRetain.add(e.getURL());
                        }
                    }
                    Set<URL> compileUrls = new HashSet<URL>(urls);
                    urls.retainAll(toRetain);
                    compileUrls.removeAll(toRetain);
                    dependentSourceRoots.addAll(urls);
                    dependentCompileRoots.addAll(compileUrls);
                } else {
                    dependentSourceRoots.add(sourceRoot);
                }
                if (FileOwnerQuery.getOwner(fo) != null) {
                    for (FileObject f : cp.getRoots()) {
                        dependentCompileRoots.add(URLMapper.findURL(f, URLMapper.INTERNAL));
                    }
                }
                }
            } else {
                for (ClassPath scp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    for (FileObject root : scp.getRoots()) {
                        dependentSourceRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                    }
                }
            }
            
            if(fo != null) {
                ClassPath fboot = ClassPath.getClassPath(fo, ClassPath.BOOT);
                ClassPath fmoduleboot = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_BOOT_PATH);
                ClassPath fcompile = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                ClassPath fmodulecompile = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_COMPILE_PATH);
                ClassPath fmoduleclass = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_CLASS_PATH);
                //When file[0] is a class file, there is no compile cp but execute cp
                //try to get it
                if (fcompile == null) {
                    fcompile = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
                }
                //If no cp found at all log the file and use nullPath since the ClasspathInfo.create
                //doesn't accept null compile or boot cp.
                if (fcompile == null) {
                    LOG.log(Level.WARNING, "No classpath for: {0} {1}", new Object[]{FileUtil.getFileDisplayName(fo), FileOwnerQuery.getOwner(fo)}); //NOI18N
                } else {
                    compile = compile != null ? merge(compile, fcompile) : fcompile;
                }
                
                if (fboot != null) {
                    boot = boot != null ? merge(boot, fboot) : fboot;
                }
                if (fmoduleboot != null) {
                    moduleBoot = moduleBoot != null ? merge(moduleBoot, fmoduleboot) : fmoduleboot;
                }
                if (fmodulecompile != null) {
                    moduleCompile = moduleCompile != null ? merge(moduleCompile, fmodulecompile) : fmodulecompile;
                }
                if (fmoduleclass != null) {
                    moduleClass = moduleClass != null ? merge(moduleClass, fmoduleclass) : fmoduleclass;
                }
            }
        }

        if (backSource) {
            for (FileObject file : files) {
                if (file != null) {
                    ClassPath source = ClassPath.getClassPath(file, ClassPath.COMPILE);
                    for (Entry root : source.entries()) {
                        Result r = SourceForBinaryQuery.findSourceRoots(root.getURL());
                        for (FileObject root2 : r.getRoots()) {
                            dependentSourceRoots.add(URLMapper.findURL(root2, URLMapper.INTERNAL));
                        }
                    }
                }
            }
        }

        ClassPath rcp = ClassPathSupport.createClassPath(dependentSourceRoots.toArray(new URL[0]));
        if (compile == null) {
            compile = nullPath;
        }
        compile = merge(compile, ClassPathSupport.createClassPath(dependentCompileRoots.toArray(new URL[0])));
        if (boot == null) {
            boot = JavaPlatform.getDefault().getBootstrapLibraries();
        }
        return new ClasspathInfo.Builder(boot == null ? nullPath : boot)
                .setModuleBootPath(moduleBoot == null ? boot == null? nullPath : boot : moduleBoot)
                .setClassPath(compile)
                .setModuleCompilePath(moduleCompile)
                .setModuleClassPath(moduleClass)
                .setSourcePath(rcp).
                build();
    }


    /**
     * @param handle
     * @return  
     */
    public static FileObject getFileObject(TreePathHandle handle) {
        ElementHandle elementHandle = handle.getElementHandle();
        if (elementHandle == null ) {
            return handle.getFileObject();
        }
       ClasspathInfo info = getClasspathInfoFor(false, handle.getFileObject()); 
       return SourceUtils.getFile(elementHandle, info);
    }    
    /**
     * create ClasspathInfo for specified handles
     *
     * @param handles
     * @return
     */
    public static ClasspathInfo getClasspathInfoFor(TreePathHandle... handles) {
        FileObject[] result = new FileObject[handles.length];
        int i = 0;
        for (TreePathHandle handle : handles) {
            FileObject fo = getFileObject(handle);
            if (i == 0 && fo == null) {
                result = new FileObject[handles.length + 1];
                result[i++] = handle.getFileObject();
            }
            result[i++] = fo;
        }
        return getClasspathInfoFor(result);
    }

    /**
     * Finds type parameters from
     * <code>typeArgs</code> list that are referenced by
     * <code>tm</code> type.
     *
     * @param utils compilation type utils
     * @param typeArgs modifiable list of type parameters to search; found types
     * will be removed (performance reasons).
     * @param result modifiable list that will contain referenced type
     * parameters
     * @param tm parametrized type to analyze
     */
    public static void findUsedGenericTypes(Types utils, List<TypeMirror> typeArgs, Set<TypeMirror> result, TypeMirror tm) {
        if (typeArgs.isEmpty()) {
            return;
        } else if (tm.getKind() == TypeKind.TYPEVAR) {
            TypeVariable type = (TypeVariable) tm;
            int index = findTypeIndex(utils, typeArgs, type);
            if (index >= 0) {
                result.add(typeArgs.get(index));
            } else {
                TypeMirror low = type.getLowerBound();
                if (low != null && low.getKind() != TypeKind.NULL) {
                    findUsedGenericTypes(utils, typeArgs, result, low);
                }
                TypeMirror up = type.getUpperBound();
                if (up != null) {
                    findUsedGenericTypes(utils, typeArgs, result, up);
                }
                int idx = findTypeIndex(utils, typeArgs, type);
                if (idx >= 0) {
                    result.add(typeArgs.get(idx));
                }
            }
        } else if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType type = (DeclaredType) tm;
            for (TypeMirror tp : type.getTypeArguments()) {
                findUsedGenericTypes(utils, typeArgs, result, tp);
            }
        } else if (tm.getKind() == TypeKind.WILDCARD) {
            WildcardType type = (WildcardType) tm;
            TypeMirror ex = type.getExtendsBound();
            if (ex != null) {
                findUsedGenericTypes(utils, typeArgs, result, ex);
            }
            TypeMirror su = type.getSuperBound();
            if (su != null) {
                findUsedGenericTypes(utils, typeArgs, result, su);
            }
        }
    }

    private static int findTypeIndex(Types utils, List<TypeMirror> typeArgs, TypeMirror type) {
        int i = -1;
        for (TypeMirror typeArg : typeArgs) {
            i++;
            if (utils.isSameType(type, typeArg)) {
                return i;
            }
        }
        return -1;
    }

    public static List<TypeMirror> filterTypes(List<TypeMirror> source, Set<TypeMirror> used) {
        List<TypeMirror> result = new ArrayList<TypeMirror>(source.size());

        for (TypeMirror tm : source) {
            if (used.contains(tm)) {
                result.add(tm);
            }
        }

        return result;
    }

    /**
     * translates list of elements to list of types
     *
     * @param typeParams elements
     * @return types
     * @deprecated 
     */
    @Deprecated
    public static List<TypeMirror> resolveTypeParamsAsTypes(List<? extends Element> typeParams) {
        if (typeParams.isEmpty()) {
            return Collections.<TypeMirror>emptyList();
        }
        List<TypeMirror> typeArgs = new ArrayList<TypeMirror>(typeParams.size());
        for (Element elm : typeParams) {
            typeArgs.add(elm.asType());
        }
        return typeArgs;
    }

    /**
     * finds the nearest enclosing ClassTree on
     * <code>path</code> that is class or interface or enum or annotation type
     * and is or is not annonymous. In case no ClassTree is found the first top
     * level ClassTree is returned.
     *
     * Especially useful for selecting proper tree to refactor.
     *
     * @param javac javac
     * @param path path to search
     * @param isClass stop on class
     * @param isInterface stop on interface
     * @param isEnum stop on enum
     * @param isAnnotation stop on annotation type
     * @param isAnonymous check if class or interface is annonymous
     * @return path to the enclosing ClassTree
     * @deprecated 
     */
    @Deprecated
    public static @NullUnknown
    TreePath findEnclosingClass(CompilationInfo javac, TreePath path, boolean isClass, boolean isInterface, boolean isEnum, boolean isAnnotation, boolean isAnonymous) {
        if (path == null) {
            return null;
        }
        Tree selectedTree = path.getLeaf();
        TreeUtilities utils = javac.getTreeUtilities();
        while (true) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(selectedTree.getKind())) {
                ClassTree classTree = (ClassTree) selectedTree;
                if (isEnum && utils.isEnum(classTree)
                        || isInterface && utils.isInterface(classTree)
                        || isAnnotation && utils.isAnnotation(classTree)
                        || isClass && !(utils.isInterface(classTree) || utils.isEnum(classTree) || utils.isAnnotation(classTree))) {

                    Tree.Kind parentKind = path.getParentPath().getLeaf().getKind();
                    if (isAnonymous || Tree.Kind.NEW_CLASS != parentKind) {
                        break;
                    }
                }
            }

            path = path.getParentPath();
            if (path == null) {
                List<? extends Tree> typeDecls = javac.getCompilationUnit().getTypeDecls();
                if (typeDecls.isEmpty()) {
                    return null;
                }
                selectedTree = typeDecls.get(0);
                if (selectedTree.getKind().asInterface() == ClassTree.class) {
                    return javac.getTrees().getPath(javac.getCompilationUnit(), selectedTree);
                } else {
                    return null;
                }
            }
            selectedTree = path.getLeaf();
        }
        return path;
    }

    //XXX: copied from SourceUtils.addImports. Ideally, should be on one place only:
    public static CompilationUnitTree addImports(CompilationUnitTree cut, List<String> toImport, TreeMaker make)
            throws IOException {
        // do not modify the list given by the caller (may be reused or immutable).
        toImport = new ArrayList<String>(toImport);
        Collections.sort(toImport);

        List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
        int currentToImport = toImport.size() - 1;
        int currentExisting = imports.size() - 1;

        while (currentToImport >= 0 && currentExisting >= 0) {
            String currentToImportText = toImport.get(currentToImport);

            while (currentExisting >= 0 && (imports.get(currentExisting).isStatic() || imports.get(currentExisting).getQualifiedIdentifier().toString().compareTo(currentToImportText) > 0)) {
                currentExisting--;
            }

            if (currentExisting >= 0) {
                imports.add(currentExisting + 1, make.Import(make.Identifier(currentToImportText), false));
                currentToImport--;
            }
        }
        // we are at the head of import section and we still have some imports
        // to add, put them to the very beginning
        while (currentToImport >= 0) {
            String importText = toImport.get(currentToImport);
            imports.add(0, make.Import(make.Identifier(importText), false));
            currentToImport--;
        }
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackageAnnotations(), cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }

    /**
     * transforms passed modifiers to abstract form
     *
     * @param make a tree maker
     * @param oldMods modifiers of method or class
     * @return the abstract form of ModifiersTree
     */
    public static ModifiersTree makeAbstract(TreeMaker make, ModifiersTree oldMods) {
        if (oldMods.getFlags().contains(Modifier.ABSTRACT)) {
            return oldMods;
        }
        Set<Modifier> flags = EnumSet.of(Modifier.ABSTRACT);
        flags.addAll(oldMods.getFlags());
        flags.remove(Modifier.FINAL);
        return make.Modifiers(flags, oldMods.getAnnotations());
    }

    public static String variableClashes(String newName, TreePath tp, CompilationInfo info) {
        LocalVarScanner lookup = new LocalVarScanner(info, newName);
        TreePath scopeBlok = tp;
        EnumSet set = EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.FOR_LOOP, Tree.Kind.METHOD);
        while (scopeBlok != null && !set.contains(scopeBlok.getLeaf().getKind())) {
            scopeBlok = scopeBlok.getParentPath();
        }
        if(scopeBlok == null) {
            return null;
        }
        Element var = info.getTrees().getElement(tp);
        if (var != null) {
            lookup.scan(scopeBlok, var);
        }

        if (lookup.hasRefernces()) {
            return NbBundle.getMessage(RefactoringUtils.class, "MSG_LocVariableClash",newName);
        }

        TreePath temp = tp;
        while (temp != null && temp.getLeaf().getKind() != Tree.Kind.METHOD) {
            Scope scope = info.getTrees().getScope(temp);
            for (Element el : scope.getLocalElements()) {
                if (el.getSimpleName().toString().equals(newName)) {
                    return NbBundle.getMessage(RefactoringUtils.class, "MSG_LocVariableClash",newName);
                }
            }
            temp = temp.getParentPath();
        }
        return null;
    }

    public static boolean isSetter(CompilationInfo info, ExecutableElement el, Element propertyElement) {
        CodeStyle codeStyle = getCodeStyle(info);
        String setterName = CodeStyleUtils.computeSetterName(
                propertyElement.getSimpleName(),
                propertyElement.getModifiers().contains(Modifier.STATIC),
                codeStyle);

        return el.getSimpleName().contentEquals(setterName)
                && el.getReturnType().getKind() == TypeKind.VOID
                && el.getParameters().size() == 1
                && info.getTypes().isSameType(el.getParameters().iterator().next().asType(), propertyElement.asType());
    }

    public static boolean isGetter(CompilationInfo info, ExecutableElement el, Element propertyElement) {
        CodeStyle codeStyle = getCodeStyle(info);
        String getterName = CodeStyleUtils.computeGetterName(
                propertyElement.getSimpleName(),
                propertyElement.asType().getKind() == TypeKind.BOOLEAN,
                propertyElement.getModifiers().contains(Modifier.STATIC),
                codeStyle);
        return el.getSimpleName().contentEquals(getterName)
                && info.getTypes().isSameType(el.getReturnType(),propertyElement.asType())
                && el.getParameters().isEmpty();
    }
    
    public static String removeFieldPrefixSuffix(Element var, CodeStyle cs) {
        boolean isStatic = var.getModifiers().contains(Modifier.STATIC);
        return CodeStyleUtils.removePrefixSuffix(var.getSimpleName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }
    
    public static String addParamPrefixSuffix(CharSequence name, CodeStyle cs) {
        return CodeStyleUtils.addPrefixSuffix(name,
                cs.getParameterNamePrefix(),
                cs.getParameterNameSuffix());
    }

    public static String getTestMethodName(String propertyName) {
	return "test" + CodeStyleUtils.getCapitalizedName(propertyName); //NOI18N
    }

    public static boolean isWeakerAccess(Set<Modifier> modifiers, Set<Modifier> modifiers0) {
        return accessLevel(modifiers) < accessLevel(modifiers0);
    }

    private static int accessLevel(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PRIVATE)) {
            return 0;
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            return 2;
        }
        if (modifiers.contains(Modifier.PUBLIC)) {
            return 3;
        }
        return 1;
    }

    public static String getAccess(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PRIVATE)) {
            return "private"; //NOI18N
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            return "protected"; //NOI18N
        }
        if (modifiers.contains(Modifier.PUBLIC)) {
            return "public"; //NOI18N
        }
        return "<default>"; //NOI18N
    }

    public static boolean isFromTestRoot(FileObject file, ClassPath cp) {
        boolean inTest = false;
        if (cp != null) {
            FileObject root = cp.findOwnerRoot(file);
            if (root != null && UnitTestForSourceQuery.findSources(root).length > 0) {
                inTest = true;
            }
        }
        return inTest;
    }

    public static FileObject getRootFileObject(URL url) throws IOException {
        FileObject result = URLMapper.findFileObject(url);
        File f;
        try {
            f = result != null ? null : FileUtil.normalizeFile(Utilities.toFile(url.toURI())); //NOI18N
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        while (result == null && f != null) {
            result = FileUtil.toFileObject(f);
            f = f.getParentFile();
        }
        return result;
    }

    @SuppressWarnings("CollectionContainsUrl")
    public static ClassPath merge(final ClassPath... cps) {
        final Set<URL> roots = new LinkedHashSet<URL>(cps.length);
        for (final ClassPath cp : cps) {
            if (cp != null) {
                for (final ClassPath.Entry entry : cp.entries()) {
                    final URL root = entry.getURL();
                    if (!roots.contains(root)) {
                        roots.add(root);
                    }
                }
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
    }

    public static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && NbDocument.findRecentEditorPane(ec) != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the element is a method or constructor. Returns {@code false} for {@code null} input.
     * @param e element to check
     * @return true iff the element is a constructor or method.
     */
    public static boolean isExecutableElement(Element e) {
        if (e == null) {
            return false;
        }
        ElementKind ek = e.getKind();
        return ek == ElementKind.CONSTRUCTOR || ek == ElementKind.METHOD;
    }

    private RefactoringUtils() {
    }
}
