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
package org.netbeans.modules.keyring.gnome.libsecret;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.keyring.gnome.libsecret.Glib.GError;
import org.netbeans.modules.keyring.gnome.libsecret.LibSecret.SecretSchema;
import org.netbeans.modules.keyring.gnome.libsecret.LibSecret.SecretSchemaAttribute;
import org.netbeans.modules.keyring.impl.KeyringSupport;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = KeyringProvider.class, position = 90)
public class GnomeLibSecretProvider implements KeyringProvider {

    private static final Logger LOG = Logger.getLogger(GnomeLibSecretProvider.class.getName());
    private static final String KEY = "key"; // NOI18N
    private static final Charset CHARSET = Charset.forName(Native.getDefaultStringEncoding());

    private final String appName;

    private SecretSchema secretSchema = null;

    public GnomeLibSecretProvider() {
        appName = KeyringSupport.getAppName();
    }

    private SecretSchema getSchema() {
        if (secretSchema != null) {
            return secretSchema;
        }

        secretSchema = new SecretSchema();
        secretSchema.name = appName;
        secretSchema.flags = LibSecret.SECRET_SCHEMA_NONE;
        secretSchema.attributes[0] = new SecretSchemaAttribute();
        secretSchema.attributes[0].name = KEY;
        secretSchema.attributes[0].type = LibSecret.SECRET_SCHEMA_ATTRIBUTE_STRING;
        return secretSchema;
    }

    @Override
    public boolean enabled() {
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            LOG.fine("native keyring integration disabled");
            return false;
        }

        try {
            read("NoNeXiStEnT"); // NOI18N
            return true;
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, null, e);
            return false;
        } catch (UnsatisfiedLinkError e) {
            LOG.log(Level.FINE, null, e);
            return false;
        }
    }

    @Override
    public char[] read(String key) {
        PointerByReference gerrorBuffer = new PointerByReference();
        SecretSchema schema = getSchema();

        Pointer pointer = LibSecret.INSTANCE.secret_password_lookup_sync(schema, null, gerrorBuffer, KEY, key);

        if (gerrorBuffer.getValue() != null) {
            processError(gerrorBuffer);
            return null;
        }

        if (pointer == null) {
            return null;
        }

        return decode(readZeroTerminatedBytes(pointer));
    }

    @Override
    public void save(String key, char[] password, String description) {
        PointerByReference gerrorBuffer = new PointerByReference();
        SecretSchema schema = getSchema();

        String label = appName + " - " + (description != null ? description : key);
        LibSecret.INSTANCE.secret_password_store_sync(schema, LibSecret.SECRET_COLLECTION_DEFAULT, label, encode(password), null, gerrorBuffer, KEY, key);

        if (gerrorBuffer.getValue() != null) {
            processError(gerrorBuffer);
        }
    }

    @Override
    public void delete(String key) {
        PointerByReference gerrorBuffer = new PointerByReference();
        SecretSchema schema = getSchema();

        LibSecret.INSTANCE.secret_password_clear_sync(schema, null, gerrorBuffer, KEY, key);

        if (gerrorBuffer.getValue() != null) {
            processError(gerrorBuffer);
        }
    }

    private void processError(PointerByReference gerrorBuffer) throws IllegalArgumentException {
        try {
            GError gerror = Structure.newInstance(GError.class, gerrorBuffer.getValue());
            gerror.read();
            throw new RuntimeException(String.format("%d/%d: %s", gerror.domain, gerror.code, gerror.message));
        } finally {
            Glib.INSTANCE.g_error_free(gerrorBuffer.getValue());
        }
    }

    private byte[] encode(char[] password) {
        ByteBuffer encodedPasswordBuffer = CHARSET.encode(CharBuffer.wrap(password));
        byte[] encodedPassword = new byte[encodedPasswordBuffer.limit() + 1]; // zero terminated
        encodedPasswordBuffer.get(encodedPassword, 0, encodedPasswordBuffer.limit());
        return encodedPassword;
    }

    private char[] decode(byte[] bytes) {
        CharBuffer decodedPasswordBuffer = CHARSET.decode(ByteBuffer.wrap(bytes));
        char[] decodedPassword = new char[decodedPasswordBuffer.limit()];
        decodedPasswordBuffer.get(decodedPassword, 0, decodedPasswordBuffer.limit());
        return decodedPassword;
    }

    private byte[] readZeroTerminatedBytes(Pointer pointer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 100000; i++) {
            byte curVal = pointer.getByte(i);
            if (curVal == 0) {
                break;
            }
            baos.write(curVal);
        }
        return baos.toByteArray();
    }

}
