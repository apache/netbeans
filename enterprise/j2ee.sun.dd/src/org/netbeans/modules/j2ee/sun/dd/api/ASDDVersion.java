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
/*
 * ASDDVersion.java
 *
 * Created on February 25, 2004, 2:36 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;

/**
 *  Enumerated types for Application and Web Server versions
 *
 * @author Peter Williams
 */
public final class ASDDVersion {

    /** Represents SunONE Application Server 7.0
     */
    public static final ASDDVersion SUN_APPSERVER_7_0 = new ASDDVersion(
        "7.0", 70,	// NOI18N
        DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_230_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_3_0,
        230,
        DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_200_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_2_0_0,
        200,
        DTDRegistry.SUN_CMP_MAPPING_700_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_700_DTD_SYSTEM_ID,
        "1.0",
        100,
        DTDRegistry.SUN_APPLICATION_130_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID,
        SunApplication.VERSION_1_3_0,
        130,
        DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_130_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_1_3_0,
        130,
        "SunONE Application Server 7.0" // NOI18N
    );

    /** Represents SunONE Application Server 7.1
     */
    // 7.1 not supported by DDAPI yet.
//    public static final ASDDVersion SUN_APPSERVER_7_1 = new ASDDVersion(
//        "7.1", 71,	// NOI18N
//        DTDRegistry.SUN_WEBAPP_231_DTD_PUBLIC_ID,
//        DTDRegistry.SUN_WEBAPP_231_DTD_SYSTEM_ID,
//        SunWebApp.VERSION_2_3_1,
//        231,
//        DTDRegistry.SUN_EJBJAR_201_DTD_PUBLIC_ID,
//        DTDRegistry.SUN_EJBJAR_201_DTD_SYSTEM_ID,
//        SunEjbJar.VERSION_2_0_1,
//        201,
//        DTDRegistry.SUN_CMP_MAPPING_700_DTD_PUBLIC_ID,
//        DTDRegistry.SUN_CMP_MAPPING_700_DTD_SYSTEM_ID,
//        "1.0"
//        100,
//        DTDRegistry.SUN_APPLICATION_130_DTD_PUBLIC_ID,
//        DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID,
//        SunApplication.VERSION_1_3_1,
//        131,
//        DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID,
//        DTDRegistry.SUN_APPCLIENT_130_DTD_SYSTEM_ID,
//        SunApplicationClient.VERSION_1_3_1,
//        131,
//        "SunONE Application Server 7.1" // NOI18N
//    );

    /** Represents Sun Java System Application Server 8.0
     */
    public static final ASDDVersion SUN_APPSERVER_8_0 = new ASDDVersion(
        "8.0", 80,	// NOI18N
        DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_240_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_4_0,
        240,
        DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_210_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_2_1_0,
        210,
        DTDRegistry.SUN_CMP_MAPPING_800_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_800_DTD_SYSTEM_ID,
        "1.1",
        110,
        DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID,
        SunApplication.VERSION_1_4_0,
        140,
        DTDRegistry.SUN_APPCLIENT_140_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_140_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_1_4_0,
        140,
        "Sun Java System Application Server 8.0" // NOI18N
    );


    /** Represents Sun Java System Application Server 8.1 (8.2 is the same)
     */
    public static final ASDDVersion SUN_APPSERVER_8_1 = new ASDDVersion(
        "8.1", 81,	// NOI18N
        DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_4_1,
        241,
        DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_2_1_1,
        211,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID,
        SunApplication.VERSION_1_4_0,
        140,
        DTDRegistry.SUN_APPCLIENT_141_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_141_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_1_4_1,
        141,
        "Sun Java System Application Server 8.1" // NOI18N
    );

    /** Represents Sun Java System Application Server 9.0
     */
    public static final ASDDVersion SUN_APPSERVER_9_0 = new ASDDVersion(
        "9.0", 90,	// NOI18N
        DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_5_0,
        250,
        DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_300_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_0_0,
        300,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID,
        SunApplication.VERSION_5_0_0,
        500,
        DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_50_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_5_0_0,
        500,
        "Sun Java System Application Server 9.0" // NOI18N
    );

    /** Represents Sun Java System Application Server 9.1.1
     */
    public static final ASDDVersion SUN_APPSERVER_9_1_1 = new ASDDVersion(
        "9.1.1", 91,	// NOI18N
        DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_5_0,
        250,
        DTDRegistry.SUN_EJBJAR_301_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_301_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_0_1,
        301,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID,
        SunApplication.VERSION_5_0_0,
        500,
        DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_50_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_5_0_0,
        500,
        "Sun Java System Application Server 9.1.1" // NOI18N
    );

    /** Represents GF Server 3.0 and 3.0.1
     */
    public static final ASDDVersion SUN_APPSERVER_10_0 = new ASDDVersion(
        "10.0", 100,	// NOI18N
        DTDRegistry.SUN_WEBAPP_300_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_300_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_0,
        300,
        DTDRegistry.SUN_EJBJAR_310_DTD_PUBLIC_ID,
        DTDRegistry.SUN_EJBJAR_310_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_0,
        310,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.SUN_APPLICATION_60_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPLICATION_60_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_0,
        600,
        DTDRegistry.SUN_APPCLIENT_60_DTD_PUBLIC_ID,
        DTDRegistry.SUN_APPCLIENT_60_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_0,
        600,
        "GlassFish Server 3.0" // NOI18N
    );

    /** Represents GF Server 3.1
     */
    public static final ASDDVersion SUN_APPSERVER_10_1 = new ASDDVersion(
        "10.1", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 3.1" // NOI18N
    );

    /** Represents GF Server 4.0
     */
    public static final ASDDVersion GLASSFISH_4_0 = new ASDDVersion(
        "4.1", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 4.0" // NOI18N
    );
    
    /** Represents GF Server 4.1
     */
    public static final ASDDVersion GLASSFISH_4_1 = new ASDDVersion(
        "4.1", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 4.1" // NOI18N
    );
    
    /** Represents GF Server 5.0
     */
    public static final ASDDVersion GLASSFISH_5_0 = new ASDDVersion(
        "5.0", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 5.0" // NOI18N
    );
    /** Represents GF Server 5.1
     */
    public static final ASDDVersion GLASSFISH_5_1 = new ASDDVersion(
        "5.1", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 5.1" // NOI18N
    );
    /** Represents GF Server 6
     */
    public static final ASDDVersion GLASSFISH_6 = new ASDDVersion(
        "6.0", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 6" // NOI18N
    );
    /** Represents GF Server 7
     */
    public static final ASDDVersion GLASSFISH_7 = new ASDDVersion(
        "7.0", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 7" // NOI18N
    );
    /** Represents GF Server 8
     */
    public static final ASDDVersion GLASSFISH_8 = new ASDDVersion(
        "8.0", 100,	// NOI18N
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID,
        SunWebApp.VERSION_3_0_1,
        301,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID,
        SunEjbJar.VERSION_3_1_1,
        311,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID,
        DTDRegistry.SUN_CMP_MAPPING_810_DTD_SYSTEM_ID,
        "1.2",
        120,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID,
        SunApplication.VERSION_6_0_1,
        601,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID,
        DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID,
        SunApplicationClient.VERSION_6_0_1,
        601,
        "GlassFish Server 8" // NOI18N
    );
    /** Represents Sun Java System Web Server 7.0
     */
    public static final ASDDVersion SUN_WEBSERVER_7_0 = new ASDDVersion(
        "8.1", 81,	// NOI18N
        DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID,
        DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID,
        SunWebApp.VERSION_2_4_1,
        241,
        "Sun Java System Web Server 7.0" // NOI18N
    );

    /** Ordered list of appserver versions.
     */
    public static final ASDDVersion asDDVersions[] = {
        SUN_APPSERVER_7_0,
//        SUN_APPSERVER_7_1,
        SUN_APPSERVER_8_0,
        SUN_APPSERVER_8_1,
        SUN_APPSERVER_9_0,
        SUN_APPSERVER_9_1_1,
        SUN_APPSERVER_10_0,
        SUN_APPSERVER_10_1,
    };

    /** Ordered list of webserver versions.
     */
    public static final ASDDVersion webServerDDVersions[] = {
        SUN_WEBSERVER_7_0
    };


    /** -----------------------------------------------------------------------
     *  Implementation
     */

    private final String version;
    private final BigDecimal numericVersion;

    private final String webAppPublicId;
    private final String webAppSystemId;
    private final String servletVersionString;
    private final BigDecimal servletVersion;

    private final String ejbJarPublicId;
    private final String ejbJarSystemId;
    private final String ejbVersionString;
    private final BigDecimal ejbVersion;

    private final String cmpMappingsPublicId;
    private final String cmpMappingsSystemId;
    private final String cmpMappingsVersionString;
    private final BigDecimal cmpMappingsVersion;

    private final String appPublicId;
    private final String appSystemId;
    private final String appVersionString;
    private final BigDecimal appVersion;

    private final String appClientPublicId;
    private final String appClientSystemId;
    private final String appClientVersionString;
    private final BigDecimal appClientVersion;

    private final String displayName;


    /** Creates a new instance of ASDDVersion for WebServier (Servlet spec only)
     */
    private ASDDVersion(String v, int nv,
            String wapi, String wasi, String svs, int sv,
            String dn) {
        this(v, nv,
                wapi, wasi, svs, sv,
                "", "", "", 0,
                "", "", "", 0,
                "", "", "", 0,
                "", "", "", 0,
                dn);
    }

    /** Creates a new instance of ASDDVersion
     */
    private ASDDVersion(String v, int nv,
            String wapi, String wasi, String svs, int sv,
            String ejpi, String ejsi, String ejbvs, int ejbv,
            String cmpi, String cmsi, String cmpvs, int cmpv,
            String api, String asi, String appvs, int appv,
            String acpi, String acsi, String acvs, int acpv,
            String dn) {
        version = v;
        numericVersion = new BigDecimal(BigInteger.valueOf(nv), 2);

        webAppPublicId = wapi;
        webAppSystemId = wasi;
        servletVersionString = svs;
        servletVersion = new BigDecimal(BigInteger.valueOf(sv), 2);

        ejbJarPublicId = ejpi;
        ejbJarSystemId = ejsi;
        ejbVersionString = ejbvs;
        ejbVersion = new BigDecimal(BigInteger.valueOf(ejbv), 2);

        cmpMappingsPublicId = cmpi;
        cmpMappingsSystemId = cmsi;
        cmpMappingsVersionString = cmpvs;
        cmpMappingsVersion = new BigDecimal(BigInteger.valueOf(cmpv), 2);

        appPublicId = api;
        appSystemId = asi;
        appVersionString = appvs;
        appVersion = new BigDecimal(BigInteger.valueOf(appv), 2);

        appClientPublicId = acpi;
        appClientSystemId = acsi;
        appClientVersionString = acvs;
        appClientVersion = new BigDecimal(BigInteger.valueOf(acpv), 2);

        displayName = dn;
    }

    /** Display name for property combo chooser (or whereever else needed.)
     */
    public String toString() {
        return displayName;
    }

    /** Comparator implementation that works only on ASDDVersion objects
     *
     *  @param obj ASDDVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a ASDDVersion object.
     */
    public int compareTo(Object obj) {
        ASDDVersion target = (ASDDVersion) obj;
        return numericVersion.compareTo(target.numericVersion);
    }

    /** Retrieve the proper ASDDVersion object for the specified version (string format).
     */
    public static final ASDDVersion getASDDVersion(String version) {
        ASDDVersion result = null;

        if(SUN_APPSERVER_7_0.toString().equals(version)) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.toString().equals(version)) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.toString().equals(version)) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.toString().equals(version)) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.toString().equals(version)) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.toString().equals(version)) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.toString().equals(version)) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.toString().equals(version)) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersion(BigDecimal bdversion) {
        ASDDVersion result = null;

        String version = bdversion.toString();
        if(SUN_APPSERVER_7_0.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.toString().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.toString().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersionFromServletVersion(BigDecimal version) {
        ASDDVersion result = null;

        if(SUN_APPSERVER_7_0.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.getNumericWebAppVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        }  else if(SUN_APPSERVER_10_1.getNumericWebAppVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersionFromEjbVersion(BigDecimal version) {
        ASDDVersion result = null;

        if(SUN_APPSERVER_7_0.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.getNumericEjbJarVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.getNumericEjbJarVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersionFromAppVersion(BigDecimal version) {
        ASDDVersion result = null;

        if(SUN_APPSERVER_7_0.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.getNumericApplicationVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        // 8.0 and 8.1 use the same DTD, pick 8.1 only here.  We can amend this later
        // if we want to somehow properly support 8.0 but it's not a requirement right now.
//        } else if(SUN_APPSERVER_8_0.getNumericApplicationVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.getNumericApplicationVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersionFromAppClientVersion(BigDecimal version) {
        ASDDVersion result = null;
        if(SUN_APPSERVER_7_0.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.getNumericAppClientVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.getNumericAppClientVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }
        return result;
    }

    /** Retrieve the proper ASDDVersion object for the specified version (BigDecimal format).
     */
    public static final ASDDVersion getASDDVersionFromCmpMappingsVersion(BigDecimal version) {
        ASDDVersion result = null;

        if(SUN_APPSERVER_7_0.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_7_0;
//        } else if(SUN_APPSERVER_7_1.getNumericCmpMappingsVersion().compareTo(version) == 0) {
//            result = SUN_APPSERVER_7_1;
        } else if(SUN_APPSERVER_8_0.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_0;
        } else if(SUN_APPSERVER_8_1.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_8_1;
        } else if(SUN_APPSERVER_9_0.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_0;
        } else if(SUN_APPSERVER_9_1_1.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_9_1_1;
        } else if(SUN_APPSERVER_10_0.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_0;
        } else if(SUN_APPSERVER_10_1.getNumericCmpMappingsVersion().compareTo(version) == 0) {
            result = SUN_APPSERVER_10_1;
        }

        return result;
    }


    /** Sun web app version as string, from DD API
     */
    public final String getWebAppVersionAsString() {
        return servletVersionString;
    }

    /** Sun ejb jar version as string, from DD API
     */
    public final String getEjbJarVersionAsString() {
        return ejbVersionString;
    }

    /** Sun cmp mappings version as string, from DD API
     */
    public final String getCmpMappingsVersionAsString() {
        return cmpMappingsVersionString;
    }

    /** Sun application version as string, from DD API
     */
    public final String getApplicationVersionAsString() {
        return appVersionString;
    }

    /** Sun application client version as string, from DD API
     */
    public final String getAppClientVersionAsString() {
        return appClientVersionString;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericServerVersion() {
        return numericVersion;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericWebAppVersion() {
        return servletVersion;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericEjbJarVersion() {
        return ejbVersion;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericCmpMappingsVersion() {
        return cmpMappingsVersion;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericApplicationVersion() {
        return appVersion;
    }

    /** Version, in the format expected by the sun-* DD API
     */
    public final BigDecimal getNumericAppClientVersion() {
        return appClientVersion;
    }

    /** Returns the public id for sun-web-app.xml for this appserver version
     */
    public final String getSunWebAppPublicId() {
        return webAppPublicId;
    }

    /** Returns the system id for sun-web-app.xml for this appserver version
     */
    public final String getSunWebAppSystemId() {
        return webAppSystemId;
    }

    /** Returns the public id for sun-ejb-jar.xml for this appserver version
     */
    public final String getSunEjbJarPublicId() {
        return ejbJarPublicId;
    }

    /** Returns the system id for sun-ejb-jar.xml for this appserver version
     */
    public final String getSunEjbJarSystemId() {
        return ejbJarSystemId;
    }

    /** Returns the public id for sun-cmp-mappings.xml for this appserver version
     */
    public final String getSunCmpMappingsPublicId() {
        return cmpMappingsPublicId;
    }

    /** Returns the system id for sun-cmp-mappings.xml for this appserver version
     */
    public final String getSunCmpMappingsSystemId() {
        return cmpMappingsSystemId;
    }

    /** Returns the public id for sun-application.xml for this appserver version
     */
    public final String getSunApplicationPublicId() {
        return appPublicId;
    }

    /** Returns the system id for sun-application.xml for this appserver version
     */
    public final String getSunApplicationSystemId() {
        return appSystemId;
    }

    /** Returns the public id for sun-application.xml for this appserver version
     */
    public final String getSunAppClientPublicId() {
        return appClientPublicId;
    }

    /** Returns the system id for sun-application.xml for this appserver version
     */
    public final String getSunAppClientSystemId() {
        return appClientSystemId;
    }
}
