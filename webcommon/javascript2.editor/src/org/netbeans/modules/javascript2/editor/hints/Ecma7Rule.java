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

import com.oracle.js.parser.Token;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ExportNode;
import com.oracle.js.parser.ir.Expression;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import static org.netbeans.modules.javascript2.editor.JsVersion.ECMA7;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

public class Ecma7Rule extends EcmaLevelRule {

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        if (ecmaEditionProjectBelow(context, ECMA7)) {
            Ecma7Visitor visitor = new Ecma7Visitor();
            visitor.process(context, hints);
        }
    }

    private void addHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range) {
        addDocumenHint(context, hints, ModelUtils.documentOffsetRange(context.getJsParserResult(),
                range.getStart(), range.getEnd()));
    }

    private void addDocumenHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range) {
        hints.add(new Hint(this, Bundle.Ecma7Desc(),
                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                range, Collections.singletonList(
                        new SwitchToEcmaXFix(context.getJsParserResult().getSnapshot(), ECMA7)), 600));
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "ecma7.hint";
    }

    @NbBundle.Messages("Ecma7Desc=ECMA7 feature used in pre-ECMA7 source")
    @Override
    public String getDescription() {
        return Bundle.Ecma7Desc();
    }

    @NbBundle.Messages("Ecma7DisplayName=ECMA7 feature used")
    @Override
    public String getDisplayName() {
        return Bundle.Ecma7DisplayName();
    }

    private class Ecma7Visitor extends PathNodeVisitor {

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
        public boolean enterFunctionNode(FunctionNode functionNode) {
            if (functionNode.isModule()) {
                functionNode.visitExports(this);
            }
            if (functionNode.isAsync()) {
                addHint(context, hints, new OffsetRange(Token.descPosition(functionNode.getFirstToken()), functionNode.getStart()));
            }
            List<IdentNode> params = functionNode.getParameters();
            if (params != null && !params.isEmpty()) {
                IdentNode last = params.get(params.size() - 1);
                checkTrailingComma(last.getFinish());
            }
            return super.enterFunctionNode(functionNode);
        }

        @Override
        public boolean enterExportNode(ExportNode exportNode) {
            // the complex export nodes are included in top level function body anyway
            // so we do not want to to visit further
            if (exportNode.isDefault()) {
                return super.enterExportNode(exportNode);
            }
            return false;
        }

        @Override
        public boolean enterVarNode(VarNode varNode) {
            if (varNode.isExport() || varNode.isDestructuring()) {
                return false;
            }
            return super.enterVarNode(varNode);
        }

        @Override
        public boolean enterClassNode(ClassNode classNode) {
            for (Expression decorator : classNode.getDecorators()) {
                addHint(context, hints, new OffsetRange(decorator.getStart(), decorator.getFinish()));
            }
            return super.enterClassNode(classNode);
        }

        @Override
        public boolean enterPropertyNode(PropertyNode propertyNode) {
            for (Expression decorator : propertyNode.getDecorators()) {
                addHint(context, hints, new OffsetRange(decorator.getStart(), decorator.getFinish()));
            }
            Expression key = propertyNode.getKey();
            if (key.isTokenType(TokenType.SPREAD_OBJECT)) {
                long token = key.getToken();
                int position = Token.descPosition(token);
                addHint(context, hints, new OffsetRange(position, position + Token.descLength(token)));
                key.accept(this);
                return false;
            }
            return super.enterPropertyNode(propertyNode);
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            long token = binaryNode.getToken();
            TokenType type = Token.descType(token);
            if (TokenType.ASSIGN_EXP == type || TokenType.EXP == type) {
                int position = Token.descPosition(token);
                addHint(context, hints, new OffsetRange(position, position + Token.descLength(token)));
            } else if (TokenType.COMMARIGHT == type) {
                Expression rhs = binaryNode.rhs();
                if (!rhs.isTokenType(TokenType.COMMARIGHT)) {
                    checkTrailingComma(rhs.getFinish());
                }
            }
            return super.enterBinaryNode(binaryNode);
        }

        @Override
        public boolean enterUnaryNode(UnaryNode unaryNode) {
            if (unaryNode.isTokenType(TokenType.AWAIT)) {
                long token = unaryNode.getToken();
                int position = Token.descPosition(token);
                addHint(context, hints, new OffsetRange(position, position + Token.descLength(token)));
            }
            return super.enterUnaryNode(unaryNode);
        }

        private void checkTrailingComma(int offset) {
            int fileOffset = context.parserResult.getSnapshot().getOriginalOffset(offset);
            if (fileOffset >= 0) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                        context.parserResult.getSnapshot(), offset, JsTokenId.javascriptLanguage());
                if (ts != null) {
                    org.netbeans.api.lexer.Token<? extends JsTokenId> next = LexUtilities.findNextNonWsNonComment(ts);
                    if (next != null && next.id() == JsTokenId.OPERATOR_COMMA) {
                        addDocumenHint(context, hints, new OffsetRange(ts.offset(), ts.offset() + next.length()));
                    }
                }
            }
        }
    }
}
