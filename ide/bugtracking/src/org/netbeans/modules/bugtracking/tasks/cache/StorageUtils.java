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
package org.netbeans.modules.bugtracking.tasks.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpeska
 */
public class StorageUtils {

    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    static class FileLocks {

        private static FileLocks instance;

        private static synchronized FileLocks getInstance() {
            if (instance == null) {
                instance = new FileLocks();
            }
            return instance;
        }
        private final Map<String, FileLock> locks = new HashMap<String, FileLock>();

        static FileLock getLock(File file) {
            synchronized (getInstance().locks) {
                FileLock fl = getInstance().locks.get(file.getAbsolutePath());
                if (fl == null) {
                    fl = getInstance().new FileLock(file);
                }
                getInstance().locks.put(file.getAbsolutePath(), fl);
                return fl;
            }
        }

        class FileLock {

            private final File file;

            public FileLock(File file) {
                this.file = file;
            }

            void release() {
                synchronized (getInstance().locks) {
                    getInstance().locks.remove(file.getAbsolutePath());
                }
            }
        }
    }
}
