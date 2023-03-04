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

package org.netbeans.modules.testng;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.testng.TestabilityResult.SkippedClass;
import org.openide.filesystems.FileObject;

/**
 * Finds all non-annotation top-level classes in a compilation unit.
 * 
 * @author  Marian Petras
 */
final class TopClassFinder {

    interface Filter {
        boolean passes(TypeElement topClass,
                       CompilationInfo compInfo);
    }

    static class BasicTestabilityFilter implements Filter {
        public boolean passes(TypeElement topClass,
                              CompilationInfo compInfo) {
            ElementKind elemKind = topClass.getKind();
            return (elemKind != ElementKind.ANNOTATION_TYPE)
                   && (elemKind.isClass()|| elemKind.isInterface());
        }
    }

    static final class MainClassOnly extends BasicTestabilityFilter {
        @Override
        public boolean passes(TypeElement topClass,
                              CompilationInfo compInfo) {
            if (!super.passes(topClass, compInfo)) {
                return false;
            }

            FileObject javaFileObj = compInfo.getFileObject();
            ClassPath sourceCP = compInfo.getClasspathInfo().getClassPath(PathKind.SOURCE);
            String qualifiedClassName = topClass.getQualifiedName().toString();
            return qualifiedClassName.equals(sourceCP.getResourceName(javaFileObj, '.', false));
        }
    }

    static final class ExtendedTestabilityFilter implements Filter {

        private final TestabilityJudge testabilityJudge;
        private final Collection<SkippedClass> nonTestable;
        private final long skipTestabilityResultMask;

        ExtendedTestabilityFilter(TestabilityJudge testabilityJudge,
                                  Collection<SkippedClass> nonTestable,
                                  long skipTestabilityResultMask) {
            this.testabilityJudge = testabilityJudge;
            this.nonTestable = nonTestable;
            this.skipTestabilityResultMask = skipTestabilityResultMask;
        }
        public boolean passes(TypeElement topClass,
                              CompilationInfo compInfo) {
            TestabilityResult testabilityStatus
                    = testabilityJudge.isClassTestable(compInfo,
                                                       topClass, skipTestabilityResultMask);
            if (testabilityStatus.isTestable()) {
                return true;
            } else {
                nonTestable.add(
                        new SkippedClass(topClass.getQualifiedName().toString(),
                                         testabilityStatus));
                return false;
            }
        }

    }
    
    private TopClassFinder() { }

    /**
     */
    private static class TopClassFinderTask
                            implements CancellableTask<CompilationController> {
        private final Filter filter;
        private List<ElementHandle<TypeElement>> topClassElems;
        private volatile boolean cancelled;
        private TopClassFinderTask() {
            this(new BasicTestabilityFilter());
        }
        private TopClassFinderTask(Filter filter) {
            this.filter = filter;
        }
        public void run(CompilationController controller) throws IOException {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            if (cancelled) {
                return;
            }
            
            if (topClassElems == null) {
                topClassElems = new ArrayList<ElementHandle<TypeElement>>(10);
            }
            topClassElems.addAll(findTopClassElemHandles(
                                                controller,
                                                controller.getCompilationUnit(),
                                                filter));
        }
        public void cancel() {
            cancelled = true;
        }
    }
    
    /**
     */
    static List<ElementHandle<TypeElement>> findTopClasses(
                                                    JavaSource javaSource)
                                                        throws IOException {
        TopClassFinderTask analyzer = new TopClassFinderTask();
        javaSource.runUserActionTask(analyzer, true);
        return analyzer.topClassElems;
    }
    
    /**
     * Finds main top classes, i.e. those whose name matches with the name
     * of the file they reside in.
     */
    static List<ElementHandle<TypeElement>> findMainTopClasses(
                                                    JavaSource javaSource)
                                                        throws IOException {
        TopClassFinderTask analyzer = new TopClassFinderTask(new MainClassOnly());
        javaSource.runUserActionTask(analyzer, true);
        return analyzer.topClassElems;
    }
    
    /**
     * Finds testable top-level classes, interfaces and enums in a given
     * Java source.
     * 
     * @param  javaSource  source in which testable classes should be found
     * @param  judge  {@code TestCreator} that will select testable
     *                top-level classes
     *                (see {@link TestCreator#isClassTestable})
     * @param  nonTestable  container where names of found non-testable classes
     *                      should be stored
     * @return  handles to testable top-level classes, interfaces and enums
     * @exception  java.lang.IllegalArgumentException
     *             if any of the parameters is {@code null}
     */
    static List<ElementHandle<TypeElement>> findTestableTopClasses(
                                                JavaSource javaSource,
                                                TestabilityJudge testabilityJudge,
                                                Collection<SkippedClass> nonTestable,
                                                long skipTestabilityResultMask)
                                                        throws IOException {
        TopClassFinderTask analyzer = new TopClassFinderTask(new ExtendedTestabilityFilter(testabilityJudge, nonTestable, skipTestabilityResultMask));
        javaSource.runUserActionTask(analyzer, true);
        return analyzer.topClassElems;
    }

    /**
     * 
     * @return  list of top classes, or an empty list of none were found
     */
    static List<ClassTree> findTopClasses(
                                        CompilationUnitTree compilationUnit,
                                        TreeUtilities treeUtils) {
        List<? extends Tree> typeDecls = compilationUnit.getTypeDecls();
        if ((typeDecls == null) || typeDecls.isEmpty()) {
            return Collections.<ClassTree>emptyList();
        }

        List<ClassTree> result = new ArrayList<ClassTree>(typeDecls.size());
        
        for (Tree typeDecl : typeDecls) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                ClassTree clsTree = (ClassTree) typeDecl;
                if (isTestable(clsTree, treeUtils)) {
                    result.add(clsTree);
                }
            }
        }

        return result;
    }

    /**
     * 
     * @return  list of {@code Element}s representing top classes,
     *          or an empty list of none were found
     */
    private static List<TypeElement> findTopClassElems(
                                        CompilationInfo compInfo,
                                        CompilationUnitTree compilationUnit,
                                        Filter filter) {
        List<? extends Tree> typeDecls = compilationUnit.getTypeDecls();
        if ((typeDecls == null) || typeDecls.isEmpty()) {
            return Collections.<TypeElement>emptyList();
        }
        
        List<TypeElement> result = new ArrayList<TypeElement>(typeDecls.size());

        Trees trees = compInfo.getTrees();
        for (Tree typeDecl : typeDecls) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                Element element = trees.getElement(
                        new TreePath(new TreePath(compilationUnit), typeDecl));
                TypeElement typeElement = (TypeElement) element;
                if (filter.passes(typeElement, compInfo)) {
                    result.add(typeElement);
                }
            }
        }

        return result;
    }

    /**
     * 
     * @return  list of handles to {@code Element}s representing top classes,
     *          or an empty list of none were found
     */
    private static List<ElementHandle<TypeElement>> findTopClassElemHandles(
                                        CompilationInfo compInfo,
                                        CompilationUnitTree compilationUnit,
                                        Filter filter) {
        return getElemHandles(
                    findTopClassElems(compInfo, compilationUnit, filter));
    }

    private static <T extends Element> List<ElementHandle<T>> getElemHandles(List<T> elements) {
        if (elements == null) {
            return null;
        }
        if (elements.isEmpty()) {
            return Collections.<ElementHandle<T>>emptyList();
        }

        List<ElementHandle<T>> handles = new ArrayList<ElementHandle<T>>(elements.size());
        for (T element : elements) {
            handles.add(ElementHandle.<T>create(element));
        }
        return handles;
    }

    private static boolean isTestable(ClassTree typeDecl,
                                      TreeUtilities treeUtils) {
        return !treeUtils.isAnnotation(typeDecl);
    }

    static boolean isTestable(TypeElement typeDeclElement) {
        ElementKind elemKind = typeDeclElement.getKind();
        return (elemKind != ElementKind.ANNOTATION_TYPE)
               && (elemKind.isClass()|| elemKind.isInterface());
    }
    
}
