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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 *
 */
public class Agent {
    public static void main(String[] args){
        if (args.length < 1) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java org.netbeans.modules.remote.impl.fileoperations.Agent fileName"); // NOI18N
            return;
        }
        String filePath = args[0];
        File file = new File(filePath);
        System.out.println("exists="+file.exists());
        System.out.println("canWrite="+file.canWrite());
        System.out.println("isDirectory="+file.isDirectory());
        System.out.println("isFile="+file.isFile());
        System.out.println("getName="+file.getName());
        System.out.println("getAbsolutePath="+file.getAbsolutePath());
        System.out.println("getParent="+file.getParent());
        String[] list = file.list();
        if (list == null) {
            System.out.println("list="+list);
        } else {
            StringBuilder buf = new StringBuilder("list=");
            for(String s : list) {
                buf.append(s);
                buf.append(';');
            }
            System.out.println(buf.toString());
        }
    }
    
    private final ExecutionEnvironment execEnv;
    private final String pathToClass;
    
    public Agent(ExecutionEnvironment execEnv, String pathToClass) {
        this.execEnv = execEnv;
        this.pathToClass = pathToClass;
    }
    
    public Map<String, Object> execute(String filePath) {
        ExitStatus res = ProcessUtils.executeInDir(pathToClass, execEnv, "java", Agent.class.getName(), filePath);
        if (res.exitCode == 0) {
            String[] split = res.getOutputString().split("\n");
            Map<String, Object> map = new HashMap<>();
            for(String s : split) {
                String key = s.substring(0, s.indexOf('='));
                String value = s.substring(s.indexOf('=')+1);
                if ("exists".equals(key) || "canWrite".equals(key) ||
                    "isDirectory".equals(key) || "isFile".equals(key)) {
                    map.put(key, value.equals("true"));
                } else if ("getName".equals(key) || "getAbsolutePath".equals(key) ||
                           "getParent".equals(key)) {
                    map.put(key, value);
                } else if ("list".equals(key)) {
                    if ("null".equals(value)) {
                        map.put(key, null);
                    } else {
                        if (value.indexOf(';') < 0) {
                            map.put(key, new String[0]);
                        } else {
                            map.put(key, value.split(";"));
                        }
                    }
                }
            }
            return map;
        } else {
            System.err.println("Cannot run java on "+execEnv.getDisplayName());
            System.err.println(res.getErrorString());
            return null;
        }
    }
}
