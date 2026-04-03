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
package org.netbeans.modules.j2ee.jboss4.ide;

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
    private final JBJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    public JpaSupportImpl(JBJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        final boolean isDefault = true, isJpa10 = true;
        boolean isJpa20 = platformImpl.isJpa2Available();
        final boolean isJpa21 = false, isJpa22 = false, isJpa30 = false, isJpa31 = false, isJpa32 = false, isJpa40 = false;
        return JpaProviderFactory.createJpaProvider(defaultProvider, isDefault, 
                isJpa10, isJpa20, isJpa21, isJpa22, isJpa30, isJpa31, isJpa32, isJpa40);
    }

    @Override
    public Set<JpaProvider> getProviders() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        final boolean isJpa10 = true;
        boolean isJpa20 = platformImpl.isJpa2Available();
        final boolean isJpa21 = false, isJpa22 = false, isJpa30 = false, isJpa31 = false, isJpa32 = false, isJpa40 = false;
        Set<JpaProvider> providers = new HashSet<>(8);
        if (platformImpl.containsPersistenceProvider(JBJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(
                    JBJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER, 
                    JBJ2eePlatformFactory.HIBERNATE_JPA_PROVIDER.equals(defaultProvider),
                    isJpa10, isJpa20, isJpa21, isJpa22, isJpa30, isJpa31, isJpa32, isJpa40));
        }
        if (platformImpl.containsPersistenceProvider(JBJ2eePlatformFactory.TOPLINK_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(
                    JBJ2eePlatformFactory.TOPLINK_JPA_PROVIDER, 
                    JBJ2eePlatformFactory.TOPLINK_JPA_PROVIDER.equals(defaultProvider), 
                    isJpa10, isJpa20, isJpa21, isJpa22, isJpa30, isJpa31, isJpa32, isJpa40));
        }
        if (platformImpl.containsPersistenceProvider(JBJ2eePlatformFactory.KODO_JPA_PROVIDER)) {
            providers.add(JpaProviderFactory.createJpaProvider(
                    JBJ2eePlatformFactory.KODO_JPA_PROVIDER, 
                    JBJ2eePlatformFactory.KODO_JPA_PROVIDER.equals(defaultProvider), 
                    isJpa10, isJpa20, isJpa21, isJpa22, isJpa30, isJpa31, isJpa32, isJpa40));
        }
        return providers;
    }

}
