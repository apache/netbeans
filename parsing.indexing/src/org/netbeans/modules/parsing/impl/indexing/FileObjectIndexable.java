/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectIndexable implements IndexableImpl, FileObjectProvider {

    private static final Logger LOG = Logger.getLogger(FileObjectIndexable.class.getName());

    private final FileObject root;
    private final String relativePath;

    private Object url;
    private String mimeType;
    private FileObject file;

    public FileObjectIndexable (FileObject root, FileObject file) {
        this(root, FileUtil.getRelativePath(root, file));
        this.file = file;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "File: {0}", FileUtil.getFileDisplayName(file));  //NOI18N
        }
    }

    public FileObjectIndexable (FileObject root, String relativePath) {
        Parameters.notNull("root", root); //NOI18N
        Parameters.notNull("relativePath", relativePath); //NOI18N
        this.root = root;
        this.relativePath = relativePath;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Root: {0}", FileUtil.getFileDisplayName(root));  //NOI18N
            LOG.log(Level.FINEST, "Path: {0}", relativePath);                       //NOI8N
        }
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public URL getURL() {
        if (url == null) {
            try {
                FileObject f = getFileObject();
                if (f != null) {
                    url = f.toURL();
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(
                            Level.FINEST,
                            "URL from existing FileObject: {0} = {1}",  //NOI18N
                            new Object[] {
                                FileUtil.getFileDisplayName(f),
                                url
                            });
                    }
                } else {
                    url = Util.resolveUrl(root.toURL(), relativePath, false);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(
                            Level.FINEST,
                            "URL from non existing FileObject root: {0} ({1}), relative path: {2} = {3}",  //NOI18N
                            new Object[] {
                                FileUtil.getFileDisplayName(root),
                                root.toURL(),
                                relativePath,
                                url
                            });
                    }
                }
            } catch (MalformedURLException ex) {
                url = ex;
            }
        }

        return url instanceof URL ? (URL) url : null;
    }

    @Override
    public String getMimeType() {
        return mimeType == null ? "content/unknown" : mimeType;
    }

    @Override
    public boolean isTypeOf(String mimeType) {
        Parameters.notNull("mimeType", mimeType); //NOI18N
        if (this.mimeType == null) {
            FileObject f = getFileObject();
            if (f != null) {
                String mt = FileUtil.getMIMEType(f, mimeType);
                if (mt != null && !mt.equals("content/unknown")) {
                    this.mimeType = mt;
                }
            }
        }
        return this.mimeType == null ? false : this.mimeType.equals(mimeType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileObjectIndexable other = (FileObjectIndexable) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        if (this.relativePath != other.relativePath && (this.relativePath == null || !this.relativePath.equals(other.relativePath))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.relativePath != null ? this.relativePath.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FileObjectIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + root.toURL() + "/" + getRelativePath() + "]"; //NOI18N
    }

    @Override
    public FileObject getFileObject() {
        if (file == null) {
            file = root.getFileObject(relativePath);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(
                    Level.FINEST,
                    "File: {0} in Root: {1}", //NOI18N
                    new Object[] {
                        FileUtil.getFileDisplayName(file),
                        FileUtil.getFileDisplayName(root)
                    });
            }
        }
        return file != null && file.isValid() ? file : null;
    }    
}
