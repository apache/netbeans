/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR_CLASS;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR_CLASS_2_0;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR_CLASS_OLD;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SPRING_SERVLET_ADAPTOR_CLASS;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class contains everything related to web.xml generation and update which used
 * to be defined directly in RestSupport or one of its subclasses. I tried to move
 * it here as a logical piece of functionality. The methods itself in this class
 * were never reviewed - I just moved them from somewhere else. See also
 * ApplicationSubclassGenerator class which has similar role for everything
 * related to subclassing Application.
 */
public class WebXmlUpdater {

    private static final String JERSEY_PROP_PACKAGES = "com.sun.jersey.config.property.packages"; //NOI18N
    private static final String JERSEY_PROP_PACKAGES_DESC = "Multiple packages, separated by semicolon(;), can be specified in param-value"; //NOI18N
    private static final String POJO_MAPPING_FEATURE = "com.sun.jersey.api.json.POJOMappingFeature"; // NOI18N
    private static final String PARAM_NAME_APPLICATION_CLASS = "javax.ws.rs.Application"; // NOI18N
    
    private RestSupport restSupport;

    public WebXmlUpdater(RestSupport restSupport) {
        this.restSupport = restSupport;
    }

    public static Servlet getRestServletAdaptorByName(WebApp webApp, String servletName) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                if (servletName.equals(s.getServletName())) {
                    return s;
                }
            }
        }
        return null;
    }

    public WebApp findWebApp() {
        WebModule wm = WebModule.getWebModule(restSupport.getProject().getProjectDirectory());
        if (wm != null) {
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo != null) {
                try {
                    return DDProvider.getDefault().getDDRoot(ddFo);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public void configRestPackages( String... packs ) throws IOException {
        try {
            addResourceConfigToWebApp();           // NOI18N
            FileObject ddFO = getWebXml(false);
            WebApp webApp = findWebApp();
            if (webApp == null) {
                return;
            }
            if (webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE ||
                     webApp.getStatus() == WebApp.STATE_INVALID_OLD_VERSION)
            {
                return;
            }
            boolean needsSave = false;
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if ( adaptorServlet == null ){
                return;
            }
            InitParam[] initParams = adaptorServlet.getInitParam();
            boolean jerseyParamFound = false;
            boolean jacksonParamFound = false;
            for (InitParam initParam : initParams) {
                if (initParam.getParamName().equals(JERSEY_PROP_PACKAGES)) {
                    jerseyParamFound = true;
                    String paramValue = initParam.getParamValue();
                    if (paramValue != null) {
                        paramValue = paramValue.trim();
                    }
                    else {
                        paramValue = "";
                    }
                    if (paramValue.length() == 0 || paramValue.equals(".")){ // NOI18N
                        initParam.setParamValue(getPackagesList(packs));
                        needsSave = true;
                    }
                    else {
                        String[] existed = paramValue.split(";");
                        LinkedHashSet<String> set = new LinkedHashSet<String>();
                        set.addAll(Arrays.asList(existed));
                        set.addAll(Arrays.asList(packs));
                        initParam.setParamValue(getPackagesList(set));
                        needsSave = existed.length != set.size();
                    }
                }
                else if ( initParam.getParamName().equals( POJO_MAPPING_FEATURE)){
                    jacksonParamFound = true;
                    String paramValue = initParam.getParamValue();
                    if (paramValue != null) {
                        paramValue = paramValue.trim();
                    }
                    if ( !Boolean.TRUE.toString().equals(paramValue)){
                        initParam.setParamValue(Boolean.TRUE.toString());
                        needsSave = true;
                    }
                }
            }
            if (!jerseyParamFound) {
                InitParam initParam = createJerseyPackagesInitParam(adaptorServlet,
                        packs);
                adaptorServlet.addInitParam(initParam);
                needsSave = true;
            }
            if ( !jacksonParamFound ){
                InitParam initParam = createInitParam(adaptorServlet,
                        POJO_MAPPING_FEATURE, Boolean.TRUE.toString(), null);
                adaptorServlet.addInitParam(initParam);
                needsSave = true;
            }
            if (needsSave) {
                webApp.write(ddFO);
                restSupport.logResourceCreation();
            }
	}
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
    private String getPackagesList( Iterable<String> packs ) {
        StringBuilder builder = new StringBuilder();
        for (String pack : packs) {
            builder.append( pack);
            builder.append(';');
        }
        String packages ;
        if ( builder.length() >0 ){
            packages  = builder.substring( 0 ,  builder.length() -1 );
        }
        else{
            packages = builder.toString();
        }
        return packages;
    }

    private String getPackagesList( String[] packs ) {
        return getPackagesList( Arrays.asList( packs));
    }

    private InitParam createJerseyPackagesInitParam( Servlet adaptorServlet,
            String... packs ) throws ClassNotFoundException
    {
        return createInitParam(adaptorServlet, JERSEY_PROP_PACKAGES,
                getPackagesList(packs), JERSEY_PROP_PACKAGES_DESC);
    }

    private InitParam createInitParam( Servlet adaptorServlet, String name,
            String value , String description ) throws ClassNotFoundException
    {
        InitParam initParam = (InitParam) adaptorServlet
                .createBean("InitParam"); // NOI18N
        initParam.setParamName(name);
        initParam.setParamValue(value);
        if ( description != null ){
            initParam.setDescription(description);
        }
        return initParam;
    }

    public void addResourceConfigToWebApp() throws IOException {
        FileObject ddFO = getWebXml(true);
        WebApp webApp = null;
        try {
            webApp = getWebApp();
        } catch (IllegalStateException ex) {
            return;
        }
        boolean servletUpdated = false;
        try {
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet == null) {
                adaptorServlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
                boolean isSpring = restSupport.hasSpringSupport();
                if (isSpring) {
                    adaptorServlet.setServletClass(REST_SPRING_SERVLET_ADAPTOR_CLASS);
                    InitParam initParam = (InitParam) adaptorServlet.createBean("InitParam"); //NOI18N
                    initParam.setParamName(JERSEY_PROP_PACKAGES);
                    initParam.setParamValue("."); //NOI18N
                    initParam.setDescription(JERSEY_PROP_PACKAGES_DESC);
                    adaptorServlet.addInitParam(initParam);
                } else {
                    if (restSupport.hasJersey2(true)) {
                        adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS_2_0);
                    } else {
                        adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS);
                    }
                }
                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
                webApp.addServlet(adaptorServlet);
                servletUpdated = true;
            }

            boolean mappingsUpdated = updateServletMapping(webApp, adaptorServlet.getServletName());
            if (servletUpdated || mappingsUpdated) {
                webApp.write(ddFO);
                restSupport.logResourceCreation();
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public void addJersey2ResourceConfigToWebApp(RestSupport.RestConfig restConfig) throws IOException {
        String applicationClassName = restConfig.getAppClassName();
        if (applicationClassName == null) {
            return;
        }
        FileObject ddFO = getWebXml(true);
        WebApp webApp = null;
        try {
            webApp = getWebApp();
        } catch (IllegalStateException ex) {
            return;
        }
        boolean servletUpdated = false;
        try {
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet == null) {
                adaptorServlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
                adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS_2_0);
                InitParam initParam = (InitParam) adaptorServlet.createBean("InitParam"); //NOI18N
                initParam.setParamName(PARAM_NAME_APPLICATION_CLASS);
                initParam.setParamValue(applicationClassName); //NOI18N
                adaptorServlet.addInitParam(initParam);
                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
                webApp.addServlet(adaptorServlet);
                servletUpdated = true;
            }
            
            boolean mappingsUpdated = updateServletMapping(webApp, adaptorServlet.getServletName());
            if (servletUpdated || mappingsUpdated) {
                webApp.write(ddFO);
                restSupport.logResourceCreation();
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private boolean updateServletMapping(WebApp webApp, String servletName) throws ClassNotFoundException {
        boolean updated = false;
        String resourcesUrl = "/webresources/*"; //NOI18N
        ServletMapping25 sm = getRestServletMapping(webApp);
        if (sm == null) {
            sm = (ServletMapping25) webApp.createBean("ServletMapping"); //NOI18N
            sm.setServletName(servletName);
            sm.addUrlPattern(resourcesUrl);
            webApp.addServletMapping(sm);
            updated = true;
        } else {
            // check old url pattern
            boolean urlPatternChanged = false;
            String[] urlPatterns = sm.getUrlPatterns();
            if (urlPatterns.length == 0 || !resourcesUrl.equals(urlPatterns[0])) {
                urlPatternChanged = true;
            }

            if (urlPatternChanged) {
                if (urlPatterns.length>0) {
                    sm.setUrlPattern(0, resourcesUrl);
                } else {
                    sm.addUrlPattern(resourcesUrl);
                }
                updated = true;
            }
        }
        return updated;
    }
    
    private WebApp getWebApp() throws java.lang.IllegalStateException {
        WebApp webApp = findWebApp();
        if (webApp == null) {
            throw new IllegalStateException("Can not parse web.xml");
        }
        if (webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE ||
                webApp.getStatus() == WebApp.STATE_INVALID_OLD_VERSION)
        {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(RestSupport.class, "MSG_InvalidDD", webApp.getError()),
                        NotifyDescriptor.ERROR_MESSAGE));
            throw new IllegalStateException("Invalid web.xml");
        }
        return webApp;
    }

    public static ServletMapping25 getRestServletMapping(WebApp webApp) {
        if (webApp == null) {
            return null;
        }
        String servletName = null;
        for (Servlet s : webApp.getServlet()) {
            String servletClass = s.getServletClass();
            if (REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) || REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS_2_0.equals(servletClass)) {
                servletName = s.getServletName();
                break;
            }
        }
        if (servletName != null) {
            for (ServletMapping sm : webApp.getServletMapping()) {
                if (servletName.equals(sm.getServletName())) {
                    return (ServletMapping25)sm;
                }
            }
        }
        return null;
    }

// moving to RestUtils
//    public FileObject getDeploymentDescriptor() {
//        WebModuleProvider wmp = restSupport.getProject().getLookup().lookup(WebModuleProvider.class);
//        if (wmp != null) {
//            return wmp.findWebModule(restSupport.getProject().getProjectDirectory()).getDeploymentDescriptor();
//        }
//        return null;
//    }
//
    public FileObject getWebXml(boolean createWebXmlIfMissing) throws IOException {
        WebModule wm = WebModule.getWebModule(restSupport.getProject().getProjectDirectory());
        if (wm != null) {
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo == null && createWebXmlIfMissing) {
                FileObject webInf = wm.getWebInf();
                if (webInf == null) {
                    FileObject docBase = wm.getDocumentBase();
                    if (docBase != null) {
                        webInf = docBase.createFolder("WEB-INF"); //NOI18N
                    }
                }
                if (webInf != null) {
                    ddFo = DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
                }
            }
            return ddFo;
        }
        return null;
    }

    private static Servlet getRestServletAdaptor(WebApp webApp) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                String servletClass = s.getServletClass();
                if ( REST_SERVLET_ADAPTOR_CLASS_2_0.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS_OLD.equals(servletClass)) {
                    return s;
                }
            }
        }
        return null;
    }

    public static boolean hasRestServletAdaptor(WebApp wa) {
        return getRestServletAdaptor(wa) != null;
    }

}
