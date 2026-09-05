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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

/**
 *
 * @author bogdan
 */
public abstract class LexerAdaptor extends Lexer {

    public int exitIfModePosition = 0;
    public boolean compomentTagOpen = false;
    public int identifierStringPos = 1;
    public int argCounter = 1;

    public LexerAdaptor(CharStream input) {
        super(input);
    }
    
    public void lookupMode(int mode){
        if (this._input.LA(1) == '('){
            this.mode(mode);
        } else {
            this.resetIdentifierStringPos();
            this.skip();
        }
    }
    
    public void flexibleMode(int mode){
        if (this._input.LA(1) == '('){
            this.mode(mode);
        }
    }
    
    public void resetIdentifierStringPos(){
        this.identifierStringPos = 1;
    }
    
    public void consumeCloseTag(int curlyBalance){
        if (curlyBalance == 0) {
            this.setType(BladeAntlrLexer.BLADE_CONTENT_CLOSE_TAG);
        } else {
            this.skip();
        }
    }
}
