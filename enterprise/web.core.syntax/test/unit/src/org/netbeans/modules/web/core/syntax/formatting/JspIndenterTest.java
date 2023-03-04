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
package org.netbeans.modules.web.core.syntax.formatting;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.core.GsfIndentTaskFactory;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.jsploader.TagLibParseSupport;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseCookie;
import org.netbeans.modules.web.core.syntax.EmbeddingProviderImpl;
import org.netbeans.modules.web.core.syntax.JSPProcessor;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.gsf.JspEmbeddingProvider;
import org.netbeans.modules.web.core.syntax.indent.ExpressionLanguageIndentTaskFactory;
import org.netbeans.modules.web.core.syntax.indent.JspIndentTaskFactory;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.netbeans.test.web.core.syntax.TestBase2;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class JspIndenterTest extends TestBase2 {

    private static TestLanguageProvider testLanguageProvider = null;

    public JspIndenterTest(String name) {
        super(name);
        if (testLanguageProvider == null) {
            testLanguageProvider = new TestLanguageProvider();
        }
    }

    public static Test xsuite() throws IOException, BadLocationException {
        TestSuite suite = new TestSuite();
        suite.addTest(new JspIndenterTest("testFormattingCase005"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JSPProcessor.ignoreLockFromUnitTest = true;
        MockLookup.setInstances(new TestClassPathProvider(createClassPaths()),
            testLanguageProvider, new FakeWebModuleProvider(getTestFile("testfilesformatting")));
        initParserJARs();
        
        AbstractIndenter.inUnitTestRun = true;

        // init TestLanguageProvider
        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        TestLanguageProvider.register(CssTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());
        TestLanguageProvider.register(JsTokenId.javascriptLanguage());

        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/css"), cssFactory);
        JspIndentTaskFactory jspReformatFactory = new JspIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), new JspKit("text/x-jsp"), jspReformatFactory);
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"));
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory, new JavacParserFactory(), new ClassParserFactory());
        ExpressionLanguageIndentTaskFactory elReformatFactory = new ExpressionLanguageIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-el"), elReformatFactory);
        GsfIndentTaskFactory jsFactory = new GsfIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), jsFactory);
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because I've already done in setUp()
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            assertNotNull(ec);

            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    private Map<String, ClassPath> createClassPaths() throws Exception {
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        ClassPath cp = createServletAPIClassPath();
        cps.put(ClassPath.COMPILE, cp);
        return cps;
    }

    private class TestClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;

        public TestClassPathProvider(Map<String, ClassPath> map) {
            this.map = map;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (map != null) {
                return map.get(type);
            } else {
                return null;
            }
        }
    }

    public void testFormattingCase001() throws Exception {
        reformatFileContents("testfilesformatting/case001.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase002() throws Exception {
        reformatFileContents("testfilesformatting/case002.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase003() throws Exception {
        reformatFileContents("testfilesformatting/case003.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase004() throws Exception {
        reformatFileContents("testfilesformatting/case004.jsp", new IndentPrefs(4, 4));
    }

// commenting out for now: it keep failing because of test setup - 
// taglib library is not lexed or something
//
//    public void testFormattingCase005() throws Exception {
//        reformatFileContents("testfilesformatting/case005.jsp", new IndentPrefs(4, 4));
//    }

    public void testFormattingCase006() throws Exception {
        reformatFileContents("testfilesformatting/case006.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase007() throws Exception {
        reformatFileContents("testfilesformatting/case007.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase008() throws Exception {
        reformatFileContents("testfilesformatting/case008.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase009() throws Exception {
        reformatFileContents("testfilesformatting/case009.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingCase010() throws Exception {
        reformatFileContents("testfilesformatting/case010.jsp", new IndentPrefs(4, 4));
    }

// randomly fails; JavaScript formatter related
//    public void testFormattingCase011() throws Exception {
//        reformatFileContents("testfilesformatting/case011.jsp", new IndentPrefs(4, 4));
//    }

    public void testFormattingCase012() throws Exception {
        reformatFileContents("testfilesformatting/case012.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue121102() throws Exception {
        reformatFileContents("testfilesformatting/issue121102.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue129778() throws Exception {
        reformatFileContents("testfilesformatting/issue129778.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue89174() throws Exception {
        reformatFileContents("testfilesformatting/issue89174.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue160098() throws Exception {
        reformatFileContents("testfilesformatting/issue160098.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue160103() throws Exception {
        reformatFileContents("testfilesformatting/issue160103.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue160527() throws Exception {
        reformatFileContents("testfilesformatting/issue160527.jsp", new IndentPrefs(4, 4));
    }

// fails in JavaScript section; commenting out for now
//    public void testFormattingIssue162017() throws Exception {
//        reformatFileContents("testfilesformatting/issue162017.jsp", new IndentPrefs(4, 4));
//    }

    public void testFormattingIssue162031() throws Exception {
        reformatFileContents("testfilesformatting/issue162031.jsp", new IndentPrefs(4, 4));
    }

    public void testFormattingIssue230077() throws Exception {
        reformatFileContents("testfilesformatting/issue230077.jsp", new IndentPrefs(4, 4));
    }

    @RandomlyFails
    public void testIndentation() throws Exception {
        insertNewline("<style>\n     h1 {\n        <%= System.\n   somth() ^%>",
                      "<style>\n     h1 {\n        <%= System.\n   somth() \n        ^%>", null);

        //#160092:
        insertNewline("^<html>\n</html>\n", "\n^<html>\n</html>\n", null);

        insertNewline("<jsp:useBean>^", "<jsp:useBean>\n    ^", null);
        insertNewline("^<jsp:body>", "\n^<jsp:body>", null);

        insertNewline("<jsp:body>\n    <html>^", "<jsp:body>\n    <html>\n        ^", null);
        insertNewline("<jsp:body>\n^<html>", "<jsp:body>\n\n    ^<html>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            ^", null);
        insertNewline("<jsp:body>\n    <html>\n^<jsp:useBean>", "<jsp:body>\n    <html>\n\n        ^<jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n            <table>\n                ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n\n            ^<table>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>a\n^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>a\n\n        ^</jsp:useBean>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>a^</jsp:useBean>", "<jsp:body>\n    <html>\n        <jsp:useBean>a\n        ^</jsp:useBean>", null);

        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>^",
                "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    </html>\n    ^", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^</html>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n    ^</html>", null);
        insertNewline("<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>^<table>", "<jsp:body>\n    <html>\n        <jsp:useBean>\n        </jsp:useBean>\n        ^<table>", null);

        insertNewline("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>^",
                "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\"\n        prefix=\"c\" %>\n^", null);

        // TODO: impl matching of INDENT/RETURN and use it to properly match incorrect document:
        //insertNewline("<jsp:body>\n    <html>^</jsp:body>", "<jsp:body>\n    <html>\n^</jsp:body>", null);

        insertNewline("<!--\n   comment\n^-->\n", "<!--\n   comment\n\n^-->\n", null);
        insertNewline(
                "<html> <!--^comment",
                "<html> <!--\n       ^comment", null);
        insertNewline(
                "<html> <!--\n             ^comment",
                "<html> <!--\n             \n       ^comment", null);

        // expression indentation:
        insertNewline(
                "<html>\n    ${\"expression+\n           exp2\"}^",
                "<html>\n    ${\"expression+\n           exp2\"}\n    ^", null);
        insertNewline(
                "<html>\n    some text ${\"expression+\n                         exp2\"^}",
                "<html>\n    some text ${\"expression+\n                         exp2\"\n    ^}", null);
        insertNewline(
                "<html>\n    ${\"expression+\n           exp2\"\n                }^",
                "<html>\n    ${\"expression+\n           exp2\"\n                }\n                ^", null);
        // #176397
        insertNewline(
                "<html>^${\"expression\"}</html>",
                "<html>\n    ^${\"expression\"}</html>", null);
        insertNewline(
                "<html>^${\"expression\"}",
                "<html>\n    ^${\"expression\"}", null);

//        // #167228
//        insertNewline(
//                "<div>\n    <div>\n       ${jdks}\n    </div>\n</div>\n^aaaa",
//                "<div>\n    <div>\n       ${jdks}\n    </div>\n</div>\n\n^aaaa", null);

// #128034
//        insertNewline(
//            "<a href=\"${path}\">^</a>",
//            "<a href=\"${path}\">\n    ^\n</a>", null);

        insertNewline("<html>\n    <head>\n        <script type=\"text/javascript\">\n            function a() {\n                <%%>\n            }\n        </script>^",
                      "<html>\n    <head>\n        <script type=\"text/javascript\">\n            function a() {\n                <%%>\n            }\n        </script>\n        ^", null);
        
        insertNewline("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html <%=\nSystem.getenv(\"aaa\")\n%> ^/>",
                      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html <%=\nSystem.getenv(\"aaa\")\n%> \n^/>", null);
    }

    
    @Override
    protected BaseDocument getDocument(FileObject fo) {
        try {
            //this create an instance of JspKit which initializes the coloring data but lazily, not parsed yet here
            EditorCookie ec = DataLoadersBridge.getDefault().getCookie(fo, EditorCookie.class);
            BaseDocument bdoc = (BaseDocument) ec.openDocument();

            //force parse and wait
            TagLibParseSupport pc = (TagLibParseSupport) DataLoadersBridge.getDefault().getCookie(fo, TagLibParseCookie.class);
            pc.getCachedParseResult(false, false, true);
            //get the parser coloring data
            JspColoringData data = pc.getJSPColoringData();

            //set correct values to the document's input attributes
            InputAttributes inputAttributes = (InputAttributes) bdoc.getProperty(InputAttributes.class);
            JspParseData jspParseData = (JspParseData) inputAttributes.getValue(LanguagePath.get(JspTokenId.language()), JspParseData.class);
            jspParseData.updateParseData(data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());
            //mark as initialized
            jspParseData.initialized();

            //any token hierarchy taken from this document should see the correct lexing - based on parsing results
            return bdoc;
        } catch (IOException ex) {
            fail(ex.toString());
            return null;
        }
    }

    private static class FakeWebModuleProvider implements WebModuleProvider {

        private FileObject webRoot;

        public FakeWebModuleProvider(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(new FakeWebModuleImplementation2(webRoot));
        }
        
    }

    private static class FakeWebModuleImplementation2 implements WebModuleImplementation2 {

        private FileObject webRoot;

        public FakeWebModuleImplementation2(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public FileObject getDocumentBase() {
            return webRoot;
        }

        @Override
        public String getContextPath() {
            return "/";
        }

        @Override
        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        @Override
        public FileObject getWebInf() {
            return null;
        }

        @Override
        public FileObject getDeploymentDescriptor() {
            return null;
        }

        @Override
        public FileObject[] getJavaSources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MetadataModel<WebAppMetadata> getMetadataModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

    }
}
