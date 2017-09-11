/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class ExtendsImplements implements ErrorRule<Void> {
    
    private static final String KEY_EXTENDS_TO_IMPLEMENTS = "compiler.err.no.intf.expected.here";
    private static final String KEY_IMPLEMENTS_TO_EXTENDS = "compiler.err.intf.expected.here";
    private static final Set<String> CODES = new HashSet<String>(Arrays.asList(KEY_EXTENDS_TO_IMPLEMENTS, KEY_IMPLEMENTS_TO_EXTENDS));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (KEY_EXTENDS_TO_IMPLEMENTS.equals(diagnosticKey)) {
            Element wrong = info.getTrees().getElement(treePath);
            TreePath clazz = findClass(treePath);
            
            if (wrong == null || !wrong.getKind().isInterface() || clazz == null) return null;
            
            return Collections.singletonList(new FixImpl(info, treePath, true).toEditorFix());
        }
        
        if (KEY_IMPLEMENTS_TO_EXTENDS.equals(diagnosticKey)) {
            Element wrong = info.getTrees().getElement(treePath);
            TreePath clazz = findClass(treePath);
            
            if (wrong == null || !wrong.getKind().isClass()|| clazz == null) return null;
            
            return Collections.singletonList(new FixImpl(info, treePath, false).toEditorFix());
        }
        
        return null;
    }

    @Override
    public String getId() {
        return ExtendsImplements.class.getName();
    }

    @Override
    @Messages("DN_ExtendsImplements=Convert between extends and implements")
    public String getDisplayName() {
        return Bundle.DN_ExtendsImplements();
    }

    @Override
    public void cancel() {
    }

    private static TreePath findClass(TreePath treePath) {
        TreePath clazz = treePath;
        while (clazz != null && !TreeUtilities.CLASS_TREE_KINDS.contains(clazz.getLeaf().getKind())) {
            clazz = clazz.getParentPath();
        }
        return clazz;
    }
    
    private static final class FixImpl extends JavaFix {

        private final boolean extends2Implements;
        private final String displayName;

        public FixImpl(CompilationInfo info, TreePath tp, boolean extends2Implements) {
            super(info, tp);
            this.extends2Implements = extends2Implements;
            this.displayName = StringEscapeUtils.escapeHtml(tp.getLeaf().toString()); //TODO: do not use toString
        }
        
        
        @Override
        @Messages({"FIX_Extend2Implements=Convert extends {0} to implements {0}",
                   "FIX_Implements2Extend=Convert implements {0} to extends {0}"})
        protected String getText() {
            return extends2Implements ? Bundle.FIX_Extend2Implements(displayName)
                                      : Bundle.FIX_Implements2Extend(displayName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath clazz = findClass(ctx.getPath());
            
            if (clazz == null) {
                //TODO: should not happen - warn?
                return ;
            }
            
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            ClassTree original = (ClassTree) clazz.getLeaf();
            ClassTree nue;
            
            if (extends2Implements) {
                nue = make.insertClassImplementsClause(make.setExtends(original, null), 0, ctx.getPath().getLeaf());
            } else {
                nue = make.setExtends(make.removeClassImplementsClause(original, ctx.getPath().getLeaf()), (ExpressionTree) ctx.getPath().getLeaf());
            }
            
            ctx.getWorkingCopy().rewrite(original, nue);
        }
        
    }
    
}
