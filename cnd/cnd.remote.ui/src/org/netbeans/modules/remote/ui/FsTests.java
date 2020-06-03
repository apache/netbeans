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
package org.netbeans.modules.remote.ui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FsTests {

    private FsTests() {
    }

    public static void testLs(List<FileObject> fileObjects, PrintWriter out, PrintWriter err) {
        try {
            new TestLs(fileObjects, out, err).test();
        } catch (IOException ex) {
            ex.printStackTrace(err);
        }
    }

    private static abstract class TestBase {

        private final PrintWriter out;
        private final PrintWriter err;
        private final List<FileObject> fileObjects;

        public TestBase(List<FileObject> fileObjects, PrintWriter out, PrintWriter err) {
            this.out = out;
            this.err = err;
            this.fileObjects = new ArrayList<>(fileObjects);
        }

        protected abstract Processor createProcessor();

        protected Counter createCounter() {
            return new Counter(out, 1000);
        }

        public void test() throws IOException {
            long time = System.currentTimeMillis();
            Counter counter = createCounter();
            try {
                for (FileObject fo : fileObjects) {
                    recurse(fo, counter, createProcessor(), new HashSet<FileObject>());
                }
            } finally {
                time = System.currentTimeMillis() - time;
                out.printf("Processing %d files in %d directories took %d seconds%n", counter.getFiles(), counter.getDirectories(), time/1000); // NOI18N
            }
        }

        private void recurse(FileObject fo, Counter counter, Processor processor, Set<FileObject> bag) throws IOException {
            if (bag != null) {
                if (!bag.contains(fo)) {
                    bag.add(fo);
                }
            }
            for (FileObject child : fo.getChildren()) {
                if (child.isFolder()) {
                    counter.incrementDirectories();
                    if (processor != null) {
                        processor.processDir(child);
                    }
                    recurse(child, counter, processor, bag);
                } else {
                    counter.incrementFiles();
                    if (processor != null) {
                        processor.processFile(child);
                    }
                }
            }
        }

    }

    private static class TestLs extends TestBase {

        public TestLs(List<FileObject> fileObjects, PrintWriter out, PrintWriter err) {
            super(fileObjects, out, err);
        }

        @Override
        protected Processor createProcessor() {
            return new LsProcessor();
        }
    }

    private static void readFileToDevNull(FileObject fo) throws IOException {
        InputStream is = fo.getInputStream();
        Charset encoding = FileEncodingQuery.getEncoding(fo);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), encoding));
        try {
            String line;
            while ((line = rdr.readLine()) != null) {
                // nothing
            }
        } finally {
            rdr.close();
        }
    }

    private static interface Processor {
        public void processFile(FileObject fo) throws IOException;
        public void processDir(FileObject fo) throws IOException;
    }

    private static class BaseProcessor implements Processor {
        @Override
        public void processFile(FileObject fo) throws IOException {
        }
        @Override
        public void processDir(FileObject fo) throws IOException {
        }
    }

    private static class ReadProcessor extends BaseProcessor {
        @Override
        public void processFile(FileObject fo) throws IOException {
            if (fo.canRead()) {
                readFileToDevNull(fo);
            } else {
                System.err.printf("Skipping %s%n", fo.getPath());
            }
        }
    }

    private static class LsProcessor extends BaseProcessor {
    }

    private static class Counter {

        private int files = 0;
        private int directories = 0;
        private final int divider;
        private final PrintWriter writer;

        public Counter(PrintWriter writer, int divider) {
            this.divider = divider;
            this.writer = writer;
        }

        public int getFiles() {
            return files;
        }

        public void incrementFiles() {
            this.files++;
            progress();
        }

        public int getDirectories() {
            return directories;
        }

        public void incrementDirectories() {
            this.directories++;
            progress();
        }

        private void progress() {
            if ((directories + files) % divider == 0) {
                writer.printf("%d files in %d directories so far...%n", files, directories); // NOI18N
            }
        }
    }

}
