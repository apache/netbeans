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

package org.netbeans.modules.websvc.wsitconf.util;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea, Martin Adamek
 */
public class SourceUtils {

    // TODO we could probably also have a SourceUtils(CompilationController, TypeElement) factory method

    /**
     * The compilation controller this instance works with.
     */
    private final CompilationController controller;

    /**
     * The type element this instance works with. Do not use directly, use
     * {@link #getTypeElement} instead.
     */
    private TypeElement typeElement;

    /**
     * The class tree corresponding to {@link #typeElement}. Do not use directly,
     * use {@link #getClassTree} instead.
     */
    private ClassTree classTree;

    // <editor-fold defaultstate="collapsed" desc="Constructors and factory methods">

    SourceUtils(CompilationController controller, TypeElement typeElement) {
        this.controller = controller;
        this.typeElement = typeElement;
    }

    SourceUtils(CompilationController controller, ClassTree classTree) {
        this.controller = controller;
        this.classTree = classTree;
    }

    public static SourceUtils newInstance(CompilationController controller, TypeElement typeElement) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("typeElement", typeElement); // NOI18N

        return new SourceUtils(controller, typeElement);
    }

    public static SourceUtils newInstance(CompilationController controller, ClassTree classTree) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("classTree", classTree); // NOI18N

        return new SourceUtils(controller, classTree);
    }

    public static SourceUtils newInstance(CompilationController controller) throws IOException {
        Parameters.notNull("controller", controller); // NOI18N

        ClassTree classTree = findPublicTopLevelClass(controller);
        if (classTree != null) {
            return newInstance(controller, classTree);
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Finds the first public top-level type in the compilation unit given by the
     * given <code>CompilationController</code>.
     *
     * This method assumes the restriction that there is at most a public
     * top-level type declaration in a compilation unit, as described in the
     * section 7.6 of the JLS.
     */
    static ClassTree findPublicTopLevelClass(CompilationController controller) throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        final String mainElementName = controller.getFileObject().getName();
        CompilationUnitTree cunittree = controller.getCompilationUnit();
        if (cunittree != null) {
            for (Tree tree : cunittree.getTypeDecls()) {
                if (!TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                    continue;
                }
                ClassTree classTree = (ClassTree)tree;
                if (!classTree.getSimpleName().contentEquals(mainElementName)) {
                    continue;
                }
                if (!classTree.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                    continue;
                }
                return classTree;
            }
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold desc="Public methods">

    /**
     * Returns the type element that this instance works with
     * (corresponding to {@link #getClassTree}.
     *
     * @return the type element that this instance works with; never null.
     */
    public TypeElement getTypeElement() {
        if (typeElement == null) {
            assert classTree != null;
            TreePath classTreePath = controller.getTrees().getPath(getCompilationController().getCompilationUnit(), classTree);
            typeElement = (TypeElement)controller.getTrees().getElement(classTreePath);
        }
        return typeElement;
    }

    /**
     * Returns the class tree that this instance works with
     * (corresponding to {@link #getTypeElement}.
     *
     * @return the class tree that this instance works with; never null.
     */
    public ClassTree getClassTree() {
        if (classTree == null) {
            assert typeElement != null;
            classTree = controller.getTrees().getTree(typeElement);
        }
        return classTree;
    }

    /**
     * Returns true if {@link #getTypeElement} is a subtype of the given type.
     *
     * @param  type the string representation of a type. The type will be parsed
     *         in the context of {@link #getTypeElement}.
     * @return true {@link #getTypeElement} is a subtype of the given type,
     *         false otherwise.
     */
    public boolean isSubtype(String type) {
        Parameters.notNull("type", type); // NOI18N

        TypeMirror typeMirror = getCompilationController().getTreeUtilities().parseType(type, getTypeElement());
        if (typeMirror != null) {
            return getCompilationController().getTypes().isSubtype(getTypeElement().asType(), typeMirror);
        }
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    /**
     * Returns the <code>CompilationController</code> that this instance
     * works with.
     */
    CompilationController getCompilationController() {
        return controller;
    }

    /**
     * Returns the non-synthetic no-arg constructor of the main type element.
     */
    ExecutableElement getNoArgConstructor() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        ElementUtilities elementUtils = controller.getElementUtilities();
        for (Element element : getTypeElement().getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement)element;
                if (constructor.getParameters().size() == 0 && !elementUtils.isSynthetic(constructor)) {
                    return constructor;
                }
            }
        }
        return null;
    }

    // </editor-fold>
}
