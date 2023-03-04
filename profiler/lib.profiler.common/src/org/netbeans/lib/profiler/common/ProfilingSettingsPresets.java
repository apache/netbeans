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

package org.netbeans.lib.profiler.common;

import org.netbeans.lib.profiler.global.CommonConstants;
import java.util.ResourceBundle;


/**
 * Factory class to create preset ProfilingSettings
 *
 * @author Jiri Sedlacek
 */
public class ProfilingSettingsPresets {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static final class CPUPreset extends ProfilingSettings {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CPUPreset(int type) {
            setIsPreset(true);
            setProfilingType(type);
            setSettingsName(CPU_PRESET_NAME);

            setCPUProfilingType(type == ProfilingSettings.PROFILE_CPU_SAMPLING ?
                    CommonConstants.CPU_SAMPLED : CommonConstants.CPU_INSTR_FULL);
            setInstrumentGetterSetterMethods(false);
            setInstrumentEmptyMethods(false);
            setInstrumentMethodInvoke(true);
            setExcludeWaitTime(true);

            if (type == ProfilingSettings.PROFILE_CPU_ENTIRE) {
                setInstrScheme(CommonConstants.INSTRSCHEME_TOTAL);
                //        setInstrumentSpawnedThreads(true);
                setInstrumentSpawnedThreads(false); // Should work better with Marker Methods
            } else if (type == ProfilingSettings.PROFILE_CPU_PART) {
                setInstrScheme(CommonConstants.INSTRSCHEME_LAZY);
                setInstrumentSpawnedThreads(false);
            } else if (type == ProfilingSettings.PROFILE_CPU_JDBC) {
                setInstrScheme(CommonConstants.INSTRSCHEME_LAZY);
                setInstrumentSpawnedThreads(false);
            } else if (type == ProfilingSettings.PROFILE_CPU_SAMPLING) {
                setSamplingFrequency(10);
                setThreadCPUTimerOn(true);
            }
        }
    }

    private static final class MemoryPreset extends ProfilingSettings {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MemoryPreset(int type) {
            setIsPreset(true);
            setProfilingType(type);
            setSettingsName(MEMORY_PRESET_NAME);
        }
    }

    private static final class MonitorPreset extends ProfilingSettings {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public MonitorPreset() {
            setIsPreset(true);
            setProfilingType(ProfilingSettings.PROFILE_MONITOR);
            setSettingsName(MONITOR_PRESET_NAME);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.lib.profiler.common.Bundle"); // NOI18N
    private static final String MONITOR_PRESET_NAME = bundle.getString("ProfilingSettingsPresets_MonitorPresetName"); // NOI18N
    private static final String CPU_PRESET_NAME = bundle.getString("ProfilingSettingsPresets_CpuPresetName"); // NOI18N
    private static final String MEMORY_PRESET_NAME = bundle.getString("ProfilingSettingsPresets_MemoryPresetName"); // NOI18N
                                                                                                                    // -----

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static ProfilingSettings createCPUPreset() {
        return createCPUPreset(ProfilingSettings.PROFILE_CPU_SAMPLING);
    }

    public static ProfilingSettings createCPUPreset(int type) {
        return new CPUPreset(type);
    }

    public static ProfilingSettings createMemoryPreset() {
        return createMemoryPreset(ProfilingSettings.PROFILE_MEMORY_SAMPLING);
    }

    public static ProfilingSettings createMemoryPreset(int type) {
        return new MemoryPreset(type);
    }

    public static ProfilingSettings createMonitorPreset() {
        return new MonitorPreset();
    }
}
