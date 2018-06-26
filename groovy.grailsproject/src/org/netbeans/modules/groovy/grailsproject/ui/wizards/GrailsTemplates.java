/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
