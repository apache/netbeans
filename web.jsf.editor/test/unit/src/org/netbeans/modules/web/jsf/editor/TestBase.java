/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.jsf.editor;

import java.net.URL;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache.JsfBeansProvider;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelProvider;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.netbeans.modules.web.jsf.impl.metamodel.JsfModelProviderImpl;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Marek Fukala
 */
public class TestBase extends CslTestBase {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N

    public TestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        super.setUp();
    }

    protected BaseDocument createDocument() {
        NbEditorDocument doc = new NbEditorDocument(HtmlKit.HTML_MIME_TYPE);
        doc.putProperty(PROP_MIME_TYPE, HtmlKit.HTML_MIME_TYPE);
        doc.putProperty(Language.class, HTMLTokenId.language());
        return doc;
    }

    protected Document[] createDocuments(String... fileName) {
        try {
            List<Document> docs = new ArrayList<Document>();
            FileSystem memFS = FileUtil.createMemoryFileSystem();
            for (String fName : fileName) {

                //we may also create folders
                StringTokenizer items = new StringTokenizer(fName, "/");
                FileObject fo = memFS.getRoot();
                while (items.hasMoreTokens()) {
                    String item = items.nextToken();
                    if (items.hasMoreTokens()) {
                        //folder
                        fo = fo.createFolder(item);
                    } else {
                        //last, create file
                        fo = fo.createData(item);
                    }
                    assertNotNull(fo);
                }

                DataObject dobj = DataObject.find(fo);
                assertNotNull(dobj);

                EditorCookie cookie = dobj.getCookie(EditorCookie.class);
                assertNotNull(cookie);

                Document document = (Document) cookie.openDocument();
                assertEquals(0, document.getLength());

                docs.add(document);

            }
            return docs.toArray(new Document[]{});
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting up tests", ex);
        }
    }

    public Document getDefaultDocument(FileObject fo) throws DataObjectNotFoundException, IOException {
        DataObject dobj = DataObject.find(fo);
        assertNotNull(dobj);

        EditorCookie cookie = dobj.getCookie(EditorCookie.class);
        assertNotNull(cookie);

        Document document = (Document) cookie.openDocument();
        return document;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }

    public ParseResultInfo parse(String fileName) throws ParseException, IOException {
        FileObject file = getWorkFile(fileName);

        assertNotNull(file);

        Source source = getTestSource(file);
        assertNotNull(source);

        final ParseResultInfo[] _result = new ParseResultInfo[]{new ParseResultInfo()};
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0].topLevelSnapshot = resultIterator.getSnapshot();
                _result[0].result = (HtmlParserResult) WebUtils.getResultIterator(resultIterator, "text/html").getParserResult();
            }
        });
        
        assertNotNull(_result[0].topLevelSnapshot);
        assertNotNull(_result[0].result);

        return _result[0];
    }

    protected FileObject getWorkFile(String path) throws IOException {
        File wholeInputFile = new File(getWorkDir(), path);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);
        return fo;
    }
  
    protected class TestClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;

        public TestClassPathProvider(Map<String, ClassPath> map) {
            this.map = map;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (map != null) {
                return map.get(type);
            } else {
                return null;
            }
        }
    }

    protected Map<String, ClassPath> createClassPaths() throws Exception {
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        ClassPath cp = createServletAPIClassPath();
        cps.put(ClassPath.COMPILE, cp);
        cps.put(ClassPath.SOURCE, cp);
        cps.put(ClassPath.BOOT, createBootClassPath());
        return cps;
    }

    /**
     * Creates boot {@link ClassPath} for platform the test is running on,
     * it uses the sun.boot.class.path property to find out the boot path roots.
     * @return ClassPath
     * @throws java.io.IOException when boot path property contains non valid path
     */
    public static ClassPath createBootClassPath() throws IOException {
        String bootPath = System.getProperty("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL> roots = new ArrayList<URL>(paths.length);
        for (String path : paths) {
            File f = new File(path);
            if (!f.exists()) {
                continue;
            }
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add(url);
//            System.out.println(url);
        }
//        System.out.println("-----------");
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    public final ClassPath createServletAPIClassPath() throws MalformedURLException, IOException {
        String path = System.getProperty("web.project.jars");
        String[] st = PropertyUtils.tokenizePath(path);
        List<FileObject> fos = new ArrayList<FileObject>();
        for (int i = 0; i < st.length; i++) {
            String token = st[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file " + token);
            }
            FileObject fo = FileUtil.toFileObject(f);
            fos.add(FileUtil.getArchiveRoot(fo));
        }
        return ClassPathSupport.createClassPath(fos.toArray(new FileObject[fos.size()]));
    }

    protected static class FakeWebModuleProvider implements WebModuleProvider {

        private FileObject webRoot, javaSources;

        public FakeWebModuleProvider(FileObject webRoot, FileObject javaSources) {
            this.webRoot = webRoot;
        }

        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(new FakeWebModuleImplementation2(webRoot, javaSources));
        }
    }

    private static class FakeWebModuleImplementation2 implements WebModuleImplementation2 {

        private FileObject webRoot, javaSources;

        public FakeWebModuleImplementation2(FileObject webRoot, FileObject javaSources) {
            this.webRoot = webRoot;
            this.javaSources = javaSources;
        }

        public FileObject getDocumentBase() {
            return webRoot;
        }

        public String getContextPath() {
            return "/";
        }

        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        public FileObject getWebInf() {
            return null;
        }

        public FileObject getDeploymentDescriptor() {
            return null;
        }

        public FileObject[] getJavaSources() {
            return new FileObject[]{javaSources};
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
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
            FileObject testproject = dir.getFileObject("web");
            return testproject != null && testproject.isFolder();
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
            ic.add(new JsfModelProviderImpl(this));

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

    protected class TestJsfBeansProvider implements JsfBeansProvider {

        private List<? extends FacesManagedBean> beans;

        public TestJsfBeansProvider(List<? extends FacesManagedBean> beans) {
            this.beans = beans;
        }

        public List<FacesManagedBean> getBeans(Project project) {
            return (List<FacesManagedBean>) beans;
        }
    }

    protected static class FacesManagedBeanImpl implements FacesManagedBean {

        private String name, clazz;

        public FacesManagedBeanImpl(String name, String clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public Boolean getEager() {
            return true; //???
        }

        public String getManagedBeanName() {
            return name;
        }

        public String getManagedBeanClass() {
            return clazz;
        }

        public Scope getManagedBeanScope() {
            return Scope.REQUEST;
        }

        public String getManagedBeanScopeString() {
            return getManagedBeanScope().toString(); //???
        }

        public List<ManagedProperty> getManagedProperties() {
            return Collections.emptyList();
        }
    }

    protected static class TestUserCatalog extends UserCatalog {

        @Override
        public EntityResolver getEntityResolver() {
            return new EntityResolver() {

                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    //avoid the content to be loaded from network by resolving each resource to empty string
                    return new InputSource(new StringReader(""));
                }
            };
        }
    }

    protected static class ParseResultInfo {
        public Snapshot topLevelSnapshot;
        public HtmlParserResult result;
    }
}
