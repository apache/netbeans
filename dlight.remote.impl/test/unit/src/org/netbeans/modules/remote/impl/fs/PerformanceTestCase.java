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
package org.netbeans.modules.remote.impl.fs;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Add the following section to your 
 * ~/.cndtestrc
 *
 * [remote.fs.performance]
 * testTraverse=dir1:dir2:dirN
 * testRead=dir1:dir2:dirN
 *
 * Where dir1... dirN are test directories,
 * each should be available by the same path
 * from both local and remote host
 * 
 */
public class PerformanceTestCase extends RemoteFileTestBase {

    private static final String RC_SECTION = "remote.fs.performance";

    public PerformanceTestCase(String testName) throws Exception {
        super(testName);
    }

    public PerformanceTestCase(String testName, ExecutionEnvironment execEnv) throws Exception {
        super(testName, execEnv);
    }

    private static String[] getTestPaths(String key) throws IOException, FormatException {
        String s = NativeExecutionTestSupport.getRcFile().get(RC_SECTION, key);
        return s.split(":");
    }

    private interface Processor {
        public void processFile(FileObject fo) throws IOException;
        public void processDir(FileObject fo) throws IOException;
    }

    private static class Counter {

        private int files = 0;
        private int directories = 0;
        private long time = System.currentTimeMillis();
        private static final int divider = 5000;

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
                System.err.printf("%d files in %d directories so far... (last %d objects processed within %d seconds)\n",
                        files, directories, divider, (System.currentTimeMillis() - time)/1000);
                time = System.currentTimeMillis();
            }
        }
    }

    private void reportException(FileObject fo, Exception ex) {
        System.err.printf("Error when processing %s: %s\n", fo.getPath(), ex.getMessage());
    }

    private void recurse(FileObject fo, Counter counter, Processor processor, Set<FileObject> bag, Collection<IOException> exceptions)  {
        if (bag != null) {
            if (!bag.contains(fo)) {
                bag.add(fo);
            }
        }
        if (fo.isFolder()) {
            counter.incrementDirectories();
            if (processor != null) {
                try {
                    processor.processDir(fo);
                } catch (IOException ex) {
                    exceptions.add(ex);
                    reportException(fo, ex);
                }
            }
            for (FileObject child : fo.getChildren()) {
                recurse(child, counter, processor, bag, exceptions);
            }
        } else {
            counter.incrementFiles();
            if (processor != null) {
                try {
                    processor.processFile(fo);
                } catch (IOException ex) {
                    reportException(fo, ex);
                    exceptions.add(ex);
                }
            }
        }
    }

    private static class ReadProcessor implements Processor {
        @Override
        public void processFile(FileObject fo) throws IOException {
            if (fo.canRead()) {
                readFileToDevNull(fo);
            } else {
                System.err.printf("Skipping unreadable %s\n", fo.getPath());
            }
        }
        @Override
        public void processDir(FileObject fo) throws IOException {
            //System.err.printf("Processing %s\n", fo.toURL());
        }
    }

    private static class PrintUrlProcessor implements Processor {
        @Override
        public void processFile(FileObject fo) throws IOException {
            System.err.printf("Processing %s\n", fo.toURL());
        }
        @Override
        public void processDir(FileObject fo) throws IOException {
            System.err.printf("Processing %s\n", fo.toURL());
        }
    }

    private static class NopProcessor implements Processor {
        @Override
        public void processFile(FileObject fo) throws IOException {
            //System.err.printf("Processing %s\n", fo.toURL());
        }
        @Override
        public void processDir(FileObject fo) throws IOException {
            //System.err.printf("Processing %s\n", fo.toURL());
        }
    }

    private static void readFileToDevNull(FileObject fo) throws IOException {
        assertTrue("File " +  fo.getPath() + " does not exist", fo.isValid());
        InputStream is = fo.getInputStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        try {
            assertNotNull("Null input stream", is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rdr.readLine()) != null) {
                // nothing
            }
        } finally {
            rdr.close();
        }
    }

    private CharSequence toString(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
//            if (sb.length() > 0) {
//                sb.append(", ");
//            }
            sb.append('\n').append('\t');
            sb.append(path);
        }
        return sb.append('\n');
    }

    private void doTestRecurseDirectories(Processor processor, String... paths) throws Throwable {

        FileObject[] remoteFileObjects = new FileObject[paths.length];
        FileObject[] localFileObjects = new FileObject[paths.length];
        for (int i = 0; i < paths.length; i++) {
            remoteFileObjects[i] = getFileObject(paths[i]);
            localFileObjects[i] = FileUtil.toFileObject(FileUtil.normalizeFile(new File(paths[i])));
        }

        System.err.printf("### Recursing locally %s\n", toString(paths));
        List<IOException> localExceptions = new ArrayList<>();
        Counter localCounter = new Counter();
        long localTime = System.currentTimeMillis();
        for (FileObject localBaseDirFO : localFileObjects) {
            recurse(localBaseDirFO, localCounter, processor, null, localExceptions);
        }
        localTime = System.currentTimeMillis() - localTime;

        System.err.printf("### Recursing remotely %s\n", toString(paths));
        List<IOException> remoteExceptions = new ArrayList<>();
        Counter remoteCounter = new Counter();
        long remoteTime = System.currentTimeMillis();
        for (FileObject remoteBaseDirFO : remoteFileObjects) {
            recurse(remoteBaseDirFO, remoteCounter, processor, null, remoteExceptions);
        }
        remoteTime = System.currentTimeMillis() - remoteTime;


        System.err.printf("### Recursed %s Local results:  %d files in %d dirs within %d s with %d exception(s)\n",
                toString(paths), localCounter.getFiles(), localCounter.getDirectories(), localTime/1000, localExceptions.size());

        System.err.printf("### Recursed %s Remote results: %d files in %d dirs within %d s with %d exception(s)\n",
                toString(paths), remoteCounter.getFiles(), remoteCounter.getDirectories(), remoteTime/1000, remoteExceptions.size());
    }

    @ForAllEnvironments
    public void testTraverse() throws Throwable {
        String[] testPaths = getTestPaths("testTraverse");
        Processor processor;
        //processor = new PrintUrlProcessor();
        processor = new NopProcessor();
        doTestRecurseDirectories(processor, testPaths);
    }

    @ForAllEnvironments
    public void testRead() throws Throwable {
        String[] testPaths = getTestPaths("testRead");
        Processor processor;
        processor = new ReadProcessor();
        doTestRecurseDirectories(processor, testPaths);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(PerformanceTestCase.class);
    }
}
