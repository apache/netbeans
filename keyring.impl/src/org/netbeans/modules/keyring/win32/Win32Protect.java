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

package org.netbeans.modules.keyring.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.keyring.utils.Utils;
import org.netbeans.modules.keyring.spi.EncryptionProvider;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Data protection utility for Microsoft Windows.
 * XXX org.tmatesoft.svn.core.internal.util.jna.SVNWinCrypt is a possibly more robust implementation
 * (though it seems to set CRYPTPROTECT_UI_FORBIDDEN which we do not necessarily want).
 */
@ServiceProvider(service=EncryptionProvider.class, position=100)
public class Win32Protect implements EncryptionProvider {

    private static final Logger LOG = Logger.getLogger(Win32Protect.class.getName());
    
    public @Override boolean enabled() {
        if (!Utilities.isWindows()) {
            LOG.fine("not running on Windows");
            return false;
        }
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            LOG.fine("native keyring integration disabled");
            return false;
        }
        try {
            if (CryptLib.INSTANCE == null) {
                LOG.fine("loadLibrary -> null");
                return false;
            }
            return true;
        } catch (Throwable t) {
            LOG.log(Level.FINE, null, t);
            return false;
        }
    }

    public @Override String id() {
        return "win32"; // NOI18N
    }

    public @Override byte[] encrypt(char[] cleartext) throws Exception {
        byte[] cleartextB = Utils.chars2Bytes(cleartext);
        CryptIntegerBlob input = new CryptIntegerBlob();
        input.store(cleartextB);
        Arrays.fill(cleartextB, (byte) 0);
        CryptIntegerBlob output = new CryptIntegerBlob();
        if (!CryptLib.INSTANCE.CryptProtectData(input, null, null, null, null, 0, output)) {
            throw new Exception("CryptProtectData failed: " + Native.getLastError());
        }
        input.zero();
        return output.load();
    }

    public @Override char[] decrypt(byte[] ciphertext) throws Exception {
        CryptIntegerBlob input = new CryptIntegerBlob();
        input.store(ciphertext);
        CryptIntegerBlob output = new CryptIntegerBlob();
        if (!CryptLib.INSTANCE.CryptUnprotectData(input, null, null, null, null, 0, output)) {
            throw new Exception("CryptUnprotectData failed: " + Native.getLastError());
        }
        byte[] result = output.load();
        // XXX gives CCE because not a Memory: output.zero();
        char[] cleartext = Utils.bytes2Chars(result);
        Arrays.fill(result, (byte) 0);
        return cleartext;
    }

    public @Override boolean decryptionFailed() {
        return false; // not much to do about it
    }

    public @Override void encryptionChangingCallback(Callable<Void> callback) {}

    public @Override void encryptionChanged() {
        assert false;
    }

    public @Override void freshKeyring(boolean fresh) {}

    public interface CryptLib extends StdCallLibrary {
        CryptLib INSTANCE = (CryptLib) Native.loadLibrary("Crypt32", CryptLib.class); // NOI18N
        /** @see <a href="http://msdn.microsoft.com/en-us/library/aa380261(VS.85,printer).aspx">Reference</a> */
        boolean CryptProtectData(
                CryptIntegerBlob pDataIn,
                WString szDataDescr,
                CryptIntegerBlob pOptionalEntropy,
                Pointer pvReserved,
                Pointer pPromptStruct,
                int dwFlags,
                CryptIntegerBlob pDataOut
        )/* throws LastErrorException*/;
        /** @see <a href="http://msdn.microsoft.com/en-us/library/aa380882(VS.85,printer).aspx">Reference</a> */
        boolean CryptUnprotectData(
                CryptIntegerBlob pDataIn,
                WString[] ppszDataDescr,
                CryptIntegerBlob pOptionalEntropy,
                Pointer pvReserved,
                Pointer pPromptStruct,
                int dwFlags,
                CryptIntegerBlob pDataOut
        )/* throws LastErrorException*/;
    }
    
    public static class CryptIntegerBlob extends Structure {
        public int cbData;
        public /*byte[]*/Pointer pbData;
        byte[] load() {
            return pbData.getByteArray(0, cbData);
            // XXX how to free pbData? [Kernel32]LocalFree?
        }
        void store(byte[] data) {
            cbData = data.length;
            pbData = new Memory(data.length);
            pbData.write(0, data, 0, cbData);
        }
        void zero() {
            ((Memory) pbData).clear();
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList( new String[] {
                "cbData",
                "pbData",
            } );
        }
    }

}
