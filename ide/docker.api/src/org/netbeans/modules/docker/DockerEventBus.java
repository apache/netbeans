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
package org.netbeans.modules.docker;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerException;
import org.openide.util.RequestProcessor;

/**
 * All the events need to refresh the UI are provided by the Docker API 1.20
 * and up so we still need refresh actions.
 *
 * @author Petr Hejl
 */
public class DockerEventBus implements Closeable, DockerEvent.Listener, ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(DockerEventBus.class.getName());

    private static final long INTERVAL = 5000;

    private final RequestProcessor processor = new RequestProcessor(DockerEventBus.class);

    private final DockerInstance instance;

    private final List<DockerInstance.ConnectionListener> connectionListeners = new ArrayList<>();

    private final List<DockerEvent.Listener> imageListeners = new ArrayList<>();

    private final List<DockerEvent.Listener> containerListeners = new ArrayList<>();

    private Endpoint endpoint;

    private DockerEvent lastEvent;

    private boolean stop;

    private boolean blocked;

    public DockerEventBus(DockerInstance instance) {
        this.instance = instance;
    }

    public void addConnectionListener(DockerInstance.ConnectionListener listener) {
        synchronized (this) {
            checkStart();
            connectionListeners.add(listener);
        }
    }

    public void removeConnectionListener(DockerInstance.ConnectionListener listener) {
        synchronized (this) {
            connectionListeners.remove(listener);
            checkStop();
        }
    }

    public void addImageListener(DockerEvent.Listener listener) {
        synchronized (this) {
            checkStart();
            imageListeners.add(listener);
        }
    }

    public void removeImageListener(DockerEvent.Listener listener) {
        synchronized (this) {
            imageListeners.remove(listener);
            checkStop();
        }
    }

    public void addContainerListener(DockerEvent.Listener listener) {
        synchronized (this) {
            checkStart();
            containerListeners.add(listener);
        }
    }

    public void removeContainerListener(DockerEvent.Listener listener) {
        synchronized (this) {
            containerListeners.remove(listener);
            checkStop();
        }
    }

    public void sendEvent(DockerEvent event) {
        List<DockerEvent.Listener> toFire;
        synchronized (this) {
            if (event.getStatus().isContainer()) {
                toFire = new ArrayList<>(containerListeners);
            } else {
                toFire = new ArrayList<>(imageListeners);
            }
        }
        LOGGER.log(Level.FINE, event.toString());
        for (DockerEvent.Listener l : toFire) {
            l.onEvent(event);
        }
    }

    @Override
    public void onEvent(DockerEvent event) {
        List<DockerEvent.Listener> toFire;
        synchronized (this) {
            if (blocked) {
                if (event.equals(lastEvent)) {
                    blocked = false;
                    return;
                } else if (lastEvent == null || lastEvent.getTime() < event.getTime()) {
                    blocked = false;
                } else {
                    return;
                }
            }
            lastEvent = event;
            if (event.getStatus().isContainer()) {
                toFire = new ArrayList<>(containerListeners);
            } else {
                toFire = new ArrayList<>(imageListeners);
            }
        }
        LOGGER.log(Level.FINE, event.toString());
        for (DockerEvent.Listener l : toFire) {
            l.onEvent(event);
        }
    }

    @Override
    public void onConnect(Endpoint e) {
        List<DockerInstance.ConnectionListener> toFire;
        synchronized (this) {
            this.endpoint = e;
            toFire = new ArrayList<>(connectionListeners);
        }
        for (DockerInstance.ConnectionListener l : toFire) {
            l.onConnect();
        }
    }

    @Override
    public void onDisconnect() {
        List<DockerInstance.ConnectionListener> toFire;
        synchronized (this) {
            toFire = new ArrayList<>(connectionListeners);
        }
        for (DockerInstance.ConnectionListener l : toFire) {
            l.onDisconnect();
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void checkStart() {
        if (connectionListeners.isEmpty() && imageListeners.isEmpty() && containerListeners.isEmpty()) {
            start();
        }
    }

    private void checkStop() {
        if (connectionListeners.isEmpty() && imageListeners.isEmpty() && containerListeners.isEmpty()) {
            stop();
        }
    }

    private void start() {
        synchronized (this) {
            stop = false;
            blocked = false;
            lastEvent = null;
            processor.post(new Runnable() {
                @Override
                public void run() {
                    DockerAction remote = new DockerAction(instance);
                    for (;;) {
                        try {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            DockerActionAccessor.getDefault().events(remote, lastEvent != null ? lastEvent.getTime() : null,
                                    DockerEventBus.this, DockerEventBus.this);
                        } catch (DockerException ex) {
                            synchronized (DockerEventBus.this) {
                                if (stop) {
                                    return;
                                }
                            }
                            LOGGER.log(Level.INFO, null, ex);
                        }

                        blocked = true;
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            });
        }
    }

    private void stop() {
        // FIXME close the socket to stop waiting for events
        Endpoint current;
        synchronized (this) {
            stop = true;
            current = endpoint;
            endpoint = null;
        }
        try {
            if (current != null) {
                current.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }
}
