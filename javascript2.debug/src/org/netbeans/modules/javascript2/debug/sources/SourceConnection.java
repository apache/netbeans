/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.debug.sources;

import org.netbeans.modules.javascript2.debug.sources.SourceURLMapper;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.Permission;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
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
