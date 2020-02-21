/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
