/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            dirs = tmp.toArray(new String[tmp.size()]);
        }
        return dirs;
    }
    
    static synchronized void assignDirs(String... arr) {
        dirs = arr;
    }
}
