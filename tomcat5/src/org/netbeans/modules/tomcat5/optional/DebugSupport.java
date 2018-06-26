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

package org.netbeans.modules.tomcat5.optional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;

import org.openide.util.Exceptions;
import org.xml.sax.SAXException;


/** Debug support addition for Tomcat5
 *
 * @author Martin Grebac
 */
public class DebugSupport {

    private static final String JSP_SERVLET_NAME  = "jsp";                          //NOI18N
    private static final String JSP_SERVLET_CLASS = "org.apache.jasper.servlet.JspServlet"; //NOI18N

    private static final String MAPPED_PARAM_NAME =  "mappedfile"; //NOI18N
    private static final String MAPPED_PARAM_VALUE = "true"; //NOI18N

    public static void allowDebugging(TomcatManager tm) throws IOException, SAXException {
        String url = tm.getUri();
        
        // find the web.xml file
        File webXML = getDefaultWebXML(tm);
        if (webXML == null) {
            Logger.getLogger(DebugSupport.class.getName()).log(Level.INFO, null, new Exception(url));
            return;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            Logger.getLogger(DebugSupport.class.getName()).log(Level.INFO, null, new Exception(url));
            return;
        }
        boolean needsSave = setMappedProperty(webApp);
        if (needsSave) {
            OutputStream os = new FileOutputStream(webXML);
            try {
                webApp.write(os);
            } finally {
                os.close();
            }
        }
    }
    
    private static File getDefaultWebXML(TomcatManager tm) {
        File cb = tm.getTomcatProperties().getCatalinaDir();
        File webXML = new File(cb, "conf" + File.separator + "web.xml");
        if (webXML.exists())
            return webXML;
        return null;
    }
    
    private static boolean setMappedProperty(WebApp webApp) {

        boolean changed=false;
        boolean isServlet=false;
        
        Servlet[] servlets = webApp.getServlet();
        int i;
        for(i=0;i<servlets.length;i++) {
            if ((servlets[i].getServletName().equals(JSP_SERVLET_NAME)) && 
                (servlets[i].getServletClass().equals(JSP_SERVLET_CLASS))) {
                isServlet=true;
                break;
            }
        }
        
        if (!isServlet) {
            try {
                Servlet servlet = (Servlet)webApp.createBean("Servlet"); //NOI18N
                servlet.setServletName(JSP_SERVLET_NAME);
                servlet.setServletClass(JSP_SERVLET_CLASS);
                InitParam initParam = (InitParam)servlet.createBean("InitParam"); //NOI18N
                initParam.setParamName(MAPPED_PARAM_NAME);
                initParam.setParamValue(MAPPED_PARAM_VALUE);
                servlet.addInitParam(initParam);
                webApp.addServlet(servlet);
                changed=true;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                boolean isInitparam = false;
                InitParam[] initparams = servlets[i].getInitParam();
                int j;
                for (j=0;j<initparams.length;j++) {
                    if ((initparams[j].getParamName().equals(MAPPED_PARAM_NAME))) {
                        isInitparam=true;
                        break;
                    }
                }
                if (isInitparam) {
                    if (!initparams[j].getParamValue().equals(MAPPED_PARAM_VALUE)) {
                        initparams[j].setParamValue(MAPPED_PARAM_VALUE);
                        changed=true;
                    }
                } else {
                    InitParam initParam = (InitParam)servlets[i].createBean("InitParam"); //NOI18N
                    initParam.setParamName(MAPPED_PARAM_NAME);
                    initParam.setParamValue(MAPPED_PARAM_VALUE);
                    servlets[i].addInitParam(initParam);
                    changed=true;
                }
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return changed;
    }

}
