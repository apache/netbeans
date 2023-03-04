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
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.matcher.AbstractMatcher;
import org.netbeans.modules.search.matcher.DefaultMatcher;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author jhavlin
 */
public class MatchingObjectTest extends NbTestCase {

    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_FILE_ENC = "UTF-8";

    public MatchingObjectTest(String name) {
        super(name);
    }

    /**
     * Adapt string to case of a pattern.
     *
     * @param found Found string - case pattern.
     * @param replacement Replacement string - value to adopt.
     */
    public String adapt(String found, String replacement) {
        return MatchingObject.adaptCase(replacement, found);
    }

    /**
     * Test adaption of case of replacement strings - simple.
     */
    public void testAdaptCase() throws Exception {

        assertEquals("next1", adapt("test1", "next1"));
        assertEquals("Next1", adapt("Test1", "next1"));
        assertEquals("NEXT1", adapt("TEST1", "next1"));
        assertEquals("nEXT1", adapt("tEST1", "next1"));
    }

    /**
     * Test adaption of case of replacement strings - camel case.
     */
    public void testAdaptCaseCamelCase() throws Exception {

        assertEquals("somethingElse", adapt("fooBar", "SomethingElse"));
        assertEquals("somethingelse", adapt("foobar", "SomethingElse"));

        assertEquals("MBox", adapt("JPanel", "MBox"));
        assertEquals("mBox", adapt("jPanel", "MBox"));
        assertEquals("mbox", adapt("jpanel", "MBox"));

        assertEquals("DaRealJunk", adapt("MyClass", "DaRealJunk"));
        assertEquals("daRealJunk", adapt("myClass", "DaRealJunk"));
        assertEquals("darealjunk", adapt("myclass", "DaRealJunk"));

        assertEquals("MyClass", adapt("DaRealJunk", "MyClass"));
        assertEquals("myClass", adapt("daRealJunk", "MyClass"));
        assertEquals("myclass", adapt("darealjunk", "MyClass"));

        assertEquals("FooBar", adapt("Foo", "fooBar"));
    }

    /**
     * Test replacing in filesystem files.
     */
    public void testReplaceInFilePreserveCase() throws IOException,
            InterruptedException,
            InvocationTargetException {

        assertEquals("writing data",
                replaceInFilePreserveCase("writing dta", "dta", "data"));

        assertEquals("writing DATA",
                replaceInFilePreserveCase("writing DTA", "dta", "data"));

        assertEquals("writing Data",
                replaceInFilePreserveCase("writing Dta", "dta", "data"));

        assertEquals("writing dATA",
                replaceInFilePreserveCase("writing dTA", "dta", "data"));
        assertEquals("writing\ndATA",
                replaceInFilePreserveCase("writing\ndTA", "dta", "data"));
        assertEquals("writing\r\ndATA",
                replaceInFilePreserveCase("writing\r\ndTA", "dta", "data"));
    }

    public void testRemoveOnlyChild() throws IOException {

        MatchingObject mo = prepareMatchingObject(1);
        final AtomicBoolean removed = new AtomicBoolean(false);
        mo.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (MatchingObject.PROP_REMOVED.equals(evt.getPropertyName())) {
                removed.set(true);
            }
        });
        mo.removeDetail(mo.getTextDetails().get(0));
        assertTrue(removed.get());
    }

    public void testRemoveOneOfChilds() throws IOException {

        MatchingObject mo = prepareMatchingObject(2);
        final AtomicBoolean childRemoved = new AtomicBoolean(false);
        mo.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (MatchingObject.PROP_CHILD_REMOVED.equals(
                    evt.getPropertyName())) {
                childRemoved.set(true);
            }
        });
        mo.removeDetail(mo.getTextDetails().get(0));
        assertTrue(childRemoved.get());
    }

    private MatchingObject prepareMatchingObject(int numTextDetails)
            throws IOException {
        ResultModel rm = new org.netbeans.modules.search.ResultModel(
                new BasicSearchCriteria(), null, null);
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot()
                .createData("test.tst");
        List<TextDetail> details = new LinkedList<>();
        for (int i = 0; i < numTextDetails; i++) {
            TextDetail td = new TextDetail(DataObject.find(fo),
                    SearchPattern.create("test", false, false, false));
            details.add(td);
        }
        rm.objectFound(fo, Charset.defaultCharset(), details);
        MatchingObject mo = rm.getMatchingObjects().get(0);
        return mo;
    }

    /**
     * Helper method - create file, write its content, find it, replace in it
     * and return its new content.
     */
    public String replaceInFilePreserveCase(String fileContent, String find,
            String replace) throws IOException, InterruptedException,
            InvocationTargetException {

        BasicSearchCriteria bsc = new BasicSearchCriteria();
        bsc.setTextPattern(find);
        bsc.setMatchType(SearchPattern.MatchType.BASIC);
        bsc.setReplaceExpr(replace);
        bsc.setPreserveCase(true);
        bsc.onOk();

        FileObject fo = createTestFile(fileContent);
        SearchScopeDefinition ss = new TempFileSearchScope(fo);
        SearchInfo si = ss.getSearchInfo();

        final SearchComposition<Def> sc =
                new BasicComposition(si,
                new DefaultMatcher(bsc.getSearchPattern()),
                bsc, null);
        EventQueue.invokeAndWait(() -> {
            sc.getSearchResultsDisplayer().getVisualComponent(); // initialize model
            final SearchTask st = new SearchTask(sc, true);
            st.run();
        });

        ReplaceTask rt = new ReplaceTask(
                ((ResultDisplayer) sc.getSearchResultsDisplayer()).getResultModel().getMatchingObjects(),
                null);
        rt.run();
        String result = fo.asText(TEST_FILE_ENC);
        return result;
    }

    /**
     * Search scope containing one temporary file only.
     */
    public static class TempFileSearchScope extends SearchScopeDefinition {

        private FileObject fo;

        public TempFileSearchScope(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public String getTypeId() {
            return "test";
        }

        @Override
        public String getDisplayName() {
            return "test search scope";
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return new SearchInfo() {

                @Override
                protected Iterator<URI> createUrisToSearchIterator(
                        SearchScopeOptions options, SearchListener listener,
                        AtomicBoolean terminated) {
                    throw new UnsupportedOperationException(
                            "Not supported yet.");                      //NOI18N
                }
                @Override
                public boolean canSearch() {
                    return true;
                }

                @Override
                public List<SearchRoot> getSearchRoots() {
                    return Collections.emptyList();
                }

                @Override
                public Iterator<FileObject> createFilesToSearchIterator(
                        SearchScopeOptions options, SearchListener listener,
                        AtomicBoolean terminated) {

                    List<FileObject> l =
                            Collections.singletonList(fo);
                    return l.iterator();
                }
            };
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public void clean() {
        }
    }

    public void testCheckFileLines() throws IOException {

        FileSystem fs = FileUtil.createMemoryFileSystem();
        Charset chs = StandardCharsets.UTF_8;
        OutputStream os = fs.getRoot().createAndOpen("find.txt");
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os,
                    chs.newEncoder());
            try {
                osw.write("Text on line 1.");
                osw.write("Line 1 has some more text included!\r\n");
                osw.write("Not matching line.\r\n");
                osw.write("Line 3 contains text\n");
                osw.write("\n");
                osw.write("Line 5 contains text\n");
            } finally {
                osw.flush();
                osw.close();
            }
        } finally {
            os.flush();
            os.close();
        }
        BasicSearchCriteria bsc = new BasicSearchCriteria();
        bsc.setFileNamePattern("*.*");
        bsc.setTextPattern("text");
        bsc.onOk();

        FileObject fo = fs.getRoot().getFileObject("find.txt");
        //bsc.matches(fo, new SearchListener() {});
        AbstractMatcher fileMatcher =
                new DefaultMatcher(bsc.getSearchPattern());
        Def resultDef = fileMatcher.check(fo, new SearchListener() {
        });
        List<TextDetail> matches = resultDef.getTextDetails();
        assertEquals(4, matches.size());

        assertEquals(1, matches.get(0).getLine());
        assertEquals(1, matches.get(0).getColumn());
        assertEquals(4, matches.get(0).getMarkLength());

        assertEquals(1, matches.get(1).getLine());
        assertEquals(37, matches.get(1).getColumn());

        assertEquals(3, matches.get(2).getLine());
        assertEquals(17, matches.get(2).getColumn());

        assertEquals(5, matches.get(3).getLine());
    }

    /**
     * Create an in-memory file with simple string content.
     *
     * @param content Content of the file.
     */
    public FileObject createTestFile(String content) throws IOException {

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = root.createData(TEST_FILE_NAME);

        try (OutputStream os = fo.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, TEST_FILE_ENC)) {
            osw.write(content);
            osw.flush();
        }
        return fo;
    }

    public void testRead() throws IOException {

        MockServices.setServices(Utf8FileEncodingQueryImpl.class);

        FileObject dir = FileUtil.toFileObject(getDataDir());
        assertNotNull(dir);
        FileObject file = dir.getFileObject(
                "textFiles/utf8file.txt");
        Charset c = FileEncodingQuery.getEncoding(file);
        MatchingObject mo = new MatchingObject(new ResultModel(
                new BasicSearchCriteria(), "", null), file, c, null);
        StringBuilder text = mo.text(true);
        String textStr = text.toString();
        int lineBreakSize = textStr.charAt(textStr.length() - 1) == '\n'
                && textStr.charAt(textStr.length() - 2) == '\r' ? 2 : 1;
        assertEquals('.', textStr.charAt(textStr.length() - lineBreakSize - 1));
    }

    public static class Utf8FileEncodingQueryImpl
            extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            if (file.getName().equals("utf8file")) {
                return StandardCharsets.UTF_8;
            } else {
                return null;
            }
        }
    }
}
