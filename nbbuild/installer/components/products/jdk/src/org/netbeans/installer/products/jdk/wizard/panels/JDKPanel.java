/**
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

package org.netbeans.installer.products.jdk.wizard.panels;

import java.io.File;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 */
public class JDKPanel extends DestinationPanel {
    public JDKPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new JDKDestinationPanelUi(this);
        }
        
        return wizardUi;
    }

    @Override
    public void initialize() {
        super.initialize();
        // add jdk.getInstallationLocation() to the all java list so that 
        // in silent installation this location is initialized and used by default        
        final Object objectContext = getWizard().getContext().get(Product.class);
        if (objectContext instanceof Product) {
            Product jdk = (Product) objectContext;
            SearchForJavaAction.addJavaLocation(
                    jdk.getInstallationLocation(),
                    jdk.getVersion(),
                    JDK_VENDOR);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class JDKDestinationPanelUi extends DestinationPanelUi {
        protected JDKPanel panel;
        
        public JDKDestinationPanelUi(JDKPanel panel) {
            super(panel);
            
            
            this.panel = panel;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new JDKDestinationPanelSwingUi(panel, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class JDKDestinationPanelSwingUi extends DestinationPanelSwingUi {
        protected JDKPanel panel;
        private Product jdk;
    
        public JDKDestinationPanelSwingUi(
                final JDKPanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {            
            super.initialize();
            final String location = panel.getWizard().getProperty(Product.INSTALLATION_LOCATION_PROPERTY);
            if(location!=null) {
                final File f = new File(location);
                int index = SearchForJavaAction.getJavaLocations().indexOf(f);
                if(index!=-1) {                    
                    SearchForJavaAction.getJavaLocations().remove(index);
                    SearchForJavaAction.getJavaLabels().remove(index);                    
                }
                JavaUtils.removeJavaInfo(f);
            }
        }
        
        @Override
        protected void saveInput() {
            super.saveInput();
            if ((jdk = getBundledJDK(panel)) != null) {
                SearchForJavaAction.addJavaLocation(
                        jdk.getInstallationLocation(),
                        jdk.getVersion(),
                        JDK_VENDOR);
            }
        }
        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();
            // don`t allow space path installation since unix JDK installer doesn`t work with that                
            if(errorMessage == null && !SystemUtils.isWindows() && 
                    getDestinationField().getText().trim().contains(StringUtils.SPACE)) {                
                    errorMessage = ERROR_SPACE_IN_PATH;                
            }
            return errorMessage;
        }
        
        private static Product getBundledJDK(JDKPanel panel) {
            final Object objectContext = panel.getWizard().getContext().get(Product.class);
            if(objectContext instanceof Product) {
                return  (Product) objectContext;                
            }
            return null;
        }
        
        private boolean isJDK8() {
            if (getBundledJDK(panel) != null) {
                return getBundledJDK(panel).getVersion().newerOrEquals(Version.getVersion("1.8.0")); // NOI18N
            }
            return false;
        }

    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.description"); // NOI18N
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.destination.button.text"); // NOI18N
    public static final String JDK_VENDOR = 
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.jdk.vendor"); // NOI18N
    public static final String ERROR_SPACE_IN_PATH = 
            ResourceUtils.getString(JDKPanel.class,
            "JDKP.error.space.in.path");//NOI18N
}
