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

