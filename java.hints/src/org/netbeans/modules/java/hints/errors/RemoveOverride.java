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
