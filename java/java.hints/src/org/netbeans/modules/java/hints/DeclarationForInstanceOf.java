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
