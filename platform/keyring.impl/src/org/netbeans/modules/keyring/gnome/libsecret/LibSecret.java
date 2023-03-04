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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import org.netbeans.modules.keyring.gnome.libsecret.Gio.GCancelable;

public interface LibSecret extends Library {

    LibSecret INSTANCE = Native.load("secret-1", LibSecret.class);

    Pointer secret_password_lookup_sync(SecretSchema schema, GCancelable cancellable, PointerByReference gerror, Object... attributes);

    Pointer secret_password_store_sync(SecretSchema schema, String collection, String label, byte[] password, GCancelable cancelable, PointerByReference gerror, Object... attributes);

    Pointer secret_password_clear_sync(SecretSchema schema, GCancelable cancellable, PointerByReference gerror, Object... attributes);

    void secret_password_free(Pointer pointer);

    @Structure.FieldOrder({"name", "flags", "attributes", "reserved", "reserved1", "reserved2", "reserved3", "reserved4", "reserved5", "reserved6", "reserved7"})
    class SecretSchema extends Structure {

        public String name;
        public int flags;
        public SecretSchemaAttribute[] attributes = new SecretSchemaAttribute[32];
        public int reserved;
        public Pointer reserved1;
        public Pointer reserved2;
        public Pointer reserved3;
        public Pointer reserved4;
        public Pointer reserved5;
        public Pointer reserved6;
        public Pointer reserved7;
    }

    @Structure.FieldOrder({"name", "type"})
    class SecretSchemaAttribute extends Structure {

        public String name;
        public int type;
    }

    final int SECRET_SCHEMA_ATTRIBUTE_STRING = 0;
    final int SECRET_SCHEMA_ATTRIBUTE_INTEGER = 1;
    final int SECRET_SCHEMA_ATTRIBUTE_BOOLEAN = 2;

    final int SECRET_SCHEMA_NONE = 0;
    final int SECRET_SCHEMA_DONT_MATCH_NAME = 1;

    final String SECRET_COLLECTION_DEFAULT = "default";
    final String SECRET_COLLECTION_SESSION = "session";
}
