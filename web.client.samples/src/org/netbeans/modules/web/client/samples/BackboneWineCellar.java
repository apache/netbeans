/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.samples;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.client.samples.wizard.iterator.OnlineSampleWizardIterator;
import org.netbeans.modules.web.client.samples.wizard.iterator.OnlineSiteTemplate;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
@NbBundle.Messages({
    "BackboneWineCellar=Backbone.js Wine Cellar"
})
@TemplateRegistration(
    position = 800,
    folder = "Project/Samples/HTML5",
    displayName = "#BackboneWineCellar",
    iconBase = "org/netbeans/modules/web/client/samples/resources/HTML5_project_icon.png",
    description = "/org/netbeans/modules/web/client/samples/resources/BackboneWineCellar.html"
)
public class BackboneWineCellar extends OnlineSampleWizardIterator {

    @Override
    protected OnlineSiteTemplate getSiteTemplate() {
        return new BackboneWineCellarTemplate(getProjectName(), getProjectZipURL(), "backbone-cellar-master.zip"); // NOI18N
    }

    @Override
    protected String getProjectName() {
        return "BackboneWineCellar"; // NOI18N
    }

    @Override
    protected String getProjectZipURL() {
        return "https://github.com/ccoenraets/backbone-cellar/archive/master.zip"; // NOI18N
    }

    private static class BackboneWineCellarTemplate extends OnlineSiteTemplate {

        public BackboneWineCellarTemplate(String name, String url, String zipName) {
            super(name, url, zipName);
        }

        @Override
        public void configure(CreateProjectProperties projectProperties) {
            projectProperties.setSiteRootFolder("bootstrap"); // NOI18N
        }

        @Override
        protected FileObject getTargetDir(FileObject projectDir, CreateProjectProperties projectProperties) {
            return projectDir;
        }
    }
}