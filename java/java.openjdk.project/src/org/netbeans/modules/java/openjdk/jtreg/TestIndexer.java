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
package org.netbeans.modules.java.openjdk.jtreg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.java.openjdk.jtreg.TagParser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class TestIndexer extends CustomIndexer {

    public static final String NAME = "jtreg-test-indexer";
    public static final int VERSION = 1;

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        //TODO: do nothing if this is not a test root(!!)
        FileObject root = context.getRoot();
//        if (root.toString().contains("test/jdk")) {
//            System.err.println("XXX");
//        }
        FileObject cacheRoot;
        try {
            cacheRoot = FileUtil.createFolder(context.getIndexFolder(), "../../tests/2");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }

        for (Indexable i : files) {
//            System.err.println("indexing: " + i.getRelativePath());
            FileObject resolved = root.getFileObject(i.getRelativePath());

            if (resolved == null) {
                continue;
            }
            Result tags = TagParser.parseTags(resolved);
            List<Tag> testTag = tags.getName2Tag().get("test");
            if (testTag != null) {
                try {
                    String fakeClassName = relativePath2FakeClassName(i.getRelativePath());
//                    System.err.println("fakeClassName: " + fakeClassName);
                    store(cacheRoot, i.getURL(), i.getRelativePath(), List.of(new TestMethod(fakeClassName, TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), new SingleMethod(resolved, "@test"), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()), TestClassInfoTask.createPosition(null, testTag.get(0).getTagStart()))));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
//            try {
//                JavaSource.forFileObject(resolved).runUserActionTask(cc -> {
//                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
//                    List<TestMethod> methods = TestClassInfoTask.computeTestMethods(cc, new AtomicBoolean(), -1);
//                    store(cacheRoot, i.getURL(), i.getRelativePath(), methods);
//                }, true);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
        }
        System.err.println("files: " + files);
    }
    
    public static String relativePath2FakeClassName(String relativePath) {
        return relativePath.substring(0, relativePath.length() - ".java".length()).replace('/', '.');
    }

    private void store(FileObject indexFolder, URL url, String resourceName, List<TestMethod> methods) {
        File cacheRoot = FileUtil.toFile(indexFolder);
        File output = new File(cacheRoot, resourceName + ".tests"); //NOI18N
        if (methods.isEmpty()) {
            if (output.exists()) {
                output.delete();
            }
        } else {
            Map<String, List<TestMethod>> class2methods = new LinkedHashMap<>();
            Map<String, Integer> class2offsets = new HashMap<>();
            for (TestMethod method : methods) {
                String className = method.getTestClassName();
                if (method.getTestClassPosition() != null) {
                    class2offsets.putIfAbsent(className, method.getTestClassPosition().getOffset());
                }
                class2methods.computeIfAbsent(className, name -> new ArrayList<>()).add(method);
            }
            output.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8))) {
                pw.print("url: "); //NOI18N
                pw.println(url.toString());
                for (Map.Entry<String, List<TestMethod>> entry : class2methods.entrySet()) {
                    pw.print("class: "); //NOI18N
                    pw.print(entry.getKey());
                    Integer offset = class2offsets.get(entry.getKey());
                    if (offset != null) {
                        pw.print(':'); //NOI18N
                        pw.println(offset);
                    } else {
                        pw.println();
                    }
                    for (TestMethod method : entry.getValue()) {
                        pw.print("method: "); //NOI18N
                        pw.print(method.method().getMethodName());
                        pw.print(':'); //NOI18N
                        pw.print(method.start().getOffset());
                        pw.print('-'); //NOI18N
                        pw.println(method.end().getOffset());
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @MimeRegistration(mimeType="text/x-java", service=CustomIndexerFactory.class)
    public static final class FactoryImpl extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new TestIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            //TODO!
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
