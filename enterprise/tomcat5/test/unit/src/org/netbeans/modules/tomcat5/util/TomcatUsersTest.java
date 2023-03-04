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
package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.FileWriter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;

/**
 *
 * @author sherold
 */
public class TomcatUsersTest extends NbTestCase {

    private final String CONTENT = "<tomcat-users>\n" +
                         "<user name='tomcat' password='tomcat' roles='tomcat,manager' />\n" +
                         "<user name='ide'  password='tomcat' roles='role1'  />\n" +
                         "<user name='test'  password='tomcat' roles='manager,admin,role1'  />\n" +
                         "</tomcat-users>\n";
    
    private final String CONTENT2 = "<tomcat-users>\n" +
                         "<user username='tomcat' password='tomcat' roles='tomcat,manager' />\n" +
                         "<user username='ide'  password='tomcat' roles='role1'  />\n" +
                         "<user username='test'  password='tomcat' roles='manager,admin,role1'  />\n" +
                         "</tomcat-users>\n";
    
    private final String CONTENT3 = "<tomcat-users>\n" +
                         "<user username='tomcat6' password='tomcat' roles='tomcat,manager' />\n" +
                         "<user username='tomcat7'  password='tomcat' roles='tomcat,manager-script'  />\n" +
                         "</tomcat-users>\n";    
    
    public TomcatUsersTest(String testName) {
        super(testName);
    }
    
    public void testHasRole() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "tomcat"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "test"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "tomcat"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "test"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        
        file = createTomcatUsersXml("tomcat-users3.xml", CONTENT3);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "tomcat6"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_70, file, "tomcat6"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_70, file, "tomcat7"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "tomcat7"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_80, file, "tomcat7"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_90, file, "tomcat7"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_100, file, "tomcat7"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_101, file, "tomcat7"));
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_110, file, "tomcat7"));
    }
    
    public void testCreateUser() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        TomcatUsers.createUser(file, "ide", "tomcat", TomcatVersion.TOMCAT_60);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "nonexisting"));
        TomcatUsers.createUser(file, "new", "tomcat", TomcatVersion.TOMCAT_60);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "new"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        TomcatUsers.createUser(file, "ide", "tomcat", TomcatVersion.TOMCAT_60);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "ide"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "nonexisting"));
        TomcatUsers.createUser(file, "new", "tomcat", TomcatVersion.TOMCAT_60);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "new"));
        
        file = createTomcatUsersXml("tomcat-users3.xml", CONTENT3);
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_110, file, "ide"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_101, file, "ide"));
        TomcatUsers.createUser(file, "tomcat6", "tomcat", TomcatVersion.TOMCAT_101);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_101, file, "tomcat7"));
        assertFalse(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_60, file, "nonexisting"));
        TomcatUsers.createUser(file, "new", "tomcat", TomcatVersion.TOMCAT_101);
        assertTrue(TomcatUsers.hasManagerRole(TomcatVersion.TOMCAT_101, file, "new"));
    }
    
    public void testUserExists() throws Exception {
        File file = createTomcatUsersXml("tomcat-users.xml", CONTENT);
        assertTrue(TomcatUsers.userExists(file, "tomcat"));
        assertTrue(TomcatUsers.userExists(file, "test"));
        assertFalse(TomcatUsers.userExists(file, "nonexisting"));
        
        file = createTomcatUsersXml("tomcat-users2.xml", CONTENT2);
        assertTrue(TomcatUsers.userExists(file, "tomcat"));
        assertTrue(TomcatUsers.userExists(file, "test"));
        assertFalse(TomcatUsers.userExists(file, "nonexisting"));
    }
    
    private File createTomcatUsersXml(String fileName, String content) throws Exception {
        File file = new File(getWorkDir(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

}
