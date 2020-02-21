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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.platform.PlatformGeneric;
import org.netbeans.modules.cnd.makeproject.platform.PlatformLinux;
import org.netbeans.modules.cnd.makeproject.platform.PlatformMacOSX;
import org.netbeans.modules.cnd.makeproject.platform.PlatformNone;
import org.netbeans.modules.cnd.makeproject.platform.PlatformSolarisIntel;
import org.netbeans.modules.cnd.makeproject.platform.PlatformSolarisSparc;
import org.netbeans.modules.cnd.makeproject.platform.PlatformWindows;

public final class Platforms {
    private static final ArrayList<Platform> platforms = new ArrayList<>();

    static {
        platforms.add(new PlatformSolarisSparc());
        platforms.add(new PlatformSolarisIntel());
        platforms.add(new PlatformLinux());
        platforms.add(new PlatformWindows());
        platforms.add(new PlatformMacOSX());
        platforms.add(new PlatformGeneric());
        platforms.add(new PlatformNone());
        platforms.trimToSize();
    }

    public static Platform getPlatform(int id) {
        for (Platform pl : getPlatforms()) {
            if (pl.getId() == id) {
                return pl;
            }
        }
        return null;
    }

    /*
     * Returns platforms names up to but not included Generic.
     */
    public static String[] getPlatformDisplayNames() {
        ArrayList<String> ret = new ArrayList<>();
        for (Platform pl : getPlatforms()) {
            if (pl.getId() == PlatformTypes.PLATFORM_GENERIC || pl.getId() == PlatformTypes.PLATFORM_NONE) {
                continue;
            }
            ret.add(pl.getDisplayName());
        }
        return ret.toArray(new String[ret.size()]);
    }

    private static ArrayList<Platform> getPlatforms() {
        return platforms;
    }

    private Platforms() {
    }
}
