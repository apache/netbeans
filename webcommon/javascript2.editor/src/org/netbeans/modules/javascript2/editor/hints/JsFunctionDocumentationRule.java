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

import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider.JsRuleContext;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsFunctionDocumentationRule extends JsAstRule {

    public static final String JSDOCUMENTATION_OPTION_HINTS = "jsdocumentation.option.hints"; //NOI18N

    @Override
    void computeHints(JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) {
        Map<?, List<? extends AstRule>> allHints = manager.getHints();
        List<? extends AstRule> conventionHints = allHints.get(JSDOCUMENTATION_OPTION_HINTS);
        Rule undocumentedParameterRule = null;
        Rule incorrectDocumentationRule = null;
        if (conventionHints != null) {
            for (AstRule astRule : conventionHints) {
                if (manager.isEnabled(astRule)) {
                    if (astRule instanceof UndocumentedParameterRule) {
                        undocumentedParameterRule = astRule;
                    } else if (astRule instanceof IncorrectDocumentationRule) {
                        incorrectDocumentationRule = astRule;
                    }
                }
            }
        }
        JsFunctionDocumentationVisitor conventionVisitor = new JsFunctionDocumentationVisitor(
                undocumentedParameterRule,
                incorrectDocumentationRule);
        conventionVisitor.process(context, hints);
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        if (context instanceof JsHintsProvider.JsRuleContext) {
            return true;
        }
        return false;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }


    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JSDOCUMENTATION_OPTION_HINTS);
    }

    @Override
    public String getId() {
        return "jsdocumentation.hint"; //NOI18N
    }

    @Override
    @NbBundle.Messages("JsDocumentationHintDesc=JavaScript Documentation Hints")
    public String getDescription() {
        return Bundle.JsDocumentationHintDesc();
    }

    @Override
    @NbBundle.Messages("JsDocumentationHintDisplayName=JavaScript Documentation")
    public String getDisplayName() {
        return Bundle.JsDocumentationHintDisplayName();
    }

    @NbBundle.Messages({
        "# {0} - parameter name which is incorectly specified", "IncorrectDocumentationRuleDisplayDescription=Incorrect Documentation: {0}",
        "# {0} - parameter name which is undocumented", "UndocumentedParameterRuleDisplayDescription=Undocumented Parameters: {0}"})
    private static final class JsFunctionDocumentationVisitor extends PathNodeVisitor {

        private List<Hint> hints;
        private JsHintsProvider.JsRuleContext context;
        private final Rule undocumentedParameterRule;
        private final Rule incorrectDocumentationRule;

        private JsFunctionDocumentationVisitor(Rule undocumentedParameterRule, Rule incorrectDocumentationRule) {
            this.incorrectDocumentationRule = incorrectDocumentationRule;
            this.undocumentedParameterRule = undocumentedParameterRule;
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
        public boolean enterFunctionNode(FunctionNode fn) {
            JsDocumentationHolder docHolder = JsDocumentationSupport.getDocumentationHolder(context.getJsParserResult());
            // TRUFFLE
            if (fn.isProgram()
                    || docHolder.getCommentForOffset(fn.getStart(), docHolder.getCommentBlocks()) == null) {
                return super.enterFunctionNode(fn);
            }

            List<DocParameter> docParameters = docHolder.getParameters(fn);
            List<IdentNode> funcParameters = fn.getParameters();

            // undocumented parameter related
            String missingParameters = missingParameters(funcParameters, docParameters);
            if (!missingParameters.isEmpty() && undocumentedParameterRule != null) {
                hints.add(new Hint(
                        undocumentedParameterRule,
                        Bundle.UndocumentedParameterRuleDisplayDescription(missingParameters),
                        context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                        ModelUtils.documentOffsetRange(context.getJsParserResult(), fn.getIdent().getStart(), fn.getIdent().getFinish()),
                        null,
                        600));
            }

            // incorect documentation related
            String superfluousParameters = superfluousParameters(funcParameters, docParameters);
            if (!superfluousParameters.isEmpty() && incorrectDocumentationRule != null) {
                hints.add(new Hint(
                        incorrectDocumentationRule,
                        Bundle.IncorrectDocumentationRuleDisplayDescription(superfluousParameters),
                        context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                        ModelUtils.documentOffsetRange(context.getJsParserResult(), fn.getIdent().getStart(), fn.getIdent().getFinish()),
                        null,
                        600));
            }

            return super.enterFunctionNode(fn);
        }

        private String missingParameters(List<IdentNode> functionParams, List<DocParameter> documentationParams) {
            StringBuilder sb = new StringBuilder();
            String delimiter = ""; //NOI18N
            for (IdentNode identNode : functionParams) {
                if (!containFunctionParamName(documentationParams, identNode.getName())) {
                    sb.append(delimiter).append(identNode.getName());
                    delimiter = ", "; //NOI18N
                }
            }
            return sb.toString();
        }

        private boolean containFunctionParamName(List<DocParameter> documentationParams, String functionParamName) {
            for (DocParameter docParameter : documentationParams) {
                if (docParameter.getParamName() != null
                        && docParameter.getParamName().getName().equals(functionParamName)) {
                    return true;
                }
            }
            return false;
        }

        private String superfluousParameters(List<IdentNode> functionParams, List<DocParameter> documentationParams) {
            StringBuilder sb = new StringBuilder();
            String delimiter = ""; //NOI18N
            for (DocParameter docParameter : documentationParams) {
                if (docParameter.isOptional()) {
                    continue;
                }
                Identifier paramName = docParameter.getParamName();
                if (paramName != null && !containDocParamName(functionParams, paramName.getName())) {
                    sb.append(delimiter).append(docParameter.getParamName().getName());
                    delimiter = ", "; //NOI18N
                }
            }
            return sb.toString();
        }

        private boolean containDocParamName(List<IdentNode> functionParams, String documentationParamName) {
            for (IdentNode identNode : functionParams) {
                if (identNode.getName().equals(documentationParamName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
