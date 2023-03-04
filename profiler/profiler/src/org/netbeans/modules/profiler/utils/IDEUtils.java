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

package org.netbeans.modules.profiler.utils;

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.Platform;
import org.openide.util.NbBundle;
import org.netbeans.lib.profiler.common.Profiler;
import org.openide.util.HelpCtx;


/**
 * Utilities for interaction with the NetBeans IDE
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "IDEUtils_CreateNewConfigurationHint=<Create new configuration>",
    "IDEUtils_SelectSettingsConfigurationLabelText=Select the settings configuration to use:",
    "IDEUtils_SelectSettingsConfigurationDialogCaption=Select Settings Configuration",
    "IDEUtils_InvalidTargetJVMExeFileError=Invalid target JVM executable file specified: {0}\n{1}",
    "IDEUtils_ErrorConvertingProfilingSettingsMessage=Error occurred during automatic conversion of old Profiler configuration file\n   {0}\nto a new version\n   {1}.\n\nOperating system message:\n{2}",
    "IDEUtils_ListAccessName=List of available settings configurations.",
    "IDEUtils_OkButtonText=OK"
})
public final class IDEUtils {
    
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static HelpCtx HELP_CTX = new HelpCtx("SelectSettingsConfiguration.HelpCtx"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static String getAntProfilerStartArgument15(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_15_STRING);
    }

    public static String getAntProfilerStartArgument16(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_16_STRING);
    }

    public static String getAntProfilerStartArgument17(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_17_STRING);
    }

    public static String getAntProfilerStartArgument18(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_18_STRING);
    }

    public static String getAntProfilerStartArgument19(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_19_STRING);
    }

    public static String getAntProfilerStartArgument110Beyond(int port, int architecture) {
        return getAntProfilerStartArgument(port, architecture, CommonConstants.JDK_110_BEYOND_STRING);
    }

//    // Searches for a localized help. The default directory is <profiler_cluster>/docs/profiler,
//    // localized help is in <profiler_cluster>/docs/profiler_<locale_suffix> as obtained by NbBundle.getLocalizingSuffixes()
//    // see Issue 65429 (http://www.netbeans.org/issues/show_bug.cgi?id=65429)
//    public static String getHelpDir() {
//        Iterator suffixesIterator = NbBundle.getLocalizingSuffixes();
//        File localizedHelpDir = null;
//
//        while (suffixesIterator.hasNext() && (localizedHelpDir == null)) {
//            localizedHelpDir = InstalledFileLocator.getDefault()
//                                                   .locate("docs/profiler" + suffixesIterator.next(),
//                                                           "org.netbeans.modules.profiler", false); //NOI18N
//        }
//
//        if (localizedHelpDir == null) {
//            return null;
//        } else {
//            return localizedHelpDir.getPath();
//        }
//    }


    private static String getAntProfilerStartArgument(int port, int architecture, String jdkVersion) {
        String ld = Profiler.getDefault().getLibsDir();
        String nativeLib = Platform.getAgentNativeLibFullName(ld, false, jdkVersion, architecture);
        
        if (ld.contains(" ")) { // NOI18N
            ld = "\"" + ld + "\""; // NOI18N
        }
        if (nativeLib.contains(" ")) { // NOI18N
            nativeLib = "\"" + nativeLib + "\""; // NOI18N
        }
        
        // -agentpath:D:/Testing/41 userdir/lib/deployed/jdk15/windows/profilerinterface.dll=D:\Testing\41 userdir\lib,5140
        return "-agentpath:" // NOI18N
               + nativeLib + "=" // NOI18N
               + ld + "," // NOI18N
               + port + "," // NOI18N
               + System.getProperty("profiler.agent.connect.timeout", "10"); // NOI18N // 10 seconds timeout by default
    }
    
}
