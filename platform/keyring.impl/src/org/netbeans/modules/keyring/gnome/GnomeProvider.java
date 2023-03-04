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

package org.netbeans.modules.keyring.gnome;

import com.sun.jna.Pointer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.SuppressWarnings;
import static org.netbeans.modules.keyring.gnome.GnomeKeyringLibrary.*;
import org.netbeans.modules.keyring.impl.KeyringSupport;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=KeyringProvider.class, position=100)
public class GnomeProvider implements KeyringProvider {

    private static final Logger LOG = Logger.getLogger(GnomeProvider.class.getName());
    private static final String KEY = "key"; // NOI18N

    public @Override boolean enabled() {
        if (Boolean.getBoolean("netbeans.keyring.no.native")) {
            LOG.fine("native keyring integration disabled");
            return false;
        }
        String appName = KeyringSupport.getAppName();
        try {
            // Need to do this somewhere, or we get warnings on console.
            // Also used by confirmation dialogs to give the app access to the login keyring.
            LIBRARY.g_set_application_name(appName);
            if (!LIBRARY.gnome_keyring_is_available()) {
                return false;
            }
            // #178571: try to read some key just to make sure gnome_keyring_find_password_sync is bound:
            read("NoNeXiStEnT"); // NOI18N
            return true;
        } catch (Throwable t) {
            LOG.log(Level.FINE, null, t);
            return false;
        }
    }

    @SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public @Override char[] read(String key) {
        Pointer[] found = new Pointer[1];
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_find_items_sync(GNOME_KEYRING_ITEM_GENERIC_SECRET, attributes, found));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
        if (found[0] != null) {
            try {
                if (LIBRARY.g_list_length(found[0]) > 0) {
                    GnomeKeyringFound result = LIBRARY.g_list_nth_data(found[0], 0);
                    if (result != null) {
                        if (result.secret != null) {
                            return result.secret.toCharArray();
                        } else {
                            LOG.warning("#183670: GnomeKeyringFound.secret == null");
                            delete(key);
                        }
                    } else {
                        LOG.warning("#183670: GList<GnomeKeyringFound>[0].result == null");
                    }
                }
            } finally {
                LIBRARY.gnome_keyring_found_list_free(found[0]);
            }
        }
        return null;
    }

    public @Override void save(String key, char[] password, String description) {
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            int[] item_id = new int[1];
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_item_create_sync(
                    null, GNOME_KEYRING_ITEM_GENERIC_SECRET, description != null ? description : key, attributes, new String(password), true, item_id));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
    }

    public @Override void delete(String key) {
        Pointer[] found = new Pointer[1];
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_find_items_sync(GNOME_KEYRING_ITEM_GENERIC_SECRET, attributes, found));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
        if (found[0] == null) {
            return;
        }
        int id;
        try {
            if (LIBRARY.g_list_length(found[0]) > 0) {
                GnomeKeyringFound result = LIBRARY.g_list_nth_data(found[0], 0);
                id = result.item_id;
            } else {
                id = 0;
            }
        } finally {
            LIBRARY.gnome_keyring_found_list_free(found[0]);
        }
        if (id > 0) {
            if ("SunOS".equals(System.getProperty("os.name")) && "5.10".equals(System.getProperty("os.version"))) { // #185698
                save(key, new char[0], null); // gnome_keyring_item_delete(null, id, null, null, null) does not seem to do anything
            } else {
                error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_item_delete_sync(null, id));
            }
        }
    }

    private static String[] ERRORS = {
        "OK", // NOI18N
        "DENIED", // NOI18N
        "NO_KEYRING_DAEMON", // NOI18N
        "ALREADY_UNLOCKED", // NOI18N
        "NO_SUCH_KEYRING", // NOI18N
        "BAD_ARGUMENTS", // NOI18N
        "IO_ERROR", // NOI18N
        "CANCELLED", // NOI18N
        "KEYRING_ALREADY_EXISTS", // NOI18N
        "NO_MATCH", // NOI18N
    };
    private static void error(int code) {
        if (code != 0 && code != 9) {
            LOG.log(Level.WARNING, "gnome-keyring error: {0}", ERRORS[code]);
        }
    }    
}
