/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
