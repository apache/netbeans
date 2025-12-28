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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.source.parsing.Hacks;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
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
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.OrganizeMembers", description = "#DESC_org.netbeans.modules.java.hints.OrganizeMembers", category = "class_structure", enabled = false)
public class OrganizeMembers {

    @TriggerTreeKind({Kind.CLASS, Kind.RECORD, Kind.ENUM})
    public static ErrorDescription checkMembers(final HintContext context) {
        for (Diagnostic<?> d : context.getInfo().getDiagnostics()) {
            if (Hacks.isSyntaxError(d)) {
                return null;
            }
        }
        Source source = context.getInfo().getSnapshot().getSource();
        try {
            ModificationResult result = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                    copy.toPhase(Phase.RESOLVED);
                    doOrganizeMembers(copy, context.getPath());
                }
            });
            List<? extends Difference> diffs = result.getDifferences(source.getFileObject());
            if (diffs != null && !diffs.isEmpty() && !checkGuarded(context.getInfo().getDocument(), diffs)) {
                Fix fix = new OrganizeMembersFix(context.getInfo(), context.getPath()).toEditorFix();
                SourcePositions sp = context.getInfo().getTrees().getSourcePositions();
                int offset = diffs.get(0).getStartPosition().getOffset();
                LineMap lm = context.getInfo().getCompilationUnit().getLineMap();
                long lno = lm.getLineNumber(offset);
                if (lno >= 1) {
                    offset = (int)lm.getStartPosition(lno);
                }
                CompilationUnitTree cut = context.getPath().getCompilationUnit();
                ClassTree clazz = (ClassTree) context.getPath().getLeaf();
                for (Tree member : clazz.getMembers()) {
                    if (context.getInfo().getTreeUtilities().isSynthetic(new TreePath(context.getPath(), member))) continue;
                    if (sp.getStartPosition(cut, member) >= offset) {
                        return ErrorDescriptionFactory.forTree(context, member, NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"), fix); //NOI18N
                    }
                }
                return ErrorDescriptionFactory.forTree(context, clazz, NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"), fix); //NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static void doOrganizeMembers(WorkingCopy copy, TreePath path) {
        GeneratorUtilities gu = GeneratorUtilities.get(copy);
        
        ClassTree clazz = (ClassTree) path.getLeaf();
        clazz = gu.importComments(clazz, copy.getCompilationUnit());
        TreeMaker maker = copy.getTreeMaker();

        ClassTree nue = maker.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getPermitsClause(), Collections.<Tree>emptyList());
        List<Tree> members = new ArrayList<>();
        Map<Tree, Tree> memberMap = new HashMap<>();
        
        List<Tree> fixedMembers = new ArrayList<>();
        for (Tree tree : clazz.getMembers()) {
            // isSynthetic does not consider record components to be synthetic
            if (copy.getTreeUtilities().isSynthetic(new TreePath(path, tree))
                    && !(tree.getKind() == Kind.VARIABLE && copy.getTreeUtilities().isRecordComponent((VariableTree)tree))) {
                continue;
            }
            Tree member = switch (tree.getKind()) {
                case CLASS, INTERFACE, ENUM, RECORD, ANNOTATION_TYPE -> 
                    maker.setLabel(tree, ((ClassTree)tree).getSimpleName());
                case VARIABLE -> {
                    VariableTree vt = (VariableTree)tree;
                    Tree mem = maker.setLabel(tree, vt.getName());
                    if (copy.getTreeUtilities().isEnumConstant(vt) || copy.getTreeUtilities().isRecordComponent(vt)) {
                        fixedMembers.add(mem);
                    }
                    yield mem;
                }
                case METHOD -> maker.setLabel(tree, ((MethodTree)tree).getName());
                case BLOCK -> maker.asReplacementOf(maker.Block(((BlockTree)tree).getStatements(), ((BlockTree)tree).isStatic()), tree, true);
                default -> tree;    
            };
            members.add(member);
            memberMap.put(member, tree);
        }
        // fool the generator utilities with cloned members, so it does not take positions into account
        if (fixedMembers.isEmpty()) {
            nue = GeneratorUtilities.get(copy).insertClassMembers(nue, members);
        } else {
            members.removeAll(fixedMembers);
            int max = nue.getMembers().size();
            // insert the enum values or record components in the original order
            for (Tree t : fixedMembers) {
                nue = maker.insertClassMember(nue, max++, t);
            }
            nue = GeneratorUtilities.get(copy).insertClassMembers(nue, members);
        }
        // now create a new class, based on the original one - retain the order decided by GeneratorUtilities.
        ClassTree changed = maker.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getPermitsClause(), Collections.<Tree>emptyList());
        int index = 0;
        for (Tree t : nue.getMembers()) {
            Tree orig = memberMap.get(t);
            changed = maker.insertClassMember(changed, index, orig);
            index++;
        }
        copy.rewrite(clazz, changed);
    }
    
    private static boolean checkGuarded(Document doc, List<? extends Difference> diffs) {
        if (doc instanceof GuardedDocument guardedDocument) {
            MarkBlockChain chain = guardedDocument.getGuardedBlockChain();
            for (Difference diff : diffs) {
                if ((chain.compareBlock(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset()) & MarkBlock.OVERLAP) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class OrganizeMembersFix extends JavaFix {

        public OrganizeMembersFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(OrganizeMembers.class, "FIX_OrganizeMembers"); //NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            doOrganizeMembers(ctx.getWorkingCopy(), ctx.getPath());
        }
    }

    @EditorActionRegistration(name = EditorActionNames.organizeMembers,
                              mimeType = JavaKit.JAVA_MIME_TYPE,
                              menuPath = "Source",
                              menuPosition = 2437,
                              menuText = "#" + EditorActionNames.organizeMembers + "_menu_text")
    public static class OrganizeMembersAction extends BaseAction {

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
                BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ModificationResult result = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                    copy.toPhase(Phase.RESOLVED);
                                    TreeUtilities tu = copy.getTreeUtilities();
                                    TreePath path = tu.pathFor(component.getCaretPosition());
                                    path = tu.getPathElementOfKind(EnumSet.of(Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.ANNOTATION_TYPE), path);
                                    if (path != null) {
                                        doOrganizeMembers(copy, path);
                                    } else {
                                        CompilationUnitTree cut = copy.getCompilationUnit();
                                        List<? extends Tree> typeDecls = cut.getTypeDecls();
                                        if (typeDecls.isEmpty()) {
                                            Toolkit.getDefaultToolkit().beep();
                                        } else {
                                            doOrganizeMembers(copy, copy.getTrees().getPath(cut, typeDecls.get(0)));
                                        }
                                    }
                                }
                            });
                            List<? extends Difference> diffs = result.getDifferences(source.getFileObject());
                            if (diffs != null && !diffs.isEmpty() && !checkGuarded(doc, diffs)) {
                                result.commit();
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                            }
                        } catch (Exception ex) {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                }, NbBundle.getMessage(OrganizeMembers.class, "MSG_OragnizeMembers"), cancel, false); //NOI18N
            }
        }
    }
}
