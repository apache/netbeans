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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.spi.GradleFiles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex.Action;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleAuxiliaryPropertiesImpl implements AuxiliaryProperties {

    private static final String WRONG_PREFIX = "nb-config."; //NOI18N
    // USE THIS PREFIX [NETBEANS-2288], AS IT CAN BE OVERWRITTEN IN
    // GRADLE PROPERTY FILES
    private static final String PROP_PREFIX = "netbeans."; //NOI18N

    final NbGradleProjectImpl project;

    public GradleAuxiliaryPropertiesImpl(NbGradleProjectImpl project) {
        this.project = project;
    }

    @Override
    public String get(final String key, final boolean shared) {
        return ProjectManager.mutex().readAccess(new Action<String>() {
            @Override
            public String run() {
                EditableProperties props = getProperties(shared);
                return props.getOrDefault(PROP_PREFIX + key, props.get(WRONG_PREFIX + key));
            }
        });
    }

    @Override
    public void put(final String key, final String value, final boolean shared) {
        ProjectManager.mutex().writeAccess(() -> {
            EditableProperties props = getProperties(shared);
            if (value != null) {
                props.put(PROP_PREFIX + key, value);
            } else {
                props.remove(PROP_PREFIX + key);
                props.remove(WRONG_PREFIX + key);
            }

            putProperties(props, shared);
        });
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        Set<String> ret = new HashSet<>();
        EditableProperties props = getProperties(shared);
        for (String key : props.keySet()) {
            if (key.startsWith(PROP_PREFIX)) {
                ret.add(key.substring(PROP_PREFIX.length()));
            }
            if (key.startsWith(WRONG_PREFIX)) {
                ret.add(key.substring(WRONG_PREFIX.length()));
            }
        }
        return ret;
    }

    private EditableProperties getProperties(boolean shared) {
        EditableProperties ret = new EditableProperties(false);
        File input = getPropFile(shared);
        if (input.canRead()) {
            try (InputStream is = new FileInputStream(input)) {
                ret.load(is);
            } catch (IOException ex) {
                //TODO: do something about this
            }
        }
        return ret;
    }

    private void putProperties(EditableProperties props, boolean shared) {
        File output = getPropFile(shared);
        if (!props.isEmpty()) {
            if (!output.exists()) {
                output.getParentFile().mkdirs();
            }
            try (OutputStream os = new FileOutputStream(output)) {
                props.store(os);
            } catch (IOException ex) {
                //TODO: do something about this
            }
        } else if (output.exists()) {
            output.delete();
        }
     }

    private File getPropFile(boolean shared) {
        GradleFiles gf = project.getGradleFiles();
        return new File(shared ? gf.getProjectDir() : NbGradleProjectImpl.getCacheDir(gf), GradleFiles.GRADLE_PROPERTIES_NAME);
    }
}
