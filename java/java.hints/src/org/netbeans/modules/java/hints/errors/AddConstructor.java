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
import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class AddConstructor implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.cant.apply.symbol", "compiler.err.cant.apply.symbols")); // NOI18N
    
    @Override
    public Set<String> getCodes() {
        return ERROR_CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) return Collections.emptyList();
        
        Element el = info.getTrees().getElement(treePath);
        
        if (el == null || !el.getKind().isClass()) return Collections.emptyList();
        
        TypeElement clazz = (TypeElement) el;
        TypeMirror superType = clazz.getSuperclass();
        
        if (superType.getKind() != TypeKind.DECLARED) return Collections.emptyList();
        
        TypeElement superClazz = (TypeElement) info.getTypes().asElement(superType);
        DeclaredType targetType = (DeclaredType) clazz.asType();
        Scope classScope = info.getTrees().getScope(treePath);
        List<Fix> result = new ArrayList<Fix>();
        
        for (ExecutableElement constr : ElementFilter.constructorsIn(superClazz.getEnclosedElements())) {
            if (!info.getTrees().isAccessible(classScope, constr, (DeclaredType) superType)) continue;
            
            StringBuilder name = new StringBuilder();
            
            name.append(clazz.getSimpleName()).append("(");
            
            ExecutableType target = (ExecutableType) info.getTypes().asMemberOf(targetType, constr);
            boolean firstParam = true;
            
            for (TypeMirror p : target.getParameterTypes()) {
                if (!firstParam) name.append(", ");
                firstParam = false;
                name.append(info.getTypeUtilities().getTypeName(p));
            }
            
            name.append(")");
            result.add(new FixImpl(info, treePath, constr, StringEscapeUtils.escapeHtml(name.toString())).toEditorFix());
        }
        
        return result;
    }

    @Override
    public String getId() {
        return AddConstructor.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AddConstructor.class, "DN_AddConstructor");
    }

    @Override
    public void cancel() {
    }
    
    private static final class FixImpl extends JavaFix {

        private final ElementHandle<ExecutableElement> constr;
        private final String constrName;
        public FixImpl(CompilationInfo info, TreePath tp, ExecutableElement constr, String constrName) {
            super(info, tp);
            this.constr = ElementHandle.create(constr);
            this.constrName = constrName;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(AddConstructor.class, "FIX_AddConstructor", constrName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TypeElement clazz = (TypeElement) ctx.getWorkingCopy().getTrees().getElement(ctx.getPath());
            if (clazz == null) {
                // TODO: report to the user
                return;
            }
            GeneratorUtilities gu = GeneratorUtilities.get(ctx.getWorkingCopy());
            MethodTree newConstr = gu.createConstructor(clazz, Collections.<VariableElement>emptyList(), constr.resolve(ctx.getWorkingCopy()));
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), gu.insertClassMember((ClassTree) ctx.getPath().getLeaf(), newConstr));
        }
        
    }
}
