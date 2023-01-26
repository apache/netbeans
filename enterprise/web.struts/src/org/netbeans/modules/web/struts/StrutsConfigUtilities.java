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

package org.netbeans.modules.web.struts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.struts.config.model.Action;
import org.netbeans.modules.web.struts.config.model.ActionMappings;
import org.netbeans.modules.web.struts.config.model.MessageResources;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.netbeans.modules.web.struts.config.model.FormBeans;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author petr
 * @author Po-Ting Wu
 */
public class StrutsConfigUtilities {
    
    public static String DEFAULT_MODULE_NAME = "config"; //NOI18N
    private static final int TYPE_ACTION=0;
    private static final int TYPE_FORM_BEAN=1;
    private static final int TYPE_MESSAGE_RESOURCES=2;
    
    public static List getAllActionsInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_ACTION,data);
    }
    
    public static List getAllFormBeansInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_FORM_BEAN,data);
    }
    
    public static List getAllMessageResourcesInModule(StrutsConfigDataObject data){
        return createConfigElements(TYPE_MESSAGE_RESOURCES,data);
    }
    
    private static List createConfigElements(int elementType, StrutsConfigDataObject data) {
        FileObject config = data.getPrimaryFile();
        ArrayList list = new ArrayList();
        WebModule wm = WebModule.getWebModule(config);
        if (wm != null){
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo != null){
                String moduleName = getModuleName(config, ddFo);
                if (moduleName == null){
                    // the conf file is not in any module (is not declared in the web.xml)
                    try{
                        StrutsConfig sConfig = data.getStrutsConfig(true);
                        switch (elementType) {
                            case TYPE_ACTION : addActions(list, sConfig);break;
                            case TYPE_FORM_BEAN : addFormBeans(list, sConfig);break;
                            case TYPE_MESSAGE_RESOURCES : addMessageResource(list, sConfig);break;
                        }
                    } catch (java.io.IOException e){
                        // Do nothing
                    }
                } else {
                    // the config file is in a Struts module, returns all actions from the
                    // conf files in the module
                    FileObject[] configs = getConfigFiles(moduleName, ddFo);
                    DataObject dOb;
                    for (int i = 0; i < configs.length; i++){
                        try{
                            dOb = DataObject.find(configs[i]);
                        } catch (DataObjectNotFoundException e){
                            dOb = null;
                        }
                        if (dOb instanceof StrutsConfigDataObject){
                            StrutsConfigDataObject con = (StrutsConfigDataObject)dOb;
                            // the conf file is not in any module (is not declared in the web.xml)
                            try{
                                StrutsConfig sConfig = con.getStrutsConfig(true);
                                switch (elementType) {
                                    case TYPE_ACTION : addActions(list, sConfig);break;
                                    case TYPE_FORM_BEAN : addFormBeans(list, sConfig);break;
                                    case TYPE_MESSAGE_RESOURCES : addMessageResource(list, sConfig);break;
                                }
                            } catch (java.io.IOException e){
                                // Do nothing
                            }
                        }
                    }
                }
            }
        } 
        return list;
    }

    private static void addActions(List<Action> list, StrutsConfig sConfig) {
        ActionMappings mappings = null;
        if (sConfig != null) {
            mappings = sConfig.getActionMappings();
        }
        if (mappings==null) return;
        Action[] actions = mappings.getAction();
        for (int j = 0; j < actions.length; j++)
            list.add(actions[j]);
    }
    
    private static void addFormBeans(List<FormBean> list, StrutsConfig sConfig) {
        FormBeans formBeans = sConfig.getFormBeans();
        if (formBeans==null) return;
        FormBean [] beans = formBeans.getFormBean();
        for (int j = 0; j < beans.length; j++)
            list.add(beans[j]);
    }
    
    private static void addMessageResource(List<MessageResources> list, StrutsConfig sConfig) {
        MessageResources[] rosources = sConfig.getMessageResources();
        for (int j = 0; j < rosources.length; j++)
            list.add(rosources[j]);
    }
    
    
    /** Returns all configuration files for the module
     **/
    public static FileObject[] getConfigFiles(String module, FileObject dd){
        WebModule wm = WebModule.getWebModule(dd);
        if (wm == null)
            return null;
        FileObject docBase = wm.getDocumentBase();
        if (docBase == null)
            return null;
        Servlet servlet = getActionServlet(dd);
        InitParam param = null;
        if (module.equals(DEFAULT_MODULE_NAME))
            param = (InitParam)servlet.findBeanByName("InitParam", "ParamName", DEFAULT_MODULE_NAME);
        else
            param = (InitParam)servlet.findBeanByName("InitParam", "ParamName", DEFAULT_MODULE_NAME+"/"+module);
        FileObject[] configs = null;
        if (param != null){
            StringTokenizer st = new StringTokenizer(param.getParamValue(), ",");
            configs = new FileObject[st.countTokens()];
            int index = 0;
            while (st.hasMoreTokens()){
                String name = st.nextToken().trim();
                configs[index] = docBase.getFileObject(name);
                index++;
            }
        }
        return configs;
    }
    
    
    
    /** Returns name of Struts module, which contains the configuration file.
     */
    public static String getModuleName(FileObject config, FileObject dd){
        String moduleName = null;
        if (dd != null) {
            Servlet servlet = getActionServlet(dd);
            if (servlet != null){
                InitParam [] param = servlet.getInitParam();
                StringTokenizer st = null;
                int index = 0;

                while (moduleName == null && index < param.length){
                    if(param[index].getParamName().trim().startsWith(DEFAULT_MODULE_NAME)){
                        String[] files = param[index].getParamValue().split(","); //NOI18N
                        for (int i = 0; i < files.length; i++){
                            String file = files[i];
                            if (config.getPath().endsWith(file)){
                                if (!param[index].getParamName().trim().equals(DEFAULT_MODULE_NAME)){
                                    moduleName = param[index].getParamName().trim()
                                    .substring(DEFAULT_MODULE_NAME.length()+1);
                                } else{
                                    moduleName = DEFAULT_MODULE_NAME;
                                }
                                break;
                            }
                        }

                    }
                    index++;
                }
            }
        }
        return moduleName;
    }
    
    public static Servlet getActionServlet(FileObject dd) {
        if (dd == null) {
            return null;
        }
        
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            Servlet servlet = (Servlet) webApp.findBeanByName("Servlet", "ServletClass", "org.apache.struts.action.ActionServlet"); //NOI18N;
            if (servlet != null) {
                return servlet;
            }
            
            // check whether a servler class doesn't extend org.apache.struts.action.ActionServlet
            final Servlet[] servlets = webApp.getServlet();
            if (servlets.length == 0) {
                return null;
            }
            
            ClasspathInfo cpi = ClasspathInfo.create(dd);
            JavaSource js = JavaSource.create(cpi, Collections.<FileObject>emptyList());
            final int[] index = new int[]{-1};
            js.runUserActionTask( new Task <CompilationController>(){
                public void run(CompilationController  cc) throws Exception {                        
                    Elements elements = cc.getElements();
                    TypeElement strutsServletElement = elements.getTypeElement("org.apache.struts.action.ActionServlet"); //NOI18N
                    TypeElement servletElement;
                    if (strutsServletElement != null){
                        for (int i = 0; i < servlets.length; i++) {
                            String servletClass = servlets[i].getServletClass(); 
                            if (servletClass == null) {
                                continue;
                            }
                            
                            servletElement = elements.getTypeElement(servletClass); 
                            if (servletElement != null 
                                    && cc.getTypes().isSubtype(servletElement.asType(),strutsServletElement.asType())){
                                index[0] = i;
                                break;
                            }
                        }
                    }
                }
                
            },false);
            
            if (index[0] > -1 )
                servlet = servlets[index[0]];
            return servlet;
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
            return null;
        }
    }
    
    /** Returns the mapping for the Struts Action Servlet.
     */
    public static String getActionServletMapping(FileObject dd){
        Servlet servlet = getActionServlet(dd);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                ServletMapping[] mappings = webApp.getServletMapping();
                for (int i = 0; i < mappings.length; i++){
                    if (mappings[i].getServletName().equals(servlet.getServletName()))
                        return ((ServletMapping25)mappings[i]).getUrlPatterns()[0];
                }
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        return null;
    }
    
    /** Returns relative path for all struts configuration files in the web module
     */
    public static String[] getConfigFiles(FileObject dd){
        if (dd != null){
            Servlet servlet = getActionServlet(dd);
            if (servlet!=null) {
                InitParam[] params = servlet.getInitParam();
                List<String> list = new ArrayList<>();
                for (int i=0;i<params.length;i++) {
                    String paramName=params[i].getParamName();
                    if (paramName!=null) {
                        if (paramName.startsWith(DEFAULT_MODULE_NAME)){
                            String[] files = params[i].getParamValue().split(","); //NOI18N
                            for (int j = 0; j < files.length; j++)
                                list.add(files[j]);
                        }
                    }
                }
                String[] result = new String[list.size()];
                list.toArray(result);
                return result;
            }
        }
        return new String[]{};
    }
    
    /** Returns all configuration files in the web module
     */
    public static FileObject[] getConfigFilesFO(FileObject dd){
        if (dd != null){
            WebModule wm = WebModule.getWebModule(dd);
            if (wm == null)
                return null;
            FileObject docBase = wm.getDocumentBase();
            if (docBase == null)
                return null;
            Servlet servlet = getActionServlet(dd);
            if (servlet!=null) {
                InitParam[] params = servlet.getInitParam();
                List<FileObject> list = new ArrayList<>();
                FileObject file;
                for (int i=0;i<params.length;i++) {
                    String paramName=params[i].getParamName();
                    if (paramName!=null) {
                        if (paramName.startsWith(DEFAULT_MODULE_NAME)){ //NOI18N
                            String[] files = params[i].getParamValue().split(","); //NOI18N
                            for (int j = 0; j < files.length; j++){
                                file = docBase.getFileObject(files[j]);
                                if (file != null)
                                    list.add(file);
                            }
                        }
                    }
                }
                FileObject[] result = new FileObject[list.size()];
                list.toArray(result);
                return result;
            }
        }
        return new FileObject[]{};
    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fo) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
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
    
    /**
     * Get the welcome file based on the URL Pattern and the Page Name.
     * @param URLPattern the URL Pattern
     * @param pageName the Page Name
     * @return If successful, returns the welcome file, "do/" + pageName if unsuccessful.
     */
    public static String getWelcomeFile(String URLPattern, String pageName) {
        int indWild = URLPattern.indexOf("*"); // NOI18N
        if (indWild >= 0) {
            String pPrefix = URLPattern.substring(0, indWild);
            String pSuffix = URLPattern.substring(indWild + 1);

            if (pPrefix.length() > 0) {
                while (pPrefix.startsWith("/")) { // NOI18N
                    pPrefix = pPrefix.substring(1);
                }
            }

            return pPrefix + pageName + pSuffix;
        }

        return "do/" + pageName;
    }
    
    public static String getActionAsResource (WebModule wm, String action){
        String resource = "";
        String mapping = StrutsConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor());
        if (mapping != null && mapping.length()>0){
            if (mapping.startsWith("*."))
                resource = action + mapping.substring(1);
            else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(0,mapping.length()-2) + action;
        }
        return resource;
    }
    
    public static String getActionAsResource(String mapping, String action){
        String resource = "";
        if (mapping != null && mapping.length()>0){
            if (mapping.startsWith("*."))
                resource = action + mapping.substring(1);
            else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(0,mapping.length()-2) + action;
        }
        return resource;
    }
    
    public static MessageResources getDefatulMessageResource(FileObject dd){
        FileObject [] files = getConfigFilesFO(dd);
        if (files == null) {
            return null;
        }

        MessageResources resource = null;
        int index = 0;
        DataObject configDO;
        try {
            while (resource == null && index < files.length){
                configDO = DataObject.find(files[index]);
                if (configDO instanceof StrutsConfigDataObject){
                    StrutsConfig strutsConfig = ((StrutsConfigDataObject)configDO).getStrutsConfig();
                    // we need to chech, whether the config is parseable
                    if (strutsConfig != null){
                        MessageResources[] resources = strutsConfig.getMessageResources();
                        for (int i = 0; i < resources.length; i++){
                            if (resources[i].getAttributeValue("key") == null)    {  //NOI18N
                                resource = resources[i];
                                break;
                            }
                        }
                    }
                }
                index++;
            }
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger("global").log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.WARNING, null, ex);
        }
        return resource;
    }
}
