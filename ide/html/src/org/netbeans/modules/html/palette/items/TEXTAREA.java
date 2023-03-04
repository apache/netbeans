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
public class TEXTAREA implements ActiveEditorDrop {

    public static final String STATE_DISABLED = "disabled"; // NOI18N
    public static final String STATE_READONLY = "readonly"; // NOI18N

    public static final int ROWS_DEFAULT = 4;
    public static final int COLS_DEFAULT = 20;
    
    private String name = "";
    private String value = "";
    private boolean disabled = false;
    private boolean readonly = false;
    private int rows = ROWS_DEFAULT;
    private int cols = COLS_DEFAULT;
    
    public TEXTAREA() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        TEXTAREACustomizer c = new TEXTAREACustomizer(this);
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
        
        String strName = " name=\"" + name + "\""; // NOI18N

        String strValue = value;
        if (value.length() > 0)
            strValue += "\n";

        String strReadOnly = (readonly ? " readonly=\"readonly\"" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String strRows = " rows=\"" + rows + "\""; // NOI18N
        String strCols = " cols=\"" + cols + "\""; // NOI18N
        
        String taBody = "<textarea" + strName + strRows + strCols + strReadOnly + strDisabled + ">\n" + // NOI18N
                        strValue +
                        "</textarea>"; // NOI18N
        
        return taBody;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
        
}
