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
package org.netbeans.modules.profiler.attach.spi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class AbstractRemotePackExporter {
    private static final Map<String, String> scriptMapper = new HashMap<String, String>() {
        {
            put(IntegrationUtils.PLATFORM_LINUX_AMD64_OS, "linuxamd64"); //NOI18N
            put(IntegrationUtils.PLATFORM_LINUX_OS, "linux"); //NOI18N
            put(IntegrationUtils.PLATFORM_LINUX_ARM_OS, "linuxarm"); //NOI18N
            put(IntegrationUtils.PLATFORM_LINUX_ARM_VFP_HFLT_OS, "linuxarmvfphflt"); //NOI18N
            put(IntegrationUtils.PLATFORM_MAC_OS, "mac"); //NOI18N
            put(IntegrationUtils.PLATFORM_SOLARIS_AMD64_OS, "solamd64"); //NOI18N
            put(IntegrationUtils.PLATFORM_SOLARIS_INTEL_OS, "solx86"); //NOI18N
            put(IntegrationUtils.PLATFORM_SOLARIS_SPARC64_OS, "solsparcv9"); //NOI18N
            put(IntegrationUtils.PLATFORM_WINDOWS_AMD64_OS, "winamd64"); //NOI18N
            put(IntegrationUtils.PLATFORM_WINDOWS_OS, "win"); //NOI18N
        }
    };
    private static final Map<String, String> jdkMapper = new HashMap<String, String>() {
        {
            // NOTE: 15 is used to only generate Ant task name which always ends with '-15'
            put(TargetPlatformEnum.JDK5.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK6.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK7.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK8.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK9.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK110_BEYOND.toString(), "15"); //NOI18N
            put(TargetPlatformEnum.JDK_CVM.toString(), "cvm"); //NOI18N
        }
    };
    
    protected final String getPlatformShort(String hostOS) {
        return scriptMapper.get(hostOS);
    }
    
    protected final String getJVMShort(String jvm) {
        return jdkMapper.get(jvm);
    }
    
    public abstract String export(String exportPath, String hostOS, String jvm) throws IOException;
    public abstract String getRemotePackPath(String exportPath, String hostOS);
}
