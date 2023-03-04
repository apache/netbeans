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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TooManyReturnStatementsHint extends HintRule {
    private static final String HINT_ID = "too.many.return.statements.hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private static final int MAX_NUMBER_OF_STATEMENTS = 1;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;
        private final Deque<ReturnStatementsHolder> functionStack;

        private CheckVisitor(FileObject fileObject, BaseDocument doc) {
            this.fileObject = fileObject;
            this.baseDocument = doc;
            hints = new ArrayList<>();
            functionStack = new ArrayDeque<>();
        }

        private Collection<? extends Hint> getHints() {
            return hints;
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            functionStack.push(new ReturnStatementsHolder());
            super.visit(node);
            createHints(functionStack.pop());
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            functionStack.push(new ReturnStatementsHolder());
            super.visit(node);
            createHints(functionStack.pop());
        }

        @Override
        public void visit(ReturnStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            ReturnStatementsHolder returnStatementsHolder = functionStack.peek();
            if (returnStatementsHolder != null) {
                returnStatementsHolder.addReturnStatement(node);
            }
        }

        private void createHints(ReturnStatementsHolder returnStatementsHolder) {
            List<ReturnStatement> returnStatements = returnStatementsHolder.getReturnStatements();
            if (returnStatements.size() > MAX_NUMBER_OF_STATEMENTS) {
                for (ReturnStatement returnStatement : returnStatements) {
                    hints.add(createHint(returnStatement));
                }
            }
        }

        @NbBundle.Messages("TooManyReturnStatementsHintText=Too Many Return Statements")
        private Hint createHint(ReturnStatement node) {
            Hint result = null;
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (showHint(offsetRange, baseDocument)) {
                result = new Hint(
                        TooManyReturnStatementsHint.this,
                        Bundle.TooManyReturnStatementsHintText(),
                        fileObject,
                        offsetRange,
                        null,
                        500);
            }
            return result;
        }

    }

    private static final class ReturnStatementsHolder {
        private final List<ReturnStatement> returnStatements;

        public ReturnStatementsHolder() {
            returnStatements = new ArrayList<>();
        }

        private void addReturnStatement(ReturnStatement node) {
            returnStatements.add(node);
        }

        private List<ReturnStatement> getReturnStatements() {
            return returnStatements;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages(
        "TooManyReturnStatementsHintDesc=It is a good practice to have just one return point from functions and methods. "
            + "It makes it more difficult to read such a function where more return statements are used."
    )
    public String getDescription() {
        return Bundle.TooManyReturnStatementsHintDesc();
    }

    @Override
    @NbBundle.Messages("TooManyReturnStatementsHintDisp=Too Many Return Statements")
    public String getDisplayName() {
        return Bundle.TooManyReturnStatementsHintDisp();
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

}
