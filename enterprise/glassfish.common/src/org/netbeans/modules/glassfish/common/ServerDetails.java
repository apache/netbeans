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
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
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
        GlassfishInstanceProvider.EE6_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3,
        "https://download.oracle.com/glassfish/v3/release/glassfish-v3.zip", // NOI18N
        "https://download.oracle.com/glassfish/v3/release/glassfish-v3.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.0/3.0.x
     */
    GLASSFISH_SERVER_3_0_1(NbBundle.getMessage(ServerDetails.class,"STR_301_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE6_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3_0_1,
        "https://download.oracle.com/glassfish/3.0.1/release/glassfish-3.0.1-ml.zip", // NOI18N
        "https://download.oracle.com/glassfish/3.0.1/release/glassfish-3.0.1-ml.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1
     */
    GLASSFISH_SERVER_3_1(NbBundle.getMessage(ServerDetails.class, "STR_31_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3_1,
        "https://download.oracle.com/glassfish/3.1/release/glassfish-3.1-ml.zip", // NOI18N
        "https://download.oracle.com/glassfish/3.1/release/glassfish-3.1-ml.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1.1
     */
    GLASSFISH_SERVER_3_1_1(NbBundle.getMessage(ServerDetails.class, "STR_311_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3_1_1,
        "https://download.oracle.com/glassfish/3.1.1/release/glassfish-3.1.1-ml.zip", // NOI18N
        "https://download.oracle.com/glassfish/3.1.1/release/glassfish-3.1.1-ml.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1.2
     */
    GLASSFISH_SERVER_3_1_2(NbBundle.getMessage(ServerDetails.class, "STR_312_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3_1_2,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/3.1.2/glassfish-3.1.2.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/3.1.2/glassfish-3.1.2.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 3.1.2.2
     */
    GLASSFISH_SERVER_3_1_2_2(NbBundle.getMessage(ServerDetails.class, "STR_3122_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE6WC_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_3_1_2_2,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/3.1.2.2/glassfish-3.1.2.2.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/3.1.2.2/glassfish-3.1.2.2.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.0.0
     */
    GLASSFISH_SERVER_4_0(NbBundle.getMessage(ServerDetails.class, "STR_40_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE7_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_4,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.0/glassfish-4.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.0/glassfish-4.0.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /** 
     * Neither repos have this release:
     * <pre>
     * -{@code https://download.oracle.com/glassfish}
     * -{@code https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/}
     * 
     * Details for an instance of GlassFish Server 4.0.1
     * </pre>
     */
    GLASSFISH_SERVER_4_0_1(NbBundle.getMessage(ServerDetails.class, "STR_401_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE7_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_4_0_1,
        "https://download.oracle.com/glassfish/4.0.1/release/glassfish-4.0.1-ml.zip", // NOI18N
        "https://download.oracle.com/glassfish/4.0.1/release/glassfish-4.0.1-ml.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N"
    ),

    /**
     * details for an instance of GlassFish Server 4.1
     */
    GLASSFISH_SERVER_4_1(NbBundle.getMessage(ServerDetails.class, "STR_41_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE7_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_4_1,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1/glassfish-4.1.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1/glassfish-4.1.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.1.1
     */
    GLASSFISH_SERVER_4_1_1(NbBundle.getMessage(ServerDetails.class, "STR_411_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE7_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_4_1_1,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1.1/glassfish-4.1.1.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1.1/glassfish-4.1.1.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.1.2
     */
    GLASSFISH_SERVER_4_1_2(NbBundle.getMessage(ServerDetails.class, "STR_412_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE7_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_4_1_2,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1.2/glassfish-4.1.2.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/4.1.2/glassfish-4.1.2.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 5
     */
    GLASSFISH_SERVER_5_0(NbBundle.getMessage(ServerDetails.class, "STR_50_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE8_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_5,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.0/glassfish-5.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.0/glassfish-5.0.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 5.0.1
     */
    GLASSFISH_SERVER_5_0_1(NbBundle.getMessage(ServerDetails.class, "STR_501_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.EE8_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_5_0_1,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.0.1/glassfish-5.0.1.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.0.1/glassfish-5.0.1.zip", // NOI18N
        "https://javaee.github.io/glassfish/LICENSE" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 5.1.0
     */
    GLASSFISH_SERVER_5_1_0(NbBundle.getMessage(ServerDetails.class, "STR_510_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE8_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_5_1_0,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.1.0/glassfish-5.1.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/5.1.0/glassfish-5.1.0.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 6.0.0
     */
    GLASSFISH_SERVER_6(NbBundle.getMessage(ServerDetails.class, "STR_6_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE9_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.0.0/glassfish-6.0.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.0.0/glassfish-6.0.0.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 6.1.0
     */
    GLASSFISH_SERVER_6_1_0(NbBundle.getMessage(ServerDetails.class, "STR_610_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_1_0,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.1.0/glassfish-6.1.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.1.0/glassfish-6.1.0.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 6.2.0
     */
    GLASSFISH_SERVER_6_2_0(NbBundle.getMessage(ServerDetails.class, "STR_620_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_0,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.0/glassfish-6.2.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.0/glassfish-6.2.0.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 6.2.1
     */
    GLASSFISH_SERVER_6_2_1(NbBundle.getMessage(ServerDetails.class, "STR_621_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_1,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.1/glassfish-6.2.1.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.1/glassfish-6.2.1.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 6.2.2
     */
    GLASSFISH_SERVER_6_2_2(NbBundle.getMessage(ServerDetails.class, "STR_622_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_2,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.2/glassfish-6.2.2.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.2/glassfish-6.2.2.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 6.2.3
     */
    GLASSFISH_SERVER_6_2_3(NbBundle.getMessage(ServerDetails.class, "STR_623_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_3,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.3/glassfish-6.2.3.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.3/glassfish-6.2.3.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 6.2.4
     */
    GLASSFISH_SERVER_6_2_4(NbBundle.getMessage(ServerDetails.class, "STR_624_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_4,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.4/glassfish-6.2.4.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.4/glassfish-6.2.4.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 6.2.5
     */
    GLASSFISH_SERVER_6_2_5(NbBundle.getMessage(ServerDetails.class, "STR_625_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE91_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_6_2_5,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.5/glassfish-6.2.5.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/6.2.5/glassfish-6.2.5.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.0
     */
    GLASSFISH_SERVER_7_0_0(NbBundle.getMessage(ServerDetails.class, "STR_700_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_0,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.0/glassfish-7.0.0.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.0/glassfish-7.0.0.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.1
     */
    GLASSFISH_SERVER_7_0_1(NbBundle.getMessage(ServerDetails.class, "STR_701_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_1,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.1/glassfish-7.0.1.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.1/glassfish-7.0.1.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.2
     */
    GLASSFISH_SERVER_7_0_2(NbBundle.getMessage(ServerDetails.class, "STR_702_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_2,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.2/glassfish-7.0.2.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.2/glassfish-7.0.2.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.3
     */
    GLASSFISH_SERVER_7_0_3(NbBundle.getMessage(ServerDetails.class, "STR_703_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_3,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.3/glassfish-7.0.3.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.3/glassfish-7.0.3.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.4
     */
    GLASSFISH_SERVER_7_0_4(NbBundle.getMessage(ServerDetails.class, "STR_704_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_4,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.4/glassfish-7.0.4.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.4/glassfish-7.0.4.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.5
     */
    GLASSFISH_SERVER_7_0_5(NbBundle.getMessage(ServerDetails.class, "STR_705_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_5,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.5/glassfish-7.0.5.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.5/glassfish-7.0.5.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.6
     */
    GLASSFISH_SERVER_7_0_6(NbBundle.getMessage(ServerDetails.class, "STR_706_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_6,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.6/glassfish-7.0.6.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.6/glassfish-7.0.6.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.7
     */
    GLASSFISH_SERVER_7_0_7(NbBundle.getMessage(ServerDetails.class, "STR_707_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_7,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.7/glassfish-7.0.7.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.7/glassfish-7.0.7.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.8
     */
    GLASSFISH_SERVER_7_0_8(NbBundle.getMessage(ServerDetails.class, "STR_708_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_8,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.8/glassfish-7.0.8.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.8/glassfish-7.0.8.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.9
     */
    GLASSFISH_SERVER_7_0_9(NbBundle.getMessage(ServerDetails.class, "STR_709_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_9,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.9/glassfish-7.0.9.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.9/glassfish-7.0.9.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.10
     */
    GLASSFISH_SERVER_7_0_10(NbBundle.getMessage(ServerDetails.class, "STR_7010_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_10,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.10/glassfish-7.0.10.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.10/glassfish-7.0.10.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),
    
    /**
     * details for an instance of GlassFish Server 7.0.11
     */
    GLASSFISH_SERVER_7_0_11(NbBundle.getMessage(ServerDetails.class, "STR_7011_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_11,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.11/glassfish-7.0.11.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.11/glassfish-7.0.11.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.12
     */
    GLASSFISH_SERVER_7_0_12(NbBundle.getMessage(ServerDetails.class, "STR_7012_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_12,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.12/glassfish-7.0.12.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.12/glassfish-7.0.12.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 7.0.13
     */
    GLASSFISH_SERVER_7_0_13(NbBundle.getMessage(ServerDetails.class, "STR_7013_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE10_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_7_0_13,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.13/glassfish-7.0.13.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/7.0.13/glassfish-7.0.13.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 8.0.0
     */
    GLASSFISH_SERVER_8_0_0(NbBundle.getMessage(ServerDetails.class, "STR_800_SERVER_NAME", new Object[]{}), // NOI18N
        GlassfishInstanceProvider.JAKARTAEE11_DEPLOYER_FRAGMENT,
        GlassFishVersion.GF_8_0_0,
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/8.0.0-M3/glassfish-8.0.0-M3.zip", // NOI18N
        "https://repo.maven.apache.org/maven2/org/glassfish/main/distributions/glassfish/8.0.0-M3/glassfish-8.0.0-M3.zip", // NOI18N
        "http://www.eclipse.org/legal/epl-2.0" //NOI18N
    );
    
    /**
     * Array with all GlassFish {@code ServerDetails} versions.
     */
    private static final ServerDetails[] serverDetails = ServerDetails.values();

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported GlassFish
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return new ServerWizardIterator(serverDetails, serverDetails);
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
        if (version != null) {
            return version.toFullInteger();
        }
        return -1;
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
    
    private final String displayName;
    private final String uriFragment;
    private final String indirectUrl;
    private final String directUrl;
    private final String licenseUrl;
    private final GlassFishVersion glassFishVersion;

    
    ServerDetails(String displayName, String uriFragment, GlassFishVersion glassFishVersion,
            String directUrl, String indirectUrl, String licenseUrl) {
            this.displayName = displayName;
            this.uriFragment = uriFragment;
            this.indirectUrl = indirectUrl;
            this.directUrl = directUrl;
            this.glassFishVersion = glassFishVersion;
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
        return glassFishVersion.toFullInteger();
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

    public String getLicenseUrl() {
        return licenseUrl;
    }
}
