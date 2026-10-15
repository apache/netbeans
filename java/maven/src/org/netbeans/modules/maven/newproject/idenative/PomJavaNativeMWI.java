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

package org.netbeans.modules.maven.newproject.idenative;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.LBL_Maven_POM_Archetype;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@TemplateRegistration(
        displayName="#LBL_Maven_POM_Archetype",
        iconBase="org/netbeans/modules/maven/resources/Maven2Icon.gif",
        description="pom-root.html",
        folder=ArchetypeWizards.TEMPLATE_FOLDER,
        position=980,
        createHandlerClass = IDENativeTemplateHandler.class
)
@NbBundle.Messages("LBL_Maven_POM_Archetype=POM Project")
public class PomJavaNativeMWI extends IDENativeMavenWizardIterator {

    public PomJavaNativeMWI() {
        super(LBL_Maven_POM_Archetype(), "org.codehaus.mojo.archetypes:pom-root:1.1", "pom");
    }
}