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
package org.netbeans.modules.javaee.wildfly.customizer;



import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;


/**
 * JBoss instance customizer which is accessible from server manager.
 *
 *
 */
public class Customizer extends JTabbedPane {

    private static final String CLASSPATH = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;
    private static final String SOURCES = J2eeLibraryTypeProvider.VOLUME_TYPE_SRC;
    private static final String JAVADOC = J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC;

    private final J2eePlatformImpl platform;
    private final CustomizerDataSupport custData;
    private final DeploymentManager dmp;

    public Customizer(DeploymentManager dmp, CustomizerDataSupport custData, J2eePlatformImpl platform) {
        this.dmp = dmp;
        this.custData = custData;
        this.platform = platform;
        initComponents ();
    }

    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer"));
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer"));
        // set help ID according to selected tab
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                String helpID = null;
                switch (getSelectedIndex()) {
                    case 0 : helpID = "jboss_customizer_platform";  // NOI18N
                             break;
                    case 1 : helpID = "jboss_customizer_platform";  // NOI18N
                             break;
                    case 2 : helpID = "jboss_customizer_classes";   // NOI18N
                             break;
                    case 3 : helpID = "jboss_customizer_sources";   // NOI18N
                             break;
                    case 4 : helpID = "jboss_customizer_javadoc";   // NOI18N
                             break;
                    default:
                             break;
                }
                putClientProperty("HelpID", helpID); // NOI18N
            }
        });
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Platform"), new WildflyTabVisualPanel(this.dmp));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Platform"), new CustomizerJVM(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Classes"),
               CustomizerSupport.createClassesCustomizer(custData.getClassModel()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Sources"),
                CustomizerSupport.createSourcesCustomizer(custData.getSourceModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Javadoc"),
                CustomizerSupport.createJavadocCustomizer(custData.getJavadocsModel(), null));
    }
}
