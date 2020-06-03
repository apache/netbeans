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

package org.netbeans.modules.cnd.makefile.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 * Indentation support for Makefiles.
 *
 */
class MakefileIndentTask implements IndentTask {

    private static final int INDENT = 8;

    private final Context context;

    public MakefileIndentTask(Context context) {
        this.context = context;
    }

    public ExtraLock indentLock() {
        return null; // no extra locking
    }

    public void reindent() throws BadLocationException {
        if (context.isIndent()) {
            int caretOffset = context.caretOffset();
            if (isRuleOrActionLine((BaseDocument) context.document(), caretOffset)) {
                context.modifyIndent(context.lineStartOffset(caretOffset), INDENT);
            }
        }
    }

    private static boolean isRuleOrActionLine(BaseDocument doc, int offset) throws BadLocationException {
        int start = Utilities.getRowStart(doc, offset - 1);
        String line = doc.getText(start, offset - start);
        int colon = line.indexOf(':'); // NOI18N
        int pound = line.indexOf('#'); // NOI18N
        return (line.charAt(0) == '\t') // NOI18N
                || (colon > 0 && pound == -1)
                || (colon > 0 && colon < pound);
    }

}
