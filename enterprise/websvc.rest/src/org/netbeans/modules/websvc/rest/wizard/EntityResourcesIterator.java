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

package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGeneratorFactory;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceModelBuilder;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Liu
 * @author ads
 */
public class EntityResourcesIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
    
    private static final long serialVersionUID = -1555851385128542149L;
    private int index;
    private transient WizardDescriptor.Panel<?>[] panels;
    private transient RequestProcessor.Task transformTask;
    private WizardDescriptor wizard;
    
    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set instantiate(ProgressHandle pHandle) throws IOException {
        pHandle.start(100);
        final Project project = Templates.getProject(wizard);

        String restAppPackage = (String) wizard.getProperty(WizardProperties.APPLICATION_PACKAGE);
        String restAppClass = (String) wizard.getProperty(WizardProperties.APPLICATION_CLASS);
        
        final RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        boolean useJersey = Boolean.TRUE.equals(wizard.getProperty(WizardProperties.USE_JERSEY));
        if (!useJersey) {
            RestSupport.RestConfig.IDE.setAppClassName(restAppPackage+"."+restAppClass); //NOI18N
        }
        if ( restSupport!= null ){
            restSupport.ensureRestDevelopmentReady(useJersey ?
                    RestSupport.RestConfig.DD : RestSupport.RestConfig.IDE);
        }

        FileObject targetFolder = Templates.getTargetFolder(wizard);
        FileObject wizardSrcRoot = (FileObject)wizard.getProperty(
                WizardProperties.TARGET_SRC_ROOT);
        /*
         *  Visual panel is used from several wizards. One of them
         *  has several options for target source roots ( different for 
         *  entities, generation classes ). 
         *  There is special property WizardProperties.TARGET_SRC_ROOT
         *  which is set up by wizard panel. This property should be used
         *  as target source root folder. 
         */
        if ( wizardSrcRoot != null ){
            targetFolder  = wizardSrcRoot;
        }
        
        String targetPackage = SourceGroupSupport.packageForFolder(targetFolder);
        final String resourcePackage = (String) wizard.getProperty(WizardProperties.RESOURCE_PACKAGE);
        String controllerPackage = (String) wizard.getProperty(WizardProperties.CONTROLLER_PACKAGE);
        List<String> entities = (List<String>) wizard.
            getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.ENTITY_CLASS);
        final PersistenceUnit pu = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
    
        /* 
         * There should be ALL found entities but they needed to compute closure. 
         * Persistence wizard already has computed closure. So there is no need 
         * in all other entities.
         * Current CTOR of builder and method <code>build</code> is not changed 
         * for now but should be changed later after  review of its usage.
         */
        EntityResourceModelBuilder builder = new EntityResourceModelBuilder(
                project, entities );
        EntityResourceBeanModel model = builder.build();
        final EntityResourcesGenerator generator = EntityResourcesGeneratorFactory.newInstance(project);
        generator.initialize(model, project, targetFolder, targetPackage, 
                resourcePackage, controllerPackage, pu);
        pHandle.progress(50);
        
        // create application config class if required
        final FileObject restAppPack = restAppPackage == null ? null :  
            FileUtil.createFolder(targetFolder, restAppPackage.replace('.', '/'));
        final String appClassName = restAppClass;
        try {
            if ( restAppPack != null && appClassName!= null && !useJersey) {
                RestUtils.createApplicationConfigClass(restSupport, restAppPack, appClassName);
            }
            RestUtils.disableRestServicesChangeListner(project);
            generator.generate(null);
            pHandle.progress(80);
            restSupport.configure(resourcePackage);
        } catch(Exception iox) {
            Exceptions.printStackTrace(iox);
        } finally {
            RestUtils.enableRestServicesChangeListner(project);
        }

        // logging usage of wizard
        Object[] params = new Object[5];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = project.getClass().getName();
        J2eeModule j2eeModule = RestUtils.getJ2eeModule(project);
        params[2] = j2eeModule == null ? null : j2eeModule.getModuleVersion()+"(WAR)"; //NOI18N
        params[3] = "REST FROM ENTITY"; //NOI18N
        LogUtils.logWsWizard(params);
        pHandle.finish();
        return Collections.<DataObject>singleton(DataFolder.findFolder(targetFolder));
    }
    
 
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        index = 0;
        WizardDescriptor.Panel<?> secondPanel = new EntitySelectionPanel(
                NbBundle.getMessage(EntityResourcesIterator.class, 
                        "LBL_EntityClasses"), wizard);      // NOI18N
        WizardDescriptor.Panel<?> thirdPanel =new EntityResourcesSetupPanel(
                NbBundle.getMessage(EntityResourcesIterator.class,
                "LBL_RestResourcesAndClasses"), wizard,         // NOI18N
                MiscUtilities.isJavaEE6AndHigher(Templates.getProject(wizard)) ||
                        RestUtils.hasSpringSupport(Templates.getProject(wizard)));
        panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel };
        String names[] = new String[] {
            NbBundle.getMessage(EntityResourcesIterator.class, 
                    "LBL_EntityClasses"),                       // NOI18N
            NbBundle.getMessage(EntityResourcesIterator.class, 
                    "LBL_RestResourcesAndClasses")              // NOI18N    
        };
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(EntityResourcesIterator.class, 
                        "Templates/WebServices/RestServicesFromEntities"));// NOI18N
        Util.mergeSteps(wizard, panels, names);
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public String name() {
        return NbBundle.getMessage(EntityResourcesIterator.class, 
                "LBL_WizardTitle_FromEntity");          // NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (! hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (! hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
}
