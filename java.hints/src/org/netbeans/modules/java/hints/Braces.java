/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class Braces {

    static final EnumSet<JavaTokenId> nonRelevant = EnumSet.<JavaTokenId>of(
            JavaTokenId.LINE_COMMENT, 
            JavaTokenId.BLOCK_COMMENT,
            JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.WHITESPACE
    );
    
    private static final String BRACES_ID = "Braces_"; // NOI18N
    
    @Hint(displayName="#LBL_Braces_For", description="#DSC_Braces_For", category="braces", id=BRACES_ID + "FOR_LOOP", enabled=false, suppressWarnings={"", "ControlFlowStatementWithoutBraces"})
    @TriggerTreeKind({Tree.Kind.FOR_LOOP, Tree.Kind.ENHANCED_FOR_LOOP})
    public static ErrorDescription checkFor(HintContext ctx) {
        StatementTree st;
        
        switch (ctx.getPath().getLeaf().getKind()){
            case FOR_LOOP: st = ((ForLoopTree) ctx.getPath().getLeaf()).getStatement(); break;
            case ENHANCED_FOR_LOOP: st = ((EnhancedForLoopTree) ctx.getPath().getLeaf()).getStatement(); break;
            default:
                throw new IllegalStateException();
        }
        return checkStatement(ctx, "LBL_Braces_For", st, ctx.getPath());
    }
    
    @Hint(displayName="#LBL_Braces_While", description="#DSC_Braces_While", category="braces", id=BRACES_ID + "WHILE_LOOP", enabled=false, suppressWarnings={"", "ControlFlowStatementWithoutBraces"})
    @TriggerTreeKind(Tree.Kind.WHILE_LOOP)
    public static ErrorDescription checkWhile(HintContext ctx) {
        WhileLoopTree wlt = (WhileLoopTree) ctx.getPath().getLeaf();
        return checkStatement(ctx, "LBL_Braces_While", wlt.getStatement(), ctx.getPath());
    }
    
    @Hint(displayName="#LBL_Braces_DoWhile", description="#DSC_Braces_DoWhile", category="braces", id=BRACES_ID + "DO_WHILE_LOOP", enabled=false, suppressWarnings={"", "ControlFlowStatementWithoutBraces"})
    @TriggerTreeKind(Tree.Kind.DO_WHILE_LOOP)
    public static ErrorDescription checkDoWhile(HintContext ctx) {
        DoWhileLoopTree dwlt = (DoWhileLoopTree) ctx.getPath().getLeaf();
        return checkStatement(ctx, "LBL_Braces_DoWhile", dwlt.getStatement(), ctx.getPath());
    }
    
    @Hint(displayName="#LBL_Braces_If", description="#DSC_Braces_If", category="braces", id=BRACES_ID + "IF", enabled=false, suppressWarnings={"", "ControlFlowStatementWithoutBraces"})
    @TriggerTreeKind(Tree.Kind.IF)
    public static List<ErrorDescription> checkIf(HintContext ctx) {
        IfTree it = (IfTree) ctx.getPath().getLeaf();
        return checkifStatements(ctx, "LBL_Braces_If", it.getThenStatement(), it.getElseStatement(), ctx.getPath());
    }
    
    // Private methods ---------------------------------------------------------
    
    private static ErrorDescription checkStatement(HintContext ctx, String dnKey, StatementTree statement, TreePath tp)  {
        if ( statement != null && 
             statement.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             statement.getKind() != Tree.Kind.BLOCK &&
             statement.getKind() != Tree.Kind.ERRONEOUS &&
             !isErroneousExpression( statement )) {
            return ErrorDescriptionFactory.forTree(
                        ctx,
                        statement,
                        NbBundle.getMessage(Braces.class, dnKey),
                        new BracesFix(ctx.getInfo().getFileObject(), TreePathHandle.create(tp, ctx.getInfo())).toEditorFix());
                    
        }        
        return null;
    }
    
    
    private static List<ErrorDescription> checkifStatements(HintContext ctx, String dnKey, StatementTree thenSt, StatementTree elseSt, TreePath tp)  {
        
        boolean fixThen = false;
        boolean fixElse = false;
        
        if ( thenSt != null && 
             thenSt.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             thenSt.getKind() != Tree.Kind.BLOCK &&
             thenSt.getKind() != Tree.Kind.ERRONEOUS &&
             !isErroneousExpression( thenSt )) {
            fixThen = true;
        }
        
        if ( elseSt != null && 
             elseSt.getKind() != Tree.Kind.EMPTY_STATEMENT && 
             elseSt.getKind() != Tree.Kind.BLOCK &&
             elseSt.getKind() != Tree.Kind.ERRONEOUS &&
             elseSt.getKind() != Tree.Kind.IF &&
             !isErroneousExpression( elseSt )) {
            fixElse = true;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        int[] span;
        
        if (fixThen) {
            BracesFix bf  = new BracesFix( ctx.getInfo().getFileObject(), TreePathHandle.create(tp, ctx.getInfo()));
            bf.fixThen = fixThen;
            bf.fixElse = fixElse;
            result.add( ErrorDescriptionFactory.forTree(
                ctx,
                thenSt,
                NbBundle.getMessage(Braces.class, dnKey), 
                bf.toEditorFix()));
        }
        
        if ( fixElse) {
            BracesFix bf  = new BracesFix( ctx.getInfo().getFileObject(), TreePathHandle.create(tp, ctx.getInfo()));
            bf.fixThen = fixThen;
            bf.fixElse = fixElse;
            result.add( ErrorDescriptionFactory.forTree(
                ctx, 
                elseSt,
                NbBundle.getMessage(Braces.class, dnKey), 
                bf.toEditorFix()));
        }
                
        return result;
    }
    
    private static boolean isErroneousExpression(StatementTree statement) {
        if ( statement instanceof ExpressionStatementTree ) {
            if ( ((ExpressionStatementTree)statement).getExpression().getKind() == Kind.ERRONEOUS ) {
                return true;
            }
        }
        return false;
    }

    private static class BracesFix extends JavaFix {

        boolean fixThen;
        boolean fixElse;
        
        public BracesFix(FileObject file, TreePathHandle tph) {
            super(tph);
        }
        
        public String getText() {
            return NbBundle.getMessage(Braces.class, "LBL_Braces_Fix"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            if ( path != null ) {
                
                TreeMaker make = copy.getTreeMaker();
                Tree oldTree = path.getLeaf();                 
                
                oldTree = GeneratorUtilities.get(copy).importComments(oldTree, copy.getCompilationUnit());
                
                switch( oldTree.getKind() ) {
                case FOR_LOOP:
                    ForLoopTree oldFor = (ForLoopTree)oldTree;
                    StatementTree oldBlock = oldFor.getStatement();
                    BlockTree newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree oldEnhancedFor = (EnhancedForLoopTree)oldTree;
                    oldBlock = oldEnhancedFor.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case WHILE_LOOP:
                    WhileLoopTree oldWhile = (WhileLoopTree)oldTree;
                    oldBlock = oldWhile.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case DO_WHILE_LOOP:
                    DoWhileLoopTree oldDoWhile = (DoWhileLoopTree)oldTree;
                    oldBlock = oldDoWhile.getStatement();
                    newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);                    
                    copy.rewrite(oldBlock, newBlock);
                    break;
                case IF:
                    IfTree oldIf = (IfTree)oldTree;
                    if ( fixThen ) {
                        oldBlock = oldIf.getThenStatement();
                        newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                        copy.rewrite(oldBlock, newBlock);
                    }
                    if ( fixElse ) {
                        oldBlock = oldIf.getElseStatement();
                        newBlock = make.Block(Collections.<StatementTree>singletonList(oldBlock), false);
                        copy.rewrite(oldBlock, newBlock);
                    } 
                    
                }
            }
        }
                
    }

    
    
}
