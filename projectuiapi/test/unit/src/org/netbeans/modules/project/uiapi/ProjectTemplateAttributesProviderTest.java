/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.uiapi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class ProjectTemplateAttributesProviderTest extends NbTestCase {

    private FileObject scratch;
    private FileObject folder;
    private FileObject projdir;

    public ProjectTemplateAttributesProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        folder = scratch.createFolder("folder");

        projdir = scratch.createFolder("proj");
        
        createProject(projdir);

        MockLookup.setInstances(new FEQImpl(), new TestProjectFactory());
        assertEquals(FEQImpl.ENCODING, FileEncodingQuery.getEncoding(folder).name());
    }

    private void createProject(FileObject projdir) throws Exception {
        TestUtil.createFileFromContent(ProjectTemplateAttributesProviderTest.class.getResource("data/test.txt"), projdir, "nbproject/test.txt");
        TestUtil.createFileFromContent(ProjectTemplateAttributesProviderTest.class.getResource("data/test.txt"), projdir, "src/test/test.txt");
    }
    
    /**
     * Checks that the attribute providers execute in the correct order and see other provider's data.
     * Legacy providers should execute first. New providers should execute after that. Each new-style
     * provider should see all attributes defined by previous providers (legacy or new).
     * 
     * @throws Exception 
     */
    public void testProjectAttributeProviders() throws Exception {
        Project prj = ProjectManager.getDefault().findProject(projdir);
        FileObject folder = projdir.getFileObject("nbproject");
        FileObject template = FileUtil.toFileObject(getDataDir()).getFileObject("file.txt");
        Map<String, Object> init = new HashMap();
        init.put("mama", "se raduje");
        FileObject result = FileBuilder.createFromTemplate(template, folder, "honza", init, FileBuilder.Mode.FORMAT);
        
        assertEquals(
                "Jedna, 2, Honza jde. Nese 2 pytle s brouky. Mama se raduje, ze bude pect vdolky.\n",
                result.asText());
    }
    

    public void testCheckProjectAttrs() throws Exception {
        Map<String, ? extends Object> checked = ProjectTemplateAttributesProvider.checkProjectAttrs(null, null, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");

        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, map, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        map.put("project", Collections.emptyMap());
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, map, folder);
        assertAttribute("default", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        map.put("project", Collections.singletonMap("license", "gpl"));
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, map, folder);
        assertAttribute("gpl", checked, "license");
        assertAttribute(FEQImpl.ENCODING, checked, "encoding");
        assertEquals("bar", checked.get("foo"));

        Map<String, String> projectMap = new HashMap<String, String>();
        projectMap.put("license", "gpl");
        projectMap.put("encoding", "UTF-8");
        map.put("project", projectMap);
        checked = ProjectTemplateAttributesProvider.checkProjectAttrs(map, map, folder);
        assertAttribute("gpl", checked, "license");
        assertAttribute("UTF-8", checked, "encoding");
        assertEquals("bar", checked.get("foo"));
    }

    private static void assertAttribute(String expected, Map<String, ? extends Object> map, String attribute) {
        Map<String, Object> attrs = NbCollections.checkedMapByFilter((Map) map.get("project"), String.class, Object.class, false);
        assertEquals(expected, attrs.get(attribute));
    }

    private final class FEQImpl extends FileEncodingQueryImplementation {

        public static final String ENCODING = "ISO-8859-1";

        @Override
        public Charset getEncoding(FileObject file) {
            if (file == folder) {
                return Charset.forName(ENCODING);
            }
            return null;
        }
    }
    
    private static final class AttrProv1 implements CreateFromTemplateAttributes {

        @Override
        public Map<String, ?> attributesFor(CreateDescriptor desc) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("jedna", 2); // used by Prov2
            m.put("dve", "Honza jde");
            return m;
        }
        
    }
    
    private static final class AttrProv2 implements CreateFromTemplateAttributes {
        @Override
        public Map<String, ?> attributesFor(CreateDescriptor desc) {
            String s = desc.getValue("pytel");
            s += " brouky";
            Map m = new HashMap();
            m.put("pytel", s); // replace /append to legacy-provided value
            m.put("nese", desc.getValue("jedna")); // copy previous value
            return m;
        }
    }
    
    private static final class AttrProvLegacy implements CreateFromTemplateAttributesProvider {
        @Override
        public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
            Map m = new HashMap();
            m.put("pytel", "s"); // appended by Prov2
            m.put("bude", "bude pect vdolky");
            return m;
        }
    }

    private static final class TestProject implements Project {
        
        private final Lookup l;
        private final FileObject projectDirectory;
        
        TestProject(FileObject projectDirectory) throws IOException {
            l = Lookups.fixed(new AttrProv1(), new AttrProv2(), new AttrProvLegacy());
            this.projectDirectory = projectDirectory;
        }
        
        public FileObject getProjectDirectory() {
            return projectDirectory;
        }
        
        public Lookup getLookup() {
            return l;
        }
        
        public String toString() {
            return "TestAntBasedProject[" + getProjectDirectory() + "]";
        }
        
    }
    
    public static class TestProjectFactory implements ProjectFactory {
        
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject("nbproject") != null;
        }
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory))
                return new TestProject(projectDirectory);
            
            return null;
        }
        
        public void saveProject(Project project) throws IOException, ClassCastException {
        }
        
    }
}
