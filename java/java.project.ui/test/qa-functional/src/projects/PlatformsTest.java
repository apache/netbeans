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

/*
 * TestCreatePlatforms.java
 * NetBeans JUnit based test
 *
 * Created on 14 September 2004, 15:37
 */

package projects;

import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import org.netbeans.jellytools.JellyTestCase;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
//import org.netbeans.junit.ide.ProjectSupport;


import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 *
 */
public class PlatformsTest extends JellyTestCase {

    public static final String JDK13_NAME = "JDK1.3";
    public static final String JDK14_NAME = "JDK1.4";
    public static final String JDK15_NAME = "JDK1.5";
    
    public PlatformsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PlatformsTest.class).
                addTest("testCreatePlatforms",
                        "testAvailablePlatforms").
                enableModules(".*").clusters(".*"));
    }
        
    // -------------------------------------------------------------------------
    
    public void testAvailablePlatforms() {
        
        JavaPlatformManager platMan = JavaPlatformManager.getDefault();
        JavaPlatform platforms[] = platMan.getInstalledPlatforms();
        String[] platNames = new String[platforms.length];
        for (int i = 0; i < platforms.length; i++) {
            System.out.println("Display Name: " + platforms[i].getDisplayName());
            platNames[i] = platforms[i].getDisplayName();
        }
        // there should be test if all added platforms are really added in IDE
        
    }
    
    // TODO Javadoc can be also added to platform
    public void testCreatePlatforms() {
        
        // learn hostname
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            fail("Cannot get hostname: " + uhe.getMessage()); // NOI18N
        }
        hostName = hostName.replace('-', '_');
        
        // load platforms.properties file
        InputStream is = this.getClass().getResourceAsStream("platforms.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (java.io.IOException ioe) {
            fail("Cannot load platforms properties: " + ioe.getMessage()); // NOI18N
        }
        
        // get folder from prop file
        
        // XXX add correct paths to platform.properties
        String folderJDK13Path = props.getProperty(hostName + "_jdk13_folder");
        TestProjectUtils.addPlatform(JDK13_NAME, folderJDK13Path);
        String folderJDK14Path = props.getProperty(hostName + "_jdk14_folder");
        TestProjectUtils.addPlatform(JDK14_NAME, folderJDK14Path);
        String folderJDK15Path = props.getProperty(hostName + "_jdk15_folder");
        TestProjectUtils.addPlatform(JDK15_NAME, folderJDK15Path);
        
    }
    
}
