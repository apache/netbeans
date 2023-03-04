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
            try (OutputStream os = new FileOutputStream(webXML)) {
                webApp.write(os);
            }
        }
    }
    
    private static File getDefaultWebXML(TomcatManager tm) {
        File cb = tm.getTomcatProperties().getCatalinaDir();
        File webXML = new File(cb, "conf" + File.separator + "web.xml");
        if (webXML.exists()) {
            return webXML;
        }
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
