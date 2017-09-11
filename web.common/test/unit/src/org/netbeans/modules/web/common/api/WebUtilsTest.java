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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

import java.net.URL;
import java.util.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author marekfukala
 */
public class WebUtilsTest extends CslTestBase {

    private static final String HTML_MIME_TYPE = "text/html";

    public WebUtilsTest(String testName) {
        super(testName);
    }

    public void testResolve() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("third.txt");
        assertNotNull(two);

        FileObject resolved = WebUtils.resolve(one, "third.txt");
        assertNotNull(resolved);
        assertEquals(two, resolved);

    }

    public void testResolveFolderReferences() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        //test resolve path reference
        FileObject resolved = WebUtils.resolve(one, "folder/second.txt");
        assertNotNull(resolved);
        assertEquals(two, resolved);

        //test resolve path reference backward
        resolved = WebUtils.resolve(two, "../one.txt");
        assertNotNull(resolved);
        assertEquals(one, resolved);

    }

    public void testResolveInvalidLinks() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        //test resolve invalid path reference
        FileReference resolved = WebUtils.resolveToReference(one, "xyz");
        assertNull(resolved);

        resolved = WebUtils.resolveToReference(one, "");
        assertNull(resolved);

    }

    public void testGetRelativePathFolderWithDot() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/inner.folder/fourth.txt");
        assertNotNull(two);
        assertEquals("folder/inner.folder/fourth.txt", WebUtils.getRelativePath(one, two));
    }

    public void test182423() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);

        FileReference resolved = WebUtils.resolveToReference(one, "/css/common.css");
        assertNull(resolved);

    }

    public void testIssue229522() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileReference reference = WebUtils.resolveToReference(one, "./space%20issue229522/test.css");
        assertNotNull(reference);
    }

    public void testGetResultIteratorForNoEmbedding() throws Exception {
        // embeddings => text/html (no embedding)
        Source source = getSourceForMimeType(HTML_MIME_TYPE);
        getResultIteratorAndCheckMimePath(source, HTML_MIME_TYPE);
    }

    public void testGetResultIteratorForEmbeddingLevel1() throws Exception {
        // embeddings => text/x-php5/text/html
        final String mimeType = "text/x-php5";
        setEmbeddingProviderIntoMockLookup(mimeType, Collections.singleton(HTML_MIME_TYPE));

        Source source = getSourceForMimeType(mimeType);
        getResultIteratorAndCheckMimePath(source, mimeType + "/" + HTML_MIME_TYPE);
    }

    public void testGetResultIteratorForEmbeddingLevel2() throws Exception {
        // embeddings => text/x-tpl/text/x-php5/text/html
        final String mimeType = "text/x-tpl";
        final String embeddedPhpMimeType = "text/x-php5";

        setEmbeddingProviderIntoMockLookup(mimeType, Collections.singleton(embeddedPhpMimeType));
        setEmbeddingProviderIntoMockLookup(embeddedPhpMimeType, Collections.singleton(HTML_MIME_TYPE));

        Source source = getSourceForMimeType(mimeType);
        getResultIteratorAndCheckMimePath(source, mimeType + "/" + embeddedPhpMimeType + "/" + HTML_MIME_TYPE);
    }

    public void testGetResultIteratorForShortestEmbedding() throws Exception {
        // embeddings => text/x-tpl/text/x-php5/text/html
        // embeddings => text/x-tpl/text/html
        final String mimeType = "text/x-tpl";
        final String embeddedPhpMimeType = "text/x-php5";

        Set<String> embeddedMimeTypes = new HashSet<String>(Arrays.asList(embeddedPhpMimeType, HTML_MIME_TYPE));
        setEmbeddingProviderIntoMockLookup(mimeType, embeddedMimeTypes);
        setEmbeddingProviderIntoMockLookup(embeddedPhpMimeType, Collections.singleton(HTML_MIME_TYPE));

        Source source = getSourceForMimeType(mimeType);
        getResultIteratorAndCheckMimePath(source, mimeType + "/" + HTML_MIME_TYPE);
    }

    public void testGetResultIteratorForFirstShortestEmbedding() throws Exception {
        // embeddings => text/x-tpl/text/x-php4/text/html
        // embeddings => text/x-tpl/text/x-php5/text/html
        final String mimeType = "text/x-tpl";
        final String embeddedPhp4MimeType = "text/x-php4";
        final String embeddedPhp5MimeType = "text/x-php5";

        Set<String> embeddedMimeTypes = new HashSet<String>(Arrays.asList(embeddedPhp4MimeType, embeddedPhp5MimeType));
        setEmbeddingProviderIntoMockLookup(mimeType, embeddedMimeTypes);
        setEmbeddingProviderIntoMockLookup(embeddedPhp4MimeType, Collections.singleton(HTML_MIME_TYPE));
        setEmbeddingProviderIntoMockLookup(embeddedPhp5MimeType, Collections.singleton(HTML_MIME_TYPE));

        Source source = getSourceForMimeType(mimeType);
        getResultIteratorAndCheckMimePath(source, mimeType + "/text/x-php[4,5]/" + HTML_MIME_TYPE);
    }

    public void testStringToUrl() throws Exception {
        URL u = WebUtils.stringToUrl("http://localhost:1234/some path with spaces/zemědělství?more spaces#mě dě");
        assertEquals("http://localhost:1234/some%20path%20with%20spaces/zemědělství?more%20spaces#mě%20dě", u.toExternalForm());
        // #219686
        u = WebUtils.stringToUrl("localhost:1234/some-path");
        assertEquals("http://localhost:1234/some-path", u.toExternalForm());

        // #221878
        URL original = Utilities.toURI(getDataDir()).toURL();
        u = WebUtils.stringToUrl(WebUtils.urlToString(original));
        assertEquals(original, u);

        u = WebUtils.stringToUrl("jar:file:/tmp/a.zip!/aa/bb/cc.txt");
        assertEquals("jar:file:/tmp/a.zip!/aa/bb/cc.txt", u.toExternalForm());
    }

    public void testUrlToString() throws Exception {
        String s = WebUtils.urlToString(new URL("http://localhost:1234/some%20path%20with%20spaces/zem%C4%9Bd%C4%9Blstv%C3%AD?more%20spaces#m%C4%9B%20d%C4%9B"));
        assertEquals("http://localhost:1234/some path with spaces/zemědělství?more spaces#mě dě", s);

        // #220006
        s = WebUtils.urlToString(new URL("http://fonts.googleapis.com/css?family=Leckerli+One|Rokkitt:700,400|Luckiest+Guy"));
        assertEquals("http://fonts.googleapis.com/css?family=Leckerli+One|Rokkitt:700,400|Luckiest+Guy", s);
        s = WebUtils.urlToString(new URL("http://fonts.googleapis.com/css?family=Leckerli+One%7CRokkitt:700,400%7CLuckiest+Guy"));
        assertEquals("http://fonts.googleapis.com/css?family=Leckerli+One|Rokkitt:700,400|Luckiest+Guy", s);

        s = WebUtils.urlToString(new URL("jar:file:/tmp/a.zip!/aa/bb/cc.txt"));
        assertEquals("jar:file:/tmp/a.zip!/aa/bb/cc.txt", s);
    }

    private void setEmbeddingProviderIntoMockLookup(String forMimeType, final Set<String> embeddedMimeTypes) {
        MockMimeLookup.setInstances(MimePath.parse(forMimeType), new TaskFactory() {
            public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singletonList(new EmbeddingProvider() {
                    public @Override List<Embedding> getEmbeddings(Snapshot snapshot) {
                        List<Embedding> embeddings = new ArrayList<Embedding>();
                        for (String embeddedType : embeddedMimeTypes) {
                            embeddings.add(snapshot.create(embeddedType + " section\n", embeddedType));
                        }
                        return embeddings;
                    }

                    public @Override int getPriority() {
                        return Integer.MAX_VALUE;
                    }

                    public @Override void cancel() {
                    }
                });
            }
        });

    }

    private void getResultIteratorAndCheckMimePath(Source source, final String mimePathRegex) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = WebUtils.getResultIterator(resultIterator, HTML_MIME_TYPE);
                assertEquals(resultIterator.getSnapshot().getMimeType(), HTML_MIME_TYPE);
                assertTrue(resultIterator.getSnapshot().getMimePath().getPath().matches(mimePathRegex));
            }
        });
    }

    private Source getSourceForMimeType(String mimeType) {
        EditorKit kit = new DefaultEditorKit();
        Document doc = kit.createDefaultDocument();
        doc.putProperty("mimeType", mimeType);
        return Source.create(doc);
    }

}