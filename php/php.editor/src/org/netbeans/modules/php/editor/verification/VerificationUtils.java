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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class VerificationUtils {
    private static final Logger LOGGER = Logger.getLogger(VerificationUtils.class.getName());

    private VerificationUtils() {
    }

    public static boolean isBefore(int caret, int margin) {
        return caret <= margin;
    }

    public static OffsetRange createLineBounds(int caretOffset, BaseDocument doc) {
        assert doc != null;
        OffsetRange result = OffsetRange.NONE;
        if (caretOffset != -1) {
            try {
                int lineBegin = caretOffset >= 0 ? LineDocumentUtils.getLineStart(doc, caretOffset) : -1;
                int lineEnd = (lineBegin != -1) ? LineDocumentUtils.getLineEnd(doc, caretOffset) : -1;
                if (lineBegin > -1 && lineEnd != -1 && lineBegin <= lineEnd) {
                    result = new OffsetRange(lineBegin, lineEnd);
                }
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        return result;
    }

}
