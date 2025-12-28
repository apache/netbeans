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
            dirs = tmp.toArray(new String[0]);
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
