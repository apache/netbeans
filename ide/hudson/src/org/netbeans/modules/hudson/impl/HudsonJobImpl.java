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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import static org.netbeans.modules.hudson.impl.Bundle.*;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Implementation of the HudsonJob
 *
 * @author pblaha
 */
public class HudsonJobImpl implements HudsonJob, OpenableInBrowser {
    
    private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    
    private HudsonInstanceImpl instance;
    
    HudsonJobImpl(HudsonInstanceImpl instance) {
        this.instance = instance;
    }
    
    public void putProperty(String name, Object o) {
        if (o == null) {
            throw new NullPointerException("putProperty: " + name); // NOI18N
        }
        properties.putProperty(name, o);
    }
    
    @NonNull @Override public String getDisplayName() {
        return properties.getProperty(JOB_DISPLAY_NAME, String.class, getName());
    }
    
    @NonNull @Override public String getName() {
        String n = properties.getProperty(JOB_NAME, String.class);
        assert n != null;
        return n;
    }
    
    @NonNull @Override public String getUrl() {
        String url = properties.getProperty(JOB_URL, String.class);
        assert url != null && url.endsWith("/") : url;
        return url;
    }
    
    @NonNull @Override public Color getColor() {
        return properties.getProperty(JOB_COLOR, Color.class, Color.grey);
    }
    
    @Override public boolean isInQueue() {
        return properties.getProperty(JOB_IN_QUEUE, Boolean.class, false);
    }
    
    @Override public boolean isBuildable() {
        return properties.getProperty(JOB_BUILDABLE, Boolean.class, false);
    }
    
    @Override public int getLastBuild() {
        return properties.getProperty(JOB_LAST_BUILD, Integer.class, -1);
    }
    
    @Override public int getLastStableBuild() {
        return properties.getProperty(JOB_LAST_STABLE_BUILD, Integer.class, -1);
    }
    
    @Override public int getLastSuccessfulBuild() {
        return properties.getProperty(JOB_LAST_SUCCESSFUL_BUILD, Integer.class, -1);
    }
    
    @Override public int getLastFailedBuild() {
        return properties.getProperty(JOB_LAST_FAILED_BUILD, Integer.class, -1);
    }
    
    @Override public int getLastCompletedBuild() {
        return properties.getProperty(JOB_LAST_COMPLETED_BUILD, Integer.class, -1);
    }

    @Override public Collection<HudsonView> getViews() {
        return views;
    }
    
    void addView(HudsonView view) {
        views.add(view);
    }
    
    @Messages({"# {0} - job name", "MSG_Starting=Starting {0}"})
    @Override public void start() {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                MSG_Starting(this.getName()));
        handle.start();
        try {
            instance.getBuilderConnector().startJob(this);
        } finally {
            handle.finish();
        }
        instance.synchronize(false);
    }

    @Override public FileSystem getRemoteWorkspace() {
        return instance.getRemoteWorkspace(this);
    }
    
    public @Override boolean equals(Object o) {
        if (!(o instanceof HudsonJobImpl)) {
            return false;
        }
        
        final HudsonJobImpl j = (HudsonJobImpl) o;
        
        if (!Utilities.compareObjects(getDisplayName(), j.getDisplayName())) {
            return false;
        }
        if (!Utilities.compareObjects(getName(), j.getName())) {
            return false;
        }
        if (!Utilities.compareObjects(getUrl(), j.getUrl())) {
            return false;
        }
        if (!Utilities.compareObjects(getColor(), j.getColor())) {
            return false;
        }
        if (isInQueue() != j.isInQueue()) {
            return false;
        }
        if (isBuildable() != j.isBuildable()) {
            return false;
        }
        if (!Utilities.compareObjects(views, j.views)) {
            return false;
        }
        if (getLastCompletedBuild() != j.getLastCompletedBuild()) {
            return false;
        }
        if (!mavenModules.equals(j.mavenModules)) {
            return false;
        }
        
        return true;
    }

    public @Override int hashCode() {
        return getName().hashCode();
    }
    
    @Override public int compareTo(HudsonJob o) {
        if (this.isSalient() != o.isSalient()) {
           return this.isSalient() ? -1 : 1;
        } else {
            return getDisplayName().compareTo(o.getDisplayName());
        }
    }

    @Override
    public String toString() {
        return getUrl();
    }

    private Collection<? extends HudsonJobBuild> builds;
    @Override public synchronized Collection<? extends HudsonJobBuild> getBuilds() {
        if (builds == null) {
            builds = createBuilds(
                    instance.getBuilderConnector().getJobBuildsData(this));
        }
        return builds;
    }

    private Collection<? extends HudsonJobBuild> createBuilds(
            Collection<BuilderConnector.BuildData> data) {

        if (data == null) {
            return Collections.emptySet();
        }
        List<HudsonJobBuildImpl> buildList = new ArrayList<HudsonJobBuildImpl>();
        for (BuilderConnector.BuildData bd : data) {
            buildList.add(new HudsonJobBuildImpl(
                    this.getInstance().getBuilderConnector(), this,
                    bd.getNumber(), bd.isBuilding(), bd.getResult()));
        }
        return buildList;
    }

    @Override public HudsonInstanceImpl getInstance() {
        return instance;
    }

    @Override public boolean isSalient() {
        return instance.isSalient(this);
    }

    @Override public void setSalient(boolean b) {
        instance.setSalient(this, b);
    }

    final List<HudsonMavenModule> mavenModules = new LinkedList<HudsonMavenModule>();
    void addModule(@NonNull String name, @NonNull String displayName, @NonNull Color color, @NonNull String url) {
        mavenModules.add(new HudsonMavenModule(name, displayName, color, url));
    }
    static class HudsonMavenModule {
        final @NonNull String name;
        final @NonNull String displayName;
        final @NonNull Color color;
        final @NonNull String url;
        HudsonMavenModule(@NonNull String name, @NonNull String displayName, @NonNull Color color, @NonNull String url) {
            this.name = name;
            this.displayName = displayName;
            this.color = color;
            this.url = url;
        }
        public @Override boolean equals(Object other) {
            if (!(other instanceof HudsonMavenModule)) {
                return false;
            }
            HudsonMavenModule o = (HudsonMavenModule) other;
            return name.equals(o.name) && displayName.equals(o.displayName) && color == o.color && url.equals(o.url);
        }
        public @Override int hashCode() {
            return name.hashCode();
        }
        public @Override String toString() {
            return url;
        }
    }
}
