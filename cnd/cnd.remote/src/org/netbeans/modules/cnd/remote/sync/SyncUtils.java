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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.DefaultListCellRenderer;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JList;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
import org.openide.filesystems.FileSystem;

/**
 *
 */
final class SyncUtils {
    
    
    /*package*/ static boolean isDoubleRemote(ExecutionEnvironment buildEnvironment, FileSystem sourceFileSystem) {
        ExecutionEnvironment sourceExecutionEnvironment = FileSystemProvider.getExecutionEnvironment(sourceFileSystem);
        if (sourceExecutionEnvironment.isRemote()) {
            return ! buildEnvironment.equals(sourceExecutionEnvironment);
        }
        return false;
    }

  

    public static List<File> toFiles(List<FSPath> paths) {
        List<File> l = new ArrayList<>(paths.size());
        for (FSPath path : paths) {
            if (FileSystemProvider.getExecutionEnvironment(path.getFileSystem()).isLocal()) {
                l.add(new File(path.getPath()));
            }
        }
        return l;
    }
    
    public static File[] toFiles(FSPath[] paths) {
        List<File> l = new ArrayList<>(paths.length);
        for (FSPath path : paths) {
            if (FileSystemProvider.getExecutionEnvironment(path.getFileSystem()).isLocal()) {
                l.add(new File(path.getPath()));
            }
        }
        return l.toArray(new File[l.size()]);
    }

    public static FileSystem getSingleFileSystem(List<FSPath> paths) {
        FileSystem fs = null;
        for (FSPath fsp : paths) {
            if (fs == null) {
                fs = fsp.getFileSystem();
            } else {
                CndUtils.assertTrue(fs == fsp.getFileSystem(),
                        "Different file systems are unsupported: " + fs + ", " + fsp.getFileSystem() ); //NOI18N
                break;
            }
        }
        if (fs == null) {
            CndUtils.getLogger().warning("Defaulting file systems to a local one"); //NOI18N
            fs = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        }
        return fs;
    }

}
