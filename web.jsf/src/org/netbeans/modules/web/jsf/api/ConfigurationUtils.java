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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.jsf.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Map;
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
    private static WeakHashMap<FileObject, WeakReference<JSFConfigModel>> configModelsEditable = new WeakHashMap();
    private static WeakHashMap<FileObject, WeakReference<JSFConfigModel>> configModelsNonEditable = new WeakHashMap();

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
            return (Servlet) webApp
                    .findBeanByName("Servlet", "ServletClass", "javax.faces.webapp.FacesServlet"); //NOI18N;
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
            ArrayList files = new ArrayList();
            FileObject file;
            for (int i = 0; i < sFiles.length; i++){
                file = documentBase.getFileObject(sFiles[i]);
                if (file != null) {
                    files.add(file);
                }
            }
            return (FileObject[])files.toArray(new FileObject[files.size()]);
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
