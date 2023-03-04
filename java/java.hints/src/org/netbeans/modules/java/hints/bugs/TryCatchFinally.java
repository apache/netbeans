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

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.Cancel;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.UseOptions;

/**
 * Contains hints for try-catch-finally blocks.
 * @author
 * sdedic
 */
@NbBundle.Messages({
    "# {0} - the rogue statement",
    "TEXT_returnBreakContinueInFinallyBlock=The ''{0}'' statement in the ''finally'' block discards unhandled exceptions",
    "TEXT_throwsInFinallyBlock=The 'throw' statement in 'finally' block may hide the original exception",
    "OPT_ReportFinallyRethrow=Report rethrow of caught exceptions"
})
public class TryCatchFinally {
    private static final Logger LOG = Logger.getLogger(TryCatchFinally.class.getName());
    
    public static final boolean DEF_REPORT_RETHROW = true;
    @BooleanOption(
            displayName = "#OPT_ReportFinallyRethrow", 
            defaultValue = DEF_REPORT_RETHROW,
            tooltip = ""
    )
    public static final String OPT_REPORT_RETHROW = "reportFinallyRethrow"; // NOI18N
    
    @Hint(category = "bugs",
          displayName = "#DN_TryCatchFinally_finallyThrowsException", // NOI18N
          description = "#DESC_TryCatchFinally_finallyThrowsException", // NOI18N
          suppressWarnings={"ThrowFromFinallyBlock"}, 
          options= Hint.Options.QUERY
    )
    @UseOptions(OPT_REPORT_RETHROW)
    @TriggerPattern("try { $smts$; } catch $catches$ finally { $handler$; }") // NOI18N
    public static List<ErrorDescription> finallyThrowsException(HintContext ctx) {
        List<TreePath>  trees = new ArrayList<TreePath>(3);
        ExitsFromBranches efab = new ExitsFromBranches(ctx.getInfo(), true);
        Collection<? extends TreePath> paths = ctx.getMultiVariables().get("$handler$"); // NOI18N
        CompilationInfo info = ctx.getInfo();
        for (TreePath tp : paths) {
            efab.scan(tp, trees);
        }
        if (trees.isEmpty()) {
            return null;
        }
        
        TreePath parent = ctx.getPath().getParentPath();
        TreePath selected = ctx.getPath();
        Set<Tree> catchVars = new HashSet<>();
        // finds the outermost enclosing catch
        while (parent != null) {
            Tree.Kind k = parent.getLeaf().getKind();
            if (k == Tree.Kind.METHOD || k == Tree.Kind.CLASS || k == Tree.Kind.INTERFACE || k == Tree.Kind.ENUM) {
                break;
            } else if (k == Tree.Kind.CATCH) {
                selected = parent;
                catchVars.add(
                        ((CatchTree)parent.getLeaf()).getParameter()
                );
                break;
            }
            parent = parent.getParentPath();
        }
        
        boolean checkRethrow = ctx.getPreferences().getBoolean(OPT_REPORT_RETHROW, DEF_REPORT_RETHROW);
        List<ErrorDescription> errs = new ArrayList<ErrorDescription>(trees.size());
        
        if (!checkRethrow) {
            FlowResult assignments = Flow.assignmentsForUse(ctx.getInfo(), selected, ctx::isCanceled);
            if (assignments == null || ctx.isCanceled()) {
                return null;
            }
            T: for (Iterator<TreePath> it = trees.iterator(); it.hasNext(); ) {
                TreePath p = it.next();
                Tree stmt = p.getLeaf();
                if (stmt.getKind() != Tree.Kind.THROW) {
                    it.remove();
                    continue;
                }
                ThrowTree tt = (ThrowTree)stmt;
                TreePath tp = new TreePath(p, tt.getExpression());

                Queue<TreePath> q = new ArrayDeque<>();
                q.offer(tp);

                boolean rethrow = true;

                Map<Tree, Iterable<?extends TreePath>> ass2Use = assignments.getAssignmentsForUse();
                while (rethrow && !q.isEmpty()) {
                    tp = q.poll();
                    Element el = info.getTrees().getElement(tp);
                    if (el == null) {
                        rethrow = false;
                        break;
                    }
                    if (el.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                        if (el.getModifiers().contains(Modifier.FINAL)) {
                            // OK
                            continue;
                        }
                    }
                    Iterable<? extends TreePath> vals = ass2Use.get(tp.getLeaf());
                    if (vals == null) {
                        rethrow = false;
                        break;
                    }
                    for (TreePath valuePath : vals) {
                        if (!catchVars.contains(valuePath.getLeaf())) {
                            q.offer(valuePath);
                        }
                    }
                }
                if (rethrow) {
                    it.remove();
                }
            }
        }
        if (ctx.isCanceled()) {
            return null;
        }
        for (TreePath p : trees) {
            Tree stmt = p.getLeaf();
            errs.add(ErrorDescriptionFactory.forTree(ctx, stmt, TEXT_throwsInFinallyBlock()));
        }
        return errs;
    }
    
    @Hint(category = "bugs",
          displayName = "#DN_TryCatchFinally_finallyDiscardsException", // NOI18N
          description = "#DESC_TryCatchFinally_finallyDiscardsException", // NOI18N
          suppressWarnings={"FinallyDiscardsException", "", "ReturnFromFinallyBlock", "ContinueOrBreakFromFinallyBlock"}, 
          options= Hint.Options.QUERY
    )
    @TriggerPattern("try { $smts$; } catch $catches$ finally { $handler$; }") // NOI18N
    public static List<ErrorDescription> finallyDiscardsException(HintContext ctx) {
        List<TreePath>  trees = new ArrayList<TreePath>(3);
        ExitsFromBranches efab = new ExitsFromBranches(ctx.getInfo());
        Collection<? extends TreePath> paths = ctx.getMultiVariables().get("$handler$"); // NOI18N
        
        for (TreePath tp : paths) {
            efab.scan(tp, trees);
        }
        if (trees.isEmpty()) {
            return null;
        }
        List<ErrorDescription> errs = new ArrayList<ErrorDescription>(trees.size());
        for (TreePath p : trees) {
            Tree stmt = p.getLeaf();
            final String stmtName;
            switch (stmt.getKind()) {
                case CONTINUE:
                    stmtName = "continue"; // NOI18N
                    break;
                case BREAK:
                    stmtName = "break"; // NOI18N
                    break;
                case RETURN:
                    stmtName = "return"; // NOI18N
                    break;
                default:
                    LOG.log(Level.WARNING, "Unexpected statement kind: {0}", stmt.getKind()); // NOI18N
                    continue;
            }
            
            errs.add(ErrorDescriptionFactory.forTree(ctx, stmt, TEXT_returnBreakContinueInFinallyBlock(stmtName)));
        }
        return errs;
    }
    
    private static final class ExitsFromBranches extends ErrorAwareTreePathScanner<Void, Collection<TreePath>> {
        private final  boolean analyzeThrows;
        private final CompilationInfo info;
        private final Set<Tree> seenTrees = new HashSet<Tree>();
        private final Stack<Set<TypeMirror>> caughtExceptions = new Stack<Set<TypeMirror>>();

        public ExitsFromBranches(CompilationInfo info, boolean analyzeThrows) {
            this.info = info;
            this.analyzeThrows = analyzeThrows;
        }
        
        public ExitsFromBranches(CompilationInfo info) {
            this.info = info;
            this.analyzeThrows = false;
        }

        @Override
        public Void scan(TreePath path, Collection<TreePath> p) {
            seenTrees.add(path.getLeaf());
            return super.scan(path, p);
        }

        @Override
        public Void scan(Tree tree, Collection<TreePath> trees) {
            seenTrees.add(tree);
            return super.scan(tree, trees);
        }

        /**
         * Note: if the labeled statement is 1st, in efab.scan(), the visit method is called without
         * prior scan(tree, param). This LabeledStatement is actually a target of break+continue, so
         * it must be also added to seenTrees.s
         */
        @Override
        public Void visitLabeledStatement(LabeledStatementTree node, Collection<TreePath> p) {
            seenTrees.add(node);
            return super.visitLabeledStatement(node, p);
        }
        
        @Override
        public Void visitIf(IfTree node, Collection<TreePath> trees) {
            scan(node.getThenStatement(), trees);
            scan(node.getElseStatement(), trees);
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree node, Collection<TreePath> trees) {
            if (!analyzeThrows) {
                trees.add(getCurrentPath());
            }
            return null;
        }

        @Override
        public Void visitBreak(BreakTree node, Collection<TreePath> trees) {
            if (!analyzeThrows && !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                trees.add(getCurrentPath());
            }
            return null;
        }

        @Override
        public Void visitContinue(ContinueTree node, Collection<TreePath> trees) {
            if (!analyzeThrows && !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
                trees.add(getCurrentPath());
            }
            return null;
        }

        @Override
        public Void visitTry(TryTree node, Collection<TreePath> trees) {
            Set<TypeMirror> caught = new HashSet<TypeMirror>();

            for (CatchTree ct : node.getCatches()) {
                TypeMirror t = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), ct), ct.getParameter()));

                if (t != null) {
                    caught.add(t);
                }
            }

            caughtExceptions.push(caught);
            
            try {
                scan(node.getBlock(), trees);
            } finally {
                caughtExceptions.pop();
            }
            scan(node.getFinallyBlock(), trees);
            return null;
        }

        @Override
        public Void visitThrow(ThrowTree node, Collection<TreePath> trees) {
            if (!analyzeThrows) {
                return null;
            }
            TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            boolean isCaught = false;

            OUTER: for (Set<TypeMirror> caught : caughtExceptions) {
                for (TypeMirror c : caught) {
                    if (info.getTypes().isSubtype(type, c)) {
                        isCaught = true;
                        break OUTER;
                    }
                }
            }

            super.visitThrow(node, trees);
            if (!isCaught) {
                trees.add(getCurrentPath());
            }
            return null;
        }

    }
}
