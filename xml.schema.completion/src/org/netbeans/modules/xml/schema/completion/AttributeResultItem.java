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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.completion;

import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.AttributePaintComponent;

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
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ATTRIBUTE));
    }
    
    /**
     * Creates a new instance of AttributeResultItem
     */
    public AttributeResultItem(AbstractAttribute attribute, String prefix, CompletionContext context) {
        super(attribute, context);
        itemText = prefix + ":" + attribute.getName();
        icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_ATTRIBUTE));
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
