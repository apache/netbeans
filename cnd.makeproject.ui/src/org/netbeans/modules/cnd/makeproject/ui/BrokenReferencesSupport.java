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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.configurations.FormattingPropPanel;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class BrokenReferencesSupport {
    
    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class BrokenReferencesSupportFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(BrokenReferencesSupport.createPlatformVersionProblemProvider(owner));
        }
    }

    private BrokenReferencesSupport() {
    }

    @NonNull
    public static ProjectProblemsProvider createPlatformVersionProblemProvider(@NonNull final MakeProject project) {
        ProjectProblemsProviderImpl pp = new ProjectProblemsProviderImpl(project);
        pp.attachListeners();
        return pp;
    }

    public static boolean isIncorrectPlatform(ConfigurationDescriptorProvider projectDescriptorProvider) {
        if (projectDescriptorProvider.gotDescriptor()) {
            Configuration[] confs = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
            for (Configuration cf : confs) {
                if (cf.isDefault()) {
                    MakeConfiguration conf = (MakeConfiguration) cf;
                    if (conf.getDevelopmentHost().isLocalhost()
                            && CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform() != conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getActiveConfigurationName(ConfigurationDescriptorProvider projectDescriptorProvider) {
        if (projectDescriptorProvider.gotDescriptor()) {
            Configuration[] confs = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
            for (Configuration cf : confs) {
                if (cf.isDefault()) {
                    return cf.getName();
                }
            }
        }
        return "";
    }

    public static String getActiveConfigurationPlatfoirmName(ConfigurationDescriptorProvider projectDescriptorProvider) {
        if (projectDescriptorProvider.gotDescriptor()) {
            Configuration[] confs = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
            for (Configuration cf : confs) {
                if (cf.isDefault()) {
                    return ((MakeConfiguration)cf).getDevelopmentHost().getBuildPlatformConfiguration().getName();
                }
            }
        }
        return "";
    }

    private static boolean isIncorectVersion(@NonNull final MakeProject project) {
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        return view.isIncorectVersion();
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getReferenceProblems(@NonNull final MakeProject project) {
        final List<BrokenLinks.BrokenLink> brokenLinks = BrokenLinks.getBrokenLinks(project);
        if (!brokenLinks.isEmpty()) {
            ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Resolve_Name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Resolve_Description"), //NOI18N
                    new ToolCollectionResolverImpl(project, brokenLinks));
            return Collections.singleton(error);
        }
        return Collections.emptySet();
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getPlatformProblems(@NonNull final MakeProject project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (BrokenReferencesSupport.isIncorrectPlatform(cdp)) {
            String name = BrokenReferencesSupport.getActiveConfigurationName(cdp);
            String platform = BrokenReferencesSupport.getActiveConfigurationPlatfoirmName(cdp);
            final ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_resolve_name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_resolve_description", name, platform), //NOI18N
                    new PlatformResolverImpl(project));
            return Collections.singleton(error);
        }
        return Collections.emptySet();
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getClangFormattingStyleProblems(@NonNull final MakeProject project) {
        FileObject clangFormat = findClangFormattingStyles(project);
        if (clangFormat != null) {
            boolean wasFixed = NbPreferences.forModule(ClangStyleResolverImpl.class).getBoolean("clang-format-fixed-"+clangFormat.getPath(), false);
            if (!wasFixed) {
                String message = NbBundle.getMessage(ResolveReferencePanel.class, "clang_style_resolve_description", clangFormat.getPath()); //NOI18N
                final ProjectProblemsProvider.ProjectProblem error =
                        ProjectProblemsProvider.ProjectProblem.createError(
                        NbBundle.getMessage(ResolveReferencePanel.class, "clang_style_resolve_name"), //NOI18N
                        message,
                        new ClangStyleResolverImpl(project, clangFormat));
                return Collections.singleton(error);
            }
        }
        return Collections.emptySet();
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getFormattingStyleProblems(@NonNull final MakeProject project) {
        List<Style> styles = getUndefinedFormattingStyles(project);
        if (styles != null && !styles.isEmpty()) {
            for(Style style : styles) {
                String source = "";
                if (MIMENames.C_MIME_TYPE.equals(style.mime)) {
                    source = NbBundle.getMessage(ResolveReferencePanel.class, "style_c"); //NOI18N
                } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(style.mime)) {
                    source = NbBundle.getMessage(ResolveReferencePanel.class, "style_cpp"); //NOI18N
                } else if (MIMENames.HEADER_MIME_TYPE.equals(style.mime)) {
                    source = NbBundle.getMessage(ResolveReferencePanel.class, "style_header"); //NOI18N
                }
                String message = NbBundle.getMessage(ResolveReferencePanel.class, "style_resolve_description", style.aStyle.getDisplayName(), source); //NOI18N
                final ProjectProblemsProvider.ProjectProblem error =
                        ProjectProblemsProvider.ProjectProblem.createError(
                        NbBundle.getMessage(ResolveReferencePanel.class, "style_resolve_name"), //NOI18N
                        message,
                        new StyleResolverImpl(project, style));
                return Collections.singleton(error);
            }
        }
        return Collections.emptySet();
    }

    private static Style undefinedStyle(MakeProject project, String mime) {
        CodeStyleWrapper aStyle = project.getProjectFormattingStyle(mime);
        if (aStyle == null) {
            return null;
        }
        Map<String, CodeStyleWrapper> allStyles = FormattingPropPanel.getAllStyles(mime);
        for(Map.Entry<String, CodeStyleWrapper> entry : allStyles.entrySet()) {
            if (aStyle.getStyleId().equals(entry.getValue().getStyleId())) {
                return null;
            }
        }
        return new Style(aStyle, mime);
    }
        
    private static FileObject findClangFormattingStyles(MakeProject project) {
        if (project.isProjectFormattingStyle() == MakeProject.FormattingStyle.ClangFormat) {
            return null;
        }
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (!pdp.gotDescriptor()) {
            return null;
        }
        MakeConfigurationDescriptor cd = (MakeConfigurationDescriptor) pdp.getConfigurationDescriptor();
        FileObject baseDir = cd.getBaseDirFileObject();
        if (baseDir != null && baseDir.isValid()) {
            FileObject format = baseDir.getFileObject(CodeStyleWrapper.CLANG_FORMAT_FILE);
            if (format != null && format.isValid()) {
                //found clang format file
                return format;
            }
            for (String root : cd.getAbsoluteSourceRoots()) {
                try {
                    FileObject rootFo = new FSPath(baseDir.getFileSystem(), root).getFileObject();
                    if (rootFo != null && !baseDir.equals(rootFo)) {
                        format = rootFo.getFileObject(CodeStyleWrapper.CLANG_FORMAT_FILE);
                        if (format != null && format.isValid()) {
                            //found clang format file
                            return format;
                        }
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    private static List<Style> getUndefinedFormattingStyles(MakeProject project) {
        if (project.isProjectFormattingStyle() == MakeProject.FormattingStyle.Project) {
            return null;
        }
        List<Style> list = new ArrayList<>();
        Style s = undefinedStyle(project, MIMENames.C_MIME_TYPE);
        if (s != null) {
            list.add(s);
        }
        s = undefinedStyle(project, MIMENames.CPLUSPLUS_MIME_TYPE);
        if (s != null) {
            list.add(s);
        }
        s = undefinedStyle(project, MIMENames.HEADER_MIME_TYPE);
        if (s != null) {
            list.add(s);
        }
        return list;
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getVersionProblems(@NonNull final MakeProject project) {
        Set<ProjectProblemsProvider.ProjectProblem> set = new LinkedHashSet<>();
        if (BrokenReferencesSupport.isIncorectVersion(project)) {
            ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_resolve_name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_resolve_description"), //NOI18N
                    new VersionResolverImpl(project));
            set.add(error);
        }
        return set;
    }

    private static void reInitWithRemovedPrivate(final MakeProject project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor mcd = cdp.getConfigurationDescriptor();
        Configuration[] confs = mcd.getConfs().toArray();
        boolean save = false;
        for (Configuration cf : confs) {
            if (cf.isDefault()) {
                MakeConfiguration conf = (MakeConfiguration) cf;
                if (conf.getDevelopmentHost().isLocalhost()) {
                    final int platform1 = CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform();
                    final int platform2 = conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue();
                    if (platform1 != platform2) {
                        conf.getDevelopmentHost().getBuildPlatformConfiguration().setValue(platform1);
                        mcd.setModified();
                        save = true;
                    }
                }
            }
        }
        if (save) {
            mcd.save();
        }
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        view.reInit(mcd, false);
    }

    private static void reInitWithUnsupportedVersion(final MakeProject project) {
        ConfigurationDescriptorProvider cdp = project.getConfigurationDescriptorProvider();
        final MakeConfigurationDescriptor mcd = cdp.getConfigurationDescriptor();
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        view.reInit(mcd, true);
    }

    private static class ProjectProblemsProviderImpl implements ProjectProblemsProvider, PropertyChangeListener {

        private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
        private final MakeProject project;
        private final EnvProjectProblemsProvider envProblemsProvider;

        public ProjectProblemsProviderImpl(@NonNull final MakeProject project) {
            this.project = project;
            this.envProblemsProvider = new EnvProjectProblemsProvider(project);
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            problemsProviderSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            problemsProviderSupport.removePropertyChangeListener(listener);
        }

        @Override
        public Collection<? extends ProjectProblemsProvider.ProjectProblem> getProblems() {
            return problemsProviderSupport.getProblems(() -> {
                return ProjectManager.mutex().readAccess((Mutex.Action<Collection<? extends ProjectProblemsProvider.ProjectProblem>>) () -> {
                    final Set<ProjectProblemsProvider.ProjectProblem> newProblems = new LinkedHashSet<>();
                    Set<? extends ProjectProblem> versionProblems = getVersionProblems(project);
                    newProblems.addAll(versionProblems);
                    if (versionProblems.isEmpty()) {
                        newProblems.addAll(getReferenceProblems(project));
                        newProblems.addAll(getPlatformProblems(project));
                        newProblems.addAll(envProblemsProvider.getEnvProblems());
                        newProblems.addAll(getFormattingStyleProblems(project));
                        newProblems.addAll(getClangFormattingStyleProblems(project));
                    }
                    return Collections.unmodifiableSet(newProblems);
                });
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            problemsProviderSupport.fireProblemsChange();
        }

        void attachListeners() {
            project.getProjectConfigurationProvider().addPropertyChangeListener(this);
        }
    }

    /*package*/ static final class Done implements Future<ProjectProblemsProvider.Result> {

        private final ProjectProblemsProvider.Result result;

        Done(@NonNull final ProjectProblemsProvider.Result result) {
            Parameters.notNull("result", result);   //NOI18N
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public ProjectProblemsProvider.Result get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public ProjectProblemsProvider.Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    public static void updateProblems(Project project) {
        ProjectProblemsProvider pp = project.getLookup().lookup(ProjectProblemsProvider.class);
        if(pp instanceof ProjectProblemsProviderImpl) {
                ((ProjectProblemsProviderImpl)pp).propertyChange(null);
        }
    }
    
    /*package*/ static abstract class BaseProjectProblemResolver implements ProjectProblemResolver {

        private final MakeProject project;
        
        public BaseProjectProblemResolver(MakeProject project) {
            CndUtils.assertNotNull(project, "null project"); //NOI18N
            this.project = project;
        }
        
        protected final void updateProblems() {
            BrokenReferencesSupport.updateProblems(project);
        }

        protected final MakeProject getProject() {
            return project;
        }
    }
    
    private static class PlatformResolverImpl extends BaseProjectProblemResolver {
        private final String name;
        private final String platform;

        public PlatformResolverImpl(MakeProject project) {
            super(project);
            ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            name = BrokenReferencesSupport.getActiveConfigurationName(cdp);
            platform = BrokenReferencesSupport.getActiveConfigurationPlatfoirmName(cdp);
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            String title = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_fix_title"); //NOI18N
            String message = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_fix", name, platform); //NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(message,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                MakeProject project = getProject();
                if (project!= null) {
                    BrokenReferencesSupport.reInitWithRemovedPrivate(project);
                    updateProblems();
                }
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
            } else {
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
            }
        }

        @Override
        public int hashCode() {
            return this.getProject().hashCode() + PlatformResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PlatformResolverImpl other = (PlatformResolverImpl) obj;
            return this.getProject().equals(other.getProject());
        }
    }

    private static class VersionResolverImpl extends BaseProjectProblemResolver {
        public VersionResolverImpl(MakeProject project) {
            super(project);
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            String title = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_ignore_title"); //NOI18N
            String message = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_ignore"); //NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(message,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                BrokenReferencesSupport.reInitWithUnsupportedVersion(getProject());
                updateProblems();
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
            } else {
                //if (negativeAction != null) {
                //    negativeAction.run();
                //}
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
            }
        }

        @Override
        public int hashCode() {
            return this.getProject().hashCode() + VersionResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VersionResolverImpl other = (VersionResolverImpl) obj;
            return this.getProject().equals(other.getProject());
        }
    }

    private static class ToolCollectionResolverImpl extends BaseProjectProblemResolver {
        private final List<BrokenLinks.BrokenLink> brokenLinks;

        public ToolCollectionResolverImpl(MakeProject project, List<BrokenLinks.BrokenLink> brokenLinks) {
            super(project);
            this.brokenLinks = brokenLinks;
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            ResolveReferencePanel panel = new ResolveReferencePanel(brokenLinks);
            DialogDescriptor dd = new DialogDescriptor(panel,
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Dialog_Title"), true,
                    new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            dialog.dispose();
            updateProblems();
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
        }
        
        @Override
        public int hashCode() {
            return this.getProject().hashCode() + ToolCollectionResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ToolCollectionResolverImpl other = (ToolCollectionResolverImpl) obj;
            return this.getProject().equals(other.getProject());
        }
    }

    public static final class Style {
        private final CodeStyleWrapper aStyle;
        private final String mime;
        
        Style(CodeStyleWrapper aStyle, String mime) {
            this.aStyle = aStyle;
            this.mime = mime;
        }

        @Override
        public int hashCode() {
            return aStyle.getStyleId().hashCode() + mime.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Style other = (Style) obj;
            return aStyle.getStyleId().equals(other.aStyle.getStyleId()) && mime.equals(other.mime);
        }
    }
    
    private static class StyleResolverImpl extends BaseProjectProblemResolver {
        private final Style style;

        private StyleResolverImpl(MakeProject project, Style style) {
            super(project);
            this.style = style;
        }
        
        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            FormattingPropPanel.createStyle(style.aStyle, style.mime);
            updateProblems();
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
        }
        
        @Override
        public int hashCode() {
            return style.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StyleResolverImpl other = (StyleResolverImpl) obj;
            return style.equals(other.style);
        }
    }

    private static class ClangStyleResolverImpl extends BaseProjectProblemResolver {
        private final FileObject style;

        private ClangStyleResolverImpl(MakeProject project, FileObject style) {
            super(project);
            this.style = style;
        }
        
        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            MakeCustomizerProvider cp = getProject().getLookup().lookup(MakeCustomizerProvider.class);
            if (cp != null) {
                cp.showCustomizer("Formatting"); // NOI18N
            }
            NbPreferences.forModule(ClangStyleResolverImpl.class).putBoolean("clang-format-fixed-"+style.getPath(), true); // NOI18N
            updateProblems();
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
        }
        
        @Override
        public int hashCode() {
            return style.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClangStyleResolverImpl other = (ClangStyleResolverImpl) obj;
            return style.equals(other.style);
        }
    }
}
