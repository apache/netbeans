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

package org.netbeans.modules.websvc.saas.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author nam
 */
public class WsdlUtil {

    private static WsdlDataManager getWsdlDataManager(){
        WsdlDataManager wsdlDataManager = null;
        int precedence = 0;
        boolean first = true;
        Collection<? extends WsdlDataManager> mgrs = Lookup.getDefault().lookupAll(WsdlDataManager.class);
        for(WsdlDataManager mgr : mgrs){
            if(first){
                first = false;
                precedence = mgr.getPrecedence();
                wsdlDataManager = mgr;
                continue;
            }
            int newPrecedence = mgr.getPrecedence();
            if(newPrecedence < precedence){
                wsdlDataManager = mgr;
                precedence = newPrecedence;
            }
        }
        return wsdlDataManager;
    }
    public static boolean hasWsdlSupport() {
        return getWsdlDataManager() != null;
    }
    
    public static WsdlData findWsdlData(String url, String serviceName) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            return manager.findWsdlData(url, serviceName);
        }
        return null;
    }
    
    public static WsdlData addWsdlData(String url, String packageName) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            return manager.addWsdlData(url, packageName);
        }
        return null;
    }
    
    public static WsdlData getWsdlData(String url, String serviceName, boolean synchronous) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            return manager.getWsdlData(url, serviceName, synchronous);
        } 
        return null;
    }
    
    public static void removeWsdlData(WsdlData data) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            manager.removeWsdlData(data.getOriginalWsdlUrl(), data.getName());
        }
    }    

    public static void removeWsdlData(String url) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            manager.removeWsdlData(url, null);
        }
    }
  
    public static void refreshWsdlData(WsdlData data) {
        WsdlDataManager manager = getWsdlDataManager();
        if (manager != null) {
            manager.refresh(data);
        }
    }

    public static boolean isJAXRPCAvailable() {
        return getWebServiceSupportLibDef(false) != null;
    }

    /**
     * @return The library definition containing the web service support jar files, null if it does not exist
     */
    public static Library getWebServiceSupportLibDef(boolean isJ2EE_15) {
        String libraryName = (isJ2EE_15) ? "jaxws21" : "jaxrpc16";
        Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
        return libDef;
    }

    public static String getCatalogForWsdl(String wsdlUrl) {
        String serviceDir = getServiceDirName(wsdlUrl);
        return "/" + serviceDir + "/catalog/catalog.xml";
    }
    
    public static String getServiceDirName(String wsdlUrl) {
        try {
            URL url;
            url = new URL(wsdlUrl);

            String urlPath = url.getPath();
            int start;
            if (url.getProtocol().toLowerCase().startsWith("file")) { // NOI18N
                start = urlPath.lastIndexOf(System.getProperty("path.separator")); // NOI18N
                start = (start < 0) ? urlPath.lastIndexOf("/") : start; // NOI18N
            } else {
                if(urlPath.endsWith("/")){
                    urlPath = urlPath.substring(0, urlPath.length() - 1);
                }
                start = urlPath.lastIndexOf("/");
            }
            start++;

            return urlPath.substring(start).replace('.', '-'); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return SaasUtil.ensureUniqueServiceDirName(SaasUtil.DEFAULT_SERVICE_NAME); //NOI18N
        }
    }

    private static final String IMPORTED_MARK = ".imported";
    
    public static boolean hasProcessedImport() {
        return SaasServicesModel.getWebServiceHome().getFileObject(IMPORTED_MARK) != null;
    }
    
    public static void markImportProcessed() {
        if (!hasProcessedImport()) {
            try {
                SaasServicesModel.getWebServiceHome().createData(IMPORTED_MARK);
            } catch(IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static void ensureImportExisting60Services() {
        if (!hasProcessedImport()) {
            findWsdlData("/foo", "bar");
        }
    }
}
