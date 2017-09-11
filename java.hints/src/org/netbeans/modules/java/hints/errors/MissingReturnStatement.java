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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class MissingReturnStatement implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.missing.ret.stmt",
            "compiler.err.prob.found.req/compiler.misc.incompatible.ret.type.in.lambda/compiler.misc.missing.ret.val",
            "compiler.err.prob.found.req/compiler.misc.incompatible.ret.type.in.lambda/compiler.misc.inconvertible.types"
            ));

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath method = null;
        if (diagnosticKey != null && diagnosticKey.contains("/compiler.misc.incompatible.ret.type.in.lambda/")) { // NOI18N
            // PENDING: when issue #258201 is implemented, use the new method instead of this HACK
            offset++;
        }
        TreePath tp = compilationInfo.getTreeUtilities().pathFor(offset);

        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            Kind kind = tp.getLeaf().getKind();
            if (kind == Kind.METHOD || kind == Kind.LAMBDA_EXPRESSION) {
                method = tp;
                break;
            }

            tp = tp.getParentPath();
        }

        if (method == null) {
            return null;
        }
        
        if (method.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) tp.getLeaf();
            if (mt.getReturnType() == null) {
                return null;
            }
        } else if (method.getLeaf().getKind() == Kind.LAMBDA_EXPRESSION) {
            LambdaExpressionTree let = (LambdaExpressionTree)method.getLeaf();
            TreePath bodyPath = new TreePath(method, let.getBody());
            if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                TypeMirror m = compilationInfo.getTrees().getTypeMirror(
                    bodyPath);
                if (m == null) {
                    return null;
                }
                if (m.getKind() == TypeKind.ERROR) {
                    m = compilationInfo.getTrees().getOriginalType((ErrorType)m);
                }
                if (m.getKind() != TypeKind.VOID) {
                    // do not offer to add return for something, which already has
                    // some type
                    return null;
                }
            } else if (Utilities.exitsFromAllBranchers(compilationInfo, bodyPath)) {
                // do not add return, returns are already there.
                return null;
            }
        }

        List<Fix> result = new ArrayList<Fix>(2);

        result.add(new FixImpl(compilationInfo.getSnapshot().getSource(), TreePathHandle.create(tp, compilationInfo)));
        if (method.getLeaf().getKind() == Kind.METHOD) {
            result.add(new ChangeMethodReturnType.FixImpl(compilationInfo, tp, TypeMirrorHandle.create(compilationInfo.getTypes().getNoType(TypeKind.VOID)), "void").toEditorFix());
        }

        return result;
    }

    @Override
    public String getId() {
        return MissingReturnStatement.class.getCanonicalName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MissingReturnStatement.class, "DN_MissingReturnStatement");
    }

    @Override
    public void cancel() {}

    private static final class FixImpl implements Fix {

        private final Source source;
        private final TreePathHandle methodHandle;

        public FixImpl(Source source, TreePathHandle methodHandle) {
            this.source = source;
            this.methodHandle = methodHandle;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissingReturnStatement.class, "FIX_AddReturnStatement");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ModificationResult mr = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                @Override public void run(ResultIterator resultIterator) throws Exception {
                    WorkingCopy wc = WorkingCopy.get(resultIterator.getParserResult());

                    wc.toPhase(Phase.RESOLVED);

                    TreePath method = methodHandle.resolve(wc);
                    TypeMirror type;
                    BlockTree body;
                    TreeMaker make = wc.getTreeMaker();
                    
                    if (method.getLeaf().getKind() == Kind.METHOD) {
                        Element methodEl = method != null ? wc.getTrees().getElement(method) : null;

                        if (methodEl == null || methodEl.getKind() != ElementKind.METHOD) {
                            return ;
                        }
                    
                        assert method.getLeaf().getKind() == Kind.METHOD;

                        body = ((MethodTree) method.getLeaf()).getBody();
                        if (body == null) {
                            return;
                        }
                        type = ((ExecutableElement) methodEl).getReturnType();
                    } else if (method.getLeaf().getKind() == Kind.LAMBDA_EXPRESSION) {
                        LambdaExpressionTree let = (LambdaExpressionTree)method.getLeaf();
                        if (let.getBody() == null) {
                            return;
                        }
                        if (let.getBody().getKind() == Kind.BLOCK) {
                            body = (BlockTree)let.getBody();
                        } else if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                            body = make.Block(Collections.singletonList(
                                    make.ExpressionStatement((ExpressionTree)let.getBody())), false);
                            wc.rewrite(let.getBody(), body);
                        } else {
                            // surround in braces
                            body = make.Block(Collections.singletonList((StatementTree)let.getBody()), false);
                            wc.rewrite(let.getBody(), body);
                        }
                        TypeMirror t = wc.getTrees().getTypeMirror(method);
                        if (t == null || t.getKind() != TypeKind.DECLARED) {
                            return;
                        }
                        ExecutableType et = wc.getTypeUtilities().getDescriptorType((DeclaredType)t);
                        if (!Utilities.isValidType(et)) {
                            return;
                        }
                        type = et.getReturnType();
                    } else {
                        return;
                    }
                    
                    TypeKind kind = type.getKind();
                    Object value;
                    if (kind.isPrimitive()) {
                        if (kind == TypeKind.BOOLEAN) {
                            value = false;
                        }
                        else {
                            value = 0;
                        }
                    }
                    else {
                        value = null;
                    }

                    LiteralTree nullValue = make.Literal(value);

                    wc.tag(nullValue, Utilities.TAG_SELECT);
                    wc.rewrite(body, make.addBlockStatement(body, make.Return(nullValue)));
                }
            });

            return Utilities.commitAndComputeChangeInfo(source.getFileObject(), mr);
        }

    }
}
