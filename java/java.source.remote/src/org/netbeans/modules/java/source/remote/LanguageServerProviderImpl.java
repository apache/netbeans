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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.spi.java.source.RemotePlatform;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 */
@MimeRegistration(mimeType="text/x-java", service=LanguageServerProvider.class)
public class LanguageServerProviderImpl implements LanguageServerProvider {
    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        Project prj = lookup.lookup(Project.class);
        RemotePlatform remotePlatform = RemotePlatform.lookupRemotePlatform(prj.getProjectDirectory());
        if (remotePlatform == null) {
            return null;
        }
        try {
            File launcher = InstalledFileLocator.getDefault().locate("modules/scripts/org-netbeans-modules-java-source-remote/bin/nb-java-lsp-server", "org.netbeans.modules.java.source.remote", false);

            Process process = new ProcessBuilder(launcher.getAbsolutePath(), "--jdkhome", new File(remotePlatform.getJavaCommand()).getParentFile().getParentFile().getAbsolutePath(), "--installdir", launcher.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
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
