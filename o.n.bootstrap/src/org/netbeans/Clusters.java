/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Clusters implements Stamps.Updater {
    private static String[] dirs;
    private static String dirPrefix;
    private static final Clusters INSTANCE = new Clusters();
    
    private Clusters() {
    }
    
    static void scheduleSave(Stamps s) {
        s.scheduleSaveImpl(INSTANCE, "all-clusters.dat", false); // NOI18N
    }
    
    static boolean compareDirs(DataInputStream is) throws IOException {
        int cnt = is.readInt();
        String[] arr = relativeDirsWithHome();
        if (cnt != arr.length) {
            return false;
        }
        for (int i = 0; i < arr.length; i++) {
            String cluster = is.readUTF();
            if (!cluster.equals(arr[i])) {
                return false;
            }
        }
        return true;
    }

    static synchronized String[] dirs() {
        if (dirs == null) {
            List<String> tmp = new ArrayList<String>();
            String nbdirs = System.getProperty("netbeans.dirs");
            if (nbdirs != null) {
                StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
                while (tok.hasMoreTokens()) {
                    tmp.add(tok.nextToken());
                }
            }
            dirs = tmp.toArray(new String[tmp.size()]);
        }
        return dirs;
    }

    static int findCommonPrefix(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        int max = 0;
        for (int i = 0; i < len; i++) {
            final char ch = s1.charAt(i);
            if (ch != s2.charAt(i)) {
                return max;
            }
            if (ch == '/' || ch == File.separatorChar) {
                max = i + 1;
            }
        }
        return len;
    }

    static synchronized String dirPrefix() {
        if (dirPrefix == null) {
            String p = System.getProperty("netbeans.home");
            for (String d : dirs()) {
                if (p == null) {
                    p = d;
                } else {
                    int len = findCommonPrefix(p, d);
                    if (len <= 3) {
                        p = "";
                        break;
                    }
                    p = p.substring(0, len);
                }
            }
            dirPrefix = p == null ? "" : p;
        }
        return dirPrefix;
    }

    static String[] relativeDirsWithHome() {
        String[] arr = dirs();
        String[] tmp = new String[arr.length + 1];
        tmp[0] = System.getProperty("netbeans.home", ""); // NOI18N
        if (tmp[0].length() >= dirPrefix().length()) {
            tmp[0] = tmp[0].substring(dirPrefix().length());
        }
        for (int i = 0; i < arr.length; i++) {
            tmp[i + 1] = arr[i].substring(dirPrefix().length()).replace(File.separatorChar, '/');
        }
        return tmp;
    }

    static synchronized void clear() {
        dirs = null;
        dirPrefix = null;
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        String[] arr = relativeDirsWithHome();
        os.writeInt(arr.length);
        for (int i = 0; i < arr.length; i++) {
            os.writeUTF(arr[i]);
        }
    }

    @Override
    public void cacheReady() {
    }
    
}
