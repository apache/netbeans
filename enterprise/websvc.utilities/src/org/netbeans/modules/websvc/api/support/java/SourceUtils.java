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

package org.netbeans.modules.websvc.api.support.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Miscellaneous utilities for working with Java sources.
 *
 * @author Andrei Badea
 */
public final class SourceUtils {

    private SourceUtils() {}

    /**
     * Finds the first public top-level type in the compilation unit given by the
     * given <code>CompilationController</code>.
     *
     * This method assumes the restriction that there is at most a public
     * top-level type declaration in a compilation unit, as described in the
     * section 7.6 of the JLS.
     *
     * @param  controller a {@link CompilationController}.
     * @return the <code>TypeElement</code> encapsulating the public top-level type
     *         in the compilation unit given by <code>controller</code> or null
     *         if no such type is found.
     * @throws IllegalStateException when the controller was created with no file
     *         objects.
     */
    public static TypeElement getPublicTopLevelElement(CompilationController controller) {
        Parameters.notNull("controller", controller); // NOI18N

        FileObject mainFileObject = controller.getFileObject();
        if (mainFileObject == null) {
            throw new IllegalStateException();
        }
        //String mainElementName = mainFileObject.getName();
        List<? extends TypeElement> elements = controller.getTopLevelElements();
        if (elements != null) {
            for (TypeElement element : elements) {
                //if (element.getModifiers().contains(Modifier.PUBLIC) && element.getSimpleName().contentEquals(mainElementName)) {
                if (element.getModifiers().contains(Modifier.PUBLIC) ){
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Finds the first public top-level type in the compilation unit given by the
     * given <code>CompilationController</code>.
     *
     * This method assumes the restriction that there is at most a public
     * top-level type declaration in a compilation unit, as described in the
     * section 7.6 of the JLS.
     *
     * @param  controller a {@link CompilationController}.
     * @return the <code>TypeElement</code> encapsulating the public top-level type
     *         in the compilation unit given by <code>controller</code> or null
     *         if no such type is found.
     * @throws IllegalStateException when the controller was created with no file
     *         objects.
     */
    public static ClassTree getPublicTopLevelTree(CompilationController controller) {
        Parameters.notNull("controller", controller); // NOI18N

        TypeElement typeElement = getPublicTopLevelElement(controller);
        if (typeElement != null) {
            return controller.getTrees().getTree(typeElement);
        }
        return null;
    }

    /**
     * Finds whether the given <code>TypeElement</code> is the subtype of a
     * given supertype. This is a convenience method for
     * {@link javax.lang.model.util.Types#isSubtype}.
     *
     * @param  controller a <code>CompilationController</code>.
     * @param  subtype the presumed subtype.
     * @param  supertype the presumed supertype.
     * @return true if <code>subtype</code> if a subtype of </code>supertype</code>,
     *         false otherwise.
     */
    public static boolean isSubtype(CompilationController controller, TypeElement subtype, String supertype) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("subtype", subtype); // NOI18N
        Parameters.notNull("supertype", supertype); // NOI18N

        if (controller.getElements().getTypeElement(supertype) != null) {
            TypeMirror typeMirror = controller.getTreeUtilities().parseType(supertype, subtype);
            if (typeMirror != null) {
                return controller.getTypes().isSubtype(subtype.asType(), typeMirror);
            }
        }
        return false;
    }

    /**
     * A convenience method for converting a <code>ClassTree</code> to the 
     * corresponding <code>TypeElement</code>, if any.
     */
    static TypeElement classTree2TypeElement(CompilationController controller, ClassTree classTree) {
        assert controller != null;
        assert classTree != null;

        TreePath classTreePath = controller.getTrees().getPath(controller.getCompilationUnit(), classTree);
        return (TypeElement)controller.getTrees().getElement(classTreePath);
    }

    /**
     * Finds the no-argument non-synthetic constructor in the specified class.
     */
    static ExecutableElement getNoArgConstructor(CompilationController controller, TypeElement typeElement) {
        assert controller != null;
        assert typeElement != null;

        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement)element;
                if (constructor.getParameters().size() == 0 && !controller.getElementUtilities().isSynthetic(constructor)) {
                    return constructor;
                }
            }
        }
        return null;
    }
}
