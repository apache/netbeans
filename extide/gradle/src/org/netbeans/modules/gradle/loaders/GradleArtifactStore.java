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

package org.netbeans.modules.gradle.loaders;

import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gradle.GradleModuleFileCache21;
import org.netbeans.modules.gradle.GradleProject;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleArtifactStore {
    private static Logger LOG = Logger.getLogger(GradleArtifactStore.class.getName());
    
    private static final String GRADLE_ARTIFACT_STORE_INFO = "gradle/artifact-store-info.ser";
    public static final RequestProcessor RP = new RequestProcessor("Gradle Artifact Store", 1); //NOI18

    private final Map<String, Set<File>> binaries = new ConcurrentHashMap<>();
    private final Map<File, File> sources = new ConcurrentHashMap<>();
    private final Map<File, File> javadocs = new ConcurrentHashMap<>();

    private static final GradleArtifactStore INSTANCE = new GradleArtifactStore();
    private final ChangeSupport cs = new ChangeSupport(this);
    private final RequestProcessor.Task notifyTask = RP.create(new Runnable() {
        @Override
        public void run() {
            cs.fireChange();
        }
    });

    @OnStart
    public static class Loader implements Runnable {

        @Override
        public void run() {
            getDefault().load();
        }
    }

    public static GradleArtifactStore getDefault() {
        return INSTANCE;
    }

    public Set<File> getBinaries(String id) {
        Set<File> ret = binaries.get(id);
        return ret;
    }
    
    public Set<File> getSources(Set<File> binaries) {
        if (binaries.size() != 1) {
            return null;
        } else {
            File f = sources.get(binaries.iterator().next());
            return f == null ? null : Collections.singleton(f);
        }
    }
    
    public Set<File> getJavadocs(Set<File> binaries) {
        if (binaries.size() != 1) {
            return null;
        } else {
            File f = javadocs.get(binaries.iterator().next());
            return f == null ? null : Collections.singleton(f);
        }
    }
    
    public File getSources(File binary) {
        File ret = sources.get(binary);
        if (ret != null && !ret.exists()) {
            sources.remove(binary);
            ret = null;
        }
        if (ret == null) {
            ret = checkM2Heuristic(binary, "sources"); //NOI18N
            if (ret != null) {
                sources.put(binary, ret);
            }
        }
        return ret;
    }

    public File getJavadoc(File binary) {
        File ret = javadocs.get(binary);
        if (ret != null && !ret.exists()) {
            javadocs.remove(binary);
            ret = null;
        }
        if (ret == null) {
            ret = checkM2Heuristic(binary, "javadoc"); //NOI18N
            if (ret != null) {
                javadocs.put(binary, ret);
            }
        }
        return ret;
    }

    void processProject(GradleProject gp) {
        if (gp.getQuality().worseThan(Quality.FULL)) {
            return;
        }
        List<String> gavs = new ArrayList<>();
        boolean changed = false;
        for (GradleConfiguration conf : gp.getBaseProject().getConfigurations().values()) {
            for (GradleDependency.ModuleDependency module : conf.getModules()) {
                Set<File> oldBins = binaries.get(module.getId());
                Set<File> newBins = module.getArtifacts();
                gavs.add(module.getId());
                if (!Objects.equals(oldBins, newBins)) {
                    binaries.put(module.getId(), newBins);
                    LOG.log(Level.FINER, "Updating JAR {0} to {1}", new Object[] { module.getId(), newBins });
                    changed = true;
                }
                if (module.getArtifacts().size() == 1) {
                    File binary = module.getArtifacts().iterator().next();
                    if (module.getSources().size() == 1) {
                        File source = module.getSources().iterator().next();
                        if (binary.isFile() && source.isFile()) {
                            File old = sources.put(binary, source);
                            boolean c = (old == null) || !old.equals(source);
                            if (c && LOG.isLoggable(Level.FINER)) {
                                LOG.log(Level.FINER, "Updating source {0} to {1}", new Object[] { module.getId(), source });
                            }
                            changed |= c;
                        }
                    }
                    if (module.getJavadoc().size() == 1) {
                        File javadoc = module.getJavadoc().iterator().next();
                        if (binary.isFile() && javadoc.isFile()) {
                            File old = javadocs.put(binary, javadoc);
                            boolean c = (old == null) || !old.equals(javadoc);
                            if (c && LOG.isLoggable(Level.FINER)) {
                                LOG.log(Level.FINER, "Updating javadoc {0} to {1}", new Object[] { module.getId(), javadoc });
                            }
                        }
                    }
                }
            }
        }
        LOG.log(Level.FINE, "Cache refresh for project {0}, changed {2}, module deps {1}", new Object[] {
            gp.getBaseProject().getProjectDir(), gavs, changed
        });
        if (changed) {
            store();
            notifyTask.schedule(1000);
        }
    }

    public void clear() {
        sources.clear();
        javadocs.clear();
        store();
    }

    void verify() {
        if (verifyMap(sources) || verifyMap(javadocs)) {
            store();
        }
    }
    
    /**
     * Checks that all dependencies the project thinks should be in the global cache
     * are actually in the global cache. If the global artifact cache does not contain
     * an entry from a resolved dependency in the project cache then many random failures can
     * occur, as an artifact is formally OK, but its JAR cannot be looked up.
     * 
     * @param gp cached project
     * @return true if the cached project resolves.
     */
    public final boolean sanityCheckCachedProject(GradleProject gp) {
        GradleModuleFileCache21 modCache = GradleModuleFileCache21.getGradleFileCache();
        for (GradleConfiguration conf : gp.getBaseProject().getConfigurations().values()) {
            for (GradleDependency.ModuleDependency module : conf.getModules()) {
                Set<File> oldBins = binaries.get(module.getId());
                boolean empty = module.getArtifacts() == null || module.getArtifacts().isEmpty();
                boolean binsEmpty = oldBins == null || oldBins.isEmpty();
                boolean cacheEmpty;
                GradleModuleFileCache21.CachedArtifactVersion cav = modCache.resolveModule(module.getId());
                if (cav != null && cav.getBinary() != null) {
                    // possibly trigger project reload if the artifact store does not match the GradleModuleFileCache21.
                    cacheEmpty = !Files.exists(cav.getBinary().getPath());
                } else {
                    cacheEmpty = true;
                }
                if (empty != (cacheEmpty && binsEmpty)) {
                    LOG.log(Level.FINE, "Checking {0}: Module dependency {1} not found in cache.", new Object[] { gp.getBaseProject().getProjectDir(), module.getId() });
                    return false;
                }
                if (!binsEmpty && oldBins.size() == 1) {
                    File binary = oldBins.iterator().next();
                    if (cav == null) {
                        LOG.log(Level.FINE, "Checking {0}: Cached artifact not found for {1}", new Object[] { gp.getBaseProject().getProjectDir(), module.getId() });
                        return false;
                    }
                    GradleModuleFileCache21.CachedArtifactVersion.Entry javadocEntry = cav.getJavaDoc();
                    GradleModuleFileCache21.CachedArtifactVersion.Entry sourceEntry = cav.getSources();
                    if (sourceEntry != null && Files.exists(sourceEntry.getPath())) {
                        File check = sources.get(binary);
                        if (check != null && !check.toPath().equals(sourceEntry.getPath())) {
                            LOG.log(Level.FINE, "Checking {0}: cache does not list CachedArtifact for source {2}", new Object[] { gp.getBaseProject().getProjectDir(), module.getId(), sourceEntry.getPath() });
                            return false;
                        }
                    }
                    if (javadocEntry != null && Files.exists(javadocEntry.getPath())) {
                        File check = javadocs.get(binary);
                        if (check != null && !check.toPath().equals(javadocEntry.getPath())) {
                            LOG.log(Level.FINE, "Checking {0}: cache does not list CachedArtifact for javadoc {2}", new Object[] { gp.getBaseProject().getProjectDir(), module.getId(), javadocEntry.getPath() });
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    private boolean verifyMap(final Map<File, File> m) {
        boolean changed = false;
        Iterator<Map.Entry<File, File>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<File, File> entry = it.next();
            if (!entry.getKey().exists() || !entry.getValue().exists()) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    @SuppressWarnings("unchecked")
    void load() {
        File cache = Places.getCacheSubfile(GRADLE_ARTIFACT_STORE_INFO);
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(cache))) {
            Map<String, Set<File>> bins = (Map<String, Set<File>>) is.readObject();
            Iterator<Map.Entry<String, Set<File>>> it = bins.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Set<File>> entry = it.next();
                for (File f : entry.getValue()) {
                    if (!f.exists()) {
                        it.remove();
                        break;
                    }
                    
                }
            }
            binaries.clear();
            binaries.putAll(bins);
            
            HashMap<File, File> srcs = (HashMap<File, File>) is.readObject();
            verifyMap(srcs);
            sources.clear();
            sources.putAll(srcs);

            HashMap<File, File> docs = (HashMap<File, File>) is.readObject();
            verifyMap(docs);
            javadocs.clear();
            javadocs.putAll(docs);
        } catch (Throwable ex) {
            // Nothing to be done. Disk cache is invalid, it will be overwritten.
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void store() {
        File cache = Places.getCacheSubfile(GRADLE_ARTIFACT_STORE_INFO);
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(cache))) {
            os.writeObject(new HashMap(binaries));
            os.writeObject(new HashMap(sources));
            os.writeObject(new HashMap(javadocs));
        } catch (IOException ex) {

        }
    }

    private static File checkM2Heuristic(File mainJar, String classifier) {
        File ret = null;
        String fname = mainJar.getName();
        StringBuilder guessName = new StringBuilder(fname);
        if (fname.endsWith(".jar")) {                                       //NOI18N
            guessName = guessName.delete(guessName.length() - 4, guessName.length());
            guessName.append('-').append(classifier).append(".jar");        //NOI18N
            File guess = new File(mainJar.getParentFile(), guessName.toString());
            if (guess.isFile()) {
                ret = guess;
            }
        }
        return ret;
    }
}
