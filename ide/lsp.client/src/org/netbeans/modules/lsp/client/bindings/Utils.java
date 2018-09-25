/**
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
package org.netbeans.modules.lsp.client.bindings;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.eclipse.lsp4j.Position;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static Position createPosition(Document doc, int offset) throws BadLocationException {
         return new Position(LineDocumentUtils.getLineIndex((LineDocument) doc, offset),
                             offset - LineDocumentUtils.getLineStart((LineDocument) doc, offset));
    }

    public static int getOffset(Document doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, pos.getLine()) + pos.getCharacter();
    }
}
