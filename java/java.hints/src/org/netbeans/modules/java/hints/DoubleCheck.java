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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * The hint detects double-locking idion that cannot work with JDK &lt; 5.
 * It also detects a variant that first assigns the field into a local variable first.
 * In JDK 5, a recommended idiom can be generated
 * @author Jaroslav tulach
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.DoubleCheck", description = "#DESC_org.netbeans.modules.java.hints.DoubleCheck", id="org.netbeans.modules.java.hints.DoubleCheck", category="thread", suppressWarnings="DoubleCheckedLocking")
public class DoubleCheck {

    @TriggerTreeKind(Kind.SYNCHRONIZED)
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Tree e = treePath.getLeaf();

        SynchronizedTree synch = (SynchronizedTree)e;
        TreePath outer = findOuterIf(ctx, treePath);
        if (outer == null) {
            return null;
        }

        IfTree same = null;
        TreePath samePath = null;
        TreePath block = new TreePath(treePath, synch.getBlock());
        TreePath exprPath = null;
        TreePath[] fieldPath = new TreePath[1];
        for (StatementTree statement : synch.getBlock().getStatements()) {
            samePath = new TreePath(block, statement);
            exprPath = sameIfAndValidate(compilationInfo, samePath, outer, fieldPath);
            if (exprPath != null) {
                same = (IfTree)statement;
                break;
            }
            if (ctx.isCanceled()) {
                return null;
            }
        }
        if (same == null) {
            return null;
        }
        Element el = compilationInfo.getTrees().getElement(fieldPath[0]);
        if (el == null) {
            return null;
        }
        Element checkEl = compilationInfo.getTrees().getElement(exprPath);
        
        boolean jdk5 = compilationInfo.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0;
        boolean checkLocalVar = checkEl != null && checkEl.getKind() == ElementKind.LOCAL_VARIABLE;
        boolean varVolatile = el.getModifiers().contains(Modifier.VOLATILE);
        boolean field = el.getKind() == ElementKind.FIELD;
        
        Fix fix = null;
        
        if (!varVolatile || !jdk5 || !field) {
            // the field is not volatile or it cannot be resolved from the code [local var cannot be volatile]
            // the only fix available when JDK5 is missing
            fix = new SynchronizeFix(
                    TreePathHandle.create(treePath, compilationInfo),
                    TreePathHandle.create(outer, compilationInfo),
                    compilationInfo.getFileObject()
            ).toEditorFix();
        }
        
        Fix fix2 = null;
        // can be only applied the real field is known
        if (jdk5 && field && !(checkLocalVar && varVolatile)) {
            int style;
            if (checkLocalVar && !varVolatile) {
                style = 0;
            } else if (varVolatile && !checkLocalVar) {
                style = 2; 
            } else {
                // default JDK5 idiom
                style = 1;
            }
            fix2 = new DoubleCheckJDK5Fix(
                TreePathHandle.create(outer, compilationInfo), 
                TreePathHandle.create(exprPath, compilationInfo),
                TreePathHandle.create(fieldPath[0], compilationInfo),
                TreePathHandle.create(samePath, compilationInfo),
                style).toEditorFix();
        }
        int span = (int)compilationInfo.getTrees().getSourcePositions().getStartPosition(
            compilationInfo.getCompilationUnit(),
            synch
        );
        if (fix == null && fix2 == null) {
            return null;
        }

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), NbBundle.getMessage(DoubleCheck.class, "ERR_DoubleCheck"), fix2, fix);// NOI18N
    }

    private static TreePath findOuterIf(HintContext ctx, TreePath treePath) {
        while (!ctx.isCanceled()) {
            treePath = treePath.getParentPath();
            if (treePath == null) {
                break;
            }
            Tree leaf = treePath.getLeaf();
            
            if (leaf.getKind() == Kind.IF) {
                return treePath;
            }
            
            if (leaf.getKind() == Kind.BLOCK) {
                BlockTree b = (BlockTree)leaf;
                if (b.getStatements().size() == 1) {
                    // ok, empty blocks can be around synchronized(this) 
                    // statements
                    continue;
                }
            }
            
            return null;
        }
        return null;
    }
    
    @SuppressWarnings("AssignmentToMethodParameter")
    private static TreePath findParentOfKind(TreePath p, Tree.Kind kind) {
        while (p != null) {
            if (p.getLeaf().getKind() == kind) {
                return p;
            }
            p = p.getParentPath();
        }
        return p;
    }
    
    private static boolean sameCompilationUnit(TreePath first, TreePath second) {
        TreePath one = findParentOfKind(first, Kind.COMPILATION_UNIT);
        TreePath two = findParentOfKind(second, Kind.COMPILATION_UNIT);
        return one != null && two != null && one.getLeaf() == two.getLeaf();
    }

    /**
     * Checks, that the two if statements test the same variable. Returns the tree that references
     * the guard variable if the two ifs are the same, or {@code null} if the ifs do not match.
     * @param info
     * @param statementTP
     * @param secondTP
     * @return 
     */
    private static TreePath sameIfAndValidate(CompilationInfo info, TreePath statementTP, TreePath secondTP, TreePath[] fieldRef) {
        StatementTree statement = (StatementTree) statementTP.getLeaf();
        
        if (statement.getKind() != Kind.IF) {
            return null;
        }
        
        IfTree first = (IfTree)statement;
        IfTree second = (IfTree) secondTP.getLeaf();
        
        if (first.getElseStatement() != null) {
            return null;
        }
        if (second.getElseStatement() != null) {
            return null;
        }
        
        TreePath varFirst = equalToNull(new TreePath(statementTP, first.getCondition()));
        TreePath varSecond = equalToNull(new TreePath(secondTP, second.getCondition()));
        
        if (varFirst == null || varSecond == null) {
            return null;
        }

        Element firstVariable = info.getTrees().getElement(varFirst);
        Element secondVariable = info.getTrees().getElement(varSecond);
        Element target = firstVariable;
        if (firstVariable != null && firstVariable.equals(secondVariable)) {
            TreePath var = info.getTrees().getPath(firstVariable);
            if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_5) < 0) {
                fieldRef[0] = var;
                return varFirst;
            }
            if (firstVariable.getKind() == ElementKind.LOCAL_VARIABLE) {
                // check how the variable was assigned:
                TreePath methodPath = Utilities.findTopLevelBlock(varFirst);
                FlowResult fr = Flow.assignmentsForUse(info, methodPath, new AtomicBoolean(false));
                Iterable<? extends TreePath> itp = fr.getAssignmentsForUse().get(varFirst.getLeaf());
                
                if (itp != null) {
                    Iterator<? extends TreePath> i = itp.iterator();
                    if (i.hasNext()) {
                        TreePath v = i.next();
                        if (!i.hasNext()) {
                            // if the local variable has exactly one possible value,
                            // use it as the field 
                            target = info.getTrees().getElement(v);
                            if (target != null && target.getKind() == ElementKind.FIELD) {
                                var = info.getTrees().getPath(target);
                                if (!sameCompilationUnit(var, varFirst)) {
                                    // the variable is somewhere ... 
                                    var = info.getTrees().getPath(firstVariable);
                                }
                            }
                        }
                    }
                }
            }
            fieldRef[0] = var;
            return varFirst;
        }
        
        return null;
    }
    
    private static TreePath equalToNull(TreePath tp) {
        ExpressionTree t = (ExpressionTree) tp.getLeaf();
        if (t.getKind() == Kind.PARENTHESIZED) {
            ParenthesizedTree p = (ParenthesizedTree)t;
            t = p.getExpression();
            tp = new TreePath(tp, t);
        }
        
        if (t.getKind() != Kind.EQUAL_TO) {
            return null;
        }
        BinaryTree bt = (BinaryTree)t;
        if (bt.getLeftOperand().getKind() == Kind.NULL_LITERAL && bt.getRightOperand().getKind() != Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getRightOperand());
        }
        if (bt.getLeftOperand().getKind() != Kind.NULL_LITERAL && bt.getRightOperand().getKind() == Kind.NULL_LITERAL) {
            return new TreePath(tp, bt.getLeftOperand());
        }
        return null;
    }
    
    /**
     * This fix will declare the checked field volatile, if it is not already. 
     * Assigns the field into a local variable
     */
    private static final class DoubleCheckJDK5Fix extends JavaFix {
        private final TreePathHandle  fieldAccessHandle;
        private final TreePathHandle  fieldHandle;
        private final TreePathHandle  innerIfHandle;
        private final int         justVolatile;
        
        private FlowResult flow;
        
        public DoubleCheckJDK5Fix(TreePathHandle handle, TreePathHandle fieldAccessHandle, TreePathHandle fieldHandle, TreePathHandle innerIfHandle, 
                int justVolatile) {
            super(handle);
            this.fieldAccessHandle = fieldAccessHandle;
            this.fieldHandle = fieldHandle;
            this.innerIfHandle = innerIfHandle;
            this.justVolatile = justVolatile;
        }

        @Override
        protected String getText() {
            switch (justVolatile) {
                case 0:
                    return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck_Volatile");
                case 1:
                    return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck_5"); // NOI18N
                case 2:
                    return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck_Local"); // NOI18N
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker mk = wc.getTreeMaker();
            TreePath varPath = fieldAccessHandle.resolve(wc);
            TreePath fieldPath = fieldHandle.resolve(wc);
            
            Element ve = wc.getTrees().getElement(fieldPath);
            if (ve == null) {
                // TODO: log/inform the user
                return;
            }
            VariableTree vt = (VariableTree)fieldPath.getLeaf();
            
            if (!vt.getModifiers().getFlags().contains(Modifier.VOLATILE)) {
                ModifiersTree mt = wc.getTreeMaker().addModifiersModifier(vt.getModifiers(), Modifier.VOLATILE);
                wc.rewrite(vt.getModifiers(), mt);
            }
            Element classEl = ((VariableElement)ve).getEnclosingElement();
            if (justVolatile == 0) {
                // the checked symbol is already a variable
                return; 
            }
            // construct the reference to the field. If the field is referenced just using the 
            // simple name, combine it with [outerClass.]this. If the reference is more complex,
            // it is qualified enough and can be used as it is
            ExpressionTree fieldAccess = (ExpressionTree)varPath.getLeaf();
            if (fieldAccess.getKind() == Tree.Kind.IDENTIFIER) {
                if (ve.getModifiers().contains(Modifier.STATIC)) {
                    fieldAccess = mk.MemberSelect(mk.QualIdent(classEl), vt.getName());
                } else {
                    fieldAccess = mk.MemberSelect(mk.Identifier("this"), vt.getName());
                }
            }
            
            TreePath ifStatement = ctx.getPath();
            TreePath ifParent = ifStatement.getParentPath();
            BlockTree blTree = (BlockTree)ifParent.getLeaf();
            VariableTree localCopy = mk.Variable(
                    mk.Modifiers(Collections.<Modifier>emptySet()), vt.getName(),
                    vt.getType(), fieldAccess);

            // insert the variable declaration before the if statement:
            int ifIndex = blTree.getStatements().indexOf(ifStatement.getLeaf());
            BlockTree newBlock = mk.insertBlockStatement(blTree, ifIndex, localCopy);
            wc.rewrite(blTree, newBlock);
            
            TreePath innerIf = innerIfHandle.resolve(wc);
            TreePath innerBlock = innerIf.getParentPath();
            assert innerBlock.getLeaf().getKind() == Tree.Kind.BLOCK;
            BlockTree bt = (BlockTree)innerBlock.getLeaf();
            int innerIfIndex = bt.getStatements().indexOf(innerIf.getLeaf());
            
            // add a second assignment to the local variable
            BlockTree nbt = mk.insertBlockStatement(bt, innerIfIndex,
                    mk.ExpressionStatement(
                            mk.Assignment(mk.Identifier(vt.getName()), fieldAccess)
                    ));
            wc.rewrite(bt, nbt);
            TreePath topLevel = Utilities.findTopLevelBlock(ifParent);
            flow = Flow.assignmentsForUse(wc, topLevel, new AtomicBoolean(false));
            
            VariableAccessWalker walker = new VariableAccessWalker((VariableElement)ve, 
                    fieldAccess, mk.Identifier(localCopy.getName()), ifStatement, wc);
            walker.scan(topLevel, null);
        }
    }
    
    private static final class VariableAccessWalker extends ErrorAwareTreePathScanner {
        private final VariableElement originalVariable;
        private final ExpressionTree  fieldAccessTree;
        /**
         * Tree that references the local variable, will be used instead of
         * field access for reading
         */
        private final ExpressionTree  localVarTree;
        private final TreePath        ifPath;
        private final WorkingCopy     wc;
        private final TreeMaker       mk;

        public VariableAccessWalker(VariableElement originalVariable, ExpressionTree fieldAccessTree, ExpressionTree localVarTree, TreePath ifPath, WorkingCopy wc) {
            this.originalVariable = originalVariable;
            this.fieldAccessTree = fieldAccessTree;
            this.localVarTree = localVarTree;
            this.ifPath = ifPath;
            this.wc = wc;
            this.mk = wc.getTreeMaker();
        }
        
        // will turn to true once the outer `if' is traversed. The following code should
        // 
        private boolean         ifEncountered;

        /**
        @Override
        public Object scan(TreePath p, Object o) {
            // after the outer if is encountered, direct every reference to local variable
            ifEncountered |= p.getLeaf() == ifPath.getLeaf();
            return super.scan(p, o);
        }
        */
        
        @Override
        public Object scan(Tree p, Object o) {
            // after the outer if is encountered, direct every reference to local variable
            ifEncountered |= p == ifPath.getLeaf();
            return super.scan(p, o);
        }
        
        
        @Override
        public Object visitIdentifier(IdentifierTree node, Object p) {
            if (ifEncountered) {
                // inside the inner if-statement, all but assignments should transform to local variable references.
                Element el = wc.getTrees().getElement(getCurrentPath());
                if (el == originalVariable) {
                    // rewrite the member select to whatever tree access is necessary
                    wc.rewrite(node, localVarTree);
                    // skip the member select
                    return null;
                }
            }
            return super.visitIdentifier(node, p); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object visitMemberSelect(MemberSelectTree node, Object p) {
            if (ifEncountered) {
                // inside the inner if-statement, all but assignments should transform to local variable references.
                Element el = wc.getTrees().getElement(getCurrentPath());
                if (el == originalVariable) {
                    // rewrite the member select to whatever tree access is necessary
                    wc.rewrite(node, localVarTree);
                    // skip the member select
                    return null;
                }
            }
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Object visitCompoundAssignment(CompoundAssignmentTree node, Object p) {
            // TODO: it's not necessary for reference-type variables; might be eventually used
            // for primitives
            return super.visitCompoundAssignment(node, p); 
        }

        @Override
        public Object visitAssignment(AssignmentTree node, Object p) {
            if (!ifEncountered) {
                return super.visitAssignment(node, p);
            }
            Element el = wc.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            if (el == originalVariable) {
                // assignment should be converted into local var + field assignment
                ExpressionTree et = mk.Assignment(fieldAccessTree, 
                        mk.Assignment(localVarTree, node.getExpression()));
                wc.rewrite(node, et);
            }
            return scan(node.getExpression(), p);
        }

        
    }
    
    private static final class SynchronizeFix extends JavaFix {
        private TreePathHandle synchHandle;
        private FileObject file;

        public SynchronizeFix(TreePathHandle synchHandle, TreePathHandle ifHandle, FileObject file) {
            super(ifHandle);
            this.synchHandle = synchHandle;
            this.file = file;
        }
        
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "FIX_DoubleCheck"); // NOI18N
        }
        
        @Override public String toString() {
            return "FixDoubleCheck"; // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath ifTreePath = ctx.getPath();
            Tree syncTree = synchHandle.resolve(wc).getLeaf();
            wc.rewrite(ifTreePath.getLeaf(), syncTree);
        }
    }
    
}
