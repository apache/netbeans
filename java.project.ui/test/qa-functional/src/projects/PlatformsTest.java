/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
