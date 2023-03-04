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

package org.netbeans.installer.utils.system;

import java.io.IOException;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
/**
 *
 * @author Kirill Sorokin
 */
public class SolarisNativeUtils extends UnixNativeUtils {
    
    public static final String LIBRARY_PATH_SOLARIS_SPARCV9 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-sparc/" + //NOI18N
            "solaris-sparcv9.so"; //NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X86 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-x86.so"; // NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X64 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-amd64.so"; // NOI18N

    public static final String[] POSSIBLE_BROWSER_LOCATIONS_SOLARIS = new String[]{
        "/usr/sfw/lib/firefox/firefox",
        "/opt/csw/bin/firefox",
        "/usr/sfw/lib/mozilla/mozilla",
        "/opt/csw/bin/mozilla",
        "/usr/dt/bin/sun_netscape",
        "/usr/bin/firefox",
        "/usr/bin/mozilla-firefox",
        "/usr/local/firefox/firefox",
        "/opt/bin/firefox",
        "/usr/bin/mozilla",
        "/usr/local/mozilla/mozilla",
        "/opt/bin/mozilla"
    };
    
    private static final String[] FORBIDDEN_DELETING_FILES_SOLARIS = {};
    
    SolarisNativeUtils() {
        String library = null;
        
        if (System.getProperty("os.arch").contains("sparc")) {
            library = LIBRARY_PATH_SOLARIS_SPARCV9;
        } else {
            library = SystemUtils.isCurrentJava64Bit() ? 
                LIBRARY_PATH_SOLARIS_X64 : 
                LIBRARY_PATH_SOLARIS_X86;
        }
        
        loadLibrary(library);
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_SOLARIS);
    }
    @Override
    protected Platform getPlatform() {        
        final String osArch = System.getProperty("os.arch");
        return osArch.contains("sparc") ? 
            Platform.SOLARIS_SPARC : 
            Platform.SOLARIS_X86;
    }
    @Override
    protected String [] getPossibleBrowserLocations() {
        return POSSIBLE_BROWSER_LOCATIONS_SOLARIS;
    }
}
