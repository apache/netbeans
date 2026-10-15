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
package org.netbeans.modules.tomcat5.j2ee;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;

/**
 * This is TomEE only class. TomEE PluME support two implementations: {@code OpenJPA}
 * and {@code EclipseLink}, </p> every other TomEE flavor only support {@code OpenJPA}
 * @author Petr Hejl
 * @author José Contreras
 */
class JpaSupportImpl implements JpaSupportImplementation {

    private static final String OPENJPA_JPA_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl"; // NOI18N
    private static final String ECLIPSELINK_JPA_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N
    private final TomcatManager instance;

    
    JpaSupportImpl(TomcatManager instance) {
        this.instance = instance;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        return JpaProviderFactory.createJpaProvider(
                    OPENJPA_JPA_PROVIDER, 
                    true, 
                    instance.isJpa10(), 
                    instance.isJpa20(), 
                    instance.isJpa21(),
                    instance.isJpa22(),
                    instance.isJpa30(),
                    instance.isJpa31(),
                    instance.isJpa32());
    }

    @Override
    public Set<JpaProvider> getProviders() {
        Set<JpaProvider> providers = new HashSet<>();
        providers.add(JpaProviderFactory.createJpaProvider(
                OPENJPA_JPA_PROVIDER, 
                true, 
                instance.isJpa10(), 
                instance.isJpa20(), 
                instance.isJpa21(),
                instance.isJpa22(),
                instance.isJpa30(),
                instance.isJpa31(),
                instance.isJpa32()));
        // TomEE PluME has Eclipselink and OpenJPA
        if (instance.isTomEEplume()) {
            providers.add(JpaProviderFactory.createJpaProvider(
                    ECLIPSELINK_JPA_PROVIDER, 
                    true, 
                    instance.isJpa10(), 
                    instance.isJpa20(), 
                    instance.isJpa21(),
                    instance.isJpa22(),
                    instance.isJpa30(),
                    instance.isJpa31(),
                    instance.isJpa32()));
        }
        return providers;
    }

}
