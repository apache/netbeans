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
package org.netbeans.modules.localhistory.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * Local History specific utilities.
 *
 * @author Tomas Stupka
 */
public class Utils {
    
    public static void revert(final Node[] nodes) {
        for(Node node : nodes) {
            StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
            if(se != null) {
                Utils.revert(se);
            }
        }
    }
    
    public static void revert(StoreEntry se) {
        InputStream is = null;
        OutputStream os = null;
        try {
            VCSFileProxy file = se.getFile();
            FileObject fo = file.toFileObject();
            if(se.getStoreFile() != null) { // XXX change this semantic to isDeleted() or something similar
                if(fo == null) {
                    fo = FileUtil.createData(file.getParentFile().toFileObject(), file.getName());
                }
                os = getOutputStream(fo);
                is = se.getStoreFileInputStream();
                FileUtil.copy(is, os);
            } else {
                fo.delete();
            }
        } catch (Exception e) {
            LocalHistory.LOG.log(Level.SEVERE, null, e);
        } finally {
            try {
                if(os != null) { os.close(); }
                if(is != null) { is.close(); }
            } catch (IOException e) {}
        }
    }
    
    private static OutputStream getOutputStream(FileObject fo) throws FileAlreadyLockedException, IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return fo.getOutputStream();
            } catch (IOException ioe) {
                retry++;
                if (retry > 7) {
                    throw ioe;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

}
