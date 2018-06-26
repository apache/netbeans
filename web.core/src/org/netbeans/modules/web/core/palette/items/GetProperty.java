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
    protected List<BeanDescr> allBeans = new ArrayList<BeanDescr>();
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String property = "";
    
    public GetProperty() {
    }

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
        String strBean = " name=\"\""; // NOI18N
        if (beanIndex == -1)
            strBean = " name=\"" + bean + "\""; // NOI18N
        else 
            strBean = " name=\"" + allBeans.get(beanIndex).getId() + "\""; // NOI18N
        
        String strProperty = " property=\"" + property + "\""; // NOI18N
        
        String gp = "<jsp:getProperty" + strBean + strProperty + " />"; // NOI18N
        return gp;
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
       ArrayList<BeanDescr> res = new ArrayList<BeanDescr>();
        for (int i = 0; i < implicitBeans.length; i++) {
            String id = implicitBeans[i];
            String fqcn = implicitTypes[i];
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
