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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 * @author Tomas Stupka
 */
public class FileUtils {

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils() {
    }
    
    public static String getPath(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if(file != null) {
            // handle as local file
            return file.getAbsolutePath();
        } else {
            try {
                return proxy.toURI().toString();
            } catch (URISyntaxException ex) {
                LocalHistory.LOG.log(Level.WARNING, proxy.getPath(), ex);
                return proxy.getPath();
            }
        }
    }    
    
    public static VCSFileProxy createProxy(String path) {
        try {
            URI uri = new URI(path);
            String scheme = uri.getScheme();
            if (scheme == null) {
                // handle as local file
                return VCSFileProxy.createFileProxy(new File(path));
            } else {
                return VCSFileProxy.createFileProxy(uri);
            }
        } catch (URISyntaxException ex) {
            LocalHistory.LOG.log(Level.FINE, path, ex);
        }
        return VCSFileProxy.createFileProxy(new File(path));
    }    
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     * 
     * @param sourceFile
     * @param targetFile
     * 
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(sourceFile);            
            copy(inputStream, targetFile);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    public static void copy(VCSFileProxy file, OutputStream os) throws IOException {
        if (file == null ) {
            throw new NullPointerException("file must not be null"); // NOI18N
        }        
        if (os == null ) {
            throw new NullPointerException("output stream must not be null"); // NOI18N
        }        
        InputStream is = null;
        try {
            is = createInputStream(file);            
            FileUtil.copy(is, os);
        } finally {
            if (is != null) { try { is.close(); } catch (IOException ex) { } }
            if (os != null) { try { os.close(); } catch (IOException ex) { } }
        }
    }
    
    /**
     * Copies the specified file to the specified outputstream.
     * It <b>closes</b> the output stream.
     * 
     * @param file
     * @param os
     * 
     */    
    public static void copy(File file, OutputStream os) throws IOException {
        if (file == null ) {
            throw new NullPointerException("file must not be null"); // NOI18N
        }        
        if (os == null ) {
            throw new NullPointerException("output stream must not be null"); // NOI18N
        }        
        InputStream is = null;
        try {
            is = createInputStream(file);            
            FileUtil.copy(is, os);
        } finally {
            if (is != null) { try { is.close(); } catch (IOException ex) { } }
            if (os != null) { try { os.close(); } catch (IOException ex) { } }
        }
    }
    
    /**
     * Copies the specified inputStream to the specified targetFile.
     * It <b>closes</b> the input stream.
     * 
     * @param inputStream
     * @param targetFile
     * 
     */
    public static void copy(InputStream inputStream, File targetFile) throws IOException {
        if (inputStream == null || targetFile == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        OutputStream outputStream = null;
        try {            
            outputStream = createOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
            catch (IOException ex) {
                targetFile.delete();
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if(file == null) {
            return;
        }
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            if(files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteRecursively(files[i]);
                }
            }
        }
        file.delete();
    }
    
    /**
     * Do the best to rename the file.
     * @param orig regular file
     * @param dest regular file (if exists it's rewritten)
     */
    public static void renameFile(File orig, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (destExists) {
            for (int i = 0; i<3; i++) {
                if (dest.delete()) {
                    destExists = false;
                    break;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        if (destExists == false) {
            for (int i = 0; i<3; i++) {
                if (orig.renameTo(dest)) {
                    return;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        // requires less permisions than renameTo
        FileUtils.copyFile(orig, dest);

        for (int i = 0; i<3; i++) {
            if (orig.delete()) {
                return;
            }
            try {
                Thread.sleep(71);
            } catch (InterruptedException e) {
            }
        }
        throw new IOException("Can not delete: " + orig.getAbsolutePath());  // NOI18N
    }
    
    /**
     * Normalizes the given file and return a FileObject
     * @param file
     * @return 
     */
    public static FileObject toFileObject(File file) {
        File nfile = FileUtil.normalizeFile(file);
        if(nfile != null) {
            return FileUtil.toFileObject(nfile);
        } else {
            return FileUtil.toFileObject(file);            
        }
    }
    
    private static BufferedInputStream createInputStream(VCSFileProxy fileProxy) throws IOException {        
        int retry = 0;
        while (true) {
            try {
                return new BufferedInputStream(fileProxy.getInputStream(false));
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }
    }

    private static BufferedInputStream createInputStream(File file) throws IOException {
        int retry = 0;
        while (true) {   
            try {
                return new BufferedInputStream(new FileInputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
    private static BufferedOutputStream createOutputStream(File file) throws IOException {
        int retry = 0;
        while (true) {            
            try {
                return new BufferedOutputStream(new FileOutputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
}
