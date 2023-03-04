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


package org.netbeans.modules.xml.text.dom;


import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import java.lang.ref.WeakReference;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.w3c.dom.Node;


/**
 *
 * Instances are produced by {@link XMLSyntaxSupport}.
 * <p>
 * <b>Warning:</b> class is public only for private purposes!
 *
 * @author  Petr Nejedly - original HTML design
 * @author  Sandeep Randhawa - XML port
 * @author  Petr Kuzel - DOM Nodes
 *
 * @version 1.0
 */
public abstract class BaseSyntaxElement implements org.netbeans.modules.xml.text.api.dom.SyntaxElement {
    
    protected XMLSyntaxSupport support;
    private WeakReference<Token> first; //a weak reference to the fist TokenItem of this SE
    private WeakReference<BaseSyntaxElement> previous;    // WR to the cached previous element
    private WeakReference<BaseSyntaxElement> next;        // WR to the cached next element
    
    // let it be visible by static inner classes extending us
    protected int offset;     // original position in document
    protected int length;     // original lenght in document
    
    /** Creates new SyntaxElement */
    BaseSyntaxElement(XMLSyntaxSupport support, Token<XMLTokenId> token, int start, int end)  {
        this.support = support;
        this.offset = start;
        this.length = end-start;
        this.first = new WeakReference(token);
    }
    
    /** returns an instance of first TokenItem of this SyntaxElement.
     * The instance is weakly held by this SyntaxElement instance, once
     * it is GC'ed, a new one is created using the offset of the original one.
     *
     * The WeakReference is used here because of a huge deep memory consumption of
     * a TokenItem-s under some circumstances (The SyntaxSupport chains the tokens
     * so it happens that each SyntaxElement instance holds it's own long
     * TokenItem-s chain.
     *
     * The current implementation lowers the CPU performance slightly, but
     * allows to GC the TokenItem-s chains if necessary.
     */
    protected Token first() {
        Token cached_first = first.get();
        if(cached_first != null)
            return cached_first;
        try {
            //it is a first token offset, so we shouldn't overlap the document length
            Token new_first = support.getNextToken(offset);
            first = new WeakReference(new_first);
            return new_first;
        } catch(BadLocationException e) {
            //fall through null
        }
        return null;
    }
    
    public int getElementOffset() {
        return offset;
    }
    
    public int getElementLength() {
        return length;
    }
    
    void setNext(BaseSyntaxElement se) {
        next = new WeakReference(se);
    }
    
    void setPrevious(BaseSyntaxElement se) {
        previous = new WeakReference(se);
    }
    
    /**
     * Get previous SyntaxElement. Weakly cache results.
     * @return previous SyntaxElement or <code>null</code> at document begining
     * or illegal location.
     */
    public BaseSyntaxElement getPrevious() {
        BaseSyntaxElement cached_previous = (previous == null) ? null : previous.get();
        if( cached_previous != null )
            return cached_previous;
        try {
            //we are on the beginning - no previous
            if (offset == 0) {
                return null;
            }
            //data not inialized yet or GC'ed already - we need to parse again
            BaseSyntaxElement new_previous = (BaseSyntaxElement)support.getElementChain( getElementOffset() - 1 );
            if( new_previous != null ) {
                setPrevious(new_previous); //weakly cache the element
                new_previous.setNext(this);
                if (new_previous.offset == offset) {
                    return null;
                }
            }
            return new_previous;
        } catch (BadLocationException ex) {
            return null;
        }
    }
    
    /**
     * Get next SyntaxElement. Cache results.
     * @return next SyntaxElement or <code>null</code> at document end
     * or illegal location.
     */
    public BaseSyntaxElement getNext() {
        BaseSyntaxElement cached_next = next == null ? null : next.get();
        if( cached_next != null )
            return cached_next;
        try {
            //data not inialized yet or GC'ed already - we need to parse again
            BaseSyntaxElement new_next = (BaseSyntaxElement)support.getElementChain( offset+length + 1);
            if( new_next != null ) {
                setNext(new_next); //weakly cache the element
                new_next.setPrevious(this);
                if (new_next.offset == offset) {
                    return null;
                }
            }
            return new_next;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    /**
     * Print element content for debug purposes.
     */
    public String toString() {
        String content = "?";
        return "SyntaxElement [offset=" + offset + "; length=" + length + " ;type = " + this.getClass().getName() + "; content:" + content +"]";
    }
    
    /**
     *
     */
    public int hashCode() {
        return super.hashCode() ^ offset ^ length;
    }
    
    /**
     * DOM Node equals. It's not compatible with Object's equals specs!
     */
    public boolean equals(Object obj) {
        if (obj instanceof BaseSyntaxElement) {
            if (((BaseSyntaxElement)obj).offset == offset) return true;
        }
        return false;
    }
        
    public String getName() {
        return null;
    }
    
    /**
     * It may stop some DOM traversing.  //!!!
     */
    public static class Error extends BaseSyntaxElement {
        public Error( XMLSyntaxSupport support, Token first, int start, int end ) {
            super( support, first, start, end);
        }

        public String toString() {
            return "Error" + super.toString();                                  // NOI18N
        }

        @Override
        public int getType() {
            return NODE_ERROR;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public SyntaxElement getParentElement() {
            BaseSyntaxElement x = this;
            while (x != null && x.getType() != Node.ELEMENT_NODE) {
                x = x.getPrevious();
            }
            return x;
        }
    }
    
}
