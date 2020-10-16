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
package org.netbeans.modules.web.el;

import com.sun.el.parser.*;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.web.el.completion.ELStreamCompletionItem;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Utility class for resolving elements/types for EL expressions.
 *
 * @author Erno Mononen
 */
public final class ELTypeUtilities {

    private static final Logger LOG = Logger.getLogger(ELTypeUtilities.class.getName());

    private static final String FACES_CONTEXT_CLASS = "javax.faces.context.FacesContext";   //NOI18N
    private static final String UI_COMPONENT_CLASS = "javax.faces.component.UIComponent";   //NOI18N
    private static final String STREAM_CLASS = "com.sun.el.stream.Stream";                  //NOI18N

    private static final FileObject EL_IMPL_JAR_FO = FileUtil.getArchiveRoot(FileUtil.toFileObject(
            InstalledFileLocator.getDefault().locate("modules/ext/el-impl.jar", "org.netbeans.modules.libs.elimpl", false))); //NOI18N

    private static final Map<Class<? extends Node>, Set<TypeKind>> TYPES = new HashMap<>();

    static {
        put(AstFloatingPoint.class, TypeKind.FLOAT, TypeKind.DOUBLE);
        put(AstTrue.class, TypeKind.BOOLEAN);
        put(AstFalse.class, TypeKind.BOOLEAN);
        put(AstInteger.class, TypeKind.INT, TypeKind.SHORT, TypeKind.LONG);
    }

    private static void put(Class<? extends Node> node, TypeKind... kinds) {
        Set<TypeKind> kindSet = new HashSet<>();
        kindSet.addAll(Arrays.asList(kinds));
        TYPES.put(node, kindSet);
    }

    private ELTypeUtilities() {
        //do not create instancies
    }

    public static String getTypeNameFor(CompilationContext info, Element element) {
        final TypeMirror tm = getTypeMirrorFor(info, element);
        return info.info().getTypeUtilities().getTypeName(tm).toString();
    }

    public static Element getTypeFor(CompilationContext info, Element element) {
        TypeMirror tm = getTypeMirrorFor(info, element);
        return info.info().getTypes().asElement(tm);
    }

    public static List<Element> getSuperTypesFor(CompilationContext info, Element element) {
        return getSuperTypesFor(info, element, null, null);
    }
    
    /**
     * 
     * @param element
     * @return a list of Element-s representing all the superclasses of the element. 
     * The list starts with the given element itself and ends with java.lang.Object
     */
    public static List<Element> getSuperTypesFor(CompilationContext info, Element element, ELElement elElement, List<Node> rootToNode) {
        final TypeMirror tm = getTypeMirrorFor(info, element, elElement, rootToNode);
        List<Element> types = new ArrayList<>();

        Deque<TypeMirror> deque = new ArrayDeque<>();
        deque.add(tm);
        while (!deque.isEmpty()) {
            TypeMirror mirror = deque.pop();
            if (mirror.getKind() == TypeKind.DECLARED) {
                Element el = info.info().getTypes().asElement(mirror);
                types.add(el);

                if (el.getKind() == ElementKind.CLASS) {
                    TypeElement tel = (TypeElement) el;
                    TypeMirror superclass = tel.getSuperclass();
                    deque.add(superclass);
                } else if (el.getKind() == ElementKind.INTERFACE) {
                    TypeElement tel = (TypeElement) el;
                    for (TypeMirror ifaceMirror : tel.getInterfaces()) {
                        deque.add(ifaceMirror);
                    }
                }
            }
        }

        return types;
    }

    /**
     * Resolves the element for the given {@code target}.
     * @param elem
     * @param target
     * @return the element or {@code null}.
     */
    public static Element resolveElement(CompilationContext info, final ELElement elem, final Node target) {
        return resolveElement(info, elem, target, Collections.<AstIdentifier, Node>emptyMap(), Collections.<VariableInfo>emptyList());
    }

    /**
     * Resolves the element for the given {@code target}.
     * @param elem
     * @param target
     * @return the element or {@code null}.
     */
    public static Element resolveElement(CompilationContext info, final ELElement elem, final Node target, Map<AstIdentifier, Node> assignments, List<VariableInfo> variableInfos) {
        TypeResolverVisitor typeResolver = new TypeResolverVisitor(info, elem, target, assignments, variableInfos);
        elem.getNode().accept(typeResolver);
        return typeResolver.getResult();
    }

    public static TypeMirror getReturnType(CompilationContext info, final ExecutableElement method) {
        return getReturnType(info, method, null, null);
    }    
    
    /**
     * Gets the return type of the given {@code method}.
     * @param method
     * @return
     */
    public static TypeMirror getReturnType(CompilationContext info, final ExecutableElement method, ELElement elElement, List<Node> rootToNode) {
        TypeKind returnTypeKind = method.getReturnType().getKind();
        if (returnTypeKind.isPrimitive()) {
            return info.info().getTypes().getPrimitiveType(returnTypeKind);
        } else if (returnTypeKind == TypeKind.VOID) {
            return info.info().getTypes().getNoType(returnTypeKind);
        } else if (returnTypeKind == TypeKind.TYPEVAR && rootToNode != null && elElement != null) {
            return getReturnTypeForGenericClass(info, method, elElement, rootToNode);
        } else {
            return method.getReturnType();
        }
    }
    
    public static TypeMirror getReturnTypeForGenericClass(CompilationContext info, final ExecutableElement method, ELElement elElement, List<Node> rootToNode) {
        Node node = null;
        for (int i = rootToNode.size() - 1; i > 0; i--) {
            node = rootToNode.get(i);
            if (node instanceof AstIdentifier) {
                break;
            }
        }
        if (node != null) {
            Element element = ELTypeUtilities.resolveElement(info, elElement, node);
            if (element == null) {
                return method.getReturnType();
            }

            TypeMirror type = element.asType();
            // interfaces are at the end of the List - first parameter has to be superclass
            TypeMirror directSupertype = info.info().getTypes().directSupertypes(type).get(0);
            if (directSupertype instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) directSupertype;
                // index of involved type argument
                int indexOfTypeArgument = -1;
                // list of all type arguments
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

                // search for the same method in the generic class
                for (Element enclosedElement : declaredType.asElement().getEnclosedElements()) {
                    if (method.equals(enclosedElement)) {
                        TypeMirror returnType = ((ExecutableElement) enclosedElement).getReturnType();
                        // get index of type argument which is returned by involved method
                        indexOfTypeArgument = info.info().getElementUtilities().enclosingTypeElement(method).
                                getTypeParameters().indexOf(((TypeVariable) returnType).asElement());
                        break;
                    }
                }
                if (indexOfTypeArgument != -1 && indexOfTypeArgument < typeArguments.size()) {
                    return typeArguments.get(indexOfTypeArgument);
                }
            }
        }

        return method.getReturnType();
    }

    
    private static List<Node> getMethodParameters(Node methodNode) {
        assert NodeUtil.isMethodCall(methodNode);
        
        if (methodNode.jjtGetNumChildren() == 0) {
            return Collections.emptyList();
        }

        Node methodArgs = methodNode.jjtGetChild(0);
        for (int i = 0; i < methodNode.jjtGetNumChildren(); i++) {
            Node currentNode = methodNode.jjtGetChild(i);
            if (currentNode instanceof AstMethodArguments) {
                methodArgs = currentNode;
                break;
            }
        }

        if (!(methodArgs instanceof AstMethodArguments)) {
            return Collections.emptyList();
        }

        List<Node> parameters = new ArrayList<>();
        for (int i = 0; i < methodArgs.jjtGetNumChildren(); i++) {
            parameters.add(methodArgs.jjtGetChild(i));
        }
        return parameters;
    }

    /**
     * Says whether the given method node and the element's method correspond.
     * @param info context
     * @param methodNode EL's method node
     * @param method method element
     * @return {@code true} if the method node correspond (param, naming) to the method element, {@code false} otherwise
     */
    public static boolean isSameMethod(CompilationContext info, Node methodNode, ExecutableElement method) {
        return isSameMethod(info, methodNode, method, false);
    }

    /**
     * Says whether the given method node and the element's method correspond.
     * @param info context
     * @param methodNode EL's method node
     * @param method method element
     * @param includeSetter whether setters method should be considered as correct - <b>not precise, do not call it in refactoring</b>
     * @return {@code true} if the method node correspond with the method element, {@code false} otherwise
     */
    public static boolean isSameMethod(CompilationContext info, Node methodNode, ExecutableElement method, boolean includeSetter) {
        String image = getMethodName(methodNode);
        String methodName = method.getSimpleName().toString();
        TypeMirror methodReturnType = method.getReturnType();
        if (image == null) {
            return false;
        }
        int methodParams = method.getParameters().size();
        if (NodeUtil.isMethodCall(methodNode) &&
                (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName, methodReturnType).equals(image))) {
            //now we are in AstDotSuffix or AstBracketSuffix

            //lets check if the parameters are equal
            List<Node> parameters = getMethodParameters(methodNode);
            int methodNodeParams = parameters.size();
            if (method.isVarArgs()) {
                return methodParams == 1 ? true : methodNodeParams >= methodParams;
            }
            return (method.getParameters().size() == methodNodeParams && haveSameParameters(info, methodNode, method))
                    || methodNodeParams == 0 && hasActionEventArgument(method);
        }

        if (methodNode instanceof AstDotSuffix) {
            if (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName, methodReturnType).equals(image)) {
                if (methodNode.jjtGetNumChildren() > 0) {
                    for (int i = 0; i < method.getParameters().size(); i++) {
                        final VariableElement methodParameter = method.getParameters().get(i);
                        final Node methodNodeParameter = methodNode.jjtGetChild(i);

                        if (!isSameType(info, methodNodeParameter, methodParameter)) {
                            return false;
                        }
                    }
                }

                if (image.equals(methodName)) {
                    return true;
                }

                return method.isVarArgs()
                        ? method.getParameters().size() == 1
                        : method.getParameters().isEmpty();
            } else if (includeSetter && RefactoringUtil.getPropertyName(methodName, methodReturnType, true).equals(image)) {
                // issue #225849 - we don't have additional information from the Facelet,
                // believe the naming conventions. This is not used for refactoring actions.
                return true;
            }
        }
        return false;
    }

    public static TypeElement getElementForType(CompilationContext info, final String clazz) {
        return info.info().getElements().getTypeElement(clazz);
    }

    public static List<String> getParameterNames(CompilationContext info, final ExecutableElement method) {
        List<String> result = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            result.add(param.getSimpleName().toString());
        }
        return result;
    }

    public static String getParametersAsString(CompilationContext info, ExecutableElement method) {
        StringBuilder result = new StringBuilder();
        for (VariableElement param : method.getParameters()) {
            if (result.length() > 0) {
                result.append(",");
            }
            String type = info.info().getTypeUtilities().getTypeName(param.asType()).toString();
            result.append(type);
            result.append(" ");
            result.append(param.getSimpleName().toString());
        }

        if (result.length() > 0) {
            result.insert(0, "(");
            result.append(")");
        }
        return result.toString();
    }

    public static Collection<ImplicitObject> getImplicitObjects(CompilationContext info) {
        return ELPlugin.Query.getImplicitObjects(info.file());
    }

    public static Collection<Function> getELFunctions(CompilationContext info) {
        return ELPlugin.Query.getFunctions(info.file());
    }

    public static boolean isScopeObject(CompilationContext info, Node target) {
        if (!(target instanceof AstIdentifier)) {
            return false;
        }
        for (ImplicitObject each : getImplicitObjects(info)) {
            if (each.getType() == ImplicitObjectType.SCOPE_TYPE
                    && each.getName().equals(target.getImage())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRawObjectReference(CompilationContext info, Node target, boolean directly) {
//        Parse tree for #{cc.attrs.muj} expression
//
//        CompositeExpression
//            DeferredExpression
//                Value
//                    Identifier[cc]
//                    PropertySuffix[attrs]
//                    PropertySuffix[muj]

        return isImplicitObjectReference(info, target, Arrays.asList(ImplicitObjectType.RAW), directly);
    }

    public static String getRawObjectName(Node root) {
        List<Node> path = new AstPath(root).rootToLeaf();
        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            if ("cc".equals(path.get(i).getImage())) { //NOI18N
                if ((i + 2) < root.jjtGetNumChildren()
                        && "attrs".equals(path.get(i + 1).getImage())) { //NOI18N
                    return path.get(i + 2).getImage();
                }
            }
        }
        return null;
    }

    public static boolean isImplicitObjectReference(CompilationContext info, Node target, List<ImplicitObjectType> types, boolean directly) {
        int repeation = directly ? 2 : Integer.MAX_VALUE;
        while (target != null && repeation > 0) {
            if (target instanceof AstIdentifier) {
                for (ImplicitObject each : getImplicitObjects(info)) {
                    if (types.contains(each.getType()) && each.getName().equals(target.getImage())) {
                        return true;
                    }
                }
            }

            target = NodeUtil.getSiblingBefore(target);
            repeation--;
        };

        return false;
    }
    
    public static boolean isResourceBundleVar(CompilationContext info, Node target) {
        if (!(target instanceof AstIdentifier)) {
            return false;
        }
        ResourceBundles resourceBundles = ResourceBundles.get(info.file());
        if (!resourceBundles.canHaveBundles()) {
            return false;
        }
        String bundleVar = target.getImage();
        return resourceBundles.isResourceBundleIdentifier(bundleVar, info.context());
    }

    /**
     * Returns name of the method which is called by bracket call.
     * @param bracketSuffixNode node of the type AstBracketSuffix
     * @return method name without any quotes or apostrophes
     */
    public static String getBracketMethodName(Node bracketSuffixNode) {
        String nameInclQuot = bracketSuffixNode.jjtGetChild(0).getImage();
        if (nameInclQuot.length() > 2) {
            return nameInclQuot.substring(1, nameInclQuot.length() - 1);
        } else {
            return ""; //NOI18N
        }
    }

    private static String getMethodName(Node methodNode) {
        if (methodNode instanceof AstBracketSuffix) {
            // method call in the brackets like bean['myCall']()
            return getBracketMethodName(methodNode);
        } else {
            // method call after the dot like bean.myCall()
            return methodNode.getImage();
        }
    }

    private static TypeMirror getTypeMirrorFor(CompilationContext info, Element element) {
        return getTypeMirrorFor(info, element, null, null);
    }

    private static TypeMirror getTypeMirrorFor(CompilationContext info, Element element, ELElement elElement, List<Node> rootToNode) {
        if (element.getKind() == ElementKind.METHOD) {
            return getReturnType(info, (ExecutableElement) element, elElement, rootToNode);
        }
        return element.asType();
    }

    private static boolean isValidatorMethod(CompilationContext info, ExecutableElement method) {
        if (method.getParameters().size() != 3) {
            return false;
        }
        VariableElement param1 = method.getParameters().get(0);
        VariableElement param2 = method.getParameters().get(1);
        CharSequence param1Type = getTypeName(info, param1.asType());
        CharSequence param2Type = getTypeName(info, param2.asType());
        return FACES_CONTEXT_CLASS.equals(param1Type) && UI_COMPONENT_CLASS.equals(param2Type);
    }

    private static CharSequence getTypeName(CompilationContext info, TypeMirror type) {
        return info.info().getTypeUtilities().getTypeName(type, TypeNameOptions.PRINT_FQN);
    }

    private static boolean haveSameParameters(CompilationContext info, Node methodNode, ExecutableElement method) {
        List<Node> methodNodeParameters = getMethodParameters(methodNode);
        for (int i = 0; i < methodNodeParameters.size(); i++) {
            Node paramNode = methodNodeParameters.get(i);
            if (!isSameType(info, paramNode, method.getParameters().get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasActionEventArgument(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();

        // check one parameter ActionEvent method
        if (parameters.size() != 1) {
            return false;
        }

        return "javax.faces.event.ActionEvent".equals(parameters.get(0).asType().toString()); //NOI18N
    }

    private static boolean isSameType(CompilationContext info, final Node paramNode, final VariableElement param) {
        TypeKind paramKind = param.asType().getKind();
        if (!paramKind.isPrimitive()) {
            // try unboxing
            try {
                PrimitiveType unboxedType = info.info().getTypes().unboxedType(param.asType());
                paramKind = unboxedType.getKind();
            } catch (IllegalArgumentException iae) {
                // not unboxable (isn't there a way to check this before trying to unbox??)
            }

        }
        if (TYPES.containsKey(paramNode.getClass())) {
            return TYPES.get(paramNode.getClass()).contains(paramKind);
        }
        if (paramNode instanceof AstString) {
            CharSequence typeName = info.info().getTypeUtilities().getTypeName(param.asType(), TypeNameOptions.PRINT_FQN);
            if (String.class.getName().contentEquals(typeName)) {
                return true;
            }
            if (paramKind == TypeKind.DECLARED) {
                return isSubtypeOf(info, param.asType(), "java.lang.Enum"); // NOI18N
            }
            return false;
        }
        // the ast param is an object whose real type we don't know
        // would need to further type inference for more exact matching
        return true;

    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private static ExecutableElement getElementForProperty(CompilationContext info, Node property, Element enclosing) {
        for (Element element : getSuperTypesFor(info, enclosing)) {
            for (ExecutableElement each : ElementFilter.methodsIn(element.getEnclosedElements())) {
                // we're only interested in public methods
                if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                if (isSameMethod(info, property, each, true)) {
                    return each;
                }
            }
        }
        return null;
    }

    private static Element getIdentifierType(CompilationContext info, AstIdentifier identifier, ELElement element) {
        if (info.file() == null) {
            // Strange case - file was deleted? Try to find out whether it's invalid.
            FileObject fileObject = element.getSnapshot().getSource().getFileObject();
            LOG.log(Level.WARNING, "FileObject to resolve doesn''t exist: {0}, isValid: {1}",
                    new Object[]{fileObject, fileObject != null ? fileObject.isValid() : "null"});
            return null;
        }
        String tempClass = null;
        // try implicit objects first
        for (ImplicitObject implicitObject : getImplicitObjects(info)) {
            if (implicitObject.getName().equals(identifier.getImage())) {
                if (implicitObject.getClazz() == null || implicitObject.getClazz().isEmpty()) {
                    // the identiefier represents an implicit object whose type we don't know
//                    tempClass = Object.class.getName();
                } else {
                    tempClass = implicitObject.getClazz();
                }
                break;
            }
        }
        if (tempClass == null) {
            // managed beans
            tempClass = ELVariableResolvers.findBeanClass(info, identifier.getImage(), element.getSnapshot().getSource().getFileObject());
        }
        if (tempClass != null) {
            return info.info().getElements().getTypeElement(tempClass);
        }

        // probably a variable
        int offset = element.getOriginalOffset().getStart() + identifier.startOffset();

        Collection<ELVariableResolver.VariableInfo> vis = ELVariableResolvers.getVariables(info, element.getSnapshot(), offset);
        for (ELVariableResolver.VariableInfo vi : vis) {
            if (identifier.getImage().equals(vi.name)) {
                try {
                    ELPreprocessor elp = new ELPreprocessor(vi.expression, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
                    Node expressionNode = ELParser.parse(elp);
                    if (expressionNode != null) {
                        return getReferredType(info, expressionNode, element.getSnapshot().getSource().getFileObject());
                    }
                } catch (ELException e) {
                    //invalid expression
                }
            }
        }

        return null;

    }

    /**
     * Resolves the given variable type
     * @param vi the variable to be resolved
     * @return source Element representing the variable
     */
    public static Element getReferredType(CompilationContext info, VariableInfo vi, FileObject context) {
        //resolved variable
        if (vi.clazz != null) {
            return info.info().getElements().getTypeElement(vi.clazz);
        }

        //unresolved variable
        assert vi.expression != null;
        try {
            ELPreprocessor elp = new ELPreprocessor(vi.expression, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
            Node expressionNode = ELParser.parse(elp);
            if (expressionNode != null) {
                return getReferredType(info, expressionNode, context);
            }
        } catch (ELException e) {
            //invalid expression
        }

        return null;
    }

    /**
     * @return the element for the type that that given {@code expression} refers to, i.e.
     * the return type of the last method in the expression.
     * 
     * The method can ONLY be used for resolved expressions, i.e. the base object must be a known bean,
     * not a variable!
     */
    public static Element getReferredType(final CompilationContext info, Node expression, final FileObject context) {

        final Element[] result = new Element[1];
        expression.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String beanClass = ELVariableResolvers.findBeanClass(info, node.getImage(), context);
                    if (beanClass == null) {
                        return;
                    }
                    Element enclosing = info.info().getElements().getTypeElement(beanClass);
                    if (enclosing == null) {
                        //no such class on the classpath
                        return;
                    }
                    ExecutableElement method, lastResolved = null;
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node current = parent.jjtGetChild(i);
                        if (enclosing != null && (current instanceof AstDotSuffix || NodeUtil.isMethodCall(current))) {
                            method = getElementForProperty(info, current, enclosing);
                            if (method == null) {
                                continue;
                            } else if (ELStreamCompletionItem.STREAM_METHOD.equals(method.getSimpleName().toString())
                                    && isIterableElement(info, enclosing)) {
                                // method is resolved - in case of JDK8 and stream used over iterable
                                break;
                            } else {
                                // issue #243833 - use last resolved method if any
                                lastResolved = method;
                            }
                            enclosing = info.info().getTypes().asElement(getReturnType(info, method));
                        }
                    }
                    if (lastResolved == null) {
                        return;
                    }
                    TypeMirror returnType = getReturnType(info, lastResolved);
                    //XXX: works just for generic collections, i.e. the assumption is
                    // that variables refer to collections, which is not always the case

                    if (returnType.getKind() == TypeKind.DECLARED) {
                        if (isSubtypeOf(info, returnType, "java.lang.Iterable")) { //NOI18N
                            List<? extends TypeMirror> typeArguments = ((DeclaredType) returnType).getTypeArguments();
                            for (TypeMirror arg : typeArguments) {
                                result[0] = info.info().getTypes().asElement(arg);
                                return;
                            }
                            //use the returned type itself
                            result[0] = info.info().getTypes().asElement(returnType);
                        }
                    } else if (returnType.getKind() == TypeKind.ARRAY) {
                        TypeMirror componentType = ((ArrayType) returnType).getComponentType();
                        result[0] = info.info().getTypes().asElement(componentType);
                    }
                }
            }
        });

        return result[0];
    }

    /**
     * Whether the given node represents static {@link Iterable} field where can be used operators.
     * @param ccontext compilation context
     * @param node node to examine
     * @return {@code true} if the object is static {@link Iterable} field, {@code false} otherwise
     * @since 1.26
     */
    public static boolean isStaticIterableElement(CompilationContext ccontext, Node node) {
        return (node instanceof AstListData || node instanceof AstMapData);
    }

    /**
     * Whether the given node represents access to the collection or array.
     * @param ccontext compilation context
     * @param node node to examine
     * @return {@code true} if the node means access to collection or array, {@code false} otherwise
     * @since 1.34
     */
    public static boolean isAccessIntoCollection(CompilationContext ccontext, Node node) {
        return (node instanceof AstInteger);
    }

    /**
     * Whether the given node represents static {@link Iterable} field where can be used operators.
     * @param ccontext compilation context
     * @param element element to examine
     * @return {@code true} if the object is static {@link Iterable} field, {@code false} otherwise
     * @since 1.26
     */
    public static boolean isIterableElement(CompilationContext ccontext, Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            return isSubtypeOf(ccontext, element.asType(), "java.lang.Iterable"); //NOI18N
        } else if (element.getKind() == ElementKind.METHOD) {
            TypeMirror returnType = ELTypeUtilities.getReturnType(ccontext, (ExecutableElement) element);
            if (returnType.getKind() == TypeKind.ARRAY
                    || isSubtypeOf(ccontext, returnType, "java.lang.Iterable")) { //NOI18N
                return true;
            }
        } else if (element.getKind() == ElementKind.INTERFACE) {
            return isSubtypeOf(ccontext, element.asType(), "java.lang.Iterable"); //NOI18N
        }
        return false;
    }

    /**
     * Whether the given node represents Map field.
     * @param ccontext compilation context
     * @param element element to examine
     * @return {@code true} if the element extends {@link Map} interface, {@code false} otherwise
     * @since 1.28
     */
    public static boolean isMapElement(CompilationContext ccontext, Element element) {
        return isSubtypeOf(ccontext, element.asType(), "java.util.Map"); //NOI18N
    }

    /**
     * Gets {@code ClasspathInfo} extended for the el-impl.jar. It guarantees to find Stream class on the classpath.
     * @param file file to be used for getting classpaths
     * @return extended classpath for the el-impl.jar
     * @since 1.39
     */
    public static ClasspathInfo getElimplExtendedCPI(FileObject file) {
        ClassPath bootPath = ClassPath.getClassPath(file, ClassPath.BOOT);
        if (bootPath == null) {
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        ClassPath compilePath = ClassPath.getClassPath(file, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = ClassPathSupport.createClassPath(new URL[0]);
        }
        ClassPath srcPath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        return ClasspathInfo.create(
                bootPath,
                ClassPathSupport.createProxyClassPath(compilePath, ClassPathSupport.createClassPath(EL_IMPL_JAR_FO)),
                srcPath);
    }

    /**
     * Gets information whether given type is inherits particular type specified by FQN.
     * @param info compilation context
     * @param tm type to be check for inheritance from typeName
     * @param typeName parent class to be inherited from
     * @return {@code true} if tm inherits the given typeName, {@code false} otherwise
     */
    public static boolean isSubtypeOf(CompilationContext info, TypeMirror tm, CharSequence typeName) {
        Element element = info.info().getElements().getTypeElement(typeName);
        if (element == null) {
            return false;
        }
        TypeMirror type = element.asType(); //NOI18N
        TypeMirror erasedType = info.info().getTypes().erasure(type);
        TypeMirror tmErasure = info.info().getTypes().erasure(tm);

        return info.info().getTypes().isSubtype(tmErasure, erasedType);
    }

    private static TypeElement getTypeFor(CompilationContext info, String clazz) {
        return info.info().getElements().getTypeElement(clazz);
    }

    private static class TypeResolverVisitor implements NodeVisitor {

        private final ELElement elem;
        private final Node target;
        private final Map<AstIdentifier, Node> assignments;
        private final List<ELVariableResolver.VariableInfo> variableInfos;
        private Element result;
        private CompilationContext info;

        public TypeResolverVisitor(CompilationContext info, ELElement elem, Node target, Map<AstIdentifier, Node> assignments, List<ELVariableResolver.VariableInfo> variableInfos) {
            this.info = info;
            this.elem = elem;
            this.target = target;
            this.assignments = assignments;
            this.variableInfos = variableInfos;
        }

        public Element getResult() {
            return result;
        }

        @Override
        public void visit(Node node) {
            Element enclosing = null;

            // look for possible assignments to the identifier
            Node evalNode;
            if (node instanceof AstIdentifier && assignments.containsKey((AstIdentifier) node)) {
                evalNode = assignments.get((AstIdentifier) node);
            } else {
                evalNode = node;
            }

            // traverses AST resolving types for each property starting from
            // an identifier until the target is found
            if (evalNode instanceof AstIdentifier) {
                enclosing = getIdentifierType(info, (AstIdentifier) evalNode, elem);
                if (enclosing != null) {
                    if (node.equals(target)) {
                        result = enclosing;
                        return;
                    }
                    Node parent = node.jjtGetParent();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (child instanceof AstDotSuffix || NodeUtil.isMethodCall(child)) {
                            Element propertyType = getElementForProperty(info, child, enclosing);
                            if (propertyType == null) {
                                // special case handling for scope objects; their types don't help
                                // in resolving the beans they contain. The code below handles cases
                                // like sessionScope.myBean => sessionScope is in position parent.jjtGetChild(i - 1)
                                if (i > 0 && isScopeObject(info, parent.jjtGetChild(i - 1))) {
                                    final String clazz = ELVariableResolvers.findBeanClass(info, child.getImage(), elem.getSnapshot().getSource().getFileObject());
                                    if (clazz == null) {
                                        return;
                                    }
                                    // it's a managed bean in a scope
                                    propertyType = getTypeFor(info, clazz);
                                }

                                // cc.attrs.<xxx> resolving - XXX better way how to detect this special case?
                                if ("cc".equals(node.getImage())) {
                                    if ("attrs".equals(child.getImage())) {
                                        propertyType = info.info().getElements().getTypeElement("java.lang.Object"); //NOI18N
                                    } else {
                                        for (VariableInfo property : variableInfos) {
                                            if (child.getImage().equals(property.name)) {
                                                if (property.clazz == null) {
                                                    propertyType = info.info().getElements().getTypeElement("java.lang.Object"); //NOI18N
                                                } else {
                                                    propertyType = info.info().getElements().getTypeElement(property.clazz);
                                                }
                                            }
                                        }
                                    }
                                }

                                // maps
                                if (ELTypeUtilities.isMapElement(info, enclosing)) {
                                    result = info.info().getElements().getTypeElement("java.lang.Object"); //NOI18N
                                    return;
                                }

                                // stream method
                                if (ELTypeUtilities.isIterableElement(info, enclosing)) {
                                    propertyType = enclosing = info.info().getElements().getTypeElement(STREAM_CLASS);
                                }
                            } else {
                                // issue #244065 - in case of JDK8 and Collection, return the EL's Stream class
                                if (propertyType instanceof ExecutableElement) {
                                    String returnType = ((ExecutableElement) propertyType).getReturnType().toString();
                                    if ("java.util.stream.Stream<E>".equals(returnType)) {  //NOI18N
                                        propertyType = info.info().getElements().getTypeElement(STREAM_CLASS);
                                    }
                                }
                            }
                            if (propertyType == null) {
                                return;
                            }
                            if (child.equals(target)) {
                                result = propertyType;
                            } else if (propertyType.getKind() == ElementKind.METHOD) {
                                final ExecutableElement method = (ExecutableElement) propertyType;
                                TypeMirror returnType = getReturnType(info, method, elem, NodeUtil.getRootToNode(elem, target));
                                if (returnType.getKind() == TypeKind.ARRAY) {
                                    // for array try to look like Iterable (operators for array return type)
                                    enclosing = info.info().getElements().getTypeElement("java.lang.Iterable"); //NOI18N
                                } else {
                                    if (isAccessIntoCollection(info, target) && returnType instanceof DeclaredType) {
                                        List<? extends TypeMirror> typeArguments = ((DeclaredType) returnType).getTypeArguments();
                                        if (!typeArguments.isEmpty()) {
                                            enclosing = info.info().getTypes().asElement(typeArguments.get(0));
                                            result = enclosing;
                                        }
                                    } else {
                                        enclosing = info.info().getTypes().asElement(returnType);
                                    }
                                }
                                if (enclosing == null) {
                                    return;
                                }
                            } else {
                                enclosing = propertyType;
                            }
                        }
                    }
                }
            } else if (evalNode instanceof AstListData || evalNode instanceof AstMapData) {
                Node parent = node.jjtGetParent();
                for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                    Node child = parent.jjtGetChild(i);
                    if (child instanceof AstDotSuffix) {
                        if (ELStreamCompletionItem.STREAM_METHOD.equals(child.getImage())) {
                            enclosing = info.info().getElements().getTypeElement(STREAM_CLASS);
                        } else {
                            if (enclosing != null) {
                                ExecutableElement propertyType = getElementForProperty(info, child, enclosing);
                                if (target.getImage() != null && target.getImage().equals(child.getImage())) {
                                    if (propertyType != null) {
                                        result = propertyType;
                                    }
                                    return;
                                } else {
                                    if (propertyType != null) {
                                        enclosing = getTypeFor(info, propertyType.getReturnType().toString());
                                    }
                                }
                            }
                        }
                        // finish when the target matches property
                        if (target.getImage() != null && target.getImage().equals(child.getImage())) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
