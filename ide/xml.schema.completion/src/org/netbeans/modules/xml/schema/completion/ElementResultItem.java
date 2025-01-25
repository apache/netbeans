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
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.ElementPaintComponent;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementResultItem extends CompletionResultItem {
    private int caretPosition = 0;
    private String replacingText;
    
    /**
     * Creates a new instance of ElementResultItem
     */
    public ElementResultItem(AbstractElement element, CompletionContext context) {
        this(element, context, null);
    }

    public ElementResultItem(AbstractElement element, CompletionContext context,
        TokenSequence tokenSequence) {
        super(element, context, tokenSequence);
        itemText = element.getName();
        icon = ImageUtilities.loadIcon(ICON_LOCATION + ICON_ELEMENT);
    }
    
    /**
     * Creates a new instance of ElementResultItem
     */
    // not the impl dependency is necessary to handle namespaces well.
    public ElementResultItem(AbstractElement element, String prefix, CompletionContextImpl context) {
        super(element, context);        
        itemText = prefix + ":" + element.getName();
        icon = ImageUtilities.loadIcon(ICON_LOCATION + ICON_ELEMENT);
    }

    @Override
    public String getDisplayText() {
        AbstractElement element = (AbstractElement)axiComponent;
        String cardinality = null;
        if(axiComponent.supportsCardinality() &&
           element.getMinOccurs() != null &&
           element.getMaxOccurs() != null) {
            cardinality = "["+element.getMinOccurs()+".."+element.getMaxOccurs()+"]";
        }
        String displayText = itemText;
        if(cardinality != null)
            displayText = displayText + " " + cardinality;
        
        return displayText;
    }
    
    /**
     * Overwrites getReplacementText of base class.
     * Add mandatory attributes. See issue: 108720
     */
    @Override
    public String getReplacementText() {
        replacingText = null;

        AbstractElement element = (AbstractElement)axiComponent;
        StringBuffer buffer = new StringBuffer(CompletionUtil.TAG_FIRST_CHAR);
        buffer.append(itemText);

        boolean firstAttr = true;
        boolean noAttrs = true;
        for (AbstractAttribute aa : element.getAttributes()) {
            if (aa instanceof AnyAttribute) continue;
            
            noAttrs = false;
            Attribute a = (Attribute)aa;
            if (a.getUse() == Use.REQUIRED) {
                if (buffer.length() == 0)
                    firstAttr = true;
                buffer.append(" " + a.getName() +
                    AttributeResultItem.ATTRIBUTE_EQUALS_AND_VALUE_STRING);
                if (firstAttr) {
                    caretPosition = buffer.length() - 1;
                }                
                firstAttr = false;
            }
        }
        if (noAttrs) {
            buffer.append(CompletionUtil.TAG_LAST_CHAR);
        }
        replacingText = buffer.toString();
        return replacingText;
    }
        
    @Override
    public CompletionPaintComponent getPaintComponent() {
        if(component == null) {
            component = new ElementPaintComponent(this);
        }
        return component;
    }

    
    /**
     * For elements, the caret should go inside the double quotes of
     * the first mandatory attribute.
     */
    @Override
    public int getCaretPosition() {
        if (replacingText == null) {
            return 0;
        }
        if (caretPosition == 0) {
            return replacingText.length();
        } else {
            return caretPosition;
        }
    }    

    @Override
    protected int removeTextLength(JTextComponent component, int offset, int removeLength) {
        if (removeLength <= 0) {
            return super.removeTextLength(component, offset, removeLength);
        }
        TokenSequence s = createTokenSequence(component);
        s.move(offset);
        s.moveNext();
        if (s.token().id() == XMLTokenId.TAG || s.token().id() == XMLTokenId.TEXT) {
            // replace entire tag, minus starting >
            if (s.token().text().toString().startsWith(CompletionUtil.TAG_FIRST_CHAR)) {
                return s.token().length() - (offset - s.offset());
            }
        }
        return super.removeTextLength(component, offset, removeLength);
    }

}
