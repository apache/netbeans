/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import org.netbeans.modules.profiler.nbimpl.javac.JavacMethodInfo;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ClasspathInfoFactory;
import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import org.netbeans.modules.profiler.spi.java.AbstractJavaProfilerSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Bachorik
 */
// registered in layer.xml as the annotation registration does not allow
// for specifying the service type explicitly, thus causing unnecessary
// class load when looking up services from Mime lookup
// @MimeRegistration(mimeType = "text/x-java", service = AbstractJavaProfilerSource.class)
public class JavaProfilerSourceImpl implements AbstractJavaProfilerSource {
    
    private static final String JUNIT_SUITE = "junit.framework.TestSuite"; // NOI18N
    private static final String JUNIT_TEST = "junit.framework.Test"; // NOI18N
    private static final String[] APPLET_CLASSES = new String[]{"java.applet.Applet", "javax.swing.JApplet"}; // NOI18N
    private static final String[] TEST_CLASSES = new String[]{JUNIT_SUITE, JUNIT_TEST};
    private static final String[] TEST_ANNOTATIONS = new String[]{"org.junit.Test", "org.junit.runners.Suite", "org.testng.annotations.Test"}; // NOI18N
    private static final Logger LOG = Logger.getLogger(JavaProfilerSourceImpl.class.getName());
    
    @Override
    public SourceClassInfo getEnclosingClass(FileObject fo, final int position) {
        final SourceClassInfo[] result = new SourceClassInfo[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }
        
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController cc)
                        throws Exception {
                    if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) < 0) {
                        return;
                    }

                    TypeElement parentClass = cc.getTreeUtilities().scopeFor(position).getEnclosingClass();

                    if (parentClass != null) {
                        result[0] = new JavacClassInfo(ElementHandle.create(parentClass), cc);
                    }
                }
            }, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result[0];
    }

    @Override
    public SourceMethodInfo getEnclosingMethod(FileObject fo, final int position) {
        final SourceMethodInfo[] result = new SourceMethodInfo[1];

        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController cc)
                        throws Exception {
                    if (cc.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                        return;
                    }

                    ExecutableElement parentMethod = cc.getTreeUtilities().scopeFor(position).getEnclosingMethod();

                    if (parentMethod != null) {
                        result[0] = new JavacMethodInfo(parentMethod, cc);
                    }
                }
            }, true);
        } catch (IOException ex) {
            ProfilerLogger.log(ex);
        }

        return result[0];
    }

    @Override
    public Set<SourceClassInfo> getMainClasses(final FileObject fo) {
        final Set<SourceClassInfo> mainClasses = new HashSet<SourceClassInfo>();
        
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return Collections.EMPTY_SET;
        }
        
        ClasspathInfo cpInfo = ClasspathInfoFactory.infoFor(p);
        for(ElementHandle<TypeElement> handle : SourceUtils.getMainClasses(fo)) {
            mainClasses.add(new JavacClassInfo(handle, cpInfo));
            
        }        
        
        return mainClasses;
    }

    @Override
    public Set<SourceMethodInfo> getConstructors(FileObject fo) {
        final Set<SourceMethodInfo> constructors = new HashSet<SourceMethodInfo>();
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController cc)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (cc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                        public Void visitMethod(MethodTree node, Void p) {
                            Void retValue;
                            ExecutableElement method = (ExecutableElement) cc.getTrees().getElement(getCurrentPath());
                            constructors.add(new JavacMethodInfo(method, cc));
                            retValue = super.visitMethod(node, p);

                            return retValue;
                        }
                    };

                    scanner.scan(cc.getCompilationUnit(), null);
                }
            }, true);
        } catch (IOException e) {
            ProfilerLogger.log(e);
        }

        return constructors;
    }

    @Override
    public Set<SourceClassInfo> getClasses(FileObject fo) {
        final Set<SourceClassInfo> result = new HashSet<SourceClassInfo>();
        
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                @Override
                public void cancel() {
                }

                @Override
                public void run(final CompilationController cc)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo(JavaSource.Phase.ELEMENTS_RESOLVED) < 0) {
                        return;
                    }

                    TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                        @Override
                        public Void visitClass(ClassTree node, Void param) {
                            try {
                                TypeElement te = (TypeElement)cc.getTrees().getElement(getCurrentPath());
                                result.add(new JavacClassInfo(ElementHandle.create(te), cc));
                            } catch (NullPointerException e) {
                                ProfilerLogger.log(e);
                            }
                            return null;
                        }
                    };

                    scanner.scan(cc.getCompilationUnit(), null);
                }
            }, true);
        } catch (IOException ex) {
            ProfilerLogger.log(ex);
        }

        return result;
    }    
    
    @Override
    public SourceClassInfo getTopLevelClass(FileObject fo) {
        String fName = fo.getName();
        for(SourceClassInfo ci : getClasses(fo)) {
            if (ci.getSimpleName().equals(fName)) {
                return ci;
            }
        }
        return null;
    }

    @Override
    public boolean hasAnnotation(FileObject fo, final String[] annotationNames, boolean allRequired) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);

        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return false;
        }
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                    // do nothing
                }

                public void run(final CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);

                    TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {

                        @Override
                        public Void visitAnnotation(AnnotationTree annTree, Void p) {
                            if (result.get()) {
                                return null;
                            }

                            TypeMirror tm = controller.getTrees().getTypeMirror(getCurrentPath());
                            if (tm != null) {
                                TypeElement annType = (TypeElement) controller.getTypes().asElement(tm);
                                if (annType != null) {
                                    boolean res = result.get();
                                    if (!res) {
                                        for (String ann : annotationNames) {
                                            if (ann.equals(ElementUtilities.getBinaryName(annType))) {
                                                res = true;
                                                break;
                                            }
                                        }
                                    }
                                    result.set(res);
                                }
                            }
                            return null;
                        }
                    };
                    scanner.scan(controller.getCompilationUnit(), null);

                    latch.countDown();
                }
            }, true);
            latch.await();
            return result.get();
        } catch (IOException e) {
            ProfilerLogger.log(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean hasAnnotation(FileObject fo, String annotation) {
        return hasAnnotation(fo, new String[]{annotation}, true);
    }

    @Override
    public boolean isApplet(FileObject fo) {
        return isInstanceOf(fo, APPLET_CLASSES, false); // NOI18N
    }

    @Override
    public boolean isInstanceOf(FileObject fo, final String[] classNames, final boolean allRequired) {
        final boolean[] result = new boolean[]{false};

        // get javasource for the java file
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return false; // not java source
        }

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(final CompilationController controller)
                        throws Exception {
                    // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                    if (controller.toPhase(Phase.ELEMENTS_RESOLVED).compareTo(Phase.ELEMENTS_RESOLVED) < 0) {
                        return;
                    }

                    Elements elements = controller.getElements();
                    Trees trees = controller.getTrees();
                    Types types = controller.getTypes();

                    Collection<TypeElement> classElements = new ArrayList<TypeElement>();

                    for (String className : classNames) {
                        TypeElement resolvedElement = elements.getTypeElement(className);

                        if (resolvedElement != null) {
                            classElements.add(resolvedElement);
                        }
                    }

                    if (classElements.isEmpty()) {
                        result[0] = false;

                        return;
                    }

                    CompilationUnitTree cu = controller.getCompilationUnit();
                    List<? extends Tree> topLevels = cu.getTypeDecls();

                    for (Tree topLevel : topLevels) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(topLevel.getKind())) {
                            TypeElement type = (TypeElement) trees.getElement(TreePath.getPath(cu, topLevel));

                            if (type != null) {
                                Set<Modifier> modifiers = type.getModifiers();

                                if (modifiers.contains(Modifier.PUBLIC) && (classElements != null)) {
                                    boolean rslt = allRequired;

                                    for (TypeElement classElement : classElements) {
                                        if (classElement == null) {
                                            continue;
                                        }

                                        if (allRequired) {
                                            rslt = rslt && types.isSubtype(type.asType(), classElement.asType());

                                            if (!rslt) {
                                                break;
                                            }
                                        } else {
                                            rslt = rslt || types.isSubtype(type.asType(), classElement.asType());

                                            if (rslt) {
                                                break;
                                            }
                                        }
                                    }

                                    result[0] = rslt;

                                    if (rslt) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException e) {
            ProfilerLogger.log(e);
        }

        return result[0];
    }

    @Override
    public boolean isInstanceOf(FileObject fo, String className) {
        return isInstanceOf(fo, new String[]{className}, true);
    }

    @Override
    public boolean isOffsetValid(FileObject fo, final int offset) {
        final Boolean[] validated = new Boolean[1];

        JavaSource js = JavaSource.forFileObject(fo);
        
        if (js != null) {
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                            throws Exception {
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        validated[0] = false; // non-validated default

                        Scope sc = controller.getTreeUtilities().scopeFor(offset);

                        if (sc.getEnclosingClass() != null) {
                            validated[0] = true;
                        }

                    }
                }, true);
            } catch (IOException ex) {
                ProfilerLogger.log(ex);
            }

        }

        return validated[0];
    }

    @Override
    public boolean isTest(FileObject fo) {
        return (hasAnnotation(fo, TEST_ANNOTATIONS, false) || isInstanceOf(fo, TEST_CLASSES, false)) || isJunit3TestSuite(fo); // NOI18N
    }

    @Override
    public SourceMethodInfo resolveMethodAtPosition(FileObject fo, final int position) {
        JavaSource js = JavaSource.forFileObject(fo);

        if (js == null) {
            return null; // not java source
        }

        // Final holder of resolved method
        final SourceMethodInfo[] resolvedMethod = new SourceMethodInfo[1];

        // Resolve the method
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController cc)
                        throws Exception {
                    if (cc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePath path = cc.getTreeUtilities().pathFor(position);

                    if (path == null) {
                        return;
                    }

//                    Use the following code to enable javac hierarchy traversal
//                    Element element = null;
//                    while (path != null) {
//                        element = ci.getTrees().getElement(path);
//                        if (element != null && ((element.getKind() == ElementKind.METHOD) || (element.getKind() == ElementKind.CONSTRUCTOR) || (element.getKind() == ElementKind.STATIC_INIT))) {
//                            break;
//                        }
//                        path = path.getParentPath();
//                    }

                    Element element = cc.getTrees().getElement(path);

                    if ((element != null) && ((element.getKind() == ElementKind.METHOD) || (element.getKind() == ElementKind.CONSTRUCTOR) || (element.getKind() == ElementKind.STATIC_INIT))) {
                        ExecutableElement method = (ExecutableElement) element;
                        resolvedMethod[0] = new JavacMethodInfo(method, cc);
                    }

                }
            }, true);
        } catch (IOException ioex) {
            ProfilerLogger.log(ioex);
            return null;
        }

        return resolvedMethod[0];
    }

    @Override
    public SourceClassInfo resolveClassAtPosition(FileObject fo, final int position, final boolean resolveField) {
        // Get JavaSource for given FileObject
        JavaSource js = JavaSource.forFileObject(fo);
        
        if (js == null) {
            return null; // not java source
        }
        
        // Final holder of resolved method
        final SourceClassInfo[] resolvedClass = new SourceClassInfo[1];

        // Resolve the method
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController cc)
                        throws Exception {
                    if (cc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    TreePath path = cc.getTreeUtilities().pathFor(position);

                    if (path == null) {
                        return;
                    }

                    Element element = cc.getTrees().getElement(path);

                    if (element == null) {
                        return;
                    }

                    // resolve class/enum at cursor
                    if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.ENUM)) {
                        TypeElement jclass = (TypeElement) element;
                        resolvedClass[0] = new JavacClassInfo(ElementHandle.create(jclass), cc);
                        return;

                    }

                    // resolve field at cursor

                    if (resolveField && ((element.getKind() == ElementKind.FIELD) || (element.getKind() == ElementKind.LOCAL_VARIABLE)) && (element.asType().getKind() == TypeKind.DECLARED)) {
                        TypeMirror jclassMirror = cc.getTypes().erasure(element.asType());
                        TypeElement jclass = (TypeElement)cc.getTypes().asElement(jclassMirror);
                        resolvedClass[0] = new JavacClassInfo(ElementHandle.create(jclass), cc);
                    }


                }
            }, true);
        } catch (IOException ioex) {
            ProfilerLogger.log(ioex);

            return null;
        }

        return resolvedClass[0];
    }
    
    private static boolean isJunit3TestSuite(FileObject fo) {
        final boolean[] rslt = new boolean[]{false};
        SourceGroup sg = SourceGroupModifier.createSourceGroup(FileOwnerQuery.getOwner(fo), JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST);
        if (sg == null) {
            LOG.log(Level.INFO, "Can not resolve source group for {0}", fo.getPath());
            return false;
        }
        if (FileUtil.getRelativePath(sg.getRootFolder(), fo) != null && // need to check for this first otherwise i will get IAE
            sg.contains(fo)) {
            JavaSource js = JavaSource.forFileObject(fo);
            if (js == null) {
                return false;
            }
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                        // do nothing
                    }

                    public void run(final CompilationController cc) throws Exception {
                        cc.toPhase(Phase.ELEMENTS_RESOLVED);

                        TreePathScanner<Void, Void> scanner = new TreePathScanner<Void, Void>() {
                            @Override
                            public Void visitMethod(MethodTree node, Void p) {
                                Element e = cc.getTrees().getElement(getCurrentPath());
                                if (e.getKind() == ElementKind.METHOD) {
                                    ExecutableElement ee = (ExecutableElement)e;
                                    if (ee.getSimpleName().contentEquals("suite") && // NOI18N
                                        (ee.getReturnType().toString().equals(JUNIT_TEST) ||
                                         ee.getReturnType().toString().equals(JUNIT_SUITE))) {
                                        rslt[0] |= true;
                                    }
                                }
                                return super.visitMethod(node, p);
                            }
                        };
                        scanner.scan(cc.getCompilationUnit(), null);
                    }
                }, true);
                return rslt[0];
            } catch (IOException ioex) {
                ProfilerLogger.log(ioex);
                return false;
            }
        }
        return rslt[0];
    }
}
