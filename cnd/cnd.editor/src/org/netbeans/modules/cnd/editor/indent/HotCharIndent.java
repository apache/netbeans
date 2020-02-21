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

package org.netbeans.modules.cnd.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.util.Exceptions;

/**
 *
 */
public enum HotCharIndent {
    INSTANCE;

    public boolean getKeywordBasedReformatBlock(BaseDocument doc, int dotPos, String typedText) {
        if (isTokenContinue(doc, dotPos)) {
            return false;
        }
        if ("e".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "else")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#else")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("f".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "#if")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#elif")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#ifdef")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#ifndef")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "#endif")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if (":".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "case")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "default")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "public")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "protected")) { // NOI18N
                    return true;
                } else if (checkCase(doc, fnw, "private")) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        } else if ("{".equals(typedText) || "}".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                return checkCase(doc, fnw, typedText);
            } catch (BadLocationException e) {
            }
        }
        if (typedText != null &&
            typedText.length() == 1 && (Character.isLetter(typedText.charAt(0)) || typedText.charAt(0) == '#')) {
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, typedText+"\n") || // NOI18N
                    dotPos == doc.getLength()-1 && checkCase(doc, fnw, typedText)) { // NOI18N
                    return true;
                }
            } catch (BadLocationException e) {
            }
        }
        return false;
    }

    private static boolean checkCase(BaseDocument doc, int fnw, String what) throws BadLocationException {
        return fnw >= 0 && fnw + what.length() <= doc.getLength() && what.equals(doc.getText(fnw, what.length()));
    }
    
    static boolean isTokenContinue(BaseDocument doc, int dotPos) {
        try {
            int rowStart = Utilities.getRowStart(doc, dotPos);
            if (rowStart > 1) {
                String prev = doc.getText(rowStart-2, 2);
                if ("\\\n".equals(prev)) { // NOI18N
                    return true;
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
}
