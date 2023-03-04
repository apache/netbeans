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
package org.netbeans.modules.xml.text.bracematch;

import java.util.Stack;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 * The brace matching algorithm is invokes by the brace matcher framework.
 * At a given context, findOrigin() is called, followed by findMatches() which
 * then tries to find a match.
 * 
 * In a XML document, following matches are carried out:
 * 1. PI_START/PI_END
 * 2. XML start and end tags
 * 3. CDATA section: <![CDATA[ with ]]>
 * 4. Declaration section: <!DOCTYPE with >
 * 5. Comments: <!-- with -->
 * 
 * @author Samaresh
 */
public class XMLBraceMatcher implements BracesMatcher {

    private static final String CDATA_START         = "<![CDATA[";  //NOI18N
    private static final String CDATA_END           = "]]>";        //NOI18N
    private static final String COMMENT_START       = "<!--";       //NOI18N
    private static final String COMMENT_END         = "-->";        //NOI18N
    private static final String DECLARATION_START   = "<!DOCTYPE";  //NOI18N
    private static final String DECLARATION_END     = ">";          //NOI18N
    
    private int searchOffset;
    private javax.swing.text.Document document;
    private MatcherContext context;
    private BracesMatcher defaultMatcher;
    
    public XMLBraceMatcher(MatcherContext context) {
        this(context.getDocument(), context.getSearchOffset());
        this.context = context;
    }
    
    //so that we could use it from unit test code
    XMLBraceMatcher(javax.swing.text.Document doc, int offset) {
        this.document = doc;
        this.searchOffset = offset;        
    }
    
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }
        //so that we could use this from unit tests
        int[] origin = doFindOrigin();
        if(origin != null)
            return origin;

        defaultMatcher = BracesMatcherSupport.defaultMatcher(context, -1, -1);
        return defaultMatcher.findOrigin();
    }
    
    public int[] doFindOrigin() throws InterruptedException, BadLocationException {
        AbstractDocument doc = (AbstractDocument)document;
        doc.readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence();
            Token token = atOffset(ts, searchOffset);
            token = ts.token();
            int start = ts.offset();
            if(token == null)
                return null;
            XMLTokenId id = (XMLTokenId)token.id();
            String tokenText = token.text().toString();

            switch(id) {
                case PI_START:
                case PI_END: {
                    return new int[] {start, start+token.length()};
                }
                case TAG: {
                    //for ">" move back till the start tag
                    if(">".equals(tokenText)) {
                        while(move(ts)) {
                            if(ts.token().id() == XMLTokenId.TAG)
                                break;
                        }
                    }                    
                    return findTagPosition(ts, false);
                }
                case BLOCK_COMMENT: {
                    if(!tokenText.startsWith(COMMENT_START) &&
                       !tokenText.endsWith(COMMENT_END))
                       return null;
                    return findGenericOrigin(ts, COMMENT_START, COMMENT_END);
                }
                case CDATA_SECTION: {
                    if(!tokenText.startsWith(CDATA_START) &&
                       !tokenText.endsWith(CDATA_END))
                        return null;
                    return findGenericOrigin(ts, CDATA_START, CDATA_END);
                }
                case DECLARATION: {
                    if(!tokenText.startsWith(DECLARATION_START) &&
                       !tokenText.endsWith(DECLARATION_END))
                        return null;
                    return findGenericOrigin(ts, DECLARATION_START, DECLARATION_END);
                }

                default:
                    break;
            }
        } finally {
            doc.readUnlock();
        }
        return null;
    }
            
    public int[] findMatches() throws InterruptedException, BadLocationException {
       
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }

        if(defaultMatcher != null) {
            return defaultMatcher.findMatches();
        }

        return doFindMatches();
    }
    
    /**
     * Moves the token sequence in the search direction
     */
    private boolean move(TokenSequence s) {
        if (context == null || context.isSearchingBackward()) {
            return s.movePrevious();
        } else {
            return s.moveNext();
        }
    }
    
    public int[] doFindMatches() throws InterruptedException, BadLocationException {
        AbstractDocument doc = (AbstractDocument)document;
        doc.readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence();
            Token token = atOffset(ts, searchOffset);
            if(token == null) return null;
            XMLTokenId id = (XMLTokenId)token.id();
            switch(id) {
                case PI_START: {
                    return findMatchingPair(ts, XMLTokenId.PI_END, true);
                }
                case PI_END: {
                    return findMatchingPair(ts, XMLTokenId.PI_START, false);
                }
                case TAG: {
                    //for ">" move back till the start tag
                    if(">".equals(ts.token().text().toString())) {
                        while(ts.movePrevious()) {
                            if(ts.token().id() == XMLTokenId.TAG)
                                break;
                        }
                    }                    
                    String tagName = ts.token().text().toString();
                    if(tagName.startsWith("</")) {
                        return findMatchingTagBackward(ts, tagName.substring(2));
                    }
                    if(tagName.startsWith("<")) {
                        return findMatchingTagForward(ts, tagName.substring(1));
                    }
                }
                case BLOCK_COMMENT: {
                    return findGenericMatch(ts, COMMENT_START, COMMENT_END);
                }
                case CDATA_SECTION: {
                    return findGenericMatch(ts, CDATA_START, CDATA_END);
                }
                case DECLARATION: {
                    return findGenericMatch(ts, DECLARATION_START, DECLARATION_END);
                }
            }
        } finally {
            doc.readUnlock();
        }
        return null;
    }
    
    /**
     * Finds the starting tag token for the specified offset. It respects the 
     * search direction in the search context. May return {@code null} in the
     * case of an error or when tag could not be found.
     * 
     * @return starting tag Token
     */
    private Token atOffset(TokenSequence ts, int offset) {
        int diff = ts.move(offset);
        if (diff == 0 && (context == null || context.isSearchingBackward())) {
            if (!ts.movePrevious()) {
                return null;
            }
        } else {
            if (!ts.moveNext()) {
                return null;
            }
        }
        if (diff == ts.token().text().length() - 1 && (context != null && !context.isSearchingBackward())) {
            if (!ts.moveNext()) {
                return null;
            }
        }
        
        do {
            Token t = ts.token();
            XMLTokenId id = (XMLTokenId)t.id();
            switch (id) {
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                case WS:
                    // continue the cycle
                    break;
                default:
                    return t;
            }
        } while (ts.movePrevious());
        return ts.token();
    }
    
    private static Token findTokenAtContext(TokenSequence ts, int offset) {
        ts.move(offset);
        Token token = ts.token();
        //there are cases when this could be null
        //in which case use the next one.
        if(token == null) {
            ts.moveNext();
            token = ts.token();
        }
        return token;
    }
        
    /**
     * For start tag such as "<tag name="hello">, return start position at '<'
     * and end position at '>'
     * 
     * For end tag such as "</tag>, return start position at '<' and end at '>'
     */
    private int[] findTagPosition(TokenSequence ts, boolean tagNameOnly) {
        //no brace matching for />
        if("/>".equals(ts.token().text().toString()))
            return null;        
        Token token = ts.token();
        int start = ts.offset();
        int tagNameEnd = start + token.length();
        
        int end = start+token.length();
        while(ts.moveNext()) {
            Token t = ts.token();
            end+=t.length();
            if(t.id() == XMLTokenId.TAG) {
                //no match for tag which ends without an end tag e.g. <hello/>
                if("/>".equals(t.text().toString()))
                    return null;
                if(">".equals(t.text().toString())) {
                    if (tagNameOnly) {
                        return new int[]{start, tagNameEnd, end -1, end};
                    } else {
                        return new int[]{start, end, start, tagNameEnd, end -1, end};
                    }
                }
                return new int[]{start, start+token.length()};
            }
        }
        return null;        
    }
    
    /**
     * Match paired tokens such as PI_START/PI_END.
     */
    private int[] findMatchingPair(TokenSequence ts, XMLTokenId idToMatch, boolean isForward) {
        while(isForward?ts.moveNext():ts.movePrevious()) {
            Token t = ts.token();
            //we don't want to scan the entire document
            //if it hits a tag before match, return null
            if(t.id() == XMLTokenId.TAG)
                return null;
            if(t.id() == idToMatch) {
                int start = ts.offset();
                int end = start+t.length();
                return new int[] {start, end};
            }
        }
        return null;        
    }
    
    private int[] findMatchingTagForward(TokenSequence ts, String tagToMatch) {
        Stack<String> stack = new Stack<String>();
        while(ts.moveNext()) {
            Token t = ts.token();
            if(XMLTokenId.TAG != t.id())
                continue;
            String tag = t.text().toString();
            if(">".equals(tag))
                continue;
            if (tag.startsWith("</") || tag.equals("/>")) {
                if (stack.empty()) {
                    if (tag.length() == 3 || tag.equals("</" + tagToMatch)) {
                        return findTagPosition(ts, false);
                    } else {
                        return null;
                    }
                }
                stack.pop();
            } else {
                stack.push(tag.substring(1));
            }
        }
        
        return null;
    }
    
    private int[] findMatchingTagBackward(TokenSequence ts, String tagToMatch) {
        int matchL = tagToMatch.length();
        int nesting = 1;
        boolean selfClosing = false;
        while(ts.movePrevious()) {
            Token t = ts.token();
            if(XMLTokenId.TAG != t.id()) {
                continue;
            }
            String tag = t.text().toString();
            int tl = tag.length();
            if (tl == 0) {
                continue;
            }
            if (tl < 3 && tag.charAt(tl - 1) == '>') { // NOI18N
                selfClosing = tag.charAt(0) == '/'; // NOI18N
                continue;
            }
            if (tl < 2) {
                continue;
            }
            if (selfClosing) {
                // reset, do not count.
                selfClosing = false;
                continue;
            }
            selfClosing = false;
            // length checked above.
            if (tag.charAt(1) == '/') {
                nesting++;
            } else if (--nesting < 0) {
                // error, sorry.
                return null;
            }
            if (nesting == 0 && tl == matchL + 1) {
                if (tag.regionMatches(1, tagToMatch, 0, matchL)) {
                    return findTagPosition(ts, true);
                }
            }
        }
        
        return null;
    }
    
    /**
     * For CDATA and XML comments, there is no start and end tokens differentiator.
     * XML lexer just gives us one token and we have to find the origin in there.
     */
    private int[] findGenericOrigin(TokenSequence ts, String startTag, String endTag) {
        Token token = ts.token();
        String text = token.text().toString();
        int start = ts.offset();
        int end = start+startTag.length();
        
        //if context.getSearchOffset() is inside start tag such as "<!--"
        if(text.startsWith(startTag) &&
           start <= searchOffset && end > searchOffset)
            return new int[] {start, end};
        
        //if context.getSearchOffset() is inside end tag such as "-->"
        start = ts.offset() + token.length()-endTag.length();
        end = start+endTag.length();
        if(text.toString().endsWith(endTag) &&
           start <= searchOffset && end >= searchOffset)
            return new int[] {start, end};
        
        //if none works return null
        return null;
    }

    /**
     * For CDATA and XML comments, there is no start and end tokens differentiator.
     * XML lexer just gives us one token and we have to find the origin in there.
     */
    private int[] findGenericMatch(TokenSequence ts, String startTag, String endTag) {
        Token token = ts.token();
        int offset = ts.offset();
        int start = offset;
        int end = start+startTag.length();
        
        //when the cursor is inside "<!--" return the end position for "-->"
        if(token.text().toString().startsWith(startTag) &&
           start <= searchOffset && end > searchOffset) {
            return findGenericMatchForward(ts, endTag);
        }
        
        //when the cursor is inside "-->" return the start position for "<!--"
        start = offset + token.length()-endTag.length();
        end = start+endTag.length();
        if(token.text().toString().endsWith(endTag) &&
           start <= searchOffset && end >= searchOffset) {
            return findGenericMatchBackward(ts, startTag);
        }
        
        //if none works return null
        return null;
    }
    
    private int[] findGenericMatchForward(TokenSequence ts, String endTag) {
        Token token = ts.token();
        int start = ts.offset() + token.length()-endTag.length();
        int end = start+endTag.length();
        if(token.text().toString().endsWith(endTag)) {
            return new int[]{start, end};
        }
        while(ts.moveNext()) {
            Token t = ts.token();
            if(t.id() == token.id() && t.text().toString().endsWith(endTag)) {
                start = ts.offset() + t.length()-endTag.length();
                end = start+endTag.length();
                return new int[]{start, end};
            }
        }
        return null;
    }
    
    private int[] findGenericMatchBackward(TokenSequence ts, String startTag) {
        Token token = ts.token();
        int start = ts.offset();
        int end = start+startTag.length();
        if(token.text().toString().startsWith(startTag)) {
            return new int[]{start, end};
        }
        while(ts.movePrevious()) {
            Token t = ts.token();
            if(t.id() == token.id() && t.text().toString().startsWith(startTag)) {
                start = ts.offset();
                return new int[]{start, start+startTag.length()};
            }
        }
        return null;
    }    
    
    /**
     * Checks to see if an end tag exists for a start tag at a given offset.
     * @param document
     * @param offset
     * @return true if an end tag is found for the start, false otherwise.
     */
    public static boolean hasEndTag(Document document, int offset, String startTag) {
        AbstractDocument doc = (AbstractDocument)document;
        doc.readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence();
            Token token = findTokenAtContext(ts, offset);
            Stack<String> stack = new Stack<String>();
            while(ts.moveNext()) {
                Token t = ts.token();
                if(XMLTokenId.TAG != t.id())
                    continue;
                String tag = t.text().toString();
                if(">".equals(tag))
                    continue;
                if(stack.empty()) {
                    if(("</"+startTag).equals(tag)) {
                        stack.empty();
                        stack = null;
                        return true;
                    }
                } else {                
                    if(tag.equals("/>") || ("</"+stack.peek()).equals(tag)) {
                        stack.pop();
                        continue;
                    }
                }
                stack.push(tag.substring(1));
            }            
        } finally {
            doc.readUnlock();
        }
        
        return false;
    }
    
}
