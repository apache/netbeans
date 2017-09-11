/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
