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

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

public class MasterPasswordEncryptionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MasterPasswordEncryptionTest.class);
    }

    public MasterPasswordEncryptionTest(String n) {
        super(n);
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    public void testEncryption() throws Exception {
        doTestEncryption("Top Secret!", "my password");
        doTestEncryption("some extra secret pass phrase", "something pretty long here for whatever reason...");
        // Acc. to PBEKeySpec, using PKCS #5 (not #12) only ASCII is supported for master passwords:
        doTestEncryption("muj heslo", "hezky česky");
        // Some extra chars
        doTestEncryption("muj heslo", "!§$%&/()=?\\}][{+*#~");
        doTestEncryption("muj heslo", "ॐ");
        // Test fr all characters
        for (char c = 0; c < Character.MAX_VALUE; ++c) {
            doTestEncryption("muj heslo", Character.toString(c));
        }
    }
    
    private void doTestEncryption(String masterPassword, String password) throws Exception {
        MasterPasswordEncryption p = new MasterPasswordEncryption();
        assertTrue(p.enabled());
        p.unlock(masterPassword.toCharArray());
        assertEquals(password, new String(p.decrypt(p.encrypt(password.toCharArray()))));
    }

    public void testWrongPassword() throws Exception {
        MasterPasswordEncryption p = new MasterPasswordEncryption();
        assertTrue(p.enabled());
        p.unlock("first password".toCharArray());
        byte[] ciphertext = p.encrypt("secret".toCharArray());
        p.unlock("second password".toCharArray());
        char[] result = new char[0];
        try {
            result = p.decrypt(ciphertext);
        } catch (Exception x) {
            // expected: "BadPaddingException: Given final block not properly padded"
        }
        assertFalse("should not be able to decrypt with incorrect password", new String(result).equals("secret"));
    }

}
