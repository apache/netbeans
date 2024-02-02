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
package org.netbeans.modules.payara.tooling.data;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Exceptions;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;

/**
 * Payara Platform version.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 * @author Gaurav Gupta
 */
public class PayaraPlatformVersion implements PayaraPlatformVersionAPI, Comparable<PayaraPlatformVersionAPI> {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    private static final Logger LOGGER = Logger.getLogger(PayaraPlatformVersion.class.getName());

    /**
     * Payara Server artifact download url
     */
    private static final String DOWNLOAD_URL = "fish/payara/distributions/payara/%s/payara-%s.zip"; // NOI18N

    public static final String DEFAULT_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/"; // NOI18N
 
    private static final String METADATA_URL = "fish/payara/distributions/payara/maven-metadata.xml"; // NOI18N

    private static final String LOCAL_METADATA_URL = "fish/payara/distributions/payara/maven-metadata-local.xml"; // NOI18N

    private static final String CDDL_LICENSE = "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"; // NOI18N

    public static final PayaraPlatformVersionAPI EMPTY = new PayaraPlatformVersion((short) 0, (short) 0, (short) 0, (short) 0, "", "", "");

    private static PayaraPlatformVersionAPI latestVersion;

    private static final Map<String, PayaraPlatformVersionAPI> versions = new TreeMap<>();

    public static PayaraPlatformVersionAPI getLatestVersion() {
        if (!getVersions().isEmpty()) {
            return latestVersion;
        } else {
            return null;
        }
    }

    public static Map<String, PayaraPlatformVersionAPI> getVersionMap() {
        if (!getVersions().isEmpty()) {
            return Collections.unmodifiableMap(versions);
        } else {
            return Collections.emptyMap();
        }
    }

    public static List<PayaraPlatformVersionAPI> getVersions() {
        if (versions.isEmpty()) {
            String repository = DEFAULT_REPOSITORY_URL;
            boolean readDefaultRepo;
            URL metadata;
            try {
                metadata = new URL(repository + METADATA_URL);
                readDefaultRepo = readVersions(repository, metadata);
            } catch (UnknownHostException ex) {
                readDefaultRepo = false;
                LOGGER.log(Level.INFO, repository, ex);
            } catch (Exception ex) {
                readDefaultRepo = false;
                Exceptions.printStackTrace(ex);
            }
            if (!readDefaultRepo) {
                for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                    try {
                        repository = info.getRepositoryUrl();
                        if (repository != null) {
                            metadata = new URL(repository + METADATA_URL);
                        } else {
                            repository = new File(info.getRepositoryPath() + File.separator).toURI().toString();
                            if(!new File(repository + LOCAL_METADATA_URL).exists()) {
                                continue;
                            }
                            metadata = new URL(repository + LOCAL_METADATA_URL);
                        }
                        if (readVersions(repository, metadata)) {
                            break;
                        }
                    } catch (UnknownHostException ex) {
                        LOGGER.log(Level.INFO, repository, ex);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

        }
        return new ArrayList<>(versions.values());
    }
    
    
    private static boolean readVersions(String repository, URL metadata) throws Exception {
        try (InputStream input = metadata.openStream()) {
            MetadataXpp3Reader reader = new MetadataXpp3Reader();
            Metadata data = reader.read(new InputStreamReader(input));
            versions.clear();
            for (String version : data.getVersioning().getVersions()) {
                if (version.contains("Alpha") || version.contains("Beta") || version.contains("SNAPSHOT")) { // NOI18N
                    continue;
                }
                PayaraPlatformVersionAPI payaraVersion = PayaraPlatformVersion.toValue(repository, version);
                versions.put(version, payaraVersion);
                if (version.equals(data.getVersioning().getLatest())) {
                    latestVersion = payaraVersion;
                }
            }
            if (!versions.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static List<PayaraPlatformVersionAPI> getVersions(String repository) {
            
        return new ArrayList<>(versions.values());
    }

    @CheckForNull
    public static PayaraPlatformVersionAPI toValue(
            @NonNull final String versionStr) {
        return toValue(DEFAULT_REPOSITORY_URL, versionStr);
    }
            
    /**
     * Returns a <code>PayaraPlatformVersionAPI</code> with a value represented by the
     * specified <code>String</code>. The <code>PayaraPlatformVersionAPI</code> returned
     * represents existing value only if specified <code>String</code> matches
     * any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p/>
     * @param repository Value containing maven repository url <code>String</code>
     * @param versionStr Value containing version <code>String</code>
     * representation.
     * @return <code>PayaraPlatformVersionAPI</code> value represented by
     * <code>String</code> or <code>null</code> if value was not recognized.
     */
    @CheckForNull
    public static PayaraPlatformVersionAPI toValue(
            @NonNull final String repository, 
            @NonNull final String versionStr) {
        if(versionStr.trim().isEmpty()) {
            return EMPTY;
        }
        PayaraPlatformVersionAPI version
                = versions.get(versionStr.toUpperCase(Locale.ENGLISH));
        if (version == null) {
            String[] versionComps = versionStr.split(SEPARATOR_PATTERN);

            short major = Short.valueOf(versionComps[0]);
            short minor = Short.valueOf(versionComps[1]);
            short update = 0, build = 0;
            if (versionComps.length > 2) {
                update = Short.valueOf(versionComps[2]);
            }
            if (versionComps.length > 3) {
                build = Short.valueOf(versionComps[3]);
            }
            version = new PayaraPlatformVersion(
                    major, minor, update, build,
                    major >= 5 ? "deployer:pfv5ee8" : "deployer:pfv4ee7",
                    repository, versionStr
            );
        }
        return version;
    }
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Major version number.
     */
    private final short major;

    /**
     * Minor version number.
     */
    private final short minor;

    /**
     * Update version number.
     */
    private final short update;

    /**
     * Build version number.
     */
    private final short build;

    private final String uriFragment;

    private final String indirectUrl;

    private final String directUrl;

    private final String value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Constructs an instance of Payara server version.
     * <p/>
     * @param major Major version number.
     * @param minor Minor version number.
     * @param update Update version number.
     * @param build Build version number.
     */
    private PayaraPlatformVersion(final short major, final short minor,
            final short update, final short build, String uriFragment,
            final String repository, final String value) {
        this.major = major;
        this.minor = minor;
        this.update = update;
        this.build = build;
        this.uriFragment = uriFragment;
        this.value = value;
        this.indirectUrl = null;
        this.directUrl = repository + String.format(DOWNLOAD_URL, value, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Get major version number.
     *
     * @return Major version number.
     */
    @Override
    public short getMajor() {
        return major;
    }

    /**
     * Get minor version number.
     * <p/>
     * @return Minor version number.
     */
    @Override
    public short getMinor() {
        return minor;
    }

    /**
     * Get update version number.
     * <p/>
     * @return Update version number.
     */
    @Override
    public short getUpdate() {
        return update;
    }

    /**
     * Get build version number.
     * <p/>
     * @return Build version number.
     */
    @Override
    public short getBuild() {
        return build;
    }

    @Override
    public String getUriFragment() {
        return uriFragment;
    }

    @Override
    public String getDirectUrl() {
        return directUrl;
    }

    @Override
    public String getIndirectUrl() {
        return indirectUrl;
    }

    @Override
    public String getLicenseUrl() {
        return CDDL_LICENSE;
    }

    @Override
    public boolean isMinimumSupportedVersion() {
        return major >= 4;
    }

    @Override
    public boolean isEE7Supported() {
        return major >= 4;
    }

    @Override
    public boolean isEE8Supported() {
        return major >= 5;
    }

    @Override
    public boolean isEE9Supported() {
        return major >= 6;
    }

    @Override
    public boolean isEE10Supported() {
        return major >= 6;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Compare major and minor parts of version number <code>String</code>s.
     * <p/>
     * @param version Payara server version to compare with this object.
     * @return Value of <code>true</code> when major and minor parts of version
     * numbers are the same or <code>false</code> otherwise.
     */
    @Override
    public boolean equalsMajorMinor(final PayaraPlatformVersionAPI version) {
        if (version == null) {
            return false;
        } else {
            return this.major == version.getMajor() && this.minor == version.getMinor();
        }
    }

    /**
     * Compare all parts of version number <code>String</code>s.
     * <p/>
     * @param version Payara server version to compare with this object.
     * @return Value of <code>true</code> when all parts of version numbers are
     * the same or <code>false</code> otherwise.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override
    public boolean equals(final PayaraPlatformVersionAPI version) {
        if (version == null) {
            return false;
        } else {
            return this.major == version.getMajor()
                    && this.minor == version.getMinor()
                    && this.update == version.getUpdate()
                    && this.build == version.getBuild();
        }
    }

    /**
     * Convert <code>PayaraPlatformVersionAPI</code> value to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Convert <code>PayaraPlatformVersionAPI</code> value to <code>String</code>
     * containing all version numbers.
     * <p/>
     * @return A <code>String</code> representation of the value of this object
     * containing all version numbers.
     */
    public String toFullString() {
        StringBuilder sb = new StringBuilder(8);
        sb.append(Integer.toString(major));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(minor));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(update));
        sb.append(SEPARATOR);
        sb.append(Integer.toString(build));
        return sb.toString();
    }

    @Override
    public int compareTo(PayaraPlatformVersionAPI o) {
        return Comparator.comparing(PayaraPlatformVersionAPI::getMajor)
                .thenComparing(PayaraPlatformVersionAPI::getMinor)
                .thenComparingInt(PayaraPlatformVersionAPI::getUpdate)
                .thenComparingInt(PayaraPlatformVersionAPI::getBuild)
                .compare(this, o);
    }

}
