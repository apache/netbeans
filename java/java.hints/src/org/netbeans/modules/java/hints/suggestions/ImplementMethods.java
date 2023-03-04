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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_ImplementMethods", description = "#DESC_ImplementMethods", category = "suggestions", hintKind=Hint.Kind.ACTION)
@Messages({
    "DN_ImplementMethods=Implement Abstract Methods",
    "DESC_ImplementMethods=Implement Abstract Methods."
})
public class ImplementMethods {
    @TriggerTreeKind(Kind.CLASS)
    @Messages({
        "# {0} - the FQN of the type whose methods will be implemented",
        "ERR_ImplementMethods=Implement unimplemented abstract methods of {0}"
    })
    public static ErrorDescription implementMethods(HintContext ctx) {
        ClassTree clazz = (ClassTree) ctx.getPath().getLeaf();
        Element typeEl = ctx.getInfo().getTrees().getElement(ctx.getPath());
        
        if (typeEl == null || !typeEl.getKind().isClass())
            return null;
        
        List<Tree> candidate = new ArrayList<Tree>(clazz.getImplementsClause());
        
        candidate.add(clazz.getExtendsClause());
        
        Tree found = null;
        
        for (Tree cand : candidate) {
            if (   ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), cand) <= ctx.getCaretLocation()
                && ctx.getCaretLocation() <= ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), cand)) {
                found = cand;
                break;
            }
        }
        
        if (found == null) return null;
        
        TreePath foundPath = new TreePath(ctx.getPath(), found);
        Element supertype = ctx.getInfo().getTrees().getElement(foundPath);
        
        if (supertype == null || (!supertype.getKind().isClass() && !supertype.getKind().isInterface()))
            return null;
        
        List<ExecutableElement> unimplemented = computeUnimplemented(ctx.getInfo(), typeEl, supertype);
        
        if (!unimplemented.isEmpty()) {
            return ErrorDescriptionFactory.forName(ctx, foundPath, Bundle.ERR_ImplementMethods(((TypeElement) supertype).getQualifiedName().toString()), new ImplementFix(ctx.getInfo(), ctx.getPath(), (TypeElement) typeEl, (TypeElement) supertype).toEditorFix());
        }
        
        return null;
    }

    private static List<ExecutableElement> computeUnimplemented(CompilationInfo info, Element typeEl, Element supertype) {
        List<ExecutableElement> unimplemented = new ArrayList<ExecutableElement>();
        
        for (ExecutableElement ee : info.getElementUtilities().findUnimplementedMethods((TypeElement) typeEl)) {
            if (ee.getEnclosingElement().equals(supertype)) {
                unimplemented.add(ee);
            }
        }
        
        return unimplemented;
    }
    
    private static final class ImplementFix extends JavaFix {
        private final String displayName;
        private final ElementHandle<TypeElement> type;
        private final ElementHandle<TypeElement> supertype;
        
        public ImplementFix(CompilationInfo ci, TreePath clazz, TypeElement type, TypeElement supertype) {
            super(ci, clazz);
            this.displayName = supertype.getQualifiedName().toString();
            this.type = ElementHandle.create(type);
            this.supertype = ElementHandle.create(supertype);
        }

        @Override
        @Messages({
            "# {0} - the FQN of the type whose methods will be implemented",
            "FIX_ImplementSuperTypeMethods=Implement unimplemented abstract methods of {0}"
        })
        protected String getText() {
            return Bundle.FIX_ImplementSuperTypeMethods(displayName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TypeElement type = this.type.resolve(ctx.getWorkingCopy());
            TypeElement supertype = this.supertype.resolve(ctx.getWorkingCopy());
            
            if (type == null || supertype == null) {
                //XXX: log
                return ;
            }
            
            GeneratorUtilities gu = GeneratorUtilities.get(ctx.getWorkingCopy());
            ClassTree ct = gu.insertClassMembers((ClassTree) ctx.getPath().getLeaf(), gu.createAbstractMethodImplementations(type, computeUnimplemented(ctx.getWorkingCopy(), type, supertype)));
            
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), ct);
        }
    }
}
