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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.prefs.Preferences;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.WrongStringComparison.WrongStringComparisonCustomizerProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Hint catching comparsion of Strings with <code>==</code> or <code>!=</code>
 * @author phrebejk
 */
@Hint(id="Wrong_String_Comparison", displayName="#LBL_WrongStringComparison", description="#DSC_WrongStringComparison", category="general", customizerProvider=WrongStringComparisonCustomizerProvider.class, suppressWarnings="StringEquality")
public class WrongStringComparison {

            static final String TERNARY_NULL_CHECK = "ternary-null-check"; // NOI18N
            static final String STRING_LITERALS_FIRST = "string-literals-first"; //NOI18N
    private static final String STRING_TYPE = "java.lang.String";  // NOI18N

    @TriggerPatterns({
        @TriggerPattern(value="$left == $right", constraints={@ConstraintVariableType(variable="$left", type=STRING_TYPE),
                                                              @ConstraintVariableType(variable="$right", type=STRING_TYPE)}),
        @TriggerPattern(value="$left != $right", constraints={@ConstraintVariableType(variable="$left", type=STRING_TYPE),
                                                              @ConstraintVariableType(variable="$right", type=STRING_TYPE)})
    })
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo info = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Tree t = treePath.getLeaf();
        
        BinaryTree bt = (BinaryTree) t;
        
        TreePath left = new TreePath(treePath, bt.getLeftOperand() );
        TreePath right = new TreePath(treePath, bt.getRightOperand() );
        
        Trees trees = info.getTrees(); 
        TypeMirror leftType = left == null ? null : trees.getTypeMirror(left);
        TypeMirror rightType = right == null ? null : trees.getTypeMirror(right);

        if ( leftType != null && rightType != null && 
             STRING_TYPE.equals(leftType.toString()) && 
             STRING_TYPE.equals(rightType.toString())) {
            
            if (checkInsideGeneratedEquals(ctx, treePath, left.getLeaf(), right.getLeaf())) {
                return null;
            }

            FileObject file = info.getFileObject();
            TreePathHandle tph = TreePathHandle.create(treePath, info);
            ArrayList<Fix> fixes = new ArrayList<Fix>();
            boolean reverseOperands = false;
            if (bt.getLeftOperand().getKind() != Tree.Kind.STRING_LITERAL) {
                if (bt.getRightOperand().getKind() == Tree.Kind.STRING_LITERAL) {
                    if (getStringLiteralsFirst(ctx.getPreferences())) {
                        reverseOperands = true;
                    } else {
                        fixes.add(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.NULL_CHECK).toEditorFix());
                    }
                } else {
                    fixes.add(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.ternaryNullCheck(getTernaryNullCheck(ctx.getPreferences()))).toEditorFix());
                }
            }
            fixes.add(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.reverseOperands(reverseOperands)).toEditorFix());
            return ErrorDescriptionFactory.forTree(
                      ctx,
                      t,
                      NbBundle.getMessage(WrongStringComparison.class, "LBL_WrongStringComparison"), 
                      fixes.toArray(new Fix[0]));

        }
        
        return null;
    }

    private static boolean checkInsideGeneratedEquals(HintContext ctx, TreePath treePath, Tree left, Tree right) {
        CompilationInfo info = ctx.getInfo();
        TreePath sourcePathParent = treePath.getParentPath();

        if (sourcePathParent.getLeaf().getKind() != Kind.CONDITIONAL_AND) { //performance
            return false;
        }
        
        SourcePositions sp = info.getTrees().getSourcePositions();
        Scope s = info.getTrees().getScope(sourcePathParent);
        
        String leftText = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), left), (int) sp.getEndPosition(info.getCompilationUnit(), left) + 1);
        String rightText = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), right), (int) sp.getEndPosition(info.getCompilationUnit(), right) + 1);
        String code = leftText + " != " + rightText + " && (" + leftText + "== null || !" + leftText + ".equals(" + rightText + "))"; // NOI18N
        ExpressionTree correct = info.getTreeUtilities().parseExpression(code, new SourcePositions[1]);

        info.getTreeUtilities().attributeTree(correct, s);

        TreePath correctPath = new TreePath(sourcePathParent.getParentPath(), correct);
        
        String originalCode = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), sourcePathParent.getLeaf()), (int) sp.getEndPosition(info.getCompilationUnit(), sourcePathParent.getLeaf()) + 1);
        ExpressionTree original = info.getTreeUtilities().parseExpression(originalCode, new SourcePositions[1]);
        
        info.getTreeUtilities().attributeTree(original, s);

        TreePath originalPath = new TreePath(sourcePathParent.getParentPath(), original);

        return Matcher.create(info)./*XXX: setCancel(cancel).*/setSearchRoot(originalPath).setTreeTopSearch().match(Pattern.createSimplePattern(correctPath)).iterator().hasNext();
    }

    static boolean getTernaryNullCheck(Preferences p) {
        return p.getBoolean(TERNARY_NULL_CHECK, true);
    }

    static boolean getStringLiteralsFirst(Preferences p) {
        return p.getBoolean(STRING_LITERALS_FIRST, true);
    }

    static void setTernaryNullCheck(Preferences p, boolean selected) {
        p.putBoolean(TERNARY_NULL_CHECK, selected);
    }

    static void setStringLiteralsFirst(Preferences p, boolean selected) {
        p.putBoolean(STRING_LITERALS_FIRST, selected);
    }

    static class WrongStringComparisonFix extends JavaFix {

        protected final Kind kind;

        public WrongStringComparisonFix(TreePathHandle tph, Kind kind) {
            super(tph);
            this.kind = kind;
        }

        public String getText() {
            switch (kind) {
                case REVERSE_OPERANDS:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_ReverseOperands"); // NOI18N
                case NO_NULL_CHECK:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_NoNullCheck"); // NOI18N
                case NULL_CHECK_TERNARY:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_TernaryNullCheck"); // NOI18N
                default:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_NullCheck"); // NOI18N
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            if (path != null) {
                TreeMaker make = copy.getTreeMaker();
                BinaryTree oldTree = (BinaryTree) path.getLeaf();
                ExpressionTree left = oldTree.getLeftOperand();
                ExpressionTree right = oldTree.getRightOperand();
                ExpressionTree newTree;
                if (kind == Kind.REVERSE_OPERANDS) {
                    // "str2".equals(str1)
                    ExpressionTree rightEquals = make.MemberSelect(right, "equals"); // NOI18N
                    ExpressionTree rightEqualsLeft = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), rightEquals, Collections.singletonList(left));
                    rightEqualsLeft = matchSign(make, oldTree, rightEqualsLeft);
                    newTree = rightEqualsLeft;
                } else {
                    ExpressionTree leftEquals = make.MemberSelect(left, "equals"); // NOI18N
                    ExpressionTree leftEqualsRight = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), leftEquals, Collections.singletonList(right));
                    leftEqualsRight = matchSign(make, oldTree, leftEqualsRight);
                    if (kind == Kind.NO_NULL_CHECK) {
                        // str1.equals(str2)
                        newTree = leftEqualsRight;
                    } else {
                        ExpressionTree leftEqNull  = make.Binary(Tree.Kind.EQUAL_TO, left, make.Identifier("null")); // NOI18N
                        ExpressionTree rightEqNull = make.Binary(oldTree.getKind(), right, make.Identifier("null")); // NOI18N
                        if (kind == Kind.NULL_CHECK_TERNARY) {
                            // str1 == null ? str2 == null : str1.equals(str2)
                            newTree = make.ConditionalExpression(leftEqNull, rightEqNull, leftEqualsRight);
                        } else {
                            ExpressionTree leftNeNull = make.Binary(Tree.Kind.NOT_EQUAL_TO, left, make.Identifier("null")); // NOI18N
                            ExpressionTree leftNeNullAndLeftEqualsRight = make.Binary(Tree.Kind.CONDITIONAL_AND, leftNeNull, leftEqualsRight);
                            if (right.getKind() == Tree.Kind.STRING_LITERAL) {
                                // str1 != null && str1.equals("str2")
                                newTree = leftNeNullAndLeftEqualsRight;
                            } else {
                                // (str1 == null && str2 == null) || (str1 != null && str1.equals(str2))
                                ExpressionTree leftEqNullAndRightEqNull  = make.Binary(Tree.Kind.CONDITIONAL_AND, leftEqNull, rightEqNull);
                                newTree = make.Binary(Tree.Kind.CONDITIONAL_OR, make.Parenthesized(leftEqNullAndRightEqNull), make.Parenthesized(leftNeNullAndLeftEqualsRight));
                            }
                        }
                        if (path.getParentPath().getLeaf().getKind() != Tree.Kind.PARENTHESIZED) {
                            newTree = make.Parenthesized(newTree);
                        }
                    }
                }
                copy.rewrite(oldTree, newTree);
            }
        }

        ExpressionTree matchSign(TreeMaker make, BinaryTree oldTree, ExpressionTree et) {
            if (oldTree.getKind() == Tree.Kind.NOT_EQUAL_TO) {
                return make.Unary(Tree.Kind.LOGICAL_COMPLEMENT, et);
            } else {
                return et;
            }
        }

        enum Kind {
            REVERSE_OPERANDS,
            NO_NULL_CHECK,
            NULL_CHECK,
            NULL_CHECK_TERNARY;

            public static Kind ternaryNullCheck(boolean ternary) {
                return ternary ? NULL_CHECK_TERNARY : NULL_CHECK;
            }

            public static Kind reverseOperands(boolean reverseOperands) {
                return reverseOperands ? REVERSE_OPERANDS : NO_NULL_CHECK;
            }

        }

    }

    public static final class WrongStringComparisonCustomizerProvider implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            JComponent customizer = new WrongStringComparisonCustomizer(prefs);
            prefs.putBoolean(STRING_LITERALS_FIRST, getStringLiteralsFirst(prefs));
            prefs.putBoolean(TERNARY_NULL_CHECK, getTernaryNullCheck(prefs));
            return customizer;
        }
        
    }
}
