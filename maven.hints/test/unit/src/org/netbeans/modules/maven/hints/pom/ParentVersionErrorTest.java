/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom;

import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ParentVersionErrorTest extends NbTestCase {
    
    public ParentVersionErrorTest(String n) {
        super(n);
    }

    private FileObject work;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        work = FileUtil.toFileObject(getWorkDir());
    }

    public void testBasicUsage() throws Exception {
        TestFileUtils.writeFile(work, "pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>common</artifactId>\n" +
                "    <version>1.1</version>\n" +
                "</project>\n");
        FileObject pom = TestFileUtils.writeFile(work, "prj/pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <parent>\n" +
                "        <groupId>grp</groupId>\n" +
                "        <artifactId>common</artifactId>\n" +
                "        <version>1.0</version>\n" +
                "    </parent>\n" +
                "    <version>1.0</version>\n" +
                "    <artifactId>prj</artifactId>\n" +
                "</project>\n");
        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project prj = ProjectManager.getDefault().findProject(pom.getParent());
        assertEquals(1, new ParentVersionError().getErrorsForDocument(model, prj).size());
    }

    public void testSpecialRelativePath() throws Exception { // #194281
        TestFileUtils.writeFile(work, "common.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>common</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        FileObject pom = TestFileUtils.writeFile(work, "prj/pom.xml", "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <parent>\n" +
                "        <groupId>grp</groupId>\n" +
                "        <artifactId>common</artifactId>\n" +
                "        <relativePath>../common.xml</relativePath>\n" +
                "    </parent>\n" +
                "    <artifactId>prj</artifactId>\n" +
                "</project>\n");
        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project prj = ProjectManager.getDefault().findProject(pom.getParent());
        assertEquals(Collections.<ErrorDescription>emptyList(), new ParentVersionError().getErrorsForDocument(model, prj));
    }

}
