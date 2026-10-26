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

package org.netbeans.modules.generic.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * The {@link AuxiliaryProperties} of a generic project, stored in a
 * single {@link Properties} file per project (name brandable via the
 * module bundle, defaults to {@code .nbgp}).
 *
 * <p>The file's location depends on the project's nature: for a
 * project with the marker file the marker
 * ({@link GenericProject#MARKER_FILE}) <em>is</em> the data file, so
 * the data is versioned along with the project; for a local
 * (marker-less) project it lives in the user configuration
 * (see {@link #dataFolder}), so that nothing auxiliary is written
 * into the working tree. Shared and private settings are
 * distinguished by a {@code shared} / {@code private} key prefix.
 *
 * <p>The location is re-evaluated on every access: if the marker
 * file is created or deleted while the project is open, the data is
 * moved to the new location on the next access (the file at the old
 * one is removed); if it is deleted while no instance of the project
 * is open, the data is lost with it.
 */
final class GpAuxiliaryProperties implements AuxiliaryProperties {

    private static final Logger LOG = Logger.getLogger(GpAuxiliaryProperties.class.getName());

    private static final String PROJECTS_FOLDER = "Projects"; // NOI18N
    private static final String SHARED_PREFIX = "shared.";   // NOI18N
    private static final String PRIVATE_PREFIX = "private."; // NOI18N

    private final GenericProject prj;
    private final Properties data = new Properties();
    private boolean loaded;
    private Boolean loadedInProject;

    GpAuxiliaryProperties(GenericProject prj) {
        this.prj = prj;
    }

    /**
     * The path of the folder in the user configuration that holds the
     * auxiliary data of a local project with the given directory.
     */
    static String dataFolder(FileObject projectDirectory) {
        return PROJECTS_FOLDER + "/" // NOI18N
                + projectDirectory.getNameExt().replace('/', '_')
                + "-" // NOI18N
                + "%08x".formatted(projectDirectory.getPath().hashCode());
    }

    /**
     * The name of the auxiliary data file, brandable via the module
     * bundle, defaults to {@code .nbgp}: identical to the name of
     * the marker file ({@link GenericProject#MARKER_FILE}).
     */
    @Messages({
        "LBL_aux_data_file_name=.nbgp",
    })
    static String dataFileName() {
        return Bundle.LBL_aux_data_file_name();
    }

    /**
     * Whether the auxiliary data is stored inside the project
     * directory: this is the case when the marker file is present,
     * i.e. the project is a "real", shareable project.
     */
    private boolean inProject() {
        return GenericProject.hasMarker(prj.getProjectDirectory());
    }

    @Override
    public String get(String key, boolean shared) {
        synchronized (data) {
            ensureLoaded();
            return data.getProperty(keyPrefix(shared) + key);
        }
    }

    @Override
    public void put(String key, String value, boolean shared) {
        synchronized (data) {
            ensureLoaded();
            String storedKey = keyPrefix(shared) + key;
            if (value == null) {
                data.remove(storedKey);
            } else {
                data.setProperty(storedKey, value);
            }
            save();
        }
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        List<String> keys = new ArrayList<>();
        synchronized (data) {
            ensureLoaded();
            String prefix = keyPrefix(shared);
            for (Object stored : data.keySet()) {
                String key = (String) stored;
                if (key.startsWith(prefix)) {
                    keys.add(key.substring(prefix.length()));
                }
            }
        }
        return keys;
    }

    private static String keyPrefix(boolean shared) {
        return shared ? SHARED_PREFIX : PRIVATE_PREFIX;
    }

    /**
     * Resolves the data file at the given location, returning
     * {@code null} if it does not exist.
     */
    private FileObject locateFile(boolean inProject) {
        if (inProject) {
            return prj.getProjectDirectory().getFileObject(dataFileName());
        }
        FileObject root = FileUtil.getConfigRoot();
        FileObject dir = root != null
                ? root.getFileObject(dataFolder(prj.getProjectDirectory()))
                : null;
        return dir != null ? dir.getFileObject(dataFileName()) : null;
    }

    /**
     * Loads the data if necessary. If the storage location has changed
     * since the data was last loaded (the marker file was created or
     * deleted), the cached data is moved to the new location and the
     * file at the old location is removed.
     */
    private void ensureLoaded() {
        boolean here = inProject();
        if (loaded && loadedInProject == here) {
            return;
        }
        if (!loaded) {
            loaded = true;
            loadedInProject = here;
            FileObject file = locateFile(here);
            if (file != null) {
                readFile(file);
            }
            return;
        }
        // the location has changed since the last access: move the
        // cached data to the new location and remove the file at the
        // old one
        boolean old = loadedInProject;
        loadedInProject = here;
        if (save()) {
            FileObject oldFile = locateFile(old);
            if (oldFile != null) {
                try {
                    oldFile.delete();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Cannot remove old auxiliary data file of " + prj, ex);
                }
            }
        }
        // the cache already holds what is now stored at the new
        // location
    }

    private void readFile(FileObject file) {
        try (InputStream is = file.getInputStream()) {
            data.load(is);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read auxiliary data of " + prj, ex);
        }
    }

    /**
     * Saves the cached data to the current location.
     *
     * @return whether the data has been written
     */
    private boolean save() {
        try {
            FileObject dir;
            if (inProject()) {
                dir = prj.getProjectDirectory();
            } else {
                FileObject root = FileUtil.getConfigRoot();
                if (root == null) {
                    LOG.log(Level.WARNING, "No user configuration available, cannot save auxiliary data of {0}", prj);
                    return false;
                }
                dir = FileUtil.createFolder(root, dataFolder(prj.getProjectDirectory()));
            }
            // createData throws if the file already exists, so only
            // create it if necessary
            FileObject file = dir.getFileObject(dataFileName());
            if (file == null) {
                file = dir.createData(dataFileName());
            }
            try (OutputStream os = file.getOutputStream()) {
                data.store(os, "Auxiliary configuration of " + prj.getProjectDirectory().getPath());
            }
            return true;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot save auxiliary data of " + prj, ex);
            return false;
        }
    }

}