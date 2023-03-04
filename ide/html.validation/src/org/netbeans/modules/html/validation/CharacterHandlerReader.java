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
