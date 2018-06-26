/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server;

import java.util.concurrent.atomic.AtomicReferenceArray;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;

/**
 * GlassFish server JPA support matrix.
 * <p/>
 * @author Tomas Kraus
 */
public class JpaSupport {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Individual JPA specifications support.
     */
    public static class ApiVersion {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** JPA 1.0 supported. */
        final boolean _1_0;

        /** JPA 1.0 supported. */
        final boolean _2_0;

        /** JPA 2.1 supported. */
        final boolean _2_1;

        /** JPA provider class. */
        final String provider;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of individual JPA specifications support class.
         * <p/>
         * @param jpa_1_0  JPA 1.0 supported.
         * @param jpa_2_0  JPA 1.0 supported.
         * @param jpa_2_1  JPA 2.1 supported.
         * @param provider JPA provider class.
         */
        ApiVersion(boolean jpa_1_0, boolean jpa_2_0,
                boolean jpa_2_1, String provider) {
            this._1_0 = jpa_1_0;
            this._2_0 = jpa_2_0;
            this._2_1 = jpa_2_1;
            this.provider = provider;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters and setters                                                //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Is JPA 1.0 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 1.0 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is10() {
            return _1_0;
        }

        /**
         * Is JPA 2.0 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 2.0 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is20() {
            return _2_0;
        }

        /**
         * Is JPA 2.1 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 2.1 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is21() {
            return _2_1;
        }

        /**
         * Get JPA provider class.
         * <p/>
         * @return JPA provider class name.
         */
        public String getProvider() {
            return provider;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server JPA provider class since V1. */
    private static final String JPA_PROVIDER_SINCE_V1
            = "oracle.toplink.essentials.PersistenceProvider";

    /** GlassFish server JPA provider class since V3. */
    private static final String JPA_PROVIDER_SINCE_V3
            = "org.eclipse.persistence.jpa.PersistenceProvider";

    /**
     * GlassFish JPA support matrix:<p/><table>
     * <tr><th>GlassFish</th><th>JPA 1.0</th><th>JPA 2.0</th><th>JPA 2.1</th></tr>
     * <tr><th>V1</th><td>YES</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V2</th><td>YES</td><td>NO</td><td>NO</td></tr>
     * <tr><th>V3</th><td>YES</td><td>YES</td><td>NO</td></tr>
     * <tr><th>V4</th><td>YES</td><td>YES</td><td>YES</td></tr>
     * </table><p/>
     * Array is stored as <code>Object[] array</code> internally which
     * is initialized with all values set to <code>null</code>.
     */
    private static final AtomicReferenceArray<ApiVersion> jpaSupport
            = new AtomicReferenceArray<>(GlassFishVersion.length);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish JPA support information for given GlassFish version.
     * <p/>
     * @param version GlassFish version to get JPA support information for.
     * @return GlassFish JPA support information for given GlassFish version.
     */
    public static ApiVersion getApiVersion(GlassFishVersion version) {
        ApiVersion apiVersion = jpaSupport.get(version.ordinal());
        if (apiVersion != null) {
            return apiVersion;
        }
        synchronized(jpaSupport) {
            apiVersion = jpaSupport.get(version.ordinal());
            if (apiVersion == null) {
                jpaSupport.set(version.ordinal(), new ApiVersion(true,
                        version.ordinal() >= GlassFishVersion.GF_3.ordinal(),
                        version.ordinal() >= GlassFishVersion.GF_4.ordinal(),
                        version.ordinal() < GlassFishVersion.GF_3.ordinal()
                        ? JPA_PROVIDER_SINCE_V1 : JPA_PROVIDER_SINCE_V3));
            }
        }
        return jpaSupport.get(version.ordinal());
    }

}
