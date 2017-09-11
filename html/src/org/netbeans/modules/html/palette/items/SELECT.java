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
public class SELECT implements ActiveEditorDrop {

    public static final int OPTIONS_DEFAULT = 2;
    public static final int OPTIONS_VISIBLE_DEFAULT = 1;

    private String name = "";
    private int options = OPTIONS_DEFAULT;
    private int optionsVisible = OPTIONS_VISIBLE_DEFAULT;
    private boolean disabled = false;
    private boolean multiple = false;
    
    
    public SELECT() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        SELECTCustomizer c = new SELECTCustomizer(this);
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
        
        String sBody = generateSBody();
        
        String strName = " name=\"" + name + "\""; // NOI18N

        String strVisibleOptions = "";
        if (optionsVisible != OPTIONS_VISIBLE_DEFAULT)
            strVisibleOptions = " size=\"" + optionsVisible + "\""; // NOI18N
        
        String strMulti = (multiple ? " multiple=\"multiple\"" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String selBody = "<select" + strName + strVisibleOptions + strMulti + strDisabled + ">\n" + // NOI18N
                        sBody +
                        "</select>"; // NOI18N
        
        return selBody;
    }
        
    private String generateSBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < options; i++)
            sb.append("<option></option>\n"); // NOI18N
                
        String sBody = sb.toString();
        
        return sBody;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    public int getOptionsVisible() {
        return optionsVisible;
    }

    public void setOptionsVisible(int optionsVisible) {
        this.optionsVisible = optionsVisible;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
    
}
