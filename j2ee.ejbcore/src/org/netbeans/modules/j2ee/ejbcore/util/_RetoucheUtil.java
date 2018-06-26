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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.ejbcore.util;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Adamek
 */
public final class _RetoucheUtil {

    private static final Logger LOG = Logger.getLogger(_RetoucheUtil.class.getName());

    private _RetoucheUtil() {}
    
    /** never call this from javac task */
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                if (typeElement != null) {
                    result[0] = typeElement.getQualifiedName().toString();
                }
            }
        }, true);
        return result[0];
    }

    /**
     * @return true if the given <code>javaClass</code> contains a feature
     * whose name is identical with the given <code>feature</code>'s name.
     */
    public static boolean containsFeature(TypeElement javaClass, Element searchedElement) {
        for (Element element : javaClass.getEnclosedElements()) {
            if (searchedElement.getSimpleName().equals(element.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static ElementHandle<TypeElement> getElementHandle(ElementHandle elementHandle) {
        if (elementHandle != null && ElementKind.CLASS == elementHandle.getKind()) {
            return (ElementHandle<TypeElement>) elementHandle;
        }
        return null;
    }
    
    public static ElementHandle<TypeElement> getJavaClassFromNode(Node node) throws IOException {
        ElementHandle<TypeElement> elementHandle = getElementHandle(node.getLookup().lookup(ElementHandle.class));
        if (elementHandle != null) {
            return elementHandle;
        }
        //TODO: RETOUCHE TypeElement from Node, this one just takes main TypeElement if ElementHandle is not found
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null || !fileObject.isValid()) {
            return null;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return null;
        }
        final List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>();
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                    if (typeElement != null) {
                        result.add(ElementHandle.create(typeElement));
                    }
                }
            }, true);
        } catch (DataObjectNotFoundException donfe) {
            // do nothing, node's file was deleted, just return null
        }
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public static ExecutableElement getMethodFromNode(Node node) {
        //TODO: RETOUCHE ExecutableElement from Node
        return null;
    }
    
    public static ElementHandle<VariableElement> generateAnnotatedField(FileObject fileObject, final String className, final String annotationType,
            final String name, final String fieldType, final Map<String, String> attributes, final boolean isStatic) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                TypeElement returnTypeElement = workingCopy.getElements().getTypeElement(fieldType);
                if (returnTypeElement == null) {
                    LOG.log(Level.WARNING, "TypeElement not found for {0}", fieldType);
                }

                // modifiers
                Set<Modifier> modifiers = new HashSet<>();
                modifiers.add(Modifier.PRIVATE);
                if (isStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                // annotation with attributes
                List<ExpressionTree> attributesList = new ArrayList<>();
                if (attributes != null) {
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        if ("".equals(entry.getKey())) { //NOI18N
                            attributesList.add(generationUtils.createAnnotationArgument(null, entry.getValue()));
                        } else {
                            attributesList.add(generationUtils.createAnnotationArgument(entry.getKey(), entry.getValue()));
                        }
                    }
                }
                AnnotationTree annotationTree = generationUtils.createAnnotation(annotationType, attributesList);
                ModifiersTree modifiersTree = treeMaker.addModifiersAnnotation(treeMaker.Modifiers(modifiers), annotationTree);
                // field itself
                ExpressionTree returnType = returnTypeElement != null ?
                    treeMaker.QualIdent(returnTypeElement) : treeMaker.QualIdent(fieldType);
                VariableTree variableTree = treeMaker.Variable(
                        modifiersTree,
                        name,
                        returnType,
                        null
                        );
                // adding field to class
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = treeMaker.insertClassMember(classTree, 0, variableTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();

        return getFieldHandle(javaSource, className, fieldType, name);
        
    }

    public static boolean isInterface(FileObject fileObject, final ElementHandle<TypeElement> elementHandle) throws IOException {
        final boolean[] isInterface = new boolean[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null || elementHandle == null) {
            return false;
        }
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle.resolve(controller);
                isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
            }
        }, true);
        return isInterface[0];
    }

    /**
     * Generates unique member name in class-scope
     * 
     * @param fileObject file containing class acting as scope for uniqueness
     * @param className name of class acting as scope for uniqueness
     * @param memberName suggested member name
     * @param defaultValue default value applied if member name cannot be converted to legal Java identifier
     * @return given member name if no such member exists or given member name without any illegal characters extended with unique number
     */
    public static String uniqueMemberName(FileObject fileObject, final String className, final String memberName, final String defaultValue) throws IOException{
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String validName = convertToJavaIdentifier(memberName, defaultValue);
                List<String> existingMethodNames = new ArrayList<String>();
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                if (typeElement == null) {
                    result[0] = validName;
                    return;
                }
                for (Element element : typeElement.getEnclosedElements()) {
                    existingMethodNames.add(element.getSimpleName().toString());
                }
                int uniquefier = 1;
                String resultName = validName;
                while (existingMethodNames.contains(resultName)){
                    resultName = validName + uniquefier++;
                }
                result[0] = resultName;
            }
        }, true);
        return result[0];
    }
    
    private static String convertToJavaIdentifier(String name, String defaultValue) {
        Parameters.notWhitespace("name", name);
        Parameters.notWhitespace("defaultValue", defaultValue);
        String str = name;
        while (str.length() > 0 && !Character.isJavaIdentifierStart(str.charAt(0))) {
            str = str.substring(1);
        }
        StringBuilder result = new StringBuilder();
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            firstChar = Character.toLowerCase(firstChar);
            result.append(firstChar);
            for (int i = 1; i < str.length(); i++) {
                char character = str.charAt(i);
                if (Character.isJavaIdentifierPart(character)) {
                    result.append(character);
                }
            }
        } else {
            result.append(defaultValue);
        }
        return result.toString();
    }

    /**
     * Tries to find {@link FileObject} which contains class with given className.<br>
     * This method will enter javac context for referenceFileObject to find class by its className,
     * therefore don't call this method within other javac context.
     * 
     * @param referenceFileObject helper file for entering javac context
     * @param className fully-qualified class name to resolve file for
     * @return resolved file or null if not found
     */
    public static FileObject resolveFileObjectForClass(FileObject referenceFileObject, final String className) throws IOException {
        final FileObject[] result = new FileObject[1];
        JavaSource javaSource = JavaSource.forFileObject(referenceFileObject);
        if (javaSource == null) {
            // Should not happen, at least some debug logging, see i.e. issue #202495.
            Logger.getLogger(_RetoucheUtil.class.getName()).log(
                    Level.SEVERE, "JavaSource not created for FileObject: path={0}, valid={1}, mime-type={2}",
                    new Object[]{
                        referenceFileObject.getPath(),
                        referenceFileObject.isValid(),
                        referenceFileObject.getMIMEType()});
        }
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                if (typeElement != null) {
                    result[0] = org.netbeans.api.java.source.SourceUtils.getFile(ElementHandle.create(typeElement), controller.getClasspathInfo());
                }
            }
        }, true);
        return result[0];
    }
    
    //TODO: RETOUCHE move/reuse in SourceUtil, or best - get from java/source!
    // package private only for unit test
    // see #90968
    public static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN : return "boolean"; // NOI18N
            case BYTE : return "byte"; // NOI18N
            case CHAR : return "char"; // NOI18N
            case DOUBLE : return "double"; // NOI18N
            case FLOAT : return "float"; // NOI18N
            case INT : return "int"; // NOI18N
            case LONG : return "long"; // NOI18N
            case SHORT : return "short"; // NOI18N
            case VOID : return "void"; // NOI18N
            case DECLARED : 
                Element element = controller.getTypes().asElement(typeMirror);
                return ((TypeElement) element).getQualifiedName().toString();
            case ARRAY : 
                ArrayType arrayType = (ArrayType) typeMirror;
                Element componentTypeElement = controller.getTypes().asElement(arrayType.getComponentType());
                return ((TypeElement) componentTypeElement).getQualifiedName().toString() + "[]";
            case ERROR :
            case EXECUTABLE :
            case NONE :
            case NULL :
            case OTHER :
            case PACKAGE :
            case TYPEVAR :
            case WILDCARD :
            default:break;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static ElementHandle<ExecutableElement> getMethodHandle(JavaSource javaSource, final MethodModel methodModel, final String className) throws IOException {

        final ElementHandle[] result = new ElementHandle[1];

        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(controller, method, methodModel)) {
                        result[0] = ElementHandle.create(method);
                        return;
                    }
                }
            }
        }, true);

        return result[0];
    }

    @SuppressWarnings("unchecked")
    public static ElementHandle<VariableElement> getFieldHandle(JavaSource javaSource, final String className, final String fieldType, final String fieldName) throws IOException {

        final ElementHandle[] result = new ElementHandle[1];

        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                for (VariableElement variable : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                    if (variable.getSimpleName().contentEquals(fieldName) && fieldType.equals(getTypeName(controller, variable.asType()))) {
                        result[0] = ElementHandle.create(variable);
                        return;
                    }
                }
            }
        }, true);

        return result[0];
    }

    public static Set<MethodModel> getMethods(FileObject fileObject, final String className) {
        final Set<MethodModel> result = new HashSet<MethodModel>();
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    if (typeElement != null) {
                        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                            MethodModel mm = MethodModelSupport.createMethodModel(controller, executableElement);
                            if (mm != null){
                                result.add(mm);
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result;
    }
    
}
