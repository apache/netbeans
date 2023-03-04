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
package org.netbeans.modules.payara.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.netbeans.modules.payara.common.parser.TreeParser;
import org.netbeans.modules.payara.common.wizards.ServerWizardIterator;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 * @author Gaurav Gupta
 */
@Deprecated
public enum ServerDetails implements PayaraPlatformVersionAPI {
    //add new version
    /**
     * details for an instance of Payara Server 4.1.144
     */
    PAYARA_SERVER_4_1_144(
            PayaraVersion.PF_4_1_144,
            "deployer:pfv4ee7", // NOI18N
            41144,
            false
    ),
    /**
     * details for an instance of Payara Server 4.1.151
     */
    PAYARA_SERVER_4_1_151(
            PayaraVersion.PF_4_1_151,
            "deployer:pfv4ee7", // NOI18N
            41151,
            false
    ),
    /**
     * details for an instance of Payara Server 4.1.152
     */
    PAYARA_SERVER_4_1_152(
            PayaraVersion.PF_4_1_152,
            "deployer:pfv4ee7", // NOI18N
            41152,
            false
    ),
    /**
     * details for an instance of Payara Server 4.1.153
     */
    PAYARA_SERVER_4_1_153(
            PayaraVersion.PF_4_1_153,
            "deployer:pfv4ee7", // NOI18N
            41153,
            false
    ),
    /**
     * details for an instance of Payara Server 4.1.1.154
     */
    PAYARA_SERVER_4_1_1_154(
            PayaraVersion.PF_4_1_1_154,
            "deployer:pfv4ee7", // NOI18N
            411154,
            false
    ),
    /**
     * details for an instance of Payara Server 4.1.1.161
     */
    PAYARA_SERVER_4_1_1_161(
            PayaraVersion.PF_4_1_1_161,
            "deployer:pfv4ee7", // NOI18N
            411161
    ),
    /**
     * details for an instance of Payara Server 4.1.1.162
     */
    PAYARA_SERVER_4_1_1_162(
            PayaraVersion.PF_4_1_1_162,
            "deployer:pfv4ee7", // NOI18N
            411162
    ),
    /**
     * details for an instance of Payara Server 4.1.1.163
     */
    PAYARA_SERVER_4_1_1_163(
            PayaraVersion.PF_4_1_1_163,
            "deployer:pfv4ee7", // NOI18N
            411163
    ),
    /**
     * details for an instance of Payara Server 4.1.1.164
     */
    PAYARA_SERVER_4_1_1_164(
            PayaraVersion.PF_4_1_1_164,
            "deployer:pfv4ee7", // NOI18N
            411164
    ),
    /**
     * details for an instance of Payara Server 4.1.1.171
     */
    PAYARA_SERVER_4_1_1_171(
            PayaraVersion.PF_4_1_1_171,
            "deployer:pfv4ee7", // NOI18N
            411171
    ),
    /**
     * details for an instance of Payara Server 4.1.2.172
     */
    PAYARA_SERVER_4_1_2_172(
            PayaraVersion.PF_4_1_2_172,
            "deployer:pfv4ee7", // NOI18N
            412172
    ),
    /**
     * details for an instance of Payara Server 4.1.2.173
     */
    PAYARA_SERVER_4_1_2_173(
            PayaraVersion.PF_4_1_2_173,
            "deployer:pfv4ee7", // NOI18N
            412173
    ),
    /**
     * details for an instance of Payara Server 4.1.2.174
     */
    PAYARA_SERVER_4_1_2_174(
            PayaraVersion.PF_4_1_2_174,
            "deployer:pfv4ee7", // NOI18N
            412174
    ),
    /**
     * details for an instance of Payara Server 4.1.2.181
     */
    PAYARA_SERVER_4_1_2_181(
            PayaraVersion.PF_4_1_2_181,
            "deployer:pfv4ee7", // NOI18N
            412181
    ),
    /**
     * details for an instance of Payara Server 5.181
     */
    PAYARA_SERVER_5_181(
            PayaraVersion.PF_5_181,
            "deployer:pfv5ee8", // NOI18N
            5181
    ),
    /**
     * details for an instance of Payara Server 5.182
     */
    PAYARA_SERVER_5_182(
            PayaraVersion.PF_5_182,
            "deployer:pfv5ee8", // NOI18N
            5182
    ),
    /**
     * details for an instance of Payara Server 5.183
     */
    PAYARA_SERVER_5_183(
            PayaraVersion.PF_5_183,
            "deployer:pfv5ee8", // NOI18N
            5183
    ),
    /**
     * details for an instance of Payara Server 5.184
     */
    PAYARA_SERVER_5_184(
            PayaraVersion.PF_5_184,
            "deployer:pfv5ee8", // NOI18N
            5184
    ),
    /**
     * details for an instance of Payara Server 5.191
     */
    PAYARA_SERVER_5_191(
            PayaraVersion.PF_5_191,
            "deployer:pfv5ee8", // NOI18N
            5191
    ),
    /**
     * details for an instance of Payara Server 5.192
     */
    PAYARA_SERVER_5_192(
            PayaraVersion.PF_5_192,
            "deployer:pfv5ee8", // NOI18N
            5192
    ),
    /**
     * details for an instance of Payara Server 5.193
     */
    PAYARA_SERVER_5_193(
            PayaraVersion.PF_5_193,
            "deployer:pfv5ee8", // NOI18N
            5193
    ),
    /**
     * details for an instance of Payara Server 5.194
     */
    PAYARA_SERVER_5_194(
            PayaraVersion.PF_5_194,
            "deployer:pfv5ee8", // NOI18N
            5194
    ),
    /**
     * details for an instance of Payara Server 5.201
     */
    PAYARA_SERVER_5_201(
            PayaraVersion.PF_5_201,
            "deployer:pfv5ee8", // NOI18N
            5201
    ),
    /**
     * details for an instance of Payara Server 5.202
     */
    PAYARA_SERVER_5_202(
            PayaraVersion.PF_5_202,
            "deployer:pfv5ee8", // NOI18N
            5202,
            false
    );

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported Payara server
     * versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return new ServerWizardIterator(
                Arrays.asList(ServerDetails.values())
        );
    }

    /**
     * Determine the version of the Payara Server installed in a directory
     *
     * @param payaraDir the directory that holds a Payara installation
     * @return -1 if the directory is not a Payara server install
     */
    public static int getVersionFromInstallDirectory(File payaraDir) {
        if (payaraDir == null) {
            return -1;
        }

        PayaraPlatformVersionAPI version
                = ServerUtils.getPlatformVersion(payaraDir.getAbsolutePath());
        Optional<ServerDetails> serverDetails = Optional.empty();
        if (version != null) {
            serverDetails = Arrays
                    .stream(ServerDetails.values())
                    .filter(value -> value.getVersion() == version)
                    .findAny();
        }
        return serverDetails.map(ServerDetails::getVersionInt).orElse(-1);
    }

    /**
     * Determine the version of the Payara Server that wrote the domain.xml file
     *
     * @param domainXml the file to analyze
     * @return -1 if domainXml is null, unreadable or not a directory
     * @throws IllegalStateException if domainXml cannot be parsed
     */
    @Deprecated
    public static int getVersionFromDomainXml(File domainXml) throws IllegalStateException {
        if (null == domainXml || !domainXml.isFile() || !domainXml.canRead()) {
            return -1;
        }
        return hasDefaultConfig(domainXml) ? PAYARA_SERVER_5_194.getVersionInt() : PAYARA_SERVER_5_181.getVersionInt();
    }

    private static boolean hasDefaultConfig(File domainXml) throws IllegalStateException {
        DomainParser dp = new DomainParser();
        List<TreeParser.Path> paths = new ArrayList<>();
        paths.add(new TreeParser.Path("/domain/configs/config", dp)); // NOI18N
        TreeParser.readXml(domainXml, paths);
        return dp.hasDefaultConfig();
    }

    private final PayaraVersion version;
    private final String displayName;
    private final String uriFragment;
    private final String indirectUrl;
    private final String directUrl;
    private final String licenseUrl;
    private final int versionInt;
    private final boolean downloadable;

    private static final String DOWNLOAD_URL = "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/%s/payara-%s.zip"; // NOI18N
    private static final String CDDL_LICENSE = "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"; // NOI18N

    ServerDetails(PayaraVersion version, String uriFragment, int versionInt) {
        this(version, uriFragment, versionInt, true);
    }

    ServerDetails(PayaraVersion version, String uriFragment, int versionInt, boolean downloadable) {
        this.version = version;
        this.displayName = NbBundle.getMessage(ServerDetails.class, "STR_SERVER_NAME", new Object[]{version.toString()});
        this.uriFragment = uriFragment;
        this.versionInt = versionInt;
        this.indirectUrl = null;
        this.directUrl = String.format(DOWNLOAD_URL, version.toString(), version.toString());
        this.licenseUrl = CDDL_LICENSE;
        this.downloadable = downloadable;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getUriFragment() {
        return uriFragment;
    }

    public int getVersionInt() {
        return versionInt;
    }

    public PayaraVersion getVersion() {
        return version;
    }

    public boolean isDownloadable() {
        return downloadable;
    }

    /**
     * Determine if the glassfishDir holds a valid install of this release of
     * Payara Server.
     *
     * @param payaraDir
     * @return true if the glassfishDir holds this particular server version.
     */
    public boolean isInstalledInDirectory(File payaraDir) {
        return getVersionFromInstallDirectory(payaraDir) == this.getVersionInt();
    }

    static class DomainParser extends TreeParser.NodeReader {

        private boolean hasDefaultConfig = false;

        private boolean hasDefaultConfig() {
            return hasDefaultConfig;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String name = attributes.getValue("name"); // NOI18N
            if ("default-config".equals(name)) { // NOI18N
                hasDefaultConfig = true;
            }
        }

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
        return licenseUrl;
    }

    @Override
    public short getMajor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getMinor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getBuild() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toFullString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMinimumSupportedVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEE7Supported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEE8Supported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equalsMajorMinor(PayaraPlatformVersionAPI version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(PayaraPlatformVersionAPI version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
