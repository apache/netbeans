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

package org.netbeans.modules.web.jsf;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class JSFInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public JSFInjectionTargetQueryImplementation() {
    }
    
    /** 
     * For method return true if:
     *      1) The web module follows 2.5 servlet specification or higher
     *      2) The jc is defined as manage bean in a jsp configuration file.
     */
    public boolean isInjectionTarget(CompilationController controller, 
            final TypeElement typeElement) 
    {
        Parameters.notNull("controller", controller);
        Parameters.notNull("typeElement", typeElement);
        
        // Find the web module, where the class is
        WebModule webModule  = WebModule.getWebModule(controller.getFileObject());
        // Is the web modile 2.5 servlet spec or higher?
        if (webModule != null && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_13_LEVEL)
                && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_14_LEVEL))
        {
            /*
             * Old JSF model usage. Change code to merged model usage.
            // Get deployment desctriptor from the web module
            FileObject ddFileObject = webModule.getDeploymentDescriptor();
            if (ddFileObject != null){
            // Get all jsf configurations files
            FileObject[] jsfConfigs = ConfigurationUtils.getFacesConfigFiles(webModule);
            for (FileObject jsfConfigFO : jsfConfigs) {
            JSFConfigModel model = ConfigurationUtils.getConfigModel(jsfConfigFO, true);
            if (model != null) {
            // Get manage beans from the configuration file
            FacesConfig facesConfig = model.getRootComponent();
            if (facesConfig != null) {
            List<ManagedBean> beans = facesConfig.getManagedBeans();
            for (ManagedBean managedBean : beans) {
            if (typeElement.getQualifiedName().contentEquals(managedBean.getManagedBeanClass())) {
            return true;
            }
            }
            }
            }
            }
            }*/
            /**
             * @author ads
             */
            Project project = FileOwnerQuery.getOwner(controller.getFileObject());
            MetadataModel<JsfModel> model = JSFUtils.getModel(project);
            if ( model != null ){
                try {
                return model.runReadAction( new MetadataModelAction<JsfModel, Boolean>() {

                    public Boolean run( JsfModel metaModel ) throws Exception {
                        List<FacesManagedBean> beans = metaModel.getElements(
                                FacesManagedBean.class);
                        for (FacesManagedBean managedBean : beans) {
                            if (typeElement.getQualifiedName().contentEquals(
                                    managedBean.getManagedBeanClass())) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                }
                catch(MetadataModelException e ){
                    Logger.getLogger( JSFInjectionTargetQueryImplementation.class.
                            getCanonicalName()).log( Level.WARNING, e.getMessage(), e );
                }
                catch(IOException e ){
                    Logger.getLogger( JSFInjectionTargetQueryImplementation.class.
                            getCanonicalName()).log( Level.WARNING, e.getMessage(), e );
                }
            }
        }
        return false;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
