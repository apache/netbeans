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
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BreakStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnreachableStatementHint extends HintRule {
    private static final String HINT_ID = "Unreachable.Statement.Hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
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
                hints.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final ArrayDeque<CheckedBlock> blocks;
        private final List<CheckedBlock> processedBlocks;
        private final List<Hint> hints;

        public CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            blocks = new ArrayDeque<>();
            processedBlocks = new ArrayList<>();
            hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            for (CheckedBlock checkedBlock : processedBlocks) {
                ASTNode unreachableStatement = checkedBlock.getUnreachableStatement();
                if (unreachableStatement != null) {
                    createHint(unreachableStatement);
                }
            }
            return hints;
        }

        @NbBundle.Messages("UnreachableStatementHintText=Unreachable Statement")
        private void createHint(ASTNode unreachableStatement) {
            OffsetRange offsetRange = new OffsetRange(unreachableStatement.getStartOffset(), unreachableStatement.getEndOffset());
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(UnreachableStatementHint.this, Bundle.UnreachableStatementHintText(), fileObject, offsetRange, null, 500));
            }
        }

        @Override
        public void visit(ForStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Statement body = node.getBody();
            if (body instanceof Block) {
                super.visit(node);
            } else {
                blocks.push(new CheckedBlock());
                super.visit(node);
                processedBlocks.add(blocks.pop());
            }
        }

        @Override
        public void visit(ForEachStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Statement body = node.getStatement();
            if (body instanceof Block) {
                super.visit(node);
            } else {
                blocks.push(new CheckedBlock());
                super.visit(node);
                processedBlocks.add(blocks.pop());
            }
        }

        @Override
        public void visit(DoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Statement body = node.getBody();
            if (body instanceof Block) {
                super.visit(node);
            } else {
                blocks.push(new CheckedBlock());
                super.visit(node);
                processedBlocks.add(blocks.pop());
            }
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getCondition());
            Statement trueStatement = node.getTrueStatement();
            if (trueStatement instanceof Block) {
                scan(trueStatement);
            } else {
                blocks.push(new CheckedBlock());
                scan(trueStatement);
                processedBlocks.add(blocks.pop());
            }
            Statement falseStatement = node.getFalseStatement();
            if (falseStatement instanceof Block) {
                scan(falseStatement);
            } else {
                blocks.push(new CheckedBlock());
                scan(falseStatement);
                processedBlocks.add(blocks.pop());
            }
        }

        @Override
        public void visit(WhileStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Statement body = node.getBody();
            if (body instanceof Block) {
                super.visit(node);
            } else {
                blocks.push(new CheckedBlock());
                super.visit(node);
                processedBlocks.add(blocks.pop());
            }
        }

        @Override
        public void visit(SwitchCase node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getValue());
            blocks.push(new CheckedBlock());
            scan(node.getActions());
            processedBlocks.add(blocks.pop());
        }

        @Override
        public void visit(Program node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            blocks.push(new CheckedBlock());
            super.visit(node);
            processedBlocks.add(blocks.pop());
        }

        @Override
        public void visit(Block node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            blocks.push(new CheckedBlock());
            super.visit(node);
            processedBlocks.add(blocks.pop());
        }

        @Override
        public void visit(ReturnStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            processLastStatement(node);
        }

        @Override
        public void visit(BreakStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            processLastStatement(node);
        }

        @Override
        public void visit(ContinueStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            processLastStatement(node);
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            // NETBEANS-4443 PHP 8.0: throw statement -> throw expression
            // https://wiki.php.net/rfc/throw_expression
            // i.e.
            // PHP 7.x:
            // <ThrowStatement></ThrowStatement>
            // PHP 8.0:
            // <ExpressionStatement>
            //     <ThrowExpression></ThrowExpression>
            // </ExpressionStatement>
            if (node.getExpression() instanceof ThrowExpression) {
                processLastStatement(node);
            }
        }

        private void processLastStatement(Statement node) {
            if (!blocks.isEmpty()) {
                CheckedBlock lastCheckedBlock = blocks.peek();
                lastCheckedBlock.setLastStatement(node);
            }
        }

        @Override
        public void scan(ASTNode node) {
            if (!blocks.isEmpty()) {
                CheckedBlock lastCheckedBlock = blocks.peek();
                if (lastCheckedBlock.hasLastStatement() && lastCheckedBlock.getUnreachableStatement() == null) {
                    lastCheckedBlock.setUnreachableStatement(node);
                }
            }
            super.scan(node);
        }

    }

    private static final class CheckedBlock {
        private Statement lastStatement;
        private ASTNode unreachableStatement;

        public void setLastStatement(Statement lastStatement) {
            this.lastStatement = lastStatement;
        }

        public boolean hasLastStatement() {
            return lastStatement != null;
        }

        public void setUnreachableStatement(ASTNode unreachableStatement) {
            this.unreachableStatement = unreachableStatement;
        }

        public ASTNode getUnreachableStatement() {
            return unreachableStatement;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("UnreachableStatementHintDesc=Detects unreachable statements after return, throw, break and continue statements.")
    public String getDescription() {
        return Bundle.UnreachableStatementHintDesc();
    }

    @Override
    @NbBundle.Messages("UnreachableStatementHintDisp=Unreachable Statement")
    public String getDisplayName() {
        return Bundle.UnreachableStatementHintDisp();
    }

}
