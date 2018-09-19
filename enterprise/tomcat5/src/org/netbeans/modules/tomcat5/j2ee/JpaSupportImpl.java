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

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;

/**
 * This is TomEE only class.
 * @author Petr Hejl
 */
public class JpaSupportImpl implements JpaSupportImplementation {

    private static final String OPENJPA_JPA_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl"; // NOI18N

    public JpaSupportImpl() {
        super();
    }

    @Override
    public JpaProvider getDefaultProvider() {
        return JpaProviderFactory.createJpaProvider(OPENJPA_JPA_PROVIDER, true, true, true, false);
    }

    @Override
    public Set<JpaProvider> getProviders() {
        return Collections.singleton(getDefaultProvider());
    }

}
