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
    "KnockoutJSGameList=Knockout.js must-play games list"
})
@TemplateRegistration(
    position = 1100,
    folder = "Project/Samples/HTML5",
    displayName = "#KnockoutJSGameList",
    iconBase = "org/netbeans/modules/web/client/samples/resources/HTML5_project_icon.png",
    description = "/org/netbeans/modules/web/client/samples/resources/KnockoutJSGameList.html"
)
public class KnockoutJSGameList extends OnlineSampleWizardIterator {

    @Override
    protected OnlineSiteTemplate getSiteTemplate() {
        return new KnockoutJSGameListTemplate(getProjectName(), getProjectZipURL(), "KnockoutJS.Tips-master.zip"); // NOI18N
    }

    @Override
    protected String getProjectName() {
        return "KnockoutJSGameList"; // NOI18N
    }

    @Override
    protected String getProjectZipURL() {
        return "https://github.com/bernardobrezende/KnockoutJS.Tips/archive/master.zip"; // NOI18N
    }

    private static class KnockoutJSGameListTemplate extends OnlineSiteTemplate {

        public KnockoutJSGameListTemplate(String name, String url, String zipName) {
            super(name, url, zipName);
        }

        @Override
        public void configure(CreateProjectProperties projectProperties) {
            projectProperties.setSiteRootFolder("src") // NOI18N
                    .setStartFile("0-iteratingwithdivs.html"); // NOI18N
        }

        @Override
        protected FileObject getTargetDir(FileObject projectDir, CreateProjectProperties projectProperties) {
            return projectDir;
        }
    }
}
