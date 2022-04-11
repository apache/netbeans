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
import java.util.List;

/**
 * Information about one process
 * 
 */
public final class ProcessInfo implements Comparable<ProcessInfo> {

    private final List<Object> info;
    private final String executable;
    private final Integer pid;
    private final Integer ppid;
    /*package*/// final ProcessInfoProvider provider;
    private final List<ProcessInfoDescriptor> descriptors;

    private ProcessInfo(
           // ProcessInfoProvider provider,
            List<ProcessInfoDescriptor> descriptors,
            Integer pid, Integer ppid, 
            String executable, 
            List<Object> info) {
        this.info = new ArrayList(info);
        this.descriptors = descriptors;
       // this.provider = provider;
        this.pid = pid;
        this.ppid = ppid;
        //and change values to Integer fo PId and PPID
        String command = null;
        for (int i = 0; i < descriptors.size(); i++) {
            if (descriptors.get(i).id.equals(ProcessInfoDescriptor.PID_COLUMN_ID)) {
                this.info.set(i, pid);
            } else if (descriptors.get(i).id.equals(ProcessInfoDescriptor.PPID_COLUMN_ID)) {
                this.info.set(i, ppid);
            } else if (descriptors.get(i).id.equals(ProcessInfoDescriptor.COMMAND_COLUMN_ID)) {
                command = info.get(i) + "";
            }
        }
        this.executable = executable == null ?  command :executable;
    }
    
    public static ProcessInfo create(List<ProcessInfoDescriptor> descriptors, List<Object> info, String executable) {
        int pid = -1;
        int ppid = -1;
        int idx = 0;
        try{
            for (ProcessInfoDescriptor descriptor : descriptors) {
                if (ProcessInfoDescriptor.PID_COLUMN_ID.equals(descriptor.id)) {
                    pid = Integer.parseInt("" + info.get(idx));
                } else  if (ProcessInfoDescriptor.PPID_COLUMN_ID.equals(descriptor.id)) {
                    ppid = Integer.parseInt("" + info.get(idx));
                }
                idx++;
            }
        }catch (NumberFormatException ex) {
            throw new IllegalArgumentException("incorrect data passed to create process info. pid  or ppid are not parsed as integer"); //NOI18N
        }
        return new ProcessInfo(descriptors, pid, ppid, executable,info);
    }

    public Integer getPID() {
        return pid;
    }

    public Integer getPPID() {
        return ppid;
    }

    public String getExecutable() {
        return executable;
    }

    public List<ProcessInfoDescriptor> getDescriptors() {
        return descriptors;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String id, Class<T> clazz) {
        int idx = 0;
        for (ProcessInfoDescriptor d : descriptors) {
            if (d.id.equals(id) && d.type.equals(clazz)) {
                return (T) info.get(idx);
            }
            idx++;
        }

        return null;
    }
//    public Object get(ProcessInfoDescriptor d) {
//        
//    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(String descriptor_id, String exactValue) {

        for (ProcessInfoDescriptor d : descriptors) {
            if (!d.id.equals(descriptor_id)) {
                continue;
            }
            Object data = get(d.id, d.type);
            if (data != null && data.toString().equals(exactValue)) {
                return true;
            }
        }

        return false;
    }
    

    @SuppressWarnings("unchecked")
    /*package*/ boolean matches(String filter) {
        if (pid.toString().contains(filter)) {
            return true;
        }

        for (ProcessInfoDescriptor d : descriptors) {
            Object data = get(d.id, d.type);
            if (data != null && data.toString().contains(filter)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProcessInfo)) {
            return false;
        }

        ProcessInfo that = (ProcessInfo) obj;
        return this.pid.equals(that.pid) && this.ppid.intValue() == that.ppid.intValue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.info != null ? this.info.hashCode() : 0);
//        hash = 89 * hash + (this.executable != null ? this.executable.hashCode() : 0);
        hash = 89 * hash + (this.pid != null ? this.pid.hashCode() : 0);
        hash = 89 * hash + (this.ppid != null ? this.ppid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ProcessInfo: " + getPID() ;//+ " " + executable; // NOI18N
    }

//    public String getCommandLine() {
//        int idx = 0;
//        for (ProcessInfoDescriptor d : provider.getDescriptors()) {
//            if ("commandline".equals(d.id)) { // NOI18N
//                return (String) info.get(idx);
//            }
//            idx++;
//        }
//        return executable;
//    }

    @Override
    public int compareTo(ProcessInfo o) {
        return pid.compareTo(o.pid);
    }
    
    public void updateInfo(String id, Object value) {
        int idx = 0;
        for (ProcessInfoDescriptor descriptor : descriptors) {
            if (descriptor.id.equals(id)) {
                info.set(idx, value);
            }
            idx++;
        }
    }
}
