/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
