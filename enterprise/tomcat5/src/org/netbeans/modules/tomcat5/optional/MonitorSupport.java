/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tomcat5.optional;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import org.openide.modules.InstalledFileLocator;
import org.xml.sax.SAXException;

import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/** Monitor enabling/disabling utilities for Tomcat 5.
 *
 * @author Milan.Kuchtiak@sun.com, Petr Jiricka
 */
public class MonitorSupport {

    private static final Logger LOGGER = Logger.getLogger(MonitorSupport.class.getName());
    
    // monitor module enable status data
    public static final String MONITOR_ENABLED_PROPERTY_NAME = "monitor_enabled"; // NOI18N
    private static final String MONITOR_MODULE_NAME="org.netbeans.modules.web.monitor"; //NOI18N    
    private static ModuleInfo httpMonitorInfo;
    private static ModuleSpy monitorSpy;
    private static Lookup.Result res;
    private static MonitorInfoListener monitorInfoListener;
    private static MonitorLookupListener monitorLookupListener;
    
    // data for declaration in web.xml
    private static final String MONITOR_FILTER_NAME  = "HTTPMonitorFilter"; //NOI18N
    private static final String MONITOR_FILTER_CLASS = "org.netbeans.modules.web.monitor.server.MonitorFilter"; //NOI18N
    private static final String MONITOR_FILTER_PATTERN = "/*"; //NOI18N
    private static final String MONITOR_INTERNALPORT_PARAM_NAME = "netbeans.monitor.ide"; //NOI18N
    
    public static void setMonitorFlag(String managerURL, boolean enable) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(managerURL);
        ip.setProperty(MONITOR_ENABLED_PROPERTY_NAME, Boolean.toString(enable));
    }
    
    public static boolean getMonitorFlag(String managerURL) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(managerURL);
        String prop = ip.getProperty(MONITOR_ENABLED_PROPERTY_NAME);
        return (prop == null) ? true : Boolean.valueOf(prop);
    }
    
    public static void setMonitorFlag(TomcatManager tm, boolean enable) {
        setMonitorFlag(tm.getUri(), enable);
    }
    
    public static boolean getMonitorFlag(TomcatManager tm) {
        return getMonitorFlag(tm.getUri());
    }
    
    public static void synchronizeMonitorWithFlag(TomcatManager tm, boolean alsoSetPort) throws IOException, SAXException {
        String url = tm.getUri();
        boolean monitorFlag = getMonitorFlag(url);
        boolean monitorModuleAvailable = isMonitorEnabled();
        boolean shouldInstall = monitorModuleAvailable && monitorFlag;
        
        // find the web.xml file
        File webXML = getDefaultWebXML(tm);
        if (webXML == null) {
            Logger.getLogger(MonitorSupport.class.getName()).log(Level.INFO, null, new Exception(url));
            return;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            Logger.getLogger(MonitorSupport.class.getName()).log(Level.INFO, null, new Exception(url));
            return;
        }
        boolean needsSave = false;
        boolean result;
        if (shouldInstall) {
            addMonitorJars(tm);
            result = changeFilterMonitor(webApp, true);
            needsSave = needsSave || result;
            if (alsoSetPort) {                  
                result = specifyFilterPortParameter(webApp);
                needsSave = needsSave || result;
            }
        }
        else {                               
            result = changeFilterMonitor(webApp, false);
            needsSave = needsSave || result; 
        }
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
    
    private static void addMonitorJars(TomcatManager tm) throws IOException {
        // getting Tomcat4.0 Directory
        File instDir = tm.getTomcatProperties().getCatalinaHome();
        if (instDir==null) {
            return;
        }
        File libFolder = tm.getTomcatProperties().getMonitorLibFolder();
        copyFromIDEInstToDir("modules/ext/org-netbeans-modules-web-httpmonitor.jar", new File(libFolder, "org-netbeans-modules-web-httpmonitor.jar"));  // NOI18N
        copyFromIDEInstToDir("modules/org-netbeans-modules-schema2beans.jar", new File(libFolder, "org-netbeans-modules-schema2beans.jar")); // NOI18N
        
        //copyFromIDEInstToDir("modules/ext/monitor-valve.jar", instDir, "server/lib/monitor-valve.jar"); // NOI18N
    }
    
    private static boolean changeFilterMonitor(WebApp webApp,boolean full) {
        boolean filterWasChanged=false;
        if (full) { // adding monitor filter/filter-mapping element
            //if (tomcat.isMonitorEnabled()) {
            boolean isFilter=false;
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isFilter=true;
                    break;
                }
            }
            if (!isFilter) {
                try {
                    Filter filter = (Filter)webApp.createBean("Filter"); //NOI18N
                    filter.setFilterName(MONITOR_FILTER_NAME);
                    filter.setFilterClass(MONITOR_FILTER_CLASS);
/*                    InitParam initParam = (InitParam)filter.createBean("InitParam"); //NOI18N
                    initParam.setParamName(MONITOR_INIT_PARAM_NAME);
                    initParam.setParamValue(MONITOR_INIT_PARAM_VALUE);
                    filter.addInitParam(initParam);*/
                    webApp.addFilter(filter);
                    filterWasChanged=true;
                }catch (ClassNotFoundException ex) {}
            }
            
            boolean isMapping=false;
            FilterMapping[] maps = webApp.getFilterMapping();
            for(int i=0;i<maps.length;i++) {
                if (maps[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isMapping=true;
                    break;
                }
            }
            if (!isMapping) {
                try {
                    FilterMapping filterMapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                    
                    // setting the dispatcher values even for Servlet2.3 web.xml
                    String[] dispatcher = new String[] {"REQUEST","FORWARD","INCLUDE","ERROR"}; //NOI18N
                    try {
                        filterMapping.setDispatcher(dispatcher);
                    } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                        ((BaseBean)filterMapping).createProperty("dispatcher", // NOI18N
                            "Dispatcher", // NOI18N
                            Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
                            java.lang.String.class);
                        ((BaseBean)filterMapping).setValue("Dispatcher",dispatcher); // NOI18N
                    }
                    
                    filterMapping.setFilterName(MONITOR_FILTER_NAME);
                    filterMapping.setUrlPattern(MONITOR_FILTER_PATTERN);
                    webApp.addFilterMapping(filterMapping);
                    filterWasChanged=true;
                } catch (ClassNotFoundException ex) {}
            }
            //}
        } else { // removing monitor filter/filter-mapping element
            FilterMapping[] maps = webApp.getFilterMapping();
            for(int i=0;i<maps.length;i++) {
                
                if (maps[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    webApp.removeFilterMapping(maps[i]);
                    filterWasChanged=true;
                    break;
                }
            }
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    webApp.removeFilter(filters[i]);
                    filterWasChanged=true;
                    break;
                }
            }
        }
        return filterWasChanged;
    }
    
    /** Finds a file inside the IDE installation, given a slash-separated
     * path relative to the IDE installation. Takes into account the fact that
     * modules may have been installed by Autoupdate, and reside in the user
     * home directory.
     * @param instRelPath file path relative to the inst dir, delimited by '/'
     * @return file containing the file, or null if it does not exist.
     */
    private static File findInstallationFile(String instRelPath) {
        return InstalledFileLocator.getDefault().locate(instRelPath, null, false);
    }
    
    private static void copyFromIDEInstToDir(String sourceRelPath, File targetFile) throws IOException {
        File sourceFile = findInstallationFile(sourceRelPath);
        if (sourceFile != null && sourceFile.exists()) {
            if (!targetFile.exists() 
                || sourceFile.length() != targetFile.length()) {
                copy(sourceFile,targetFile);
            }
        }
    }
    
    private static void copy(File file1, File file2) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file1));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file2))) {
            int b;
            while((b=bis.read())!=-1) {
                bos.write(b);
            }
        }
    }
    
    /** Inserts or and updates in the Monitor Filter element the parameter
     *  which tells the Monitor the number of the internal port,
     *  depending on whether the integration mode is full or minimal
     *  @param webApp deployment descriptor in which to do the changes
     *  @return true if the default deployment descriptor was modified
     */
    private static boolean specifyFilterPortParameter(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        Filter myFilter = null;
        for(int i=0; i<filters.length; i++) {
            if (MONITOR_FILTER_NAME.equals(filters[i].getFilterName())) {
                myFilter = filters[i];
                break;
            }
        }
        // see if we found it
        if (myFilter == null) {
            return false;
        }
        
        // look for the parameter
        InitParam[] params = myFilter.getInitParam();
        InitParam myParam = null;
        for(int i=0; i<params.length; i++) {
            if (MONITOR_INTERNALPORT_PARAM_NAME.equals(params[i].getParamName())) {
                myParam = params[i];
                break;
            }
        }
        
        // host name acts as the parameter name
        String correctParamValue = getLocalHost() + ":" + getInternalServerPort(); // NOI18N
        
        // insert or correct the param
        if (myParam == null) {
            // add it
            try {
                InitParam init = (InitParam)myFilter.createBean("InitParam"); //NOI18N
                init.setParamName(MONITOR_INTERNALPORT_PARAM_NAME);
                init.setParamValue(correctParamValue);
                myFilter.addInitParam(init);
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return true;
        }
        else {
            // check whether the value is correct
            if (correctParamValue.equals(myParam.getParamValue())) {
                // no need to change
                return false;
            }
            else {
                // change
                myParam.setParamValue(correctParamValue);
                return true;
            }
        }
        
    } // end of specifyFilterPortParameter
    
    public static String getLocalHost() {
        // just return 127.0.0.1, other values don't seem to work reliably
        return "127.0.0.1"; // NOI18N
        /**
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "127.0.0.1"; // NOI18N
        }
        */
    }
    
    private static URL getSampleHTTPServerURL() {
	    FileObject fo = FileUtil.getConfigFile("HTTPServer_DUMMY");
	    if (fo == null) {
	        return null;
	    }
	    URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
	    return u;
    }

    private static String getInternalServerPort() {
        //URL u = HttpServer.getRepositoryRoot();
        URL u = getSampleHTTPServerURL();
        if (u != null) {
            return "" + u.getPort(); // NOI18N
        }
        else {
            return "8082"; // NOI18N
        }
    }
    
/*    private void manageConfiguration(){
        Tomcat40DataObject tdo = getTomcatDataObject();
        boolean monitorEnabled = Tomcat40WebServer.getServer().isMonitorEnabled();
        boolean ideMode = tdo.isIdeMode();
        FileObject fo = tdo.getPrimaryFile();
        Boolean monitorFilter = (Boolean)fo.getAttribute(Tomcat40DataObject.MONITOR_FILTER_ATTRIBUTE);
        if (ideMode) {
            
            // add compilation stuff
            tdo.addCompilationJar();
            
            if (monitorEnabled) {
                if (!Boolean.TRUE.equals(monitorFilter)) {
                    // monitor filter need to be added
                    DDDataObject dd = tdo.getDD();
                    if (dd==null) return;
                    WebApp webApp = dd.getWebApp();
                    if (webApp==null) return;
                    if (tdo.changeFilterMonitor(webApp, true)) dd.setNodeDirty(true);
                }
                // also need to add the monitor jars
                // all this code should be redesigned, so the following line should only be consodered temporary
                tdo.addMonitorJars();
            }
            if (!monitorEnabled && !Boolean.FALSE.equals(monitorFilter)) {
                // monitor filter need to be deleted
                DDDataObject dd = tdo.getDD();
                if (dd==null) return;
                WebApp webApp = dd.getWebApp();
                if (webApp==null) return;
                if (tdo.changeFilterMonitor(webApp, false)) dd.setNodeDirty(true);
                tdo.removeAllMonitorValves();
            }
        }
        
        // pointbase stuff
        tdo.addPointbaseJar();
        
        DDDataObject dd = tdo.getDD();
        if (dd==null) return;
        WebApp webApp = dd.getWebApp();
        if (webApp==null) return;
        if (tdo.specifyIDEServletParameter(webApp)) {
            dd.setNodeDirty(true);
        }
        if (tdo.specifyFilterPortParameter(webApp)) {
            dd.setNodeDirty(true);
        }
    }
*/    
    
    private static void startModuleSpy (final ModuleSpy spy) {
        // trying to hang a listener on monitor module 
        res = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class));
        java.util.Iterator it = res.allInstances ().iterator ();
        final String moduleId = spy.getModuleId();        
        boolean found = false;
        while (it.hasNext ()) {
            org.openide.modules.ModuleInfo mi = (ModuleInfo)it.next ();
            if (mi.getCodeName ().startsWith(moduleId)) {
                httpMonitorInfo=mi;
                spy.setEnabled(mi.isEnabled());
                monitorInfoListener = new MonitorInfoListener(spy);
                httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
                found=true;
                break;
            }
        }
        // hanging a listener to the lookup result
        monitorLookupListener = new MonitorLookupListener(spy,httpMonitorInfo);
        res.addLookupListener(monitorLookupListener); 
    }
    
    private static class ModuleSpy {
        private boolean enabled;
        private String moduleId;
        
        public ModuleSpy (String moduleId) {
            this.moduleId=moduleId;
        }
        public void setModuleId(String moduleId){
            this.moduleId=moduleId;
        }
        public void setEnabled(boolean enabled){
            this.enabled=enabled;
        }
        public boolean isEnabled(){
            return enabled;
        }
        public String getModuleId(){
            return moduleId;
        }
    }
    
    static synchronized boolean isMonitorEnabled(){
        if (monitorSpy==null) {
            monitorSpy = new ModuleSpy(MONITOR_MODULE_NAME);
            startModuleSpy(monitorSpy);
        }
        return monitorSpy.isEnabled();
    }
    
    // PENDING - who should call this?
    void removeListeners() {
        if (httpMonitorInfo!=null) {
            httpMonitorInfo.removePropertyChangeListener(monitorInfoListener);
        }
        if (res!=null) {
            res.removeLookupListener(monitorLookupListener);
        }
    }
    
    private static class MonitorInfoListener implements java.beans.PropertyChangeListener {
        ModuleSpy spy;
        MonitorInfoListener(ModuleSpy spy) {
            this.spy=spy;
        }
        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("enabled")){ // NOI18N
                spy.setEnabled(((Boolean)evt.getNewValue()));
            }            
        }  
    }
    
    private static class MonitorLookupListener implements LookupListener {
        
        ModuleSpy spy;
        ModuleInfo httpMonitorInfo;
        MonitorLookupListener(ModuleSpy spy, ModuleInfo httpMonitorInfo) {
            this.spy=spy;
            this.httpMonitorInfo=httpMonitorInfo;
        }
        
        @Override
        public void resultChanged(LookupEvent lookupEvent) {
            java.util.Iterator it = res.allInstances ().iterator ();
            boolean moduleFound=false;
            while (it.hasNext ()) {
                ModuleInfo mi = (ModuleInfo)it.next ();
                if (mi.getCodeName ().startsWith(spy.getModuleId())) {
                    spy.setEnabled(mi.isEnabled());
                    if (httpMonitorInfo==null) {
                        httpMonitorInfo=mi;                        
                        monitorInfoListener = new MonitorInfoListener(spy);
                        httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
                    }
                    moduleFound=true;
                    break;
                }
            }
            if (!moduleFound) {
                if (httpMonitorInfo!=null) {
                    httpMonitorInfo.removePropertyChangeListener(monitorInfoListener);
                    httpMonitorInfo=null;
                    spy.setEnabled(false);
                }
            }            
        }
        
    }
}
