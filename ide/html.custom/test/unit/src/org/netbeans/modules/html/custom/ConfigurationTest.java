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
package org.netbeans.modules.html.custom;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.json.simple.JSONObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.custom.conf.Attribute;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author marek
 */
public class ConfigurationTest extends CslTestBase {

    private FileObject testAppRoot;
    private Project testProject;

    public ConfigurationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testAppRoot = getTestFile("testfiles/testHtmlApp");
        testProject = new TestProject(testAppRoot);
        MockLookup.setInstances(new FileOwnerQueryImpl(testAppRoot));
    }

    public void testLoad() {
        Project project = FileOwnerQuery.getOwner(testAppRoot);
        assertNotNull(project);

        Configuration conf = Configuration.get(project);
        assertNotNull(conf);
        
        //test elements
        Tag foo = conf.getTag("foo");
        assertNotNull(foo);
        Collection<String> contexts = foo.getContexts();
        assertNotNull(contexts);
        assertEquals(1, contexts.size());
        String ctx = contexts.iterator().next();
        assertEquals("*", ctx);
        
        assertNotNull(foo.getDescription());
        assertNotNull(foo.getDocumentation());
        assertNotNull(foo.getDocumentationURL());
        
        Attribute cool = foo.getAttribute("cool");
        assertNotNull(cool);
        assertEquals("boolean", cool.getType());
        
        Attribute clazz = foo.getAttribute("class");
        assertNotNull(clazz);
        assertEquals("css-class", clazz.getType());
        assertNotNull(clazz.getDocumentation());
        assertNotNull(clazz.getDocumentationURL());
        
        //test attributes
        Attribute one = conf.getAttribute("one");
        assertNotNull(one);
        
        contexts = one.getContexts();
        assertNotNull(contexts);
        assertEquals(1, contexts.size());
        ctx = contexts.iterator().next();
        assertEquals("*", ctx);
        assertEquals("boolean", one.getType());
        assertNotNull(one.getDescription());

    }

    public void testStore() throws IOException, BadLocationException {
        Project project = FileOwnerQuery.getOwner(testAppRoot);
        assertNotNull(project);

        Configuration conf = Configuration.get(project);
        assertNotNull(conf);

        JSONObject root = conf.store();
        assertNotNull(root);

    }

    public class TestProject implements Project {

        private final FileObject file;

        public TestProject(FileObject file) {
            this.file = file;
        }

        @Override
        public FileObject getProjectDirectory() {
            return file;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

    public class FileOwnerQueryImpl implements FileOwnerQueryImplementation {

        private final FileObject projectRoot;

        public FileOwnerQueryImpl(FileObject projectRoot) {
            this.projectRoot = projectRoot;
        }

        @Override
        public Project getOwner(URI file) {
            return null;
        }

        @Override
        public Project getOwner(FileObject file) {
            return projectRoot.equals(file) || FileUtil.isParentOf(projectRoot, file) ? testProject : null;
        }

    }

}
