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
package org.netbeans.modules.payara.jakartaee;

import static java.util.Collections.unmodifiableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;

/**
 * Payara server JPA support.
 * <p/>
 * @author Tomas Kraus
 */
public class Hk2JpaSupportImpl implements JpaSupportImplementation {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Individual JPA specifications support.
     */
    private static class JpaSupportVector {

        /**
         * Creates an instance of individual JPA specifications support class.
         * <p/>
         * @param jpa_1_0 JPA 1.0 supported.
         * @param jpa_2_0 JPA 2.0 supported.
         * @param jpa_2_1 JPA 2.1 supported.
         * @param jpa_2_2 JPA 2.2 supported.
         * @param jpa_3_0 JPA 3.0 supported.
         * @param jpa_3_1 JPA 3.1 supported.
         * @param jpa_3_2 JPA 3.2 supported.
         */
        JpaSupportVector(boolean jpa_1_0, boolean jpa_2_0, 
                boolean jpa_2_1, boolean jpa_2_2,
                boolean jpa_3_0, boolean jpa_3_1, boolean jpa_3_2) {
            _1_0 = jpa_1_0;
            _2_0 = jpa_2_0;
            _2_1 = jpa_2_1;
            _2_2 = jpa_2_2;
            _3_0 = jpa_3_0;
            _3_1 = jpa_3_1;
            _3_2 = jpa_3_2;
        }

        /** JPA 1.0 supported. */
        boolean _1_0;

        /** JPA 2.0 supported. */
        boolean _2_0;

        /** JPA 2.1 supported. */
        boolean _2_1;

        /** JPA 2.2 supported. */
        boolean _2_2;

        /** JPA 3.0 supported. */
        boolean _3_0;

        /** JPA 3.1 supported. */
        boolean _3_1;
        /** JPA 3.2 supported. */
        boolean _3_2;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server JPA provider class. */
    private static final String JPA_PROVIDER
            = "org.eclipse.persistence.jpa.PersistenceProvider";

    private static final Map<String, JpaSupportVector> jpaSupport
            = new HashMap<>();

    // Initialize Payara JPA support matrix.
    static {
        for (PayaraPlatformVersionAPI version : PayaraPlatformVersion.getVersions()) {
            jpaSupport.put(
                    version.toString(),
                    new JpaSupportVector(
                            true, true, 
                            version.isEE7Supported(), version.isEE8Supported(), 
                            version.isEE9Supported(), version.isEE10Supported(),
                            false
                    )
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server instance. */
    private final PayaraServer instance;

    /** Default provider instance. */
    private volatile JpaProvider defaultProvider;

    /** {@see Set} of available provider instances. */
    private volatile Set<JpaProvider> providers = null;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara server JPA support.
     * <p/>
     */
    Hk2JpaSupportImpl(PayaraServer instance) {
        this.instance = instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // JpaSupportImplementation methods                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns Payara server JPA providers.
     * <p/>
     * @return Payara server JPA providers.
     */
    @Override
    public Set<JpaProvider> getProviders() {
        if (providers != null) {
            return unmodifiableSet(providers);
        }
        synchronized(this) {
            if (providers == null) {
                Set<JpaProvider> newProviders = new HashSet<JpaProvider>();
                newProviders.add(getDefaultProvider());
                providers = newProviders;
            }
        }
        return unmodifiableSet(providers);
    }

    /**
     * Returns default Payara server JPA provider.
     * <p/>
     * @return Default Payara server JPA provider.
     */
    @Override
    public JpaProvider getDefaultProvider() {
        if (defaultProvider != null) {
            return defaultProvider;
        }
        synchronized(this) {
            if (defaultProvider == null) {
                // Unknown version is as the worst known case.
                JpaSupportVector instanceJpaSupport
                        = jpaSupport.get(instance.getPlatformVersion() != null
                                ? instance.getPlatformVersion().toString()
                                : PayaraPlatformVersion.getLatestVersion().toString());
                defaultProvider = JpaProviderFactory.createJpaProvider(
                    JPA_PROVIDER, true, instanceJpaSupport._1_0,
                    instanceJpaSupport._2_0, instanceJpaSupport._2_1,
                    instanceJpaSupport._2_2, instanceJpaSupport._3_0,
                    instanceJpaSupport._3_1, instanceJpaSupport._3_2);
            }
        }
        return defaultProvider;
    }
    
}
