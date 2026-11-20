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
 * The {@link AuxiliaryProperties} of a generic project, stored in
 * {@link Properties} file(s) per project (name brandable via the
 * module bundle, defaults to {@code .nbgp}).
 *
 * <p>Shared and private settings are kept in separate files:
 *
 * <ul>
 * <li>the <em>shared</em> settings live in the data file whose
 *     location depends on the project's nature: for a project with
 *     the marker file the marker
 *     ({@link GenericProject#MARKER_FILE}) <em>is</em> the data file,
 *     so the data is versioned along with the project; for a local
 *     (marker-less) project it lives in the user configuration
 *     (see {@link #dataFolder});</li>
 * <li>the <em>private</em> settings always live in the user
 *     configuration (see {@link #privateDataFileName}), even for
 *     marker projects, so that nothing private is ever written into
 *     the working tree.</li>
 * </ul>
 *
 * <p>The location of the shared file is re-evaluated on every access:
 * if the marker file is created or deleted while the project is open,
 * the shared data is moved to the new location on the next access
 * (the file at the old one is removed); if it is deleted while no
 * instance of the project is open, the shared data is lost with it.
 * Private data is not affected by the marker state.
 */
final class GpAuxiliaryProperties implements AuxiliaryProperties {

    private static final Logger LOG = Logger.getLogger(GpAuxiliaryProperties.class.getName());

    private final GenericProject prj;
    private final Properties sharedData = new Properties();
    private final Properties privateData = new Properties();
    private boolean loaded;
    private boolean sharedLoadedInProject;

    GpAuxiliaryProperties(GenericProject prj) {
        this.prj = prj;
    }

    /**
     * The path of the folder in the user configuration that holds
     * the user-local auxiliary data of a project with the given
     * directory: the shared data of a local project and, always, the
     * private data.
     */
    static String dataFolder(FileObject projectDirectory) {
        return "Projects/" + projectDirectory.getNameExt() // NOI18N
                + "-%08x".formatted(projectDirectory.getPath().hashCode()); // NOI18N
    }

    /**
     * The name of the shared auxiliary data file, brandable via the
     * module bundle, defaults to {@code .nbgp}: identical to the name
     * of the marker file ({@link GenericProject#MARKER_FILE}).
     */
    @Messages({
        "LBL_aux_data_file_name=.nbgp",
    })
    static String dataFileName() {
        return Bundle.LBL_aux_data_file_name();
    }

    /**
     * The name of the file holding the private auxiliary settings,
     * derived from the (brandable) shared data file name.
     */
    static String privateDataFileName() {
        return dataFileName() + ".private";  // NOI18N
    }

    /**
     * Whether the shared auxiliary data is stored inside the project
     * directory: this is the case when the marker file is present,
     * i.e. the project is a "real", shareable project.
     */
    private boolean hasMarker() {
        return GenericProject.hasMarker(prj.getProjectDirectory());
    }

    @Override
    public String get(String key, boolean shared) {
        synchronized (this) {
            ensureLoaded();
            return storeOf(shared).getProperty(keyPrefix(shared) + key);
        }
    }

    @Override
    public void put(String key, String value, boolean shared) {
        synchronized (this) {
            ensureLoaded();
            String storedKey = keyPrefix(shared) + key;
            Properties store = storeOf(shared);
            if (value == null) {
                store.remove(storedKey);
            } else {
                store.setProperty(storedKey, value);
            }
            save(shared);
        }
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        List<String> keys = new ArrayList<>();
        synchronized (this) {
            ensureLoaded();
            String prefix = keyPrefix(shared);
            for (String stored : storeOf(shared).stringPropertyNames()) {
                if (stored.startsWith(prefix)) {
                    keys.add(stored.substring(prefix.length()));
                }
            }
        }
        return keys;
    }

    private Properties storeOf(boolean shared) {
        return shared ? sharedData : privateData;
    }

    private boolean save(boolean shared) {
        return shared ? saveShared() : savePrivate();
    }

    private static String keyPrefix(boolean shared) {
        return shared ? "shared." : "private."; // NOI18N
    }

    private static String dataFileName(boolean shared) {
        return shared ? dataFileName() : privateDataFileName();
    }

    /**
     * Resolves the data file of the given channel at the location it
     * has when the shared data is (or is not) stored in the project
     * directory, returning {@code null} if the file does not exist.
     * The location of the private file does not depend on the marker
     * state.
     *
     * @param inProject whether to resolve the shared data file in the
     *        project directory (or in the user configuration)
     * @param shared whether to resolve the shared or the private data
     *        file
     */
    private FileObject locateFile(boolean inProject, boolean shared) {
        if (shared && inProject) {
            return prj.getProjectDirectory().getFileObject(dataFileName());
        }
        FileObject root = FileUtil.getConfigRoot();
        if (root != null) {
            FileObject dir = root.getFileObject(dataFolder(prj.getProjectDirectory()));
            if (dir != null) {
                return dir.getFileObject(dataFileName(shared));
            }
        }
        return null;
    }

    /**
     * Loads the data if necessary.
     *
     * <p>If the marker state has changed since the data was last
     * loaded (the marker file was created or deleted), the cached
     * shared data is moved to the new location and the file at the
     * old one is removed; the private data is not affected.
     */
    private void ensureLoaded() {
        boolean hasMarker = hasMarker();
        if (loaded && sharedLoadedInProject == hasMarker) {
            return;
        }
        if (!loaded) {
            loaded = true;
            sharedLoadedInProject = hasMarker;
            FileObject privateFile = locateFile(false, false);
            if (privateFile != null) {
                readFileInto(privateFile, privateData);
            }
            FileObject sharedFile = locateFile(hasMarker, true);
            if (sharedFile != null) {
                readFileInto(sharedFile, sharedData);
            }
            return;
        }
        // the marker state has changed since the last access: move the
        // shared data to the new location and remove the file at the
        // old one
        boolean old = sharedLoadedInProject;
        sharedLoadedInProject = hasMarker;
        if (saveShared()) {
            FileObject oldFile = locateFile(old, true);
            if (oldFile != null) {
                try {
                    oldFile.delete();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Cannot remove old auxiliary data file of " + prj, ex);
                }
            }
        }
    }

    private void readFileInto(FileObject file, Properties into) {
        try (InputStream is = file.getInputStream()) {
            into.load(is);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read auxiliary data file of " + prj + ": " + file, ex);
        }
    }

    /**
     * Saves the shared data to its current location.
     *
     * @return whether the data has been written
     */
    private boolean saveShared() {
        try {
            FileObject dir;
            if (hasMarker()) {
                dir = prj.getProjectDirectory();
            } else {
                FileObject root = FileUtil.getConfigRoot();
                if (root == null) {
                    LOG.log(Level.WARNING, "No user configuration available, cannot save auxiliary data of {0}", prj);
                    return false;
                }
                dir = FileUtil.createFolder(root, dataFolder(prj.getProjectDirectory()));
            }
            return storeInto(dir, dataFileName(), sharedData,
                    "Auxiliary configuration of " + prj.getProjectDirectory().getPath());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot save auxiliary data of " + prj, ex);
            return false;
        }
    }

    /**
     * Saves the private data to the user configuration.
     *
     * @return whether the data has been written
     */
    private boolean savePrivate() {
        FileObject root = FileUtil.getConfigRoot();
        if (root == null) {
            LOG.log(Level.WARNING, "No user configuration available, cannot save private auxiliary data of {0}", prj);
            return false;
        }
        try {
            FileObject dir = FileUtil.createFolder(root, dataFolder(prj.getProjectDirectory()));
            return storeInto(dir, privateDataFileName(), privateData,
                    "Auxiliary configuration (private) of " + prj.getProjectDirectory().getPath());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot save private auxiliary data of " + prj, ex);
            return false;
        }
    }

    /**
     * Writes the given data to the named file in the directory,
     * creating it if necessary.
     */
    private boolean storeInto(FileObject dir, String fileName, Properties data, String header) throws IOException {
        FileObject file = dir.getFileObject(fileName);
        if (file == null) {
            file = dir.createData(fileName);
        }
        try (OutputStream os = file.getOutputStream()) {
            data.store(os, header);
        }
        return true;
    }

}