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

package org.openide.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownServiceException;
import java.security.Permission;
import org.openide.util.URLStreamHandlerRegistration;

/** Special URL connection directly accessing an internal file object.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach
*/
class FileURL extends URLConnection {
    /** Protocol name for this type of URL. */
    public static final String PROTOCOL = "nbfs"; // NOI18N

    /** Default implemenatation of handler for this type of URL.
     */
    @URLStreamHandlerRegistration(protocol=PROTOCOL)
    public static class Handler extends URLStreamHandler {
        public URLConnection openConnection(URL u) throws IOException {
            return new FileURL(u);
        }
        protected @Override synchronized InetAddress getHostAddress(URL u) {
            return null;
        }
    }

    /** 1 URLConnection == 1 InputSteam*/
    InputStream iStream = null;

    /** 1 URLConnection == 1 OutputSteam*/
    OutputStream oStream = null;

    /** FileObject that we want to connect to. */
    protected FileObject fo;

    /**
    * Create a new connection to a {@link FileObject}.
    * @param u URL of the connection. Please use {@link #encodeFileObject(FileObject)} to create the URL.
    */
    protected FileURL(URL u) {
        super(u);
    }

    /** Provides a URL to access a file object.
    * @param fo the file object
    * @return a URL using the correct syntax and {@link #PROTOCOL protocol}
    */
    public static URL encodeFileObject(FileObject fo) {
        return NbfsUtil.getURL(fo);
    }

    /** Retrieves the file object specified by an internal URL.
    * @param u the url to decode
    * @return the file object that is represented by the URL, or <code>null</code> if the URL is somehow invalid or the file does not exist
    */
    public static FileObject decodeURL(URL u) {
        return NbfsUtil.getFileObject(u);
    }

    /* A method for connecting to a FileObject.
    */
    public void connect() throws IOException {
        if (fo != null) {
            return;
        }

        fo = decodeURL(url);

        if (fo == null) {
            throw new FileNotFoundException("Cannot find: " + url); // NOI18N
        }
    }

    /*
    * @return InputStream or given FileObject.
    */
    @Override
    public InputStream getInputStream() throws IOException, UnknownServiceException {
        connect();

        if (iStream == null) {
            try {
                if (fo.isFolder()) {
                    iStream = new FIS(fo);
                } else {
                    iStream = fo.getInputStream();
                }
            } catch (FileNotFoundException e) {
                ExternalUtil.exception(e);
                throw e;
            }
        }

        return iStream;
    }

    /*
    * @return OutputStream for given FileObject.
    */
    @Override
    public OutputStream getOutputStream() throws IOException, UnknownServiceException {
        connect();

        if (fo.isFolder()) {
            throw new UnknownServiceException();
        }

        if (oStream == null) {
            FileLock flock = fo.lock();
            oStream = new LockOS(fo.getOutputStream(flock), flock);
        }

        return oStream;
    }

    /*
    * @return length of FileObject.
    */
    @Override
    public int getContentLength() {
        try {
            connect();

            return (int) fo.getSize();
        } catch (IOException ex) {
            return 0;
        }
    }

    /** Get a header field (currently, content type only).
    * @param name the header name. Only <code>content-type</code> is guaranteed to be present.
    * @return the value (i.e., MIME type)
    */
    @Override
    public String getHeaderField(String name) {
        if (name.equalsIgnoreCase("content-type")) { // NOI18N

            try {
                connect();

                if (fo.isFolder()) {
                    return "text/html"; // NOI18N
                } else {
                    return fo.getMIMEType();
                }
            } catch (IOException e) {
            }
        }

        return super.getHeaderField(name);
    }

    public @Override long getHeaderFieldDate(String name, long Default) {
        if (name.equalsIgnoreCase("last-modified")) { // NOI18N
            try {
                connect();
                return fo.lastModified().getTime();
            } catch (IOException e) {
            }
        }
        return super.getHeaderFieldDate(name, Default);
    }

    // #13038: URLClassPath is going to check this.
    // Better not return AllPermission!
    // SocketPermission on localhost might also work.
    @Override
    public Permission getPermission() throws IOException {
        // Note this is normally called by URLClassPath with an unconnected
        // URLConnection, so the fo will probably be null anyway.
        if (fo != null) {
            File f = FileUtil.toFile(fo);

            if (f != null) {
                return new FilePermission(f.getAbsolutePath(), "read"); // NOI18N
            }

            try {
                FileSystem fs = fo.getFileSystem();

                if (fs instanceof JarFileSystem) {
                    return new FilePermission(((JarFileSystem) fs).getJarFile().getAbsolutePath(), "read"); // NOI18N
                }

                // [PENDING] could do XMLFileSystem too...
            } catch (FileStateInvalidException fsie) {
                // ignore
            }
        }

        // fallback
        return new FilePermission("<<ALL FILES>>", "read"); // NOI18N
    }

    /** Stream that also closes the lock, if closed.
     */
    private static class LockOS extends java.io.BufferedOutputStream {
        /** lock */
        private FileLock flock;

        /**
        * @param os is an OutputStream for writing in
        * @param lock is a lock for the stream
        */
        public LockOS(OutputStream os, FileLock lock) throws IOException {
            super(os);
            flock = lock;
        }

        /** overriden */
        @Override
        public void close() throws IOException {
            flock.releaseLock();
            super.close();
        }
    }

    /** The class allows reading of folder via URL. Because of html
    * oriented user interface the document has html format.
    *
    * @author Ales Novak
    * @version 0.10 May 15, 1998
    */
    private static final class FIS extends InputStream {
        /** delegated reader that reads the document */
        private StringReader reader;

        /**
        * @param folder is a folder
        */
        public FIS(FileObject folder) throws IOException {
            reader = new StringReader(createDocument(folder));
        }

        /** creates html document as string */
        private String createDocument(FileObject folder)
        throws IOException {
            StringBuffer buff = new StringBuffer(150);
            StringBuffer lit = new StringBuffer(15);
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
        public int read() throws IOException {
            return reader.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            char[] ch = new char[len];
            int r = reader.read(ch, 0, len);

            for (int i = 0; i < r; i++)
                b[off + i] = (byte) ch[i];

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
     // end of FIS
}
