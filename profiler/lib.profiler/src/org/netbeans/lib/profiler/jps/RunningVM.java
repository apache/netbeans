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

package org.netbeans.lib.profiler.jps;


/**
 * A container for various information available for a running JVM.
 * Note that "VM flags" that we have for the VM in principle, is various -XX:+... options, which are supposed to
 * be used only by real expert users, or for debugging. We have them here just for completeness, but since they
 * are used very rarely, there is probably no reason to display them in the attach dialog or whatever.
 *
 * @author Misha Dmitriev
 */
public class RunningVM {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String mainArgs;
    private String mainClass;
    private String vmArgs;
    private String vmFlags;
    private int pid;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of RunningVM */
    public RunningVM(int pid, String vmFlags, String vmArgs, String mainClass, String mainArgs) {
        this.pid = pid;
        this.vmFlags = vmFlags;
        this.vmArgs = vmArgs;
        this.mainClass = mainClass;
        this.mainArgs = mainArgs;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getMainArgs() {
        return mainArgs;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getPid() {
        return pid;
    }

    public String getVMArgs() {
        return vmArgs;
    }

    public String getVMFlags() {
        return vmFlags;
    }

    public String toString() {
        return getPid() + "  " + getVMFlags() + "  " + getVMArgs() + "  " + getMainClass() + "  " + getMainArgs(); // NOI18N
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof RunningVM)) return false;
        return toString().equals(o.toString());
    }
}
