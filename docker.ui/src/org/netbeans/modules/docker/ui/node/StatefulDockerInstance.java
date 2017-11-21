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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerSupport;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class StatefulDockerInstance implements Refreshable {

    private static final RequestProcessor RP = new RequestProcessor(StatefulDockerInstance.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // FIXME default value
    private final AtomicBoolean available = new AtomicBoolean(true);

    private final DockerInstance.ConnectionListener listener = new DockerInstance.ConnectionListener() {
        @Override
        public void onConnect() {
            update(true);
        }

        @Override
        public void onDisconnect() {
            update(false);
        }
    };

    private final DockerInstance instance;

    public StatefulDockerInstance(DockerInstance instance) {
        this.instance = instance;
        instance.addConnectionListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public DockerInstance getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public void refresh() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                update(new DockerAction(instance).ping());
            }
        });
    }

    public void remove() {
        instance.removeConnectionListener(listener);
        DockerSupport.getDefault().removeInstance(instance);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.instance);
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
        final StatefulDockerInstance other = (StatefulDockerInstance) obj;
        if (!Objects.equals(this.instance, other.instance)) {
            return false;
        }
        return true;
    }

    private void update(boolean newValue) {
        boolean oldValue = available.getAndSet(newValue);
        if (oldValue != newValue) {
            changeSupport.fireChange();
        }
    }
}
