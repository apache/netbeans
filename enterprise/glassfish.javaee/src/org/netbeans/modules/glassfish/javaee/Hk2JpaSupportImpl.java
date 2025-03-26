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
package org.netbeans.modules.glassfish.javaee;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;

/**
 * GlassFish server JPA support.
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

    /** GlassFish server JPA provider class. */
    private static final String JPA_PROVIDER
            = "org.eclipse.persistence.jpa.PersistenceProvider";

    // This matrix should be moved to GlassFish Tooling SDK in next
    // major release.
    /**
     * GlassFish JPA support matrix:<p/><table>
     * <tr><th>GlassFish</th><th>JPA 1.0</th><th>JPA 2.0</th><th>JPA 2.1</th>
     * <th>JPA 2.2</th><th>JPA 3.0</th><th>JPA 3.1</th><th>JPA 3.2</th></tr>
     * <tr><th>V1</th><td>YES</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V2</th><td>YES</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V3</th><td>YES</td><td>YES</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V4</th><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>NO</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V5</th><td>YES</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V6</th><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>YES</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V7</th><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>YES</td><td>YES</td><td>NO</td></tr>
     * <tr><th>V8</th><td>NO</td><td>NO</td><td>NO</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td></tr>
     * </table>
     */
    private static final JpaSupportVector jpaSupport[]
            = new JpaSupportVector[GlassFishVersion.length];

    // Initialize GlassFish JPA support matrix.
    static {
        for (GlassFishVersion version : GlassFishVersion.values()) {
            jpaSupport[version.ordinal()] = new JpaSupportVector(
                    GlassFishVersion.lt(version, GlassFishVersion.GF_6),
                    GlassFishVersion.lt(version, GlassFishVersion.GF_6) && GlassFishVersion.ge(version, GlassFishVersion.GF_3),
                    GlassFishVersion.lt(version, GlassFishVersion.GF_6) && GlassFishVersion.ge(version, GlassFishVersion.GF_4),
                    GlassFishVersion.lt(version, GlassFishVersion.GF_6) && GlassFishVersion.ge(version, GlassFishVersion.GF_5),
                    GlassFishVersion.lt(version, GlassFishVersion.GF_7_0_0) && GlassFishVersion.ge(version, GlassFishVersion.GF_6),
                    GlassFishVersion.lt(version, GlassFishVersion.GF_8_0_0) && GlassFishVersion.ge(version, GlassFishVersion.GF_7_0_0),
                    GlassFishVersion.ge(version, GlassFishVersion.GF_8_0_0)
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server instance. */
    private final GlassFishServer instance;

    /** Default provider instance. */
    private volatile JpaProvider defaultProvider;

    /** {@see Set} of available provider instances. */
    private volatile Set<JpaProvider> providers = null;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server JPA support.
     * <p/>
     */
    Hk2JpaSupportImpl(GlassFishServer instance) {
        this.instance = instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // JpaSupportImplementation methods                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns GlassFish server JPA providers.
     * <p/>
     * @return GlassFish server JPA providers.
     */
    @Override
    public Set<JpaProvider> getProviders() {
        if (providers != null) {
            return providers;
        }
        synchronized(this) {
            if (providers == null) {
                Set<JpaProvider> newProviders = new HashSet<JpaProvider>();
                newProviders.add(getDefaultProvider());
                providers = newProviders;
            }
        }
        return providers;
    }

    /**
     * Returns default GlassFish server JPA provider.
     * <p/>
     * @return Default GlassFish server JPA provider.
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
                        = jpaSupport[instance.getVersion() != null
                            ? instance.getVersion().ordinal()
                            : GlassFishVersion.GF_1.ordinal()];
                        defaultProvider = JpaProviderFactory.createJpaProvider(
                                JPA_PROVIDER,
                                true,
                                instanceJpaSupport._1_0,
                                instanceJpaSupport._2_0,
                                instanceJpaSupport._2_1,
                                instanceJpaSupport._2_2,
                                instanceJpaSupport._3_0,
                                instanceJpaSupport._3_1,
                                instanceJpaSupport._3_2);
            }
        }
        return defaultProvider;
    }
    
}
