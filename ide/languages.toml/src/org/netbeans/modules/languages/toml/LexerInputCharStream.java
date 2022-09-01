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
package org.netbeans.modules.languages.toml;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.netbeans.spi.lexer.*;

/**
 *
 * @author lkishalmi
 */
public class LexerInputCharStream implements CharStream {
    private final LexerInput input;

    private int tokenMark = Integer.MAX_VALUE;
    private int index = 0;

    public LexerInputCharStream(LexerInput input) {
        this.input = input;
    }

    @Override
    public String getText(Interval intrvl) {
        if (intrvl.a < tokenMark) {
            throw new UnsupportedOperationException("Read before the current token start is not supported: " + intrvl.a + " < " + tokenMark);
        }
        int start = intrvl.a - tokenMark;
        int end = intrvl.b - tokenMark + 1;
        return String.valueOf(input.readText(start, end));
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

    public final void markToken() {
        tokenMark = index;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Stream size is unknown.");
    }

    @Override
    public String getSourceName() {
        return UNKNOWN_SOURCE_NAME;
    }
}
