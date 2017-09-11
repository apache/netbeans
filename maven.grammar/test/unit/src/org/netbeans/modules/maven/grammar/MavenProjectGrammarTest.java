/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
