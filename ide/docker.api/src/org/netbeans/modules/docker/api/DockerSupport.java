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

package org.netbeans.modules.docker.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.newsclub.net.unix.AFUNIXSocket;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public final class DockerSupport {

    private static final Logger LOGGER = Logger.getLogger(DockerSupport.class.getName());

    private static DockerSupport support;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // GuardedBy("this")
    private final Map<String, DockerInstance> instances = new HashMap<>();

    // GuardedBy("this")
    private boolean initialized;

    private DockerSupport() {
        super();
    }

    public static DockerSupport getDefault() {
        DockerSupport ret;
        synchronized (DockerSupport.class) {
            if (support == null) {
                support = new DockerSupport();
                Preferences p = NbPreferences.forModule(DockerInstance.class).node(DockerInstance.INSTANCES_KEY);
                p.addNodeChangeListener(new NodeChangeListener() {
                    @Override
                    public void childAdded(NodeChangeEvent evt) {
                        support.refresh();
                    }

                    @Override
                    public void childRemoved(NodeChangeEvent evt) {
                        support.refresh();
                    }
                });
            }
            ret = support;
        }
        synchronized (ret) {
            if (!ret.isInitialized()) {
                ret.refresh();
            }
        }
        return ret;
    }

    public DockerInstance addInstance(@NonNull DockerInstance instance) {
        Parameters.notNull("instance", instance);

        String url = instance.getUrl();
        synchronized (this) {
            if (instances.containsKey(url)) {
                throw new IllegalStateException("Docker instance already exist: " + url);
            }
            instance.save();
            instances.put(url, instance);
        }
        changeSupport.fireChange();
        return instance;
    }

    public void removeInstance(@NonNull DockerInstance instance) {
        Parameters.notNull("instance", instance);

        synchronized (this) {
            instances.remove(instance.getUrl());
            instance.delete();

            // we shouldn't need it and use it
            //instance.getEventBus().close();
        }
        changeSupport.fireChange();
    }

    public Collection<? extends DockerInstance> getInstances() {
        synchronized (this) {
            return new HashSet<>(instances.values());
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public boolean isSocketSupported() {
        return AFUNIXSocket.isSupported();
    }

    private boolean isInitialized() {
        synchronized (this) {
            return initialized;
        }
    }

    private void refresh() {
        boolean fire = false;
        synchronized (this) {
            initialized = true;
            Set<String> toRemove = new HashSet<>(instances.keySet());
            for (DockerInstance i : DockerInstance.loadAll()) {
                if (instances.get(i.getUrl()) == null) {
                    fire = true;
                    instances.put(i.getUrl(), i);
                }
                toRemove.remove(i.getUrl());
            }
            if (instances.keySet().removeAll(toRemove)) {
                fire = true;
            }
        }
        if (fire) {
            changeSupport.fireChange();
        }
    }
}
