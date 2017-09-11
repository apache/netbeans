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

package org.netbeans.modules.maven.model.pom.impl;

import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Resource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ResourceImplTest extends NbTestCase {
    
    public ResourceImplTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testIncludes() throws Exception { // #198361
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                Resource res = model.getFactory().createResource();
                res.setTargetPath("META-INF"); //NOI18N
                res.setDirectory("src"); //NOI18N
                res.addInclude("stuff/"); //NOI18N
                Build build = model.getFactory().createBuild();
                build.addResource(res);
                model.getProject().setBuild(build);
            }
        }));
        assertEquals("<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "    <build>\n" +
                "        <resources>\n" +
                "            <resource>\n" +
                "                <targetPath>META-INF</targetPath>\n" +
                "                <directory>src</directory>\n" +
                "                <includes>\n" +
                "                    <include>stuff/</include>\n" +
                "                </includes>\n" +
                "            </resource>\n" +
                "        </resources>\n" +
                "    </build>\n" +
                "</project>\n",
                pom.asText().replace("\r\n", "\n"));
    }

}
