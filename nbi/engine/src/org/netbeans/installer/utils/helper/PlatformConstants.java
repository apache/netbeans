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

package org.netbeans.installer.utils.helper;

public final class PlatformConstants {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private PlatformConstants() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String OS_FAMILY_WINDOWS = 
            "windows";
    
    public static final String OS_FAMILY_LINUX = 
            "linux";
    
    public static final String OS_FAMILY_SOLARIS = 
            "solaris";
    
    public static final String OS_FAMILY_MACOSX = 
            "macosx";
    
    public static final String OS_FAMILY_UNIX = 
            "unix";
    
    public static final String OS_FAMILY_FREEBSD = 
            "freebsd";

    public static final String OS_FAMILY_OPENBSD = 
            "openbsd";
    
    public static final String OS_FAMILY_BSD = 
            "bsd";

    public static final String OS_FAMILY_AIX =
            "aix";

    public static final String OS_FAMILY_HPUX =
            "hpux";
    
    public static final String HARDWARE_X86 = 
            "x86";
    
    public static final String HARDWARE_X64 = 
            "x64";

    public static final String HARDWARE_IA64 = 
            "ia64";

    //this includes PA_RISC 1.0 and 1.1
    public static final String HARDWARE_PA_RISC = 
            "pa_risc"; 
    
    public static final String HARDWARE_PA_RISC20 = 
            "pa_risc2.0";
    
    public static final String HARDWARE_SPARC = 
            "sparc";
    
    public static final String HARDWARE_PPC = 
            "ppc";
    
    public static final String HARDWARE_PPC64 = 
            "ppc64";
}
