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

package org.netbeans.modules.maven.model;

import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class UtilitiesTest extends NbTestCase {

    public UtilitiesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    protected @Override String logRoot() {
        return Utilities.class.getName();
    }

    public void testPerformPOMModelOperations() throws Exception {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                model.getProject().addModule("child1");
                model.getProject().addModule("child2");
            }
        }));
        assertEquals("<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "    <modules>\n" +
                "        <module>child1</module>\n" +
                "        <module>child2</module>\n" +
                "    </modules>\n" +
                "</project>\n",
                pom.asText().replace("\r\n", "\n"));
    }

    public void testPerformNothing() throws Exception {
        FileObject pom = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "p0m.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>grp</groupId>\n" +
                "    <artifactId>art</artifactId>\n" +
                "    <version>1.0</version>\n" +
                "</project>\n");
        CharSequence log = Log.enable(logRoot(), Level.FINE);
        Utilities.performPOMModelOperations(pom, Collections.<ModelOperation<POMModel>>emptyList());
        assertFalse(log.toString(), log.toString().contains("changes in"));
    }

}
