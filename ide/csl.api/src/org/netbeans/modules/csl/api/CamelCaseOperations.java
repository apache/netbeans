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

package org.netbeans.modules.csl.api;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;

/**
 * CamelCase operations - based on Java ones but rewritten to delegate all logic
 * to language plugins
 * 
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 * @author Tor Norbye
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(JTextComponent textComponent) {
        int offset = textComponent.getCaretPosition();
        Document doc = textComponent.getDocument();

        // Are we at the end of the document?
        if (offset == doc.getLength()) {
            return -1;
        }

        KeystrokeHandler bc = UiUtils.getBracketCompletion(doc, offset);
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(doc, offset, false);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getNextWord(textComponent, offset);
        } catch (BadLocationException ble) {
            // something went wrong :(
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static int previousCamelCasePosition(JTextComponent textComponent) {
        int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document?
        if (offset == 0) {
            return -1;
        }

        final Document doc = textComponent.getDocument();
        
        KeystrokeHandler bc = UiUtils.getBracketCompletion(doc, offset);
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(
                    doc, offset, true);
            if (nextOffset != -1) {
                return nextOffset;
            }
        }
        
        try {
            return Utilities.getPreviousWord(textComponent, offset);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static void replaceChar(JTextComponent textComponent, int offset, char c) {
        if (!textComponent.isEditable()) {
            return;
        }
        replaceText(textComponent, offset, 1, String.valueOf(c));
    }

    static void replaceText(JTextComponent textComponent, final int offset, final int length, final String text) {
        if (!textComponent.isEditable()) {
            return;
        }
        final Document document = textComponent.getDocument();
        AtomicLockDocument ld = LineDocumentUtils.asRequired(document, AtomicLockDocument.class);
        // rely on editor utilities API to provide a stub, one is defined for the base Document class.
        ld.runAtomic(() -> {
            try {
                if (length > 0) {
                    document.remove(offset, length);
                }
                document.insertString(offset, text, null);
            } catch (BadLocationException ble) {
                ErrorManager.getDefault().notify(ble);
            }
        });
    }
}
