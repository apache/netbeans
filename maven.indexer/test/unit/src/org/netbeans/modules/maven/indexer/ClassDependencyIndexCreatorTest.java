/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;
import org.openide.util.test.JarBuilder;
import org.openide.util.test.TestFileUtils;

public class ClassDependencyIndexCreatorTest extends NexusTestBase {

    public ClassDependencyIndexCreatorTest(String n) {
        super(n);
    }

    public void testCrc32base64() throws Exception {
        assertEquals("ThFDsw", ClassDependencyIndexCreator.crc32base64("whatever"));
        assertEquals("tqQ_oA", ClassDependencyIndexCreator.crc32base64("mod1/Stuff"));
    }

    public void testFindClassUsages() throws Exception {
        installPOM("test", "mod1", "0", "jar");
        File mod1 = new JarBuilder(getWorkDir()).
                source("mod1.API", "public class API {}").
                source("mod1.Util", "public class Util {}").
                source("mod1.Stuff", "public class Stuff implements Outer {}").
                source("mod1.Outer", "public interface Outer {interface Inner {} interface Unused {}}").
                build();
        install(mod1, "test", "mod1", "0", "jar");
        installPOM("test", "mod2", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod2.Client", "class Client extends mod1.API {}").
                source("mod2.OtherClient", "class OtherClient extends mod1.API {}").
                source("mod2.Outer", "class Outer implements mod1.Outer, mod1.Outer.Inner {static class Inner implements mod1.Outer.Inner {}}").
                classpath(mod1).build(), "test", "mod2", "0", "jar");
        installPOM("test", "mod3", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod3.Client", "class Client extends mod1.API {}").
                classpath(mod1).build(), "test", "mod3", "0", "jar");
        // This is what nbm:populate-repository currently produces:
        install(TestFileUtils.writeFile(new File(getWorkDir(), "mod4.pom"),
                "<project><modelVersion>4.0.0</modelVersion>" +
                "<groupId>test</groupId><artifactId>mod4</artifactId>" +
                "<version>0</version></project>"), "test", "mod4", "0", "pom");
        install(new JarBuilder(getWorkDir()).
                source("mod4.Install", "class Install extends mod1.Util {}").
                classpath(mod1).build(), "test", "mod4", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod4.nbm"), "Info/info.xml:<whatever/>"), "test", "mod4", "0", "nbm");
        // And as produced by a Maven source build of a module:
        installPOM("test", "mod5", "0", "nbm");
        install(new JarBuilder(getWorkDir()).
                source("mod5.Install", "class Install extends mod1.Stuff {}").
                classpath(mod1).build(), "test", "mod5", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod5.nbm"), "Info/info.xml:<whatever/>"), "test", "mod5", "0", "nbm");
        nrii.indexRepo(info);
        // repo set up, now index and query:
        assertEquals("[test:mod2:0:test[mod2.Client, mod2.OtherClient], test:mod3:0:test[mod3.Client]]", nrii.findClassUsages("mod1.API", Collections.singletonList(info)).getResults().toString());
        List<ClassUsage> r = nrii.findClassUsages("mod1.Util", Collections.singletonList(info)).getResults();
        assertEquals("[test:mod4:0:test[mod4.Install]]", r.toString());
        assertEquals("jar", r.get(0).getArtifact().getType());
        r = nrii.findClassUsages("mod1.Stuff", Collections.singletonList(info)).getResults();
        assertEquals("[test:mod5:0:test[mod5.Install]]", r.toString());
        assertEquals("jar", r.get(0).getArtifact().getType());
        assertEquals("[]", nrii.findClassUsages("java.lang.Object", Collections.singletonList(info)).getResults().toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", nrii.findClassUsages("mod1.Outer", Collections.singletonList(info)).getResults().toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", nrii.findClassUsages("mod1.Outer$Inner", Collections.singletonList(info)).getResults().toString());
        assertEquals("[]", nrii.findClassUsages("mod1.Outer$Unused", Collections.singletonList(info)).getResults().toString());
        // XXX InnerClass attribute will produce spurious references to outer classes even when just an inner is used
    }

    public void testRead() throws Exception { // #206111
        File jar = TestFileUtils.writeZipFile(new File(getWorkDir(), "x.jar"),
                // XXX failed to produce a manifest that would generate a SecurityException if loaded with verify=true
                "pkg/Clazz.class:ABC");
        Map<String,byte[]> content = ClassDependencyIndexCreator.read(jar);
        assertEquals("[pkg/Clazz]", content.keySet().toString());
        assertEquals("[65, 66, 67]", Arrays.toString(content.get("pkg/Clazz")));
    }

}
