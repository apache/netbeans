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
package org.netbeans.modules.java.source.remote.api;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStop;
import org.openide.modules.Places;

/**
 *
 * @author lahvac
 */
public class RemoteProvider {

    private static final Map<JavaPlatform, RemoteProcessDescription> platform2Remote = new HashMap<>();

    public static URI getRemoteURL(FileObject source) {
        JavaPlatform p = findPlatform(source);
        
        if (p == null) {
            return null;
        }
        
//        if (JavaPlatform.getDefault().getSpecification().getVersion().compareTo(p.getSpecification().getVersion()) >= 0) {
//            //better criterion
//            return null;
//        }

        RemoteProcessDescription desc = platform2Remote.get(p);
        
        if (desc != null) {
            return desc.baseURI;
        }

        File java = FileUtil.toFile(p.findTool("java"));
        try {
            byte[] data = MessageDigest.getInstance("SHA-1").digest(java.getAbsolutePath().getBytes("UTF-8"));
            StringBuilder name = new StringBuilder();
            for (byte b : data) {
                name.append(String.format("%02X", Byte.toUnsignedInt(b)));
            }
            File userdir = Places.getCacheSubdirectory("java-remote/" + name.toString());
            File launcher = new File(InstalledFileLocator.getDefault().locate("VERSION.txt", null, false).getParentFile().getParentFile(), "bin/netbeans");
            ServerSocket ss = new ServerSocket(0);
            Process process = new ProcessBuilder(launcher.getAbsolutePath(), "--userdir", userdir.getAbsolutePath(), "--jdkhome", java.getParentFile().getParentFile().getAbsolutePath(), "--start-java-server", String.valueOf(ss.getLocalPort()), "--nogui", "-J-Dnetbeans.full.hack=true", "--nosplash", "-J-agentlib:jdwp=transport=dt_socket,suspend=n,server=y,address=8887").inheritIO().start();
            try (Socket s = ss.accept();
                 InputStream in = s.getInputStream();
                 DataInputStream dis = new DataInputStream(in)) {
                desc = new RemoteProcessDescription(URI.create("http://localhost:" + dis.readInt() + "/"), process);
            }
            
            platform2Remote.put(p, desc);
            return desc.baseURI;
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static JavaPlatform findPlatform(FileObject source) {
        ClassPath systemCP = ClassPath.getClassPath(source, ClassPath.BOOT);
        FileObject jlObject = systemCP.findResource("java/lang/Object.class");

        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            FileObject platformJLObject = p.getBootstrapLibraries().findResource("java/lang/Object.class");
            if (jlObject.equals(platformJLObject)) {
                return p;
            }
        }

        return null;
    }

    private static final class RemoteProcessDescription {
        public final URI baseURI;
        public final Process process;

        public RemoteProcessDescription(URI baseURI, Process process) {
            this.baseURI = baseURI;
            this.process = process;
        }

    }

    @OnStop
    public static class StopAll implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            for (Map.Entry<JavaPlatform, RemoteProcessDescription> e : platform2Remote.entrySet()) {
                e.getValue().process.destroy();
                e.getValue().process.waitFor(2, TimeUnit.SECONDS);
                e.getValue().process.destroyForcibly();
                e.getValue().process.waitFor();
            }

            return null;
        }
        
    }
    
}
