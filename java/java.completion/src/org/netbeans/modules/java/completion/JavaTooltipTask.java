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

package org.netbeans.modules.java.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collections;
import javax.lang.model.element.AnnotationValue;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.*;

/**
 *
 * @author Dusan Balek
 */
public final class JavaTooltipTask extends BaseTask {

    public static JavaTooltipTask create(final int caretOffset, @NullAllowed final Callable<Boolean> cancel) {
        return new JavaTooltipTask(caretOffset, cancel);
    }

    private static final String INIT = "<init>"; //NOI18N
    private static final String THIS_KEYWORD = "this"; //NOI18N
    private static final String SUPER_KEYWORD = "super"; //NOI18N

    private int anchorOffset;
    private List<List<String>> toolTipData;
    private List<String> toolTipSignatures;
    private int toolTipIndex;
    private int activeSignatureIndex;
    private int toolTipOffset;

    private JavaTooltipTask(final int caretOffset, final Callable<Boolean> cancel) {
        super(caretOffset, cancel);
    }

    public List<List<String>> getTooltipData() {
        return toolTipData;
    }

    public List<String> getTooltipSignatures() {
        return toolTipSignatures;
    }

    public int getTooltipIndex() {
        return toolTipIndex;
    }

    public int getActiveSignatureIndex() {
        return activeSignatureIndex;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    public int getTooltipOffset() {
        return toolTipOffset;
    }

    @Override
    protected void resolve(CompilationController controller) throws IOException {
        Env env = getCompletionEnvironment(controller, false);
        if (env == null) {
            return;
        }
        Tree lastTree = null;
        int offset = env.getOffset();
        TreePath path = env.getPath();
        while (path != null) {
            Tree tree = path.getLeaf();
            if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
                MethodInvocationTree mi = (MethodInvocationTree) tree;
                CompilationUnitTree root = env.getRoot();
                SourcePositions sourcePositions = env.getSourcePositions();
                int startPos = lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset;
                List<Tree> argTypes = getArgumentsUpToPos(env, mi.getArguments(), (int) sourcePositions.getEndPosition(root, mi.getMethodSelect()), startPos, false);
                if (argTypes != null) {
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    final Trees trees = controller.getTrees();
                    TypeMirror[] types = new TypeMirror[argTypes.size()];
                    int j = 0;
                    for (Tree t : argTypes) {
                        types[j++] = trees.getTypeMirror(new TreePath(path, t));
                    }
                    final Tree mid = mi.getMethodSelect();
                    final Element activeElement = trees.getElement(path);
                    path = new TreePath(path, mid);
                    switch (mid.getKind()) {
                        case MEMBER_SELECT: {
                            ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                            path = new TreePath(path, exp);
                            final TypeMirror type = trees.getTypeMirror(path);
                            final Element element = trees.getElement(path);
                            final boolean isStatic = element != null && (element.getKind().isClass() || element.getKind().isInterface() || element.getKind() == TYPE_PARAMETER);
                            final boolean isSuperCall = element != null && element.getKind().isField() && element.getSimpleName().contentEquals(SUPER_KEYWORD);
                            final Scope scope = env.getScope();
                            TypeElement enclClass = scope.getEnclosingClass();
                            final TypeMirror enclType = enclClass != null ? enclClass.asType() : null;
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                @Override
                                public boolean accept(Element e, TypeMirror t) {
                                    return (!isStatic || e.getModifiers().contains(STATIC) || e.getKind() == CONSTRUCTOR) && (t.getKind() != TypeKind.DECLARED || trees.isAccessible(scope, e, (DeclaredType) (isSuperCall && enclType != null ? enclType : t)));
                                }
                            };
                            handleMatchingParams(controller, type, activeElement, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree) mid).getIdentifier().toString(), types);
                            break;
                        }
                        case IDENTIFIER: {
                            final Scope scope = env.getScope();
                            final TreeUtilities tu = controller.getTreeUtilities();
                            final TypeElement enclClass = scope.getEnclosingClass();
                            final boolean isStatic = enclClass != null ? (tu.isStaticContext(scope) || (env.getPath().getLeaf().getKind() == Tree.Kind.BLOCK && ((BlockTree) env.getPath().getLeaf()).isStatic())) : false;
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                @Override
                                public boolean accept(Element e, TypeMirror t) {
                                    switch (e.getKind()) {
                                        case CONSTRUCTOR:
                                            return !e.getModifiers().contains(PRIVATE);
                                        case METHOD:
                                            return (!isStatic || e.getModifiers().contains(STATIC)) && trees.isAccessible(scope, e, (DeclaredType) t);
                                        default:
                                            return false;
                                    }
                                }
                            };
                            String name = ((IdentifierTree) mid).getName().toString();
                            if (SUPER_KEYWORD.equals(name) && enclClass != null) {
                                TypeMirror superclass = enclClass.getSuperclass();
                                handleMatchingParams(controller, superclass, activeElement, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types);
                            } else if (THIS_KEYWORD.equals(name) && enclClass != null) {
                                TypeMirror thisclass = enclClass.asType();
                                handleMatchingParams(controller, thisclass, activeElement, controller.getElementUtilities().getMembers(thisclass, acceptor), INIT, types);
                            } else {
                                handleMatchingParams(controller, enclClass != null ? enclClass.asType() : null, activeElement, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), name, types);
                            }
                            break;
                        }
                    }
                    toolTipIndex = types.length;
                    startPos = (int) sourcePositions.getEndPosition(env.getRoot(), mi.getMethodSelect());
                    String text = controller.getText().substring(startPos, offset);
                    int idx = text.indexOf('('); //NOI18N
                    anchorOffset = idx < 0 ? startPos : startPos + controller.getSnapshot().getOriginalOffset(idx);
                    idx = text.lastIndexOf(','); //NOI18N
                    toolTipOffset = idx < 0 ? startPos : startPos + controller.getSnapshot().getOriginalOffset(idx);
                    if (toolTipOffset < anchorOffset) {
                        toolTipOffset = anchorOffset;
                    }
                    return;
                }
            } else if (tree.getKind() == Tree.Kind.NEW_CLASS) {
                NewClassTree nc = (NewClassTree) tree;
                CompilationUnitTree root = env.getRoot();
                SourcePositions sourcePositions = env.getSourcePositions();
                int startPos = lastTree != null ? (int) sourcePositions.getStartPosition(root, lastTree) : offset;
                int pos = (int) sourcePositions.getEndPosition(root, nc.getIdentifier());
                List<Tree> argTypes = getArgumentsUpToPos(env, nc.getArguments(), pos, startPos, false);
                if (argTypes != null) {
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    final Trees trees = controller.getTrees();
                    TypeMirror[] types = new TypeMirror[argTypes.size()];
                    int j = 0;
                    for (Tree t : argTypes) {
                        types[j++] = trees.getTypeMirror(new TreePath(path, t));
                    }
                    final Element activeElement = trees.getElement(path);
                    path = new TreePath(path, nc.getIdentifier());
                    TypeMirror type = trees.getTypeMirror(path);
                    if (type != null && type.getKind() == TypeKind.ERROR && path.getLeaf().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                        path = new TreePath(path, ((ParameterizedTypeTree) path.getLeaf()).getType());
                        type = trees.getTypeMirror(path);
                    }
                    final Element el = trees.getElement(path);
                    final Scope scope = env.getScope();
                    final boolean isAnonymous = nc.getClassBody() != null || (el != null && (el.getKind().isInterface() || el.getModifiers().contains(ABSTRACT)));
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        @Override
                        public boolean accept(Element e, TypeMirror t) {
                            return e.getKind() == CONSTRUCTOR && (trees.isAccessible(scope, e, (DeclaredType) t) || isAnonymous && e.getModifiers().contains(PROTECTED));
                        }
                    };
                    handleMatchingParams(controller, type, activeElement, controller.getElementUtilities().getMembers(type, acceptor), INIT, types);
                    toolTipIndex = types.length;
                    if (pos < 0) {
                        path = path.getParentPath();
                        pos = (int) sourcePositions.getStartPosition(root, path.getLeaf());
                    }
                    String text = controller.getText().substring(pos, offset);
                    int idx = text.indexOf('('); //NOI18N
                    anchorOffset = idx < 0 ? pos : pos + controller.getSnapshot().getOriginalOffset(idx);
                    idx = text.lastIndexOf(','); //NOI18N
                    toolTipOffset = idx < 0 ? pos : pos + controller.getSnapshot().getOriginalOffset(idx);
                    if (toolTipOffset < anchorOffset) {
                        toolTipOffset = anchorOffset;
                    }
                    return;
                }
            } else if (tree.getKind() == Tree.Kind.ANNOTATION) {
                AnnotationTree at = (AnnotationTree) tree;
                controller.toPhase(JavaSource.Phase.RESOLVED);
                final Trees trees = controller.getTrees();
                final Element element = trees.getElement(path);
                if (element != null && element.getKind() == ANNOTATION_TYPE) {
                    final Element activeElement = lastTree != null && lastTree.getKind() == Tree.Kind.ASSIGNMENT ? trees.getElement(new TreePath(path, ((AssignmentTree) lastTree).getVariable())) : null;
                    TypeUtilities tu = controller.getTypeUtilities();
                    List<List<String>> data = new ArrayList<>();
                    List<String> signatures = new ArrayList<>();
                    for (Element e : element.getEnclosedElements()) {
                        if (e.getKind() == METHOD && e.asType().getKind() == TypeKind.EXECUTABLE) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(tu.getTypeName(((ExecutableType) e.asType()).getReturnType())).append(' '); //NOI18N
                            sb.append(e.getSimpleName()).append("()"); //NOI18N
                            AnnotationValue defaultValue = ((ExecutableElement) e).getDefaultValue();
                            if (defaultValue != null) {
                                sb.append(" default ").append(defaultValue.toString());
                            }
                            if (e == activeElement) {
                                activeSignatureIndex = signatures.size();
                            }
                            data.add(Collections.singletonList(sb.toString()));
                            signatures.add(sb.toString());
                        }
                    }
                    toolTipData = data.isEmpty() ? null : data;
                    toolTipSignatures = signatures.isEmpty() ? null : signatures;
                    toolTipIndex = -1;
                    CompilationUnitTree root = env.getRoot();
                    SourcePositions sourcePositions = env.getSourcePositions();
                    int pos = (int) sourcePositions.getEndPosition(root, at.getAnnotationType());
                    String text = controller.getText().substring(pos, offset);
                    int idx = text.indexOf('('); //NOI18N
                    anchorOffset = idx < 0 ? pos : pos + controller.getSnapshot().getOriginalOffset(idx);
                    idx = text.lastIndexOf(','); //NOI18N
                    toolTipOffset = idx < 0 ? pos : pos + controller.getSnapshot().getOriginalOffset(idx);
                    if (toolTipOffset < anchorOffset) {
                        toolTipOffset = anchorOffset;
                    }
                    return;
                }
            }
            lastTree = tree;
            path = path.getParentPath();
        }
    }

    private void handleMatchingParams(CompilationInfo info, TypeMirror type, Element activeElement, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes) {
        List<List<String>> data = new ArrayList<>();
        List<String> signatures = new ArrayList<>();
        Types types = info.getTypes();
        TypeUtilities tu = info.getTypeUtilities();
        activeSignatureIndex = 0;
        for (Element e : elements) {
            if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                List<? extends VariableElement> params = ((ExecutableElement) e).getParameters();
                int parSize = params.size();
                boolean varArgs = ((ExecutableElement) e).isVarArgs();
                if (!varArgs && (parSize < argTypes.length)) {
                    continue;
                }
                if (e == activeElement) {
                    activeSignatureIndex = signatures.size();
                }
                ExecutableType eType = (ExecutableType) asMemberOf(e, type, types);
                StringBuilder sig = new StringBuilder(INIT.equals(name) && type != null && type.getKind() == TypeKind.DECLARED ? ((DeclaredType) type).asElement().getSimpleName() : name).append('(');
                if (parSize == 0) {
                    data.add(new ArrayList<>());
                    sig.append(')');
                    if (e.getKind() == METHOD) {
                        sig.append(" : ").append(tu.getTypeName(eType.getReturnType()));
                    }
                    signatures.add(sig.toString());
                } else {
                    Iterator<? extends TypeMirror> parIt = eType.getParameterTypes().iterator();
                    TypeMirror param = null;
                    for (int i = 0; i <= argTypes.length; i++) {
                        if (parIt.hasNext()) {
                            param = parIt.next();
                            if (!parIt.hasNext() && param.getKind() == TypeKind.ARRAY) {
                                param = ((ArrayType) param).getComponentType();
                            }
                        } else if (!varArgs) {
                            break;
                        }
                        if (i == argTypes.length) {
                            List<String> paramStrings = new ArrayList<>(parSize);
                            Iterator<? extends TypeMirror> tIt = eType.getParameterTypes().iterator();
                            for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                                VariableElement ve = it.next();
                                StringBuilder sb = new StringBuilder();
                                CharSequence typeName = tu.getTypeName(tIt.next());
                                sb.append(typeName);
                                sig.append(typeName);
                                if (varArgs && !tIt.hasNext()) {
                                    sb.delete(sb.length() - 2, sb.length()).append("..."); //NOI18N
                                    sig.delete(sig.length() - 2, sig.length()).append("..."); //NOI18N
                                }
                                CharSequence veName = ve.getSimpleName();
                                if (veName != null && veName.length() > 0) {
                                    sb.append(" ").append(veName); // NOI18N
                                    sig.append(" ").append(veName); // NOI18N
                                }
                                if (it.hasNext()) {
                                    sig.append(", "); // NOI18N
                                }
                                paramStrings.add(sb.toString());
                            }
                            data.add(paramStrings);
                            sig.append(')');
                            if (e.getKind() == METHOD) {
                                sig.append(" : ").append(tu.getTypeName(eType.getReturnType()));
                            }
                            signatures.add(sig.toString());
                            break;
                        }
                        if (argTypes[i] == null || argTypes[i].getKind() != TypeKind.ERROR && !isAssignable(types, argTypes[i], param)) {
                            break;
                        }
                    }
                }
            }
        }
        toolTipData = data.isEmpty() ? null : data;
        toolTipSignatures = signatures.isEmpty() ? null : signatures;
    }

    private static boolean isAssignable(Types types, TypeMirror arg, TypeMirror parameter) {
        if(types.isAssignable(arg, parameter)) {
            return true;
        } else if (parameter instanceof TypeVariable) {
            TypeMirror requiredTypes = ((TypeVariable) parameter).getUpperBound();
            return types.isAssignable(arg, requiredTypes);
        }
        return false;
    }
}
