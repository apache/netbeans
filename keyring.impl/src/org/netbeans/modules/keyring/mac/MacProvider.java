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

package org.netbeans.modules.keyring.mac;

import com.sun.jna.Pointer;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=KeyringProvider.class, position=200)
public class MacProvider implements KeyringProvider {

    private static final Logger LOG = Logger.getLogger(MacProvider.class.getName());

    public boolean enabled() {
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            LOG.fine("native keyring integration disabled");
            return false;
        }
        return Utilities.isMac();
    }

    public char[] read(String key) {
        try {
            byte[] serviceName = key.getBytes("UTF-8");
            byte[] accountName = "NetBeans".getBytes("UTF-8");
            int[] dataLength = new int[1];
            Pointer[] data = new Pointer[1];
            error("find", SecurityLibrary.LIBRARY.SecKeychainFindGenericPassword(null, serviceName.length, serviceName,
                    accountName.length, accountName, dataLength, data, null));
            if (data[0] == null) {
                return null;
            }
            byte[] value = data[0].getByteArray(0, dataLength[0]); // XXX ought to call SecKeychainItemFreeContent
            return new String(value, "UTF-8").toCharArray();
        } catch (UnsupportedEncodingException x) {
            LOG.log(Level.WARNING, null, x);
            return null;
        }
    }

    public void save(String key, char[] password, String description) {
        try {
            byte[] serviceName = key.getBytes("UTF-8");
            byte[] accountName = "NetBeans".getBytes("UTF-8");
            // Keychain Access seems to expect UTF-8, so do not use Utils.chars2Bytes:
            byte[] data = new String(password).getBytes("UTF-8");
            Pointer[] itemRef = new Pointer[1];
            error("find (for save)", SecurityLibrary.LIBRARY.SecKeychainFindGenericPassword(null, serviceName.length, serviceName,
                    accountName.length, accountName, null, null, itemRef));
            if (itemRef[0] != null) {
                error("save (update)", SecurityLibrary.LIBRARY.SecKeychainItemModifyContent(itemRef[0], null, data.length, data));
                SecurityLibrary.LIBRARY.CFRelease(itemRef[0]);
            } else {
                error("save (new)", SecurityLibrary.LIBRARY.SecKeychainAddGenericPassword(null, serviceName.length, serviceName,
                        accountName.length, accountName, data.length, data, null));
            }
        } catch (UnsupportedEncodingException x) {
            LOG.log(Level.WARNING, null, x);
        }
        // XXX use description somehow... better to use SecItemAdd with kSecAttrDescription
    }

    public void delete(String key) {
        try {
            byte[] serviceName = key.getBytes("UTF-8");
            byte[] accountName = "NetBeans".getBytes("UTF-8");
            Pointer[] itemRef = new Pointer[1];
            error("find (for delete)", SecurityLibrary.LIBRARY.SecKeychainFindGenericPassword(null, serviceName.length, serviceName,
                    accountName.length, accountName, null, null, itemRef));
            if (itemRef[0] != null) {
                error("delete", SecurityLibrary.LIBRARY.SecKeychainItemDelete(itemRef[0]));
                SecurityLibrary.LIBRARY.CFRelease(itemRef[0]);
            }
        } catch (UnsupportedEncodingException x) {
            LOG.log(Level.WARNING, null, x);
        }
    }

    private static void error(String msg, int code) {
        if (code != 0 && code != /* errSecItemNotFound, always returned from find it seems */-25300) {
            Pointer translated = SecurityLibrary.LIBRARY.SecCopyErrorMessageString(code, null);
            String str;
            if (translated == null) {
                str = String.valueOf(code);
            } else {
                char[] buf = new char[(int) SecurityLibrary.LIBRARY.CFStringGetLength(translated)];
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = SecurityLibrary.LIBRARY.CFStringGetCharacterAtIndex(translated, i);
                }
                SecurityLibrary.LIBRARY.CFRelease(translated);
                str = new String(buf) + " (" + code + ")";
            }
            LOG.log(Level.WARNING, "{0}: {1}", new Object[] {msg, str});
        }
    }

}
