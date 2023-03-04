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

package org.netbeans.modules.hudson.impl;

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public class HudsonFolderImpl implements HudsonFolder, OpenableInBrowser, HudsonChangeListener {

    private final ChangeSupport cs = new ChangeSupport(this);
    private final HudsonInstanceImpl instance;
    private final String name, url;
    private BuilderConnector.InstanceData children;
    private Collection<HudsonJob> jobs;
    private Collection<HudsonFolder> folders;

    HudsonFolderImpl(HudsonInstanceImpl instance, String name, String url) {
        this.instance = instance;
        this.name = name;
        this.url = url;
        instance.addHudsonChangeListener(WeakListeners.create(HudsonChangeListener.class, this, instance));
    }

    @Override public String getName() {
        return name;
    }

    @Override public String getUrl() {
        return url;
    }

    private synchronized void load() {
        if (children == null) {
            children = instance.getBuilderConnector().getInstanceData(this, true);
            jobs = instance.createJobs(children.getJobsData());
            folders = instance.createFolders(children.getFoldersData());
        }
    }

    @Override public Collection<HudsonJob> getJobs() {
        load();
        return jobs;
    }

    @Override public Collection<HudsonFolder> getFolders() {
        load();
        return folders;
    }

    @Override public HudsonInstance getInstance() {
        return instance;
    }

    @Override public String toString() {
        return url;
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof HudsonFolderImpl && ((HudsonFolderImpl) obj).url.equals(url);
    }

    @Override public int hashCode() {
        return url.hashCode();
    }

    @Override public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override public void stateChanged() {
        synchronized (this) {
            children = null;
        }
        cs.fireChange();
    }

    @Override public void contentChanged() {}

}
