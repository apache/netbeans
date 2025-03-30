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
package org.netbeans.modules.xml.schema.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.AttributePaintComponent;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeResultItem extends CompletionResultItem {
    
    public static final String ATTRIBUTE_EQUALS_AND_VALUE_STRING = "=\"\"";
    
    /**
     * Creates a new instance of AttributeResultItem
     */
    public AttributeResultItem(AbstractAttribute attribute, CompletionContext context) {
        super(attribute, context);
        itemText = attribute.getName();
        icon = ImageUtilities.loadIcon(ICON_LOCATION + ICON_ATTRIBUTE);
    }
    
    /**
     * Creates a new instance of AttributeResultItem
     */
    public AttributeResultItem(AbstractAttribute attribute, String prefix, CompletionContext context) {
        super(attribute, context);
        itemText = prefix + ":" + attribute.getName();
        icon = ImageUtilities.loadIcon(ICON_LOCATION + ICON_ATTRIBUTE);
    }
    
    private int caretOffset = -1;

    @Override
    protected String getInsertingText(JTextComponent component, int textPos, String primaryText, int removeLen) {
        createTokenSequence(component);
        int d = tokenSequence.move(textPos);
        if (!tokenSequence.moveNext()) {
            return super.getInsertingText(component, textPos, primaryText, removeLen);
        }
        Token token = tokenSequence.token();
        TokenId id = token.id();
        if (d == 0 && context.getTypedChars() == null) {
            // creating a completely new attribute; if the token at the caret is another attribute, at least inset a trailing space.
            if (id == XMLTokenId.ARGUMENT) {
                caretOffset = 2; // inside quotes
                return primaryText + " ";
            } else {
                caretOffset = 1; // inside quotes;
                return primaryText;
            }
        }
        if (id != XMLTokenId.ARGUMENT) {
            caretOffset = 1; // inside quotes;
            return primaryText;
        } else {
            while (tokenSequence.moveNext()) {
                token = tokenSequence.token();
                id = token.id();
                if (id == XMLTokenId.WS || id == XMLTokenId.OPERATOR) {
                    continue;
                }
                if (id == XMLTokenId.VALUE) {
                    // without ending quotes
                    caretOffset = 0;
                    return primaryText.substring(0, primaryText.length() - 1);
                }
            }
            caretOffset = 1; // inside quotes;
            return primaryText;
        }
    }

    @Override
    protected int caretOffset() {
        return caretOffset;
    }
    
    @Override
    protected int removeTextLength(JTextComponent component, int offset, int removeLength) {
        if (removeLength <= 0) {
            return super.removeTextLength(component, offset, removeLength);
        }
        TokenSequence s = createTokenSequence(component);
        s.move(offset);
        if (!s.moveNext()) {
            return super.removeTextLength(component, offset, removeLength);
        }
        TokenId id = s.token().id();
        if (id != XMLTokenId.ARGUMENT) {
            return s.token().length() - (offset - s.offset());
        }
        int l = s.offset() + s.token().length();
        while (s.moveNext()) {
            id = s.token().id();
            if (id== XMLTokenId.VALUE) {
                // remove up to and including the quote
                return s.offset() - offset + 1;
            } else if (!(id == XMLTokenId.WS || id == XMLTokenId.OPERATOR)) {
                break;
            }
            l = s.offset() + s.token().length();
        }
        return l - offset;
    }

    /**
     * Overwrites getReplacementText of base class.
     */
    @Override
    public String getReplacementText(){
        return itemText+ATTRIBUTE_EQUALS_AND_VALUE_STRING;
    }
        
    @Override
    public String getDisplayText() {
        return getItemText();
    }
    
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new AttributePaintComponent(this);
        }
        return component;
    }

    /**
     * For atributes, the caret should go inside the double quotes.
     */
    @Override
    public int getCaretPosition() {
        return getReplacementText().length() - caretOffset;
    }
}
