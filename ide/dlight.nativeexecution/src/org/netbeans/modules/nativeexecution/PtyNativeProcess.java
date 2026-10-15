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
package org.netbeans.modules.nativeexecution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.PtyUtility;

/**
 *
 * @author ak119685
 */
public final class PtyNativeProcess extends AbstractNativeProcess {

    private AbstractNativeProcess delegate = null;

    public PtyNativeProcess(final NativeProcessInfo info) {
        super(new NativeProcessInfo(info, true));
    }

    @Override
    protected void create() throws Throwable {
        ExecutionEnvironment env = info.getExecutionEnvironment();
        Pty pty = info.getPty();

        List<String> newArgs = new ArrayList<>();

        if (pty != null) {
            newArgs.add("-p"); // NOI18N
            newArgs.add(pty.getSlaveName());
        }

        if (FIX_ERASE_KEY_IN_TERMINAL) {
            newArgs.add("--set-erase-key"); // NOI18N
        }

        final MacroMap envMap = info.getEnvironment();

        // We don't want pty to be affected by passed environment.
        // So at least defend ourselfs from LD_PRELOAD
        final Map<String, String> removedEntries = new HashMap<>();
        removedEntries.put("LD_PRELOAD", envMap.remove("LD_PRELOAD")); // NOI18N
        removedEntries.put("LD_PRELOAD_32", envMap.remove("LD_PRELOAD_32")); // NOI18N
        removedEntries.put("LD_PRELOAD_64", envMap.remove("LD_PRELOAD_64")); // NOI18N
        removedEntries.put("DYLD_INSERT_LIBRARIES", envMap.remove("DYLD_INSERT_LIBRARIES")); // NOI18N

        Iterator<Entry<String, String>> it = removedEntries.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (entry.getValue() == null) {
                it.remove();
                continue;
            }

            if (!entry.getValue().isEmpty()) {
                newArgs.add("--env"); // NOI18N
                newArgs.add(entry.getKey().trim() + "=" + entry.getValue().trim()); // NOI18N
            }
        }

        String origCommand = info.getCommandLineForShell();

        if (origCommand != null) {
            newArgs.add(hostInfo.getShell());
            newArgs.add("-c"); // NOI18N
            if (info.isRedirectError()) {
                newArgs.add("exec 2>&1; exec " + origCommand); // NOI18N
            } else {
                newArgs.add("exec " + origCommand); // NOI18N
            }
        } else {
            // this means that there is no shell available
            String processExecutable = info.getExecutable();

            if (hostInfo.getOSFamily() == OSFamily.WINDOWS) {
                // pty requires Unix style executable path
                processExecutable = WindowsSupport.getInstance().convertToShellPath(processExecutable);
            }

            newArgs.add(processExecutable);
            newArgs.addAll(info.getArguments());
        }

        info.setCommandLine(null);
        final String path = PtyUtility.getInstance().getPath(env);
        info.setExecutable(path);
        info.setArguments(newArgs.toArray(new String[0]));

        // no need to preload unbuffer in case of running in internal terminal
        info.setUnbuffer(false);

        NativeProcessInfo delegateInfo = new NativeProcessInfo(info, false);

        if (env.isLocal()) {
            delegate = new LocalNativeProcess(delegateInfo);
        } else {
            delegate = new RemoteNativeProcess(delegateInfo);
        }

        delegate.createAndStart();

        InputStream inputStream = delegate.getInputStream();

        if (pty != null) {
            setInputStream(pty.getInputStream());
            setOutputStream(pty.getOutputStream());
        } else {
            setInputStream(inputStream);
            setOutputStream(delegate.getOutputStream());
        }

        setErrorStream(delegate.getErrorStream());

        String pidLine = null;
        String ttyLine = null;
        String line;

        while ((line = readLine(inputStream)) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                break;
            }

            if (line.startsWith("PID=")) { // NOI18N
                pidLine = line.substring(4);
            } else if (line.startsWith("TTY=")) { // NOI18N
                addProcessInfo(line);
                ttyLine = line;
            }
        }

        if (pidLine == null || ttyLine == null) {
            String error = ProcessUtils.readProcessErrorLine(this);
            LOG.log(Level.INFO, "Unable to start pty process: binary={0}; args={1}; rc={2}", new Object[]{path, newArgs, error}); //NOI18N
            throw new IOException("Unable to start pty process: " + error); // NOI18N
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(pidLine.getBytes());
        readPID(bis);
    }

    @Override
    protected int waitResult() throws InterruptedException {
        if (delegate == null) {
            return 1;
        }

        int result = delegate.waitResult();
        finishing();

        return result;
    }

    private String readLine(final InputStream is) throws IOException {
        int c;
        StringBuilder sb = new StringBuilder(20);

        while (!isInterrupted()) {
            c = is.read();

            if (c < 0 || c == '\n') {
                break;
            }

            sb.append((char) c);
        }

        return sb.toString().trim();
    }
}
