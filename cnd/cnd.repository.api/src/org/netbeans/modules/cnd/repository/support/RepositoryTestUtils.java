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
package org.netbeans.modules.cnd.repository.support;

import java.io.File;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.modules.Places;

/**
 *
 */
public final class RepositoryTestUtils {

    private RepositoryTestUtils() {
    }

//    public static void debugClear() {
//        LM.debugClear();
//    }
//
    public static void debugDistribution() {
//        Repository.debugDistribution();
    }

    public static void debugDump(Key key) {
//        Repository.debugDump(key);
    }

    public static void deleteDefaultCacheLocation() {
        deleteDirectory(getDefaultCacheLocation());
    }

    private static File getDefaultCacheLocation() {
        return Places.getCacheSubdirectory("cnd/model"); // NOI18N
    }

    private static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        if (!files[i].delete()) {
                            if (!CndUtils.isUnitTestMode()) {
                                System.err.println("Cannot delete repository file " + files[i].getAbsolutePath());
                            }
                        }
                    }
                }
            }
            if (!path.delete()) {
                System.err.println("Cannot delete repository folder " + path.getAbsolutePath());
            }
        }
    }
}
