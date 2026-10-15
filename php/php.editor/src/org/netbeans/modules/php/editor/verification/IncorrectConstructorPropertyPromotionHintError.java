/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle incorrect declaration of Constructor Property Promotion.
 */
public class IncorrectConstructorPropertyPromotionHintError extends HintErrorRule {

    private FileObject fileObject;

    @Override
    @NbBundle.Messages("IncorrectConstructorPropertyPromotionHintError.displayName=Incorrect Declaration of Constructor Property Promotion")
    public String getDisplayName() {
        return Bundle.IncorrectConstructorPropertyPromotionHintError_displayName();
    }

    @Override
    @NbBundle.Messages({
        "IncorrectConstructorPropertyPromotionHintError.abstract.desc=Can't declare a promoted property in an abstract constructor.",
        "IncorrectConstructorPropertyPromotionHintError.non.constructor.desc=Can't declare a promoted property in a function/method other than a constructor.",
        "IncorrectConstructorPropertyPromotionHintError.with.variadic.desc=Can't declare a promoted property with a variadic parameter.",
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
            for (FormalParameter parameter : checkVisitor.getIncorrectAbstractConstructorParameters()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(parameter, Bundle.IncorrectConstructorPropertyPromotionHintError_abstract_desc(), hints);
            }
            for (FormalParameter parameter : checkVisitor.getIncorrectFunctionParameters()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(parameter, Bundle.IncorrectConstructorPropertyPromotionHintError_non_constructor_desc(), hints);
            }
            for (FormalParameter parameter : checkVisitor.getIncorrectVariadicParameters()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(parameter, Bundle.IncorrectConstructorPropertyPromotionHintError_with_variadic_desc(), hints);
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

        private final Set<FormalParameter> incorrectAbstractConstructorParameters = new HashSet<>();
        private final Set<FormalParameter> incorrectFunctionParameters = new HashSet<>();
        private final Set<FormalParameter> incorrectVariadicParameters = new HashSet<>();

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            FunctionDeclaration function = node.getFunction();
            List<FormalParameter> parameters = function.getFormalParameters();
            if (CodeUtils.isConstructor(node)) {
                for (FormalParameter parameter : parameters) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (BodyDeclaration.Modifier.isVisibilityModifier(parameter.getModifier())
                            || BodyDeclaration.Modifier.isSetVisibilityModifier(parameter.getModifier())) {
                        Block body = function.getBody();
                        if (body == null
                                || (!body.isCurly() && body.getStatements().isEmpty())) {
                            // abstract constructor
                            // e.g.
                            // abstract public function __construct(private int $filed);
                            // public function __construct(private int $field);
                            incorrectAbstractConstructorParameters.add(parameter);
                        }
                        if (parameter.isVariadic()) {
                            // with variadic
                            // e.g.
                            // public function __construct(private string ...$variadic){}
                            incorrectVariadicParameters.add(parameter);
                        }
                    }
                }
                scan(node.getAttributes());
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkParameters(node.getFormalParameters());
            super.visit(node);
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkParameters(node.getFormalParameters());
            super.visit(node);
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkParameters(node.getFormalParameters());
            super.visit(node);
        }

        private void checkParameters(List<FormalParameter> parameters) {
            for (FormalParameter parameter : parameters) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (BodyDeclaration.Modifier.isVisibilityModifier(parameter.getModifier())
                        || BodyDeclaration.Modifier.isSetVisibilityModifier(parameter.getModifier())) {
                    // other than a constructor
                    // e.g.
                    // public function freeMethod(private $field) {}
                    // function freeFunction(private $field) {}
                    incorrectFunctionParameters.add(parameter);
                }
            }
        }

        public Set<FormalParameter> getIncorrectAbstractConstructorParameters() {
            return Collections.unmodifiableSet(incorrectAbstractConstructorParameters);
        }

        public Set<FormalParameter> getIncorrectFunctionParameters() {
            return Collections.unmodifiableSet(incorrectFunctionParameters);
        }

        public Set<FormalParameter> getIncorrectVariadicParameters() {
            return Collections.unmodifiableSet(incorrectVariadicParameters);
        }

    }
}
