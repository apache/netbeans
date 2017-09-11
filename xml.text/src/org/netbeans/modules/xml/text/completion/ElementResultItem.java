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
    
    /**
     * @deprecated we use startElement flag
     */
//    static class EndTag extends ElementResultItem {
//    }
    
    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }
}
