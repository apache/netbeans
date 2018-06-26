/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

// START OF IASRI 4661135

package org.netbeans.modules.j2ee.sun.dd.impl;

public final class DTDRegistry {

     public static final String Package = "com.sun.enterprise.deployment.xml";

     // Standard DTDs
    
     public static final String APPLICATION_13_DTD_PUBLIC_ID =
         "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN";
     public static final String APPLICATION_13_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/application_1_3.dtd";

     public static final String APPLICATION_12_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN";
     public static final String APPLICATION_12_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/application_1_2.dtd";

     public static final String EJBJAR_20_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
     public static final String EJBJAR_20_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/ejb-jar_2_0.dtd";

    public static final String EJBJAR_11_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
    public static final String EJBJAR_11_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/ejb-jar_1_1.dtd";

    public static final String APPCLIENT_13_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN";
    public static final String APPCLIENT_13_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/application-client_1_3.dtd";

    public static final String APPCLIENT_12_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN";
    public static final String APPCLIENT_12_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/application-client_1_2.dtd";

    public static final String CONNECTOR_10_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Connector 1.0//EN";
    public static final String CONNECTOR_10_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/connector_1_0.dtd";

    public static final String WEBAPP_23_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WEBAPP_23_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/web-app_2_3.dtd";

    public static final String WEBAPP_22_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WEBAPP_22_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/web-app_2_2.dtd";

    public static final String TAGLIB_12_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String TAGLIB_12_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd";

    public static final String TAGLIB_11_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String TAGLIB_11_DTD_SYSTEM_ID =
        "http://java.sun.com/dtd/web-jsptaglibrary_1_1.dtd";

    public static final String GLASSFISH_RESOURCE_15_DTD_PUBLIC_ID =
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN";
    public static final String GLASSFISH_RESOURCE_15_DTD_SYSTEM_ID =
        "http://glassfish.org/dtds/glassfish-resources_1_5.dtd";
    public static final String SUN_RESOURCE_13_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN";
    public static final String SUN_RESOURCE_13_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd";
    public static final String SUN_RESOURCE_12_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN";
    public static final String SUN_RESOURCE_12_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-resources_1_2.dtd";
    //SunONE specific dtds

    /**
     * Application: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_APPLICATION_130_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 J2EE Application 1.3//EN";
    public static final String SUN_APPLICATION_130_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-application_1_3-0.dtd";
    public static final String SUN_APPLICATION_140beta_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 J2EE Application 1.4//EN";
    public static final String SUN_APPLICATION_140beta_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-application_1_4-0.dtd";    
    public static final String SUN_APPLICATION_140_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 J2EE Application 1.4//EN";
    public static final String SUN_APPLICATION_140_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application_1_4-0.dtd";    
    
    public static final String SUN_APPLICATION_50_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Java EE Application 5.0//EN";
    public static final String SUN_APPLICATION_50_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application_5_0-0.dtd";
    
    public static final String SUN_APPLICATION_60_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Java EE Application 6.0//EN";
    public static final String SUN_APPLICATION_60_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application_6_0-0.dtd";

    public static final String GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID =
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Java EE Application 6.0//EN";
    public static final String GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID =
        "http://glassfish.org/dtds/glassfish-application_6_0-1.dtd";
    /**
     * EJB: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_EJBJAR_200_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 EJB 2.0//EN";
    public static final String SUN_EJBJAR_200_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-ejb-jar_2_0-0.dtd";
    public static final String SUN_EJBJAR_201_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.1 EJB 2.0//EN";
    public static final String SUN_EJBJAR_201_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-ejb-jar_2_0-1.dtd";
    public static final String SUN_EJBJAR_210beta_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 EJB 2.1//EN";
    public static final String SUN_EJBJAR_210beta_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-ejb-jar_2_1-0.dtd";    
    public static final String SUN_EJBJAR_210_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 EJB 2.1//EN";
    public static final String SUN_EJBJAR_210_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_2_1-0.dtd";    

    public static final String SUN_EJBJAR_211_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 EJB 2.1//EN";
    public static final String SUN_EJBJAR_211_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_2_1-1.dtd";    
    
    public static final String SUN_EJBJAR_300_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 EJB 3.0//EN";
    public static final String SUN_EJBJAR_300_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_3_0-0.dtd";  

    public static final String SUN_EJBJAR_301_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.1.1 EJB 3.0//EN";
    public static final String SUN_EJBJAR_301_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_3_0-1.dtd";
    
    public static final String SUN_EJBJAR_310_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 EJB 3.1//EN";
    public static final String SUN_EJBJAR_310_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-ejb-jar_3_1-0.dtd";

    public static final String GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID =
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 EJB 3.1//EN";
    public static final String GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID =
        "http://glassfish.org/dtds/glassfish-ejb-jar_3_1-1.dtd";
    /**
     * Application Client: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_APPCLIENT_130_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client 1.3//EN";
    public static final String SUN_APPCLIENT_130_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-application-client_1_3-0.dtd";
    public static final String SUN_APPCLIENT_140beta_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Application Client 1.4//EN";
    public static final String SUN_APPCLIENT_140beta_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-application-client_1_4-0.dtd";    
    public static final String SUN_APPCLIENT_140_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client 1.4//EN";
    public static final String SUN_APPCLIENT_140_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application-client_1_4-0.dtd";    
    public static final String SUN_APPCLIENT_141_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client 1.4//EN";
    public static final String SUN_APPCLIENT_141_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application-client_1_4-1.dtd";
    public static final String SUN_APPCLIENT_50_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Application Client 5.0//EN";
    public static final String SUN_APPCLIENT_50_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application-client_5_0-0.dtd";    
    public static final String SUN_APPCLIENT_60_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Application Client 6.0//EN";
    public static final String SUN_APPCLIENT_60_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-application-client_6_0-0.dtd";
    
    public static final String GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID =
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Java EE Application Client 6.0//EN";
    public static final String GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID =
        "http://glassfish.org/dtds/glassfish-application-client_6_0-1.dtd";
    /**
     * Connectors: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_CONNECTOR_100_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Connector 1.0//EN";
    public static final String SUN_CONNECTOR_100_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-connector_1_0-0.dtd";

    /**
     * Web: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_WEBAPP_230_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Servlet 2.3//EN";
    public static final String SUN_WEBAPP_230_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-web-app_2_3-0.dtd";

    public static final String SUN_WEBAPP_231_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.1 Servlet 2.3//EN";
    public static final String SUN_WEBAPP_231_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-web-app_2_3-1.dtd";

    public static final String SUN_WEBAPP_240beta_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Servlet 2.4//EN";	
    public static final String SUN_WEBAPP_240beta_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-web-app_2_4-0.dtd";	
    public static final String SUN_WEBAPP_240_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Servlet 2.4//EN";	
    public static final String SUN_WEBAPP_240_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-web-app_2_4-0.dtd";	

    public static final String SUN_WEBAPP_241_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Servlet 2.4//EN";	
    public static final String SUN_WEBAPP_241_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-web-app_2_4-1.dtd";	

    public static final String SUN_WEBAPP_250_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Servlet 2.5//EN";	
    public static final String SUN_WEBAPP_250_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-web-app_2_5-0.dtd";

    public static final String SUN_WEBAPP_300_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Servlet 3.0//EN";
    public static final String SUN_WEBAPP_300_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-web-app_3_0-0.dtd";

    public static final String GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID =
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Servlet 3.0//EN";
    public static final String GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID =
        "http://glassfish.org/dtds/glassfish-web-app_3_0-1.dtd";
    /**
     * Application Client Container: Sun ONE App Server specific dtd info.
     */
    public static final String SUN_CLIENTCONTAINER_700_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client Container 1.0//EN";
    public static final String SUN_CLIENTCONTAINER_700_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-application-client-container_1_0.dtd";

    //4690447-adding it for sun-cmp-mapping.xml
    /**
     * EJB CMP Mapping : Sun ONE App Server specific dtd info.
     */
    public static final String SUN_CMP_MAPPING_700_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 OR Mapping //EN";
    public static final String SUN_CMP_MAPPING_700_DTD_SYSTEM_ID =
        "http://www.sun.com/software/sunone/appserver/dtds/sun-cmp-mapping.dtd";

    public static final String SUN_CMP_MAPPING_800_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 OR Mapping//EN";
    public static final String SUN_CMP_MAPPING_800_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-cmp-mapping_1_1.dtd";

    public static final String SUN_CMP_MAPPING_810_DTD_PUBLIC_ID =
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN";
    public static final String SUN_CMP_MAPPING_810_DTD_SYSTEM_ID =
        "http://www.sun.com/software/appserver/dtds/sun-cmp-mapping_1_2.dtd";
}

// END OF IASRI 4661135

