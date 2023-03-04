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
package org.netbeans.modules.editor.impl;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.text.IndentEngine;

/**
 * IndentEngine implementation delegating to the new Editor Indentation API.
 *
 * @author Dusan Balek, Miloslav Metelka
 */
public final class DefaultIndentEngine extends IndentEngine {

    @Override
    public int indentLine(Document doc, int offset) {
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            if (doc instanceof BaseDocument) {
                ((BaseDocument) doc).atomicLock();
            }
            try {
                Position pos = doc.createPosition(offset);
                indent.reindent(offset);
                return pos.getOffset();
            } catch (BadLocationException ble) {
                return offset;
            } finally {
                if (doc instanceof BaseDocument) {
                    ((BaseDocument) doc).atomicUnlock();
                }
            }
        } finally {
            indent.unlock();
        }
    }

    @Override
    public int indentNewLine(Document doc, int offset) {
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            if (doc instanceof BaseDocument) {
                ((BaseDocument) doc).atomicLock();
            }
            try {
                return indent.indentNewLine(offset);
            } catch (BadLocationException ble) {
                return offset;
            } finally {
                if (doc instanceof BaseDocument) {
                    ((BaseDocument) doc).atomicUnlock();
                }
            }
        } finally {
            indent.unlock();
        }
    }

    @Override
    public Writer createWriter(Document doc, int offset, Writer writer) {
        return new WriterImpl(doc, offset, writer);
    }

    /**
     * Implementation of the reformatting writer for the indent SPI.
     * It inserts the formatted text into document first then reformats it
     * and then removes it from the document upon or Writer.flush().
     */
    private static class WriterImpl extends Writer {

        private Document doc;
        private int offset;
        private Writer writer;
        private StringBuilder buffer;
        private int writtenLen = 0;

        private WriterImpl(Document doc, int offset, Writer writer) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset=" + offset + " < 0"); // NOI18N
            }
            if (offset > doc.getLength()) {
                throw new IllegalArgumentException("offset=" + offset + " > docLen=" + doc.getLength()); // NOI18N
            }

            this.doc = doc;
            this.offset = offset;
            this.writer = writer;
            this.buffer = new StringBuilder();
        }

        @Override
        public void write(int c) throws IOException {
            write(new char[]{(char) c}, 0, 1);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // Add the chars to the buffer for formatting
            buffer.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            Reformat reformat = Reformat.get(doc);
            reformat.lock();
            try {
                String text = buffer.toString();
                if (text.length() > 0 && offset <= doc.getLength()) {
                    try {
                        doc.insertString(offset, text, null);
                        Position endPos = doc.createPosition(offset + text.length());
                        reformat.reformat(offset, endPos.getOffset());
                        int len = endPos.getOffset() - offset;
                        String reformattedText = doc.getText(offset, len);
                        doc.remove(offset, len);
                        writer.write(reformattedText.substring(writtenLen));
                        writtenLen = len;
                    } catch (BadLocationException e) {
                    }
                }
            } finally {
                reformat.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }
}
