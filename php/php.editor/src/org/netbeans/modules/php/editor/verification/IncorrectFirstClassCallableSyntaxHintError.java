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
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Dispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class IncorrectFirstClassCallableSyntaxHintError extends HintErrorRule {

    private FileObject fileObject;

    @Override
    @NbBundle.Messages("IncorrectFirstClassCallableSyntaxHintError.displayName=Incorrect First Class Callable Syntax")
    public String getDisplayName() {
        return Bundle.IncorrectFirstClassCallableSyntaxHintError_displayName();
    }

    @Override
    @NbBundle.Messages({
        "# {0} - with or as",
        "IncorrectFirstClassCallableSyntaxHintError.incorrectArgs=Cannot use \"(...)\" {0}",
        "IncorrectFirstClassCallableSyntaxHintError.withNewExpression= with new expression",
        "IncorrectFirstClassCallableSyntaxHintError.asAttributeArgs= as attribute argument",
        "IncorrectFirstClassCallableSyntaxHintError.withNullSafeOperator= with null safe operator",
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            for (FirstClassCallableArg incorrectCtorArg : checkVisitor.getIncorrectConstructorArgs()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectCtorArg, Bundle.IncorrectFirstClassCallableSyntaxHintError_incorrectArgs(Bundle.IncorrectFirstClassCallableSyntaxHintError_withNewExpression()), hints);
            }
            for (FirstClassCallableArg incorrectAttributeArg : checkVisitor.getIncorrectAttributeArgs()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectAttributeArg, Bundle.IncorrectFirstClassCallableSyntaxHintError_incorrectArgs(Bundle.IncorrectFirstClassCallableSyntaxHintError_asAttributeArgs()), hints);
            }
            for (FirstClassCallableArg incorrectMethodInvocationArg : checkVisitor.getIncorrectMethodInvocationArgs()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectMethodInvocationArg, Bundle.IncorrectFirstClassCallableSyntaxHintError_incorrectArgs(Bundle.IncorrectFirstClassCallableSyntaxHintError_withNullSafeOperator()), hints);
            }
        }
    }

    private void addHint(ASTNode node, String description, List<Hint> hints) {
        addHint(node, description, hints, Collections.emptyList());
    }

    private void addHint(ASTNode node, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                fixes,
                500
        ));
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final Set<FirstClassCallableArg> incorrectConstructorArgs = new HashSet<>();
        private final Set<FirstClassCallableArg> incorrectAttributeArgs = new HashSet<>();
        private final Set<FirstClassCallableArg> incorrectMethodInvocationArgs = new HashSet<>();

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkIncorrectArgs(node.ctorParams(), incorrectConstructorArgs);
            super.visit(node);
        }

        @Override
        public void visit(AttributeDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getParameters() != null) {
                checkIncorrectArgs(node.getParameters(), incorrectAttributeArgs);
            }
            super.visit(node);
        }

        @Override
        public void visit(MethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (hasNullSafeOperator(node)) {
                FunctionInvocation method = node.getMethod();
                checkIncorrectArgs(method.getParameters(), incorrectMethodInvocationArgs);
            }
            super.visit(node);
        }

        private boolean hasNullSafeOperator(Dispatch dispatch) {
            if (dispatch.isNullsafe()) {
                return true;
            }
            if (!(dispatch.getDispatcher() instanceof Dispatch)) {
                return false;
            }
            return hasNullSafeOperator((Dispatch) dispatch.getDispatcher());
        }

        private void checkIncorrectArgs(List<Expression> params, Set<FirstClassCallableArg> incorrectArgs) {
            for (Expression param : params) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (param instanceof FirstClassCallableArg) {
                    incorrectArgs.add((FirstClassCallableArg) param);
                    break;
                }
            }
        }

        public Set<FirstClassCallableArg> getIncorrectConstructorArgs() {
            return Collections.unmodifiableSet(incorrectConstructorArgs);
        }

        public Set<FirstClassCallableArg> getIncorrectAttributeArgs() {
            return Collections.unmodifiableSet(incorrectAttributeArgs);
        }

        public Set<FirstClassCallableArg> getIncorrectMethodInvocationArgs() {
            return Collections.unmodifiableSet(incorrectMethodInvocationArgs);
        }

    }
}
