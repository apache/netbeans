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
import org.openide.util.NbBundle;


/**
 *
 * @author Libor Kotouc
 */
public class A implements ActiveEditorDrop {

    public static final String[] protocols = new String[] { "file", "http", "https", "ftp", "mailto" }; // NOI18N
    public static final int PROTOCOL_DEFAULT = 0;
    public static final String[] targets = new String[] { 
        NbBundle.getMessage(A.class, "LBL_SameFrame"),
        NbBundle.getMessage(A.class, "LBL_NewWindow"),
        NbBundle.getMessage(A.class, "LBL_ParentFrame"),
        NbBundle.getMessage(A.class, "LBL_FullWindow")
    };
    public static final int TARGET_DEFAULT = 0;
    
    private int protocolIndex = PROTOCOL_DEFAULT;
    private String url = "";
    private String text = "";
    private int targetIndex = TARGET_DEFAULT;
    private String target = "";
    
    public A() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        ACustomizer c = new ACustomizer(this, targetComponent);
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
        
        String strProtocol = " href=\""; // NOI18N
        if (getProtocolIndex() != PROTOCOL_DEFAULT) {
            try {
                switch (getProtocolIndex()) {
                    case 1: strProtocol += "http://"; // NOI18N
                            break;
                    case 2: strProtocol += "https://"; // NOI18N
                            break;
                    case 3: strProtocol += "ftp://"; // NOI18N
                            break;
                    case 4: strProtocol += "mailto:"; // NOI18N
                }
            }
            catch (NumberFormatException nfe) {} // cannot occur
        }
        
        String strURL = "\"";
        if (getUrl().length() > 0)
            strURL = getUrl() + "\"";
        
        strProtocol += strURL;

        String strTarget = "";
        if (targetIndex != -1 && targetIndex != TARGET_DEFAULT) {
            try {
                switch (getTargetIndex()) {
                    case 1: setTarget("_blank"); // NOI18N
                            break;
                    case 2: setTarget("_parent"); // NOI18N
                            break;
                    case 3: setTarget("_top"); // NOI18N
                }
            }
            catch (NumberFormatException nfe) {}
        }
        
        if (getTarget().length() > 0)
            strTarget = " target=\"" + getTarget() + "\""; // NOI18N

        String aLink = "<a" + strProtocol + strTarget + ">" + getText() + "</a>"; // NOI18N
        
        return aLink;
    }

    public int getProtocolIndex() {
        return protocolIndex;
    }

    public void setProtocolIndex(int protocolIndex) {
        this.protocolIndex = protocolIndex;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
        
}
