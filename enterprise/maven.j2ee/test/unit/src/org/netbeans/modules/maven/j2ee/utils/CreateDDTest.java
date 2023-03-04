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
package org.netbeans.modules.maven.j2ee.utils;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestBase;

/**
 * Tests related to creating deployment descriptor using MavenProjectSupport class
 * @author Martin Janicek
 */
public class CreateDDTest extends JavaEEMavenTestBase {

    public CreateDDTest(String name) {
        super(name);
    }


    /***********************************************************************************************************
     * Calling createDDIfRequired with server set in auxiliary properties but without passing him as a parameter.
     ***********************************************************************************************************/
    public void testCreateDDIfRequired_nullServerPassed_webLogic() {
        MavenProjectSupport.setServerID(project, WEBLOGIC);
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_6_FULL);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(true, isWebDDpresent(project));
    }

    // gfv5ee8 - Java EE 8
    public void testCreateDDIfRequired_nullServerPassed_glassfish() {
        MavenProjectSupport.setServerID(project, GLASSFISH);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv510ee8 - Jakarta EE 8
    public void testCreateDDIfRequired_nullServerPassed_glassfish51() {
        MavenProjectSupport.setServerID(project, ECLIPSEGLASSFISH);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv6ee9 - Jakarta EE 9
    public void testCreateDDIfRequired_nullServerPassed_glassfish6() {
        MavenProjectSupport.setServerID(project, ECLIPSEGLASSFISH6);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv610ee9 - Jakarta EE 9.1
    public void testCreateDDIfRequired_nullServerPassed_glassfish61() {
        MavenProjectSupport.setServerID(project, ECLIPSEGLASSFISH610);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }
        
    // gfv700ee10 - Jakarta EE 10
    public void testCreateDDIfRequired_nullServerPassed_glassfish7() {
        MavenProjectSupport.setServerID(project, ECLIPSEGLASSFISH7);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }

    public void testCreateDDIfRequired_nullServerPassed_tomcat() {
        MavenProjectSupport.setServerID(project, TOMCAT);
        MavenProjectSupport.createWebXMLIfRequired(project, null);

        assertEquals(false, isWebDDpresent(project));
    }

    public void testCreateDDIfRequired_nullServerPassed_jboss() {
        MavenProjectSupport.setServerID(project, JBOSS);
        MavenProjectSupport.createWebXMLIfRequired(project, null);
        
        assertEquals(false, isWebDDpresent(project));
    }



    /****************************************************************************
     * Calling createDDIfRequired with server passed to the method as a parameter
     ****************************************************************************/
    public void testCreateDDIfRequired_weblogicPassed() {
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_6_FULL);
        MavenProjectSupport.createWebXMLIfRequired(project, WEBLOGIC);
        assertEquals(true, isWebDDpresent(project));
    }

    // gfv5ee8 - Java EE 8
    public void testCreateDDIfRequired_glassfishPassed() {
        MavenProjectSupport.createWebXMLIfRequired(project, GLASSFISH);
        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv510ee8 - Jakarta EE 8
    public void testCreateDDIfRequired_glassfish51Passed() {
        MavenProjectSupport.createWebXMLIfRequired(project, ECLIPSEGLASSFISH);
        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv6ee9 - Jakarta EE 9
    public void testCreateDDIfRequired_glassfish6Passed() {
        MavenProjectSupport.createWebXMLIfRequired(project, ECLIPSEGLASSFISH6);
        assertEquals(false, isWebDDpresent(project));
    }
    
    // gfv6ee9 - Jakarta EE 9.1
    public void testCreateDDIfRequired_glassfish61Passed() {
        MavenProjectSupport.createWebXMLIfRequired(project, ECLIPSEGLASSFISH6);
        assertEquals(false, isWebDDpresent(project));
    }

    // gfv700ee10 - Jakarta EE 10
    public void testCreateDDIfRequired_glassfish7Passed() {
        MavenProjectSupport.createWebXMLIfRequired(project, ECLIPSEGLASSFISH7);
        assertEquals(false, isWebDDpresent(project));
    }

    public void testCreateDDIfRequired_tomcatPassed() {
        MavenProjectSupport.createWebXMLIfRequired(project, TOMCAT);
        assertEquals(false, isWebDDpresent(project));
    }

    public void testCreateDDIfRequired_jbossPassed() {
        MavenProjectSupport.createWebXMLIfRequired(project, JBOSS);
        assertEquals(false, isWebDDpresent(project));
    }
}
