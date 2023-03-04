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
package org.netbeans.test.web.core.syntax;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Document;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPProcessor;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.SimplifiedJspServlet;
import org.netbeans.modules.web.core.syntax.gsf.JspEmbeddingProvider;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;



/**
 * @author ads
 *
 */
public class ContextJspServletGenTest extends TestBase2 {
    
    private static final String TEST_FOLDER_CONTEXT_JPS = "testContextJsps";

    public ContextJspServletGenTest( String name ) {
        super(name);
    }
    
    public void testBasicInclude() throws Exception {
        generateServlet("basicInclude");
    }
    
    protected void generateServlet( String prjFolder ) throws Exception{
        String jsp = prjFolder +"/index.jsp";
        SimplifiedJspServlet processor = getProcessor( jsp );
        processor.process();
        Embedding servlet = processor.getSimplifiedServlet();
        assertServletMatches( prjFolder , servlet.getSnapshot().getText().toString());
    }
    
    protected void generateServlet( String testPrj, String fileName )throws Exception{
        SimplifiedJspServlet processor = getProcessor( testPrj +"/"+fileName);
        processor.process();
        Embedding servlet = processor.getSimplifiedServlet();
        assertServletMatches( testPrj , fileName , 
                servlet.getSnapshot().getText().toString(), true);
    }
    
    protected void assertServletMatches( String testFolder , String content ) throws Exception{
        assertServletMatches(testFolder, "index.jsp", content, false);
    }
    
    protected void assertServletMatches( String testFolder , String fileName , 
            String content , boolean addFileName ) throws Exception
    {
        String prjPath = TEST_FOLDER_CONTEXT_JPS+"/"+testFolder;
        String newFile;
        if (addFileName) {
            int i = fileName.lastIndexOf('.');
            if (i > -1) {
                newFile = "."+fileName.substring(0, i);
            }
            else {
                newFile = "."+fileName;
            }
        }
        else {
            newFile = "";
        }
        newFile = testFolder +newFile+".java";                      // NOI18N
        assertFileContentsMatches(prjPath +"/"+fileName, TEST_FOLDER_CONTEXT_JPS+
                "/" +newFile,content);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JSPProcessor.ignoreLockFromUnitTest = true;
        myClassPathProvider = new TestClassPathProvider(createClassPaths(
                getTestFile(TEST_FOLDER_CONTEXT_JPS)));
        MockLookup.setInstances( myClassPathProvider,
                new TestLanguageProvider(), 
                new TestWebModuleProvider(getTestFile(TEST_FOLDER_CONTEXT_JPS),
                        myClassPathProvider));
        initParserJARs();
        
        // init TestLanguageProvider
        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        TestLanguageProvider.register(CssTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());
        TestLanguageProvider.register(JsTokenId.javascriptLanguage());
        
        JspIndentTaskFactory jspReformatFactory = new JspIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JspKit("text/x-jsp"), 
                jspReformatFactory, new EmbeddingProviderImpl.Factory(), 
                new JspEmbeddingProvider.Factory());

    }
    
    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            assertNotNull(ec);

            return (BaseDocument) ec.openDocument();
        }
        catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }
    
    private SimplifiedJspServlet getProcessor( String fileName ){
        FileObject fo = getTestFile(TEST_FOLDER_CONTEXT_JPS +"/" +fileName );
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        SimplifiedJspServlet processor = new SimplifiedJspServlet( 
                createSnaphot(doc) , doc );
        return processor;
    }
    
    private Snapshot createSnaphot(Document doc){
        return Source.create( doc ).createSnapshot();
    }
    
    private Map<String, ClassPath> createClassPaths( FileObject fo ) 
        throws Exception 
    {
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        ClassPath cp = createServletAPIClassPath();
        cps.put(ClassPath.COMPILE, cp);
        
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath( fo ));
        cps.put( ClassPath.BOOT , createClassPath("sun.boot.class.path"));
        return cps;
    }
    
    private static class TestWebModuleProvider implements WebModuleProvider {

        public TestWebModuleProvider(FileObject webRoot, ClassPathProvider provider) {
            myWebRoot = webRoot;
            myClassPathProvider = provider;
        }

        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(
                    new TestWebModuleImplementation2(myWebRoot, myClassPathProvider));
        }
        
        private FileObject myWebRoot;
        private ClassPathProvider myClassPathProvider;
        
    }

    private static class TestWebModuleImplementation2 
        implements WebModuleImplementation2 
    {
        private static final String WEB_INF = "WEB-INF";    // NOI18N
        private static final String DD      = "web.xml";    // NOI18N

        public TestWebModuleImplementation2(FileObject webRoot, 
                ClassPathProvider provider ) 
        {
            myWebRoot = webRoot;
            myClassPathProvider = provider; 
        }

        public FileObject getDocumentBase() {
            return myWebRoot;
        }

        public String getContextPath() {
            return "/";
        }

        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        public FileObject getWebInf() {
            return myWebRoot.getFileObject( WEB_INF );
        }

        public FileObject getDeploymentDescriptor() {
            if ( getWebInf() == null ){
                return null;
            }
            return getWebInf().getFileObject( DD );
        }

        public FileObject[] getJavaSources() {
            return new FileObject[]{ myWebRoot };
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            if (myWebAppMetadataModel == null) {
                FileObject ddFO = getDeploymentDescriptor();
                File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
                MetadataUnit metadataUnit = MetadataUnit.create(
                    myClassPathProvider.findClassPath( myWebRoot , ClassPath.BOOT),
                    myClassPathProvider.findClassPath( myWebRoot ,ClassPath.COMPILE),
                    myClassPathProvider.findClassPath( myWebRoot ,ClassPath.SOURCE),
                    ddFile);
                myWebAppMetadataModel = WebAppMetadataModelFactory.
                    createMetadataModel(metadataUnit, true);
            }
            return myWebAppMetadataModel;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
        private FileObject myWebRoot;
        private ClassPathProvider myClassPathProvider;
        private MetadataModel<WebAppMetadata> myWebAppMetadataModel;

    }
    
    private ClassPathProvider myClassPathProvider;

}
