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

package org.netbeans.modules.java.source.parsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Enumerations;

/**
 *
 * @author tom
 */
public class FileObjectArchive implements Archive {
    private static final Logger LOG = Logger.getLogger(FileObjectArchive.class.getName());
    
    private final FileObject root;
    private volatile Boolean multiRelease;
    
    /** Creates a new instance of FileObjectArchive */
    public FileObjectArchive (final FileObject root) {
        this.root = root;
    }

    @NonNull
    @Override
    public Iterable<JavaFileObject> getFiles(
            @NonNull final String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed final Set<JavaFileObject.Kind> kinds,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean recursive) throws IOException {
        final FileObject folder = root.getFileObject(folderName);
        if (folder == null || !(entry == null || entry.includes(folder))) {
            return Collections.<JavaFileObject>emptySet();
        }
        final Enumeration<? extends FileObject> children;
        final List<JavaFileObject> result;
        if (recursive) {
            children = Enumerations.filter(
                    folder.getChildren(recursive),
                    (p,x)->{
                        return  !p.isFolder() && isInJavaPackage(folder,p) ?
                                p :
                                null;
                    });
            result = new ArrayList<>(/*unknown size*/);
        } else {
            final FileObject[] chlds = folder.getChildren();
            children = Enumerations.array(chlds);
            result = new ArrayList<>(chlds.length);
        }
        while (children.hasMoreElements()) {
            final FileObject fo = children.nextElement();
            if (fo.isData() && (entry == null || entry.includes(fo))) {
                final Kind kind = FileObjects.getKind(fo.getExt());
                if (kinds == null || kinds.contains (kind)) {
                    JavaFileObject file;
                    if (kind == Kind.CLASS) {
                        file = FileObjects.fileObjectFileObject(fo, root, filter, null);
                    } else {
                        file = FileObjects.sourceFileObject(fo, root, filter,false);
                    }
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    public JavaFileObject create (final String relativePath, final JavaFileFilterImplementation filter) {
        throw new UnsupportedOperationException("Write not supported");   //NOI18N
    }

    @Override
    public void clear() {
        multiRelease = null;
    }

    @Override
    public JavaFileObject getFile(String name) throws IOException {
        final FileObject file = root.getFileObject(name);
        return file == null ? null : FileObjects.sourceFileObject(file, root, null, false);
    }

    @Override
    public boolean isMultiRelease() {
        Boolean res = multiRelease;
        if (res == null) {
            res = Boolean.FALSE;
            try {
                if (root.getFileSystem() instanceof JarFileSystem) {
                    final FileObject manifest = root.getFileObject("META-INF/MANIFEST.MF"); //NOI18N
                    if (manifest != null) {
                        try(InputStream in = new BufferedInputStream(manifest.getInputStream())) {
                            res = FileObjects.isMultiVersionArchive(in);
                        }
                    }
                }
            } catch (IOException ioe) {
                LOG.log(
                        Level.WARNING,
                        "Cannot read: {0} manifest",    //NOI18N
                        FileUtil.getFileDisplayName(root));
            }
            multiRelease = res;
        }
        return res;
    }

    @Override
    public String toString() {
        return String.format(
            "%s[folder: %s]",   //NOI18N
            getClass().getSimpleName(),
            FileUtil.getFileDisplayName(root));
    }

    private static boolean isInJavaPackage(
            @NonNull final FileObject root,
            @NonNull final FileObject file) {
        FileObject fld = file.getParent();
        while (fld != null && !fld.equals(root)) {
            if (!SourceVersion.isIdentifier(fld.getNameExt())) {
                return false;
            }
            fld = fld.getParent();
        }
        return true;
    }

}
