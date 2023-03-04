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
package org.netbeans.modules.java.nativeimage.debugger.actions;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Retrieve a list of native processes.
 * Duplicates org.netbeans.modules.java.lsp.server.debugging.attach.Processes
 * Replace with the direct use of ProcessHandle, after JDK 8 is abandoned.
 *
 * @author martin
 */
final class Processes {

    private static final String CMD_UNIX = "ps -x -o pid,command";  // NOI18N
    private static final String CMD_WIN = "tasklist /FO CSV";       // NOI18N

    static List<ProcessInfo> getAllProcesses() {
        List<ProcessInfo> processInfos;
        Function<Stream<ProcessInfo>, List<ProcessInfo>> collector =
                infoStream -> infoStream
                    .filter(info -> info != null)
                    .sorted((info1, info2) -> (info1.getPid() == info2.getPid() ? 0 : info1.getPid() > info2.getPid() ? -1 : 1)) // descending
                    .collect(Collectors.toList());
        try {
            Class<?> processHandleClass = Class.forName("java.lang.ProcessHandle");    // NOI18N
            processInfos = getProcesses(processHandleClass, collector);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            if (Utilities.isWindows()) {
                processInfos = getProcessesWinCmd(collector);
            } else {
                processInfos = getProcessesUNIXCmd(collector);
            }
        }
        return processInfos;
    }

    private static List<ProcessInfo> getProcesses(Class<?> processHandleClass,
            Function<Stream<ProcessInfo>, List<ProcessInfo>> collector) throws NoSuchMethodException, IllegalAccessException {
        Method allProcessesMethod = processHandleClass.getMethod("allProcesses");   // NOI18N
        try {
            @SuppressWarnings("unchecked")
            Stream<Object> allProcesses = (Stream<Object>) allProcessesMethod.invoke(null);
            return collector.apply(allProcesses
                    .map(new ProcessHandle2Info(processHandleClass)));
        } catch (IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    private static class ProcessHandle2Info implements Function<Object, ProcessInfo> {

        private final Method pidMethod;
        private final Method infoMethod;
        private final Method commandMethod;
        private final Method commandLineMethod;

        ProcessHandle2Info(Class<?> processHandleClass) throws NoSuchMethodException {
            pidMethod = processHandleClass.getMethod("pid");        // NOI18N
            infoMethod = processHandleClass.getMethod("info");      // NOI18N
            commandMethod = infoMethod.getReturnType().getMethod("command");    // NOI18N
            commandLineMethod = infoMethod.getReturnType().getMethod("commandLine"); // NOI18N
        }

        @Override
        @SuppressWarnings("unchecked")
        public ProcessInfo apply(Object processHandle) {
            try {
                long pid = (Long) pidMethod.invoke(processHandle);
                Object info = infoMethod.invoke(processHandle);
                Optional<String> command = (Optional<String>) commandMethod.invoke(info);
                Optional<String> commandLine = (Optional<String>) commandLineMethod.invoke(info);
                if (command.isPresent() && commandLine.isPresent()) {
                    return new ProcessInfo(pid, command.get(), commandLine.get());
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }

    private static List<ProcessInfo> getProcessesUNIXCmd(Function<Stream<ProcessInfo>, List<ProcessInfo>> collector) {
        return getProcesses(CMD_UNIX, line -> {
            int i2 = line.indexOf(' ');
            if (i2 < 0) {
                return null;
            }
            String pidStr = line.substring(0, i2);
            long pid;
            try {
                pid = Long.parseLong(pidStr);
            } catch (NumberFormatException ex) {
                return null;
            }
            int i1 = i2 + 1;
            String commandLine = line.substring(i1).trim();
            if (CMD_UNIX.equals(commandLine)) {
                return null;
            }
            return new ProcessInfo(pid, null, commandLine);
        }, collector);
    }

    private static List<ProcessInfo> getProcessesWinCmd(Function<Stream<ProcessInfo>, List<ProcessInfo>> collector) {
        return getProcesses(CMD_WIN, line -> {
            if (!line.startsWith("")) {
                return null;
            }
            int i1 = 1;
            int i2 = line.indexOf('"', i1);
            if (i2 < 0) {
                return null;
            }
            String execName = line.substring(i1, i2);
            i1 = line.indexOf('"', i2 + 1);
            if (i1 < 0) {
                return null;
            }
            i1++;
            i2 = line.indexOf('"', i1);
            if (i2 < 0) {
                return null;
            }
            String pidStr = line.substring(i1, i2);
            long pid;
            try {
                pid = Long.parseLong(pidStr);
            } catch (NumberFormatException ex) {
                return null;
            }
            return new ProcessInfo(pid, execName, execName);
        }, collector);
    }

    private static List<ProcessInfo> getProcesses(String cmd,
            Function<? super String, ? extends ProcessInfo> pmap,
            Function<Stream<ProcessInfo>, List<ProcessInfo>> collector) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                return collector.apply(reader
                        .lines()
                        .map(line -> line.trim())
                        .map(pmap));
            }
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    static final class ProcessInfo {

        private final long pid;
        private final String executable;
        private final String command;

        ProcessInfo(long id, String executable, String command) {
            assert command != null;
            this.pid = id;
            this.executable = executable;
            this.command = command;
        }

        /**
         * Get the ID of the process.
         */
        long getPid() {
            return pid;
        }

        /**
         * Get the executable of the process.
         */
        String getExecutable() {
            if (executable != null) {
                return executable;
            }
            File exeFile = new File("/proc/" + pid + "/exe");
            if (exeFile.exists()) {
                try {
                    return exeFile.getCanonicalPath();
                } catch (IOException ex) {}
            }
            int i = command.indexOf(' ');
            return i > 0 ? command.substring(0, i) : command;
        }

        /**
         * Get the command line of the process.
         */
        String getCommand() {
            return command;
        }

    }
}
