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
package org.netbeans.modules.gradle.api.execute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
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
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This class helps organizing the {@link GradleDistribution}-s used in the IDE.
 *
 * @since 2.4
 * @author lkishalmi
 */
public final class GradleDistributionManager {

    private static final RequestProcessor RP = new RequestProcessor("Gradle Installer", 1); //NOI18N

    private static final String DOWNLOAD_URI = "https://services.gradle.org/distributions/gradle-%s-%s.zip"; //NOI18N
    private static final Pattern DIST_VERSION_PATTERN = Pattern.compile(".*(gradle-(\\d+\\.\\d+.*))-(bin|all)\\.zip"); //NOI18N
    private static final Set<String> VERSION_BLACKLIST = Set.of("2.3", "2.13"); //NOI18N
    private static final Map<File, GradleDistributionManager> CACHE = new WeakHashMap<>();
    private static final GradleVersion MINIMUM_SUPPORTED_VERSION = GradleVersion.version("3.0"); //NOI18N
    private static final GradleVersion[] JDK_COMPAT = new GradleVersion[]{
        GradleVersion.version("4.2.1"), // JDK-9
        GradleVersion.version("4.7"), // JDK-10
        GradleVersion.version("4.10.2"), // JDK-11
        GradleVersion.version("5.4"), // JDK-12
        GradleVersion.version("6.0"), // JDK-13
        GradleVersion.version("6.3"), // JDK-14
        GradleVersion.version("6.7"), // JDK-15
        GradleVersion.version("7.0"), // JDK-16
        GradleVersion.version("7.3"), // JDK-17
        GradleVersion.version("7.5"), // JDK-18
        GradleVersion.version("7.6"), // JDK-19
        GradleVersion.version("8.3"), // JDK-20
        GradleVersion.version("8.5"), // JDK-21
        GradleVersion.version("8.8"), // JDK-22
        GradleVersion.version("8.10"),// JDK-23
        GradleVersion.version("8.14"),// JDK-24
    };

    private static final GradleVersion LAST_KNOWN_GRADLE = GradleVersion.version("8.14"); //NOI18N

    final File gradleUserHome;

    private GradleDistributionManager(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome;
    }

    /**
     * Return a {@link GradleDistributionManager} for the given Gradle user
     * home.
     *
     * @param gradleUserHome
     * @return
     */
    public static GradleDistributionManager get(File gradleUserHome) {
        File home = gradleUserHome != null ? gradleUserHome : GradleSettings.getDefault().getGradleUserHome();
        GradleDistributionManager ret = CACHE.get(home);
        if (ret == null) {
            ret = new GradleDistributionManager(home);
            CACHE.put(home, ret);
        }
        return ret;
    }

    /**
     * Return a {@link GradleDistributionManager} for the Gradle user
     * home, set in the IDE.
     * 
     * @return the GradleDistributionManager for the default Gradle user home.
     * @since 2.23
     */
    public static GradleDistributionManager get() {
        return GradleDistributionManager.get(null);
    }

    /**
     * Create a {@link GradleDistribution} from a manually downloaded and
     * unpacked directory.
     *
     * @param distDir the directory where Gradle has been installed manually
     * @return the created GradleDistribution object.
     * @throws IOException when the provided directory does not seem to be a
     * Gradle distribution.
     */
    public GradleDistribution distributionFromDir(File distDir) throws IOException {
        File lib = new File(distDir, "lib"); //NO18N
        File[] gradleLauncher = lib.listFiles((dir, name) -> {
            return name.startsWith("gradle-launcher-") && name.endsWith(".jar"); //NOI18N
        });
        if ((gradleLauncher == null) || (gradleLauncher.length != 1)) {
            throw new FileNotFoundException(lib.getAbsolutePath() + "lib/gradle-launcher-xxxx.jar not found or ambigous!"); //NOI18N
        }
        JarFile launcherJar = new JarFile(gradleLauncher[0]);
        String version = launcherJar.getManifest().getMainAttributes().getValue("Implementation-Version"); //NOI18N
        return new GradleDistribution(distDir, null, version);
    }

    /**
     * Create a {@link GradleDistribution} from a simple version string like
     * <code>"6.3"</code>. The returned distribution might be not available but
     * it can be downloaded and installed with the
     * {@link GradleDistribution#install()} method. This method sets the
     * standard download URI, if the <code>withSources</code> is set to
     * <code>false</code> then the binary only URI would be set.
     *
     * @param version the Gradle version
     * @param withSources choose between the 'all' and 'bin' distribution
     * @return the created GradleDistribution object.
     */
    public GradleDistribution distributionFromVersion(String version, boolean withSources) {
        try {
            URI uri = new URI(String.format(DOWNLOAD_URI, version, withSources ? "all" : "bin")); //NOI18N
            return new GradleDistribution(distributionBaseDir(uri, version), uri, version);
        } catch (URISyntaxException ex) {
            //This shall not happen;
            return null;
        }
    }

    /**
     * Create a {@link GradleDistribution} from a simple version string like
     * <code>"6.3"</code>. The returned distribution might be not available but
     * it can be downloaded and installed with the
     * {@link GradleDistribution#install()} method. This method sets the
     * standard download URI for the binary distribution package (no source code
     * attached).
     *
     * @param version the Gradle version
     * @return the created GradleDistribution object.
     */
    public GradleDistribution distributionFromVersion(String version) {
        return distributionFromVersion(version, false);
    }

    /**
     * Create a {@link GradleDistribution} from a Gradle root project directory
     * which contains a Gradle wrapper. The wrapper properties file is expected
     * to be at <code>gradle/wrapper/gradle-wrapper.properties</code>.
     *
     * @param gradleProjectRoot the directory of the root Gradle project.
     * @return the created GradleDistribution object.
     * @throws IOException if the wrapper properties file not found or cannot be
     *         read for some reason.
     * @throws URISyntaxException when the <code>distributionUrl</code>
     *         property is missing, has URI syntax problem or the version of
     *         the Gradle distribution cannot be determined form it.
     */
    public GradleDistribution distributionFromWrapper(File gradleProjectRoot) throws IOException, URISyntaxException {
        URI uri = getWrapperDistributionURI(gradleProjectRoot);
        Matcher m = DIST_VERSION_PATTERN.matcher(uri.getPath());
        if (m.matches()) {
            String version = m.group(2);
            return new GradleDistribution(distributionBaseDir(uri, version), uri, version);
        } else {
            throw new URISyntaxException(uri.getPath(), "Cannot get the Gradle distribution version from the URI"); //NOI18N
        }
    }

    /**
     * Retrieves a normalized URI for the Gradle Wrapper distribution for the
     * given root project directory.
     *
     * @param rootDir the root project directory
     * @return the normalized URI of the Gradle wrapper distribution.
     * @throws IOException if there is no <code>gradle-wrapper.properties</code>
     *         or it cannot be read.
     * @throws URISyntaxException if the <code>distributionUrl</code> is missing
     *         or cannot be resolved to a valid URI.
     */
    public static URI getWrapperDistributionURI(File rootDir) throws IOException, URISyntaxException {
        URI ret;

        File wrapperProps =  new File(rootDir, GradleFiles.WRAPPER_PROPERTIES);
        if (wrapperProps.isFile() && wrapperProps.canRead()) {
            Properties wrapper = new Properties();
            try (FileInputStream is = new FileInputStream(wrapperProps)) {
                wrapper.load(is);
            } catch (IOException ex) {
                throw ex;
            }
            String distUrlProp = wrapper.getProperty("distributionUrl"); //NOI18N
            if (distUrlProp != null) {
                ret = new URI(distUrlProp);
                if (ret.getScheme() == null) {
                    ret = wrapperProps.getParentFile().toPath().resolve(distUrlProp).normalize().toUri();
                }
            } else {
                throw new URISyntaxException("", "No distributionUrl property found in: " + wrapperProps.getAbsolutePath()); //NOI18N
            }
        } else {
            throw new FileNotFoundException("Gradle Wrapper properties not found at: " + wrapperProps.getAbsolutePath()); //NOI18N
        }
        return ret;
    }

    /**
     * Create a {@link GradleDistribution} from the current (latest) Gradle
     * release available from the Gradle site. This method uses the
     * <a href="https://services.gradle.org/versions/current">https://services.gradle.org/versions/current</a>
     * web service to query the latest available version.
     *
     * @return the current Gradle distribution
     * @throws java.io.IOException if information on the current Gradle release
     * cannot be accessed
     */
    public GradleDistribution currentDistribution() throws IOException {
        JSONParser parser = new JSONParser();
        URL versionsCurrent = URI.create("https://services.gradle.org/versions/current").toURL(); //NOI18N
        try (InputStreamReader is = new InputStreamReader(versionsCurrent.openStream(), StandardCharsets.UTF_8)) {
            JSONObject current = (JSONObject) parser.parse(is);
            URI downloadURL = new URI((String) current.get("downloadUrl")); //NOI18N
            String version = (String) current.get("version");
            return new GradleDistribution(distributionBaseDir(downloadURL, version), downloadURL, version);
        } catch (ParseException | URISyntaxException | ClassCastException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Create a {@link GradleDistribution} from the Gradle version distributed
     * with the Gradle Tooling of the IDE. This should be the most IDE compatible
     * version, so it can be used as a fallback.
     *
     * @return the default Gradle distribution matches the IDE Gradle tooling.
     */
    public GradleDistribution defaultDistribution() {
        return distributionFromVersion(GradleVersion.current().getVersion());
    }

    /**
     * Lists all the {@link GradleDistribution}s available on the Gradle site and
     * supported by the IDE. This method uses the
     * <a href="https://services.gradle.org/versions/all">https://services.gradle.org/versions/all</a>
     * web service to download the list of available versions.
     * @param releaseOnly list only the released versions
     *                    (release candidates and milestones included).
     * @return the list of available Gradle distributions from the Gradle site.
     * @throws IOException if downloading the list would fail.
     */
    public List<GradleDistribution> availableDistributions(boolean releaseOnly) throws IOException {
        List<GradleDistribution> ret = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            URL allVersions = URI.create("https://services.gradle.org/versions/all").toURL(); //NOI18N
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
                    ret.add(new GradleDistribution(distributionBaseDir(downloadURL, version), downloadURL, version));
                }
            } catch (ParseException | URISyntaxException ex) {
                //TODO: Shall we do something about this?
            } catch (IOException iex) {
                throw iex;
            }
        } catch (MalformedURLException ex) {
            //Shall not happen with hardcoded URL
        }
        return ret;
    }

    /**
     * Lists all the {@link GradleDistribution}s available on the Gradle Home
     * of this distribution manager. It looks for the <code>$GRADLE_HOME/wrapper/dists</code>
     * directory for already downloaded distributions.
     * @return the list of available Gradle distributions from the Gradle Home.
     * @since 2.10
     */
    public List<GradleDistribution> availableLocalDistributions() {
        List<GradleDistribution> ret = new ArrayList<>();
        Path dists = gradleUserHome.toPath().resolve("wrapper").resolve("dists"); //NOI18N
        if (Files.isDirectory(dists)) {
            try {
                Files.walkFileTree(dists, EnumSet.noneOf(FileVisitOption.class), 2, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                        String fname = f.getFileName().toString();
                        Matcher m = DIST_VERSION_PATTERN.matcher(fname);
                        if (m.matches()) {
                            Path dist = f.resolveSibling(m.group(1));
                            if (Files.isDirectory(dist)) {
                                try {
                                    GradleDistribution d = distributionFromDir(dist.toFile());
                                    if (GradleVersion.version(d.getVersion()).compareTo(MINIMUM_SUPPORTED_VERSION) >= 0) {
                                        ret.add(d);
                                    }
                                } catch (IOException ex) {
                                    // This might be a broken distribution
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                //Do nothing if we fail to scan the files
            }
        }
        return ret;
    }
    
    File distributionBaseDir(URI downloadLocation, String version) {
        WrapperConfiguration conf = new WrapperConfiguration();
        conf.setDistribution(downloadLocation);
        PathAssembler pa = new PathAssembler(gradleUserHome, null);
        PathAssembler.LocalDistribution dist = pa.getDistribution(conf);
        return new File(dist.getDistributionDir(), "gradle-" + version);
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static final class GradleVersionRange {

        public final GradleVersion lowerBound;
        public final GradleVersion upperBound;
        public static final GradleVersionRange UNBOUNDED = new GradleVersionRange(null, null);

        GradleVersionRange(GradleVersion lowerBound, GradleVersion upperBound) {
            if ((lowerBound != null) && (upperBound != null) && (lowerBound.compareTo(upperBound) >= 0)) {
                throw new IllegalArgumentException("Invalid version range: [" + lowerBound + ", " + upperBound + ")");
            }
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public boolean contains(GradleVersion ver) {
            return ((lowerBound == null) || (lowerBound.compareTo(ver) <= 0)) && ((upperBound == null) || (upperBound.compareTo(ver) > 0));
        }

        public boolean contains(String ver) {
            return contains(GradleVersion.version(ver));
        }

        public static GradleVersionRange from(GradleVersion lowerBound) {
            return new GradleVersionRange(lowerBound, null);
        }

        public static GradleVersionRange from(String lowerBound) {
            return from(GradleVersion.version(lowerBound));
        }

        public static GradleVersionRange until(GradleVersion upperBound) {
            return new GradleVersionRange(null, upperBound);
        }

        public static GradleVersionRange until(String upperBound) {
            return until(GradleVersion.version(upperBound));
        }

        public static GradleVersionRange range(GradleVersion lowerRange, GradleVersion upperRange) {
            return new GradleVersionRange(lowerRange, upperRange);
        }

        public static GradleVersionRange range(GradleVersion lowerRange, String upperRange) {
            return range(lowerRange, GradleVersion.version(upperRange));
        }

        public static GradleVersionRange range(String lowerRange, GradleVersion upperRange) {
            return new GradleVersionRange(GradleVersion.version(lowerRange), upperRange);
        }

        public static GradleVersionRange range(String lowerRange, String upperRange) {
            return new GradleVersionRange(GradleVersion.version(lowerRange), GradleVersion.version(upperRange));
        }
    }

    /**
     * This object represents a Gradle distribution in NetBeans combining the
     * following four attributes:
     * <ul>
     *   <li>Gradle user home</li>
     *   <li>Distribution directory</li>
     *   <li>Gradle version</li>
     *   <li>Gradle distribution URI</li>
     * </ul>
     */
    public final class GradleDistribution implements Comparable<GradleDistribution> {

        final File distributionDir;
        final URI distributionURI;
        final GradleVersion version;

        private GradleDistribution(File distributionDir, URI distributionURL, String version) {
            this.distributionDir = distributionDir;
            this.distributionURI = distributionURL;
            this.version = GradleVersion.version(version);
        }

        /**
         * The Gradle user home directory of this distribution. Inherited through
         * the {@link GradleDistributionManager}.
         * @return the Gradle user home directory of this distribution
         */
        public File getGradleUserHome() {
            return gradleUserHome;
        }

        /**
         * The Gradle distribution directory. May or may not exists yet.
         * If it does not exist then the {@link #getDistributionURI()} must
         * return a valid URI to the distribution source.
         *
         * @return the Gradle distribution directory
         */
        public File getDistributionDir() {
            return distributionDir;
        }

        /**
         * The Gradle distribution URI. It may return <code>null</code> if this
         * is a manually installed distribution. In that case the
         * {@link #getDistributionDir()} shall return an existing distribution
         * directory.
         *
         * @return the Gradle distribution URI
         */
        public URI getDistributionURI() {
            return distributionURI;
        }

        /**
         * The Gradle version of this distribution.
         * @return the Gradle version of this distribution
         */
        public String getVersion() {
            return version.getVersion();
        }

        /**
         * Checks if this Gradle distribution is compatible with the given
         * major version of Java. Java 1.6, 1.7 and 1.8 are treated as major
         * version 6, 7, and 8.
         * <p>
         * NetBeans uses a built in fixed list of compatibility matrix. That
         * means it might not know about the compatibility of newer Gradle
         * versions. Optimistic bias would return {@code true} on these
         * versions form 2.37. 
         * </p>
         * @param jdkMajorVersion the major version of the JDK
         * @return <code>true</code> if this version is supported with that JDK.
         */
        public boolean isCompatibleWithJava(int jdkMajorVersion) {
            
            // Optimistic bias, if the GradleVersion is newer than the last NB
            // knows, we say it's compatible with any JDK
            return LAST_KNOWN_GRADLE.compareTo(version.getBaseVersion()) < 0
                    || jdkMajorVersion <= lastSupportedJava();
        }

        /**
         * Returns the newest major JDK version that is supported with this
         * distribution.
         * @return the newest major JDK version that is supported with this
         *    distribution.
         * @since 2.22
         */
        public int lastSupportedJava() {
            int i = JDK_COMPAT.length - 1;
            //Make sure that even RC-s are considered to be compatible.
            GradleVersion baseVersion = version.getBaseVersion();
            while ((i >= 0) && baseVersion.compareTo(JDK_COMPAT[i]) < 0) {
                i--;
            }
            return i + 9;
        }
        
        /**
         * Checks if this Gradle distribution is compatible the NetBeans
         * runtime JDK.
         *
         * @return <code>true</code>.
         * @deprecated shall be no reason to be used.
         */
        @Deprecated
        public boolean isCompatibleWithSystemJava() {
            return true;
        }

        /**
         * Checks if this distribution is downloaded and available to use.
         * Well the current check is based on if the distribution directory
         * exists or not.
         *
         * @return if this distribution is available for use.
         */
        public boolean isAvailable() {
            return distributionDir.isDirectory();
        }

        /**
         * Checks if this distribution has known issues to be used with NetBeans.
         * While these versions can be used, their usage are not recommended..
         *
         * @return if this distribution has compatibility issues with NetBeans.
         */
        public boolean isBlackListed() {
            return VERSION_BLACKLIST.contains(version.getVersion());
        }

        /**
         * Start to download and install this distribution from its
         * distribution URI to its distribution directory asynchronous. It returns
         * <code>null</code> if this distribution is available.
         * <p>
         * The install task has UI bindings in form of a IDE progress bar and 
         * notification entry on the downloading of the distribution.
         *
         * @return the {@link Future} of the install task or <code>null</code>
         *         if this distribution is already installed.
         */
        public Future<Void> install() {
            return isAvailable() ? null : RP.schedule(new DownloadTask(this), 500, TimeUnit.MILLISECONDS);
        }

        /**
         * This method only compare the version attribute. It could happen that
         * two distribution are equal using this method when their versions are
         * equal, though the {@link #equals(java.lang.Object)} return false as
         * that is based on comparing the distribution directory.
         * @param o the GradleDistribution to compare with.
         * @return a signed value comparing the version attribute of this and
         *         the specified distribution.
         */
        @Override
        public int compareTo(GradleDistribution o) {
            return version.compareTo(o.version);
        }


        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.distributionDir);
            return hash;
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
            final GradleDistribution other = (GradleDistribution) obj;
            return Objects.equals(this.distributionDir, other.distributionDir);
        }

        @Override
        public String toString() {
            return "GradleDistribution{" + "gradleUserHome=" + gradleUserHome + ", distributionDir=" + distributionDir + ", distributionURI=" + distributionURI + ", version=" + version + '}';
        }

    }

    private class DownloadTask implements Callable<Void>, IDownload {

        private final GradleDistribution dist;
        private final ProgressHandle handle;
        private final Notification notification;

        @NbBundle.Messages({
            "# {0} - The downloading GradleVersion ",
            "TIT_Download_Gradle=Downloading {0}",
            "# {0} - The downloading GradleVersion ",
            "MSG_Download_Gradle=Gradle {0} is being downloaded and installed."
        })
        public DownloadTask(GradleDistribution dist) {
            this.dist = dist;
            handle = ProgressHandle.createSystemHandle(Bundle.TIT_Download_Gradle(dist.getVersion()), null);
            notification = NotificationDisplayer.getDefault().notify(
                    Bundle.TIT_Download_Gradle(dist.getVersion()),
                    NbGradleProject.getIcon(),
                    Bundle.MSG_Download_Gradle(dist.getVersion()),
                    null,
                    NotificationDisplayer.Priority.NORMAL,
                    NotificationDisplayer.Category.INFO);
        }

        @NbBundle.Messages({
            "# {0} - The downloading GradleVersion ",
            "TIT_Install_Gradle_Failed=Failed installing Gradle {0}",})
        @Override
        public Void call() throws Exception {
            try {
                WrapperConfiguration conf = new WrapperConfiguration();
                conf.setDistribution(dist.getDistributionURI());
                PathAssembler pa = new PathAssembler(gradleUserHome, null);
                Install install = new Install(new Logger(true), this, pa);
                install.createDist(conf);
            } catch (Exception ex) {
                //Happens if something goes wrong with the download.
                //TODO: Is it ok to let id silently die?
                NotificationDisplayer.getDefault().notify(
                        Bundle.TIT_Install_Gradle_Failed(dist.getVersion()),
                        NbGradleProject.getWarningIcon(),
                        ex.getLocalizedMessage(),
                        null,
                        NotificationDisplayer.Priority.HIGH,
                        NotificationDisplayer.Category.WARNING);
                throw ex;
            } finally {
                handle.finish();
                notification.clear();
            }
            return null;
        }

        @Override
        @SuppressWarnings("NestedAssignment")
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
