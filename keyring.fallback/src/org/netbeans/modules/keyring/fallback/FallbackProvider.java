/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.keyring.fallback;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.keyring.utils.Utils;
import org.netbeans.modules.keyring.spi.EncryptionProvider;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Platform-independent keyring provider using a master password and the user directory.
 */
@ServiceProvider(service=KeyringProvider.class, position=1000)
public class FallbackProvider implements KeyringProvider, Callable<Void> {

    private static final Logger LOG = Logger.getLogger(FallbackProvider.class.getName());
    private static final String DESCRIPTION = ".description";
    private static final String SAMPLE_KEY = "__sample__";

    private EncryptionProvider encryption;
 
    @Override
    public boolean enabled() {
        for (EncryptionProvider p : Lookup.getDefault().lookupAll(EncryptionProvider.class)) {
            if (p.enabled()) {
                encryption = p;
                Preferences prefs = prefs();
                Utils.goMinusR(prefs);
                p.encryptionChangingCallback(this);
                if (!testSampleKey(prefs)) {
                    continue;
                }
                LOG.log(Level.FINE, "Using provider: {0}", p);
                return true;
            }
        }
        LOG.fine("No provider");
        return false;
    }
    
    private boolean testSampleKey(Preferences prefs) {
        byte[] ciphertext = prefs.getByteArray(SAMPLE_KEY, null);
        if (ciphertext == null) {
            encryption.freshKeyring(true);
            byte[] randomArray = new byte[36];
            new SecureRandom().nextBytes(randomArray);
            if (_save(SAMPLE_KEY, (SAMPLE_KEY + new String(randomArray)).toCharArray(),
                    NbBundle.getMessage(FallbackProvider.class, "FallbackProvider.sample_key.description"))) {
                LOG.fine("saved sample key");
                return true;
            } else {
                LOG.fine("could not save sample key");
                return false;
            }
        } else {
            encryption.freshKeyring(false);
            while (true) {
                try {
                    if (new String(encryption.decrypt(ciphertext)).startsWith(SAMPLE_KEY)) {
                        LOG.fine("succeeded in decrypting sample key");
                        return true;
                    } else {
                        LOG.fine("wrong result decrypting sample key");
                    }
                } catch (Exception x) {
                    LOG.log(Level.FINE, "failed to decrypt sample key", x);
                }
                if (!encryption.decryptionFailed()) {
                    LOG.fine("sample key decryption failed");
                    return promptToDelete(prefs);
                }
                LOG.fine("will retry decryption of sample key");
            }
        }
    }

    private boolean promptToDelete(Preferences prefs) {
        Object result = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(FallbackProvider.class, "FallbackProvider.msg_clear_keys"),
                NbBundle.getMessage(FallbackProvider.class, "FallbackProvider.title_clear_keys"),
                NotifyDescriptor.OK_CANCEL_OPTION));
        if (result == NotifyDescriptor.OK_OPTION) {
            try {
                LOG.log(Level.FINE, "agreed to delete stored passwords: {0}", Arrays.asList(prefs.keys()));
                prefs.clear();
                return testSampleKey(prefs);
            } catch (BackingStoreException x) {
                LOG.log(Level.INFO, null, x);
            }
        } else {
            LOG.fine("refused to delete stored passwords");
        }
        return false;
    }

    private Preferences prefs() {
        return NbPreferences.forModule(Keyring.class).node(encryption.id());
    }

    public char[] read(String key) {
        byte[] ciphertext = prefs().getByteArray(key, null);
        if (ciphertext == null) {
            return null;
        }
        try {
            return encryption.decrypt(ciphertext);
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to decrypt password for " + key, x);
        }
        return null;
    }

    public void save(String key, char[] password, String description) {
        _save(key, password, description);
    }
    private boolean _save(String key, char[] password, String description) {
        Preferences prefs = prefs();
        try {
            prefs.putByteArray(key, encryption.encrypt(password));
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to encrypt password for " + key, x);
            return false;
        }
        if (description != null) {
            // Preferences interface gives no access to *.properties comments, so:
            prefs.put(key + DESCRIPTION, description);
        }
        return true;
    }

    public void delete(String key) {
        Preferences prefs = prefs();
        prefs.remove(key);
        prefs.remove(key + DESCRIPTION);
    }

    public Void call() throws Exception { // encryption changing
        LOG.fine("encryption changing");
        Map<String,char[]> saved = new HashMap<String,char[]>();
        Preferences prefs = prefs();
        for (String k : prefs.keys()) {
            if (k.endsWith(DESCRIPTION)) {
                continue;
            }
            byte[] ciphertext = prefs.getByteArray(k, null);
            if (ciphertext == null) {
                continue;
            }
            saved.put(k, encryption.decrypt(ciphertext));
        }
        LOG.log(Level.FINE, "reencrypting keys: {0}", saved.keySet());
        encryption.encryptionChanged();
        for (Map.Entry<String,char[]> entry : saved.entrySet()) {
            prefs.putByteArray(entry.getKey(), encryption.encrypt(entry.getValue()));
        }
        LOG.fine("encryption changing finished");
        return null;
    }

}
