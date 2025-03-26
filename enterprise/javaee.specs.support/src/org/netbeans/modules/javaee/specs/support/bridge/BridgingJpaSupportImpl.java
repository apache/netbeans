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
package org.netbeans.modules.javaee.specs.support.bridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;

/**
 *
 * @author Petr Hejl
 */
public class BridgingJpaSupportImpl implements JpaSupportImplementation {

    private final J2eePlatform platform;

    public BridgingJpaSupportImpl(J2eePlatform platform) {
        this.platform = platform;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        for (JpaProvider provider : getProviders()) {
            if (provider.isDefault()) {
                return provider;
            }
        }
        return null;
    }

    @Override
    public Set<JpaProvider> getProviders() {
        Set<JpaProvider> result = new HashSet<JpaProvider>();
        boolean check = platform.isToolSupported(JPAModuleInfo.JPACHECKSUPPORTED);
        boolean jpa1 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_1_0);
        boolean jpa2 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_2_0);
        boolean jpa21 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_2_1);
        boolean jpa22 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_2_2);
        boolean jpa30 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_3_0);
        boolean jpa31 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_3_1);
        boolean jpa32 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_3_2);
        
        for (Map.Entry<Provider, String> entry : getPossibleContainerProviders().entrySet()) {
            Provider provider = entry.getKey();
            if (platform.isToolSupported(provider.getProviderClass())) {
                JpaProvider jpaProvider = JpaProviderFactory.createJpaProvider(
                        provider.getProviderClass(), 
                        platform.isToolSupported(entry.getValue()), 
                        jpa1, jpa2, jpa21, jpa22, jpa30, jpa31, jpa32);
                result.add(jpaProvider);
            }
        }
        return result;
    }
    
    // TODO: Add missing JPA 3.x providers
    private static Map<Provider, String> getPossibleContainerProviders() {
        Map<Provider, String> candidates = new HashMap<>();
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER1_0, "hibernatePersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER2_0, "hibernatePersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER2_1, "hibernatePersistenceProviderIsDefault2.1"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER2_2, "hibernatePersistenceProviderIsDefault2.2"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER3_0, "hibernatePersistenceProviderIsDefault3.0"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER3_1, "hibernatePersistenceProviderIsDefault3.1"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER3_2, "hibernatePersistenceProviderIsDefault3.2"); // NOI18N
        candidates.put(ProviderUtil.TOPLINK_PROVIDER1_0, "toplinkPersistenceProviderIsDefault"); // NOI18N
        candidates.put(ProviderUtil.KODO_PROVIDER, "kodoPersistenceProviderIsDefault"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER1_0, "dataNucleusPersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER2_0, "dataNucleusPersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER2_1, "dataNucleusPersistenceProviderIsDefault2.1"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER2_2, "dataNucleusPersistenceProviderIsDefault2.2"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER3_0, "dataNucleusPersistenceProviderIsDefault3.0"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER3_1, "dataNucleusPersistenceProviderIsDefault3.1"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER3_2, "dataNucleusPersistenceProviderIsDefault3.2"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER1_0, "openJpaPersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER2_0, "openJpaPersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER2_1, "openJpaPersistenceProviderIsDefault2.1"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER2_2, "openJpaPersistenceProviderIsDefault2.2"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER1_0, "eclipseLinkPersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER2_0, "eclipseLinkPersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER2_1, "eclipseLinkPersistenceProviderIsDefault2.1"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER2_2, "eclipseLinkPersistenceProviderIsDefault2.2"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER3_0, "eclipseLinkPersistenceProviderIsDefault3.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER3_1, "eclipseLinkPersistenceProviderIsDefault3.1"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER3_2, "eclipseLinkPersistenceProviderIsDefault3.2"); // NOI18N
        return candidates;
    }
}
