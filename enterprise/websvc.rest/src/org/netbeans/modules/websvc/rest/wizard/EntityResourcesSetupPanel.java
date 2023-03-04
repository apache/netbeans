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

import java.awt.Component;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 * @author ads
 */
public final class EntityResourcesSetupPanel extends AbstractPanel {
    
    private FinishEntityPanel component;
    
    /** Create the wizard panel descriptor. */
    public EntityResourcesSetupPanel(String name, 
            WizardDescriptor wizardDescriptor, boolean noController) 
    {
        super(name, wizardDescriptor);
        withoutController = noController;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null) {
            component = new FinishEntityPanel(panelName,
                    withoutController);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel#readSettings(java.lang.Object)
     */
    @Override
    public void readSettings( Object settings ) {
        super.readSettings(settings);
        if (wizardDescriptor
                .getProperty(WizardProperties.CREATE_PERSISTENCE_UNIT) != null)
        {
            Project project = Templates.getProject(wizardDescriptor);
            try {
                org.netbeans.modules.j2ee.persistence.wizard.Util
                        .createPersistenceUnitUsingWizard(project, null,
                                TableGeneration.NONE);
            }
            catch (InvalidPersistenceXmlException e) {
                Logger.getLogger(EntitySelectionPanel.class.getName()).log(
                        Level.WARNING, null, e);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel#storeSettings(java.lang.Object)
     */
    @Override
    public void storeSettings( Object settings ) {
        super.storeSettings(settings);
        if ( Util.getPersistenceUnit(wizardDescriptor, 
                Templates.getProject(wizardDescriptor)) != null )
        {
            wizardDescriptor.putProperty(
                    WizardProperties.CREATE_PERSISTENCE_UNIT,null);
        }
    }
    
    static class FinishEntityPanel extends JPanel implements AbstractPanel.Settings, 
        SourcePanel
    {
        private static final long serialVersionUID = -1899506976995286218L;
        
        FinishEntityPanel(String name , boolean withoutController ){
            mainPanel = new EntityResourcesSetupPanelVisual(name,
                    withoutController);
            setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
            add( mainPanel );
            jaxRsConfigurationPanel = new JaxRsConfigurationPanel( this );
            mainPanel.addChangeListener(jaxRsConfigurationPanel );
            add( jaxRsConfigurationPanel );
            setName(name);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.rest.wizard.SourcePanel#getSourceGroup()
         */
        @Override
        public SourceGroup getSourceGroup() {
            return mainPanel.getSourceGroup();
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#read(org.openide.WizardDescriptor)
         */
        @Override
        public void read( final WizardDescriptor wizard ) {
            mainPanel.read(wizard);
            Project project = Templates.getProject(wizard);
            final RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
            // TODO: for Jersey2 I temporarily disable Spring support:
            // Use web.xml configuration for Java EE5 project types and Jersey 1.x
            if (restSupport.isEE5() && restSupport.hasJersey1(true) || 
                    restSupport.hasSpringSupport() && !restSupport.hasJersey2(true)) {
                wizard.putProperty(WizardProperties.USE_JERSEY, true);
            }
            if (jaxRsConfigurationPanel != null) {
                ScanDialog.runWhenScanFinished(new Runnable() {

                    @Override
                    public void run() {
                        boolean configured = restSupport.isRestSupportOn();
                        if (!configured) {
                            configured = restSupport.hasJerseyServlet();
                        }
                        if (!configured) {
                            List<RestApplication> restApplications = restSupport.
                                    getRestApplications();
                            configured = restApplications != null
                                    && !restApplications.isEmpty();
                        }
                        configureJaxRsConfigurationPanel(configured, wizard);
                    }
                }, NbBundle.getMessage(PatternResourcesSetupPanel.class,
                        "LBL_SearchAppConfig")); // NOI18N

            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.rest.wizard.AbstractPanel.Settings#store(org.openide.WizardDescriptor)
         */
        @Override
        public void store( WizardDescriptor wizard ) {
            mainPanel.store(wizard);
            if ( hasJaxRsConfigurationPanel() ){
                jaxRsConfigurationPanel.store(wizard);
            }            
        }

        @Override
        public boolean valid(WizardDescriptor wizard) {
            boolean isValid = ((AbstractPanel.Settings)mainPanel).valid(wizard);
            if ( isValid && hasJaxRsConfigurationPanel() ){
                return jaxRsConfigurationPanel.valid(wizard);
            }
            else {
                return isValid;
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            mainPanel.addChangeListener(l);
            if ( hasJaxRsConfigurationPanel() ){
                jaxRsConfigurationPanel.addChangeListener(l);
            }
        }
        
        private boolean hasJaxRsConfigurationPanel(){
            return jaxRsConfigurationPanel != null;
        }
        
        private void configureJaxRsConfigurationPanel(boolean remove, WizardDescriptor wizard){
            if ( jaxRsConfigurationPanel == null ){
                return;
            }
            if ( remove )
            {
                remove( jaxRsConfigurationPanel );
                jaxRsConfigurationPanel = null;
            }
            else {
                jaxRsConfigurationPanel.read(wizard);
            }
        }
        
        private EntityResourcesSetupPanelVisual mainPanel;
        private JaxRsConfigurationPanel jaxRsConfigurationPanel;

        @Override
        public String getPackageName() {
            return mainPanel.getPackageName();
        }
    }
    
    private boolean withoutController;
}
