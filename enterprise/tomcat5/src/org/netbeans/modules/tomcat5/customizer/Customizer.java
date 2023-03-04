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
package org.netbeans.modules.tomcat5.customizer;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;

/**
 * Tomcat instance customizer which is accessible from server manager.
 *
 * @author Stepan Herold
 */
public class Customizer extends JTabbedPane {

    private final TomcatManager manager;

    public Customizer(TomcatManager aManager) {
        manager = aManager;
        initComponents ();
    }

    private void initComponents() {
        getAccessibleContext().setAccessibleName (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(Customizer.class,"ACS_Customizer")); // NOI18N
        CustomizerDataSupport custData = new CustomizerDataSupport(manager);
        // set help ID according to selected tab
        addChangeListener( (ChangeEvent e) -> {
            String helpID = null;
            switch (getSelectedIndex()) {
                case 0 : helpID = "tomcat_customizer_general";    // NOI18N
                break;
                case 1 : helpID = "tomcat_customizer_startup";    // NOI18N
                break;
                case 2 : helpID = "tomcat_customizer_platform";    // NOI18N
                break;
                case 3 : helpID = "tomcat_customizer_deployment";   // NOI18N
                break;
                case 4 : helpID = "tomcat_customizer_classes";    // NOI18N
                break;
                case 5 : helpID = "tomcat_customizer_sources";    // NOI18N
                break;
                case 6 : helpID = "tomcat_customizer_javadoc";    // NOI18N
                break;
            }
            putClientProperty("HelpID", helpID); // NOI18N
        });
        addTab(NbBundle.getMessage(Customizer.class,"TXT_General"),
                new CustomizerGeneral(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Startup"),
                new CustomizerStartup(custData, manager.getTomcatProperties().getCatalinaHome()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Platform"),
                new CustomizerJVM(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Deployment"),
                new CustomizerDeployment(custData));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Classes"), 
               CustomizerSupport.createClassesCustomizer(custData.getClassModel()));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Sources"), 
                CustomizerSupport.createSourcesCustomizer(custData.getSourceModel(), null));
        addTab(NbBundle.getMessage(Customizer.class,"TXT_Tab_Javadoc"), 
                CustomizerSupport.createJavadocCustomizer(custData.getJavadocsModel(), null));
    }
}
