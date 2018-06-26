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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class UseBean implements ActiveEditorDrop {

    private static final int BEAN_DEFAULT = -1;

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;
    
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String clazz = "";
    private int scopeIndex = SCOPE_DEFAULT;
    
    private String[] beans;
   
    public UseBean() {
        beans = findBeans();
        if (beans.length > 0)
            beanIndex = 0;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        UseBeanCustomizer c = new UseBeanCustomizer(this, targetComponent);
        boolean accept = c.showDialog(JspPaletteUtilities.getAllBeans(targetComponent));
        if (accept) {
            String body = createBody();
            try {
                JspPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        String strBean = " id=\"\""; // NOI18N
        if (beanIndex == -1)
            strBean = " id=\"" + bean + "\""; // NOI18N
        else 
            strBean = " id=\"" + beans[beanIndex] + "\""; // NOI18N
        
        String strClass = " class=\"" + clazz + "\""; // NOI18N
        
        String strScope = " scope=\"" + scopes[scopeIndex] + "\""; // NOI18N

        String ub = "<jsp:useBean" + strBean + strScope + strClass + " />"; // NOI18N
        return ub;
    }
        
    private String[] findBeans() {
        //TODO retrieve existing beans
        String[] beans = new String[] {};
        return beans;
    }

    public int getBeanIndex() {
        return beanIndex;
    }

    public void setBeanIndex(int beanIndex) {
        this.beanIndex = beanIndex;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public int getScopeIndex() {
        return scopeIndex;
    }

    public void setScopeIndex(int scopeIndex) {
        this.scopeIndex = scopeIndex;
    }

    public String[] getBeans() {
        return beans;
    }

    public void setBeans(String[] beans) {
        this.beans = beans;
    }
    
}
