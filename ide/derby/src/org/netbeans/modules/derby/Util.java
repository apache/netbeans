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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.derby.ui.DerbyPropertiesPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class Util {
    private static final String DERBY_CLIENT = "derbyclient.jar"; // NOI18N

    private Util() {
    }

    public static boolean hasInstallLocation() {
        return getCheckedLocation() != null;
    }
    
    public static boolean checkInstallLocation() {
        if (!hasInstallLocation()) {
            showInformation(NbBundle.getMessage(Util.class, "MSG_DerbyLocationIncorrect"));
            return false;
        }
        return true;
    }

    private static File getCheckedLocation() {
        File location = new File(DerbyOptions.getDefault().getLocation());
        if (location.isAbsolute() && location.isDirectory() && location.exists()) {
            return location;
        }
        return null;
    }

    public static File getDerbyFile(String relPath) {
        File location = getCheckedLocation();
        if (location != null) {
            return new File(location, relPath);
        }
        return null;
    }
    
    public static boolean ensureSystemHome() {
        if (DerbyOptions.getDefault().getSystemHome().length() <= 0) {
            return Mutex.EVENT.writeAccess(new Mutex.Action<Boolean>() {
                public Boolean run() {
                    return DerbyPropertiesPanel.showDerbyProperties();
                }
            });
        }
        return true;
    }

    public static void showInformation(final String msg){
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }

    public static boolean isDerbyInstallLocation(File location) {
        if (location == null || ! location.exists()) {
            return false;
        }
        File libDir = new File(location, "lib"); // NOI18N
        if (!libDir.exists()) {
            return false;
        }
        File[] libs = libDir.listFiles();
        if (libs == null || libs.length <= 0) {
            return false;
        }
        for (File lib : libs) {
            if (lib.getName().equals(DERBY_CLIENT)) {
                return true;
            }
        }
        return false;
    }

    public static void extractZip(File source, FileObject target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();

                // if directory, create
                if (ze.isDirectory()) {
                    FileUtil.createFolder(target, name);
                    continue;
                }

                // if file, copy
                FileObject fd = FileUtil.createData(target, name);
                FileLock lock = fd.lock();
                try {
                    OutputStream os = fd.getOutputStream(lock);
                    try {
                        FileUtil.copy(zis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            is.close();
        }
    }

    /**
     * Check if candiate FileObject is a derby database.
     * 
     * @param candidate the value of candidate
     * @return true if candidate is a derby database
     */
    public static boolean isDerbyDatabase(FileObject candidate) {
        if (candidate.isFolder()) {
            FileObject sp = candidate.getFileObject("service.properties");
            return sp != null && FileUtil.toFile(sp) != null;
        } else {
            return false;
        }
    }
}
