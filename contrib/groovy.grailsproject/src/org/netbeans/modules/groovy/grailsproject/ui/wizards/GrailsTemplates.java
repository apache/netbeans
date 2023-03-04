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

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public final class GrailsTemplates {

    private GrailsTemplates() {
    }


    @NbBundle.Messages({
        "Service=Grails Service",
        "UnitTest=Grails Unit Test",
        "Controller=Grails Controller",
        "TagLib=Grails Tag Library",
        "GantScript=Grails Gant Script",
        "DomainClass=Grails Domain Class",
        "IntegrationTest=Grails Integration Test"
    })
    @TemplateRegistrations(value = {
        @TemplateRegistration(
            id = "DomainClass",
            position = 130,
            folder = "Groovy",
            category = "groovy",
            displayName = "#DomainClass",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/DomainClass.html"
        ),
        @TemplateRegistration(
            id = "Controller",
            position = 140,
            folder = "Groovy",
            category = "groovy",
            displayName = "#Controller",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/Controller.html"
        ),
        @TemplateRegistration(
            id = "IntegrationTest",
            position = 150,
            folder = "Groovy",
            category = "groovy",
            displayName = "#IntegrationTest",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/IntegrationTest.html"
        ),
        @TemplateRegistration(
            id = "GantScript",
            position = 160,
            folder = "Groovy",
            category = "groovy",
            displayName = "#GantScript",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/GantScript.html"
        ),
        @TemplateRegistration(
            id = "Service",
            position = 170,
            folder = "Groovy",
            category = "groovy",
            displayName = "#Service",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/Service.html"
        ),
        @TemplateRegistration(
            id = "TagLib",
            position = 180,
            folder = "Groovy",
            category = "groovy",
            displayName = "#TagLib",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/TagLib.html"
        ),
        @TemplateRegistration(
            id = "UnitTest",
            position = 190,
            folder = "Groovy",
            category = "groovy",
            displayName = "#UnitTest",
            iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
            description = "/org/netbeans/modules/groovy/grailsproject/resources/UnitTest.html"
        )
    })
    public static WizardDescriptor.InstantiatingIterator createArtifactIterator() {
        return new GrailsArtifactWizardIterator();
    }

    @NbBundle.Messages("GrailsAppDisplayName=Grails Application")
    @TemplateRegistration(
        position = 400,
        folder = "Project/Groovy",
        displayName = "#GrailsAppDisplayName",
        iconBase = "org/netbeans/modules/groovy/grailsproject/resources/GrailsIcon16x16.png",
        description = "/org/netbeans/modules/groovy/grailsproject/resources/emptyProject.html"
    )
    public static WizardDescriptor.InstantiatingIterator create() {
        return new GrailsProjectWizardIterator();
    }
}
