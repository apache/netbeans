/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.el;

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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import static org.netbeans.modules.csl.api.test.CslTestBase.getCaretOffset;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
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

/**
 * @author Marek Fukala
 */
public class ELTestBase extends CslTestBase {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N

    public ELTestBase(String name) {
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
        return new ELLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/xhtml";
    }

    public void checkCompletion(final String file, final String caretLine, final boolean includeModifiers, final List<String> toCheck) throws Exception {
        // TODO call TestCompilationInfo.setCaretOffset!
        final CodeCompletionHandler.QueryType type = CodeCompletionHandler.QueryType.COMPLETION;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == CodeCompletionHandler.QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk =
                            org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset ) {
                                if (upToOffset) {
                                    prefix = doc.getText(start, caretOffset - start);
                                } else {
                                    prefix = doc.getText(start, blk[1] - start);
                                }
                            }
                        }
                    }
                }

                final int finalCaretOffset = caretOffset;
                final String finalPrefix = prefix;
                final ParserResult finalParserResult = pr;
                CodeCompletionContext context = new CodeCompletionContext() {

                    @Override
                    public int getCaretOffset() {
                        return finalCaretOffset;
                    }

                    @Override
                    public ParserResult getParserResult() {
                        return finalParserResult;
                    }

                    @Override
                    public String getPrefix() {
                        return finalPrefix;
                    }

                    @Override
                    public boolean isPrefixMatch() {
                        return true;
                    }

                    @Override
                    public CodeCompletionHandler.QueryType getQueryType() {
                        return type;
                    }

                    @Override
                    public boolean isCaseSensitive() {
                        return caseSensitive;
                    }
                };

                CodeCompletionResult completionResult = cc.complete(context);
                List<CompletionProposal> proposals = completionResult.getItems();

                final boolean deprecatedHolder[] = new boolean[1];
                final HtmlFormatter formatter = new HtmlFormatter() {
                    private StringBuilder sb = new StringBuilder();

                    @Override
                    public void reset() {
                        sb.setLength(0);
                    }

                    @Override
                    public void appendHtml(String html) {
                        sb.append(html);
                    }

                    @Override
                    public void appendText(String text, int fromInclusive, int toExclusive) {
                        sb.append(text, fromInclusive, toExclusive);
                    }

                    @Override
                    public void emphasis(boolean start) {
                    }

                    @Override
                    public void active(boolean start) {
                    }

                    @Override
                    public void name(ElementKind kind, boolean start) {
                    }

                    @Override
                    public void parameters(boolean start) {
                    }

                    @Override
                    public void type(boolean start) {
                    }

                    @Override
                    public void deprecated(boolean start) {
                        deprecatedHolder[0] = true;
                    }

                    @Override
                    public String getText() {
                        return sb.toString();
                    }
                };

                String described = describeCompletion(caretLine, pr.getSnapshot().getSource().createSnapshot().getText().toString(), caretOffset, true, caseSensitive, type, proposals, includeModifiers, deprecatedHolder, formatter, toCheck);
                assertDescriptionMatches(file, described, true, ".completion");
            }
        });
    }

    private String describeCompletion(String caretLine, String text, int caretOffset, boolean prefixSearch, boolean caseSensitive, CodeCompletionHandler.QueryType type, List<CompletionProposal> proposals,
            boolean includeModifiers, boolean[] deprecatedHolder, final HtmlFormatter formatter, final List<String> toCheck) {
        assertTrue(deprecatedHolder != null && deprecatedHolder.length == 1);
        StringBuilder sb = new StringBuilder();
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", prefixSearch=" + prefixSearch + ", caseSensitive=" + caseSensitive + ")");
        sb.append("\n");

        // Sort to make test more stable
        proposals.sort(new Comparator<CompletionProposal>() {

            public int compare(CompletionProposal p1, CompletionProposal p2) {
                // Smart items first
                if (p1.isSmart() != p2.isSmart()) {
                    return p1.isSmart() ? -1 : 1;
                }

                if (p1.getKind() != p2.getKind()) {
                    return p1.getKind().compareTo(p2.getKind());
                }

                formatter.reset();
                String p1L = p1.getLhsHtml(formatter);
                formatter.reset();
                String p2L = p2.getLhsHtml(formatter);

                if (!p1L.equals(p2L)) {
                    return p1L.compareTo(p2L);
                }

                formatter.reset();
                String p1Rhs = p1.getRhsHtml(formatter);
                formatter.reset();
                String p2Rhs = p2.getRhsHtml(formatter);
                if (p1Rhs == null) {
                    p1Rhs = "";
                }
                if (p2Rhs == null) {
                    p2Rhs = "";
                }
                if (!p1Rhs.equals(p2Rhs)) {
                    return p1Rhs.compareTo(p2Rhs);
                }

                // Yuck - tostring comparison of sets!!
                if (!p1.getModifiers().toString().equals(p2.getModifiers().toString())) {
                    return p1.getModifiers().toString().compareTo(p2.getModifiers().toString());
                }

                return 0;
            }
        });

        boolean isSmart = true;
        for (CompletionProposal proposal : proposals) {
            if (!toCheck.contains(proposal.getName())) {
                continue;
            }
            if (isSmart && !proposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }

            deprecatedHolder[0] = false;
            formatter.reset();
            proposal.getLhsHtml(formatter); // Side effect to deprecatedHolder used
            boolean strike = includeModifiers && deprecatedHolder[0];

            String n = proposal.getKind().toString();
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }

//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

            sb.append(" ");

            formatter.reset();
            n = proposal.getLhsHtml(formatter);
            int MAX_LHS = 30;
            if (strike) {
                MAX_LHS -= 6; // Account for the --- --- strikethroughs
                sb.append("---");
            }
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            if (strike) {
                sb.append("---");
            }

            sb.append("  ");

            assertNotNull("Return Collections.emptySet() instead from getModifiers!", proposal.getModifiers());
            if (proposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = proposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");

            formatter.reset();
            sb.append(proposal.getRhsHtml(formatter));
            sb.append("\n");

            isSmart = proposal.isSmart();
        }

        return sb.toString();
    }

    private String getSourceLine(String s, int offset) {
        int begin = offset;
        if (begin > 0) {
            begin = s.lastIndexOf('\n', offset-1);
            if (begin == -1) {
                begin = 0;
            } else if (begin < s.length()) {
                begin++;
            }
        }
        if (s.length() == 0) {
            return s;
        }
//        s.charAt(offset);
        int end = s.indexOf('\n', begin);
        if (end == -1) {
            end = s.length();
        }

        if (offset < end) {
            return (s.substring(begin, offset)+"|"+s.substring(offset,end)).trim();
        } else {
            return (s.substring(begin, end) + "|").trim();
        }
    }

    public ParseResultInfo parse(String fileName) throws ParseException {
        FileObject file = getTestFile(fileName);

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
        return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
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
        return ClassPathSupport.createClassPath(fos.toArray(new FileObject[0]));
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
            return Profile.JAVA_EE_8_FULL;
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

    protected static class ParseResultInfo {
        public Snapshot topLevelSnapshot;
        public HtmlParserResult result;
    }
}
