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
package org.netbeans.modules.gsf.testrunner.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;

/**
 *
 * @author Dusan Balek
 */
public final class TestMethodFinderImpl extends EmbeddingIndexer {

    public static final String NAME = "tests"; // NOI18N
    public static final int VERSION = 1;
    public static TestMethodFinderImpl INSTANCE = null;

    private final WeakSet<BiConsumer<FileObject, Collection<TestMethodController.TestMethod>>> listeners = new WeakSet<>();

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        List<TestMethodController.TestMethod> testMethods = new ArrayList<>();
        for (ComputeTestMethods ctm : MimeLookup.getLookup(indexable.getMimeType()).lookupAll(ComputeTestMethods.class)) {
            testMethods.addAll(ctm.computeTestMethods(parserResult, new AtomicBoolean()));
        }
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        store(context.getIndexFolder(), indexable.getURL(), indexable.getRelativePath(), testMethods);
        if (!context.isAllFilesIndexing()) {
            synchronized (listeners) {
                for (BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener : listeners) {
                    listener.accept(fo, testMethods);
                }
            }
        }
    }

    public void addListener(BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener) {
        synchronized(listeners) {
            listeners.putIfAbsent(listener);
        }
    }

    private void store(FileObject indexFolder, URL url, String resourceName, List<TestMethodController.TestMethod> methods) {
        File cacheRoot = FileUtil.toFile(indexFolder);
        File output = new File(cacheRoot, resourceName + ".tests"); //NOI18N
        if (methods.isEmpty()) {
            if (output.exists()) {
                output.delete();
            }
        } else {
            output.getParentFile().mkdirs();
            boolean printHeader = true;
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"))) {
                for (TestMethodController.TestMethod method : methods) {
                    if (printHeader) {
                        pw.print("url: "); //NOI18N
                        pw.println(url.toString());
                        pw.print("class: "); //NOI18N
                        pw.print(method.getTestClassName());
                        if (method.getTestClassPosition() != null) {
                            pw.print(':'); //NOI18N
                            pw.println(method.getTestClassPosition().getOffset());
                        } else {
                            pw.println();
                        }
                        printHeader = false;
                    }
                    pw.print("method: "); //NOI18N
                    pw.print(method.method().getMethodName());
                    pw.print(':'); //NOI18N
                    pw.println(method.start().getOffset());
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @MimeRegistration(mimeType="", service=EmbeddingIndexerFactory.class) //NOI18N
    public static class Factory extends EmbeddingIndexerFactory {

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (INSTANCE == null) {
                INSTANCE = new TestMethodFinderImpl();
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
