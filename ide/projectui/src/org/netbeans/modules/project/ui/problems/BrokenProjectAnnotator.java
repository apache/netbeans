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
package org.netbeans.modules.project.ui.problems;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=ProjectIconAnnotator.class)
public class BrokenProjectAnnotator implements ProjectIconAnnotator, PropertyChangeListener {


    @StaticResource
    private static final String BROKEN_PROJECT_BADGE_PATH = "org/netbeans/modules/project/ui/resources/brokenProjectBadge.gif";    //NOI18N
    private static final URL BROKEN_PROJECT_BADGE_URL = BrokenProjectAnnotator.class.getClassLoader().getResource(BROKEN_PROJECT_BADGE_PATH);
    private static final Image BROKEN_PROJECT_BADGE = ImageUtilities.loadImage(BROKEN_PROJECT_BADGE_PATH, true);
    private static final int FIRE_DELAY = 500;
    private static final RequestProcessor FIRER = new RequestProcessor(BrokenProjectAnnotator.class.getName()+".fire", 1, false, false);   //NOI18N
    private static final RequestProcessor LOADER = new RequestProcessor(BrokenProjectAnnotator.class.getName()+".load", 5);   //NOI18N
    private static final Logger LOG = Logger.getLogger(BrokenProjectAnnotator.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor.Task task = FIRER.create(new Runnable(){
        @Override
        public void run() {
            changeSupport.fireChange();
            LOG.fine("Fire.");  //NOI18N
        }
    });
    private final Object cacheLock = new Object();
    //@GuardedBy("cacheLock")
    private final Map<Project, Integer> brokenCache = new WeakHashMap<Project, Integer>();
    //@GuardedBy("cacheLock")
    private final Map<ProjectProblemsProvider, Set<Reference<Project>>> problemsProvider2prj =
            new WeakHashMap<ProjectProblemsProvider, Set<Reference<Project>>>();


    @NonNull
    @Override
    public Image annotateIcon(
            @NonNull final Project project,
            @NonNull Image original,
            final boolean openedNode) {
        Parameters.notNull("project", project);     //NOI18N
        Parameters.notNull("original", original);   //NOI18N
        LOG.log(Level.FINE, "The annotateIcon called for project: {0}.", project);  //NOI18N
        Integer problemsCount = null;
        synchronized (cacheLock) {
            if (brokenCache.containsKey(project)) {
                problemsCount = brokenCache.get(project);
                LOG.log(Level.FINE, "In cache: {0}.", problemsCount);   //NOI18N
            } else {
                brokenCache.put(project, problemsCount);
                final ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
                if (ppp != null) {
                    ppp.addPropertyChangeListener(this);
                    Set<Reference<Project>> projects = problemsProvider2prj.get(ppp);
                    if (projects == null) {
                        projects = new HashSet<Reference<Project>>();
                        problemsProvider2prj.put(ppp, projects);
                    }
                    projects.add(new WeakReference<Project>(project));
                }
                LOG.fine("Added listeners.");    //NOI18N
            }
        }

        if (problemsCount == null) {
            problemsCount = 0;
            LOADER.post(new Runnable() {
                @Override
                public void run() {
                    final ProjectProblemsProvider provider = project.getLookup().lookup(ProjectProblemsProvider.class);
                    final Collection<? extends ProjectProblemsProvider.ProjectProblem> problems
                            = provider == null
                            ? Collections.<ProjectProblemsProvider.ProjectProblem>emptySet()
                            : provider.getProblems();
                    int pCount = problems.size();
                    synchronized (cacheLock) {
                        brokenCache.put(project, pCount);
                        LOG.log(Level.FINE, "Set {0} to cache.", pCount);   //NOI18N
                    }
                    task.schedule(FIRE_DELAY);
                }
            });
        }
        if (problemsCount > 0) {            
            final String message = problemsCount == 1 ?
                    NbBundle.getMessage(BrokenProjectAnnotator.class, "MSG_OneProblem") :
                    NbBundle.getMessage(BrokenProjectAnnotator.class, "MSG_MoreProblems", problemsCount);
            final String messageHtml = String.format(
                "<img src=\"%s\">&nbsp;%s",   //NOI18N
                BROKEN_PROJECT_BADGE_URL.toExternalForm(),
                message);
            original = ImageUtilities.mergeImages(
                    original,
                    ImageUtilities.assignToolTipToImage(BROKEN_PROJECT_BADGE, messageHtml),
                    8,
                    0);
        }
        return original;
    }

    @Override
    public void addChangeListener(@NonNull final ChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        changeSupport.addChangeListener(listener);

    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (ProjectProblemsProvider.PROP_PROBLEMS.equals(name)) {
            synchronized (cacheLock) {
                final Set<Reference<Project>> toRefresh = problemsProvider2prj.get(evt.getSource());
                if (toRefresh != null) {
                    LOG.fine("Event from known ProjectProblemsProvider -> clearing cache for:");  //NOI18N
                    for (final Iterator<Reference<Project>> it = toRefresh.iterator();it.hasNext();) {
                        final Reference<Project> ref = it.next();
                        final Project prj = ref.get();
                        if (prj != null) {
                            brokenCache.put(prj, null);
                            LOG.log(Level.FINE, "Project: {0}", prj);  //NOI18N
                        } else {
                            it.remove();
                        }
                    }
                } else {
                    LOG.fine("Event from unknown ProjectProblemsProvider -> clearing all caches.");  //NOI18N
                }
            }
            task.schedule(FIRE_DELAY);
        }
    }
}
