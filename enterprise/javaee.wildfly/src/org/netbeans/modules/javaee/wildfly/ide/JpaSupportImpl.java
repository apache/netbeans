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
package org.netbeans.modules.javaee.wildfly.ide;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;

/**
 *
 * @author Petr Hejl
 */
class JpaSupportImpl implements JpaSupportImplementation {
    private final WildflyJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    public JpaSupportImpl(WildflyJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        final boolean isDefault = true;
        final boolean isJpa10 = true, isJpa20 = true, isJpa21 = true, isJpa22 = true;
        final boolean isJpa30 = true, isJpa31 = true, isJpa32 = true, isJpa40 = true;
        return JpaProviderFactory.createJpaProvider(defaultProvider, isDefault, 
                isJpa10, isJpa20, isJpa21, isJpa22, isJpa30, isJpa31, isJpa32, isJpa40);
    }

    @Override
    public Set<JpaProvider> getProviders() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        Set<JpaProvider> providers = new HashSet<>();
        if (platformImpl.containsPersistenceProvider(WildflyJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(WildflyJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER, 
                    WildflyJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER.equals(defaultProvider), true, true, true, true, true, true, true, true));
        }
        if (platformImpl.containsPersistenceProvider(WildflyJ2eePlatformFactory.TOPLINK_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(WildflyJ2eePlatformFactory.TOPLINK_JPA_PROVIDER, 
                    WildflyJ2eePlatformFactory.TOPLINK_JPA_PROVIDER.equals(defaultProvider), true, false, false, false, false, false, false, false));
        }
        if (platformImpl.containsPersistenceProvider(WildflyJ2eePlatformFactory.KODO_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(WildflyJ2eePlatformFactory.KODO_JPA_PROVIDER, 
                    WildflyJ2eePlatformFactory.KODO_JPA_PROVIDER.equals(defaultProvider), true, false, false, false, false, false, false, false));
        }
        return providers;
    }

}
