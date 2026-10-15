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
import java.util.Collections;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * PSR-12: https://www.php-fig.org/psr/psr-12/
 *
 */
public abstract class PSR12Hint extends HintRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = createVisitor(fileObject, context.doc);
                if (checkVisitor.needScan()) {
                    phpParseResult.getProgram().accept(checkVisitor);
                }
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    protected abstract CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument);

    public abstract static class CheckVisitor extends DefaultVisitor {

        private final PSR12Hint psr12hint;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;

        public CheckVisitor(PSR12Hint psr1hint, FileObject fileObject, BaseDocument baseDocument) {
            this.psr12hint = psr1hint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return Collections.unmodifiableList(hints);
        }

        protected void createHint(ASTNode node, String message) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            createHint(offsetRange, message);
        }

        @NbBundle.Messages({
            "# {0} - Text which describes the violation",
            "PSR12ViolationHintText=PSR-12 Violation:\n{0}"
        })
        protected void createHint(OffsetRange offsetRange, String message) {
            if (psr12hint.showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        psr12hint,
                        Bundle.PSR12ViolationHintText(message),
                        fileObject,
                        offsetRange,
                        null,
                        500));
            }
        }

        protected boolean needScan() {
            return true;
        }
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

}
