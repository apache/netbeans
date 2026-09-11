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
package org.netbeans.modules.payara.server.maven.project;

import org.netbeans.api.project.Project;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Adds {@link ServerMavenApplicationContent} to every Maven WAR project, plus
 * a dynamic extra lookup that receives a {@link ServerMavenIcon} only when the
 * project is detected as a payara-server-maven-plugin project. This ensures the
 * icon only appears in server-maven project lookups and never interferes with
 * Micro or plain WAR project icon resolution.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
@LookupProvider.Registration(projectType = MAVEN_WAR_PROJECT_TYPE)
public class LookupProviderImpl implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup lookup) {
        Project project = lookup.lookup(Project.class);
        ServerMavenApplicationContent content = new ServerMavenApplicationContent(project);
        return new ProxyLookup(Lookups.singleton(content), content.getExtraLookup());
    }
}
