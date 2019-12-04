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

package org.netbeans.modules.gradle.spi;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Collection of notable files used in a Gradle project.
 *
 * @author Laszlo Kishalmi
 */
public final class GradleFiles implements Serializable {

    public enum Kind {

        BUILD_SCRIPT,
        ROOT_SCRIPT,
        SETTINGS_SCRIPT,
        USER_PROPERTIES,
        PROJECT_PROPERTIES,
        ROOT_PROPERTIES;

        public static final Set<Kind> SCRIPTS = EnumSet.of(ROOT_SCRIPT, BUILD_SCRIPT, SETTINGS_SCRIPT);
        public static final Set<Kind> PROPERTIES = EnumSet.of(USER_PROPERTIES, PROJECT_PROPERTIES, ROOT_PROPERTIES);
        public static final Set<Kind> PROJECT_FILES = EnumSet.of(ROOT_SCRIPT, BUILD_SCRIPT, SETTINGS_SCRIPT, PROJECT_PROPERTIES, ROOT_PROPERTIES);
    }

    public static final String SETTINGS_FILE_NAME     = "settings.gradle"; //NOI18N
    public static final String SETTINGS_FILE_NAME_KTS = "settings.gradle.kts"; //NOI18N
    public static final String BUILD_FILE_NAME        = "build.gradle"; //NOI18N
    public static final String BUILD_FILE_NAME_KTS    = "build.gradle.kts"; //NOI18N
    public static final String GRADLE_PROPERTIES_NAME = "gradle.properties"; //NOI18N
    public static final String WRAPPER_PROPERTIES     = "gradle/wrapper/gradle-wrapper.properties"; //NOI18N

    final File projectDir;
    final boolean knownProject;
    File rootDir;
    File buildScript;
    File parentScript;
    File settingsScript;
    File gradlew;
    File wrapperProperties;

    public GradleFiles(File dir) {
        this(dir, false);
    }
    
    public GradleFiles(File dir, boolean knownProject) {
        this.knownProject = knownProject;
        try {
            dir = dir.getCanonicalFile();
        } catch (IOException ex) {
            dir = FileUtil.normalizeFile(dir);
        }
        projectDir = dir;
        rootDir = projectDir;
        searchBuildScripts();
        searchWrapper();
    }

    private List<File> searchPropertyFiles() {
        List<File> ret = new ArrayList<>(3);
        for (Kind kind:Kind.PROPERTIES){
            File f = getFile(kind);
            if (f.exists()){
                ret.add(f);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    private void searchBuildScripts() {
        File f1 = new File(projectDir, BUILD_FILE_NAME_KTS);
        if (!f1.canRead()) {
            f1 = new File(projectDir, BUILD_FILE_NAME);
        }
        File f2 = new File(projectDir, projectDir.getName() + ".gradle.kts");
        if (!f2.canRead()) {
            f2 = new File(projectDir, projectDir.getName() + ".gradle");
        }

        settingsScript = searchPathUp(projectDir, SETTINGS_FILE_NAME_KTS);
        if (settingsScript == null) {
            settingsScript = searchPathUp(projectDir, SETTINGS_FILE_NAME);
        }
        File settingsDir = settingsScript != null ? settingsScript.getParentFile() : null;
        buildScript = f1.canRead() ? f1 : f2.canRead() ? f2 : null;
        if (settingsDir != null) {
            //Guessing subprojects
            rootDir = settingsDir;
            File rootScript = new File(settingsDir, BUILD_FILE_NAME);
            if (rootScript.canRead() && !rootScript.equals(buildScript)) {
                parentScript = rootScript;
            }
        } else {
            if (buildScript != null) {
                rootDir = buildScript.getParentFile();
            } else {
                // TODO: NotSupportedLayout
            }
        }
    }

    private void searchWrapper() {
        File w = new File(rootDir, Utilities.isWindows() ? "gradlew.bat" : "gradlew");
        if (w.isFile()) {
            gradlew = w;
            wrapperProperties = new File(rootDir, WRAPPER_PROPERTIES);
        }
    }

    private File searchPathUp(@NonNull File baseDir, @NonNull String name) {
        File ret = null;
        File dir = baseDir;
        do {
            File f = new File(dir, name);
            ret = f.canRead() ? f : null;
            dir = f.canRead() ? dir : dir.getParentFile();
        } while ((ret == null) && (dir != null));
        return ret;
    }

    public File getBuildScript() {
        return buildScript;
    }

    public File getParentScript() {
        return parentScript;
    }

    public File getSettingsScript() {
        return settingsScript;
    }

    public List<File> getPropertyFiles() {
        return searchPropertyFiles();
    }

    public File getProjectDir() {
        return isProject() ? projectDir : null;
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getGradlew() {
        return gradlew;
    }

    public File getWrapperProperties() {
        return wrapperProperties;
    }

    public boolean hasWrapper() {
        return wrapperProperties != null;
    }

    public boolean isRootProject() {
        return (buildScript != null) && rootDir.equals(projectDir);
    }

    public boolean isSubProject() {
        return isProject() && !isRootProject();
    }

    public boolean isScriptlessSubProject() {
        return (buildScript == null) && isSubProject();
    }

    public boolean isProject() {
        boolean ret = knownProject || (buildScript != null);
        if (!ret && (settingsScript != null)) {
            ret = SettingsFile.getSubProjects(settingsScript).contains(projectDir);
        }
        return ret;
    }

    /**
     * Returns the main suspected build scripts available for this project. This includes the project build script (if
     * has any). The root build script and the settings script as well.
     *
     * @return The set of the existing scripts. En empty set if this GradleFiles doesn't look like an ordinary Gradle
     * project.
     */
    public Set<File> getProjectFiles() {
        if (isProject()) {
            Set<File> ret = new HashSet<>();
            if (parentScript != null) {
                ret.add(parentScript);
            }
            if (buildScript != null) {
                ret.add(buildScript);
            }
            if (settingsScript != null) {
                ret.add(settingsScript);
            }
            return ret;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Returns the possible file names for a Gradle project file,
     *
     * @param kind The role of the project file.
     * @return
     */
    public File getFile(Kind kind) {
        switch (kind) {
            case BUILD_SCRIPT:
                return buildScript != null ? buildScript : new File(projectDir, BUILD_FILE_NAME);
            case ROOT_SCRIPT:
                return parentScript != null ? parentScript : new File(rootDir, BUILD_FILE_NAME);
            case SETTINGS_SCRIPT:
                return settingsScript != null ? settingsScript : new File(rootDir, SETTINGS_FILE_NAME);

            case PROJECT_PROPERTIES:
                return new File(projectDir, GRADLE_PROPERTIES_NAME);
            case ROOT_PROPERTIES:
                return new File(rootDir, GRADLE_PROPERTIES_NAME);
            case USER_PROPERTIES: {
                File guh = GradleSettings.getDefault().getGradleUserHome();
                return new File(guh, GRADLE_PROPERTIES_NAME);
            }
            default:
                return null;
        }
    }

    public long lastChanged() {
        long time = (buildScript != null) ? buildScript.lastModified() : 0;
        if (settingsScript != null) {
            time = Math.max(settingsScript.lastModified(), time);
        }
        if (parentScript != null) {
            time = Math.max(parentScript.lastModified(), time);
        }
        return time;
    }

    @Override
    public int hashCode() {
        return  Objects.hashCode(this.projectDir) * 83;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GradleFiles other = (GradleFiles) obj;
        return Objects.equals(this.projectDir, other.projectDir);
    }

    @Override
    public String toString() {
        return "GradleFiles[projectDir=" + projectDir + ", rootDir=" + rootDir + "]";
    }

    public static class SettingsFile {

        private static final Pattern SET_PROJECTDIR_PATTERN
                = Pattern.compile(".*['\\\"](.+)['\\\"].*\\.projectDir.*=.*['\\\"](.+)['\\\"].*"); //NOI18N
        private static final Map<File, SettingsFile> CACHE = new WeakHashMap<>();

        final Set<File> subProjects = new HashSet<>();

        final long time;

        public SettingsFile(File f) {
            time = f.lastModified();
            parse(f);
        }

        private void parse(File f) {
            Map<String, String> projectPaths = new HashMap<>();
            String rootDir = f.getParentFile().getAbsolutePath();
            try {
                List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8")); //NOI18N
                for (String line : lines) {
                    line = line.trim();
                    if (!line.startsWith("//")) {

                        String[] split = line.split("[\\s'\",\\(\\)]+"); //NOI18N
                        if ((split.length > 1) && "include".equals(split[0])) { //NOI18N
                            for (int i = 1; i < split.length; i++) {
                                String s = split[i];
                                projectPaths.put(s, rootDir + "/" + s.replace(':', '/')); //NOI18N
                            }
                        }
                        Matcher matcher = SET_PROJECTDIR_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            String project = matcher.group(1);
                            String dir = matcher.group(2);
                            dir = dir.replace("$rootDir", rootDir); //NOI18N
                            dir = dir.replace("${rootDir}", rootDir); //NOI18N
                            projectPaths.put(project, dir);
                        }
                    }
                }
            } catch (IOException ex) {
                // Can't read the settings file for some reason.
                // It is ok for now simply return an emty list.
            }
            File root = f.getParentFile();
            for (Map.Entry<String, String> entry : projectPaths.entrySet()) {
                subProjects.add(guessDir(entry.getKey(), root, new File(entry.getValue())));
            }
        }

        File guessDir(String projectName, File rootDir, File firstGuess) {
            if (firstGuess.isDirectory()) {
                return firstGuess;
            }
            for (String subdirName : Arrays.asList("subProjects", "modules")) {
                File subdir = new File(rootDir, subdirName);
                if (subdir.isDirectory()) {
                    if (new File(subdir, projectName).isDirectory()) {
                        return new File(subdir, projectName);
                    }
                    String gradleStyle = projectName.replaceAll("\\p{Upper}", "-$0").toLowerCase();
                    if (new File(subdir, gradleStyle).isDirectory()) {
                        return new File(subdir, gradleStyle);
                    }
                }
            }
            return firstGuess;
        }

        public static Set<File> getSubProjects(File f) {
            SettingsFile sf = CACHE.get(f);
            if ((sf == null) || (sf.time < f.lastModified())) {
                sf = new SettingsFile(f);
                CACHE.put(f, sf);
            }
            return sf.subProjects;
        }
    }

}
