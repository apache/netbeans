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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ParenthesizedPatternTree;
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.CaseTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.CodeStyleUtils;
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
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "DN_ConvertToNestedRecordPattern=Convert to nested record pattern",
    "DESC_ConvertToNestedRecordPattern=Convert to nested record pattern",
    "ERR_ConvertToNestedRecordPattern=Convert to nested record pattern",
    "FIX_ConvertToNestedRecordPattern=Convert to nested record pattern"
})
@Hint(displayName = "#DN_ConvertToNestedRecordPattern", description = "#DESC_ConvertToNestedRecordPattern", category = "rules15",
        minSourceVersion = "19")
/**
 *
 * @author mjayan
 */
public class ConvertToNestedRecordPattern {

    private static final int RECORD_PATTERN_PREVIEW_JDK_VERSION = 19;

    @TriggerTreeKind(Tree.Kind.DECONSTRUCTION_PATTERN)
    public static ErrorDescription convertToNestedRecordPattern(HintContext ctx) {
        if (Utilities.isJDKVersionLower(RECORD_PATTERN_PREVIEW_JDK_VERSION) && !CompilerOptionsQuery.getOptions(ctx.getInfo().getFileObject()).getArguments().contains("--enable-preview")) {
            return null;
        }
        TreePath t = ctx.getPath();
        if (t.getParentPath().getLeaf().getKind() != Tree.Kind.INSTANCE_OF) {
            return null;
        }
        Set<String> recordPatternVarSet = new HashSet<>();
        Map<PatternTree, List<PatternTree>> recordComponentMap = new LinkedHashMap<>();
        DeconstructionPatternTree recordPattern = (DeconstructionPatternTree) t.getLeaf();
        recordComponentMap = findNested(recordPattern, recordComponentMap);

        for (PatternTree p : recordComponentMap.keySet()) {
            BindingPatternTree bTree = (BindingPatternTree) p;
            recordPatternVarSet.add(bTree.getVariable().getName().toString());
        }
        while (t != null && t.getLeaf().getKind() != Tree.Kind.BLOCK) {
            t = t.getParentPath();
        }
        Set<TreePath> convertPath = new HashSet<>();
        List<String> localVarList = new ArrayList<>();
        Map<String, List<UserVariables>> userVars = new HashMap<>();
        new CancellableTreePathScanner<Void, Void>() {

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                localVarList.add(node.getName().toString());
                Map<String, TreePath> outerVariables = new HashMap<>();
                Map<String, String> innerVariables = new HashMap<>();
                List<UserVariables> nList = new ArrayList<>();
                boolean match = MatcherUtilities.matches(ctx, getCurrentPath(), "$type $var1 = $expr3.$meth1()", outerVariables, new HashMap<>(), innerVariables);

                if (match && recordPatternVarSet.contains(outerVariables.get("$expr3").getLeaf().toString())) {
                    String expr3 = outerVariables.get("$expr3").getLeaf().toString();
                    nList.clear();
                    if (userVars.get(expr3) != null) {
                        nList = userVars.get(expr3);
                    }
                    nList.add(new UserVariables(innerVariables.get("$var1"), innerVariables.get("$meth1")));
                    userVars.put(expr3, nList);
                    convertPath.add(getCurrentPath());
                }
                return super.visitVariable(node, p);
            }

            @Override
            protected boolean isCanceled() {
                return ctx.isCanceled();
            }
        }.scan(t, null);
        if (!convertPath.isEmpty()) {
            Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), convertPath, localVarList, userVars).toEditorFix();
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToNestedRecordPattern(), fix);
        }
        return null;
    }

    private static Map<PatternTree, List<PatternTree>> findNested(PatternTree pTree, Map<PatternTree, List<PatternTree>> recordComponentMap) {
        switch (pTree.getKind()) {
            case BINDING_PATTERN:
                recordComponentMap.put(pTree, new ArrayList<>());
                return recordComponentMap;
            case DECONSTRUCTION_PATTERN:
                DeconstructionPatternTree bTree = (DeconstructionPatternTree) pTree;
                for (PatternTree p : bTree.getNestedPatterns()) {
                    findNested(p, recordComponentMap);
                }
                break;
            case PARENTHESIZED_PATTERN:
                ParenthesizedPatternTree parenthTree = (ParenthesizedPatternTree) pTree;
                findNested(parenthTree.getPattern(), recordComponentMap);
                break;
            default:
                return recordComponentMap;
        }
        return recordComponentMap;
    }

    private static class UserVariables {

        String methodName;
        String variable;

        UserVariables(String variable, String methodName) {
            this.variable = variable;
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getVariable() {
            return variable;
        }
    }

    private static final class FixImpl extends JavaFix {

        private final Map<String, List<UserVariables>> userVars;
        private final Set<TreePathHandle> replaceOccurrences;
        List<String> localVarList;

        public FixImpl(CompilationInfo info, TreePath main, Set<TreePath> replaceOccurrences, List<String> localVarList, Map<String, List<UserVariables>> userVars) {
            super(info, main);
            this.replaceOccurrences = replaceOccurrences.stream().map(tp -> TreePathHandle.create(tp, info)).collect(Collectors.toSet());
            this.userVars = userVars;
            this.localVarList = localVarList;
        }

        @Override
        protected String getText() {
            return Bundle.ERR_ConvertToNestedRecordPattern();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath t = ctx.getPath();
            TypeElement type = null;
            Map<PatternTree, List<PatternTree>> recordComponentMap = new LinkedHashMap<>();
            DeconstructionPatternTree recordPattern = (DeconstructionPatternTree) t.getLeaf();
            recordComponentMap = findNested(recordPattern, recordComponentMap);

            Set<String> localVars = new HashSet<>(localVarList);
            for (PatternTree p : recordComponentMap.keySet()) {
                List<PatternTree> bindTree = new ArrayList<>();
                BindingPatternTree bTree = (BindingPatternTree) p;
                VariableTree v = bTree.getVariable();
                type = (TypeElement) wc.getTrees().getElement(TreePath.getPath(t, v.getType()));
                if (type == null || type.getRecordComponents().size() == 0) {
                    continue;
                }
                outer:
                for (RecordComponentElement recordComponent : type.getRecordComponents()) {
                    String name = recordComponent.getSimpleName().toString();
                    TypeMirror returnType = recordComponent.getAccessor().getReturnType();
                    if (userVars.get(v.getName().toString()) != null) {
                        for (UserVariables var : userVars.get(v.getName().toString())) {
                            if (var.getMethodName().equals(name)) {
                                bindTree.add((BindingPatternTree) wc.getTreeMaker().BindingPattern(wc.getTreeMaker().Variable(wc.getTreeMaker().
                                        Modifiers(EnumSet.noneOf(Modifier.class)), var.getVariable(), wc.getTreeMaker().Type(returnType), null)));
                                continue outer;
                            }
                        }
                    }
                    String baseName = name;
                    int cnt = 1;
                    while (SourceVersion.isKeyword(name) || localVars.contains(name)) {
                        name = CodeStyleUtils.addPrefixSuffix(baseName + cnt++, "", "");
                    }
                    localVars.add(name);
                    bindTree.add((BindingPatternTree) wc.getTreeMaker().BindingPattern(wc.getTreeMaker().Variable(wc.getTreeMaker().
                            Modifiers(EnumSet.noneOf(Modifier.class)), name, wc.getTreeMaker().Type(returnType), null)));
                }
                recordComponentMap.put(p, bindTree);
            }

            DeconstructionPatternTree d = (DeconstructionPatternTree) createNestedPattern((PatternTree) t.getLeaf(), wc, recordComponentMap);
            while (t != null && t.getLeaf().getKind() != Tree.Kind.BLOCK) {
                t = t.getParentPath();
            }

            List<Tree> removeList = replaceOccurrences.stream().map(tph -> tph.resolve(wc).getLeaf()).collect(Collectors.toList());
            for (Tree tree : removeList) {
                StatementTree statementTree = (StatementTree) tree;
                Utilities.removeStatements(wc, TreePath.getPath(t, statementTree), null);
            }
            wc.rewrite(ctx.getPath().getLeaf(), d);
        }
    }

    private static PatternTree createNestedPattern(PatternTree pTree, WorkingCopy wc, Map<PatternTree, List<PatternTree>> map) {
        switch (pTree.getKind()) {
            case BINDING_PATTERN:
                if (map.containsKey(pTree) && !map.get(pTree).isEmpty()) {
                    BindingPatternTree p = (BindingPatternTree) pTree;
                    VariableTree v = (VariableTree) p.getVariable();
                    return (DeconstructionPatternTree) wc.getTreeMaker().RecordPattern((ExpressionTree) v.getType(), map.get(pTree), v);
                } else {
                    return pTree;
                }
            case DECONSTRUCTION_PATTERN:
                DeconstructionPatternTree bTree = (DeconstructionPatternTree) pTree;
                List<PatternTree> list = new ArrayList<>();
                for (PatternTree p : bTree.getNestedPatterns()) {
                    PatternTree val = createNestedPattern(p, wc, map);
                    list.add(val);
                }
                return (DeconstructionPatternTree) wc.getTreeMaker().RecordPattern(bTree.getDeconstructor(), list, bTree.getVariable());
            case PARENTHESIZED_PATTERN:
                ParenthesizedPatternTree parenthTree = (ParenthesizedPatternTree) pTree;
                return createNestedPattern(parenthTree.getPattern(), wc, map);
            default:
                return pTree;
        }
    }
}
