/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
