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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.glassfish.common.parser.TreeParser;
import org.netbeans.modules.glassfish.common.wizards.ServerWizardIterator;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.Utils;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_0_1;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_1;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_2;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_2_2;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_2_3;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_2_4;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3_1_2_5;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_4;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_4_0_1;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public enum ServerDetails {
    
    /**
     * details for an instance of GlassFish Server 3.0/3.0.x
     */
    GLASSFISH_SERVER_3(NbBundle.getMessage(ServerDetails.class,"STR_3_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6", // NOI18N
        300,
        "http://download.java.net/glassfish/v3/release/glassfish-v3.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.0/3.0.x
     */
    GLASSFISH_SERVER_3_0_1(NbBundle.getMessage(ServerDetails.class,"STR_301_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6", // NOI18N
        301,
        "http://download.java.net/glassfish/3.0.1/release/glassfish-3.0.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-0-1.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1
     */
    GLASSFISH_SERVER_3_1(NbBundle.getMessage(ServerDetails.class, "STR_31_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        310,
        "http://download.java.net/glassfish/3.1/release/glassfish-3.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-1.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1.1
     */
    GLASSFISH_SERVER_3_1_1(NbBundle.getMessage(ServerDetails.class, "STR_311_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        311,
        "http://download.java.net/glassfish/3.1.1/release/glassfish-3.1.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-1-1.txt" // NOI18N
            ),
    /**
     * details for an instance of GlassFish Server 3.1.2
     */
    GLASSFISH_SERVER_3_1_2(NbBundle.getMessage(ServerDetails.class, "STR_312_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        312,
        "http://download.java.net/glassfish/3.1.2/release/glassfish-3.1.2-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v3-1-2.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 3.1.2.2
     */
    GLASSFISH_SERVER_3_1_2_2(NbBundle.getMessage(ServerDetails.class, "STR_3122_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        312,
        "http://download.java.net/glassfish/3.1.2.2/release/glassfish-3.1.2.2-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v3-1-2.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.0.0
     */
    GLASSFISH_SERVER_4_0(NbBundle.getMessage(ServerDetails.class, "STR_40_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        400,
        "http://download.java.net/glassfish/4.0/release/glassfish-4.0-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-0.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.0.1
     */
    GLASSFISH_SERVER_4_0_1(NbBundle.getMessage(ServerDetails.class, "STR_401_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        401,
        "http://download.java.net/glassfish/4.0.1/release/glassfish-4.0.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-0-1.txt" // NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 4.1 and dev 4.2
     */
    GLASSFISH_SERVER_4_1(NbBundle.getMessage(ServerDetails.class, "STR_41_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        410,
        "http://download.java.net/glassfish/4.1/release/glassfish-4.1.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-1.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.1.1 and dev 4.2
     */
    GLASSFISH_SERVER_4_1_1(NbBundle.getMessage(ServerDetails.class, "STR_411_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        411,
        "http://download.java.net/glassfish/4.1.1/release/glassfish-4.1.1.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-1-1.txt" // NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 5
     */
    GLASSFISH_SERVER_5_0(NbBundle.getMessage(ServerDetails.class, "STR_50_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        500,
        "http://download.java.net/glassfish/5.0/release/glassfish-5.0.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v5-0.txt" // NOI18N
    );

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported GlassFish
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return new ServerWizardIterator(new ServerDetails[]{
                    GLASSFISH_SERVER_5_0,
                    GLASSFISH_SERVER_4_1_1,
                    GLASSFISH_SERVER_4_1,
                    GLASSFISH_SERVER_4_0_1,
                    GLASSFISH_SERVER_4_0,
                    GLASSFISH_SERVER_3_1_2_2,
                    GLASSFISH_SERVER_3_1_2,
                    GLASSFISH_SERVER_3_1_1,
                    GLASSFISH_SERVER_3_1,
                    GLASSFISH_SERVER_3_0_1,
                    GLASSFISH_SERVER_3,},
                new ServerDetails[]{
                    // not yet
                    //GLASSFISH_SERVER_5_0, 
                    GLASSFISH_SERVER_4_1_1,
                    GLASSFISH_SERVER_4_1,
                    GLASSFISH_SERVER_4_0,
                    GLASSFISH_SERVER_3_1_2_2});
    }

    /**
     * Determine the version of the GlassFish Server installed in a directory
     * @param glassfishDir the directory that holds a GlassFish installation
     * @return -1 if the directory is not a GlassFish server install
     */
    public static int getVersionFromInstallDirectory(File glassfishDir)  {
        if (glassfishDir == null) {
            return -1;
        }

        GlassFishVersion version
                = ServerUtils.getServerVersion(glassfishDir.getAbsolutePath());
        ServerDetails sd = null;
        if (version != null) {
            switch(version) {
                case GF_3:       return GLASSFISH_SERVER_3.getVersion();
                case GF_3_0_1:   return GLASSFISH_SERVER_3_0_1.getVersion();
                case GF_3_1:     return GLASSFISH_SERVER_3_1.getVersion();
                case GF_3_1_1:   return GLASSFISH_SERVER_3_1_1.getVersion();
                case GF_3_1_2:   return GLASSFISH_SERVER_3_1_2.getVersion();
                case GF_3_1_2_2:
                case GF_3_1_2_3:
                case GF_3_1_2_4:
                case GF_3_1_2_5: return GLASSFISH_SERVER_3_1_2_2.getVersion();
                case GF_4:       return GLASSFISH_SERVER_4_0.getVersion();
                case GF_4_0_1:   return GLASSFISH_SERVER_4_0_1.getVersion();
                case GF_4_1:     return GLASSFISH_SERVER_4_1.getVersion();
                case GF_4_1_1:   return GLASSFISH_SERVER_4_1_1.getVersion();
                case GF_5:       return GLASSFISH_SERVER_5_0.getVersion();
                default:         return -1;
            }
        }
        return  null==sd?-1:sd.getVersion();
    }

    /**
     * Determine the version of the GlassFish Server that wrote the domain.xml file
     * 
     * @param domainXml the file to analyze
     * @return -1 if domainXml is null, unreadable or not a directory
     * @throws IllegalStateException if domainXml cannot be parsed
     */
    public static int getVersionFromDomainXml(File domainXml) throws IllegalStateException {
        if (null == domainXml || !domainXml.isFile() || !domainXml.canRead()) {
            return -1;
        }
        return hasDefaultConfig(domainXml) ? GLASSFISH_SERVER_3_1.getVersion() :
            GLASSFISH_SERVER_3.getVersion();
    }

    private static boolean hasDefaultConfig(File domainXml) throws IllegalStateException {
        DomainParser dp = new DomainParser();
        List<TreeParser.Path> paths = new ArrayList<TreeParser.Path>();
        paths.add(new TreeParser.Path("/domain/configs/config",dp)); // NOI18N
        TreeParser.readXml(domainXml, paths);
        return dp.hasDefaultConfig();
    }
    
    private String displayName;
    private String uriFragment;
    private String indirectUrl;
    private String directUrl;
    private int versionInt;
    

    ServerDetails(String displayName, String uriFragment, int versionInt,
            String directUrl, String indirectUrl) {
            this.displayName = displayName;
            this.uriFragment = uriFragment;
            this.indirectUrl = indirectUrl;
            this.directUrl = directUrl;
            this.versionInt = versionInt;
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
     * GlassFish Server.
     * @param glassfishDir
     * @return true if the glassfishDir holds this particular server version.
     */
    public boolean isInstalledInDirectory(File glassfishDir) {
        return getVersionFromInstallDirectory(glassfishDir) == this.getVersion();
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

}
