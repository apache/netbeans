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
package org.netbeans.modules.javascript2.editor.hints;

import com.oracle.js.parser.Source;
import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.LiteralNode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import static org.netbeans.modules.javascript2.editor.JsVersion.ECMA12;
import static org.netbeans.modules.javascript2.editor.hints.EcmaLevelRule.ecmaEditionProjectBelow;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

public class Ecma12Rule extends EcmaLevelRule {

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        if (ecmaEditionProjectBelow(context, ECMA12)) {
            Ecma12Visitor visitor = new Ecma12Visitor();
            visitor.process(context, hints);
        }
    }

    private void addHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range) {
        addDocumenHint(context, hints, ModelUtils.documentOffsetRange(context.getJsParserResult(),
                range.getStart(), range.getEnd()));
    }

    private void addDocumenHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range) {
        hints.add(new Hint(this, Bundle.Ecma12Desc(),
                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                range, Collections.singletonList(
                        new SwitchToEcmaXFix(context.getJsParserResult().getSnapshot(), ECMA12)), 600));
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "ecma12.hint";
    }

    @NbBundle.Messages("Ecma12Desc=ECMA12 feature used in pre-ECMA12 source")
    @Override
    public String getDescription() {
        return Bundle.Ecma12Desc();
    }

    @NbBundle.Messages("Ecma12DisplayName=ECMA12 feature used")
    @Override
    public String getDisplayName() {
        return Bundle.Ecma12DisplayName();
    }

    private class Ecma12Visitor extends PathNodeVisitor {

        private List<Hint> hints;

        private JsHintsProvider.JsRuleContext context;

        public void process(JsHintsProvider.JsRuleContext context, List<Hint> hints) {
            this.hints = hints;
            this.context = context;
            FunctionNode root = context.getJsParserResult().getRoot();
            if (root != null) {
                context.getJsParserResult().getRoot().accept(this);
            }
        }

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            if(identNode.isPrivate()) { // Private Identifiers
                addHint(context, hints, new OffsetRange(
                        identNode.getStart(),
                        identNode.getFinish())
                );
            }
            return super.enterIdentNode(identNode);
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            if(accessNode.getProperty().startsWith("#")) { // Private Identifier // NOI18N
                addHint(context, hints, new OffsetRange(
                        accessNode.getStart(),
                        accessNode.getFinish())
                );
            }
            return super.enterAccessNode(accessNode);
        }

        @Override
        public boolean enterLiteralNode(LiteralNode literalNode) {
            if(literalNode.isNumeric()) {
                Source source = context.getJsParserResult().getRoot().getSource();
                String image = source.getString(literalNode.getToken());
                if (image.contains("_") || image.endsWith("n")) {
                    addHint(context, hints, new OffsetRange(
                            literalNode.getStart(),
                            literalNode.getFinish())
                    );
                }
            }

            return super.enterLiteralNode(literalNode);
        }
    }
}
