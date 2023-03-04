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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement.OutputType;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class WrongOrderOfArgsHint extends HintRule {

    private static final String HINT_ID = "Wrong.Order.Of.Args.Hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        TokenHierarchy<?> tokenHierarchy = phpParseResult.getSnapshot().getTokenHierarchy();
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc, tokenHierarchy);
        phpParseResult.getProgram().accept(checkVisitor);
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final List<FunctionDeclaration> wrongFunctions = new ArrayList<>();
        private final List<Hint> hints = new ArrayList<>();
        private final BaseDocument doc;
        private final TokenHierarchy<?> tokenHierarchy;

        public CheckVisitor(FileObject fileObject, BaseDocument doc, TokenHierarchy<?> tokenHierarchy) {
            this.fileObject = fileObject;
            this.doc = doc;
            this.tokenHierarchy = tokenHierarchy;
        }

        public List<Hint> getHints() {
            for (FunctionDeclaration wrongFunction : wrongFunctions) {
                processWrongFunction(wrongFunction);
            }
            return new ArrayList<>(hints);
        }

        @Messages("WrongOrderOfArgsDesc=Wrong order of arguments")
        private void processWrongFunction(FunctionDeclaration node) {
            RearrangeParametersFix hintFix = new RearrangeParametersFix(doc, node, tokenHierarchy);
            OffsetRange offsetRange = hintFix.getOffsetRange();
            if (showHint(offsetRange, doc)) {
                hints.add(new Hint(WrongOrderOfArgsHint.this, Bundle.WrongOrderOfArgsDesc(), fileObject, offsetRange, Collections.<HintFix>singletonList(hintFix), 500));
            }
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            boolean previousParamIsOptional = false;
            boolean currentParamIsOptional;
            for (FormalParameter formalParameter : node.getFormalParameters()) {
                currentParamIsOptional = !formalParameter.isMandatory();
                if (currentParamIsOptional) {
                    previousParamIsOptional = currentParamIsOptional;
                } else if (previousParamIsOptional && !formalParameter.isVariadic()) {
                    wrongFunctions.add(node);
                    break;
                }
            }
        }

    }

    private static class RearrangeParametersFix implements HintFix {

        private final FunctionDeclaration node;
        private final BaseDocument doc;
        private final FunctionDeclarationInfo functionDeclarationInfo;
        private final TokenHierarchy<?> tokenHierarchy;

        public RearrangeParametersFix(BaseDocument doc, FunctionDeclaration node, TokenHierarchy<?> tokenHierarchy) {
            this.doc = doc;
            this.node = node;
            this.tokenHierarchy = tokenHierarchy;
            functionDeclarationInfo = FunctionDeclarationInfo.create(new RearrangedFunctionDeclaration(node));
        }

        @Override
        @Messages({
            "# {0} - Method or function name",
            "RearrangeParamsDisp=Rearrange arguments of the method or function: {0}"
        })
        public String getDescription() {
            return Bundle.RearrangeParamsDisp(functionDeclarationInfo.getName());
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            List<FormalParameter> originalParameters = node.getFormalParameters();
            List<ParameterElement> parameters = functionDeclarationInfo.getParameters();
            assert originalParameters.size() == parameters.size() : originalParameters.size() + " != " + parameters.size();
            // maybe, in the case of constructor property promotion
            // parameters can be declared with multiple lines
            // so, replace the rearranged parameters with positions of original parameters
            // instead of replacing whole parameters as one line
            for (int i = 0; i < originalParameters.size(); i++) {
                FormalParameter originalParameter = originalParameters.get(i);
                OffsetRange originalRange = new OffsetRange(originalParameter.getStartOffset(), originalParameter.getEndOffset());
                edits.replace(originalRange.getStart(), originalRange.getLength(), parameters.get(i).asString(OutputType.COMPLETE_DECLARATION_WITH_MODIFIER), false, 0);
            }
            edits.apply();
        }

        public OffsetRange getOffsetRange() {
            int start = 0;
            int end = 0;
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, node.getStartOffset());
            if (ts != null) {
                ts.move(node.getStartOffset());
                int braceMatch = 0;
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == PHPTokenId.PHP_TOKEN) {
                        if (TokenUtilities.textEquals(t.text(), "(")) { // NOI18N
                            if (braceMatch == 0) {
                                start = ts.offset() + 1;
                            }
                            braceMatch++;
                        } else if (TokenUtilities.textEquals(t.text(), ")")) { // NOI18N
                            braceMatch--;
                        }
                        if (braceMatch == 0) {
                            end = ts.offset();
                            ts.moveNext();
                            break;
                        }
                    }
                }
            }
            return new OffsetRange(start, end);
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

    private static class RearrangedFunctionDeclaration extends FunctionDeclaration {

        public RearrangedFunctionDeclaration(FunctionDeclaration node) {
            super(node.getStartOffset(), node.getEndOffset(), node.getFunctionName(), node.getFormalParameters(), node.getReturnType(), node.getBody(), node.isReference());
        }

        @Override
        public List<FormalParameter> getFormalParameters() {
            List<FormalParameter> rearrangedList = new ArrayList<>();
            List<FormalParameter> parametersWithDefault = new ArrayList<>();
            FormalParameter variadicParam = null;
            for (FormalParameter param : super.getFormalParameters()) {
                if (param.isMandatory()) {
                    rearrangedList.add(param);
                } else if (param.isVariadic()) {
                    variadicParam = param;
                } else {
                    parametersWithDefault.add(param);
                }
            }
            rearrangedList.addAll(parametersWithDefault);
            if (variadicParam != null) {
                rearrangedList.add(variadicParam);
            }
            return rearrangedList;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("WrongOrderOfArgsHintDesc=Optional arguments should be grouped on the right side for better readability.<br><br>Example offending code:<br><code>function foo($optional=NULL, $required){}</code><br><br>Recommended code:<br><code>function foo($required, $optional=NULL){}</code>")
    public String getDescription() {
        return Bundle.WrongOrderOfArgsHintDesc();
    }

    @Override
    @Messages("WrongOrderOfArgsHintDispName=Order of Arguments")
    public String getDisplayName() {
        return Bundle.WrongOrderOfArgsHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

}
