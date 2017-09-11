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


/**
 *
 * @author Libor Kotouc
 */
public class META implements ActiveEditorDrop {

    public static final String TYPE_HEADERS = "HEADERS"; // NOI18N
    public static final String TYPE_ENGINES = "ENGINES"; // NOI18N

    public static final String[] headers = new String[] { "Content-Type", "Content-Language", "Refresh", "Cache-Control", "Expires" }; // NOI18N
    public static final int HEADER_DEFAULT = 0;
    public static final String[] engines = new String[] { "robots", "description", "keywords" }; // NOI18N
    public static final int ENGINE_DEFAULT = 0;
    
    private String type = TYPE_HEADERS;
    private int nameIndex = 0;
    private String name = "";
    private String content = "";
    
    public META() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        METACustomizer c = new METACustomizer(this);
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
        
        if (getNameIndex() != -1) {
            if (getType().equals(TYPE_HEADERS) && getNameIndex() != -1) 
                setName(headers[getNameIndex()]);
            else if (getType().equals(TYPE_ENGINES) && getNameIndex() != -1) 
                setName(engines[getNameIndex()]);
        }
        
        String strType = "";
        if (getType().equals(TYPE_HEADERS))
            strType = " http-equiv=\"" + getName() + "\""; // NOI18N
        else
            strType = " name=\"" + getName() + "\""; // NOI18N
        
        String strContent = " content=\"\""; // NOI18N
        if (getContent().length() > 0)
            strContent = " content=\"" + getContent() + "\""; // NOI18N  
        
        
        String meta = "<meta" + strType + strContent + " />"; // NOI18N
        
        return meta;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
        
}
