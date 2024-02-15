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
package org.netbeans.modules.maven.embedder.impl;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.sisu.plexus.Roles;

/**
 * this module is meant to be used by the project embedder only
 * @author mkleint
 */
public class ExtensionModule implements Module {

    public ExtensionModule() {
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(PluginDependenciesResolver.class).to(NbPluginDependenciesResolver.class);
        binder.bind(Roles.componentKey(RepositoryConnectorFactory.class, "offline")).to(OfflineConnector.class);
        //#212214 the EnhancedLocalRepositoryManager will claim artifact is not locally present even if file is there but some metadata is missing
        //we just replace it with the simple variant that relies on file's presence only. 
        //I'm a bit afraid to remove the binding altogether, that's why we map simple to enhanced.
        binder.bind(Roles.componentKey(LocalRepositoryManagerFactory.class, "enhanced")).to(SimpleLocalRepositoryManagerFactory.class);
        
        //exxperimental only.
//        binder.bind(InheritanceAssembler.class).to(NbInheritanceAssembler.class);
        binder.bind(ModelBuilder.class).to(NBModelBuilder.class);
        
        // This allows to capture origin for version and artifact queries, so that ArtifactFixer can determine
        // if a pom was really requested, or some other artifact's classifier/type was
        binder.bind(VersionResolver.class).to(NbVersionResolver2.class).in(Scopes.SINGLETON);
        binder.bind(VersionRangeResolver.class).to(NbVersionResolver2.class).in(Scopes.SINGLETON);
        binder.bind(ArtifactDescriptorReader.class).to(NbVersionResolver2.class).in(Scopes.SINGLETON);
    }
    
}
