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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Name;

import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.OrganizeImports", description = "#DESC_org.netbeans.modules.java.hints.OrganizeImports", category = "imports", enabled = false)
public class OrganizeImports {
    
    private static final String ERROR_CODE = "compiler.err.expected"; // NOI18N

    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static ErrorDescription checkImports(final HintContext context) {
        Source source = context.getInfo().getSnapshot().getSource();
        ModificationResult result = null;
        try {
            result = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                    copy.toPhase(Phase.RESOLVED);
                    doOrganizeImports(copy, context.isBulkMode());
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        List<? extends Difference> diffs = result != null ? result.getDifferences(source.getFileObject()) : null;
        if (diffs != null && !diffs.isEmpty()) {
            Fix fix = new OrganizeImportsFix(context.getInfo(), context.getPath(), context.isBulkMode()).toEditorFix();
            SourcePositions sp = context.getInfo().getTrees().getSourcePositions();
            int offset = diffs.get(0).getStartPosition().getOffset();
            CompilationUnitTree cu = context.getInfo().getCompilationUnit();
            for (ImportTree imp : cu.getImports()) {
                if (sp.getEndPosition(cu, imp) >= offset)
                    return ErrorDescriptionFactory.forTree(context, imp, NbBundle.getMessage(OrganizeImports.class, "MSG_OragnizeImports"), fix); //NOI18N
            }
            return ErrorDescriptionFactory.forTree(context, context.getInfo().getCompilationUnit().getImports().get(0), NbBundle.getMessage(OrganizeImports.class, "MSG_OragnizeImports"), fix); //NOI18N
        }
        return null;
    }
    
    private static void doOrganizeImports(WorkingCopy copy, boolean isBulkMode) throws IllegalStateException {
        doOrganizeImports(copy, null, isBulkMode);
    }

    /**
     * Performs 'organize imports' in two modes. If 'addImports' is null, it optimizes imports according to coding style.
     * However if addImports is not null && not empty, it will add the elements in addImports, and reorder the set
     * of import statements, but will not remove unused imports.
     * Combination of bulk + addImports is not tested.
     * 
     * @param copy working copy to change
     * @param addImports if not null, just adds and reorders imports. If null, performs optimization
     * @param isBulkMode called from batch processing
     * @throws IllegalStateException 
     */
    public static void doOrganizeImports(WorkingCopy copy, Set<Element> addImports, boolean isBulkMode) throws IllegalStateException {
        CompilationUnitTree cu = copy.getCompilationUnit();
        List<? extends ImportTree> imports = cu.getImports();
        if (imports.isEmpty()) {
            if (addImports == null) {
                return;
            }
        } else if (addImports == null) {
            // check diag code only if in the 'optimize all' mode.
            List<Diagnostic> diags = copy.getDiagnostics();
            if (!diags.isEmpty()) {
                SourcePositions sp = copy.getTrees().getSourcePositions();
                long startPos = sp.getStartPosition(cu, imports.get(0));
                long endPos = sp.getEndPosition(cu, imports.get(imports.size() - 1));
                for (Diagnostic d : diags) {
                    if (startPos <= d.getPosition() && d.getPosition() <= endPos) {
                        if (ERROR_CODE.contentEquals(d.getCode()))
                            return;
                    }
                }
            }
        }
        final CodeStyle cs = CodeStyle.getDefault(copy.getFileObject());
        Set<Element> starImports = new HashSet<Element>();
        Set<Element> staticStarImports = new HashSet<Element>();
        Set<Element> toImport = getUsedElements(copy, cu, starImports, staticStarImports);
        List<ImportTree> imps = new LinkedList<ImportTree>();  
        TreeMaker maker = copy.getTreeMaker();
        
        if (addImports != null) {
            // copy over all imports to be added + existing imports.
            toImport.addAll(addImports);
            imps.addAll(cu.getImports());
        } else if (!toImport.isEmpty() || isBulkMode) {
            // track import star import scopes, so only one star import/scope appears in imps - #251977
            Set<Element> starImportScopes = new HashSet<>();
            Trees trees = copy.getTrees();
            for (ImportTree importTree : cu.getImports()) {
                Tree qualIdent = importTree.getQualifiedIdentifier();
                if (qualIdent.getKind() == Tree.Kind.MEMBER_SELECT && "*".contentEquals(((MemberSelectTree)qualIdent).getIdentifier())) {
                    ImportTree imp = null;
                    Element importedScope = trees.getElement(TreePath.getPath(cu, ((MemberSelectTree)qualIdent).getExpression()));
                    
                    if (importTree.isStatic()) {
                        if (staticStarImports != null && 
                            staticStarImports.contains(importedScope) &&
                            !starImportScopes.contains(importedScope)) {
                            imp = maker.Import(qualIdent, true);
                        }
                    } else {
                        if (starImports != null && 
                            starImports.contains(importedScope) &&
                            !starImportScopes.contains(importedScope)) {
                            imp = maker.Import(qualIdent, false);
                        }
                    }
                    if (imp != null) {
                        starImportScopes.add(importedScope);
                        imps.add(imp);
                    }
                }
            }
        } else {
            return;
        }
        if (!imps.isEmpty()) {
            imps.sort(new Comparator<ImportTree>() {

                private CodeStyle.ImportGroups groups = cs.getImportGroups();

                @Override
                public int compare(ImportTree o1, ImportTree o2) {
                    if (o1 == o2)
                        return 0;
                    String s1 = o1.getQualifiedIdentifier().toString();
                    String s2 = o2.getQualifiedIdentifier().toString();
                    int bal = groups.getGroupId(s1, o1.isStatic()) - groups.getGroupId(s2, o2.isStatic());
                    return bal == 0 ? s1.compareTo(s2) : bal;
                }
            });
        }
        CompilationUnitTree cut = maker.CompilationUnit(cu.getPackageAnnotations(), cu.getPackageName(), imps, cu.getTypeDecls(), cu.getSourceFile());
        ((JCCompilationUnit)cut).packge = ((JCCompilationUnit)cu).packge;
        if (starImports != null || staticStarImports != null) {
            ((JCCompilationUnit)cut).starImportScope = ((JCCompilationUnit)cu).starImportScope;
        }
        CompilationUnitTree ncu = toImport.isEmpty() ? cut : GeneratorUtilities.get(copy).addImports(cut, toImport);
        copy.rewrite(cu, ncu);
    }

    private static Set<Element> getUsedElements(final CompilationInfo info, final CompilationUnitTree cut, final Set<Element> starImports, final Set<Element> staticStarImports) {
        final Set<Element> ret = new HashSet<Element>();
        final Trees trees = info.getTrees();
        final Types types = info.getTypes();
        new ErrorAwareTreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                addElement(trees.getElement(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitClass(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
            
            private void addElement(Element element) {
                if (element != null) {
                    switch (element.getKind()) {
                        case ENUM_CONSTANT:
                        case FIELD:
                        case METHOD:
                            if (element.getModifiers().contains(Modifier.STATIC)) {
                                Element glob = global(element, staticStarImports);
                                if (glob != null)
                                    ret.add(glob);
                            }
                            break;
                        case ANNOTATION_TYPE:
                        case CLASS:
                        case RECORD:
                        case ENUM:
                        case INTERFACE:
                            Element glob = global(element, starImports);
                            if (glob != null)
                                ret.add(glob);
                    }
                }
            }

            private Element global(Element element, Set<Element> stars) {
                for (Symbol sym : ((JCCompilationUnit)cut).namedImportScope.getSymbolsByName((Name)element.getSimpleName())) {
                    if (element == sym || element.asType().getKind() == TypeKind.ERROR && element.getKind() == sym.getKind())
                        return sym;
                }
                for (Symbol sym : ((JCCompilationUnit)cut).packge.members().getSymbolsByName((Name)element.getSimpleName())) {
                    if (element == sym || element.asType().getKind() == TypeKind.ERROR && element.getKind() == sym.getKind())
                        return sym;
                }
                for (Symbol sym : ((JCCompilationUnit)cut).starImportScope.getSymbolsByName((Name)element.getSimpleName())) {
                    if (element == sym || element.asType().getKind() == TypeKind.ERROR && element.getKind() == sym.getKind()) {
                        if (stars != null) {
                            stars.add(sym.owner);
                        }
                        return sym;
                    }
                }
                return null;
            }
        }.scan(cut, null);
        return ret;
    }

    private static final class OrganizeImportsFix extends JavaFix {

        private final boolean isBulkMode;
        
        public OrganizeImportsFix(CompilationInfo info, TreePath tp, boolean isBulkMode) {
            super(info, tp);
            this.isBulkMode = isBulkMode;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(OrganizeImports.class, "FIX_OrganizeImports"); //NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            doOrganizeImports(ctx.getWorkingCopy(), isBulkMode);
        }
    }

    @EditorActionRegistration(name = EditorActionNames.organizeImports,
                              mimeType = JavaKit.JAVA_MIME_TYPE,
                              menuPath = "Source",
                              menuPosition = 2430,
                              menuText = "#" + EditorActionNames.organizeImports + "_menu_text")
    public static class OrganizeImportsAction extends BaseAction {

        @Override
        public void actionPerformed(final ActionEvent evt, final JTextComponent component) {
            if (component == null || !component.isEditable() || !component.isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            final BaseDocument doc = (BaseDocument) component.getDocument();
            final Source source = Source.create(doc);
            if (source != null) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                    copy.toPhase(Phase.RESOLVED);
                                    doOrganizeImports(copy, false);
                                }
                            }).commit();
                        } catch (Exception ex) {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                }, NbBundle.getMessage(OrganizeImports.class, "MSG_OragnizeImports"), cancel, false); //NOI18N
            }
        }
    }
}
