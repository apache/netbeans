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

package org.netbeans.modules.payara.eecommon.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.xml.sax.SAXException;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

public class HttpMonitorHelper {

    // monitor module enable status data
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
    
    public static boolean synchronizeMonitor(String domainLoc, String domainName, boolean monitorFlag, String... others)  throws FileNotFoundException, IOException, SAXException {
        boolean monitorModuleAvailable = isMonitorEnabled();
        boolean shouldInstall = monitorModuleAvailable && monitorFlag;
        // find the web.xml file
        File webXML = getDefaultWebXML(domainLoc, domainName);
        if (webXML == null) {
            return false;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(""));
            return false;
        }
        boolean needsSave = false;
        boolean needRestart = false;
        boolean result;
        try {
            if (shouldInstall) {
                needRestart = addMonitorJars(domainLoc,domainName,others);
                result = changeFilterMonitor(webApp, true);
                needsSave = needsSave || result;
                result = specifyFilterPortParameter(webApp);
                needsSave = needsSave || result;                
            } else {
                result = changeFilterMonitor(webApp, false);
                needsSave = needsSave || result;
            }
        } catch (ClassNotFoundException cnfe) {
            needsSave = false;
            ErrorManager.getDefault().notify(ErrorManager.ERROR, cnfe);
        }
        if (needsSave) {
            OutputStream os = new FileOutputStream(webXML);
            try {
                webApp.write(os);
            } finally {
                os.close();
            }
        }
        return needRestart;
    }
    
    // Workaround to eliminate Glassfih issue 8609
    // https://glassfish.dev.java.net/issues/show_bug.cgi?id=8609
    // Workaround may be removed when GF issue 8609 is fixed
    // visible for unit testing only
    //
    static File getDefaultWebXML(String domainLoc, String domainName) {
        String loc = domainLoc+"/"+domainName+"/config/default-web.xml"; // NOI18N
        File webXML = new File(loc);

        if (webXML.exists())  {
            String backupLoc = domainLoc+"/"+domainName+"/config/default-web.xml.orig"; // NOI18N
            File backupXml = new File(backupLoc);
            if (!backupXml.exists()) {
                try {
                    copy(webXML,backupXml);
                    createCopyAndUpgrade(backupXml, webXML);
                } catch (FileNotFoundException fnfe) {
                    Logger.getLogger("payara-eecommon").log(Level.WARNING, "This file existed a few milliseconds ago: "+ webXML.getAbsolutePath(), fnfe);
                } catch (IOException ioe) {
                    if (backupXml.exists()) {
                        backupXml.delete();
                        Logger.getLogger("payara-eecommon").log(Level.WARNING,"failed to backup data from "+webXML.getAbsolutePath(), ioe);
                    } else {
                        Logger.getLogger("payara-eecommon").log(Level.WARNING,"failed to create backup file "+backupXml.getAbsolutePath(), ioe);
                    }
                }
            }
            return webXML.exists() ? webXML : null;
        }
        return null;
    }

    // visible for unit testing only
    //
    static void createCopyAndUpgrade(File webXML, File newWebXML) {
        BufferedReader fr= null;
        BufferedWriter fw= null;
        boolean deleteNew = true;
        try {
            fr = new BufferedReader(new InputStreamReader(new FileInputStream(webXML),"ISO-8859-1"));
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newWebXML),"ISO-8859-1"));
            while (true) {
                String line = fr.readLine();
                if (line == null)
                    break;
                if (line.startsWith("<!DOCTYPE")) {
                    while (!line.startsWith("<web-app")) {
                        line = fr.readLine();
                    }
                    fw.write("<web-app version=\"2.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">");
                    fw.newLine();
                }
                else {
                    fw.write(line);
                    fw.newLine();
                }
            }
            deleteNew = false;
            fw.close();
            fw = null;
            fr.close();
            fr = null;
        } catch (FileNotFoundException fnfe) {
            Logger.getLogger("payara-eecommon").log(Level.WARNING, "This file existed a few milliseconds ago: "+ webXML.getAbsolutePath());
        } catch (Exception e) {
            if (null != fw && deleteNew) {
                if (!newWebXML.delete()) {
                    Logger.getLogger("payara-eecommon").log(Level.WARNING, "hack to eliminate GF bug 8609 failed and left bogus file: {0}", newWebXML.getAbsolutePath());
                }
            }
            Logger.getLogger("payara-eecommon").log(Level.WARNING, "hack to eliminate GF bug 8609 failed", e);
        } finally {
            if (null != fw) {
                try {
                    fw.close();
                } catch (IOException ioe) {
                    Logger.getLogger("payara-eecommon").log(Level.INFO, "close of fw failed: "+ newWebXML.getAbsolutePath(), ioe);
                }
            }
            if (null != fr) {
                try {
                    fr.close();
                } catch (IOException ioe) {
                    Logger.getLogger("payara-eecommon").log(Level.INFO, "close of fr failed: "+ webXML.getAbsolutePath(), ioe);                }
            }
        }
    }

    private static boolean addMonitorJars(String domainLoc, String domainName, String... others) throws FileNotFoundException, IOException {
        String loc = domainLoc+"/"+domainName;
        File instDir = new File(loc);
        boolean retVal = copyFromIDEInstToDir("modules/ext/org-netbeans-modules-web-httpmonitor.jar"  , instDir, "lib/org-netbeans-modules-web-httpmonitor.jar");  // NOI18N  
        for (String anOther : others) {
            int lastSlash = anOther.lastIndexOf("/");
            if (lastSlash > -1) {
                String jarName = anOther.substring(lastSlash+1);
                retVal = retVal && copyFromIDEInstToDir(anOther , instDir, "lib/"+jarName);  // NOI18N                  
            }
        }
        return retVal;
    }

    // visible for unit testing
    //
    static boolean changeFilterMonitor(WebApp webApp,boolean full)  throws ClassNotFoundException {
        boolean filterWasChanged=false;
        if (full) { // adding monitor filter/filter-mapping element
            boolean isFilter=false;
            Filter[] filters = webApp.getFilter();
            for(int i=0;i<filters.length;i++) {
                if (filters[i].getFilterName().equals(MONITOR_FILTER_NAME)){
                    isFilter=true;
                    break;
                }
            }
            if (!isFilter) {
                    Filter filter = (Filter)webApp.createBean("Filter"); //NOI18N
                    filter.setFilterName(MONITOR_FILTER_NAME);
                    filter.setFilterClass(MONITOR_FILTER_CLASS);
                    webApp.addFilter(filter);
                    filterWasChanged=true;
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
                    FilterMapping filterMapping = (FilterMapping)webApp.createBean("FilterMapping"); //NOI18N
                    
                    // setting the dispatcher values even for Servlet2.3 web.xml
                    String[] dispatcher = new String[] {"REQUEST","FORWARD","INCLUDE","ERROR"}; //NOI18N
                    try {
                        filterMapping.setDispatcher(dispatcher);
                    } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                        Logger.getLogger("payara-eecommon").log(Level.FINER,"ignorable and ignoring",ex);
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
    
    private static boolean copyFromIDEInstToDir(String sourceRelPath, File copyTo, String targetRelPath) throws FileNotFoundException, IOException {
        File targetFile = findFileUnderBase(copyTo, targetRelPath);
        File sourceFile = findInstallationFile(sourceRelPath);
        if (sourceFile != null && sourceFile.exists()) {
            File targetParent = targetFile.getParentFile();
            if (!targetParent.exists()) {
                targetParent.mkdirs();
            }
            if (!targetFile.exists() || sourceFile.length() != targetFile.length()) {
                copy(sourceFile, targetFile);
                return true;
            }
        }
        return false;
    }
    
    private static void copy(File file1, File file2)  throws FileNotFoundException, IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
             bis = new BufferedInputStream(new FileInputStream(file1));
             bos = new BufferedOutputStream(new FileOutputStream(file2));
            int b;
            while((b=bis.read())!=-1)bos.write(b);
        } finally {
            if (null != bis) {
                try { 
                    bis.close(); 
                } catch (IOException ioe) {
                    Logger.getLogger("payara-eecommon").log(Level.FINEST,"bis", ioe);
                }
            }
            if (null != bos) {
                try { 
                    bos.close(); 
                } catch (IOException ioe) {
                    Logger.getLogger("payara-eecommon").log(Level.FINEST,"bos", ioe);
                }
            }
        }
    }
    
    private static File findFileUnderBase(File base, String fileRelPath) {
        if (fileRelPath.startsWith("/")) { // NOI18N
            fileRelPath = fileRelPath.substring(1);
        }
        fileRelPath = fileRelPath.replace('/', File.separatorChar);
        return new File(base, fileRelPath);
    }

    // visible for unit testing only
    //
    /** Inserts or and updates in the Monitor Filter element the parameter
     *  which tells the Monitor the number of the internal port,
     *  depending on whether the integration mode is full or minimal
     *  @param webApp deployment descriptor in which to do the changes
     *  @return true if the default deployment descriptor was modified
     */
     static boolean specifyFilterPortParameter(WebApp webApp) throws ClassNotFoundException {
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
                InitParam init = (InitParam)myFilter.createBean("InitParam"); //NOI18N
                init.setParamName(MONITOR_INTERNALPORT_PARAM_NAME);
                init.setParamValue(correctParamValue);
                myFilter.addInitParam(init);
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
    
    private static String getLocalHost() {
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
            return Integer.toString(u.getPort()); // NOI18N
        }
        else {
            return "8082"; // NOI18N
        }
    }
    
    
    private static void startModuleSpy (final ModuleSpy spy) {
        // trying to hang a listener on monitor module 
        res = Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
        java.util.Iterator it = res.allInstances ().iterator ();
        final String moduleId = spy.getModuleId();        
       // boolean found = false;
        while (it.hasNext ()) {
            org.openide.modules.ModuleInfo mi = (ModuleInfo)it.next ();
            if (mi.getCodeName ().startsWith(moduleId)) {
                httpMonitorInfo=mi;
                spy.setEnabled(mi.isEnabled());
                monitorInfoListener = new MonitorInfoListener(spy);
                httpMonitorInfo.addPropertyChangeListener(monitorInfoListener);
               // found=true;
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
                spy.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
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
