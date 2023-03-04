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
