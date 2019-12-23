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
package org.netbeans.modules.javascript2.lexer;

import java.util.Stack;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Petr Pisl
 */
public class NbLexerCharStream implements CharStream {

    private static final String STREAM_NAME = "NbLexerCharStream"; //NOI18N
    private final LexerInput li;
    private final Stack<Integer> markers = new Stack<>();

    public NbLexerCharStream(LexerRestartInfo lri) {
        this.li = lri.input();
    }

    @Override
    public void consume() {
        read();
    }

    @Override
    public int LA(int lookahead) {
        if (lookahead == 0) {
            return 0; //the behaviour is not defined
        }

        int c = 0;
        for (int i = 0; i < lookahead; i++) {
            c = read();
        }
        li.backup(lookahead);
        return c;
    }

    @Override
    public int mark() {
        markers.push(index());
        return markers.size() - 1;
    }

    @Override
    public int index() {
        return li.readLengthEOF();
    }

    @Override
    public void release(int marker) {
        if(markers.size() < marker) {
            return ; //report?
        }
        
        //remove all markers from the given one, including the requested one
        for(int i = marker; i < markers.size(); i++) {
            markers.remove(i);
        }
    }

    @Override
    public void seek(int i) {
        if (i < index()) {
            //go backward
            li.backup(index() - i);
        } else {
            // go forward
            while (index() < i) {
                consume();
            }
        }
    }

    @Override
    public int size() {
        return -1; //uknown
    }

    @Override
    public String getSourceName() {
        return STREAM_NAME;
    }
    
     private int read() {
        int result = li.read();
        if (result == LexerInput.EOF) {
            result = CharStream.EOF;
        }

        return result;
    }

    @Override
    public String getText(Interval intrvl) {
        return li.readText(intrvl.a, intrvl.b).toString();
    }
}
