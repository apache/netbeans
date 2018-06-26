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
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowStatement;
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
        public void visit(ThrowStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            processLastStatement(node);
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
