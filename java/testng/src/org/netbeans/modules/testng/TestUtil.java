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

package org.netbeans.modules.testng;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;


/**
 *
 * @author  rmatous
 * @author  Marian Petras
 * @version 1.1
 */
public class TestUtil extends CommonTestUtil {
    private static final String JAVA_SOURCES_SUFFIX               = "java";
    private static final String JAVA_MIME_TYPE = "text/x-java";         //NOI18N

    private static String getTestClassSuffix() {
        return TestNGSettings.TEST_CLASSNAME_SUFFIX;
    }
    
    private static String getTestClassPrefix() {
        return TestNGSettings.TEST_CLASSNAME_PREFIX;
    }
    
    private static String getTestSuiteSuffix() {
        return TestNGSettings.SUITE_CLASSNAME_SUFFIX;
    }
    
    private static String getTestSuitePrefix() {
        return TestNGSettings.SUITE_CLASSNAME_PREFIX;
    }
    
    private static String getRootSuiteName() {
        return TestNGSettings.getDefault().getRootSuiteClassName();
    }
    
    //
    // test class names    
    //
    public static String getTestClassFullName(String sourceClassName, String packageName) {
        String shortTestClassName = getTestClassName(sourceClassName);
        return ((packageName == null) || (packageName.length() == 0))
               ? shortTestClassName
               : packageName.replace('.','/') + '/' + shortTestClassName;
    }
    
    public static String getTestClassName(String sourceClassName) {
        return getTestClassPrefix() + sourceClassName + getTestClassSuffix();
    }
        
    
    //
    // suite class names
    //
    
    
    /**
     * Converts given package filename to test suite filename, e.g.
     * &quot;<tt>org/netbeans/foo</tt>&quot; -&gt;
     * &quot;<tt>org/netbeans/foo/{suite-prefix}Foo{suite-suffix}</tt>&quot;
     * @param packageFileName package filename in form of "org/netbeans/foo"
     */
    public static String convertPackage2SuiteName(String packageFileName) {
        if (packageFileName.length() == 0) {
            return getRootSuiteName();
        } else {
            int index = packageFileName.lastIndexOf('/');
            String pkg = index > -1 ? packageFileName.substring(index+1) : packageFileName;
            pkg = pkg.substring(0, 1).toUpperCase() + pkg.substring(1);
            return packageFileName + "/" + getTestSuitePrefix()+pkg+getTestSuiteSuffix();
        }
    }
    
    
    /**
     * Converts given class filename to test filename, e.g.
     * &quot;<tt>org/netbeans/Foo</tt>&quot;
     * -&gt; &quot;<tt>org/netbeans/{test-prefix}Foo{test-suffix}</tt>&quot;
     *
     * @param  classFileName  class filename in form of
     *                        &quot;<tt>org/netbeans/Foo</tt>&quot;,
     *                        i.e. without extension, no inner class
     */
    public static String convertClass2TestName(String classFileName) {
        int index = classFileName.lastIndexOf('/');
        String pkg = index > -1 ? classFileName.substring(0, index) : "";
        String clazz = index > -1 ? classFileName.substring(index+1) : classFileName;
        clazz = clazz.substring(0, 1).toUpperCase() + clazz.substring(1);
        if (pkg.length() > 0) {
            pkg += "/";
        }
        return pkg + getTestClassPrefix()+clazz+getTestClassSuffix();
    }

    /**
     * Show error message box. 
     */
    public static void notifyUser(String msg) {
        notifyUser(msg, NotifyDescriptor.ERROR_MESSAGE);
    }
    
    /**
     * Show message box of the specified severity. 
     */
    public static void notifyUser(String msg, int messageType) {
        NotifyDescriptor descr = new NotifyDescriptor.Message(msg, messageType);
        DialogDisplayer.getDefault().notify(descr);
    }


    
    // other misc methods

//    static public FileObject getFileObjectFromNode(Node node) {
//        DataObject      dO;
//        DataFolder      df;
//        
//        dO = node.getCookie(DataObject.class);
//        if (null != dO) {
//            return dO.getPrimaryFile();
//        }
//
//        df = node.getCookie(DataFolder.class);
//        if (null != df) {
//            return df.getPrimaryFile();
//        }
//        
////XXX: retouche
////        ClassElement ce = (ClassElement) node.getCookie(ClassElement.class);
////        if (null != ce) {
////            // find the parent DataObject, which node belongs to
////            while (null != (node = node.getParentNode())) {
////                if (null != (dO = (DataObject) node.getCookie(DataObject.class)))
////                    return dO.getPrimaryFile();
////            }
////        }
//        return null;
//    }

//    /**
//     */
//    static boolean isJavaFile(FileObject fileObj) {
//        return "java".equals(fileObj.getExt())                          //NOI18N
//               || "text/x-java".equals(FileUtil.getMIMEType(fileObj));  //NOI18N
//    }
    

        
    static boolean isClassTest(CompilationInfo compilationInfo,
                               TypeElement classElem) {
        return isClassImplementingTestInterface(compilationInfo, classElem);
    }
    
    // is JavaClass a Test class?
    static boolean isClassImplementingTestInterface(
                                            CompilationInfo compilationInfo,
                                            TypeElement classElem) {        
        String testIfaceFullName = "junit.framework.Test";              //NOI18N
        TypeElement testIface = compilationInfo.getElements()
                               .getTypeElement(testIfaceFullName);
        
        if (testIface == null) {
            String msg = "testng: TestUtil.isClassImplementingTestInterface(...) " //NOI18N
                         + "could not find TypeElement for "            //NOI18N
                         + testIfaceFullName;
            Logger.getLogger("global").log(Level.FINE, msg);         //NOI18N
            return false;
        }
        
        return compilationInfo.getTypes().isSubtype(classElem.asType(),
                                                    testIface.asType());
    }    
    
        
    
    // is class an exception


    static boolean isClassException(CompilationInfo compilationInfo,
                                    TypeElement classElem) {
        String throwableFullName = "java.lang.Throwable";               //NOI18N
        TypeElement throwable = compilationInfo.getElements()
                                .getTypeElement(throwableFullName);
        
        if (throwable == null) {
            String msg = "testng: TestUtil.isClassException(...) "       //NOI18N
                         + "could not find TypeElement for "            //NOI18N
                         + throwableFullName;
            Logger.getLogger("global").log(Level.SEVERE, msg);          //NOI18N
            return false;
        }
        
        return compilationInfo.getTypes().isSubtype(classElem.asType(),
                                                    throwable.asType());
    }


    /**
     * Finds a main class.
     *
     * @param  compInfo  defines scope in which the class is to be found
     * @param  className  name of the class to be found
     * @return  the found class; or <code>null</code> if the class was not
     *          found (e.g. because of a broken source file)
     */
    public static ClassTree findMainClass(final CompilationInfo compInfo) {
        final String className = compInfo.getFileObject().getName();
        
        CompilationUnitTree compUnitTree = compInfo.getCompilationUnit();
        String shortClassName = getSimpleName(className);
        for (Tree typeDecl : compUnitTree.getTypeDecls()) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                ClassTree clazz = (ClassTree) typeDecl;
                if (clazz.getSimpleName().toString().equals(shortClassName)) {
                    return clazz;
                }
            }
        }
        return null;
    }    
    
    /**
     * Converts filename to the fully qualified name of the main class
     * residing in the file.<br />
     * For example: <tt>test/myapp/App.java</tt> --&gt; <tt>test.myapp.App</tt>
     *
     * @param  filename
     * @return  corresponding package name. Null if the input is not
     *          well formed.
     */
    static String fileToClassName(String fileName) {
        if (fileName.endsWith(".java")) {                               //NOI18N
            return (fileName.substring(0, fileName.length()-5)).replace('/','.');
        } else {
            return null;
        }
    }

    /**
     * Returns full names of all primary Java classes
     * withing the specified folder (non-recursive).
     *
     * @param  packageFolder  folder to search
     * @param  classPath  classpath to be used for the search
     * @return  list of full names of all primary Java classes
     *          within the specified package
     */
    public static List<String> getJavaFileNames(FileObject packageFolder, ClasspathInfo cpInfo) {
        FileObject[] children = packageFolder.getChildren();
        if (children.length == 0) {
            return Collections.<String>emptyList();
        }
        
        final Collection<FileObject> javaFiles = new ArrayList<FileObject>(children.length);
        for (FileObject child : children) {
            if (!child.isFolder()
                    && child.isValid()
                    && child.getMIMEType().equals(JAVA_MIME_TYPE)) {
                javaFiles.add(child);
            }
        }
        if (javaFiles.isEmpty()) {
            return Collections.<String>emptyList();
        }

        FileObject[] javaFilesArr = (javaFiles.size() == children.length)
                                    ? children
                                    : javaFiles.toArray(new FileObject[0]);
        final JavaSource source = JavaSource.create(cpInfo, javaFilesArr);
        if (source == null) {
            ErrorManager.getDefault().log(
                    ErrorManager.EXCEPTION,
                    "Could not get JavaSource for files "               //NOI18N
                            + javaFilesArr);
            return Collections.<String>emptyList();
        }

        List<ElementHandle<TypeElement>> topClasses;
        try {
            topClasses = TopClassFinder.findMainTopClasses(source);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            topClasses = null;
        }
        if ((topClasses == null) || topClasses.isEmpty()) {
            return Collections.<String>emptyList();
        }
            
        final List<String> result = new ArrayList<String>(topClasses.size());
        for (ElementHandle<TypeElement> topClassHandle : topClasses) {
            result.add(topClassHandle.getQualifiedName());
        }
        return result.isEmpty() ? Collections.<String>emptyList() : result;
    }

//XXX: retouche
//    public static List filterFeatures(JavaClass cls, Class type) {
//        LinkedList ret = new LinkedList();
//        Iterator it = cls.getFeatures().iterator();
//
//        while (it.hasNext()) {
//            Feature f = (Feature)it.next();
//            if (type.isAssignableFrom(f.getClass())) ret.add(f);
//        }
//        return ret;
//    }

//XXX: retouche
//    public static Feature getFeatureByName(JavaClass src, Class cls, String name) {
//        if (!Feature.class.isAssignableFrom(cls)) throw new IllegalArgumentException("cls is not Feature");
//        
//        Iterator it = src.getFeatures().iterator();
//        while (it.hasNext()) {
//            Object o = it.next();
//            if (cls.isAssignableFrom(o.getClass())) {
//                Feature f = (Feature)o;
//                if (f.getName().equals(name)) return f;
//            }
//        }
//        return null;
//    }


//XXX: retouche
//    public static JavaClass getClassBySimpleName(JavaClass cls, String name) {
//        return cls.getInnerClass(name, false);
//    }

    public static String createNewName(int i, Set usedNames) {
        String ret;
        do {
            ret = "p" + i++;
        } while (usedNames.contains(ret));
        return ret;
    }

//XXX: retouche
//    static public Parameter cloneParam(Parameter p, JavaModelPackage pkg, int order, Set usedNames) {
//        String name = p.getName();
//        if (name == null || name.length()==0 || usedNames.contains(name)) {
//            name = createNewName(order, usedNames);
//        } 
//        usedNames.add(name);
//
//        
//        Parameter ret =
//            pkg.getParameter().
//            createParameter(name,
//                            p.getAnnotations(), 
//                            p.isFinal(),
//                            null,
//                            0,//p.getDimCount(),
//                            p.isVarArg());
//        ret.setType(p.getType());
//        return ret;
//    }

//XXX: retouche
//    public static List cloneParams(List params, JavaModelPackage pkg) {
//        Iterator origParams = params.iterator();
//        List newParams = new LinkedList();
//        int o = 0; 
//        HashSet usedNames = new HashSet(params.size()*2);
//        while (origParams.hasNext()) {
//            Parameter p = (Parameter)origParams.next();
//            newParams.add(TestUtil.cloneParam(p, pkg, o++, usedNames));
//        }
//        return newParams;
//    }


//XXX: retouche
//    /**
//     * Gets collection of types of the parameters passed in in the
//     * argument. The returned collection has the same size as the
//     * input collection.
//     * @param params List<Parameter>
//     * @return List<Type> 
//     */
//    static public List getParameterTypes(List params) {
//        List ret = new ArrayList(params.size());
//        Iterator it = params.iterator();
//        while (it.hasNext()) {
//            ret.add(((Parameter)it.next()).getType());
//        }
//        return ret;
//    }



//XXX: retouche
//    /**
//     * Gets list of all features within the given class of the given
//     * class and modifiers.
//     * @param c the JavaClass to search
//     * @param cls the Class to search for
//     * @param modifiers the modifiers to search for
//     * @param recursive if true, the search descents to superclasses
//     *                  and interfaces
//     * @return List of the collected Features
//     */
//    public static List collectFeatures(JavaClass c, Class cls, 
//                                   int modifiers, boolean recursive) {
//
//        return collectFeatures(c, cls, modifiers, recursive, new LinkedList(), new HashSet());
//    }
        



//XXX: retouche
//    private static List collectFeatures(JavaClass c, Class cls, 
//                                   int modifiers, boolean recursive, 
//                                   List list, Set visited ) 
//    {
//
//	if (!visited.add(c)) return list;
//        // this class
//        
//        int mo = (c.isInterface()) ? Modifier.ABSTRACT : 0;
//        Iterator it = TestUtil.filterFeatures(c, cls).iterator();
//        while (it.hasNext()) {
//            Feature m = (Feature)it.next();
//            if (((m.getModifiers() | mo) & modifiers) == modifiers) {
//                list.add(m);
//            }
//        }
//
//        if (recursive) {
//            // super
//            JavaClass sup = c.getSuperClass();
//            if (sup != null) collectFeatures(sup, cls, modifiers, recursive, list, visited);
//
//            // interfaces
//            Iterator ifaces = c.getInterfaces().iterator();
//            while (ifaces.hasNext()) collectFeatures((JavaClass)ifaces.next(), cls,
//                                                     modifiers, recursive, list, visited);
//        }
//
//        return list;
//    }

//XXX: retouche
//    public static boolean hasMainMethod(JavaClass cls) {
//
//        JavaModelPackage pkg = (JavaModelPackage)cls.refImmediatePackage();  
//        return cls.getMethod("main", 
//                             Collections.singletonList(pkg.getArray().resolveArray(TestUtil.getStringType(pkg))),
//                             false) != null;
//
//    }
        
//XXX: retouche
//    public static Type getStringType(JavaModelPackage pkg) {
//        return pkg.getType().resolve("java.lang.String");
//    }

//XXX: retouche
//    public static TypeReference getTypeReference(JavaModelPackage pkg, String name) {
//        return pkg.getMultipartId().createMultipartId(name, null, Collections.EMPTY_LIST);
//    }

//    /**
//     * Finds <code>SourceGroup</code>s where a test for the given class
//     * can be created (so that it can be found by the projects infrastructure
//     * when a test for the class is to be opened or run).
//     *
//     * @param  fileObject  <code>FileObject</code> to find target
//     *                     <code>SourceGroup</code>(s) for
//     * @return  an array of objects - each of them can be either
//     *          a <code>SourceGroup</code> for a possible target folder
//     *          or simply a <code>FileObject</code> representing a possible
//     *          target folder (if <code>SourceGroup</code>) for the folder
//     *          was not found);
//     *          the returned array may be empty but not <code>null</code>
//     * @author  Marian Petras
//     */
//    public static Object[] getTestTargets(FileObject fileObject) {
//        
//        /* .) get project owning the given FileObject: */
//        final Project project = FileOwnerQuery.getOwner(fileObject);
//        if (project == null) {
//            return new Object[0];
//        }
//        
//        SourceGroup sourceGroupOwner = findSourceGroupOwner(fileObject);
//        if (sourceGroupOwner == null) {
//            return new Object[0];
//        }
//        
//        /* .) get URLs of target SourceGroup's roots: */
//        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(
//                                       sourceGroupOwner.getRootFolder());
//        if (rootURLs.length == 0) {
//            return new Object[0];
//        }
//        
//        /* .) convert the URLs to FileObjects: */
//        boolean someSkipped = false;
//        FileObject[] sourceRoots = new FileObject[rootURLs.length];
//        for (int i = 0; i < rootURLs.length; i++) {
//            if ((sourceRoots[i] = URLMapper.findFileObject(rootURLs[i]))
//                    == null) {
//                ErrorManager.getDefault().notify(
//                        ErrorManager.INFORMATIONAL,
//                        new IllegalStateException(
//                           "No FileObject found for the following URL: "//NOI18N
//                           + rootURLs[i]));
//                someSkipped = true;
//                continue;
//            }
//            if (FileOwnerQuery.getOwner(sourceRoots[i]) != project) {
//                ErrorManager.getDefault().notify(
//                        ErrorManager.INFORMATIONAL,
//                        new IllegalStateException(
//                    "Source root found by FileOwnerQuery points "       //NOI18N
//                    + "to a different project for the following URL: "  //NOI18N
//                    + rootURLs[i]));
//                sourceRoots[i] = null;
//                someSkipped = true;
//                continue;
//            }
//        }
//        
//        if (someSkipped) {
//            FileObject roots[] = skipNulls(sourceRoots, new FileObject[0]);
//            if (roots.length == 0) {
//                return new Object[0];
//            }
//            sourceRoots = roots;
//        }
//        
//        /* .) find SourceGroups corresponding to the FileObjects: */
//        final Object[] targets = new Object[sourceRoots.length];
//        Map<FileObject,SourceGroup> map = getFileObject2SourceGroupMap(project);
//        for (int i = 0; i < sourceRoots.length; i++) {
//            SourceGroup srcGroup = map.get(sourceRoots[i]);
//            targets[i] = srcGroup != null ? srcGroup : sourceRoots[i];
//        }
//        return targets;
//    }
    
//    /**
//     * Finds a <code>SourceGroup</code> the given file belongs to.
//     * Only Java <code>SourceGroup</code>s are taken into account.
//     *
//     * @param  file  <code>FileObject</code> whose owning
//     *               <code>SourceGroup</code> to be found
//     * @return  Java <code>SourceGroup</code> containing the given
//     *          file; or <code>null</code> if no such
//     *          <code>SourceGroup</code> was found
//     * @author  Marian Petras
//     */
//    public static SourceGroup findSourceGroupOwner(FileObject file) {
//        final Project project = FileOwnerQuery.getOwner(file);
//        return findSourceGroupOwner(project, file);
//    }
//    
//    /**
//     * Finds a <code>SourceGroup</code> the given file belongs to.
//     * Only Java <code>SourceGroup</code>s are taken into account. 
//     *
//     * @param project the <code>Project</code> the file belongs to
//     * @param  file  <code>FileObject</code> whose owning
//     *               <code>SourceGroup</code> to be found
//     * @return  Java <code>SourceGroup</code> containing the given
//     *          file; or <code>null</code> if no such
//     *          <code>SourceGroup</code> was found
//     */
//
//    public static SourceGroup findSourceGroupOwner(Project project, FileObject file) {        
//        final SourceGroup[] sourceGroups
//                = new Utils(project).getJavaSourceGroups();
//        for (int i = 0; i < sourceGroups.length; i++) {
//            SourceGroup srcGroup = sourceGroups[i];
//            FileObject root = srcGroup.getRootFolder();
//            if (((file==root)||(FileUtil.isParentOf(root,file))) && 
//                 srcGroup.contains(file)) {
//                return srcGroup;
//            }
//        }
//        return null;
//    }
    
//    /**
//     * Finds all <code>SourceGroup</code>s of the given project
//     * containing a class of the given name.
//     *
//     * @param  project  project to be searched for matching classes
//     * @param  className  class name pattern
//     * @return  unmodifiable collection of <code>SourceGroup</code>s
//     *          which contain files corresponding to the given name
//     *          (may be empty but not <code>null</code>)
//     * @author  Marian Petras
//     */
//    public static Collection<SourceGroup> findSourceGroupOwners(
//            final Project project,
//            final String className) {
//        final SourceGroup[] sourceGroups
//                = new Utils(project).getJavaSourceGroups();
//        if (sourceGroups.length == 0) {
//            return Collections.<SourceGroup>emptyList();
//        }
//        
//        final String relativePath = className.replace('.', '/')
//                                    + ".java";                          //NOI18N
//        
//        ArrayList<SourceGroup> result = new ArrayList<SourceGroup>(4);
//        for (int i = 0; i < sourceGroups.length; i++) {
//            SourceGroup srcGroup = sourceGroups[i];
//            FileObject root = srcGroup.getRootFolder();
//            FileObject file = root.getFileObject(relativePath);
//            if (file != null && FileUtil.isParentOf(root, file)
//                             && srcGroup.contains(file)) {
//                result.add(srcGroup);
//            }
//        }
//        if (result.isEmpty()) {
//            return Collections.<SourceGroup>emptyList();
//        }
//        result.trimToSize();
//        return Collections.unmodifiableList(result);
//    }
    
//    /**
//     * Creates a copy of the given array, except that <code>null</code> objects
//     * are omitted.
//     * The length of the returned array is (<var>l</var> - <var>n</var>), where
//     * <var>l</var> is length of the passed array and <var>n</var> is number
//     * of <code>null</code> elements of the array. Order of
//     * non-<code>null</code> elements is kept in the returned array.
//     * The returned array is always a new array, even if the passed
//     * array does not contain any <code>null</code> elements.
//     *
//     * @param  objs  array to copy
//     * @param  type  an empty array of the correct type to be returned
//     * @return  array containing the same objects as the passed array, in the
//     *          same order, just with <code>null</code> elements missing
//     * @author  Marian Petras
//     */
//    public static <T> T[] skipNulls(final T[] objs, final T[] type) {
//        List<T> resultList = new ArrayList<T>(objs.length);
//        
//        for (int i = 0; i < objs.length; i++) {
//            if (objs[i] != null) {
//                resultList.add(objs[i]);
//            }
//        }
//        
//        return resultList.toArray(type);
//    }
    
//    /**
//     * Creates a map from folders to <code>SourceGroup</code>s of a given
//     * project.
//     * The map allows to ascertian for a given folder
//     * which <code>SourceGroup</code> it is a root folder of.
//     *
//     * @param  project  project whose <code>SourceGroup</code>s should be in the
//     *                  returned map
//     * @return  map from containing all <code>SourceGroup</code>s of a given
//     *          project, having their root folders as keys
//     * @author  Marian Petras
//     */
//    public static Map<FileObject,SourceGroup> getFileObject2SourceGroupMap(
//                                                              Project project) {
//        final SourceGroup[] sourceGroups
//                = new Utils(project).getJavaSourceGroups();
//        
//        if (sourceGroups.length == 0) {
//            return Collections.<FileObject,SourceGroup>emptyMap();
//        } else if (sourceGroups.length == 1) {
//            return Collections.singletonMap(sourceGroups[0].getRootFolder(),
//                                            sourceGroups[0]);
//        } else {
//            Map<FileObject,SourceGroup> map;
//            map = new HashMap<FileObject,SourceGroup>(
//                    Math.round(sourceGroups.length * 1.4f + .5f),
//                               .75f);
//            for (int i = 0; i < sourceGroups.length; i++) {
//                map.put(sourceGroups[i].getRootFolder(),
//                        sourceGroups[i]);
//            }
//            return map;
//        }
//    }

    // Nice copy of useful methods (Taken from JavaModule)
    public static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     *
     */
    public static TestNGPlugin getPluginForProject(final Project project) {
        Object pluginObj = project.getLookup().lookup(TestNGPlugin.class);
        return (pluginObj != null) ? (TestNGPlugin) pluginObj
                                   : new DefaultPlugin();
    }

//    /**
//     * Creates a map of parameters according to the current TestNG module
//     * settings.<br />
//     * Note: The map may not contain all the necessary settings,
//     *       i.g. name of a test class is missing.
//     *
//     * @param  multipleFiles  if {@code true}, the map should contain
//     *                        also settings need for creation of multiple
//     *                        tests
//     * @return  map of settings to be used by a
//     *          {@link org.netbeans.modules.gsf.testrunner.plugin TestNGPlugin}
//     * @see  org.netbeans.modules.testng.ui.TestNGPlugin
//     */
//    public static Map<CreateTestParam, Object> getSettingsMap(
//            boolean multipleFiles) {
//        final TestNGSettings settings = TestNGSettings.getDefault();
//        final Map<CreateTestParam, Object> params
//                    = new HashMap<CreateTestParam, Object>(17);
//        
//        params.put(CreateTestParam.INC_PUBLIC,
//                   Boolean.valueOf(settings.isMembersPublic()));
//        params.put(CreateTestParam.INC_PROTECTED,
//                   Boolean.valueOf(settings.isMembersProtected()));
//        params.put(CreateTestParam.INC_PKG_PRIVATE,
//                   Boolean.valueOf(settings.isMembersPackage()));
//        params.put(CreateTestParam.INC_CODE_HINT,
//                   Boolean.valueOf(settings.isBodyComments()));
//        params.put(CreateTestParam.INC_METHOD_BODIES,
//                   Boolean.valueOf(settings.isBodyContent()));
//        params.put(CreateTestParam.INC_JAVADOC,
//                   Boolean.valueOf(settings.isJavaDoc()));
//        
//        if (multipleFiles) {
//            params.put(CreateTestParam.INC_GENERATE_SUITE,
//                       Boolean.valueOf(settings.isGenerateSuiteClasses()));
//            params.put(CreateTestParam.INC_PKG_PRIVATE_CLASS,
//                    Boolean.valueOf(settings.isIncludePackagePrivateClasses()));
//            params.put(CreateTestParam.INC_ABSTRACT_CLASS,
//                       Boolean.valueOf(settings.isGenerateAbstractImpl()));
//            params.put(CreateTestParam.INC_EXCEPTION_CLASS,
//                       Boolean.valueOf(settings.isGenerateExceptionClasses()));
//        }
//        else {
//            // If a class is explicitly selected then corresponding test class
//            // should be generated in any cases.
//            params.put(CreateTestParam.INC_PKG_PRIVATE_CLASS,
//                       Boolean.valueOf(true));
//            params.put(CreateTestParam.INC_ABSTRACT_CLASS,
//                       Boolean.valueOf(true));
//            params.put(CreateTestParam.INC_EXCEPTION_CLASS,
//                       Boolean.valueOf(true));
//        }
//        
//        params.put(CreateTestParam.INC_SETUP,
//                   Boolean.valueOf(settings.isGenerateSetUp()));
//        params.put(CreateTestParam.INC_TEAR_DOWN,
//                   Boolean.valueOf(settings.isGenerateTearDown()));
//        
//        return params;
//    }
    
    /**
     *
     */
    static String getPackageName(String fullName) {
        int i = fullName.lastIndexOf('.');
        return (i != -1) ? fullName.substring(0, i)
                         : "";                                          //NOI18N
    }

    /**
     * Gets the last part of a fully qualified Java name.
     */
    static String getSimpleName(String fullName) {
        if(fullName == null) { // #166459
            return null;
        }
        int lastDotIndex = fullName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fullName
                                    : fullName.substring(lastDotIndex + 1);
    }

    private TestUtil() {
    }

    /**
     * Determines source level of the given file.
     * 
     * @param  file  file whose source level is to be determined
     * @return  string denoting source level of the given file
     *          (e.g. <code>&quot;1.5&quot;</code>)
     *          or {@code null} if the source level could not be determined
     */
    static String getSourceLevel(FileObject file) {
        ClassPath srcCP = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (srcCP == null) {
            return null;
        }
        
        FileObject srcRoot = srcCP.findOwnerRoot(file);
        if (srcRoot == null) {
            return null;
        }
        
        return SourceLevelQuery.getSourceLevel(srcRoot);
    }

    /**
     * Checks whether Java annotations are applicable in the given file.
     * The result is {@code true} if source level of the given is known
     * and the source level is 1.5 or higher.
     * 
     * @param  file  file to be checked
     * @return  {@code true} if the given file is known to be based on
     *          JDK 1.5 or higher; {@code false} otherwise
     * @see  #getSourceLevel
     * @see  #areAnnotationsSupported(String)
     */
    static boolean areAnnotationsSupported(FileObject file) {
        return areAnnotationsSupported(getSourceLevel(file));
    }

    /**
     * Determines whether Java annotations are supported by the given
     * Java source level.
     * 
     * @param  sourceLevel  Java source level (e.g. <code>&quot;1.5&quot;</code>),
     *                      or {@code null} if the source level is unknown
     * @return  {@code true} if the source level is known and Java annotations
     *          are supported in the source level, {@code false} otherwise
     * @see  #areAnnotationsSupported(FileObject)
     */
    static boolean areAnnotationsSupported(String sourceLevel) {
        return (sourceLevel != null)
                && (sourceLevel.compareTo("1.5") >= 0);                 //NOI18N
    }
    
}
