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

package org.netbeans.modules.cnd.remote.sync.download;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

class HostUpdatesPersistence {

    private final Properties data;
    private final FileObject dataFile;

    // In version 2.0 value has format
    // (0|1)(p|v)  
    // where 1 means "on" and 0 means "off"
    // and p means "permanent" (i.e. user checked "remember my choice") 
    // and v means, in contrary, "volatile" 
    private static final String VERSION = "2.0"; // NOI18N

    private static final String VERSION_KEY = "____VERSION"; // NOI18N

    public HostUpdatesPersistence(FileObject privProjectStorageDir, ExecutionEnvironment executionEnvironment) throws IOException {
        super();
        data = new Properties();
        String dataFileName = "downloads-" + // NOI18N
                RemoteUtil.hostNameToLocalFileName(executionEnvironment.getHost()) + 
                '-' + RemoteUtil.hostNameToLocalFileName(executionEnvironment.getUser()) + 
                '-' + executionEnvironment.getSSHPort(); // NOI18N
        //NOI18N
        dataFile = FileUtil.createData(privProjectStorageDir, dataFileName);
        try {
            load();
            if (!VERSION.equals(data.get(VERSION_KEY))) {
                data.clear();
            }
        } catch (IOException ex) {
            data.clear();
            Exceptions.printStackTrace(ex);
        }
    }

    private void load() throws IOException {
        if (dataFile.isValid()) {
            InputStream is = dataFile.getInputStream();
            BufferedInputStream bs = new BufferedInputStream(is);
            try {
                data.load(bs);
            } finally {
                bs.close();
            }
        }
    }

    @SuppressWarnings(value = "RV")
    public void store() {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(dataFile.getOutputStream());
            data.setProperty(VERSION_KEY, VERSION);
            data.store(os, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            try {
                dataFile.delete();
            } catch (IOException ex2) {
                System.err.printf("Error deleting file %s%n", dataFile.getPath());
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public boolean getFileSelected(File file, boolean defaultValue) {
        final String key = file.getAbsolutePath();
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return (value instanceof String && ((String) value).startsWith("1")); // NOI18N
        }
    }

    public void setFileSelected(File file, boolean selected, boolean markAsPermanent) {
        final String key = file.getAbsolutePath();
        data.put(key, (selected ? "1" : "0") + (markAsPermanent ? 'p' : 'v')); // NOI18N
    }
    
    public boolean isAnswerPersistent(File file) {
        final String key = file.getAbsolutePath();
        Object value = data.get(key);
        return (value instanceof String && ((String) value).endsWith("p")); // NOI18N
    }
}
