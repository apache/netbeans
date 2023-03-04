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

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 * Java formatter tests.
 *
 * @author Miloslav Metelka
 */
public class JavaFormatterUnitTestCase extends JavaBaseDocumentUnitTestCase {

    public JavaFormatterUnitTestCase(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentNewLine() {
        // this actually only inserts \n in the document, the indentation is
        // hooked through the ExtKit.ExtDefaultKeyTypedAction.checkIndentHotChars(),
        // which calls f.getReformatBlock and f.reformat
        // IMO this should just be replaced by simple doc.insertString(getCaretOffset(), "\n", null)
        Indent indenter = Indent.get(getDocument());
        indenter.lock();
        try {
            int offset = indenter.indentNewLine(getCaretOffset());
            getCaret().setDot(offset);
        } catch (BadLocationException ble) {
            throw new IllegalStateException(ble);
        } finally {
            indenter.unlock();
        }
    }
    
    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        final BaseDocument doc = getDocument();
        final Reformat formatter = Reformat.get(getDocument());
        formatter.lock();
        try {
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        formatter.reformat(0, doc.getLength());
                    } catch (BadLocationException e) {
                        e.printStackTrace(getLog());
                        fail(e.getMessage());
                    }
                }
            });
        } finally {
            formatter.unlock();
        }
    }
    
}
