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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import junit.framework.AssertionFailedError;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.*;

/**
 * Test for SortSuiteModule
 * @author pzajac
 */
public class SortSuiteModulesTest extends NbTestCase {
    private Project project;
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";
    String e = "e";
    String f = "f";
    String g = "g";
    String SORTED_MODULES = "sorted_modules";
    String NULL[] = new String[0];

    public SortSuiteModulesTest(java.lang.String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        project = new Project();
        project.setBaseDir(getWorkDir());
    }


    public void testOnlyModuleDependencies() throws IOException {

        // a -> b means: a depends on b

        // a-> b,c
        // b -> d,e
        // f -> g
        createModule(g,NULL);
        createModule(d,NULL);
        createModule(c,NULL);
        createModule(e,NULL);
        createModule(a,new String[]{b,c});
        createModule(b,new String[]{e,d});
        createModule(f,new String[]{g});

        Path path = createPath(new String[]{a,b,c,d,e,f,g});
        SortSuiteModules ssm = new SortSuiteModules();
        ssm.setProject(project);
        ssm.setUnsortedModules(path);
        ssm.setSortedModulesProperty(SORTED_MODULES);
        ssm.execute();

        String property = project.getProperty(SORTED_MODULES);
        assertNotNull("null sorted modules path",property);
        String paths[] = getSorted(property);

        assertEdge(paths,a,b);
        assertEdge(paths,a,c);
        assertEdge(paths,b,d);
        assertEdge(paths,b,e);
        assertEdge(paths,f,g);
    }
    public void testModuleDependenciesCycle() throws IOException {
        createModule(a,new String[]{b});
        createModule(b,new String[]{a});
        Path path = createPath(new String[]{a,b});
        SortSuiteModules ssm = new SortSuiteModules();
        ssm.setProject(project);
        ssm.setUnsortedModules(path);
        ssm.setSortedModulesProperty(SORTED_MODULES);
        try {
            ssm.execute();
            fail("Exception must be thrown");
        } catch(BuildException be) {
            // ok
        }
    }
    public void testModuleAndTestDependenciesDisabledTestSort() throws IOException {
        generateTestModules1(false);

        String property = project.getProperty(SORTED_MODULES);
        assertNotNull("null sorted modules path",property);
        String paths[] = getSorted(property);

        assertEdge(paths,a,b);
        assertEdge(paths,a,c);
        assertEdge(paths,b,d);
        assertEdge(paths,b,e);
        assertEdge(paths,f,g);
        try {
            assertEdge(paths,b,g);
            fail("sort test deps disabled");
        } catch (AssertionFailedError be) {}
    }
    public void testModuleAndTestDependenciesEnabledTestSort() throws IOException {
        generateTestModules1(true);

        String property = project.getProperty(SORTED_MODULES);
        assertNotNull("null sorted modules path",property);
        String paths[] = getSorted(property);

        assertEdge(paths,a,b);
        assertEdge(paths,a,c);
        assertEdge(paths,b,d);
        assertEdge(paths,b,e);
        assertEdge(paths,b,g);
        assertEdge(paths,f,g);
    }

    private void generateTestModules1(boolean sortTests) throws IOException, BuildException {

        // a -> b means: a depends on b

        // a-> b,c
        // b -> d,e, unittest g
        // f -> g
        createModule(g,NULL);
        createModule(d,NULL);
        createModule(c,NULL);
        createModule(e,NULL);
        createModule(a,new String[]{b,c});
        createModule(b,new String[]{e,d},new String[]{g},NULL);
        createModule(f,new String[]{g});

        Path path = createPath(new String[]{a,b,c,d,e,f,g});
        SortSuiteModules ssm = new SortSuiteModules();
        ssm.setProject(project);
        ssm.setUnsortedModules(path);
        ssm.setSortedModulesProperty(SORTED_MODULES);
        if (sortTests) {
            ssm.setSortTests(true);
        }
        ssm.execute();
    }

    private void createModule(String module, String[] mdeps) throws IOException {
        createModule(module,mdeps,new String[0],new String[0]);
    }

    /** create module/nbbuild/project.xml
     * @param module module and cnd
     * @param mdeps runtime dependencies
     * @param udeps test unit dependencies with tests
     * @param qadeps qa-functional dependencies with tests
     */
    private void createModule(String module, String[] mdeps, String[] udeps, String[] qadeps) throws IOException {
        File dir = new File(getWorkDir(),module + File.separator + "nbproject");
        assertTrue("cannot create module dir",dir.mkdirs());
        File xml = new File(dir,"project.xml");
        PrintStream ps = new PrintStream(xml);
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println("<project xmlns=\"http://www.netbeans.org/ns/project/1\">");
        ps.println("    <type>org.netbeans.modules.apisupport.project</type>");
        ps.println("    <configuration>");
        ps.println("        <data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">");
        ps.println("            <code-name-base>" + module + "</code-name-base>");
        ps.println("            <module-dependencies>");
        for (int it = 0 ; it < mdeps.length ; it++) {
            ps.println("                <dependency>");
            ps.println("                    <code-name-base>" + mdeps[it] + "</code-name-base>");
            ps.println("                    <build-prerequisite/>");
            ps.println("                </dependency>");
        }
        ps.println("            </module-dependencies>");
        ps.println("          <test-dependencies>");
        ps.println("              <test-type>");
        ps.println("                  <name>unit</name>");
        for (int it = 0 ; it < udeps.length ; it++ ) {
            ps.println("                  <test-dependency>");
            ps.println("                      <code-name-base>" + udeps[it] + "</code-name-base>");
            ps.println("                      <test/>");
            ps.println("                  </test-dependency>");
        }
        ps.println("              </test-type>");
        ps.println("              <test-type>");
        ps.println("                  <name>qa-functional</name>");
        for (int it = 0 ; it < qadeps.length ; it++ ) {
            ps.println("                  <test-dependency>");
            ps.println("                      <code-name-base>" + qadeps[it] + "</code-name-base>");
            ps.println("                      <test/>");
            ps.println("                  </test-dependency>");
        }
        ps.println("              </test-type>");
        ps.println("          </test-dependencies>");
        ps.println("            <public-packages/>");
        ps.println("        </data>");
        ps.println("    </configuration>");
        ps.println("</project>");
    }

    private Path createPath(String[] paths) {
        Path path = new Path(project);
        StringBuffer sb = new StringBuffer();
        for (int it = 0; it < paths.length; it++) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            sb.append(paths[it]);
        }
        path.setPath(sb.toString());
        return path;
    }

    private String[] getSorted(String property) {
        Path path = new Path(project);
        path.setPath(property);
        String paths[] = path.list();

        String rets [] = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            rets[i] = new File(paths[i]).getName();

        }
        return rets;
    }

    private void assertEdge(String[] names, String a, String b) {
         assertTrue(a + " after " + b + " in " + Arrays.toString(names), getIndex(names,a) > getIndex(names,b));
    }

    private int getIndex(String[] names, String a) {
        for (int i = 0; i < names.length; i++) {
            log(names[i]);
            if (names[i].equals(a)) {
                return i;
            }
        }
        fail("index " + a);
        return -1;
    }

    public void testTestDependenciesCycleEnabledTestSort() throws IOException {
        createModule(a,new String[]{b},new String[]{b},NULL);
        createModule(b,NULL,new String[]{a},NULL);
        Path path = createPath(new String[]{a,b});
        SortSuiteModules ssm = new SortSuiteModules();
        ssm.setProject(project);
        ssm.setUnsortedModules(path);
        ssm.setSortedModulesProperty(SORTED_MODULES);
        ssm.setSortTests(true);
        try {
            ssm.execute();
            fail("Exception must be thrown");
        } catch(BuildException be) {
            // ok
        }
    }
    public void testTestDependenciesCycleDisabledTestSort() throws IOException {
        createModule(a,new String[]{b},new String[]{b},NULL);
        createModule(b,NULL,new String[]{a},NULL);
        Path path = createPath(new String[]{a,b});
        SortSuiteModules ssm = new SortSuiteModules();
        ssm.setProject(project);
        ssm.setUnsortedModules(path);
        ssm.setSortedModulesProperty(SORTED_MODULES);
        // no exception
        ssm.execute();
    }
}
