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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cpplite.editor.Utils;
import org.netbeans.modules.cpplite.editor.file.MIMETypes;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.util.Lookup;
import org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider;
import org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider.ProjectConfiguration;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Pair;

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

    private static final Map<Project, Pair<Process, LanguageServerDescription>> prj2Server = new HashMap<>();

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
                if (evt.getKey() == null || Utils.KEY_CCLS_PATH.equals(evt.getKey()) || Utils.KEY_CLANGD_PATH.equals(evt.getKey())) {
                    prj2Server.remove(prj);
                    restarter.restart();
                    Utils.settings().removePreferenceChangeListener(this);
                }
            }
        });
        String ccls = Utils.getCCLSPath();
        String clangd = Utils.getCLANGDPath();
        if (ccls != null || clangd != null) {
            Pair<Process, LanguageServerDescription> serverEntry = prj2Server.compute(prj, (p, pair) -> {
                if (pair != null && pair.first().isAlive()) {
                    return pair;
                }
                try {
                    List<String> command = new ArrayList<>();

                    CProjectConfigurationProvider config = getProjectSettings(prj);
                    config.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            prj2Server.remove(prj);
                            restarter.restart();
                            config.removeChangeListener(this);
                        }
                    });
                    File compileCommandDirs = getCompileCommandsDir(config);

                    if (compileCommandDirs != null) {
                        if (ccls != null) {
                            command.add(ccls);
                            StringBuilder initOpt = new StringBuilder();
                            initOpt.append("--init={\"compilationDatabaseDirectory\":\"");
                            initOpt.append(compileCommandDirs.getAbsolutePath());
                            initOpt.append("\"}");
                            command.add(initOpt.toString());
                        } else {
                            command.add(clangd);
                            command.add("--compile-commands-dir=" + compileCommandDirs.getAbsolutePath());
                            command.add("--clang-tidy");
                            command.add("--completion-style=detailed");
                        }
                        ProcessBuilder builder = new ProcessBuilder(command);
                        if (LOG.isLoggable(Level.FINEST)) {
                            builder.redirectError(Redirect.INHERIT);
                        } else {
                            builder.redirectError(Redirect.DISCARD);
                        }
                        Process process = builder.start();
                        InputStream in = process.getInputStream();
                        OutputStream out = process.getOutputStream();
                        if (LOG.isLoggable(Level.FINEST)) {
                            in = new CopyInput(in, System.err);
                            out = new CopyOutput(out, System.err);
                        }
                        return Pair.of(process, LanguageServerDescription.create(in, out, process));
                    }
                    return null;
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                    return null;
                }
            });
            if(serverEntry != null) {
                return serverEntry.second();
            }
            return null;
        }
        return null;
    }
    
    public static File getCompileCommandsDir(Project prj) {
        return getCompileCommandsDir(getProjectSettings(prj));
    }

    private static CProjectConfigurationProvider getProjectSettings(Project prj) {
        CProjectConfigurationProvider configProvider = prj.getLookup().lookup(CProjectConfigurationProvider.class);
        if (configProvider == null) {
            configProvider = new CProjectConfigurationProvider() {
                @Override
                public ProjectConfiguration getProjectConfiguration() {
                    return new ProjectConfiguration(new File(FileUtil.toFile(prj.getProjectDirectory()), "compile_commands.json").getAbsolutePath());
                }
                @Override
                public void addChangeListener(ChangeListener listener) {
                }
                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }
        return configProvider;
    }

    private static int tempDirIndex = 0;

    private static File getCompileCommandsDir(CProjectConfigurationProvider configProvider) {
        ProjectConfiguration config = configProvider.getProjectConfiguration();

        if (config == null) {
            return null;
        }

        File commandsPath = config.commandJsonPath != null ? new File(config.commandJsonPath) : null;

        if (config.commandJsonCommand != null || (commandsPath != null && commandsPath.canRead()) || config.commandJsonContent != null) {
            File tempFile = Places.getCacheSubfile("cpplite/compile_commands/" + tempDirIndex++ + "/compile_commands.json");
            if (config.commandJsonCommand != null) {
                try {
                    new ProcessBuilder(config.commandJsonCommand).redirectOutput(tempFile).redirectError(Redirect.INHERIT).start().waitFor();
                } catch (IOException | InterruptedException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return null;
                }
            } else if (commandsPath != null && commandsPath.canRead()) {
                try (InputStream in = new FileInputStream(commandsPath);
                     OutputStream out = new FileOutputStream(tempFile)) {
                    FileUtil.copy(in, out);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return null;
                }
            } else if (config.commandJsonContent != null) {
                try (OutputStream out = new FileOutputStream(tempFile)) {
                    out.write(config.commandJsonContent.getBytes());
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return null;
                }
            } else {
                return null;
            }
            return tempFile.getParentFile();
        }
        return null;
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
