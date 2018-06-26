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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.openide.util.NbBundle;

/** SectionValidator.java
 *
 * Created on February 9, 2005, 4:57 PM
 * @author mkuchtiak
 */
public class SectionValidator {

    static Error validateServlets(WebApp webApp) {
        Servlet[] servlets = webApp.getServlet();
        Error error=null;
        for (int i=0;i<servlets.length;i++) {
            // valiadation for missing servlet-name
            String name = servlets[i].getServletName();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletName"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Name", loc);
                break;
            }
            // valiadation for missing servlet-class/jsp-file
            name = servlets[i].getServletClass();
            if (name==null) name = servlets[i].getJspFile();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletClass"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Class", loc);
                break;
            }
            // validation for duplicite url-patterns
            String[] urlPatterns = DDUtils.getUrlPatterns(webApp,servlets[i]);
            for (int j=0;j<urlPatterns.length;j++) {
                String errMessage = DDUtils.checkServletMappig(urlPatterns[j]);
                if (errMessage!=null) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletMapping"); //NOI18N
                    error = new Error(Error.ERROR_MESSAGE, errMessage , loc);
                    break;
                } 
                if (DDUtils.isServletMapping(webApp, servlets[i], urlPatterns[j])) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletMapping"); //NOI18N
                    error = new Error(Error.DUPLICATE_VALUE_MESSAGE, urlPatterns[j] , loc);
                    break;
                }
            }
        }
        return error;
    }
    
    static Error validateFilters(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        Error error=null;
        for (int i=0;i<filters.length;i++) {
            // valiadation for missing filter-name
            String name = filters[i].getFilterName();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(filters[i],"FilterName"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Filter Name", loc);
                break;
            }
            // valiadation for missing filter-class
            name = filters[i].getFilterClass();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(filters[i],"FilterClass"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Filter Class", loc);
                break;
            }
        }
        return error;
    }
    
    static Error validatePages(WebApp webApp) {
        Error error=null;
        JspConfig jspConfig=null;
        JspPropertyGroup[] groups=null;
        try {
            jspConfig = webApp.getSingleJspConfig();
        } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
            return null;
        }
        if (jspConfig==null) {
            return null;
        } else groups = jspConfig.getJspPropertyGroup();
        if (groups==null) return null;
        
        // validation for missing url-patern(s) 
        for (int i=0;i<groups.length;i++) {
            if (groups[i].sizeUrlPattern()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(groups[i],"url_patterns"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "URL Pattern", loc);
                break;
            }
        }
        return error;
    }
    
    static String validateNewServlet(WebApp webApp, String servletName, String servletClass, String jspFile, String urlPatterns) {
        if (servletName.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyServletName");
        } else {
            Servlet[] servlets = webApp.getServlet();
            boolean exists=false;
            for (int i=0;i<servlets.length;i++) {
                if (servletName.equals(servlets[i].getServletName())) {
                    exists=true;
                    break;
                }
            }
            if (exists) {
                return NbBundle.getMessage(SectionValidator.class,"TXT_ServletNameExists",servletName);
            }
        }
        if (servletClass.length()==0 && jspFile.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_Servlet_BothMissing");
        }
        if (servletClass.length()>0 && jspFile.length()>0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_Servlet_BothSpecified");
        }
        String[] patterns = DDUtils.getStringArray(urlPatterns);
        if (patterns.length>0) {
            for (int i=0;i<patterns.length;i++) {
                String errMessage = DDUtils.checkServletMappig(patterns[i]);
                if (errMessage!=null) return errMessage;
                if (DDUtils.isServletMapping(webApp,patterns[i])) {
                    return NbBundle.getMessage(SectionValidator.class,"TXT_UrlPatternExists",patterns[i]);
                }
            }
        }
        return null;
    }
    
    static String validateNewFilter(WebApp webApp, String filterName, String filterClass) {
        if (filterName.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyFilterName");
        } else {
            Filter[] filters = webApp.getFilter();
            boolean exists=false;
            for (int i=0;i<filters.length;i++) {
                if (filterName.equals(filters[i].getFilterName())) {
                    exists=true;
                    break;
                }
            }
            if (exists) {
                return NbBundle.getMessage(SectionValidator.class,"TXT_FilterNameExists",filterName);
            }
        }
        if (filterClass.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyFilterClass");
        }
        return null;
    }
    
    static String validateNewJspPropertyGroup(String urlPatterns) {
        String[] patterns = DDUtils.getStringArray(urlPatterns);
        if (patterns.length==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyUrls");
        }
        return null;
    }
}
