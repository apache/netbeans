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

