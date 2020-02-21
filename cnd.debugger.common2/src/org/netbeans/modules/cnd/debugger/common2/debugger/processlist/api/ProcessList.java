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

package org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.modules.cnd.debugger.common2.ProcessListAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public final class ProcessList {
    
    static {
        ProcessListAccessor.setDefault(new ProcessListAccessorImpl());
    }

    private final HashMap<Integer, ProcessInfo> data;
    private final ExecutionEnvironment execEnv;

    /*package*/ ProcessList(final Collection<ProcessInfo> info, ExecutionEnvironment execEnv) {
        this.data = new HashMap<Integer, ProcessInfo>(info.size());
        this.execEnv = execEnv;
        for (ProcessInfo i : info) {
            this.data.put(i.getPID(), i);
        }
    }

    public ProcessInfo getInfo(Integer pid) {
        return data.get(pid);
    }

    public Collection<Integer> getPIDs() {
        return data.keySet();
    }
        
//    public Collection<Integer> getExecutablePIDs(String executable) {
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        if (executable == null) {
//            return result;
//        }
//        for (Integer pid : getPIDs()) {
//            ProcessInfo info = getInfo(pid);
//            String infoExecutable = info.getExecutable();
//            if (!infoExecutable.equals(executable)) {
//                infoExecutable = FileSystemProvider.normalizeAbsolutePath(infoExecutable, execEnv);
//            }
//            if (infoExecutable.equals(executable)) {
//                result.add(pid);
//            }
//        }
//
//        return result;
//    }
    
    public Collection<Integer> getPIDs(String filter) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (filter == null || filter.isEmpty()) {
            return getPIDs();
        }
        for (Integer pid : getPIDs()) {
            ProcessInfo info = getInfo(pid);

            if (info.matches(filter)) {
                result.add(pid);
            }
        }

        return result;
    }
      

    public Collection<Integer> getPIDs(Integer ppid) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Integer pid : getPIDs()) {
            if (getInfo(pid).getPPID().equals(ppid)) {
                result.add(pid);
            }
        }

        return result;
    }
    
    private static class ProcessListAccessorImpl extends ProcessListAccessor {

        @Override
        public ProcessList create(Collection<ProcessInfo> info, ExecutionEnvironment execEnv) {
            return new ProcessList(info, execEnv);
        }
        
    } 
}

