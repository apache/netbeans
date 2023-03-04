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
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class BracesHint extends HintRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = createVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.addAll(checkVisitor.getHints());
            }
        }
    }

    abstract CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument);

    public static final class IfBracesHint extends BracesHint {

        private static final String HINT_ID = "If.Braces.Hint"; //NOI18N

        @Override
        protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new IfVisitor(this, fileObject, baseDocument);
        }

        private static final class IfVisitor extends CheckVisitor {

            public IfVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
                super(bracesHint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("IfBracesHintText=If-Else Statements Must Use Braces")
            public void visit(IfStatement node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                Statement trueStatement = node.getTrueStatement();
                if (trueStatement != null && !(trueStatement instanceof Block)) {
                    addHint(node, trueStatement, Bundle.IfBracesHintText());
                }
                Statement falseStatement = node.getFalseStatement();
                if (falseStatement != null && !(falseStatement instanceof Block) && !(falseStatement instanceof IfStatement)) {
                    addHint(node, falseStatement, Bundle.IfBracesHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("IfBracesHintDesc=If-Else Statements Must Use Braces")
        public String getDescription() {
            return Bundle.IfBracesHintDesc();
        }

        @Override
        @NbBundle.Messages("IfBracesHintDisp=If-Else Statements Must Use Braces")
        public String getDisplayName() {
            return Bundle.IfBracesHintDisp();
        }

    }

    public static final class DoWhileBracesHint extends BracesHint {

        private static final String HINT_ID = "Do.While.Braces.Hint"; //NOI18N

        @Override
        protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new DoWhileVisitor(this, fileObject, baseDocument);
        }

        private static final class DoWhileVisitor extends CheckVisitor {

            public DoWhileVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
                super(bracesHint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("DoWhileBracesHintText=Do-While Loops Must Use Braces")
            public void visit(DoStatement node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                Statement bodyStatement = node.getBody();
                if (bodyStatement != null && !(bodyStatement instanceof Block)) {
                    addHint(node, bodyStatement, Bundle.DoWhileBracesHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("DoWhileBracesHintDesc=Do-While Loops Must Use Braces")
        public String getDescription() {
            return Bundle.DoWhileBracesHintDesc();
        }

        @Override
        @NbBundle.Messages("DoWhileBracesHintDisp=Do-While Loops Must Use Braces")
        public String getDisplayName() {
            return Bundle.DoWhileBracesHintDisp();
        }

    }

    public static final class WhileBracesHint extends BracesHint {

        private static final String HINT_ID = "While.Braces.Hint"; //NOI18N

        @Override
        protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new WhileVisitor(this, fileObject, baseDocument);
        }

        private static final class WhileVisitor extends CheckVisitor {

            public WhileVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
                super(bracesHint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("WhileBracesHintText=While Loops Must Use Braces")
            public void visit(WhileStatement node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                Statement bodyStatement = node.getBody();
                if (bodyStatement != null && !(bodyStatement instanceof Block)) {
                    addHint(node, bodyStatement, Bundle.WhileBracesHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("WhileBracesHintDesc=While Loops Must Use Braces")
        public String getDescription() {
            return Bundle.WhileBracesHintDesc();
        }

        @Override
        @NbBundle.Messages("WhileBracesHintDisp=While Loops Must Use Braces")
        public String getDisplayName() {
            return Bundle.WhileBracesHintDisp();
        }

    }

    public static final class ForBracesHint extends BracesHint {

        private static final String HINT_ID = "For.Braces.Hint"; //NOI18N

        @Override
        protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new ForVisitor(this, fileObject, baseDocument);
        }

        private static final class ForVisitor extends CheckVisitor {

            public ForVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
                super(bracesHint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("ForBracesHintText=For Loops Must Use Braces")
            public void visit(ForStatement node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                Statement bodyStatement = node.getBody();
                if (bodyStatement != null && !(bodyStatement instanceof Block)) {
                    addHint(node, bodyStatement, Bundle.ForBracesHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("ForBracesHintDesc=For Loops Must Use Braces")
        public String getDescription() {
            return Bundle.ForBracesHintDesc();
        }

        @Override
        @NbBundle.Messages("ForBracesHintDisp=For Loops Must Use Braces")
        public String getDisplayName() {
            return Bundle.ForBracesHintDisp();
        }

    }

    public static final class ForEachBracesHint extends BracesHint {

        private static final String HINT_ID = "ForEach.Braces.Hint"; //NOI18N

        @Override
        protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new ForEachVisitor(this, fileObject, baseDocument);
        }

        private static final class ForEachVisitor extends CheckVisitor {

            public ForEachVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
                super(bracesHint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("ForEachBracesHintText=ForEach Loops Must Use Braces")
            public void visit(ForEachStatement node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                Statement bodyStatement = node.getStatement();
                if (bodyStatement != null && !(bodyStatement instanceof Block)) {
                    addHint(node, bodyStatement, Bundle.ForEachBracesHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("ForEachBracesHintDesc=ForEach Loops Must Use Braces")
        public String getDescription() {
            return Bundle.ForEachBracesHintDesc();
        }

        @Override
        @NbBundle.Messages("ForEachBracesHintDisp=ForEach Loops Must Use Braces")
        public String getDisplayName() {
            return Bundle.ForEachBracesHintDisp();
        }

    }

    private abstract static class CheckVisitor extends DefaultVisitor {
        private final List<Hint> hints;
        private final BaseDocument baseDocument;
        private final FileObject fileObject;
        private final BracesHint bracesHint;

        private CheckVisitor(BracesHint bracesHint, FileObject fileObject, BaseDocument baseDocument) {
            this.bracesHint = bracesHint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        protected void addHint(Statement enclosingStatement, Statement node, String description) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (bracesHint.showHint(offsetRange, baseDocument)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.add(new Hint(bracesHint, description, fileObject, offsetRange, Collections.<HintFix>singletonList(new Fix(enclosingStatement, node, baseDocument)), 500));
            }
        }

        public List<Hint> getHints() {
            return hints;
        }

    }

    private static final class Fix implements HintFix {
        private final Statement node;
        private final Statement enclosingStatement;
        private final BaseDocument baseDocument;

        public Fix(Statement enclosingStatement, Statement node, BaseDocument baseDocument) {
            this.node = node;
            this.enclosingStatement = enclosingStatement;
            this.baseDocument = baseDocument;
        }

        @Override
        @NbBundle.Messages("AddBraces=Add Braces")
        public String getDescription() {
            return Bundle.AddBraces();
        }

        @Override
        public void implement() throws Exception {
            int removeLength = enclosingStatement.getEndOffset() - enclosingStatement.getStartOffset();
            EditList editList = new EditList(baseDocument);
            editList.replace(enclosingStatement.getStartOffset(), removeLength, createReplaceText(), true, 0);
            editList.apply();
        }

        private String createReplaceText() throws BadLocationException {
            String leadingText = baseDocument.getText(enclosingStatement.getStartOffset(), node.getStartOffset() - enclosingStatement.getStartOffset());
            String middleText = baseDocument.getText(node.getStartOffset(), node.getEndOffset() - node.getStartOffset());
            String trailingText = baseDocument.getText(node.getEndOffset(), enclosingStatement.getEndOffset() - node.getEndOffset());
            return leadingText + "{\n" + middleText + "\n}" + trailingText; //NOI18N
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

}
