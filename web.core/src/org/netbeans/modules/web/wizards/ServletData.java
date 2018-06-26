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
package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

// PENDING - it would be better to have a FilterData which extends
// ServletData, and keep the filter specific code in that class. 
/** 
 * Deployment data validator object for servlets. 
 * @author ana.von.klopp@sun.com
 */
class ServletData extends DeployData {

    private static final Logger LOG = Logger.getLogger(ServletData.class.getName());
    private static final Pattern VALID_URI_PATTERN = Pattern.compile("[-_.!~*'();/?:@&=+$,a-zA-Z0-9]+"); // NOI18N
    private String errorMessage = null;
    private String name = null;
    // These are URL mappings - they're used by both Servlets and Filters
    private String[] urlMappings = null;
    // These are mappings to servlet names - used by Filters only
    private List<FilterMappingData> filterMappings = null;
    private String[][] initparams = null;
    private boolean paramOK = true;
    private String duplicitParam = null;
    FileType fileType = null;

    ServletData(FileType fileType) {
        this.fileType = fileType;
    }

    String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    void setName(String name) {
        if (name != this.name) {
            if (fileType == FileType.FILTER) {
                updateFilterMappings(getName(), name);
            }
            this.name = name;
        }
    }
    
    boolean canCreate(TemplateWizard wizard){
        if ( webApp == null  ){
            // This case is considered as normal in other cases. So I keep it also as valid.
            return true;
        }
        if ( webApp.getStatus() == WebApp.STATE_INVALID_OLD_VERSION ){
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(ServletData.class, "MSG_OldVersion"));
            return false;
        } else if ( webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE ){
            if ( webApp.getVersion() == null ){
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServletData.class, "MSG_UnuspportedVersion"));
            }
            else {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServletData.class, "MSG_InvalidWebXml"));
            }
            return false;
        }
        return true;
    }

    String[] getServletNames() {
        if (webApp == null) {
            return new String[0];
        }
        Servlet[] ss = webApp.getServlet();
        String[] names = new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            try {
                names[i] = ss[i].getServletName();
            } catch (Exception e) {
                LOG.log(Level.FINE, "error", e);
                names[i] = "";
            }
        }
        return names;
    }

    private List<String> getUrlPatterns() {
        if (webApp == null) {
            return new ArrayList<String>();
        }
        ServletMapping[] maps = webApp.getServletMapping();
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < maps.length; i++) {
            l.addAll(Arrays.asList(((ServletMapping25)maps[i]).getUrlPatterns()));
        }
        return l;
    }

    List<FilterMappingData> getFilterMappings() {
        if (filterMappings != null) {
            return filterMappings;
        }
        if (webApp == null) {
            return new ArrayList<FilterMappingData>();
        }

        LOG.finer("Creating the filter mapping list"); //NOI18N
        FilterMapping[] fm = webApp.getFilterMapping();
        LOG.finer("Order of mappings according to DD APIs"); //NOI18N
        for (int i = 0; i < fm.length; ++i) {
            LOG.finer("Servlet name: " + fm[i].getFilterName()); //NOI18N
        }
        filterMappings = new ArrayList<FilterMappingData>();
        filterMappings.add(new FilterMappingData(getName()));

        String string;
        String[] d = null;
        FilterMappingData fmd;
        FilterMappingData.Dispatcher[] dispatchList;

        for (int i = 0; i < fm.length; i++) {
            fmd = new FilterMappingData();
            fmd.setName(fm[i].getFilterName());

            string = fm[i].getUrlPattern();
            if (string == null || string.length() == 0) {
                fmd.setType(FilterMappingData.Type.SERVLET);
                fmd.setPattern(fm[i].getServletName());
            } else {
                fmd.setType(FilterMappingData.Type.URL);
                fmd.setPattern(string);
            }

            try {
                if (fm[i].sizeDispatcher() == 0) {
                    filterMappings.add(fmd);
                    continue;
                }
            } catch (Exception ex) {
                LOG.log(Level.FINE, "error", ex);
                // Not supported
                filterMappings.add(fmd);
                continue;
            }

            try {
                d = fm[i].getDispatcher();
            } catch (Exception ex) {
                LOG.log(Level.FINE, "error", ex);

            // PENDING ...
            // Servlet 2.3
            }
            if (d == null) {
                dispatchList = new FilterMappingData.Dispatcher[0];
            } else {
                dispatchList = new FilterMappingData.Dispatcher[d.length];
                for (int j = 0; j < d.length; ++j) {
                    dispatchList[j] = FilterMappingData.Dispatcher.findDispatcher(d[j]);
                    LOG.finer("Dispatch: " + dispatchList[j]);//NOI18N
                }
            }
            fmd.setDispatcher(dispatchList);
            filterMappings.add(fmd);
        }
        return filterMappings;
    }

    void setFilterMappings(List<FilterMappingData> fmds) {
        filterMappings = fmds;
    }

    private void updateFilterMappings(String oldName, String newName) {
        Iterator<FilterMappingData> i = getFilterMappings().iterator();
        while (i.hasNext()) {
            FilterMappingData fmd = i.next();
            if (fmd.getName().equals(oldName)) {
                fmd.setName(newName);
            }
        }
    }

    private boolean isNameUnique() {
        if (webApp == null) {
            return true;
        }
        Servlet[] ss = webApp.getServlet();
        for (int i = 0; i < ss.length; i++) {
            if (name.equals(ss[i].getServletName())) {
                return false;
            }
        }

        Filter[] ff = webApp.getFilter();
        for (int i = 0; i < ff.length; i++) {
            if (name.equals(ff[i].getFilterName())) {
                return false;
            }
        }
        return true;
    }

    String[] getUrlMappings() {
        if (urlMappings == null) {
            return new String[0];
        }
        return urlMappings;
    }

    String createDDServletName(String className) {
        if (webApp == null) {
            return null;
        }
        String result = className;
        Servlet servlet = (Servlet) webApp.findBeanByName("Servlet", "ServletName", result); //NOI18N
        while (servlet != null) {
            result = findNextId(result);
            servlet = (Servlet) webApp.findBeanByName("Servlet", "ServletName", result); //NOI18N
        }
        setName(result);
        return result;
    }

    void createDDServletMapping(String servletName) {
        if (webApp == null) {
            return;
        }
        String result = getRFC2396URI("/" + servletName);
        ServletMapping mapping = (ServletMapping) webApp.findBeanByName("ServletMapping", "UrlPattern", result); //NOI18N
        while (mapping != null) {
            result = findNextId(result);
            mapping = (ServletMapping) webApp.findBeanByName("ServletMapping", "UrlPattern", result); //NOI18N
        }
        urlMappings = new String[]{result};
    }

    /** Compute the next proper value for the id
     */
    private String findNextId(String id) {
        char ch = id.charAt(id.length() - 1);
        if (Character.isDigit(ch)) {
            String lastDigit = id.substring(id.length() - 1);
            int num = new Integer(lastDigit).intValue() + 1;
            return id.substring(0, id.length() - 1) + num;
        } else {
            return id + "_1"; //NOI18N
        }
    }

    String getUrlMappingsAsString() {
        if (urlMappings == null || urlMappings.length == 0) {
            return ""; //NOI18N
        }
        StringBuffer buf = new StringBuffer();
        int index = 0;
        while (index < urlMappings.length - 1) {
            buf.append(urlMappings[index]);
            buf.append(", "); //NOI18N
            index++;
        }

        buf.append(urlMappings[index]);
        return buf.toString();
    }

    void parseUrlMappingString(String raw) {
        urlMappings = null;
        StringTokenizer st = new StringTokenizer(raw, ",");
        List<String> list = new ArrayList<String>();

        while (st.hasMoreTokens()) {
            String mapping = st.nextToken().trim();
            if (mapping.length() == 0) {
                continue;
            }
            list.add(mapping);
        }

        urlMappings = new String[list.size()];
        list.toArray(urlMappings);
    }

    String[][] getInitParams() {
        if (initparams == null) {
            return new String[0][2];
        }
        return initparams;
    }

    void setInitParams(String[][] initparams, boolean paramOK, String duplicitParam) {
        this.initparams = initparams;
        this.paramOK = paramOK;
        this.duplicitParam = duplicitParam;
    }

    boolean isValid() {
        errorMessage = "";
        //if (webApp == null) {
        //    return true;
        //}
        //if (!makeEntry()) return true;

        if (getName().length() == 0) {
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_no_name");
            return false;
        }

        if (!isNameUnique()) {
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_name_unique");
            return false;
        }

        if (fileType == FileType.SERVLET) {
            if (!checkMappingsForServlet()) {
                return false;
            }
            if (!checkServletDuplicitMappings()) {
                return false;
            }
        } else if (fileType == FileType.FILTER) {
            if (!checkMappingsForFilter()) {
                return false;
            }
        }

        if (!paramOK) {
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_invalid_param");
            return false;
        }

        if (duplicitParam != null) {
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_duplicit_param", duplicitParam);
            return false;
        }

        return true;
    }

    private boolean checkMappingsForServlet() {
        errorMessage = "";
        String[] mappings = getUrlMappings();
        if (mappings == null || mappings.length == 0) {
            LOG.finer("No URL mappings"); //NOI18N
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_no_mapping");
            return false;
        }
        for (int i = 0; i < mappings.length; i++) {
            String errMessage = checkServletMappig(mappings[i]);
            if (errMessage != null) {
                errorMessage = errMessage;
                return false;
            }
        }
        return true;
    }

    private boolean checkServletDuplicitMappings() {
        errorMessage = "";
        String[] newMappings = getUrlMappings();
        List<String> urlPatterns = getUrlPatterns();
        for (int i = 0; i < newMappings.length; i++) {
            Iterator<String> it = urlPatterns.iterator();
            while (it.hasNext()) {
                String urlPattern = it.next();
                if (newMappings[i].equals(urlPattern)) {
                    LOG.finer("Duplicit URL mappings"); //NOI18N
                    errorMessage = NbBundle.getMessage(ServletData.class, "MSG_url_pattern_unique");
                    return false;
                }
            }
            // new Url Patterns need to be compare to each other 
            urlPatterns.add(newMappings[i]);
        }
        return true;
    }

    private boolean checkMappingsForFilter() {
        errorMessage = "";
        if (filterMappings == null || filterMappings.size() == 0) {
            LOG.finer("No mappings"); //NOI18N
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_no_mapping");
            return false;
        }
        Iterator<FilterMappingData> i = getFilterMappings().iterator();
        boolean found = false;
        while (i.hasNext()) {
            FilterMappingData fmd = i.next();
            if (fmd.getName().equals(getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            errorMessage = NbBundle.getMessage(ServletData.class, "MSG_no_mapping");
            return false;
        }
        return true;
    }

    void createDDEntries() {
        if (webApp == null) {
            return;
        }

        if (fileType == FileType.SERVLET) {
            boolean added = addServlet();
            if (added) {
                addUrlMappings();
                try {
                    writeChanges();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error", ex);
                }
            }
        } else if (fileType == FileType.FILTER) {
            boolean added = addFilter();
            if (added) {
                addFilterMappings();
                try {
                    writeChanges();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "error", ex);
                }
            }
        }
    }

    private boolean addServlet() {
        if (webApp == null) {
            return false;
        }
        Servlet s;
        try {
            s = (Servlet) webApp.createBean("Servlet"); //NOI18N
        } catch (ClassNotFoundException cnfe) {
            LOG.log(Level.FINE, "servlet creation error", cnfe);
            return false;
        }

        s.setServletName(name);
        s.setServletClass(className);

        int numInitParams = getInitParams().length;
        for (int i = 0; i < numInitParams; ++i) {
            InitParam param;
            try {
                param = (InitParam) s.createBean("InitParam"); //NOI18N
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.FINE, "servlet init parameter creation error", cnfe);
                continue;
            }

            param.setParamName(initparams[i][0]);
            param.setParamValue(initparams[i][1]);
            s.addInitParam(param);
        }

        webApp.addServlet(s);
        return true;
    }

    private boolean addFilter() {
        if (webApp == null) {
            return false;
        }
        Filter f;
        try {
            f = (Filter) webApp.createBean("Filter"); //NOI18N
        } catch (ClassNotFoundException cnfe) {
            LOG.log(Level.FINE, "filter creation error", cnfe);
            return false;
        }

        f.setFilterName(name);
        f.setFilterClass(className);

        int numInitParams = getInitParams().length;
        for (int i = 0; i < numInitParams; ++i) {
            InitParam param;
            try {
                param = (InitParam) f.createBean("InitParam"); //NOI18N
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.FINE, "filter init parameter creation error", cnfe);
                continue;
            }

            param.setParamName(initparams[i][0]);
            param.setParamValue(initparams[i][1]);
            f.addInitParam(param);
        }

        webApp.addFilter(f);
        return true;
    }

    private void addUrlMappings() {
        if (webApp == null) {
            return;
        }
        ServletMapping25 m;
        try {
            m = (ServletMapping25) webApp.createBean("ServletMapping"); //NOI18N
        } catch (ClassNotFoundException cnfe) {
            LOG.log(Level.FINE, "error", cnfe);
            return;
        }
        m.setServletName(name);
        m.setUrlPatterns(urlMappings);
        webApp.addServletMapping(m);
    }

    private void addFilterMappings() {
        if (webApp == null) {
            return;
        }

        // filterMappings cannot be null, or of size zero
        int numFilterMappings = filterMappings.size();
        Iterator<FilterMappingData> iterator = filterMappings.iterator();

        FilterMapping[] fm = new FilterMapping[numFilterMappings];

        for (int i = 0; i < numFilterMappings; ++i) {
            FilterMappingData fmd = iterator.next();

            try {
                fm[i] = (FilterMapping) webApp.createBean("FilterMapping"); //NOI18N
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.FINE, "filter mapping creation error", cnfe);
                return;
            }

            fm[i].setFilterName(fmd.getName());
            if (fmd.getType() == FilterMappingData.Type.URL) {
                fm[i].setUrlPattern(fmd.getPattern());
            } else {
                fm[i].setServletName(fmd.getPattern());
            }

            int length = fmd.getDispatcher().length;
            if (length == 0) {
                LOG.finer("No dispatcher, continue"); //NOI18N
                continue;
            }

            String[] s = new String[length];
            FilterMappingData.Dispatcher[] d = fmd.getDispatcher();
            for (int j = 0; j < length; ++j) {
                s[j] = d[j].toString();
            }
            try {
                fm[i].setDispatcher(s);
            } catch (Exception e) {
                LOG.log(Level.FINE, "Failed to set dispatcher", e);
            // do nothing, wrong version
            }
        }
        webApp.setFilterMapping(fm);
    }

    String getErrorMessage() {
        return errorMessage;
    }

    private String checkServletMappig(String uri) {
        if (!uri.matches("[\\*/].*")) { //NOI18N
            return NbBundle.getMessage(ServletData.class, "MSG_WrongUriStart");
        } else if (uri.length() > 1 && uri.endsWith("/")) {
            return NbBundle.getMessage(ServletData.class, "MSG_WrongUriEnd");
        } else if (uri.matches(".*\\*.*\\*.*")) { //NOI18N
            return NbBundle.getMessage(ServletData.class, "MSG_TwoAsterisks");
        } else if (uri.matches("..*\\*..*")) { //NOI18N
            return NbBundle.getMessage(ServletData.class, "MSG_AsteriskInTheMiddle");
        } else if (uri.length() > 1 && !isRFC2396URI(uri.substring(1))) {
            return NbBundle.getMessage(ServletData.class, "MSG_WrongUri");
        }
        return null;
    }

    /**
     * Get the valid URI according to <a href="http://www.ietf.org/rfc/rfc2396.txt">the RFC 2396</a>.
     * @param uri URI to be checked.
     * @return valid URI according to <a href="http://www.ietf.org/rfc/rfc2396.txt">the RFC 2396</a>.
     */
    static String getRFC2396URI(String uri) {
        if (isRFC2396URI(uri)) {
            return uri;
        }
        StringBuilder sb = new StringBuilder(uri);
        for (int i = 0; i < sb.length(); i++) {
            if (!isRFC2396URI(sb.substring(i, i + 1))) {
                sb.replace(i, i + 1, "_"); // NOI18N
            }
        }
        return sb.toString();
    }

    private static boolean isRFC2396URI(String uri) {
        return VALID_URI_PATTERN.matcher(uri).matches();
    }
}

