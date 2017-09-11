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

package org.netbeans.modules.keyring.gnome;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        private static Object load(Map<?,?> options) {
            try {
                return Native.loadLibrary(GENERIC, GnomeKeyringLibrary.class, options);
            } catch (UnsatisfiedLinkError x) {
                // #203735: on Oneiric, may have trouble finding right lib.
                // Precise is using multiarch (#211401) which should work automatically using JNA 3.4+ (#211403).
                // Unclear if this workaround is still needed for Oneiric with 3.4, but seems harmless to leave it in for now.
                if (new File(EXPLICIT_ONEIRIC).isFile()) {
                    return Native.loadLibrary(EXPLICIT_ONEIRIC, GnomeKeyringLibrary.class, options);
                } else {
                    throw x;
                }
            }
        }
        private LibFinder() {}
    }

    GnomeKeyringLibrary LIBRARY = (GnomeKeyringLibrary) LibFinder.load(Collections.singletonMap(OPTION_TYPE_MAPPER, new DefaultTypeMapper() {
        {
            addTypeConverter(Boolean.TYPE, new TypeConverter() { // #198921
                @Override public Object toNative(Object value, ToNativeContext context) {
                    return Boolean.TRUE.equals(value) ? 1 : 0;
                }
                @Override public Object fromNative(Object value, FromNativeContext context) {
                    return ((Integer) value).intValue() != 0;
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
    int GnomeKeyringAttribute_SIZE = Pointer.SIZE * 3; // conservatively: 2 pointers + 1 enum

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

    class GnomeKeyringFound extends Structure {
        public String keyring;
        public int item_id;
        public /*GnomeKeyringAttributeList*/Pointer attributes;
        public String secret;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList( new String[] {
                "keyring",
                "item_id",
                "attributes",
                "secret",
            } );
        }
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
