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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectimport.eclipse.core.Workspace.Variable;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;

/**
 * @author Martin Krauskopf
 */
public class ClassPathParserTest extends NbTestCase {
    
    public ClassPathParserTest(String testName) {
        super(testName);
    }
    
    public void testParse_71770() throws Exception {
        DotClassPath cp = DotClassPathParser.parse(new File(getDataDir(), "71770.classpath"), Collections.<Link>emptyList());
        assertEquals("17 classpath entries", 17, cp.getClassPathEntries().size());
        assertEquals("1 sources entry", 1, cp.getSourceRoots().size());
        assertNotNull("non empty output", cp.getOutput());
        assertNotNull("non empty JDK", cp.getJREContainer());

        DotClassPathEntry entry = cp.getClassPathEntries().get(0);
        assertEquals("lib", entry.getProperty("kind"));
        assertEquals("C:/MyProjects/JavaAPI/integrationServerApi.jar", entry.getProperty("path"));
        assertEquals("jar:file:/C:/MyProjects/JavaAPI/docs/javaApiDoc.jar!/", entry.getProperty("javadoc_location"));
        
        EclipseProject ep = new EclipseProject(getWorkDir());
        ep.setClassPath(cp);
        entry = cp.getClassPathEntries().get(0);
        assertEquals("C:/MyProjects/JavaAPI/integrationServerApi.jar", entry.getAbsolutePath());
        
        entry = cp.getClassPathEntries().get(2);
        assertEquals("/MyProjects/JavaAPI/activation.jar", entry.getProperty("path"));
        assertEquals("/MyProjects/JavaAPI/activation.jar", entry.getAbsolutePath());
        entry = cp.getClassPathEntries().get(3);
        assertEquals("JavaAPI/axis.jar", entry.getProperty("path"));
        assertEquals(getWorkDirPath()+File.separatorChar+"JavaAPI/axis.jar", entry.getAbsolutePath());
    }
    
    public void testAccessrulesDoesNotCauseException() throws Exception { // #91669
        DotClassPath cp = DotClassPathParser.parse(new File(getDataDir(), "91669.classpath"), Collections.<Link>emptyList());
        assertEquals("one classpath entries", 0, cp.getClassPathEntries().size());
        assertNotNull("non empty JDK", cp.getJREContainer());
    }
    
    public void testParseExternalSourceRoots() throws Exception {
        Set<String> natures = new HashSet<String>();
        List<Link> links = new ArrayList<Link>();
        Set<Variable> variables = new HashSet<Variable>();
        variables.add(new Variable("SOME_ROOT", "/tmp"));
        ProjectParser.parse(new File(getDataDir(), "external-source-roots.project"), natures, links, variables);
        assertEquals(3, links.size());
        assertEquals(new Link("java-app-src", false, "/home/david/projs/JavaApplication1/src"), links.get(0));
        assertEquals(new Link("src2", false, "/tmp/JavaApplication2/src"), links.get(1));
        assertEquals(new Link("my_jars", false, "/tmp/libs"), links.get(2));
        
        DotClassPath cp = DotClassPathParser.parse(new File(getDataDir(), "external-source-roots.classpath"), links);
        assertEquals("3 sources entry", 3, cp.getSourceRoots().size());
        DotClassPathEntry entry = cp.getSourceRoots().get(0);
        assertEquals("src", entry.getProperty("path"));
        entry = cp.getSourceRoots().get(1);
        assertEquals("/tmp/JavaApplication2/src", entry.getProperty("path"));
        entry = cp.getSourceRoots().get(2);
        assertEquals("/home/david/projs/JavaApplication1/src", entry.getProperty("path"));
        assertEquals("1 cp entry", 1, cp.getClassPathEntries().size());
        entry = cp.getClassPathEntries().get(0);
        assertEquals("/tmp/libs/commons-lang-2.1.jar", entry.getProperty("path"));
    }

    public void testParseMalformedClasspath() {
        try {
            DotClassPathParser.parse(new File(getDataDir(), "180876.classpath"), Collections.<Link>emptyList());
            fail();
        } catch (IOException x) {
            // OK
        }
    }
    
}
