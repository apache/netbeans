/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
