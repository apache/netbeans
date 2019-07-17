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
package org.netbeans.modules.xml.text.completion;

import java.lang.reflect.Constructor;
import java.net.URL;
import javax.swing.Icon;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionResultSetImpl;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.api.model.DescriptionSource;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 * Checks behaviour of {@link GrammarResult} support in the ElementResultItem.
 * plain CompletionDocumentation must work unchanged. If mixin interface is implemented,
 * explicit values (content, resolved links) must take precedence.
 * <p/>
 * If contentURL is provided, links must be resolved and content loaded by the 
 * ElementResultItem support even though the supplied CompletionDocumentation itself does not 
 * provide content/links.
 * 
 * @author sdedic
 */
public class ElementResultItemDocumentationTest extends NbTestCase {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ElementResultItemDocumentationTest.class);
        return suite;
    }

    public ElementResultItemDocumentationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private CompletionResultSetImpl rsImpl;
    
    
    private CompletionResultSet resultSetFor(CompletionTask task, int type) throws Exception {
        rsImpl = createCompletionResultImpl(task, type);
        return rsImpl.getResultSet();
    }
    
    private CompletionResultSetImpl createCompletionResultImpl(CompletionTask task, int type) throws Exception {
        Class<?> clazz = CompletionResultSetImpl.class;
        Constructor ctor = clazz.getDeclaredConstructor(CompletionImpl.class, Object.class, CompletionTask.class, Integer.TYPE);
        ctor.setAccessible(true);
        return (CompletionResultSetImpl)ctor.newInstance(CompletionImpl.get(), "", task, type);
    }
    
    /**
     * Check the previou state, result items without the mixin interface. 
     * Should return the description 'as is'
     * 
     * @throws Exception 
     */
    private static final String PLAIN_DESCRIPTION_TEXT = "Plain description";
    
    public void testPlainContent() throws Exception {
        MockGrammarResult r = new MockGrammarResult();
        
        r.setDescription(PLAIN_DESCRIPTION_TEXT);
        ElementResultItem item = new ElementResultItem(0, r);
        CompletionTask task = item.doCreateDocumentationTask(r);

        CompletionResultSet rs = resultSetFor(task, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
        task.query(rs);
        
        assertTrue(rs.isFinished());
        
        assertEquals(PLAIN_DESCRIPTION_TEXT, rsImpl.getDocumentation().getText());
     
        // should return null from the url.
        assertNull(rsImpl.getDocumentation().getURL());
        
        // should not be able to resolve links
        assertNull(rsImpl.getDocumentation().resolveLink("link"));
    }
    
    private URL  createResourceName(String n) {
        return ElementResultItemDocumentationTest.class.getResource(n);
    }
    
    private CompletionDocumentation createDocResourceResultSet(MockUrlGrammarResult r) throws Exception {
        if (r == null) {
            r = new MockUrlGrammarResult();
            r.setContentURL(createResourceName("res/docResource.html"));
            r.setExternal(true);
        }
        
        ElementResultItem item = new ElementResultItem(0, r);
        CompletionTask task = item.doCreateDocumentationTask(r);
        CompletionResultSet rs = resultSetFor(task, CompletionProvider.DOCUMENTATION_QUERY_TYPE);
        task.query(rs);

        assertTrue(rs.isFinished());
        
        return rsImpl.getDocumentation();
    }
    
    /**
     * Checks that custom contents overrides the one supplied by XMLResultItem
     * 
     * @throws Exception 
     */
    public void testCustomContent() throws Exception {
        URL res = EncodingUtil.class.getResource("/org/netbeans/modules/xml/core/resources/Bundle.properties");
        MockUrlGrammarResult test = new MockUrlGrammarResult();
        test.setContentURL(res);
        test.setExternal(false);
        test.setDescription(PLAIN_DESCRIPTION_TEXT);
        
        CompletionDocumentation doc = createDocResourceResultSet(test);
        assertNull(doc.getURL());
        assertEquals("Invalid content", PLAIN_DESCRIPTION_TEXT, doc.getText());
    }

    /**
     * Checks documentation bundled in a JAR - not openable by external browser.
     * Need a resource within a JAR
     * 
     * @throws Exception 
     */
    public void testInternalUrlDocumentation() throws Exception {
        URL res = EncodingUtil.class.getResource("/org/netbeans/modules/xml/core/resources/Bundle.properties");
        MockUrlGrammarResult test = new MockUrlGrammarResult();
        test.setContentURL(res);
        test.setExternal(false);
        
        CompletionDocumentation doc = createDocResourceResultSet(test);
        assertNull(doc.getURL());
        assertTrue(doc.getText().contains("OpenIDE-Module-Name=XML Core"));
        
        // check that relative links still resolve to internal doc
        CompletionDocumentation  internal = doc.resolveLink("mf-layer.xml");
        assertNotNull("Relative links must be resolvable", internal);
        assertNull("Relative links cannot be opened in extbrowser", doc.getURL());
        assertTrue("Content must be accessible", 
                internal.getText().contains("org-netbeans-modules-xml-dtd-grammar-DTDGrammarQueryProvider.instance"));
        
        // check that absolute links resolve to external doc
        CompletionDocumentation  external = doc.resolveLink("http://www.netbeans.org");
        assertNotNull("Absolute links must be resolvable", external);
        assertEquals("Absolute links must have URL", external.getURL(), new URL("http://www.netbeans.org"));
    }
    
    /**
     * Checks that file-based documentation reverts to external as soon as possible
     * @throws Exception 
     */
    public void testFileDocumentation() throws Exception {
        URL res = EncodingUtil.class.getResource("/org/netbeans/modules/xml/core/resources/Bundle.properties");
        MockUrlGrammarResult test = new MockUrlGrammarResult();
        test.setContentURL(res);
        test.setExternal(false);

        CompletionDocumentation doc = createDocResourceResultSet(test);
        assertNull(doc.getURL());
        assertTrue("Invalid content", doc.getText().contains("OpenIDE-Module-Name=XML Core"));
        
        // check that resolve of file-based URL turns the doc to external:

        URL url = createResourceName("res/docResource.html");
        CompletionDocumentation  file = doc.resolveLink(url.toString());
        assertNotNull(file);
        assertEquals("URL must be openable in browser", url, file.getURL());
        assertTrue("Invalid content of the linked doc", file.getText().contains("This is an URL resource with <a href="));
    }
    
    /**
     * Checks that relative and .. link resolution works OK.
     * 
     * @throws Exception 
     */
    public void testResolveRelativeLinks() throws Exception {
        CompletionDocumentation doc = createDocResourceResultSet(null);
                
        assertEquals(createResourceName("res/docResource.html"), doc.getURL());
        
        CompletionDocumentation linked = doc.resolveLink("relativeLink1.html");
        assertNotNull(linked);
        assertEquals("Relative link must be resolved", createResourceName("res/relativeLink1.html"), linked.getURL());
        
        linked = doc.resolveLink("../parentDoc.html");
        assertNotNull(linked);
        assertEquals("Link to resource in parent folder must be resolved", createResourceName("parentDoc.html"), linked.getURL());
    }
    
    /**
     * Checks that DS returning 'external' provides URL and resolves links, although
     * no custom resolver is given
     * 
     * @throws Exception 
     */
    public void testNetworkUrlDocumentation() throws Exception {
        MockUrlGrammarResult test = new MockUrlGrammarResult();
        test.setExternal(true);
        test.setContentURL(new URL("http://www.netbeans.org"));
        
        CompletionDocumentation doc = createDocResourceResultSet(test);
        assertEquals("External documentation must have URL", new URL("http://www.netbeans.org"), doc.getURL());
        
        CompletionDocumentation resolved = doc.resolveLink("index.html");
        assertEquals("Resolved external link must have URL", new URL("http://www.netbeans.org/index.html"), resolved.getURL());
    }
    
    /**
     * Checks that without content URL, no item is produced
     * @throws Exception 
     */
    public void testNoContentUrl() throws Exception {
        MockUrlGrammarResult res = new MockUrlGrammarResult();
        CompletionDocumentation doc = createDocResourceResultSet(res);
        
        assertNull(doc);
    }
    
    public void testCustomResolveLink() throws Exception {
        MockUrlGrammarResult res = new MockUrlGrammarResult() {

            // resolves relative links in different directory
            @Override
            public DescriptionSource resolveLink(String link) {
                MockUrlGrammarResult res = new MockUrlGrammarResult();
                URL base = createResourceName("res/" + link);
                res.setContentURL(base);
                res.setExternal(link.contains("Exist.html"));
                if (link.contains("customContent")) {
                    res.setDescription(PLAIN_DESCRIPTION_TEXT);
                }
                return res;
            }
        };
        res.setContentURL(createResourceName("res/docResource.html"));
        CompletionDocumentation doc = createDocResourceResultSet(res);
        
        // doc does not exist, but documentation item is there
        CompletionDocumentation notExisting = doc.resolveLink("doesNotExist.html");
        assertNotNull(notExisting);
        //  it is external, as defined by custom resolver
        assertEquals(createResourceName("doesNotExist.html"), notExisting.getURL());
        // but contents cannot be fetched by the framework
        assertNull("Supplied description must take precedence", notExisting.getText());

        // existing item
        CompletionDocumentation resolved = doc.resolveLink("relativeLink1.html");
        // custom resolver says this is an internal link!
        assertNull("Must not be openable in browser", resolved.getURL());
        // fwk is able to fetch contents
        assertTrue("Framework must fetch contents of linked URL", 
                resolved.getText().contains("Resource referenced by relative link"));
        
        CompletionDocumentation parent = doc.resolveLink("customContent");
        assertEquals(PLAIN_DESCRIPTION_TEXT, parent.getText());
    }
    
    private static class MockUrlGrammarResult extends MockGrammarResult implements DescriptionSource {
        private URL contentURL;
        private boolean external;

        public void setContentURL(URL contentURL) {
            this.contentURL = contentURL;
        }

        public void setExternal(boolean external) {
            this.external = external;
        }
        
        @Override
        public URL getContentURL() {
            return contentURL;
        }

        @Override
        public boolean isExternal() {
            return external;
        }

        @Override
        public DescriptionSource resolveLink(String link) {
            return null;
        }
        
    }
    
    private static class MockGrammarResult extends AbstractNode implements GrammarResult {
        private String description;
        private String displayName;

        public void setDescription(String description) {
            this.description = description;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Icon getIcon(int kind) {
            return null;
        }

        @Override
        public boolean isEmptyElement() {
            return false;
        }

        @Override
        public short getNodeType() {
            return ELEMENT_NODE;
        }
        
    }
}
