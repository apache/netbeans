/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.JsPreferences;
import org.netbeans.modules.javascript2.editor.JsVersion;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class Ecma7Rule extends EcmaLevelRule {

    private static RequestProcessor RP = new RequestProcessor(Ecma7Rule.class);

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        if (JsPreferences.isPreECMAScript7(FileOwnerQuery.getOwner(context.getJsParserResult().getSnapshot().getSource().getFileObject()))) {
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
                        new SwitchToEcma7Fix(context.getJsParserResult().getSnapshot())), 600));
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

    private static final class SwitchToEcma7Fix implements HintFix {

        private final FileObject fo;

        public SwitchToEcma7Fix(Snapshot snapshot) {
            this.fo = snapshot.getSource().getFileObject();
        }

        @NbBundle.Messages("MSG_SwitchToEcma7=Switch project to ECMA7")
        @Override
        public String getDescription() {
            return Bundle.MSG_SwitchToEcma7();
        }

        @Override
        public void implement() throws Exception {
            if (fo == null) {
                return;
            }

            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                JsPreferences.putECMAScriptVersion(p, JsVersion.ECMA7);
            }

            refresh(fo);
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
