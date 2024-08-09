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
package org.netbeans.modules.kotlin.editor.lsp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

@MimeRegistration(mimeType="text/x-kotlin", service=LanguageServerProvider.class)
public class KotlinLSPServer implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(KotlinLSPServer.class.getName());

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        ServerRestarter restarter = lookup.lookup(ServerRestarter.class);
        Preferences settingsNode = Utils.settings().node(Utils.NODE_LSP);
        settingsNode.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey() == null || Utils.KEY_LOCATION.equals(evt.getKey())) {
                    restarter.restart();
                    settingsNode.removePreferenceChangeListener(this);
                }
            }
        });
        File server = Utils.getServer();
        if (server == null) {
            return null;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(new String[] {server.getAbsolutePath()}).directory(server.getParentFile().getParentFile());
            Map<String, String> env = builder.environment();
            File mvnCommand = InstalledFileLocator.getDefault().locate("maven/bin/mvn", null, false);
            if (mvnCommand != null) {
                env.put("PATH", env.get("PATH") + File.pathSeparator + mvnCommand.getParentFile().getAbsolutePath());
            }
            builder.redirectError(LOG.isLoggable(Level.FINE) ? Redirect.INHERIT : Redirect.DISCARD);
            Process p = builder.start();
            InputStream in = p.getInputStream();
            OutputStream out = p.getOutputStream();

            if (LOG.isLoggable(Level.FINE)) {
                in = new DuplicateInput(in);
                out = new DuplicateOutput(out);
            }
            return LanguageServerDescription.create(in, out, p);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

    private static final class DuplicateInput extends InputStream {

        private final InputStream source;

        public DuplicateInput(InputStream source) {
            this.source = source;
        }

        @Override
        public int read() throws IOException {
            int read = source.read();

            if (read != (-1)) {
                System.err.write(read);
            }

            return read;
        }

    }

    private static final class DuplicateOutput extends OutputStream {
        private final OutputStream sink;

        public DuplicateOutput(OutputStream out) {
            this.sink = out;
        }

        @Override
        public void write(int b) throws IOException {
            System.err.write(b);
            sink.write(b);
        }

        @Override
        public void flush() throws IOException {
            sink.flush();
        }

    }
}
