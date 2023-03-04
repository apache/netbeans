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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner.BufferedLineProcessor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 * @author Egor Ushakov
 */
public class PathUtils {

    private PathUtils() {
    }

    public static String getPathFromSymlink(String path, ExecutionEnvironment execEnv) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("/bin/ls").setArguments("-l", path).redirectError(); // NOI18N
        ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
        List<String> outputLines = res.getOutputLines();
        String line = outputLines.isEmpty() ? null : outputLines.get(0); // just read 1st line...
        if (line != null) {
            int pos = line.indexOf("->"); // NOI18N
            if (pos > 0) {
                return line.substring(pos + 2).trim();
            }
        }
        return null;
    }
    
    public static String expandPath(String text, ExecutionEnvironment env) throws IOException, ConnectionManager.CancellationException, ParseException {
        HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
        
        text = MacroExpanderFactory.getExpander(env).expandMacros(text, hostInfo.getEnvironment());
        
        char fileSeparatorChar = '/'; //NOI18N
        if (env.isLocal()) {
            fileSeparatorChar =  File.separatorChar;
        } 
        if (text.equals("~")) {//NOI18N
            return hostInfo.getUserDir();
        } else if (text.startsWith("~" + fileSeparatorChar)) {//NOI18N
            return hostInfo.getUserDir() + text.substring(1);
        } else {
            return text;
        }
    }


    public static String getExePath(long pid, ExecutionEnvironment execEnv) {
        if (pid > 0) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            String path = PathUtils.getPathFromSymlink(procdir + "/path/a.out", execEnv); // NOI18N - Solaris only?
            if (path == null) {
                path = PathUtils.getPathFromSymlink(procdir + "/exe", execEnv); // NOI18N - Linux?
            }
            if (path != null && path.length() > 0) {
                return path;
            }
            //alternative: run script on the remote machine which will provide the information about executable for pid
           path = getExePath_(pid, execEnv);           
           return path;
        }
        return null;
    }
    
    private static String getExePath_(long pid, ExecutionEnvironment execEnv) {
        return run_("exepath.sh", pid, execEnv);//NOI18N
    }
    
    private static String getCwdPath_(long pid, ExecutionEnvironment execEnv) {  
        return run_("cwdpath.sh", pid, execEnv);//NOI18N
    }
    
    private static String run_(String scriptName, long pid, ExecutionEnvironment execEnv) {
        try {
            InstalledFileLocator fl = InstalledFileLocator.getDefault();//InstalledFileLocatorProvider.getDefault();
            File localScript = fl.locate("bin/nativeexecution/" + scriptName, "org.netbeans.modules.nativeexecution", false); // NOI18N                
            if (localScript == null) {
                return null;
            }                    
            BufferedLineProcessor bufferedLineProcessor = new ShellScriptRunner.BufferedLineProcessor();
            ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, Utilities.toURI(localScript), bufferedLineProcessor);
            scriptRunner.setArguments(pid + "");//NOI18N
            int execute = scriptRunner.execute();
            if (execute != 0) {
                return null;
            }
            
            return bufferedLineProcessor.getAsString();
        } catch (IOException ex) {            
        } catch (CancellationException ex) {            
        }
        return null;
    }
    
    public static String getCwdPath(long pid, ExecutionEnvironment execEnv) {
        if (pid > 0) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            String path = PathUtils.getPathFromSymlink(procdir + "/cwd", execEnv); // NOI18N

            if (path != null && path.length() > 0) {
                return path;
            }
            //alternative: run script on the remote machine which will provide the information about executable for pid
           path = getCwdPath_(pid, execEnv);
           return path;            
        }
        return null;
    }
}
