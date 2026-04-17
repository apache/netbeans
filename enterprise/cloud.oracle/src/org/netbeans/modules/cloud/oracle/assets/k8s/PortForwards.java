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
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.PortForward;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.NotificationUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Manages port forwarding for Kubernetes Pods.
 *
 * <p>This class provides methods to start, list, and stop port forwards
 * for specific Pods. It maintains the state of active port forwards
 * and ensures thread-safe operations.</p>
 *
 * <p>Example Usage:</p>
 * <pre>
 * PortForwards manager = PortForwards.getDefault();
 * manager.startPortForward(podItem);
 * List<PortForwardItem> forwards = manager.getActivePortForwards("pod-name");
 * manager.closePortForward("pod-name");
 * </pre>
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "PortNotFree=Port {0} is occupied by another process and cannot be used.",
    "Forwarding=→ {0}",
    "AlreadyActive=Port forwarding already active for: {0}",
    "NoPorts=No ports found for pod: {0},",
    "PodNotFound=Pod not found: {0}",
    "MaxForwards=Maximum number ({0}) of port forwards is already active"
})
public class PortForwards {
    private static int LIMIT = 10;
    private static final RequestProcessor RP = new RequestProcessor(PortForwards.class.getName(), LIMIT);

    private static PortForwards instance = null;
    private final Map<PodItem, List<PortForwardItem>> activePortForwards;
    private final Map<String, CountDownLatch> stopLatches;
    private final PropertyChangeSupport propertyChangeSupport;

    private PortForwards() {
        activePortForwards = new ConcurrentHashMap<>();
        stopLatches = new ConcurrentHashMap<>();
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public static synchronized PortForwards getDefault() {
        if (instance == null) {
            instance = new PortForwards();
        }
        return instance;
    }
    
    /**
     * Adds a {@code PropertyChangeListener} to listen for changes in port forwarding for a specific Pod.
     *
     * @param pod The {@code PodItem} for which the listener is interested.
     * @param listener The listener to be added.
     */
    public void addPropertyChangeListener(PodItem pod, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(pod.getName(), listener);
    }

    /**
     * Removes a {@code PropertyChangeListener} for a specific Pod.
     *
     * @param pod The {@code PodItem} for which the listener should be removed.
     * @param listener The listener to be removed.
     */
    public void removePropertyChangeListener(PodItem pod, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(pod.getName(), listener);
    }
    
    /**
     * Notifies listeners of a property change for a specific Pod.
     *
     * @param pod The {@code PodItem} for which the change occurred.
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     */
    private void firePropertyChange(PodItem pod, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(pod.getName(), oldValue, newValue);
    }

    /**
     * Starts port forwarding for a specified Kubernetes Pod.
     *
     * <p>
     * This method retrieves the Pod's ports and forwards them to the local
     * machine. If any port is unavailable, the operation is halted for that
     * port and a notification is displayed.</p>
     *
     * @param podItem The {@code PodItem} representing the Pod for which port
     * forwarding should start.
     */
    public void startPortForward(PodItem podItem) {
        if (activePortForwards.containsKey(podItem)) {
            NotificationUtils.showMessage(Bundle.AlreadyActive(podItem.getName()));
            return;
        }
        if (activePortForwards.size() >= LIMIT) {
            NotificationUtils.showMessage(Bundle.MaxForwards(LIMIT));
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        ProgressHandle handle = ProgressHandle.createHandle(Bundle.Forwarding(podItem.getName()), () -> {
            closePortForward(podItem);
            return true;
        });
        handle.start();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            KubernetesUtils.runWithClient(podItem.getCluster(), client -> {
                Pod pod = client.pods().inNamespace(podItem.getNamespace()).withName(podItem.getName()).get();

                if (pod == null) {
                    NotificationUtils.showMessage(Bundle.PodNotFound(podItem.getName()));
                    return;
                }
                List<Integer> ports = pod.getSpec().getContainers().stream()
                        .flatMap(container -> container.getPorts().stream())
                        .map(port -> port.getContainerPort())
                        .collect(Collectors.toList());
                List<PortForwardItem> forwardItems = new ArrayList<>();
                
                if (ports.isEmpty()) {
                    NotificationUtils.showMessage(Bundle.NoPorts(podItem.getName()));
                    return;
                }
                try {
                    for (Integer port : ports) {
                        if (!isPortAvailable(port)) {
                            NotificationUtils.showErrorMessage(Bundle.PortNotFree(port));
                            break;
                        }
                        LocalPortForward fwd = client.pods()
                                .inNamespace(pod.getMetadata().getNamespace())
                                .withName(pod.getMetadata().getName())
                                .portForward(port, port);
                        forwardItems.add(new PortForwardItem(podItem, fwd.getLocalPort(), port, fwd));

                    }
                    stopLatches.put(podItem.getName(), latch);
                    activePortForwards.put(podItem, forwardItems);
                    firePropertyChange(podItem, null, 1);
                    latch.await();
                    
                } catch (InterruptedException | IllegalStateException ex) {
                    NotificationUtils.showErrorMessage(ex.getMessage());
                } finally {
                    handle.finish();
                }

            });
        }, RP);
    }

    /**
     * Retrieves the list of active port forwards for the specified Pod.
     *
     * @param podName The name of the Pod.
     * @return A list of active {@code PortForward} instances for the Pod, or an
     * empty list if none are found.
     */
    public List<PortForwardItem> getActivePortForwards(PodItem pod) {
        return activePortForwards.getOrDefault(pod, Collections.emptyList());
    }

    /**
     * Stops and closes all active port forwards for the specified Pod.
     *
     * <p>
     * Resources associated with the port forwarding are released, and the port
     * forward is removed from the active list.</p>
     *
     * @param podName The name of the Pod for which port forwarding should be
     * stopped.
     */
    public void closePortForward(PodItem pod) {
        List<PortForwardItem> removed = activePortForwards.remove(pod);
        CountDownLatch latch = stopLatches.remove(pod.getName());
        if (latch != null) {
            latch.countDown();
        }
        for (PortForwardItem portForwardItem : removed) {
            PortForward fwd = portForwardItem.getForward();
            if (fwd != null && fwd.isAlive()) {
                try {
                    fwd.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        firePropertyChange(pod, 1, null);
    }

    /**
     * Checks if a TCP port is available.
     *
     * @param port The port number to check.
     * @return true if the port is available, false otherwise.
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
