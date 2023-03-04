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

package org.netbeans.modules.keyring.fallback;

import java.awt.Dialog;
import java.util.concurrent.Callable;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.keyring.spi.EncryptionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class FallbackProviderTest extends NbTestCase {

    public FallbackProviderTest(String name) {
        super(name);
    }

    // #197205: if Win32Protect.encrypt fails with NTE_BAD_KEY_STATE, we must skip this provider.
    public void testFreshSampleKeyEncryptionFails() throws Exception {
        System.setProperty("netbeans.keyring.no.native", "true");
        System.setProperty("netbeans.keyring.no.master", "true");
        MockServices.setServices(BrokenProvider.class, DoNotPrompt.class);
        assertFalse(new FallbackProvider().enabled());
    }

    public static class BrokenProvider implements EncryptionProvider {
        public @Override boolean enabled() {
            return true;
        }
        public @Override String id() {
            return "broken";
        }
        public @Override byte[] encrypt(char[] cleartext) throws Exception {
            throw new Exception("oops!");
        }
        public @Override char[] decrypt(byte[] ciphertext) throws Exception {
            return new char[0];
        }
        public @Override boolean decryptionFailed() {
            return false;
        }
        public @Override void encryptionChangingCallback(Callable<Void> callback) {}
        public @Override void encryptionChanged() {}
        public @Override void freshKeyring(boolean fresh) {}
    }

    public static class DoNotPrompt extends DialogDisplayer {
        public @Override Object notify(NotifyDescriptor descriptor) {
            throw new AssertionError();
        }
        public @Override Dialog createDialog(DialogDescriptor descriptor) {
            throw new AssertionError();
        }
    }

}
