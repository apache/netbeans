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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 */
public class LinuxNativeUtils extends UnixNativeUtils {
    public static final String LIBRARY_PREFIX_LINUX =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "linux/" ; //NOI18N
    
    public static final String LIBRARY_I386 =
            "linux.so"; //NO18N
    public static final String LIBRARY_AMD64 =
            "linux-amd64.so"; //NO18N
    
    private static final String PROC_MOUNTS_FILE = "/proc/mounts";

    public static final String[] POSSIBLE_BROWSER_LOCATIONS_LINUX = new String[]{
        "/usr/bin/firefox",
        "/usr/bin/mozilla-firefox",
        "/usr/local/firefox/firefox",
        "/opt/bin/firefox",
        "/usr/bin/mozilla",
        "/usr/local/mozilla/mozilla",
        "/opt/bin/mozilla"
    };
    
    public static final String[] FORBIDDEN_DELETING_FILES_LINUX = {};
    
    LinuxNativeUtils() {        
        final String arch = System.getProperty("os.arch");
        String library = arch.equals("amd64") ?
                LIBRARY_AMD64 : 
            arch.equals("i386") || arch.equals("x86") ? 
                LIBRARY_I386 : null;
        
        if(library!=null) {
            loadLibrary(LIBRARY_PREFIX_LINUX + library);
        }        
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_LINUX);
    }
    
    @Override
    public File getDefaultApplicationsLocation() {
        File usrlocal = new File("/usr/local");
        
        if (usrlocal.exists() &&
                usrlocal.isDirectory() &&
                FileUtils.canWrite(usrlocal)) {
            return usrlocal;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }
    
    @Override
    public List<File> getFileSystemRoots(String... files) throws IOException {
        List <File> roots = super.getFileSystemRoots();
        final File mounts = new File(PROC_MOUNTS_FILE);
        try {
            if(FileUtils.exists(mounts) && FileUtils.canRead(mounts)) {
                List <String> strings = FileUtils.readStringList(mounts);
                for(int i=0;i<strings.size();i++ ) {
                    String line = strings.get(i).trim();
                    final int firstSpaceIndex = line.indexOf(StringUtils.SPACE);
                    if(firstSpaceIndex!=-1) {
                        String mountPoint = line.substring(firstSpaceIndex + 1).trim();
                        final int nextSpaceIndex = mountPoint.indexOf(StringUtils.SPACE);
                        if(nextSpaceIndex!=-1) {
                            mountPoint = mountPoint.substring(0, nextSpaceIndex);
                            if(mountPoint.startsWith(File.separator)) {
                                final File mountFile = new File(mountPoint);
                                if(!roots.contains(mountFile)) {
                                    LogManager.log("... adding mount point from /proc/mounts: " + mountFile);
                                    roots.add(mountFile);
                                }
                            }
                        }
                        
                    }
                }
            }
        } catch (IOException e) {
            LogManager.log("... cannot read " + mounts, e);
        }
        return roots;
    }
    
    @Override
    protected Platform getPlatform() {        
        final String osArch = System.getProperty("os.arch");
        if (osArch.contains("ppc")) {
            return SystemUtils.isCurrentJava64Bit() ? 
                Platform.LINUX_PPC64 : 
                Platform.LINUX_PPC;
        } else if (osArch.contains("sparc")) {
            return  Platform.LINUX_SPARC;
        } else if(osArch.equals("ia64")) {
            return Platform.LINUX_IA64;
        } else {
            return SystemUtils.isCurrentJava64Bit() ? 
                Platform.LINUX_X64 : 
                Platform.LINUX_X86;
        }        
    }

    protected String [] getPossibleBrowserLocations() {
        return POSSIBLE_BROWSER_LOCATIONS_LINUX;
    }
}
