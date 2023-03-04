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

package org.netbeans.modules.java.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.*;
import org.openide.util.NbBundle;

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
    private int toolTipIndex;
    private int toolTipOffset;

    private JavaTooltipTask(final int caretOffset, final Callable<Boolean> cancel) {
        super(caretOffset, cancel);
    }

    public List<List<String>> getTooltipData() {
        return toolTipData;
    }

    public int getTooltipIndex() {
        return toolTipIndex;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    public int getTooltipOffset() {
        return toolTipOffset;
    }

    @Override
    protected void resolve(CompilationController controller) throws IOException {
        Env env = getCompletionEnvironment(controller, true);
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
                    TypeMirror[] types = new TypeMirror[argTypes.size()];
                    int j = 0;
                    for (Tree t : argTypes) {
                        types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                    }
                    Tree mid = mi.getMethodSelect();
                    path = new TreePath(path, mid);
                    switch (mid.getKind()) {
                        case MEMBER_SELECT: {
                            ExpressionTree exp = ((MemberSelectTree) mid).getExpression();
                            path = new TreePath(path, exp);
                            final Trees trees = controller.getTrees();
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
                            toolTipData = getMatchingParams(controller, type, controller.getElementUtilities().getMembers(type, acceptor), ((MemberSelectTree) mid).getIdentifier().toString(), types, controller.getTypes());
                            break;
                        }
                        case IDENTIFIER: {
                            final Scope scope = env.getScope();
                            final TreeUtilities tu = controller.getTreeUtilities();
                            final Trees trees = controller.getTrees();
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
                                toolTipData = getMatchingParams(controller, superclass, controller.getElementUtilities().getMembers(superclass, acceptor), INIT, types, controller.getTypes());
                            } else if (THIS_KEYWORD.equals(name) && enclClass != null) {
                                TypeMirror thisclass = enclClass.asType();
                                toolTipData = getMatchingParams(controller, thisclass, controller.getElementUtilities().getMembers(thisclass, acceptor), INIT, types, controller.getTypes());
                            } else {
                                toolTipData = getMatchingParams(controller, enclClass != null ? enclClass.asType() : null, controller.getElementUtilities().getLocalMembersAndVars(scope, acceptor), name, types, controller.getTypes());
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
                    TypeMirror[] types = new TypeMirror[argTypes.size()];
                    int j = 0;
                    for (Tree t : argTypes) {
                        types[j++] = controller.getTrees().getTypeMirror(new TreePath(path, t));
                    }
                    path = new TreePath(path, nc.getIdentifier());
                    final Trees trees = controller.getTrees();
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
                    toolTipData = getMatchingParams(controller, type, controller.getElementUtilities().getMembers(type, acceptor), INIT, types, controller.getTypes());
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
            }
            lastTree = tree;
            path = path.getParentPath();
        }
    }

    private List<List<String>> getMatchingParams(CompilationInfo info, TypeMirror type, Iterable<? extends Element> elements, String name, TypeMirror[] argTypes, Types types) {
        List<List<String>> ret = new ArrayList<>();
        TypeUtilities tu = info.getTypeUtilities();
        for (Element e : elements) {
            if ((e.getKind() == CONSTRUCTOR || e.getKind() == METHOD) && name.contentEquals(e.getSimpleName())) {
                List<? extends VariableElement> params = ((ExecutableElement) e).getParameters();
                int parSize = params.size();
                boolean varArgs = ((ExecutableElement) e).isVarArgs();
                if (!varArgs && (parSize < argTypes.length)) {
                    continue;
                }
                if (parSize == 0) {
                    ret.add(Collections.<String>singletonList(NbBundle.getMessage(JavaCompletionTask.class, "JCP-no-parameters")));
                } else {
                    ExecutableType eType = (ExecutableType) asMemberOf(e, type, types);
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
                                sb.append(tu.getTypeName(tIt.next()));
                                if (varArgs && !tIt.hasNext()) {
                                    sb.delete(sb.length() - 2, sb.length()).append("..."); //NOI18N
                                }
                                CharSequence veName = ve.getSimpleName();
                                if (veName != null && veName.length() > 0) {
                                    sb.append(" "); // NOI18N
                                    sb.append(veName);
                                }
                                if (it.hasNext()) {
                                    sb.append(", "); // NOI18N
                                }
                                paramStrings.add(sb.toString());
                            }
                            ret.add(paramStrings);
                            break;
                        }
                        if (argTypes[i] == null || argTypes[i].getKind() != TypeKind.ERROR && !types.isAssignable(argTypes[i], param)) {
                            break;
                        }
                    }
                }
            }
        }
        return ret.isEmpty() ? null : ret;
    }
}
