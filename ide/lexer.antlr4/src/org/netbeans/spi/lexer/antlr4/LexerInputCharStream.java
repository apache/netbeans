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

import java.util.Stack;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.netbeans.spi.lexer.*;

/**
 * An ANTLR4 CharSteam implementation over NetBeans LexerInput.
 *
 * @author Laszlo Kishalmi
 */
public final class LexerInputCharStream implements CharStream {
    private final LexerInput input;
    private final Stack<Integer> markers = new Stack<>();

    public LexerInputCharStream(LexerInput input) {
        this.input = input;

    }

    @Override
    public String getText(Interval intrvl) {
        return input.readText(intrvl.a, intrvl.b).toString();
    }

    @Override
    public void consume() {
        input.read();
    }

    @Override
    public int LA(int count) {
        if (count == 0) {
            return 0; //the behaviour is not defined
        }

        int c = 0;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                c = input.read();
            }
            input.backup(count);
        } else {
            input.backup(count);
            c = input.read();
        }
        return c;
    }

    @Override
    public int mark() {
        markers.push(index());
        return markers.size() - 1;
    }

    @Override
    public void release(int marker) {
        while (marker < markers.size()) {
            markers.pop();
        }
    }

    @Override
    public int index() {
        return input.readLengthEOF();
    }

    @Override
    public void seek(int i) {
        if (i < input.readLengthEOF()) {
            input.backup(index() - i);
        } else {
            while (input.readLengthEOF() < i) {
                if (input.read() == LexerInput.EOF) {
                    break;
                }
            }
        }
    }

    @Override
    public int size() {
        return -1;
        //throw new UnsupportedOperationException("Stream size is unknown.");
    }

    @Override
    public String getSourceName() {
        return UNKNOWN_SOURCE_NAME;
    }
}
