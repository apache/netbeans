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

package org.netbeans.modules.maven.queries;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

public class MavenAnnotationProcessingQueryImplTest extends NbTestCase {

    public MavenAnnotationProcessingQueryImplTest(String name) {
        super(name);
    }

    private FileObject wd;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
    }

    private void assertOpts(String body, String expected, String root) throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>" + body + "</project>");
        FileObject rootFO = FileUtil.createFolder(wd, root);
        assertNotNull(rootFO);
        AnnotationProcessingQuery.Result r = AnnotationProcessingQuery.getAnnotationProcessingOptions(rootFO);
        URL sOD = r.sourceOutputDirectory();
        Map<String,String> opts = new TreeMap<String,String>(r.processorOptions());
        assertEquals("false", opts.remove("eclipselink.canonicalmodel.use_static_factory"));
        assertEquals(expected,
                "enabled=" + r.annotationProcessingEnabled() +
                " run=" + r.annotationProcessorsToRun() +
                " s=" + (sOD != null ? sOD.toString().replace(Utilities.toURI(getWorkDir()).toString(), ".../") : "-") +
                " opts=" + opts);
    }

    public void testDefaults() throws Exception {
        assertOpts("", "enabled=[ON_SCAN, IN_EDITOR] run=null s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testProcNone() throws Exception {
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<configuration><compilerArgument>-proc:none</compilerArgument></configuration></plugin></plugins></build>",
            "enabled=[] run=null s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testNewCompiler() throws Exception {
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<version>2.2</version></plugin></plugins></build>",
            "enabled=[ON_SCAN, IN_EDITOR] run=null s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testOldCompiler() throws Exception {
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<version>2.1</version></plugin></plugins></build>",
            "enabled=[] run=null s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testCustomOutputLocationDirect() throws Exception {
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<configuration><generatedSourcesDirectory>${project.build.directory}/generated-sources/annos</generatedSourcesDirectory></configuration></plugin></plugins></build>",
                "enabled=[ON_SCAN, IN_EDITOR] run=null s=.../target/generated-sources/annos/ opts={}", "src/main/java");
    }

    public void testCustomOutputLocationImplicit() throws Exception {
        assertOpts("<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory></build>",
                "enabled=[ON_SCAN, IN_EDITOR] run=null s=.../build/maven/prj/target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testExplicitProcessors() throws Exception {
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<configuration><annotationProcessors><annotationProcessor>p1.Proc1</annotationProcessor><annotationProcessor>p2.Proc2</annotationProcessor></annotationProcessors></configuration></plugin></plugins></build>",
            "enabled=[ON_SCAN, IN_EDITOR] run=[p1.Proc1, p2.Proc2] s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
    }

    public void testTestRoots() throws Exception { // #208286
        String pom = "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><executions>"
                + "<execution><id>main</id><goals><goal>compile</goal></goals><configuration><annotationProcessors><annotationProcessor>p.MainProc</annotationProcessor></annotationProcessors></configuration></execution>"
                + "<execution><id>tests</id><goals><goal>testCompile</goal></goals><configuration><annotationProcessors><annotationProcessor>p.TestProc</annotationProcessor></annotationProcessors></configuration></execution>"
                + "</executions></plugin></plugins></build>";
        assertOpts(pom, "enabled=[ON_SCAN, IN_EDITOR] run=[p.MainProc] s=.../target/generated-sources/annotations/ opts={}", "src/main/java");
        assertOpts(pom, "enabled=[ON_SCAN, IN_EDITOR] run=[p.TestProc] s=.../target/generated-sources/test-annotations/ opts={}", "src/test/java");
    }

    public void testArgs() throws Exception {
        // Note MCOMPILER-135: <Averbose>true</Averbose> will only work in 2.4+ plugin
        assertOpts("<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId>"
                + "<configuration><compilerArguments><Aflag/><Averbose>true</Averbose></compilerArguments><compilerArgument>-Awhich=this</compilerArgument></configuration></plugin></plugins></build>",
            "enabled=[ON_SCAN, IN_EDITOR] run=null s=.../target/generated-sources/annotations/ opts={flag=null, verbose=true, which=this}", "src/main/java");
    }
    
}
