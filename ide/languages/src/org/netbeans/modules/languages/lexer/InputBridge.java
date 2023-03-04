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

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.spi.lexer.LexerInput;


class InputBridge extends CharInput {

    private LexerInput input;
    private int index = 0;
    private Pattern start;
    private Pattern end;

    
    InputBridge (LexerInput input) {
        this.input = input;
    }

    public char read () {
        index++;
        return (char) input.read ();
    }

    public void setIndex (int index) {
        while (this.index < index)
            read ();
        input.backup (this.index - index);
        this.index = index;
    }

    public int getIndex () {
        return index;
    }

    public char next () {
        char ch = (char) input.read ();
        input.backup (1);
        return ch;
    }

    public boolean eof () {
        return next () == (char) input.EOF;
    }

    public String getString (int from, int to) {
        return input.readText ().toString ();
    }

    public String toString () {
        return input.readText ().toString ();
    }
}


