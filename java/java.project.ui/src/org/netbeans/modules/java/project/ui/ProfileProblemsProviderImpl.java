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
package org.netbeans.modules.java.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.SourceLevelQuery.Profile;
import org.netbeans.api.java.source.support.ProfileSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.MutexException;

import static org.netbeans.modules.java.project.ui.Bundle.*;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
/**
 *
 * @author Tomas Zezula
 */
final class ProfileProblemsProviderImpl implements ProjectProblemsProvider, PropertyChangeListener, ChangeListener, Runnable {

    private static final String LIB_PREFIX = "${libs."; // NOI18N
    private static final String PRJ_PREFIX = "${reference."; // NOI18N
    private static final String FILE_PREFIX = "${file.reference."; //NOI18N
    private static final String REF_PREFIX = "${"; //NOI18N
    private static final String VOL_CLASSPATH = "classpath";    //NOI18N
    private static final char PATH_SEPARATOR_CHAR = ':';                          //NOI18N
    private static final String ICON_LIBRARIES = "org/netbeans/modules/java/project/ui/resources/libraries.gif"; //NOI18N
    private static final String ICON_FILE = "org/netbeans/modules/java/project/ui/resources/jar.gif";//NOI18N
    private static final int SLIDING_DELAY = 1000;  //1s
    private static final Logger LOG = Logger.getLogger(ProfileProblemsProviderImpl.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ProjectProblemsProviders.class);

    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper referenceHelper;
    private final PropertyEvaluator evaluator;
    private final String profileProperty;
    private final Set<String> classPathProperties;
    private final ProjectProblemsProviderSupport problemsProviderSupport;
    //@GuardedBy("foreignSlResults")
    private final Collection<SourceLevelQuery.Result> foreignSlResults;
    private final RequestProcessor.Task firer;
    private final Object listenersInitLock = new Object();
    //@GuardedBy("listenersInitLock")
    private ClassPath classPath;
    //@GuardedBy("listenersInitLock")
    private SourceLevelQuery.Result slRes;

    ProfileProblemsProviderImpl(
            @NonNull final AntProjectHelper antProjectHelper,
            @NonNull final ReferenceHelper referenceHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String profileProperty,
            @NonNull final String... classPathProperties) {
        assert antProjectHelper != null;
        assert referenceHelper != null;
        assert evaluator != null;
        assert profileProperty != null;
        assert classPathProperties != null;
        this.antProjectHelper = antProjectHelper;
        this.referenceHelper = referenceHelper;
        this.evaluator = evaluator;
        this.profileProperty = profileProperty;
        this.classPathProperties = new HashSet<String>(
                Arrays.asList(classPathProperties));
        this.problemsProviderSupport = new ProjectProblemsProviderSupport(this);
        this.foreignSlResults = Collections.synchronizedCollection(new ArrayList<SourceLevelQuery.Result>());
        this.firer = RP.create(this);
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
    @NbBundle.Messages({
        "LBL_InvalidProfile=Invalid JRE 8 Profile",
        "DESC_InvalidProfile=The project profile ({0}) is lower than the profile of used libraries ({1}).",
        "DESC_IllegalProfile=The project libraries have illegal value of profile."
    })
    public Collection<? extends ProjectProblem> getProblems() {        
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblem> collectProblems() {
                return ProjectManager.mutex().readAccess(new Mutex.Action<Collection<? extends ProjectProblem>>() {
                    @Override
                    public Collection<? extends ProjectProblem> run() {
                        final SourceLevelQuery.Result mySL = listenenOnProjectMetadata();
                        final Profile profile = mySL.getProfile();
                        if (profile == Profile.DEFAULT) {
                            return Collections.<ProjectProblem>emptySet();
                        }
                        final Set<Reference> problems = collectReferencesWithWrongProfile(profile);
                        if (problems.isEmpty()) {
                            return Collections.<ProjectProblem>emptySet();
                        }
                        Profile minProfile = null;
                        for (Reference problem : problems) {
                            final Profile problemProfile = problem.getRequiredProfile();
                            minProfile = max(minProfile, problemProfile);
                        }
                        return Collections.<ProjectProblem>singleton(
                                ProjectProblem.createError(
                                LBL_InvalidProfile(),
                                minProfile != null
                                ? DESC_InvalidProfile(profile.getDisplayName(), minProfile.getDisplayName())
                                : DESC_IllegalProfile(),
                                new ProfileResolver(
                                    antProjectHelper,
                                    profileProperty,
                                    profile,
                                    problems)));
                    }
                });
            }
        });
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent event) {
        final String propName = event.getPropertyName();
        if (ClassPath.PROP_ROOTS.equals(propName) ||
            JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName)) {
            firer.schedule(SLIDING_DELAY);
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {        
        firer.schedule(SLIDING_DELAY);
    }

    @Override
    public void run() {        
        problemsProviderSupport.fireProblemsChange();
    }

    private SourceLevelQuery.Result listenenOnProjectMetadata() {
        synchronized (listenersInitLock) {
            if (slRes == null) {
                assert classPath == null;
                final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
                jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
                slRes = SourceLevelQuery.getSourceLevel2(antProjectHelper.getProjectDirectory());
                slRes.addChangeListener(this);
                final File baseFolder = FileUtil.toFile(antProjectHelper.getProjectDirectory());
                if (baseFolder != null) {
                    final ClassPath cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(baseFolder,
                        evaluator,
                        classPathProperties.toArray(new String[0])));
                    cp.addPropertyChangeListener(this);
                    cp.getRoots();
                    classPath = cp;
                }
            }
            return slRes;
        }
    }

    @NonNull
    private Set<Reference> collectReferencesWithWrongProfile(@NonNull final Profile currentProfile) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        final Set<Reference> res = new HashSet<Reference>();
        final EditableProperties projectProps = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties privateProps = antProjectHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final EditableProperties globalProps = PropertyUtils.getGlobalProperties();
        final Collection<SourceLevelQuery.Result> newSlResults = new ArrayDeque<SourceLevelQuery.Result>();
        for (String cpId : classPathProperties) {
            collectReferencesWithWrongProfileImpl(
                    cpId,
                    currentProfile,
                    antProjectHelper,
                    evaluator,
                    referenceHelper,
                    projectProps,
                    privateProps,
                    globalProps,
                    res,
                    newSlResults);
        }
        synchronized (foreignSlResults) {
            for (final Iterator<? extends SourceLevelQuery.Result> it = foreignSlResults.iterator(); it.hasNext();) {
                final SourceLevelQuery.Result cslr = it.next();
                it.remove();
                cslr.removeChangeListener(this);
            }
            assert  foreignSlResults.isEmpty();
            for (SourceLevelQuery.Result cslr : newSlResults) {
                cslr.addChangeListener(this);
                foreignSlResults.add(cslr);
            }
        }
        return res;
    }

    private static void collectReferencesWithWrongProfileImpl(
            @NonNull final String classPathId,
            @NonNull final Profile requiredProfile,
            @NonNull final AntProjectHelper antProjectHelper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final ReferenceHelper refHelper,
            @NonNull final EditableProperties projectProps,
            @NonNull final EditableProperties privateProps,
            @NonNull final EditableProperties globalProps,
            @NonNull final Set<? super Reference> collector,
            @NonNull final Collection<? super SourceLevelQuery.Result> slResCollector) {
        String cp = projectProps.getProperty(classPathId);
        if (cp == null) {
            cp = privateProps.getProperty(classPathId);
        }
        if (cp == null) {
            cp = globalProps.getProperty(classPathId);
        }
        if (cp != null) {
            final Queue<String> todo = new ArrayDeque<String>(Arrays.asList(PropertyUtils.tokenizePath(cp)));
            while (!todo.isEmpty()) {
                final String rawEntry = todo.remove();
                final String propName = getAntPropertyName(rawEntry);
                if (propName == null) {
                    continue;
                }
                if (rawEntry.startsWith(LIB_PREFIX)) {
                    final String libName = rawEntry.substring(LIB_PREFIX.length(),rawEntry.lastIndexOf('.'));
                    final Library lib = refHelper.findLibrary(libName);
                    if (lib != null) {
                        final Collection<? extends ProfileSupport.Violation> res =
                            ProfileSupport.findProfileViolations(
                                requiredProfile,
                                Collections.<URL>emptySet(),
                                lib.getContent(VOL_CLASSPATH),
                                Collections.<URL>emptySet(),
                                EnumSet.of(ProfileSupport.Validation.BINARIES_BY_MANIFEST));
                            if (!res.isEmpty()) {
                                final Profile maxProfile = findMaxProfile(res);
                                collector.add(new LibraryReference(classPathId, rawEntry, maxProfile, lib));
                            }
                    }
                } else if (rawEntry.startsWith(PRJ_PREFIX)) {
                    final Object[] ref = refHelper.findArtifactAndLocation(rawEntry);
                    if (ref[0] != null) {
                        AntArtifact artifact = (AntArtifact)ref[0];
                        final SourceLevelQuery.Result slRes = SourceLevelQuery.getSourceLevel2(artifact.getProject().getProjectDirectory());
                        slResCollector.add(slRes);
                        final Profile minProfile = slRes.getProfile();
                        if (isBroken(requiredProfile, minProfile)) {
                            collector.add(new ProjectReference(classPathId, rawEntry, minProfile, artifact.getProject()));
                        }
                    }

                } else if (rawEntry.startsWith(FILE_PREFIX)) {
                    final String path = eval.getProperty(propName);
                    if (path != null) {
                        final File file = antProjectHelper.resolveFile(path);
                        final URL root = FileUtil.urlForArchiveOrDir(file);
                        if (root != null) {
                            final Collection<? extends ProfileSupport.Violation> res =
                                ProfileSupport.findProfileViolations(
                                    requiredProfile,
                                    Collections.<URL>emptySet(),
                                    Collections.singleton(root),
                                    Collections.<URL>emptySet(),
                                    EnumSet.of(ProfileSupport.Validation.BINARIES_BY_MANIFEST));
                            if (!res.isEmpty()) {
                                final Profile maxProfile = findMaxProfile(res);
                                collector.add(new FileReference(classPathId, rawEntry, maxProfile, file));
                            }
                        }
                    }
                } else if (rawEntry.startsWith(REF_PREFIX)) {
                    collectReferencesWithWrongProfileImpl(
                            propName,
                            requiredProfile,
                            antProjectHelper,
                            eval,
                            refHelper,
                            projectProps,
                            privateProps,
                            globalProps,
                            collector,
                            slResCollector);
                } else {
                    final File file = antProjectHelper.resolveFile(propName);
                    final URL root = FileUtil.urlForArchiveOrDir(file);
                    if (root != null) {
                        final Collection<? extends ProfileSupport.Violation> res =
                                ProfileSupport.findProfileViolations(
                                    requiredProfile,
                                    Collections.<URL>emptySet(),
                                    Collections.singleton(root),
                                    Collections.<URL>emptySet(),
                                    EnumSet.of(ProfileSupport.Validation.BINARIES_BY_MANIFEST));
                        if (!res.isEmpty()) {
                            final Profile maxProfile = findMaxProfile(res);
                            collector.add(new FileReference(classPathId, rawEntry, maxProfile, file));
                        }
                    }
                }
            }
        }
    }

    private static boolean isBroken(
            @NonNull final Profile requiredProfile,
            @NullAllowed final Profile profile) {
        if (profile == null) {
            return true;
        }
        final Profile max = max(requiredProfile, profile);
        return !max.equals(Profile.DEFAULT) &&
               !max.equals(requiredProfile);
    }

    @CheckForNull
    private static String getAntPropertyName(@NullAllowed String reference) {
        if (reference != null
                && reference.startsWith("${") // NOI18N
                && reference.endsWith("}")) { // NOI18N
            reference =  reference.substring(2, reference.length() - 1);
        }
        return reference;
    }

    @CheckForNull
    private static Profile findMaxProfile(@NonNull final Iterable<? extends ProfileSupport.Violation> violations) {
        Profile current = null;
        for (ProfileSupport.Violation violation : violations) {
            Profile p = violation.getRequiredProfile();
            if (p == null) {
                //Broken profile - no need to continue
                return null;
            }
            current = max(current, p);
        }
        return current;
    }
    
    @CheckForNull
    static Profile max (@NullAllowed Profile a, @NullAllowed Profile b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }
        return a.compareTo(b) <= 0 ?
            b :
            a;
    }

    @NonNull
    static Profile requiredProfile(
            @NonNull final Collection<? extends Reference> state,
            @NonNull Profile initial) {
        Profile current = initial;
        for (ProfileProblemsProviderImpl.Reference re : state) {
            current = max(current, re.getRequiredProfile());
        }
        return current;
    }
    
    abstract static class Reference {

        private final String classPathId;
        private final String rawId;
        private final Profile requiredProfile;

        private Reference(
                @NonNull final String classPathId,
                @NonNull final String rawId,
                @NullAllowed final Profile requiredProfile) {
            Parameters.notNull("classPathId", classPathId); //NOI18N
            Parameters.notNull("rawId", rawId);             //NOI18N
            this.classPathId = classPathId;
            this.rawId = rawId;
            this.requiredProfile = requiredProfile;
        }

        @NonNull
        abstract String getDisplayName();

        @NonNull
        abstract String getToolTipText();

        @NonNull
        abstract Icon getIcon();        

        @CheckForNull
        final Profile getRequiredProfile() {
            return requiredProfile;
        }

        private void remove(@NonNull final AntProjectHelper helper) {
            final EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String rawPath = props.getProperty (classPathId);
            if (rawPath != null) {
                final String[] pathElements = PropertyUtils.tokenizePath(rawPath);
                final List<String> result = new ArrayList<String>(pathElements.length);
                boolean changed = false;
                for (String pathElement : pathElements) {
                    if (rawId.equals(pathElement)) {
                        changed = true;
                        continue;
                    }
                    result.add(pathElement + PATH_SEPARATOR_CHAR);
                }
                if (!result.isEmpty()) {
                    final String last = result.get(result.size()-1);
                    result.set(result.size()-1, last.substring(0, last.length()-1));
                }
                if (changed) {
                    props.setProperty(classPathId, result.toArray(new String[0]));
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 31 * hash + this.classPathId.hashCode();
            hash = 31 * hash + this.rawId.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
               return true;
            }
            if (!(other instanceof Reference)) {
                return false;
            }
            final Reference otherRef = (Reference) other;
            return classPathId.equals(otherRef.classPathId) &&
                rawId.equals(otherRef.rawId);
        }


    }

    private static final class LibraryReference extends Reference {

        private final Library lib;

        private LibraryReference(
                @NonNull final String classPathId,
                @NonNull final String rawId,
                @NullAllowed final Profile requiredProfile,
                @NonNull final Library lib) {
            super(classPathId, rawId, requiredProfile);
            Parameters.notNull("lib", lib); //NOI18N
            this.lib = lib;
        }

        @Override
        String getDisplayName() {
            return this.lib.getDisplayName();
        }

        @Override
        String getToolTipText() {
            return lib.getDisplayName();
        }

        @Override
        Icon getIcon() {
            return ImageUtilities.loadImageIcon(ICON_LIBRARIES, false);
        }       
    }

    private static final class ProjectReference extends Reference {
        private final Project prj;

        private ProjectReference(
                @NonNull final String classPathId,
                @NonNull final String rawId,
                @NullAllowed final Profile requiredProfile,
                @NonNull final Project prj) {
            super(classPathId, rawId, requiredProfile);
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        String getDisplayName() {
            return ProjectUtils.getInformation(prj).getDisplayName();
        }

        @Override
        String getToolTipText() {
            return FileUtil.getFileDisplayName(prj.getProjectDirectory());
        }

        @Override
        Icon getIcon() {
            return ProjectUtils.getInformation(prj).getIcon();
        }
    }

    private static final class FileReference extends Reference {

        private final File file;

        private FileReference(
                @NonNull final String classPathId,
                @NonNull final String rawId,
                @NullAllowed final Profile requiredProfile,
                @NonNull final File file) {
            super(classPathId, rawId, requiredProfile);
            Parameters.notNull("file", file);   //NOI18N
            this.file = file;
        }

        @Override
        String getDisplayName() {
            return file.getName();
        }

        @Override
        String getToolTipText() {
            return file.getAbsolutePath();
        }

        @Override
        Icon getIcon() {
            return ImageUtilities.loadImageIcon(ICON_FILE, false);
        }        
    }

    private static final class ProfileResolver implements ProjectProblemResolver {

        private final AntProjectHelper antProjectHelper;
        private final String profileProperty;
        private final Profile currentProfile;
        private final Collection<? extends Reference> state;

        private ProfileResolver(
                @NonNull final AntProjectHelper antProjectHelper,
                @NonNull final String profileProperty,
                @NonNull final Profile currentProfile,
                @NonNull final Collection<? extends Reference> state) {
            assert antProjectHelper != null;
            assert profileProperty != null;
            assert currentProfile != null;
            assert state != null;
            this.antProjectHelper = antProjectHelper;
            this.profileProperty = profileProperty;
            this.currentProfile = currentProfile;
            this.state = state;
        }

        @Override
        @NbBundle.Messages({
            "LBL_ResolveProfile=Resolve Invalid Project Profile",
            "LBL_ResolveButton=OK",
            "AN_ResolveButton=Resolve",
            "AD_ResolveButton=Resolve the profile problems"
        })
        public Future<Result> resolve() {
            final JButton ok = new JButton(LBL_ResolveButton());
            ok.getAccessibleContext().setAccessibleName(AN_ResolveButton());
            ok.getAccessibleContext().setAccessibleDescription(AD_ResolveButton());
            final FixProfile panel = new FixProfile(ok, currentProfile, state);
            final DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    LBL_ResolveProfile(),
                    true,
                    new Object[]{
                ok,
                DialogDescriptor.CANCEL_OPTION
            },
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            if (DialogDisplayer.getDefault().notify(dd) == ok) {
                return RP.submit(new Callable<ProjectProblemsProvider.Result>() {
                    @Override
                    public ProjectProblemsProvider.Result call() throws Exception {
                        ProjectProblemsProvider.Status status = ProjectProblemsProvider.Status.UNRESOLVED;
                        try {
                            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                                @Override
                                public Void run() throws IOException {
                                    final boolean shouldUpdate = panel.shouldUpdateProfile();
                                    if (shouldUpdate) {
                                        final Profile newProfile = panel.getProfile();
                                        final EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                        if (newProfile == null ||
                                            newProfile == Profile.DEFAULT) {
                                            props.remove(profileProperty);
                                        } else {
                                            props.put(profileProperty, newProfile.getName());
                                        }
                                        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                    }
                                    for (Reference rootToRemove : panel.getRootsToRemove()) {
                                        rootToRemove.remove(antProjectHelper);
                                    }
                                    ProjectManager.getDefault().saveProject(
                                            FileOwnerQuery.getOwner(antProjectHelper.getProjectDirectory()));
                                    return null;
                                }
                            });
                            status = ProjectProblemsProvider.Status.RESOLVED;
                        } catch (MutexException e) {
                            Exceptions.printStackTrace(e);
                        }
                        return ProjectProblemsProvider.Result.create(status);
                    }
                });
            }
            final RunnableFuture<Result> res = new FutureTask<Result>(
                    new Callable<Result>() {
                        @Override
                        public Result call() {
                            return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED);
                        }
                    });
            res.run();
            return res;
        }

        @Override
        public int hashCode() {
            int res = 17;
            final FileObject projDir = antProjectHelper.getProjectDirectory();
            res = res * 31 + (projDir == null ? 0 : projDir.toURI().hashCode());
            res = res * 31 + currentProfile.hashCode();
            return res;
        }

        @Override
        public boolean equals(@NullAllowed Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof ProfileResolver)) {
                return false;
            }
            final ProfileResolver otherResolver = (ProfileResolver) other;
            final FileObject projDir = antProjectHelper.getProjectDirectory();
            final FileObject otherProjDir = otherResolver.antProjectHelper.getProjectDirectory();
            return
                currentProfile.equals(otherResolver.currentProfile) &&
                (projDir == null ? otherProjDir == null : projDir.equals(otherProjDir));
        }
    }    
}