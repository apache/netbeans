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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ErrorControlOperatorHint extends HintRule {
    private static final String HINT_ID = "error.control.operator.hint"; //NOI18N
    private static final Set<IgnoreErrorValidator> IGNORE_ERROR_VALIDATORS = new HashSet<>();
    static {
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("fopen")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("unlink")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("mysql_connect")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("ob_end_clean")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("ob_end_flush")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("mkdir")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new FunctionInvocationValidator("iconv")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new MethodInvocationValidator(QualifiedName.create("\\DOMDocument"), "loadHTML")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new MethodInvocationValidator(QualifiedName.create("\\DOMDocument"), "loadHTMLFile")); //NOI18N
        IGNORE_ERROR_VALIDATORS.add(new MethodInvocationValidator(QualifiedName.create("\\DOMDocument"), "loadXML")); //NOI18N
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc, phpParseResult.getModel());
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final Model model;
        private final List<Hint> hints;

        private CheckVisitor(FileObject fileObject, BaseDocument baseDocument, Model model) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.model = model;
            this.hints = new ArrayList<>();
        }

        private Collection<? extends Hint> getHints() {
            return hints;
        }

        @Override
        @NbBundle.Messages("ErrorControlOperatorHintText=Error Control Operator Misused")
        public void visit(IgnoreError node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (!isValidCase(node)) {
                createHint(node);
            }
        }

        private void createHint(IgnoreError node) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        ErrorControlOperatorHint.this,
                        Bundle.ErrorControlOperatorHintText(),
                        fileObject,
                        offsetRange,
                        Collections.<HintFix>singletonList(new Fix(node, baseDocument)),
                        500));
            }
        }

        private boolean isValidCase(IgnoreError node) {
            boolean result = false;
            for (IgnoreErrorValidator ignoreErrorValidator : IGNORE_ERROR_VALIDATORS) {
                if (ignoreErrorValidator.isValid(node, model)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

    }

    private static final class Fix implements HintFix {
        private static final String ERROR_CONTROL_OPERATOR = "@"; //NOI18N
        private final IgnoreError node;
        private final BaseDocument baseDocument;

        private Fix(IgnoreError node, BaseDocument baseDocument) {
            this.node = node;
            this.baseDocument = baseDocument;
        }

        @Override
        @NbBundle.Messages("ErrorControlOperatorHintFix=Remove Error Control Operator")
        public String getDescription() {
            return Bundle.ErrorControlOperatorHintFix();
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(baseDocument);
            editList.replace(node.getStartOffset(), ERROR_CONTROL_OPERATOR.length(), "", true, 0); //NOI18N
            editList.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    private interface IgnoreErrorValidator {
        boolean isValid(IgnoreError node, Model model);
    }

    private static final class FunctionInvocationValidator implements IgnoreErrorValidator {
        private final String functionName;

        public FunctionInvocationValidator(String functionName) {
            assert functionName != null;
            this.functionName = functionName.toLowerCase();
        }

        @Override
        public boolean isValid(IgnoreError node, Model model) {
            assert node != null;
            assert model != null;
            boolean result = false;
            Expression expression = node.getExpression();
            if (expression instanceof FunctionInvocation) {
                result = isValid((FunctionInvocation) expression);
            }
            return result;
        }

        private boolean isValid(FunctionInvocation functionInvocation) {
            boolean result = false;
            String functionInvocationName = CodeUtils.extractFunctionName(functionInvocation);
            if (functionInvocationName != null) {
                result = functionName.equalsIgnoreCase(functionInvocationName);
            }
            return result;
        }

    }

    private static final class MethodInvocationValidator implements IgnoreErrorValidator {
        private final QualifiedName fullyQualifiedTypeName;
        private final String methodName;

        public MethodInvocationValidator(QualifiedName fullyQualifiedTypeName, String methodName) {
            assert fullyQualifiedTypeName != null;
            assert fullyQualifiedTypeName.getKind().isFullyQualified();
            assert methodName != null;
            this.fullyQualifiedTypeName = fullyQualifiedTypeName;
            this.methodName = methodName;
        }

        @Override
        public boolean isValid(IgnoreError node, Model model) {
            assert node != null;
            assert model != null;
            boolean result = false;
            Expression expression = node.getExpression();
            if (expression instanceof MethodInvocation) {
                result = isValid((MethodInvocation) expression, model);
            }
            return result;
        }

        private boolean isValid(MethodInvocation methodInvocation, Model model) {
            boolean result = false;
            String methodInvocationName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            if (methodName.equals(methodInvocationName)) {
                Collection<? extends TypeScope> types = ModelUtils.resolveType(model, methodInvocation);
                TypeScope type = ModelUtils.getFirst(types);
                if (type != null) {
                    result = fullyQualifiedTypeName.equals(type.getFullyQualifiedName());
                }
            }
            return result;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("ErrorControlOperatorHintDesc=Error control operator disables all error reporting for an affected expression. "
            + "It should be used only for some special cases (like fopen(), unlink(), etc.). "
            + "Otherwise it's a cause of an unexpected behavior of the application. Handle your errors in a common way.")
    public String getDescription() {
        return Bundle.ErrorControlOperatorHintDesc();
    }

    @Override
    @NbBundle.Messages("ErrorControlOperatorHintDisp=Error Control Operator Misused")
    public String getDisplayName() {
        return Bundle.ErrorControlOperatorHintDisp();
    }

}
