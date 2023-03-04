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
import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.FunctionNode;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.JsPreferences;
import org.netbeans.modules.javascript2.editor.JsVersion;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

public class OperatorRule extends EcmaLevelRule {

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        JsVersion version = JsPreferences.getECMAScriptVersion(FileOwnerQuery.getOwner(context.getJsParserResult().getSnapshot().getSource().getFileObject()));
        OperatorVisitor visitor = new OperatorVisitor(version);
        visitor.process(context, hints);
    }

    private void addHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range, JsVersion requiredVersion) {
        addDocumenHint(context, hints, ModelUtils.documentOffsetRange(context.getJsParserResult(),
                range.getStart(), range.getEnd()), requiredVersion);
    }

    private void addDocumenHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, OffsetRange range, JsVersion requiredVersion) {
        List<HintFix> fixes;
        String description;
        if(requiredVersion != null) {
            fixes = Collections.singletonList(
                        new SwitchToEcmaXFix(context.getJsParserResult().getSnapshot(), requiredVersion));
            description = Bundle.OperatorDescDetail(requiredVersion);
        } else {
            fixes = Collections.emptyList();
            description = Bundle.OperatorDesc();
        }
        hints.add(new Hint(this, description,
                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                range, fixes, 600));
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "operator.hint";
    }

    @NbBundle.Messages({
        "# {0} - ECMAScript Version required",
        "OperatorDescDetail=Operator from {0} used",
        "OperatorDesc=Operator from future ECMAScript edition used"
    })
    @Override
    public String getDescription() {
        return Bundle.OperatorDesc();
    }

    @NbBundle.Messages("OperatorDisplayName=Operator from future ECMAScript edition used")
    @Override
    public String getDisplayName() {
        return Bundle.OperatorDisplayName();
    }

    private class OperatorVisitor extends PathNodeVisitor {
        private final JsVersion targetVersion;
        private List<Hint> hints;
        private JsHintsProvider.JsRuleContext context;

        public OperatorVisitor(JsVersion targetVersion) {
            this.targetVersion = targetVersion;
        }

        public void process(JsHintsProvider.JsRuleContext context, List<Hint> hints) {
            this.hints = hints;
            this.context = context;
            FunctionNode root = context.getJsParserResult().getRoot();
            if (root != null) {
                context.getJsParserResult().getRoot().accept(this);
            }
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            if(!binaryNode.tokenType().isSupported(targetVersion.getEcmascriptEdition())) {
                JsVersion requiredVersion = JsVersion.fromEcmascriptEdition(
                        binaryNode.tokenType().getEcmascriptEdition()
                );
                addHint(context, hints, new OffsetRange(
                        binaryNode.getStart(),
                        binaryNode.getFinish()),
                        requiredVersion
                );
            }
            return super.enterBinaryNode(binaryNode);
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            TokenType tt = Token.descType(accessNode.getToken());
            if(!tt.isSupported(targetVersion.getEcmascriptEdition())) {
                JsVersion requiredVersion = JsVersion.fromEcmascriptEdition(
                        tt.getEcmascriptEdition()
                );
                addHint(context, hints, new OffsetRange(
                        accessNode.getStart(),
                        accessNode.getFinish()),
                        requiredVersion
                );
            }
            return super.enterAccessNode(accessNode);
        }
    }

}
