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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author mkuchtiak
 */
public final class DDUtils {
    
    private static final Logger LOG = Logger.getLogger(DDUtils.class.getName());

    private DDUtils() {
    }

    public static String[] getUrlPatterns(WebApp webApp, Servlet servlet) {
        if (servlet.getServletName()==null) return new String[]{};
        ServletMapping[] mapping = webApp.getServletMapping();
        List<String> maps = new LinkedList<String>();
        for (ServletMapping sm : mapping) {
            if (servlet.getServletName().equals(sm.getServletName())) {
                maps.addAll(getUrlPatterns((ServletMapping25)sm));
            }
        }
        String[] urlPatterns = new String[maps.size()];
        maps.toArray(urlPatterns);
        return urlPatterns;
    }
    
    public static String[] getUrlPatterns(WebApp webApp, Filter filter) {
        if (filter.getFilterName()==null) return new String[]{};
        FilterMapping[] mapping = webApp.getFilterMapping();
        List maps = new ArrayList();
        for (int i=0;i<mapping.length;i++) {
            if (filter.getFilterName().equals(mapping[i].getFilterName())) {
                String urlPattern = mapping[i].getUrlPattern();
                if (urlPattern!=null) maps.add(urlPattern);
                else {
                    String servletName = mapping[i].getServletName();
                    if (servletName!=null) maps.add(servletName);
                }
            }
        }
        String[] urlPatterns = new String[maps.size()];
        maps.toArray(urlPatterns);
        return urlPatterns;
    }
    
    public static String[] getStringArray(String text) {
        StringTokenizer tok = new StringTokenizer(text, ",");
        Set set = new HashSet();
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().trim();
            if (token.length()>0) set.add(token);
        }
        String[] stringArray = new String[set.size()];
        set.toArray(stringArray);
        return stringArray;
    }
    
    public static boolean isServletMapping(WebApp webApp, String urlPattern) {
        if (isWebApp25(webApp)) {
            for (ServletMapping mapping : webApp.getServletMapping()) {
                if (getUrlPatterns((ServletMapping25)mapping).contains(urlPattern)) {
                    return true;
                }
            }
            return false;
        }
        return webApp.findBeanByName("ServletMapping","UrlPattern",urlPattern)!=null;
    }
    
    public static boolean isServletMapping(WebApp webApp, Servlet servlet, String urlPattern) {
        ServletMapping[] maps = webApp.getServletMapping();
        String servletName = servlet.getServletName();
        if (servletName != null) {
            for (ServletMapping sm : maps) {
                if (!servletName.equals(sm.getServletName()) && getUrlPatterns((ServletMapping25)sm).contains(urlPattern)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String urlPatternList(String[] urlPatterns) {
        if (urlPatterns == null) {
            return ""; // NOI18N
        }
        StringBuilder buf = new StringBuilder();
        for (int i=0;i<urlPatterns.length;i++) {
            if (i>0) buf.append(", "); //NOI18N
            buf.append(urlPatterns[i]);
        }
        return buf.toString();
    }
    
    public static void addServletMappings(WebApp webApp, Servlet servlet, String[] urlPatterns) {
        String servletName = servlet.getServletName();
        try {
            ServletMapping25 mapping = (ServletMapping25) webApp.createBean("ServletMapping"); // NOI18
            mapping.setServletName(servletName);
            mapping.setUrlPatterns(urlPatterns);
            webApp.addServletMapping(mapping);
        } catch (ClassNotFoundException ex){}
    }

    public static void setServletMappings(WebApp webApp, Servlet servlet, String[] urlPatterns) {
        setServletMappings25(webApp, servlet, urlPatterns);
    }
    
    public static ServletMapping[] getServletMappings(WebApp webApp, Servlet servlet) {
        List maps = getServletMappingList(webApp,servlet);
        ServletMapping[] newMappings = new ServletMapping[maps.size()];
        maps.toArray(newMappings);
        return newMappings;
    }
    
    public static FilterMapping[] getFilterMappings(WebApp webApp, Filter filter) {
        List maps = getFilterMappingList(webApp,filter);
        FilterMapping[] newMappings = new FilterMapping[maps.size()];
        maps.toArray(newMappings);
        return newMappings;
    }
    
    /**
     * @return filter mappings that refer to given <code>servlet</code>. 
     */
    public static FilterMapping[] getFilterMappings(WebApp webApp, Servlet servlet) {
        List maps = new ArrayList();
        if (servlet == null){
            return new FilterMapping[0];
        }
        FilterMapping[] mapping = webApp.getFilterMapping();
        for (int i=0;i<mapping.length;i++) {
            FilterMapping fm = mapping[i];
            if (fm.getServletName() != null && fm.getServletName().equals(servlet.getServletName())){
                maps.add(fm);
            }
        }
        return (FilterMapping[]) maps.toArray(new FilterMapping[maps.size()]);
    }
    
    private static List<ServletMapping> getServletMappingList(WebApp webApp, Servlet servlet) {
        String servletName = servlet.getServletName();
        List<ServletMapping> maps = new LinkedList<ServletMapping>();
        if (servletName==null) return maps;
        ServletMapping[] mapping = webApp.getServletMapping();
        for (int i=0;i<mapping.length;i++) {
            if (servlet.getServletName().equals(mapping[i].getServletName())) {
                maps.add(mapping[i]);
            }
        }
        return maps;
    }
    
    private static List getFilterMappingList(WebApp webApp, Filter filter) {
        String filterName = filter.getFilterName();
        List maps = new ArrayList();
        if (filterName==null) return maps;
        FilterMapping[] mapping = webApp.getFilterMapping();
        for (int i=0;i<mapping.length;i++) {
            if (filter.getFilterName().equals(mapping[i].getFilterName())) {
                maps.add(mapping[i]);
            }
        }
        return maps;
    }
    
    public static void openEditorFor(DDDataObject dObj, String className) {
        if (className==null || className.length()==0) return;
        try {
            SourceGroup[] sourceGroups =  getJavaSourceGroups(dObj);
            String resource = className.trim().replace('.','/');
            for (int i=0;i<sourceGroups.length;i++) {
                FileObject fo = sourceGroups[i].getRootFolder();
                FileObject target = fo.getFileObject(resource+".java"); //NOI18N
                if (target!=null) {
                    DataObject javaDo = DataObject.find(target);
                    org.openide.cookies.OpenCookie cookie =
                            (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                    if (cookie !=null) {
                        cookie.open();
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
        }
        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
                org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourceNotFound")));
    }
    
    public static void openEditorForSingleFile(DDDataObject dObj, String fileName) {
        if (fileName==null || fileName.length()==0) return;
        FileObject docBase = null;
        try {
            docBase = getDocumentBase(dObj);
        } catch (IOException ex) {
            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
            return;
        }
        if (docBase!=null) {
            FileObject target = docBase.getFileObject(fileName.trim());
            if (target!=null) {
                try {
                    DataObject javaDo = DataObject.find(target);
                    org.openide.cookies.OpenCookie cookie =
                            (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                    if (cookie !=null) {
                        cookie.open();
                        return;
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                }
            }
        }
        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
                org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourceNotFound")));
    }
    
    public static void openEditorForFiles(DDDataObject dObj, StringTokenizer tok) {
        FileObject docBase = null;
        try {
            docBase = getDocumentBase(dObj);
        } catch (IOException ex) {
            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
            return;
        }
        if (!tok.hasMoreTokens()) return;
        boolean found=false;
        if (docBase!=null)
            while (tok.hasMoreTokens()) {
            String resource = tok.nextToken().trim();
            if (resource.length()>0) {
                FileObject target = docBase.getFileObject(resource);
                if (target!=null) {
                    try {
                        DataObject javaDo = DataObject.find(target);
                        org.openide.cookies.OpenCookie cookie =
                                (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                        if (cookie !=null) {
                            cookie.open();
                            found=true;
                        }
                    } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                        LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            }
            }
        if (!found) {
            org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
                    org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourcesNotFound")));
        }
    }
    
    public static SourceGroup[] getJavaSourceGroups(DDDataObject dObj) throws IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    public static SourceGroup[] getDocBaseGroups(DDDataObject dObj) throws IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static FileObject getDocumentBase(DDDataObject dObj) throws IOException {
        WebModule wm = WebModule.getWebModule(dObj.getPrimaryFile());
        if (wm==null) return null;
        return wm.getDocumentBase();
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo) {
        return getResourcePath(groups, fo, '.', false);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator) {
        return getResourcePath(groups, fo, separator, false);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fo)) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    /** removes all filter mappings for given servlet name
     */
    public static void removeServletMappings(WebApp webApp, String servletName) {
        if (servletName==null) return;
        ServletMapping[] oldMaps = webApp.getServletMapping();
        for (int i=0;i<oldMaps.length;i++) {
            if (servletName.equals(oldMaps[i].getServletName())) {
                webApp.removeServletMapping(oldMaps[i]);
            }
        }
    }
    /** removes all filter mappings for given filter name
     * @return Stack of deleted rows
     */
    public static Stack removeFilterMappings(WebApp webApp, String filterName) {
        Stack deletedRows = new Stack();
        if (filterName==null) return deletedRows;
        FilterMapping[] oldMaps = webApp.getFilterMapping();
        for (int i=0;i<oldMaps.length;i++) {
            if (filterName.equals(oldMaps[i].getFilterName())) {
                webApp.removeFilterMapping(oldMaps[i]);
                deletedRows.push(new Integer(i));
            }
        }
        return deletedRows;
    }
    
    /** removes all filter mappings for given servlet name
     */
    public static void removeFilterMappingsForServlet(WebApp webApp, String servletName) {
        if (servletName==null) return;
        FilterMapping[] oldMaps = webApp.getFilterMapping();
        for (int i=0;i<oldMaps.length;i++) {
            if (servletName.equals(oldMaps[i].getServletName())) {
                webApp.removeFilterMapping(oldMaps[i]);
            }
        }
    }
    
    public static String addItem(String text, String newItem, boolean asFirst) {
        String[] stringArray = getStringArray(text);
        List list = new ArrayList();
        if (asFirst) {
            list.add(newItem);
            for (int i=0;i<stringArray.length;i++) {
                if (!newItem.equals(stringArray[i])) list.add(stringArray[i]);
            }
        } else {
            for (int i=0;i<stringArray.length;i++) {
                if (!newItem.equals(stringArray[i])) list.add(stringArray[i]);
            }
            list.add(newItem);
        }
        return getAsString(list);
    }
    
    private static String getAsString(List list) {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<list.size();i++) {
            if (i>0) buf.append(", "); //NOI18N
            buf.append((String)list.get(i));
        }
        return buf.toString();
    }
    
    public static String[] getServletNames(WebApp webApp) {
        Servlet[] allServlets = webApp.getServlet();
        List list = new ArrayList();
        for (int i=0;i<allServlets.length;i++) {
            String servletName = allServlets[i].getServletName();
            if (servletName!=null && !list.contains(allServlets[i])) list.add(servletName);
        }
        String[] names = new String[list.size()];
        list.toArray(names);
        return names;
    }
    
    public static String[] getFilterNames(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        List list = new ArrayList();
        for (int i=0;i<filters.length;i++) {
            String filterName = filters[i].getFilterName();
            if (filterName!=null && !list.contains(filters[i])) list.add(filterName);
        }
        String[] names = new String[list.size()];
        list.toArray(names);
        return names;
    }
    
    public static String checkServletMappig(String uri) {
        if (!uri.matches("[\\*/].*")) { //NOI18N
            return NbBundle.getMessage(DDUtils.class,"MSG_WrongUriStart");
        } else if (uri.length()>1  && uri.endsWith("/")) {
            return NbBundle.getMessage(DDUtils.class,"MSG_WrongUriEnd");
        } else if (uri.matches(".*\\*.*\\*.*")) { //NOI18N
            return NbBundle.getMessage(DDUtils.class,"MSG_TwoAsterisks");
        } else if (uri.matches("..*\\*..*")) { //NOI18N
            return NbBundle.getMessage(DDUtils.class,"MSG_AsteriskInTheMiddle");
        }
        return null;
    }

    private static List<String> getUrlPatterns(ServletMapping25 sm) {
        assert sm != null;
        List<String> urlPatterns = new LinkedList<String>();
        String[] patterns = sm.getUrlPatterns();
        if (patterns != null) {
            for (String p : patterns) {
                if (p != null) {
                    urlPatterns.add(p);
                }
            }
        }
        return urlPatterns;
    }

    private static void setServletMappings25(WebApp webApp, Servlet servlet, String[] urlPatterns) {
        final String servletName = servlet.getServletName();
        // first remove _all_ the mappings of the servlet
        for (ServletMapping mapping : getServletMappingList(webApp, servlet)) {
            if (servletName.equals(mapping.getServletName())) {
                webApp.removeServletMapping(mapping);
            }
        }
        // add only one
        addServletMappings(webApp, servlet, urlPatterns);
    }

    private static boolean isWebApp25(WebApp webApp) {
        BigDecimal ver = new BigDecimal(webApp.getVersion());
        return ver.compareTo(new BigDecimal(WebApp.VERSION_2_5)) >= 0;
    }
}
