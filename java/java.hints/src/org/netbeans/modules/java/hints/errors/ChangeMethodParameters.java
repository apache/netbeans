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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;


public class ChangeMethodParameters implements ErrorRule<Void> {
    private String DEFAULT_NAME = "par";
    private boolean cancel = false;

    @Override
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.apply.symbol",
                                                 "compiler.err.cant.apply.symbol.1",
                                                 "compiler.err.cant.apply.symbols",
                                                 "compiler.err.prob.found.req")); // NOI18N
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            cancel = false;
            return analyze(info, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    @Override
    public String getId() {
        return ChangeMethodParameters.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CreateElement.class, "LBL_ChangeMethodParameters"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(CreateElement.class, "DSC_ChangeMethodParameters"); // NOI18N
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);
        if (errorPath == null || /*#208510, usecase unknown:*/errorPath.getParentPath() == null) return Collections.<Fix>emptyList();
        
        Tree error = errorPath.getParentPath().getLeaf();
        if (error == null) return Collections.<Fix>emptyList();
        
        if (info.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<Fix>emptyList();
        }
        LinkedList<Fix> fixes = new LinkedList<>();
        if(error.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree invocation = (MethodInvocationTree) error;
            List<? extends ExpressionTree> arguments = invocation.getArguments();
            
            TreePath enclosingTypePath = null;
            Name methodName = null;
            switch (invocation.getMethodSelect().getKind()) {
                case IDENTIFIER:
                    methodName = ((IdentifierTree) invocation.getMethodSelect()).getName();
                    enclosingTypePath = findEnclosingType(errorPath.getParentPath());
                    break;
                case MEMBER_SELECT:
                    methodName = ((MemberSelectTree) invocation.getMethodSelect()).getIdentifier();
                    TypeMirror type = info.getTrees().getTypeMirror(new TreePath(errorPath, ((MemberSelectTree) invocation.getMethodSelect()).getExpression()));
                    Element element = type != null && type.getKind() == TypeKind.DECLARED ? ((DeclaredType) type).asElement() : null;
                    enclosingTypePath = element != null ? info.getTrees().getPath(element) : null;
                    if (enclosingTypePath == null) {
                        ElementHandle<Element> eh = ElementHandle.create(element);
                        FileObject file = SourceUtils.getFile(eh, info.getClasspathInfo());
                        JavaSource js = file != null ? JavaSource.forFileObject(file) : null;
                        if (js != null) {
                            Pair<List<? extends TypeMirror>, List<String>> formalArguments = Utilities.resolveArguments(info, errorPath.getParentPath(), arguments, element);
                            if (formalArguments != null) {
                                Name mName = methodName;
                                js.runUserActionTask(cInfo -> {
                                    cInfo.toPhase(JavaSource.Phase.RESOLVED);
                                    Element e = eh.resolve(cInfo);
                                    TreePath elPath = e != null ? cInfo.getTrees().getPath(e): null;
                                    if (elPath != null) {
                                        List<TreePath> methods = new LinkedList<>();
                                        for (Tree tree : ((ClassTree) elPath.getLeaf()).getMembers()) {
                                            if (tree.getKind() == Tree.Kind.METHOD) {
                                                MethodTree method = (MethodTree) tree;
                                                if (method.getName().contentEquals(mName)) {
                                                    methods.add(new TreePath(elPath, method));
                                                }
                                            }
                                        }
                                        for (TreePath path : methods) {
                                            ExecutableElement method = (ExecutableElement) cInfo.getTrees().getElement(path);
                                            createFixes(cInfo, arguments, path, elPath, method, fixes);
                                        }
                                    }
                                }, true);
                                return fixes;
                            }
                        }
                    }
                    break;

            }

            if (enclosingTypePath == null || methodName == null) {
                return Collections.<Fix>emptyList();
            }

            Pair<List<? extends TypeMirror>, List<String>> formalArguments = Utilities.resolveArguments(info, errorPath.getParentPath(), arguments, info.getTrees().getElement(enclosingTypePath));

            //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
            if (formalArguments == null) {
                return Collections.<Fix>emptyList();
            }
            
            List<TreePath> methods = new LinkedList<>();
            for (Tree tree : ((ClassTree) enclosingTypePath.getLeaf()).getMembers()) {
                if (cancel) return Collections.<Fix>emptyList();
                if (tree.getKind() == Tree.Kind.METHOD) {
                    MethodTree method = (MethodTree) tree;
                    if (method.getName().contentEquals(methodName)) {
                        methods.add(new TreePath(enclosingTypePath, method));
                    }
                }
            }

            for (TreePath path : methods) {
                if(cancel) return Collections.<Fix>emptyList();
                ExecutableElement method = (ExecutableElement) info.getTrees().getElement(path);
                if (!createFixes(info, arguments, path, enclosingTypePath, method, fixes)) {
                    return Collections.<Fix>emptyList();
                }
            }
        } else if(error.getKind() == Tree.Kind.NEW_CLASS) {
            NewClassTree invocation = (NewClassTree) error;
            
            TreePath enclosingTypePath = findEnclosingType(errorPath.getParentPath());
            if (enclosingTypePath == null) {
                return Collections.<Fix>emptyList();
            }
            
            List<? extends ExpressionTree> arguments = invocation.getArguments();

            Element element = info.getTrees().getElement(new TreePath(errorPath, invocation.getIdentifier()));
            Element enclosingType = info.getTrees().getElement(enclosingTypePath);
            
            if (element != null) {
                if (element.equals(enclosingType)) {
                    Pair<List<? extends TypeMirror>, List<String>> formalArguments = Utilities.resolveArguments(info, errorPath.getParentPath(), arguments, info.getTrees().getElement(enclosingTypePath));
                    //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
                    if (formalArguments == null) {
                        return Collections.<Fix>emptyList();
                    }

                    for (ExecutableElement method : ElementFilter.constructorsIn(((TypeElement)enclosingType).getEnclosedElements())) {
                        if(cancel) return Collections.<Fix>emptyList();

                        TreePath path = info.getTrees().getPath(method);
                        if(path == null) continue;
                        if (!createFixes(info, arguments, path, enclosingTypePath, method, fixes)) {
                            return Collections.<Fix>emptyList();
                        }
                    }
                } else {
                    ElementHandle<Element> eh = ElementHandle.create(element);
                    FileObject file = SourceUtils.getFile(eh, info.getClasspathInfo());
                    JavaSource js = file != null ? JavaSource.forFileObject(file) : null;
                    if (js != null) {
                        Pair<List<? extends TypeMirror>, List<String>> formalArguments = Utilities.resolveArguments(info, errorPath.getParentPath(), arguments, element);
                        if (formalArguments != null) {
                            js.runUserActionTask(cInfo -> {
                                cInfo.toPhase(JavaSource.Phase.RESOLVED);
                                Element e = eh.resolve(cInfo);
                                TreePath elPath = e != null && e.getKind().isClass() ? cInfo.getTrees().getPath(e): null;
                                if (elPath != null) {
                                    for (ExecutableElement method : ElementFilter.constructorsIn(((TypeElement)e).getEnclosedElements())) {
                                        TreePath path = cInfo.getTrees().getPath(method);
                                        if (path != null) {
                                            createFixes(cInfo, arguments, path, elPath, method, fixes);
                                        }
                                    }
                                }
                            }, true);
                            return fixes;
                        }
                    }
                }
            }
        }
        
        return fixes;
    }
    
    private static String makeNameUnique(CompilationInfo info, Scope s, String name, ParameterInfo[] parameters, int current) {
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            if (s != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new Utilities.VariablesFilter())) {
                    if (proposedName.equals(e.getSimpleName().toString())) {
                        counter++;
                        cont = true;
                        break;
                    }
                }
            }
            for (int i = 0; i < parameters.length; i++) {
                ParameterInfo parameterInfo = parameters[i];
                if (current != i && parameterInfo != null && proposedName.equals(parameterInfo.getName())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        }
        
        return proposedName;
    }
    
    public String genDeclarationString(TypeElement typeElement, MethodTree methodTree, ChangeParametersRefactoring.ParameterInfo[] parameters) {
        StringBuilder buf = new StringBuilder();
        buf.append(getMethodName(methodTree, typeElement));
        buf.append('('); //NOI18N
        if (parameters.length > 0) {
            int i;
            for (i = 0; i < parameters.length - 1; i++) {
                buf.append(parameters[i].getType());
                buf.append(' '); //NOI18N
                buf.append(parameters[i].getName());
                buf.append(',').append(' '); //NOI18N
            }
            buf.append(parameters[i].getType());
            buf.append(' '); //NOI18N
            buf.append(parameters[i].getName());
        }
        buf.append(')'); //NOI18N
        
        return buf.toString();
    }

    private TreePath findEnclosingType(TreePath parentPath) {
        TreePath klazz = parentPath;
        while(klazz != null) {
            if(TreeUtilities.CLASS_TREE_KINDS.contains(klazz.getLeaf().getKind())) 
                return klazz;
            klazz = klazz.getParentPath();
        }
        return null;
    }

    private ParameterInfo findNextByType(CompilationInfo info, ParameterInfo[] parameterInfo, String type, TypeElement scopeType) {
        for (int i = 0; i < parameterInfo.length; i++) {
            ParameterInfo param = parameterInfo[i];
            if(param.getOriginalIndex() > -1 && isSameType(info, type, param.getType(), scopeType)) {
                parameterInfo[i] = new ParameterInfo(-1, param.getName(), param.getType(), null); // NOI18N
                return param;
            }
        }
        return null;
    }

    private boolean isSameType(CompilationInfo info, String from, String to, TypeElement scopeType) {
        if(from.equals(to)) {
            return true;
        } else {
            Element fromElement = info.getTypes().asElement(info.getTreeUtilities().parseType(from, scopeType));
            Element toElement = info.getTypes().asElement(info.getTreeUtilities().parseType(to, scopeType));
            
            if (fromElement != null && toElement != null) {
                
                if(!(fromElement.getKind().isClass() || fromElement.getKind().isInterface() || fromElement.getKind().isField())) {
                    return false;
                }

                if(!(toElement.getKind().isClass() || toElement.getKind().isInterface() || toElement.getKind().isField())) {
                    return false;
                }

                TypeElement fromType = (TypeElement) fromElement;
                TypeElement toType = (TypeElement) toElement;

                return info.getTypes().isSubtype(fromType.asType(), toType.asType());
            } else {
                return false;
            }
        }
    }

    private boolean createFixes(CompilationInfo info, List<? extends ExpressionTree> arguments, TreePath path, TreePath enclosingTypePath, ExecutableElement method, LinkedList<Fix> fixes) throws IllegalArgumentException {
        if (method == null) {
            return false;
        }
        List<? extends VariableElement> parameters = method.getParameters();
        ChangeParametersRefactoring.ParameterInfo[] parameterInfo = new ChangeParametersRefactoring.ParameterInfo[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement param = parameters.get(i);
            VariableTree parTree = (VariableTree) info.getTrees().getTree(param);
            parameterInfo[i] = new ChangeParametersRefactoring.ParameterInfo(i, param.toString(), parTree.getType().toString(), null);
        }
        ChangeParametersRefactoring.ParameterInfo[] newParameterInfo = new ChangeParametersRefactoring.ParameterInfo[arguments.size()];
        MethodTree methodTree = (MethodTree) path.getLeaf();
        BlockTree methodBody = methodTree.getBody();
        Scope scope =  null;
        if(methodBody != null) {
            TreePath bodyPath = new TreePath(path, methodBody);
            scope = info.getTrees().getScope(bodyPath);
        } else {
            scope = info.getTrees().getScope(path);
        } 
        int i = 0;
        for (ExpressionTree argument : arguments) {
            if (cancel) {
                return false;
            }
            TreePath argumentPath = new TreePath(path, argument);
            TypeMirror argumentType = info.getTrees().getTypeMirror(argumentPath);
            String type = argumentType.toString();
            String name = org.netbeans.modules.editor.java.Utilities.varNameSuggestion(argumentPath);
            if (name == null) {
                name = DEFAULT_NAME;
            }
            name = makeNameUnique(info, scope, name, newParameterInfo, i);
            newParameterInfo[i] = new ChangeParametersRefactoring.ParameterInfo(-1, name, type, argument.toString());
            i++;
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(enclosingTypePath);
        // Find old parameters with the same type and copy the information
        for (i = 0; i < newParameterInfo.length; i++) {
            if (cancel) {
                return false;
            }
            if (typeElement == null) {
                continue;
            }
            ParameterInfo param = newParameterInfo[i];
            ParameterInfo next = findNextByType(info, parameterInfo, param.getType(), typeElement);
            if (next != null) {
                newParameterInfo[i] = new ParameterInfo(next.getOriginalIndex(), next.getName(), next.getType(), param.getDefaultValue());
            }
        }
        // Reaplace parameter types with simple type names
        for (i = 0; i < newParameterInfo.length; i++) {
            if (cancel) {
                return false;
            }
            ParameterInfo param = newParameterInfo[i];
            TypeMirror type = info.getTreeUtilities().parseType(param.getType(), typeElement);
            String typeString = org.netbeans.modules.editor.java.Utilities.getTypeName(info, type, false).toString();
            newParameterInfo[i] = new ParameterInfo(param.getOriginalIndex(), param.getName(), typeString, param.getDefaultValue());
        }
        // Find old parameters with the same index and copy the information if removed
        for (i = 0; i < newParameterInfo.length; i++) {
            if (cancel) {
                return false;
            }
            ParameterInfo param = newParameterInfo[i];
            if (param.getOriginalIndex() == -1 &&
                    parameterInfo.length > i &&
                    parameterInfo[i].getOriginalIndex() != -1) {
                newParameterInfo[i] = new ParameterInfo(parameterInfo[i].getOriginalIndex(), parameterInfo[i].getName(), param.getType(), param.getDefaultValue());
            }
        }
        TreePathHandle tph = TreePathHandle.create(path, info);
        boolean doFullRefactoring = true;
        if(methodTree.getModifiers().getFlags().contains(Modifier.PRIVATE)) {
            doFullRefactoring = false;
        }
        Set<Modifier> modifiers = method.getModifiers();
        fixes.add(new ChangeParametersFix(doFullRefactoring, tph, modifiers, genDeclarationString(typeElement, methodTree, parameterInfo), genDeclarationString(typeElement, methodTree, newParameterInfo), newParameterInfo, method.getKind() == ElementKind.CONSTRUCTOR));
        return true;
    }

    private String getMethodName(MethodTree methodTree, TypeElement typeElement) {
        String name = methodTree.getName().toString();
        if(name.equals("<init>")) { //NOI18N
            name = typeElement.getSimpleName().toString();
        }
        return name;
    }
}
