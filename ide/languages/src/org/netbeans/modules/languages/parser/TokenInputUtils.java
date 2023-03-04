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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.*;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.parser.Parser.Cookie;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.languages.lexer.SLexer;


public abstract class TokenInputUtils {

    public static TokenInput create (
        Language        language,
        Parser          parser, 
        CharInput       input
    ) {
        return new TokenInputImpl (
            new TokenReader (language, parser, input),
            input
        );
    }

    public static TokenInput create (ASTToken[] array) {
        return new ArrayInput (array);
    }

    public static TokenInput create (List list) {
        return new ListInput (list);
    }

    
    // innerclasses ............................................................

    private static class TokenReader {
        
        private Language        language;
        private Parser          parser;
        private CharInput       input;
        private int             state = -1;
        private Cookie          cookie = new MyCookie ();


        private TokenReader (
            Language            language,
            Parser              parser, 
            CharInput           input
        ) {
            this.language = language;
            this.parser = parser;
            this.input = input;
        }

        private ASTToken next;

        public ASTToken nextToken (CharInput input) {
            if (next == null)
                next = readToken (input);
            return next;
        }

        public ASTToken readToken (CharInput input) {
            if (input.eof ()) return null;
            if (next != null) {
                ASTToken p = next;
                next = null;
                return p;
            }
            ASTToken token = parser.read (cookie, input, language);
            if (token != null) return token;
            int offset = input.getIndex ();
            return ASTToken.create (
                language,
                SLexer.ERROR_TOKEN_TYPE_NAME,
                String.valueOf (input.read ()),
                offset,
                1,
                null
            );
        }
    
        private class MyCookie implements Cookie {
            public int getState () {
                return state;
            }

            public void setState (int state) {
                TokenReader.this.state = state;
            }
            
            public void setProperties (Feature tokenProperties) {
            }
        }
    }

    private static class TokenInputImpl extends TokenInput {

        private TokenReader     tokenReader;
        private List<ASTToken>  tokens = new ArrayList<ASTToken> ();
        private int             index = 0;
        private CharInput       input;


        TokenInputImpl (TokenReader tokenReader, CharInput input) {
            this.input = input;
            this.tokenReader = tokenReader;
        }

        public ASTToken next (int i) {
            while (index + i - 1 >= tokens.size ())
                tokens.add (tokenReader.readToken (input));
            return (ASTToken) tokens.get (index + i - 1);
        }

        public boolean eof () {
            return next (1) == null;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            ASTToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                int i = tokens.size () - 1;
                do {
                    t = ((ASTToken) tokens.get (i--));
                } while (t == null && i > 0);
                if (t == null) return 0;
                return t.getOffset () + t.getLength ();
            } else {
                t = (ASTToken) next (1);
                return t.getOffset ();
            }
        }

        public ASTToken read () {
            ASTToken t = next (1);
            if (t != null) index++;
            return t;
        }

        public void setIndex (int index) {
            if (index > tokens.size ()) 
                throw new IndexOutOfBoundsException ();
            this.index = index;
        }

        public String getString (int from) {
            throw new InternalError ();
        }
        
        public String toString () {
            return input.toString ();
        }
    }

    private static class ArrayInput extends TokenInput {

        private ASTToken[] array;
        private int index = 0;
        private int length;

        private ArrayInput (ASTToken[] array) {
            this.array = array;
            length = array.length;
        }

        public ASTToken read () {
            if (index < length)
                return array [index++];
            return null;
        }

        public void setIndex (int index) {
            this.index = index;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            ASTToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                t = (ASTToken) array [array.length - 1];
                return t.getOffset () + t.getLength ();
            } else {
                t = (ASTToken) next (1);
                return t.getOffset ();
            }
        }
        
        public boolean eof () {
            return index >= length;
        }

        public ASTToken next (int i) {
            if (index + i - 1 < length)
                return array [index + i - 1];
            return null;
        }

        public String toString () {
            StringBuilder sb = new StringBuilder ();
            int i = index, j = 0;
            while (j < 10 && i < length) {
                sb.append (array [i]).append (" ");
                i++; j++;
            }
            return sb.toString ();
        }
    }

    private static class ListInput extends TokenInput {

        private List list;
        private int index = 0;
        private int length;

        private ListInput (List list) {
            this.list = list;
            length = list.size ();
        }

        public ASTToken read () {
            if (index < length)
                return (ASTToken) list.get (index++);
            return null;
        }

        public void setIndex (int index) {
            this.index = index;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            ASTToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                t = ((ASTToken) list.get (list.size () - 1));
                return t.getOffset () + t.getLength ();
            } else {
                t = (ASTToken) next (1);
                return t.getOffset ();
            }
        }

        public boolean eof () {
            return index >= length;
        }

        public ASTToken next (int i) {
            if (index + i - 1 < length)
                return (ASTToken) list.get (index + i - 1);
            return null;
        }

        public String toString () {
            StringBuilder sb = new StringBuilder ();
            int i = index, j = 0;
            while (j < 10 && i < length) {
                sb.append (list.get (i)).append (" ");
                i++; j++;
            }
            return sb.toString ();
        }
    }
}
