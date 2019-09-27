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

package org.netbeans.modules.payara.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.payara.common.parser.TreeParser;
import org.netbeans.modules.payara.common.wizards.ServerWizardIterator;
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
public enum ServerDetails {
    //add new version
    /**
     * details for an instance of Payara Server 4.1.144
     */
    PAYARA_SERVER_4_1_144(NbBundle.getMessage(ServerDetails.class, "STR_41144_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            41144,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.144/payara.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.151
     */
    PAYARA_SERVER_4_1_151(NbBundle.getMessage(ServerDetails.class, "STR_41151_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            41151,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.151/payara-4.1.151.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.152
     */
    PAYARA_SERVER_4_1_152(NbBundle.getMessage(ServerDetails.class, "STR_41152_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            41152,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.152/payara-4.1.152.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.153
     */
    PAYARA_SERVER_4_1_153(NbBundle.getMessage(ServerDetails.class, "STR_41153_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            41153,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.153/payara-4.1.153.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.154
     */
    PAYARA_SERVER_4_1_1_154(NbBundle.getMessage(ServerDetails.class, "STR_411154_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411154,
            "hhttps://github.com/payara/Payara/releases/download/payara-server-4.1.1.154/payara-4.1.1.154.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.161
     */
    PAYARA_SERVER_4_1_1_161(NbBundle.getMessage(ServerDetails.class, "STR_411161_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411161,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.1.161/payara-4.1.1.161.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.162
     */
    PAYARA_SERVER_4_1_1_162(NbBundle.getMessage(ServerDetails.class, "STR_411162_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411162,
            "https://github.com/payara/Payara/releases/download/payara-server-4.1.1.162/payara-4.1.1.162.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.163
     */
    PAYARA_SERVER_4_1_1_163(NbBundle.getMessage(ServerDetails.class, "STR_411163_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411163,
            "https://github.com/payara/Payara/releases/download/4.1.1.163/payara-4.1.1.163.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.164
     */
    PAYARA_SERVER_4_1_1_164(NbBundle.getMessage(ServerDetails.class, "STR_411164_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411164,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.1.164/payara-4.1.1.164.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.1.171
     */
    PAYARA_SERVER_4_1_1_171(NbBundle.getMessage(ServerDetails.class, "STR_411171_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            411171,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.1.171/payara-4.1.1.171.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.2.172
     */
    PAYARA_SERVER_4_1_2_172(NbBundle.getMessage(ServerDetails.class, "STR_412172_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            412172,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.2.172/payara-4.1.2.172.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.2.173
     */
    PAYARA_SERVER_4_1_2_173(NbBundle.getMessage(ServerDetails.class, "STR_412173_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            412173,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.2.173/payara-4.1.2.173.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.2.174
     */
    PAYARA_SERVER_4_1_2_174(NbBundle.getMessage(ServerDetails.class, "STR_412174_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            412174,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.2.174/payara-4.1.2.174.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 4.1.2.181
     */
    PAYARA_SERVER_4_1_2_181(NbBundle.getMessage(ServerDetails.class, "STR_412181_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv4ee7", // NOI18N
            412181,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/4.1.2.181/payara-4.1.2.181.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.181
     */
    PAYARA_SERVER_5_181(NbBundle.getMessage(ServerDetails.class, "STR_5181_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5181,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.181/payara-5.181.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.182
     */
    PAYARA_SERVER_5_182(NbBundle.getMessage(ServerDetails.class, "STR_5182_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5182,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.182/payara-5.182.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.183
     */
    PAYARA_SERVER_5_183(NbBundle.getMessage(ServerDetails.class, "STR_5183_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5183,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.183/payara-5.183.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.184
     */
    PAYARA_SERVER_5_184(NbBundle.getMessage(ServerDetails.class, "STR_5184_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5184,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.184/payara-5.184.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.191
     */
    PAYARA_SERVER_5_191(NbBundle.getMessage(ServerDetails.class, "STR_5191_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5191,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.191/payara-5.191.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.192
     */
    PAYARA_SERVER_5_192(NbBundle.getMessage(ServerDetails.class, "STR_5192_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5192,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.192/payara-5.192.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    ),
    /**
     * details for an instance of Payara Server 5.193
     */
    PAYARA_SERVER_5_193(NbBundle.getMessage(ServerDetails.class, "STR_5193_SERVER_NAME", new Object[]{}), // NOI18N
            "deployer:pfv5ee8", // NOI18N
            5193,
            "https://oss.sonatype.org/service/local/repositories/releases/content/fish/payara/distributions/payara/5.193/payara-5.193.zip", // NOI18N
            null,
            "https://raw.githubusercontent.com/payara/Payara/master/LICENSE.txt"
    );

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported Payara
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return new ServerWizardIterator(
                //add new version
                new ServerDetails[]{
                    PAYARA_SERVER_5_193,
                    PAYARA_SERVER_5_192,
                    PAYARA_SERVER_5_191,
                    PAYARA_SERVER_5_184,
                    PAYARA_SERVER_5_183,
                    PAYARA_SERVER_5_182,
                    PAYARA_SERVER_5_181,
                    PAYARA_SERVER_4_1_2_181,
                    PAYARA_SERVER_4_1_2_174,
                    PAYARA_SERVER_4_1_2_173,
                    PAYARA_SERVER_4_1_2_172,
                    PAYARA_SERVER_4_1_1_171,
                    PAYARA_SERVER_4_1_1_164,
                    PAYARA_SERVER_4_1_1_163,
                    PAYARA_SERVER_4_1_1_162,
                    PAYARA_SERVER_4_1_1_161,
                    PAYARA_SERVER_4_1_1_154,
                    PAYARA_SERVER_4_1_153,
                    PAYARA_SERVER_4_1_152,
                    PAYARA_SERVER_4_1_151,
                    PAYARA_SERVER_4_1_144
                },
                new ServerDetails[]{
                    PAYARA_SERVER_5_193,
                    PAYARA_SERVER_5_192,
                    PAYARA_SERVER_5_191,
                    PAYARA_SERVER_5_184,
                    PAYARA_SERVER_5_183,
                    PAYARA_SERVER_5_182,
                    PAYARA_SERVER_5_181,
                    PAYARA_SERVER_4_1_2_181,
                    PAYARA_SERVER_4_1_2_174,
                    PAYARA_SERVER_4_1_2_173,
                    PAYARA_SERVER_4_1_2_172,
                    PAYARA_SERVER_4_1_1_171,
                    PAYARA_SERVER_4_1_1_164,
                    PAYARA_SERVER_4_1_1_163,
                    PAYARA_SERVER_4_1_1_162,
                    PAYARA_SERVER_4_1_1_161
                }
        );
        
    }

    /**
     * Determine the version of the Payara Server installed in a directory
     * @param payaraDir the directory that holds a Payara installation
     * @return -1 if the directory is not a Payara server install
     */
    public static int getVersionFromInstallDirectory(File payaraDir)  {
        if (payaraDir == null) {
            return -1;
        }

        PayaraVersion version
                = ServerUtils.getServerVersion(payaraDir.getAbsolutePath());
        ServerDetails sd = null;
        if (version != null) {
            switch (version) {
                //add new version
                case PF_4_1_144:
                    return PAYARA_SERVER_4_1_144.getVersion();
                case PF_4_1_151:
                    return PAYARA_SERVER_4_1_151.getVersion();
                case PF_4_1_153:
                    return PAYARA_SERVER_4_1_153.getVersion();
                case PF_4_1_1_154:
                    return PAYARA_SERVER_4_1_1_154.getVersion();
                case PF_4_1_1_161:
                    return PAYARA_SERVER_4_1_1_161.getVersion();
                case PF_4_1_1_162:
                    return PAYARA_SERVER_4_1_1_162.getVersion();
                case PF_4_1_1_163:
                    return PAYARA_SERVER_4_1_1_163.getVersion();
                case PF_4_1_1_171:
                    return PAYARA_SERVER_4_1_1_171.getVersion();
                case PF_4_1_2_172:
                    return PAYARA_SERVER_4_1_2_172.getVersion();
                case PF_4_1_2_173:
                    return PAYARA_SERVER_4_1_2_173.getVersion();
                case PF_4_1_2_174:
                    return PAYARA_SERVER_4_1_2_174.getVersion();
                case PF_4_1_2_181:
                    return PAYARA_SERVER_4_1_2_181.getVersion();
                case PF_5_181:
                    return PAYARA_SERVER_5_181.getVersion();
                case PF_5_182:
                    return PAYARA_SERVER_5_182.getVersion();
                case PF_5_183:
                    return PAYARA_SERVER_5_183.getVersion();
                case PF_5_184:
                    return PAYARA_SERVER_5_184.getVersion();
                case PF_5_191:
                    return PAYARA_SERVER_5_191.getVersion();
                case PF_5_192:
                    return PAYARA_SERVER_5_192.getVersion();
                case PF_5_193:
                    return PAYARA_SERVER_5_193.getVersion();
                default:
                    return -1;
            }
        }
        return  null==sd?-1:sd.getVersion();
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
        return hasDefaultConfig(domainXml) ? PAYARA_SERVER_5_193.getVersion() : PAYARA_SERVER_5_181.getVersion();
    }

    private static boolean hasDefaultConfig(File domainXml) throws IllegalStateException {
        DomainParser dp = new DomainParser();
        List<TreeParser.Path> paths = new ArrayList<>();
        paths.add(new TreeParser.Path("/domain/configs/config",dp)); // NOI18N
        TreeParser.readXml(domainXml, paths);
        return dp.hasDefaultConfig();
    }
    
    private final String displayName;
    private final String uriFragment;
    private final String indirectUrl;
    private final String directUrl;
    private final String licenseUrl;
    private final int versionInt;
    

    ServerDetails(String displayName, String uriFragment, int versionInt,
            String directUrl, String indirectUrl, String licenseUrl) {
            this.displayName = displayName;
            this.uriFragment = uriFragment;
            this.indirectUrl = indirectUrl;
            this.directUrl = directUrl;
            this.versionInt = versionInt;
            this.licenseUrl = licenseUrl;
    }
    
    @Override 
    public String toString() {
        return displayName;
    }

    public String getUriFragment() {
        return uriFragment;
    }

    public int getVersion() {
        return versionInt;
    }

    /**
     * Determine if the glassfishDir holds a valid install of this release of
     * Payara Server.
     * @param payaraDir
     * @return true if the glassfishDir holds this particular server version.
     */
    public boolean isInstalledInDirectory(File payaraDir) {
        return getVersionFromInstallDirectory(payaraDir) == this.getVersion();
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

    public String getDirectUrl() {
        return directUrl;
    }

    public String getIndirectUrl() {
        return indirectUrl;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }
}
