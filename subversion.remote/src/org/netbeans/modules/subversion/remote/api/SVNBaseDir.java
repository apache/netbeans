/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
