/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class DefaultReplaceTokenProviderTest extends NbTestCase {

    public DefaultReplaceTokenProviderTest(String name) {
        super(name);
    }

    private FileObject d;

    @Override protected void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testTestSingle() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstTest.java", "package p1; class FirstTest {}");
        TestFileUtils.writeFile(d, "src/test/java/p1/SecondTest.java", "package p1; class SecondTest {}");
        TestFileUtils.writeFile(d, "src/test/java/p2/deeper/ThirdTest.java", "package p2.deeper; class ThirdTest {}");
        TestFileUtils.writeFile(d, "src/test/java/Suite.java", "class Suite {}");
        Project p = ProjectManager.getDefault().findProject(d);
        assertEquals("p1.FirstTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("Suite", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/Suite.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        // CLASSNAME no longer used in standard bindings (#147793); CLASSNAME_EXT probably never used; but for compatibility with custom bindings:
        assertEquals("FirstTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.CLASSNAME));
        assertEquals("FirstTest.java", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.CLASSNAME_EXT));
        // #212839: multiselections
        assertEquals("p1.FirstTest,p2.deeper.ThirdTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.fixed(d.getFileObject("src/test/java/p1/FirstTest.java"), d.getFileObject("src/test/java/p2/deeper/ThirdTest.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("p1.**.*Test", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("p2.deeper.**.*Test", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p2/deeper"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        // XXX TBD what should be passed for src/test/java/p2; is p2.*Test (i.e. nonrecursive) OK? If Surefire supports either, do we check for NonrecursiveFolder?
        // XXX test src/main/java selections
        // XXX test selections across groups, or outside groups
        // XXX test single methods
        
        //#213671
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.CLASSNAME));
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.CLASSNAME_EXT));
        
    }
    
    public void testNgSingle() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstNGTest.java", "package p1; class FirstNGTest {}");
        TestFileUtils.writeFile(d, "src/main/java/p1/First.java", "package p1; class First {}");
        
        Project p = ProjectManager.getDefault().findProject(d);
        assertEquals("p1.FirstNGTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/main/java/p1/First.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));

    }

}
