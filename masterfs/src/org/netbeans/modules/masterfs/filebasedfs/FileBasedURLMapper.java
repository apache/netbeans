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

package org.netbeans.modules.masterfs.filebasedfs;

import java.net.URISyntaxException;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObj;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

public final class FileBasedURLMapper extends URLMapper {
    private static final Logger LOG = Logger.getLogger(FileBasedURLMapper.class.getName());
    
    @Override
    public final URL getURL(final FileObject fo, final int type) {
        if (type == URLMapper.NETWORK) {
            return null;
        }
        URL retVal = null;
        try {
            if (fo instanceof BaseFileObj)  {
                final BaseFileObj bfo = (BaseFileObj) fo;
                retVal = FileBasedURLMapper.fileToURL(bfo.getFileName().getFile(), fo);
            } else if (fo instanceof RootObj<?>) {
                final RootObj<?> rfo = (RootObj<?>) fo;
                return getURL(rfo.getRealRoot(), type);                
            }
        } catch (MalformedURLException e) {
            retVal = null;
        }
        return retVal;
    }

    public final FileObject[] getFileObjects(final URL url) {
        if (!"file".equals(url.getProtocol())) {  //NOI18N
            return null;
        }
        // return null for UNC root
        if(url.getPath().equals("//") || url.getPath().equals("////")) {  //NOI18N
            return null;
        }
        //TODO: review and simplify         
        FileObject retVal = null;
        File file;
        try {
            file = FileUtil.normalizeFile(BaseUtilities.toFile(url.toURI()));
        } catch (URISyntaxException e) {
            LOG.log(Level.INFO, "URL=" + url, e); // NOI18N
            return null;
        } catch (IllegalArgumentException iax) {
            LOG.log(Level.INFO, "URL=" + url, iax); // NOI18N
            return null;
        }
        
        retVal = FileBasedFileSystem.getFileObject(file, FileObjectFactory.Caller.ToFileObject);
        return new FileObject[]{retVal};
    }

    private static URL fileToURL(final File file, final FileObject fo) throws MalformedURLException {
        URL retVal = toURI(file, fo.isFolder()).toURL();
        if (fo.isFolder()) {
            // #155742 - URL for folder must always end with slash
            final String urlDef = retVal.toExternalForm();
            final String pathSeparator = "/";//NOI18N
            if (!urlDef.endsWith(pathSeparator)) {
                retVal = new URL(urlDef + pathSeparator);
            }
        }
        return retVal;
    }
    /** {@link BaseUtilities#toURI} replacement.
     * #171330: we know whether given
     * FileObject is a file or folder, so we can eliminate {@link File#isDirectory}
     * disk touch which is needed otherwise.
     * Might be useful as an API method.
     */
    private static URI toURI(final File file, boolean isDirectory) {
        return toURI(file.getAbsolutePath(), isDirectory, File.separatorChar);
    }
    
    static URI toURI(String path, boolean isDirectory, char separator) {
        try {
            String sp = slashify(path, isDirectory, separator);
            return new URI("file", null, sp, null);  //NOI18N
        } catch (URISyntaxException x) {
            throw new Error(x);		// Can't happen
        }
    }

    private static String slashify(String p, boolean isDirectory, char separatorChar) {
        if (separatorChar != '/') {  //NOI18N
            p = p.replace(separatorChar, '/');  //NOI18N
        }
        if (!p.startsWith("/")) {  //NOI18N
            p = "/" + p;  //NOI18N
        }
        if (!p.endsWith("/") && isDirectory) {  //NOI18N
            p = p + "/";  //NOI18N
        }
        return p;
    }
}
