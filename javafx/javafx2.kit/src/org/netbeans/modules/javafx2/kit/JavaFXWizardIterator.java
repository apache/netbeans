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
package org.netbeans.modules.javafx2.kit;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;

public class JavaFXWizardIterator {
    static final String JAVAFX_SAMPLES_TEMPLATE_FOLDER = "Project/Samples/JavaFXMaven";

//    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=920, displayName="#LBL_Maven_JavaFx_Archetype", iconBase="org/netbeans/modules/maven/resources/jaricon.png", description="javafx.html")
//    @Messages("LBL_Maven_JavaFx_Archetype=JavaFX Application")
//    public static WizardDescriptor.InstantiatingIterator<?> javafx() {
//        return ArchetypeWizards.definedArchetype("org.codehaus.mojo.archetypes", "javafx", "0.6", null, LBL_Maven_JavaFx_Archetype());
//    }
//    @TemplateRegistration(folder=JAVAFX_SAMPLES_TEMPLATE_FOLDER, position=2450, displayName="#LBL_Maven_JavaFx_Sample_Archetype", iconBase="org/netbeans/modules/maven/resources/jaricon.png", description="javafx.html")
//    @Messages("LBL_Maven_JavaFx_Sample_Archetype=Maven FXML MigPane Sample")
//    public static WizardDescriptor.InstantiatingIterator<?> javafxSample() {
//        return ArchetypeWizards.definedArchetype("org.codehaus.mojo.archetypes", "sample-javafx", "0.5", null, LBL_Maven_JavaFx_Sample_Archetype());
//    }
    @TemplateRegistration(folder = ArchetypeWizards.TEMPLATE_FOLDER, position = 925, displayName = "#LBL_Maven_FXML_Archetype", iconBase = "org/netbeans/modules/javafx2/kit/resources/jaricon.png", description = "javafx.html")
    @Messages("LBL_Maven_FXML_Archetype=FXML JavaFX Maven Archetype")
    public static WizardDescriptor.InstantiatingIterator<?> openJFXFML() {
        return definedFXArchetype("com.raelity.jfx", "javafx-archetype-fxml-netbeans", "0.0.4", Bundle.LBL_Maven_FXML_Archetype());
    }

    @TemplateRegistration(folder = ArchetypeWizards.TEMPLATE_FOLDER, position = 926, displayName = "#LBL_Maven_Simple_Archetype", iconBase = "org/netbeans/modules/javafx2/kit/resources/jaricon.png", description = "javafx.html")
    @Messages("LBL_Maven_Simple_Archetype=Simple JavaFX Maven Archetype")
    public static WizardDescriptor.InstantiatingIterator<?> openJFXSimple() {
        return definedFXArchetype("com.raelity.jfx", "javafx-archetype-simple-netbeans", "0.0.4", Bundle.LBL_Maven_Simple_Archetype());
    }

    private static WizardDescriptor.InstantiatingIterator<?> definedFXArchetype(String g, String a, String v, String name) {
        Map<String, String> props = new HashMap<>();
        props.put("add-debug-configuration", "Y");
        return ArchetypeWizards.definedArchetype(g, a, v, null, name, props);
    }
    
}
