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

package org.netbeans.modules.gradle.options;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.wrapper.IDownload;
import org.gradle.wrapper.Install;
import org.gradle.wrapper.Logger;
import org.gradle.wrapper.PathAssembler;
import org.gradle.wrapper.WrapperConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;
import org.openide.windows.OnShowing;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleDistributionManager {

    private static final String TOOLING_API_NAME = "modules/gradle/gradle-tooling-api.jar"; //NOI18N
    private static final File TOOLING_API = InstalledFileLocator.getDefault().locate(TOOLING_API_NAME, NbGradleProject.CODENAME_BASE, false);
    private static final Pattern WRAPPER_DIR_PATTERN = Pattern.compile("gradle-(\\d+\\.\\d+.*)-(bin|all)"); //NOI18N

    private static final String DOWNLOAD_URI = "https://services.gradle.org/distributions/gradle-%s-all.zip"; //NOI18N

    private static final RequestProcessor RP = new RequestProcessor("Gradle Installer", 1); //NOI18N
    private static String defaultVersion;

    private static final Set<String> VERSION_BLACKLIST = new HashSet<>(Arrays.asList("2.3", "2.13")); //NOI18N
    private static final Map<URL, GradleVersion> VERSIONS = new HashMap<>();
    private static final String LAST_KNOWN_GOOD_RELEASE = "4.10.2"; //NOI18N

    private GradleDistributionManager() {
    }

    public static File distributionDir(File gradleUserHome, GradleVersion version) {
        File distDir = distributionBaseDir(gradleUserHome, String.format(DOWNLOAD_URI, version.getVersion()));
        if (distDir.isDirectory()) {
            List<File> dirs = listDirs(distDir);
            assert dirs.size() <= 1 : "Only one directory allowed in distribution dir"; //NOI18N
            if (!dirs.isEmpty()) {
                return dirs.get(0);
            }
        }
        //Try to guess something not too wild here
        return new File(distDir, "gradle-" + version.getVersion());
    }

    public static File distributionBaseDir(File gradleUserHome, String downloadURL) {
        WrapperConfiguration conf = new WrapperConfiguration();
        try {
            URI uri = new URI(downloadURL);
            conf.setDistribution(uri);
            PathAssembler pa = new PathAssembler(gradleUserHome);
            PathAssembler.LocalDistribution dist = pa.getDistribution(conf);
            return dist.getDistributionDir();

        } catch (URISyntaxException ex) {
            //TODO: Ignore?
        }
        assert false : "We can't  evaluete Gradle base distribution dir, maybe Gradle changed something..."; //NOI18N
        return null;
    }

    public static String defaultToolingVersion() {
        if (defaultVersion == null) {
            try {
                String jarVersion = null;
                try {
                    jarVersion = new JarFile(TOOLING_API).getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                } catch (NullPointerException ex) {
                    try (InputStream is = GradleDistributionManager.class.getClassLoader().getResourceAsStream("/org/gradle/build-receipt.properties")) { //NOI18N
                        Properties props = new Properties();
                        props.load(is);
                        jarVersion = props.getProperty("versionNumber"); //NOI18N
                    } catch (IOException iex) {}
                }
                defaultVersion = !VERSION_BLACKLIST.contains(jarVersion) ? jarVersion : LAST_KNOWN_GOOD_RELEASE;
            } catch (IOException ex) {
                //Extreme case, shall not happen
                assert false : "We can't read the Tooling API something is really broken."; //NOI18N
            }
        }
        return defaultVersion;
    }

    /**
     * Tries to evaluate the project distribution directory offline. Wrapper
     * preference is not on account.
     *
     * @return a non-null, but possibly non existent distribution directory.
     */
    public static File evaluateGradleDistribution() {
        GradleSettings settings = GradleSettings.getDefault();
        File ret = null;
        if ((ret == null) && settings.useCustomGradle() && !settings.getDistributionHome().isEmpty()) {
            File f = FileUtil.normalizeFile(new File(settings.getDistributionHome()));
            if (f.isDirectory()) {
                ret = f;
            }
        }
        if (ret == null) {
            ret = distributionDir(settings.getGradleUserHome(), settings.getGradleVersion());
        }
        return ret;
    }

    public static String getDistributionVersion(File distributionBase) {
        File libs = new File(distributionBase, "lib"); //NOI18N
        File gradleCore = null;
        for (File f : libs.listFiles()) {
            if (f.getName().startsWith("gradle-core") && f.getName().endsWith(".jar")) {
                gradleCore = f;
                break;
            }
        }
        if (gradleCore != null) {
            try (JarFile jar = new JarFile(gradleCore)) {
                String versionString = jar.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                return versionString;
            } catch (IOException ex) {
                //Do not really care.
                //TODO: At least log this.
            }
        }
        return null;
    }

    /**
     * Tries to evaluate the project distribution directory offline. If wrapper
     * is preferred and no offline execution is required then it's better to
     * leave the GradleConnetor use Installation empty, so the requested
     * distribution will be downloaded.
     *
     * @param rootDir the root directory of the root project.
     * @return a non-null, but possibly non existent distribution directory.
     */
    public static File evaluateGradleWrapperDistribution(File rootDir) {
        GradleSettings settings = GradleSettings.getDefault();
        File ret = null;
        if (settings.isWrapperPreferred()) {
            File wrapperProps = new File(rootDir, "gradle/wrapper/gradle-wrapper.properties"); //NOI18N
            if (wrapperProps.isFile() && wrapperProps.canRead()) {
                Properties wrapper = new Properties();
                try (FileInputStream is = new FileInputStream(wrapperProps)) {
                    wrapper.load(is);
                } catch (IOException ex) {

                }
                String distUrl = wrapper.getProperty("distributionUrl"); //NOI18N
                if (distUrl != null) {
                    File distDir = distributionBaseDir(settings.getGradleUserHome(), distUrl);
                    if (distDir.exists() && distDir.isDirectory()) {
                        List<File> dirs = listDirs(distDir);
                        assert dirs.size() <= 1 : "Only one directory allowed in distribution dir"; //NOI18N
                        ret = dirs.get(0);
                    }
                }
            }
        }
        if (ret == null) {
            ret = evaluateGradleDistribution();
        }
        return ret;
    }

    public static List<GradleVersion> availableVersions(boolean releaseOnly) {
        List<GradleVersion> ret = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            URL allVersions = new URL("http://services.gradle.org/versions/all"); //NOI18N
            try (InputStreamReader is = new InputStreamReader(allVersions.openStream())) {
                JSONArray versions = (JSONArray) parser.parse(is);
                for (Object o : versions) {
                    JSONObject v = (JSONObject) o;
                    URL downloadURL = new URL((String) v.get("downloadUrl"));
                    boolean snapshot = (Boolean) v.get("snapshot");
                    boolean nightly = (Boolean) v.get("nightly");
                    boolean broken = (Boolean) v.get("broken");
                    String version = (String) v.get("version");
                    String rcFor = (String) v.get("rcFor");
                    if (nightly || broken || snapshot) {
                        continue;
                    }
                    if (!rcFor.isEmpty() && releaseOnly) {
                        continue;
                    }
                    ret.add(createVersion(version, downloadURL, rcFor.isEmpty()));
                }
            } catch (ParseException | IOException ex) {
                //TODO: Shall we do something about this?
            }
        } catch (MalformedURLException ex) {
            //Shall not happen with hardcoded URL
        }
        return ret;
    }

    public List<String> installedVersions(File gradleUserHome) {
        List<String> ret = new ArrayList<>();
        File wrapperDir = new File(gradleUserHome, "wrapper/dists"); //NOI18N
        if (wrapperDir.isDirectory()) {
            File[] dirs = wrapperDir.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    Matcher m = WRAPPER_DIR_PATTERN.matcher(dir.getName());
                    if (m.matches()) {
                        ret.add(m.group(1));
                    }
                }
            }
        }
        return ret;
    }

    public static boolean isDefaultVersionAvailable() {
        GradleSettings settings = GradleSettings.getDefault();
        return settings.getGradleVersion().isAvailable(settings.getGradleUserHome());
    }

    public static GradleVersion createVersion(String version) {
        try {
            URL url = new URL(String.format(DOWNLOAD_URI, version));
            return createVersion(version, url, !version.contains("-"));
        } catch (MalformedURLException ex) {
            //Should not really happen.
        }
        return null;
    }

    private static GradleVersion createVersion(String version, URL url, boolean b) {
        GradleVersion ret = VERSIONS.get(url);
        if (ret == null) {
            ret = new GradleVersion(version, url, b);
            VERSIONS.put(url, ret);
        }
        return ret;
    }

    private static List<File> listDirs(File d) {
        List<File> ret = new ArrayList<>();
        if (d.isDirectory()) {
            for (File f : d.listFiles()) {
                if (f.isDirectory()) {
                    ret.add(f);
                }
            }
        }
        return ret;
    }

    @OnShowing
    public static class InstallDefaultGradle implements Runnable {

        @Override
        public void run() {
            GradleSettings settings = GradleSettings.getDefault();
            if (!settings.useCustomGradle() && !settings.isOffline()) {
                settings.getGradleVersion().install(settings.getGradleUserHome());
            }
        }

    }

    public static class GradleVersion {

        public static final String PROP_AVAILABLE = "available"; //NOI18N

        final String version;
        final URL downloadLocation;
        final boolean release;
        private PropertyChangeSupport pcs;

        private GradleVersion(String version, URL downloadLocation, boolean release) {
            this.version = version;
            this.downloadLocation = downloadLocation;
            this.release = release;
        }

        public String getVersion() {
            return version;
        }

        public URL getDownloadLocation() {
            return downloadLocation;
        }

        public boolean isRelease() {
            return release;
        }

        public boolean isAvailable(File gradleUserHome) {
            File distDir = distributionDir(gradleUserHome, this);
            return (distDir != null) && distDir.isDirectory();
        }

        public boolean isBlackListed() {
            return VERSION_BLACKLIST.contains(version);
        }

        public void install(File gradleUserHome) {
            if (!isAvailable(gradleUserHome)) {
                RP.post(new InstallTask(this, gradleUserHome), 500);
            }
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (pcs == null) {
                pcs = new PropertyChangeSupport(this);
            }
            pcs.addPropertyChangeListener(l);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            if (pcs != null) {
                pcs.removePropertyChangeListener(l);
            }
        }

        void fireVersionAvailable() {
            if (pcs != null) {
                pcs.firePropertyChange(PROP_AVAILABLE, null, null);
            }
        }

        @Override
        public int hashCode() {
            return version.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof GradleVersion)) {
                return version.equals(((GradleVersion) obj).version);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return version;
        }

    }

    private static class InstallTask implements Runnable, IDownload {

        private final GradleVersion version;
        private final File gradleUserHome;
        private final ProgressHandle handle;

        public InstallTask(GradleVersion version, File gradleUserHome) {
            this.version = version;
            this.gradleUserHome = gradleUserHome;
            handle = ProgressHandleFactory.createSystemHandle("Installing Gradle " + version.getVersion());
        }

        @Override
        public void run() {
            try {
                WrapperConfiguration conf = new WrapperConfiguration();
                conf.setDistribution(version.getDownloadLocation().toURI());
                PathAssembler pa = new PathAssembler(gradleUserHome);
                Install install = new Install(new Logger(true), this, pa);
                install.createDist(conf);
                version.fireVersionAvailable();
            } catch (Exception ex) {
                //Happens if something goes wrong with the download.
                //TODO: Is it ok to let id silently die?
            }
        }

        @Override
        public void download(URI uri, File file) throws Exception {
            URL url = uri.toURL();
            URLConnection conn = url.openConnection();
            byte[] buf = new byte[2048];
            try (FileOutputStream os = new FileOutputStream(file)) {
                conn.connect();
                int size = conn.getContentLength();
                if (size > 0) {
                    handle.start(size);
                } else {
                    handle.start();
                }
                int allRead = 0;
                int read;
                InputStream is = url.openStream();
                while ((read = is.read(buf)) > 0) {
                    os.write(buf, 0, read);
                    allRead += read;
                    if (size > 0) {
                        handle.progress(allRead);
                    }
                }
            } finally {
                handle.finish();
            }
        }

    }
}
