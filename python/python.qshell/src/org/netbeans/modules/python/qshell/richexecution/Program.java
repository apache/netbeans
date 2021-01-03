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

package org.netbeans.modules.python.qshell.richexecution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description of a program to be executed.
 * After and partial delegator to {@link java.lang.ProcessBuilder}.
 * Use {@link PtyExecutor} or subclasses thereof to run the program.
 */
public class Program {
    private final ProcessBuilder processBuilder;
    private List<String> command;

    public Program() {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        command = new ArrayList<>();
    }

    /**
     * After {@link ProcessBuilder#ProcessBuilder(java.util.List)}.
     */
    public Program(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
    }

    /**
     * After {@link ProcessBuilder#ProcessBuilder(java.lang.String[])}.
     */
    public Program(String... command) {
        processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        command(command);
    }

    public void add(String arg) {
        command.add(arg);
    }

    protected String basename(String name) {
        File nameFile = new File(name);
        return nameFile.getName();
    }

    public ProcessBuilder processBuilder() {
        return processBuilder;
    }

    public List<String> command() {
        return command;
    }

    public void command(List<String> command) {
        if (command == null) {
            throw new NullPointerException();
        }
        this.command = command;
    }

    public void command(String... command) {
        this.command = new ArrayList<>(command.length);
        for (String arg : command) {
            this.command.add(arg);
        }
    }

    public File directory() {
        return processBuilder.directory();
    }

    public void directory(File directory) {
        processBuilder.directory(directory);
    }

    public Map<String, String> environment() {
        return processBuilder.environment();
    }

    public String name() {
        if (command.size() == 0) {
            throw new IllegalStateException("No arguments assigned yet");
        }
        return command.get(0);
    }
}
