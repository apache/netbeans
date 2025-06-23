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

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.CreateElementUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.UseOptions;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;
import static com.sun.source.tree.Tree.Kind.NEW_CLASS;
import static javax.lang.model.type.TypeKind.EXECUTABLE;

/**
 *
 * @author lahvac
 */
public class Tiny {

    static final boolean SC_IGNORE_SUBSTRING_DEFAULT = true;
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.perf.Tiny.SC_IGNORE_SUBSTRING", tooltip = "#TP_org.netbeans.modules.java.hints.perf.Tiny.SC_IGNORE_SUBSTRING", defaultValue=SC_IGNORE_SUBSTRING_DEFAULT)
    static final String SC_IGNORE_SUBSTRING = "ignore.substring";
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.stringConstructor", category="performance", suppressWarnings="RedundantStringConstructorCall")
    @UseOptions(SC_IGNORE_SUBSTRING)
    @TriggerPattern(value="new java.lang.String($original)",
                    constraints=@ConstraintVariableType(variable="$original", type="java.lang.String"))
    public static ErrorDescription stringConstructor(HintContext ctx) {
        TreePath original = ctx.getVariables().get("$original");

        if (ctx.getPreferences().getBoolean(SC_IGNORE_SUBSTRING, SC_IGNORE_SUBSTRING_DEFAULT)) {
            if (   MatcherUtilities.matches(ctx, original, "$str1.substring($s)", true)
                || MatcherUtilities.matches(ctx, original, "$str2.substring($s, $e)", true)) {
                TreePath str = ctx.getVariables().get("$str1") != null ? ctx.getVariables().get("$str1") : ctx.getVariables().get("$str2");

                assert str != null;

                TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(str);

                if (type != null && type.getKind() == TypeKind.DECLARED) {
                    TypeElement te = (TypeElement) ((DeclaredType) type).asElement();

                    if (te.getQualifiedName().contentEquals("java.lang.String")) {
                        return null;
                    }
                }
            }
        }

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringConstructor");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$original");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringConstructor");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }


    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.stringEqualsEmpty", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.stringEqualsEmpty", category="performance", enabled=false, suppressWarnings={"StringEqualsEmpty", "", "StringEqualsEmptyString"})
    @TriggerPatterns({
        @TriggerPattern(value="$string.equals(\"\")",
                        constraints=@ConstraintVariableType(variable="$string", type="java.lang.String")),
        
        @TriggerPattern(value="$string.equalsIgnoreCase(\"\")",
                        constraints=@ConstraintVariableType(variable="$string", type="java.lang.String"))
    })
    public static ErrorDescription stringEqualsEmpty(HintContext ctx) {
        Fix f;
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_6) >= 0) {
            String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringEqualsEmpty16");
            f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$string.isEmpty()");
        } else {
            boolean not = ctx.getPath().getParentPath().getLeaf().getKind() == Kind.LOGICAL_COMPLEMENT;
            String fixDisplayName = NbBundle.getMessage(Tiny.class, not ? "FIX_StringEqualsEmptyNeg" : "FIX_StringEqualsEmpty");
            f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, not ? ctx.getPath().getParentPath() : ctx.getPath(), not ? "$string.length() != 0" : "$string.length() == 0");
        }
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringEqualsEmpty");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }


    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.lengthOneStringIndexOf", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.lengthOneStringIndexOf", category="performance", enabled=false, suppressWarnings="SingleCharacterStringConcatenation")
    @TriggerPatterns({
        @TriggerPattern(value="$string.indexOf($toSearch)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String")}),
        @TriggerPattern(value="$string.lastIndexOf($toSearch)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String")}),
        @TriggerPattern(value="$string.indexOf($toSearch, $index)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$index", type="int")}),
        @TriggerPattern(value="$string.lastIndexOf($toSearch, $index)",
                        constraints={@ConstraintVariableType(variable="$string", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$toSearch", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$index", type="int")})
    })
    public static ErrorDescription lengthOneStringIndexOf(HintContext ctx) {
        TreePath toSearch = ctx.getVariables().get("$toSearch");

        if (toSearch.getLeaf().getKind() != Kind.STRING_LITERAL) {
            return null;
        }

        LiteralTree lt = (LiteralTree) toSearch.getLeaf();
        final String data = (String) lt.getValue();

        if (data.length() != 1) {
            return null;
        }

        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), toSearch.getLeaf());
        int end   = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), toSearch.getLeaf());
        final String literal = ctx.getInfo().getText().substring(start, end);

        Fix f = new JavaFix(ctx.getInfo(), toSearch) {
            @Override
            protected String getText() {
                return NbBundle.getMessage(Tiny.class, "FIX_LengthOneStringIndexOf");
            }

            @Override
            protected void performRewrite(TransformationContext ctx) {
                WorkingCopy wc = ctx.getWorkingCopy();
                TreePath tp = ctx.getPath();
                String content;

                if ("'".equals(data)) {
                    content = "\\'";
                } else if ("\"".equals(data)) {
                    content = "\"";
                } else {
                    content = literal;
                    if (content.length() > 0 && content.charAt(0) == '"') {
                        content = content.substring(1);
                    }
                    if (content.length() > 0 && content.charAt(content.length() - 1) == '"') {
                        content = content.substring(0, content.length() - 1);
                    }
                }

                wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Identifier("'" + content + "'"));
            }
        }.toEditorFix();
        
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_LengthOneStringIndexOf", literal);
        
        return ErrorDescriptionFactory.forTree(ctx, toSearch, displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.getClassInsteadOfDotClass", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.getClassInsteadOfDotClass", category="performance", enabled=false, suppressWarnings="InstantiatingObjectToGetClassObject")
    @TriggerPattern(value="new $O($params$).getClass()")
    public static ErrorDescription getClassInsteadOfDotClass(HintContext ctx) {
        TreePath O = ctx.getVariables().get("$O");
        if (O.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE) {
            O = new TreePath(O, ((ParameterizedTypeTree) O.getLeaf()).getType());
        }
        ctx.getVariables().put("$OO", O);//XXX: hack
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_GetClassInsteadOfDotClass");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$OO.class");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_GetClassInsteadOfDotClass");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    private static final Set<Kind> KEEP_PARENTHESIS = EnumSet.of(Kind.MEMBER_SELECT);
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.constantIntern", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.constantIntern", category="performance", enabled=false, suppressWarnings="ConstantStringIntern")
    @TriggerPattern(value="$str.intern()",
                    constraints=@ConstraintVariableType(variable="$str", type="java.lang.String"))
    public static ErrorDescription constantIntern(HintContext ctx) {
        TreePath str = ctx.getVariables().get("$str");
        TreePath constant;
        if (str.getLeaf().getKind() == Kind.PARENTHESIZED) {
            constant = new TreePath(str, ((ParenthesizedTree) str.getLeaf()).getExpression());
        } else {
            constant = str;
        }
        if (!Utilities.isConstantString(ctx.getInfo(), constant))
            return null;
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_ConstantIntern");
        String target;
        if (constant != str && KEEP_PARENTHESIS.contains(ctx.getPath().getParentPath().getLeaf().getKind())) {
            target = "$str";
        } else {
            target = "$constant";
            ctx.getVariables().put("$constant", constant);//XXX: hack
        }
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), target);
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ConstantIntern");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.enumSet", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.enumSet", category="performance", suppressWarnings="SetReplaceableByEnumSet", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern("new $coll<$param>($params$)")
    })
    public static ErrorDescription enumSet(HintContext ctx) {
        return enumHint(ctx, "java.util.Set", null, "ERR_Tiny_enumSet");
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.enumMap", description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.enumMap", category="performance", suppressWarnings="MapReplaceableByEnumMap")
    @TriggerPatterns({
        @TriggerPattern("new $coll<$param, $to>($params$)")
    })
    public static ErrorDescription enumMap(HintContext ctx) {
        Fix[] fixes;
        Collection<? extends TreePath> mvars = ctx.getMultiVariables().get("$params$");

        if (mvars != null && mvars.isEmpty()) {
            String displayName = NbBundle.getMessage(Tiny.class, "FIX_Tiny_enumMap");

            fixes = new Fix[] {
                JavaFixUtilities.rewriteFix(ctx, displayName, ctx.getPath(), "new java.util.EnumMap<$param, $to>($param.class)")
            };
        } else {
            fixes = new Fix[0];
        }

        return enumHint(ctx, "java.util.Map", "java.util.EnumMap", "ERR_Tiny_enumMap", fixes);
    }

    private static ErrorDescription enumHint(HintContext ctx, String baseName, String targetTypeName, String key, Fix... fixes) {
        Element type = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$param"));

        if (type == null || type.getKind() != ElementKind.ENUM) {
            return null;
        }

        Element coll = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$coll"));

        if (coll == null || coll.getKind() != ElementKind.CLASS) {
            return null;
        }
        
        TypeElement base = ctx.getInfo().getElements().getTypeElement(baseName);
        
        if (base == null) {
            return null;
        }

        Types t = ctx.getInfo().getTypes();

        if (!t.isSubtype(t.erasure(coll.asType()), t.erasure(base.asType()))) {
            return null;
        }

        if (targetTypeName != null) {
            TypeElement target = ctx.getInfo().getElements().getTypeElement(targetTypeName);

            if (target == null) {
                return null;
            }

            if (t.isSubtype(t.erasure(coll.asType()), t.erasure(target.asType()))) {
                return null;
            }
            
            List<? extends TypeMirror> assignedTo = CreateElementUtilities.resolveType(EnumSet.noneOf(ElementKind.class), ctx.getInfo(), ctx.getPath().getParentPath(), ctx.getPath().getLeaf(), (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getPath().getCompilationUnit(), ctx.getPath().getLeaf()), new TypeMirror[1], new int[1]);
            
            if (assignedTo != null && assignedTo.size() == 1) {
                if (t.isSubtype(t.erasure(assignedTo.get(0)), t.erasure(coll.asType())))
                    return null;
            }
        }

        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fixes);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.Tiny.collectionsToArray",
          description = "#DESC_org.netbeans.modules.java.hints.perf.Tiny.collectionsToArray",
          category="performance",
          enabled=true,
          suppressWarnings="CollectionsToArray")
    @TriggerPatterns({
        @TriggerPattern(value = "$collection.toArray(new $clazz[$collection.size()])",
                        constraints = @ConstraintVariableType(variable="$collection", type="java.util.Collection")),
        @TriggerPattern(value = "$collection.toArray(new $clazz[0])",
                        constraints = @ConstraintVariableType(variable="$collection", type="java.util.Collection")),
        @TriggerPattern(value = "$collection.toArray(new $clazz[]{})",
                        constraints = @ConstraintVariableType(variable="$collection", type="java.util.Collection"))
    })
    public static ErrorDescription collectionsToArray(HintContext ctx) {

        boolean pureMemberSelect = true;
        TreePath tp = ctx.getVariables().get("$collection");
        if (tp == null) return null;

        Tree msTest = tp.getLeaf();
        OUTER: while (true) {
            switch (msTest.getKind()) {
                case IDENTIFIER: break OUTER;
                case MEMBER_SELECT: msTest = ((MemberSelectTree) msTest).getExpression(); break;
                default:
                    pureMemberSelect = false;
                    break OUTER;
            }
        }

        Fix[] fixes;

        if (pureMemberSelect) {

            SourceVersion version = ctx.getInfo().getSourceVersion();
            TreePath type = ctx.getVariables().get("$clazz");
            String typeName = type.getLeaf().toString();
            
            if (version.compareTo(SourceVersion.RELEASE_11) >= 0) {
                String byRef = NbBundle.getMessage(Tiny.class, "FIX_Tiny_collectionsToArrayByMethodRef", typeName);
                fixes = new Fix[] {
                    JavaFixUtilities.rewriteFix(ctx, byRef, ctx.getPath(), "$collection.toArray($clazz[]::new)"),
                };
            } else if (isNewArrayWithSize(type)) {
                String byZero = NbBundle.getMessage(Tiny.class, "FIX_Tiny_collectionsToArrayByZeroArray", typeName);
                fixes = new Fix[] {
                    JavaFixUtilities.rewriteFix(ctx, byZero, ctx.getPath(), "$collection.toArray(new $clazz[0])")
                };
            } else {
                return null; // new T[0] or new T[]{} and version < 11 -> nothing to do
            }
        } else {
            fixes = new Fix[0];
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_Tiny_collectionsToArray");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fixes);
    }

    private static boolean isNewArrayWithSize(TreePath type) {
        Tree parent = type.getParentPath().getLeaf();
        if (parent instanceof NewArrayTree newArrayTree) {
            List<? extends ExpressionTree> dim = newArrayTree.getDimensions();
            return dim.isEmpty() ? false : dim.get(0).getKind() == Kind.METHOD_INVOCATION; // size()
        }
        return false;
    }
    
    @NbBundle.Messages({
        "TEXT_RedundantToString=Redundant String.toString()",
        "FIX_RedundantToString=Remove .toString()"
    })
    @TriggerPattern(value = "$v.toString()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String"))
    @Hint(
        displayName = "#DN_RedundantToString",
        description = "#DESC_RedundantToString",
        enabled = true,
        category = "performance",
        suppressWarnings = "RedundantStringToString"
    )
    public static ErrorDescription redundantToString(HintContext ctx) {
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), 
                Bundle.TEXT_RedundantToString(), 
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_RedundantToString(), ctx.getPath(), "$v"));
    }
    
    private static final Map<TypeKind, String[]> PARSE_METHODS = new EnumMap<>(TypeKind.class);
    static {
        PARSE_METHODS.put(TypeKind.BOOLEAN, new String[] { "Boolean", "parseBoolean" }); // NOI18N
        PARSE_METHODS.put(TypeKind.BYTE, new String[] { "Byte", "parseByte"}); // NOI18N
        PARSE_METHODS.put(TypeKind.DOUBLE, new String[] { "Double", "parseDouble"}); // NOI18N
        PARSE_METHODS.put(TypeKind.FLOAT, new String[] { "Float", "parseFloat"}); // NOI18N
        PARSE_METHODS.put(TypeKind.INT, new String[] { "Integer", "parseInt"}); // NOI18N
        PARSE_METHODS.put(TypeKind.LONG, new String[] { "Long", "parseLong"}); // NOI18N
        PARSE_METHODS.put(TypeKind.SHORT, new String[] { "Short", "parseShort"}); // NOI18N
    }
    
    @TriggerPatterns({
        @TriggerPattern(value = "java.lang.Integer.parseInt($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Byte.parseByte($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Short.parseShort($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Long.parseLong($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Float.parseFloat($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Double.parseDouble($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Boolean.parseBoolean($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),

        @TriggerPattern(value = "java.lang.Integer.parseInt($s, $r)", constraints = {@ConstraintVariableType(variable = "$s", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Byte.parseByte($s, $r)", constraints = {@ConstraintVariableType(variable = "$s", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Short.parseShort($s, $r)", constraints = {@ConstraintVariableType(variable = "$s", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Long.parseLong($s, $r)", constraints = {@ConstraintVariableType(variable = "$s", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),


        @TriggerPattern(value = "java.lang.Byte.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Double.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Float.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Long.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Short.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Boolean.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),

        @TriggerPattern(value = "java.lang.Byte.valueOf($v, $r)", constraints = {@ConstraintVariableType(variable = "$v", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v, $r)", constraints = {@ConstraintVariableType(variable = "$v", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Long.valueOf($v, $r)", constraints = {@ConstraintVariableType(variable = "$v", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
        @TriggerPattern(value = "java.lang.Short.valueOf($v, $r)", constraints = {@ConstraintVariableType(variable = "$v", type = "java.lang.String"), @ConstraintVariableType(variable = "$r", type = "int")}),
    })
    @NbBundle.Messages({
        "TEXT_UnnecessaryTempFromString=Unnecessary temporary when converting from String",
        "# {0} - wrapper type simple name",
        "# {1} - parse method name",
        "FIX_UnnecessaryTempFromString1=Replace with {0}.{1}()",
    })
    @Hint(
        displayName = "#DN_UnnecessaryTempFromString",
        description = "#DESC_UnnecessaryTempFromString",
        enabled = true,
        category = "performance",
        suppressWarnings = "UnnecessaryTemporaryOnConversionFromString" 
    )
    public static ErrorDescription unnecessaryTempFromString(HintContext ctx) {

        String type;
        String method;

        // determine if the destination is primitive
        TypeMirror destType = getDestinationType(ctx, ctx.getPath());
        TypeMirror srcType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());

        if (srcType == null || destType == null) {
            return null;
        } else if (destType.getKind().isPrimitive() && !srcType.getKind().isPrimitive()) {
            srcType = ctx.getInfo().getTypes().unboxedType(srcType);
            String[] replacement = PARSE_METHODS.get(srcType.getKind());
            type = replacement[0];
            method = replacement[1];
        } else if (!destType.getKind().isPrimitive() && srcType.getKind().isPrimitive()) {
            type = PARSE_METHODS.get(srcType.getKind())[0];
            method = "valueOf";  // NOI18N
        } else {
            return null;  // nothing to do, a different rule handles .intValue() boxing problems
        }

        if (srcType.getKind() == TypeKind.BOOLEAN && ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_5) < 0) {
            return null;  // JDK < 5 has no primitive-valued pasre* method for booleans.
        }

        Fix fix = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_UnnecessaryTempFromString1(type, method), ctx.getPath(), type + "." + method + "($v)"); // NOI18N
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_UnnecessaryTempFromString(), fix); // NOI18N
    }

    private static TypeMirror getDestinationType(HintContext ctx, TreePath path) {

        TreePath parent = path.getParentPath();
        Tree parentLeaf = parent.getLeaf();
        Tree leaf = path.getLeaf();

        Trees trees = ctx.getInfo().getTrees();

        if (parentLeaf.getKind() == METHOD_INVOCATION) {

            MethodInvocationTree met = (MethodInvocationTree) parentLeaf;
            int index = met.getArguments().indexOf(leaf);
            return paramTypeOfExecutable(trees.getTypeMirror(new TreePath(path, met.getMethodSelect())), index);
        } else if (parentLeaf.getKind() == NEW_CLASS) {

            NewClassTree nct = (NewClassTree) parentLeaf;
            int index = nct.getArguments().indexOf(leaf);
            return paramTypeOfExecutable(trees.getElement(new TreePath(path, nct)).asType(), index);
        } else {

            int pos = (int) trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), leaf);
            List<? extends TypeMirror> type = CreateElementUtilities.resolveType(
                    EnumSet.noneOf(ElementKind.class), ctx.getInfo(), parent, leaf, pos, new TypeMirror[1], new int[1]);

            if ((type != null) && !type.isEmpty()) {
                return type.get(0);
            }
        }

        return null;
    }

    private static TypeMirror paramTypeOfExecutable(TypeMirror executable, int index) {
        if (index != -1 && executable != null && executable.getKind() == EXECUTABLE) {
            List<? extends TypeMirror> paramTypes = ((ExecutableType) executable).getParameterTypes();
            if (paramTypes.size() > index) {
                return paramTypes.get(index);
            }
        }
        return null;
    }
    
    @TriggerPatterns({
        @TriggerPattern(value = "java.lang.Byte.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "java.lang.Double.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "double")),
        @TriggerPattern(value = "java.lang.Float.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "float")),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "java.lang.Long.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "long")),
        @TriggerPattern(value = "java.lang.Short.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "java.lang.Boolean.valueOf($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "boolean")),
    })
    @NbBundle.Messages({
        "TEXT_UnnecessaryTempToSring=Unnecessary temporary when converting to String",
        "# {0} - wrapper type simple name",
        "FIX_UnnecessaryTempToString=Replace with {0}.toString()",
    })
    @Hint(
        displayName = "#DN_UnnecessaryTempToString",
        description = "#DESC_UnnecessaryTempToString",
        enabled = true,
        category = "performance",
        suppressWarnings = "UnnecessaryTemporaryOnConversionToString" 
    )
    public static ErrorDescription unnecessaryTypeToString(HintContext ctx) {
        TreePath vPath = ctx.getVariables().get("$v"); // NOI18N
        TypeMirror resType = ctx.getInfo().getTrees().getTypeMirror(vPath);
        if (resType == null) {
            return null;
        }
        String[] arr = PARSE_METHODS.get(resType.getKind());
        if (arr == null) {
            return null; // just in case
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_UnnecessaryTempFromString(),
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_UnnecessaryTempToString(arr[0]), ctx.getPath(), arr[0] + ".toString($v)")); // NOI18N
    }

    // TODO move to jdk package?
    @TriggerPatterns({
        @TriggerPattern(value = "new java.lang.Byte($v)"),
        @TriggerPattern(value = "new java.lang.Double($v)"),
        @TriggerPattern(value = "new java.lang.Float($v)"),
        @TriggerPattern(value = "new java.lang.Integer($v)"),
        @TriggerPattern(value = "new java.lang.Long($v)"),
        @TriggerPattern(value = "new java.lang.Short($v)"),
        @TriggerPattern(value = "new java.lang.Boolean($v)"),
    })
    @NbBundle.Messages({
        "TEXT_BoxedPrimitiveConstruction=Replace usage of deprecated boxed primitive constructors with factory methods.",
        "# {0} - wrapper type simple name",
        "FIX_BoxedPrimitiveConstruction=Replace with {0}.valueOf()",
    })
    @Hint(
        displayName = "#DN_BoxedPrimitiveConstruction",
        description = "#DESC_BoxedPrimitiveConstruction",
        enabled = true,
        category = "rules15",
        suppressWarnings = "BoxedPrimitiveConstruction"
    )
    public static ErrorDescription boxedPrimitiveConstruction(HintContext ctx) {
        TypeMirror resType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());
        if (resType == null) {
            return null;
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_BoxedPrimitiveConstruction(),
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_BoxedPrimitiveConstruction(resType), ctx.getPath(), resType + ".valueOf($v)")); // NOI18N
    }
}
