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

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreePathHandle;
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
@Override protected String getText() {
return NbBundle.getMessage(Tiny.class, "FIX_LengthOneStringIndexOf");
}
@Override protected void performRewrite(TransformationContext ctx) {
WorkingCopy wc = ctx.getWorkingCopy();
TreePath tp = ctx.getPath();
String content;

if ("'".equals(data)) content = "\\'";
else if ("\"".equals(data)) content = "\"";
else {
content = literal;
if (content.length() > 0 && content.charAt(0) == '"') content = content.substring(1);
if (content.length() > 0 && content.charAt(content.length() - 1) == '"') content = content.substring(0, content.length() - 1);
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
          enabled=false,
          suppressWarnings="CollectionsToArray")
    @TriggerPattern(value = "$collection.toArray(new $clazz[0])",
                    constraints=@ConstraintVariableType(variable="$collection",
                                                        type="java.util.Collection"))
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
            String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_Tiny_collectionsToArray");

            fixes = new Fix[] {
                JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$collection.toArray(new $clazz[$collection.size()])")
            };
        } else {
            fixes = new Fix[0];
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_Tiny_collectionsToArray");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fixes);
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
    
    private static final Map<TypeKind, String[]> PARSE_METHODS = new HashMap<TypeKind, String[]>(7);
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
        @TriggerPattern(value = "new java.lang.Byte($v).byteValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Double($v).doubleValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Float($v).floatValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Integer($v).intValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Long($v).longValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Short($v).shortValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "new java.lang.Boolean($v).booleanValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        
        
        @TriggerPattern(value = "java.lang.Byte.valueOf($v).byteValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Double.valueOf($v).doubleValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Float.valueOf($v).floatValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v).intValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Long.valueOf($v).longValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Short.valueOf($v).shortValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
        @TriggerPattern(value = "java.lang.Boolean.valueOf($v).booleanValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.String")),
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
        TypeMirror resType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());
        if (resType == null) {
            return null;
        }
        if (resType.getKind() == TypeKind.BOOLEAN) {
            if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_5) < 0) {
                // might alter new Boolean($v) to Boolean.valueOf($v), but that's all we can do. JDK < 5 has no 
                // primitive-valued pasre* method for booleans.
                return null;
            }
        }
        String[] arr = PARSE_METHODS.get(resType.getKind());
        if (arr == null) {
            return null; // just in case
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_UnnecessaryTempFromString(),
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_UnnecessaryTempFromString1(arr[0], arr[1]), ctx.getPath(),
                arr[0] + "." + arr[1] + "($v)")); // NOI18N
    }
    
    @TriggerPatterns({
        @TriggerPattern(value = "new java.lang.Byte($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "new java.lang.Double($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "double")),
        @TriggerPattern(value = "new java.lang.Float($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "float")),
        @TriggerPattern(value = "new java.lang.Integer($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "new java.lang.Long($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "long")),
        @TriggerPattern(value = "new java.lang.Short($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "int")),
        @TriggerPattern(value = "new java.lang.Boolean($v).toString()", constraints = @ConstraintVariableType(variable = "$v", type = "boolean")),
        
        
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
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_UnnecessaryTempToString(arr[0]), ctx.getPath(),
                arr[0] + ".toString($v)")); // NOI18N
    }
}