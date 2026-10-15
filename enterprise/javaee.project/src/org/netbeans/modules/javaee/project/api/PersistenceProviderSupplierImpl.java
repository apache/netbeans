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

package org.netbeans.modules.javaee.project.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;

/**
 * Common implementation of {@link PersistenceProviderSupplier}. Any project type
 * which need to use {@link PersistenceProviderSupplier} should either put the
 * instance to the project lookup or better subclass this one and use
 * {@link org.netbeans.spi.project.ProjectServiceProvider} for correct registration.
 *
 * @author Martin Janicek
 */
public final class PersistenceProviderSupplierImpl implements PersistenceProviderSupplier {

    private final Project project;


    public PersistenceProviderSupplierImpl(Project project) {
        this.project = project;
    }

    @Override
    public List<Provider> getSupportedProviders() {
        try {
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            ServerInstance si = serverInstanceId != null ? Deployment.getDefault().getServerInstance(serverInstanceId) : null;
            J2eePlatform platform = null;
            if (si != null) {
                platform = si.getJ2eePlatform();
            }
            return findPersistenceProviders(platform);
        } catch (InstanceRemovedException ex) {
            return findPersistenceProviders(null);
        }
    }
    
    private List<Provider> findPersistenceProviders(J2eePlatform platform) {
        final List<Provider> providers = new ArrayList<Provider>();
        boolean lessEE7 = true;//we may not know platform
        if(platform != null) {
            final Map<String, JpaProvider> jpaProviderMap = createProviderMap(platform);

            boolean defaultFound = false; // see issue #225071
            for (Profile profile: platform.getSupportedProfiles()) {
                if (profile.isAtLeast(Profile.JAVA_EE_7_WEB)) {
                    lessEE7 = false; //we know gf4 do not support old providers, #233726
                    break;
                }
            }
            
            // Here we are mapping the JpaProvider to the correct Provider
            for (Provider provider : ProviderUtil.getAllProviders()) {

                // Find JpaProvider for corespond Provider --> we are using concrete class for that
                JpaProvider jpa = jpaProviderMap.get(provider.getProviderClass());
                if (jpa != null) {
                    String version = ProviderUtil.getVersion(provider);
                    if (version == null
                            || (version.equals(Persistence.VERSION_3_2) && jpa.isJpa32Supported())
                            || (version.equals(Persistence.VERSION_3_1) && jpa.isJpa31Supported())
                            || (version.equals(Persistence.VERSION_3_0) && jpa.isJpa30Supported())
                            || (version.equals(Persistence.VERSION_2_2) && jpa.isJpa22Supported())
                            || (version.equals(Persistence.VERSION_2_1) && jpa.isJpa21Supported())
                            || (version.equals(Persistence.VERSION_2_0) && jpa.isJpa2Supported() && lessEE7)
                            || (version.equals(Persistence.VERSION_1_0) && jpa.isJpa1Supported()) && lessEE7) {

                        if (jpa.isDefault() && !defaultFound) {
                            providers.add(0, provider);
                            defaultFound = true;
                        } else {
                            providers.add(provider);
                        }
                    }
                }
            }
        }
        for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()){
            boolean found = false;
            for (int i = 0; i < providers.size(); i++) {
                Object elem = providers.get(i);
                if (elem instanceof Provider && each.equals(elem)){
                    found = true;
                    break;
                }
            }
            if (!found){
                String version = ProviderUtil.getVersion(each);
                // we know gf4 do not support old providers, #233726, todo, we need to get supported from gf plugin instead
                if(lessEE7 || version == null 
                        || version.equals(Persistence.VERSION_2_1) 
                        || version.equals(Persistence.VERSION_2_2) 
                        || version.equals(Persistence.VERSION_3_0) 
                        || version.equals(Persistence.VERSION_3_1)
                        || version.equals(Persistence.VERSION_3_2)) {
                    providers.add(each);
                }
            }
        }
        return providers;
    }

    private Map<String, JpaProvider> createProviderMap(J2eePlatform platform) {
        final JpaSupport jpaSupport = JpaSupport.getInstance(platform);
        final Map<String, JpaProvider> providerMap = new HashMap<String, JpaProvider>();

        for (JpaProvider provider : jpaSupport.getProviders()) {
            providerMap.put(provider.getClassName(), provider);
        }

        JpaProvider defaultProvider = jpaSupport.getDefaultProvider();
        if (defaultProvider != null) {
            providerMap.put(defaultProvider.getClassName(), defaultProvider);
        }

        return providerMap;
    }

    @Override
    public boolean supportsDefaultProvider() {
        final J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        if (capabilities != null) {
            return capabilities.hasDefaultPersistenceProvider();
        }
        return false;
    }
}
