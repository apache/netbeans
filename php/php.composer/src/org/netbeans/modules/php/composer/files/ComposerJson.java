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
package org.netbeans.modules.php.composer.files;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private static final String FIELD_SCRIPTS = "scripts"; // NOI18N
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

    public Set<String> getScripts() {
        Map<String, Object> scripts = composerJson.getContentValue(Map.class, FIELD_SCRIPTS);
        if (scripts == null) {
            return Collections.emptySet();
        }
        return scripts.keySet();
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
