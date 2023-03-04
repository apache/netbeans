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

package org.netbeans.modules.web.common.remote;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.netbeans.modules.web.common.spi.RemoteFileCacheImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

public class RemoteFilesCache {

    private static final RemoteFilesCache DEFAULT = new RemoteFilesCache();
    private static final String REMOTE_URL = "remote.url"; //NOI18N
    
    private RequestProcessor RP2 = new RequestProcessor(RemoteFilesCache.class.getName(), 5);
    
    public static RemoteFilesCache getDefault() {
        return DEFAULT;
    }
    
    private RemoteFilesCache() {
    }
    
//    public boolean isInCache(URL url) {
//        File f = getCachedFileName(url);
//        return f.exists();
//    }
    
    public URL isRemoteFile(FileObject fo){
        String remoteUrl = (String) fo.getAttribute(REMOTE_URL);
        if (remoteUrl == null) {
            return null;
        }
        try {
            return new URL(remoteUrl);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public FileObject getRemoteFile(final URL url) throws IOException {
        return getRemoteFile(url, true);
    }
    
    FileObject getRemoteFile(final URL url, boolean asynchronous) throws IOException {
        final File f = getCachedFileName(url);
        if (!f.exists()) {
            f.createNewFile();
            if (asynchronous) {
                RP2.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fetchRemoteFile(f, url);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else {
                fetchRemoteFile(f, url);
            }
        }
        FileObject fo = FileUtil.toFileObject(f);
        fo.setAttribute(REMOTE_URL, url.toExternalForm());
        return fo;
    }
    
    static File getCachedFileName(URL url){
        String s = url.toExternalForm();
        if (s.lastIndexOf('.') != -1) {
            s = s.substring(s.lastIndexOf('.'));
        } else {
            s = ""; //NOI18N
        }
        String fileName = getMD5(url.toExternalForm())+s;
        File f = new File(getCacheRoot(), fileName);
        return f;
    }
    
    private void fetchRemoteFile(File destination, URL url) throws IOException {
        InputStream is = null;
        try {
            is = url.openStream();
        } catch (FileNotFoundException ex) {
            is = new ByteArrayInputStream(("file not found at "+url.toExternalForm()+" \n"+ex.toString()).getBytes()); //NOI18N
        } catch (Throwable ex) {
            is = new ByteArrayInputStream(("could not open stream for "+url.toExternalForm()+" \n"+ex.toString()).getBytes()); //NOI18N
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(destination);
            FileUtil.copy(is, os);
        } finally {
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
        FileObject fo = FileUtil.toFileObject(destination);
        fo.refresh();
    }

    private static File getCacheRoot() {
        return Places.getCacheSubdirectory("remotefiles"); //NOI18N
    }
    
    private static String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5"); // NOI18N
        } catch (NoSuchAlgorithmException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex; // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }

    @ServiceProvider(service=RemoteFileCacheImplementation.class)
    public static class RemoteFileCacheImpl implements RemoteFileCacheImplementation {

        @Override
        public FileObject getRemoteFile(URL url) throws IOException {
            return RemoteFS.getDefault().getFileForURL(url);
        }

        @Override
        public URL isRemoteFile(FileObject fo) {
            return getDefault().isRemoteFile(fo);
        }

    }
}
