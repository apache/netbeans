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

package org.netbeans.spi.autoupdate;

import java.security.KeyStore;

/** Registers <code>java.security.KeyStore</code> into Autoupdate infrastructure.
 * This KeyStore is used for verification of downloaded NBMs. The system finds out
 * the KeyStoreProvider in <code>Lookup.getDefault()<code>.
 * The <code>KeyStoreProvider</code> should be registered using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * @author Jiri Rechtacek
 */
public interface KeyStoreProvider {
    /**
     * TrustLevel describes the level of trust, that a {@link KeyStoreProvider}
     * assigns to the provided keystore.
     *
     * @since 1.61
     */
    public enum TrustLevel {
        /**
         * Unlimited trust - modules signed with certificates in this store
         * will be installed without further user requests. This level is by
         * default used for the update centers of the IDE itself.
         */
        TRUST,
        /**
         * Unlimited trust - modules signed with certificates in this store
         * will be installed without further user requests. This level is by
         * default used for the update centers of the IDE itself. It differes
         * from {@link TRUST} in that, these certificates are subject to a
         * {@code CertPathValidator}
         */
        TRUST_CA,
        /**
         * Plugins signed with certificates from this store will show up as
         * "Signed and valid".
         */
        VALIDATE,
        /**
         * Plugins signed with certificates created by these certificates
         * will show up as "Signed and valid". While certificates provided by
         * {@link VALIDATE} not subject to PKIX checking, these certificates
         * are run through a {@code CertPathValidator}.
         */
        VALIDATE_CA
    }

    /**
     * @return KeyStore
     */
    public KeyStore getKeyStore ();

    /**
     * @return TrustLevel that is provided by the keystore this
     *         {@link KeyStoreProvider} provides
     * @since 1.61
     */
    default TrustLevel getTrustLevel() {
        return TrustLevel.TRUST;
    }
}
