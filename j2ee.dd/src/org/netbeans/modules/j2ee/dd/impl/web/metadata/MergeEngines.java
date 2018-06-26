/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.EjbRefHelper;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.AnnotationHelpers;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.SecurityRoles;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.WebFilter;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.WebServlet;

/**
 * @author Petr Slechta
 */
public class MergeEngines {

    private static ServletsEngine servletsEngine = new ServletsEngine();
    private static FiltersEngine filtersEngine = new FiltersEngine();
    private static SecurityRolesEngine securityRolesEngine = new SecurityRolesEngine();
    private static ResourceRefsEngine resourceRefsEngine = new ResourceRefsEngine();
    private static ResourceEnvRefsEngine resourceEnvRefsEngine = new ResourceEnvRefsEngine();
    private static ResourceEnvEntriesEngine resourceEnvEntriesEngine = new ResourceEnvEntriesEngine();
    private static ResourceMsgDestsEngine resourceMsgDestsEngine = new ResourceMsgDestsEngine();
    private static ResourceServicesEngine resourceServicesEngine = new ResourceServicesEngine();
    private static EjbLocalRefsEngine ejbLocalRefsEngine = new EjbLocalRefsEngine();
    private static EjbRefsEngine ejbRefsEngine = new EjbRefsEngine();

    private MergeEngines() {
    }

    // -------------------------------------------------------------------------
    static MergeEngine<ServletInfo> servletsEngine() {
        return servletsEngine;
    }

    static MergeEngine<FilterInfo> filtersEngine() {
        return filtersEngine;
    }

    static MergeEngine<String> securityRolesEngine() {
        return securityRolesEngine;
    }

    static MergeEngine<ResourceRef> resourceRefsEngine() {
        return resourceRefsEngine;
    }

    static MergeEngine<ResourceEnvRef> resourceEnvRefsEngine() {
        return resourceEnvRefsEngine;
    }

    static MergeEngine<EnvEntry> resourceEnvEntriesEngine() {
        return resourceEnvEntriesEngine;
    }

    static MergeEngine<MessageDestinationRef> resourceMsgDestsEngine() {
        return resourceMsgDestsEngine;
    }

    static MergeEngine<ServiceRef> resourceServicesEngine() {
        return resourceServicesEngine;
    }

    static MergeEngine<EjbLocalRef> ejbLocalRefsEngine() {
        return ejbLocalRefsEngine;
    }

    static MergeEngine<EjbRef> ejbRefsEngine() {
        return ejbRefsEngine;
    }

    // -------------------------------------------------------------------------
    private static class ServletsEngine extends MergeEngine<ServletInfo> {
        @Override
        void addItems(WebApp webXml) {
            addServlets(webXml.getServlet(), webXml.getServletMapping());
        }

        @Override
        void addItems(WebFragment webXml) {
            addServlets(webXml.getServlet(), webXml.getServletMapping());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            for (WebServlet ann : annotationHelpers.getWebServletPOM().getObjects()) {
                res.add(ServletInfoAccessor.getDefault().createServletInfo(
                        ann.getName(), ann.getServletClass(), ann.getUrlPatterns()));
            }
        }

        private void addServlets(Servlet[] servlets, ServletMapping[] mappings) {
            if (servlets != null) {
                for (Servlet s : servlets) {
                    String name = s.getServletName();
                    String clazz = s.getServletClass();
                    if (clazz == null || clazz.trim().length() == 0) {
                        // ignore servlets which do not have a proper servlet class
                        continue;
                    }
                    List<String> urlMappings = findUrlMappingsForServlet(mappings, name);
                    res.add(ServletInfoAccessor.getDefault().createServletInfo(name, clazz, urlMappings));
                }
            }
        }

        private List<String> findUrlMappingsForServlet(ServletMapping[] mappings, String servletName) {
            List<String> mpgs = new ArrayList<String>();
            if (mappings != null) {
                for (ServletMapping sm : mappings) {
                    if (sm.getServletName().equals(servletName))
                        mpgs.addAll(Arrays.asList(((ServletMapping25)sm).getUrlPatterns()));
                }
            }
            return mpgs;
        }
    }

    // -------------------------------------------------------------------------
    private static class FiltersEngine extends MergeEngine<FilterInfo> {
        @Override
        void addItems(WebApp webXml) {
            addFilters(webXml.getFilter(), webXml.getFilterMapping());
        }

        @Override
        void addItems(WebFragment webXml) {
            addFilters(webXml.getFilter(), webXml.getFilterMapping());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            for (WebFilter ann : annotationHelpers.getWebFilterPOM().getObjects()) {
                res.add(FilterInfoAccessor.getDefault().createFilterInfo(
                        ann.getName(), ann.getFilterClass(), ann.getUrlPatterns()));
            }
        }

        private void addFilters(Filter[] filters, FilterMapping[] mappings) {
            if (filters != null) {
                for (Filter f : filters) {
                    String name = f.getFilterName();
                    String clazz = f.getFilterClass();
                    List<String> urlMappings = findUrlMappingsForFilter(mappings, name);
                    res.add(FilterInfoAccessor.getDefault().createFilterInfo(name, clazz, urlMappings));
                }
            }
        }

        private List<String> findUrlMappingsForFilter(FilterMapping[] mappings, String filterName) {
            List<String> mpgs = new ArrayList<String>();
            if (mappings != null) {
                for (FilterMapping fm : mappings) {
                    if (fm.getFilterName().equals(filterName) && fm.getUrlPattern() != null)
                        mpgs.add(fm.getUrlPattern());
                }
            }
            return mpgs;
        }
    }

    // -------------------------------------------------------------------------
    private static class SecurityRolesEngine extends MergeEngine<String> {
        @Override
        void addItems(WebApp webXml) {
            addRole(webXml.getSecurityRole());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addRole(webFragment.getSecurityRole());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            for (SecurityRoles ann : annotationHelpers.getSecurityRolesPOM().getObjects()) {
                res.addAll(ann.getRoles());
            }
        }

        private void addRole(SecurityRole[] roles) {
            for (SecurityRole r : roles) {
                res.add(r.getRoleName());
            }
        }
    }

    // -------------------------------------------------------------------------
    private static class ResourceRefsEngine extends MergeEngine<ResourceRef> {
        @Override
        void addItems(WebApp webXml) {
            addAll(webXml.getResourceRef());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addAll(webFragment.getResourceRef());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ResourceRef[] refs = CommonAnnotationHelper.getResourceRefs(annotationHelpers.getHelper());
            for (ResourceRef r : refs) {
                res.add(r);
            }
        }
    }

    // -------------------------------------------------------------------------
    private static class ResourceEnvRefsEngine extends MergeEngine<ResourceEnvRef> {
        @Override
        void addItems(WebApp webXml) {
            addAll(webXml.getResourceEnvRef());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addAll(webFragment.getResourceEnvRef());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ResourceEnvRef[] refs = CommonAnnotationHelper.getResourceEnvRefs(annotationHelpers.getHelper());
            for (ResourceEnvRef r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceEnvEntriesEngine extends MergeEngine<EnvEntry> {
        @Override
        void addItems(WebApp webXml) {
            addAll(webXml.getEnvEntry());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addAll(webFragment.getEnvEntry());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            EnvEntry[] refs = CommonAnnotationHelper.getEnvEntries(annotationHelpers.getHelper());
            for (EnvEntry r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceMsgDestsEngine extends MergeEngine<MessageDestinationRef> {
        @Override
        void addItems(WebApp webXml) {
            try {
                addAll(webXml.getMessageDestinationRef());
            }
            catch (VersionNotSupportedException ex) {
            }
        }

        @Override
        void addItems(WebFragment webFragment) {
            try {
                addAll(webFragment.getMessageDestinationRef());
            }
            catch (VersionNotSupportedException ex) {
            }
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            MessageDestinationRef[] refs = CommonAnnotationHelper.getMessageDestinationRefs(annotationHelpers.getHelper());
            for (MessageDestinationRef r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceServicesEngine extends MergeEngine<ServiceRef> {
        @Override
        void addItems(WebApp webXml) {
            try {
                addAll(webXml.getServiceRef());
            }
            catch (VersionNotSupportedException ex) {
            }
        }

        @Override
        void addItems(WebFragment webFragment) {
            try {
                addAll(webFragment.getServiceRef());
            }
            catch (VersionNotSupportedException ex) {
            }
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ServiceRef[] refs = CommonAnnotationHelper.getServiceRefs(annotationHelpers.getHelper());
            for (ServiceRef r : refs) {
                res.add(r);
            }
        }
    }

    // -------------------------------------------------------------------------
    private static class EjbLocalRefsEngine extends MergeEngine<EjbLocalRef> {
        @Override
        void addItems(WebApp webXml) {
            addAll(webXml.getEjbLocalRef());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addAll(webFragment.getEjbLocalRef());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            List<EjbLocalRef> l = new ArrayList<EjbLocalRef>();
            EjbRefHelper.setEjbRefs(annotationHelpers.getHelper(), null, l);
            res.addAll(l);
        }
    }

    // -------------------------------------------------------------------------
    private static class EjbRefsEngine extends MergeEngine<EjbRef> {
        @Override
        void addItems(WebApp webXml) {
            addAll(webXml.getEjbRef());
        }

        @Override
        void addItems(WebFragment webFragment) {
            addAll(webFragment.getEjbRef());
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            List<EjbRef> l = new ArrayList<EjbRef>();
            EjbRefHelper.setEjbRefs(annotationHelpers.getHelper(), l, null);
            res.addAll(l);
        }
    }

}
