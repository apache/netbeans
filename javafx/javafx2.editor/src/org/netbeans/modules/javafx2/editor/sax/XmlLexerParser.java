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
package org.netbeans.modules.javafx2.editor.sax;

import org.netbeans.modules.javafx2.editor.ErrorMark;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import static org.netbeans.modules.javafx2.editor.sax.Bundle.*;

/**
 * SAX-like parser, which uses XML lexer to tokenize the text and 
 * attempts to recover from simple "in the middle of typing" errors.
 *
 * Error recovery strategies:
 * 
 * Unclosed tag: pretend the tag is closed and is self-closed, so it ends 
 * immediately. 
 * 
 * Unclosed end tag:
 * emit subsequent child tags (going to level N+1 and deeper). 
 * 
 * 
 * @author sdedic
 */
public class XmlLexerParser implements ContentLocator {
    public static final String XMLNS_PREFIX = "xmlns"; // NOI18N
    public static final String NO_NAMESPACE_PREFIX = ""; // NOI18N
    public static final char PREFIX_DELIMITER_CHAR = ':'; // NOI18N
    public static final String TAG_CLOSE = ">"; // NOI18N
    public static final String OPEN_TAG = "<"; // NOI18N
    public static final String OPEN_CLOSING_TAG = "</"; // NOI18N
    public static final String SELF_CLOSING_TAG_CLOSE = "/>"; // NOI18N
    public static final char TAG_START_CHAR = '<'; // NOI18N
    public static final char TAG_END_CHAR = '>'; // NOI18N
    public static final char CLOSE_TAG_CHAR = '/'; // NOI18N
    
    private LexicalHandler  lexicalHandler;
    private ContentHandler  contentHandler;
    private SequenceContentHandler seqHandler;
    private TokenHierarchy  hierarchy;
    private TokenSequence<XMLTokenId>   seq;
    
    private LinkedList<Level>    levelStack = new LinkedList<Level>();
    
    private int errorCount;
    
    private static class Level {
        private String tagQName;
        private Map<String, String> savedPrefixes;
        private Collection<String>  newPrefixes = new LinkedList<String>();
    }
    
    private static final String ERR_UnexpectedToken = "unexpected-token"; // NOI18N
    private static final String ERR_DuplicateAttribute = "duplicate-attribute"; // NOI18N
    private static final String ERR_UnclosedValue = "unclosed-value"; // NOI18N
    private static final String ERR_InvalidTagContent = "invalid-tag-content"; // NOI18N
    private static final String ERR_TagNotFinished = "tag-not-finished"; // NOI18N
    private static final String ERR_MissingEndTag = "missing-end-tag"; // NOI18N
    private static final String ERR_UnexpectedTag = "unexpected-tag"; // NOI18N

    private List<ErrorMark>     errors = Collections.emptyList();
    
    private List<ErrorMark>     leftErrors = Collections.emptyList();
    
    private Level currentLevel;
    
    private void markError() {
        addError(ERR_UnexpectedToken, ERR_unexpectedToken(currentToken.text().toString()));
    }
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_elementNotClosed=Element is not closed: {0}"
    })
    private void markUnclosedElement(String qName) {
        addError(ERR_MissingEndTag, ERR_elementNotClosed(qName), qName);
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TokenSequence<XMLTokenId> getTokenSequence() {
        return (TokenSequence<XMLTokenId>)hierarchy.tokenSequence();
    }
    
    private void addError(String type, String msg, Object... params) {
        Token t = seq.token();
        
        ErrorMark mark = new ErrorMark(seq.offset(), t == null ? 1 : t.length(),
                type, msg, params);
        addError(mark);
    }
    
    private void addError(ErrorMark mark) {
        if (errors.isEmpty()) {
            errors = new ArrayList<ErrorMark>();
        }
        errors.add(mark);
        errorCount++;
    }
    
    private void resetOffsets() {
        elementOffset = -1;
        endOffset = -1;
        if (attrOffsets != null && attrOffsets.isEmpty()) {
            attrOffsets = null;
        }
        flush();
    }
    
    private int errorsFetched = 0;
    
    private void flush() {
        if (errorsFetched < errors.size()) {
            if (leftErrors.isEmpty()) {
                leftErrors = new ArrayList<ErrorMark>();
            }
            leftErrors.addAll(errors.subList(errorsFetched, errors.size()));
        }
        errorsFetched = 0;
        errors = Collections.emptyList();
        matchedTokens = Collections.emptyList();
        tokenOffsets = Collections.emptyList();
    }

    public XmlLexerParser(TokenHierarchy<?> hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        if (contentHandler instanceof ContentLocator.Receiver) {
            ((ContentLocator.Receiver)contentHandler).setContentLocator(this);
        }
        if (contentHandler instanceof SequenceContentHandler) {
            seqHandler = (SequenceContentHandler)contentHandler;
        }
    }
    
    private int[] targetOffset;
    private int[] dataOffset;
    
    private void parseProcessingInstruction() throws SAXException {
        StringBuilder content = new StringBuilder();
        String target = null;
        
        targetOffset = new int[] { -1, -1, -1, -1 };
        dataOffset  = new int[] { -1, -1, -1, -1 };
        
        skipWhitespace();
        
        Token<XMLTokenId> t = nextToken();
        if (t == null || t.id() != XMLTokenId.PI_TARGET) {
            markUnexpectedToken();
            return;
        }
        targetOffset[OFFSET_VALUE_START] =  targetOffset[OFFSET_START] = seq.offset();
        targetOffset[OFFSET_VALUE_END] = targetOffset[OFFSET_END] = seq.offset() + t.length();
        target = t.text().toString();
        consume();
        
        boolean dataPresent = false;
        
        skipWhitespace();
        while ((t = nextToken()) != null) {
            switch (t.id()) {
                case WS:
                    if (dataPresent) {
                        content.append(t.text().toString());
                    }
                    skipWhitespace();
                    break;

                case PI_CONTENT:
                    if (!dataPresent) {
                        dataPresent = true;
                        dataOffset[OFFSET_START] = dataOffset[OFFSET_VALUE_START] = seq.offset();
                    }
                    content.append(t.text().toString());
                    dataOffset[OFFSET_END] = dataOffset[OFFSET_VALUE_END] = seq.offset() + t.length();
                    consume();
                    break;
                case PI_END:
                    contentHandler.processingInstruction(target, content.toString().trim());
                    flush();
                    consume();
                    resetOffsets();
                    return;
                    
                default:
                    // bail out, the current processing instruction ends
                    contentHandler.processingInstruction(target, content.toString().trim());
                    // FIXME - add error info
                    endOffset = makeErrorOffset();
                    flush();
                    return;
            }
        }
    }
    
    private int makeErrorOffset() {
        return (- seq.offset()) - 1;
    }
    
    private XMLTokenId  forcedId;
    
    private void handleErrorAs(XMLTokenId id) {
        markError();
        forcedId = id;
    }
    
    private XMLTokenId getTokenId() {
        if (forcedId != null) {
            XMLTokenId id = this.forcedId;
            forcedId = null;
            return id;
        }
        return nextToken().id();
    }
    
    @SuppressWarnings("unchecked")
    public void parse() throws SAXException {
        seq = (TokenSequence<XMLTokenId>)hierarchy.tokenSequence();
        seq.move(0);
        parse2();
    }
    
    private void callCharacters(CharSequence t) throws SAXException {
        if (seqHandler != null) {
            seqHandler.characterSequence(t);
        } else {
            contentHandler.characters(t.toString().toCharArray(), 0, t.length());
        }
    }
    
    private void callCharacters(Token<XMLTokenId> t) throws SAXException {
        callCharacters(t.text());
    }
    
    private void callIgnorableWhitespace(Token<XMLTokenId> t) throws SAXException {
        if (seqHandler != null) {
            seqHandler.ignorableWhitespaceSequence(t.text());
        } else {
            contentHandler.ignorableWhitespace(t.text().toString().toCharArray(), 0, t.length());
        }
    }
    
    private int endDocOffset = -1;
    
    @NbBundle.Messages({
        "# {0} - token text",
        "ERR_UnexpectedToken=Unexpected token: {0}"
    })
    void parse2() throws SAXException {
        boolean whitespacePossible = true;
        
        Token<XMLTokenId> t;
        
        elementOffset = 0;
        contentHandler.startDocument();
        
        while ((t = nextToken()) != null) {
            resetOffsets();
            elementOffset = seq.offset();
            endOffset = elementOffset + t.length();
            switch (t.id()) {
                case PI_START:
                    consume();
                    parseProcessingInstruction();
                    whitespacePossible = true;
                    break;
                case BLOCK_COMMENT:
                    consume();
                    if (lexicalHandler != null) {
                        lexicalHandler.comment(t.text().toString().toCharArray(), 0, t.length());
                    }
                    break;
                case CDATA_SECTION:
                    consume();
                    whitespacePossible = false;
                    if (lexicalHandler != null) {
                        lexicalHandler.startCDATA();
                    }
                    callCharacters(t);
                    if (lexicalHandler != null) {
                        lexicalHandler.endCDATA();
                    }
                    break;
                case TAG: {
                    CharSequence cs = t.text();
                    if (cs.charAt(0) == TAG_START_CHAR && cs.length() > 1) {
                        if (cs.charAt(1) == CLOSE_TAG_CHAR) {
                            consume();
                            parseClosingTag(cs.subSequence(2, cs.length()).toString());
                        } else {
                            parseTag(t);
                        }
                    } else {
                        markError();
                        consume();
                        break;
                    }
                    whitespacePossible = true;
                    break;
                }
                case ERROR:
                case CHARACTER:
                    // character entity - will be reported as usual characters data
                case TEXT: 
                default: {
                    consume();
                    String s = t.text().toString();
                    if (whitespacePossible) {
                        String trimmed = s.trim();
                        if (trimmed.isEmpty()) {
                            callIgnorableWhitespace(t);
                            break;
                            
                        } else {
                            if (trimmed.startsWith("<")) { // NOI18N
                                // error, "<" is present in the text
                                int idx = s.indexOf(trimmed);
                                ErrorMark mark = new ErrorMark(seq.offset() + idx, 1,
                                        ERR_UnexpectedToken, ERR_unexpectedToken("<")); // NOI18N
                                addError(mark);

                                if (trimmed.length() == 1) {
                                    // consumed everything important
                                    break;
                                }
                                // move the offset until after the <
                                elementOffset = seq.offset() + idx + 1;
                                s = s.substring(idx);
                                
                            }
                        }
                    }
                    callCharacters(s);
                    whitespacePossible = false;
                    break;
                }
            }
        }
        endDocOffset = endOffset;
        
        while (!levelStack.isEmpty() && currentLevel != null) {
            markUnclosedElement(currentLevel.tagQName);
            String[] nsName = parseQName(currentLevel.tagQName);
            resetAndSetErrorOffsets();
            contentHandler.endElement(prefix2Uri.get(nsName[0]), nsName[1], currentLevel.tagQName);
            terminateLevel();
        }
        elementOffset = endDocOffset;
        contentHandler.endDocument();
        
        // report leftover errors
        this.errors = leftErrors;
    }
    
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    
    private void processPrefixMappings() throws SAXException {
        Map<String, String> savedPrefixMap = prefix2Uri;
        Map<String, String> prefixMap = null;
        
        for (String s : attrNames) {
            if (!s.startsWith(XMLNS_PREFIX)) {
                continue;
            }
            
            String prefix = NO_NAMESPACE_PREFIX; // NOI18N
            
            if (s.length() > 5 && s.charAt(5) == PREFIX_DELIMITER_CHAR) {
                prefix = s.substring(6);
            }
            
            if (prefixMap == null) {
                prefixMap = new HashMap<String, String>(savedPrefixMap);
            }
            currentLevel.newPrefixes.add(s);
            prefixMap.put(prefix, attrs.get(s));
            contentHandler.startPrefixMapping(prefix, attrs.get(s));
        }
        if (prefixMap != null) {
            this.prefix2Uri = prefixMap;
        }
        currentLevel.savedPrefixes = savedPrefixMap;
    }
    
    /**
     * Starts element's level. initializes attribute lists etc. Saves the current
     * level on the stack, and defines a new level.
     * 
     */
    private void startLevel() {
        this.attrNames = new ArrayList<String>();
        this.attrOffsets = new HashMap<String, int[]>();
        this.attrs = new HashMap<String, String>();

        this.currentLevel = new Level();
        currentLevel.tagQName = qName;
        currentLevel.savedPrefixes = prefix2Uri;
        levelStack.push(currentLevel);
    }
    
    /**
     * Finishes the element's level. 
     * @throws SAXException 
     */
    private void terminateLevel() throws SAXException {
        if (currentLevel == null) {
            return;
        }
        for (String s : currentLevel.newPrefixes) {
            contentHandler.endPrefixMapping(s);
        }
        prefix2Uri = currentLevel.savedPrefixes;
        levelStack.pop();

        if (levelStack.size() <= levelBound) {
            currentLevel = null;
            return;
        }
        currentLevel = levelStack.peek();
        
        if (currentLevel != null) {
            qName = currentLevel.tagQName;
            String[] qn = parseQName(qName);
            this.tagUri = prefix2Uri.get(qn[0]);
            this.tagName = qn[1];
        } else {
            qName = null;
            tagName = null;
            tagUri = null;
        }
    }
    
    private String[] parseQName(String qn) {
        int pos = qn.indexOf(PREFIX_DELIMITER_CHAR);
        if (pos == -1) {
            return new String[] {"", qn}; // NOI18N
        } else {
            return new String[] { qn.substring(0, pos), qn.substring(pos + 1) };
        }
    }
    
    private Object[] save() {
        return new Object[] {
            elementOffset, endOffset, tokenOffsets, matchedTokens
        };
    }

    @SuppressWarnings("unchecked")
    private void restore(Object[] o) {
        this.elementOffset = (Integer)o[0];
        this.endOffset = (Integer)o[1];
        this.tokenOffsets = (List<Integer>)o[2];
        this.matchedTokens = (List<Token<XMLTokenId>>)o[3];
    }
    
    private int endOffset;
    private int elementOffset;
    
    private String tagName;
    private String tagUri;
    private String qName;
    
    private Map<String, String>   attrs;
    private List<String>          attrNames;
    private Map<String, int[]>  attrOffsets;
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_unexpectedTag=Unexpected closing tag: {0}",
        "# {0} - tag name",
        "ERR_TagNotFinished=Tag is not finished: {0}",
        "# {0} - content",
        "ERR_InvalidTagContent=Invalid tag content: {0}"
    })
    private void parseClosingTag(String tagName) throws SAXException {
        this.elementOffset = seq.offset();
        
        Token<XMLTokenId> token;

        skipWhitespace();
        
        qName = tagName;
        
        out:
        while ((token = nextToken()) != null) {
            XMLTokenId id = token.id();
            
            switch (id) {
                case WS:
                    skipWhitespace();
                    break;
                    
                case TAG:
                    String s = token.text().toString();
                    if (s.equals(TAG_CLOSE)) {
                        consume();
                    } else {
                       addError(ERR_TagNotFinished, ERR_TagNotFinished(tagName));
                       markApproxEnd();
                    }
                    break out;
                    
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                    // just mark error, but consume and ignore
                    consume();
                    addError(ERR_InvalidTagContent, ERR_InvalidTagContent(token.text()));
                    break;
                    
                default:
                    markUnexpectedToken();
                    break out;
            }
        }

        processTagName(tagName);
        if (levelStack.isEmpty()) {
            // raise an error, do not emit 
            addError(new ErrorMark(this.elementOffset, seq.offset() + token.length() - this.elementOffset, ERR_MissingEndTag, ERR_unexpectedTag(tagName), tagName));
            terminateLevel();
            return;
        }
        if (levelStack.size() <= levelBound || this.qName.equals(currentLevel.tagQName)) {
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            return;
        }
        
        Object[] o = save();

        // case 1: the tagName may equal to a tag on the stack. This would indicate
        // one or more unclosed tags, which should be reported as "closed - error"
        if (findUpperOpening(tagName)) {
            restore(o);
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            return;
        }
        
        // ignore the tag completely; the content handler does not have a callback for this,
        // just ignore the end tag completely, and go on. If this tag was a mismatch for an existing open tag,
        // the next close tag will be hopefully found and the incorrectly opened open tag will be closed.
        addError(ERR_UnexpectedTag, ERR_unexpectedTag(tagName));
    }
    
    private void markApproxEnd() {
        this.endOffset = APPROX;
    }
    
    private void resetAndSetErrorOffsets() {
        resetAndSetErrorOffsets(-1);
    }
    
    private void resetAndSetErrorOffsets(int startError) {
        resetOffsets();
        if (endDocOffset != -1) {
            elementOffset = ( -endDocOffset ) - 1;
        } else {
            int o = startError > -1 ? startError : seq.offset();
            elementOffset = (- o) - 1;
        }
        endOffset = elementOffset;
    }
    
    /**
     * Attempts to find a place in the stack, which actually matches the text.
     * Matches the closing tags in the text to the stack
     * 
     * @param tagName
     * @return
     * @throws SAXException 
     */
    private boolean findUpperOpening(String tagName) throws SAXException {
        int depth = 0;
        Level found = null;
        
        for (Iterator<Level> it = levelStack.iterator(); it.hasNext(); ) {
            Level l = it.next();
            if (l.tagQName.equals(tagName)) {
                found = l;
                break;
            }
            depth++;
        }
        if (found == null) {
            return false;
        }
        /*
        if (depth == 1) {
            // simple case, the immediate parent matches the closing tagname
            // mark error, close this level etc
            markUnclosedElement(currentLevel.tagQName);
            String[] nsName = parseQName(currentLevel.tagQName);
            resetAndSetErrorOffsets(elementOffset);
            contentHandler.endElement(prefix2Uri.get(nsName[0]), nsName[1], currentLevel.tagQName);
            terminateLevel();
            return true;
        }*/
        
        // some additional assuarance is needed before popping more levels:
        // parse ahead and match 2* popped levels or root
        
        int markSequence = seq.offset();
        
        XmlLexerParser parser = duplicate();
        parser.consume();
        
        ParentCollector pm = new ParentCollector((depth - 1) * 2 + 1);
        parser.setContentHandler(pm);
        
        boolean success = false;
        try {
            parser.parse2();
        } catch (StopParseException ex) {
            // expected
        } finally {
            seq.move(markSequence);
            seq.moveNext();
        }
        
        Deque<String>    followingClosingTags = pm.closingTags;
        followingClosingTags.addLast(tagName);
        
        // try to match / find match on the stack:
        int count = 0;
        
        for (int stackPtr = 0; stackPtr < levelStack.size() - followingClosingTags.size(); stackPtr++) {
            Iterator<String> it = followingClosingTags.descendingIterator();
            boolean ok = true;
            for (int i = stackPtr; i >= 0 && it.hasNext(); i++) {
                String s = it.next();
                Level l = levelStack.get(i);
                if (!l.tagQName.equals(s)) {
                    // break the for cycle, move next
                    ok = false;
                    break;
                }
            }
            
            if (ok) {
                // stackPtr is the place 
                success = true;
                count = stackPtr;
                break;
            }
        }

        if (!success) {
            // no match in upper hierarchy
            return false;
        }

        for (int i = 0; i <= count && currentLevel != null; i++) {
            processTagName(qName = currentLevel.tagQName);
            markUnclosedElement(qName);
            // the elements are artifical; discard all offset information
            resetAndSetErrorOffsets();
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            /*
            markUnclosedElement(qName);
            // the elements are artifical; discard all offset information
            resetAndSetErrorOffsets();
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            processTagName(qName = currentLevel.tagQName);
            */
        }
        resetOffsets();
        
        return true;
    }
        
    static class StopParseException extends SAXException {
        private boolean success;

        public StopParseException(boolean success) {
            this.success = success;
        }
        
    }
    
    /**
     * Scan forward the token stream, and collect the closing tags up to 
     * 'matchLevels' count. 
     */
    private static class ParentCollector implements ContentHandler {
        int matchLevels;
        Deque<String> closingTags = new LinkedList<String>();
        int levelsBelow;
        
        ParentCollector(int matchLevels) {
            this.matchLevels = matchLevels;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            levelsBelow++;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (levelsBelow > 0) {
                levelsBelow--;
                return;
            }
            closingTags.push(qName);
            if (closingTags.size() == matchLevels) {
                throw new StopParseException(true);
            }
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }
    }
    
    private XmlLexerParser duplicate() {
        XmlLexerParser parser = new XmlLexerParser(hierarchy);
        parser.seq = seq;
        parser.levelStack = new LinkedList<Level>(levelStack);
        parser.levelBound = levelStack.size();
        parser.currentLevel = this.currentLevel;
        parser.currentToken = this.currentToken;
        
        return parser;
    }
    
    private int levelBound;
    
    private Token<XMLTokenId> currentToken;
    
    
    private Token<XMLTokenId> nextToken() {
        if (currentToken == null) {
            if (seq.moveNext()) {
                return currentToken = seq.token();
            } else {
                return null;
            }
        } else {
            return currentToken;
        }
    }
    
    private void consume() {
        if (currentToken != null) {
            if (matchedTokens.isEmpty()) {
                matchedTokens = new ArrayList<Token<XMLTokenId>>();
                tokenOffsets = new ArrayList<Integer>();
            }
            tokenOffsets.add(seq.offset());
            matchedTokens.add(currentToken);
            if (endOffset != APPROX) {
                endOffset = seq.offset() + currentToken.length();
            }
        }
        currentToken = null;
    }
    
    private void skipWhitespace() {
        Token<XMLTokenId> t;
        
        while ((t = nextToken()) != null && t.id() == XMLTokenId.WS) {
            consume();
        }
    }
    
    private int[] currentAttrOffsets;
    
    private int fuzzyOffset(int pos) {
        return -pos - 1;
    }
    
    private void markUnexpectedAttrToken() {
        if (nextToken() != null && nextToken().id() == XMLTokenId.ERROR) {
            return;
        }
        int offset = seq.offset();
        
        if (currentAttrOffsets[OFFSET_VALUE_START] == -1) {
            currentAttrOffsets[OFFSET_VALUE_START] = fuzzyOffset(offset);
        }
        currentAttrOffsets[OFFSET_END] = currentAttrOffsets[OFFSET_VALUE_END] = fuzzyOffset(offset);
        markUnexpectedToken();
    }
    
    @NbBundle.Messages({
        "# {0} - attribute name",
        "ERR_DuplicateAttribute=Duplicate attribute: {0}"
    })
    private boolean parseAttribute(Token<XMLTokenId> t) {
        String argName = t.text().toString();
        boolean ignore;
        
        if (attrs.containsKey(argName)) {
            addError(ERR_DuplicateAttribute, ERR_DuplicateAttribute(argName));
            ignore = true;
        } else {
            attrOffsets.put(argName, currentAttrOffsets = new int[] { seq.offset(), -1, -1, -1 });
            attrNames.add(argName);
            attrs.put(argName, null);
            ignore = false;
        }

        consume();
        skipWhitespace();
        t = nextToken();
        if (t == null || t.id() != XMLTokenId.OPERATOR) {
            markUnexpectedAttrToken();
            return false;
        }
        consume();
        skipWhitespace();
        t = nextToken();
        if (t == null ||t.id() != XMLTokenId.VALUE) {
            markUnexpectedAttrToken();
            return false;
        }
        consume();
        
        CharSequence s = t.text();
        StringBuilder sb = null;
        
        int valStart = seq.offset();
        int valEnd = seq.offset() + t.length();
        char quote = s.charAt(0);
        int end;
        
        t = nextToken();
        while (t != null && (t.id() == XMLTokenId.VALUE || t.id() == XMLTokenId.CHARACTER)) {
            valEnd = seq.offset() + t.length();
            if (sb == null) {
                sb = new StringBuilder();
                sb.append(s.toString());
            }
            sb.append(t.text());
            consume();
            t = nextToken();
        }
        end = valEnd;
        if (sb != null) {
            s = sb;
        }
        if (quote == '\'' || quote == '"') { // NOI18N
            if (s.charAt(s.length() - 1) == quote) {
                valStart++;
                // also handle opening quote with no content (yet)
                if (s.length() > 1) {
                    valEnd--;
                    s = s.subSequence(1, s.length() - 1);
                } else {
                    s = ""; // NOI18N
                }
            } else if (t == null || t.id() == XMLTokenId.ERROR) {
                // strip at least 1st quote in case of an error
                s = s.subSequence(1, s.length());
                valStart++;
            }
        } 
        if (!ignore) {
            attrs.put(argName, s.toString());
            int[] offsets = attrOffsets.get(argName);
            offsets[OFFSET_END] = end;
            offsets[OFFSET_VALUE_START] = valStart;
            offsets[OFFSET_VALUE_END] = valEnd;
        }
        return true;
    }
    
    private void processTagName(String tagName) {
        String [] qn = parseQName(tagName);
        this.tagUri = prefix2Uri.get(qn[0]);
        this.tagName = qn[1];
    }
    
    /**
     * Handles stat tag that appears in a tagname. Suppose the user did not yet type
     * the closing brace (>). Report tag open with attributes.
     */
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_tagNotTerminated=Open tag is not terminated: {0}"
    })
    private void errorStartTagInTagName() throws SAXException {
        // remove the last error, it was surely some unexpected token:
        if (!errors.isEmpty()) {
            ErrorMark em = errors.get(errors.size() - 1);
            if (em.getErrorType().equals(ERR_UnexpectedToken)) {
                errors.remove(errors.size() - 1);
            }
        }
        addError(ERR_TagNotFinished, ERR_tagNotTerminated(this.qName), this.qName);
    }
    
    /**
     * Makes only sense within tag processing
     */
    private boolean selfClosed;
    
    @NbBundle.Messages({
        "# {0} - token text",
        "ERR_unexpectedToken=Unexpected token: \"{0}\"",
        "ERR_UnexpectedEndOfFile=Unexpected end of file",
        "TOKEN_newline=< new line >",
        "TOKEN_tab=< tab >"
    })
    private void markUnexpectedToken() {
        String s;
        if (currentToken != null) {
            s = currentToken.text().toString();
            s = s.replaceAll("\\\n", TOKEN_newline()).replaceAll("\\\t", TOKEN_tab()); // NOI18N
            addError(ERR_UnexpectedToken, ERR_unexpectedToken(s));
        } else {
            addError(ERR_UnexpectedToken, ERR_UnexpectedEndOfFile());
        }
    }
    
    private void parseTag(Token<XMLTokenId> t) throws SAXException {
        String tagMark = t.text().toString();
        
        selfClosed = false;
        
        // shorten the tagName so it does not contain the <
        tagMark = tagMark.substring(1);
        if (tagMark.endsWith(SELF_CLOSING_TAG_CLOSE)) {
            selfClosed = true;
            tagMark = tagMark.substring(0, tagMark.length() - 2);
        }
        consume();
        qName = tagMark;

        startLevel();
        
        boolean inError = false;

        out:
        while ((t = nextToken()) != null) {
            XMLTokenId id = t.id();
            
            switch (id) {
                case WS:
                    consume();
                    break;
                case ARGUMENT:
                    inError = !parseAttribute(t);
                    break;
                    
                case TAG: {
                    // expect end tag
                    CharSequence cs = t.text();
                    if (cs.charAt(0) == TAG_START_CHAR) {
                        // some error - bail out
                        inError = true;
                        errorStartTagInTagName();
                        // report tag start as usual
                        break out;
                    }
                    if (cs.charAt(cs.length() - 1) != TAG_END_CHAR) {
                        // does not begin with tag start, does not end with tag end - how is it
                        // possible it is a tag ?
                        throw new IllegalStateException("Invalid tag text: " + cs); // NOI18N
                    } else if (cs.charAt(0) == CLOSE_TAG_CHAR) {
                        selfClosed = true;
                    }
                    consume();
                    break out;
                }
                    
                case ERROR:
                    markUnexpectedToken();
                    consume();
                    // try to recover
                    inError = true;
                    break;
                    
                default:
                    break out;
            }
        }
        
        boolean close = selfClosed;
        
        if (inError) {
            close |= determineClosedTag();
        }
        
        handleStartElement(close);
    }
    
    /**
     * Called when opening tag is not closed, to find & determine whether
     * there's a matching close tag later in the document
     * 
     * @return 
     */
    private boolean determineClosedTag() throws SAXException {
        int markSequence = seq.offset();
        
        XmlLexerParser parser = duplicate();
        
        ParentCollector pm = new ParentCollector(levelStack.size());
        parser.setContentHandler(pm);
        
        try {
            parser.parse2();
        } catch (StopParseException ex) {
            // expected
        } finally {
            seq.move(markSequence);
            seq.moveNext();
        }
        if (levelStack.isEmpty() || pm.closingTags.isEmpty()) {
            return false;
        }
        Level l = levelStack.peek();
        return !pm.closingTags.peekLast().equals(l.tagQName);
    }
    
    private void handleStartElement(boolean selfClosed) throws SAXException {
        processPrefixMappings();
        processTagName(qName);
        
        contentHandler.startElement(tagUri, tagName, qName, 
                new Attrs(attrNames, attrs, prefix2Uri));
        flush();
        if (selfClosed) {
            contentHandler.endElement(tagUri, tagName, qName);
            terminateLevel();
        }
    }
    
    @Override
    public int getElementOffset() {
        return elementOffset;
    }

    @Override
    public int[] getAttributeOffsets(String attribute) {
        if (attribute == ATTRIBUTE_TARGET) {
            return targetOffset;
        } else if (attribute == ATTRIBUTE_DATA) {
            return dataOffset;
        }
        if (attrOffsets == null) {
            return null;
        }
        return attrOffsets.get(attribute);
    }

    @Override
    public Collection<ErrorMark> getErrors() {
        errorsFetched = errors.size();
        return errors.isEmpty() ? errors : Collections.unmodifiableList(errors);
    }

    /**
     * Token offsets
     */
    private List<Integer>           tokenOffsets = Collections.emptyList();
    
    /**
     * Tokens matched for the currently reported tag.
     */
    private List<Token<XMLTokenId>> matchedTokens = Collections.emptyList();

    @Override
    public List<Token<XMLTokenId>> getMatchingTokens() {
        return matchedTokens.isEmpty() ? 
                Collections.<Token<XMLTokenId>>emptyList() : 
                Collections.unmodifiableList(matchedTokens);
    }

    private static final class Attrs implements Attributes {
        private List<String>            order;
        private Map<String, String>     attributes;
        private Map<String, String>     prefi2URI;

        public Attrs(List<String> order, Map<String, String> attributes, Map<String, String> prefi2URI) {
            assert order.size() == attributes.size();
            this.order = order;
            this.attributes = attributes;
            this.prefi2URI = prefi2URI;
        }
        
        
        
        @Override
        public int getLength() {
            return order.size();
        }
        
        private String n(int index) {
            return order.get(index);
        }

        @Override
        public String getURI(int index) {
            String n = n(index);
            int i = n.indexOf(PREFIX_DELIMITER_CHAR);
            if (i == -1) {
                return null;
            }
            return prefi2URI.get(n.substring(0, i));
        }

        @Override
        public String getLocalName(int index) {
            String n = n(index);
            int i = n.indexOf(PREFIX_DELIMITER_CHAR);
            if (i == -1) {
                return n;
            }
            return n.substring(i + 1);
        }

        @Override
        public String getQName(int index) {
            return n(index);
        }

        @Override
        public String getType(int index) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getValue(int index) {
            return attributes.get(n(index));
        }

        @Override
        public int getIndex(String uri, String localName) {
            return order.indexOf(qn(uri, localName));
        }

        @Override
        public int getIndex(String qName) {
            return order.indexOf(qName);
        }

        @Override
        public String getType(String uri, String localName) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getType(String qName) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getValue(String uri, String localName) {
            return attributes.get(qn(uri, localName));
        }

        @Override
        public String getValue(String qName) {
            return attributes.get(qName);
        }
        
        private String qn(String uri, String localName) {
            String qn = localName;
            String prefix = uri2prefix(uri);
            if (prefix != null) {
                qn = prefix + PREFIX_DELIMITER_CHAR + localName;
            }
            return qn;
        }
        
        private String uri2prefix(String uri) {
            for (Map.Entry<String, String> en : prefi2URI.entrySet()) {
                if (uri.equals(en.getValue())) {
                    return en.getKey();
                }
            }
            return null;
        }
    
    }
}
