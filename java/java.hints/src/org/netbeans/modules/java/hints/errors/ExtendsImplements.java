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
