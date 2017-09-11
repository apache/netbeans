/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.validation;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import nu.validator.htmlparser.common.CharacterHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class CharacterHandlerReader extends Reader {

    private List<CharacterHandler> handlers = new LinkedList<>();
    private boolean reading = false;
    
    private Reader reader;

    public CharacterHandlerReader(Reader reader) {
        this.reader = reader;
    }

    public void addCharacterHandler(CharacterHandler handler) {
        handlers.add(handler);
    }

    public boolean removeCharacterHandler(CharacterHandler handler) {
        return handlers.remove(handler);
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
    }

    @Override
    public int read() throws IOException {
        reading();

        int read = reader.read();
        characters(new char[]{(char) read}, 0, 1);
        return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        reading();

        int read = reader.read(cbuf, off, len);
        characters(cbuf, off, len);
        return read;
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    @Override
    public long skip(long ns) throws IOException {
        return reader.skip(ns);
    }

    //implementation copied from the StringReader
    @Override
    public int read(CharBuffer target) throws IOException {
        reading();

        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0) {
            characters(cbuf, 0, n);
        }
        target.put(cbuf, 0, n);
        return n;
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public void close() throws IOException {
        reader.close();
        end();
    }

    private void characters(char[] chars, int offset, int len) throws IOException {
        assert chars != null;
        assert chars.length > 0;
        assert offset >= 0;
        assert len > 0;

        try {
            for (CharacterHandler handler : handlers) {
                handler.characters(chars, offset, len);
            }
        } catch (SAXException ex) {
            throw new IOException(ex);
        }

    }

    private void start() throws IOException {
        try {
            for (CharacterHandler handler : handlers) {
                handler.start();
            }
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }

     private void end() {
        try {
            for (CharacterHandler handler : handlers) {
                handler.end();
            }
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void reading() throws IOException {
        if(!reading) {
            start();
            reading = true;
        }
    }
}
