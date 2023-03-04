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
package org.netbeans.modules.javascript.bower.file;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * Class representing project's <tt>bower.json</tt> file.
 */
@MIMEResolver.Registration(displayName = "bower.json", resource = "../resources/bower-resolver.xml", position = 128)
public final class BowerJson {

    public static final String FILE_NAME = "bower.json"; // NOI18N
    public static final String PROP_DEPENDENCIES = "DEPENDENCIES"; // NOI18N
    public static final String PROP_DEV_DEPENDENCIES = "DEV_DEPENDENCIES"; // NOI18N
    // file content
    public static final String FIELD_DEPENDENCIES = "dependencies"; // NOI18N
    public static final String FIELD_DEV_DEPENDENCIES = "devDependencies"; // NOI18N

    private final JsonFile bowerJson;


    public BowerJson(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    BowerJson(FileObject directory, String filename) {
        assert directory != null;
        bowerJson = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_DEPENDENCIES, FIELD_DEPENDENCIES)
                .add(PROP_DEV_DEPENDENCIES, FIELD_DEV_DEPENDENCIES));
    }

    public BowerDependencies getDependencies() {
        Map<Object, Object> dependencies = bowerJson.getContentValue(Map.class, BowerJson.FIELD_DEPENDENCIES);
        Map<Object, Object> devDependencies = bowerJson.getContentValue(Map.class, BowerJson.FIELD_DEV_DEPENDENCIES);
        return new BowerDependencies(sanitizeDependencies(dependencies), sanitizeDependencies(devDependencies));
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

    public boolean exists() {
        return bowerJson.exists();
    }

    public String getPath() {
        return bowerJson.getPath();
    }

    public File getFile() {
        return bowerJson.getFile();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        bowerJson.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        bowerJson.removePropertyChangeListener(listener);
    }

    public void refresh() {
        bowerJson.refresh();
    }

    public void setContent(List<String> fieldHierarchy, Object value) throws IOException {
        bowerJson.setContent(fieldHierarchy, value);
    }

    void cleanup() {
        bowerJson.cleanup();
    }

    //~ Inner classes

    public static final class BowerDependencies {

        public final Map<String, String> dependencies = new ConcurrentHashMap<>();
        public final Map<String, String> devDependencies = new ConcurrentHashMap<>();


        BowerDependencies(@NullAllowed Map<String, String> dependencies, @NullAllowed Map<String, String> devDependencies) {
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
            return "BowerDependencies{" + "dependencies=" + dependencies + ", devDependencies=" + devDependencies + '}'; // NOI18N
        }

    }

}
