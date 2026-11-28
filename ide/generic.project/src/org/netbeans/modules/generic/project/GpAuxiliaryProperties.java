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
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@link AuxiliaryProperties} of a generic project.
 *
 * <p>Shared and private settings are stored according to the project's
 * nature:
 *
 * <ul>
 * <li>the <em>shared</em> settings of a project with the marker file
 *     live in the marker file ({@link GenericProject#MARKER_FILE}),
 *     which <em>is</em> the data file, so the data is versioned along
 *     with the project;</li>
 * <li>everything else — the <em>shared</em> settings of a local
 *     (marker-less) project and, always, the <em>private</em>
 *     settings — lives in the project's <em>private</em> auxiliary
 *     configuration (a {@code FileObject} attribute on the project
 *     directory, see
 *     {@link AuxiliaryConfiguration#putConfigurationFragment}): the
 *     same mechanism and storage (the user's {@code var/attributes.xml}
 *     in the running IDE) as the per-project data of the other
 *     project types, and nothing is ever written into the working
 *     tree of a local project.</li>
 * </ul>
 *
 * <p>The storage location of the shared data is re-evaluated on every
 * access: if the marker file is created or deleted while the project
 * is open, the shared data is moved to the new location on the next
 * access; if it is deleted while no instance of the project is open,
 * the shared data is lost with it. Private data is not affected by
 * the marker state.
 */
final class GpAuxiliaryProperties implements AuxiliaryProperties {

    private static final Logger LOG = Logger.getLogger(GpAuxiliaryProperties.class.getName());

    /**
     * Namespace of the auxiliary configuration fragment holding the
     * auxiliary data of a local project (the shared settings) and,
     * for every project, the private settings.
     */
    static final String NAMESPACE = "http://www.netbeans.org/ns/generic-project/1"; // NOI18N

    /**
     * Name of the root element of the auxiliary configuration
     * fragment, one {@code <property name=... value=.../>} child per
     * stored setting.
     */
    static final String ROOT = "properties"; // NOI18N

    private static final String EL_PROPERTY = "property"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_VALUE = "value"; // NOI18N

    private static final String SHARED_PREFIX = "shared."; // NOI18N
    private static final String PRIVATE_PREFIX = "private."; // NOI18N

    private final GenericProject prj;
    private final AuxiliaryConfiguration auxConfig;

    // marker projects: EditableProperties keeps files stable, new properties are appended
    private final EditableProperties markerData = new EditableProperties(false);

    // The data stored in the auxiliary configuration fragment:
    // always the private settings, plus - for a local (marker-less)
    // project the shared settings as well, so each setting lives in exactly one
    // of the two storage locations (here, or in the marker file) at any time.
    // Kept sorted for stable output
    private final TreeMap<String, String> fragmentData = new TreeMap<>();

    private boolean loaded;
    private boolean hasMarkerAtLoad;

    GpAuxiliaryProperties(GenericProject prj) {
        this.prj = prj;
        this.auxConfig = ProjectUtils.getAuxiliaryConfiguration(prj);
    }

    /**
     * The name of the shared auxiliary data file, brandable via the
     * module bundle, defaults to {@code .nbgp.properties} (the
     * {@code .properties} suffix, so that tools recognize the file
     * type): identical to the name of the marker file
     * ({@link GenericProject#MARKER_FILE}).
     */
    @Messages({
        "LBL_aux_data_file_name=.nbgp.properties",
    })
    static String dataFileName() {
        return Bundle.LBL_aux_data_file_name();
    }

    @Override
    public String get(String key, boolean shared) {
        synchronized (this) {
            ensureLoaded();
            if (shared && hasMarkerAtLoad) {
                return markerData.getProperty(SHARED_PREFIX + key);
            } else {
                return fragmentData.get(prefixOf(shared) + key);
            }
        }
    }

    @Override
    public void put(String key, String value, boolean shared) {
        synchronized (this) {
            ensureLoaded();
            String storedKey = prefixOf(shared) + key;
            if (shared && hasMarkerAtLoad) {
                if (value == null) {
                    markerData.remove(storedKey);
                } else {
                    markerData.setProperty(storedKey, value);
                }
                saveMarker();
            } else {
                if (value == null) {
                    fragmentData.remove(storedKey);
                } else {
                    fragmentData.put(storedKey, value);
                }
                saveFragment();
            }
        }
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        List<String> keys = new ArrayList<>();
        synchronized (this) {
            ensureLoaded();
            String prefix = prefixOf(shared);
            if (shared && hasMarkerAtLoad) {
                for (String stored : markerData.keySet()) {
                    if (stored.startsWith(prefix)) {
                        keys.add(stored.substring(prefix.length()));
                    }
                }
            } else {
                for (String stored : fragmentData.keySet()) {
                    if (stored.startsWith(prefix)) {
                        keys.add(stored.substring(prefix.length()));
                    }
                }
            }
        }
        return keys;
    }

    private static String prefixOf(boolean shared) {
        return shared ? SHARED_PREFIX : PRIVATE_PREFIX;
    }

    /**
     * Whether the shared auxiliary data is stored inside the project
     * directory: this is the case when the marker file is present,
     * i.e. the project is a "real", shareable project.
     */
    private boolean hasMarker() {
        return GenericProject.hasMarker(prj.getProjectDirectory());
    }

    /**
     * Loads the data if necessary.
     *
     * <p>If the marker state has changed since the data was last
     * loaded (the marker file was created or deleted), the cached
     * shared data is moved to the new storage location and, in the
     * other direction, the old location is left without the shared
     * data; the private data is not affected.
     */
    private void ensureLoaded() {
        boolean hasMarker = hasMarker();
        if (loaded && hasMarkerAtLoad == hasMarker) {
            return;
        }
        if (!loaded) {
            loadMarkerData();
            loadFragmentData();
        } else if (hasMarker) {
            // local -> marker: move the shared data from the
            // auxiliary configuration fragment to the (re)created marker file
            loadFragmentData(); // re-read, another instance may have written
            markerData.clear();
            for (String key : new ArrayList<>(fragmentData.keySet())) {
                if (key.startsWith(SHARED_PREFIX)) {
                    markerData.setProperty(key, fragmentData.get(key));
                }
            }
            fragmentData.keySet().removeIf(key -> key.startsWith(SHARED_PREFIX));
            postMigration(this::saveFragmentAndMarker);
        } else {
            // marker -> local: move the shared data from the (in-memory
            // copy of the) marker file to the auxiliary configuration
            // fragment; the marker file itself is removed by whatever
            // changed the marker state
            loadFragmentData(); // re-read, another instance may have written
            for (String key : new ArrayList<>(markerData.keySet())) {
                if (key.startsWith(SHARED_PREFIX)) {
                    fragmentData.put(key, markerData.getProperty(key));
                }
            }
            markerData.clear();
            postMigration(this::saveFragment);
        }
        hasMarkerAtLoad = hasMarker;
        loaded = true;
    }

    /**
     * Persists the data after the storage-location migration in
     * {@link #ensureLoaded()}.
     *
     * <p>The migration is triggered by an auxiliary property access,
     * which is usually a read performed under the project's read
     * access: the write lock is not available there and must not be
     * upgraded from the read (it would deadlock when another thread
     * reads concurrently). The persistence is therefore posted with
     * {@link Mutex#postWriteRequest}, which runs it immediately if
     * the current thread already holds the write access, or as soon
     * as the current thread leaves its read access. The in-memory
     * data is already up to date, so the access that triggered the
     * migration still sees the migrated values.
     */
    private void postMigration(Runnable persist) {
        ProjectManager.mutex(false, prj).postWriteRequest(persist);
    }

    private void loadMarkerData() {
        FileObject file = prj.getProjectDirectory().getFileObject(GenericProject.MARKER_FILE);
        if (file != null) {
            try (InputStream is = file.getInputStream()) {
                markerData.load(is);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Cannot read auxiliary data file of " + prj + ": " + file, ex);
            }
        }
    }

    private synchronized void saveMarker() {
        FileObject dir = prj.getProjectDirectory();
        try {
            FileObject file = dir.getFileObject(GenericProject.MARKER_FILE);
            if (file == null) {
                file = dir.createData(GenericProject.MARKER_FILE);
            }
            try (OutputStream os = file.getOutputStream()) {
                markerData.store(os);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot save auxiliary data of " + prj, ex);
        }
    }

    private void loadFragmentData() {
        fragmentData.clear();
        Element el = auxConfig.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el != null) {
            NodeList nl = el.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE
                        && NAMESPACE.equals(node.getNamespaceURI())
                        && EL_PROPERTY.equals(node.getLocalName())) {
                    Element property = (Element) node;
                    String name = property.getAttribute(ATTR_NAME);
                    if (!name.isEmpty()) {
                        fragmentData.put(name, property.getAttribute(ATTR_VALUE));
                    }
                }
            }
        }
    }

    private synchronized void saveFragmentAndMarker() {
        saveFragment();
        saveMarker();
    }

    private synchronized void saveFragment() {
        if (fragmentData.isEmpty()) {
            // keep an uncustomized (local) project free of any
            // auxiliary configuration of its own
            auxConfig.removeConfigurationFragment(ROOT, NAMESPACE, false);
            return;
        }
        try {
            Document doc = XMLUtil.createDocument(ROOT, NAMESPACE, null, null);
            Element el = doc.getDocumentElement();
            for (Map.Entry<String, String> e : fragmentData.entrySet()) {
                Element property = doc.createElementNS(NAMESPACE, EL_PROPERTY);
                property.setAttribute(ATTR_NAME, e.getKey());
                property.setAttribute(ATTR_VALUE, e.getValue());
                el.appendChild(property);
            }
            auxConfig.putConfigurationFragment(el, false);
        } catch (DOMException ex) {
            LOG.log(Level.WARNING, "Cannot save auxiliary data of " + prj, ex);
        }
    }

}