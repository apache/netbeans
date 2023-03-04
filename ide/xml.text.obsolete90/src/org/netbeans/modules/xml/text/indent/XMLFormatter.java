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
package org.netbeans.modules.xml.text.indent;

import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.formatting.TagBasedFormatter;
import org.netbeans.modules.xml.text.indent.XMLFormatSupport;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class XMLFormatter extends TagBasedFormatter {
    
    private static final String TAG_OPENING_PREFIX = "<"; //NOI18N
    private static final String TAG_CLOSING_PREFIX = "</"; //NOI18N
    private static final String TAG_CLOSED_SUFFIX = "/>"; //NOI18N
    private static final String TAG_CLOSING_SUFFIX = "/>"; //NOI18N
    
    //at least one character
    private static final Pattern VALID_TAG_NAME = Pattern.compile("[\\w+|-]*"); // NOI18N
    
    private static final int WORKUNITS_MAX = 100;
    
    public XMLFormatter(Class kitClass) {
        super(kitClass);
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new XMLFormatSupport(fw);
    }
    
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof JJEditorSyntax);
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new StripEndWhitespaceLayer());
    }
    
    protected String extractTagName(TokenItem tknTag){
        
        String tagImage = tknTag.getImage();
        int startIndex = -1;
        
        if (isOpeningTag(tknTag)){
            startIndex = TAG_OPENING_PREFIX.length();
        } else if (isClosingTag(tknTag)){
            startIndex = TAG_CLOSING_PREFIX.length();
        }
        
        if (startIndex >= 0){
            String tagName = tagImage.substring(startIndex);
            return tagName;
        }
        
        return null;
    }
    
    @Override protected boolean isOpeningTag(TokenItem token){
        return token != null
                && token.getTokenID() == XMLTokenIDs.TAG
                && token.getImage().startsWith(TAG_OPENING_PREFIX)
                && !token.getImage().startsWith(TAG_CLOSING_PREFIX);
    }
    
    @Override protected boolean isClosingTag(TokenItem token){
        return token != null
                && token.getTokenID() == XMLTokenIDs.TAG
                && token.getImage().startsWith(TAG_CLOSING_PREFIX);
    }
    
    @Override protected int getTagEndOffset(TokenItem token){
        TokenItem t = token.getNext();
        
        while (t != null && t.getTokenID() != XMLTokenIDs.TAG){
            t = t.getNext();
        }
        
        return t == null ? -1 : t.getOffset();
    }
    
    @Override protected ExtSyntaxSupport getSyntaxSupport(BaseDocument doc){
        return (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
    }
    
    @Override protected boolean areTagNamesEqual(String tagName1, String tagName2){
        return tagName1.equals(tagName2);
    }
    
    @Override protected boolean isClosingTagRequired(BaseDocument doc, String tagName){
        return true;
    }
    
    @Override protected int getOpeningSymbolOffset(TokenItem tknTag){
        return tknTag.getOffset();
    }
    
    @Override protected TokenItem getTagTokenEndingAtPosition(BaseDocument doc, int position) throws BadLocationException{
        if (position >= 0) {
            ExtSyntaxSupport sup = getSyntaxSupport(doc);
            TokenItem token = sup.getTokenChain(position, position + 1);
            
            if (token.getTokenID() == XMLTokenIDs.TAG &&
                    token.getImage().equals(">")){ //NOI18N
                do {
                    token = token.getPrevious();
                }
                while (token != null && !isOpeningTag(token) && !isClosingTag(token));
                
                return token;
            }
        }
        return null;
    }
    
    @Override protected boolean isUnformattableToken(TokenItem token) {
        
        if (token.getTokenID() == XMLTokenIDs.BLOCK_COMMENT
                || token.getTokenID() == XMLTokenIDs.CDATA_SECTION){
            return true;
        }
        
        return false;
    }
    
    @Override protected boolean isUnformattableTag(String tag) {
        return false;
    }
    
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
        
        public StripEndWhitespaceLayer() {
            super("xml-strip-whitespace-at-line-end-layer"); // NOI18N
        }
        
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new XMLFormatSupport(fw);
        }
        
        public void format(FormatWriter fw) {
            XMLFormatSupport xfs = (XMLFormatSupport)createFormatSupport(fw);
            
            FormatTokenPosition pos = xfs.getFormatStartPosition();
            
            if ( (xfs.isLineStart(pos) == false) ||
                    xfs.isIndentOnly() ) { // don't do anything
                
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = xfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = xfs.getNextPosition(pos);
                    }
                }
            }
        }
    }
}
