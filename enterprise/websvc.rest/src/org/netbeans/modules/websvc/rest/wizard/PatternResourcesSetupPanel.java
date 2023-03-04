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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author nam
 */
final class PatternResourcesSetupPanel extends AbstractPanel {
    private JComponent component;
    private Pattern currentPattern = PatternResourcesSetupPanel.Pattern.CONTAINER;
    
    /** Create the wizard panel descriptor. */
    public PatternResourcesSetupPanel(String name, WizardDescriptor wizardDescriptor) {
        super(name, wizardDescriptor);
    }
    
    @Override
    public boolean isFinishPanel() {
        return true;
    }

    public enum Pattern {
        CONTAINER,
        STANDALONE,
        CLIENTCONTROLLED;
                
        public JComponent createUI(String name) {   
            return new PatternPanel(name, this);
        }
    }

    public void setCurrentPattern(Pattern pattern) {
        if (currentPattern != pattern) {
            component = null;
            currentPattern = pattern;
        }
    }
    
    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = currentPattern.createUI(panelName);
            if (allWizardSteps != null) {
                component.putClientProperty(Util.WIZARD_PANEL_CONTENT_DATA, allWizardSteps);
                component.putClientProperty(Util.WIZARD_PANEL_CONTENT_SELECTED_INDEX, indexInAllSteps);
            }
            component.setName(panelName);
            ((Settings)component).addChangeListener(this);
        }
        return component;
    }
    
    private String[] allWizardSteps;
    private int indexInAllSteps = 2;
    
    void saveStepsAndIndex() {
        if (component != null) {
            allWizardSteps = (String[]) component.getClientProperty(Util.WIZARD_PANEL_CONTENT_DATA);
            indexInAllSteps = (Integer) component.getClientProperty(Util.WIZARD_PANEL_CONTENT_SELECTED_INDEX);
        }        
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PatternResourcesSetupPanel.class.getCanonicalName() + "." + this.currentPattern);
    }
    
    static class PatternPanel extends JPanel implements AbstractPanel.Settings, 
        SourcePanel
    {
        private static final long serialVersionUID = -5802330662876130253L;
        
        PatternPanel(String name , Pattern pattern) {
            JPanel panel = null;
            switch( pattern){
                case CONTAINER:
                case CLIENTCONTROLLED:
                    panel = new ContainerItemSetupPanelVisual(name);
                    break;
                case STANDALONE:
                    panel = new SingletonSetupPanelVisual(name );
                    break;
                default :
                    assert false;
            }
            setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
            add( panel);
            mainPanel = (AbstractPanel.Settings)panel;
            jaxRsConfigurationPanel = new JaxRsConfigurationPanel( this );
            mainPanel.addChangeListener(jaxRsConfigurationPanel );
            add( jaxRsConfigurationPanel );
            // Fix for BZ#214951 - Hidden Text Box during REST endpoint creation
            addHierarchyListener( new HierarchyListener(){

                @Override
                public void hierarchyChanged( HierarchyEvent e ) {
                    final HierarchyListener listener = this; 
                    SwingUtilities.invokeLater( new Runnable() {
                        
                        @Override
                        public void run() {
                            double height = 0;
                            Component[] components = getComponents();
                            for (Component component : components) {
                                if ( component instanceof SingletonSetupPanelVisual ){
                                    double renderedHeight = 
                                            ((SingletonSetupPanelVisual)component).
                                                getRenderedHeight();
                                    height+=renderedHeight;
                                    resize(component, renderedHeight);
                                }
                                else if (component instanceof ContainerItemSetupPanelVisual ){
                                    double renderedHeight = 
                                            ((ContainerItemSetupPanelVisual)component).
                                                getRenderedHeight();
                                    height+=renderedHeight;
                                    resize(component, renderedHeight);
                                } 
                                else if ( component instanceof JaxRsConfigurationPanel ){
                                    double renderedHeight = 
                                            ((JaxRsConfigurationPanel)component).
                                                getRenderedHeight();
                                    height+=renderedHeight;
                                    resize(component, renderedHeight);
                                }
                            }
                            Dimension dim = getSize();
                            int newHeight = (int)height;
                            if ( dim.height < newHeight ) {
                                setPreferredSize( new Dimension( dim.width, newHeight ));
                                Window window = SwingUtilities.
                                        getWindowAncestor(PatternPanel.this);
                                if ( window!= null ){
                                    window.pack();
                                }
                            }
                            removeHierarchyListener(listener);
                        }

                    });   
                     
              }
                
            });
        }
        
        @Override
        public SourceGroup getSourceGroup(){
            return ((SourcePanel)mainPanel).getSourceGroup();
        }

        @Override
        public String getPackageName() {
            return ((SourcePanel)mainPanel).getPackageName();
        }

        @Override
        public void read(final WizardDescriptor wizard) {
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

        @Override
        public void store(WizardDescriptor wizard) {
            mainPanel.store(wizard);
            if ( hasJaxRsConfigurationPanel() ){
                jaxRsConfigurationPanel.store(wizard);
            }
        }

        @Override
        public boolean valid(WizardDescriptor wizard) {
            boolean isValid = mainPanel.valid(wizard);
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
        
        private void resize( Component component, double height )
        {
            Dimension size = component.getSize();
            if ( size.height < height ){
                component.setPreferredSize(
                        new Dimension(size.width, (int)height));
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
        
        private AbstractPanel.Settings mainPanel;    
        private JaxRsConfigurationPanel jaxRsConfigurationPanel;
    }
}
