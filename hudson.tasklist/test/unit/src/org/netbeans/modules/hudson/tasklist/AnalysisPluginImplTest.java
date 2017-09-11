/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.tasklist;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AnalysisPluginImplTest extends NbTestCase {
    
    public AnalysisPluginImplTest(String n) {
        super(n);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testLocate() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        FileUtil.createData(d, "build.xml");
        FileUtil.createData(d, "src/p/C.java");
        FileUtil.createData(d, "test/build.xml");
        FileUtil.createData(d, "test/p/CTest.java");
        Collection<FileObject> roots = Arrays.asList(d, d.getFileObject("src"), d.getFileObject("test"));
        assertEquals(d.getFileObject("build.xml"), AnalysisPluginImpl.locate("/h/workspace/myjob/build.xml", roots));
        assertEquals(d.getFileObject("src/p/C.java"), AnalysisPluginImpl.locate("/h/workspace/myjob/src/p/C.java", roots));
        assertEquals(d.getFileObject("test/p/CTest.java"), AnalysisPluginImpl.locate("/h/workspace/myjob/test/p/CTest.java", roots));
        assertEquals(d.getFileObject("src/p/C.java"), AnalysisPluginImpl.locate("/tmp/clover123.tmp/p/C.java", roots));
        assertEquals(d.getFileObject("test/p/CTest.java"), AnalysisPluginImpl.locate("/tmp/clover456.tmp/p/CTest.java", roots));
        assertEquals(null, AnalysisPluginImpl.locate("/h/workspace/myjob/src/p/X.java", roots));
        assertEquals(null, AnalysisPluginImpl.locate("/junk", roots));
        assertEquals(null, AnalysisPluginImpl.locate("huh?!", roots));
    }

    public void testWorkspacePath() throws Exception {
        assertEquals("trunk/src/main/org/apache/tools/ant/taskdefs/optional/net/FTPTask.java", AnalysisPluginImpl.workspacePath("/x1/jenkins/jenkins-slave/workspace/Ant_Nightly/trunk/src/main/org/apache/tools/ant/taskdefs/optional/net/FTPTask.java", "Ant_Nightly"));
        assertEquals("src/p/C.java", AnalysisPluginImpl.workspacePath("C:\\hudson\\workspace\\some job\\src\\p\\C.java", "some job"));
        assertEquals(null, AnalysisPluginImpl.workspacePath("/tmp/whatever", "j"));
        assertEquals("src/p/C.java", AnalysisPluginImpl.workspacePath("/hudson/workdir/jobs/j/workspace/src/p/C.java", "j"));
    }

}
