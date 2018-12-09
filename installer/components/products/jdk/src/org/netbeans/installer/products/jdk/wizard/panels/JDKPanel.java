/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
        if (objectContext != null && objectContext instanceof Product) {
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
            if(objectContext != null && objectContext instanceof Product) {
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
