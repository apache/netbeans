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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
        return file;
    }

}
