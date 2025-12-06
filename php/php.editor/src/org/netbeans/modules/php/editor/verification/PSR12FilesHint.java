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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * https://www.php-fig.org/psr/psr-12/#22-files
 */
public class PSR12FilesHint extends PSR12Hint {

    private static final String HINT_ID = "PSR12.Hint.Files"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(PSR12FilesHint.class.getName());

    @Override
    @NbBundle.Messages({
        "PSR12FilesLFOnlyHint=PHP files MUST use the Unix LF (linefeed) line ending only",
        "PSR12FilesTerminatedWithSingleLFHint=PHP files MUST end with a non-blank line, terminated with a single LF"
    })
    protected CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
        FilesVisitor filesVisitor = new FilesVisitor(this, fileObject, baseDocument);
        try {
            int lastPosition = baseDocument.getLength();
            int lineStart = LineDocumentUtils.getLineStartOffset(baseDocument, lastPosition);
            if (!endsWithSingleLF(baseDocument)) {
                filesVisitor.createHint(new OffsetRange(lineStart, lastPosition), Bundle.PSR12FilesTerminatedWithSingleLFHint());
            }
            if (!isLF(baseDocument)) {
                int lineEnd = LineDocumentUtils.getLineEndOffset(baseDocument, 0);
                filesVisitor.createHint(new OffsetRange(0, lineEnd), Bundle.PSR12FilesLFOnlyHint());
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Cannot get the line position. document length: {0}, invalid offset: {1}", // NOI18N
                    new Object[]{baseDocument.getLength(), ex.offsetRequested()});
        }
        return filesVisitor;
    }

    private boolean endsWithSingleLF(BaseDocument document) {
        int docLength = document.getLength();
        try {
            // document seems have LF as a line separator evenif CRLF is set to the document
            if (docLength == 1) {
                String lastChar = document.getText(docLength - 1, 1);
                if (lastChar.equals(BaseDocument.LS_LF) && isLF(document)) {
                    return true;
                }
            } else if (docLength > 1) {
                char[] chars = document.getChars(docLength - 2, 2);
                if (chars[1] == '\n' && chars[0] != '\n' && chars[0] != '\r') {
                    return true;
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Cannot get the text. document length: {0}, invalid offset: {1}", // NOI18N
                    new Object[]{document.getLength(), ex.offsetRequested()});
        }
        return false;
    }

    private static boolean isLF(BaseDocument document) {
        Object lineSeparator = document.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP);
        if (lineSeparator instanceof String) {
            String lineEnding = (String) lineSeparator;
            if (lineEnding.equals(BaseDocument.LS_LF)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("PSR12FilesHintDescription=<html>"
            + "<ul>"
            + "<li>PHP files MUST use the Unix LF (linefeed) line ending only</li>"
            + "<li>PHP files MUST end with a non-blank line, terminated with a single LF</li>"
            + "</ul>"
    )
    public String getDescription() {
        return Bundle.PSR12FilesHintDescription();
    }

    @Override
    @NbBundle.Messages("PSR12FilesHintDisplayName=Files")
    public String getDisplayName() {
        return Bundle.PSR12FilesHintDisplayName();
    }

    private static final class FilesVisitor extends CheckVisitor {

        public FilesVisitor(PSR12Hint psr12hint, FileObject fileObject, BaseDocument baseDocument) {
            super(psr12hint, fileObject, baseDocument);
        }

        @Override
        protected void createHint(OffsetRange offsetRange, String message) {
            super.createHint(offsetRange, message);
        }

        @Override
        protected boolean needScan() {
            return false;
        }
    }
}
