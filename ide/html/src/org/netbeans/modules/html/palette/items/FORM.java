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
public class FORM implements ActiveEditorDrop {

    public static final String METHOD_GET = "GET"; // NOI18N
    public static final String METHOD_POST = "POST"; // NOI18N

    public static final String ENC_URLENC = "application/x-www-form-urlencoded"; // NOI18N
    public static final String ENC_MULTI = "multipart/form-data"; // NOI18N
    
    private static final String METHOD_DEFAULT = METHOD_GET;
    private static final String ENC_DEFAULT = ENC_URLENC;
    
    private String action = "";
    private String method = METHOD_DEFAULT;
    private String enc = ENC_DEFAULT;
    private String name = "";
    
    public FORM() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        FORMCustomizer c = new FORMCustomizer(this, targetComponent);
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
        
        String strAction = "";
        if (action.length() > 0)
            strAction = " action=\"" + action + "\""; // NOI18N
        
        String strMethod = "";
        if (!method.equals(METHOD_DEFAULT))
            strMethod = " method=\"" + method + "\""; // NOI18N

        String strEnc = "";
        if (!enc.equals(ENC_DEFAULT))
            strEnc = " enctype=\"" + enc + "\""; // NOI18N

        String strName = "";
        if (name.length() > 0)
            strName = " name=\"" + name + "\""; // NOI18N

        String formBody = "<form" + strName + strAction + strMethod + strEnc + ">\n</form>"; // NOI18N
        
        return formBody;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnc() {
        return enc;
    }

    public void setEnc(String enc) {
        this.enc = enc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
        
}
