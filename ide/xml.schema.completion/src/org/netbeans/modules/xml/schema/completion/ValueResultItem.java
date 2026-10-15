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
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.ValuePaintComponent;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ValueResultItem extends CompletionResultItem {
    /**
     * Creates a new instance of ValueResultItem
     */
    public ValueResultItem(AXIComponent forComponent, String value, CompletionContext context) {
        super(forComponent, context);
        this.itemText = value;
        this.icon = ImageUtilities.loadIcon(ICON_LOCATION + ICON_VALUE);
    }

    @Override
    protected int removeTextLength(JTextComponent component, int offset, int removeLength) {
        TokenSequence<XMLTokenId> s = createTokenSequence(component);
        s.move(offset);
        if (!s.moveNext()) {
            return super.removeTextLength(component, offset, removeLength);
        }
        TokenId id = s.token().id();
        if (id == XMLTokenId.VALUE) {
            // check up to the end of tag, that there's not an error, e.g. an unterminated attribute
            int off = s.offset();
            String t = s.token().text().toString();
            char c = t.charAt(t.length() - 1);
            int len = t.length();
            boolean error = false;
            
            L: if (s.moveNext()) {
                XMLTokenId tid = s.token().id();
                if (tid != XMLTokenId.ARGUMENT && tid != XMLTokenId.TAG) {
                    error = true;
                    // only replace up to and excluding the first whitespace:
                    for (int i = 0; i < len; i++) {
                        if (Character.isWhitespace(t.charAt(i))) {
                            len = i;
                            break L;
                        }
                    }
                }
            }
            
            int l = off + t.length() - offset;
            if (t.isEmpty()) {
                return 0;
            }
            if (c == '\'' || c == '"') {
                return t.length() == -1 ? 0 : l - 1;
            } else {
                return 0;
            }
        }
        return super.removeTextLength(component, offset, removeLength);
    }
    
    
    
    /**
     * Overwrites getReplacementText of base class.
     */
    @Override
    public String getReplacementText(){
        return itemText;
    }
        
    @Override
    public String getDisplayText() {
        return getItemText();
    }
    
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new ValuePaintComponent(this);
        }
        return component;
    }

    /**
     * For atributes, the caret should go inside the double quotes.
     */
    @Override
    public int getCaretPosition() {
        return getReplacementText().length();
    }
}
