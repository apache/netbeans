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
package org.netbeans.modules.cloud.oracle.assets.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.assets.OpenProjectsFinder;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
public class Pods {
    private final RequestProcessor RP = new RequestProcessor(Pods.class);
    private final ClusterItem cluster;
    private Set<String> names = null;
    private final List<PodItem> pods = new ArrayList<> ();
    private transient PropertyChangeSupport changeSupport = null;
    private CountDownLatch latch = null;
    
    private Pods(ClusterItem cluster) {
        this.cluster = cluster;
    }
    
    public static Pods from(ClusterItem cluster) {
        Pods instance = new Pods(cluster);
        instance.init();
        return instance;
    }
    
    public List<PodItem> getItems() {
        return new ArrayList<> (pods);
    }
    
    private void addPod(PodItem pod) {
        synchronized (this) {
            pods.add(pod);
            changeSupport.firePropertyChange("pods", pods.size(), pods.size() - 1);
        }
        
    }
    
    private void removePod(final PodItem pod) {
        synchronized (this) {
            pods.remove(pod);
            changeSupport.firePropertyChange("pods", pods.size(), pods.size() + 1);
        }
        PortForwards.getDefault().closePortForward(pod);
    }
    
    private void removeAll() {
        pods.clear();
        changeSupport.firePropertyChange("pods", pods.size(), pods.size() + 1);
    }

    private void init() {
        
        try {
            changeSupport = new PropertyChangeSupport(this);
            cluster.addChangeListener((PropertyChangeEvent evt) -> {
                if ("namespace".equals(evt.getPropertyName())) {
                    watchNamespace((String) evt.getNewValue());
                }
            });
            CompletableFuture<Project[]> projectsFuture = OpenProjectsFinder.getDefault().findTopLevelProjects();
            projectsFuture.thenApply(projects -> {
                List<String> projectNames = new ArrayList<>();
                for (int i = 0; i < projects.length; i++) {
                    ProjectInformation pi = ProjectUtils.getInformation(projects[i]);
                    projectNames.add(pi.getDisplayName());
                }
                names = new HashSet<> (projectNames);
                return projectNames;
            }).get();
            watchNamespace(cluster.getNamespace());
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void watchNamespace(String namespace) {
        CountDownLatch l;
        removeAll();
        synchronized(this) {
            if (latch != null) {
                latch.countDown();
            }
            latch = new CountDownLatch(1);
            l = latch;
        }
        addWatcher(cluster, namespace, new Watcher<Pod>() {
            @Override
            public void eventReceived(Watcher.Action action, Pod t) {
                String app = t.getMetadata().getLabels().get("app");
                if (!names.contains(app)) {
                    return;
                }
                if (action == Watcher.Action.ADDED) {
                    addPod(new PodItem(cluster, t.getMetadata().getNamespace(), t.getMetadata().getName()));
                } else if (action == Watcher.Action.DELETED) {
                    removePod(new PodItem(cluster, t.getMetadata().getNamespace(), t.getMetadata().getName()));
                }
            }

            @Override
            public void onClose(WatcherException we) {
                l.countDown();
            }
        });
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
    public void addWatcher(ClusterItem cluster, String namespace, Watcher<Pod> watcher) {
        RP.post(() -> {
            KubernetesUtils.runWithClient(cluster, client -> {
                try {
                    Watch watch = client.pods()
                        .inNamespace(namespace)
                        .watch(watcher); 
                    latch.await();
                    watch.close();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });
    }
    
}
