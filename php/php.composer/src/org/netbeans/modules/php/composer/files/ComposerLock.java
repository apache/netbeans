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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;

public final class ComposerLock {

    private static final Logger LOGGER = Logger.getLogger(ComposerLock.class.getName());

    public static final String FILE_NAME = "composer.lock"; // NOI18N
    public static final String PROP_PACKAGES = "PACKAGES"; // NOI18N
    public static final String PROP_PACKAGES_DEV = "PACKAGES_DEV"; // NOI18N
    // file content
    public static final String FIELD_PACKAGES = "packages"; // NOI18N
    public static final String FIELD_PACKAGES_DEV = "packages-dev"; // NOI18N
    public static final String FIELD_NAME = "name"; // NOI18N
    public static final String FIELD_VERSION = "version"; // NOI18N

    private final JsonFile composerLock;


    public ComposerLock(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    ComposerLock(FileObject directory, String filename) {
        assert directory != null;
        assert filename != null;
        composerLock = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_PACKAGES, FIELD_PACKAGES)
                .add(PROP_PACKAGES_DEV, FIELD_PACKAGES_DEV));
    }

    public File getFile() {
        return composerLock.getFile();
    }

    public boolean exists() {
        return composerLock.exists();
    }

    public void addPropertyChangeListener(PropertyChangeListener composerLockListener) {
        composerLock.addPropertyChangeListener(composerLockListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener composerLockListener) {
        composerLock.removePropertyChangeListener(composerLockListener);
    }

    public ComposerPackages getPackages() {
        List<Map<String, Object>> packages = composerLock.getContentValue(List.class, FIELD_PACKAGES);
        List<Map<String, Object>> packagesDev = composerLock.getContentValue(List.class, FIELD_PACKAGES_DEV);
        return new ComposerPackages(getPackages(packages), getPackages(packagesDev));
    }

    @CheckForNull
    private Map<String, String> getPackages(@NullAllowed List<Map<String, Object>> data) {
        if (data == null
                || data.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>(data.size() * 2);
        for (Map<String, Object> pckg : data) {
            // be defensive
            result.put(String.valueOf(pckg.get(FIELD_NAME)), String.valueOf(pckg.get(FIELD_VERSION)));
        }
        return result;
    }

    //~ Inner classes

    public static final class ComposerPackages {

        public final Map<String, String> packages = new ConcurrentHashMap<>();
        public final Map<String, String> packagesDev = new ConcurrentHashMap<>();


        ComposerPackages(@NullAllowed Map<String, String> packages, @NullAllowed Map<String, String> packagesDev) {
            if (packages != null) {
                this.packages.putAll(packages);
            }
            if (packagesDev != null) {
                this.packagesDev.putAll(packagesDev);
            }
        }

        public boolean isEmpty() {
            return packages.isEmpty()
                    && packagesDev.isEmpty();
        }

        public int getCount() {
            return packages.size() + packagesDev.size();
        }

        @Override
        public String toString() {
            return "ComposerPackages{" + "packages=" + packages + ", packagesDev=" + packagesDev + '}'; // NOI18N
        }

    }

}
