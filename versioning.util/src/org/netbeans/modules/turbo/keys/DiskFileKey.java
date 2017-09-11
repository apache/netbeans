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

package org.netbeans.modules.turbo.keys;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;

import java.io.File;

/**
 * Key for FileObject with identity given by disk files.
 * It means that keys can be equal for non-equal FileObjects.
 *
 * @author Petr Kuzel
 */
public final class DiskFileKey {
    private final FileObject fileObject;
    private final int hashCode;
    private String absolutePath;


    public static DiskFileKey createKey(FileObject fo) {
        return new DiskFileKey(fo);
    }

    private DiskFileKey(FileObject fo) {

        // PERFORMANCE optimalization, it saves memory because elimintes nedd for creating absolute paths.
        // XXX unwrap from MasterFileSystem, hidden dependency on "VCS-Native-FileObject" attribute knowledge
        // Unfortunately MasterFileSystem API does not support generic unwrapping.
        FileObject nativeFileObject = (FileObject) fo.getAttribute("VCS-Native-FileObject");  // NOI18N
        if (nativeFileObject == null) nativeFileObject = fo;


        fileObject = fo;
        hashCode = fo.getNameExt().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof DiskFileKey) {

            DiskFileKey key = (DiskFileKey) o;

            if (hashCode != key.hashCode) return false;
            FileObject fo2 = key.fileObject;
            FileObject fo = fileObject;

            if (fo == fo2) return true;

            try {
                FileSystem fs = fo.getFileSystem();
                FileSystem fs2 = fo2.getFileSystem();
                if (fs.equals(fs2)) {
                    return fo.equals(fo2);
                } else {
                    // fallback use absolute paths (cache them)
                    if (absolutePath == null) {
                        File f = FileUtil.toFile(fo);
                        absolutePath = f.getAbsolutePath();
                    }
                    if (key.absolutePath == null) {
                        File f2 = FileUtil.toFile(fo2);
                        key.absolutePath = f2.getAbsolutePath();
                    }
                    return absolutePath.equals(key.absolutePath);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        if (absolutePath != null) {
            return absolutePath;
        }
        return fileObject.toString();
    }
}
