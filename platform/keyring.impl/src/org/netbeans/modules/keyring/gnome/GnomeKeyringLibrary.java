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

package org.netbeans.modules.keyring.gnome;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.annotations.common.SuppressWarnings;

/**
 * JNA wrapper for certain functions from GNOME Keyring API.
 * #178571: must work against GNOME 2.6 to support JDS 3 (Solaris 10).
 * @see <a href="http://library.gnome.org/devel/gnome-keyring/stable/">gnome-keyring API Reference</a>
 */
public interface GnomeKeyringLibrary extends Library {

    class LibFinder {
        private static final String GENERIC = "gnome-keyring";
        // http://packages.ubuntu.com/search?suite=precise&arch=any&mode=exactfilename&searchon=contents&keywords=libgnome-keyring.so.0
        private static final String EXPLICIT_ONEIRIC = "/usr/lib/libgnome-keyring.so.0";
        @SuppressWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
        private static GnomeKeyringLibrary load(Map<String,?> options) {
            try {
                return Native.load(GENERIC, GnomeKeyringLibrary.class, options);
            } catch (UnsatisfiedLinkError x) {
                // #203735: on Oneiric, may have trouble finding right lib.
                // Precise is using multiarch (#211401) which should work automatically using JNA 3.4+ (#211403).
                // Unclear if this workaround is still needed for Oneiric with 3.4, but seems harmless to leave it in for now.
                if (new File(EXPLICIT_ONEIRIC).isFile()) {
                    return Native.load(EXPLICIT_ONEIRIC, GnomeKeyringLibrary.class, options);
                } else {
                    throw x;
                }
            }
        }
        private LibFinder() {}
    }

    GnomeKeyringLibrary LIBRARY = LibFinder.load(Collections.singletonMap(OPTION_TYPE_MAPPER, new DefaultTypeMapper() {
        {
            addTypeConverter(Boolean.TYPE, new TypeConverter() { // #198921
                @Override public Object toNative(Object value, ToNativeContext context) {
                    return Boolean.TRUE.equals(value) ? 1 : 0;
                }
                @Override public Object fromNative(Object value, FromNativeContext context) {
                    return ((Integer) value) != 0;
                }
                @Override public Class<?> nativeType() {
                    // gint is 32-bit int
                    return Integer.class;
                }
            });
        }
    }));

    boolean gnome_keyring_is_available();

    /*GnomeKeyringItemType*/int GNOME_KEYRING_ITEM_GENERIC_SECRET = 0;

    // GnomeKeyringAttributeList gnome_keyring_attribute_list_new() = g_array_new(FALSE, FALSE, sizeof(GnomeKeyringAttribute))
    int GnomeKeyringAttribute_SIZE = Native.POINTER_SIZE * 3; // conservatively: 2 pointers + 1 enum

    void gnome_keyring_attribute_list_append_string(
            /*GnomeKeyringAttributeList*/Pointer attributes,
            String name,
            String value);

    void gnome_keyring_attribute_list_free(
            /*GnomeKeyringAttributeList*/Pointer attributes);

    int gnome_keyring_item_create_sync(
            String keyring,
            /*GnomeKeyringItemType*/int type,
            String display_name,
            /*GnomeKeyringAttributeList*/Pointer attributes,
            String secret,
            boolean update_if_exists,
            int[] item_id);

    int gnome_keyring_item_delete_sync(
            String keyring,
            int id);

    int gnome_keyring_find_items_sync(
            /*GnomeKeyringItemType*/int type,
            /*GnomeKeyringAttributeList*/Pointer attributes,
            /*GList<GnomeKeyringFound>*/Pointer[] found);

    void gnome_keyring_found_list_free(
            /*GList<GnomeKeyringFound>*/Pointer found_list);

    @java.lang.SuppressWarnings("PublicField")
    @FieldOrder({"keyring", "item_id", "attributes", "secret"})
    class GnomeKeyringFound extends Structure {
        public String keyring;
        public int item_id;
        public /*GnomeKeyringAttributeList*/Pointer attributes;
        public String secret;
    }

    /** http://library.gnome.org/devel/glib/2.6/glib-Miscellaneous-Utility-Functions.html#g-set-application-name */
    void g_set_application_name(String name);

    /** http://library.gnome.org/devel/glib/2.6/glib-Arrays.html */

    Pointer g_array_new(
            /*gboolean*/int zero_terminated,
            /*gboolean*/int clear,
            int element_size);

    /** http://library.gnome.org/devel/glib/2.6/glib-Doubly-Linked-Lists.html */

    int g_list_length(
            Pointer list);

    /*gpointer*/GnomeKeyringFound g_list_nth_data(
            Pointer list,
            int n);

}
