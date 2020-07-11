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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeListener;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleArtifactStore {

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
        boolean changed = false;
        for (GradleConfiguration conf : gp.getBaseProject().getConfigurations().values()) {
            for (GradleDependency.ModuleDependency module : conf.getModules()) {
                Set<File> oldBins = binaries.get(module.getId());
                Set<File> newBins = module.getArtifacts();
                if (oldBins != newBins) {
                    binaries.put(module.getId(), newBins);
                    changed = true;
                }
                if (module.getArtifacts().size() == 1) {
                    File binary = module.getArtifacts().iterator().next();
                    if (module.getSources().size() == 1) {
                        File source = module.getSources().iterator().next();
                        if (binary.isFile() && source.isFile()) {
                            File old = sources.put(binary, source);
                            changed |= (old == null) || !old.equals(source);
                        }
                    }
                    if (module.getJavadoc().size() == 1) {
                        File javadoc = module.getJavadoc().iterator().next();
                        if (binary.isFile() && javadoc.isFile()) {
                            File old = javadocs.put(binary, javadoc);
                            changed |= (old == null) || !old.equals(javadoc);
                        }
                    }
                }
            }
        }
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
