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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Abstract implementation of editor character services.
 *
 * @author mmetelka
 */
final class DefaultEditorCharacterServices extends EditorCharacterServices {

    @Override
    public int getIdentifierEnd(Document doc, int offset, boolean backward) {
        DocumentCharacterAcceptor characterAcceptor = DocumentCharacterAcceptor.get(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        if (backward) {
            while (--offset >= 0 && characterAcceptor.isIdentifier(docText.charAt(offset))) { }
            return offset + 1;
        } else {
            int docTextLen = docText.length();
            while (offset < docTextLen && characterAcceptor.isIdentifier(docText.charAt(offset))) {
                offset++;
            }
            return offset;
        }
    }

}
