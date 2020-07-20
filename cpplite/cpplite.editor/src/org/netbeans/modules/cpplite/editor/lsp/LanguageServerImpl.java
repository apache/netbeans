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
package org.netbeans.modules.cpplite.editor.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cpplite.editor.Utils;
import org.netbeans.modules.cpplite.editor.file.MIMETypes;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider;
import org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider.ProjectConfiguration;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
@MimeRegistrations({
    @MimeRegistration(service=LanguageServerProvider.class,
                      mimeType=MIMETypes.C),
    @MimeRegistration(service=LanguageServerProvider.class,
                      mimeType=MIMETypes.CPP),
    @MimeRegistration(service=LanguageServerProvider.class,
                      mimeType=MIMETypes.H),
    @MimeRegistration(service=LanguageServerProvider.class,
                      mimeType=MIMETypes.HPP)
})
public class LanguageServerImpl implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());

    private Map<Project, LanguageServerDescription> prj2Server = new HashMap<>();

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        Project prj = lookup.lookup(Project.class);
        if (prj == null) {
            return null;
        }
        ServerRestarter restarter = lookup.lookup(ServerRestarter.class);
        Utils.settings().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey() == null || Utils.KEY_CCLS_PATH.equals(evt.getKey())) {
                    restarter.restart();
                    Utils.settings().removePreferenceChangeListener(this);
                }
            }
        });
        String ccls = Utils.settings().get(Utils.KEY_CCLS_PATH, null);
        if (ccls != null) {
            return prj2Server.computeIfAbsent(prj, (Project p) -> {
                try {
                    List<String> command = new ArrayList<>();
                    command.add(ccls);

                    List<String> cat = getProjectSettings(prj);

                    if (cat != null) {
                        StringBuilder initOpt = new StringBuilder();
                        initOpt.append("--init={\"compilationDatabaseCommand\":\"");
                        String sep = "";
                        for (String c : cat) {
                            initOpt.append(sep);
                            initOpt.append(c);
                            sep = " ";
                        }
                        initOpt.append("\"}");
                        command.add(initOpt.toString());
                        Process process = new ProcessBuilder(command).redirectError(Redirect.INHERIT).start();
                        return LanguageServerDescription.create(new CopyInput(process.getInputStream(), System.err), new CopyOutput(process.getOutputStream(), System.err), process);
                    }
                    return null;
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                    return null;
                }
            });
        }
        return null;
    }
    
    public static List<String> getProjectSettings(Project prj) {
        CProjectConfigurationProvider configProvider = prj.getLookup().lookup(CProjectConfigurationProvider.class);

        if (configProvider != null) {
            ProjectConfiguration config = configProvider.getProjectConfiguration();
            if (config != null && config.commandJsonCommand != null) {
                return configProvider.getProjectConfiguration().commandJsonCommand;
            } else if (config != null && configProvider.getProjectConfiguration().commandJsonPath != null) {
                //TODO: Linux independent!
                return Arrays.asList("cat", configProvider.getProjectConfiguration().commandJsonPath);
            }
            return null;
        } else if (prj.getProjectDirectory().getFileObject("compile_commands.json") != null) {
            //TODO: Linux independent!
            return Arrays.asList("cat", FileUtil.toFile(prj.getProjectDirectory().getFileObject("compile_commands.json")).getAbsolutePath());
        } else {
            return null;
        }
    }
    private static class CopyInput extends InputStream {

        private final InputStream delegate;
        private final OutputStream log;

        public CopyInput(InputStream delegate, OutputStream log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public int read() throws IOException {
            int read = delegate.read();
            log.write(read);
            return read;
        }
    }
    
    private static class CopyOutput extends OutputStream {

        private final OutputStream delegate;
        private final OutputStream log;

        public CopyOutput(OutputStream delegate, OutputStream log) {
            this.delegate = delegate;
            this.log = log;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            log.write(b);
            log.flush();
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
            log.flush();
        }
        
    }
}
