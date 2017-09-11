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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.html.editor.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.plain.*;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Html syntax analyzer/plain parser
 *
 * @author mfukala@netbeans.org
 */
public class ElementsParser implements Iterator<Element> {

    //parser state
    private int state;
    //parser state constants
    private static final int S_INIT = 0;
    private static final int S_TAG_OPEN_SYMBOL = 1;
    private static final int S_TAG = 2;
    private static final int S_TAG_ATTR = 3;
    private static final int S_TAG_VALUE = 4;
    private static final int S_COMMENT = 5;
    private static final int S_DECLARATION = 6;
    private static final int S_DOCTYPE_DECLARATION = 7;
    private static final int S_DOCTYPE_AFTER_ROOT_ELEMENT = 8;
    private static final int S_DOCTYPE_PUBLIC_ID = 9;
    private static final int S_DOCTYPE_FILE = 10;
    private static final int S_TEXT = 11;
    private static final int S_TAG_AFTER_NAME = 12;
    //eof parser state constants
    public static final String UNEXPECTED_SYMBOL_IN_OPEN_TAG = "unexpected_symbol_in_open_tag"; //NOI18N
    private CharSequence sourceCode;
    private TokenSequence<HTMLTokenId> ts;
    //inner parsing states
    private Token<HTMLTokenId> token;
    private int start;
    private boolean openTag = true;
    private String tagName;
    private TokenInfo attrib;
    private List<TokenInfo> attr_keys;
    private List<List<TokenInfo>> attr_values;
    private Element current;
    private boolean eof;
    private AtomicReference<Element> lastFoundElement;
    private String root_element, doctype_public_id, doctype_file, doctype_name;

    /* The {@link TokenSequence} needs to be properly positioned. */
    private ElementsParser(CharSequence sourceCode, TokenSequence<HTMLTokenId> tokenSequence) {
        this.sourceCode = sourceCode;
        this.ts = tokenSequence;
        
        state = S_INIT;
        start = -1;
        attr_keys = new ArrayList<>();
        attr_values = new ArrayList<>();
        eof = false;
    }

    public static ElementsParser forOffset(CharSequence sourceCode, TokenSequence<HTMLTokenId> tokenSequence, int position) {
        if (position < 0) {
            throw new IllegalArgumentException(String.format("Position (%s) must be positive", position));
        }
        
        int diff = tokenSequence.move(position);
        if (diff != 0) {
            throw new IllegalArgumentException(String.format("Parser must be started "
                    + "at a token beginning, not in the middle (position=%s, token diff=%s, token=%s)",
                    position, diff, (tokenSequence.moveNext() ? tokenSequence.token() : null))); //NOI18N
        }
        return new ElementsParser(sourceCode, tokenSequence);
    }
    
    public static ElementsParser forTokenIndex(CharSequence sourceCode, TokenSequence<HTMLTokenId> tokenSequence, int tokenIndex) {
        if (tokenIndex < 0) {
            throw new IllegalArgumentException(String.format("TokenSequence index (%s) must be positive", tokenIndex));
        }
        tokenSequence.moveEnd();
        int lastTokenIndex = tokenSequence.index();
        if(tokenIndex > lastTokenIndex) {
            throw new IllegalArgumentException(String.format("token index (%s) is bigger than last index in the sequence (%s)", tokenIndex, lastTokenIndex));
        }
        tokenSequence.moveIndex(tokenIndex);
        return new ElementsParser(sourceCode, tokenSequence);
    }
    
    @Override
    public boolean hasNext() {
        if (lastFoundElement == null) {
            lastFoundElement = new AtomicReference<>(findNextElement());
        }
        return lastFoundElement.get() != null;
    }

    @Override
    public Element next() {
        if (!hasNext()) {
            throw new IllegalStateException("No such element");
        }
        Element element = lastFoundElement.get();
        lastFoundElement = null;
        return element;
    }

    @Override
    public void remove() {
        //no-op
    }

    //---------------------------- private methods -----------------------------
    private void error() {
        current = new ErrorElement(sourceCode,
                start,
                (short) (ts.offset() + ts.token().length() - start));
    }

    private void text() {
        current = new TextElement(start, ts.offset() + ts.token().length());
    }

    private void entityReference() {
        current = new EntityReferenceElement(sourceCode,
                start,
                (short) (ts.offset() + ts.token().length() - start));

    }

    private void comment() {
        current = new CommentElement(sourceCode,
                start,
                ts.offset() + ts.token().length() - start);
    }

    private void declaration() {
        current = new DeclarationElement(sourceCode,
                start,
                (short) (ts.offset() + ts.token().length() - start),
                root_element,
                doctype_public_id,
                doctype_file,
                doctype_name);
    }

    private void tag(boolean emptyTag) {
        tag(emptyTag, null);
    }

    private void tag(boolean emptyTag, ProblemDescription problem) {
        List<Attribute> attributes = new ArrayList<>(1); //use small initial capacity since typically there are one or two attribs (if any)
        for (int i = 0; i < attr_keys.size(); i++) {
            TokenInfo key = attr_keys.get(i);
            List<TokenInfo> values = attr_values.get(i);
            StringBuilder joinedValue = new StringBuilder();

            if (values == null) {
                //attribute has no value
                assert key.token.length() < Short.MAX_VALUE;
                Attribute ta = new AttributeElement(
                        sourceCode,
                        key.offset,
                        (short) key.token.length());
                attributes.add(ta);
            } else {
                if (values.size() == 1) {
                    //one part value
                    TokenInfo ti = values.get(0);

                    assert key.token.length() < Short.MAX_VALUE;
                    Attribute ta = new AttributeElement(
                            sourceCode,
                            key.offset,
                            ti.offset,
                            (short) key.token.length(),
                            ti.token.length());

                    attributes.add(ta);

                } else {
                    //multipart value
                    for (TokenInfo t : values) {
                        joinedValue.append(t.token.text());
                    }

                    TokenInfo firstValuePart = values.get(0);
                    TokenInfo lastValuePart = values.get(values.size() - 1);

                    Attribute ta = new AttributeElement.AttributeElementWithJoinedValue(
                            sourceCode,
                            key.offset,
                            (short) key.token.length(),
                            firstValuePart.offset,
                            joinedValue.toString().intern());

                    attributes.add(ta);
                }
            }
        }

        //Bug 220775 - AssertionError: element length must be positive! debug>>>
        if (start == -1) {
            throw new IllegalStateException(getCodeSnippet());
        }
        int len = ts.offset() + ts.token().length() - start;
        if (len <= 0) {
            throw new IllegalStateException(getCodeSnippet());
        }
        //<<<

        if (openTag) {

            if (attributes.isEmpty()) {
                //no attributes
                if (problem == null) {
                    current = new AttributelessOpenTagElement(
                            sourceCode,
                            start,
                            (short) len,
                            (byte) tagName.length(),
                            emptyTag);
                } else {
                    current = new ProblematicAttributelessOpenTagElement(
                            sourceCode,
                            start,
                            (short) len,
                            (byte) tagName.length(),
                            emptyTag,
                            problem);

                }
            } else {
                //attributes
                if (problem == null) {
                    //open tag w/o error
                    if (len > Short.MAX_VALUE) {
                        //unusually long element
                        current = new LongOpenTagElement(
                                sourceCode,
                                start,
                                len,
                                (byte) tagName.length(),
                                attributes,
                                emptyTag);
                    } else {
                        current = new OpenTagElement(
                                sourceCode,
                                start,
                                (short) len,
                                (byte) tagName.length(),
                                attributes,
                                emptyTag);
                    }
                } else {
                    //open tag w/ error
                    //note: the ProblematicOpenTagElement also extends LongOpenTagElement 
                    current = new ProblematicOpenTagElement(
                            sourceCode,
                            start,
                            (short) len,
                            (byte) tagName.length(),
                            attributes,
                            emptyTag,
                            problem);

                }
            }
        } else {
            current = new EndTagElement(
                    sourceCode,
                    start,
                    (short) len,
                    (byte) tagName.length());
        }

        tagName = null;
        attrib = null;
        attr_keys = new ArrayList<>();
        attr_values = new ArrayList<>();
    }
    private static final int SNIPPET_LEN = 100;

    private String getCodeSnippet() {
        int offset = ts.offset();
        int from = Math.max(0, offset - (SNIPPET_LEN / 2));
        int to = Math.min(sourceCode.length(), offset + (SNIPPET_LEN / 2));
        return sourceCode.subSequence(from, to).toString();
    }

    //an error inside a tag, at least the tag name is known
    private void tag_with_error(ProblemDescription problem) {
        //lets put back the errorneous symbol first
        backup(1);
        //make the tag, we do not know if empty or not
        tag(false, problem);

        state = S_INIT;
        start = -1;
    }

    //recover from error
    private void reset() {
        backup(1);
        //create error element excluding the last token caused the error
        error();
        state = S_INIT;
        start = -1;
    }

    private void backup(int tokens) {
        for (int i = 0; i < tokens; i++) {
            ts.movePrevious();
            token = ts.token();
        }
    }

    private Element findNextElement() {
        Element element = null;
        //parse tokens until a syntaxelement is found
        while (!eof && (element = processNextToken()) == null) {
            //no-op
        }
        return element;
    }

    private Element processNextToken() {
        current = null;

        if (!ts.moveNext()) {
            //eof
            handleEOF(); //may possibly set current element
            eof = true; //finish the parsing cycle
            return current;
        }

        int offset = ts.offset();
        token = ts.token();
        HTMLTokenId id = token.id();

        switch (state) {
            case S_INIT:
                switch (id) {
                    case CHARACTER:
                        start = ts.offset();
                        entityReference();
                        state = S_INIT;
                        start = -1;
                        break;
                    case TAG_OPEN_SYMBOL:
                        start = ts.offset();
                        state = S_TAG_OPEN_SYMBOL;
                        break;
                    case BLOCK_COMMENT:
                        start = ts.offset();
                        state = S_COMMENT;
                        break;
                    case DECLARATION:
                        start = ts.offset();
                        if (LexerUtils.equals("<!doctype", token.text(), true, true)) { //NOI18N
                            root_element = null;
                            doctype_public_id = null;
                            doctype_file = null;
                            state = S_DOCTYPE_DECLARATION;
                        } else {
                            state = S_DECLARATION;
                        }
                        doctype_name = token.text().subSequence(2, token.text().length()).toString(); //strip off the <! chars
                        break;
                    default:
                        //everything else is just a text
                        start = ts.offset();
                        state = S_TEXT;
                        break;
                }
                break;

            case S_TEXT:
                switch (id) {
                    case TEXT:
                        break;
                    default:
                        backup(1);
                        text();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_TAG_OPEN_SYMBOL:
                switch (id) {
                    case TAG_OPEN:
                        state = S_TAG_AFTER_NAME;
                        openTag = true;
                        tagName = token.text().toString();
                        break;
                    case TAG_CLOSE:
                        state = S_TAG_AFTER_NAME;
                        openTag = false;
                        tagName = token.text().toString();
                        break;
                    default:
                        reset(); //error
                        break;
                }
                break;

            case S_TAG_AFTER_NAME:
                //just switch to 'in tag state'
                backup(1);
                state = S_TAG;
                break;

            case S_TAG:
                switch (id) {
                    case WS:
                    case EOL:
                    case ERROR:
                        break;
                    case ARGUMENT:
                        state = S_TAG_ATTR;
                        attrib = tokenInfo();
                        break;
                    case TAG_CLOSE_SYMBOL:
                        boolean emptyTag = "/>".equals(token.text().toString());
                        tag(emptyTag);
                        state = S_INIT;
                        start = -1;
                        break;
                    default:
                        tag_with_error(
                                ProblemDescription.create(UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                }
                break;


            case S_TAG_ATTR:
                switch (id) {
                    case OPERATOR:
                    case WS:
                        break;
                    case VALUE:
                    case VALUE_JAVASCRIPT:
                    case VALUE_CSS:
                        backup(1); //backup the value
                        state = S_TAG_VALUE;
                        break;
                    case ARGUMENT:
                    case TAG_CLOSE_SYMBOL:
                        //attribute without value
                        attr_keys.add(attrib);
                        attr_values.add(null);
                        state = S_TAG;
                        backup(1);
                        break;
                    default:
                        tag_with_error(
                                ProblemDescription.create(UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                }
                break;

            case S_TAG_VALUE:
                switch (id) {
                    case VALUE:
                    case VALUE_JAVASCRIPT:
                    case VALUE_CSS:
                    case EL_OPEN_DELIMITER:
                    case EL_CONTENT:
                    case EL_CLOSE_DELIMITER:
                        int index = attr_keys.indexOf(attrib);
                        if (index == -1) {
                            List<TokenInfo> values = new ArrayList<>();
                            values.add(tokenInfo());
                            attr_keys.add(attrib);
                            attr_values.add(values);
                        } else {
                            List<TokenInfo> valueParts = attr_values.get(index);
                            //http://statistics.netbeans.org/exceptions/messageslog?id=679650
                            //NPE might happen as attr_values.get(index) might return null
                            //I cannot see the code path which leads to this so adding a silly NPE check
                            if(valueParts != null) {
                                valueParts.add(tokenInfo());
                            }
                        }

                        break;
                    case ERROR:
                        tag_with_error(
                                ProblemDescription.create(UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                    default:
                        backup(1);
                        state = S_TAG;
                        break;
                }
                break;

            case S_COMMENT:
                switch (id) {
                    case BLOCK_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        comment();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DECLARATION:
                switch (id) {
                    case DECLARATION:
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_DECLARATION:
                switch (id) {
                    case DECLARATION:
                        root_element = token.text().toString();
                        state = S_DOCTYPE_AFTER_ROOT_ELEMENT;
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_AFTER_ROOT_ELEMENT:
                switch (id) {
                    case DECLARATION:
                        if (LexerUtils.equals("public", token.text(), true, true)) { //NOI18N
                            doctype_public_id = new String();
                            state = S_DOCTYPE_PUBLIC_ID;
                            break;
                        } else if (LexerUtils.equals("system", token.text(), true, true)) { //NOI18N
                            state = S_DOCTYPE_FILE;
                            doctype_file = new String();
                            break;
                        } else if (token.text().charAt(0) == '>') {
                            declaration();
                            state = S_INIT;
                            start = -1;
                        }
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_PUBLIC_ID:
                switch (id) {
                    case WS:
                    case DECLARATION:
                        String tokenText = token.text().toString();
                        if (tokenText.startsWith("\"")) {
                            //first token
                            tokenText = tokenText.substring(1); //cut off the quotation mark
                        }
                        if (tokenText.endsWith("\"")) {
                            //last token
                            tokenText = tokenText.substring(0, tokenText.length() - 1); //cut off the quotation mark
                            doctype_public_id += tokenText; //short and rare strings, no perf problem
                            doctype_public_id = doctype_public_id.trim();
                            state = S_DOCTYPE_FILE;
                            break;
                        }
                        doctype_public_id += tokenText; //short and rare strings, no perf problem

                        break;
                    case SGML_COMMENT:
                    case EOL:

                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_FILE:
                switch (id) {
                    case DECLARATION:
                        doctype_file = token.text().toString();
                        //jump to simple sgml declaration so potentially 
                        //other declaration tokens are inluded
                        state = S_DECLARATION;
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

        } //switch end

        return current;

    }

    private void handleEOF() {
        if (state != S_INIT) {
            //an incomplete syntax element at the end of the file
            switch (state) {
                case S_COMMENT:
                    comment();
                    break;
                case S_DECLARATION:
                case S_DOCTYPE_AFTER_ROOT_ELEMENT:
                case S_DOCTYPE_DECLARATION:
                case S_DOCTYPE_FILE:
                case S_DOCTYPE_PUBLIC_ID:
                    declaration();
                    break;
                case S_TEXT:
                    text();
                    break;
                case S_TAG:
                case S_TAG_ATTR:
                case S_TAG_VALUE:
                    tag(false);
                    break;
                case S_TAG_AFTER_NAME:
                    tag(false);
                    break;
                default:
                    error();
                    break;
            }

        }
    }

    private TokenInfo tokenInfo() {
        return new TokenInfo(ts.offset(), token);


    }

    static final class TokenInfo {

        public int offset;
        public Token token;

        public TokenInfo(int offset, Token token) {
            this.offset = offset;
            this.token = token;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TokenInfo other = (TokenInfo) obj;
            if (this.offset != other.offset) {
                return false;
            }
            if (this.token != other.token && (this.token == null || !this.token.equals(other.token))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.offset;
            hash = 37 * hash + (this.token != null ? this.token.hashCode() : 0);
            return hash;
        }
    }
}
