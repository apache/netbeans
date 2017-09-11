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

package org.netbeans.modules.j2ee.core.api.support.java.method;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import java.util.Map;
import javax.lang.model.type.ArrayType;
import org.netbeans.api.java.source.CompilationController;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.openide.util.Parameters;

/**
 * Support class for {@link MethodModel} providing some factory and conversion methods.
 * 
 * @author Martin Adamek
 */
public final class MethodModelSupport {
    
    private static final Logger LOG = Logger.getLogger(MethodModelSupport.class.getName());
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String UNKNOWN = "<unknown>"; //NOI18N
    
    private MethodModelSupport() {}
    
    /**
     * Creates new instance of method model. None of the parameters can be null.
     * This method must be called from within javac context.
     * 
     * @param workingCopy controller from javac context
     * @param method method for which the model is to be created
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return immutable model of method
     */
    public static MethodModel createMethodModel(CompilationController controller, ExecutableElement method) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("method", method); // NOI18N
        List<MethodModel.Variable> parameters = new ArrayList<MethodModel.Variable>();
        for (VariableElement variableElement : method.getParameters()) {
            String type = getTypeName(variableElement.asType());
            String name = variableElement.getSimpleName().toString();
            try{
                parameters.add(MethodModel.Variable.create(type, name));
            }catch(IllegalArgumentException iae){
                //in case of illegal arguments, for ex. illegal parameter/method name
                return null;
            }
        }
        List<String> exceptions = new ArrayList<String>();
        for (TypeMirror typeMirror : method.getThrownTypes()) {
            exceptions.add(getTypeName(typeMirror));
        }
        MethodModel mm = null;
        try{
            mm = MethodModel.create(
                    method.getSimpleName().toString(),
                    getTypeName(method.getReturnType()),
                    //TODO: RETOUCHE get body of method
                    "",
                    parameters,
                    exceptions,
                    method.getModifiers()
                    );
        }catch(IllegalArgumentException iae){
            return null;
        }
        return mm;
    }
    
    /**
     * Creates new instance of model of class variable or method parameter
     * This method must be called from within javac context.
     * 
     * @param workingCopy controller from javac context
     * @param variableElement variable or method parameter for which the model is to be created
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return immutable model of variable or method parameter
     */
    public static MethodModel.Variable createVariable(CompilationController controller, VariableElement variableElement) {
        Parameters.notNull("controller", controller); //NOI18N
        Parameters.notNull("variableElement", variableElement); //NOI18N
        return MethodModel.Variable.create(
                getTypeName(variableElement.asType()),
                variableElement.getSimpleName().toString(),
                variableElement.getModifiers().contains(Modifier.FINAL)
                );
    }
    
    /**
     * Creates {@link MethodTree} represented by methodModel in given javac context.
     * 
     * @param workingCopy controller from javac context
     * @param methodModel model of method
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return tree representing methodModel
     */
    public static MethodTree createMethodTree(WorkingCopy workingCopy, MethodModel methodModel) {
        return createMethodTree(workingCopy, methodModel, false);
    }
    
    /**
     * Creates {@link MethodTree} represented by methodModel in given javac context.
     * 
     * @param workingCopy controller from javac context
     * @param methodModel model of method
     * @param generateDefaultBody if true and body on methodModel is null or empty string, default return statement will be generated
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return tree representing methodModel
     */
    public static MethodTree createMethodTree(WorkingCopy workingCopy, MethodModel methodModel, boolean generateDefaultBody) {
        Parameters.notNull("workingCopy", workingCopy); //NOI18N
        Parameters.notNull("methodModel", methodModel); //NOI18N
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        List<VariableTree> paramsList = new ArrayList<VariableTree>();
        if (methodModel.getParameters() != null) {
            for (MethodModel.Variable parameter : methodModel.getParameters()) {
                VariableTree variableTree = treeMaker.Variable(
                        treeMaker.Modifiers(parameter.getFinalModifier() ? Collections.<Modifier>singleton(Modifier.FINAL) : Collections.<Modifier>emptySet()),
                        parameter.getName(),
                        getTypeTree(workingCopy, parameter.getType()),
                        null
                        );
                paramsList.add(variableTree);
            }
        }
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (String exceptionName : methodModel.getExceptions()) {
            throwsList.add(createQualIdent(workingCopy, exceptionName));
        }
        MethodTree result;
        String body = methodModel.getBody();
        // if passed body is null, generate default return statement (if return type is not void)
        if (generateDefaultBody && (body == null || "".equals(body.trim()))) {
            String generatedBody = getDefaultReturnValue(workingCopy, methodModel.getReturnType());
            body = generatedBody == null ? "" : "return " + generatedBody + ";";
        }
        if (body == null) {
            result = treeMaker.Method(
                    treeMaker.Modifiers(methodModel.getModifiers()),
                    methodModel.getName(),
                    getTypeTree(workingCopy, methodModel.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    paramsList,
                    throwsList,
                    (BlockTree) null,
                    null
                    );
        } else {
            result = treeMaker.Method(
                    treeMaker.Modifiers(methodModel.getModifiers()),
                    methodModel.getName(),
                    getTypeTree(workingCopy, methodModel.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    paramsList,
                    throwsList,
                    "{" + body + "}",
                    null
                    );
        }

        // add annotations if supported
        if (workingCopy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0) {
            GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
            List<AnnotationTree> annotationList = new ArrayList<AnnotationTree>();
            // create annotation list for the method
            for (MethodModel.Annotation annotation : methodModel.getAnnotations()) {
                AnnotationTree annotationTree = null;
                if (annotation.getArguments() == null) { 
                    annotationTree = genUtils.createAnnotation(annotation.getType());
                } else {
                    List<ExpressionTree> annotationArgs = new ArrayList<ExpressionTree>();
                    Iterator it = annotation.getArguments().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry)it.next();
                        annotationArgs.add(genUtils.createAnnotationArgument((String) pairs.getKey(),pairs.getValue()));
                    }
                    annotationTree = genUtils.createAnnotation(annotation.getType(), annotationArgs);
                }
                annotationList.add(annotationTree);
            }
            // annotate method with all annotations
            for (AnnotationTree annotation : annotationList) {
                result = genUtils.addAnnotation(result, annotation);
            }
        }

        return (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(result);
    }
    
    /**
     * Checks if signature of {@link ExecutableElement} is represented by {@link MethodModel} methodModel
     * in given javac context.
     * 
     * @param compilationInfo controller from javac context
     * @param method method existing in given javac context
     * @param methodModel model of method
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return true if method and methodModel have same signature, false otherwise
     */
    public static boolean isSameMethod(CompilationController controller, ExecutableElement method, MethodModel methodModel) {
        //TODO: RETOUCHE fix this method, see #90505
        Parameters.notNull("compilationInfo", controller); // NOI18N
        Parameters.notNull("method", method); // NOI18N
        Parameters.notNull("methodModel", methodModel); // NOI18N
        if (!method.getSimpleName().contentEquals(methodModel.getName())) {
            return false;
        }
        List<? extends VariableElement> methodParams = method.getParameters();
        if (methodParams.size() != methodModel.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < methodParams.size(); i++) {
            VariableElement variableElement = methodParams.get(i);
            String variableElementType = getTypeName(variableElement.asType());
            MethodModel.Variable variable = methodModel.getParameters().get(i);
            if (!variableElementType.equals(variable.getType())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Generates default value for provided type
     * 
     * @param workingCopy workingCopy
     * @param typeName fully-qualified type name or primitive type name or "void"
     * @return values according to JLS '4.5.5 Initial Values of Variables'
     * with exception for "char" where it returns "c"
     */
    public static String getDefaultReturnValue(WorkingCopy workingCopy, String typeName) {
        if ("boolean".equals(typeName)) {           // NOI18N
            return "false";                         // NOI18N
        } else if ("byte".equals(typeName)) {       // NOI18N
            return "0";                             // NOI18N
        } else if ("short".equals(typeName)) {      // NOI18N
            return "0";                             // NOI18N
        } else if ("int".equals(typeName)) {        // NOI18N
            return "0";                             // NOI18N
        } else if ("long".equals(typeName)) {       // NOI18N
            return "0L";                            // NOI18N
        } else if ("char".equals(typeName)) {       // NOI18N
            // should be '\u0000' which is null character, but we cannot return that
            return "'c'";                           // NOI18N
        } else if ("float".equals(typeName)) {      // NOI18N
            return "0.0f";                          // NOI18N
        } else if ("double".equals(typeName)) {     // NOI18N
            return "0.0d";                          // NOI18N
        } else if ("void".equals(typeName)) {       // NOI18N
            return null;
        } else {
            return "null";                          // NOI18N
        }
    }
    
    //TODO: RETOUCHE move/reuse to GenerationUtil, this one has also void type
    private static Tree getTypeTree(WorkingCopy workingCopy, String typeName) {
        TreeMaker make = workingCopy.getTreeMaker();
        TypeKind primitiveTypeKind = null;
        if ("boolean".equals(typeName)) {           // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) {        // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        } else if ("void".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        } else {
            return createQualIdent(workingCopy, typeName);
        }
    }
    
    //TODO: RETOUCHE move/reuse to GenerationUtil
    private static ExpressionTree createQualIdent(WorkingCopy workingCopy, String typeName) {
        TypeElement typeElement = workingCopy.getElements().getTypeElement(typeName);
        if (typeElement == null) {
            typeElement = workingCopy.getElements().getTypeElement("java.lang." + typeName);
            if (typeElement == null) {
                return workingCopy.getTreeMaker().Identifier(typeName);
            }
        }
        return workingCopy.getTreeMaker().QualIdent(typeElement);
    }
    
    static String getTypeName(TypeMirror typeMirror) {
        CharSequence name = getTypeName(typeMirror, true, false);
        return name.toString();
    }

    // from org.netbeans.modules.java.source.ui.JavaSymbolProvider
    private static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
    	if (type == null)
            return ""; //NOI18N
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }

    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {

        private boolean varArg;
        private boolean insideCapturedWildcard = false;

        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }

        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }

        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            if (!insideCapturedWildcard) {
                insideCapturedWildcard = true;
                TypeMirror bound = t.getLowerBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" super "); //NOI18N
                    visit(bound, p);
                } else {
                    bound = t.getUpperBound();
                    if (bound != null && bound.getKind() != TypeKind.NULL) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        if (bound.getKind() == TypeKind.TYPEVAR)
                            bound = ((TypeVariable)bound).getLowerBound();
                        visit(bound, p);
                    }
                }
                insideCapturedWildcard = false;
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            int len = DEFAULT_VALUE.length();
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else if (len == 0) {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }

}
