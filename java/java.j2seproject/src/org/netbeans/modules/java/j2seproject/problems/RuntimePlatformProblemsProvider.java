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

package org.netbeans.modules.java.j2seproject.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver;
import org.netbeans.modules.java.j2seproject.api.J2SERuntimePlatformProvider;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-java-j2seproject")
public class RuntimePlatformProblemsProvider implements ProjectProblemsProvider, PropertyChangeListener, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(RuntimePlatformProblemsProvider.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(RuntimePlatformProblemsProvider.class);
    private static final String CFG_PATH = "nbproject/configs"; //NOI18N

    private final Project project;
    private final PropertyChangeSupport support;    
    private final AtomicBoolean listens;

    //@GuardedBy("this")
    private long eventId;
    //@GuardedBy("this")
    private Collection<? extends ProjectProblem> problemCache;

    public RuntimePlatformProblemsProvider(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
        this.support = new PropertyChangeSupport(this);
        this.listens = new AtomicBoolean();
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.removePropertyChangeListener(listener);
    }    

    @NonNull
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        Collection< ? extends ProjectProblem> problems;
        long currentId;
        synchronized (this) {
             problems = problemCache;
             currentId = eventId;
        }
        if (problems == null) {
            initListeners();
            problems = findProblems();
            synchronized (this) {
                if (eventId == currentId) {
                    problemCache = problems;
                } else if (problemCache != null) {
                     problems = problemCache;
                }
            }
        }
        assert problems != null;
        return problems;
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        Parameters.notNull("evt", evt); //NOI18N
        final String propName = evt.getPropertyName();
        if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName) ||
            ProjectProperties.JAVAC_TARGET.equals(propName) ||
            ProjectProperties.JAVAC_PROFILE.equals(propName)) {
            resetAndFire();
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        resetAndFire();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        resetAndFire();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        resetAndFire();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        resetAndFire();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void initListeners() {
        if (listens.compareAndSet(false, true)) {
            final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(
                this,
                jpm));
            J2SEProject j2sePrj = project.getLookup().lookup(J2SEProject.class);
            if (j2sePrj != null) {
                j2sePrj.evaluator().addPropertyChangeListener(this);
            } else {
                LOG.log(
                   Level.WARNING,
                   "No property evaluator provider in project {0}({1})", //NOI18N
                   new Object[]{
                       ProjectUtils.getInformation(project).getDisplayName(),
                       FileUtil.getFileDisplayName(project.getProjectDirectory())
                   });
            }

            final FileObject projectFolder = project.getProjectDirectory();
            if (projectFolder != null) {
                final File projectDir = FileUtil.toFile(projectFolder);
                if (projectDir != null) {
                    final File cfgDir = new File(projectDir, CFG_PATH);   //NOI18N
                    FileUtil.addFileChangeListener(this, cfgDir);
                }
            }
        }
    }

    private void resetAndFire() {
        synchronized (this) {
            problemCache = null;
            eventId++;
        }
        support.firePropertyChange(PROP_PROBLEMS, null, null);
    }

    @NonNull
    private Collection<? extends ProjectProblem> findProblems() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Collection<? extends ProjectProblem>>() {
            @Override
            public Collection<? extends ProjectProblem> run() {
                final Collection<ProjectProblem> collector = new HashSet<>();
                final FileObject prjDir = project.getProjectDirectory();
                if (prjDir != null) {
                    final FileObject cfgFolder = prjDir.getFileObject(CFG_PATH);
                    if (cfgFolder != null) {
                        final Set<JavaPlatform> allowedPlatforms = new HashSet<>();
                        final SourceLevelQuery.Result slqr = SourceLevelQuery.getSourceLevel2(prjDir);
                        final String sourceLevel = slqr.getSourceLevel();
                        final SourceLevelQuery.Profile profile = slqr.getProfile();
                        if (sourceLevel != null) {
                            for (J2SERuntimePlatformProvider rpp : project.getLookup().lookupAll(J2SERuntimePlatformProvider.class)) {
                                allowedPlatforms.addAll(rpp.getPlatformType(new SpecificationVersion(sourceLevel), profile));
                            }
                            for (FileObject cfgFile : cfgFolder.getChildren()) {
                                if (!cfgFile.hasExt("properties")) {    //NOI18N
                                    continue;
                                }
                                try {
                                    final EditableProperties ep = new EditableProperties(true);
                                    try (final InputStream in = cfgFile.getInputStream()){
                                        ep.load(in);
                                    }
                                    final String runtimePlatform = ep.getProperty(J2SEProjectProperties.PLATFORM_RUNTIME);
                                        if (runtimePlatform != null && !runtimePlatform.isEmpty()) {
                                            final JavaPlatform platform = findPlatform(runtimePlatform);
                                            if (platform == null) {
                                                final RuntimePlatformResolver resolver = new RuntimePlatformResolver(
                                                    project,
                                                    cfgFile.getName(),
                                                    ep.getProperty("$label"),   //NOI18N
                                                    runtimePlatform);
                                                collector.add(ProjectProblem.createError(
                                                    NbBundle.getMessage(RuntimePlatformProblemsProvider.class, "LBL_MissingRuntimePlatform"),
                                                    NbBundle.getMessage(RuntimePlatformProblemsProvider.class, "DESC_MissingRuntimePlatform", resolver.getDisplayName()),
                                                    resolver));
                                            } else if (!allowedPlatforms.contains(platform)) {
                                                final RuntimePlatformResolver resolver = new RuntimePlatformResolver(
                                                    project,
                                                    cfgFile.getName(),
                                                    ep.getProperty("$label"),   //NOI18N
                                                    platform,
                                                    new SpecificationVersion(sourceLevel),
                                                    profile);
                                                collector.add(ProjectProblem.createError(
                                                    NbBundle.getMessage(RuntimePlatformProblemsProvider.class, "LBL_InvalidRuntimePlatform"),
                                                    NbBundle.getMessage(RuntimePlatformProblemsProvider.class, "DESC_InvalidRuntimePlatform", resolver.getDisplayName()),
                                                    resolver));
                                            }
                                        }
                                } catch (IOException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            }
                        }
                    }
                }
                return Collections.unmodifiableCollection(collector);
            }
        });
    }

    private static JavaPlatform findPlatform(@NonNull final String platformId)  {
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (platformId.equals(jp.getProperties().get(J2SEProjectProperties.PROP_PLATFORM_ANT_NAME))) {
                return jp;
            }
        }
        return null;
    }

    @NonNull
    static SourceLevelQuery.Profile getPlatformProfile(@NonNull final JavaPlatform jp) {
        SourceLevelQuery.Profile profile = SourceLevelQuery.Profile.forName(jp.getProperties().get("netbeans.java.profile"));   //NOI18N
        if (profile == null) {
            profile = SourceLevelQuery.Profile.DEFAULT;
        }
        return profile;
    }

    static final class InvalidPlatformData {
        private final JavaPlatform jp;
        private final SpecificationVersion targetLevel;
        private final SourceLevelQuery.Profile profile;

        InvalidPlatformData(
            @NonNull final JavaPlatform jp,
            @NonNull final SpecificationVersion targetLevel,
            @NonNull final SourceLevelQuery.Profile profile) {
            Parameters.notNull("jp", jp);   //NOI18N
            Parameters.notNull("targetLevel", targetLevel); //NOI18N
            Parameters.notNull("profile", profile); //NOI18N
            this.jp = jp;
            this.targetLevel = targetLevel;
            this.profile = profile;
        }

        @NonNull
        JavaPlatform getJavaPlatform() {
            return jp;
        }

        @NonNull
        SpecificationVersion getTargetLevel() {
            return targetLevel;
        }

        @NonNull
        SourceLevelQuery.Profile getProfile() {
            return profile;
        }
    }
    
    private static final class RuntimePlatformResolver implements ProjectProblemResolver {

        private final Project prj;
        private final String cfgId;
        private final String cfgDisplayName;
        private final Union2<String,InvalidPlatformData> data;

        private RuntimePlatformResolver(
            @NonNull final Project project,
            @NonNull final String cfgId,
            @NullAllowed final String cfgDisplayName,
            @NonNull final Union2<String,InvalidPlatformData> data) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("cfgId", cfgId);     //NOI18N
            Parameters.notNull("data", data);   //NOI18N
            this.prj = project;
            this.cfgId = cfgId;
            this.cfgDisplayName = cfgDisplayName == null ?
                cfgId :
                cfgDisplayName;
            this.data = data;
        }
        
        RuntimePlatformResolver(
            @NonNull final Project project,
            @NonNull final String cfgId,
            @NullAllowed final String cfgDisplayName,
            @NonNull final String platformId) {
            this(
                project,
                cfgId,
                cfgDisplayName,
                Union2.<String,InvalidPlatformData>createFirst(platformId));
        }

        RuntimePlatformResolver(
            @NonNull final Project project,
            @NonNull final String cfgId,
            @NullAllowed final String cfgDisplayName,
            @NonNull final JavaPlatform jp,
            @NonNull final SpecificationVersion targetLevel,
            @NonNull final SourceLevelQuery.Profile profile) {
            this(
                project,
                cfgId,
                cfgDisplayName,
                Union2.<String,InvalidPlatformData>createSecond(
                    new InvalidPlatformData(jp, targetLevel, profile)));
        }

        String getDisplayName() {
            return cfgDisplayName;
        }

        @Override
        public Future<Result> resolve() {
            final ResolveBrokenRuntimePlatform panel = data.hasFirst() ?
                    ResolveBrokenRuntimePlatform.createMissingPlatform(
                        prj,
                        data.first()) :
                    ResolveBrokenRuntimePlatform.createInvalidPlatform(
                        prj,
                        data.second());
            final OK okButton = new OK(panel);
            final DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(RuntimePlatformProblemsProvider.class, "TITLE_MissingRuntimePlatform"),
                    true,
                    new Object[] {DialogDescriptor.CANCEL_OPTION, okButton},
                    okButton,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            if (DialogDisplayer.getDefault().notify(dd) == okButton) {
                final boolean isSourceLevelChange = panel.isDowngradeSourceLevel();
                final String newPlatformId = panel.isSpecificPlatform() ?
                    panel.getRuntimePlatform().getProperties().get(J2SEProjectProperties.PROP_PLATFORM_ANT_NAME) :
                    null;
                final FutureTask<Result> res = new FutureTask<>(new Callable<Result>() {
                    @Override
                    public Result call() throws Exception {
                        if (isSourceLevelChange) {
                            return resolveSourceLevelImpl(data.second().jp);
                        } else {
                            return resolvePlatformImpl(newPlatformId);
                        }
                    }
                });
                RP.post(res);
                return res;
            }
            final RunnableFuture<Result> res = new FutureTask<>(
                new Runnable() {
                    @Override
                    public void run() {
                    }
                },
                Result.create(Status.UNRESOLVED));
            res.run();
            return res;
        }

        @Override
        public boolean equals(@NullAllowed final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RuntimePlatformResolver)) {
                return false;
            }
            final RuntimePlatformResolver other = (RuntimePlatformResolver) obj;
            final FileObject thisPrjDir = prj.getProjectDirectory();
            final FileObject otherPrjDir = other.prj.getProjectDirectory();
            return thisPrjDir == null ? otherPrjDir == null : thisPrjDir.equals(otherPrjDir) &&
                data.equals(other.data);
        }

        @Override
        public int hashCode() {
            int res = 17;
            res = res * 31 + Objects.hashCode(prj.getProjectDirectory());
            res = res * 31 + data.hashCode();
            return res;
        }

        @NonNull
        private Result resolvePlatformImpl(@NullAllowed final String newPlatformId) throws Exception {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Result>() {
                @Override
                public Result run() throws Exception {
                    FileObject prjDir = prj.getProjectDirectory();
                    if (prjDir != null) {
                        final FileObject cfgDir = prjDir.getFileObject(CFG_PATH);
                        if (cfgDir != null) {
                            FileObject cfg = cfgDir.getFileObject(cfgId, "properties"); //NOI18N
                            if (cfg != null) {
                                final EditableProperties ep = new EditableProperties(true);
                                try (final InputStream in = cfg.getInputStream()) {
                                    ep.load(in);
                                }
                                if (newPlatformId == null) {
                                    ep.remove(J2SEProjectProperties.PLATFORM_RUNTIME);
                                } else {
                                    ep.setProperty(J2SEProjectProperties.PLATFORM_RUNTIME, newPlatformId);
                                }
                                final FileLock lock = cfg.lock();
                                try (OutputStream out = cfg.getOutputStream(lock)) {
                                    ep.store(out);
                                } finally {
                                    lock.releaseLock();
                                }
                                for (J2SECustomPropertySaver saver : prj.getLookup().lookupAll(J2SECustomPropertySaver.class)) {
                                    saver.save(prj);
                                }
                                return Result.create(Status.RESOLVED);
                            }
                        }
                    }
                    return Result.create(Status.UNRESOLVED);
                }
            });
        }

        @NonNull
        private Result resolveSourceLevelImpl(@NonNull final JavaPlatform jp) throws Exception {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Result>() {
                @Override
                public Result run() throws Exception {
                    final J2SEProject j2sePrj = prj.getLookup().lookup(J2SEProject.class);
                    if (j2sePrj != null) {
                        final EditableProperties props = j2sePrj.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.setProperty(ProjectProperties.JAVAC_SOURCE, jp.getSpecification().getVersion().toString());
                        props.setProperty(ProjectProperties.JAVAC_TARGET, jp.getSpecification().getVersion().toString());
                        final SourceLevelQuery.Profile profile = getPlatformProfile(jp);
                        if (profile == SourceLevelQuery.Profile.DEFAULT) {
                            props.remove(ProjectProperties.JAVAC_PROFILE);
                        } else {
                            props.setProperty(ProjectProperties.JAVAC_PROFILE, profile.getName());
                        }
                        j2sePrj.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(prj);
                    }
                    return Result.create(Status.UNRESOLVED);
                }
            });
        }
    }

    private static class OK extends JButton implements ChangeListener {

        private ResolveBrokenRuntimePlatform panel;

        OK (@NonNull final ResolveBrokenRuntimePlatform panel) {
            super(NbBundle.getMessage(RuntimePlatformProblemsProvider.class,"LBL_OK"));
            Parameters.notNull("panel", panel);
            this.panel = panel;
            panel.addChangeListener(this);
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            setEnabled(panel.hasValidData());
        }

    }

}
