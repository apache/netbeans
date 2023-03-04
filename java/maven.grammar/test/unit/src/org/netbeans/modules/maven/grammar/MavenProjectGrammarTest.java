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
package org.netbeans.modules.maven.grammar;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author tomas
 */
public class MavenProjectGrammarTest extends NbTestCase {
    public MavenProjectGrammarTest(String n) {
        super(n);
    }

    public void testGetNodeValue() throws IOException {
        File pom = new File(getWorkDir(), "pom.xml"); 
        
        String gid = "${gid}";
        String gidProp = "org.apache.wicket";
        writeFile(pom, gid, gidProp);        
        assertEquals(gidProp, MavenProjectGrammar.getNodeValue(gid, getProject(pom).getProjectWatcher()));
        
        pom = new File(getWorkDir() + "1", "pom.xml"); 
        gid = "${gid}t";
        gidProp = "org.apache.wicke";
        writeFile(pom, gid, gidProp);        
        assertEquals(gidProp + "t", MavenProjectGrammar.getNodeValue(gid, getProject(pom).getProjectWatcher()));
        
        pom = new File(getWorkDir() + "2", "pom.xml"); 
        gid = "o${gid}";
        gidProp = "rg.apache.wicket";
        writeFile(pom, gid, gidProp);        
        assertEquals("o" + gidProp, MavenProjectGrammar.getNodeValue(gid, getProject(pom).getProjectWatcher()));
        
        pom = new File(getWorkDir() + "3", "pom.xml"); 
        gid = "o${gid1}apa${gid2}t";
        gidProp = "rg.";
        String gidProp2 = "che.wicke";             
        TestFileUtils.writeFile(pom,
                "<project><modelVersion>4.0.0</modelVersion>"
                        + "<groupId>" + gid + "</groupId>"
                        + "<artifactId>prj</artifactId>"
                        + "<version>1.0</version>"
                        + "<properties>"
                        + "<gid1>" + gidProp + "</gid1>\n" 
                        + "<gid2>" + gidProp2 + "</gid2>\n" 
                        + "</properties>"
                        + "</project>");
        assertEquals("o" + gidProp + "apa" + gidProp2 + "t", MavenProjectGrammar.getNodeValue(gid, getProject(pom).getProjectWatcher()));

        pom = new File(getWorkDir() + "4", "pom.xml"); 
        gid = "org.apache.wicket";
        writeFile(pom, gid, gid);        
        assertEquals(gid, MavenProjectGrammar.getNodeValue(gid, getProject(pom).getProjectWatcher()));
    }

    protected void writeFile(File pom, String gid, String gidProp) throws IOException {
        TestFileUtils.writeFile(pom,
                "<project><modelVersion>4.0.0</modelVersion>"
                        + "<groupId>" + gid + "</groupId>"
                        + "<artifactId>prj</artifactId>"
                        + "<version>1.0</version>"
                        + "<properties>"
                        + "<gid>" + gidProp + "</gid>\n" 
                        + "</properties>"
                        + "</project>");
    }

    protected static NbMavenProjectImpl getProject(File pom) {
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(pom));
        return ((NbMavenProjectImpl)p);
    }
    
}
