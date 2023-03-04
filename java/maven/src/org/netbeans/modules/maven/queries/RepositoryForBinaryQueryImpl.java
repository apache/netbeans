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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * SourceForBinaryQueryImplementation implementation
 * for items in the maven2 repository. It checks the artifact and
 * looks for the same artifact but of type "sources.jar".
 *
 * @author  Milos Kleint
 */
@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class, position = 68),
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class, position = 68),
    @ServiceProvider(service = JavadocForBinaryQueryImplementation.class, position = 68)
})
public class RepositoryForBinaryQueryImpl extends AbstractMavenForBinaryQueryImpl {
    
    private final Map<URL, WeakReference<SrcResult>> srcCache = Collections.synchronizedMap(new HashMap<URL, WeakReference<SrcResult>>());
    private final Map<URL, WeakReference<JavadocResult>> javadocCache = Collections.synchronizedMap(new HashMap<URL, WeakReference<JavadocResult>>());
    private final Map<File, List<Coordinates>> coorCache = Collections.synchronizedMap(new HashMap<>());

    //http://maven.apache.org/guides/mini/guide-attached-tests.html
    //issue 219453
    private static final String CLASSIFIER_TESTS = "tests";

    private static final RequestProcessor RP = new RequestProcessor("Maven Repository SFBQ result change");
    private static final Logger LOG = Logger.getLogger(SrcResult.class.getName());

    private final FileChangeAdapter binaryChangeListener = new FileChangeAdapter() {
        @Override
        public void fileDataCreated(FileEvent fe) {                                        
            removeCoordinates(fe);                    
        }
        @Override
        public void fileChanged(FileEvent fe) {
            removeCoordinates(fe);
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            removeCoordinates(fe);
        }
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            removeCoordinates(fe);
        }
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            removeCoordinates(fe);
        }
        private void removeCoordinates(FileEvent fe) {
            File file = FileUtil.toFile(fe.getFile());
            if(file != null) {
                synchronized(coorCache) {
                    if(coorCache.remove(file) != null) {
                        FileUtil.removeFileChangeListener(binaryChangeListener, file);
                    }
                }
            }
        }
    };
    
    @Override
    public synchronized Result findSourceRoots2(URL url) {
        if (!"jar".equals(url.getProtocol())) { //NOI18N
            // null for directories.
            return null;
         }

        WeakReference<SrcResult> cached = srcCache.get(url);
        if (cached != null) {
            SrcResult result = cached.get();
            if (result != null) {
                return result;
            }
        }

        //#223841 at least one project opened is a stronger condition, embedder gets sometimes reset.
        //once we have the project loaded, not loaded embedder doesn't matter anymore, we have to process.
        // sometimes the embedder is loaded even though a maven project is not yet loaded, it doesn't hurt to proceed then.
        if (!NbMavenProjectFactory.isAtLeastOneMavenProjectAround() && !EmbedderFactory.isProjectEmbedderLoaded()) { 
            return null;
        }

        File jarFile = FileUtil.archiveOrDirForURL(url);
        if (jarFile != null) {
//                String name = jarFile.getName();
            File parent = jarFile.getParentFile();
            if (parent != null) {
                File parentParent = parent.getParentFile();
                if (parentParent != null) {
                    // each repository artifact should have this structure
                    String artifact = parentParent.getName();
                    String version = parent.getName();
//                        File pom = new File(parent, artifact + "-" + version + ".pom");
//                        // maybe this condition is already overkill..
//                        if (pom.exists()) {
                    String start = artifact + "-" + version;
                    if (jarFile.getName().startsWith(start)) { //one last perf check before calling the embedder
                        URI localRepo = Utilities.toURI(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
                        URI rel = localRepo.relativize(Utilities.toURI(parentParent.getParentFile()));
                        if (!rel.isAbsolute()) {
                            String groupId = rel.getPath();
                            if (groupId != null && !groupId.equals("")) {
                                groupId = groupId.replace("/", ".");
                                if (groupId.endsWith(".")) {
                                    groupId = groupId.substring(0, groupId.length() - 1);
                                }
                                String classifier = null;
                                if (jarFile.getName().startsWith(start + "-")) {
                                    //we have classifier here..
                                    String end = jarFile.getName().substring((start + "-").length());
                                    if (end.indexOf('.') > -1) {
                                        classifier = end.substring(0, end.indexOf('.'));
                                    }
                                }
                                File srcs = new File(parent, start + (classifier != null ? ("-" + ("tests".equals(classifier) ? "test" : classifier)) : "") + "-sources.jar"); //NOI18N
                                SrcResult result = new SrcResult(groupId, artifact, version, classifier, FileUtil.getArchiveFile(url), srcs, (f) -> getJarMetadataCoordinatesIntern(f));
                                srcCache.put(url, new WeakReference<SrcResult>(result));
                                return result;
                            }
                        }

                    }
//                        }
                }
            }
            File[] f = SourceJavadocByHash.find(url, false);
            if (f != null && f.length > 0) {
                SrcResult result = new SrcResult(null, null, null, null, FileUtil.getArchiveFile(url), null, (file) -> getJarMetadataCoordinatesIntern(file));
                srcCache.put(url, new WeakReference<SrcResult>(result));
                return result;
            }
        }
        return null;
    }


    @Override
    public JavadocForBinaryQuery.Result findJavadoc(URL url) {
        URL binRoot;

        if ("jar".equals(url.getProtocol())) { //NOI18N
            binRoot = FileUtil.getArchiveFile(url);
        } else {
            // null for directories.
            return null;
        }

        // Hack for Java EE jar docs which we ship with netbeans and which are not in any maven repository
        if (binRoot.getPath().endsWith("/javax/javaee-api/7.0/javaee-api-7.0.jar")
         || binRoot.getPath().endsWith("/javax/javaee-api/6.0/javaee-api-6.0.jar")
         || binRoot.getPath().endsWith("/javax/javaee-web-api/7.0/javaee-web-api-7.0.jar")
         || binRoot.getPath().endsWith("/javax/javaee-web-api/6.0/javaee-web-api-6.0.jar")
         || binRoot.getPath().endsWith("javax.persistence-2.1.0.jar")
         || binRoot.getPath().endsWith("javax.persistence-2.0.0.jar")) { //NOI18N
            return new JavaEEJavadocResult();
        }

        WeakReference<JavadocResult> cached = javadocCache.get(url);
        if (cached != null) {
            JavadocResult result = cached.get();
            if (result != null) {
                return result;
            }
        }


        File jarFile = FileUtil.archiveOrDirForURL(url);
        if (jarFile != null) {
            File parent = jarFile.getParentFile();
            if (parent != null) {
                File parentParent = parent.getParentFile();
                if (parentParent != null) {
                    // each repository artifact should have this structure
                    String artifact = parentParent.getName();
                    String version = parent.getName();
                    String start = artifact + "-" + version;
                    if (jarFile.getName().startsWith(start)) { //one last perf check before calling the embedder
                        URI localRepo = Utilities.toURI(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
                        URI rel = localRepo.relativize(Utilities.toURI(parentParent.getParentFile()));
                        if (!rel.isAbsolute()) {
                            String groupId = rel.getPath();
                            if (groupId != null && !groupId.equals("")) {
                                groupId = groupId.replace("/", ".");
                                if (groupId.endsWith(".")) {
                                    groupId = groupId.substring(0, groupId.length() - 1);
                                }
                                String classifier = null;
                                if (jarFile.getName().startsWith(start + "-")) {
                                    //we have classifier here..
                                    String end = jarFile.getName().substring((start + "-").length());
                                    if (end.indexOf('.') > -1) {
                                        classifier = end.substring(0, end.indexOf('.'));
                                    }
                                }
                                File javadoc = new File(parent, start + (classifier != null ? ("-" + ("tests".equals(classifier) ? "test" : classifier)) : "") + "-javadoc.jar"); //NOI18N
                                JavadocResult result = new JavadocResult(groupId, artifact, version, classifier, binRoot, javadoc, (f) -> getJarMetadataCoordinatesIntern(f));
                                javadocCache.put(url, new WeakReference<JavadocResult>(result));
                                return result;
                            }
                        }
                    }
                }
            }
            File[] f = SourceJavadocByHash.find(url, true);
            if (f != null && f.length > 0) {
                JavadocResult result = new JavadocResult(null, null, null, null, binRoot, null, (file) -> getJarMetadataCoordinatesIntern(file));
                javadocCache.put(url, new WeakReference<JavadocResult>(result));
                return result;
            }
        }
        return null;
    }

    private static class SrcResult implements SourceForBinaryQueryImplementation2.Result  {

        private static final String ATTR_PATH = "lastRootCheckPath"; //NOI18N
        private static final String ATTR_STAMP = "lastRootCheckStamp"; //NOI18N
        private final File sourceJarFile;
        private final File fallbackSourceJarFile;
        private final ChangeSupport support;
        private final ChangeListener mfoListener;
        private final PropertyChangeListener projectListener;
        private final FileChangeListener sourceJarChangeListener;
        private final RequestProcessor.Task checkChangesTask;
        private static final int CHECK_CHANGES_DELAY = 50;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;
        private final URL binary;

        //TODO should this be weak referenced? how to add/remove project listeners then.
        private Project currentProject;
        private FileObject[] cached;
        private Boolean cachedPreferedSources;
        private boolean avoidScheduling = false; //to be accessed and modified only under synchronized lock
        private final AtomicBoolean needsFiring = new AtomicBoolean(false);
        private final Function<File, List<Coordinates>> coorProvider;

        SrcResult(@NullAllowed String groupId, @NullAllowed String artifactId, @NullAllowed String version, @NullAllowed String classifier, @NonNull URL binary, @NullAllowed File sourceJar, Function<File, List<Coordinates>> coorProvider) {
            sourceJarFile = sourceJar;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.binary = binary;
            this.classifier = classifier;
            this.coorProvider = coorProvider;

            support = new ChangeSupport(this);
            checkChangesTask = RP.create(
                    new Runnable() {
                        @Override
                        public void run() {
                            checkChanges(true);
                        }
                    });
            mfoListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e instanceof MavenFileOwnerQueryImpl.GAVCHangeEvent) {
                        MavenFileOwnerQueryImpl.GAVCHangeEvent gav = (MavenFileOwnerQueryImpl.GAVCHangeEvent)e;
                        //reload only when the project mapping for the current result's gav changed.
                        if (gav.getGroupId().equals(SrcResult.this.groupId) && gav.getArtifactId().equals(SrcResult.this.artifactId) && gav.getVersion().equals(SrcResult.this.version)) {
                            checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                        }
                    } else {
                        //external roots in local repository changed..
                        checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                    }
                }
            };
            projectListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                        //project could have changed source roots..
                        checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                    }
                }
            };
            sourceJarChangeListener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent fe) {
                    //source jar was created..
                    checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                }
            };
            checkChanges(false);
            
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.create(ChangeListener.class, mfoListener, MavenFileOwnerQueryImpl.getInstance()));

            if (sourceJarFile != null) {
                FileUtil.addFileChangeListener(FileUtil.weakFileChangeListener(sourceJarChangeListener, null));
                if (classifier != null) {
                    // fall back to regular sources if attached sources for classifier are missing
                    String regularSources = artifactId + "-" + version + "-sources.jar"; //NOI18N
                    if (!sourceJarFile.getName().equals(regularSources)) {
                        fallbackSourceJarFile = new File(sourceJarFile.getParentFile(), regularSources);
                        // already listening for changes through the file change listener above
                        return;
                    }
                }
            }            
            fallbackSourceJarFile = null;
        }

        private void checkChanges(boolean fireChanges) {
            // use MFOQI to determine what is the current project owning our coordinates in local repository.
            Project owner = null;
            if (groupId != null && artifactId != null && version != null) {
                owner = MavenFileOwnerQueryImpl.getInstance().getOwner(groupId, artifactId, version);
                if (owner != null && owner.getLookup().lookup(NbMavenProject.class) == null) {
                    owner = null;
                }
            }
            synchronized (this) {
                if (currentProject != null && !currentProject.equals(owner)) {
                    currentProject.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(projectListener);
                }
                if (owner != null && !owner.equals(currentProject)) {
                    owner.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
                }
                currentProject = owner;
                if (fireChanges && !needsFiring.get()) { //only do check if interested in changes and when no known change occured
                    avoidScheduling = true;
                    try {
                        getRoots();
                        preferSources();
                    } finally {
                        avoidScheduling = false;
                    }
                }
            }

            if (fireChanges && needsFiring.get()) {
                support.fireChange();
            }
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
            support.addChangeListener(changeListener);
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
            support.removeChangeListener(changeListener);
        }

        @Override
        public FileObject[] getRoots() {
            FileObject[] toRet;
            Project prj;
            synchronized (this) {
                prj = currentProject;
            }
            if (prj != null && classifier == null) {
                toRet = getProjectSrcRoots(prj);
            } else if (prj != null && CLASSIFIER_TESTS.equals(classifier)) {
                toRet = getProjectTestSrcRoots(prj);
            } else {
                //either no project or a classifier present
                URL root = FileUtil.isArchiveFile(binary) ? FileUtil.getArchiveRoot(binary) : binary;
                File[] f = SourceJavadocByHash.find(root, false);
                if (f != null) {
                    //hashes are processed separately, should not mesh with other means of discovery.
                    List<FileObject> accum = new ArrayList<FileObject>();
                    for (File ff : f) {
                        accum.addAll(Arrays.asList(getSourceJarRoot(ff)));
                    }
                    toRet = accum.toArray(new FileObject[0]);
                } 
                else {
                    //now comes the magic.
                    //all this should always come with preferSources == false
                    
                    List<FileObject> fos = new ArrayList<FileObject>();
                    
                    if (prj != null) { // have project here means we also have classifier
                        add(fos, getProjectSrcRoots(prj));
                    }
                    //ordering of source jar file and project roots is hard to guess.
                    // both can fail dependending on how the jar was created.
                    // the only way to let user decide would be some sort of stamp file inside maven local repository.
                    if (sourceJarFile != null && sourceJarFile.exists()) {
                        add(fos, getSourceJarRoot(sourceJarFile));
                    } else if (fallbackSourceJarFile != null && fallbackSourceJarFile.exists()) {
                        add(fos, getSourceJarRoot(fallbackSourceJarFile));
                    }
                    add(fos, getShadedJarSources());
                    toRet = fos.toArray(new FileObject[0]);
                }
            }
            synchronized (this) {
                if (cached != null && !Arrays.equals(cached, toRet)) {
                    if (needsFiring.compareAndSet(false, true) && !avoidScheduling) {
                        checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                    }
                }
                cached = toRet;
            }
            return toRet;
        }
        
        private void add(List<FileObject> to, FileObject[] add) {
            for (FileObject a : add) {
                if (!to.contains(a)) {
                    to.add(a);
                }
            }
        }

        private static String checkPath(FileObject jarRoot, FileObject fo) {
            String toRet = null;
            FileObject root = JavadocAndSourceRootDetection.findSourceRoot(jarRoot);
            try {
                if (root != null && !root.equals(jarRoot)) {
                    toRet = FileUtil.getRelativePath(jarRoot, root);
                    fo.setAttribute(ATTR_PATH, toRet);
                }
                fo.setAttribute(ATTR_STAMP, new Date());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toRet;
        }

        private @NonNull static FileObject[] getSourceJarRoot(File sourceJar) {
            FileObject fo = FileUtil.toFileObject(sourceJar);
            if (fo != null) {
                FileObject jarRoot = FileUtil.getArchiveRoot(fo);
                if (jarRoot != null) { //#139894 it seems that sometimes it can return null.
                                  // I suppose it's in the case when the jar/zip file in repository exists
                                  // but is corrupted (not zip, eg. when downloaded from a wrongly
                                  //setup repository that returns html documents on missing jar files.

                    //try detecting the source path root, in case the source jar has the sources not in root.
                    Date date = (Date) fo.getAttribute(ATTR_STAMP);
                    String path = (String) fo.getAttribute(ATTR_PATH);
                    if (date == null || fo.lastModified().after(date)) {
                        path = checkPath(jarRoot, fo);
                    }

                    FileObject[] fos = new FileObject[1];
                    if (path != null) {
                        fos[0] = jarRoot.getFileObject(path);
                    }
                    if (fos[0] == null) {
                        fos[0] = jarRoot;
                    }
                    return fos;
                }
            }
            return new FileObject[0];
        }

        @Override
        public boolean preferSources() {
            boolean toRet = false;
            Project prj;
            synchronized (this) {
                prj = currentProject;
            }
            if (prj != null && classifier == null) {
                if (!NbMavenProject.isErrorPlaceholder(prj.getLookup().lookup(NbMavenProject.class).getMavenProject())) {
                    toRet = prj.getLookup().lookup(ForeignClassBundler.class).preferSources();
                }
            } else if (prj != null && CLASSIFIER_TESTS.equals(classifier)) {
                toRet = true;
            }
            synchronized (this) {
                if (cachedPreferedSources != null && !cachedPreferedSources.equals(toRet)) {
                    if (needsFiring.compareAndSet(false, true) && !avoidScheduling) {
                        checkChangesTask.schedule(CHECK_CHANGES_DELAY);
                    }
                }
                cachedPreferedSources = toRet;
            }
            
            return toRet;
        }

        private @NonNull synchronized FileObject[] getShadedJarSources() {
            try {
                List<Coordinates> coordinates = coorProvider.apply(Utilities.toFile(binary.toURI()));
                File lrf = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
                List<FileObject> fos = new ArrayList<FileObject>();
                if (coordinates != null) {
                    for (Coordinates coord : coordinates) {
                        if (coord.artifactId.equals(artifactId) && coord.groupId.equals(groupId) && coord.version.equals(version)) {
                            continue; //skip the current jar, we've catered to in other ways.
                        }
                        File sourceJar = new File(lrf, coord.groupId.replace(".", File.separator) + File.separator + coord.artifactId + File.separator + coord.version + File.separator + coord.artifactId + "-" + coord.version + "-sources.jar");
                        fos.addAll(Arrays.asList(getSourceJarRoot(sourceJar)));
                    }
                }
                return fos.toArray(new FileObject[0]);
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error while examining binary " + binary, ex);
            }
            return new FileObject[0];
        }
    }

    public static class Coordinates {
        public final String groupId;
        public final String artifactId;
        public final String version;

        private Coordinates(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }

    private List<Coordinates> getJarMetadataCoordinatesIntern(File binaryFile) {        
        if (binaryFile == null || !binaryFile.exists() || !binaryFile.isFile()) {
            return null;
        }        
        synchronized (coorCache) {
            List<Coordinates> toRet = coorCache.get(binaryFile);
            if(toRet == null) {
                toRet = getJarMetadataCoordinates(binaryFile);         
                if(toRet != null) {
                    FileUtil.addFileChangeListener(binaryChangeListener, binaryFile);
                    coorCache.put(binaryFile, toRet);
                }                
            } 
            return toRet;
        }
    }
    
    public static List<Coordinates> getJarMetadataCoordinates(File binaryFile) {
        if (binaryFile == null || !binaryFile.exists() || !binaryFile.isFile()) {
            return null;
        }        
        ZipFile zip = null;
        try {
            List<Coordinates> toRet = new ArrayList<Coordinates>();
            zip = new ZipFile(binaryFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ent = entries.nextElement();
                String name = ent.getName();
                if (name.startsWith("META-INF") && name.endsWith("pom.properties")) {
                    Properties p = new Properties();
                    p.load(zip.getInputStream(ent));
                    String groupId = p.getProperty("groupId");
                    String artifactId = p.getProperty("artifactId");
                    String version = p.getProperty("version");
                    if (groupId != null && artifactId != null && version != null) {
                        toRet.add(new Coordinates(groupId, artifactId, version));
                    }
                }
            }
            if (toRet.size() > 0) {
                return toRet;
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "error while examining binary " + binaryFile, ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    private static class JavadocResult implements JavadocForBinaryQuery.Result {
        private final File javadocJarFile;
        private final File fallbackJavadocJarFile;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;
        private final URL binary;
        private final String gav;
        private final ChangeSupport support;
        private final PropertyChangeListener projectListener;
        private final FileChangeAdapter javadocJarChangeListener;
        private Project currentProject;
        private URL[] cached;
        private static final String ATTR_PATH = "lastRootCheckPath"; //NOI18N
        private static final String ATTR_STAMP = "lastRootCheckStamp"; //NOI18N
        private final ChangeListener mfoListener;
        private final Function<File, List<Coordinates>> coorProvider;

        JavadocResult(@NullAllowed String groupId, @NullAllowed String artifactId, @NullAllowed String version, @NullAllowed String classifier, @NonNull URL binary, @NullAllowed File javadocJar, @NonNull Function<File, List<Coordinates>> coorProvider) {
            javadocJarFile = javadocJar;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.binary = binary;
            this.classifier = classifier;
            this.gav = MavenFileOwnerQueryImpl.cacheKey(groupId, artifactId, version);
            this.coorProvider = coorProvider;

            support = new ChangeSupport(this);
            mfoListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    //external root in local repository changed..
                    checkChanges();
                }
            };
            projectListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
                        checkChanges();
                    }
                }
            };
            javadocJarChangeListener = new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent fe) {
                    //source jar was created..
                    checkChanges();
                }

            };
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.create(ChangeListener.class, mfoListener, MavenFileOwnerQueryImpl.getInstance()));
            if (javadocJarFile != null) {
                FileUtil.addFileChangeListener(javadocJarChangeListener, javadocJarFile);
                if (classifier != null) {
                    // listen for regular javadoc because attached javadoc for classifier might be missing
                    String regularJavadoc = artifactId + "-" + version + "-javadoc.jar"; //NOI18N
                    if (!javadocJarFile.getName().equals(regularJavadoc)) {
                        fallbackJavadocJarFile = new File(javadocJarFile.getParentFile(), regularJavadoc);
                        FileUtil.addFileChangeListener(javadocJarChangeListener, fallbackJavadocJarFile);
                        return;
                    }
                }
            }
            fallbackJavadocJarFile = null;
        }

        @Override
        public synchronized URL[] getRoots() {
            URL[] toRet;
            checkCurrentProject();
            Project prj = currentProject;
            if (prj != null) {
                toRet = new URL[0];
            } else {
                URL root = FileUtil.isArchiveFile(binary) ? FileUtil.getArchiveRoot(binary) : binary;
                File[] f = SourceJavadocByHash.find(root, true);
                if (f != null) {
                    List<URL> accum = new ArrayList<URL>();
                    for (File ff : f) {
                        URL[] url = getJavadocJarRoot(ff);
                        if (url != null) {
                            accum.addAll(Arrays.asList(url));
                        }
                    }
                    toRet = accum.toArray(new URL[0]);
                } else if (javadocJarFile != null && javadocJarFile.exists()) {
                    toRet = getJavadocJarRoot(javadocJarFile);
                } else if (fallbackJavadocJarFile != null && fallbackJavadocJarFile.exists()) {
                    toRet = getJavadocJarRoot(fallbackJavadocJarFile);
                } else {
                    toRet = checkShadedMultiJars();
                }
            }
            if (!Arrays.equals(cached, toRet)) {
                //how to figure otherwise that something changed, possibly multiple people hold the result instance
                // and one asks the roots, later we get event from outside, but then the cached value already updated..
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        support.fireChange();
                    }
                });
            }
            cached = toRet;
            return toRet;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            support.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            support.removeChangeListener(l);
        }

        private void checkChanges() {
            //getRoots will fire change in the result if the old cached value is different from the newly generated one
            getRoots();
        }
        /**
         * use MFOQI to determine what is the current project owning our coordinates in local repository.
         */
        private void checkCurrentProject() {
            Project owner = null;
            if (groupId != null && artifactId != null && version != null) {
                owner = MavenFileOwnerQueryImpl.getInstance().getOwner(groupId, artifactId, version);
            }
            if (owner != null && owner.getLookup().lookup(NbMavenProject.class) == null) {
                owner = null;
            }
            if (currentProject != null && !currentProject.equals(owner)) {
                currentProject.getLookup().lookup(NbMavenProject.class).removePropertyChangeListener(projectListener);
            }
            if (owner != null && !owner.equals(currentProject)) {
                owner.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
            }
            currentProject = owner;
        }

        private URL[] getJavadocJarRoot(File file) {
            try {
                if (file.exists()) {
                    FileObject fo = FileUtil.toFileObject(file);
                    if (!FileUtil.isArchiveFile(fo)) {
                        //#124175  ignore any jar files that are not jar files (like when downloaded file is actually an error html page).
                        Logger.getLogger(RepositoryForBinaryQueryImpl.class.getName()).log(Level.INFO, "javadoc in repository is not really a JAR: {0}", file);
                        return new URL[0];
                    }
                    //try detecting the source path root, in case the source jar has the sources not in root.
                    Date date = (Date) fo.getAttribute(ATTR_STAMP);
                    String path = (String) fo.getAttribute(ATTR_PATH);
                    if (date == null || fo.lastModified().after(date)) {
                        path = checkPath(FileUtil.getArchiveRoot(fo), fo);
                    }

                    URL[] url;
                    if (path != null) {
                        url = new URL[1];
                        URL root = FileUtil.getArchiveRoot(Utilities.toURI(file).toURL());
                        if (!path.endsWith("/")) { //NOI18N
                            path = path + "/"; //NOI18N
                        }
                        url[0] = new URL(root, path);
                    } else {
                         url = new URL[1];
                        url[0] = FileUtil.getArchiveRoot(Utilities.toURI(file).toURL());
                    }
                    return url;
                }
            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            return new URL[0];
        }


        private String checkPath(FileObject jarRoot, FileObject fo) {
            String toRet = null;
            FileObject root = JavadocAndSourceRootDetection.findJavadocRoot(jarRoot);
            try {
                if (root != null && !root.equals(jarRoot)) {
                    toRet = FileUtil.getRelativePath(jarRoot, root);
                    fo.setAttribute(ATTR_PATH, toRet);
                }
                fo.setAttribute(ATTR_STAMP, new Date());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return toRet;
        }

        private synchronized URL[] checkShadedMultiJars() {
            try {
                List<Coordinates> coordinates = coorProvider.apply(Utilities.toFile(binary.toURI()));
                File lrf = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
                List<URL> urls = new ArrayList<URL>();
                if (coordinates != null) {
                    for (Coordinates coord : coordinates) {
                            File javadocJar = new File(lrf, coord.groupId.replace(".", File.separator) + File.separator + coord.artifactId + File.separator + coord.version + File.separator + coord.artifactId + "-" + coord.version + "-javadoc.jar");
                            URL[] fo = getJavadocJarRoot(javadocJar);
                            if (fo.length == 1) {
                                urls.add(fo[0]);
                            }
                    }
                }
                if (urls.size() > 1) {
                    URL[] shaded = urls.toArray(new URL[0]);
                    return shaded;
                }
            } catch (Exception ex) {
                LOG.log(Level.INFO, "error while examining binary " + binary, ex);
            }
            return new URL[0];
        }

    }


    private static class JavaEEJavadocResult implements JavadocForBinaryQuery.Result {

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        @Override
        public URL[] getRoots() {
            try {
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee-doc-api.jar", "org.netbeans.modules.j2ee.platform", false); // NOI18N
                if (j2eeDoc != null) {
                    URL url = FileUtil.getArchiveRoot(Utilities.toURI(j2eeDoc).toURL());
                    url = new URL(url + "docs/api/"); //NOI18N
                    return new URL[]{url};
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new URL[0];
        }
    }        
}