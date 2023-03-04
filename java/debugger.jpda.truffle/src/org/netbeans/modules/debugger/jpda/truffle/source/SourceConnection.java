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

package org.netbeans.modules.debugger.jpda.truffle.source;

import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.Permission;

import org.openide.filesystems.FileObject;

final class SourceConnection extends URLConnection {
    
    /** FileObject that we want to connect to. */
    private FileObject fo;
    /** 1 URLConnection == 1 InputSteam*/
    private InputStream iStream = null;

    
    SourceConnection(URL url) {
        super(url);
    }
    
    public @Override synchronized void connect() throws IOException {
        if (fo == null) {
            fo = SourceURLMapper.find(url);
        }
        if (fo == null) {
            throw new FileNotFoundException(url.toString());
        }
    }
    
    /*
     * @return InputStream of the given FileObject.
     */
    public @Override InputStream getInputStream() throws IOException, UnknownServiceException {
        connect();

        if (iStream == null) {
            if (fo.isFolder()) {
                throw new FileNotFoundException("Can not read from a folder.");
            } else {
                iStream = fo.getInputStream();
            }
        }

        return iStream;
    }

    /*
     * @return length of FileObject.
     */
    public @Override int getContentLength() {
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
    public @Override String getHeaderField(String name) {
        if (name.equalsIgnoreCase("content-type")) { // NOI18N

            try {
                connect();

                if (fo.isData()) {
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

    public @Override Permission getPermission() throws IOException {
        // fallback
        return new FilePermission("<<ALL FILES>>", "read"); // NOI18N
    }

}
