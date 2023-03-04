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
package org.netbeans.modules.websvc.rest.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Ayub Khan
 */
public class ZipUtil {
    
    private static final int READ_BUF_SIZE = 65536;
    private static final int WRITE_BUF_SIZE = 65536;
    
    private List<UnZipFilter> filters = new ArrayList<UnZipFilter>();
    
    public ZipUtil() {
    }
    
    public void addFilter(UnZipFilter filter) {
        filters.add(filter);
    }
    
    private boolean allow(ZipEntry entry) {
        for(UnZipFilter f:filters) {
            if(!f.allow(entry))
                return false;
        }
        return true;
    }
    
    public void zip(File zipFile, String[] sources, String[] paths) {
        try {

            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            for (int i = 0; i < sources.length; i++) {
                File f = new File(sources[i]);
                addEntry(f, paths[i], out);
            }
            out.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void addEntry(File file, String path, ZipOutputStream out) throws FileNotFoundException, IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File f = new File(file + File.separator + files[i]);
                addEntry(f, path + File.separator + file.getName(), out);
            }
        } else {
            byte[] data = new byte[WRITE_BUF_SIZE];
            BufferedInputStream origin = null;
            //System.out.println("Adding: " + file);
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, READ_BUF_SIZE);
            ZipEntry entry = new ZipEntry(path + File.separator + file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, WRITE_BUF_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
    }

    public boolean unzip(final InputStream source,
            final FileObject targetFolderFO, boolean overwrite) throws IOException {
        boolean result = true;
        FileSystem targetFS = targetFolderFO.getFileSystem();
        File targetFolder = FileUtil.toFile(targetFolderFO);
        ZipInputStream zip = null;
        try {
            final byte [] buffer = new byte [WRITE_BUF_SIZE];
            zip = new ZipInputStream(new BufferedInputStream(source, READ_BUF_SIZE));
            final InputStream in = zip;
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                if(!allow(entry)) {
                    continue;
                }
                final File entryFile = new File(targetFolder, entry.getName());
                if(entry.isDirectory()) {
                    if(!entryFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(entryFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                } else {
                    if(entryFile.exists() && overwrite) {
                        if (!entryFile.delete()) {
                            throw new RuntimeException("Failed to delete file: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(parentFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                parentFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    targetFS.runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileOutputStream os = null;
                            try {
                                os = new FileOutputStream(entryFile);
                                int len;
                                while((len = in.read(buffer)) >= 0) {
                                    os.write(buffer, 0, len);
                                }
                            } finally {
                                if(os != null) {
                                    try {
                                        os.close();
                                    } catch(IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        } finally {
            if(zip != null) {
                try {
                    zip.close();
                } catch(IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return result;
    }
    
    public interface UnZipFilter {
        public boolean allow(ZipEntry entry);
    }

}

