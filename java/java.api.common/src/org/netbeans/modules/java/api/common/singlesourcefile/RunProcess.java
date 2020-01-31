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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sarvesh Kesharwani
 */
class RunProcess implements Callable<Process> {
    
    private static final Logger LOG = Logger.getLogger(RunProcess.class.getName());

    private final String dirPath;
    private final List<String> commandsList;
    private InputStream is;
    private Process p;

    public RunProcess(String command, String dirPath) {
        this.dirPath = dirPath;
        commandsList = new ArrayList<>();
        commandsList.add(command);
        setupProcess();
    }

    public RunProcess(String command) {
        commandsList = new ArrayList<>();
        commandsList.add(command);
        this.dirPath = System.getProperty("user.home");
        setupProcess();
    }

    public RunProcess(List<String> commandsList) {
        this.commandsList = commandsList;
        this.dirPath = System.getProperty("user.home");
        setupProcess();
    }

    public void setupProcess() {
        try {
            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(commandsList);
            runFileProcessBuilder.directory(new File(dirPath));
            runFileProcessBuilder.redirectErrorStream(true);
            p = runFileProcessBuilder.start();
            is = p.getInputStream();
        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Could not get InputStream of Run Process"); //NOI18N
        }
    }

    public InputStream getInputStream() {
        return is;
    }

    @Override
    public Process call() throws Exception {
        return p;
    }
    
}
