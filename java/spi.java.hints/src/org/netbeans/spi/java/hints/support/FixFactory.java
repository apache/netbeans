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
package org.netbeans.spi.java.hints.support;

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.Parameters;

/** Factory for creating generally useful fixes. Currently, changes to the modifiers
 *  are supported.
 *
 * @author Dusan Balek
 */
public final class FixFactory {

    private FixFactory() {}

    /** Creates a fix, which when invoked adds a set of modifiers to the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param text text displayed as a fix description
     */
    public static final Fix addModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toAdd", toAdd);
        Parameters.notNull("text", text);

        return changeModifiersFix(compilationInfo, treePath, toAdd, Collections.<Modifier>emptySet(), text);
    }

    /** Creates a fix, which when invoked removes a set of modifiers from the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix removeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toRemove, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toRemove", toRemove);
        Parameters.notNull("text", text);

        return changeModifiersFix(compilationInfo, treePath, Collections.<Modifier>emptySet(), toRemove, text);
    }

    /** Creates a fix, which when invoked changes the existing modifiers
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix changeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, Set<Modifier> toRemove, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toAdd", toAdd);
        Parameters.notNull("toRemove", toRemove);
        Parameters.notNull("text", text);

        if (treePath.getLeaf().getKind() != Kind.MODIFIERS) {
            return null;
        }
        return new ChangeModifiersFixImpl(TreePathHandle.create(treePath, compilationInfo), toAdd, toRemove, text).toEditorFix();
    }

    private static final class ChangeModifiersFixImpl extends JavaFix {

        private final TreePathHandle modsHandle;
        private final Set<Modifier> toAdd;
        private final Set<Modifier> toRemove;
        private final String text;

        public ChangeModifiersFixImpl(TreePathHandle modsHandle, Set<Modifier> toAdd, Set<Modifier> toRemove, String text) {
            super(modsHandle);
            this.modsHandle = modsHandle;
            this.toAdd = toAdd;
            this.toRemove = toRemove;
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            TreeMaker make = wc.getTreeMaker();
            ModifiersTree newMods = (ModifiersTree) path.getLeaf();
            for (Modifier a : toAdd) {
                newMods = make.addModifiersModifier(newMods, a);
            }
            for (Modifier r : toRemove) {
                newMods = make.removeModifiersModifier(newMods, r);
            }
            wc.rewrite(path.getLeaf(), newMods);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChangeModifiersFixImpl other = (ChangeModifiersFixImpl) obj;
            if (this.modsHandle != other.modsHandle && (this.modsHandle == null || !this.modsHandle.equals(other.modsHandle))) {
                return false;
            }
            if (this.toAdd != other.toAdd && (this.toAdd == null || !this.toAdd.equals(other.toAdd))) {
                return false;
            }
            if (this.toRemove != other.toRemove && (this.toRemove == null || !this.toRemove.equals(other.toRemove))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.modsHandle != null ? this.modsHandle.hashCode() : 0);
            hash = 71 * hash + (this.toAdd != null ? this.toAdd.hashCode() : 0);
            hash = 71 * hash + (this.toRemove != null ? this.toRemove.hashCode() : 0);
            return hash;
        }
    }
}
