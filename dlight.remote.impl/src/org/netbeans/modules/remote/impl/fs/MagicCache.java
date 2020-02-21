/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class MagicCache {
    static final int BUF_LENGTH = 4000;
    private static final String cacheName = ".rfs_magic"; // NOI18N
    private final RemoteDirectory dir;
    private Map<String, byte[]> cache;

    private static final RequestProcessor RP = new RequestProcessor("ErrorReader"); // NOI18N

    public MagicCache(RemoteDirectory dir) {
        this.dir = dir;
    }
    
    public synchronized byte[] get(String fileName) {
        if (cache == null) {
            cache = new HashMap<>();
            if (!readCache()) {
                cache = null;
                return null;
            }
        }
        
        return cache.get(fileName);
    }
    
    public synchronized void clean(String fileName) {
        File od = new File(dir.getCache(),cacheName);
        if (od.exists()) {
            od.delete();
            if (cache != null) {
                if (fileName == null) {
                    cache.clear();
                } else {
                    cache.remove(fileName);
                }
            }
        }
    }
     
    private boolean readCache() {
        File od = new File(dir.getCache(),cacheName);
        if (!od.exists()) {
            try {
                if (!ConnectionManager.getInstance().isConnectedTo(dir.getExecutionEnvironment())) {
                     return false;
                }
                updateCache();
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                ex.printStackTrace(System.err);
                return false;
            } catch (InterruptedIOException ex) {
                // don't report interruptions
                return false;
            } catch (IOException ex) {                
                ex.printStackTrace(System.err);
                return false;
            }
        }
        if (od.exists()) {
            BufferedReader in = null;
            try {
                in = Files.newBufferedReader(od.toPath(), Charset.forName("UTF-8")); // NOI18N
                String line = null;
                String file = null;
                byte[] res = null;
                int pos = 0;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("./")) { // NOI18N
                        createEntry(file, res, pos);
                        file = line.substring(2);
                        res = new byte[BUF_LENGTH];
                        pos = 0;
                    } else if (line.startsWith("000")){ // NOI18N
                        String[] split = line.split(" +"); // NOI18N
                        if (split.length > 1) {
                            for(int i = 1; i< split.length; i++) {
                                String s = split[i];
                                try {
                                    if (s.length() == 2) {
                                        if (pos < res.length) { // should never be so; but see #239322
                                            int L = Integer.parseInt(s, 16);
                                            res[pos++] = (byte) (L & 0xFF);
                                        }
                                    } else {
                                        long L = Long.parseLong(s, 16);
                                        if (pos < res.length) { // should never be so; but see #239322
                                            res[pos++] = (byte) (L & 0xFF);
                                        }
                                        if (pos < res.length) {
                                            res[pos++] = (byte) (L>>8 & 0xFF);
                                        }
                                        if (pos < res.length) {
                                            res[pos++] = (byte) (L>>16 & 0xFF);
                                        }
                                        if (pos < res.length) {
                                            res[pos++] = (byte) (L>>24 & 0xFF);
                                        }
                                    }
                                } catch (NumberFormatException ex) {
                                    break;
                                }
                            }
                        }
                    }
                }
                createEntry(file, res, pos);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace(System.err);
                return false;
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                return false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return true;
    }
    
    private void createEntry(String file, byte[] res, int pos) {
        if (file != null) {
            if (pos < res.length) {
                byte[] ares = new byte[pos];
                System.arraycopy(res, 0, ares, 0, pos);
                res = ares;
            }
            cache.put(file, res);
        }
    }
    
    private void updateCache() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        // TODO check connection
        String command = "/usr/bin/find . ! -name . -prune -type f -print -exec od -t x1 -N "+BUF_LENGTH+" {} \\;"; // NOI18N
        String path = dir.getPath();
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        File od = new File(dir.getCache(), cacheName);
        OutputStream os = null;
        InputStream is = null;
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("reading MIME", path); // NOI18N
        try {
            os = new FileOutputStream(od);
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(dir.getExecutionEnvironment());
            processBuilder.setExecutable("/bin/sh"); //NOI18N
            processBuilder.setArguments("-c", command); //NOI18N
            processBuilder.setWorkingDirectory(path);
            final Process process = processBuilder.call();
            RP.post(new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader = ProcessUtils.getReader(process.getErrorStream(), true);
                    try {
                        while (reader.readLine() != null) {
                        }
                    } catch (IOException ex) {
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            });
            is = process.getInputStream();
            FileUtil.copy(is, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            RemoteStatistics.stopChannelActivity(activityID, od.length());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
}
