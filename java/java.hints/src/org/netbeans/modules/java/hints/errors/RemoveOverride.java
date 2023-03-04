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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_RemoveOverride=Remove @Override Annotation",
    "FIX_RemoveOverride=Remove @Override Annotation"
})
public class RemoveOverride implements ErrorRule<Void> {

    private static final Set<String> CODES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("compiler.err.method.does.not.override.superclass")));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        treePath = compilationInfo.getTreeUtilities().pathFor(offset + 1);

        //#222582: treePath.getParentPath() may be null under unknown conditions:
        if (treePath.getParentPath() == null) return null;
        
        if (treePath.getParentPath().getLeaf().getKind() == Kind.MODIFIERS)
            return Collections.<Fix>singletonList(new FixImpl(compilationInfo, treePath.getParentPath()).toEditorFix());
        return null;
    }

    @Override
    public String getId() {
        return RemoveOverride.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_RemoveOverride();
    }

    @Override
    public void cancel() {}
    
    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_RemoveOverride();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            ModifiersTree mt = (ModifiersTree) ctx.getPath().getLeaf();
            
            for (AnnotationTree at : mt.getAnnotations()) {
                Element el = ctx.getWorkingCopy().getTrees().getElement(new TreePath(ctx.getPath(), at));
                
                if (el != null && el.getKind().isInterface() && ((TypeElement ) el).getQualifiedName().contentEquals("java.lang.Override")) {
                    ctx.getWorkingCopy().rewrite(mt, ctx.getWorkingCopy().getTreeMaker().removeModifiersAnnotation(mt, at));
                    return ;
                }
            }
        }
        
    }
}
