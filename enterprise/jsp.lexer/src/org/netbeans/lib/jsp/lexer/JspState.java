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
package org.netbeans.lib.jsp.lexer;

/**
 * Holds JspLexer state.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JspState {

    //main internal lexer state
    private int lexerState;

    //secondary internal state for EL expressions in JSP
    //is it used to eliminate a number of lexer states when EL is found -
    //we have 8 states just in attribute value so I would have to copy the EL
    //recognition code eight-times.
    private int lexerStateBeforeEL;

    //the same for jsp scriptlets
    private int lexerStateBeforeScriptlet;

    //internal state signalling whether the lexer is in <jsp:scriptlet> tag
    private int lexerStateJspScriptlet;

    //internal state signalling deep of the curly bracket nested level
    private int lexerStateCurlyNestedLevel;

    public JspState(int lexerState, int lexerStateBeforeEL, int lexerStateBeforeScriptlet, int lexerStateJspScriptlet, int lexerStateCurlyNestedLevel) {
        this.lexerState = lexerState;
        this.lexerStateBeforeEL = lexerStateBeforeEL;
        this.lexerStateBeforeScriptlet = lexerStateBeforeScriptlet;
        this.lexerStateJspScriptlet = lexerStateJspScriptlet;
        this.lexerStateCurlyNestedLevel = lexerStateCurlyNestedLevel;
    }

    public int getLexerState() {
        return lexerState;
    }

    public int getLexerStateBeforeEL() {
        return lexerStateBeforeEL;
    }

    public int getLexerStateBeforeScriptlet() {
        return lexerStateBeforeScriptlet;
    }

    public int getLexerStateJspScriptlet() {
        return lexerStateJspScriptlet;
    }

    public int getLexerStateCurlyNestedLevel() {
        return lexerStateCurlyNestedLevel;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.lexerState;
        hash = 83 * hash + this.lexerStateBeforeEL;
        hash = 83 * hash + this.lexerStateBeforeScriptlet;
        hash = 83 * hash + this.lexerStateJspScriptlet;
        hash = 83 * hash + this.lexerStateCurlyNestedLevel;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JspState other = (JspState) obj;
        if (this.lexerState != other.lexerState) {
            return false;
        }
        if (this.lexerStateBeforeEL != other.lexerStateBeforeEL) {
            return false;
        }
        if (this.lexerStateBeforeScriptlet != other.lexerStateBeforeScriptlet) {
            return false;
        }
        if (this.lexerStateJspScriptlet != other.lexerStateJspScriptlet) {
            return false;
        }
        if (this.lexerStateCurlyNestedLevel != other.lexerStateCurlyNestedLevel) {
            return false;
        }
        return true;
    }
}
