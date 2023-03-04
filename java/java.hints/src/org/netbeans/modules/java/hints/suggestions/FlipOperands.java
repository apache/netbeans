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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_FlipOperands=Flip operands of the binary operator",
    "DESC_FlipOperands=Flip operands of the binary operator",
    "ERR_FlipOperands=",
    "# {0} - one of the following Java operands: ==, !=, *, +, &, |, ^, &&, ||",
    "FIX_FlipOperands1=Flip operands of ''{0}''",
    "# {0} - one of the following Java operands (source): <, >, >=, <=",
    "# {1} - one of the following Java operands (target): >=, <=, <, >",
    "FIX_FlipOperands2=Flip ''{0}'' to ''{1}''",
    "# {0} - one of the following Java operands: /, %, -, <<, >>, >>>",
    "FIX_FlipOperands3=Flip operands of ''{0}'' (may alter semantics)",
})
@Hint(displayName = "#DN_FlipOperands", description = "#DESC_FlipOperands", category = "suggestions", hintKind = Kind.ACTION, severity = Severity.HINT)
public class FlipOperands {
    
    private static final Set<Tree.Kind> /*ALMOST*/SAFE_FLIP = EnumSet.of(
            Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO, Tree.Kind.MULTIPLY, Tree.Kind.PLUS,
            Tree.Kind.AND, Tree.Kind.OR, Tree.Kind.XOR, Tree.Kind.CONDITIONAL_AND, Tree.Kind.CONDITIONAL_OR
    );
    private static final Set<Tree.Kind> UNSAFE_FLIP = EnumSet.of(
            Tree.Kind.DIVIDE, Tree.Kind.REMAINDER, Tree.Kind.MINUS, Tree.Kind.LEFT_SHIFT,
            Tree.Kind.RIGHT_SHIFT, Tree.Kind.UNSIGNED_RIGHT_SHIFT
    );
    private static final Map<Tree.Kind, Tree.Kind> CONVERT_FLIP = new HashMap<>();
    private static final Map<Tree.Kind, String> OPERATOR_DN = new HashMap<>();
    
    static {
        CONVERT_FLIP.put(Tree.Kind.LESS_THAN, Tree.Kind.GREATER_THAN_EQUAL);
        CONVERT_FLIP.put(Tree.Kind.GREATER_THAN, Tree.Kind.LESS_THAN_EQUAL);
        CONVERT_FLIP.put(Tree.Kind.GREATER_THAN_EQUAL, Tree.Kind.LESS_THAN);
        CONVERT_FLIP.put(Tree.Kind.LESS_THAN_EQUAL, Tree.Kind.GREATER_THAN);
        
        OPERATOR_DN.put(Tree.Kind.MULTIPLY, "*");
        OPERATOR_DN.put(Tree.Kind.DIVIDE, "/");
        OPERATOR_DN.put(Tree.Kind.REMAINDER, "%");
        OPERATOR_DN.put(Tree.Kind.PLUS, "+");
        OPERATOR_DN.put(Tree.Kind.MINUS, "-");
        OPERATOR_DN.put(Tree.Kind.LEFT_SHIFT, "&lt;&lt;");
        OPERATOR_DN.put(Tree.Kind.RIGHT_SHIFT, "&gt;&gt;");
        OPERATOR_DN.put(Tree.Kind.UNSIGNED_RIGHT_SHIFT, "&gt;&gt;&gt;");
        OPERATOR_DN.put(Tree.Kind.LESS_THAN, "&lt;");
        OPERATOR_DN.put(Tree.Kind.GREATER_THAN, "&gt;");
        OPERATOR_DN.put(Tree.Kind.LESS_THAN_EQUAL, "&lt;=");
        OPERATOR_DN.put(Tree.Kind.GREATER_THAN_EQUAL, "&gt;=");
        OPERATOR_DN.put(Tree.Kind.EQUAL_TO, "==");
        OPERATOR_DN.put(Tree.Kind.NOT_EQUAL_TO, "!=");
        OPERATOR_DN.put(Tree.Kind.AND, "&amp;");
        OPERATOR_DN.put(Tree.Kind.OR, "|");
        OPERATOR_DN.put(Tree.Kind.XOR, "^");
        OPERATOR_DN.put(Tree.Kind.CONDITIONAL_AND, "&amp;&amp;");
        OPERATOR_DN.put(Tree.Kind.CONDITIONAL_OR, "||");
    }
    
    @TriggerTreeKind({
        Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO, Tree.Kind.MULTIPLY, Tree.Kind.PLUS,
        Tree.Kind.AND, Tree.Kind.OR, Tree.Kind.XOR, Tree.Kind.CONDITIONAL_AND, Tree.Kind.CONDITIONAL_OR,
        Tree.Kind.DIVIDE, Tree.Kind.REMAINDER, Tree.Kind.MINUS, Tree.Kind.LEFT_SHIFT,
        Tree.Kind.RIGHT_SHIFT, Tree.Kind.UNSIGNED_RIGHT_SHIFT, Tree.Kind.MULTIPLY,
        Tree.Kind.DIVIDE, Tree.Kind.REMAINDER, Tree.Kind.PLUS, Tree.Kind.MINUS,
        Tree.Kind.LEFT_SHIFT, Tree.Kind.RIGHT_SHIFT, Tree.Kind.UNSIGNED_RIGHT_SHIFT,
        Tree.Kind.LESS_THAN, Tree.Kind.GREATER_THAN, Tree.Kind.LESS_THAN_EQUAL,
        Tree.Kind.GREATER_THAN_EQUAL, Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO,
        Tree.Kind.AND, Tree.Kind.OR, Tree.Kind.XOR, Tree.Kind.CONDITIONAL_AND,
        Tree.Kind.CONDITIONAL_OR,
    })
    public static ErrorDescription equals(HintContext ctx) {
        final BinaryTree bt = (BinaryTree)ctx.getPath().getLeaf();
        Tree.Kind kind = bt.getKind();
        Tree.Kind targetKind;
        String displayName;
        final CompilationInfo ci = ctx.getInfo();
        final boolean unsafe;
        
        if (kind == Tree.Kind.PLUS) {
            // special case: if either of the operands is String, + is not commutative
            TypeMirror leftType = ci.getTrees().getTypeMirror(new TreePath(ctx.getPath(), bt.getLeftOperand()));
            if (Utilities.isJavaString(ci, leftType)) {
                unsafe = true;
            } else {
                TypeMirror rightType = ci.getTrees().getTypeMirror(new TreePath(ctx.getPath(), bt.getLeftOperand()));
                unsafe = Utilities.isJavaString(ci, rightType);
            }
        } else {
            unsafe = false;
        }

        if (unsafe || UNSAFE_FLIP.contains(kind)) {
            displayName = Bundle.FIX_FlipOperands3(OPERATOR_DN.get(kind));
            targetKind = kind;
        } else if (SAFE_FLIP.contains(kind)) {
            displayName = Bundle.FIX_FlipOperands1(OPERATOR_DN.get(kind));
            targetKind = kind;
        } else {
            targetKind = CONVERT_FLIP.get(kind);
            displayName = Bundle.FIX_FlipOperands2(OPERATOR_DN.get(kind), OPERATOR_DN.get(targetKind));
        }
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.DESC_FlipOperands(), new FixImpl(ctx.getInfo(), ctx.getPath(), displayName, targetKind).toEditorFix());
    }
    
    private static final class FixImpl extends JavaFix {

        private final String displayName;
        private final Tree.Kind targetKind;

        public FixImpl(CompilationInfo info, TreePath tp, String displayName, Tree.Kind targetKind) {
            super(info, tp);
            this.displayName = displayName;
            this.targetKind = targetKind;
        }
        
        @Override
        protected String getText() {
            return displayName;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            BinaryTree orig = (BinaryTree) ctx.getPath().getLeaf();
            BinaryTree nue = ctx.getWorkingCopy().getTreeMaker().Binary(targetKind, orig.getRightOperand(), orig.getLeftOperand());
            
            ctx.getWorkingCopy().rewrite(orig, nue);
        }
        
    }
}
