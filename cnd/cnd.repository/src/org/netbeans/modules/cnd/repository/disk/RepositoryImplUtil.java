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
package org.netbeans.modules.cnd.repository.disk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public class RepositoryImplUtil {
    
    private static final int DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM = 0;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static int version = DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM;

    public static int getVersion() {
        return version;
    }

    public static void setVersion(int version) {
        RepositoryImplUtil.version = version;
    }

    public static void assertWritable(boolean writable, Object messageApx) {
        if (!writable && CndUtils.isDebugMode()) {
            CndUtils.severe(new Exception("Trying to perform write operatin in read-only object " + messageApx)); // NOI18N
        }
    }

    public static void warnNotWritable(File file) {
        CndUtils.getLogger().log(Level.WARNING, "Can not write to {0}", file.getAbsolutePath());
    }

    private static String reduceString(String name) {
        if (name.length() > 128) {
            int hashCode = name.hashCode();
            name = name.substring(0, 64) + "--" + name.substring(name.length() - 32); // NOI18N
            name += hashCode;
        }
        return name;
    }

    private final static char SEPARATOR_CHAR = '-';

    public static String getKeyFileName(Key key) throws IOException {
        assert key != null;
        int size = key.getDepth();

        StringBuilder nameBuffer = new StringBuilder(""); // NOI18N

        for (int j = 0; j < key.getSecondaryDepth(); ++j) {
            nameBuffer.append(key.getSecondaryAt(j)).append(SEPARATOR_CHAR);
        }

        if (size != 0) {
            for (int i = 0; i < size; ++i) {
                nameBuffer.append(key.getAt(i)).append(SEPARATOR_CHAR);
            }
        }

        String fileName = nameBuffer.toString();
        fileName = URLEncoder.encode(fileName, Stats.ENCODING);
        return reduceString(fileName);
    }

    public static void deleteDirectory(File path, List<String> doNotDeleteFilesList, boolean deleteDirItself) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    //check if we should not delete, only for the top level files?
                    if (doNotDeleteFilesList != null && doNotDeleteFilesList.contains(files[i].getName())) {
                        //do not delete file
                        continue;
                    }
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i], true);
                    } else {
                        if (!files[i].delete()) {
                            if (!CndUtils.isUnitTestMode() || Stats.TRACE_IZ_224249) {
                                System.err.println("Cannot delete repository file " + files[i].getAbsolutePath());
                                if (Stats.TRACE_IZ_224249) {
                                    CndUtils.threadsDump();
                                }
                            }
                        }
                    }
                }
            }
            //do not delete folder if there are some files which should not be deleted
            if ((doNotDeleteFilesList == null || doNotDeleteFilesList.isEmpty()) && deleteDirItself) {
                if (!path.delete()) {
                    System.err.println("Cannot delete repository folder " + path.getAbsolutePath());
                }
            }
        }
    }

    public static void deleteDirectory(File path, boolean deleteDirItself) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i], true);
                    } else {
                        if (!files[i].delete()) {
                            if (!CndUtils.isUnitTestMode() || Stats.TRACE_IZ_224249) {
                                System.err.println("Cannot delete repository file " + files[i].getAbsolutePath());
                                if (Stats.TRACE_IZ_224249) {
                                    CndUtils.threadsDump();
                                }
                            }
                        }
                    }
                }
            }
            if (deleteDirItself) {
                if (!path.delete()) {
                    System.err.println("Cannot delete repository folder " + path.getAbsolutePath());
                }
            }
        }
    }

    public static DataOutputStream getBufferedDataOutputStream(File file) throws FileNotFoundException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE));
    }

    public static DataInputStream getBufferedDataInputStream(File file) throws FileNotFoundException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE));
    }
}
