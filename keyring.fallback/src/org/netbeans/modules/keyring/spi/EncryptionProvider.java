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

package org.netbeans.modules.keyring.spi;

import java.util.concurrent.Callable;
import org.netbeans.spi.keyring.KeyringProvider;

/**
 * A weaker version of {@link KeyringProvider} which can only encrypt passwords securely.
 * Rather than managing the complete storage of the keyring, a NetBeans-specific keyring
 * is used, but this provider can encrypt the sensitive contents.
 * The encryption is assumed to be symmetric but the encryption key should be secured.
 * If the NetBeans {@link KeyringProvider} is used, the first encryption provider to be found
 * in global lookup which claims to be enabled will be used;
 * a standard implementation exists (at position 1000) which uses a simple master password.
 */
public interface EncryptionProvider {

    /**
     * Check whether this provider can be used in the current JVM session.
     * If integrating a native library, this should attempt to load it.
     * This method will be called at most once per JVM session,
     * prior to any other methods in this interface being called.
     * @return true if this provider should be used, false if not
     */
    boolean enabled();

    /**
     * Define a unique ID for this encryption provider, so that if the same userdir
     * is reused on machines of different architecture the encrypted passwords will not conflict.
     * @return an arbitrary ID specific to the algorithm
     */
    String id();

    /**
     * Encrypt a password or other sensitive data so that only the current user can decrypt it.
     * @param cleartext some data (may be nulled out after this call)
     * @return encrypted data
     * @throws Exception if anything goes wrong
     */
    byte[] encrypt(char[] cleartext) throws Exception;

    /**
     * Decrypt a password or other sensitive data.
     * @param ciphertext encrypted data
     * @return cleartext (may be nulled out after this call)
     * @throws Exception if anything goes wrong
     */
    char[] decrypt(byte[] ciphertext) throws Exception;

    /**
     * Called if {@link #decrypt} produced incorrect results on a sample key.
     * The provider can react by prompting again for a master password, for example.
     * <p>Implementations which do not support dynamic changes to the encryption
     * key or method should return false from this method.
     * @return true if an attempt was made to correct the encryption, false if nothing has changed
     */
    boolean decryptionFailed();

    /**
     * Offers a callback in case the encryption needs to change.
     * For example, this may be employed if the user asks to change a master password.
     * During the callback, the provider will be asked to decrypt existing secrets
     * using the old encryption key; then {@link #encryptionChanged}
     * will be called; finally the secrets will be reencrypted using the new encryption key.
     * <p>Implementations which do not support dynamic changes to the encryption
     * key or method may ignore this method.
     * @param callback a callback which the provider may store and later call
     */
    void encryptionChangingCallback(Callable<Void> callback);

    /**
     * See {@link #encryptionChangingCallback} for description.
     * <p>Implementations which do not support dynamic changes to the encryption
     * key or method may ignore this method.
     */
    void encryptionChanged();

    /**
     * Tells the provider whether this is a new, empty keyring.
     * @param fresh true if this is a new keyring, false if it has been used before
     */
    void freshKeyring(boolean fresh);

}
