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
package org.netbeans.core.startup.preferences;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class RelPaths {
    private static String[] dirs;

    private RelPaths() {
    }

    /** Splits the path to relative (with respect to some cluster) and
     * the cluster identification.
     * 
     * @param path the absolute path
     * @param array of two elements [0] - identification of the cluster
     *    [1] - relative path
     */
    public static String[] findRelativePath(String path) {
        if (path.isEmpty()) {
            return null;
        }
        String[] ret = {null, null};
        testWritePath(path, System.getProperty("netbeans.user"), "user", ret); // NOI18N
        int cnt = 0;
        for (String p : dirs()) {
            testWritePath(path, p, "" + cnt, ret);
            cnt++;
        }
        testWritePath(path, System.getProperty("netbeans.home"), "home", ret); // NOI18N
        if (ret[1] == null) {
            ret[0] = "abs"; // NOI18N
            ret[1] = path;
        }
        return ret;
    }
    
    /** Reads relative path from a buffer.
     */
    public static String readRelativePath(final ByteBuffer bb) throws IOException {
        class IS extends InputStream {

            @Override
            public int read() throws IOException {
                return bb.position() < bb.limit() ? bb.get() : -1;
            }
        }
        final DataInputStream dis = new DataInputStream(new IS());
        return readRelativePath(dis);
    }
    
    /** Finds the path for n-th cluster.
     * @param cluster the number of cluster to find path for
     * @return full path of the cluster
     */
    public static String cluster(int cluster) {
        return dirs()[cluster];
    }

    /** Reads relative path from stream.
     */
    public static String readRelativePath(DataInputStream dis) throws IOException {
        String index = dis.readUTF();
        if (index.isEmpty()) {
            return index;
        }
        String relative = dis.readUTF();
        if ("user".equals(index)) { // NOI18N
            return System.getProperty("netbeans.user").concat(relative); // NOI18N
        }
        if ("home".equals(index)) { // NOI18N
            return System.getProperty("netbeans.home").concat(relative); // NOI18N
        }
        if ("abs".equals(index)) { // NOI18N
            return relative;
        }
        int indx = 0;
        try {
            indx = Integer.parseInt(index);
        } catch (NumberFormatException nfe) {
            throw new IOException(nfe);
        }
        return dirs()[indx].concat(relative); // NOI18N
    }
    
    
    private static boolean testWritePath(String path, String prefix, String codeName, String[] ret) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        if (path.startsWith(prefix)) {
            String relPath = path.substring(prefix.length());
            while (relPath.startsWith("/")) {
                relPath = relPath.substring(1);
            }
            if (
                ret[1] == null || 
                ret[1].length() > relPath.length()
            ) {
                ret[0] = codeName;
                ret[1] = relPath;
            }
            return true;
        }
        return false;
    }

    private static synchronized String[] dirs() {
        if (dirs == null) {
            List<String> tmp = new ArrayList<String>();
            String nbdirs = System.getProperty("netbeans.dirs"); // NOI18N
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
    
    static synchronized void assignDirs(String... arr) {
        dirs = arr;
    }
}
