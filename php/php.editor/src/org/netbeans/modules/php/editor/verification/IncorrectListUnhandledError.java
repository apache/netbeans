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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handles cases when incorrect mixed list syntaxes are used.
 *
 * <pre>
 * e.g.
 * - ["a" => $a, [$b, $c]] = ["a" => 1, [2, 3]];
 * - [$a, list($b, $c)] = [1, [2, 3]];
 * - list($a, [$b, $c]) = [1, [2, 3]];
 * </pre>
 */
public class IncorrectListUnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("IncorrectListUnhandledError.displayName=Cannot mix [] and list(), keyed and unkeyed array entries in assignments.")
    @Override
    public String getDisplayName() {
        return Bundle.IncorrectListUnhandledError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getErrors());
            }
        }
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        CheckVisitor(FileObject fileObject) {
            assert fileObject != null;
            this.fileObject = fileObject;
        }

        List<VerificationError> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        @Override
        public void visit(ListVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ListVariable.SyntaxType type = node.getSyntaxType();
            List<ArrayElement> elements = node.getElements();
            checkMixedList(elements, null, type, true);
        }

        private void checkMixedList(List<ArrayElement> elements, Expression key, ListVariable.SyntaxType firstSyntaxType, boolean root) {
            Expression firstKey = key;
            boolean first = root;
            for (ArrayElement element : elements) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                // check key
                if (first) {
                    first = false;
                } else {
                    if (firstKey != null && element.getKey() == null
                            || (firstKey == null && element.getKey() != null)) {
                        // error
                        createError(element);
                        break;
                    }
                }
                firstKey = element.getKey();

                // check value
                Expression value = element.getValue();
                if (value instanceof ListVariable) {
                    ListVariable listVariable = (ListVariable) value;
                    if (firstSyntaxType != listVariable.getSyntaxType()) {
                        createError(listVariable);
                        break;
                    }
                    // check recursively
                    checkMixedList(listVariable.getElements(), firstKey, firstSyntaxType, false);
                } else if (value instanceof ArrayCreation) {
                    // NOTE: ArrayCreation is used as new list syntax in elements
                    ArrayCreation arrayCreation = (ArrayCreation) value;
                    if (firstSyntaxType != getType(arrayCreation.getType())) {
                        createError(arrayCreation);
                        break;
                    }
                    // check recursively
                    checkMixedList(arrayCreation.getElements(), firstKey, firstSyntaxType, false);
                }
            }
        }

        private ListVariable.SyntaxType getType(ArrayCreation.Type type) {
            return type == ArrayCreation.Type.NEW ? ListVariable.SyntaxType.NEW : ListVariable.SyntaxType.OLD;
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new IncorrectList(fileObject, startOffset, endOffset));
        }

    }

    private static final class IncorrectList extends VerificationError {

        private static final String KEY = "Php.List.Syntax.Mixed"; // NOI18N

        IncorrectList(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("IncorrectList.displayName=Cannot mix [] and list(), keyed and unkeyed array entries in assignments.")
        @Override
        public String getDisplayName() {
            return Bundle.IncorrectListUnhandledError_displayName();
        }

        @NbBundle.Messages("IncorrectList.description=Use the same list syntax.")
        @Override
        public String getDescription() {
            return Bundle.IncorrectList_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

}
