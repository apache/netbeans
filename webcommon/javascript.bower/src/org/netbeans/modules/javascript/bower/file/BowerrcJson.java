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
import org.netbeans.modules.web.clientproject.api.json.JsonFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

@MIMEResolver.Registration(displayName = "bowerrc", resource = "../resources/bowerrc-resolver.xml", position = 129)
public final class BowerrcJson {

    public static final String FILE_NAME = ".bowerrc"; // NOI18N
    public static final String PROP_DIRECTORY = "DIRECTORY"; // NOI18N
    // file content
    public static final String FIELD_DIRECTORY = "directory"; // NOI18N
    // default values
    static final String DEFAULT_BOWER_COMPONENTS_DIR = "bower_components"; // NOI18N

    private final JsonFile bowerrcJson;


    public BowerrcJson(FileObject directory) {
        this(directory, FILE_NAME);
    }

    // for unit tests
    BowerrcJson(FileObject directory, String filename) {
        assert directory != null;
        assert filename != null;
        bowerrcJson = new JsonFile(filename, directory, JsonFile.WatchedFields.create()
                .add(PROP_DIRECTORY, FIELD_DIRECTORY));
    }

    public boolean exists() {
        return bowerrcJson.exists();
    }

    public File getFile() {
        return bowerrcJson.getFile();
    }

    public File getBowerComponentsDir() {
        String directory = bowerrcJson.getContentValue(String.class, BowerrcJson.FIELD_DIRECTORY);
        if (directory == null) {
            directory = DEFAULT_BOWER_COMPONENTS_DIR;
        }
        return new File(bowerrcJson.getFile().getParentFile(), directory);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        bowerrcJson.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        bowerrcJson.removePropertyChangeListener(listener);
    }

}
