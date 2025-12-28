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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_indentation=Confusing indentation",
    "DESC_indentation=Warns about indentation that suggests possible missing surrounding block",
    "ERR_indentation=Confusing indentation",
    "TEXT_MissingSwitchCase=Possibly missing switch `case' statement",
    "# {0} - constant identifier",
    "FIX_AddMissingSwitchCase=Replace label with switch case {0}",
})
public class Tiny {

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.singleCharRegex", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.singleCharRegex", category="bugs", suppressWarnings="SingleCharRegex")
    @TriggerPatterns({
        @TriggerPattern(value="$str.replaceAll($pattern, $to)",
                constraints = {
                    @ConstraintVariableType(variable="$str", type="java.lang.String"),
                    @ConstraintVariableType(variable="$pattern", type="java.lang.String") }),
        @TriggerPattern(value = "$str.replaceFirst($pattern, $repl)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$repl", type = "java.lang.String")
                }),
        @TriggerPattern(value="$str.split($pattern)",
                constraints = {
                    @ConstraintVariableType(variable="$str", type="java.lang.String"),
                    @ConstraintVariableType(variable="$pattern", type="java.lang.String") }),
        @TriggerPattern(value = "$str.split($pattern, $limit)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$limit", type = "int")
                })
    })
    public static ErrorDescription singleCharRegex(HintContext ctx) {
        Tree constant = ((MethodInvocationTree) ctx.getPath().getLeaf()).getArguments().get(0);
        TreePath constantTP = new TreePath(ctx.getPath(), constant);

        if (constantTP.getLeaf().getKind() == Kind.STRING_LITERAL) {

            String value = (String) ((LiteralTree) constantTP.getLeaf()).getValue();

            if (value != null && value.length() == 1 && isRegExControlCharacter(value.charAt(0))) {

                String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_single-char-regex");
                String displayName = NbBundle.getMessage(Tiny.class, "ERR_single-char-regex");

                Fix fix = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, constantTP, "\"\\\\"+value.charAt(0)+"\"");
                return ErrorDescriptionFactory.forTree(ctx, constant, displayName, fix);
            }
        }

        return null;
    }

    private static boolean isRegExControlCharacter(char c) {
        return c == '.' || c == '$' || c == '|' || c == '^' || c == '?' || c == '*' || c == '+' || c == '\\'
                        || c == '(' || c == ')' || c == '[' || c == ']'  || c == '{' || c == '}';
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.newObject", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.newObject", category="bugs", suppressWarnings="ResultOfObjectAllocationIgnored", options=Options.QUERY)
    //TODO: anonymous innerclasses?
    @TriggerPatterns({
        @TriggerPattern(value="new $type($params$);"),
        @TriggerPattern(value="$enh.new $type($params$);")
    })
    public static ErrorDescription newObject(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_newObject");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.systemArrayCopy", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.systemArrayCopy", category="bugs", suppressWarnings="SuspiciousSystemArraycopy", options=Options.QUERY)
    @TriggerPattern(value="java.lang.System.arraycopy($src, $srcPos, $dest, $destPos, $length)")
    public static List<ErrorDescription> systemArrayCopy(HintContext ctx) {
        List<ErrorDescription> result = new LinkedList<ErrorDescription>();

        for (String objName : Arrays.asList("$src", "$dest")) {
            TreePath obj = ctx.getVariables().get(objName);
            TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(obj);

            if (Utilities.isValidType(type) && type.getKind() != TypeKind.ARRAY) {
                String treeDisplayName = Utilities.shortDisplayName(ctx.getInfo(), (ExpressionTree) obj.getLeaf());
                String displayName = NbBundle.getMessage(Tiny.class, "ERR_system_arraycopy_notarray", treeDisplayName);
                
                result.add(ErrorDescriptionFactory.forTree(ctx, obj, displayName));
            }
        }

        for (String countName : Arrays.asList("$srcPos", "$destPos", "$length")) {
            TreePath count = ctx.getVariables().get(countName);
            Number value = ArithmeticUtilities.compute(ctx.getInfo(), count, true);

            if (value != null && value.intValue() < 0) {
                String treeDisplayName = Utilities.shortDisplayName(ctx.getInfo(), (ExpressionTree) count.getLeaf());
                String displayName = NbBundle.getMessage(Tiny.class, "ERR_system_arraycopy_negative", treeDisplayName);

                result.add(ErrorDescriptionFactory.forTree(ctx, count, displayName));
            }
        }

        return result;
    }


    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.equalsNull", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.equalsNull", category="bugs", suppressWarnings="ObjectEqualsNull")
    @TriggerPattern(value="$obj.equals(null)")
    public static ErrorDescription equalsNull(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_equalsNull");
        Fix fix = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$obj == null");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_equalsNull");

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, fix);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.varTypeDiamondOperator", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.varTypeDiamondOperator", category="bugs", suppressWarnings="AllowVarTypeDiamondOperator")
    @TriggerPatterns({
        @TriggerPattern(value="$mods$ $varType $name = new $type<>($args$)", constraints=@ConstraintVariableType(variable="$type", type="java.util.Collection")),
        @TriggerPattern(value="$mods$ $varType $name = new $type<>($args$)", constraints=@ConstraintVariableType(variable="$type", type="java.util.Map"))
    })
    public static ErrorDescription varTypeDiamondOperator(HintContext ctx) {
        TreePath path = ctx.getPath();
        Boolean isVarUsed = ctx.getInfo().getTreeUtilities().isVarType(path);
        if(!isVarUsed){
            return null;
        }

        VariableTree vt = (VariableTree) ctx.getPath().getLeaf();
        NewClassTree nct = (NewClassTree) vt.getInitializer();
        Element constructorCand = ctx.getInfo().getTrees().getElement(new TreePath(ctx.getPath(), nct));

        if (constructorCand.getKind() != ElementKind.CONSTRUCTOR) {
            return null;
        }

        ExecutableElement constructor = (ExecutableElement) constructorCand;

        for (VariableElement param : constructor.getParameters()) {
            if (param.asType().getKind() == TypeKind.DECLARED) {
                DeclaredType dt = (DeclaredType) param.asType();
                if (!dt.getTypeArguments().isEmpty()) {
                    return null;
                }
            }
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_varTypeDiamondOperator");

        return ErrorDescriptionFactory.forTree(ctx, path, displayName);
    }
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Tiny.resultSet", description = "#DESC_org.netbeans.modules.java.hints.bugs.Tiny.resultSet", category="bugs", suppressWarnings="UseOfIndexZeroInJDBCResultSet", options=Options.QUERY)
    @TriggerPattern(value="$set.$method($columnIndex, $other$)",
                    constraints={
                        @ConstraintVariableType(variable="$set", type="java.sql.ResultSet"),
                        @ConstraintVariableType(variable="$columnIndex", type="int")
                    })
    public static ErrorDescription resultSet(HintContext ctx) {
        TypeElement resultSet = ctx.getInfo().getElements().getTypeElement("java.sql.ResultSet");
        String methodName = ctx.getVariableNames().get("$method");

        if (resultSet == null || !METHOD_NAME.contains(methodName)) {
            return null;
        }

        TreePath columnIndex = ctx.getVariables().get("$columnIndex");
        Number value = ArithmeticUtilities.compute(ctx.getInfo(), columnIndex, true);

        if (value == null) {
            return null;
        }

        int intValue = value.intValue();

        if (intValue > 0) {
            return null;
        }

        Element methodEl = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (methodEl == null || methodEl.getKind() != ElementKind.METHOD) {
            return null;
        }

        ExecutableElement methodElement = (ExecutableElement) methodEl;
        boolean found = false;

        for (ExecutableElement e : ElementFilter.methodsIn(resultSet.getEnclosedElements())) {
            if (e.equals(methodEl)) {
                found = true;
                break;
            }
            if (ctx.getInfo().getElements().overrides(methodElement, e, (TypeElement) methodElement.getEnclosingElement())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return null;
        }

        String key = intValue == 0 ? "ERR_ResultSetZero" : "ERR_ResultSetNegative";
        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, columnIndex, displayName);
    }
    
    private static final Set<String> METHOD_NAME = new HashSet<String>(Arrays.asList(
            "getString", "getBoolean", "getByte", "getShort", "getInt", "getLong",
            "getFloat", "getDouble", "getBigDecimal", "getBytes", "getDate",
            "getTime", "getTimestamp", "getAsciiStream", "getUnicodeStream",
            "getBinaryStream", "getObject", "getCharacterStream", "getBigDecimal",
            "updateNull", "updateBoolean", "updateByte", "updateShort", "updateInt",
            "updateLong", "updateFloat", "updateDouble", "updateBigDecimal", "updateString",
            "updateBytes", "updateDate", "updateTime", "updateTimestamp", "updateAsciiStream",
            "updateBinaryStream", "updateCharacterStream", "updateObject", "updateObject",
            "getObject", "getRef", "getBlob", "getClob", "getArray", "getDate", "getTime",
            "getTimestamp", "getURL", "updateRef", "updateBlob", "updateClob", "updateArray",
            "getRowId", "updateRowId", "updateNString", "updateNClob", "getNClob", "getSQLXML",
            "updateSQLXML", "getNString", "getNCharacterStream", "updateNCharacterStream",
            "updateAsciiStream", "updateBinaryStream", "updateCharacterStream", "updateBlob",
            "updateClob", "updateNClob", "updateNCharacterStream", "updateAsciiStream",
            "updateBinaryStream", "updateCharacterStream", "updateBlob", "updateClob",
            "updateNClob"
    ));
    
    @Hint(displayName = "#DN_indentation", description = "#DESC_indentation", category="bugs", suppressWarnings="SuspiciousIndentAfterControlStatement", options=Options.QUERY)
    @TriggerTreeKind({Kind.IF, Kind.WHILE_LOOP, Kind.FOR_LOOP, Kind.ENHANCED_FOR_LOOP})
    public static ErrorDescription indentation(HintContext ctx) {
        Tree firstStatement;
        Tree found = ctx.getPath().getLeaf();
        
        switch (found.getKind()) {
            case IF:
                IfTree it = (IfTree) found;
                if (it.getElseStatement() != null) firstStatement = it.getElseStatement();
                else firstStatement = it.getThenStatement();
                break;
            case WHILE_LOOP:
                firstStatement = ((WhileLoopTree) found).getStatement();
                break;
            case FOR_LOOP:
                firstStatement = ((ForLoopTree) found).getStatement();
                break;
            case ENHANCED_FOR_LOOP:
                firstStatement = ((EnhancedForLoopTree) found).getStatement();
                break;
            default:
                return null;
        }
        
        if (firstStatement != null && firstStatement.getKind() == Kind.BLOCK) {
            return null;
        }
        
        Tree parent = ctx.getPath().getParentPath().getLeaf();
        List<? extends Tree> parentStatements;
        
        switch (parent.getKind()) {
            case BLOCK: parentStatements = ((BlockTree) parent).getStatements(); break;
            case CASE: parentStatements = ((CaseTree) parent).getStatements(); break;
            default: return null;
        }
        
        int index = parentStatements.indexOf(found);
        
        if (index < 0 || index + 1 >= parentStatements.size()) return null;
        
        Tree secondStatement = parentStatements.get(index + 1);
        int firstIndent = indent(ctx, firstStatement);
        int secondIndent = indent(ctx, secondStatement);
        
        if (firstIndent == (-1) || secondIndent == (-1) || firstIndent != secondIndent) return null;
        
        return ErrorDescriptionFactory.forTree(ctx, secondStatement, Bundle.ERR_indentation());
    }
    
    private static int indent(HintContext ctx, Tree t) {
        long start = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), t);
        LineMap lm = ctx.getInfo().getCompilationUnit().getLineMap();
        // see defect #240493; incorrect data may be provided by Lombok processing.
        if (start == -1) {
            return -1;
        }
        long lno = lm.getLineNumber(start);
        if (lno < 1) {
            return -1;
        }
        long lineStart = lm.getStartPosition(lno);
        String text = ctx.getInfo().getText();
        CodeStyle cs = CodeStyle.getDefault(ctx.getInfo().getFileObject());
        int indent = 0;
        
        while (start-- > lineStart) {
            char c = text.charAt((int) start);
            if (c == ' ') indent++;
            else if (c == '\t') indent += cs.getTabSize();
            else return -1;
        }
        
        return indent;
    }
    
    @Hint(category = "bugs", displayName = "#DN_MissingSwitchcase", description = "#DESC_MissingSwitchcase", 
            enabled = true, severity = Severity.VERIFIER)
//    @TriggerPattern("switch ($expr) { $cases1$; case $c: $stmts1$; $l: $stmt; $stmts2$; $cases2$;")
    @TriggerPattern("case $c: $stmts1$; $l: $stmt;")
    public static ErrorDescription switchCaseLabelMismatch(HintContext ctx) {
        TreePath path = ctx.getPath();
        if (path.getLeaf().getKind() != Tree.Kind.CASE) {
            return null;
        }
        final CompilationInfo ci = ctx.getInfo();
        TreePath switchPath = path.getParentPath();
        Tree swTree = switchPath.getLeaf();
        assert swTree.getKind() == Tree.Kind.SWITCH;
        Tree xp = ((SwitchTree)swTree).getExpression();
        TypeMirror m = ci.getTrees().getTypeMirror(new TreePath(switchPath, xp));
        boolean enumType = false;
        if (m != null && m.getKind() == TypeKind.DECLARED) {
            Element e = ((DeclaredType)m).asElement();
            if (e != null && e.getKind() == ElementKind.ENUM) {
                enumType = true;
            }
        }
        // check that the label is not used within its case statement in no break / continue clause
        // the $l is bound to the label identifier, not to the labeled statement!
        TreePath stPath = ctx.getVariables().get("$stmt"); // NOI18N
        TreePath lPath = stPath.getParentPath();
        final LabeledStatementTree lt = (LabeledStatementTree)lPath.getLeaf();
        final Name l = lt.getLabel();
        final CompilationInfo info = ctx.getInfo();
        Boolean b = new ErrorAwareTreePathScanner<Boolean, Void>() {

            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                if (r1 == null) {
                    return r2;
                } else if (r2 == null) {
                    return r1;
                } else {
                    return r1 || r2;
                }
            }

            @Override
            public Boolean visitContinue(ContinueTree node, Void p) {
                Tree t = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
                return lt == t || lt.getStatement() == t;
            }

            @Override
            public Boolean visitBreak(BreakTree node, Void p) {
                Tree t = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
                return lt == t || lt.getStatement() == t;
            }
            
        }.scan(path, null);
        if (Boolean.TRUE == b) {
            // label is a target of a break/continue do not report.
            return null;
        }
        List<String> options = new ArrayList<>();
        // eliminate duplicities
        Set<Element> resolved = new HashSet<>();
        // if not an enum type, inspect the other cases to see what identifiers are there.
        // Attempt to resolve the identifiers using the same qualifier(s) as the other cases
        END: if (enumType) {
            // try to resolve the identifier as an enum constant:
            TypeElement te = (TypeElement)((DeclaredType)m).asElement();
            for (Element f : te.getEnclosedElements()) {
                if (f.getKind() == ElementKind.ENUM_CONSTANT) {
                    if (f.getSimpleName().equals(l)) {
                        options.add(l.toString());
                        break;
                    }
                }
            }
        } else {
            for (CaseTree cst : ((SwitchTree)swTree).getCases()) {
                Tree expr = cst.getExpression();
                if (expr == null || 
                    (expr.getKind() != Tree.Kind.IDENTIFIER && expr.getKind() != Tree.Kind.MEMBER_SELECT)) {
                    continue;
                }
                Element el = info.getTrees().getElement(new TreePath(path, cst.getExpression()));
                if (el == null) {
                    continue;
                }
                if (expr.getKind() == Tree.Kind.IDENTIFIER) {
                    // try to resolve an unqualified identifier
                    if (tryResolveIdentifier(info, lPath, m, resolved, l.toString())) {
                        options.add(0, l.toString());
                    }
                } 
                // attempt to resolve a simple-qualified identifier; assuming the user already has an import in the source
                Element outer = el.getEnclosingElement();
                if (outer != null && (outer.getKind() == ElementKind.CLASS || outer.getKind() == ElementKind.INTERFACE
                                   || outer.getKind() == ElementKind.RECORD || outer.getKind() == ElementKind.ENUM)) {
                    TypeElement tel = (TypeElement)outer;
                    String x =  tel.getSimpleName() + "." + l.toString();
                    if (tryResolveIdentifier(info, lPath, m, resolved, x)) {
                        options.add(x);
                    } else {
                        // last attempt: use FQN
                        x = tel.getQualifiedName().toString() + "." + l.toString();
                        if (tryResolveIdentifier(info, lPath, m, resolved, x)) {
                            options.add(x);
                        }
                    }
                }
            }
        }
        if (options.isEmpty()) {
            return ErrorDescriptionFactory.forName(ctx, lt, Bundle.TEXT_MissingSwitchCase());
        }
        List<Fix> fixes = new ArrayList<>(options.size());
        for (String s : options) {
            fixes.add(JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_AddMissingSwitchCase(s), lPath, 
                        "case " + s + ": $stmt;"));
        }
        return ErrorDescriptionFactory.forName(ctx, lt, Bundle.TEXT_MissingSwitchCase(), 
                fixes.toArray(new Fix[0]));
    }
    
    private static boolean tryResolveIdentifier(CompilationInfo info, TreePath place, 
            TypeMirror expectedType, Set<Element> resolved, String ident) {
        SourcePositions[] positions = new SourcePositions[1];
        ExpressionTree et = info.getTreeUtilities().parseExpression(ident, positions);
        TypeMirror unqType = info.getTreeUtilities().attributeTree(et, info.getTrees().getScope(place));
        Element e = info.getTrees().getElement(new TreePath(place, et));
        if (!Utilities.isValidType(unqType) || e == null || 
                (e.getKind() != ElementKind.FIELD && e.getKind() != ElementKind.ENUM_CONSTANT)) {
            return false;
        }
        if (!resolved.add(e)) {
            return false;
        }
        return info.getTypes().isAssignable(unqType, expectedType);
    }

    @Hint(
            displayName = "#DN_HashCodeOnArray",
            description = "#DESC_HashCodeOnArray",
            category = "bugs",
            enabled = true,
            suppressWarnings = { "ArrayHashCode" }
    )
    @TriggerPatterns({
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.hashCode()", 
                constraints = {
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    @NbBundle.Messages({
        "TEXT_HashCodeOnArray=hashCode() called on array instance",
        "FIX_UseArraysHashCode=Use Arrays.hashCode()",
        "FIX_UseArraysDeepHashCode=Use Arrays.deepHashCode()"
    })
    public static List<ErrorDescription> hashCodeOnArray(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        TreePath arrayRef = ctx.getVariables().get("$v"); // NOI18N
        boolean enableDeep = ArrayStringConversions.canContainArrays(ci, arrayRef);
        List<ErrorDescription> result = new ArrayList<ErrorDescription>(enableDeep ? 2 : 1);
        TreePathHandle handle = TreePathHandle.create(ctx.getPath(), ci);
        result.add(ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_HashCodeOnArray(),
                new HashCodeFix(false, handle).toEditorFix()));
        if (enableDeep) {
            result.add(ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_HashCodeOnArray(),
                    new HashCodeFix(true, handle).toEditorFix()));
        }
        return result;
    }
    
    private static final class HashCodeFix extends JavaFix {
        private final boolean deep;

        public HashCodeFix(boolean deep, TreePathHandle handle) {
            super(handle);
            this.deep = deep;
        }
        
        @Override
        protected String getText() {
            return deep ? Bundle.FIX_UseArraysDeepHashCode() : Bundle.FIX_UseArraysHashCode();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
            Tree t = ctx.getPath().getLeaf();
            if (t.getKind() != Tree.Kind.METHOD_INVOCATION) {
                return;
            }
            MethodInvocationTree mi = (MethodInvocationTree)t;
            if (mi.getMethodSelect().getKind() != Tree.Kind.MEMBER_SELECT) {
                return;
            }
            MemberSelectTree selector = ((MemberSelectTree)mi.getMethodSelect());
            TreeMaker maker = ctx.getWorkingCopy().getTreeMaker();
            ExpressionTree ms = maker.MemberSelect(maker.QualIdent("java.util.Arrays"), deep ? "deepHashCode" : "hashCode"); // NOI18N
            Tree nue = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(), 
                            ms, 
                            Collections.singletonList(selector.getExpression())
            );
            ctx.getWorkingCopy().rewrite(t, nue);
        }
    }
}
