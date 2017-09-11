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
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.completion.CompletionPaintComponent.ValuePaintComponent;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;

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
        this.icon = new ImageIcon(CompletionResultItem.class.
                getResource(ICON_LOCATION + ICON_VALUE));
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
