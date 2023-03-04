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
package org.netbeans.modules.profiler.nbimpl.javac;

import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.utils.VMUtils;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * These methods should probably be moved to org.netbeans.api.java.source.ElementUtilities class
 * @author Jaroslav Bachorik
 */
public class ElementUtilitiesEx {
    private static final String VM_CONSTRUCTUR_SIG = "<init>"; // NOI18N
    private static final String VM_INITIALIZER_SIG = "<clinit>"; // NOI18N
    private static final Logger LOG = Logger.getLogger(ElementUtilitiesEx.class.getName());
    
    public static String getBinaryName(ExecutableElement method, CompilationInfo ci) {
        try {
            switch (method.getKind()) {
                case METHOD:
                case CONSTRUCTOR:
                case STATIC_INIT:

                    //case INSTANCE_INIT: // not supported
                    String paramsVMSignature = getParamsSignature(method.getParameters(), ci);
                    String retTypeVMSignature = VMUtils.typeToVMSignature(getRealTypeName(method.getReturnType(), ci));

                    return "(" + paramsVMSignature + ")" + retTypeVMSignature; //NOI18N
                default:
                    return null;
            }

        } catch (IllegalArgumentException e) {
            ProfilerLogger.warning(e.getMessage());
        }

        return null;
    }

    /**
     * Resolves a class by its name
     * @param className The name of the class to be resolved
     * @param cpInfo The classpath info used to resolve the class
     * @param fuzzy Indicates whether in case of an unresolvable anonymous inner class the parent class should be returned instead
     * @return Returns a handle representing the resolved class or NULL
     */
    public static ElementHandle<TypeElement> resolveClassByName(
            final String className, final ClasspathInfo cpInfo, final boolean fuzzy) {
        if (className == null || cpInfo == null) {
            return null;
        }
        
        final ElementHandle<TypeElement>[] rslt = new ElementHandle[1];
        
        ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>() {

            @Override
            public void run(CompilationController cc) throws Exception {
                TypeElement te = resolveClassByName(className, cc, fuzzy);
                if (te != null) {
                    rslt[0] = ElementHandle.create(te);
                }
            }

            @Override
            public boolean shouldRetry() {
                return rslt[0] == null;
            }
        });
        
        return rslt[0];
    }
    
    /**
     * Resolves a class by its name
     * @param className The name of the class to be resolved
     * @param controller The compilation controller to be used to resolve the class
     * @param fuzzy Indicates whether in case of an unresolvable anonymous inner class the parent class should be returned instead
     * @return Returns a TypeElement representing the resolved class or NULL
     */
    @NbBundle.Messages("MDRUtils_ClassNotResolvedMessage=Can not resolve class {0}")
    public static TypeElement resolveClassByName(
            final String className, final CompilationController controller, final boolean fuzzy) {
        if ((className == null) || (controller == null)) {
            return null;
        }

        // 1. try to resolve the class
        TypeElement mainClass = controller.getElements().getTypeElement(className.replace('$', '.')); // NOI18N
        
        if (mainClass == null) {
            // 2. probably an anonymous inner class; use a pinch of black magic to resolve it
            try {
                int innerIndex = className.indexOf('$');
                if (innerIndex > -1) {
                    FileObject fo = null;
                    String topClassName = className.substring(0, innerIndex); // NOI18N
                    mainClass = controller.getElements().getTypeElement(topClassName);

                    if (mainClass != null) {
                        fo = SourceUtils.getFile(ElementHandle.create(mainClass), controller.getClasspathInfo());
                    }
                    TypeElement anon;
                    if (fo != null) {
                        anon = getAnonymousFromSource(fo, className);
                        
                    } else {
                        anon = getAnonymousFromBinary(controller, className);
                        mainClass = (anon );
                    }
                    mainClass = (anon == null && fuzzy) ? mainClass : anon;
                }
            } catch (IOException e) {
                ProfilerLogger.log(e);
            }

        }

        if (mainClass != null) {
            ProfilerLogger.debug("Resolved: " + mainClass); // NOI18N
        } else {
            ProfilerLogger.debug("Could not resolve: " + className); // NOI18N
        }

        if (mainClass == null) {
            StatusDisplayer.getDefault().setStatusText(Bundle.MDRUtils_ClassNotResolvedMessage(className)); // notify user
        }

        return mainClass;
    }

    private static TypeElement getAnonymousFromSource(FileObject fo, final String className) throws IllegalArgumentException, IOException {
        final TypeElement[] resolvedClassElement = new TypeElement[1];
        JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(final CompilationController cc) throws Exception {
                    cc.toPhase(Phase.RESOLVED);
                    new ErrorAwareTreePathScanner<Void, Void>() {

                        @Override
                        public Void visitClass(ClassTree node, Void p) {
                            TypeElement te = (TypeElement)cc.getTrees().getElement(getCurrentPath());
                            if (te != null) {
                                if (className.equals(ElementUtilities.getBinaryName(te))) {
                                    resolvedClassElement[0] = te;
                                }
                            }
                            return super.visitClass(node, p);
                        }

                    }.scan(cc.getCompilationUnit(), null);
                }
            }, true);
        }
        return resolvedClassElement[0];
    }

    private static TypeElement getAnonymousFromBinary(CompilationController controller, final String className) throws IOException {
        String resPath = className.replace('.', '/') + ".class"; // NOI18N
        FileObject fo = controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT).findResource(resPath);
        if (fo == null) {
            fo = controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resPath);
        }
        if (fo == null) {
            fo = controller.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE).findResource(resPath);
        }
        if (fo != null) {
            final TypeElement[] resolvedClassElement = new TypeElement[1];
            JavaSource js = JavaSource.forFileObject(fo);
            if (js != null) {
                js.runUserActionTask(new Task<CompilationController>(){

                    @Override
                    public void run(CompilationController cc) throws Exception {
                        for(TypeElement te : cc.getTopLevelElements()) {
                            if (ElementUtilities.getBinaryName(te).equals(className)) {
                                resolvedClassElement[0] = te;
                                break;
                            }
                        }
                    }
                }, true);
                return resolvedClassElement[0];
            }
        }
        return null;
    }
    
    public static Set<ElementHandle<TypeElement>> findImplementors(final ClasspathInfo cpInfo, final ElementHandle<TypeElement> baseType) {
        final Set<ClassIndex.SearchKind> kind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
        final Set<ClassIndex.SearchScope> scope = EnumSet.allOf(ClassIndex.SearchScope.class);
        
        final Set<ElementHandle<TypeElement>> allImplementors = new HashSet<ElementHandle<TypeElement>>();

        ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>(true) {
            @Override
            public void run(CompilationController cc) {
                Set<ElementHandle<TypeElement>> implementors = cpInfo.getClassIndex().getElements(baseType, kind, scope);
                do {
                    Set<ElementHandle<TypeElement>> tmpImplementors = new HashSet<ElementHandle<TypeElement>>();
                    allImplementors.addAll(implementors);

                    for (ElementHandle<TypeElement> element : implementors) {
                        tmpImplementors.addAll(cpInfo.getClassIndex().getElements(element, kind, scope));
                    }

                    implementors = tmpImplementors;
                } while (!implementors.isEmpty());
            }
        });
        
        return allImplementors;
    }

    public static Set<TypeElement> findImplementorsResolved(final ClasspathInfo cpInfo, final ElementHandle<TypeElement> baseType) {
        final Set<TypeElement> implementors = new HashSet<TypeElement>();
        final Set<ElementHandle<TypeElement>> implHandles = findImplementors(cpInfo, baseType);
        
        if (!implHandles.isEmpty()) {
            ParsingUtils.invokeScanSensitiveTask(cpInfo, new ScanSensitiveTask<CompilationController>(true) {
                public void run(CompilationController controller)
                        throws Exception {
                    if (controller.toPhase(Phase.ELEMENTS_RESOLVED).compareTo(Phase.ELEMENTS_RESOLVED) < 0) {
                        return;
                    }

                    for(ElementHandle<TypeElement> eh : implHandles) {
                        implementors.add(eh.resolve(controller));
                    }

                }
            });
        }

        return implementors;
    }
    
    /**
     * Resolves a method by its name, signature and parent class
     * @param parentClass The parent class
     * @param methodName The method name
     * @param signature The VM signature of the method
     * @return Returns an ExecutableElement representing the method or null
     */
    public static ExecutableElement resolveMethodByName(CompilationInfo ci,
            TypeElement parentClass, String methodName, String signature) {
        // TODO: static initializer
        if ((parentClass == null) || (methodName == null)) {
            return null;
        }

        ExecutableElement foundMethod = null;
        boolean found = false;

        List<ExecutableElement> methods;

        if (methodName.equals(VM_CONSTRUCTUR_SIG)) {
            methods = ElementFilter.constructorsIn(ci.getElements().getAllMembers(parentClass));

        //    } else if (methodName.equals(VM_INITIALIZER_SIG)) {
        //      methods = constructorsIn(parentClass.getEnclosedElements());
        } else {
            // retrieve all defined methods
            methods = ElementFilter.methodsIn(ci.getElements().getAllMembers(parentClass));
        }

// loop over all methods
        for (ExecutableElement method : methods) {
            // match the current method against the required method name and signature
            if (methodNameMatch(methodName, method)) {
                if (signature != null && methodSignatureMatch(ci, signature, method)) {
                    foundMethod = method;
                    found = true;
                    break;
                }
                foundMethod = method; // keeping the track of the closest match
            }
        }

        if (!found) {
            ProfilerLogger.debug("Could not find exact signature match, opening at first method with same name: " + foundMethod); // NOI18N
        }

        return foundMethod;
    }
    
    // ***

    private static String getParamsSignature(List<? extends VariableElement> params, CompilationInfo ci) {
        StringBuilder ret = new StringBuilder();
        Iterator<? extends VariableElement> it = params.iterator();

        while (it.hasNext()) {
            TypeMirror type = it.next().asType();
            String realTypeName = getRealTypeName(type, ci);
            String typeVMSignature = VMUtils.typeToVMSignature(realTypeName);
            ret.append(typeVMSignature);
        }

        return ret.toString();
    }

    private static String getRealTypeName(TypeMirror type, CompilationInfo ci) {
        final TypeMirror et = ci.getTypes().erasure(type);
        if (et.getKind() == TypeKind.DECLARED) {
            return ElementUtilities.getBinaryName((TypeElement)((DeclaredType)et).asElement());
        }
        if (et.getKind() == TypeKind.ARRAY) {
            return getRealTypeName(((ArrayType)et).getComponentType(), ci) + "[]";  // NOI18N
        }
        return et.toString();
    }    
        /**
     * Returns the JavaSource repository for given source roots
     */
    private static JavaSource getSources(FileObject[] roots) {
        // create the javasource repository for all the source files
        return JavaSource.create(getClasspathInfo(roots), Collections.<FileObject>emptyList());
    }
    
    /**
     * Create ClassPathInfo for JavaSources only -> (bootPath, classPath, sourcePath)
     * @param roots Source roots
     * @return 
     */
    private static ClasspathInfo getClasspathInfo(FileObject[] roots) {
        ClassPath srcPath;
        ClassPath bootPath;

        ClassPath compilePath;

        final ClassPath cpEmpty = ClassPathSupport.createClassPath(new FileObject[0]);
        
        if (roots == null || roots.length == 0) {
            Set<ClassPath> paths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
            srcPath = ClassPathSupport.createProxyClassPath(paths.toArray(new ClassPath[0]));
            bootPath = JavaPlatform.getDefault().getBootstrapLibraries();
            paths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
            compilePath = ClassPathSupport.createProxyClassPath(paths.toArray(new ClassPath[0]));
        } else {
            srcPath = ClassPathSupport.createClassPath(roots);
            bootPath =
                    ClassPath.getClassPath(roots[0], ClassPath.BOOT);
            compilePath =
                    ClassPath.getClassPath(roots[0], ClassPath.COMPILE);
        }
        
        return ClasspathInfo.create(bootPath != null ? bootPath : cpEmpty, compilePath != null ? compilePath : cpEmpty, srcPath);
    }
    
    /**
     * Returns the JavaSource repository of a given project or global JavaSource if no project is provided
     */
    public static JavaSource getSources(Project project) {
        if (project == null) {
            return getSources((FileObject[]) null);
        } else {
            return getSources(ProjectUtilities.getSourceRoots(project, true));
        }
    }
    
    /**
     * Compares the desired textual method name with a name of particualt executable element (method, constructor ...)
     * @param vmName The name to match against. Can be a real method name, "<init>" or "<cinit>"
     * @param ee The executable element to use in matching
     * @return Returns true if the given textual name matches the name of the executable element
     */
    private static boolean methodNameMatch(final String vmName,
            final ExecutableElement ee) {
        switch (ee.getKind()) {
            // for method use textual name matching
            case METHOD:
                return ee.getSimpleName().contentEquals(vmName);

            // for constructor use the special <init> name
            case CONSTRUCTOR:
                return vmName.equals(VM_CONSTRUCTUR_SIG);

            // for initializer use the special <cinit> name
            case STATIC_INIT:
            case INSTANCE_INIT:
                return vmName.equals(VM_INITIALIZER_SIG);
        }

// default fail-over
        return false;
    }

    /**
     * Compares the desired textual representation of a VM signature with a VM signature of the provided ExecutableElement (method, constructor ...)
     * @param vmSig The desired VM signature
     * @param ee The executable element to compare the signature to (method, constructor ...)
     * @return Returns true if the signature of the executable element matches the desired signature
     */
    private static boolean methodSignatureMatch(CompilationInfo ci, final String vmSig, final ExecutableElement ee) {
        return getBinaryName(ee,ci).equals(vmSig);
    }
}
