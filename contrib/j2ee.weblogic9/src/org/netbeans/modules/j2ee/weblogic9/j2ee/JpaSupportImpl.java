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
package org.netbeans.modules.j2ee.weblogic9.j2ee;

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
    private final WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    public JpaSupportImpl(WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        return JpaProviderFactory.createJpaProvider(defaultProvider, true, true,
                platformImpl.isJpa2Available(), platformImpl.isJpa21Available(), false, false, false, false);
    }

    @Override
    public Set<JpaProvider> getProviders() {
        String defaultProvider = platformImpl.getDefaultJpaProvider();
        Set<JpaProvider> providers = new HashSet<JpaProvider>();
        providers.add(JpaProviderFactory.createJpaProvider(WLJ2eePlatformFactory.OPENJPA_JPA_PROVIDER,
                WLJ2eePlatformFactory.OPENJPA_JPA_PROVIDER.equals(defaultProvider), true, false, false, false, false, false, false));
        providers.add(JpaProviderFactory.createJpaProvider(
                WLJ2eePlatformFactory.ECLIPSELINK_JPA_PROVIDER,
                WLJ2eePlatformFactory.ECLIPSELINK_JPA_PROVIDER.equals(defaultProvider), 
                true, platformImpl.isJpa2Available(), platformImpl.isJpa21Available(), false, false, false, false));
        return providers;
    }

}
