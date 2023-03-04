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

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.errors.Utilities.Visibility;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class FieldEncapsulation {

    private static final Logger LOG = Logger.getLogger(FieldEncapsulation.class.getName());
    private static final String KW_THIS = "this";

    static final boolean ALLOW_ENUMS_DEFAULT = false;
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.ALLOW_ENUMS_KEY", tooltip = "#TP_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.ALLOW_ENUMS_KEY", defaultValue=ALLOW_ENUMS_DEFAULT)
    static final String ALLOW_ENUMS_KEY = "allow.enums";

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.protectedField", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.protectedField", category="encapsulation", suppressWarnings={"ProtectedField"}, enabled=false, options=Options.QUERY) //NOI18N
    @UseOptions(ALLOW_ENUMS_KEY)
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription protectedField(final HintContext ctx) {
        return create(ctx,
            Visibility.PROTECTED,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_ProtectedField"));
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.publicField", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.publicField", category="encapsulation", suppressWarnings={"PublicField"}, enabled=false, options=Options.QUERY) //NOI18N
    @UseOptions(ALLOW_ENUMS_KEY)
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription publicField(final HintContext ctx) {
        return create(ctx,
            Visibility.PUBLIC,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_PublicField"));
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.packageField", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.packageField", category="encapsulation", suppressWarnings={"PackageVisibleField"}, enabled=false, options=Options.QUERY) //NOI18N
    @UseOptions(ALLOW_ENUMS_KEY)
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription packageField(final HintContext ctx) {
        return create(ctx,
            Visibility.PACKAGE_PRIVATE,
            NbBundle.getMessage(FieldEncapsulation.class, "TXT_PackageField"));
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.privateField", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.FieldEncapsulation.privateField", category="encapsulation", suppressWarnings={"AccessingNonPublicFieldOfAnotherObject"}, enabled=false, options=Options.QUERY) //NOI18N
    @TriggerTreeKind(Kind.MEMBER_SELECT)
    public static ErrorDescription privateField(final HintContext ctx) {
        assert ctx != null;
        final TreePath tp = ctx.getPath();
        final Element selectElement = ctx.getInfo().getTrees().getElement(tp);
        if (selectElement == null ||
            selectElement.getKind()!= ElementKind.FIELD ||
            !((VariableElement)selectElement).getModifiers().contains(Modifier.PRIVATE)||
            ((VariableElement)selectElement).getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        final ExpressionTree subSelect = ((MemberSelectTree)tp.getLeaf()).getExpression();
        if ((subSelect.getKind() == Tree.Kind.IDENTIFIER && KW_THIS.contentEquals(((IdentifierTree)subSelect).getName())) ||
            (subSelect.getKind() == Tree.Kind.MEMBER_SELECT && KW_THIS.contentEquals(((MemberSelectTree)subSelect).getIdentifier()))){
            return null;
        }
        final TypeElement selectOwner = getEnclosingClass(tp, ctx.getInfo().getTrees());
        if (selectOwner == null ||
            SourceUtils.getOutermostEnclosingTypeElement(selectElement) != SourceUtils.getOutermostEnclosingTypeElement(selectOwner)) {
            return null;
        }
        SourceUtils.getOutermostEnclosingTypeElement(selectElement);
        return ErrorDescriptionFactory.forName(ctx, tp,
                NbBundle.getMessage(FieldEncapsulation.class, "TXT_OtherPrivateField"));
    }

    private static TypeElement getEnclosingClass (TreePath path, final Trees trees) {
        while (path != null && path.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind())) {
                return (TypeElement) trees.getElement(path);
            }
            path = path.getParentPath();
        }
        return null;
    }

    private static ErrorDescription create (final HintContext ctx,
                                            final Visibility visibility,
                                            final String message) {
        assert ctx != null;
        assert message != null;
        final TreePath tp = ctx.getPath();
        final Tree parent = tp.getParentPath().getLeaf();
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) ||
            ctx.getInfo().getTreeUtilities().isInterface((ClassTree)parent)) {
            return null;
        }
        final VariableTree vt = (VariableTree) tp.getLeaf();
        final ModifiersTree mt = vt.getModifiers();
        if (mt.getFlags().contains(Modifier.FINAL) || Utilities.effectiveVisibility(tp) != visibility) {
            return null;
        }
        if (ctx.getPreferences().getBoolean(ALLOW_ENUMS_KEY, ALLOW_ENUMS_DEFAULT)) {
            Element type = ctx.getInfo().getTrees().getElement(new TreePath(tp, vt.getType()));
            if (type != null && type.getKind() == ElementKind.ENUM) {
                return null;
            }
        }
        final Collection<? extends TreePath> fieldGroup = Utilities.resolveFieldGroup(ctx.getInfo(), tp);
        if (fieldGroup.size() != 1 && fieldGroup.iterator().next().getLeaf() != tp.getLeaf()) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, tp, message,
                new FixImpl(TreePathHandle.create(tp, ctx.getInfo())));
    }

    private static boolean hasRequiredVisibility(final Set<Modifier> mods, final Modifier reqMod) {
        return reqMod != null ?
            mods.contains(reqMod):
            mods.isEmpty() ?
                true:
                !EnumSet.copyOf(mods).removeAll(EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC));
    }

    private static class FixImpl implements Fix {

        private final TreePathHandle handle;

        public FixImpl(final TreePathHandle handle) {
            this.handle = handle;
        }


        @Override
        public String getText() {
            return NbBundle.getMessage(FieldEncapsulation.class, "FIX_EncapsulateField");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            final FileObject file = handle.getFileObject();
            final JTextComponent comp = EditorRegistry.lastFocusedComponent();
            if (file != null && file == getFileObject(comp)) {
                final int[] pos = new int[]{-1};
                JavaSource.forFileObject(file).runUserActionTask(new Task<CompilationController>(){
                    @Override
                    public void run(CompilationController info) throws Exception {
                        info.toPhase(JavaSource.Phase.PARSED);
                        final TreePath tp = handle.resolve(info);
                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                            pos[0] = (int) info.getTrees().getSourcePositions().getEndPosition(
                                    tp.getCompilationUnit(),
                                    ((VariableTree)tp.getLeaf()).getType()) + 1;
                        }
                    }
                }, true);
                invokeRefactoring (comp, pos[0]);
            }
            return null;
        }

        public static FileObject getFileObject(JTextComponent comp) {
            if (comp == null) {
                return null;
            }
            final Document doc = comp.getDocument();
            if (doc == null) {
                return null;
            }
            final Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof FileObject) {
                return (FileObject)sdp;
            }
            if (sdp instanceof DataObject) {
                return ((DataObject)sdp).getPrimaryFile();
            }
            return null;
        }

        /**
         * todo:
         * Currently there is no API to invoke encapsulate field action.
         */
        private void invokeRefactoring(final JTextComponent component, final int position) {
            final Action a = Actions.forID("Refactoring", "org.netbeans.modules.refactoring.java.api.ui.EncapsulateFieldAction");
            if (a == null) {
                LOG.warning("Encapsulate Field action not found"); //NOI18N
                return;
            }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (position != -1) {
                            component.setCaretPosition(position);
                        }
                        a.actionPerformed(new ActionEvent(component, 0, null));
                    }
                });
        }
    }
    
}
