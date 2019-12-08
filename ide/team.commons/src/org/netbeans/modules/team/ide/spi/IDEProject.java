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
package org.netbeans.modules.team.ide.spi;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Icon;

/**
 * Representation of a standalone (top-level) project the user can open in the
 * IDE directly. It is used for visualization of projects the user opened or can
 * open from the versioned sources obtained from a team server project. <p>
 * Specific subclass should override addDeleteListener and implement the
 * actual listening on project deletion.
 *
 * @author Tomas Pavek
 */
public abstract class IDEProject {
    private final String displayName;
    private final Icon icon;
    private final URL url;

    private final List<DeleteListener> deleteListeners = new CopyOnWriteArrayList<>();

    protected IDEProject(String displayName, Icon icon, URL url) {
        this.displayName = displayName;
        this.icon = icon;
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public URL getURL() {
        return url;
    }

    /**
     * To be overridden in subclass to start observing the actual project deletion.
     * It should call notifyDeleted when the project is deleted.
     * @param l
     * @return true if listener was added (not existed already)
     */
    public synchronized boolean addDeleteListener(DeleteListener l) {
        if (deleteListeners.contains(l)) {
            return false;
        } else {
            deleteListeners.add(l);
            return true;
        }
    }

    public boolean removeDeleteListener(DeleteListener l) {
        return deleteListeners.remove(l);
    }

    protected final List<DeleteListener> getDeleteListeners() {
        return deleteListeners;
    }

    public final void notifyDeleted() {
        for (DeleteListener dl : deleteListeners) {
            dl.projectDeleted(this);
        }
    }

    public interface OpenListener {
        void projectsOpened(IDEProject[] prj);
    }

    public interface DeleteListener {
        void projectDeleted(IDEProject prj);
    }

    // -----

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IDEProject other = (IDEProject) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }
}
