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
