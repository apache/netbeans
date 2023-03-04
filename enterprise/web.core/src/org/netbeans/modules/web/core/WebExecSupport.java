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

package org.netbeans.modules.web.core;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.jsploader.JspCompileUtil;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation;
import org.openide.filesystems.FileObject;

/** Static methods for execution parameters.
*
* @author Petr Jiricka
*/
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.web.spi.webmodule.RequestParametersQueryImplementation.class)
public class WebExecSupport implements RequestParametersQueryImplementation {

    private static final Logger LOG = Logger.getLogger(WebExecSupport.class.getName());
    public static final String EA_REQPARAMS = "NetBeansAttrReqParams"; // NOI18N

    /* Sets execution query string for the associated entry.
    * @param qStr the query string
    * @exception IOException if arguments cannot be set
    */
    public static void setQueryString(FileObject fo, String qStr) throws IOException {
        fo.setAttribute (EA_REQPARAMS, qStr);
    }

    /* Getter for query string associated with given file.
    * @return the query string or empty string if no quesy string associated
    */
    public static String getQueryString(FileObject fo) {
        try {
            String qStr = (String)fo.getAttribute (EA_REQPARAMS);
            if (qStr != null) {
                if ((qStr.length() > 0) && (!qStr.startsWith("?"))) // NOI18N
                    qStr = "?" + qStr; // NOI18N
                return qStr;
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE, "error", ex);
        }
        return ""; // NOI18N
    }

    /** Returns a web execution URL for a file
     * @param f file to run
     * @return part of URL string corresponding to file and parameters to use for execution. May be null if it can not be determined.
     */
    public String getFileAndParameters(FileObject f) {
        
        List <WebFrameworkProvider> frameworks = WebFrameworks.getFrameworks(); 
        String url = null;
        WebModule wm = WebModule.getWebModule(f);
        if (wm != null && frameworks.size() > 0){
            for ( WebFrameworkProvider frameworkProvider : frameworks) {
                if (frameworkProvider.isInWebModule(wm)){
                    url = frameworkProvider.getServletPath(f);
                    if (url != null)
                        break;
                }
            }
        }
        if (url == null & wm != null) {
            FileObject docBase = wm.getDocumentBase();
            if (docBase != null)
                url = JspCompileUtil.findRelativeContextPath(docBase, f);
        }
        if (url != null) {
            url = url + getQueryString(f);
            url = url.replace(" ", "%20");
        }
        return url;
    }
}

