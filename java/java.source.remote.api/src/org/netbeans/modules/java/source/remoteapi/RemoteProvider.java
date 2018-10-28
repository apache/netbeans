/**
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
package org.netbeans.modules.java.source.remoteapi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.java.source.remote.api.RemoteRunner;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.source.remote.api.RemoteParserTask;
import org.netbeans.modules.java.source.remoteapi.Server;
import org.netbeans.modules.java.source.remote.spi.RemotePlatform;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStop;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class RemoteProvider {

    private static final Logger LOG = Logger.getLogger(RemoteProvider.class.getName());
    private static final Map<RemotePlatform, RemoteProcessDescription> platform2Remote = new IdentityHashMap<>();

    @NbBundle.Messages({
        "CAP_StartingRemote=Starting Java editor support on target platform"
    })
    public static RemoteRunner getRunner(FileObject source, BiFunction<DataInputStream, DataOutputStream, RemoteRunner> createRunner) {
        synchronized (platform2Remote) {
        RemotePlatform p = null;

        for (RemotePlatform.Provider c : Lookup.getDefault().lookupAll(RemotePlatform.Provider.class)) {
            RemotePlatform cp = c.findPlatform(source);

            if (cp != null) {
                p = cp;
                break;
            }
        }

        if (p == null) {
            return null;
        }
        
        RemoteProcessDescription desc = platform2Remote.get(p);
        
        if (desc != null) {
            return desc.runner;
        }

        ProgressHandle h = ProgressHandle.createHandle(Bundle.CAP_StartingRemote());
        try {
            h.start();
            byte[] data = MessageDigest.getInstance("SHA-1").digest(p.getJavaCommand().getBytes("UTF-8"));
            StringBuilder name = new StringBuilder();
            for (byte b : data) {
                name.append(String.format("%02X", Byte.toUnsignedInt(b)));
            }
            File userdir = Places.getCacheSubdirectory("java-remote/" + name.toString());
            File version = InstalledFileLocator.getDefault().locate("VERSION.txt", null, false);
            File nbRoot = version != null ? version.getParentFile().getParentFile()
                    : new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getParentFile();
            List<File> jars = new ArrayList<>();
            findJars(new File(nbRoot, "platform"), jars);
            findJars(new File(nbRoot, "ide"), jars);
            findJars(new File(nbRoot, "java"), jars);
            if (extraClassPathElements != null)
                jars.add(extraClassPathElements);
            ServerSocket ss = new ServerSocket(0);
            List<String> options = new ArrayList<>();
            options.add(p.getJavaCommand());
//            options.add("-agentlib:jdwp=transport=dt_socket,suspend=y,server=y,address=8887");
            options.addAll(p.getJavaArguments());
            options.add("-classpath"); options.add(jars.stream().map(f -> f.getAbsolutePath()).collect(Collectors.joining(System.getProperty("path.separator"))));
            options.add("--add-exports=jdk.javadoc/com.sun.tools.javadoc.main=ALL-UNNAMED");
            options.add("-Djdk.home=" + System.getProperty("jdk.home"));
            options.add(Server.class.getName());
            options.add(String.valueOf(ss.getLocalPort()));
            options.add(userdir.getAbsolutePath());
            options.add(Places.getCacheDirectory().getAbsolutePath());
            Process process = new ProcessBuilder(options.toArray(new String[0])).inheritIO().start();
            Socket endpoint = ss.accept();
            DataInputStream input = new DataInputStream(endpoint.getInputStream());
            DataOutputStream output = new DataOutputStream(endpoint.getOutputStream());

            desc = new RemoteProcessDescription(process, p, createRunner.apply(input, output));
            
            platform2Remote.put(p, desc);

            try {
                desc.runner.readAndDecode(source, InitTask.class, String.class, null).get();
            } catch (Throwable t) {
                LOG.log(Level.FINE, null, t);
            }

            return desc.runner;
        } catch (NoSuchAlgorithmException | IOException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
        } finally {
            h.finish();
        }
        }
    }
    
    private static void findJars(File in, List<File> jars) {
        if (in.getName().contains("org-netbeans-modules-java-source-nbjavac.jar"))
            return ;
        if (in.getName().endsWith(".jar")) {
            jars.add(in);
        }
        File[] children = in.listFiles();
        if (children == null) {
            return ;
        }
        for (File c : children) {
            findJars(c, jars);
        }
    }


    private static final class RemoteProcessDescription implements ChangeListener {
        public final Process process;
        public final RemotePlatform platform;
        public final RemoteRunner runner;

        public RemoteProcessDescription(Process process,
                                        RemotePlatform platform,
                                        RemoteRunner runner) throws IOException {
            this.process = process;
            this.platform = platform;
            this.platform.addChangeListener(this);
            this.runner = runner;
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            try {
                stop();
            } catch (InterruptedException ex) {
                //ignored
            }
            //refresh focused editor:
            JTextComponent comp = EditorRegistry.focusedComponent();
            if (comp != null) {
                FileObject file = NbEditorUtilities.getFileObject(comp.getDocument());
                
                if (file != null) {
                    Source source = Source.create(file);
                    try {
                        Class<?> utilities = Class.forName("org.netbeans.modules.parsing.impl.Utilities", false, Source.class.getClassLoader());
                        Method m = utilities.getMethod("revalidate", Source.class);
                        m.invoke(null, source);
                    } catch (Throwable ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            }
        }

        private void stop() throws InterruptedException {
            synchronized (platform2Remote) {
                platform2Remote.remove(platform);
                process.destroy();
                process.waitFor(2, TimeUnit.SECONDS);
                process.destroyForcibly();
                process.waitFor();
            }
        }
    }

    public static void stopAll() throws InterruptedException {
        for (RemoteProcessDescription d : new ArrayList<>(platform2Remote.values())) {
            d.stop();
        }
    }

    @OnStop
    public static class StopAll implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            stopAll();
            return null;
        }
        
    }
    
    @ServiceProvider(service=RemoteParserTask.class)
    public static class InitTask implements RemoteParserTask<String, CompilationController, Void> {

        @Override
        public Future<String> computeResult(CompilationController cc, Void p) throws IOException {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            cc.getTokenHierarchy().tokenSequence().moveNext();
            return new SynchronousFuture<String>(() -> "initialized", () -> {});
        }
        
    }

    //for tests:
    public static File extraClassPathElements;
}
