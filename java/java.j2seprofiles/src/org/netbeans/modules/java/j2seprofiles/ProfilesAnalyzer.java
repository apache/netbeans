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
package org.netbeans.modules.java.j2seprofiles;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.ProfileSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public class ProfilesAnalyzer implements Analyzer {

    private static final String ICON = "org/netbeans/modules/java/j2seprofiles/resources/profile.png"; //NOI18N

    private final Context context;
    private final Result result;
    private final AtomicBoolean canceled = new AtomicBoolean();

    private ProfilesAnalyzer(
            @NonNull final Context context,
            @NonNull final Result result) {
        Parameters.notNull("context", context); //NOI18N
        Parameters.notNull("result", result);   //NOI18N
        this.context = context;
        this.result = result;
    }

    @Override
    @NonNull
    @NbBundle.Messages ({
        "MSG_BuildingClasses=Building Classes..."
    })
    public Iterable<? extends ErrorDescription> analyze() {
        context.progress(Bundle.MSG_BuildingClasses());
        try {
            final JavaSource js = JavaSource.create(
            ClasspathInfo.create(
                JavaPlatform.getDefault().getBootstrapLibraries() ,
                ClassPath.EMPTY,
                ClassPath.EMPTY));
            final Future<Void> f = js.runWhenScanFinished(
                    new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController parameter) throws Exception {
                            analyzeImpl();
                        }
                    }, true);
            while (!f.isDone()) {
                try {
                    f.get(2500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    throw new IOException(ex);
                } catch (TimeoutException ex) {
                    if (canceled.get()) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<ErrorDescription>emptySet();
    }

    @NbBundle.Messages ({
        "MSG_AnalyzingRoot=Analyzing root {0}"
    })
    private void  analyzeImpl() {
        final Scope scope = context.getScope();
        final Set<FileObject> roots = new HashSet<>();
        final Set<FileObject> completeRoots = scope.getSourceRoots();
        final Map<FileObject, Filter.Builder> filters = new HashMap<>();
        roots.addAll(completeRoots);
        for (NonRecursiveFolder nrf : scope.getFolders()) {
            final FileObject ownerRoot = findOwnerRoot(nrf.getFolder());
            if (ownerRoot != null && !completeRoots.contains(ownerRoot)) {
                roots.add(ownerRoot);
                Filter.Builder filterForRoot = filters.get(ownerRoot);
                if (filterForRoot == null) {
                    filterForRoot = new Filter.Builder();
                    filters.put(ownerRoot, filterForRoot);
                }
                filterForRoot.addNonRecursiveFolder(nrf);
            }
        }
        for (FileObject f : scope.getFiles()) {
            Collection<FileObject> ownerRoots = asCollection(findOwnerRoot(f));
            if (ownerRoots.isEmpty()) {
                ownerRoots = findOwnedRoots(f);
            }
            for (FileObject ownerRoot : ownerRoots) {
                if (!completeRoots.contains(ownerRoot)) {
                    roots.add(ownerRoot);
                    Filter.Builder filterForRoot = filters.get(ownerRoot);
                    if (filterForRoot == null) {
                        filterForRoot = new Filter.Builder();
                        filters.put(ownerRoot, filterForRoot);
                    }
                    if (f.isFolder()) {
                        filterForRoot.addFolder(f);
                    } else if (f.isData()) {
                        filterForRoot.addFile(f);
                    }
                }
            }
        }
        final ProfileProvider pp = new ProfileProvider(context);
        final HashMap<Pair<URI,SourceLevelQuery.Profile>,Set<Project>> submittedBinaries = new HashMap<>();
        final Set<URI> submittedSources = new HashSet<>();
        final CollectorFactory.ViolationsProvider vp = new CollectorFactory.ViolationsProvider();
        for (FileObject root : roots) {
            if (canceled.get()) {
                break;
            }
            final SourceLevelQuery.Profile profile = pp.findProfile(root);
            if (profile != SourceLevelQuery.Profile.DEFAULT) {
                final ClassPath boot = ClassPath.getClassPath(root, ClassPath.BOOT);
                final ClassPath compile = ClassPath.getClassPath(root, ClassPath.COMPILE);
                if (boot == null || compile == null) {
                    continue;
                }
                final Project owner = FileOwnerQuery.getOwner(root);
                if (owner == null) {
                    continue;
                }
                submittedSources.add(root.toURI());
                final Set<Project> projectRefs = new HashSet<>();
                ProfileSupport.findProfileViolations(
                    profile,
                    cpToRootUrls(boot, profile, null, null, null),
                    cpToRootUrls(compile, profile, owner, submittedBinaries, projectRefs),
                    Collections.singleton(root.toURL()),
                    EnumSet.of(
                        ProfileSupport.Validation.BINARIES_BY_MANIFEST,
                        ProfileSupport.Validation.BINARIES_BY_CLASS_FILES,
                        ProfileSupport.Validation.SOURCES),
                    new CollectorFactory(vp, profile, canceled));
                verifySubProjects(projectRefs, owner, profile, result);
            }
        }
        if (!canceled.get()) {
            context.start(submittedBinaries.size() + submittedSources.size());
            int count = 0;
            while (!submittedBinaries.isEmpty() || !submittedSources.isEmpty()) {
                try {
                    final Pair<Pair<URI,SourceLevelQuery.Profile>,Collection<? extends ProfileSupport.Violation>> violationsPair = vp.poll(2500);
                    if (violationsPair == null) {
                        continue;
                    }
                    if (canceled.get()) {
                        break;
                    }
                    final URI rootURI = violationsPair.first().first();
                    final Set<Project> projects = submittedBinaries.remove(violationsPair.first());
                    final boolean binary = projects != null;
                    if (!binary) {
                        submittedSources.remove(rootURI);
                    }
                    final Collection<? extends ProfileSupport.Violation> violations = violationsPair.second();
                    if (violations.isEmpty()) {
                        continue;
                    }
                    final FileObject root = URLMapper.findFileObject(rootURI.toURL());
                    if (root == null) {
                        continue;
                    }
                    context.progress(Bundle.MSG_AnalyzingRoot(FileUtil.getFileDisplayName(archiveFileOrFolder(root))), count);
                    if (binary) {
                        //Binary roots
                        verifyBinaryRoot(root, violations, projects, result);
                    } else {
                        //Source roots
                        final Filter.Builder filter = filters.get(root);
                        verifySourceRoot(root, filter == null ? null : filter.build(), violations, result);
                    }
                    context.progress(++count);
                } catch (InterruptedException ex) {
                    break;
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            context.finish();
        }
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        return true;
    }

    @NbBundle.Messages ({
        "MSG_ProjectHigherProfile=Project requires profile: {0}",
        "DESC_ProjectHigherProfile=The project {0} located in {1} requires profile: {2}",
    })
    private static void verifySubProjects(
        @NonNull final Collection<? extends Project> projectRefs,
        @NonNull final Project owner,
        @NonNull final SourceLevelQuery.Profile profile,
        @NonNull final Result result) {
        for (Project p : projectRefs) {
            final FileObject pHome = p.getProjectDirectory();
            final SourceLevelQuery.Profile pProfile = SourceLevelQuery.getSourceLevel2(pHome).getProfile();
            if (pProfile.compareTo(profile) > 0) {
                result.reportError(owner, ErrorDescriptionFactory.createErrorDescription(
                        null,
                        Severity.ERROR,
                        Bundle.MSG_ProjectHigherProfile(pProfile.getDisplayName()),
                        Bundle.DESC_ProjectHigherProfile(
                            ProjectUtils.getInformation(p).getDisplayName(),
                            FileUtil.getFileDisplayName(pHome),
                            profile.getDisplayName()),
                        ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                        p.getProjectDirectory(),
                        null));
            }
        }
    }

    @NbBundle.Messages ({
        "MSG_LibraryHigherProfile=Library requires profile: {0}",
        "DESC_LibraryHigherProfile=The Profile attribute in the manifest of the library {0} requires profile: {1}",
        "MSG_LibraryInvalidProfile=Library has invalid profile",
        "DESC_LibraryInvalidProfile=The library Manifest of the library {0} has invalid value of the Profile attribute",
        "MSG_ClassFileHigherProfile={0} requires profile: {1}",
        "DESC_ClassFileHigherProfile=The {0} used in class {1} of library {2} requires profile: {3}"
    })
    private static void verifyBinaryRoot(
            @NonNull final FileObject root,
            @NonNull final Collection<? extends ProfileSupport.Violation> violations,
            @NonNull final Set<Project> projects,
            @NonNull final Result result) {
        for (ProfileSupport.Violation violation : violations) {
            final URL fileURL = violation.getFile();
            FileObject target;
            String message;
            String description;
            final SourceLevelQuery.Profile requiredProfile = violation.getRequiredProfile();
            if (fileURL == null) {
                target = root;
                if (requiredProfile != null) {
                    message = Bundle.MSG_LibraryHigherProfile(requiredProfile.getDisplayName());
                    description = Bundle.DESC_LibraryHigherProfile(
                            FileUtil.getFileDisplayName(archiveFileOrFolder(target)),
                            requiredProfile.getDisplayName());
                } else {
                    message = Bundle.MSG_LibraryInvalidProfile();
                    description = Bundle.DESC_LibraryInvalidProfile(FileUtil.getFileDisplayName(archiveFileOrFolder(target)));
                }
            } else {
                final ElementHandle<TypeElement> usedType = violation.getUsedType();
                assert usedType != null;
                assert requiredProfile != null;
                target = URLMapper.findFileObject(fileURL);
                message = Bundle.MSG_ClassFileHigherProfile(
                        simpleName(usedType),
                        requiredProfile.getDisplayName());
                description = Bundle.DESC_ClassFileHigherProfile(
                        usedType.getQualifiedName(),
                        stripExtension(FileUtil.getRelativePath(root, target)),
                        FileUtil.getFileDisplayName(archiveFileOrFolder(root)),
                        requiredProfile.getDisplayName());
            }
            for (Project p : projects) {
                result.reportError(p, ErrorDescriptionFactory.createErrorDescription(
                    null,
                    Severity.ERROR,
                    message,
                    description,
                    ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                    target,
                    null));
            }
        }
    }

    private static void verifySourceRoot(
            @NonNull final FileObject root,
            @NullAllowed final Filter filter,
            @NonNull final Collection<? extends ProfileSupport.Violation> violations,
            @NonNull final Result result) {
        try {
            final ClasspathInfo cpInfo = ClasspathInfo.create(root);
            final Map<FileObject,Collection<ProfileSupport.Violation>> violationsByFiles =
                new HashMap<>();
            final JavaSource js = JavaSource.create(
                cpInfo,
                violationsToFileObjects(violations, filter, violationsByFiles));
            if (js != null) {
                js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(@NonNull final CompilationController cc) throws Exception {
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            final FileObject currentFile = cc.getFileObject();
                            if (currentFile != null) {
                                final FindPosScanner fps = new FindPosScanner(
                                        currentFile,
                                        cc.getTrees(),
                                        cc.getElements(),
                                        cc.getTreeUtilities(),
                                        violationsByFiles.get(currentFile),
                                        result);
                                fps.scan(cc.getCompilationUnit(), null);
                            }
                        }
                    },
                    true);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @NonNull
    private Collection<FileObject> asCollection(@NullAllowed final FileObject file) {
        return file == null ?
            Collections.<FileObject>emptySet() :
            Collections.singleton(file);
    }


    @CheckForNull
    private static FileObject findOwnerRoot(@NonNull final FileObject file) {
        final ClassPath sourcePath = ClassPath.getClassPath(file, ClassPath.SOURCE);
        return sourcePath == null ? null : sourcePath.findOwnerRoot(file);
    }

    @NonNull
    private static Collection<FileObject> findOwnedRoots(@NonNull final FileObject file) {
        final Project p = FileOwnerQuery.getOwner(file);
        if (p == null || !file.equals(p.getProjectDirectory())) {
            return Collections.emptySet();
        }
        final Sources sources = ProjectUtils.getSources(p);
        final Collection<FileObject> res = new ArrayList<>();
        for (SourceGroup grp : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            res.add(grp.getRootFolder());
        }
        return res;
    }

    @NonNull
    private static FileObject archiveFileOrFolder(@NonNull final FileObject root) {
        final FileObject archiveFile = FileUtil.getArchiveFile(root);
        return archiveFile != null ? archiveFile : root;
    }

    @NonNull
    private static String stripExtension(@NonNull final String path) {
        final int index = path.lastIndexOf('.');    //NOI18N
        return index <= 0 ? path : path.substring(0, index);
    }

    @NonNull
    private static String simpleName(@NullAllowed final ElementHandle<TypeElement> eh) {
        if (eh == null) {
            return "";  //NOI18N
        }
        final String qn = eh.getQualifiedName();
        int index = qn.lastIndexOf('.');    //NOI18N
        return index < 0 ? qn : qn.substring(index+1);
    }

    @NonNull
    private static Iterable<URL> cpToRootUrls(
            @NonNull final ClassPath cp,
            @NonNull final SourceLevelQuery.Profile requiredProfile,
            @NullAllowed final Project owner,
            @NullAllowed final Map<Pair<URI,SourceLevelQuery.Profile>,Set<Project>> alreadyProcessed,
            @NullAllowed final Set<? super Project> projectRefs) {
        assert (owner == null && alreadyProcessed == null && projectRefs == null) ||
               (owner != null && alreadyProcessed != null && projectRefs != null);
        final Queue<URL> res = new ArrayDeque<>();
nextCpE:for (ClassPath.Entry e : cp.entries()) {
            final URL url = e.getURL();
            try {
                if (projectRefs != null) {
                    final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(url);
                    if (sfbqRes.preferSources()) {
                        for (FileObject src : sfbqRes.getRoots()) {
                            final Project prj = FileOwnerQuery.getOwner(src);
                            if (prj != null) {
                                for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                                    if (src.equals(sg.getRootFolder())) {
                                        if (!prj.equals(owner)) {
                                            projectRefs.add(prj);
                                        }
                                        continue nextCpE;
                                    }
                                }
                            }
                        }
                    }
                }

                if (alreadyProcessed == null) {
                    res.offer(url);
                } else {
                    final URI uri = url.toURI();
                    final Pair<URI,SourceLevelQuery.Profile> key = Pair.of(uri,requiredProfile);
                    Set<Project> projects = alreadyProcessed.get(key);
                    if (projects == null) {
                        projects = new HashSet<>();
                        alreadyProcessed.put(key, projects);
                        res.offer(url);
                    }
                    projects.add(owner);
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return res;
    }

    private static FileObject[] violationsToFileObjects(
            @NonNull final Collection<? extends ProfileSupport.Violation> violations,
            @NullAllowed final Filter filter,
            @NullAllowed final Map<FileObject,Collection<ProfileSupport.Violation>> violationsByFiles) {
        final Collection<FileObject> fos = new HashSet<>(violations.size());
        for (ProfileSupport.Violation v : violations) {
            final URL fileURL = v.getFile();
            if (fileURL != null) {
                final FileObject fo = URLMapper.findFileObject(fileURL);
                if (shouldProcessViolationsInSource(fo, filter)) {
                    fos.add(fo);
                    if (violationsByFiles != null) {
                        Collection<ProfileSupport.Violation> violationsInFile = violationsByFiles.get(fo);
                        if (violationsInFile == null) {
                            violationsInFile = new ArrayList<>();
                            violationsByFiles.put(fo, violationsInFile);
                        }
                        violationsInFile.add(v);
                    }
                }
            }
        }
        return fos.toArray(new FileObject[0]);
    }

    private static boolean shouldProcessViolationsInSource(
            @NullAllowed final FileObject source,
            @NullAllowed final Filter filter) {
        return filter == null ?
                true :
                filter.accept(source);
    }

    private static final class ProfileProvider {

        private final SourceLevelQuery.Profile profile;

        ProfileProvider (@NonNull Context ctx) {
            final Preferences prefs = ctx.getSettings();
            final String profileName = prefs == null ?
                    null :
                    prefs.get(ProfilesCustomizerProvider.PROP_PROFILE_TO_CHECK, null);
            profile = profileName == null ?
                    null :
                    SourceLevelQuery.Profile.forName(profileName);
        }

        @NonNull
        public SourceLevelQuery.Profile findProfile (@NonNull final FileObject root) {
            return profile != null ?
                    profile :
                    SourceLevelQuery.getSourceLevel2(root).getProfile();
        }

    }

    //@ThreadSafe
    private static final class CollectorFactory implements ProfileSupport.ViolationCollectorFactory {

        private final ViolationsProvider provider;
        private final SourceLevelQuery.Profile profile;
        private final AtomicBoolean canceled;


        CollectorFactory(
                @NonNull final ViolationsProvider provider,
                @NonNull final SourceLevelQuery.Profile profile,
                @NonNull final AtomicBoolean canceled) {
            assert provider != null;
            assert profile != null;
            assert canceled != null;
            this.provider = provider;
            this.profile = profile;
            this.canceled = canceled;
        }

        @Override
        public ProfileSupport.ViolationCollector create(@NonNull final URL root) {
            try {
                return new Collector(root.toURI());
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public boolean isCancelled() {
            return canceled.get();
        }

        private void addViolations(
                @NonNull final URI root,
                @NonNull final Collection<? extends ProfileSupport.Violation> violations) {
            provider.allViolations.offer(Pair.<Pair<URI,SourceLevelQuery.Profile>,Collection<? extends ProfileSupport.Violation>>of(
                    Pair.of(root,profile),violations));
        }

        //@ThreadSafe
        static final  class ViolationsProvider {

            private final BlockingQueue<Pair<Pair<URI,SourceLevelQuery.Profile>,Collection<? extends ProfileSupport.Violation>>>
                    allViolations = new LinkedBlockingQueue<>();

            @CheckForNull
            Pair<Pair<URI,SourceLevelQuery.Profile>,Collection<? extends ProfileSupport.Violation>> poll(long timeOut) throws InterruptedException {
                return allViolations.poll(timeOut, TimeUnit.MILLISECONDS);
            }
        }

        private final class Collector implements ProfileSupport.ViolationCollector {

            private final URI root;

            Collector(@NonNull final URI root) {
                Parameters.notNull("root", root);   //NOI18N
                this.root = root;
            }

            private final Queue<ProfileSupport.Violation> violations = new ArrayDeque<>();

            @Override
            public void reportProfileViolation(@NonNull final ProfileSupport.Violation violation) {
                violations.offer(violation);
            }

            @Override
            public void finished() {
                addViolations(root, violations);
            }
        }
    }

    //@NonThreadSafe
    private static final class FindPosScanner extends ErrorAwareTreePathScanner<Void, Void> {

        private final FileObject target;
        private final Elements elements;
        private final TreeUtilities treeUtilities;
        private final Trees trees;
        private final Result errors;
        private final Map<String,ProfileSupport.Violation> violationsByBinNames =
                new HashMap<>();

        FindPosScanner(
                @NonNull final FileObject target,
                @NonNull final Trees trees,
                @NonNull final Elements elements,
                @NonNull final TreeUtilities treeUtilities,
                @NonNull final Collection<? extends ProfileSupport.Violation> violations,
                @NonNull final Result errors) {
            assert target != null;
            assert trees != null;
            assert elements != null;
            assert treeUtilities != null;
            assert violations != null;
            assert errors != null;
            this.target = target;
            this.trees = trees;
            this.elements = elements;
            this.treeUtilities = treeUtilities;
            this.errors = errors;
            for (ProfileSupport.Violation v : violations) {
                final ElementHandle<TypeElement> eh = v.getUsedType();
                if (eh != null) {
                    violationsByBinNames.put(eh.getBinaryName(), v);
                }
            }
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            handleIdentSelect();
            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            handleIdentSelect();
            return super.visitMemberSelect(node, p);
        }

        @NbBundle.Messages({
            "MSG_SourceFileHigherProfile={0} requires profile: {1}",
            "DESC_SourceFileHigherProfile=The {0} requires profile: {1}"
        })
        private void handleIdentSelect() {
            final TreePath tp = getCurrentPath();
            Element e = trees.getElement(tp);
            if (e != null) {
                final ElementKind ek = e.getKind();
                if (ek == ElementKind.OTHER ||
                    ek.isField() ||
                    ek == ElementKind.CONSTRUCTOR ||
                    ek == ElementKind.METHOD) {
                        e = e.getEnclosingElement();
                }
                if ((e.getKind().isClass() || e.getKind().isInterface()) && !treeUtilities.isSynthetic(tp)) {
                    final Name binName = elements.getBinaryName((TypeElement)e);
                    final ProfileSupport.Violation v = violationsByBinNames.get(binName.toString());
                    if (v != null) {
                        final SourcePositions sp = trees.getSourcePositions();
                        final int start = (int) sp.getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                        final int end = (int) sp.getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
                        final SourceLevelQuery.Profile requiredProfile = v.getRequiredProfile();
                        assert requiredProfile != null;
                        errors.reportError(ErrorDescriptionFactory.createErrorDescription(
                            null,
                            Severity.ERROR,
                            Bundle.MSG_SourceFileHigherProfile(e.getSimpleName(), requiredProfile.getDisplayName()),
                            Bundle.DESC_SourceFileHigherProfile(((TypeElement)e).getQualifiedName(), requiredProfile.getDisplayName()),
                            ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                            target,
                            start,
                            end));
                    }
                }
            }
        }
    }

    @NbBundle.Messages({
        "NAME_JdkProfiles=JRE 8 Profiles Conformance"
    })
    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class Factory extends Analyzer.AnalyzerFactory {

        public Factory() {
            super("jdk-profiles", Bundle.NAME_JdkProfiles(), ICON);
        }

        @Override
        public Analyzer createAnalyzer(
                @NonNull final Context context,
                @NonNull final Result result) {
            return new ProfilesAnalyzer(context, result);
        }

        @Override
        public Analyzer createAnalyzer(@NonNull final Context context) {
            throw new IllegalStateException();
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptySet();
        }

        @CheckForNull
        @Override
        public CustomizerProvider<Void, ProfilesCustomizer> getCustomizerProvider() {
            return new ProfilesCustomizerProvider();
        }
    }

    private static interface Filter {

        public boolean accept(@NullAllowed FileObject fo);

        static final class Builder {

            private final Set<FileObject> folders;
            private final Set<FileObject> nonRecursiveFolders;
            private final Set<FileObject> files;

            Builder() {
                this.folders = new HashSet<>();
                this.nonRecursiveFolders = new HashSet<>();
                this.files = new HashSet<>();
            }

            @NonNull
            Builder addFolder(@NonNull final FileObject folder) {
                assert folder.isFolder();
                this.folders.add(folder);
                return this;
            }

            @NonNull
            Builder addNonRecursiveFolder(@NonNull final NonRecursiveFolder nonRecursiveFolder) {
                final FileObject folder = nonRecursiveFolder.getFolder();
                assert folder.isFolder();
                this.nonRecursiveFolders.add(folder);
                return this;
            }

            @NonNull
            Builder addFile(@NonNull final FileObject file) {
                assert file.isData();
                this.files.add(file);
                return this;
            }

            @NonNull
            public Filter build() {
                return new Filter() {
                    @Override
                    public boolean accept(@NullAllowed final FileObject fo) {
                        if (fo == null) {
                            return false;
                        }
                        if (files.contains(fo)) {
                            return true;
                        }
                        for (FileObject folder : nonRecursiveFolders) {
                            if (folder.equals(fo.getParent())) {
                                return true;
                            }
                        }
                        for (FileObject folder : folders) {
                            if (FileUtil.isParentOf(folder, fo)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        }
    }
}
