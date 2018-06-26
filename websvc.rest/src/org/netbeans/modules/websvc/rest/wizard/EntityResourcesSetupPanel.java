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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
