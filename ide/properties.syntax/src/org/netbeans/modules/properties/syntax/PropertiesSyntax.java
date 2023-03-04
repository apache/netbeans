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

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
* Syntax analyzes for properties files.
* Tokens and internal states are given below.
*
* @author Petr Jiricka, Miloslav Metelka
* @version 1.00
*/

public class PropertiesSyntax extends Syntax {

    // Internal states
    private static final int ISI_LINE_COMMENT = 2; // inside line comment
    private static final int ISI_KEY = 3; // inside a key
    private static final int ISI_KEY_A_BSLASH = 4; // inside a key after backslash
    private static final int ISI_EQUAL = 5; // inside an equal sign
    private static final int ISI_EQUAL2 = 6; // after key but not yet value or equal Note: EQUAL2 was revised
    private static final int ISI_VALUE = 7; // inside a value
    private static final int ISI_VALUE_A_BSLASH = 8; // inside a value after backslash
    private static final int ISI_VALUE_AT_NL = 9; // inside a value at new line
    private static final int ISI_EQUAL_AT_NL = 10; // between key and not yet value at new line


    public PropertiesSyntax() {
        tokenContextPath = PropertiesTokenContext.contextPath;
    }

    protected TokenID parseToken() {
        char actChar;

        while(offset < stopOffset) {
            actChar = buffer[offset];

            switch (state) {
            case INIT:
                switch (actChar) {
                case '\n':
                    offset++;
                    return PropertiesTokenContext.EOL;
                case '\t':
                case '\f':
                case ' ':
                    offset++;
                    return PropertiesTokenContext.TEXT;
                case '#':
                case '!':
                    state = ISI_LINE_COMMENT;
                    break;
                case '=': // in case the key is an empty string (first non-white is '=' or ':')
                case ':':
                    state = ISI_EQUAL;
                    return PropertiesTokenContext.TEXT;
                case '\\': // when key begins with escape
                    state = ISI_KEY_A_BSLASH;
                    break;
                default:
                    state = ISI_KEY;
                    break;
                }
                break; // end state INIT

            case ISI_LINE_COMMENT:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return PropertiesTokenContext.LINE_COMMENT;
                }
                break; // end state ISI_LINE_COMMENT

            case ISI_KEY:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return PropertiesTokenContext.KEY;
                case '\\':
                    state = ISI_KEY_A_BSLASH;
                    break;
                case '=':
                case ':':
                case ' ': // the whitspaces after key
                case '\t':
                    state = ISI_EQUAL;
                    return PropertiesTokenContext.KEY;
                }
                break; // end state ISI_KEY

            case ISI_KEY_A_BSLASH:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return PropertiesTokenContext.KEY;
                default:
                    state = ISI_KEY;
                }
                break; // end state ISI_KEY_A_BSLASH

            case ISI_EQUAL:
                switch (actChar) {
                case '=':
                case ':':
                    offset++;
                    state = ISI_VALUE;
                    return PropertiesTokenContext.EQ;
                case ' ': // whitespaces also separates key from value: note which whitespaces can do that
                case '\t':
                    break;
                case '\\': // in case of alone '\\' line continuation character
                    state = ISI_EQUAL2;
                    break;
                case '\n':
                    state = INIT;
                    return PropertiesTokenContext.EQ;
                default:
                    state = ISI_VALUE;
                }
                break; // end state ISI_KEY

            // only for case the last "\\" continuation char is but was not startes value yet (still can appear : or = char)
            case ISI_EQUAL2:
                switch (actChar) {
                case '\n':
                    state = ISI_EQUAL_AT_NL;
                    return PropertiesTokenContext.EQ; // PENDING
                default:
                    state = ISI_VALUE;
                }
                break; // end state ISI_EQUAL_A_BSLASH
                
            // in case of end of line     
            case ISI_EQUAL_AT_NL:
                switch (actChar) {
                case '\n':
                    offset++;
                    state = ISI_EQUAL;
                    return PropertiesTokenContext.EOL;
                default:
                    throw new Error("Something smells 4");
                }
                
// this previous version of ISI_EQUAL2 is needless because ':=' is not separator the second = char belongs to the value already
//            case ISI_EQUAL2:
//                switch (actChar) {
//                case '\n':
//                    state = INIT;
//                    return EQ;
//                case '=':
//                case ':':
//                    offset++;
//                    state = ISI_VALUE;
//                    return EQ;
//                default:
//                    state = ISI_VALUE;
//                    return EQ;
//                }
                //break; // end state ISI_KEY

            case ISI_VALUE:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return PropertiesTokenContext.VALUE;
                case '\\':
                    state = ISI_VALUE_A_BSLASH;
                    break;
                }
                break; // end state ISI_KEY

            case ISI_VALUE_A_BSLASH:
                switch (actChar) {
                case '\n':
                    state = ISI_VALUE_AT_NL;
                    return PropertiesTokenContext.VALUE;
                default:
                    state = ISI_VALUE;
                }
                break; // end state ISI_KEY

            case ISI_VALUE_AT_NL:
                switch (actChar) {
                case '\n':
                    offset++;
                    state = ISI_VALUE;
                    return PropertiesTokenContext.EOL;
                default:
                    throw new Error("Something smells 2");
                }
                //break; // end state ISI_KEY

            default:
                throw new Error("Unhandled state " + state);

            } // end of the outer switch statement

            offset = ++offset;

        } // end of while loop

        /* At this stage there's no more text in the scanned buffer. */

        if (lastBuffer || !lastBuffer) {
            switch(state) {
            case ISI_LINE_COMMENT:
                return PropertiesTokenContext.LINE_COMMENT;
            case ISI_KEY:
            case ISI_KEY_A_BSLASH:
                return PropertiesTokenContext.KEY;
            case ISI_EQUAL:
            case ISI_EQUAL2:
                return PropertiesTokenContext.EQ;
            case ISI_VALUE:
            case ISI_VALUE_A_BSLASH:
                return PropertiesTokenContext.VALUE;
            case ISI_VALUE_AT_NL:
            case ISI_EQUAL_AT_NL: // TEMP
                throw new Error("Something smells 3");
            }
        }

        return null;

    } // parseToken

    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case ISI_LINE_COMMENT:
            return "ISI_LINE_COMMENT";
        case ISI_KEY:
            return "ISI_KEY";
        case ISI_KEY_A_BSLASH:
            return "ISI_KEY_A_BSLASH";
        case ISI_EQUAL:
            return "ISI_EQUAL";
        case ISI_EQUAL2:
            return "ISI_EQUAL2";
        case ISI_EQUAL_AT_NL:
            return "ISI_EQUAL_AT_NL";
        case ISI_VALUE:
            return "ISI_VALUE";
        case ISI_VALUE_A_BSLASH:
            return "ISI_VALUE_A_BSLASH";
        case ISI_VALUE_AT_NL:
            return "ISI_VALUE_AT_NL";
        default:
            return super.getStateName(stateNumber);
        }
    }


}

/*
 * <<Log>>
 *  5    Jaga      1.3.1.0     3/15/00  Miloslav Metelka Structural change
 *  4    Gandalf   1.3         1/12/00  Petr Jiricka    Syntax coloring API 
 *       fixes
 *  3    Gandalf   1.2         12/28/99 Miloslav Metelka Structural change and 
 *       some renamings
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */

