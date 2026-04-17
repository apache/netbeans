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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.openide.util.NbBundle;
import static org.netbeans.modules.java.hints.errors.Utilities.isEnhancedForLoopIdentifier;
import org.netbeans.spi.java.hints.JavaFix;


/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFix extends JavaFix {
    
    private FileObject file;
    private TypeMirrorHandle type;
    private String name;
    private ElementKind kind;
    
    public AddParameterOrLocalFix(CompilationInfo info,
                                  TypeMirror type, String name,
                                  ElementKind kind,
                                  int /*!!!Position*/ unresolvedVariable) {
        super(info, info.getTreeUtilities().pathFor(unresolvedVariable + 1), getSortText(kind, name));
        this.file = info.getFileObject();
        if (type.getKind() == TypeKind.NULL || type.getKind() == TypeKind.NONE) {
            TypeElement te = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
            if (te != null) {
                type = te.asType();
                this.type = TypeMirrorHandle.create(type);
            } else {
                this.type = null;
            }
        } else {
            this.type = TypeMirrorHandle.create(type);
        }
        this.name = name;
        this.kind = kind;
    }

    public String getText() {
        switch (kind) {
            case LOCAL_VARIABLE: return NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Local_Variable", name); // NOI18N
            case PARAMETER: return NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Parameter", name); // NOI18N
            case RESOURCE_VARIABLE: return NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Resource", name); // NOI18N
            case OTHER: return NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_For_Init_Variable", name); // NOI18N
            default:
                throw new IllegalStateException(kind.name());
        }
    }

    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        WorkingCopy working = ctx.getWorkingCopy();
        TypeMirror proposedType = type.resolve(working);

        if (proposedType == null) {
            ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
            return;
        }

        TreeMaker make = working.getTreeMaker();

        //TreePath tp = working.getTreeUtilities().pathFor(unresolvedVariable + 1);
        //Use TreePathHandle instead of position supplied as field (#143318)
        TreePath tp = ctx.getPath();
        if (tp == null || tp.getLeaf().getKind() != Kind.IDENTIFIER)
            return;

        switch (kind) {
            case PARAMETER:
                TreePath targetPath = findMethod(tp);

                if (targetPath == null) {
                    Logger.getLogger("global").log(Level.WARNING, "Add parameter - cannot find the method."); // NOI18N
                    return;
                }

                MethodTree targetTree = (MethodTree) targetPath.getLeaf();

                Element el = working.getTrees().getElement(targetPath);
                if (el == null) {
                    return;
                }
                int index = targetTree.getParameters().size();

                if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                    ExecutableElement ee = (ExecutableElement) el;

                    if (ee.isVarArgs()) {
                        index = ee.getParameters().size() - 1;
                    }
                }

                MethodTree result = make.insertMethodParameter(targetTree, index, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null));

                working.rewrite(targetTree, result);
                break;
            case LOCAL_VARIABLE:
                if (ErrorFixesFakeHint.isCreateLocalVariableInPlace(ErrorFixesFakeHint.getPreferences(working.getFileObject(), FixKind.CREATE_LOCAL_VARIABLE)) || isEnhancedForLoopIdentifier(tp)) {
                    resolveLocalVariable(working, tp, make, proposedType);
                } else {
                    resolveLocalVariable55(working, tp, make, proposedType);
                }
                break;
            case RESOURCE_VARIABLE:
                resolveResourceVariable(working, tp, make, proposedType);
                break;
            case OTHER:
                resolveForInitVariable(working, tp, make, proposedType);
                break;
            default:
                throw new IllegalStateException(kind.name());
        }
    }

    /** In case statement is an Assignment, replace it with variable declaration */
    private boolean initExpression(StatementTree statement, TreeMaker make, final String name, TypeMirror proposedType, final WorkingCopy wc, TreePath tp) {
        ExpressionTree exp = ((ExpressionStatementTree) statement).getExpression();
        if (exp.getKind() == Kind.ASSIGNMENT) {
            AssignmentTree at = (AssignmentTree) exp;
            if (at.getVariable().getKind() == Kind.IDENTIFIER && ((IdentifierTree) at.getVariable()).getName().contentEquals(name)) {
                //replace the expression statement with a variable declaration:
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), at.getExpression());
                vt = Utilities.copyComments(wc, statement, vt);
                wc.rewrite(statement, vt);
                return true;
            }
        }
        return false;
    }

    private static boolean isError(CompilationInfo info, Element el) {
        if (el == null) return true;

        if (!el.getKind().isClass()) return false;//TODO: currently holds, but is not guaranteed?

        return el.asType() == null || el.asType().getKind() == TypeKind.ERROR;
    }

    private static boolean isEnhancedForLoopVariable(TreePath tp) {
        if (tp.getLeaf().getKind() != Kind.VARIABLE)
            return false;
        TreePath context = tp.getParentPath();
        if (context == null || context.getLeaf().getKind() != Kind.ENHANCED_FOR_LOOP)
            return false;
        return true;
    }

    private void resolveLocalVariable55(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();
        TreePath blockPath = findOutmostBlock(tp);

        if (blockPath == null) {
            return;
        }
        
        int index = 0;
        BlockTree block = ((BlockTree) blockPath.getLeaf());
        
        TreePath method = findMethod(tp);

        if (method != null && ((MethodTree) method.getLeaf()).getReturnType() == null && !block.getStatements().isEmpty()) {
            StatementTree stat = block.getStatements().get(0);
            
            if (stat.getKind() == Kind.EXPRESSION_STATEMENT) {
                Element thisMethodEl = wc.getTrees().getElement(method);
                TreePath pathToFirst = new TreePath(new TreePath(new TreePath(method, block), stat), ((ExpressionStatementTree) stat).getExpression());
                Element superCall = wc.getTrees().getElement(pathToFirst);

                if (thisMethodEl != null && superCall != null && thisMethodEl.getKind() == ElementKind.CONSTRUCTOR && superCall.getKind() == ElementKind.CONSTRUCTOR) {
                    index = 1;
                }
            }
        }
        
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null);
        
        wc.rewrite(block, wc.getTreeMaker().insertBlockStatement(block, index, vt));
    }

    private static final Set<Kind> CAN_HOLD_VARIABLE = EnumSet.of(Kind.BLOCK, Kind.CASE);
    
    private void resolveLocalVariable(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();
        
        final Element el = wc.getTrees().getElement(tp);
        if (el == null) {
            return;
        }
        
        //find first usage of this (undeclared) variable:
        TreePath blockPath = findOutmostBlock(tp);
        
        if (blockPath == null) {
            //?
            return;
        }
        
        class FirstUsage extends ErrorAwareTreePathScanner<TreePath, Void> {
            public @Override TreePath visitIdentifier(IdentifierTree tree, Void v) {
                if (tree.getName().contentEquals(el.getSimpleName()) && isError(wc, wc.getTrees().getElement(getCurrentPath()))) {
                    return findStatement(getCurrentPath());
                }
                return null;
            }
            public @Override TreePath visitBlock(BlockTree tree, Void v) {
                TreePath result = null;
                TreePath firstBranchStatementWithUsage = null;
                for (StatementTree t : tree.getStatements()) {
                    TreePath currentResult = scan(t, null);

                    if (currentResult == null) continue;

                    if (result == null) {
                        result = currentResult;
                        firstBranchStatementWithUsage = new TreePath(getCurrentPath(), t);
                    } else {
                        //ie.: { x = 1; } ... { x = 1; }
                        result = firstBranchStatementWithUsage;
                    }
                }
                super.visitBlock(tree, v);
                return result;
            }
            public @Override TreePath reduce(TreePath tp1, TreePath tp2) {
                if (tp2 == null)
                    return tp1;
                
                return tp2;
                
            }
        }
        
        FirstUsage firstUsage  = new FirstUsage();
        TreePath firstUse = firstUsage.scan(blockPath, null);
        
        if (firstUse == null || !isStatement(firstUse.getLeaf())) {
            Logger.getLogger("global").log(Level.WARNING, "Add local variable - cannot find a statement."); // NOI18N
            return;
        }

        while (firstUse.getParentPath() != null && !CAN_HOLD_VARIABLE.contains(firstUse.getParentPath().getLeaf().getKind())) {
            firstUse = firstUse.getParentPath();
        }

        if (firstUse.getParentPath() == null) {
            Logger.getLogger("global").log(Level.WARNING, "Add local variable - cannot find a statement."); // NOI18N
            return;
        }

        StatementTree statement = (StatementTree) firstUse.getLeaf();

        if (statement.getKind() == Kind.EXPRESSION_STATEMENT) {
            if (initExpression(statement, make, name, proposedType, wc, tp)) {
                return;
            }
        }

        Tree statementParent = firstUse.getParentPath().getLeaf();
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null);

        if (isEnhancedForLoopIdentifier(tp)) {
            wc.rewrite(tp.getParentPath().getLeaf(), vt);
        } else if (isEnhancedForLoopVariable(firstUse)) {
            wc.rewrite(firstUse.getLeaf(), vt);
        } else if (statementParent.getKind() == Kind.BLOCK) {
            BlockTree block = (BlockTree) statementParent;

            FirstUsage fu = new FirstUsage();
            TreePath result = fu.scan(firstUse, null);
            if (result == null|| !isStatement(result.getLeaf())) {
                Logger.getLogger("global").log(Level.WARNING, "Add local variable - cannot find a statement inside nested block"); // NOI18N
                return;
            }
            Tree resultLeaf = result.getLeaf();
            //if resultLeaf is an Expression && Parent is a block (not an unperenthisized if, while... treee
            if (resultLeaf.getKind() == Kind.EXPRESSION_STATEMENT && result.getParentPath().getLeaf().getKind() == Kind.BLOCK) {
                //init the expression if first use and the error where hint was
                //invoked from the same block
                if (findBlock(result).getLeaf().equals(findBlock(tp).getLeaf())) {
                    if (initExpression((StatementTree) result.getLeaf(), make, name, proposedType, wc, tp)) {
                        return;
                    }
                }

                //not first use, but result is not a parent and vice versa
                if (!isParent(result, tp) && !isParent(tp, result)) {
                    if (initExpression((StatementTree) tp.getParentPath().getParentPath().getLeaf(), make, name, proposedType, wc, tp)) {
                        return;
                    }
                }

            }

            //there in an incomplete ENHACED_FOR_LOOP as a parent, see testEnhancedForLoopInsideItsBody
            if (result.getParentPath().getLeaf().getKind() == Kind.ENHANCED_FOR_LOOP && resultLeaf.getKind() == Kind.VARIABLE) {
                wc.rewrite(resultLeaf, vt);
                return;
            }

            BlockTree nueBlock = make.insertBlockStatement(block, block.getStatements().indexOf(statement), vt);

            wc.rewrite(block, nueBlock);
        } else {
            BlockTree block = make.Block(Arrays.asList(vt, statement), false);
            
            wc.rewrite(statement, block);
        }
    }
    
    private void resolveResourceVariable(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();

        final Element el = wc.getTrees().getElement(tp);
        if (el == null) {
            return;
        }

        if (tp.getParentPath().getLeaf().getKind() != Kind.ASSIGNMENT) {
            //?
            return ;
        }

        AssignmentTree at = (AssignmentTree) tp.getParentPath().getLeaf();
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), at.getExpression());

        wc.rewrite(at, vt);
    }

    private void resolveForInitVariable(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();

        final Element el = wc.getTrees().getElement(tp);
        if (el == null) {
            return;
        }

        if (tp.getParentPath().getLeaf().getKind() != Kind.ASSIGNMENT ||
            tp.getParentPath().getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT) {
            //?
            return ;
        }

        AssignmentTree at = (AssignmentTree) tp.getParentPath().getLeaf();
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), at.getExpression());

        wc.rewrite(tp.getParentPath().getParentPath().getLeaf(), vt);
    }

    private TreePath findStatement(TreePath tp) {
        TreePath statement = tp;
        
        while (statement.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            if (isStatement(statement.getLeaf())) {
                return statement;
            }
            
            statement = statement.getParentPath();
        }
        
        return null;
    }
    
    private TreePath findMethod(TreePath tp) {
        TreePath method = tp;
        
        while (method != null) {
            if (method.getLeaf().getKind() == Kind.METHOD) {
                return method;
            }
            
            method = method.getParentPath();
        }
        
        return null;
    }

    private TreePath findOutmostBlock(TreePath tp) {
        TreePath block = null;

        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            if (tp.getLeaf().getKind() == Kind.BLOCK) {
                block = tp;
            }

            tp = tp.getParentPath();
        }

        return block;
    }

    private boolean isParent(TreePath parent, TreePath son) {
        TreePath parentBlock = findBlock(parent);
        TreePath block = son;

        while (block != null) {
            if (block.getLeaf().getKind() == Kind.BLOCK) {
                if (block.getLeaf().equals(parentBlock.getLeaf())) {
                    return true;
                }
            }
            block = block.getParentPath();
        }

        return false;
    }

    private TreePath findBlock(TreePath tp) {
        TreePath block = tp;

        while (block != null) {
            if (block.getLeaf().getKind() == Kind.BLOCK) {
                return block;
            }

            block = block.getParentPath();
        }

        return null;
    }

    private boolean isStatement(Tree t) {
        Class intClass = t.getKind().asInterface();
        
        return StatementTree.class.isAssignableFrom(intClass);
    }
    
    String toDebugString(CompilationInfo info) {
        return "AddParameterOrLocalFix:" + name + ":" + type.resolve(info).toString() + ":" + kind.name(); // NOI18N
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AddParameterOrLocalFix other = (AddParameterOrLocalFix) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        return hash;
    }
    
    private static String getSortText(ElementKind kind, String name) {
        //see usage at org.netbeans.modules.editor.hints.FixData.getSortText(org.netbeans.spi.editor.hints.Fix):java.lang.CharSequence
    
        //creates ordering top to bottom: create resource>local variable>create field>create parameter
        //see org.netbeans.modules.java.hints.errors.CreateFieldFix.getSortText():java.lang.CharSequence
        switch (kind) {
            case PARAMETER: return "Create 7000 " + name;
            case LOCAL_VARIABLE: return "Create 5000 " + name;
            case RESOURCE_VARIABLE: return "Create 3000 " + name;
            case OTHER: return "Create 3000 " + name;
            default:
                throw new IllegalStateException();
        }
    }
    
}
