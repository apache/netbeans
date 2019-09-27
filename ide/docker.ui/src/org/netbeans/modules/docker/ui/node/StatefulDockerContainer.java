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
package org.netbeans.modules.docker.ui.node;

import java.io.Closeable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerException;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class StatefulDockerContainer implements Refreshable, Closeable {

    private static final Logger LOGGER = Logger.getLogger(StatefulDockerContainer.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(StatefulDockerContainer.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final DockerEvent.Listener listener = new DockerEvent.Listener() {
        @Override
        public void onEvent(DockerEvent event) {
            if (event.getId().equals(StatefulDockerContainer.this.container.getId())
                    && event.getStatus() != DockerEvent.Status.DESTROY && event.getStatus() != DockerEvent.Status.UNTAG) {
                DockerContainer.Status fresh = getStatus(event);
                if (fresh != null) {
                    update(fresh);
                } else {
                    refresh();
                }
            }
        }
    };

    private final DockerContainer container;

    private DockerContainerDetail detail;
    
    private boolean attached;
    
    public StatefulDockerContainer(DockerContainer container) {
        this.container = container;
        this.detail = new DockerContainerDetail(container.getName(), container.getStatus(), false, false);
        attach();
    }
    
    public final void attach() {
        synchronized (this) {
            if (!attached) {
                container.getInstance().addContainerListener(listener);
                attached = true;
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public DockerContainer getContainer() {
        return container;
    }

    public DockerContainerDetail getDetail() {
        synchronized (this) {
            return detail;
        }
    }

    @Override
    public void refresh() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                DockerAction action = new DockerAction(container.getInstance());
                try {
                    update(action.getDetail(container));
                } catch (DockerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        });
    }

    @Override
    public final void close() {
        synchronized (this) {
            if (attached) {
                container.getInstance().removeContainerListener(listener);
                attached = false;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.container);
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
        final StatefulDockerContainer other = (StatefulDockerContainer) obj;
        if (!Objects.equals(this.container, other.container)) {
            return false;
        }
        return true;
    }

    private void update(DockerContainer.Status status) {
        synchronized (this) {
            detail = new DockerContainerDetail(detail.getName(), status, detail.isStdin(), detail.isTty());
        }
        changeSupport.fireChange();
    }

    private void update(DockerContainerDetail value) {
        synchronized (this) {
            detail = value;
        }
        changeSupport.fireChange();
    }

    private static DockerContainer.Status getStatus(DockerEvent event) {
        DockerEvent.Status status = event.getStatus();
        switch (status) {
            case DIE:
                return DockerContainer.Status.STOPPED;
            case START:
                return DockerContainer.Status.RUNNING;
            case PAUSE:
                return DockerContainer.Status.PAUSED;
            case UNPAUSE:
                return DockerContainer.Status.RUNNING;
            default:
                return null;
        }
    }
}
