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
package org.netbeans.modules.subversion.remote.api;

import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class SVNBaseDir {

    private SVNBaseDir() {
    }

    public static VCSFileProxy getBaseDir(VCSFileProxy[] files) {
        VCSFileProxy rootDir = getRootDir(files);
        return rootDir;
        //VCSFileProxy baseDir = getCommonPart(rootDir, new File("."));
        //return baseDir;
    }

    public static VCSFileProxy getRootDir(VCSFileProxy[] files) {
        if (files == null || files.length == 0) {
            return null;
        }

        VCSFileProxy commonPart = files[0];
        for (int i = 0; i < files.length; i++) {
            commonPart = getCommonPart(commonPart, files[i]);
            if (commonPart == null) {
                return null;
            }
        }

        if (commonPart.isFile()) {
            return commonPart.getParentFile();
        } else {
            return commonPart;
        }
    }

    private static VCSFileProxy getCommonPart(VCSFileProxy file1, VCSFileProxy file2) {
        if (file1 == null) {
            return null;
        }
        if (file2 == null) {
            return null;
        }
        String file1AbsPath = file1.getPath();
        String file2AbsPath = file2.getPath();
        if (file1AbsPath.equals(file2AbsPath)) {
            return file1;
        }
        String file1Parts[] = file1AbsPath.split("/"); //NOI18N
        String file2Parts[] = file2AbsPath.split("/"); //NOI18N
        if (file1Parts[0].equals("")) { //NOI18N
            file1Parts[0] = "/"; //NOI18N
        }
        if (file2Parts[0].equals("")) { //NOI18N
            file2Parts[0] = "/"; //NOI18N
        }
        int parts1Length = file1Parts.length;
        int parts2Length = file2Parts.length;
        int minLength = parts1Length >= parts2Length ? parts2Length : parts1Length;
        StringBuilder commonsPart = new StringBuilder();
        for (int i = 0; i < minLength; i++) {
            String part1 = file1Parts[i];
            String part2 = file2Parts[i];
            if (!part1.equals(part2)) {
                break;
            }
            if (i > 0) {
                commonsPart.append("/"); //NOI18N
            }
            commonsPart.append(part1);
        }

        if (commonsPart.length() == 0) {
            return null;
        } else {
            return VCSFileProxySupport.getResource(file1, commonsPart.toString());
        }
    }
}
