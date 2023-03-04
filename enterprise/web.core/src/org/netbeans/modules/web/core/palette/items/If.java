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
 * @author Libor Kotouc
 */
public class If implements ActiveEditorDrop {

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;

    private String condition = "";  //NOI18N
    private String variable = "";  //NOI18N
    private int scopeIndex = SCOPE_DEFAULT;
    
    public If() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        IfCustomizer c = new IfCustomizer(this, targetComponent);
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
        if(condition.equals("")) { // NOI18N
            condition = JspPaletteUtilities.CARET;
        }
        String strCondition = " test=\"" + condition + "\""; // NOI18N
        
        String strVariable = "";  //NOI18N
        if (variable.length() > 0)
            strVariable = " var=\"" + variable + "\""; // NOI18N
            
        String strScope = "";  //NOI18N
        if (scopeIndex != SCOPE_DEFAULT)
            strScope = " scope=\"" + scopes[scopeIndex] + "\""; // NOI18N

        return "<"+prefix+":if" + strCondition + strVariable + strScope + ">\n" + // NOI18N
                         "</"+prefix+":if>";  // NOI18N
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public int getScopeIndex() {
        return scopeIndex;
    }

    public void setScopeIndex(int scopeIndex) {
        this.scopeIndex = scopeIndex;
    }
        
   
}
