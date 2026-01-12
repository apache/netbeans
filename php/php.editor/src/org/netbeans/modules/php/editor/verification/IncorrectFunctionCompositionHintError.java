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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.model.impl.Type.FALSE;
import static org.netbeans.modules.php.editor.model.impl.Type.NULL;
import static org.netbeans.modules.php.editor.model.impl.Type.TRUE;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.CompositionExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import static org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type.FLOAT;
import static org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type.INT;
import static org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type.REAL;
import static org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type.STRING;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class IncorrectFunctionCompositionHintError extends HintErrorRule {
    @NbBundle.Messages("IncorrectFunctionCompositionHintErrorDisplayName=Pipe operator right-handle requires to be a callable")
    @Override
    public String getDisplayName() {
        return Bundle.IncorrectFunctionCompositionHintErrorDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null && appliesTo(fileObject)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, this);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.addAll(checkVisitor.getHintErrors());
            }
        }
    }
    
    protected PhpVersion getPhpVersion(@NullAllowed FileObject file) {
        if (file == null) {
            return PhpVersion.getDefault();
        }
        return CodeUtils.getPhpVersion(file);
    }
    
    protected boolean appliesTo(FileObject file) {
        return getPhpVersion(file).compareTo(PhpVersion.PHP_85) >= 0;
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<Hint> hintErrors = new ArrayList<>();
        private final FileObject fileObject;
        private final HintErrorRule rule ;

        public CheckVisitor(FileObject fileObject, HintErrorRule rule) {
            this.fileObject = fileObject;
            this.rule = rule;
        }

        public Collection<Hint> getHintErrors() {
            return Collections.unmodifiableCollection(hintErrors);
        }
  
        @Override
        public void visit(CompositionExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            
            Expression right = node.getRight();

            if (right instanceof Scalar rightValue){
                switch (rightValue.getScalarType()) {
                    case INT, REAL, FLOAT ->  {
                        createHintError(right);
                    }
                    case STRING -> {
                        switch (rightValue.getStringValue().toLowerCase()) {
                            case NULL, TRUE, FALSE -> {
                                createHintError(right);
                            }
                        }
                    }
                }
            } else if (right instanceof ParenthesisExpression rightValue) {
                if (rightValue.getExpression() instanceof InfixExpression) {
                    //infix result in a boolean type
                    createHintError(right);
                }
            }

            super.visit(node);
        }

        private void createHintError(ASTNode node) {
            hintErrors.add(new Hint(
                rule,
                getErrorDisplayName(),
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                new ArrayList<>(),
                500));
        }

        @NbBundle.Messages("CheckVisitor.displayName=Pipe operator right-handle requires to be a callable")
        private String getErrorDisplayName() {
            return Bundle.IncorrectFunctionCompositionHintErrorDisplayName();
        }
    }

}
