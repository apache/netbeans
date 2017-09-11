/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

        public int read(byte[] b, int off, int len) throws IOException {
            char[] ch = new char[len];
            int r = reader.read(ch, 0, len);

            for (int i = 0; i < r; i++)
                b[off + i] = (byte) ch[i];

            return r;
        }

        public long skip(long skip) throws IOException {
            return reader.skip(skip);
        }

        public void close() throws IOException {
            reader.close();
        }

        public void reset() throws IOException {
            reader.reset();
        }

        public boolean markSupported() {
            return false;
        }
    }
     // end of FIS
}
