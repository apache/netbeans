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
        CryptLib INSTANCE = Native.loadLibrary("Crypt32", CryptLib.class); // NOI18N
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
