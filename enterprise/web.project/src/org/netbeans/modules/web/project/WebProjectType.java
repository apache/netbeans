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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.project.spi.WebProjectImplementationFactory;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Lookup;

public final class WebProjectType {

    public static final String TYPE = "org.netbeans.modules.web.project";
    private static final String PROJECT_CONFIGURATION_NAME = "data";
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project/3";
    private static final String PRIVATE_CONFIGURATION_NAME = "data";
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project-private/1";
    
    private static final String[] PROJECT_CONFIGURATION_NAMESPACE_LIST =
            {"http://www.netbeans.org/ns/web-project/1",
            "http://www.netbeans.org/ns/web-project/2",
            "http://www.netbeans.org/ns/web-project/3"};

    /** Do nothing, just a service. */
    private WebProjectType() {}
    
    @AntBasedProjectRegistration(
        iconResource="org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif", // NOI18N
        type=TYPE,
        sharedName=PROJECT_CONFIGURATION_NAME,
        sharedNamespace=PROJECT_CONFIGURATION_NAMESPACE,
        privateName=PRIVATE_CONFIGURATION_NAME,
        privateNamespace=PRIVATE_CONFIGURATION_NAMESPACE
    )
    public static Project createProject(AntProjectHelper helper) throws IOException {
        for(WebProjectImplementationFactory factory : getProjectFactories()) {
            if (factory.acceptProject(helper)) {
                //delegate project completely to another implementation
                return factory.createProject(helper);
            }
        }
        return new WebProject(helper);
    }

    private static Collection<? extends WebProjectImplementationFactory> getProjectFactories() {
        return Lookup.getDefault().lookupAll(WebProjectImplementationFactory.class);
    }

    public static String[] getConfigurationNamespaceList() {
        return PROJECT_CONFIGURATION_NAMESPACE_LIST.clone();
    }
}
