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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.errors.Utilities.Visibility;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class AccessError implements ErrorRule<Void> {

    private static final Set<String> CODES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("compiler.err.report.access")));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        Element el = info.getTrees().getElement(treePath);
        if (el == null || el.asType().getKind() == TypeKind.ERROR) return null;
        if (el.getEnclosingElement() == null || !(el.getEnclosingElement().getKind().isClass() || el.getEnclosingElement().getKind().isInterface())) return null;
        TypeElement targetEnclosing = (TypeElement) el.getEnclosingElement();
        
        if (!Utilities.isTargetWritable(targetEnclosing, info)) return null;
        
        TreePath outtermostClassPath = null;
        TreePath up = treePath;
        
        while (up != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(up.getLeaf().getKind())) {
                outtermostClassPath = up;
            }
            up = up.getParentPath();
        }
        
        Element outtermostClassEl = outtermostClassPath != null ? info.getTrees().getElement(outtermostClassPath) : null;
        
        if (outtermostClassEl == null || !(outtermostClassEl.getKind().isClass() || outtermostClassEl.getKind().isInterface())) return null;
        
        TypeElement outtermostClass = (TypeElement) outtermostClassEl;
        Visibility newVisibility = Utilities.getAccessModifiers(info, outtermostClass, targetEnclosing);
        
        if (newVisibility.ordinal() <= Visibility.forElement(el).ordinal() || newVisibility == Visibility.PRIVATE) return null; //should not happen?
        
        return Collections.<Fix>singletonList(new FixImpl(info, el, newVisibility).toEditorFix());
    }

    @Override
    public String getId() {
        return AccessError.class.getName();
    }

    @Override
    @Messages("DN_AccessError=Upgrade elements access")
    public String getDisplayName() {
        return Bundle.DN_AccessError();
    }

    @Override
    public void cancel() {}
    
    private static final class FixImpl extends JavaFix {

        private final Visibility newVisibility;
        private final String elementDisplayName;
        public FixImpl(CompilationInfo info, Element el, Visibility newVisibility) {
            super(TreePathHandle.create(el, info));
            this.newVisibility = newVisibility;
            this.elementDisplayName = el.getSimpleName().toString();
        }

        @Override
        protected String getText() {
            String key = "FIX_AccessError_" + newVisibility.name();
            return NbBundle.getMessage(FixImpl.class, key, elementDisplayName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            ModifiersTree mods;
            Tree el = ctx.getPath().getLeaf();
            
            switch (el.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    mods = ((ClassTree) el).getModifiers();
                    break;
                case METHOD:
                    mods = ((MethodTree) el).getModifiers();
                    break;
                case VARIABLE:
                    mods = ((VariableTree) el).getModifiers();
                    break;
                default:
                    throw new IllegalStateException(el.getKind().name());
            }
            
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            ModifiersTree result = mods;
            
            result = make.removeModifiersModifier(result, Modifier.PUBLIC);    //should not be there, presumably...
            result = make.removeModifiersModifier(result, Modifier.PROTECTED);
            result = make.removeModifiersModifier(result, Modifier.PRIVATE);
            
            for (Modifier m : newVisibility.getRequiredModifiers()) {
                result = make.addModifiersModifier(result, m);
            }
            
            ctx.getWorkingCopy().rewrite(mods, result);
        }
        
    }
    
}
