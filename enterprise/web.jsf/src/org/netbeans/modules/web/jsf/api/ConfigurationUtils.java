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
package org.netbeans.modules.web.jsf.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 * @author Po-Ting Wu
 */
public class ConfigurationUtils {
    
    // We can override equals() and hashcode() methods here for accepting 2 keys in HashMap
    // However due to the performance issue and clear codes, use 2 HashMap here will be better
    private static WeakHashMap<FileObject, WeakReference<JSFConfigModel>> configModelsEditable = new WeakHashMap<>();
    private static WeakHashMap<FileObject, WeakReference<JSFConfigModel>> configModelsNonEditable = new WeakHashMap<>();

    /**
     * This methods returns the model source for the faces config file.
     * @param confFile - the faces config file
     * @param editable - if the source will be editable. Clients should use true. 
     * @return The ModelSource for the configuration file. If the file is not faces config file
     * or a version which is not handled, then returns null. 
     */
    public static synchronized JSFConfigModel getConfigModel(FileObject confFile, boolean editable) {
        JSFConfigModel configModel = null;
        if (confFile != null && confFile.isValid()) {
            Map<FileObject,WeakReference<JSFConfigModel>> configModelsRef = editable ? configModelsEditable : configModelsNonEditable;
            WeakReference<JSFConfigModel> configModelRef = configModelsRef.get(confFile);
            if (configModelRef != null) {
                configModel = configModelRef.get();
                if (configModel != null) {
                    return configModel;
                }

                configModelsRef.remove(confFile);
            }

            try {
                ModelSource modelSource = Utilities.createModelSource(confFile,editable);
                configModel = JSFConfigModelFactory.getInstance().getModel(modelSource);
                configModelsRef.put(confFile, new WeakReference<JSFConfigModel>(configModel));
            } catch (CatalogModelException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(), ex);
            }
        }
        return configModel;
    }
    
    /**
     * The methods finds the definition of the Faces Servlet in the deployment descriptor
     * of the given web module.
     * @param webModule the given web module, where the Faces Servlet is.
     * @return Faces Servlet definition or null if the Faces Servlet definition is not 
     * found in the given web module.
     */
    public static Servlet getFacesServlet(WebModule webModule) {
        FileObject deploymentDescriptor = webModule.getDeploymentDescriptor();
        if (deploymentDescriptor == null) {
            return null;
        }
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            // Try to find according the servlet class name. The javax.faces.webapp.FacesServlet is final, so
            // it can not be extended.
            if (WebApp.VERSION_6_1.equals(webApp.getVersion()) || WebApp.VERSION_6_0.equals(webApp.getVersion()) || 
                    WebApp.VERSION_5_0.equals(webApp.getVersion())) {
                return (Servlet) webApp
                    .findBeanByName("Servlet", "ServletClass", "jakarta.faces.webapp.FacesServlet"); //NOI18N;
            } else {
                return (Servlet) webApp
                    .findBeanByName("Servlet", "ServletClass", "javax.faces.webapp.FacesServlet"); //NOI18N;
            }
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Faces Servlet.
     * @param webModule web module, where the JSF framework should be defined
     * @return The maping for the faces servlet. Null if the web module doesn't
     * contains definition of faces servlet.
     */
    public static String getFacesServletMapping(WebModule webModule){
        FileObject deploymentDescriptor = webModule.getDeploymentDescriptor();
        Servlet servlet = getFacesServlet(webModule);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDescriptor);
                ServletMapping[] mappings = webApp.getServletMapping();
                for (int i = 0; i < mappings.length; i++){
                    if (mappings[i].getServletName().equals(servlet.getServletName()))
                        return ((ServletMapping25)mappings[i]).getUrlPatterns()[0];
                }
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }
    
    /**
     * The method returns all faces configuration files in the web module.
     * If there is faces-config.xml file in the web project, then it's returned 
     * as the first one. Other configuration files are in the same order as are 
     * listed in the javax.faces.CONFIG_FILES attribute in the web.xml file.
     * @param webModule - the web module, where you want to find the faces
     * configuration files
     * @return array of all faces configuration files. If there are not any 
     * configuration file, then empty array is returned.
     **/
    
    public static FileObject[] getFacesConfigFiles(WebModule webModule){
        String[] sFiles = JSFConfigUtilities.getConfigFiles(webModule);
        if (sFiles.length > 0){
            FileObject documentBase = webModule.getDocumentBase();
            if (documentBase == null) {
                return new FileObject [0];
            }
            List<FileObject> files = new ArrayList<>();
            FileObject file;
            for (int i = 0; i < sFiles.length; i++){
                file = documentBase.getFileObject(sFiles[i]);
                if (file != null) {
                    files.add(file);
                }
            }
            return files.toArray(new FileObject[0]);
        }
        return new FileObject [0];
    }
    
    /**
     * Translates an URI to be executed with faces serlvet with the given mapping.
     * For example, the servlet has mapping <i>*.jsf</i> then uri <i>hello.jsp</i> will be
     * translated to <i>hello.jsf</i>. In the case where the mapping is <i>/faces/*</i>
     * will be translated to <i>faces/hello.jsp<i>.
     *
     * @param mapping The servlet mapping
     * @param uri The original URI
     * @return The translated URI
     */
    public static String translateURI(String mapping, String uri){
        String resource = "";
        if (mapping != null && mapping.length()>0){
            if (mapping.startsWith("*.")){
                if (uri.indexOf('.') > 0)
                    resource = uri.substring(0, uri.lastIndexOf('.'))+mapping.substring(1);
                else
                    resource = uri + mapping.substring(1);
            } else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(1,mapping.length()-1) + uri;
        }
        return resource;
    }
    
    /**
     * Helper method which finds the faces configuration file, where is the managed bean
     * defined.
     * @param webModule the web module, wher the managed bean is defined.
     * @param name Name of the managed bean. 
     * @return faces configuration file, where the managed bean is defined. Null, if a bean
     * with the given name is not defined in the web module.
     */
    public static FileObject findFacesConfigForManagedBean(WebModule webModule, String name){
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        
        
        for (int i = 0; i < configs.length; i++) {
            //DataObject dObject = DataObject.find(configs[i]);
            FacesConfig facesConfig = getConfigModel(configs[i], true).getRootComponent();
            Collection<ManagedBean>beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if(name.equals(managedBean.getManagedBeanName()))
                    return configs[i];
            }
            
        }
        return null;
    }
    
}
