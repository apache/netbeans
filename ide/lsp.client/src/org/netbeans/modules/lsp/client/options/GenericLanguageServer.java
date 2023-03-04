/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client.options;

import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jlahoda
 */
public class GenericLanguageServer implements LanguageServerProvider {

    private final RequestProcessor WORKER = new RequestProcessor(GenericLanguageServer.class.getName(), Integer.MAX_VALUE, false, false);
    private static final long STARTUP_DELAY = 10000;

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        MimeTypeInfo mti = lookup.lookup(MimeTypeInfo.class);

        if (mti == null) {
            return null;
        }
        
        Project prj = lookup.lookup(Project.class);
        FileObject server = FileUtil.getConfigFile("Editors/" + mti.mimeType + "/org-netbeans-modules-lsp-client-options-GenericLanguageServer.instance");
        String[] command = (String[]) server.getAttribute("command");
        String name = (String) server.getAttribute("name");

        if (name == null) {
            name = command[0];
        }

        ServerRestarter restarter = lookup.lookup(ServerRestarter.class);

        if (restarter != null) {
            server.addFileChangeListener(new FileChangeListener() {
                @Override
                public void fileFolderCreated(FileEvent fe) {
                    update();
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    update();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    update();
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    update();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    update();
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    update();
                }

                private void update() {
                    restarter.restart();
                    server.removeFileChangeListener(this);
                }
            });
        }

        try {
            InputOutput io = InputOutput.get("Language Server for " + name + " for project " + (prj != null ? ProjectUtils.getInformation(prj).getDisplayName() : "<unknown>"), false);
            io.reset();
            Process process = new ProcessBuilder(command).start();
            WORKER.post(() -> {
                long start = System.currentTimeMillis();
                try (Reader r = new InputStreamReader(process.getErrorStream())) {
                    int read;
                    while ((read = r.read()) != (-1)) {
                        io.getErr().write("" + (char) read);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    io.getErr().close();
                    io.getOut().close();
                    try {
                        io.getIn().close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                long end = System.currentTimeMillis();
                if (!process.isAlive() && (process.exitValue() != 0 || (end - start) < STARTUP_DELAY)) {
                    io.show();
                }
            });
            return LanguageServerDescription.create(process.getInputStream(), process.getOutputStream(), process);
        } catch (Throwable t) {
            t.printStackTrace(); //TODO
            return null;
        }
    }

}
