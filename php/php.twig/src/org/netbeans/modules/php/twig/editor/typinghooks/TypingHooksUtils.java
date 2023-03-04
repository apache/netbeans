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
package org.netbeans.modules.php.twig.editor.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;

public final class TypingHooksUtils {

    private TypingHooksUtils() {
    }

    public static boolean isInsertMatchingEnabled() {
        EditorOptions options = EditorOptions.get(TwigLanguage.TWIG_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }
        return true;
    }

    public static boolean sameAsExistingChar(BaseDocument doc, char c, int dotPos) throws BadLocationException {
        if (dotPos <= 0 || doc.getLength() <= dotPos) {
            return false;
        }
        char[] nextChars = doc.getChars(dotPos, 1);
        return nextChars.length > 0 && nextChars[0] == c;
    }

    public static boolean isEscapeSequence(BaseDocument doc, int dotPos) throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }
        char[] previousChars = doc.getChars(dotPos - 1, 1);
        return previousChars.length > 0 && previousChars[0] == '\\';
    }

    public static boolean isOpeningDelimiterChar(char c) {
        return c == '{' || c == '%';
    }

}
