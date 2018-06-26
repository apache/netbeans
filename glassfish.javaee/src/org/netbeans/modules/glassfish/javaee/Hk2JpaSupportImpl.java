/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
         * @param jpa_2_0 JPA 1.0 supported.
         * @param jpa_2_1 JPA 2.1 supported.
         */
        JpaSupportVector(boolean jpa_1_0, boolean jpa_2_0, boolean jpa_2_1) {
            _1_0 = jpa_1_0;
            _2_0 = jpa_2_0;
            _2_1 = jpa_2_1;
        }

        /** JPA 1.0 supported. */
        boolean _1_0;

        /** JPA 1.0 supported. */
        boolean _2_0;

        /** JPA 2.1 supported. */
        boolean _2_1;
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
     * <tr><th>GlassFish</th><th>JPA 1.0</th><th>JPA 2.0</th><th>JPA 2.1</th></tr>
     * <tr><th>V1</th><td>YES</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V2</th><td>YES</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V3</th><td>YES</td><td>YES</td><td>NO</td></tr>
     * <tr><th>V4</th><td>YES</td><td>YES</td><td>YES</td></tr>
     * </table>
     */
    private static final JpaSupportVector jpaSupport[]
            = new JpaSupportVector[GlassFishVersion.length];

    // Initialize GlassFish JPA support matrix.
    static {
        for (GlassFishVersion version : GlassFishVersion.values()) {
            jpaSupport[version.ordinal()] = new JpaSupportVector(
                    true, version.ordinal() >= GlassFishVersion.GF_3.ordinal(),
                    version.ordinal() >= GlassFishVersion.GF_4.ordinal());
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
                        JPA_PROVIDER, true, instanceJpaSupport._1_0,
                        instanceJpaSupport._2_0, instanceJpaSupport._2_1);
            }
        }
        return defaultProvider;
    }
    
}
