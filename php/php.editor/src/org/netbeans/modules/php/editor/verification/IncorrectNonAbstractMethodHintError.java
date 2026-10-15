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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Check incorrect non-abstract method declarations.
 *
 * <pre>
 * e.g.
 * class MyClass
 * {
 *     public function test();
 * }
 * </pre>
 */
public class IncorrectNonAbstractMethodHintError extends HintErrorRule {

    private FileObject fileObject;

    @NbBundle.Messages("IncorrectNonAbstractMethodHintErrorDisplayName=Incorrect Non-abstract Method")
    @Override
    public String getDisplayName() {
        return Bundle.IncorrectNonAbstractMethodHintErrorDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            Set<MethodDeclaration> incorrectNonAbstractMethods = checkVisitor.getIncorrectNonAbstractMethods();
            addIcorrectNonAbstractMethodHints(incorrectNonAbstractMethods, hints, context.doc);
        }
    }

    @NbBundle.Messages({
        "# {0} - Method Name",
        "IncorrectNonAbstractMethodHintErrorHintDesc=Non-abstract method \"{0}\" must contain body"
    })
    private void addIcorrectNonAbstractMethodHints(Set<MethodDeclaration> methodDeclarations, List<Hint> hints, BaseDocument doc) {
        for (MethodDeclaration methodDeclaration: methodDeclarations) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            List<HintFix> fixes = Collections.singletonList(new AddBodyFix(doc, methodDeclaration));
            addHint(methodDeclaration, Bundle.IncorrectNonAbstractMethodHintErrorHintDesc(CodeUtils.extractMethodName(methodDeclaration)), hints, fixes);
        }
    }

    private void addHint(ASTNode node, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(this,
                description,
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                fixes,
                500
        ));
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final Set<MethodDeclaration> incorrectNonAbstractMethods = new HashSet<>();

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!Modifier.isAbstract(node.getModifier())) {
                FunctionDeclaration function = node.getFunction();
                Block body = function.getBody();
                if (body == null) {
                    incorrectNonAbstractMethods.add(node);
                }
            }
        }

        public Set<MethodDeclaration> getIncorrectNonAbstractMethods() {
            return Collections.unmodifiableSet(incorrectNonAbstractMethods);
        }

    }

    private static final class AddBodyFix implements HintFix {

        private final BaseDocument doc;
        private final MethodDeclaration methodDeclaration;

        public AddBodyFix(BaseDocument doc, MethodDeclaration methodDeclaration) {
            this.doc = doc;
            this.methodDeclaration = methodDeclaration;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - Method name",
            "AddBodyFixDesc=Add body of the method: {0}"
        })
        public String getDescription() {
            return Bundle.AddBodyFixDesc(CodeUtils.extractMethodName(methodDeclaration));
        }

        @Override
        public void implement() throws Exception {
            EditList edhitList = new EditList(doc);
            int startOffset = methodDeclaration.getStartOffset();
            int textLength = methodDeclaration.getEndOffset() - methodDeclaration.getStartOffset();
            String text = doc.getText(startOffset, textLength);
            String insertText = text.substring(0, textLength - 1) + "{}"; // NOI18N
            edhitList.replace(startOffset, textLength, insertText, true, 0);
            edhitList.apply();
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
