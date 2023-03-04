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
package org.netbeans.modules.groovy.editor.hints;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovySelectionRule;
import org.openide.util.NbBundle;

public class SurroundWithHint extends GroovySelectionRule {

    public static final Logger LOG = Logger.getLogger(SurroundWithHint.class.getName()); // NOI18N

    private enum Operation {
        COMMENT_OUT,
        ADD_IF
    };

    @Override
    @NbBundle.Messages({
        "CommentOutRuleHintDescription=Surround with /*selection*/",
        "AddIfAroundBlockHintDescription=Surround with if (exp) {...|}"
    })
    public void run(RuleContext context, List<Hint> result) {
        ParserResult info = context.parserResult;
        int start = context.selectionStart;
        int end = context.selectionEnd;

        assert start < end;

        BaseDocument baseDoc = context.doc;

        if (end > baseDoc.getLength()) {
            return;
        }

        if (end - start > 1000) {
            // Avoid doing tons of work when the user does a Ctrl-A to select all in a really
            // large buffer.
            return;
        }

        ASTNode root = ASTUtils.getRoot(info);

        if (root == null) {
            return;
        }

        OffsetRange range = new OffsetRange(start, end);

        result.add(getDescriptor(Operation.COMMENT_OUT, Bundle.CommentOutRuleHintDescription(), context, baseDoc, range));
        result.add(getDescriptor(Operation.ADD_IF, Bundle.AddIfAroundBlockHintDescription(), context, baseDoc, range));
    }

    private Hint getDescriptor(Operation operation, String description, RuleContext context,
        BaseDocument baseDoc, OffsetRange range) {

        int DEFAULT_PRIORITY = 292;
        HintFix fixToApply = new SimpleFix(operation, description, baseDoc, context);

        List<HintFix> fixList = new ArrayList<>(1);
        fixList.add(fixToApply);
        // FIXME parsing API
        Hint descriptor = new Hint(this, fixToApply.getDescription(), context.parserResult.getSnapshot().getSource().getFileObject(), range,
            fixList, DEFAULT_PRIORITY);

        return descriptor;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    @NbBundle.Messages("CommentOutRuleDescription=Surround with ...")
    public String getDisplayName() {
        return Bundle.CommentOutRuleDescription();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    private static class SimpleFix implements HintFix {

        final BaseDocument baseDoc;
        final String desc;
        final RuleContext context;
        final Operation operation;

        public SimpleFix(Operation operation, String desc, BaseDocument baseDoc, RuleContext context) {
            this.desc = desc;
            this.baseDoc = baseDoc;
            this.context = context;
            this.operation = operation;
        }

        @Override
        public String getDescription() {
            return desc;
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(baseDoc);

            int start = context.selectionStart;
            int end = context.selectionEnd;

            JTextComponent component = Utilities.getFocusedComponent();

            switch (operation) {
                case COMMENT_OUT:
                    edits.replace(end, 0, "*/", false, 0);
                    edits.replace(start, 0, "/*", false, 1);
                    edits.apply();

                    // Clear selection 
                    component.setCaretPosition(start);

                    break;
                case ADD_IF:
                    String START_INSERT = "if (true) {\n";
                    String END_INSERT = "\n}";

                    edits.replace(end, 0, END_INSERT, false, 0);

                    int startOfRow = Utilities.getRowStart(baseDoc, start);

                    edits.replace(startOfRow, 0, START_INSERT, false, 1);
                    edits.setFormatAll(true);
                    edits.apply();

                    component.setCaretPosition(start + 4);
                    component.moveCaretPosition(start + 8);

                    break;
            }
        }

        @Override
        public boolean isSafe() {
            return false;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }
}
