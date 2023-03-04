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

package org.netbeans.modules.maven.osgi.templates;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.osgi.templates.Bundle.*;

public class BundleWizard {
    
    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=290, displayName="#template.project.OSGi", iconBase="org/netbeans/modules/maven/osgi/maven_osgi_16.png", description="OSGiDescription.html")
    @Messages("template.project.OSGi=OSGi Bundle")
    public static WizardDescriptor.InstantiatingIterator<?> create() {
        return ArchetypeWizards.definedArchetype("org.codehaus.mojo.archetypes", "osgi-archetype", "1.4", null, template_project_OSGi());
    }

    private BundleWizard() {}

}
