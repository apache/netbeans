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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import javax.tools.Diagnostic;

/**
 * @author Michal Hlavac
 * @author Samuel Halliday
 *
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=70746">RFE 70746</a>
 * @see <a href="http://kenai.com/projects/nb-svuid-generator/sources/mercurial/show/src/eu/easyedu/netbeans/svuid">Original Implementation Source Code</a>
 */
public class SerialVersionUID implements ErrorRule<Void>, OverrideErrorMessage<Void> {
    public static final Set<String> CODES = Collections.singleton("compiler.warn.missing.SVUID");

    private static final String SERIAL = "serial"; //NOI18N
    private static final String SVUID = "serialVersionUID"; //NOI18N
    private final AtomicBoolean cancel = new AtomicBoolean();

    public SerialVersionUID() {
    }

    public Set<Kind> getTreeKinds() {
        return TreeUtilities.CLASS_TREE_KINDS;
    }

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, Data data) {
        if (treePath == null || !TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
            return null;
        }
        if (treePath.getLeaf().getKind() == Tree.Kind.INTERFACE) {
            return "";
        }
        return null;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (treePath == null || !TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
            return null;
        }
        if (treePath.getLeaf().getKind() == Tree.Kind.INTERFACE) {
            return null;
        }
        
        TypeElement type = (TypeElement) info.getTrees().getElement(treePath);
        if (type == null) {
            return null;
        }
        List<Fix> fixes = new ArrayList<Fix>();

        fixes.add(new FixImpl(TreePathHandle.create(treePath, info), false).toEditorFix());
        // fixes.add(new FixImpl(TreePathHandle.create(treePath, info), true));

        if (type.getNestingKind() != NestingKind.ANONYMOUS) {
            // add SuppressWarning only to non-anonymous class
            fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SERIAL));
        }

        return fixes;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DN_SerialVersionUID");//NOI18N
    }

    public void cancel() {
        cancel.set(true);
    }

    private static class FixImpl extends JavaFix {

        private final TreePathHandle handle;
        private final boolean generated;

        /**
         * @param handle to the CLASS
         * @param generated true will insert a generated value, false will use a default
         */
        public FixImpl(TreePathHandle handle, boolean generated) {
            super(handle);
            this.handle = handle;
            this.generated = generated;
            if (generated) {
                throw new UnsupportedOperationException("TODO: implement");
            }
        }

        @Override
        protected String getText() {
            if (generated) {
                return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID_Generated");//NOI18N
            }
            return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID");//NOI18N
        }

        @Override
        protected void performRewrite(@NonNull TransformationContext ctx) throws Exception {
            TreePath treePath = ctx.getPath();
            ClassTree classTree = (ClassTree) treePath.getLeaf();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();

            // documentation recommends private
            Set<Modifier> modifiers = EnumSet.of(PRIVATE, STATIC, FINAL);
            VariableTree svuid = make.Variable(make.Modifiers(modifiers), SVUID, make.Identifier("long"), make.Literal(1L)); //NO18N

            ClassTree decl = GeneratorUtilities.get(ctx.getWorkingCopy()).insertClassMember(classTree, svuid);
            ctx.getWorkingCopy().rewrite(classTree, decl);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FixImpl other = (FixImpl) obj;
            if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
                return false;
            }
            if (this.generated != other.generated) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.handle != null ? this.handle.hashCode() : 0);
            hash = 41 * hash + (this.generated ? 1 : 0);
            return hash;
        }
    }

}
