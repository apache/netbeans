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

package org.netbeans.modules.remote.impl.fs;

import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class RemoteFileURLConnection extends URLConnection {

    private final ExecutionEnvironment execEnv;
    private final String path;

    private FileObject fileObject;

    /** a stream per connection */
    private InputStream iStream = null;

    /** a stream per connection */
    private OutputStream oStream = null;

    public RemoteFileURLConnection(URL url) throws IOException {
        super(url);
        if (! RemoteFileURLStreamHandler.PROTOCOL.equals(url.getProtocol())) {
            throw new IllegalArgumentException("Illegal url protocol: " + url.getProtocol()); //NOI18N
        }
        String user = PathUtilities.unescapePath(url.getUserInfo());
        String host = PathUtilities.unescapePath(url.getHost());
        execEnv = ExecutionEnvironmentFactory.createNew(user, host, url.getPort());
        path = PathUtilities.unescapePath(url.getFile());
    }

    @Override
    public void connect() throws IOException {
        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        fileObject = fs.findResource(path);
        if (fileObject == null || !fileObject.isValid()) {
            throw newFileNotFoundException();
        }
    }

    private FileNotFoundException newFileNotFoundException() {
        return RemoteExceptions.createFileNotFoundException(RemoteFileObjectBase.getDisplayName(execEnv, path));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (fileObject == null) {
            connect();
        }
        if (fileObject == null) {
            throw newFileNotFoundException();
        }
        if (iStream == null) {
            if (fileObject.isData()) {
                iStream = fileObject.getInputStream();
            } else {
                iStream = new FIS(fileObject);
            }
        }
        return iStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (fileObject == null) {
            throw newFileNotFoundException();
        }
        if (oStream == null) {
            FileLock flock = fileObject.lock();
            oStream = new LockOS(fileObject.getOutputStream(flock), flock);
        }
        return oStream;
    }

    @Override
    public int getContentLength() {
        try {
            connect();
        } catch (IOException ex) {
            return 0;
        }
        return (int) fileObject.getSize();
    }

    @Override
    public Permission getPermission() throws IOException {
        connect();
        StringBuilder actions = new StringBuilder();
        if (fileObject != null) {
            if (fileObject.isValid()) {
                if (fileObject.canRead()) {
                    //SecurityConstants.FILE_READ_ACTION is deprecated
                    // "read" is documented in FilePermission javadoc
                    actions.append("read").append(' '); //NOI18N
                }
                if (fileObject.canWrite()) {
                    //SecurityConstants.FILE_WRITE_ACTION deprecated,
                    // "write" is documented in FilePermission javadoc
                    actions.append("write").append(' '); //NOI18N
                }
            }
        }
        return new FilePermission(path, actions.toString());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + url;
    }

    private static class LockOS extends java.io.BufferedOutputStream {

        private final FileLock flock;

        public LockOS(OutputStream os, FileLock lock) throws IOException {
            super(os);
            flock = lock;
        }

        @Override
        public void close() throws IOException {
            flock.releaseLock();
            super.close();
        }
    }
    
    /**
     * The class allows reading of folder via URL. Because of html oriented user
     * interface the document has html format. Taken from
     * org.openide.filesystems.FileURL
     *
     * @version 0.10 May 15, 1998
     */
    private static final class FIS extends InputStream {

        /**
         * delegated reader that reads the document
         */
        private final StringReader reader;

        /**
         * @param folder is a folder
         */
        public FIS(FileObject folder) throws IOException {
            reader = new StringReader(createDocument(folder));
        }

        /**
         * creates html document as string
         */
        private String createDocument(FileObject folder)
                throws IOException {
            StringBuilder buff = new StringBuilder(150);
            StringBuilder lit = new StringBuilder(15);
            FileObject[] fobia = folder.getChildren();
            String name;

            buff.append("<HTML>\n"); // NOI18N
            buff.append("<BODY>\n"); // NOI18N

            FileObject parent = folder.getParent();

            if (parent != null) {
                // lit.setLength(0);
                // lit.append('/').append(parent.getPackageName('/'));
                buff.append("<P>"); // NOI18N
                buff.append("<A HREF=").append("..").append(">").append("..").append("</A>").append("\n"); // NOI18N
                buff.append("</P>"); // NOI18N
            }

            for (int i = 0; i < fobia.length; i++) {
                lit.setLength(0);
                lit.append(fobia[i].getNameExt());
                name = lit.toString();

                if (fobia[i].isFolder()) {
                    lit.append('/'); // NOI18N
                }

                buff.append("<P>"); // NOI18N
                buff.append("<A HREF=").append((Object) lit).append(">").append(name).append("</A>").append("\n"); // NOI18N
                buff.append("</P>"); // NOI18N
            }

            buff.append("</BODY>\n"); // NOI18N
            buff.append("</HTML>\n"); // NOI18N

            return buff.toString();
        }

        //************************************** stream methods **********
        @Override
        public int read() throws IOException {
            return reader.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            char[] ch = new char[len];
            int r = reader.read(ch, 0, len);

            for (int i = 0; i < r; i++) {
                b[off + i] = (byte) ch[i];
            }

            return r;
        }

        @Override
        public long skip(long skip) throws IOException {
            return reader.skip(skip);
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        @Override
        public void reset() throws IOException {
            reader.reset();
        }

        @Override
        public boolean markSupported() {
            return false;
        }
    }
}
