/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.updatecenters.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        if (file == null) return null;
        KeyStore keyStore = null;
        InputStream is = null;
        
        try {

            is = new FileInputStream (file);

            keyStore = KeyStore.getInstance (KeyStore.getDefaultType ());
            keyStore.load (is, password.toCharArray ());
            
        } catch (Exception ex) {
            Logger.getLogger ("global").log (Level.INFO, ex.getMessage (), ex);
        } finally {
            try {
                if (is != null) is.close ();
            } catch (IOException ex) {
                assert false : ex;
            }
        }

        return keyStore;
    }
    
    private static String getPassword () {
        String password = KS_DEFAULT_PASSWORD;
        //XXX: read password from bundle
        return password;
    }

}
