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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class WrongParamNameHint extends HintRule {
    private static final Logger LOGGER = Logger.getLogger(WrongParamNameHint.class.getName());
    private static final String HINT_ID = "wrong.param.name.hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
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
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final BaseDocument doc;
        private final List<Hint> hints;
        private Program program;

        private CheckVisitor(FileObject fileObject, BaseDocument doc) {
            this.fileObject = fileObject;
            this.doc = doc;
            hints = new ArrayList<>();
        }

        private Collection<? extends Hint> getHints() {
            return hints;
        }

        @Override
        public void visit(Program program) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            this.program = program;
            super.visit(program);
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Comment comment = Utils.getCommentForNode(program, node);
            if (comment != null) {
                checkNodeWithComment(node, comment);
            }
        }

        private void checkNodeWithComment(FunctionDeclaration node, Comment comment) {
            CommentVisitor commentVisitor = new CommentVisitor();
            comment.accept(commentVisitor);
            List<PHPDocNode> paramVariables = commentVisitor.getParamVariables();
            List<FormalParameter> formalParameters = node.getFormalParameters();
            if (formalParameters.size() == paramVariables.size()) {
                for (int i = 0; i < paramVariables.size(); i++) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    checkParametersEquality(paramVariables, formalParameters, i);
                }
            }
        }

        private void checkParametersEquality(List<PHPDocNode> paramVariables, List<FormalParameter> formalParameters, int i) {
            PHPDocNode paramVariable = paramVariables.get(i);
            String paramVariableName = paramVariable.getValue();
            FormalParameter formalParameter = formalParameters.get(i);
            Expression parameterNameExpression = formalParameter.getParameterName();
            if (parameterNameExpression instanceof Variable) {
                Variable parameterVariable = (Variable) parameterNameExpression;
                String parameterName = CodeUtils.extractVariableName(parameterVariable);
                if (StringUtils.hasText(paramVariableName) && !paramVariableName.equals(parameterName)) {
                    createHint(paramVariable, parameterName);
                }
            }
        }

        @NbBundle.Messages("WrongParamNameHintText=Wrong Param Name")
        private void createHint(PHPDocNode paramVariable, String parameterName) {
            OffsetRange checkOffsetRange = new OffsetRange(paramVariable.getStartOffset(), getLineEnd(paramVariable));
            if (showHint(checkOffsetRange, doc)) {
                OffsetRange variableOffsetRange = new OffsetRange(paramVariable.getStartOffset(), paramVariable.getEndOffset());
                hints.add(new Hint(
                        WrongParamNameHint.this,
                        Bundle.WrongParamNameHintText(),
                        fileObject,
                        variableOffsetRange,
                        Collections.<HintFix>singletonList(new Fix(doc, variableOffsetRange, parameterName)),
                        500));
            }
        }

        private int getLineEnd(PHPDocNode paramVariable) {
            int result = paramVariable.getEndOffset();
            try {
                result = LineDocumentUtils.getLineEnd(doc, paramVariable.getStartOffset());
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return result;
        }

    }

    private static final class CommentVisitor extends DefaultVisitor {
        private final List<PHPDocNode> paramVariables = new ArrayList<>();

        @Override
        public void visit(PHPDocVarTypeTag node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            paramVariables.add(node.getVariable());
        }

        public List<PHPDocNode> getParamVariables() {
            return paramVariables;
        }

    }

    private static final class Fix implements HintFix {
        private final BaseDocument doc;
        private final OffsetRange offsetRange;
        private final String parameterName;

        public Fix(BaseDocument doc, OffsetRange offsetRange, String parameterName) {
            this.doc = doc;
            this.offsetRange = offsetRange;
            this.parameterName = parameterName;
        }

        @Override
        @NbBundle.Messages("WrongParamNameHintFix=Rename Param")
        public String getDescription() {
            return Bundle.WrongParamNameHintFix();
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(doc);
            editList.replace(offsetRange.getStart(), offsetRange.getLength(), parameterName, true, 0);
            editList.apply();
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

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("WrongParamNameHintDesc=Parameter names in @param annotations should correspond with parameter names in commented functions.")
    public String getDescription() {
        return Bundle.WrongParamNameHintDesc();
    }

    @Override
    @NbBundle.Messages("WrongParamNameHintName=Wrong Param Name")
    public String getDisplayName() {
        return Bundle.WrongParamNameHintName();
    }

}
