/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.core.palette.items;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class GetProperty implements ActiveEditorDrop {

    public static final String[] implicitBeans = new String[] {  // NOI18N
        "request",
        "response",
        "pageContext",
        "session",
        "application",
        "out",
        "config",
        "page",
        "exception"
    };
    public static final int BEAN_DEFAULT = 0;
    public static final String[] implicitTypes = new String[] { // NOI18N
        "javax.servlet.http.HttpServletRequest",
        "javax.servlet.http.HttpServletResponse",
        "javax.servlet.jsp.PageContext",
        "javax.servlet.http.HttpSession",
        "javax.servlet.ServletContext",
        "javax.servlet.jsp.JspWriter",
        "javax.servlet.ServletConfig",
        "java.lang.Object",
        "java.lang.Throwable"
    };
    public static final String[] implicitTypesJakarta = new String[] { // NOI18N
        "jakarta.servlet.http.HttpServletRequest",
        "jakarta.servlet.http.HttpServletResponse",
        "jakarta.servlet.jsp.PageContext",
        "jakarta.servlet.http.HttpSession",
        "jakarta.servlet.ServletContext",
        "jakarta.servlet.jsp.JspWriter",
        "jakarta.servlet.ServletConfig",
        "java.lang.Object",
        "java.lang.Throwable"
    };
    protected List<BeanDescr> allBeans = new ArrayList<>();
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String property = "";

    public GetProperty() {
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        allBeans = initAllBeans(targetComponent);
        GetPropertyCustomizer c = new GetPropertyCustomizer(this, targetComponent);

        boolean accept = c.showDialog();
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
        String strBean; // NOI18N
        if (beanIndex == -1) {
            strBean = " name=\"" + bean + "\""; // NOI18N
        } else {
            strBean = " name=\"" + allBeans.get(beanIndex).getId() + "\""; // NOI18N
        }

        String strProperty = " property=\"" + property + "\""; // NOI18N
        return "<jsp:getProperty" + strBean + strProperty + " />"; // NOI18N
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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    protected List<BeanDescr> initAllBeans(JTextComponent targetComponent) {
        String[] types;
        if(JspPaletteUtilities.isJakartaVariant(targetComponent)) {
            types = implicitTypesJakarta;
        } else {
            types = implicitTypes;
        }

        ArrayList<BeanDescr> res = new ArrayList<>();
        for (int i = 0; i < implicitBeans.length; i++) {
            String id = implicitBeans[i];
            String fqcn = types[i];
            res.add(new BeanDescr(id, fqcn));
        }
        PageInfo.BeanData[] bd = JspPaletteUtilities.getAllBeans(targetComponent);
        if (bd != null) {
            for (BeanData beanData : bd) {
                res.add(new BeanDescr(beanData.getId(), beanData.getClassName()));
            }
        }

        return res;
    }

    class BeanDescr {
        private String id;
        private String fqcn;

        public void setFqcn(String fqcn) {
            this.fqcn = fqcn;
        }

        public String getFqcn() {
            return fqcn;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public BeanDescr(String id, String fqcn) {
            this.id = id;
            this.fqcn = fqcn;
        }

        @Override
        public String toString() {
            return id;
        }

    }

    public List<BeanDescr> getAllBeans(){
        return allBeans;
    }
}
