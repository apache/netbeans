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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author Libor Kotouc
 */
public class Choose implements ActiveEditorDrop {

    public static final int DEFAULT_WHENS = 1;

    private int whens = DEFAULT_WHENS;
    private boolean otherwise = true;

    public Choose() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        ChooseCustomizer c = new ChooseCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String prefix = JspPaletteUtilities.findJstlPrefix(targetComponent);
            String body = createBody(prefix);
            try {
                JspPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        return accept;
    }

    private String createBody(String prefix) {
        return "<"+prefix+":choose>\n" + // NOI18N
                generateChooseBody(prefix) +
                "</"+prefix+":choose>\n"; // NOI18N
    }
    
    private String generateChooseBody(String prefix) {
        StringBuffer sb = new StringBuffer();
        sb.append("<"+prefix+":when test=\""+JspPaletteUtilities.CARET+"\">\n</"+prefix+":when>\n");  //NOI18N
        for (int i = 1; i < whens; i++)
            sb.append("<"+prefix+":when test=\"\">\n</"+prefix+":when>\n"); // NOI18N
        
        if (otherwise)
            sb.append("<"+prefix+":otherwise>\n</"+prefix+":otherwise>\n"); // NOI18N
                
        return sb.toString();
    }

    public int getWhens() {
        return whens;
    }

    public void setWhens(int whens) {
        this.whens = whens;
    }

    public boolean isOtherwise() {
        return otherwise;
    }

    public void setOtherwise(boolean otherwise) {
        this.otherwise = otherwise;
    }
    
}
