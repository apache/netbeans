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
package org.netbeans.modules.gradle.dists;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.dists.api.GradleDistProject;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;

/**
 *
 * @author lkishalmi
 */
@NbBundle.Messages({
    "# {0} - source group name",
    "MAIN_DIST=Distribution Files",
    "# {0} - source group name",
    "OTHER_DIST={0} Distribution Files",
})
public final class DistributionSourcesImpl implements Sources {

    public static final String DISTRIBUTION_SOURCES = "distribution"; //NOI18N
    private final Project proj;
    private final ChangeSupport cs = new ChangeSupport(this);

    private final PropertyChangeListener pcl;

    private SourceGroup[] cache;
    private Set<String> dists;
    
    public DistributionSourcesImpl(Project proj) {
        this.proj = proj;
        pcl = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.get(proj).isUnloadable()) {
                return; //let's just continue with the old value, stripping classpath for broken project and re-creating it later serves no greater good.
            }
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())
                    || NbGradleProject.PROP_RESOURCES.equals(evt.getPropertyName())) {
                checkChanges(true);
            }
        };
    }

    
    @Override
    public SourceGroup[] getSourceGroups(String type) {
        if (DISTRIBUTION_SOURCES.equals(type)) {
            if (cache == null) {
                List<SourceGroup> ret = new ArrayList<>(2);
                GradleDistProject gdp = GradleDistProject.get(proj);
                if (gdp != null) {
                    dists = gdp.getAvailableDistributions();
                    for (String distribution : dists) {
                        FileObject rootFO = FileUtil.toFileObject(gdp.getDistributionSource(distribution));
                        if (rootFO != null) {
                            
                            String displayName = "main".equals(distribution) ?  //NOI18N
                                    Bundle.MAIN_DIST(distribution):
                                    Bundle.OTHER_DIST(distribution);
                            String sgName = "main".equals(distribution) ? "01main" : "02" + distribution; //NOI18N
                            SourceGroup grp = GenericSources.group(proj, rootFO, sgName, displayName, null, null);
                            ret.add(grp);
                        }
                    }
                }
                cache = ret.toArray(new SourceGroup[0]);
            }
            return cache;
        }
        return new SourceGroup[0];
    }

    
    private void checkChanges(boolean fireChanges) {
        boolean changed = dists == null;

        if (GradleDistProject.get(proj) != null) {
            Set<String> newDists = GradleDistProject.get(proj).getAvailableDistributions();
            if (dists != null) {
                Set<String> enteringGroups = new HashSet<>(newDists);
                Set<String> leavingGroups = new HashSet<>(dists);
                Set<String> remainingGroups = new HashSet<>(newDists);
                remainingGroups.retainAll(dists);
                enteringGroups.removeAll(remainingGroups);
                leavingGroups.removeAll(remainingGroups);
                changed = !leavingGroups.isEmpty() || !enteringGroups.isEmpty();
            }
            dists = newDists;
            if (changed) {
                cache = null;
            }
        }
        if (changed && fireChanges) {
            cs.fireChange();
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        if (!cs.hasListeners()) {
            NbGradleProject.addPropertyChangeListener(proj, pcl);
        }
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
        if (!cs.hasListeners()) {
            NbGradleProject.removePropertyChangeListener(proj, pcl);
        }
    }

}
