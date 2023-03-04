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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HtmlPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class BUTTON implements ActiveEditorDrop {

    public static final String TYPE_SUBMIT = "submit"; // NOI18N
    public static final String TYPE_RESET = "reset"; // NOI18N
    public static final String TYPE_BUTTON = "button"; // NOI18N

    private String value = "";
    private String type = TYPE_SUBMIT;
    private boolean disabled = false;
    private String name = "";
    
    public BUTTON() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        BUTTONCustomizer c = new BUTTONCustomizer(this);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HtmlPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strType = " type=\"" + type + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N

        String strName = "";
        if (name.length() > 0)
            strName = " name=\"" + name + "\""; // NOI18N

        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String inputBody = "<input" + strType + strValue + strName + strDisabled + " />"; // NOI18N
        
        return inputBody;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
        
}
