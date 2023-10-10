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

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Adds or removes the final modifier from/to the local variable declarations or
 * local parameters. Based on {@link MakeVariableFinal} from Jan Lahoda.
 *
 * @author markiewb
 * @author Jan Lahoda
 */
public class AddOrRemoveFinalModifier implements ErrorRule<Void> {

    private final String id;
    private final Type type;
    private final String errorCode;
    private final String displayName;
    private final String description;
    private final String fixDescription;

    /**
     *
     * @param errorCode javac error code - see
     * http://hg.netbeans.org/main/nb-javac/raw-file/tip/src/share/classes/com/sun/tools/javac/resources/compiler.properties
     * for possible codes
     */
    private AddOrRemoveFinalModifier(String id, String errorCode, String displayName, String description, String fixDescription, Type type) {
        this.id = id;
        this.errorCode = errorCode;
        this.displayName = displayName;
        this.description = description;
        this.fixDescription = fixDescription;
        this.type = type;
     }
    
    @NbBundle.Messages({
        "DN_RemoveFinalModifierFromVariable=Remove \"final\" modifier",
        "DESC_RemoveFinalModifierFromVariable=Removes the \"final\" modifier from the variable declaration",
        "# {0} - variable name",
        "FIX_RemoveFinalModifierFromVariable=Remove \"final\" modifier from variable \"{0}\""})
    public static AddOrRemoveFinalModifier createRemoveFinalFromVariable() {
        return new AddOrRemoveFinalModifier(AddOrRemoveFinalModifier.class.getName() + ".var", "compiler.err.cant.assign.val.to.var", Bundle.DN_RemoveFinalModifierFromVariable(), Bundle.DESC_RemoveFinalModifierFromVariable(), "FIX_RemoveFinalModifierFromVariable", Type.REMOVE);
    }
    
    @NbBundle.Messages({
        "DN_RemoveFinalModifierFromParameter=Remove \"final\" modifier",
        "DESC_RemoveFinalModifierFromParameter=Removes the \"final\" modifier from the parameter declaration",
        "# {0} - parameter name",
        "FIX_RemoveFinalModifierFromParameter=Remove \"final\" modifier from parameter \"{0}\""})
    public static AddOrRemoveFinalModifier createRemoveFinalFromParameter() {
        return new AddOrRemoveFinalModifier(AddOrRemoveFinalModifier.class.getName() + ".param", "compiler.err.final.parameter.may.not.be.assigned", Bundle.DN_RemoveFinalModifierFromParameter(), Bundle.DESC_RemoveFinalModifierFromParameter(), "FIX_RemoveFinalModifierFromParameter", Type.REMOVE);
    }

    @NbBundle.Messages({
        "DN_MakeVariableFinal=Add \"final\" modifier",
        "DESC_MakeVariableFinal=Add \"final\" modifier to variable declaration or parameter",
        "# {0} - variable or parameter name",
        "FIX_MakeVariableFinal=Add \"final\" modifier to \"{0}\""})
    public static AddOrRemoveFinalModifier createAddFinalModifier() {
        return new AddOrRemoveFinalModifier("org.netbeans.modules.java.hints.errors.MakeVariableFinal", "compiler.err.local.var.accessed.from.icls.needs.final", Bundle.DN_MakeVariableFinal(), Bundle.DESC_MakeVariableFinal(), "FIX_MakeVariableFinal", Type.ADD);
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(errorCode));
     }

    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        Tree leaf = treePath.getLeaf();

        if (leaf.getKind() == Kind.IDENTIFIER) {
            Element el = compilationInfo.getTrees().getElement(treePath);
            if (el == null) {
                return null;
            }
            TreePath declaration = compilationInfo.getTrees().getPath(el);
            
            // do not offer any modifications for members in other CUs
            if (declaration != null && declaration.getCompilationUnit() == compilationInfo.getCompilationUnit()) {
                return Collections.singletonList(new FixImpl(compilationInfo.getFileObject(), el.getSimpleName().toString(), TreePathHandle.create(declaration, compilationInfo), fixDescription, type).toEditorFix());
            }
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
    }

    public String getId() {
        return id;
    }

    @Override
     public String getDisplayName() {
        return displayName;
     }
 
     public String getDescription() {
        return description;
     }

    private static final class FixImpl extends JavaFix {
        
        private String variableName;
        private TreePathHandle variable;
        private FileObject file;
        private final String fixDescription;
        private final Type type;
        
        public FixImpl(FileObject file, String variableName, TreePathHandle variable, String fixDescription, Type type) {
            super(variable);
            this.file = file;
            this.variableName = variableName;
            this.variable = variable;
            this.fixDescription = fixDescription;
            this.type = type;
        }

        public String getText() {
            return NbBundle.getMessage(AddOrRemoveFinalModifier.class, fixDescription, String.valueOf(variableName));
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();

            if (tp == null) {
                return;
            }

            VariableTree vt = (VariableTree) tp.getLeaf();
            ModifiersTree mt = vt.getModifiers();
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

            modifiers.addAll(mt.getFlags());
            switch (type) {
                case ADD:
                    modifiers.add(Modifier.FINAL);
                    break;
                case REMOVE:
                    modifiers.remove(Modifier.FINAL);
                    break;
                default:
                    throw new IllegalArgumentException("Type " + type + " not supported");
            }

            ModifiersTree newMod = wc.getTreeMaker().Modifiers(modifiers, mt.getAnnotations());

            wc.rewrite(mt, newMod);
        }

        @Override
        public int hashCode() {
            return variable.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FixImpl)) {
                return false;
            }
	    return variable.equals(((FixImpl) obj).variable);
        }
        
    }

    /**
     * Defines the type of this hint. Remove the final modifier OR add the final
     * modifier.
     */
    private enum Type {

        REMOVE,
        ADD
     }
}