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

package org.netbeans.modules.xml.text.completion;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;

import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helper class used in XMLCompletionQuery and other classes that use grammar
 * Instances of this class must only be constructed and used from the AWT
 * dispatch thread, because this implementation is not reentrant (see ctx field).
 *
 * @author  asgeir@dimonsoftware.com
 */
final class SyntaxQueryHelper {
    
    public final static int COMPLETION_TYPE_UNKNOWN = 0;
    public final static int COMPLETION_TYPE_ATTRIBUTE = 1;
    public final static int COMPLETION_TYPE_VALUE = 2;
    public final static int COMPLETION_TYPE_ELEMENT = 3;
    public final static int COMPLETION_TYPE_ENTITY = 4;
    public final static int COMPLETION_TYPE_NOTATION = 5;
    public final static int COMPLETION_TYPE_DTD = 6;
    
    private XMLSyntaxSupport support;

    /** Currect oken or previous one if at token boundary */
    private Token<XMLTokenId> token = null;
    
    private int tokenOffset;
    
    private String preText = "";
    
    private int erase = 0;
    
    private int tunedOffset = 0;
    
    private SyntaxElement element;
    
    private int completionType = 0;
    
    private boolean tokenBoundary;

    private DefaultContext ctx = new DefaultContext();

    /** Creates a new instance of SyntaxQueryHelper */
    public SyntaxQueryHelper(XMLSyntaxSupport sup, int offset) throws BadLocationException, IllegalStateException {
        this.support = sup;
        tunedOffset = offset;
        sup.runWithSequence(tunedOffset, (TokenSequence seq) -> {
            token = sup.getPreviousToken(tunedOffset);
            tokenOffset = seq.offset();
            return null;
        });
        
        if( token != null ) { // inside document
            tokenBoundary = tokenOffset + token.length() == tunedOffset;
        } else {
            //??? start of document no choice now, but should be prolog if not followed by it
            throw new BadLocationException("No token found at current position", offset); // NOI18N
        }

        // find out last typed chars that can hint

        int itemOffset = tokenOffset;
        preText = "";
        erase = 0;
        int eraseRight = 0;
        XMLTokenId id = token.id();

        // determine last typed text, prefix text

        if ( tokenBoundary == false ) {

            preText = token.text().toString().substring( 0, tunedOffset - tokenOffset);
            if ("".equals(preText)) throw new IllegalStateException("Cannot get token prefix at " + tunedOffset);

            // manipulate tunedOffset to delete rest of an old name
            // for cases where it iseasy to locate original name end

            if (sup.lastTypedChar() != '<' && sup.lastTypedChar() != '&') {
                switch (id) {

                    case TAG:
                    case CHARACTER:
                    case ARGUMENT:

                        int i = token.length();
                        int tail = i - (tunedOffset - itemOffset);
                        tunedOffset += tail;
                        eraseRight = tail;
                        break;
                }
            }
         } else {
           switch (id) {
                case TEXT:
                case TAG:
                case ARGUMENT:
                case CHARACTER:
                case PI_CONTENT:
                    preText = token.text().toString();
                    break;                        
            }
         }

        // adjust how much do you want to erase from the preText

        switch (id) {
            case TAG:
                // do not erase start delimiters
                erase = preText.length() - 1 + eraseRight;
                break;
            case CHARACTER:
                //entity references
                erase = preText.length() + -1 + eraseRight;
                break;
            case ARGUMENT:
                erase = preText.length() + eraseRight;
                break;
            case VALUE:
                erase = preText.length();
                if (erase > 0 && (preText.charAt(0) == '\'' || preText.charAt(0) == '"')) {
                    // Because of attribute values, preText is adjusted in initContext
                    erase--;
                } else
                break;
        }

        element =  sup.getElementChain( tunedOffset);
        
        if (element == null) throw new IllegalStateException("There exists a token therefore a syntax element must exist at " + offset + ", too.");

        // completion request originates from area covered by DOM, 
        if (element.getType() != SyntaxElement.NODE_ERROR && element.getType() != Node.DOCUMENT_TYPE_NODE) {
            completionType = support.runLocked(this::initContext);
        } else {
            // prolog, internal DTD no completition yet
            completionType = COMPLETION_TYPE_DTD;
        }
    }
    
    public int getTokenOffset() {
        return tokenOffset;
    }
    
    /**
     * Find out what to complete: attribute, value, element, entity or notation?
     * <p>
     * <pre>
     * Triggering criteria:
     *
     * ELEMENT      TOKEN (,=seq)   PRETEXT         QUERY
     * -------------------------------------------------------------------
     * Text         text            &lt;            element name
     * Text         text            &lt;/           pairing end element
     * StartTag     tag             &lt;prefix      element name
     * StartTag     ws                              attribute name
     * StartTag     attr, operator  =               quoted attribute value
     * StartTag     value           'prefix         attribute value
     * StartTag     tag             >               element value
     * Text         text            &amp;           entity ref name     
     * StartTag     value           &amp;           entity ref name
     * </pre>
     *
     * @return the type of completion which is one of 
     *          COMPLETION_TYPE_UNKNOWN = 0,
     *          COMPLETION_TYPE_ATTRIBUTE = 1,
     *          COMPLETION_TYPE_VALUE = 2,
     *          COMPLETION_TYPE_ELEMENT = 3,
     *          COMPLETION_TYPE_ENTITY = 4,
     *          COMPLETION_TYPE_NOTATION = 5.
     */
    private int initContext() throws BadLocationException {
        XMLTokenId id = token.id();
        final Node syntaxNode = element.getNode();
        switch ( id) {
            case TEXT:
                if ( preText.endsWith("<" ) || preText.endsWith("</")) {
                    ctx.init(syntaxNode, "");
                    return COMPLETION_TYPE_ELEMENT;
                } else if ( preText.startsWith("&")) {
                    ctx.init(syntaxNode, preText.substring(1));
                    return COMPLETION_TYPE_ENTITY;
                } else {
                    //??? join all previous texts? 
                    // No they are DOM nodes.
                    ctx.init(syntaxNode, preText);
                    return COMPLETION_TYPE_VALUE;
                }
//                break;
                
            case TAG:
                if (support.isNormalTag(element)) {
                    if (preText.equals("")) {  
                        //??? should not occure
                        if (token.text().toString().endsWith(">")) {
                            ctx.init(syntaxNode, preText);
                            return COMPLETION_TYPE_VALUE;
                        } else {
                            ctx.init(syntaxNode, preText);
                            return COMPLETION_TYPE_ELEMENT;
                        }
                    } else if (preText.endsWith("/>")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;                        
                    } else if (preText.endsWith(">")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                    } else if (preText.startsWith("</")) {
                        //??? replace immediatelly?
                        ctx.init(syntaxNode, preText.substring(2));
                        return COMPLETION_TYPE_ELEMENT;
                    } else if (preText.startsWith("<")) {
                        ctx.init(syntaxNode, preText.substring(1));
                        return COMPLETION_TYPE_ELEMENT;
                    }
                } else if(support.isEndTag(element) && preText.startsWith("</")){
                    //endtag
                    ctx.init(syntaxNode, preText.substring(2));
                    return COMPLETION_TYPE_ELEMENT;
                } else {
                    // pairing tag completion if not at boundary
                    if ("".equals(preText) && token.text().toString().endsWith(">")) {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;
                
            case VALUE:
                if (preText.endsWith("&")) {
                    ctx.init(syntaxNode, "");
                    return COMPLETION_TYPE_ENTITY;
                } else if ("".equals(preText)) {   //??? improve check to addres inner '"'
                    String image = token.text().toString();
                    char ch = image.charAt(image.length()-1);
                    
                    // findout if it is closing '
                    
                    if (ch == '\'' || ch == '"') {
                        
                        if (image.charAt(0) == ch && image.length() > 1) {
                            // we got whole quoted value as single token ("xxx"|)
                            return COMPLETION_TYPE_UNKNOWN;                            
                        }

                        int res = support.<Integer>runWithSequence(tokenOffset, (TokenSequence seq) -> {
                            Token<XMLTokenId> prev = support.getPreviousToken(tokenOffset);
                            boolean closing = false;

                            while (prev != null) {
                                XMLTokenId tid = prev.id();
                                if (tid == XMLTokenId.VALUE) {
                                    closing = true;
                                    break;
                                } else if (tid == XMLTokenId.CHARACTER) {
                                    if (!seq.movePrevious()) {
                                        return COMPLETION_TYPE_UNKNOWN;
                                    }
                                    prev = seq.token();
                                } else {
                                    break;
                                }
                            }
                            if (closing == false) {
                                ctx.init(syntaxNode, preText);
                                return COMPLETION_TYPE_VALUE;
                            } else {
                                return COMPLETION_TYPE_UNKNOWN;
                            }
                        });
                        if (res != COMPLETION_TYPE_UNKNOWN) {
                            return res;
                        }
                    } else {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;                        
                    }
                } else {
                    // This is probably an attribute value
                    // Let's find the matching attribute node and use it to initialize the context
                    NamedNodeMap attrs = syntaxNode.getAttributes();
                    int maxOffsetLessThanCurrent = -1;
                    Node curAttrNode = null;
                    for (int ind = 0; ind < attrs.getLength(); ind++) {
                        SyntaxElement attr = (SyntaxElement)attrs.item(ind);
                        int attrTokOffset = attr.getElementOffset();
                        if (attrTokOffset > maxOffsetLessThanCurrent && attrTokOffset < tokenOffset) {
                            maxOffsetLessThanCurrent = attrTokOffset;
                            curAttrNode = (Node)attr;
                        }
                    }

                    // eliminate "'",'"' delimiters
                    if (preText.length() > 0) {
                        preText = preText.substring(1);
                    }
                    if (curAttrNode != null) {
                        ctx.init(curAttrNode, preText);
                    } else {
                        ctx.init(syntaxNode, preText);
                    }
                    return COMPLETION_TYPE_VALUE;
                }
                break;
                
            case OPERATOR:
                if ("".equals(preText)) {
                    if ("=".equals(token.text())) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;

            case WS:
                if (support.isNormalTag(element)
                 && !token.text().toString().startsWith("/")) {
                    ctx.init((Element)syntaxNode, ""); // GrammarQuery.v2 takes Element ctx 
                    return COMPLETION_TYPE_ATTRIBUTE;
                } else {
                    // end tag no attributes to complete
                    return COMPLETION_TYPE_UNKNOWN;
                }
//                break;
                
            case ARGUMENT:
                if (support.isStartTag(element)
                || support.isEmptyTag(element)) {
                    //try to find the current attribute 
                    NamedNodeMap nnm = syntaxNode.getAttributes();
                    for(int i = 0; i < nnm.getLength(); i++) {
                        Attr attrNode = (Attr)nnm.item(i);
                        if(support.getNodeOffset(attrNode) == tokenOffset) {
                            ctx.init(attrNode, preText);
                        }
                    }
                    if(!ctx.isInitialized()) {
                        ctx.init((Element)syntaxNode, preText); // GrammarQuery.v2 takes Element ctx
                    } 
                    return COMPLETION_TYPE_ATTRIBUTE;
                }
                break;
                
            case CHARACTER:  // entity reference
                if (preText.startsWith("&#")) {
                    // character ref, ignore
                    return COMPLETION_TYPE_UNKNOWN;
                } else if (preText.endsWith(";")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                } else if (preText.startsWith("&")) {
                    ctx.init(syntaxNode, preText.substring(1));
                    return COMPLETION_TYPE_ENTITY;
                } else if ("".equals(preText)) {
                    if (token.text().toString().endsWith(";")) {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;
                
            default:

        }
        
//        System.err.println("Cannot complete: " + syntaxNode + "\n\t" + token + "\n\t" + preText);
        return COMPLETION_TYPE_UNKNOWN;
    }
    
    public HintContext getContext() {
        if (completionType != COMPLETION_TYPE_UNKNOWN && completionType != COMPLETION_TYPE_DTD) {
            return ctx;
        } else {
            return null;
        }
    }

    /** Current token or previous one if at token boundary. */
    public Token<XMLTokenId> getToken() {
        return token;
    }
    
    public String getPreText() {
        return preText;
    }
    
    public int getEraseCount() {
        return erase;
    }
    
    public int getOffset() {
        return tunedOffset;
    }
    
    public SyntaxElement getSyntaxElement() {
        return element;
    }
    
    public int getCompletionType() {
        return completionType;
    }

    /** token boundary */
    public boolean isBoundary() {
        return tokenBoundary;
    }


}
