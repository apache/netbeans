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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author mjayan
 */
@NbBundle.Messages({
    "DN_ConvertToRecordPattern=Convert to instanceof <record pattern>",
    "DESC_ConvertToRecordPattern=Convert to instanceof <record pattern>",
    "ERR_ConvertToRecordPattern=instanceof <record pattern> can be used here",
    "FIX_ConvertToRecordPattern=Use instanceof record pattern"
})
@Hint(displayName = "#DN_ConvertToRecordPattern", description = "#DESC_ConvertToRecordPattern", category = "rules15",
        minSourceVersion = "19")
public class ConvertToRecordPattern {

    private static final int RECORD_PATTERN_PREVIEW_JDK_VERSION = 19;

    @TriggerPatterns({
        @TriggerPattern(value = "if ($expr instanceof $typeI0 $var0 ) { $statements$;} else $else$;")
    })
    public static ErrorDescription trivial(HintContext ctx) {
        if (Utilities.isJDKVersionLower(RECORD_PATTERN_PREVIEW_JDK_VERSION) && !CompilerOptionsQuery.getOptions(ctx.getInfo().getFileObject()).getArguments().contains("--enable-preview")) {
            return null;
        }
        ElementKind kind = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$typeI0")).getKind();
        if (kind == ElementKind.RECORD) {
            Set<TreePath> convertPath = new HashSet<>();
            Set<String> localVarList = new HashSet<>();
            localVarList.add(ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$expr")).getSimpleName().toString());
            Map<String, String> varNames = new HashMap<>();
            new CancellableTreePathScanner<Void, Void>() {
                String variableName = null;

                @Override
                public Void visitVariable(VariableTree node, Void p) {
                    if (variableName == null) {
                        variableName = node.getName().toString();
                    }
                    localVarList.add(node.getName().toString());
                    Map<String, TreePath> outerVariables = new HashMap<>();
                    Map<String, String> innerVariables = new HashMap<>();
                    boolean match = MatcherUtilities.matches(ctx, getCurrentPath(), "$type $var1 = $expr3.$meth1()", outerVariables, new HashMap<String, Collection<? extends TreePath>>(), innerVariables);

                    if (match && outerVariables.get("$expr3").getLeaf().toString().equals(variableName)) {
                        varNames.put(innerVariables.get("$meth1"), innerVariables.get("$var1"));
                        convertPath.add(getCurrentPath());
                    }
                    return super.visitVariable(node, p);
                }

                @Override
                protected boolean isCanceled() {
                    return ctx.isCanceled();
                }
            }.scan(ctx.getPath(), null);
            TypeElement type = (TypeElement) ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$typeI0"));
            List<? extends RecordComponentElement> recordSig = type.getRecordComponents();
            if (!convertPath.isEmpty()) {
                Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), convertPath, recordSig, varNames, localVarList).toEditorFix();
                return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToRecordPattern(), fix);
            }
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {

        private final Set<TreePathHandle> replaceOccurrences;
        private final List<? extends ElementHandle> recordSig;
        private final Map<String, String> varNames;
        private final Set<String> localVarList;

        public FixImpl(CompilationInfo info, TreePath main, Set<TreePath> replaceOccurrences, List<? extends RecordComponentElement> recordSig, Map<String, String> varNames, Set<String> localVarList) {
            super(info, main);
            this.recordSig = recordSig.stream().map(elem -> ElementHandle.create(elem)).collect(Collectors.toList());
            this.varNames = varNames;
            this.replaceOccurrences = replaceOccurrences.stream().map(tp -> TreePathHandle.create(tp, info)).collect(Collectors.toSet());
            this.localVarList = new HashSet<>(localVarList);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ConvertToRecordPattern();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath main = ctx.getPath();
            IfTree it = (IfTree) main.getLeaf();
            InstanceOfTree iot = (InstanceOfTree) ((ParenthesizedTree) it.getCondition()).getExpression();
            BindingPatternTree pattern = (BindingPatternTree) iot.getPattern();
            StatementTree bt = it.getThenStatement();

            List<PatternTree> bindTree = new ArrayList<>();

            List<RecordComponentElement> recordSignature = new ArrayList<>();
            recordSig.stream().map(elem -> elem.resolve(wc)).forEach(elem -> {
                recordSignature.add((RecordComponentElement) elem);
            });
            Set<String> localVars = new HashSet<>(localVarList);
            for (RecordComponentElement recordComponent : recordSignature) {
                String compName = recordComponent.getSimpleName().toString();
                String name = null;
                String returnType = null;
                if (varNames.containsKey(compName)) {
                    name = varNames.get(compName);
                } else {
                    int cnt = 1;
                    name = compName;
                    while (SourceVersion.isKeyword(name) || localVars.contains(name)) {
                        name = CodeStyleUtils.addPrefixSuffix(compName + cnt++, "", "");
                    }
                    localVars.add(name);
                }

                returnType = recordComponent.getAccessor().getReturnType().toString();
                returnType = returnType.substring(returnType.lastIndexOf(".") + 1);
                bindTree.add((BindingPatternTree) wc.getTreeMaker().BindingPattern(wc.getTreeMaker().Variable(wc.getTreeMaker().
                        Modifiers(EnumSet.noneOf(Modifier.class)), name, wc.getTreeMaker().Identifier(returnType), null)));
            }
            InstanceOfTree cond = wc.getTreeMaker().InstanceOf(iot.getExpression(), wc.getTreeMaker().RecordPattern((ExpressionTree) pattern.
                    getVariable().getType(), bindTree, pattern.getVariable()));
            List<Tree> removeList = replaceOccurrences.stream().map(tph -> tph.resolve(wc).getLeaf()).collect(Collectors.toList());
            for (Tree t : removeList) {
                bt = wc.getTreeMaker().removeBlockStatement((BlockTree) bt, (StatementTree) t);
            }
            wc.rewrite(it, wc.getTreeMaker().If(wc.getTreeMaker().Parenthesized(cond), bt, it.getElseStatement()));
        }
    }
}
