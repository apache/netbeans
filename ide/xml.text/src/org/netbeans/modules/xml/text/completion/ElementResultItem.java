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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;

import java.beans.BeanInfo;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.xml.api.model.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * Represent element name (or its part for namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class ElementResultItem extends XMLResultItem {
    private static final Color COLOR = new Color(64, 64, 255);
    
    // does it represent start element name?
    // then there is more possibilities how to complete it
    private final boolean startElement;
    
    private final boolean empty;
    private GrammarResult res;
    
    /**
     * Create a start element result item.
     */
    public ElementResultItem(int position, GrammarResult res){
        super(position, res.getNodeName());
        this.res = res;
        foreground = LFCustoms.shiftColor(COLOR);
        startElement = true;
        empty = res.isEmptyElement();
        icon = res.getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    
    /**
     * Create an end element result item.
     */
    public ElementResultItem(int position, String name) {
        super(position, name);
        foreground = LFCustoms.shiftColor(COLOR);
        startElement = false;
        empty = false;
    }
    
    /**
     * Replacenment text can be cutomized to retun pairs, empty tag or
     * just name of element.
     */
    public String getReplacementText(int modifiers) {
        boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;
        
        if (shift && startElement) {
            if (empty) {
                return displayText + "/>";
            } else {
                return displayText + ">";
            }
        } else if (startElement) {
            return displayText;
        } else {
            return displayText + '>';
        }
    }
    
    @Override
    public CompletionTask createDocumentationTask() {
        return doCreateDocumentationTask(res);
    }

    /**
     * If called with <code>SHIFT_MASK</code> modified it createa a start tag and
     * end tag pair and place caret between them.
     */
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        String replacementText = getReplacementText(modifiers);
        replaceText(c, replacementText, offset, len);
        
        boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;

        if (shift && startElement) {
            Caret caret = c.getCaret();  // it is at the end of replacement
            int dot = caret.getDot();
            int rlen = replacementText.length();
            if (empty) {
                caret.setDot((dot  - rlen) + replacementText.indexOf('/'));
            }
        }
        
        return false;
    }
    
//    /**
//     * @deprecated we use startElement flag
//     */
//    static class EndTag extends ElementResultItem {
//    }

    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }
}
