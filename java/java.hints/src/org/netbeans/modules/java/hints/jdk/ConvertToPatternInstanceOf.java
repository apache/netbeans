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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
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
 * @author sdedic
 */
@NbBundle.Messages({
    "DN_ConvertToPatternInstanceOf=Convert to instanceof <pattern>",
    "DESC_ConvertToPatternInstanceOf=Convert to instanceof <pattern>",
    "ERR_ConvertToPatternInstanceOf=instanceof <pattern> can be used here",
    "FIX_ConvertToPatternInstanceOf=Use instanceof <pattern>"
})
@Hint(displayName="#DN_ConvertToPatternInstanceOf", description="#DESC_ConvertToPatternInstanceOf", category="rules15",
        minSourceVersion = "14")
public class ConvertToPatternInstanceOf {
    
    @TriggerPatterns({
        @TriggerPattern(value="if ($expr instanceof $typeI) { $statements$;} else $else$;"),
    })
    public static ErrorDescription trivial(HintContext ctx) {
        //XXX: sideeffects in $expr
        if (!MatcherUtilities.matches(ctx, ctx.getPath(), "if ($expr instanceof $typeI) { $typeV $var = ($typeC) $expr; $other$;} else $else$;", true)) {
            Set<TreePath> convertPath = new HashSet<>();
            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitTypeCast(TypeCastTree node, Void p) {
                    if (MatcherUtilities.matches(ctx, getCurrentPath(), /*TODO: different type*/"($typeI) $expr")) {
                        convertPath.add(getCurrentPath());
                    }
                    return super.visitTypeCast(node, p);
                }
            }.scan(ctx.getPath(), null);
            if (!convertPath.isEmpty()) {
                TreePath typeI = ctx.getVariables().get("$typeI");
                TypeMirror typeITM = ctx.getInfo().getTrees().getTypeMirror(typeI);
                List<String> varNameCandidates = org.netbeans.modules.editor.java.Utilities.varNamesSuggestions(typeITM, ElementKind.LOCAL_VARIABLE, EnumSet.noneOf(Modifier.class), null, null, ctx.getInfo().getTypes(), ctx.getInfo().getElements(), Collections.emptyList(), CodeStyle.getDefault(ctx.getInfo().getFileObject()));
                String varName = Utilities.makeNameUnique(ctx.getInfo(), ctx.getInfo().getTrees().getScope(ctx.getPath()), varNameCandidates.get(0));
                Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), varName, false, convertPath).toEditorFix();

                return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToPatternInstanceOf(), fix);
            }
            return null;
        }
        if (ctx.getMultiVariables().get("$other$").isEmpty()) {
            return null; //could be solved, but what's the point?
        }
        TypeMirror typeI = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$typeI"));
        TypeMirror typeC = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$typeC"));
        if (!ctx.getInfo().getTypes().isSameType(typeI, typeC)) {
            System.err.println("different types (" + typeI + ", " + typeC + ") in " + ctx.getInfo().getFileObject());
            return null;
        }
        IfTree it = (IfTree) ctx.getPath().getLeaf();
        BlockTree bt = (BlockTree) it.getThenStatement();
        VariableTree var = (VariableTree) bt.getStatements().get(0);
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), var.getName().toString(), true, Collections.emptySet()).toEditorFix();
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToPatternInstanceOf(), fix);
    }

    private static final class FixImpl extends JavaFix {

        private final String varName;
        private final boolean removeFirst;
        private final Set<TreePathHandle> replaceOccurrences;

        public FixImpl(CompilationInfo info, TreePath main, String varName, boolean removeFirst, Set<TreePath> replaceOccurrences) {
            super(info, main);
            this.varName = varName;
            this.removeFirst = removeFirst;
            this.replaceOccurrences = replaceOccurrences.stream().map(tp -> TreePathHandle.create(tp, info)).collect(Collectors.toSet());
        }


        @Override
        protected String getText() {
            return Bundle.FIX_ConvertToPatternInstanceOf();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath main = ctx.getPath();
            IfTree it = (IfTree) main.getLeaf();
            InstanceOfTree iot = (InstanceOfTree) ((ParenthesizedTree) it.getCondition()).getExpression();
            StatementTree bt = it.getThenStatement();
            InstanceOfTree cond = wc.getTreeMaker().InstanceOf(iot.getExpression(),
                                                               wc.getTreeMaker().BindingPattern(wc.getTreeMaker().Variable(wc.getTreeMaker().Modifiers(EnumSet.noneOf(Modifier.class)), varName, iot.getType(), null)));
            StatementTree thenBlock = removeFirst ? wc.getTreeMaker().removeBlockStatement((BlockTree) bt, 0) : bt;
            wc.rewrite(it, wc.getTreeMaker().If(wc.getTreeMaker().Parenthesized(cond), thenBlock, it.getElseStatement()));
            replaceOccurrences.stream().map(tph -> tph.resolve(wc)).forEach(tp -> {
                if (!removeFirst && tp.getParentPath().getLeaf().getKind() == Kind.PARENTHESIZED) {
                    tp = tp.getParentPath();
                }
                wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Identifier(varName));
            });
        }

    }

}
