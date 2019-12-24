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

import java.beans.PropertyChangeEvent;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.util.GradleVersion;
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
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleDistributionManager {

    private static final Pattern WRAPPER_DIR_PATTERN = Pattern.compile("gradle-(\\d+\\.\\d+.*)-(bin|all)"); //NOI18N
    private static final Pattern DIST_VERSION_PATTERN = Pattern.compile(".*gradle-(\\d+\\.\\d+.*)-(bin|all)\\.zip"); //NOI18N

    private static final String DOWNLOAD_URI = "https://services.gradle.org/distributions/gradle-%s-bin.zip"; //NOI18N

    private static final RequestProcessor RP = new RequestProcessor("Gradle Installer", 1); //NOI18N

    private static final Set<String> VERSION_BLACKLIST = new HashSet<>(Arrays.asList("2.3", "2.13")); //NOI18N
    private static final GradleVersion MINIMUM_SUPPORTED_VERSION = GradleVersion.version("2.0"); //NOI18N

    private static final Map<File, GradleDistributionManager> CACHE = new WeakHashMap<>();
    private static final int JAVA_VERSION;

    static {
        int ver = 8;
        String version = System.getProperty("java.specification.version", System.getProperty("java.version")); //NOI18N
        try {
            int dot = version.indexOf('.');
            ver = dot > 0 ? Integer.parseInt(version.substring(0,dot)) : Integer.parseInt(version);
            if (ver == 1) {
                version = version.substring(dot + 1);
                dot = version.indexOf('.');
                ver = dot > 0 ? Integer.parseInt(version.substring(0,dot)) : Integer.parseInt(version);
            }
        } catch (NumberFormatException ex) {
            Exceptions.printStackTrace(ex);
        }
        JAVA_VERSION = ver;
    }

    final File gradleUserHome;
    private final Map<URI, NbGradleVersion> versions = new HashMap<>();

    private GradleDistributionManager(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome;
    }

    public static GradleDistributionManager get(File gradleUserHome) {
        GradleDistributionManager ret = CACHE.get(gradleUserHome);
        if (ret == null) {
            ret = new GradleDistributionManager(gradleUserHome);
            CACHE.put(gradleUserHome, ret);
        }
        return ret;
    }

    public NbGradleVersion defaultToolingVersion() {
        return createVersion(GradleVersion.current().getVersion());
    }

    public File install(NbGradleVersion version) {
        File ret = null;
        if (version.install()) {
            Lock lock = new ReentrantLock();
            PropertyChangeListener pcl = (PropertyChangeEvent evt) -> {
                if (NbGradleVersion.PROP_AVAILABLE.equals(evt.getPropertyName())) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            };
            try {
                synchronized (lock) {
                    version.addPropertyChangeListener(pcl);
                    lock.wait();
                }
            } catch (InterruptedException ex) {
                return ret;
            } finally {
                version.removePropertyChangeListener(pcl);
            }
            ret = version.distributionDir();
        } else {
            ret = version.distributionDir();
        }
        return ret;
    }
    /**
     * Tries to evaluate the project distribution. If wrapper
     * is preferred and no offline execution is required then it's better to
     * leave the GradleConnetor use Installation empty, so the requested
     * distribution will be downloaded.
     *
     * @param rootDir the root directory of the root project.
     * @return a the version used in the wrapper or null if there is no wrapper
     *         or the version cannot be determined.
     */
    public NbGradleVersion evaluateGradleWrapperDistribution(File rootDir) {
        NbGradleVersion ret = null;
        File wrapperProps = new File(rootDir, "gradle/wrapper/gradle-wrapper.properties"); //NOI18N
        if (wrapperProps.isFile() && wrapperProps.canRead()) {
            Properties wrapper = new Properties();
            try (FileInputStream is = new FileInputStream(wrapperProps)) {
                wrapper.load(is);
            } catch (IOException ex) {

            }
            String distUrlProp = wrapper.getProperty("distributionUrl"); //NOI18N
            if (distUrlProp != null) {
                try {
                    URI distURL = new URI(distUrlProp);
                    ret = createVersion(distURL);
                } catch (URISyntaxException ex) {
                    // Wrong URL, give up
                }
            }
        }
        return ret;
    }

    public List<NbGradleVersion> availableVersions(boolean releaseOnly) {
        List<NbGradleVersion> ret = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            URL allVersions = new URL("https://services.gradle.org/versions/all"); //NOI18N
            try (InputStreamReader is = new InputStreamReader(allVersions.openStream(), StandardCharsets.UTF_8)) {
                JSONArray versions = (JSONArray) parser.parse(is);
                for (Object o : versions) {
                    JSONObject v = (JSONObject) o;
                    URI downloadURL = new URI((String) v.get("downloadUrl")); //NOI18N
                    boolean snapshot = (Boolean) v.get("snapshot");           //NOI18N
                    boolean nightly = (Boolean) v.get("nightly");             //NOI18N
                    boolean broken = (Boolean) v.get("broken");               //NOI18N
                    String version = (String) v.get("version");               //NOI18N
                    String rcFor = (String) v.get("rcFor");                   //NOI18N
                    if (nightly || broken || snapshot) {
                        continue;
                    }
                    if (!rcFor.isEmpty() && releaseOnly) {
                        continue;
                    }
                    if (GradleVersion.version(version).compareTo(MINIMUM_SUPPORTED_VERSION) < 0) {
                        continue;
                    }
                    ret.add(createVersion(version, downloadURL, rcFor.isEmpty()));
                }
            } catch (ParseException | IOException | URISyntaxException ex) {
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

    public NbGradleVersion createVersion(URI distributionUrl) {
        NbGradleVersion ret = null;
        Matcher m = DIST_VERSION_PATTERN.matcher(distributionUrl.getPath());
        if (m.matches()) {
            String version = m.group(1);
            ret = createVersion(version, distributionUrl, !version.contains("-"));
        }
        return ret;
    }

    public NbGradleVersion createVersion(String version) {
        try {
            URI url = new URI(String.format(DOWNLOAD_URI, version));
            return createVersion(version, url, !version.contains("-"));
        } catch (URISyntaxException ex) {
            //Should not really happen.
        }
        return null;
    }

    private NbGradleVersion createVersion(String version, URI url, boolean b) {
        NbGradleVersion ret = versions.get(url);
        if (ret == null) {
            ret = new NbGradleVersion(version, url, b);
            versions.put(url, ret);
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

    public final class NbGradleVersion implements Comparable<NbGradleVersion>{

        public static final String PROP_AVAILABLE = "available"; //NOI18N
        final GradleVersion version;
        final URI downloadLocation;
        final boolean release;
        private PropertyChangeSupport pcs;

        private NbGradleVersion(String version, URI downloadLocation, boolean release) {
            this.version = GradleVersion.version(version);
            this.downloadLocation = downloadLocation;
            this.release = release;
        }

        public GradleVersion getVersion() {
            return version;
        }

        public URI getDownloadLocation() {
            return downloadLocation;
        }

        public boolean isRelease() {
            return release;
        }

        public boolean isCompatibleWithSystemJava() {
            return  JAVA_VERSION < 11 ? true : version.compareTo(GradleVersion.version("4.10.2")) >= 0; //NOI18N
        }

        public boolean isAvailable() {
            File distDir = distributionDir();
            return (distDir != null) && distDir.isDirectory();
        }

        public boolean isBlackListed() {
            return VERSION_BLACKLIST.contains(version.getVersion());
        }

        @Messages("TIT_GradleInstall=Install Gradle")
        public boolean install() {
            if (!isAvailable()) {
                if (GradleSettings.getDefault().isSilentInstall()) {
                    RP.post(new DownloadTask(this), 500);
                    return true;
                } else {
                    GradleInstallPanel panel = new GradleInstallPanel(version.getVersion());
                    DialogDescriptor dd = new DialogDescriptor(panel,
                            Bundle.TIT_GradleInstall(),
                            true,
                            DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION,
                            null
                    );
                    if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                        GradleSettings.getDefault().setSilentInstall(panel.isSilentInstall());
                        RP.post(new DownloadTask(this), 500);
                        return true;
                    }
                }
            }
            return false;
        }

        public File distributionDir() {
            File distDir = distributionBaseDir();
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

        private File distributionBaseDir() {
            WrapperConfiguration conf = new WrapperConfiguration();
            conf.setDistribution(downloadLocation);
            PathAssembler pa = new PathAssembler(gradleUserHome);
            PathAssembler.LocalDistribution dist = pa.getDistribution(conf);
            return dist.getDistributionDir();
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
            if ((obj != null) && (obj instanceof NbGradleVersion)) {
                return version.equals(((NbGradleVersion) obj).version);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return version.getVersion();
        }

        @Override
        public int compareTo(NbGradleVersion o) {
            return version.compareTo(o.getVersion());
        }

    }

    private class DownloadTask implements Runnable, IDownload {

        private final NbGradleVersion version;
        private final ProgressHandle handle;
        private final Notification notification;

        @Messages({
            "# {0} - The downloading GradleVersion ",
            "TIT_Download_Gradle=Downloading {0}",
            "# {0} - The downloading GradleVersion ",
            "MSG_Download_Gradle={0} is being downloaded and installed."
        })
        public DownloadTask(NbGradleVersion version) {
            this.version = version;
            handle = ProgressHandleFactory.createSystemHandle(Bundle.TIT_Download_Gradle(version.getVersion()));
            notification = NotificationDisplayer.getDefault().notify(
                    Bundle.TIT_Download_Gradle(version.getVersion()),
                    NbGradleProject.getIcon(),
                    Bundle.MSG_Download_Gradle(version.getVersion()),
                    null,
                    NotificationDisplayer.Priority.NORMAL,
                    NotificationDisplayer.Category.INFO);
        }

        @Messages({
            "# {0} - The downloading GradleVersion ",
            "TIT_Install_Gradle_Failed=Failed installing {0}",
        })
        @Override
        public void run() {
            try {
                WrapperConfiguration conf = new WrapperConfiguration();
                conf.setDistribution(version.getDownloadLocation());
                PathAssembler pa = new PathAssembler(gradleUserHome);
                Install install = new Install(new Logger(true), this, pa);
                install.createDist(conf);
                version.fireVersionAvailable();
            } catch (Exception ex) {
                //Happens if something goes wrong with the download.
                //TODO: Is it ok to let id silently die?
                NotificationDisplayer.getDefault().notify(
                        Bundle.TIT_Install_Gradle_Failed(version.getVersion()),
                        NbGradleProject.getWarningIcon(),
                        ex.getLocalizedMessage(),
                        null,
                        NotificationDisplayer.Priority.HIGH,
                        NotificationDisplayer.Category.WARNING);
            } finally {
                handle.finish();
                notification.clear();
            }
        }

        @Override
        public void download(URI uri, File file) throws Exception {
            URL url = uri.toURL();
            URLConnection conn = url.openConnection();
            byte[] buf = new byte[8192];
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
            }
        }

    }
}
