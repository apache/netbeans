/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
