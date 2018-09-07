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
package org.netbeans.modules.java.source.remote.api;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.remoteapi.RemoteProvider;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class RemoteRunnerTest extends NbTestCase {
    
    public RemoteRunnerTest(String name) {
        super(name);
    }

    private FileObject src;

    @Override
    protected void setUp() throws Exception {
        RemoteProvider.extraClassPathElements = new File(TestRemoteParserTask.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        super.setUp();
        clearWorkDir();
        File wd = getWorkDir();
        File userDir = new File(wd, "ud");
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
        File root = new File(wd, "src");
        src = FileUtil.createData(new File(root, "Test.java"));
        TestUtilities.copyStringToFile(src, "public class Test { }");
    }
    
    public void testGetRemoteURL() throws Exception {
        RemoteRunner runner = RemoteRunner.create(src);
        assertNotNull(runner);
        String actual = runner.readAndDecode(src, TestRemoteParserTask.class, String.class, 42).get();
        
        assertEquals("good: Test/RELEASE_7/42", actual);

        actual = runner.readAndDecode(src, TestRemoteParserTask.class, String.class, 42).get();
        
        assertEquals("good: Test/RELEASE_7/42", actual);
    }

    public void testException() throws Exception {
        RemoteRunner runner = RemoteRunner.create(src);
        assertNotNull(runner);
        
        try {
            runner.readAndDecode(src, ExceptionRemoteParserTask.class, String.class, null).get();
            fail("Expected exception did not occur.");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            assertTrue(cause.getClass().getName(), cause instanceof IOException);
            cause = ex.getCause().getCause();
            assertTrue(cause.getClass().getName(), cause instanceof IOException);
            assertEquals("java.util.concurrent.ExecutionException:java.lang.IllegalStateException: expected", cause.getMessage());
        }
    }

    public void testCancel() throws Exception {
        RemoteRunner runner = RemoteRunner.create(src);
        assertNotNull(runner);

        Future<String> future1 = runner.readAndDecode(src, CancelRemoteParserTask.class, String.class, null);

        assertNotNull(future1);

        new Thread(() -> {
            future1.cancel(true);
        }).start();

        try {
            future1.get();
            fail("Expected exception did not occur.");
        } catch (CancellationException ex) {
            //OK
        }

        ServerSocket ser = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Future<String> future2 = runner.readAndDecode(src, DelayedCancelRemoteParserTask.class, String.class, ser.getLocalPort());

        assertNotNull(future2);

        ser.accept().close();

        new Thread(() -> {
            future2.cancel(true);
        }).start();

        try {
            future2.get();
            fail("Expected exception did not occur.");
        } catch (CancellationException ex) {
            //OK
        }

        String actual = runner.readAndDecode(src, TestRemoteParserTask.class, String.class, 42).get();
        
        assertEquals("good: Test/RELEASE_7/42", actual);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteProvider.stopAll();
    }

    static {
        System.setProperty("java.use.remote.platform", "true");
        System.setProperty("jdk.home", System.getProperty("java.home")); //so that default JavaPlatform works
    }
    
    @ServiceProvider(service=SourceLevelQueryImplementation2.class, position=1)
    public static final class TestSourceLevelQueryImpl implements SourceLevelQueryImplementation2 {

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            return new Result() {
                @Override
                public String getSourceLevel() {
                    return "7";
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }
        
    }

    @ServiceProvider(service=RemoteParserTask.class)
    public static class TestRemoteParserTask implements RemoteParserTask<String, CompilationController, Integer> {

        @Override
        public Future<String> computeResult(CompilationController info, Integer param) throws IOException {
            info.toPhase(JavaSource.Phase.RESOLVED);
            CompletableFuture<String> cf = new CompletableFuture<>();

            cf.complete("good: " + info.getTopLevelElements().get(0).getQualifiedName() + "/" + info.getSourceVersion() + "/" + param);
            return cf;
        }

    }

    @ServiceProvider(service=RemoteParserTask.class)
    public static class ExceptionRemoteParserTask implements RemoteParserTask<String, CompilationController, Integer> {

        @Override
        public Future<String> computeResult(CompilationController info, Integer param) throws IOException {
            CompletableFuture<String> cf = new CompletableFuture<>();

            cf.completeExceptionally(new IllegalStateException("expected"));
            return cf;
        }

    }

    @ServiceProvider(service=RemoteParserTask.class)
    public static class CancelRemoteParserTask implements RemoteParserTask<String, CompilationController, Integer> {

        @Override
        public Future<String> computeResult(CompilationController info, Integer param) throws IOException {
            return new CompletableFuture<>();
        }

    }

    @ServiceProvider(service=RemoteParserTask.class)
    public static class DelayedCancelRemoteParserTask implements RemoteParserTask<String, CompilationController, Integer> {

        @Override
        public Future<String> computeResult(CompilationController info, Integer param) throws IOException {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), param));
            s.close();
            return new CompletableFuture<>();
        }

    }

}
