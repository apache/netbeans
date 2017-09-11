/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
