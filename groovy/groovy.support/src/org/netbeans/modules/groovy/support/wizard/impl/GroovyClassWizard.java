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

package org.netbeans.modules.groovy.support.wizard.impl;

import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.modules.groovy.support.wizard.AbstractGroovyWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
@NbBundle.Messages(value = {
    "LBL_DisplayName_GroovyClass=Groovy Class",
    "LBL_DisplayName_GroovyScript=Groovy Script",
    "LBL_DisplayName_GroovyTrait=Groovy Trait"
})
@TemplateRegistrations(value = {
    @TemplateRegistration(
        folder = "Groovy",
        position = 100,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyClass.groovy",
        scriptEngine = "freemarker",
        displayName = "#LBL_DisplayName_GroovyClass",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyClass.html",
        category = {
            "groovy",
            "java-main-class"
        }
    ),

    @TemplateRegistration(
        folder = "Groovy",
        position = 110,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyScript.groovy",
        scriptEngine = "freemarker",
        displayName = "#LBL_DisplayName_GroovyScript",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyScript.html",
        category = {
            "groovy",
            "java-main-class"
        }
    ),
    @TemplateRegistration(
        folder = "Groovy",
        position = 120,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyTrait.groovy",
        scriptEngine = "freemarker",
        displayName = "#LBL_DisplayName_GroovyTrait",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyTrait.html",
        category = {
            "groovy",
            "java-main-class"
        }
    )
})
public class GroovyClassWizard extends AbstractGroovyWizard {

    @Override
    protected List<SourceGroup> getSourceGroups() {
        List<SourceGroup> sourceGroups = retrieveGroups();

        if (!strategy.existsGroovySourceFolder(sourceGroups)) {
            strategy.createGroovySourceFolder();

            // Retrieve the source groups again, but now with a newly created /test/groovy folder
            sourceGroups = retrieveGroups();
        }
        return strategy.moveSourceFolderAsFirst(sourceGroups);
    }
}
