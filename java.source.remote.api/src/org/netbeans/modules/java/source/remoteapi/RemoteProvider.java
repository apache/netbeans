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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.java.source.remoteapi.Server;
import org.netbeans.modules.java.source.remote.spi.RemotePlatform;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStop;
import org.openide.modules.Places;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class RemoteProvider {

    private static final Map<RemotePlatform, RemoteProcessDescription> platform2Remote = new IdentityHashMap<>();

    public static URI getRemoteURL(FileObject source) {
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
            return desc.baseURI;
        }

        try {
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
//            options.add("-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=8887");
            options.addAll(p.getJavaArguments());
            options.add("-classpath"); options.add(jars.stream().map(f -> f.getAbsolutePath()).collect(Collectors.joining(System.getProperty("path.separator"))));
            options.add("--add-exports=jdk.javadoc/com.sun.tools.javadoc.main=ALL-UNNAMED");
            options.add("-Djdk.home=" + System.getProperty("jdk.home"));
            options.add(Server.class.getName());
            options.add(String.valueOf(ss.getLocalPort()));
            options.add(userdir.getAbsolutePath());
            options.add(Places.getCacheDirectory().getAbsolutePath());
            Process process = new ProcessBuilder(options.toArray(new String[0])).inheritIO().start();
            try (Socket s = ss.accept();
                 InputStream in = s.getInputStream();
                 DataInputStream dis = new DataInputStream(in)) {
                desc = new RemoteProcessDescription(URI.create("http://localhost:" + dis.readInt() + "/"), process, p);
            }
            
            platform2Remote.put(p, desc);
            return desc.baseURI;
        } catch (NoSuchAlgorithmException | IOException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
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
        public final URI baseURI;
        public final Process process;
        public final RemotePlatform platform;

        public RemoteProcessDescription(URI baseURI,
                                        Process process,
                                        RemotePlatform platform) {
            this.baseURI = baseURI;
            this.process = process;
            this.platform = platform;
            this.platform.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            synchronized (platform2Remote) {
                platform2Remote.remove(platform);
            }
        }

        private void stop() throws InterruptedException {
            synchronized (platform2Remote) {
                process.destroy();
                process.waitFor(2, TimeUnit.SECONDS);
                process.destroyForcibly();
                process.waitFor();
                platform2Remote.remove(platform);
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
    
    //for tests:
    public static File extraClassPathElements;
}
