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
public class INPUT implements ActiveEditorDrop {

    public static final String TYPE_TEXT = "text"; // NOI18N
    public static final String TYPE_PASS = "password"; // NOI18N
    public static final String TYPE_HIDDEN = "hidden"; // NOI18N
    public static final String STATE_DISABLED = "disabled"; // NOI18N
    public static final String STATE_READONLY = "readonly"; // NOI18N
    
    private String name = "";
    private String value = "";
    private String type = TYPE_TEXT;
    private boolean disabled = false;
    private boolean hidden = false;
    private boolean readonly = false;
    private String width = "";
    
    public INPUT() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        INPUTCustomizer c = new INPUTCustomizer(this);
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

        String strName = " name=\"" + name + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N
        if ("file".equals(type) || "image".equals(type)){ // NOI18N
            strValue = "";// NOI18N
        }

        String strReadOnly = (readonly ? " readonly=\"readonly\"" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String strWidth = "";
        if (width.length() > 0)
            strWidth = " size=\"" + width + "\""; // NOI18N
        
        String inputBody = "<input" + strType + strName + strValue + strWidth + strReadOnly + strDisabled + " />"; // NOI18N
        
        return inputBody;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
        
}
