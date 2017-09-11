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
