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

package org.netbeans.upgrade.systemoptions;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
class PropertiesStorage  {
    private static final String USERROOT_PREFIX = "/Preferences";//NOI18N
    private static final FileObject SFS_ROOT = FileUtil.getConfigRoot();
    
    private final String folderPath;
    private String filePath;
            
    static PropertiesStorage instance(final String absolutePath) {
        return new PropertiesStorage(absolutePath);
    }
    
    FileObject preferencesRoot() throws IOException {
        return FileUtil.createFolder(SFS_ROOT, USERROOT_PREFIX);
    }
    
    
    /** Creates a new instance */
    private PropertiesStorage(final String absolutePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(USERROOT_PREFIX).append(absolutePath);
        folderPath = sb.toString();
    }
        
    
    public Properties load() throws IOException {
        try {
            Properties retval = new Properties();
            InputStream is = inputStream();
            if (is != null) {
                try {
                    retval.load(is);
                } finally {
                    if (is != null) is.close();
                }
            }
            return retval;
        } finally {
        }
    }
    
    public void save(final Properties properties) throws IOException {
        if (!properties.isEmpty()) {
            try (OutputStream os = outputStream()) {
                properties.store(os,new Date().toString());//NOI18N
            }
        } else {
            FileObject file = toPropertiesFile();
            if (file != null) {
                file.delete();
            }
            FileObject folder = toFolder();
            while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                folder.delete();
                folder = folder.getParent();
            }
        }
    }
    
    private InputStream inputStream() throws IOException {
        FileObject file = toPropertiesFile(false);
        return (file == null) ? null : file.getInputStream();
    }
    
    private OutputStream outputStream() throws IOException {
        FileObject fo = toPropertiesFile(true);
        final FileLock lock = fo.lock();
        final OutputStream os = fo.getOutputStream(lock);
        return new FilterOutputStream(os) {
            @Override
            public void close() throws IOException {
                super.close();
                lock.releaseLock();
            }
        };
    }
    
    private String folderPath() {
        return folderPath;
    }
    
    private String filePath() {
        if (filePath == null) {
            String[] all = folderPath().split("/");//NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < all.length-1; i++) {
                sb.append(all[i]).append("/");//NOI18N
            }
            if (all.length > 0) {
                sb.append(all[all.length-1]).append(".properties");//NOI18N
            } else {
                sb.append("root.properties");//NOI18N
            }
            filePath = sb.toString();
        }
        return filePath;
    }        
    
    protected FileObject toFolder()  {
        return SFS_ROOT.getFileObject(folderPath);
    }
    
    protected  FileObject toPropertiesFile() {
        return SFS_ROOT.getFileObject(filePath());
    }
    
    protected FileObject toFolder(boolean create) throws IOException {
        FileObject retval = toFolder();
        if (retval == null && create) {
            retval = FileUtil.createFolder(SFS_ROOT, folderPath);
        }
        assert (retval == null && !create) || (retval != null && retval.isFolder());
        return retval;
    }
    
    protected FileObject toPropertiesFile(boolean create) throws IOException {
        FileObject retval = toPropertiesFile();
        if (retval == null && create) {
            retval = FileUtil.createData(SFS_ROOT,filePath());//NOI18N
        }
        assert (retval == null && !create) || (retval != null && retval.isData());
        return retval;
    }
}
