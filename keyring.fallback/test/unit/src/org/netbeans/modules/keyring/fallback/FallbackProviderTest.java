/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
