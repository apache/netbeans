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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * It represents attribute name (or namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class AttributeResultItem extends XMLResultItem {
    private static final Color COLOR = new Color(64, 64, 255);
    
    // we are requested to avoid appending extra data
    private boolean inline = false;
    
    private final GrammarResult res;

    public AttributeResultItem(int position, GrammarResult res, boolean inline){
        super(position, res.getNodeName());
        this.res = res;
        selectionForeground = foreground = Color.green.darker().darker();        
        this.inline = inline;
    }
    
    public String getReplacementText(int modifiers) {
        String extend = inline ? "" : "=\"\"";     // NOI18N
        return super.getReplacementText(modifiers) + extend;
    }
    
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        boolean result = super.replaceText(c, getReplacementText(modifiers), offset, len);
        c.getCaret().setDot(c.getCaretPosition() - 1); //shift cursor into bracklets
        return result;
    }
    
    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }

    @Override
    public CompletionTask createDocumentationTask() {
        return doCreateDocumentationTask(res);
    }
}
