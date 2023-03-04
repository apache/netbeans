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
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
@NbBundle.Messages({
    "EmberJSTrek=Ember.js Trek Tutorial"
})
@TemplateRegistration(
    position = 1000,
    folder = "Project/Samples/HTML5",
    displayName = "#EmberJSTrek",
    iconBase = "org/netbeans/modules/web/client/samples/resources/HTML5_project_icon.png",
    description = "/org/netbeans/modules/web/client/samples/resources/EmberJSTrek.html"
)
public class EmberJSTrek extends OnlineSampleWizardIterator {

    @Override
    protected OnlineSiteTemplate getSiteTemplate() {
        return new OnlineSiteTemplate(getProjectName(), getProjectZipURL(), "trek-ember-tutorial-master.zip"); // NOI18N
    }

    @Override
    protected String getProjectName() {
        return "EmberJSTrek"; // NOI18N
    }

    @Override
    protected String getProjectZipURL() {
        return "https://github.com/jdourlens/trek-ember-tutorial/archive/master.zip"; // NOI18N
    }
}
