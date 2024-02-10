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

package org.netbeans.modules.updatecenters.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Jiri Rechtacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.autoupdate.KeyStoreProvider.class)
public final class NetBeansKeyStoreProvider implements KeyStoreProvider {
    
    public static final String KS_FILE_PATH = "core/ide.ks";
    private static final String KS_DEFAULT_PASSWORD = "open4all";
    
    @Override
    public KeyStore getKeyStore() {
        return getKeyStore (getKeyStoreFile (), getPassword ());
    }
    
    private static File getKeyStoreFile () {
        File ksFileLocated = InstalledFileLocator.getDefault ().locate(KS_FILE_PATH, "org.netbeans.modules.updatecenters", true);
        assert ksFileLocated != null : "File found at " + KS_FILE_PATH;
        return ksFileLocated;
    }

    /** Creates keystore and loads data from file.
    * @param filename - name of the keystore
    * @param password
    */
    private static KeyStore getKeyStore(File file, String password) {
        if (file == null) {
            return null;
        }
        try (InputStream is = new FileInputStream(file)) {
            KeyStore keyStore = KeyStore.getInstance (KeyStore.getDefaultType());
            keyStore.load (is, password.toCharArray());
            return keyStore;
        } catch (Exception ex) {
            Logger.getLogger ("global").log (Level.INFO, ex.getMessage (), ex);
        }
        return null;
    }
    
    private static String getPassword () {
        String password = KS_DEFAULT_PASSWORD;
        //XXX: read password from bundle
        return password;
    }

}
