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
