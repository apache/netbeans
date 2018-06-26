/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer.files;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;

public final class ComposerJson {

    public static final String FILE_NAME = "composer.json"; // NOI18N
    public static final String PROP_REQUIRE = "REQUIRE"; // NOI18N
    public static final String PROP_REQUIRE_DEV = "REQUIRE_DEV"; // NOI18N
    public static final String PROP_VENDOR_DIR = "VENDOR_DIR"; // NOI18N
    // file content
    private static final String FIELD_REQUIRE = "require"; // NOI18N
    private static final String FIELD_REQUIRE_DEV = "require-dev"; // NOI18N
    private static final String FIELD_CONFIG = "config"; // NOI18N
    private static final String FIELD_VENDOR_DIR = "vendor-dir"; // NOI18N
    // default values
    static final String DEFAULT_VENDOR_DIR = "vendor"; // NOI18N

    private final JsonFile composerJson;


    public ComposerJson(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    ComposerJson(FileObject directory, String filename) {
        assert directory != null;
        assert filename != null;
        composerJson = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_REQUIRE, FIELD_REQUIRE)
                .add(PROP_REQUIRE_DEV, FIELD_REQUIRE_DEV)
                .add(PROP_VENDOR_DIR, FIELD_CONFIG, FIELD_VENDOR_DIR));
    }

    public File getFile() {
        return composerJson.getFile();
    }

    public boolean exists() {
        return composerJson.exists();
    }

    public File getVendorDir() {
        String vendorDir = composerJson.getContentValue(String.class, FIELD_CONFIG, FIELD_VENDOR_DIR);
        if (vendorDir == null) {
            vendorDir = DEFAULT_VENDOR_DIR;
        }
        return new File(getFile().getParentFile(), vendorDir);
    }

    public void addPropertyChangeListener(PropertyChangeListener composerJsonListener) {
        composerJson.addPropertyChangeListener(composerJsonListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener composerJsonListener) {
        composerJson.removePropertyChangeListener(composerJsonListener);
    }

    public ComposerDependencies getDependencies() {
        Map<Object, Object> dependencies = composerJson.getContentValue(Map.class, FIELD_REQUIRE);
        Map<Object, Object> devDependencies = composerJson.getContentValue(Map.class, FIELD_REQUIRE_DEV);
        return new ComposerDependencies(sanitizeDependencies(dependencies), sanitizeDependencies(devDependencies));
    }

    @CheckForNull
    private Map<String, String> sanitizeDependencies(@NullAllowed Map<Object, Object> data) {
        if (data == null
                || data.isEmpty()) {
            return null;
        }
        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            sanitized.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return sanitized;
    }

    //~ Inner classes

    public static final class ComposerDependencies {

        public final Map<String, String> dependencies = new ConcurrentHashMap<>();
        public final Map<String, String> devDependencies = new ConcurrentHashMap<>();


        ComposerDependencies(@NullAllowed Map<String, String> dependencies, @NullAllowed Map<String, String> devDependencies) {
            if (dependencies != null) {
                this.dependencies.putAll(dependencies);
            }
            if (devDependencies != null) {
                this.devDependencies.putAll(devDependencies);
            }
        }

        public boolean isEmpty() {
            return dependencies.isEmpty()
                    && devDependencies.isEmpty();
        }

        public int getCount() {
            return dependencies.size() + devDependencies.size();
        }

        @Override
        public String toString() {
            return "ComposerDependencies{" + "dependencies=" + dependencies + ", devDependencies=" + devDependencies + '}'; // NOI18N
        }

    }

}
