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
package org.netbeans.modules.web.core.syntax.parser;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Simple plain JSP syntax analyzer.
 *
 * Recognizes only JSP tags, directives and JSP comments.
 * 
 * Does NOT recognize Expression language and scripting elements!
 *
 * @author mfukala@netbeans.org
 */
public final class JspSyntaxParser {

    private final TokenHierarchy hi;
    private static final JspSyntaxElement SHARED_TEXT_ELEMENT = new JspSyntaxElement.SharedTextElement();
    
    private CharSequence source;
    
    public static Result parse(CharSequence source) {
        JspSyntaxParser instance = new JspSyntaxParser(source);
        List<JspSyntaxElement> elements = instance.parseDocument();
        return new Result(source, elements);
    }

    public static Result parse(Snapshot snapshot) {
        CharSequence sourceCode = snapshot.getText();
        JspSyntaxParser instance = new JspSyntaxParser(sourceCode, snapshot.getTokenHierarchy());
        List<JspSyntaxElement> elements = instance.parseDocument();
        return new Result(sourceCode, elements);
    }

    public JspSyntaxParser(CharSequence source) {
        this(source, TokenHierarchy.create(source, JspTokenId.language()));
    }
    
    public JspSyntaxParser(CharSequence source, TokenHierarchy<?> hierarchy) {
        this.source = source;
        this.hi = hierarchy;
    }

    //---------------------------- private methods -----------------------------
 
    private void error() {
        elements.add(new JspSyntaxElement.Error(source,
                start,
                ts.offset() + ts.token().length()));
    }

    private void text() {
        //Memory consumption optimalization: Since noone seems to use the text elements
        //there's no need to create a separate instance for each piece of text in the source.
        //Instead a shared instance is used, but of course none of the information
        //provided are valid
        elements.add(SHARED_TEXT_ELEMENT);
    }

    private void comment() {
        elements.add(new JspSyntaxElement.Comment(source,
                start, 
                ts.offset() + ts.token().length()));
    }
    
    private void scriptlet() {
        elements.add(new JspSyntaxElement.Scripting(source,
                start, 
                ts.offset() + ts.token().length()));
    }
    
    private void tag(boolean emptyTag) {
        List<JspSyntaxElement.Attribute> attributes = new ArrayList<JspSyntaxElement.Attribute>();
            for(int i = 0; i < attr_keys.size(); i++) {
                TokenInfo key = attr_keys.get(i);
                TokenInfo value = attr_values.get(i);
                
                if(value == null) {
                    //attribute has no value
                    JspSyntaxElement.Attribute ta = new JspSyntaxElement.Attribute(
                            key.token.text().toString().intern(),
                            null,
                            key.offset,
                            key.offset + key.token.length(),
                            0);
                    attributes.add(ta);
                } else {
                    JspSyntaxElement.Attribute ta = new JspSyntaxElement.Attribute(
                            key.token.text().toString().intern(),
                            value.token.text().toString().intern(),
                            key.offset, 
                            value.offset,
                            value.token.length());
                    attributes.add(ta);
                }
            }

        if(openTag) {
            elements.add(new JspSyntaxElement.OpenTag(source,
                    start,
                    ts.offset() + ts.token().length(),
                    tagName.intern(),
                    attributes.isEmpty() ? null : attributes,
                    emptyTag));
        } else {
            elements.add(new JspSyntaxElement.EndTag(source, start, ts.offset() + ts.token().length(), tagName));
        }
        
        tagName = null;
        attrib = null;
        attr_keys = new ArrayList<TokenInfo>();
        attr_values = new ArrayList<TokenInfo>();
    }

    //an error inside a tag, at least the tag name is known
    private void tag_with_error() {
        //lets put back the errorneous symbol first
        backup(1);
        //make the tag, we do not know if empty or not
        tag(false);
        
        state = S_INIT;
        start = -1;
    }

    private void dir() {
        List<JspSyntaxElement.Attribute> attributes = new ArrayList<JspSyntaxElement.Attribute>();
            for(int i = 0; i < attr_keys.size(); i++) {
                TokenInfo key = attr_keys.get(i);
                TokenInfo value = attr_values.get(i);

                if(value == null) {
                    //attribute has no value
                    JspSyntaxElement.Attribute ta = new JspSyntaxElement.Attribute(
                            key.token.text().toString().intern(),
                            null,
                            key.offset,
                            key.offset + key.token.length(),
                            0);
                    attributes.add(ta);
                } else {
                    JspSyntaxElement.Attribute ta = new JspSyntaxElement.Attribute(
                            key.token.text().toString().intern(),
                            value.token.text().toString().intern(),
                            key.offset,
                            value.offset,
                            value.token.length());
                    attributes.add(ta);
                }
            }

        elements.add(new JspSyntaxElement.Directive(source,
                start,
                ts.offset() + ts.token().length(),
                tagName.intern(),
                attributes.isEmpty() ? null : attributes));

        tagName = null;
        attrib = null;
        attr_keys = new ArrayList<TokenInfo>();
        attr_values = new ArrayList<TokenInfo>();
    }

    //an error inside a tag, at least the tag name is known
    private void dir_with_error() {
        //lets put back the errorneous symbol first
        backup(1);
        //make the tag, we do not know if empty or not
        dir();

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
        for(int i = 0; i < tokens; i++) {
            ts.movePrevious();
            token = ts.token();
        }
    }

    //STATES:
    //todo - use enum

    //common
    private static final int S_INIT = 0;
    private static final int S_COMMENT = 5;
    private static final int S_TEXT = 11;
    //tag
    private static final int S_TAG_OPEN_SYMBOL = 1;
    private static final int S_TAG = 2;
    private static final int S_TAG_ATTR = 3;
    private static final int S_TAG_VALUE = 4;
    private static final int S_TAG_AFTER_NAME = 12;
    //drirective
    private static final int S_DIR_OPEN_SYMBOL = 13;
    private static final int S_DIR = 14;
    private static final int S_DIR_ATTR = 15;
    private static final int S_DIR_VALUE = 16;
    private static final int S_DIR_AFTER_NAME = 19;
    
    private static final int S_SCRIPTLET_OPEN_SYMBOL = 20;
    private static final int S_SCRIPTLET_INSIDE = 21;

    private int state;
    private int start;
    private TokenSequence ts;
    private Token<JspTokenId> token;
    private List<JspSyntaxElement> elements;
    
    private boolean openTag = true;
    private String tagName = null;
    private TokenInfo attrib = null;
    private ArrayList<TokenInfo> attr_keys = null;
    private ArrayList<TokenInfo> attr_values = null;
    
    private List<JspSyntaxElement> parseDocument() {
        elements = new ArrayList<JspSyntaxElement>();
        ts = hi.tokenSequence(JspTokenId.language());

        state = S_INIT;
        start = -1;
        attr_keys = new ArrayList<TokenInfo>();
        attr_values = new ArrayList<TokenInfo>();
        
            while (ts.moveNext()) {
                token = ts.token();
                JspTokenId id = token.id();

                switch (state) {
                    case S_INIT:
                        switch (id) {
                            case SYMBOL:
                                if(CharSequenceUtilities.equals("<", token.text()) ||
                                        CharSequenceUtilities.equals("</", token.text())) { //NOI18N
                                    //jsp tag
                                    start = ts.offset();
                                    state = S_TAG_OPEN_SYMBOL;
                                } else if(CharSequenceUtilities.equals("<%@", token.text())) {
                                    //directive
                                    start = ts.offset();
                                    state = S_DIR_OPEN_SYMBOL;

                                }
                                break;
                            case COMMENT:
                                start = ts.offset();
                                state = S_COMMENT;
                                break;
                            case SYMBOL2:
                                //scriptlet delimiter: <%
                                start = ts.offset();
                                state = S_SCRIPTLET_OPEN_SYMBOL;
                                break;
                            default:
                                //everything else is just a text
                                start = ts.offset();
                                state = S_TEXT;
                                break;
                        }
                        break;
                        
                    case S_SCRIPTLET_OPEN_SYMBOL:
                        switch(id) {
                            case SCRIPTLET:
                                state = S_SCRIPTLET_INSIDE;
                                break;
                            default:
                                error();
                                state = S_INIT;
                                start = -1;
                                break;
                        }
                        break;

                    case S_SCRIPTLET_INSIDE:
                        switch(id) {
                            case SYMBOL2:
                                //closing scriptlet symbol: %>
                                scriptlet();
                                state = S_INIT;
                                start = -1;
                                break;
                                
                            case SCRIPTLET:
                                break; //SCRIPTLET tokens may? repeat possibly
                                
                            default:
                                error();
                                state = S_INIT;
                                start = -1;
                                break; 
                        }
                        break;
                        
                    case S_TEXT:
                        switch(id) {
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

                    case S_COMMENT:
                        switch(id) {
                            case COMMENT:
                            case EOL:
                            case WHITESPACE:
                                break;
                            default:
                                backup(1);
                                comment();
                                state = S_INIT;
                                start = -1;
                                break;
                        }
                        break;
                        
                    // <editor-fold defaultstate="collapsed" desc="in tag states handling">
                    case S_TAG_OPEN_SYMBOL:
                        switch (id) {
                            case TAG:
                                state = S_TAG_AFTER_NAME;
                                openTag = true;
                                tagName = token.text().toString();
                                break;
                            case ENDTAG:
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
                            case WHITESPACE:
                            case EOL:
                            case ERROR:
                                break;
                            case ATTRIBUTE:
                                state = S_TAG_ATTR;
                                attrib = tokenInfo();
                                break;
                            case SYMBOL:
                                boolean emptyTag = "/>".equals(token.text().toString());
                                tag(emptyTag);
                                state = S_INIT;
                                start = -1;
                                break;
                            default:
                                tag_with_error();
                                break;
                        }
                        break;


                    case S_TAG_ATTR:
                        switch (id) {
                            case WHITESPACE:
                                break;
                            case ATTR_VALUE:
                                backup(1); //backup the value
                                state = S_TAG_VALUE;
                                break;
                            case ATTRIBUTE:
                            case SYMBOL:
                                //equal sign
                                if (CharSequenceUtilities.equals("=", token.text())) {
                                    break;
                                } else {
                                    //the symbol is close tag symbol
                                }

                                //attribute without value
                                attr_keys.add(attrib);
                                attr_values.add(null);
                                state = S_TAG;
                                backup(1);
                                break;
                            default:
                                tag_with_error();
                                break;
                        }
                        break;

                    case S_TAG_VALUE:
                        switch (id) {
                            case ATTR_VALUE:
                                int index = attr_keys.indexOf(attrib);
                                if (index == -1) {
                                    attr_keys.add(attrib);
                                    attr_values.add(tokenInfo());
                                } else {
                                    attr_values.set(index, tokenInfo());
                                }

                                break;
                            case ERROR:
                                tag_with_error();
                                break;
                            default:
                                backup(1);
                                state = S_TAG;
                                break;
                        }
                        break;// </editor-fold>
                        
                    case S_DIR_OPEN_SYMBOL:
                        switch (id) {
                            case TAG:
                                state = S_DIR_AFTER_NAME;
                                tagName = token.text().toString();
                                break;
                            default:
                                reset(); //error
                                break;
                        }
                        break;

                    case S_DIR_AFTER_NAME:
                        //just switch to 'in dir state'
                        backup(1);
                        state = S_DIR;
                        break;

                    case S_DIR:
                        switch (id) {
                            case WHITESPACE:
                            case EOL:
                            case ERROR:
                                break;
                            case ATTRIBUTE:
                                state = S_DIR_ATTR;
                                attrib = tokenInfo();
                                break;
                            case SYMBOL:
                                //the symbol here might by the equal sign as well if the code is corrupted
                                if(CharSequenceUtilities.equals("%>", token.text())) {
                                    dir();
                                    state = S_INIT;
                                    start = -1;
                                    break;
                                }
                            default:
                                dir_with_error();
                                break;
                        }
                        break;


                    case S_DIR_ATTR:
                        switch(id) {
                            case WHITESPACE:
                                break;
                            case ATTR_VALUE:
                                backup(1); //backup the value
                                state = S_DIR_VALUE;
                                break;
                            case ATTRIBUTE:
                            case SYMBOL:
                                //equal sign
                                if(CharSequenceUtilities.equals("=", token.text())) {
                                    break;
                                } else {
                                    //the symbol is close tag symbol
                                }

                                //attribute without value
                                attr_keys.add(attrib);
                                attr_values.add(null);
                                state = S_DIR;
                                backup(1);
                                break;
                            default:
                                dir_with_error();
                                break;
                        }
                        break;

                    case S_DIR_VALUE:
                        switch(id) {
                            case ATTR_VALUE:
                                int index = attr_keys.indexOf(attrib);
                                if(index == -1) {
                                    attr_keys.add(attrib);
                                    attr_values.add(tokenInfo());
                                } else {
                                    attr_values.set(index, tokenInfo());
                                }

                                break;
                            case ERROR:
                                tag_with_error();
                                break;
                            default:
                                backup(1);
                                state = S_DIR;
                                break;
                        }
                        break;

                    
                }
            }
        
        
        if(state != S_INIT) {
            //an incomplete syntax element at the end of the file
            switch(state) {
                case S_COMMENT:
                    comment();
                    break;
                case S_TEXT:
                    text();
                    break;
                case S_TAG:
                case S_TAG_ATTR:
                case S_TAG_VALUE:
                case S_TAG_AFTER_NAME:
                    tag(false);
                    break;
                case S_DIR:
                case S_DIR_ATTR:
                case S_DIR_VALUE:
                case S_DIR_AFTER_NAME:
                    dir();
                    break;
                default:
                    error();
                    break;
            }
            
        }

        return elements;

    }

    private TokenInfo tokenInfo() {
        return new TokenInfo(ts.offset(), token);
    }

    private static final class TokenInfo {
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
