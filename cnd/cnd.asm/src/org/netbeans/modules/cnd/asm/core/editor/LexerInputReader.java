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


package org.netbeans.modules.cnd.asm.core.editor;

import java.io.IOException;
import java.io.Reader;

import org.netbeans.spi.lexer.LexerInput;

class LexerInputReader extends Reader {

    private final LexerInput input;
    
    boolean isClosed;
    boolean isEof;
    
    public LexerInputReader(LexerInput input) {
        this.input = input;
        
        isClosed = false;
        isEof = false;
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (isClosed) {
            throw new IOException();
        }
        
        if (isEof) {
            return -1;
        }
        
        int ch = LexerInput.EOF + 1;
        int count = 0;
        
        while(count < len && (ch = input.read()) != LexerInput.EOF) {
            cbuf[count + off] = (char) ch;
            count++;
        }
        
        if (ch == LexerInput.EOF) {
            isEof = true;
            if (count == 0) {
                return -1;
            }
        }
        
        return count; 
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

}
