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
package org.netbeans.modules.java.api.common.problems;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MissingModuleProblemsProvider implements ProjectProblemsProvider, PreferenceChangeListener, LookupListener {

    private static final Logger LOG = Logger.getLogger(MissingModuleProblemsProvider.class.getName());
    private static final String PATTERN_REQ_MODULE = "requiredModule-";    //NOI18N

    private static final AtomicBoolean catalogRefreshed = new AtomicBoolean();

    private final PropertyChangeSupport listeners;
    private final AtomicReference<Collection<ProjectProblem>> cache;
    private final AtomicReference<Preferences> prefsCache;
    private final AtomicReference<Lookup.Result<ModuleInfo>> modulesResult;
    private final Project project;

    MissingModuleProblemsProvider(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
        this.listeners = new PropertyChangeSupport(this);
        this.cache = new AtomicReference<>();
        this.prefsCache = new AtomicReference<>();
        this.modulesResult = new AtomicReference<>();
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        Collection<ProjectProblem> problems = cache.get();
        if (problems == null) {
            try {
                listenOnModules();
                final Preferences prefs = getPreferences();
                problems = Arrays.stream(prefs.keys())
                        .filter((n) -> n.startsWith(PATTERN_REQ_MODULE))
                        .map((n) -> prefs.get(n, PROP_PROBLEMS))
                        .filter((mn) -> mn != null)
                        .filter(MissingModuleProblemsProvider::notInstalled)
                        .map((modName) -> ProjectProblem.createWarning(
                            NbBundle.getMessage(MissingModuleProblemsProvider.class, "WARN_MissingRequiredModule"),
                            NbBundle.getMessage(MissingModuleProblemsProvider.class, "DESC_MissingRequiredModule",
                                    ProjectUtils.getInformation(project).getDisplayName(),
                                    modName),
                            new ReqModuleProblem(modName)))
                        .collect(Collectors.toList());
            } catch (BackingStoreException e) {
                Exceptions.printStackTrace(e);
                problems = Collections.emptySet();
            }
            if (!cache.compareAndSet(null, problems)) {
                final Collection<ProjectProblem> current = cache.get();
                if (current != null) {
                    problems = current;
                }
            }
        }
        return problems;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        problemsChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        problemsChanged();
    }

    private void problemsChanged() {
        this.cache.set(null);
        this.listeners.firePropertyChange(PROP_PROBLEMS, null, null);
    }

    private void listenOnModules() {
        Lookup.Result<ModuleInfo> modules = modulesResult.get();
        if (modules == null) {
            modules = Lookup.getDefault().lookupResult(ModuleInfo.class);
            if (modulesResult.compareAndSet(null, modules)) {
                modules.addLookupListener(WeakListeners.create(LookupListener.class, this, modules));
            }
        }
    }

    private Preferences getPreferences() {
        Preferences prefs = prefsCache.get();
        if (prefs == null) {
            prefs = ProjectUtils.getPreferences(project, MissingModuleProblemsProvider.class, true);
            if (prefsCache.compareAndSet(null, prefs)) {
                prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
            }
        }
        return prefs;
    }

    private static boolean notInstalled(@NonNull final String cnb) {
        final ModuleInfo module = Modules.getDefault().findCodeNameBase(cnb);
        return module == null;
    }

    private static void refreshModuleList() {
        if (catalogRefreshed.compareAndSet(false, true)) {
            final ProgressHandle refreshHandle = ProgressHandle.createHandle(NbBundle.getMessage(
                    MissingModuleProblemsProvider.class,
                    "TXT_ModuleListRefresh"));
            refreshHandle.start();
            try {
                for (UpdateUnitProvider provider : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false)) {
                    try {
                        provider.refresh(refreshHandle, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } finally {
                refreshHandle.finish();
            }
        }
    }

    @CheckForNull
    private static UpdateUnit findUpdateUnit(@NonNull final String moduleCNB) {
        for (UpdateUnit updateUnit : UpdateManager.getDefault().getUpdateUnits()) {
             final String codeName = updateUnit.getCodeName();
             if (moduleCNB.equals(codeName)) {
                 return updateUnit;
            }
        }
        return null;
    }

    private static InstallationResult installUpdate(@NonNull final UpdateElement update) {
        final ProgressHandle installHandle = ProgressHandle.createHandle(NbBundle.getMessage(
                MissingModuleProblemsProvider.class,
                "TXT_InstallModule",
                update.getDisplayName()));
        installHandle.start();
        try {
            final OperationContainer<InstallSupport> container = OperationContainer.createForInstall();
            container.add(Collections.singleton(update));
            final InstallSupport support = container.getSupport();
            try {
                final InstallSupport.Validator v = support.doDownload(installHandle, true, true);
                final InstallSupport.Installer i = support.doValidate(v, installHandle);
                final Restarter r = support.doInstall(i, installHandle);
                return InstallationResult.success(support, r);
            } catch (OperationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            installHandle.finish();
        }
        return InstallationResult.failure();
    }

    private static final class InstallationResult {
        private final boolean success;
        private final InstallSupport support;
        private final Restarter restarter;

        private InstallationResult(
                final boolean success,
                final InstallSupport support,
                final Restarter restarter) {
            this.success = success;
            this.support = support;
            this.restarter = restarter;
        }

        boolean isSuccess() {
            return success;
        }

        Optional<Pair<InstallSupport,Restarter>> getRestarter() {
            if (!isSuccess()) {
                throw new IllegalStateException("Failed installation.");
            }
            return Optional.ofNullable(restarter)
                    .map((r) -> Pair.of(support, r));
        }

        static InstallationResult success(
                @NonNull final InstallSupport support,
                @NullAllowed final Restarter restarter) {
            Parameters.notNull("support", support);
            return new InstallationResult(true, support, restarter);
        }

        static InstallationResult failure() {
            return new InstallationResult(false, null, null);
        }
    }

    private static final class ReqModuleProblem implements ProjectProblemResolver {
        private static final RequestProcessor WORKER = new RequestProcessor(ReqModuleProblem.class);

        private final String moduleCNB;

        ReqModuleProblem(@NonNull final String moduleCNB) {
            Parameters.notNull("moduleCNB", moduleCNB);   //NOI18N
            this.moduleCNB = moduleCNB;
        }

        @Override
        public Future<Result> resolve() {
            return WORKER.submit(() -> {
                refreshModuleList();
                final UpdateUnit moduleToInstall = findUpdateUnit(moduleCNB);
                if (moduleToInstall != null) {
                    final UpdateElement update = findLatest(moduleToInstall.getAvailableUpdates());
                    if (update != null) {
                        final InstallationResult instRes = installUpdate(update);
                        if (instRes.isSuccess()) {
                            instRes.getRestarter().ifPresent((p) -> {
                                maybeRestart(p.first(), p.second());
                            });
                            return Result.create(Status.RESOLVED);
                        } else {
                            showError(NbBundle.getMessage(
                            MissingModuleProblemsProvider.class,
                            "ERR_FailedToInstallModule",
                            moduleCNB));
                        }
                    }
                } else {
                    showError(NbBundle.getMessage(
                            MissingModuleProblemsProvider.class,
                            "ERR_NoSuchModule",
                            moduleCNB));
                }
                return Result.create(Status.UNRESOLVED);
            });
        }

        private static UpdateElement findLatest(List<? extends UpdateElement> updates) {
            UpdateElement newest = null;
            for (UpdateElement ue : updates) {
                if (newest == null || compare(newest, ue) < 0) {
                    newest = ue;
                }
            }
            return newest;
        }

        private static int compare(UpdateElement u1, UpdateElement u2) {
            return new SpecificationVersion(u1.getSpecificationVersion()).compareTo(
                    new SpecificationVersion(u2.getSpecificationVersion()));
        }

        private static void showError(@NonNull final String message) {
            SwingUtilities.invokeLater(() -> {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        message,
                        NotifyDescriptor.Message.ERROR_MESSAGE));
            });
        }

        private static void maybeRestart(
                @NonNull final InstallSupport is,
                @NonNull final Restarter r) {
            SwingUtilities.invokeLater(()-> {
                final Object option = DialogDisplayer.getDefault().notify(new DialogDescriptor.Message.Confirmation(
                        NbBundle.getMessage(MissingModuleProblemsProvider.class, "TXT_RestartConfirm"),
                        NbBundle.getMessage(MissingModuleProblemsProvider.class, "TITLE_Restart"),
                        DialogDescriptor.YES_NO_OPTION));
                if (option == DialogDescriptor.YES_OPTION) {
                    final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(
                            MissingModuleProblemsProvider.class,
                            "TXT_Restart"));
                    handle.start();
                    try {
                        try {
                            is.doRestart(r, handle);
                        } catch (OperationException e) {
                            //Cancelled
                            is.doRestartLater(r);
                        }
                    } finally {
                        handle.finish();
                    }
                } else {
                    is.doRestartLater(r);
                }
            });
        }
    }
}
