/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "DN_AbstractMethodCannotHaveBody=Remove invalid modifier",
    "DESC_AbstractMethodCannotHaveBody=Remove invalid modifier",
    "FIX_AbstractMethodCannotHaveBodyRemoveAbstract=Remove abstract modifier",
    "FIX_AbstractMethodCannotHaveBodyRemoveBody=Remove method body"
})
public class AbstractMethodCannotHaveBody implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<>(Arrays.asList(
            "compiler.err.abstract.meth.cant.have.body"
    ));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (treePath.getLeaf().getKind() != Kind.METHOD) return null;
        MethodTree mt = (MethodTree) treePath.getLeaf();
        Fix removeAbstractFix = FixFactory.removeModifiersFix(compilationInfo, new TreePath(treePath, mt.getModifiers()), EnumSet.of(Modifier.ABSTRACT), Bundle.FIX_AbstractMethodCannotHaveBodyRemoveAbstract());
        //TODO: would be better to reused JavaFixUtilities.removeFromParent, but that requires HintContext:
        Fix removeBodyFix = new RemoveBodyFix(compilationInfo, treePath).toEditorFix();
        return Arrays.asList(removeAbstractFix, removeBodyFix);
    }
    

    @Override
    public void cancel() {
    }

    @Override
    public String getId() {
        return AbstractMethodCannotHaveBody.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_AbstractMethodCannotHaveBody();
    }

    public String getDescription() {
        return Bundle.DESC_AbstractMethodCannotHaveBody();
    }
    
    private static final class RemoveBodyFix extends JavaFix {

        public RemoveBodyFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_AbstractMethodCannotHaveBodyRemoveBody();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            MethodTree mt = (MethodTree) ctx.getPath().getLeaf();
            MethodTree nue = ctx.getWorkingCopy().getTreeMaker().Method(mt.getModifiers(), mt.getName(), mt.getReturnType(), mt.getTypeParameters(), mt.getParameters(), mt.getThrows(), (BlockTree) null, (ExpressionTree) mt.getDefaultValue());
            
            ctx.getWorkingCopy().rewrite(mt, nue);
        }
        
    }

}
