/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
final class ScanStatement extends TreePathScanner<Void, Void> {
    private static final int PHASE_BEFORE_SELECTION = 1;
    private static final int PHASE_INSIDE_SELECTION = 2;
    private static final int PHASE_AFTER_SELECTION = 3;
    private final CompilationInfo info;
    private int phase = PHASE_BEFORE_SELECTION;
    private final Tree firstInSelection;
    private final Tree lastInSelection;
    final Set<VariableElement> localVariables = new HashSet<VariableElement>();
    final Map<VariableElement, Boolean> usedLocalVariables = new LinkedHashMap<VariableElement, Boolean>(); /*true if all uses have been definitelly assigned inside selection*/
    final Set<VariableElement> selectionLocalVariables = new HashSet<VariableElement>();
    final Map<VariableElement, Boolean> usedAfterSelection = new LinkedHashMap<VariableElement, Boolean>(); /*true if all uses have been definitelly assigned inside selection*/
    final Set<TreePath> selectionExits = new HashSet<TreePath>();
    private final Set<Tree> treesSeensInSelection = new HashSet<Tree>();
    private final Map<TypeMirror, TreePathHandle> typeVar2Def;
    private final Map<Tree, Iterable<? extends TreePath>> assignmentsForUse;
    final Set<TreePathHandle> usedTypeVariables = new HashSet<TreePathHandle>();
    boolean hasReturns = false;
    private boolean hasBreaks = false;
    private boolean hasContinues = false;
    private boolean secondPass = false;
    private boolean stopSecondPass = false;
    private final AtomicBoolean cancel;
    
    /**
     * Nesting level for local classes and lambdas. Ignore returns in nested scopes
     */
    private int nesting;

    public ScanStatement(CompilationInfo info, Tree firstInSelection, Tree lastInSelection, Map<TypeMirror, TreePathHandle> typeVar2Def, Map<Tree, Iterable<? extends TreePath>> assignmentsForUse, AtomicBoolean cancel) {
        this.info = info;
        this.firstInSelection = firstInSelection;
        this.lastInSelection = lastInSelection;
        this.typeVar2Def = typeVar2Def;
        this.assignmentsForUse = assignmentsForUse;
        this.cancel = cancel;
    }

    @Override
    public Void scan(Tree tree, Void p) {
        if (stopSecondPass) {
            return null;
        }
        if (phase != PHASE_AFTER_SELECTION) {
            if (tree == firstInSelection) {
                phase = PHASE_INSIDE_SELECTION;
            }
            if (phase == PHASE_INSIDE_SELECTION) {
                treesSeensInSelection.add(tree);
            }
        }
        if (secondPass && tree == firstInSelection) {
            stopSecondPass = true;
            return null;
        }
        super.scan(tree, p);
        if (tree == lastInSelection) {
            phase = PHASE_AFTER_SELECTION;
        }
        return null;
    }

    @Override
    public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
        nesting++;
        super.visitLambdaExpression(node, p);
        nesting--;
        return null;
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        nesting++;
        super.visitNewClass(node, p);
        nesting--;
        return null;
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        nesting++;
        super.visitClass(node, p);
        nesting--;
        return null;
    }
    
    

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        Element e = info.getTrees().getElement(getCurrentPath());
        if (e != null && IntroduceHint.LOCAL_VARIABLES.contains(e.getKind())) {
            switch (phase) {
                case PHASE_BEFORE_SELECTION:
                    localVariables.add((VariableElement) e);
                    break;
                case PHASE_INSIDE_SELECTION:
                    selectionLocalVariables.add((VariableElement) e);
                    break;
            }
        }
        return super.visitVariable(node, p);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        Element e = info.getTrees().getElement(getCurrentPath());
        if (e != null) {
            if (IntroduceHint.LOCAL_VARIABLES.contains(e.getKind())) {
                switch (phase) {
                    case PHASE_INSIDE_SELECTION:
                        if (localVariables.contains(e) && usedLocalVariables.get(e) == null) {
                            Iterable<? extends TreePath> writes = assignmentsForUse.get(getCurrentPath().getLeaf());
                            Boolean definitellyAssignedInSelection = true;
                            if (writes != null) {
                                for (TreePath w : writes) {
                                    if (w == null || !treesSeensInSelection.contains(w.getLeaf())) {
                                        definitellyAssignedInSelection = false;
                                        break;
                                    }
                                }
                            } else if (getCurrentPath().getParentPath().getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                                definitellyAssignedInSelection = null;
                            } else {
                                definitellyAssignedInSelection = false;
                            }
                            usedLocalVariables.put((VariableElement) e, definitellyAssignedInSelection);
                        }
                        break;
                    case PHASE_AFTER_SELECTION:
                        Iterable<? extends TreePath> writes = assignmentsForUse.get(getCurrentPath().getLeaf());
                        boolean assignedInSelection = false;
                        boolean definitellyAssignedInSelection = true;
                        if (writes != null) {
                            for (TreePath w : writes) {
                                if (w != null && treesSeensInSelection.contains(w.getLeaf())) {
                                    assignedInSelection = true;
                                }
                                if (w == null || !treesSeensInSelection.contains(w.getLeaf())) {
                                    definitellyAssignedInSelection = false;
                                }
                            }
                        }
                        if (assignedInSelection) {
                            usedAfterSelection.put((VariableElement) e, definitellyAssignedInSelection);
                        }
                        break;
                }
            }
        }
        if (phase == PHASE_INSIDE_SELECTION) {
            TypeMirror type = info.getTrees().getTypeMirror(getCurrentPath());
            if (type != null) {
                TreePathHandle def = typeVar2Def.get(type);
                usedTypeVariables.add(def);
            }
        }
        return super.visitIdentifier(node, p);
    }
    
    public Collection<TreePathHandle> getTypeVarDefs() {
        return typeVar2Def.values();
    }
    
    public List<TreePathHandle> getUsedTypeVars() {
        List<TreePathHandle> ll = new ArrayList<TreePathHandle>(getTypeVarDefs());
        ll.retainAll(usedTypeVariables);
        return ll;
    }
    
    private boolean isMethodCode() {
        return nesting == 0;
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        if (isMethodCode() && phase == PHASE_INSIDE_SELECTION) {
            selectionExits.add(getCurrentPath());
            hasReturns = true;
        }
        return super.visitReturn(node, p);
    }

    @Override
    public Void visitBreak(BreakTree node, Void p) {
        if (isMethodCode() && phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
            selectionExits.add(getCurrentPath());
            hasBreaks = true;
        }
        return super.visitBreak(node, p);
    }

    @Override
    public Void visitContinue(ContinueTree node, Void p) {
        if (isMethodCode() && phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()))) {
            selectionExits.add(getCurrentPath());
            hasContinues = true;
        }
        return super.visitContinue(node, p);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        super.visitWhileLoop(node, p);
        if (isMethodCode() && phase == PHASE_AFTER_SELECTION) {
            //#109663&#112552:
            //the selection was inside the while-loop, the variables inside the
            //condition&statement of the while loop need to be considered to be used again after the loop:
            if (!secondPass) {
                secondPass = true;
                scan(node.getCondition(), p);
                scan(node.getStatement(), p);
                secondPass = false;
                stopSecondPass = false;
            }
        }
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        super.visitForLoop(node, p);
        if (isMethodCode() && phase == PHASE_AFTER_SELECTION) {
            //#109663&#112552:
            //the selection was inside the for-loop, the variables inside the
            //condition, update and statement parts of the for loop need to be considered to be used again after the loop:
            if (!secondPass) {
                secondPass = true;
                scan(node.getCondition(), p);
                scan(node.getUpdate(), p);
                scan(node.getStatement(), p);
                secondPass = false;
                stopSecondPass = false;
            }
        }
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        super.visitDoWhileLoop(node, p);
        if (isMethodCode() && phase == PHASE_AFTER_SELECTION) {
            //#109663&#112552:
            //the selection was inside the do-while, the variables inside the
            //statement part of the do-while loop need to be considered to be used again after the loop:
            if (!secondPass) {
                secondPass = true;
                scan(node.getStatement(), p);
                secondPass = false;
                stopSecondPass = false;
            }
        }
        return null;
    }

    String verifyExits(boolean exitsFromAllBranches) {
        int i = 0;
        i += hasReturns ? 1 : 0;
        i += hasBreaks ? 1 : 0;
        i += hasContinues ? 1 : 0;
        if (i > 1) {
            return "ERR_Too_Many_Different_Exits"; // NOI18N
        }
        if ((exitsFromAllBranches ? 0 : i) + usedAfterSelection.size() > 1) {
            return "ERR_Too_Many_Return_Values"; // NOI18N
        }
        StatementTree breakOrContinueTarget = null;
        boolean returnValueComputed = false;
        TreePath returnValue = null;
        for (TreePath tp : selectionExits) {
            if (tp.getLeaf().getKind() == Tree.Kind.RETURN) {
                if (!exitsFromAllBranches) {
                    ReturnTree rt = (ReturnTree) tp.getLeaf();
                    TreePath currentReturnValue = rt.getExpression() != null ? new TreePath(tp, rt.getExpression()) : null;
                    if (!returnValueComputed) {
                        returnValue = currentReturnValue;
                        returnValueComputed = true;
                    } else {
                        if (returnValue != null && currentReturnValue != null) {
                            Set<TreePath> candidates = SourceUtils.computeDuplicates(info, returnValue, currentReturnValue, cancel);
                            if (candidates.size() != 1 || candidates.iterator().next().getLeaf() != rt.getExpression()) {
                                return "ERR_Different_Return_Values"; // NOI18N
                            }
                        } else {
                            if (returnValue != currentReturnValue) {
                                return "ERR_Different_Return_Values"; // NOI18N
                            }
                        }
                    }
                }
            } else {
                StatementTree target = info.getTreeUtilities().getBreakContinueTarget(tp);
                if (breakOrContinueTarget == null) {
                    breakOrContinueTarget = target;
                }
                if (breakOrContinueTarget != target) {
                    return "ERR_Break_Mismatch"; // NOI18N
                }
            }
        }
        return null;
    }
    
}
