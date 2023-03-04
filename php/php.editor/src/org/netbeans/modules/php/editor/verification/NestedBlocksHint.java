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

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class NestedBlocksHint extends HintRule implements CustomisableRule {
    private static final Logger LOGGER = Logger.getLogger(NestedBlocksHint.class.getName());
    private static final String HINT_ID = "Nested.Blocks.Hint"; //NOI18N
    private static final String NUMBER_OF_ALLOWED_NESTED_BLOCKS = "php.verification.number.of.allowed.nested.blocks"; //NOI18N
    private static final int DEFAULT_NUMBER_OF_ALLOWED_NESTED_BLOCKS = 2;
    private static final String ALLOW_CONDITION_BLOCK = "php.verification.allow.condition.block"; //NOI18N
    private static final boolean DEFAULT_ALLOW_CONDITION_BLOCK = true;
    private Preferences preferences;

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

    private final class CheckVisitor extends DefaultTreePathVisitor {
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<ASTNode> unallowedNestedBlocks;
        private final List<Hint> hints;
        private boolean isInFunctionDeclaration;
        private int countOfNestedBlocks;

        private CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            unallowedNestedBlocks = new ArrayList<>();
            hints = new ArrayList<>();
        }

        @NbBundle.Messages(
                "NestedBlocksHintText=Too Many Nested Blocks in Function Declaration"
                + "\n- It is a good practice to introduce a new function rather than to use more nested blocks."
        )
        private Collection<? extends Hint> getHints() {
            for (ASTNode block : unallowedNestedBlocks) {
                createHint(block);
            }
            return hints;
        }

        private void createHint(ASTNode block) {
            int lineEnd = block.getEndOffset();
            try {
                lineEnd = LineDocumentUtils.getLineEnd(baseDocument, block.getStartOffset());
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            OffsetRange offsetRange = new OffsetRange(block.getStartOffset(), lineEnd);
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(NestedBlocksHint.this, Bundle.NestedBlocksHintText(), fileObject, offsetRange, null, 500));
            }
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getFunctionName());
            scan(node.getFormalParameters());
            Block body = node.getBody();
            if (body != null) {
                isInFunctionDeclaration = true;
                scan(body.getStatements());
                isInFunctionDeclaration = false;
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
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    super.visit(node);
                    countOfNestedBlocks--;
                } else {
                    super.visit(node);
                }
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
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    super.visit(node);
                    countOfNestedBlocks--;
                } else {
                    super.visit(node);
                }
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
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    super.visit(node);
                    countOfNestedBlocks--;
                } else {
                    super.visit(node);
                }
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
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    super.visit(node);
                    countOfNestedBlocks--;
                } else {
                    super.visit(node);
                }
            }
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addToPath(node);
            Statement trueStatement = node.getTrueStatement();
            if (trueStatement instanceof Block) {
                scan((Block) trueStatement);
            } else if (trueStatement != null) {
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    scan(trueStatement);
                    countOfNestedBlocks--;
                } else {
                    scan(trueStatement);
                }
            }
            Statement falseStatement = node.getFalseStatement();
            if (falseStatement instanceof Block) {
                scan((Block) falseStatement);
            } else if (falseStatement instanceof IfStatement) {
                scan((IfStatement) falseStatement);
            } else if (falseStatement != null) {
                if (isInFunctionDeclaration) {
                    countOfNestedBlocks++;
                    evaluatePossiblyUnallowedNestedBlock();
                    scan(falseStatement);
                    countOfNestedBlocks--;
                } else {
                    scan(falseStatement);
                }
            }
        }

        @Override
        public void visit(Block node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (isInFunctionDeclaration) {
                countOfNestedBlocks++;
                evaluatePossiblyUnallowedNestedBlock();
                super.visit(node);
                countOfNestedBlocks--;
            } else {
                super.visit(node);
            }
        }

        private void evaluatePossiblyUnallowedNestedBlock() {
            if ((isUnallowedNestedBlock(Rank.FIRST) && !isAllowedConditionInLoop())
                    || (isUnallowedNestedBlock(Rank.SECOND) && allowConditionBlock(preferences))) {
                unallowedNestedBlocks.add(getParentNode());
            }
        }

        private boolean isAllowedConditionInLoop() {
            return allowConditionBlock(preferences) && (getParentNode() instanceof IfStatement)
                    && isInLoopNode();
        }

        private boolean isInLoopNode() {
            boolean isLoopNode = false;
            List<ASTNode> path = getPath();
            int pathSize = path.size();
            if (pathSize > 1) {
                isLoopNode = isLoopNode(path.get(1));
            }
            if (!isLoopNode && pathSize > 2) {
                isLoopNode = (path.get(1) instanceof Block) && isLoopNode(path.get(2));
            }
            return isLoopNode;
        }

        private boolean isLoopNode(ASTNode node) {
            return (node instanceof WhileStatement) || (node instanceof DoStatement)
                    || (node instanceof ForEachStatement) || (node instanceof ForStatement);
        }

        private ASTNode getParentNode() {
            return getPath().get(0);
        }

        private boolean isUnallowedNestedBlock(Rank rank) {
            int numberOfAllowedNestedBlocks = getNumberOfAllowedNestedBlocks(preferences);
            return countOfNestedBlocks > numberOfAllowedNestedBlocks && (countOfNestedBlocks - numberOfAllowedNestedBlocks) == rank.getDistance();
        }

    }

    private enum Rank {
        FIRST(1),
        SECOND(2);

        private final int distance;

        private Rank(int distance) {
            this.distance = distance;
        }

        public int getDistance() {
            return distance;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("NestedBlocksHintDesc=It is a good practice to introduce a new function (method) rather than to use more nested blocks.")
    public String getDescription() {
        return Bundle.NestedBlocksHintDesc();
    }

    @Override
    @NbBundle.Messages("NestedBlocksHintDisp=Nested Blocks in Functions")
    public String getDisplayName() {
        return Bundle.NestedBlocksHintDisp();
    }

    @Override
    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        JComponent customizer = new NestedHintsCustomizer(preferences, this);
        setNumberOfAllowedNestedBlocks(preferences, getNumberOfAllowedNestedBlocks(preferences));
        setAllowConditionBlock(preferences, allowConditionBlock(preferences));
        return customizer;
    }

    public void setNumberOfAllowedNestedBlocks(Preferences preferences, Integer value) {
        assert preferences != null;
        assert value != null;
        preferences.putInt(NUMBER_OF_ALLOWED_NESTED_BLOCKS, value);
    }

    public int getNumberOfAllowedNestedBlocks(Preferences preferences) {
        assert preferences != null;
        return preferences.getInt(NUMBER_OF_ALLOWED_NESTED_BLOCKS, DEFAULT_NUMBER_OF_ALLOWED_NESTED_BLOCKS);
    }

    public void setAllowConditionBlock(Preferences preferences, boolean isEnabled) {
        assert preferences != null;
        preferences.putBoolean(ALLOW_CONDITION_BLOCK, isEnabled);
    }

    public boolean allowConditionBlock(Preferences preferences) {
        assert preferences != null;
        return preferences.getBoolean(ALLOW_CONDITION_BLOCK, DEFAULT_ALLOW_CONDITION_BLOCK);
    }

}
