/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Cache of various attributes of packages (vendor, spec & impl version, etc.)
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class PackageAttrsCache implements Stamps.Updater {
    private static final String NULL_REPLACEMENT = "\000";
    private static final String CACHE = "package-attrs.dat"; // NOI18N
    private static final String[] EMPTY = new String[7];
    private static final Logger LOG = Logger.getLogger(PackageAttrsCache.class.getName());
    private static PackageAttrsCache packages;
    
    static void initialize() {
        packages = new PackageAttrsCache();
    }
    
    private final Map<String,String[]> cache;
    PackageAttrsCache() {
        InputStream is = Stamps.getModulesJARs().asStream(CACHE);
        Map<String,String[]> tmp = null;
        if (is != null) {
            try {
                tmp = new ConcurrentHashMap<>();
                DataInputStream dis = new DataInputStream(is);
                for (;;) {
                    String key = Stamps.readRelativePath(dis);
                    if (key.isEmpty()) {
                        break;
                    }
                    String[] arr = new String[7];
                    for (int i = 0; i < 7; i++) {
                        arr[i] = fromSafeValue(dis.readUTF());
                    }
                    tmp.put(key, arr);
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot read " + CACHE, ex);
                tmp = null;
            }
        }
        if (tmp == null) {
            cache = new ConcurrentHashMap();
            Stamps.getModulesJARs().scheduleSave(this, CACHE, false);
        } else {
            cache = Collections.unmodifiableMap(tmp);
        }
    }

    static String[] findPackageAttrs(URL src, Manifest man, String path) {
        PackageAttrsCache p = packages;
        return p == null ? extractFromManifest(man, path) : p.findImpl(src, man, path);
    }
    
    final String[] findImpl(URL src, Manifest man, String path) {
        String key = src.toExternalForm();
        if (key.startsWith("jar:file:")) { // NOI18N
            key = key.substring(9);
        }
        if (!key.endsWith("!/")) { // NOI18N
            key += "!/"; // NOI18N
        }
        key += path;
        String[] arr;
        if (cache instanceof ConcurrentHashMap) {
            arr = extractFromManifest(man, path);
            if (isEmpty(arr)) {
                arr = EMPTY;
            } else {
                cache.put(key, arr);
            }
        } else {
            arr = cache.get(key);
            if (arr == null) {
                arr = EMPTY;
            }
        }
        return arr;
    }

    
    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        for (Map.Entry<String, String[]> entry : cache.entrySet()) {
            Stamps.writeRelativePath(entry.getKey(), os);
            for (String s : entry.getValue()) {
                os.writeUTF(toSafeValue(s));
            }
        }
        Stamps.writeRelativePath("", os);
    }

    @Override
    public void cacheReady() {
    }

    private static String getAttr(Attributes spec, Attributes main, Attributes.Name name) {
        String val = null;
        if (spec != null) val = spec.getValue (name);
        if (val == null && main != null) val = main.getValue (name);
        return val;
    }

    private static String[] extractFromManifest(Manifest man, String path) {
        Attributes spec = man.getAttributes(path);
        Attributes main = man.getMainAttributes();
        String[] arr = new String[7];
        arr[0] = getAttr(spec, main, Attributes.Name.SPECIFICATION_TITLE);
        arr[1] = getAttr(spec, main, Attributes.Name.SPECIFICATION_VERSION);
        arr[2] = getAttr(spec, main, Attributes.Name.SPECIFICATION_VENDOR);
        arr[3] = getAttr(spec, main, Attributes.Name.IMPLEMENTATION_TITLE);
        arr[4] = getAttr(spec, main, Attributes.Name.IMPLEMENTATION_VERSION);
        arr[5] = getAttr(spec, main, Attributes.Name.IMPLEMENTATION_VENDOR);
        arr[6] = getAttr(spec, main, Attributes.Name.SEALED);
        return arr;
    }

    private static boolean isEmpty(String[] arr) {
        if (arr == EMPTY) {
            return true;
        }
        for (String s : arr) {
            if (s != null) {
                return false;
            }
        }
        return true;
    }

    private static String toSafeValue(String value) {
        return value == null ?
            NULL_REPLACEMENT :
            value;
    }

    private static String fromSafeValue(String value) {
        return NULL_REPLACEMENT.equals(value) ?
            null:
            value;
    }
}
