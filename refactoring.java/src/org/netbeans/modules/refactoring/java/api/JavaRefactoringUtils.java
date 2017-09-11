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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.api;

import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tim Boudreau
 * @author Jan Becicka
 */
public final class JavaRefactoringUtils {
    private JavaRefactoringUtils() {}

    /**
     * @param method 
     * @param info 
     * @return collection of ExecutableElements which are overidden by 'method'
     */
    @SuppressWarnings("deprecation")
    public static Collection<ExecutableElement> getOverriddenMethods(ExecutableElement method, CompilationInfo info) {
        return RefactoringUtils.getOverridenMethods (method, info);
    }

    /**
     * @param method 
     * @param info 
     * @since 1.33
     * @return collection of ExecutableElements which overrides 'method'
     */
    @SuppressWarnings("deprecation")
    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement method, CompilationInfo info, AtomicBoolean cancel) {
        return RefactoringUtils.getOverridingMethods(method, info, cancel);
    }

    /**
     * @param method 
     * @param info 
     * @return collection of ExecutableElements which overrides 'method'
     * @deprecated use {@link #getOverridingMethods(javax.lang.model.element.ExecutableElement, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean) 
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement method, CompilationInfo info) {
        return RefactoringUtils.getOverridingMethods(method, info, new AtomicBoolean());
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isFromLibrary(ElementHandle<? extends Element> element, ClasspathInfo info) {
        return RefactoringUtils.isFromLibrary(element, info);
    }

    /**
     * Returns true if file is on known source path.
     *
     * @param fo 
     * @return 
     */
    @SuppressWarnings("deprecation")
    public static boolean isOnSourceClasspath(FileObject fo) {
        return RefactoringUtils.isOnSourceClasspath(fo);
    }

    /**
     * returns true if file's mime type is text/x-java and file is on know source path
     * @param file 
     * @return 
     */
    @SuppressWarnings("deprecation")
    public static boolean isRefactorable(FileObject file) {
        return RefactoringUtils.isRefactorable(file) && file.canWrite() && file.canRead();
    }

    /**
     * Returns all supertypes of given type.
     * @param type 
     * @param info 
     * @param sourceOnly library classes ignored if true
     * @return 
     */
    @SuppressWarnings("deprecation")
    public static Collection<TypeElement> getSuperTypes(TypeElement type, CompilationInfo info, boolean sourceOnly) {
        return RefactoringUtils.getSuperTypes(type, info, sourceOnly);
    }

    /**
     * Finds the nearest enclosing ClassTree on <code>path</code> that
     * is class or interface or enum or annotation type and is or is not annonymous.
     * In case no ClassTree is found the first top level ClassTree is returned.
     *
     * Especially useful for selecting proper tree to refactor.
     *
     * @param javac javac
     * @param path path to search
     * @param isClass stop on class
     * @param isInterface  stop on interface
     * @param isEnum stop on enum
     * @param isAnnotation stop on annotation type
     * @param isAnonymous check if class or interface is annonymous
     * @return path to the enclosing ClassTree
     */
    @SuppressWarnings("deprecation")
    public static TreePath findEnclosingClass(CompilationInfo javac, TreePath path, boolean isClass, boolean isInterface, boolean isEnum, boolean isAnnotation, boolean isAnonymous) {
        return RefactoringUtils.findEnclosingClass(javac, path, isClass, isInterface, isEnum, isAnnotation, isAnonymous);
    }

    @SuppressWarnings("deprecation")
    public static List<TypeMirror> elementsToTypes(List<? extends Element> typeParams) {
        return RefactoringUtils.resolveTypeParamsAsTypes(typeParams);
    }

//    /**
//     * Finds type parameters from <code>typeArgs</code> list that are referenced
//     * by <code>tm</code> type.
//     * @param utils compilation type utils
//     * @param typeArgs modifiable list of type parameters to search; found types will be removed (performance reasons).
//     * @param result modifiable list that will contain referenced type parameters
//     * @param tm parametrized type to analyze
//     */
//    public static void findUsedGenericTypes(Types utils, List<TypeMirror> typeArgs, List<TypeMirror> result, TypeMirror tm) {
//        RefactoringUtils.findUsedGenericTypes(utils, typeArgs, result, tm);
//    }

    @SuppressWarnings("deprecation")
    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        return RefactoringUtils.getClasspathInfoFor(files);
    }

    //From here down is useful stuff from contrib/refactorings

    public static List <TreePathHandle> treesToHandles (TreePath parent, Iterable <? extends Tree> trees, CompilationInfo info) {
        List <TreePathHandle> result = new ArrayList <TreePathHandle> (
                trees instanceof Collection ? ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(parent, tree);
            TreePathHandle handle = TreePathHandle.create(path, info);
            result.add (handle);
            assert handle.resolve(info) != null : "Newly created TreePathHandle resolves to null"; //NOI18N
            assert handle.resolve(info).getLeaf() != null : "Newly created TreePathHandle.getLeaf() resolves to null"; //NOI18N
        }
        return result;
    }

    /**
     * Convert Trees to TreePathHandles
     * @param trees 
     * @param info 
     * @return 
     */
    public static List <TreePathHandle> treesToHandles (Iterable <? extends Tree> trees, CompilationInfo info) {
        List <TreePathHandle> result = new ArrayList <TreePathHandle> (trees instanceof Collection ?
            ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(info.getCompilationUnit(), tree);
            if (path == null) {
                throw new IllegalArgumentException (tree + " does not belong to " + //NOI18N
                        "the same compilation unit passed to this method"); //NOI18N
            }
            TreePathHandle handle = TreePathHandle.create(path, info);
            result.add (handle);
            assert handle.resolve(info) != null : "Newly created TreePathHandle resolves to null"; //NOI18N
            assert handle.resolve(info).getLeaf() != null : "Newly created TreePathHandle.getLeaf() resolves to null"; //NOI18N
        }
        return result;
    }

    /**
     * Resolves ElementHandles to Elemnts
     * @param handles 
     * @param info 
     * @return 
     */
    public static <T extends Element> List <T> handlesToElements (Iterable <ElementHandle<T>> handles, CompilationInfo info) {
        List <T> result = new ArrayList <T> (handles instanceof Collection ? ((Collection)handles).size() : 0);
        for (ElementHandle<? extends T> h : handles) {
            T element = h.resolve(info);
            assert element != null : element + " resolves to null"; //NOI18N
            result.add (element);
        }
        return result;
    }


    /**
     * Resolves TypeMirrorHandles to TypeMirrors
     * @param types 
     * @param info 
     * @return 
     */
    public static List <TypeMirror> handlesToTypes (Iterable <? extends TypeMirrorHandle> types, CompilationInfo info) {
        List <TypeMirror> result = new ArrayList <TypeMirror> ();
        for (TypeMirrorHandle h : types) {
            result.add (h.resolve(info));
        }
        return result;
    }

    /**
     * Creates TypeMirrosHandles from TypeMirrors
     * @param types 
     * @return 
     */
    public static List <TypeMirrorHandle> typesToHandles (Iterable <? extends TypeMirror> types) {
        List <TypeMirrorHandle> result = new ArrayList <TypeMirrorHandle> ();
        for (TypeMirror h : types) {
            result.add (TypeMirrorHandle.create(h));
        }
        return result;
    }

    /**
     * Create ElementHandles from Elements
     * @param elements 
     * @return 
     */
    public static <T extends Element> List <ElementHandle<T>> elementsToHandles (Iterable <? extends T> elements) {
        List <ElementHandle<T>> result = new ArrayList <ElementHandle<T>> (elements instanceof
                Collection ? ((Collection)elements).size() : 11);
        for (T element : elements) {
            ElementHandle<T> handle = ElementHandle.<T>create(element);
            assert handle != null : "Couldn't create handle for " + element; //NOI18N
            result.add (handle);
        }
        return result;
    }

    /**
     * 
     * @param e 
     * @param wc 
     * @return 
     * @throws java.io.IOException 
     */
    public static Collection <TreePathHandle> getInvocationsOf(ElementHandle e, CompilationController wc) throws IOException {
        assert e != null;
        assert wc != null;
        wc.toPhase (Phase.RESOLVED);
        Element element = e.resolve(wc);
        TypeElement type = wc.getElementUtilities().enclosingTypeElement(element);
        ElementHandle<TypeElement> elh = ElementHandle.<TypeElement>create(type);
        assert elh != null;
        //XXX do I want the enclosing type element for elh here?
        Set <ElementHandle<TypeElement>> classes = wc.getClasspathInfo().getClassIndex().getElements(elh, EnumSet.<SearchKind>of (SearchKind.METHOD_REFERENCES), EnumSet.<SearchScope>of(SearchScope.SOURCE));
        List <TreePathHandle> result = new ArrayList <TreePathHandle> ();
        for (ElementHandle<TypeElement> h : classes) {
            result.addAll (getReferencesToMember(h, wc.getClasspathInfo(), e));
        }
        return result;
    }

    /**
     * Get all of the references to the given member element (which may be part of another type) on
     * the passed element.
     * @param on A type which presumably refers to the passed element
     * @param toFind An element, presumably a field or method, of some type (not necessarily the passed one)
     */
    public static Collection <TreePathHandle> getReferencesToMember (ElementHandle<TypeElement> on, ClasspathInfo info, ElementHandle toFind) throws IOException {
        FileObject ob = SourceUtils.getFile(on, info);
        assert ob != null : "SourceUtils.getFile(" + on + ") returned null"; //NOI18N
        JavaSource src = JavaSource.forFileObject(ob);
        InvocationScanner scanner = new InvocationScanner (toFind);
        src.runUserActionTask(scanner, true);
        return scanner.usages;
    }

    private static final class InvocationScanner extends TreePathScanner <Tree, ElementHandle> implements CancellableTask <CompilationController> {
        private CompilationController cc;
        private final ElementHandle toFind;
        InvocationScanner (ElementHandle toFind) {
            this.toFind = toFind;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, ElementHandle p) {
            assert cc != null;
            Element e = p.resolve(cc);
            addIfMatch(getCurrentPath(), node, e);
            return super.visitMemberSelect(node, p);
        }

        private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
            if (cc.getTreeUtilities().isSynthetic(path)) {
                return;
            }

            Element el = cc.getTrees().getElement(path);
            if (el==null) {
                return;
            }

            if (elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
                if (el.equals(elementToFind) || cc.getElements().overrides(((ExecutableElement) el), (ExecutableElement) elementToFind, (TypeElement) elementToFind.getEnclosingElement())) {
                    addUsage(getCurrentPath());
                }
            } else if (el.equals(elementToFind)) {
                addUsage(getCurrentPath());
            }
        }

        Set <TreePathHandle> usages = new HashSet <TreePathHandle> ();
        void addUsage (TreePath path) {
            usages.add (TreePathHandle.create(path, cc));
        }

        boolean cancelled;
        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (cancelled) {
                return;
            }
            cc.toPhase(Phase.RESOLVED);
            if (cancelled) {
                return;
            }
            this.cc = cc;
            try {
                TreePath path = new TreePath (cc.getCompilationUnit());
                scan (path, toFind);
            } finally {
                this.cc = null;
            }
        }
    }

    
    public static void cacheTreePathInfo(TreePath tp, CompilationInfo info) {
        ElementGripFactory.getDefault().put(info.getFileObject(), tp, info);
    }

}
