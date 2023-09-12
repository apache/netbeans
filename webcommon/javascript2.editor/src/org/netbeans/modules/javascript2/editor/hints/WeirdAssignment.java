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
package org.netbeans.modules.javascript2.editor.hints;

import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.TokenType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider.JsRuleContext;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class WeirdAssignment extends JsAstRule {

    @Override
    void computeHints(JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) {
        WeirdVisitor conventionVisitor = new WeirdVisitor(this);
        conventionVisitor.process(context, hints);
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "jsweirdassignment.hint";
    }

    @NbBundle.Messages("JsWeirdAssignmentDesc=Weird assignment hint informs you about assignments like x = x.")
    @Override
    public String getDescription() {
        return Bundle.JsWeirdAssignmentDesc();
    }

    @NbBundle.Messages("JsWeirdAssignmentDN=Weird assignment")
    @Override
    public String getDisplayName() {
        return Bundle.JsWeirdAssignmentDN();
    }

    private static class WeirdVisitor extends PathNodeVisitor {
        private List<Hint> hints;
        private JsRuleContext context;
        private final Rule rule;

        public WeirdVisitor(Rule rule) {
            this.rule = rule;
        }

        public void process(JsRuleContext context, List<Hint> hints) {
            this.hints = hints;
            this.context = context;
            FunctionNode root = context.getJsParserResult().getRoot();
            if (root != null) {
                context.getJsParserResult().getRoot().accept(this);
            }
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            if (binaryNode.isTokenType(TokenType.ASSIGN) && binaryNode.lhs().toString().equals(binaryNode.rhs().toString())) {
                hints.add(new Hint(rule, Bundle.JsWeirdAssignmentDN(),
                        context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                        ModelUtils.documentOffsetRange(context.getJsParserResult(), binaryNode.getStart(), binaryNode.getFinish()), null, 500));
            }
            return super.enterBinaryNode(binaryNode);
        }
    }
}
