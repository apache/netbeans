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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class DeclarationForInstanceOf implements TreeRule {

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.INSTANCE_OF);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return run(compilationInfo, treePath, CaretAwareJavaSourceTaskFactory.getLastPosition(compilationInfo.getFileObject()));
    }
    
    List<ErrorDescription> run(CompilationInfo info, TreePath treePath, int offset) {
        TreePath ifPath = treePath;
        
        while (ifPath != null) {
            Kind lk = ifPath.getLeaf().getKind();
            
            if (lk == Kind.IF) {
                break;
            }
            
            if (lk == Kind.METHOD || TreeUtilities.CLASS_TREE_KINDS.contains(lk)) {
                return null;
            }
            
            ifPath = ifPath.getParentPath();
        }
        
        if (ifPath == null) {
            return null;
        }
        
        InstanceOfTree leaf = (InstanceOfTree) treePath.getLeaf();
        
        if (leaf.getType() == null || leaf.getType().getKind() == Kind.ERRONEOUS) {
            return null;
        }
        
        TypeMirror castTo = info.getTrees().getTypeMirror(new TreePath(treePath, leaf.getType()));
        TreePath expression = new TreePath(treePath, leaf.getExpression());
        TypeMirror expressionType = info.getTrees().getTypeMirror(expression);
        
        if (!(Utilities.isValidType(castTo) && Utilities.isValidType(expressionType)) || !info.getTypeUtilities().isCastable(expressionType, castTo)) {
            return null;
        }
        
        List<Fix> fix = Collections.<Fix>singletonList(new FixImpl(info.getJavaSource(), TreePathHandle.create(ifPath, info), TreePathHandle.create(expression, info), TypeMirrorHandle.create(castTo), Utilities.getName(castTo)));
        String displayName = NbBundle.getMessage(DeclarationForInstanceOf.class, "ERR_DeclarationForInstanceof");
        ErrorDescription err = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, fix, info.getFileObject(), offset, offset);

        return Collections.singletonList(err);
    }

    public String getId() {
        return DeclarationForInstanceOf.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DeclarationForInstanceOf.class, "DN_DeclarationForInstanceof");
    }

    public void cancel() {
    }

    static final class FixImpl implements Fix {

        private JavaSource js;
        private TreePathHandle ifHandle;
        private TreePathHandle expression;
        private TypeMirrorHandle type;
        private String name;

        public FixImpl(JavaSource js, TreePathHandle ifHandle, TreePathHandle expression, TypeMirrorHandle type, String name) {
            this.js = js;
            this.ifHandle = ifHandle;
            this.expression = expression;
            this.type = type;
            this.name = name;
        }
        
        public String getText() {
            return NbBundle.getMessage(DeclarationForInstanceOf.class, "FIX_CreateDeclaration");
        }

        public ChangeInfo implement() throws Exception {
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    
                    TreePath ifTP = ifHandle.resolve(wc);
                    TreePath resolvedExpression = expression.resolve(wc);
                    TypeMirror resolvedType = type.resolve(wc);
                    
                    if (ifTP == null || resolvedType == null || resolvedExpression == null) {
                        return ;
                    }
                    
                    IfTree ift = (IfTree) ifTP.getLeaf();
                    StatementTree then = ift.getThenStatement();
                    
                    if (then.getKind() == Kind.ERRONEOUS) {
                        return ; //TODO.
                    }
                    
                    List<StatementTree> statements = new LinkedList<StatementTree>();
                    
                    if (then.getKind() == Kind.BLOCK) {
                        statements.addAll(((BlockTree) then).getStatements());
                    } else {
                        statements.add(then);
                    }
                    
                    TreeMaker make = wc.getTreeMaker();
                    VariableTree decl = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(resolvedType), make.TypeCast(make.Type(resolvedType), (ExpressionTree) resolvedExpression.getLeaf()));
                    
                    statements.add(0, decl);
                    
                    BlockTree nue = make.Block(statements, false);
                    
                    wc.rewrite(then, nue);
                }
            }).commit();
            return null;
        }
        
    }
}
