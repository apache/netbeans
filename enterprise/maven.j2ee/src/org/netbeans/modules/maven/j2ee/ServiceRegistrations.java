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

package org.netbeans.modules.maven.j2ee;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolverFactory;
import org.netbeans.modules.javaee.project.api.PersistenceProviderSupplierImpl;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolver;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.browser.spi.URLDisplayerImplementation;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Martin Janicek
 */
public final class ServiceRegistrations {

    private ServiceRegistrations() {
    }


    @ProjectServiceProvider(
        service =
            PersistenceProviderSupplier.class,
        projectType = {
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
        }
    )
    public static PersistenceProviderSupplier createPersistenceProviderSupplier(Project project) {
        return new PersistenceProviderSupplierImpl(project);
    }

    @ProjectServiceProvider(
        service =
            EntityManagerGenerationStrategyResolver.class,
        projectType = {
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB
        }
    )
    public static EntityManagerGenerationStrategyResolver createEntityManagerGenerationStrategyResolver(Project project) {
        return EntityManagerGenerationStrategyResolverFactory.createInstance(project);
    }

    @ProjectServiceProvider(
        service = {
            ServerURLMappingImplementation.class,
            URLDisplayerImplementation.class,
            PageInspectorCustomizer.class
        },
        projectType = {
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
        }
    )
    public static ClientSideDevelopmentSupport createClientSideSupport(Project project) {
        return ClientSideDevelopmentSupport.createInstance(project, 
                "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
                "org.netbeans.ui.metrics.maven");
    }
}
