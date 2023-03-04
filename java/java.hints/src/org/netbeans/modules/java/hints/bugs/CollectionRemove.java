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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.CollectionRemove", description = "#DESC_org.netbeans.modules.java.hints.bugs.CollectionRemove", id="org.netbeans.modules.java.hints.bugs.CollectionRemove",
      category="bugs",
      suppressWarnings={CollectionRemove.SUPPRESS_WARNING_KEY, "", "collection-remove"},
      options=Options.QUERY)
public class CollectionRemove {

            static final String  SUPPRESS_WARNING_KEY = "element-type-mismatch";
    static final boolean WARN_FOR_CASTABLE_DEFAULT = true;
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.bugs.CollectionRemove.WARN_FOR_CASTABLE_KEY", tooltip = "#TP_org.netbeans.modules.java.hints.bugs.CollectionRemove.WARN_FOR_CASTABLE_KEY", defaultValue=WARN_FOR_CASTABLE_DEFAULT)
    static final String  WARN_FOR_CASTABLE_KEY = "warn-for-castable";
  
    @TriggerPattern(value="$col.remove($obj)",
                    constraints={@ConstraintVariableType(variable="$col",
                                             type="java.util.Collection"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> collectionRemove(HintContext ctx) {
        return run(ctx, "java.util.Collection.remove(java.lang.Object)", "java.util.Collection.add(java.lang.Object)", 0, 0);
    }

    @TriggerPattern(value="$col.contains($obj)",
                    constraints={@ConstraintVariableType(variable="$col",
                                             type="java.util.Collection"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> collectionContains(HintContext ctx) {
        return run(ctx, "java.util.Collection.contains(java.lang.Object)", "java.util.Collection.add(java.lang.Object)", 0, 0);
    }

    @TriggerPattern(value="$map.remove($obj)",
                    constraints={@ConstraintVariableType(variable="$map",
                                             type="java.util.Map"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> mapRemove(HintContext ctx) {
        return run(ctx, "java.util.Map.remove(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0);
    }

    @TriggerPattern(value="$map.get($obj)",
                    constraints={@ConstraintVariableType(variable="$map",
                                             type="java.util.Map"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> mapGet(HintContext ctx) {
        return run(ctx, "java.util.Map.get(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0);
    }

    @TriggerPattern(value="$map.containsKey($obj)",
                    constraints={@ConstraintVariableType(variable="$map",
                                             type="java.util.Map"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> mapContainsKey(HintContext ctx) {
        return run(ctx, "java.util.Map.containsKey(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 0);
    }

    @TriggerPattern(value="$map.containsValue($obj)",
                    constraints={@ConstraintVariableType(variable="$map",
                                             type="java.util.Map"),
                                 @ConstraintVariableType(variable="$obj",
                                             type="java.lang.Object")
    })
    public static List<ErrorDescription> mapContainsValue(HintContext ctx) {
        return run(ctx, "java.util.Map.containsValue(java.lang.Object)", "java.util.Map.put(java.lang.Object, java.lang.Object)", 0, 1);
    }
    
    private static List<ErrorDescription> run(HintContext ctx, String checkMethod, String checkAgainst, int... parameterMapping) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        ExpressionTree select = mit.getMethodSelect();
        TreePath method = new TreePath(ctx.getPath(), select);
        Element el = ctx.getInfo().getTrees().getElement(method);

        if (el == null || el.getKind() != ElementKind.METHOD)
            return null;

        ExecutableElement invoked = (ExecutableElement) el;
        TypeElement owner = (TypeElement) invoked.getEnclosingElement();
        List<ErrorDescription> result = new LinkedList<ErrorDescription>();
        ExecutableElement toCheckAgainst = resolveMethod(ctx.getInfo(), checkAgainst);

        if (toCheckAgainst == null) {
            return null; //XXX: log?
        }

        DeclaredType site;

        OUTER: switch (select.getKind()) {
            case MEMBER_SELECT:
                TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(method, ((MemberSelectTree) select).getExpression()));
                if (tm != null && tm.getKind() == TypeKind.TYPEVAR)
                    tm = ((TypeVariable) tm).getUpperBound();
                if (tm != null && tm.getKind() == TypeKind.DECLARED)
                    site = (DeclaredType) tm;
                else
                    site = null;
                break;
            case IDENTIFIER:
                Scope s = ctx.getInfo().getTrees().getScope(ctx.getPath());

                while (s != null) {
                    if (s.getEnclosingClass() != null) {
                        for (ExecutableElement ee : ElementFilter.methodsIn(ctx.getInfo().getElements().getAllMembers(s.getEnclosingClass()))) {
                            if (ee == toCheckAgainst || ctx.getInfo().getElements().overrides(ee, toCheckAgainst, owner)) {
                                site = (DeclaredType) s.getEnclosingClass().asType();
                                break OUTER;
                            }
                        }
                    }
                    s = s.getEnclosingScope();
                }

                site = null;
                break;
            default:
                throw new IllegalStateException();
        }

        if (site == null) {
            return null; //XXX: log
        }

        ExecutableType againstType = (ExecutableType) ctx.getInfo().getTypes().asMemberOf(site, toCheckAgainst);

        assert parameterMapping.length % 2 == 0;

        for (int cntr = 0; cntr < parameterMapping.length; cntr += 2) {
            TypeMirror actualParam = ctx.getInfo().getTrees().getTypeMirror(new TreePath(ctx.getPath(), mit.getArguments().get(parameterMapping[cntr + 0])));
            TypeMirror designedType = org.netbeans.modules.java.hints.errors.Utilities.resolveCapturedType(ctx.getInfo(), againstType.getParameterTypes().get(parameterMapping[cntr + 1]));

            if (designedType.getKind() == TypeKind.WILDCARD) {
                designedType = ((WildcardType) designedType).getExtendsBound();

                if (designedType == null) {
                    TypeElement te = ctx.getInfo().getElements().getTypeElement("java.lang.Object");
                    if (te != null) {
                        designedType = te.asType();
                    }
                }
            }

            if (designedType != null && !ctx.getInfo().getTypes().isAssignable(actualParam,designedType)) {
                String warningKey;

                if (compatibleTypes(ctx.getInfo(), actualParam,designedType)) {
                    warningKey = "HINT_SuspiciousCall"; //NOI18N

                    if (!ctx.getPreferences().getBoolean(WARN_FOR_CASTABLE_KEY, WARN_FOR_CASTABLE_DEFAULT)) {
                        continue;
                    }
                } else {
                    warningKey = "HINT_SuspiciousCallIncompatibleTypes"; //NOI18N
                }

                ExecutableElement checkMethodElement = resolveMethod(ctx.getInfo(), checkMethod);
                ExecutableElement enclosingMethod = findEnclosingMethod(ctx.getInfo(), ctx.getPath());

                if (   enclosingMethod != null
                    && checkMethodElement != null
                    && ctx.getInfo().getElements().overrides(enclosingMethod, checkMethodElement, (TypeElement) checkMethodElement.getEnclosingElement())) {
                    continue;
                }
                
                String semiFQN = checkMethod.substring(0, checkMethod.indexOf('('));

                String warning = NbBundle.getMessage(CollectionRemove.class,
                                                     warningKey,
                                                     semiFQN,
                                                     Utilities.getTypeName(ctx.getInfo(), actualParam, false),
                                                     Utilities.getTypeName(ctx.getInfo(), designedType, false));

                result.add(ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), warning));
            }
        }

        return result;
    }

    private static boolean compatibleTypes(CompilationInfo info, TypeMirror type1, TypeMirror type2) {
        type1 = info.getTypes().erasure(type1);
        type2 = info.getTypes().erasure(type2);

        return info.getTypeUtilities().isCastable(type1, type2);
    }

    private static ExecutableElement resolveMethod(CompilationInfo info, String name) {
        return (ExecutableElement) info.getElementUtilities().findElement(name);
    }

    private static ExecutableElement findEnclosingMethod(CompilationInfo info, TreePath path) {
        while (path != null && path.getLeaf().getKind() != Kind.CLASS && path.getLeaf().getKind() != Kind.METHOD) {
            path = path.getParentPath();
        }

        if (path != null && path.getLeaf().getKind() == Kind.METHOD) {
            Element el = info.getTrees().getElement(path);

            if (el != null && el.getKind() == ElementKind.METHOD) {
                return (ExecutableElement) el;
            }
        }

        return null;
    }

}
