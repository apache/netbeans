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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.completion.Utilities;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 * Implementation of all hints for import statements
 *
 * @author phrebejk
 * @author Max Sauer
 */
public class Imports {
  
    private static final String DEFAULT_PACKAGE = "java.lang"; // NOI18N
    

    @Hint(displayName = "#DN_Imports_STAR", description = "#DESC_Imports_STAR", category="imports", id="Imports_STAR", enabled=false, options=Options.QUERY, suppressWarnings={"", "OnDemandImport"})
    @TriggerTreeKind(Kind.IMPORT)
    public static ErrorDescription starImport(HintContext ctx) {
        ImportTree it = (ImportTree) ctx.getPath().getLeaf();

        if (it.isStatic() || !(it.getQualifiedIdentifier() instanceof MemberSelectTree)) {
            return null; // XXX
        }

        MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();

        if (!"*".equals(ms.getIdentifier().toString())) return null;

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(Imports.class, "DN_Imports_STAR"));
    }

    @Hint(displayName = "#DN_Imports_DEFAULT_PACKAGE", description = "#DESC_Imports_DEFAULT_PACKAGE", category="imports", id="Imports_DEFAULT_PACKAGE", suppressWarnings={"", "JavaLangImport"})
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> defaultImport(HintContext ctx) {
        return importMultiHint(ctx, ImportHintKind.DEFAULT_PACKAGE, getAllImportsOfKind(ctx.getInfo(), ImportHintKind.DEFAULT_PACKAGE));
    }
    
    @Hint(displayName = "#DN_Imports_UNUSED", description = "#DESC_Imports_UNUSED", category="imports", id="Imports_UNUSED", suppressWarnings={"", "UnusedImport", "UNUSED_IMPORT"})
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> unusedImport(HintContext ctx) throws IOException {
        return importMultiHint(ctx, ImportHintKind.UNUSED, UnusedImports.computeUnusedImports(ctx.getInfo()));
    }

    @Hint(displayName = "#DN_Imports_SAME_PACKAGE", description = "#DESC_Imports_SAME_PACKAGE", category="imports", id="Imports_SAME_PACKAGE", suppressWarnings={"", "SamePackageImport"})
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> samePackage(HintContext ctx) throws IOException {
        return importMultiHint(ctx, ImportHintKind.SAME_PACKAGE, getAllImportsOfKind(ctx.getInfo(), ImportHintKind.SAME_PACKAGE));
    }

    private static List<ErrorDescription> importMultiHint(HintContext ctx, ImportHintKind kind, List<TreePathHandle> violatingImports) {
        // Has to be done in order to provide 'remove all' fix
        Fix allFix = null;
        if (ctx.isBulkMode() && !violatingImports.isEmpty()) {
            Fix af = new ImportsFix(violatingImports, kind).toEditorFix();
            TreePath tp = ctx.getPath();
            long pos = Integer.MAX_VALUE;
            
            for (TreePathHandle h : violatingImports) {
                TreePath currentPath = h.resolve(ctx.getInfo());
                assert currentPath != null;
                long currentPos = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(currentPath.getCompilationUnit(), currentPath.getLeaf());
                
                if (currentPos < pos) {
                    tp = currentPath;
                    pos = currentPos;
                }
            }
            
            return Collections.singletonList(ErrorDescriptionFactory.forTree(ctx, tp, NbBundle.getMessage(Imports.class, "DN_Imports_" + kind.toString() + "_Multi", violatingImports.size()), af));
        }
        if (violatingImports.size() > 1) {
            allFix = new ImportsFix(violatingImports, kind).toEditorFix();
        }
        List<ErrorDescription> result = new ArrayList<>(violatingImports.size());
        for (TreePathHandle it : violatingImports) {
            TreePath resolvedIt = it.resolve(ctx.getInfo());
            if (resolvedIt == null) continue; //#204580
            List<Fix> fixes = new ArrayList<>();
            fixes.add(new ImportsFix(Collections.singletonList(it), kind).toEditorFix());
            if (allFix != null) {
                fixes.add(allFix);
            }
            result.add(ErrorDescriptionFactory.forTree(ctx, resolvedIt, NbBundle.getMessage(Imports.class, "DN_Imports_" + kind.toString()), fixes.toArray(new Fix[0])));
        }

        return result;
    }

    @Hint(displayName = "#DN_Imports_EXCLUDED", description = "#DESC_Imports_EXCLUDED", category="imports", id="Imports_EXCLUDED", options=Options.QUERY)
    @TriggerTreeKind(Kind.IMPORT)
    public static ErrorDescription exlucded(HintContext ctx) throws IOException {
        ImportTree it = (ImportTree) ctx.getPath().getLeaf();

        if (it.isStatic() || !(it.getQualifiedIdentifier() instanceof MemberSelectTree)) {
            return null; // XXX
        }

        MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
        String pkg = ms.getExpression().toString();
        String klass = ms.getIdentifier().toString();
        String exp = pkg + "." + (!klass.equals("*") ? klass : ""); //NOI18N
        if (Utilities.isExcluded(exp)) {
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(Imports.class, "DN_Imports_EXCLUDED"));
        }

        return null;
    }

    private static List<TreePathHandle> getAllImportsOfKind(CompilationInfo ci, ImportHintKind kind) {
        //allow only default and samepackage
        assert (kind == ImportHintKind.DEFAULT_PACKAGE || kind == ImportHintKind.SAME_PACKAGE);

        CompilationUnitTree cut = ci.getCompilationUnit();
        TreePath topLevel = new TreePath(cut);
        List<TreePathHandle> result = new ArrayList<>(3);

        List<? extends ImportTree> imports = cut.getImports();
        for (ImportTree it : imports) {
            if (it.getQualifiedIdentifier() instanceof MemberSelectTree) {
                MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
                if (kind == ImportHintKind.DEFAULT_PACKAGE) {
                    if (it.isStatic()) {
                        if (ms.getExpression().toString().equals("java.lang.StringTemplate") && ms.getIdentifier().toString().equals("STR")) {
                            result.add(TreePathHandle.create(new TreePath(topLevel, it), ci));
                        }
                    } else {
                        if ((ms.getExpression().toString().equals(DEFAULT_PACKAGE))) {
                            result.add(TreePathHandle.create(new TreePath(topLevel, it), ci));
                        }
                    }
                }
                if (it.isStatic()) {
                    continue; // XXX
                }
                if (kind == ImportHintKind.SAME_PACKAGE) {
                    ExpressionTree packageName = cut.getPackageName();
                    if (packageName != null &&
                        ms.getExpression().toString().equals(packageName.toString())) {
                        result.add(TreePathHandle.create(new TreePath(topLevel, it), ci));
                    }
                }
            }
        }
        return result;
    }

    // Private methods ---------------------------------------------------------
    
    private static enum ImportHintKind {

        DELEGATE,
        UNUSED,
        DUPLICATE,
        SAME_PACKAGE,
        DEFAULT_PACKAGE,
        EXCLUDED,
        STAR;

        boolean defaultOn() {

            switch (this) {
                case DELEGATE:
                case EXCLUDED:
                case SAME_PACKAGE:
                case DEFAULT_PACKAGE:
                case UNUSED:
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class ImportsFix extends JavaFix {

        List<TreePathHandle> tphList;
        ImportHintKind ihk;
        
        public ImportsFix(List<TreePathHandle> tphList, ImportHintKind ihk) {
            super(tphList.get(0));
            this.tphList = tphList;
            this.ihk = ihk;
        }
        
        @Override
        public String getText() {
            if ( tphList.size() == 1 ) {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_One_" + ihk.toString()); // NOI18N
            }
            else {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_All_" + ihk.toString()); // NOI18N
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            CompilationUnitTree cut = copy.getCompilationUnit();
            
            TreeMaker make = copy.getTreeMaker();
            
            CompilationUnitTree newCut = cut;
            for (TreePathHandle tph : tphList) {
                TreePath path = tph.resolve(copy);
                if ( path != null && path.getLeaf() instanceof ImportTree) {
                     newCut = make.removeCompUnitImport(newCut, (ImportTree)path.getLeaf());
                }
            }
            copy.rewrite(cut, newCut);
                        
        }
                
    }

    
    
}
