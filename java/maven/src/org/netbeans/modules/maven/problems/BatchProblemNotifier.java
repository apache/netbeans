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

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TestChecker;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ReactorChecker;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 * Keeps track of problems in all open Maven projects and offers to do reactor sanity builds.
 */
public class BatchProblemNotifier {

    private static final Logger LOG = Logger.getLogger(BatchProblemNotifier.class.getName());

    /** reactors by root directory */
    private static final Map<File,Reactor> reactors = new HashMap<File,Reactor>();
    private static final Map<NbMavenProjectImpl,File> roots = new WeakHashMap<NbMavenProjectImpl,File>();

    public static void opened(NbMavenProjectImpl p) {
        ProblemReporterImpl pr = p.getProblemReporter();
        Set<File> missingArtifacts = pr.getMissingArtifactFiles();
        if (!missingArtifacts.isEmpty()) {
            Set<File> files = new HashSet<File>();
            for (File file : missingArtifacts) {
                if (file != null) {
                    files.add(file);
                } 
            }
            final File root = ReactorChecker.findReactor(p.getProjectWatcher()).getMavenProject().getBasedir();
            synchronized (roots) {
                roots.put(p, root);
            }
            File basedir = p.getPOMFile().getParentFile();
            String path = FileUtilities.relativizeFile(root, basedir);
            if (path == null) {
                path = basedir.getAbsolutePath();
            }
            synchronized (reactors) {
                Reactor reactor = reactors.get(root);
                if (reactor == null) {
                    reactor = new Reactor(root);
                    reactors.put(root, reactor);
                }
                reactor.register(path, files);
            }
        }
    }

    public static void closed(NbMavenProjectImpl p) {
        File root;
        synchronized (roots) {
            root = roots.remove(p);
        }
        if (root == null) {
            return;
        }
        File basedir = p.getPOMFile().getParentFile();
        String path = FileUtilities.relativizeFile(root, basedir);
        if (path == null) {
            path = basedir.getAbsolutePath();
        }
        synchronized (reactors) {
            Reactor reactor = reactors.get(root);
            if (reactor != null) {
                reactor.unregister(path);
            }
        }
    }

    static void resolved(File f) {
        synchronized (reactors) {
            for (Reactor reactor : new ArrayList<Reactor>(reactors.values())) {
                reactor.resolved(f);
            }
        }
    }

    @Messages({
//        "dialog_title=Run Priming Build",
        "# {0} - directory name of reactor", "build_label=Priming {0}"
    })
    private static void showUI(File reactor, Set<String> projects) {
//        RunGoalsPanel pnl = new RunGoalsPanel();
        BeanRunConfig cfg = new BeanRunConfig();
        String label = build_label(reactor.getName());
        cfg.setExecutionName(label);
        cfg.setTaskDisplayName(label);
        cfg.setExecutionDirectory(reactor);
        NbMavenProject mavenPrj = null;
        try {
            FileObject reactorFO = FileUtil.toFileObject(reactor);
            if (reactorFO != null && reactorFO.isFolder()) {
                Project reactorP = ProjectManager.getDefault().findProject(reactorFO);
                if (reactorP != null) {
                    mavenPrj = reactorP.getLookup().lookup(NbMavenProject.class);
                    cfg.setProject(reactorP);
                    // Similar to ReactorChecker, except there can be multiple submodules to build.
                    M2Configuration m2c = reactorP.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration();
                    if (m2c != null) {
                        cfg.setActivatedProfiles(m2c.getActivatedProfiles());
                    }
                }
            }
        } catch (IOException x) {
            LOG.log(Level.FINE, null, x);
        }
        StringBuilder pl = new StringBuilder();
        for (String project : projects) {
            if (pl.length() > 0) {
                pl.append(',');
            }
            pl.append(project);
        }
        // validate, test-compile, dependency:go-offline also possible
        if (mavenPrj != null
            && mavenPrj.getMavenProject().getVersion() != null 
            && mavenPrj.getMavenProject().getVersion().endsWith("SNAPSHOT")) {
            cfg.setGoals(Arrays.asList("--fail-at-end", "--also-make", "--projects", pl.toString(), "install"));
        } else {
            cfg.setGoals(Arrays.asList("--fail-at-end", "--also-make", "--projects", pl.toString(), "package"));
        }
        cfg.setUpdateSnapshots(true);
        cfg.setProperty(TestChecker.PROP_SKIP_TEST, "true");
//        pnl.readConfig(cfg);
//        DialogDescriptor dd = new DialogDescriptor(pnl, dialog_title());
//        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
//            LOG.log(Level.FINE, "running build for {0}", reactor);
//            pnl.applyValues(cfg);
            RunUtils.run(cfg);
//        }
    }

    private static class Reactor implements ActionListener {

        final File root;

        /** affected open projects, as (usually) relative paths from reactor root to sets of artifacts still missing */
        final Map<String,Set<File>> projects = new TreeMap<String,Set<File>>();

        final Notification n;

        @Messages({
            "# {0} - directory basename", "build_title=Build {0}",
            "# {0} - full directory path", "build_details=Run priming build in {0}"
        })
        Reactor(File root) {
            assert Thread.holdsLock(reactors);
            this.root = root;
            n = NotificationDisplayer.getDefault().notify(
                build_title(root.getName()),
                ImageUtilities.image2Icon(ImageUtilities.mergeImages(
                    ImageUtilities.loadImage(IconResources.MAVEN_ICON, true),
                    ImageUtilities.loadImage(IconResources.BROKEN_PROJECT_BADGE_ICON, true), 8, 0)),
                build_details(root), this);
            LOG.log(Level.FINE, "created for {0}", root);
        }

        void register(String path, Set<File> files) {
            assert Thread.holdsLock(reactors);
            projects.put(path, files);
            LOG.log(Level.FINE, "registered {0} for {1} in {2}", new Object[] {files, path, root});
        }

        void unregister(String path) {
            assert Thread.holdsLock(reactors);
            projects.remove(path);
            LOG.log(Level.FINE, "unregistered {0} in {1}", new Object[] {path, root});
            checkComplete();
        }

        void resolved(File f) {
            assert Thread.holdsLock(reactors);
            Iterator<Map.Entry<String,Set<File>>> it = projects.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,Set<File>> entry = it.next();
                String path = entry.getKey();
                Set<File> files = entry.getValue();
                if (files.remove(f)) {
                    LOG.log(Level.FINE, "resolved {0} for {1} in {2}", new Object[] {f, path, root});
                }
                if (files.isEmpty()) {
                    it.remove();
                    LOG.log(Level.FINE, "completed {0} in {1}", new Object[] {path, root});
                }
            }
            checkComplete();
        }

        private void checkComplete() {
            if (projects.isEmpty()) {
                n.clear();
                reactors.remove(root);
                LOG.log(Level.FINE, "completed {0}", root);
            }
        }

        @Override public void actionPerformed(ActionEvent e) {
            LOG.log(Level.FINE, "showing UI for {0} with {1}", new Object[] {Reactor.this.root, projects.keySet()});
            synchronized (reactors) {
                reactors.remove(Reactor.this.root);
            }
            showUI(Reactor.this.root, projects.keySet());
        }
        
    }

    private BatchProblemNotifier() {}

}
