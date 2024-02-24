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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.IfTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 * @author markiewb (refactoring)
 */
public class EmptyStatements {

    private static final String SUPPRESS_WARNINGS_KEY = "empty-statement";
    
    @Hint(displayName = "#LBL_Empty_BLOCK", description = "#DSC_Empty_BLOCK", category = "empty", hintKind = Hint.Kind.INSPECTION, severity = Severity.VERIFIER, suppressWarnings = SUPPRESS_WARNINGS_KEY, id = "EmptyStatements_BLOCK")
    @TriggerTreeKind(Tree.Kind.EMPTY_STATEMENT)
    @NbBundle.Messages({"ERR_EmptyBLOCK=Remove semicolon"})
    public static ErrorDescription forBLOCK(HintContext ctx) {
    
        Tree parent = ctx.getPath().getParentPath().getLeaf();
        if (!EnumSet.of(Kind.BLOCK).contains(parent.getKind())) {
            return null;
    }

        final List<Fix> fixes = new ArrayList<>();
        fixes.add(FixFactory.createSuppressWarningsFix(ctx.getInfo(), ctx.getPath(), SUPPRESS_WARNINGS_KEY));
        fixes.add(JavaFixUtilities.removeFromParent(ctx, Bundle.ERR_EmptyBLOCK(), ctx.getPath()));
    
        return createErrorDescription(ctx, ctx.getPath().getLeaf(), fixes, Kind.BLOCK);
    }
    
    @Hint(displayName = "#LBL_Empty_WHILE_LOOP", description = "#DSC_Empty_WHILE_LOOP", category = "empty", hintKind = Hint.Kind.INSPECTION, severity = Severity.VERIFIER, suppressWarnings = SUPPRESS_WARNINGS_KEY, id = "EmptyStatements_WHILE_LOOP")
    @TriggerTreeKind(Tree.Kind.EMPTY_STATEMENT)
    public static ErrorDescription forWHILE_LOOP(HintContext ctx) {
        final TreePath parentPath = ctx.getPath().getParentPath();
        final Tree parentLeaf = parentPath.getLeaf();
        if (!EnumSet.of(Kind.WHILE_LOOP).contains(parentLeaf.getKind())) {
            return null;
    }
    
        final List<Fix> fixes = new ArrayList<>();
        fixes.add(FixFactory.createSuppressWarningsFix(ctx.getInfo(), parentPath, SUPPRESS_WARNINGS_KEY));
    
        return createErrorDescription(ctx, parentLeaf, fixes, Kind.WHILE_LOOP);
    }
    
    @Hint(displayName = "#LBL_Empty_IF", description = "#DSC_Empty_IF", category = "empty", hintKind = Hint.Kind.INSPECTION, severity = Severity.VERIFIER, suppressWarnings = SUPPRESS_WARNINGS_KEY, id = "EmptyStatements_IF", enabled = false)
    @TriggerTreeKind(Tree.Kind.EMPTY_STATEMENT)
    public static ErrorDescription forIF(HintContext ctx) {
        final TreePath treePath = ctx.getPath();
    
        Tree parent = treePath.getParentPath().getLeaf();        
        if (!EnumSet.of(Kind.IF).contains(parent.getKind())) {
            return null;
        }
        
        TreePath treePathForWarning = treePath;
        IfTree it = (IfTree) parent;
        if (it.getThenStatement() != null
                && it.getThenStatement().getKind() == Tree.Kind.EMPTY_STATEMENT) {
            treePathForWarning = treePath.getParentPath();
                }
        if (it.getElseStatement() != null
                && it.getElseStatement().getKind() == Tree.Kind.EMPTY_STATEMENT) {
            treePathForWarning = treePath;
                }
        
        final List<Fix> fixes = new ArrayList<>();
        fixes.add(FixFactory.createSuppressWarningsFix(ctx.getInfo(), treePathForWarning, SUPPRESS_WARNINGS_KEY));
        
        return createErrorDescription(ctx, parent, fixes, parent.getKind());
    }

    @Hint(displayName = "#LBL_Empty_FOR_LOOP", description = "#DSC_Empty_FOR_LOOP", category = "empty", hintKind = Hint.Kind.INSPECTION, severity = Severity.VERIFIER, suppressWarnings = SUPPRESS_WARNINGS_KEY, id = "EmptyStatements_FOR_LOOP")
    @TriggerTreeKind(Tree.Kind.EMPTY_STATEMENT)
    public static ErrorDescription forFOR_LOOP(HintContext ctx) {
    
        Tree parent = ctx.getPath().getParentPath().getLeaf();
        if (!EnumSet.of(Kind.FOR_LOOP, Kind.ENHANCED_FOR_LOOP).contains(parent.getKind())) {
            return null;
        }

        final List<Fix> fixes = new ArrayList<>();
        fixes.add(FixFactory.createSuppressWarningsFix(ctx.getInfo(), ctx.getPath().getParentPath(), SUPPRESS_WARNINGS_KEY));
    
        return createErrorDescription(ctx, parent, fixes, parent.getKind());
    }
    
    @Hint(displayName = "#LBL_Empty_DO_WHILE_LOOP", description = "#DSC_Empty_DO_WHILE_LOOP", category = "empty", hintKind = Hint.Kind.INSPECTION, severity = Severity.VERIFIER, suppressWarnings = SUPPRESS_WARNINGS_KEY, id = "EmptyStatements_DO_WHILE_LOOP")
    @TriggerTreeKind(Tree.Kind.EMPTY_STATEMENT)
    public static ErrorDescription forDO_WHILE_LOOP(HintContext ctx) {
    
        Tree parent = ctx.getPath().getParentPath().getLeaf();
        if (Kind.DO_WHILE_LOOP != parent.getKind()) {
            return null;
        }

        final List<Fix> fixes = new ArrayList<>();
        fixes.add(FixFactory.createSuppressWarningsFix(ctx.getInfo(), ctx.getPath().getParentPath(), SUPPRESS_WARNINGS_KEY));
            
        return createErrorDescription(ctx, parent, fixes, parent.getKind());
                    }
                    
    /**
     * package private for reuse in unit tests.
             */ 
    static String getDisplayName(@NonNull Kind treeKind) {
        //same pattern as in @Hint(displayName = "#LBL_Empty_XXXX"
        return NbBundle.getMessage(EmptyStatements.class, "LBL_Empty_" + treeKind.toString()); // NOI18N                
        }
        
    private static ErrorDescription createErrorDescription(HintContext ctx, final Tree leaf, final List<Fix> fixes, Kind treeKind) {
        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), leaf);
        int end = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), leaf);
        return org.netbeans.spi.java.hints.ErrorDescriptionFactory.forSpan(ctx, start, end, getDisplayName(treeKind), fixes.toArray(new Fix[0]));
    }
}
