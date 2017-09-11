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
package org.netbeans.modules.versioning.util;

import org.junit.Assert;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Ondrej Vrabec
 */
public class KeyringSupportTest extends NbTestCase {
    private String prefix;
    private String key;
    
    public KeyringSupportTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        prefix = "myvcs_uri_password";
        key = "https://ovrabec@myserver.mydomain.com/path/to/repository.repo";
        Keyring.delete(KeyringSupport.getKeyringKeyHashed(prefix, key));
        Keyring.delete(KeyringSupport.getKeyringKey(prefix, key));
    }
        
    public void testReplaceHashedKeyOldRecord () throws Exception {
        String fullKey = KeyringSupport.getKeyringKeyHashed(prefix, key);
        char[] password = "password".toCharArray();
        Keyring.save(fullKey, password.clone(), "test record");
        
        // old key exists
        Assert.assertArrayEquals(password, Keyring.read(fullKey));
        // old key can be obtained back
        Assert.assertArrayEquals(password, KeyringSupport.read(prefix, key));
        // old key should be no longer present
        assertNull(Keyring.read(fullKey));
        // new key should be present
        Assert.assertArrayEquals(password, Keyring.read(KeyringSupport.getKeyringKey(prefix, key)));
    }
    
    public void testSaveNoHash () throws Exception {
        String fullKey = KeyringSupport.getKeyringKey(prefix, key);
        char[] password = "password".toCharArray();
        
        assertNull(Keyring.read(fullKey));
        
        KeyringSupport.save(prefix, key, password.clone(), "test record");
        
        Assert.assertArrayEquals(password, Keyring.read(fullKey));
        Assert.assertArrayEquals(password, KeyringSupport.read(prefix, key));
    }
    
}
