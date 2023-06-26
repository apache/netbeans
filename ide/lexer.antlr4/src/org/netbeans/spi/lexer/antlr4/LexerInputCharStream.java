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
package org.netbeans.spi.lexer.antlr4;

import org.antlr.v4.runtime.CharStream;
import static org.antlr.v4.runtime.IntStream.UNKNOWN_SOURCE_NAME;
import org.antlr.v4.runtime.misc.Interval;
import org.netbeans.spi.lexer.*;

/**
 * An ANTLR4 CharSteam implementation over NetBeans LexerInput.
 *
 * @author Laszlo Kishalmi
 */
final class LexerInputCharStream implements CharStream {
    private final LexerInput input;

    private int tokenMark = Integer.MAX_VALUE;
    private int index = 0;

    public LexerInputCharStream(LexerInput input) {
        this.input = input;
    }

    @Override
    public String getText(Interval intrvl) {
        if (intrvl.a < tokenMark) {
            throw new UnsupportedOperationException("Can't read before the last token end: " + tokenMark);
        }
        int start = intrvl.a - tokenMark;
        int end = intrvl.b - tokenMark + 1;
        int readCount = 0;
        int next = 0;
        while ((end > readLength()) && (next != EOF)) {
            next = input.read();
            readCount++;
        }
        String ret = String.valueOf(input.readText(start, Math.min(end, readLength())));
        input.backup(readCount);
        return ret;
    }

    @Override
    public void consume() {
        read();
    }

    @Override
    public int LA(int count) {
        if (count == 0) {
            return 0; //the behaviour is not defined
        }

        int c = 0;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                c = read();
            }
            backup(count);
        } else {
            backup(count);
            c = read();
        }
        return c;
    }

    //Marks are for buffering in ANTLR4, we do not really need them
    @Override
    public int mark() {
        return -1;
    }

    public void markToken() {
        tokenMark = index;
    }

    @Override
    public void release(int marker) {
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public void seek(int i) {
        if (i < index()) {
            backup(index() - i);
        } else {
            while (index() < i) {
                if (read() == LexerInput.EOF) {
                    break;
                }
            }
        }
    }


    private int read() {
        int ret = input.read();
        index += 1;
        return ret;
    }

    private void backup(int count) {
        index -= count;
        input.backup(count);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Stream size is unknown.");
    }

    int readLength() {
        return input.readLength();
    }

    @Override
    public String getSourceName() {
        return UNKNOWN_SOURCE_NAME;
    }
}
