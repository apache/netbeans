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
import org.netbeans.modules.cloud.oracle.compute.PodItem;
import org.netbeans.modules.cloud.oracle.compute.PortForwardItem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
    "Forwarding=→ {0}"
})
public class PortForwards {

    private static PortForwards instance = null;
    private final Map<String, List<PortForwardItem>> activePortForwards;
    private final Map<String, CountDownLatch> latches;
    private final PropertyChangeSupport propertyChangeSupport;

    private PortForwards() {
        activePortForwards = new ConcurrentHashMap<>();
        latches = new ConcurrentHashMap<>();
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
        if (activePortForwards.containsKey(podItem.getName())) {
            NotificationUtils.showMessage("Port forwarding already active for: " + podItem.getName());
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        ProgressHandle handle = ProgressHandle.createHandle(Bundle.Forwarding(podItem.getName()), () -> {
            latch.countDown();
            return true;
        });
        handle.start();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            KubernetesUtils.runWithClient(podItem.getCluster(), client -> {
                Pod pod = client.pods().inNamespace(podItem.getNamespace()).withName(podItem.getName()).get();

                if (pod == null) {
                    System.out.println("Pod not found: " + podItem.getName());
                    return;
                }
                List<Integer> ports = pod.getSpec().getContainers().stream()
                        .flatMap(container -> container.getPorts().stream())
                        .map(port -> port.getContainerPort())
                        .collect(Collectors.toList());
                List<PortForwardItem> forwardItems = new ArrayList<>();
                List<PortForward> forwards = new ArrayList<>();
                if (ports.isEmpty()) {
                    NotificationUtils.showMessage("No ports found for pod: " + podItem.getName());
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
                        forwards.add(fwd);
                        forwardItems.add(new PortForwardItem(podItem, fwd.getLocalPort(), port));

                        NotificationUtils.showMessage(Bundle.ForwardingPorts(port.toString(), podItem.getName()));
                    }
                    latches.put(podItem.getName(), latch);
                    List<PortForwardItem> oldValue = activePortForwards.put(podItem.getName(), forwardItems);
                    firePropertyChange(podItem, oldValue, forwardItems);
                    latch.await();
                    for (PortForward forward : forwards) {
                        forward.close();
                    }
                    activePortForwards.remove(podItem.getName());
                    firePropertyChange(podItem, forwardItems, oldValue);
                } catch (InterruptedException | IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalStateException e) {
                    NotificationUtils.showErrorMessage(e.getMessage());
                } finally {
                    handle.finish();
                }

            });
        });
    }

    /**
     * Retrieves the list of active port forwards for the specified Pod.
     *
     * @param podName The name of the Pod.
     * @return A list of active {@code PortForward} instances for the Pod, or an
     * empty list if none are found.
     */
    public List<PortForwardItem> getActivePortForwards(String podName) {
        return activePortForwards.getOrDefault(podName, Collections.emptyList());
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
    public void closePortForward(String podName) {
        CountDownLatch latch = latches.get(podName);
        if (latch != null) {
            latch.countDown();
        }
        activePortForwards.remove(podName);
        latches.remove(podName);
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
