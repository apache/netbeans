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
package org.netbeans.modules.docker.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.docker.DockerEventBus;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hejl
 */
public class DockerInstance {

    static final String INSTANCES_KEY = "instances";

    private static final Logger LOGGER = Logger.getLogger(DockerInstance.class.getName());

    private static final String DISPLAY_NAME_KEY = "display_name";

    private static final String URL_KEY = "url";

    private static final String CA_CERTIFICATE_PATH_KEY = "ca_certificate";

    private static final String CERTIFICATE_PATH_KEY = "certificate";

    private static final String KEY_PATH_KEY = "key";

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final InstanceListener listener = new InstanceListener();

    private final String url;

    // GuardedBy("this")
    private String displayName;

    // GuardedBy("this")
    private File caCertificate;

    // GuardedBy("this")
    private File certificate;

    // GuardedBy("this")
    private File key;

    // GuardedBy("this")
    private Preferences prefs;

    private final DockerEventBus eventBus = new DockerEventBus(this);

    private DockerInstance(String url, String displayName, File caCertificate, File certificate, File key) {
        this.url = url;
        this.displayName = displayName;
        this.caCertificate = caCertificate;
        this.certificate = certificate;
        this.key = key;
    }

    @NonNull
    public static DockerInstance getInstance(@NonNull String url, @NullAllowed String displayName,
            @NullAllowed File caCertificate, @NullAllowed File certificate, @NullAllowed File key) {

        return new DockerInstance(url, displayName, caCertificate, certificate, key);
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        synchronized (this) {
            return displayName;
        }
    }

    public File getCaCertificateFile() {
        synchronized (this) {
            return caCertificate;
        }
    }

    public File getCertificateFile() {
        synchronized (this) {
            return certificate;
        }
    }

    public File getKeyFile() {
        synchronized (this) {
            return key;
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        eventBus.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        eventBus.removeConnectionListener(listener);
    }

    public void addImageListener(DockerEvent.Listener listener) {
        eventBus.addImageListener(listener);
    }

    public void removeImageListener(DockerEvent.Listener listener) {
        eventBus.removeImageListener(listener);
    }

    public void addContainerListener(DockerEvent.Listener listener) {
        eventBus.addContainerListener(listener);
    }

    public void removeContainerListener(DockerEvent.Listener listener) {
        eventBus.removeContainerListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DockerInstance other = (DockerInstance) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DockerInstance{" + "url=" + url + '}';
    }

    static Collection<? extends DockerInstance> loadAll() {
        Preferences global = NbPreferences.forModule(DockerInstance.class).node(INSTANCES_KEY);
        assert global != null;

        List<DockerInstance> instances = new ArrayList<>();
        try {
            String[] names = global.childrenNames();
            if (names.length == 0) {
                LOGGER.log(Level.INFO, "No preferences nodes");
            }
            for (String name : names) {
                Preferences p = global.node(name);
                String displayName = p.get(DISPLAY_NAME_KEY, null);
                String url = p.get(URL_KEY, null);
                if (displayName != null && url != null
                        && (!url.startsWith("file:") || DockerSupport.getDefault().isSocketSupported())) { // NOI18N
                    DockerInstance instance = new DockerInstance(url, null, null, null, null);
                    instance.load(p);

                    instances.add(instance);
                } else {
                    LOGGER.log(Level.INFO, "Invalid Docker instance {0}", name);
                }
            }
            LOGGER.log(Level.FINE, "Loaded {0} Docker instances", instances.size());
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return instances;
    }

    final void save() {
        synchronized (this) {
            if (prefs != null) {
                throw new IllegalStateException();
            }

            Preferences global = NbPreferences.forModule(DockerInstance.class).node(INSTANCES_KEY);

            Preferences p = null;
            int suffix = 0;
            do {
                p = global.node(escapeUrl(url) + suffix);
                suffix++;
            } while (p.get(URL_KEY, null) != null && suffix < Integer.MAX_VALUE);

            p.put(DISPLAY_NAME_KEY, displayName);
            p.put(URL_KEY, url);
            if (caCertificate != null) {
                p.put(CA_CERTIFICATE_PATH_KEY, FileUtil.normalizeFile(caCertificate).getAbsolutePath());
            }
            if (certificate != null) {
                p.put(CERTIFICATE_PATH_KEY, FileUtil.normalizeFile(certificate).getAbsolutePath());
            }
            if (key != null) {
                p.put(KEY_PATH_KEY, FileUtil.normalizeFile(key).getAbsolutePath());
            }
            try {
                p.flush();
            } catch (BackingStoreException ex) {
                // XXX better solution?
                throw new IllegalStateException(ex);
            }
            this.prefs = p;
            this.prefs.addPreferenceChangeListener(listener);
        }
    }

    final void delete() {
        synchronized (this) {
            if (prefs == null) {
                throw new IllegalStateException();
            }
            try {
                prefs.removePreferenceChangeListener(listener);
                prefs.removeNode();
            } catch (BackingStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            prefs = null;
        }
    }

    private final void load(Preferences p) {
        synchronized (this) {
            if (prefs != null) {
                throw new IllegalStateException();
            }
            prefs = p;
            prefs.addPreferenceChangeListener(listener);
            refresh();
        }
    }

    private void refresh() {
        synchronized (this) {
            if (prefs == null) {
                return;
            }

            displayName = prefs.get(DISPLAY_NAME_KEY, null);
            String caCertPath = prefs.get(CA_CERTIFICATE_PATH_KEY, null);
            caCertificate = caCertPath == null ? null : new File(caCertPath);
            String certPath = prefs.get(CERTIFICATE_PATH_KEY, null);
            certificate = certPath == null ? null : new File(certPath);
            String keyPath = prefs.get(KEY_PATH_KEY, null);
            key = keyPath == null ? null : new File(keyPath);
        }
    }

    final DockerEventBus getEventBus() {
        return eventBus;
    }

    private static String escapeUrl(String url) {
        return url.replaceAll("[:/]", "_"); // NOI18N
    }

    public static interface ConnectionListener extends EventListener {

        void onConnect();

        void onDisconnect();
    }

    private class InstanceListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            refresh();
            changeSupport.fireChange();
        }
    }

}
