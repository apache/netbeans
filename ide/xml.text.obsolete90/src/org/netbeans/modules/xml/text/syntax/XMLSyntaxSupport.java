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

package org.netbeans.modules.xml.text.syntax;

import java.util.*;

import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;

/**
 * Creates higher level syntax elements (DOM nodes) above token chain.
 * <p>
 * It also defines rules for auto code completion poping up (Editor architecture issue).
 *
 * @author  Petr Nejedly - original HTML code
 * @author  Sandeep S. Randhawa - XML port
 * @author  Petr Kuzel - use before strategy, use tokens whenever possible
 * @version 0.8
 */
public final class XMLSyntaxSupport extends ExtSyntaxSupport implements XMLTokenIDs {
    
    private String systemId = null;  // cached refernce to DTD
    private String publicId = null;  // cached refernce to DTD
    private volatile boolean requestedAutoCompletion = false;
    
    /** Holds last character user have typed. */
    private char lastInsertedChar = 'X';  // NOI18N
    
    private final DocumentMonitor documentMonitor;
    
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";
    
    /** Creates new XMLSyntaxSupport */
    public XMLSyntaxSupport(BaseDocument doc) {
        super(doc);
        
        // listener has same lifetime as this class
        documentMonitor = new DocumentMonitor();
        DocumentListener l = WeakListeners.document(documentMonitor, doc);
        doc.addDocumentListener(l);
        
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document.
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public TokenItem getPreviousToken( int offset) throws BadLocationException {
        
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " + offset + " cannot be less than 0.", offset);  //NOI18N
        
        // find first token item at the offset
        
        TokenItem item = null;
        int step = 11;
        int len = getDocument().getLength();  //??? read lock
        if (offset > len) throw new BadLocationException("Offset " + offset + " cannot be higher that document length " + len + " .", offset );  //NOI18N
        int from = Math.min(len, offset);
        int to = Math.min(len, offset);
        
        // go ahead to document beginning
        
        while ( item == null) {
            from = Math.max( from - step, 0);
            if ( from == 0) {
                to = Math.min(to + step, len);
            }
            item = getTokenChain( from, to);
            if ( from == 0 && to == len && item == null) {
                throw new IllegalStateException("Token at " + offset + " cannot be located!\nInspected range:[" + from + ", " + to + "].");  //NOI18N
            }
        }
        
        // if we are are at token boundary or at the fist document tokem all is OK
        // otherwise the offset actually resides in some next token
        
        while (item.getOffset() + item.getImage().length() < offset) {  // it must cross or touch it
            TokenItem next = item.getNext();
            if (next == null) {
                if (item.getOffset() + item.getImage().length() >= len) {
                    return item;  // we are at boundary at the end of document
                } else {
                    throw new IllegalStateException("Token at " + offset + " cannot be located!\nPrevious token: " + item);  //NOI18N
                }
            }
            item = next;
        }
        
        return item;
    }
    
    /**
     * Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just before the offset.
     * @param offset Offset in document where to search for SyntaxElement.
     * @return SyntaxElement Element surrounding or laying BEFORE the offset
     * or <code>null</code> at document begining.
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        
        TokenItem item = getPreviousToken( offset);
        if (item == null) return null;
        
        // locate SyntaxElement start boundary by traversing previous tokens
        // then create element starting from that boundary
        
        TokenID id = item.getTokenID();
        TokenItem first = item;
        
        // reference can be in attribute or in content
        
        if( id == CHARACTER ) {
            while( id == CHARACTER ) {
                item = item.getPrevious();
                if (item == null) break;
                id = item.getTokenID();
                first = item;
            }
            
            // #62654 incorrect syntax element create for reference when it is right after a tag ( <atag>&ref;... )
            if(id == XMLDefaultTokenContext.TAG && item.getImage().endsWith(">")) {
                return createElement(item.getNext());
            }
            
            // now item is either XMLSyntax.VALUE or we're in text, or at BOF
            if( id != VALUE && id != TEXT && id != CDATA_SECTION ) {
                // #34453 it may start of element tag or end of start tag (skip attributtes)
                if( id == XMLDefaultTokenContext.TAG ) {
                    if( item.getImage().startsWith( "<" ) ) {
                        return createElement( item );  // TAGO/ETAGO
                    } else {
                        do {
                            item = item.getPrevious();
                            id = item.getTokenID();
                        } while( id != XMLDefaultTokenContext.TAG );
                        return createElement( item );       // TAGC
                    }
                }
                return createElement( first );
            } // else ( for VALUE or TEXT ) fall through
            
        }
        
        // these are possible only in containers (tags or doctype)
        if ( id == XMLDefaultTokenContext.WS
                || id == XMLDefaultTokenContext.ARGUMENT
                || id == XMLDefaultTokenContext.OPERATOR
                || id == XMLDefaultTokenContext.VALUE)  // or doctype
        {
            while (true) {
                item = item.getPrevious();
                id = item.getTokenID();
                if (id == XMLDefaultTokenContext.TAG) break;
                if (id == XMLDefaultTokenContext.DECLARATION
                        && item.getImage().trim().length() > 0) break;
                if (isInPI(id, false)) break;
            };
        }
        
        if( id == TEXT) {
            
            while( id == TEXT || id == CHARACTER ) {
                first = item;
                item = item.getPrevious();
                if (item == null)  break;
                id = item.getTokenID();
            }
            return createElement( first ); // from start of continuous text
        }
        
        if( id == CDATA_SECTION) {
            //the entire CDATA section is a one big fat token :-)
            return createElement( item );
        }
        
        //
        // it may start of element tag or end of start tag (skip attributtes)
        //
        if( id == XMLDefaultTokenContext.TAG ) {
            if( item.getImage().startsWith( "<" ) ) {
                return createElement( item );  // TAGO/ETAGO
            } else {
                do {
                    item = item.getPrevious();
                    id = item.getTokenID();
                } while( id != XMLDefaultTokenContext.TAG );
                return createElement( item );       // TAGC
            }
        }
        
        if( id == XMLDefaultTokenContext.ERROR )
            return new SyntaxElement.Error( this, item, getTokenEnd( item ) );
        
        if( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
            while( id == XMLDefaultTokenContext.BLOCK_COMMENT && !item.getImage().startsWith( "<!--" ) ) { // NOI18N
                first = item;
                item = item.getPrevious();
                id = item.getTokenID();
            }
            return createElement( first ); // from start of Commment
        }
        
        
        if ( id == XMLDefaultTokenContext.DECLARATION ) {
            while(true) {
                first = item;
                if (id == XMLDefaultTokenContext.DECLARATION
                        && item.getImage().startsWith("<!"))                          // NOI18N
                {
                    break;
                }
                item = item.getPrevious();
                if (item == null) break;
                id = item.getTokenID();
            }
            return createElement( first );
        }
        
        // PI detection
        
        if (isInPI(id, false)) {
            do {
                item = item.getPrevious();
                id = item.getTokenID();
            } while (id != XMLDefaultTokenContext.PI_START);
        }
        
        if (id == XMLDefaultTokenContext.PI_START) {
            return createElement(item);
        }
        
        return null;
    }
    
    // return if in PI exluding PI_START and including WSes
    private boolean isInPI(TokenID id, boolean includeWS) {
        return id == XMLDefaultTokenContext.PI_TARGET
                || id == XMLDefaultTokenContext.PI_CONTENT
                || id == XMLDefaultTokenContext.PI_END
                || (includeWS && id == XMLDefaultTokenContext.WS);
    }
    
    /**
     * Create elements starting with given item.
     *
     * @param  item or null if EOD
     * @return SyntaxElement startting at offset, or null, if EoD
     */
    public SyntaxElement createElement( TokenItem item ) throws BadLocationException {
        
        if( item == null ) return null; // on End of Document
        
//        System.err.println("Creating element for: "  + item.getTokenID().getName() + " " + item.getImage());
        
        TokenID id = item.getTokenID();
        TokenItem first = item;
        int lastOffset = getTokenEnd( item );
        switch (id.getNumericID()) {
            
            case XMLDefaultTokenContext.BLOCK_COMMENT_ID:
                
                while( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                }
                return new CommentImpl( this, first, lastOffset );
                
            case XMLDefaultTokenContext.DECLARATION_ID:
                
                // we treat internal DTD as one syntax element
                boolean seekforDTDEnd = false;;
                while( id == XMLDefaultTokenContext.DECLARATION
                        || id == XMLDefaultTokenContext.VALUE
                        || seekforDTDEnd) {
                    lastOffset = getTokenEnd( item );
                    if (seekforDTDEnd) {
                        if (item.getImage().endsWith("]>")) {
                            break;
                        }
                    } else if (id == DECLARATION) {
                        seekforDTDEnd = item.getImage().endsWith("[");
                    }
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                }
                return new DocumentTypeImpl( this, first, lastOffset);
                
            case XMLDefaultTokenContext.ERROR_ID:
                
                return new SyntaxElement.Error( this, first, lastOffset);
                
            case TEXT_ID:
            case CHARACTER_ID:
                
                while( id == TEXT || id == CHARACTER || id == CDATA_SECTION) {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                }
                return new TextImpl( this, first, lastOffset );
                
            case CDATA_SECTION_ID:
                return new CDATASectionImpl( this, first, first.getOffset() + first.getImage().length() );
                
            case XMLDefaultTokenContext.TAG_ID:
                
                String text = item.getImage();
                if ( text.startsWith( "</" ) ) {                 // endtag      // NOI18N
                    String name = text.substring( 2 );
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();
                    
                    while( id == XMLDefaultTokenContext.WS ) {
                        lastOffset = getTokenEnd( item );
                        item = item.getNext();
                        id = item == null ? null : item.getTokenID();
                    }
                    
                    if( id == XMLDefaultTokenContext.TAG && item.getImage().equals( ">" ) ) {   // with this tag
                        return new EndTag( this, first, getTokenEnd( item ), name );
                    } else {                                                            // without this tag
                        return new EndTag( this, first, lastOffset, name );
                    }
                } else {                                                                // starttag
                    String name = text.substring( 1 );
                    ArrayList attrs = new ArrayList();
                    
                    // skip attributes
                    
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();
                    
                    while( id == XMLDefaultTokenContext.WS
                            || id == XMLDefaultTokenContext.ARGUMENT
                            || id == XMLDefaultTokenContext.OPERATOR
                            || id == XMLDefaultTokenContext.VALUE
                            || id == XMLDefaultTokenContext.CHARACTER) {
                        if ( id == XMLDefaultTokenContext.ARGUMENT ) {
                            attrs.add( item.getImage() );  // remember all attributes
                        }
                        lastOffset = getTokenEnd( item );
                        item = item.getNext();
                        if (item == null) break;
                        id = item.getTokenID();
                    }
                    
                    // empty or start tag handling
                    
                    if( id  == XMLDefaultTokenContext.TAG && (item.getImage().equals( "/>") || item.getImage().equals(">") || item.getImage().equals("?>"))){
                        if(item.getImage().equals("/>"))
                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
                        else if(item.getImage().equals("?>"))
                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
                        else
                            return new StartTag( this, first, getTokenEnd( item ), name, attrs );
                    } else {                                                            // without this tag
                        return new StartTag( this, first, lastOffset, name, attrs );
                    }
                }
                
            case XMLDefaultTokenContext.PI_START_ID:
                do {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                } while( isInPI(id, true));
                return new ProcessingInstructionImpl( this, first, lastOffset);
                
            default:
                // BadLocationException
        }
        
        throw new BadLocationException( "Cannot create SyntaxElement at " + item, item.getOffset() );  //NOI18N
    }
    
    // ~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Locate DOCTYPE from the start of document.
     */
//    public SyntaxElement.Declaration getDeclarationElement(){
//        int offset = 5;
//        SyntaxElement elem = null;
//
//        try {
//            while(true){  //??? optimalize stop on first element
//                elem = getElementChain(offset);
//                if(elem instanceof SyntaxElement.Declaration || elem == null)
//                    break;
//                offset += elem.getElementLength()+1;
//            }
//        } catch (BadLocationException ble) {
//            org.openide.TopManager.getDefault().notifyException(ble);
//        }
//        return elem != null ? (SyntaxElement.Declaration)elem : null;
//    }
    
    
    /**
     * Look for pairing closing tag.
     *
     * @param offset where to start the search
     * @return name of pairing start tag
     */
    public String getEndTag(int offset) throws BadLocationException {
        SyntaxElement elem = getElementChain( offset );
        
        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return "";
            }
        }
        
        int counter = 0;
        for( ; elem != null; elem = elem.getPrevious() ) {
            //EMPTY TAG MUST MUST come before Tagcuz it extends from Tag
            if(elem instanceof EmptyTag)
                continue;
            else if(elem instanceof StartTag )
                counter++;
            else if(elem instanceof EndTag)
                counter--;
            else
                continue;
            
            if(counter == 1 ){
                String name = ((StartTag)elem).getTagName();
                return name;
            }
        }
        return "";
    }
    
    
    /**
     * @param  offset of a child in parent
     * @return all tags used as children of given parent that precedes the offset
     */
    public List getPreviousLevelTags( int offset) throws BadLocationException {
        List result = new ArrayList();
        Stack stack = new Stack();
        Vector children = new Vector();
        
        SyntaxElement elem = getElementChain( offset );
        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return result;
            }
        }
        
        for( ; elem != null; elem = elem.getPrevious() ) {
            if( elem instanceof EndTag )
                stack.push( ((EndTag)elem).getTagName() );
            else if( elem instanceof EmptyTag ) {
                if(stack.size()==0)
                    //here we r a child of level root element so add him
                    children.add(((EmptyTag)elem).getTagName() );
                continue;
            }else if( elem instanceof Tag ) {
                String name = ((Tag)elem).getTagName();
                
                //                if(name.equals(prefix))
                //                    continue;
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    result.add(name);
                    
                    for(int k=children.size();k>0;k--){
                        result.add(children.get(k-1));
                    }
                    
                    return result;
                } else                         // not empty - we match content of stack
                    if( stack.peek().equals( name ) ) { // match - close this branch of document tree
                    
                    if(stack.size()==1)
                        //we need this name to add to our list of tags before the point of insertion
                        //we r at depth 1
                        children.add(name);
                    
                    stack.pop();
                    }
            }
        }
        result.clear();
        return result;
    }
    
    /**
     * @param  offset of a child in parent
     * @return all tags used as children of given parent that follows the offset
     */
    public List getFollowingLevelTags(int offset)throws BadLocationException{
        Stack stack = new Stack();
        Vector children = new Vector();
        
        SyntaxElement elem = getElementChain( offset );
        if( elem != null ) {
            elem = elem.getNext();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return new ArrayList();
            }
        }
        
        for( ; elem != null; elem = elem.getNext() ) {
            if( elem instanceof EmptyTag ) {
                if(stack.size()==0)
                    //here we r a child of level root element so add him
                    children.add(((EmptyTag)elem).getTagName() );
                continue;
            }else if( elem instanceof Tag ) {
                stack.push( ((Tag)elem).getTagName() );
            }else if( elem instanceof EndTag ){
                String name = ((EndTag)elem).getTagName();
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes and can return the children now
                    return children;
                } else if( stack.peek().equals( name ) ) { // not empty - we match content of stack
                    // match - close this branch of document tree
                    if(stack.size()==1)
                        //we need this name to add to our list of tags before the point of insertion
                        //we r at depth 1
                        children.add(name);
                    
                    stack.pop();
                }
            }
        }
        children.clear();
        return children;
    }
    
    
    /**
     * Defines <b>auto-completion</b> popup trigering criteria.
     * @param typedText single last typed char
     *
     */
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        
        requestedAutoCompletion = false;
        
        if( !visible ) {
            int retVal = COMPLETION_CANCEL;
            switch( typedText.charAt( typedText.length()-1 ) ) {
                case '/':
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    if (dotPos >= 2) { // last char before inserted slash
                        try {
                            String txtBeforeSpace = doc.getText(dotPos-2, 2);
                            if( txtBeforeSpace.equals("</") )  // NOI18N
                                retVal = COMPLETION_POPUP;
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                    break;
                    
                case '<':
                case '&':
                case '"':
                case '\'':
                    retVal = COMPLETION_POPUP;
                    break;
                case '>':
                    dotPos = target.getCaret().getDot();
                    try {
                        SyntaxElement sel = getElementChain(dotPos);
                        if(sel != null && sel instanceof StartTag) {
                            retVal = COMPLETION_POPUP;
                        }
                    } catch (BadLocationException e) {
                        //ignore
                    }
                    break;
            }
            if(noCompletion(target))
                return COMPLETION_HIDE;            
            if (retVal == COMPLETION_POPUP) requestedAutoCompletion = true;
            return retVal;
        } else { // the pane is already visible
            switch (typedText.charAt(0)) {
                case '>':
                case ';':
                    return COMPLETION_HIDE;
            }
            //requestedAutoCompletion = true;
            return COMPLETION_POST_REFRESH; //requery it
        }
    }
    
    /**
     * No completion inside PI, CDATA, comment section.
     * True only inside PI or CDATA section, false otherwise.
     * @param target
     */
    public boolean noCompletion(JTextComponent target) {
        if(target == null || target.getCaret() == null)
            return false;
        int offset = target.getCaret().getDot();
        if(offset < 0)
            return false;            
        //no completion inside CDATA or comment section
        BaseDocument document = (BaseDocument)target.getDocument();
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            if(ts == null)
                return false;
            ts.move(offset);
            Token token = ts.token();
            if(token == null) {
                ts.moveNext();
                token = ts.token();
                if(token == null)
                    return false;
            }
            if( token.id() == XMLTokenId.CDATA_SECTION ||
               token.id() == XMLTokenId.BLOCK_COMMENT ||
               token.id() == XMLTokenId.PI_START ||
               token.id() == XMLTokenId.PI_END ||
               token.id() == XMLTokenId.PI_CONTENT ||
               token.id() == XMLTokenId.PI_TARGET ) {
               return true;
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        
        return false;
    }
    
    /**
     * Return true is this syntax requested auto completion.
     * XMLCompletionQuery can utilize it to not show needless 'No suggestion.'.
     */
    public boolean requestedAutoCompletion() {
        return requestedAutoCompletion;
    }
    
    /**
     * @return end offset of given item
     */
    static int getTokenEnd( TokenItem item ) {
        return item.getOffset() + item.getImage().length();
    }
    
    /** Returns last inserted character. It's most likely one recently typed by user. */
    public final char lastTypedChar() {
        return lastInsertedChar;
    }
    
    /** Find matching tags with the current position.
     * @param offset position of the starting tag
     * @param simple whether the search should skip comment and possibly other areas.
     *  This can be useful when the speed is critical, because the simple
     *  search is faster.
     * @return array of integers containing starting and ending position
     *  of the block in the document. Null is returned if there's
     *  no matching block.
     */
    public int[] findMatchingBlock(int offset, boolean simpleSearch)
    throws BadLocationException {
        //return findMatch(offset, simpleSearch);
        return null;
    }
    
    public int[] findMatch(int offset, boolean simpleSearch)
    throws BadLocationException {
        // TODO - replanning to the other thread. Now it's in awt thread
        TokenItem token = getTokenChain(offset, offset+1);
        TokenItem tokenOnOffset = token;
        
        // if the carret is after char '>' or '/>', ship inside the tag
        if (token != null &&
                token.getTokenID() == XMLTokenIDs.TAG &&
                token.getImage().endsWith(">")) token = token.getPrevious();
        
        if(tokenOnOffset == null) return null;
        
        //declaration matching e.g. (<!DOCTYPE tutorial SYSTEM "newXMLWizard.dtd">)
        if(tokenOnOffset.getTokenID() == XMLTokenIDs.DECLARATION) {
            String tokenImage = tokenOnOffset.getImage();
            if(tokenImage.startsWith("<!")) { //NOI18N
                //declaration start
                TokenItem toki = tokenOnOffset;
                do {
                    toki = toki.getNext();
                } while (toki != null && toki.getTokenID() != XMLTokenIDs.DECLARATION);
                
                if(toki != null && toki.getTokenID() == XMLTokenIDs.DECLARATION && toki.getImage().endsWith(">")) {
                    int start = toki.getOffset();
                    int end = toki.getOffset() + toki.getImage().length();
                    return new int[] {start, end};
                }
            }
            if(tokenImage.endsWith(">") && (offset >= (tokenOnOffset.getOffset()) + tokenOnOffset.getImage().length() - ">".length())) { //NOI18N
                //declaration end
                TokenItem toki = tokenOnOffset;
                do {
                    toki = toki.getPrevious();
                } while (toki != null && toki.getTokenID() != XMLTokenIDs.DECLARATION);
                if(toki != null && toki.getTokenID() == XMLTokenIDs.DECLARATION && toki.getImage().startsWith("<!")) {
                    int start = toki.getOffset();
                    //end = PI_START offset + PI_START length + PI_TARGET length
                    int end = toki.getOffset() + "<!".length();
                    return new int[] {start, end};
                }
            }
            return null;
        }
        
        //PI matching e.g. <?xml vertion="1.0"?> will match <?xml (PI-START + PI-TARGET) and ?> (PI-END)
        if(tokenOnOffset.getTokenID() == XMLTokenIDs.PI_START ||
                tokenOnOffset.getTokenID() == XMLTokenIDs.PI_TARGET) {
            //carret in on PI_START or PI_TARGET => find PI end
            TokenItem toki = tokenOnOffset;
            do {
                toki = toki.getNext();
            } while (toki != null && toki.getTokenID() != XMLTokenIDs.PI_END);
            if(toki != null && toki.getTokenID() == XMLTokenIDs.PI_END) {
                int start = toki.getOffset();
                int end = toki.getOffset() + toki.getImage().length();
                return new int[] {start, end};
            }
        } else if(tokenOnOffset.getTokenID() == XMLTokenIDs.PI_END) {
            //carret is on PI_END => find PI start
            TokenItem toki = tokenOnOffset;
            do {
                toki = toki.getPrevious();
            } while (toki != null && toki.getTokenID() != XMLTokenIDs.PI_START);
            if(toki != null && toki.getTokenID() == XMLTokenIDs.PI_START) {
                int start = toki.getOffset();
                //end = PI_START offset + PI_START length + PI_TARGET length
                int end = toki.getOffset() + toki.getImage().length() + toki.getNext().getImage().length();
                return new int[] {start, end};
            }
        }
        
        //CDATA matching
        if(tokenOnOffset.getTokenID() == XMLTokenIDs.CDATA_SECTION) {
            String tokenImage = tokenOnOffset.getImage();
            
            TokenItem toki = tokenOnOffset;
            if(tokenImage.startsWith(CDATA_START) && (offset < (tokenOnOffset.getOffset()) + CDATA_START.length())) { //NOI18N
                //CDATA section start
                int start = toki.getOffset() + toki.getImage().length() - CDATA_END.length(); //NOI18N
                int end = toki.getOffset() + toki.getImage().length();
                return new int[] {start, end};
            }
            if(tokenImage.endsWith(CDATA_END) && (offset >= (tokenOnOffset.getOffset()) + tokenOnOffset.getImage().length() - CDATA_END.length())) { //NOI18N
                //CDATA section end
                int start = toki.getOffset();
                int end = toki.getOffset() + CDATA_START.length(); //NOI18N
                return new int[] {start, end};
            }
            return null;
        }
        
        //match xml comments
        if(tokenOnOffset.getTokenID() == XMLTokenIDs.BLOCK_COMMENT) {
            String tokenImage = tokenOnOffset.getImage();
            TokenItem toki = tokenOnOffset;
            if(tokenImage.startsWith("<!--") && (offset < (tokenOnOffset.getOffset()) + "<!--".length())) { //NOI18N
                //start html token - we need to find the end token of the html comment
                while(toki != null) {
                    if((toki.getTokenID() == XMLTokenIDs.BLOCK_COMMENT)) {
                        if(toki.getImage().endsWith("-->")) {//NOI18N
                            //found end token
                            int start = toki.getOffset() + toki.getImage().length() - "-->".length(); //NOI18N
                            int end = toki.getOffset() + toki.getImage().length();
                            return new int[] {start, end};
                        }
                    } else break;
                    toki = toki.getNext();
                }
            }
            if(tokenImage.endsWith("-->") && (offset >= (tokenOnOffset.getOffset()) + tokenOnOffset.getImage().length() - "-->".length())) { //NOI18N
                //end html token - we need to find the start token of the html comment
                while(toki != null) {
                    if((toki.getTokenID() == XMLTokenIDs.BLOCK_COMMENT)) {
                        if(toki.getImage().startsWith("<!--")) { //NOI18N
                            //found end token
                            int start = toki.getOffset();
                            int end = toki.getOffset() + "<!--".length(); //NOI18N
                            return new int[] {start, end};
                        }
                    } else break;
                    toki = toki.getPrevious();
                }
            }
            return null;
        } //eof match xml comments
        
        
        //tags matching
        boolean isInside = false;  // flag, whether the carret is somewhere in a HTML tag
        if( token != null ) {
            if (token.getImage().startsWith("<")) {
                isInside = true; // the carret is somewhere in '<htmltag' or '</htmltag'
            } else {
                // find out whether the carret is inside an HTML tag
                //try to find the beginning of the tag.
                while (token!=null &&
                        token.getTokenID() != XMLTokenIDs.TAG &&
                        !token.getImage().startsWith("<"))
                    token = token.getPrevious();
                if (token!=null && token.getTokenID() == XMLTokenIDs.TAG &&
                        token.getImage().startsWith("<"))
                    isInside = true;
            }
        }
        
        if (token != null && isInside){
            int start; // possition where the matched tag starts
            int end;   // possition where the matched tag ends
            int poss = -1; // how many the same tags is inside the mathed tag
            
            //test whether we are in a close tag
            if (token.getTokenID() == XMLTokenIDs.TAG && token.getImage().startsWith("</")) {
                //we are in a close tag
                String tag = token.getImage().substring(2).trim().toLowerCase();
                while ( token != null){
                    if (token.getTokenID() == XMLTokenIDs.TAG && !">".equals(token.getImage())) {
                        if (token.getImage().substring(1).trim().toLowerCase().equals(tag)
                        && !isSingletonTag(token)) {
                            //it's an open tag
                            if (poss == 0){
                                //get offset of previous token: < or </
                                start = token.getOffset();
                                end = token.getOffset()+token.getImage().length();
                                //include the closing > token into the block if it follows the opentag token
                                TokenItem next = token.getNext();
                                if(next != null && next.getTokenID() == XMLTokenIDs.TAG && ">".equals(next.getImage()))
                                    end++;
                                
                                return new int[] {start, end};
                            } else{
                                poss--;
                            }
                        } else {
                            //test whether the tag is a close tag for the 'tag' tagname
                            if ((token.getImage().substring(2).toLowerCase().indexOf(tag) > -1)
                            && !isSingletonTag(token)) {
                                poss++;
                            }
                        }
                    }
                    token = token.getPrevious();
                }
                
            } else{
                //we are in an open tag
                //We need to find out whether the open tag is a singleton tag or not.
                //In the first case no matching is needed
                if(isSingletonTag(token)) return null;
                
                String tag = token.getImage().substring(1).toLowerCase();
                while ( token != null){
                    if (token.getTokenID() == XMLTokenIDs.TAG && !">".equals(token.getImage())) {
                        if (token.getImage().substring(2).trim().toLowerCase().equals(tag)) {
                            //it's a close tag
                            if (poss == 0) {
                                //get offset of previous token: < or </
                                start = token.getOffset();
                                end = token.getOffset()+token.getImage().length()+1;
                                
                                return new int[] {start, end};
                            } else
                                poss--;
                        } else{
                            if (token.getImage().substring(1).toLowerCase().equals(tag)
                            && !isSingletonTag(token))
                                poss++;
                        }
                    }
                    token = token.getNext();
                }
            }
        }
        
        return super.findMatchingBlock(offset, simpleSearch);
    }
    
    /** Finds out whether the given tagTokenItem is a part of a singleton tag (e.g. <div style=""/>).
     * @tagTokenItem a token item whithin a tag
     * @return true is the token is a part of singleton tag
     */
    public boolean isSingletonTag(TokenItem tagTokenItem) {
        TokenItem ti = tagTokenItem;
        while(ti != null) {
            if(ti.getTokenID() == XMLTokenIDs.TAG) {
                if("/>".equals(ti.getImage())) { // NOI18N
                    return true;
                }
                if(">".equals(ti.getImage())) return false; // NOI18N
            }
            //break the loop on TEXT or on another open tag symbol
            //(just to prevent long loop in case the tag is not closed)
            if(ti.getTokenID() == XMLTokenIDs.TEXT) break;
            
            
            ti = ti.getNext();
        }
        return false;
    }
    
    
    
    /** Keep track of last typed character */
    private class DocumentMonitor implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
        }
        
        public void insertUpdate(DocumentEvent e) {
            int start = e.getOffset();
            int len = e.getLength();
            try {
                String s = e.getDocument().getText(start + len - 1, 1);
                lastInsertedChar = s.charAt(0);
            } catch (BadLocationException e1) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e1);
            }
        }
        
        public void removeUpdate(DocumentEvent e) {
        }
    }
}

