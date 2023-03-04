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

package org.netbeans.modules.editor.indent;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.openide.util.Exceptions;

/**
 * Implementation of the reformatting writer for the indent SPI.
 * <br/>
 * It inserts the formatted text into document first then reformats it
 * and then removes it from the document upon or Writer.close().
 *
 * @author Miloslav Metelka
 */
public final class FormatterWriterImpl extends Writer {
    
    private IndentImpl indentImpl;
    
    private int offset;
    
    private Writer writer;
    
    private StringBuilder buffer;
    
    FormatterWriterImpl(IndentImpl indentImpl, int offset, Writer writer) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset=" + offset + " < 0"); // NOI18N
        }
        if (offset > indentImpl.document().getLength()) {
            throw new IllegalArgumentException("offset=" + offset + " > docLen=" + indentImpl.document().getLength()); // NOI18N
        }

        this.indentImpl = indentImpl;
        this.offset = offset;
        this.writer = writer;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int c) throws IOException {
        write(new char[] { (char)c }, 0, 1);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        // Add the chars to the buffer for formatting
        buffer.append(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        indentImpl.reformatLock();
        try {
            Document doc = indentImpl.document();
            String text = buffer.toString();
            if (text.length() > 0 && offset <= doc.getLength()) {
                try {
                    doc.insertString(offset, text, null);
                    Position startPos = doc.createPosition(offset);
                    Position endPos = doc.createPosition(offset + text.length());
                    indentImpl.reformat(startPos.getOffset(), endPos.getOffset(), startPos.getOffset());
                    int len = endPos.getOffset() - startPos.getOffset();
                    String reformattedText = doc.getText(startPos.getOffset(), len);
                    doc.remove(startPos.getOffset(), len);
                    writer.write(reformattedText);
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        } finally {
            indentImpl.reformatUnlock();
        }
    }

    @Override
    public void flush() throws IOException {
        // Wait for close()
    }
    
}
