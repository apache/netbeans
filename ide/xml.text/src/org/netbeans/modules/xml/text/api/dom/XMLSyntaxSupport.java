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

package org.netbeans.modules.xml.text.api.dom;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import static org.netbeans.api.xml.lexer.XMLTokenId.ARGUMENT;
import static org.netbeans.api.xml.lexer.XMLTokenId.OPERATOR;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.dom.BaseSyntaxElement;
import org.netbeans.modules.xml.text.dom.CDATASection;
import org.netbeans.modules.xml.text.dom.Comment;
import org.netbeans.modules.xml.text.dom.DocumentType;
import org.netbeans.modules.xml.text.dom.EmptyTag;
import org.netbeans.modules.xml.text.dom.EndTag;
import org.netbeans.modules.xml.text.dom.ProcessingInstruction;
import org.netbeans.modules.xml.text.dom.StartTag;
import org.netbeans.modules.xml.text.dom.TextImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Creates higher level syntax elements (DOM nodes) above token chain from XML lexer.
 * The support class provides API to access lexical tokens around a particular location
 * and build a navigable DOM-like structure for the text over the lexer output so the
 * clients may read attributes, values and traverse the document. The structure may
 * be built incrementally on demand so if the client traverses or works with the entire
 * structure, the document should be read-locked; otherwise the element starts/ends may get
 * screwed by concurrent document mutations.
 * <p/>
 * The XMLSyntaxSupport creates DOM node implementations based on the lexer tokens. The entire
 * structure is coupled together but its entry points are only weakly referenced; once the caller
 * looses all references to SyntaxElements and XMLSyntaxSupport, the entire structure may
 * be collected.
 * <p/>
 * SyntaxElements are created on-demand and incrementally. 
 * It's not guaranteed that {@link #getElementChain} returns the same instance 
 * for the same offset if called multiple times. Also when document is traversed starting from
 * different SyntaxElements, the same offset/place in the document may be represented by 
 * different SyntaxElement instances in both traversals. Use {@link SyntaxElement#getElementOffset} to
 * check if the underlying location is the same.
 * <p/>
 * In order to traverse through lexical {@link Token}s, the client may call {@link #runWithSequence(int, org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable)}
 * and get access to the {@link TokenSequence} of XML tokens. User code executes with document
 * read-locked and all calls from user code to {@link #getPreviousToken(int)}, {@link #getNextToken(int)} will use that
 * same sequence, so the sequence can be queried for current offset or repositioned.
 * 
 * @author  Samaresh Panda
 * @author Svatopluk Dedic
 * @since 1.60
 */
public final class XMLSyntaxSupport {
    
    /** Holds last character user have typed. */
    private final DocumentMonitor documentMonitor;
    private final BaseDocument document;
    private static final Map<BaseDocument, Reference<XMLSyntaxSupport>> supportMap =
            new WeakHashMap<>();
    
    /** Creates new XMLSyntaxSupport */
    private XMLSyntaxSupport(BaseDocument doc) {
        this.document = doc;
        documentMonitor = createDocumentMonitor();
    }
    
    private DocumentMonitor createDocumentMonitor() {
        synchronized (document) {
            Object o = document.getProperty(DocumentMonitor.class);
            if (o != null) {
                return (DocumentMonitor)o;
            }
            DocumentMonitor m = new DocumentMonitor();
            document.addDocumentListener(m);
            document.putProperty(DocumentMonitor.class, m);
            return m;
        }        
    }
    
    /**
     * Creates a new instance for the given document. The instance will not be
     * registered anywhere; the caller is responsible for bookkeeping. The method
     * can return null if the document implementation is not appropriate (does
     * not offer appropriate API/services). NB editor documents are guaranteed
     * to work with this support.
     * 
     * @param d document
     * @return XML support instance, or {@code null} for incompatible documents.
     */
    @CheckForNull
    public static XMLSyntaxSupport createSyntaxSupport(Document d) {
        if (d == null) {
            throw new NullPointerException("Document may not be null");
        }
        if (!(d instanceof BaseDocument)) {
            return null;
        }
        BaseDocument doc = (BaseDocument)d;
        return new XMLSyntaxSupport(doc);
    }

    /**
     * Obtains XML Syntax support for the document. The instance may be shared
     * with different callers working with the same Document instance. May return
     * {@code null} for an incompatible Document; NB Editor documents are guaranteed
     * to work.
     * 
     * @param d underlying document
     * @return syntax support
     */
    @CheckForNull
    public static XMLSyntaxSupport getSyntaxSupport(Document d) {
        if (d == null) {
            throw new NullPointerException("Document may not be null");
        }
        if (!(d instanceof BaseDocument)) {
            return null;
        }
        BaseDocument doc = (BaseDocument)d;
        XMLSyntaxSupport support = null;
        Reference<XMLSyntaxSupport> refSupport = supportMap.get(doc);
        if (refSupport != null) {
            support = refSupport.get();
        }
        if(support != null)
            return support;

        support = new XMLSyntaxSupport(doc);
        supportMap.put(doc, new WeakReference<>(support));
        return support;
    }

    /**
     * @return underlying Document instance
     */
    @NonNull
    public LineDocument getDocument() {
        return document;
    }
    
    /**
     * Run the given operation on a read-locked document. 
     * @param <T> return type
     * @param userCode code to execute under the lock
     * @return result of user code.
     * @throws BadLocationException propagated from the user code
     * @throws IllegalStateException if a checked exception occurs in user code
     */
    @NullUnknown
    public <T> T runLocked(Callable<T> userCode) throws BadLocationException {
        try {
            ((AbstractDocument)document).readLock();
            return userCode.call();
        } catch (BadLocationException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    /**
     * Callback interface for user operation, which runs on lexical token sequence. 
     * If the client needs to iterativaly traverse {@link TokenSequence} of tokens in the document,
     * calling {@link #getNextToken(int)} could be expensive and unreliable, as each call locks/unlocks the
     * document. Clients may use {@link #runWithSequence(org.netbeans.api.lexer.Token, org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable)}
     * to get access to the TokenSequence and work with it under document read lock.
     * 
     * @param <T> result of the user code
     */
    public interface SequenceCallable<T> {
        /**
         * Callback which receives the TokenSequence instance
         * @param sequence initialized TokenSequence
         * @return user-defined value
         * @throws BadLocationException should be thrown if navigation fails; propagated from the {@code runWithSequence}
         */
        public T call(@NonNull TokenSequence sequence) throws BadLocationException;
    }
    
    private ThreadLocal<TokenSequence> cachedSequence = new ThreadLocal<>();
    
    private TokenSequence getSequence() {
        TokenSequence cached = cachedSequence.get();
        if (cached != null) {
            return cached;
        }
        TokenHierarchy th = TokenHierarchy.get(((AbstractDocument)document));
        TokenSequence ts = th.tokenSequence();
        return ts;
    }
    
    /**
     * Executes user code on token sequence from the document.
     * Read-locks the document, obtains {@link TokenSequence} from the Lexer and executes {@code userCode} 
     * passing the initialized sequence. The sequence is moved to the desired offset and the token that contains
     * or starts at that position. The client can move the sequence elsewhere.
     * <p/>
     * If the {@code userCode} calls this {@code SyntaxSupport} methods like {@link #getNextToken(int)}, they will use
     * the <b>same TokenSequence</b> as passed to {@code userCode}. This allows to combine navigation calls from {@link XMLSyntaxSupport}
     * with client's own sequence movements. The TokenSequence instance passed to {@code userCode} can be queried for
     * current token offset after navigation.
     * 
     * @param <T>
     * @param offset offset to position the sequence at
     * @param userCode code to execute
     * @return user-defined value
     * @throws BadLocationException if the user code throws BadLocationException
     * @throws IllegalStateException if the user code throws a checked exception
     */
    @NullUnknown
    public <T> T runWithSequence(int offset, SequenceCallable<T> userCode) throws BadLocationException {
        T result;
        TokenSequence old = null;
        try {
            ((AbstractDocument)document).readLock();
            old = cachedSequence.get();
            cachedSequence.remove();
            TokenSequence ts = getSequence();
            if (ts == null) {
                throw new BadLocationException("No sequence for position", offset); // NOI18N
            }
            cachedSequence.set(ts);
            synchronized (ts) {
                ts.move(offset);
                ts.moveNext();
                result = userCode.call(ts);
            }
        } finally {
            cachedSequence.set(old);
            ((AbstractDocument)document).readUnlock();
        }
        return result;
    }
    
    /**
     * Exeutes user code with {@link TokenSequence} positioned at a particular token.
     * This convenience method works much like {@link #runWithSequence(int, org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable)},
     * except that Token (its starting offset) is used to position the sequence instead of raw offset value. 
     * 
     * @param <T>
     * @param startFrom token to start from
     * @param userCode user code to execute
     * @return user-defined value
     * @throws BadLocationException if the user code throws BadLocationException
     */
    public <T> T runWithSequence(Token<XMLTokenId> startFrom, SequenceCallable<T> userCode) throws BadLocationException {
        T result;
        TokenSequence old = null;
        try {
            ((AbstractDocument)document).readLock();
            old = cachedSequence.get();
            cachedSequence.remove();
            TokenHierarchy th = TokenHierarchy.get(((AbstractDocument)document));
            TokenSequence ts = th.tokenSequence();
            if (ts == null) {
                throw new BadLocationException("No sequence for position", startFrom.offset(null)); // NOI18N
            }
            cachedSequence.set(ts);
            synchronized (ts) {
                ts.move(startFrom.offset(th));
                ts.moveNext();
                result = userCode.call(ts);
            }
        } finally {
            cachedSequence.set(old);
            ((AbstractDocument)document).readUnlock();
        }
        return result;
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document. 
     * <p/>
     * Note: if the call is made within {@link #runWithSequence(int, org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable)} exection scope, the
     * search for previous token will use the same TokenSequence as passed to executed user callback.
     * 
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public Token<XMLTokenId> getPreviousToken( int offset) throws BadLocationException {
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenSequence ts = getSequence();
            synchronized (ts) {
                return getToken(ts, offset, false, null);
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * 
     * It does not lock the document. 
     * <p/>
     * Note: if the call is made within {@link #runWithSequence(int, org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable)} exection scope, the
     * search for previous token will use the same TokenSequence as passed to executed user callback.
     * <p/>
     * This variant returns start + end (after the token) offsets for the caller's convenience.
     * 
     * @param offset valid position in document
     * @param tokenBounds output; will receive token start and end positions
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public Token<XMLTokenId> getPreviousToken(int offset, int[] tokenBounds) throws BadLocationException {
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenSequence ts = getSequence();
            synchronized (ts) {
                return getToken(ts, offset, false, tokenBounds);
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    /**
     * Returns the token occupying the given position. The token either extends over this position, or
     * it starts at that position (first token's char offset == offset).
     * 
     * @param offset offset in text for which the Token should be produced
     * @return XML token occupying the position
     * @throws BadLocationException 
     */
    public Token<XMLTokenId> getTokenAtPosition(int offset, int[] tokenBounds) throws BadLocationException {
        return getNextToken(offset + 1, tokenBounds);
    }

    /**    
     * Retrieves the start of an attribute token. If the passed position does not correspond to
     * attribute value, operator or whitespace (between attribute name and value), {@code null}
     * is returned.
     * <p/>
     * Convenience method.
     * 
     * @param offset starting offset
     * @return token corresponding to the attribute name ({@link XMLTokenId#ARGUMENT}).
     */
    public Token<XMLTokenId> getAttributeToken(int offset) {
        try {
            return this.<Token<XMLTokenId>>runWithSequence(offset, (TokenSequence ts) -> {
                Token<XMLTokenId> currentToken = ts.token();
                if(currentToken.id() != XMLTokenId.VALUE) {
                    return null;
                }
                Token<XMLTokenId> equalsToken = null;
                while (ts.movePrevious()) {
                    Token<XMLTokenId> t = ts.token();
                    if (t.id() == OPERATOR) {
                        equalsToken = t;
                        break;
                    } else if (t.id() == XMLTokenId.ARGUMENT) {
                        return t;
                    } else if (t.id() != XMLTokenId.WS) {
                        return null;
                    }
                }
                if(equalsToken == null) {
                    return null;
                }
                while (ts.movePrevious()) {
                    Token<XMLTokenId> t = ts.token();
                    if (t.id() == ARGUMENT) {
                        return t;
                    } else if (t.id() != XMLTokenId.WS) {
                        return null;
                    }
                }
                return null;
            });
        } catch (BadLocationException ex) {
        }
        return null;
    }
    
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document. Returns {@code null} for invalid offsets.
     * 
     * @param offset valid position in document
     * @return Token instance
     */
    public Token<XMLTokenId> getNextToken( int offset) throws BadLocationException {
        if (offset == 0) {
            offset = 1;
        }
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenSequence ts = getSequence();
            synchronized (ts) {
                return getToken(ts, offset, true, null);
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document. Returns {@code null} for invalid offsets.
     * <p/>
     * This variant returns start + end (after the token) offsets for the caller's convenience.
     * 
     * @param offset valid position in document
     * @param tokenBounds output; will receive token start and end positions
     * @return Token instance
     */
    public Token getNextToken( int offset, int[] tokenBounds) throws BadLocationException {
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenSequence ts = getSequence();
            synchronized (ts) {
                return getToken(ts, offset, true, tokenBounds);
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    private Token getToken(TokenSequence ts, int offset, boolean next, int[] startOffset) {
        int diff = ts.move(offset);
        boolean ok;
        if(next) {
            ok = ts.moveNext();
        } else if (diff > 0) {
            ok = ts.moveNext();
        } else {
            ok = ts.movePrevious();
        }
        if (!ok) {
            return null;
        }
        if (startOffset != null) {
            startOffset[0] = ts.offset();
            if (startOffset.length > 1) {
                startOffset[1] = ts.offset() + ts.token().length();
            }
        }
        return ts.token();
    }

    
    /**
     * Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just before the offset.
     * @param offset Offset in document where to search for SyntaxElement.
     * @return SyntaxElement Element surrounding or laying BEFORE the offset
     * or <code>null</code> at document begining.
     * @throws javax.swing.text.BadLocationException when offset is invalid or navigation fails
     */
    public SyntaxElement getElementChain(final int offset ) throws BadLocationException {

        ((AbstractDocument)document).readLock();
        try {
            TokenSequence<XMLTokenId> ts = getSequence();
            Token<XMLTokenId> token = initialize(ts, offset);
            if(token == null)
                return null;
            while (token.id() == XMLTokenId.CHARACTER) {
                if (!ts.movePrevious()) {
                    return null;
                }
                token = ts.token();
            }
            switch(token.id()) {
                case PI_START:
                case PI_END:
                case PI_CONTENT:
                case PI_TARGET: {
                    Token<XMLTokenId> first = token;
                    while(token.id() != XMLTokenId.PI_START) {
                        if (!ts.movePrevious()) {
                            break;
                        }
                        token = ts.token();
                    }
                    return createElement(ts, token);
                }

                case CHARACTER:
                    while(token.id() == XMLTokenId.CHARACTER || token.id() == XMLTokenId.TEXT) {
                        if (!ts.movePrevious()) {
                            break;
                        }
                        token = ts.token();
                    }
                    return createElement(ts, token);
                    
                case TEXT:
                case DECLARATION:
                case CDATA_SECTION:
                case BLOCK_COMMENT:
                case TAG:
                case ERROR: {
                    return createElement(ts, token);
                }
            }

        } finally {
            ((AbstractDocument)document).readUnlock();
        }

        return null;
    }

    private Token<XMLTokenId> initialize(TokenSequence ts, int offset) {
        int diff = ts.move(offset);
        Token<XMLTokenId> token = ts.token();
        if (diff > 0) {
            if (!ts.moveNext()) {
                return null;
            }
        } else if (!ts.movePrevious()) {
            return null;
        }
        token = ts.token();
        XMLTokenId id = token.id();
        while (token.id() == XMLTokenId.CHARACTER) {
            if (!ts.movePrevious()) {
                return token;
            }
            token = ts.token();
            id = token.id();
        }
        String image = token.text().toString();
        if ( id == XMLTokenId.WS ||
                id == XMLTokenId.ARGUMENT ||
                id == XMLTokenId.OPERATOR ||
                id == XMLTokenId.VALUE ||
                (id == XMLTokenId.TAG &&
                (">".equals(image) || "/>".equals(image)))) { //NOI18N
            while (ts.movePrevious()) {
                token = ts.token();
                id = token.id();
                if (id == XMLTokenId.TAG || id == XMLTokenId.PI_START || id == XMLTokenId.DECLARATION)
                    break;
            } //while
        } //if
        return token;
    }

    /**
     * Create elements starting with given item.
     *
     * @param  item or null if EOD
     * @return SyntaxElement startting at offset, or null, if EoD
     */
    private SyntaxElement createElement(final TokenSequence ts,
            final Token<XMLTokenId> token) throws BadLocationException {
        //default start and end.
        int start = ts.offset();
        int end = start + token.length();
        switch(token.id()) {
            
            case PI_START: {
                String target = null;
                String content = null;
                Token<XMLTokenId> t = token;
                while(t.id() != XMLTokenId.PI_END) {
                    if(t.id() == XMLTokenId.PI_TARGET)
                        target = t.text().toString();
                    if(t.id() == XMLTokenId.PI_CONTENT)
                        content = t.text().toString();
                    if (!ts.moveNext()) {
                        break;
                    }
                    t = ts.token();
                }
                end = ts.offset() + t.length();
                return new ProcessingInstruction(this, token, start,
                        end, target, content);
            }

            case DECLARATION: {
                Token<XMLTokenId> t = token;
                do {
                    if (t.id() == XMLTokenId.DECLARATION) {
                        if (t.length() == 1 && t.text().charAt(0) == '>') {
                            // we have the end of the declaration
                            end = ts.offset() + t.length();
                            break;
                        }
                    } else if (t.id() != XMLTokenId.VALUE) {
                        // premature end of declaration
                        end = ts.offset();
                        break;
                    }
                    end = ts.offset() + t.length();
                    t  = ts.token();
                } while (ts.moveNext());
                return new DocumentType(this, token, start, end);
            }

            case CDATA_SECTION: {
                return new CDATASection(this, token, start, end);
            }
            
            case BLOCK_COMMENT: {
                return new Comment(this, token, start, end);
            }
            
            case CHARACTER:
            case TEXT: {
                end = ts.offset() + token.length();
                Token<XMLTokenId> tukac = token;
                while (tukac.id() == XMLTokenId.CHARACTER || tukac.id() == XMLTokenId.TEXT) {
                    end = ts.offset() + tukac.length();
                    if (!ts.moveNext()) {
                        break;
                    }
                    tukac = ts.token();
                }
                return new TextImpl(this, token, start, end);
            }

            case TAG: {
                Token<XMLTokenId> t = token;
                do {
                    if (!ts.moveNext()) {
                        break;
                    }
                    t = ts.token();
                } while(t.id() != XMLTokenId.TAG);
                end = ts.offset() + t.length();
                //empty tag
                if(t.text().toString().equals("/>")) {
                    return new EmptyTag(this, token, start, end);
                }
                //end tag
                if(token.text().toString().startsWith("</")) {//NOI18N
                    return new EndTag(this, token, start, end);
                }
                return new StartTag(this, token, start, end);
            }

            case ERROR: {
                return new BaseSyntaxElement.Error(this, token, start, end );
            }
        }

        return null;
    }

    /** 
     * Returns last inserted character. 
     * It's most likely one recently typed by user. Note that in order to start capturing
     * editor events, the XMLSupport must be activated for the document. When XMLSupport is first 
     * created for a Document, it does not provide any lastTypedChar; the subsequent edits are
     * recorded.
     */
    public final char lastTypedChar() {
        return documentMonitor.lastInsertedChar;
    }
        
    /** Keep track of last typed character */
    private static class DocumentMonitor implements DocumentListener {
        
        private char lastInsertedChar = 'X';  // NOI18N
        
        public void changedUpdate(DocumentEvent e) {
        }
        
        public void insertUpdate(DocumentEvent e) {
            int start = e.getOffset();
            int len = e.getLength();
            try {
                String s = e.getDocument().getText(start + len - 1, 1);
                lastInsertedChar = s.charAt(0);
            } catch (BadLocationException e1) {
            }
        }
        
        public void removeUpdate(DocumentEvent e) {
        }
    }
    
    /**
     * Determines if the SyntaxElement is a start or end tag.
     * 
     * @param n element to test
     * @return true, if the element is a start or end tag. False if Node does not represent a tag, or for an empty tag
     */
    public boolean isNormalTag(SyntaxElement n) {
        return isStartTag(n) || isEndTag(n);
    }
    
    /**
     * Determines if the SyntaxElement is a start tag.
     * Returns {@code false} if Node does not represent a start tag, or is a self-closed (empty content) tag
     * @param n element to test
     * @return true, if the element is a start tag.
     */
    public boolean isStartTag(SyntaxElement n) {
        return n instanceof StartTag;
    }
    
    /**
     * Returns true iff the element is a self-closing tag.
     * Returns true if and only if the SyntaxElement represents a self-closing tag without content. False otherwise.
     * @param n element to check
     * @return true, if self-closing tag
     */
    public boolean isEmptyTag(SyntaxElement n) {
        return n instanceof EmptyTag;
    }
    
    /**
     * Determines if the SyntaxElement is an end tag.
     * Returns {@code false} if Node does not represent an end tag, or is a self-closed (empty content) tag
     * @param n element to test
     * @return true, if the element is a start tag.
     */
    public boolean isEndTag(SyntaxElement n) {
        return n instanceof EndTag;
    }
    
    /**
     * Returns text offset of the Node start in the underlying document.
     * Returns -1 if the offset could not be determined. Use this method to find out
     * offset of a DOM Node obtained from a {@link SyntaxElement} or {@link XMLSyntaxSupport}
     * @param n
     * @return offset or -1 if the offset could not be determined.
     */
    public int getNodeOffset(Node n) {
        if (!(n instanceof SyntaxElement)) {
            if (n instanceof Document) {
                return 0;
            }
            return -1;
        } 
        return ((SyntaxElement)n).getElementOffset();
    }
    
    /**
     * Obtains a SyntaxElement for the W3C Node, if possible.
     * @param n the Node
     * @return corresponding SyntaxElement or {@code null}.
     */
    public SyntaxElement getSyntaxElement(Node n) {
        if (n instanceof SyntaxElement) {
            return (SyntaxElement)n;
        } else {
            return null;
        }
    }
    
    /**
     * Convenience method to get attribute value as string. Returns {@code null}
     * if the Node is not a start/empty element, or does not contain attribute of the specified name.
     * For namespaced names, you must use the exact {@code prefix:localName}.
     * 
     * @param n node
     * @param name attribute name
     * @return attribute (string) value or {@code null} if no such attribute exist for element n.
     */
    public static String getAttributeValue(Node n, String name) {
        NamedNodeMap a = n.getAttributes();
        if (a == null) {
            return null;
        }
        Node item = a.getNamedItem(name);
        if (item == null) {
            return null;
        }
        return item.getNodeValue();
    }

    /**
     * Constructs a path from the root of the document to the given syntax element.
     * 
     * @param element the element to start with
     * @return top-down path of SyntaxElements from the document root towards the original SyntaxElement
     */
    public List<SyntaxElement> getPathFromRoot(SyntaxElement element) {
        Deque<SyntaxElement> stack = new ArrayDeque<>();
        SyntaxElement elementRef = element;
        while (elementRef != null) {
            if (isEndTag(element) ||
                    (isEmptyTag(elementRef) && stack.isEmpty()) ||
                    (isStartTag(elementRef) && stack.isEmpty())) {
                stack.push(elementRef);
                elementRef = elementRef.getPrevious();
                continue;
            }
            if (isStartTag(elementRef)) {
                if (isEndTag(stack.peek())) {
                    SyntaxElement end = stack.peek();
                    if (end.getNode().getNodeName().equals(elementRef.getNode().getNodeName())) {
                        stack.pop();
                    }
                } else {
                    SyntaxElement e = stack.peek();
                    stack.push(elementRef);
                }
            }
            elementRef = elementRef.getPrevious();
        }
        // reverse:
        List<SyntaxElement> res = new ArrayList<>(stack.size());
        while ((elementRef = stack.poll()) != null) {
            res.add(elementRef);
        }
        return res;
    }
    
    /**
     * Skips forward or backward specified token types. Simplified variant of {@link #skip(int, boolean, java.util.function.BiPredicate)},
     * only token types to skip can be specified. The first token of type other than those passed in {@code skipTokens} will be returned from the method.
     * 
     * @param offset position to start at.
     * @param forward true means froward, false backward
     * @param skipTokens token types to skip
     * @return first token whose type does not match the specified values
     * @throws BadLocationException 
     */
    public Token<XMLTokenId> skip(int offset, boolean forward, XMLTokenId... skipTokens) throws BadLocationException {
        EnumSet<XMLTokenId> en = EnumSet.copyOf(Arrays.asList(skipTokens));
        return skip(offset, forward, (TokenSequence s, Token<XMLTokenId> t) -> en.contains(t.id()));
    }
    
    /**
     * Skips tokens matched by the predicate from the given offset. Positions {@link TokenSequence} on the specified offset,
     * then traverses either forward or backward, depending on a parameter until the predicate returns false. 
     * The token for which the predicate failed will be returned as the return value.
     * 
     * @param offset offset to start traversal
     * @param forward true for forward traversal, false for backward
     * @param pred predicate to match tokens. Method skips tokens that satisfy the predicate (until the first which does not)
     * @return
     * @throws BadLocationException 
     */
    public Token<XMLTokenId>  skip(int offset, boolean forward, BiPredicate<TokenSequence, Token<XMLTokenId>> pred) throws BadLocationException {
        Token<XMLTokenId> tukac = runWithSequence(offset, (TokenSequence s) -> {
            s.move(offset);
            while ((forward && s.moveNext()) || (!forward && s.movePrevious())) {
                if (!pred.test(s, s.token())) {
                    return s.token();
                }
            }
            return null;
        });
        return tukac;
    }
        
}

