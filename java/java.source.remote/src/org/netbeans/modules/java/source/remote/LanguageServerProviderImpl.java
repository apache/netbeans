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
package org.netbeans.modules.java.source.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.spi.java.source.RemoteEditorPlatform.Provider;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.netbeans.spi.java.source.RemoteEditorPlatform;

/**
 *
 */
@MimeRegistration(mimeType="text/x-java", service=LanguageServerProvider.class)
public class LanguageServerProviderImpl implements LanguageServerProvider {
    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        if (!RemoteEditorPlatform.isRemoteEditorPlatformSupported()) {
            return null;
        }

        Project prj = lookup.lookup(Project.class);

        if (prj == null) {
            return null;
        }

        RemoteEditorPlatform remotePlatform = null;

        for (Provider p : Lookup.getDefault().lookupAll(Provider.class)) {
            RemoteEditorPlatform rp = p.findPlatform(prj.getProjectDirectory());
            if (rp != null) {
                remotePlatform = rp;
                break;
            }
        }

        if (remotePlatform == null || !remotePlatform.isEnabled()) {
            return null;
        }
        try {
            File launcher = InstalledFileLocator.getDefault().locate("modules/scripts/org-netbeans-modules-java-source-remote/bin/nb-java-lsp-server", "org.netbeans.modules.java.source.remote", false);
            String jdkHome = new File(remotePlatform.getJavaCommand()).getParentFile().getParentFile().getAbsolutePath();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            StringBuilder digest = new StringBuilder();
            for (byte b : md.digest(jdkHome.getBytes(StandardCharsets.UTF_8))) {
                digest.append(String.format("%02X", b));
            }
            File cache = Places.getCacheSubdirectory("java-lsp-server/" + digest.toString());
            //disable nb-javac:
            File disable_nbjavacapi = new File(new File(new File(cache, "config"), "Modules"), "org-netbeans-libs-nbjavacapi.xml_hidden");
            disable_nbjavacapi.getParentFile().mkdirs();
            new FileOutputStream(disable_nbjavacapi).close();
            Process process = new ProcessBuilder(launcher.getAbsolutePath(), "--jdkhome", jdkHome, "--installdir", launcher.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath(), "--userdir", cache.getAbsolutePath(), "-J-Dremote.editor.platform.running=true").redirectError(ProcessBuilder.Redirect.INHERIT).start();
            return LanguageServerDescription.create(new InputStreamWrapper(process.getInputStream()), new OutputStreamWrapper(process.getOutputStream()), process);
        } catch (Throwable t) {
            t.printStackTrace(); //TODO
            return null;
        }
    }

    private static class InputStreamWrapper extends InputStream {

        private final InputStream delegateTo;

        public InputStreamWrapper(InputStream delegateTo) {
            this.delegateTo = delegateTo;
        }
        
        @Override
        public int read() throws IOException {
            int read = delegateTo.read();
//            System.out.print((char) read);
            return read;
        }
        
    }
    
    private static class OutputStreamWrapper extends OutputStream {

        private final OutputStream delegateTo;

        public OutputStreamWrapper(OutputStream out) {
            this.delegateTo = out;
        }

        @Override
        public void write(int arg0) throws IOException {
//            System.out.print((char) arg0);
            delegateTo.write(arg0);
        }

        @Override
        public void flush() throws IOException {
            delegateTo.flush();
        }
    }
}
