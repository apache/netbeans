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
package org.netbeans.modules.html.angular.editor;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.csl.api.DeclarationFinder;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Pisl
 */
public class AngularJsDelcarationFinderTest extends JsTestBase {
    
    public AngularJsDelcarationFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;

        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), JsTokenId.javascriptLanguage());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
        
        FileObject srcFo = getTestFile("angularTestProject");
        Sources sources = new TestSources(srcFo);
        ClassPathProvider classpathProvider = new TestClassPathProvider(srcFo, createClassPathsForTest());
        MockLookup.setInstances(new TestProjectFactory(classpathProvider, sources));
        
        IndexingManager.getDefault().refreshIndexAndWait(srcFo.getURL(), null, true);
        
    }
    
    public void testDeclaration_01() throws Exception {
        checkDeclaration("angularTestProject/public_html/gotoTest.html", "<body ng-controller=\"Ph^oneListCtrl\">", "controllers.js", 79);
    }
 
    //TODO problem with scanning    
//    public void testTemplateUrl_01() throws Exception {
//        checkDeclaration("angularTestProject/public_html/js/app.js", "templateUrl: 'partials/p^hone-detail.html',", "phone-detail.html", 0);
//    }
//    
//    public void testTemplateUrl_02() throws Exception {
//        checkDeclaration("angularTestProject/public_html/js/app.js", "templateUrl: 'partials^/phone-list.html?v=1',", "phone-list.html", 0);
//    }
    
    protected DeclarationFinder.DeclarationLocation findDeclaration(String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        enforceCaretOffset(testSource, caretOffset);

        final DeclarationFinder.DeclarationLocation [] location = new DeclarationFinder.DeclarationLocation[] { null };
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                DeclarationFinder finder = getFinder();
                location[0] = finder.findDeclaration(pr, caretOffset);
            }
        });
        return location[0];
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>(/*ClasspathProviderImplAccessor.getJsStubs()*/);
        
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/angularTestProject/public_html")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }

    protected final class TestProjectFactory implements ProjectFactory {

        private ClassPathProvider provider;
        private Sources sources;

        public  TestProjectFactory(ClassPathProvider provider, Sources sources) {
            this.provider = provider;
            this.sources = sources;
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return new TestProject(projectDirectory, state, provider, sources );
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        public boolean isProject(FileObject dir) {
            return true;
        }
    }
    
    protected static class TestProject implements Project {

        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        private Lookup lookup;

        public TestProject(FileObject dir, ProjectState state, ClassPathProvider classpathProvider, Sources sources) {
            this.dir = dir;
            this.state = state;

            InstanceContent ic = new InstanceContent();
            ic.add(classpathProvider);
            ic.add(sources);

            this.lookup = new AbstractLookup(ic);

        }

        public Lookup getLookup() {
            return lookup;
        }

        public FileObject getProjectDirectory() {
            return dir;
        }

        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
    }
    
    protected final class TestSources implements Sources {

        private FileObject[] roots;

        TestSources(FileObject... roots) {
            this.roots = roots;
        }

        public SourceGroup[] getSourceGroups(String type) {
            SourceGroup[] sg = new SourceGroup[roots.length];
            for (int i = 0; i < roots.length; i++) {
                sg[i] = new TestSourceGroup(roots[i]);
            }
            return sg;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
    protected final class TestSourceGroup implements SourceGroup {

        private FileObject root;

        public TestSourceGroup(FileObject root) {
            this.root = root;
        }

        public FileObject getRootFolder() {
            return root;
        }

        public String getName() {
            return root.getNameExt();
        }

        public String getDisplayName() {
            return getName();
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return FileUtil.getRelativePath(root, file) != null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    
    private static class TestClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;
        private FileObject root;

        public TestClassPathProvider(FileObject root, Map<String, ClassPath> map) {
            this.map = map;
            this.root = root;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (FileUtil.isParentOf(root, file)) {
                if (map != null) {
                    return map.get(type);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
