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
package org.netbeans.modules.css.lib.nblexer;

import java.util.Stack;
import org.antlr.runtime.CharStream;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author marekfukala
 */
public class NbLexerCharStream implements CharStream {

    private static final String STREAM_NAME = "NbLexerCharStream"; //NOI18N
    private LexerInput li;
    private Stack<Integer> markers = new Stack<>();
//    private int index;

    public NbLexerCharStream(LexerRestartInfo<CssTokenId> lri) {
        this.li = lri.input();

    }

    @Override
    public String substring(int i, int i1) {
        return li.readText(i, i1).toString();
    }

    @Override
    public int LT(int i) {
        return LA(i);
    }

    @Override
    public int getLine() {
        return -1;
    }

    @Override
    public void setLine(int i) {
        //no-op
    }

    @Override
    public void setCharPositionInLine(int i) {
        //no-op
    }

    @Override
    public int getCharPositionInLine() {
        return -1;
    }

    @Override
    public void consume() {
        read();
//        index++;
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
    public void rewind(int marker) {
        if(markers.size() < marker) {
            return ; //report?
        }
        int markedIndex = markers.get(marker);
        
        //remove all markers after the given one, keep the requested one
        for(int i = marker + 1; i < markers.size(); i++) {
            markers.remove(i);
        }
        
        //seek to the marked index
        seek(markedIndex);
    }

    @Override
    public void rewind() {
        if(markers.isEmpty()) { 
            return ;
        }
        
        rewind(markers.size() - 1);
    }

    @Override
    public void release(int marker) {
        if(markers.size() < marker) {
            return ; //report?
        }
        
        //remove all markers from the given one, including the requested one
        do {
            markers.pop();
        } while (marker < markers.size());
    }

    @Override
    public void seek(int i) {
        if (i < index()) {
            //go backward
            li.backup(index() - i);
//            index = i;
        } else {
            // go forward
            while (index() < i) {
                consume();
            }
        }
    }

    @Override
    public int size() {
        return -1; //not supported
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
}
