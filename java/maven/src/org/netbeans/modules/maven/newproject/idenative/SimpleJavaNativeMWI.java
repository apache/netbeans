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

package org.netbeans.modules.maven.newproject.idenative;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.LBL_Maven_Quickstart_Archetype;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
@TemplateRegistration(
    id = "JavaApp/",
    displayName="#LBL_Maven_Quickstart_Archetype",
    iconBase="org/netbeans/modules/maven/resources/jaricon.png",
    description="quickstart.html",
    folder=ArchetypeWizards.TEMPLATE_FOLDER,
    position=100,
    createHandlerClass = SimpleJavaTemplateHandler.class
)
@Messages("LBL_Maven_Quickstart_Archetype=Java Application")
public class SimpleJavaNativeMWI extends IDENativeMavenWizardIterator {

    public SimpleJavaNativeMWI() {
        // no way how to describe packaging and log on the template; otherwise this class could be removed.
        super(LBL_Maven_Quickstart_Archetype(), "org.apache.maven.archetypes:maven-archetype-quickstart:1.1", "jar");
    }
}
