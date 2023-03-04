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
