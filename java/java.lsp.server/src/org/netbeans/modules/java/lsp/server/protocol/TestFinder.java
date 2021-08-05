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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
public final class TestFinder extends EmbeddingIndexer {

    private static final String NAME = "tests"; // NOI18N
    private static final int VERSION = 1;
    private static TestFinder INSTANCE = null;

    private NbCodeLanguageClient client;

    @MimeRegistration(mimeType="text/x-groovy", service=EmbeddingIndexerFactory.class) //NOI18N
    public static EmbeddingIndexerFactory createGroovyFactory() {
        return new Factory();
    }

    @MimeRegistration(mimeType = "text/x-java", service = EmbeddingIndexerFactory.class) //NOI18N
    public static EmbeddingIndexerFactory createJavaFactory() {
        return new Factory();
    }

    public static Collection<TestSuiteInfo> findTests(Iterable<FileObject> testRoots, NbCodeLanguageClient client) {
        if (INSTANCE != null) {
            INSTANCE.client = client;
        }
        Map<FileObject, TestSuiteInfo> file2TestSuites = new HashMap<>();
        for (FileObject testRoot : testRoots) {
            try {
                FileObject cacheRoot = getCacheRoot(testRoot.toURL());
                if (cacheRoot != null) {
                    Enumeration<? extends FileObject> children = cacheRoot.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (child.hasExt("tests")) { //NOI18N
                            loadTestSuites(child, file2TestSuites);
                        }
                    }
                }
            } catch (IOException ex) {}
        }
        return file2TestSuites.values();
    }

    private static void loadTestSuites(FileObject input, Map<FileObject, TestSuiteInfo> file2TestSuites) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input.getInputStream(), "UTF-8")); //NOI18N
        try {
            FileObject fo = null;
            String url = null;
            TestSuiteInfo suite = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("url: ") && url == null) { //NOI18N
                    url = line.substring(5);
                    fo = Utils.fromUri(url);
                } else if (line.startsWith("class: ") && fo != null) { //NOI18N
                    String info = line.substring(7);
                    suite = file2TestSuites.computeIfAbsent(fo, fObj -> {
                        int idx = info.lastIndexOf(':');
                        String name = (idx < 0 ? info : info.substring(0, idx)).trim();
                        Integer lineNum = idx < 0 ? null : Utils.createPosition(fObj, Integer.parseInt(info.substring(idx + 1))).getLine();
                        return new TestSuiteInfo(name, Utils.toUri(fObj), lineNum, TestSuiteInfo.State.Loaded, new ArrayList<>());
                    });
                } else if (line.startsWith("method: ") && suite != null) { //NOI18N
                    String info = line.substring(7);
                    int idx = info.lastIndexOf(':');
                    String name = (idx < 0 ? info : info.substring(0, idx)).trim();
                    Integer lineNum = idx < 0 ? null : Utils.createPosition(fo, Integer.parseInt(info.substring(idx + 1))).getLine();
                    String id = suite.getSuiteName() + ':' + name;
                    String fullName = suite.getSuiteName() + '.' + name;
                    suite.getTests().add(new TestSuiteInfo.TestCaseInfo(id, name, fullName, suite.getFile(), lineNum, TestSuiteInfo.State.Loaded, null));
                }
            }
        } finally {
            br.close();
        }
    }

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        if (UnitTestForSourceQuery.findUnitTests(context.getRoot()).length == 0) {
            List<TestMethodController.TestMethod> testMethods = new ArrayList<>();
            for (ComputeTestMethods ctm : MimeLookup.getLookup(indexable.getMimeType()).lookupAll(ComputeTestMethods.class)) {
                testMethods.addAll(ctm.computeTestMethods(parserResult, new AtomicBoolean()));
            }
            if (!testMethods.isEmpty()) {
                store(context.getIndexFolder(), indexable.getURL(), indexable.getRelativePath(), testMethods);
                if (client != null && !context.isAllFilesIndexing()) {
                    FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                    String url = Utils.toUri(fo);
                    String testClassName = null;
                    Integer testClassLine = null;
                    List<TestSuiteInfo.TestCaseInfo> tests = new ArrayList<>(testMethods.size());
                    for (TestMethodController.TestMethod testMethod : testMethods) {
                        if (testClassName == null) {
                            testClassName = testMethod.getTestClassName();
                        }
                        if (testClassLine == null) {
                            testClassLine = testMethod.getTestClassPosition() != null
                                    ? Utils.createPosition(fo, testMethod.getTestClassPosition().getOffset()).getLine()
                                    : null;
                        }
                        String id = testMethod.getTestClassName() + ':' + testMethod.method().getMethodName();
                        String fullName = testMethod.getTestClassName() + '.' + testMethod.method().getMethodName();
                        int testLine = Utils.createPosition(parserResult.getSnapshot().getSource().getFileObject(), testMethod.start().getOffset()).getLine();
                        tests.add(new TestSuiteInfo.TestCaseInfo(id, testMethod.method().getMethodName(), fullName, url, testLine, TestSuiteInfo.State.Loaded, null));
                    }
                    client.notifyTestProgress(new TestProgressParams(url, new TestSuiteInfo(testClassName, url, testClassLine, TestSuiteInfo.State.Loaded, tests)));
                }
            }
        }
    }

    private void store(FileObject indexFolder, URL url, String resourceName, List<TestMethodController.TestMethod> methods) {
        try {
            File cacheRoot = FileUtil.toFile(indexFolder);
            File output = new File(cacheRoot, resourceName + ".tests"); //NOI18N
            output.getParentFile().mkdirs();
            final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8")); //NOI18N
            boolean printHeader = true;
            try {
                for (TestMethodController.TestMethod method : methods) {
                    if (printHeader) {
                        pw.print("url: "); //NOI18N
                        pw.println(url.toString());
                        pw.print("class: "); //NOI18N
                        pw.print(method.getTestClassName());
                        pw.print(':'); //NOI18N
                        pw.println(method.getTestClassPosition().getOffset());
                        printHeader = false;
                    }
                    pw.print("method: "); //NOI18N
                    pw.print(method.method().getMethodName());
                    pw.print(':'); //NOI18N
                    pw.println(method.start().getOffset());
                }
            } finally {
                pw.close();
            }
        } catch (IOException ex) {
        }
    }

    private static FileObject getCacheRoot(URL root) throws IOException {
        final FileObject dataFolder = CacheFolder.getDataFolder(root, true);
        return dataFolder != null ? FileUtil.createFolder(dataFolder, NAME + "/" + VERSION) : null; //NOI18N
    }

    private static class Factory extends EmbeddingIndexerFactory {

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (INSTANCE == null) {
                INSTANCE = new TestFinder();
            }
            return INSTANCE;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            File cacheRoot = FileUtil.toFile(context.getIndexFolder());
            for (Indexable indexable : deleted) {
                File output = new File(cacheRoot, indexable.getRelativePath() + ".tests"); //NOI18N
                if (output.exists()) {
                    output.delete();
                }
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    }
}
