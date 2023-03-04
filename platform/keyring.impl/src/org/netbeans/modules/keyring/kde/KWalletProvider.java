/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.keyring.kde;

import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.lookup.ServiceProvider;

// #256606
@ServiceProvider(service = KeyringProvider.class, position = 99)
public class KWalletProvider implements KeyringProvider {

    private static final String KWALLET_VERSION = "netbeans.keyring.kwallet.version"; // NOI18N
    private static final String PATH_VERSION = "netbeans.keyring.kwallet.path.version"; // NOI18N

    private KeyringProvider keyringProvider = null;


    @Override
    public boolean enabled() {
        return getKeyringProvider().enabled();
    }

    @Override
    public char[] read(String key) {
        return getKeyringProvider().read(key);
    }

    @Override
    public void save(String key, char[] password, String description) {
        getKeyringProvider().save(key, password, description);
    }

    @Override
    public void delete(String key) {
        getKeyringProvider().delete(key);
    }

    private KeyringProvider getKeyringProvider() {
        if (keyringProvider == null) {
            String kwalletVersion = System.getProperty(KWALLET_VERSION);
            if (kwalletVersion == null) {
                // default is KDE 5 now
                kwalletVersion = "5"; // NOI18N
            } else if (kwalletVersion.equals("4")) { // NOI18N
                // KDE 4 uses just kwallet (no version)
                kwalletVersion = ""; // NOI18N
            }
            String pathVersion = System.getProperty(PATH_VERSION);
            if (pathVersion == null) {
                pathVersion = kwalletVersion;
            }
            keyringProvider = new CommonKWalletProvider(kwalletVersion, pathVersion);
            if (!kwalletVersion.isEmpty()
                    && !pathVersion.isEmpty()
                    && !keyringProvider.enabled()) {
                // fallback to KDE 4
                keyringProvider = new CommonKWalletProvider("", ""); // NOI18N
            }
        }
        return keyringProvider;
    }

}
